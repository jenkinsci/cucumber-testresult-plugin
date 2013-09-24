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

import gherkin.formatter.model.Background;
import hudson.model.AbstractBuild;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

/**
 * Represents a Background belonging to a Scenario.
 * Although this is a test Object as it is a background it is not intended for individual Display.
 * @author James Nord
 */
public class BackgroundResult extends TestResult {
	
	private Background background;
	private ArrayList<StepResult> stepResults = new ArrayList<StepResult>(); 
	
	private ScenarioResult parent;
	
	private transient AbstractBuild<?, ?> owner;
	
	/* Recomputed by a call to {@link CucumberTestResult#tally()} */
	// true if this test failed
	private transient boolean failed;
	private transient float duration;
	
	BackgroundResult(Background background) {
		this.background = background;
	}
	
	@Override
   public AbstractBuild<?, ?> getOwner() {
	   return owner;
   }

	void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
		for (StepResult sr : stepResults) {
			sr.setOwner(owner);
		}
	}
	
	@Override
	public String getName() {
		return "Cucumber Background"; 
	}
	
	@Override
	public int getFailCount() {
		return failed ? 1 : 0;
	}

	@Override
	public int getSkipCount() {
		return 0;
	}

	@Override
	public int getPassCount() {
		return failed ? 0 : 1;
	}

	


	@Override
	public ScenarioResult getParent() {
		return parent;
	}
	
	void setParent(ScenarioResult parent) {
		this.parent = parent;
	}

	@Override
	public float getDuration() {
	   return duration;
	}

	@Override
   public TestResult findCorrespondingResult(String id) {
	   // TODO Auto-generated method stub
	   return null;
   }

	public String getDisplayName() {
	   return "Background Result";
   }

	public Background getBackground() {
		return this.background;
	}
	
	void addStepResult(StepResult stepResult) {
		stepResults.add(stepResult);
	}
	
	
	Collection<StepResult> getStepResults() {
		return stepResults;
	}
	
	@Override
	public void tally() {
		duration = 0.0f;

		for (StepResult sr : stepResults) {
			sr.tally();
			if (sr.getFailCount() != 0) {
				failed = true;
			}
			duration += sr.getDuration();
		}
	}

}
