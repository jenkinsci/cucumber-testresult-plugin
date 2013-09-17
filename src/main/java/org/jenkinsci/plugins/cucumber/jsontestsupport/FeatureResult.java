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

import gherkin.formatter.model.Feature;
import hudson.model.AbstractBuild;
import hudson.tasks.test.MetaTabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single Feature in Cucumber.
 * 
 * @author James Nord
 */
public class FeatureResult extends MetaTabulatedResult {

	private static final long serialVersionUID = 995206500596875310L;

	private Feature feature;
	private String uri;
	
	private List<ScenarioResult> scenarioResults = new ArrayList<ScenarioResult>();

	// TODO needs to be reset on loading from xStream
	private transient CucumberTestResult parent;

	FeatureResult(String uri, Feature feature) {
		this.uri = uri;
		this.feature = feature;
	}
	

	public String getDisplayName() {
		return "Cucumber Feature";
	}


	@Override
	public Collection<ScenarioResult> getChildren() {
		return scenarioResults;
	}


	@Override
	public String getChildTitle() {
		return "Cucumber Scenarios";
	}


	@Override
	public boolean hasChildren() {
		return !scenarioResults.isEmpty();
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public TestObject getParent() {
		return parent;
	}


	protected void setParent(CucumberTestResult parent) {
		this.parent = parent;
	}


	@Override
	public TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<ScenarioResult> getFailedTests() {
		ArrayList failedResults = new ArrayList<ScenarioResult>();
		// TODO implement me.
		/*
		 * ArrayList<ScenarioResult> failures; 
		 * for (ScenarioResult result : scenarioResults) { 
		 *   if
		 *     ScenarioResult... 
		 * } 
		 * return failedScenarioResults;
		 */
		return failedResults;
	}
	

	public String getURI() {
		return uri;
	}
	
	public Feature getFeature() {
		return feature;
	}
	
	void addScenarioResult(ScenarioResult scenarioResult) {
		scenarioResults.add(scenarioResult);
	}

}
