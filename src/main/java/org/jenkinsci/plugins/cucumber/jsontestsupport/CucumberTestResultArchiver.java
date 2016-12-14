/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Martin Eigenbrodt,
 * Tom Huybrechts, Yahoo!, Inc., Richard Hierlmeier
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.cucumber.jsontestsupport;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.*;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.FormValidation;
import jenkins.security.MasterToSlaveCallable;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.cucumber.jsontestsupport.rerun.CucumberRerun1TestResultAction;
import org.jenkinsci.plugins.cucumber.jsontestsupport.rerun.CucumberRerun2TestResultAction;
import org.kohsuke.stapler.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Generates HTML report from Cucumber JSON files.
 * 
 * @author James Nord
 * @author Kohsuke Kawaguchi (original JUnit code)
 */
public class CucumberTestResultArchiver extends Recorder implements MatrixAggregatable, SimpleBuildStep {
	private static final Logger LOGGER = Logger.getLogger(CucumberTestResultArchiver.class.getName());

	/**
	 * {@link FileSet} "includes" string, like "foo/bar/*.xml"
	 */
	private final String testResults;

	private boolean ignoreBadSteps;

	private boolean ignoreDiffTracking;

	@DataBoundConstructor
	public CucumberTestResultArchiver(String testResults) {
		this.testResults = testResults;
	}

	public CucumberTestResultArchiver(String testResults, boolean ignoreBadSteps, boolean ignoreDiffTracking){
		this(testResults);
		setIgnoreBadSteps(ignoreBadSteps);
		setIgnoreDiffTracking(ignoreDiffTracking);
	}

	@DataBoundSetter
	public void setIgnoreBadSteps(boolean ignoreBadSteps){
		this.ignoreBadSteps = ignoreBadSteps;
	}

	public boolean getIgnoreBadSteps(){
		return ignoreBadSteps;
	}

	@DataBoundSetter
	public void setIgnoreDiffTracking(boolean ignoreDiffTracking){
		this.ignoreDiffTracking = ignoreDiffTracking;
	}

	public boolean getIgnoreDiffTracking(){
		return ignoreDiffTracking;
	}

    @Override
    @SuppressFBWarnings(value={"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification="whatever")
    public boolean
    perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException,
            IOException {
        return publishReport(build, build.getWorkspace(), launcher, listener);
    }


	@Override
	public void perform(Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener taskListener) throws InterruptedException, IOException {
		publishReport(run, filePath, launcher, taskListener);
	}

	@SuppressFBWarnings(value={"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE"}, justification="move to java.nio for file stuff")
	public boolean
	      publishReport(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException,
	                                                                             IOException {
		// listener.getLogger().println(Messages.JUnitResultArchiver_Recording());

		CucumberTestResultAction action;

		final String _testResults = build.getEnvironment(listener).expand(this.testResults);

		CucumberJSONParser parser = new CucumberJSONParser(ignoreBadSteps);

		CucumberTestResult result = parser.parseResult(_testResults, build, workspace, launcher, listener);

		// TODO - look at all of the Scenarios and see if there are any embedded items contained with in them
		String remoteTempDir = launcher.getChannel().call(new TmpDirCallable());

		// if so we need to copy them to the master.
		for (FeatureResult f : result.getFeatures()) {
			for (ScenarioResult s : f.getScenarioResults()) {
				for (EmbeddedItem item : s.getEmbeddedItems()) {
					// this is the wrong place to do the copying...
					// XXX Need to do something with MasterToSlaveCallable to makesure we are safe from evil
					// injection
					FilePath srcFilePath = new FilePath(launcher.getChannel(), remoteTempDir + '/' + item.getFilename());
					// XXX when we support the workflow we will need to make sure that these files do not clash....
					File destRoot = new File(build.getRootDir(), "/cucumber/embed/" + f.getSafeName() + '/' + s
							.getSafeName() + '/');
					destRoot.mkdirs();
					File destFile = new File(destRoot, item.getFilename());
					if (!destFile.getAbsolutePath().startsWith(destRoot.getAbsolutePath())) {
						// someone is trying to trick us into writing abitrary files...
						throw new IOException("Exploit attempt detected - Build attempted to write to " +
								destFile.getAbsolutePath());
					}
					FilePath destFilePath = new FilePath(destFile);
					srcFilePath.copyTo(destFilePath);
					srcFilePath.delete();
				}
			}
		}


		action = build.getAction(CucumberTestResultAction.class);
		if (action == null) {
			action = new CucumberTestResultAction(build, result, listener);
			if (!ignoreDiffTracking) {
				CHECKPOINT.block();
				CHECKPOINT.report();
			}
		} else {
			if (!ignoreDiffTracking) {
				CHECKPOINT.block();
			}
			action.mergeResult(result, listener);
			build.save();
			if (!ignoreDiffTracking) {
				CHECKPOINT.report();
			}
		}

		if (result.getPassCount() == 0 && result.getFailCount() == 0 && result.getSkipCount() == 0)
			throw new AbortException("No cucumber scenarios appear to have been run.");

		if (action.getResult().getTotalCount() == action.getResult().getFailCount()){
			build.setResult(Result.FAILURE);
		} else if (action.getResult().getFailCount() > 0) {
			build.setResult(Result.UNSTABLE);
		}

		String rerun1Path = filterFileThatContains(workspace,_testResults,"rerun1");
		if(!Strings.isNullOrEmpty(rerun1Path)) {
			CucumberTestResult rerun1Result = parser.parseResult(rerun1Path, build, workspace, launcher, listener);
			rerun1Result.setNameAppendix("Rerun 1");
			CucumberRerun1TestResultAction rerun1Action = build.getAction(CucumberRerun1TestResultAction.class);
			if (rerun1Action == null) {
				rerun1Action = new CucumberRerun1TestResultAction(build, rerun1Result, listener);
				if (!ignoreDiffTracking) {
					CHECKPOINT.block();
					CHECKPOINT.report();
				}
			} else {
				if (!ignoreDiffTracking) {
					CHECKPOINT.block();
				}
				rerun1Action.mergeResult(rerun1Result, listener);
				build.save();
				if (!ignoreDiffTracking) {
					CHECKPOINT.report();
				}
			}
		}

		String rerun2Path = filterFileThatContains(workspace,_testResults,"rerun2");
		if(!Strings.isNullOrEmpty(rerun2Path)) {
			CucumberTestResult rerun2Result = parser.parseResult(rerun2Path, build, workspace, launcher, listener);
			rerun2Result.setNameAppendix("Rerun 2");
			CucumberRerun2TestResultAction rerun2Action = build.getAction(CucumberRerun2TestResultAction.class);
			if (rerun2Action == null) {
				rerun2Action = new CucumberRerun2TestResultAction(build, rerun2Result, listener);
				if (!ignoreDiffTracking) {
					CHECKPOINT.block();
					CHECKPOINT.report();
				}
			} else {
				if (!ignoreDiffTracking) {
					CHECKPOINT.block();
				}
				rerun2Action.mergeResult(rerun2Result, listener);
				build.save();
				if (!ignoreDiffTracking) {
					CHECKPOINT.report();
				}
			}
		}
		return true;
	}

	private String filterFileThatContains(FilePath workspace, String _testResults, String filePathPart) throws IOException, InterruptedException {
		FilePath[] paths = workspace.list(_testResults);
		for(FilePath filePath : paths){
			String remote = filePath.getRemote();
			int index = remote.indexOf(filePathPart);
			if(index > 0){
				return "**/"+ remote.substring(index);
			}
		}
		return "";
	}


	/**
	 * This class does explicit checkpointing.
	 */
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}


	public String getTestResults() {
		return testResults;
	}


	@Override
	public Collection<Action> getProjectActions(AbstractProject<?, ?> project) {
		return Collections.<Action> singleton(new TestResultProjectAction((Job)project));
	}
	

	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
		return new TestResultAggregator(build, launcher, listener);
	}

	/**
	 * Test result tracks the diff from the previous run, hence the checkpoint.
	 */
	private static final CheckPoint CHECKPOINT = new CheckPoint("Cucumber result archiving");

	private static final long serialVersionUID = 1L;


	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}


	/**
	 * {@link Callable} that gets the temporary directory from the node. 
	 */
	private final static class TmpDirCallable extends MasterToSlaveCallable<String, InterruptedException> {

		private static final long serialVersionUID = 1L;

		@Override
		public String call() {
			return System.getProperty("java.io.tmpdir");
		}
	}



	@Extension
	@Symbol("cucumber")
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public String getDisplayName() {
			return "Publish Cucumber test result report - custom";
		}

		@Override
		public Publisher
		      newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
			String testResults = formData.getString("testResults");
			boolean ignoreBadSteps = formData.getBoolean("ignoreBadSteps");
			boolean ignoreDiffTracking = formData.getBoolean("ignoreDiffTracking");
			LOGGER.fine("ignoreBadSteps = "+ ignoreBadSteps);
			LOGGER.fine("ignoreDiffTracking ="+ ignoreDiffTracking);
			return new CucumberTestResultArchiver(testResults, ignoreBadSteps, ignoreDiffTracking);
		}


		/**
		 * Performs on-the-fly validation on the file mask wildcard.
		 */
		public FormValidation doCheckTestResults(@AncestorInPath AbstractProject project,
		                                         @QueryParameter String value) throws IOException {
			if (project != null) {
				return FilePath.validateFileMask(project.getSomeWorkspace(), value);
			}
			return FormValidation.ok(); 
		}


		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
	}

}
