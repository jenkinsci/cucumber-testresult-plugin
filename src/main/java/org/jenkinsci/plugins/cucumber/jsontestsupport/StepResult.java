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
import hudson.model.Run;
import hudson.tasks.test.TestResult;

/**
 * Represents a Step belonging to a Scenario from Cucumber.
 * 
 * @author James Nord
 */
public class StepResult extends TestResult {

	private static final long serialVersionUID = 1L;

	private Step step;
	private Match match;
	private Result result;

	private ScenarioResult parent;
	private transient Run<?, ?> owner;


	StepResult(Step step, Match match, Result result) {
		this.step = step;
		this.match = match;
		this.result = result;
	}


	public String getDisplayName() {
		return "Cucumber Step result";
	}


	@Override
	public Run<?, ?> getRun() {
		return owner;
	}


	void setOwner(Run<?, ?> owner) {
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
		return CucumberUtils.PASSED_TEST_STRING.equals(result.getStatus()) ? 1 : 0;
	}


	/**
	 * Gets the total number of failed tests.
	 */
	public int getFailCount() {
		if (CucumberUtils.FAILED_TEST_STRING.equals(result.getStatus())
			    || CucumberUtils.UNDEFINED_TEST_STRING.equals(result.getStatus())) {
			return 1;
		}
		return 0;
	}


	/**
	 * Gets the total number of skipped tests.
	 */
	public int getSkipCount() {
		if (CucumberUtils.SKIPPED_TEST_STRING.equals(result.getStatus())
		    || CucumberUtils.PENDING_TEST_STRING.equals(result.getStatus())) {
			return 1;
		}
		return 0;
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
}
