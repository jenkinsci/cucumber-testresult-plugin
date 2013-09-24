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

import gherkin.JSONParser;
import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.tasks.test.DefaultTestResultParserImpl;
import hudson.tasks.test.TestResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

/**
 * Parser that understands Cucumbers <a href="http://cukes.info/reports.html#json">JSON</a> notation and will
 * generate {@link hudson.tasks.test.TestResult} so that Jenkins will display the results.
 */
@Extension
public class CucumberJSONParser extends DefaultTestResultParserImpl {

	private static final Logger logger = Logger.getLogger(CucumberJSONParser.class.getName());


	public CucumberJSONParser() {
	}


	@Override
	public String getDisplayName() {
		return "Cucumber JSON parser";
	}

   /**
    * This method is executed on the slave that has the report files to parse test reports and builds {@link TestResult}.
    *
    * @param reportFiles
    *      List of files to be parsed. Never be empty nor null.
    * @param launcher
    *      Can be used to fork processes on the machine where the build is running. Never null.
    * @param listener
    *      Use this to report progress and other problems. Never null.
    *
    * @throws InterruptedException
    *      If the user cancels the build, it will be received as a thread interruption. Do not catch
    *      it, and instead just forward that through the call stack.
    * @throws IOException
    *      If you don't care about handling exceptions gracefully, you can just throw IOException
    *      and let the default exception handling in Hudson takes care of it.
    * @throws AbortException
    *      If you encounter an error that you handled gracefully, throw this exception and Hudson
    *      will not show a stack trace.
    */
	@Override
   protected CucumberTestResult parse(List<File> reportFiles, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		
		CucumberTestResult result = new CucumberTestResult();
		GherkinCallback callback = new GherkinCallback(result);
		listener.getLogger().println("[Cucumber Tests] Parsing results.");
		JSONParser jsonParser = new JSONParser(callback, callback);
		
		try {
			for (File f : reportFiles) {
				String s = FileUtils.readFileToString(f, "UTF-8");
				// if no scenarios where executed for a feature then a json file may still exist.
				if (s.isEmpty()) {
					listener.getLogger().println("[Cucumber Tests] ignoring empty file (" + f.getName() + ")");
				}
				else {listener.getLogger().println("[Cucumber Tests] parsing " + f.getName());
					jsonParser.parse(s);
				}
			}
		}
		catch (CucumberModelException ccm) {
			throw new IOException("Failed to parse Cucumber JSON", ccm);
		}
		finally {
			// even though this is a noop prevent an eclipse warning.
			callback.close();
		}
		result.tally();
		return result;
	}


	@Override
	public CucumberTestResult parse(final String testResultLocations,
	                        final AbstractBuild build,
	                        final Launcher launcher,
	                        final TaskListener listener) throws InterruptedException, IOException {
		// overridden so we tally and set the owner on the master.  for some reason this worked on a smaller setup but not on a larger setup?
		CucumberTestResult result = (CucumberTestResult) super.parse(testResultLocations, build, launcher, listener);
		result.tally();
		result.setOwner(build);
		return result;
	}
}
