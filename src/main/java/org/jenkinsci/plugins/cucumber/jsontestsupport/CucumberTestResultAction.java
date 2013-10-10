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

import hudson.XmlFile;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.TestResult;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.HeapSpaceStringConverter;
import hudson.util.XStream2;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.Exported;

import com.thoughtworks.xstream.XStream;

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
public class CucumberTestResultAction extends AbstractTestResultAction<CucumberTestResultAction> implements StaplerProxy {

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


	
	public CucumberTestResultAction(AbstractBuild owner, CucumberTestResult result, BuildListener listener) {
		super(owner);
		setResult(result, listener);
	}
	
   /**
    * Overwrites the {@link CucumberTestResult} by a new data set.
    */
   public synchronized void setResult(CucumberTestResult result, BuildListener listener) {
       
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
      return new XmlFile(XSTREAM,new File(owner.getRootDir(), "cucumberResult.xml"));
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
       r.setOwner(this.owner);
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

}
