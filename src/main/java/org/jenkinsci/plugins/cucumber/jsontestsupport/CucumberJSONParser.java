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

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;
import hudson.tasks.test.TestResultParser;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

/**
 * Parser that understands Cucumbers <a href="http://cukes.info/reports.html#json">JSON</a> notation and will
 * generate {@link hudson.tasks.test.TestResult} so that Jenkins will display the results.
 */
@Extension
public class CucumberJSONParser extends TestResultParser {

	private static final Logger logger = Logger.getLogger(CucumberJSONParser.class.getName());


	public CucumberJSONParser() {
	}


	@Override
	public String getDisplayName() {
		return "Cucumber JSON parser";
	}


	@Override
	public TestResult parse(String testResultLocations,
	                        AbstractBuild build,
	                        Launcher launcher,
	                        TaskListener listener) throws InterruptedException, IOException {
		logger.log(Level.FINE, "parse({}, {}#{}, launcher, listener)", new Object[] {
		                                                                             testResultLocations,
		                                                                             build.getProject()
		                                                                                  .getFullName(),
		                                                                             build.getNumber()});
		// used so we don't parse files that where not modified during this build
		// (ie the test result is left over from a previous run)
		final long buildTime = build.getTimestamp().getTimeInMillis();
		final long timeOnMaster = System.currentTimeMillis();

		FilePath workspace = build.getWorkspace();
		if (workspace == null) {
			throw new AbortException("No workspace for build ( + " + build.getProject().getFullName() + '#'
			                         + build.getNumber() + ")");
		}
		return workspace.act(new ParseResultCallable(testResultLocations, buildTime, timeOnMaster));
	}




	private static final class ParseResultCallable implements FilePath.FileCallable<TestResult> {

		private final String testResults;
		private final long buildTime;
		private final long timeOnMaster;


		private ParseResultCallable(String testResults, long buildTime, long timeOnMaster) {
			this.testResults = testResults;
			this.buildTime = buildTime;
			this.timeOnMaster = timeOnMaster;
		}


		public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
			final long timeOnSlave = System.currentTimeMillis();

			FileSet fs = Util.createFileSet(ws, testResults);
			DirectoryScanner ds = fs.getDirectoryScanner();

			String[] files = ds.getIncludedFiles();
			if (files.length == 0) {
				// no test result. Most likely a configuration
				// error or fatal problem
				throw new AbortException("No JSON files found");
			}

			TestResult result = new TestResult(buildTime + (timeOnSlave - timeOnMaster), ds);
			return result;
		}
	}
}
