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

import gherkin.formatter.model.Scenario;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.CaseResult.Status;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents a Scenario belonging to a Feature from Cucumber.
 * 
 * @author James Nord
 */
@ExportedBean
public class ScenarioResult extends TestResult {

	private static final long serialVersionUID = 6813769160332278223L;

	private static final Logger LOGGER = Logger.getLogger(ScenarioResult.class.getName());

	private Scenario scenario;

	private List<StepResult> steps = new ArrayList<StepResult>();

	/** Possibly empty list of code executed before the Scenario. */
	private List<BeforeAfterResult> beforeResults = new ArrayList<BeforeAfterResult>();
	/** Possibly <code>null</code> Background executed before the Scenario. */
	private BackgroundResult backgroundResult = null;
	/** Possibly empty list of code executed before the Scenario. */
	private List<BeforeAfterResult> afterResults = new ArrayList<BeforeAfterResult>();

	private FeatureResult parent;
	
	private transient AbstractBuild<?, ?> owner;
	private transient String safeName;

	// true if this test failed
	private transient boolean failed;
	private transient float duration;

   /**
    * This test has been failing since this build number (not id.)
    *
    * If {@link #isPassed() passing}, this field is left unused to 0.
    */
   private int failedSince;
   
	
	ScenarioResult(Scenario scenario, BackgroundResult backgroundResult) {
		this.scenario = scenario;
		this.backgroundResult = backgroundResult;
	}

	@Override
	@Exported(visibility=9)
	public String getName() {
		return scenario.getName();
	}
	
	// XXX: getFullName was added in 1.594+
	// when we bump core this should be tagged as an override.
	/* @Override */
	public String getFullName() {
		return getParent().getName() + " \u01c2 " + getName();
	}

	/*
	 * Whilst a ScenarioResult contains a TestResult we do not count those individually. That would be akin to
	 * reporting each JUnit Assert as a test.
	 */
	@Override
	public int getFailCount() {
		return (failed ? 1 : 0);
	}

	@Override
	public synchronized String getSafeName() {
		if (safeName != null) {
			return safeName;
		}
		String name = safe(scenario.getId());
		String parentName = parent.getSafeName() + ';';
		
		if (name.startsWith(parentName)) {
			name = name.replace(parentName, "");
		}
		safeName = uniquifyName(parent.getChildren(), name);
		return safeName;
	}
	
	@Override
	public int getSkipCount() {
		return 0;
	}


	@Override
	@Exported(visibility=9)
	public int getPassCount() {
		return (failed ? 0 : 1);
	}


	
	@Override
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}


	public void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
		for (BeforeAfterResult bar : beforeResults) {
			bar.setOwner(owner);
		}
		for (BeforeAfterResult bar : afterResults) {
			bar.setOwner(owner);
		}
		for (StepResult sr : steps) {
			sr.setOwner(owner);
		}
		if (backgroundResult != null) {
			backgroundResult.setOwner(owner);
		}
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
		// we have no children so it is either us or null
		if (id.equals(getId())) {
			return this;
		}
		return null;
	}


	public String getDisplayName() {
		return getName();
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

	public Collection<StepResult> getStepResults() {
		return steps;
	}
	
	public Scenario getScenario() {
		return scenario;
	}

	@Override
	@Exported(visibility=9)
	public float getDuration() {
		return duration;
	}
	

	@Exported(name = "status", visibility = 9)
	// stapler strips the trailing 's'
	public Status getStatus() {
		if (getSkipCount() > 0) {
			// cucumber doesn't report skipped scenarios
			return Status.SKIPPED;
		}
		ScenarioResult psr = (ScenarioResult) getPreviousResult();
		if (psr == null) {
			return isPassed() ? Status.PASSED : Status.FAILED;
		}
		if (psr.isPassed()) {
			return isPassed() ? Status.PASSED : Status.REGRESSION;
		}
		else {
			return isPassed() ? Status.FIXED : Status.FAILED;
		}
	}


	/**
	 * If this test failed, then return the build number when this test started failing.
	 */
	@Override
	@Exported(visibility = 9)
	public int getFailedSince() {
		// If we haven't calculated failedSince yet, and we should,
		// do it now.
		if (failedSince == 0 && getFailCount() == 1) {
			ScenarioResult prev = (ScenarioResult) getPreviousResult();
			if (prev != null && !prev.isPassed())
				this.failedSince = prev.failedSince;
			else if (getOwner() != null) {
				this.failedSince = getOwner().getNumber();
			}
			else {
				LOGGER.warning("Can not calculate failed since. we have a previous result but no owner.");
				// failedSince will be 0, which isn't correct.
			}
		}
		return failedSince;
	}

	/**
	 * Gets the number of consecutive builds (including this) that this test case has been failing.
	 */
	@Exported(visibility = 9)
	public int getAge() {
		if (isPassed())
			return 0;
		else if (getOwner() != null) {
			return getOwner().getNumber() - getFailedSince() + 1;
		}
		else {
			LOGGER.fine("Trying to get age of a ScenarioResult without an owner");
			return 0;
		}
	}


	@Override
	public void tally() {
		failed = false;
		duration = 0.0f;
		for (StepResult sr : steps) {
			duration += sr.getDuration();
			if (sr.getFailCount() != 0) {
				failed = true;
			}
		}
		
		if (backgroundResult != null) {
			backgroundResult.tally();
			duration += backgroundResult.getDuration();
			if (backgroundResult.getFailCount() != 0) {
				failed = true;
			}
		}
		for (BeforeAfterResult bar : beforeResults) {
			duration += bar.getDuration();
			if (bar.getFailCount() != 0) {
				failed = true;
			}
		}
		for (BeforeAfterResult bar : afterResults) {
			duration += bar.getDuration();
			if (bar.getFailCount() != 0) {
				failed = true;
			}
		}
	}


	/**
	 * If there was an error or a failure, this is the text from the message.
	 */
	public String getErrorDetails() {
		if (!isPassed()) {
			for (BeforeAfterResult before : getBeforeResults()) {
				if (!before.isPassed()) {
					return before.getResult().getErrorMessage();
				}
			}
			for (StepResult step : getStepResults()) {
				if (!step.isPassed()) {
					return step.getResult().getErrorMessage();
				}
			}
			for (BeforeAfterResult after : getAfterResults()) {
				if (!after.isPassed()) {
					return after.getResult().getErrorMessage();
				}
			}
		}
		return null;
	}


	public String getSource() {
		return ScenarioToHTML.getHTML(this); 
	}
	
	@Override
	// Takes into account that this can be reached from a TagResult as well as a FeatureResult. 
	public String getRelativePathFrom(TestObject from) {
		if (from == this) {
			return ".";
		}
		
		String path = _getRelativePathFrom(from, this);
		if (path == null) {
			// try our parent as we could be coming indirectly from a tag not a Feature
			path = _getRelativePathFrom(from.getParent(), this);
			if (path != null) {
				path = "../" + path;
			}
		}
		if (path != null) {
			return path;
		}
		return  super.getRelativePathFrom(from);
	}
	
	private String _getRelativePathFrom(TestObject from, TestObject src) {
		StringBuilder buf = new StringBuilder();
		TestObject next = src;
		TestObject cur = next;
		// Walk up my ancestors from leaf to root, looking for "from"
		// and accumulating a relative url as I go
		while (next != null && from != next) {
			cur = next;
			buf.insert(0, '/');
			buf.insert(0, cur.getSafeName());
			next = cur.getParent();
		}
		if (from == next) {
			return buf.toString();
		}
		return null;
	}
}
