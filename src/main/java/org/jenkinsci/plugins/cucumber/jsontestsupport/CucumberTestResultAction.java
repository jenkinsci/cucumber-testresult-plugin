/*
 * The MIT License
 *
 * Copyright (c) 2013, Cisco Systems, Inc., a California corporation
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

import hudson.Util;
import hudson.XmlFile;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;

import com.thoughtworks.xstream.XStream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * {@link Action} that displays the Cucumber test result.
 *
 * <p>
 * The actual test reports are isolated by {@link WeakReference}
 * so that it doesn't eat up too much memory.
 *
 * @author James Nord
 * @author Kohsuke Kawaguchi (original junit support)
 */
@SuppressFBWarnings(value={"UG_SYNC_SET_UNSYNC_GET"}, justification="the getter and setter are both synchronized")
public class CucumberTestResultAction extends AbstractTestResultAction<CucumberTestResultAction> implements StaplerProxy, LastBuildAction {

   private static final Logger LOGGER = Logger.getLogger(CucumberTestResultAction.class.getName());

   private static final XStream XSTREAM = new XStream2();

   private transient WeakReference<CucumberTestResult> result;
   
   private int totalCount = -1;
	private int failCount = -1;
	private int skipCount = -1;

	static {
     XSTREAM.alias("result",CucumberTestResult.class);
      //XSTREAM.alias("suite",SuiteResult.class);
      //XSTREAM.alias("case",CaseResult.class);
      //XSTREAM.registerConverter(new HeapSpaceStringConverter(),100);
      
       XSTREAM.registerConverter(new HeapSpaceStringConverter(),100);
   }


	
	public CucumberTestResultAction(Run<?, ?> owner, CucumberTestResult result, TaskListener listener) {
		super();
		owner.addAction(this);
		setResult(result, listener);
	}
	
   /**
    * Overwrites the {@link CucumberTestResult} by a new data set.
    */
   public synchronized void setResult(CucumberTestResult result, TaskListener listener) {
       
       totalCount = result.getTotalCount();
       failCount = result.getFailCount();
       skipCount = result.getSkipCount();

       // persist the data
       try {
           getDataFile().write(result);
       } catch (IOException ex) {
           ex.printStackTrace(listener.fatalError("Failed to save the Cucumber test result."));
           LOGGER.log(Level.WARNING, "Failed to save the Cucumber test result.", ex);
       }

       this.result = new WeakReference<CucumberTestResult>(result);
   }
	
   private XmlFile getDataFile() {
      return new XmlFile(XSTREAM,new File(run.getRootDir(), "cucumberResult.xml"));
  }

   /**
    * Loads a {@link TestResult} from disk.
    */
   private CucumberTestResult load() {
   	CucumberTestResult r;
       try {
           r = (CucumberTestResult)getDataFile().read();
       } catch (IOException e) {
           LOGGER.log(Level.WARNING, "Failed to load " + getDataFile(), e);
           r = new CucumberTestResult(); // return a dummy
       }
       r.tally();
       r.setOwner(this.run);
       return r;
   }
   
	@Override
   @Exported(visibility = 2)
   public int getFailCount() {
		return failCount;
	}

	@Override
   @Exported(visibility = 2)
   public int getTotalCount() {
		return totalCount;
	}

	@Override
	@Exported(visibility = 2)
   public int getSkipCount() {
		return skipCount;
	}
	

	@Override
	@Exported(visibility = 5)
	public synchronized CucumberTestResult getResult() {
		CucumberTestResult r;
		if (result == null) {
			r = load();
			result = new WeakReference<CucumberTestResult>(r);
		}
		else {
			r = result.get();
		}

		if (r == null) {
			r = load();
			result = new WeakReference<CucumberTestResult>(r);
		}

		if (totalCount == -1) {
			totalCount = r.getTotalCount();
			failCount = r.getFailCount();
			skipCount = r.getSkipCount();
		}
		return r;
	}
	
// Can't do this as AbstractTestResult is not generic!!!
//	@Override
//	public Collection<ScenarioResult> getFailedTests() {
//		return getResult().getFailedTests();
//	};
	
	public Object getTarget() {
	   return getResult();
   }


   @Override
    public String getDisplayName() {
       return "Cucumber Test Result";
   }

   @Override
    public  String getUrlName() {
       return "cucumberTestReport";
   }

	/**
	 * Merge results from other into an existing set of results.
	 * @param other
	 *           the result to merge with the current results.
	 * @param listener
	 */
	synchronized void mergeResult(CucumberTestResult other, TaskListener listener) {
		CucumberTestResult cr = getResult();
		for (FeatureResult fr : other.getFeatures()) {
			// We need to add =the new results to the existing ones to keep the names stable
			// otherwise any embedded items will be attached to the wrong result
			// XXX this has the potential to cause a concurrentModificationException or other bad issues if someone is getting all the features...
			cr.addFeatureResult(fr);
		}
		//cr.tally();
		// XXX Do we need to add TagResults or call tally()?
		// persist the new result to disk
		this.setResult(cr, listener);
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		// TODO use our own action to not conflict with junit
		Job<?,?> job = run.getParent();
		if (/* getAction(Class) produces a StackOverflowError */!Util.filter(job.getActions(), TestResultProjectAction.class).isEmpty()) {
			// JENKINS-26077: someone like XUnitPublisher already added one
			return Collections.emptySet();
		}
		return Collections.singleton(new TestResultProjectAction(job));
	}
}
