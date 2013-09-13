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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.cucumber.jsontestsupport.utils.NullAppender;

import gherkin.JSONParser;
import gherkin.deps.com.google.gson.Gson;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Feature;
import hudson.model.AbstractBuild;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

/**
 * Represents a single Feature in Cucumber.
 * 
 * @author James Nord
 */
public class FeatureResult extends TabulatedResult {

	/**
	 * Construct a new FeatureResult with the data contained in the specified File.
	 * 
	 * @param jsonFile the json output from a single cucumber feature
	 */
	public FeatureResult(Feature feature) {
		handleFeature(feature);
	}


	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<? extends TestResult> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
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
	public TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	
	protected void handleFature(Feature feature) {
		// do something with the feature!
	}
}
