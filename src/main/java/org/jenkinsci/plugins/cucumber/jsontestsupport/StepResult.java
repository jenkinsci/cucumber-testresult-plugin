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

import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import hudson.model.AbstractBuild;
import hudson.tasks.test.TestResult;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberUtils.GherkinState;

/**
 * Represents a Step belonging to a Scenario from Cucumber.
 * 
 * @author James Nord
 */
public class StepResult extends TestResult {

	private Step step;
	private Match match;
	private Result result;

	private ScenarioResult parent;
	private transient AbstractBuild<?, ?> owner;


	StepResult(Step step, Match match, Result result) {
		this.step = step;
		this.match = match;
		this.result = result;
	}


	public String getDisplayName() {
		return "Cucumber Step result";
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}


	void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
	}


	@Override
	public ScenarioResult getParent() {
		return parent;
	}


	protected void setParent(ScenarioResult parent) {
		this.parent = parent;
	}


	@Override
	public TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public float getDuration() {
		return CucumberUtils.durationFromResult(result);
	}


	/**
	 * Gets the total number of passed tests.
	 */
	public int getPassCount() {
		return CucumberUtils.GherkinState.parseState(result.getStatus()).isPassedState() ? 1 : 0;
	}


	/**
	 * Gets the total number of failed tests.
	 */
	public int getFailCount() {
		return CucumberUtils.GherkinState.parseState(result.getStatus()).isFailureState() ? 1 : 0;
	}


	/**
	 * Gets the total number of skipped tests.
	 */
	public int getSkipCount() {
		return CucumberUtils.GherkinState.parseState(result.getStatus()).isSkippedState() ? 1 : 0;
	}


	Step getStep() {
		return step;
	}


	Match getMatch() {
		return match;
	}


	Result getResult() {
		return result;
	}

	public String getErrorMessage() {
		String retVal = "";
		GherkinState state = GherkinState.parseState(result.getStatus());
		switch (state) {
			case UNDEFINED:
				retVal = "Step \"" + step.getName() + "\" is undefined";
				break;
			case FAILED:
				retVal = result.getErrorMessage();
				break;
			case PASSED:
				//fallthrough
			case SKIPPED:
				retVal = "";
				break;
		}
		return retVal;
	}
}
