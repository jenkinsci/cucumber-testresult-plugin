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

import hudson.model.AbstractBuild;
import hudson.tasks.test.MetaTabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

/**
 * A TagResult is a pseudo result to link scenarios with the same tag.
 * 
 * @author James Nord
 */
public class TagResult extends MetaTabulatedResult {

	private static final long serialVersionUID = -5418078481483188238L;

	private transient AbstractBuild<?, ?> owner;
	private transient String safeName;

	private Set<ScenarioResult> scenarioResults = new HashSet<ScenarioResult>();
	private transient List<ScenarioResult> failedScenarioResults;

	private String tagName;

	private int passCount;
	private int failCount;
	private int skipCount;
	private float duration;

	private CucumberTestResult parent;


	TagResult(String tagName) {
		this.tagName = tagName;
	}


	public String getDisplayName() {
		return getName();
	}


	public String getName() {
		return tagName;
	}


	@Override
	public Collection<ScenarioResult> getChildren() {
		return scenarioResults;
	}


	public Collection<ScenarioResult> getScenarioResults() {
		return scenarioResults;
	}


	@Override
	public String getChildTitle() {
		return "Cucumber Scenario";
	}


	@Override
	public boolean hasChildren() {
		return !scenarioResults.isEmpty();
	}


	@Override
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}


	public void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
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
		return failedScenarioResults;
	}


	public String getTagName() {
		return tagName;
	}


	void addScenarioResult(ScenarioResult scenarioResult) {
		scenarioResults.add(scenarioResult);
	}


	@Override
	public synchronized String getSafeName() {
		// no need to make unique as tags are shared!
		if (safeName != null) {
			return safeName;
		}
		safeName = safe(getName());
		return safeName;
	}


	@Override
	public void tally() {
		if (failedScenarioResults == null) {
			failedScenarioResults = new ArrayList<ScenarioResult>();
		}
		else {
			failedScenarioResults.clear();
		}
		passCount = 0;
		failCount = 0;
		skipCount = 0;
		duration = 0.0f;

		for (ScenarioResult sr : scenarioResults) {
			// ScenarioResult will have already been tallyed
			passCount += sr.getPassCount();
			failCount += sr.getFailCount();
			skipCount += sr.getSkipCount();
			duration += sr.getDuration();
			if (!sr.isPassed()) {
				failedScenarioResults.add(sr);
			}
		}
	}


	@Override
	public int getFailCount() {
		return failCount;
	}


	@Override
	public int getPassCount() {
		return passCount;
	}


	@Override
	public float getDuration() {
		return duration;
	}


	@Override
	public int getSkipCount() {
		// always zero?
		return skipCount;
	}

	@Override
	public int getTotalCount() {
		int retVal = super.getTotalCount();
		return retVal;
	}

	@Override
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {

		// if (token.equals(getId())) {
//			return this;
//		}
//		ScenarioResult result = scenariosByID.get(token);
//		if (result != null) {
//			return result;
//		}
//		else {
//			return super.getDynamic(token, req, rsp);
//		}
		return super.getDynamic(token, req, rsp);
	}

}
