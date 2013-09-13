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

import gherkin.deps.com.google.gson.Gson;
import gherkin.formatter.model.Feature;
import hudson.model.AbstractBuild;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;

/**
 * Represents all the Features from Cucumber.
 * 
 * @author James Nord
 */
public class TestResult extends TabulatedResult {

	private List<FeatureResult> featureResults = new ArrayList<FeatureResult>();
	
	public TestResult(long epoch, DirectoryScanner scanner) throws IOException {
		parse(epoch, scanner);
	}


	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<? extends hudson.tasks.test.TestResult> getChildren() {
		return featureResults;
	}


	@Override
	public boolean hasChildren() {
		return !featureResults.isEmpty();
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public TestObject getParent() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public hudson.tasks.test.TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Parse the results filtering out any that are older than epoch.
	 * 
	 * @param epoch the Time of which any reports are considered to be from previous builds.
	 * @param results the scanner containing the files to parse.
	 * @throws IOException if we could not parse the jso report
	 */
	protected void parse(long epoch, DirectoryScanner results) throws IOException {
		String[] reports = results.getIncludedFiles();
		File baseDir = results.getBasedir();
		for (String report : reports) {
			File reportFile = new File(baseDir, report);
			// only parse files that were actually updated during this build (with a fudge factor)
			if (epoch - 2000 <= reportFile.lastModified()) {
				try {
					Feature[] features = parse(reportFile);
					for (Feature f : features) {
						FeatureResult fr = new FeatureResult(f);
						featureResults.add(fr);
					}
				}
				catch (IOException ioEx) {
					throw new IOException("Failed to parse " + reportFile, ioEx);
				}
			}
		}
	}

	/**
	 * Parse the specified json cucumber report.
	 * @param jsonFile the file to read containing cucumber/gherkin json format output.
	 * @throws IOException if we could not read the file.
	 * @return The features contained in the jsonFile.
	 */
	protected Feature[] parse(File jsonFile) throws IOException {
		
		// the JSON file will contain scenarios and each of those will contain features...
		FileInputStream fis = new FileInputStream(jsonFile);

		// We are using the platform default encoding.
		// note: the build job may be use arbitrary charset to write the file
		// so we should expose this as a configurable option at some point.
		InputStreamReader isr = new InputStreamReader(fis);
		
		BufferedReader br = new BufferedReader(isr, 1024 * 4); // 1 block on an SSD
		
		try {
			Gson gson = new Gson();
			Feature[] features = gson.fromJson(br, Feature[].class);
			return features;
		}
		finally {
			br.close();
		}
	}
	
}
