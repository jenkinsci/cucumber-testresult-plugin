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
import hudson.model.Run;
import hudson.tasks.test.MetaTabulatedResult;
import hudson.tasks.test.TestObject;
import hudson.tasks.test.TestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents a single Feature in Cucumber.
 * 
 * @author James Nord
 */
@ExportedBean
public class FeatureResult extends MetaTabulatedResult {

	private static final long serialVersionUID = 995206500596875310L;

	private Feature feature;
	private String uri;
	private transient Run<?, ?> owner;
	private transient String safeName;
	
	private List<ScenarioResult> scenarioResults = new ArrayList<ScenarioResult>();

	private transient List<ScenarioResult> failedScenarioResults;
	/**
	 *  Map of scenarios keyed by scenario name.
	 *  Recomputed by a call to {@link CucumberTestResult#tally()}
	 */
	private transient Map<String,ScenarioResult> scenariosByID = new TreeMap<String, ScenarioResult>();
	
	// XXX do we need to store these or should they be transient and recomputed on load.
	private int passCount;
	private int failCount;
	private int skipCount;
	private float duration;
	
	
	// TODO should this be reset on loading from xStream
	private CucumberTestResult parent;

	FeatureResult(String uri, Feature feature) {
		this.uri = uri;
		this.feature = feature;
	}
	

	public String getDisplayName() {
		return getName();
	}

	@Exported(visibility=9)
	public String getName() {
		return feature.getName();
	}
	
	
	@Override
	public Collection<ScenarioResult> getChildren() {
		return scenarioResults;
	}

	@Exported(visibility=9)
	public Collection<ScenarioResult> getScenarioResults() {
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
	public Run<?, ?> getRun() {
		return owner;
	}

	public void setOwner(Run<?, ?> owner) {
	   this.owner = owner;
	   for (ScenarioResult sr : scenarioResults) {
	   	sr.setOwner(owner);
	   }
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
		return scenariosByID.get(id);
	}


	@Override
	public Collection<ScenarioResult> getFailedTests() {
		return failedScenarioResults;
	}
	

	public String getURI() {
		return uri;
	}
	
	public Feature getFeature() {
		return feature;
	}
	
	void addScenarioResult(ScenarioResult scenarioResult) {
		scenarioResults.add(scenarioResult);
		scenarioResult.setParent(this);
	}
	
	@Override
	public synchronized String getSafeName() {
		if (safeName != null) {
			return safeName;
		}
		safeName = uniquifyName(parent.getChildren(), safe(feature.getId()));
		return safeName;
	}

	@Override
	public void tally() {
		if (scenariosByID == null) {
			scenariosByID = new TreeMap<String, ScenarioResult>();
		}
		else {
			scenariosByID.clear();
		}
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
			sr.tally();
			// XXX scenarious may be duplicated!??!
			scenariosByID.put(sr.getSafeName(), sr);
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
		return skipCount;
	}


	@Override
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
		if (token.equals(getId())) {
			return this;
		}
		ScenarioResult result = scenariosByID.get(token);
		if (result != null) {
			return result;
		}
		else {
			return super.getDynamic(token, req, rsp);
		}
	}
	
}
