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

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.CheckPoint;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.remoting.Callable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.test.TestResultAggregator;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.FormValidation;

import jenkins.MasterToSlaveFileCallable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import net.sf.json.JSONObject;

import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Generates HTML report from Cucumber JSON files.
 * 
 * @author James Nord
 * @author Kohsuke Kawaguchi (original JUnit code)
 */
public class CucumberTestResultArchiver extends Recorder implements MatrixAggregatable {

	/**
	 * {@link FileSet} "includes" string, like "foo/bar/*.xml"
	 */
	private final String testResults;


	@DataBoundConstructor
	public CucumberTestResultArchiver(String testResults) {
		this.testResults = testResults;
	}


	@Override
	public boolean
	      perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException,
	                                                                             IOException {
		// listener.getLogger().println(Messages.JUnitResultArchiver_Recording());

		CucumberTestResultAction action;

		final String _testResults = build.getEnvironment(listener).expand(this.testResults);

		try {
			CucumberJSONParser parser = new CucumberJSONParser();

			CucumberTestResult result = parser.parse(_testResults, build, launcher, listener);

			// TODO - look at all of the Scenarios and see if there are any embedded items contained with in them
			String remoteTempDir = launcher.getChannel().call(new Callable<String, InterruptedException >() {
				@Override
				public String call() throws InterruptedException {
					return System.getProperty("java.io.tmpdir");
				}
			});

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
			
			action = new CucumberTestResultAction(build, result, listener);

			if (result.getPassCount() == 0 && result.getFailCount() == 0 && result.getSkipCount() == 0)
				throw new AbortException("No cucumber scenarios appear to have been run.");

			CHECKPOINT.block();

		}
		catch (AbortException e) {
			if (build.getResult() == Result.FAILURE) {
				// most likely a build failed before it gets to the test phase.
				// don't report confusing error message.
				return true;
			}
			listener.getLogger().println(e.getMessage());
			build.setResult(Result.FAILURE);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace(listener.error("Failed to archive cucumber reports"));
			build.setResult(Result.FAILURE);
			return true;
		}

		build.getActions().add(action);
		CHECKPOINT.report();

		if (action.getResult().getFailCount() > 0)
			build.setResult(Result.UNSTABLE);

		return true;
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
		return Collections.<Action> singleton(new TestResultProjectAction(project));
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



	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public String getDisplayName() {
			return "Publish Cucumber test result report";
		}


		@Override
		public String getHelpFile() {
			return "/help/tasks/cucumber/report.html";
		}


		@Override
		public Publisher
		      newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
			String testResults = formData.getString("testResults");
			return new CucumberTestResultArchiver(testResults);
		}


		/**
		 * Performs on-the-fly validation on the file mask wildcard.
		 */
		public FormValidation doCheckTestResults(@AncestorInPath AbstractProject project,
		                                         @QueryParameter String value) throws IOException {
			return FilePath.validateFileMask(project.getSomeWorkspace(), value);
		}


		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
	}
}
