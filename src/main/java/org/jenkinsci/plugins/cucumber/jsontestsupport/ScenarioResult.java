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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import hudson.model.AbstractBuild;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

/**
 * Represents a Scenario belonging to a Feature from Cucumber.
 * 
 * @author James Nord
 */
public class ScenarioResult extends TestResult {

	private Scenario scenario;

	private List<StepResult> steps = new ArrayList<StepResult>();

	/** Possibly empty list of code executed before the Scenario. */
	private List<BeforeAfterResult> beforeResults = new ArrayList<BeforeAfterResult>();
	/** Possibly <code>null</code> Background executed before the Scenario. */
	private BackgroundResult backgroundResult = null;
	/** Possibly empty list of code executed before the Scenario. */
	private List<BeforeAfterResult> afterResults = new ArrayList<BeforeAfterResult>();

	private transient FeatureResult parent;

	// true if this test was skipped
	private boolean skipped;
	// true if this test failed
	private boolean failed;


	ScenarioResult(Scenario scenario, BackgroundResult backgroundResult) {
		this.scenario = scenario;
		this.backgroundResult = backgroundResult;
	}


	@Override
	public String getName() {
		return "Cucumber Scenario";
	}


	@Override
	public int getFailCount() {
		return failed ? 1 : 0;
	}


	@Override
	public int getSkipCount() {
		return skipped ? 1 : 0;
	}


	@Override
	public int getPassCount() {
		return (!failed && !skipped) ? 1 : 0;
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public FeatureResult getParent() {
		return parent;
	}


	protected void setParent(FeatureResult parent) {
		this.parent = parent;
	}


	@Override
	public TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDisplayName() {
		return "Cucumber Scenario";
	}


	public BackgroundResult getBackgroundResult() {
		return backgroundResult;
	}


	public List<BeforeAfterResult> getAfterResults() {
		return afterResults;
	}


	void addAfterResult(BeforeAfterResult afterResult) {
		afterResults.add(afterResult);
	}


	public List<BeforeAfterResult> getBeforeResults() {
		return beforeResults;
	}


	void addBeforeResult(BeforeAfterResult beforeResult) {
		beforeResults.add(beforeResult);
	}


	void addStepResult(StepResult stepResult) {
		steps.add(stepResult);
	}
	
	public Scenario getScenario() {
		return scenario;
	}
}
