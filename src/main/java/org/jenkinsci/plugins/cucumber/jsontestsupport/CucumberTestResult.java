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

import gherkin.formatter.model.Tag;
import hudson.model.AbstractBuild;
import hudson.tasks.test.MetaTabulatedResult;
import hudson.tasks.test.TestObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Represents all the Features from Cucumber.
 * 
 * @author James Nord
 */
public class CucumberTestResult extends MetaTabulatedResult {

	private static final long serialVersionUID = 3499017799686036745L;

	private List<FeatureResult> featureResults = new ArrayList<FeatureResult>();

	/**
	 *  Map of features keyed by feature name.
	 *  Recomputed by a call to {@link CucumberTestResult#tally()}
	 */
	private transient Map<String,FeatureResult> featuresById = new TreeMap<String, FeatureResult>();
	
	/** 
	 * List of all failed ScenarioResults.
	 * Recomputed by a call to {@link CucumberTestResult#tally()}
	 */
	private transient List<ScenarioResult> failedScenarioResults = new ArrayList<ScenarioResult>();

	/** 
	 * map of Tags to Scenarios. 
	 * recomputed by a call to {@link CucumberTestResult#tally()}
	 */
	private transient SetMultimap<String, ScenarioResult> tagMap =  HashMultimap.create();

	private transient AbstractBuild<?, ?> owner;
	
	/* Recomputed by a call to {@link CucumberTestResult#tally()} */
	private transient int passCount;
	private transient int failCount;
	private transient int skipCount;
	private transient float duration;


	public CucumberTestResult() {
	}


	/**
	 * Add a FeatureResult to this TestResult
	 * 
	 * @param featureResult the result of the feature to add.
	 */
	void addFeatureResult(FeatureResult result) {
		featureResults.add(result);
		result.setParent(this);
		passCount += result.getPassCount();
		failCount += result.getFailCount();
		skipCount += result.getSkipCount();
		duration += result.getDuration();
	}


	@Override
	public String getName() {
		return "Cucumber Tests";
	}


	public String getChildTitle() {
		return "Feature Name";
	}
   
	@Override
	public Collection<FeatureResult> getChildren() {
		return featureResults;
	}

	@Exported(inline=true, visibility=9)
	public Collection<FeatureResult> getFeatures() {
		return featureResults;
	}

	@Override
	public boolean hasChildren() {
		return !featureResults.isEmpty();
	}


	@Override
	public Collection<ScenarioResult> getFailedTests() {
		return failedScenarioResults;
	}

	
	@Override
	public AbstractBuild<?, ?> getOwner() {
		return owner;
	}

	void setOwner(AbstractBuild<?, ?> owner) {
		this.owner = owner;
		for (FeatureResult fr : featureResults) {
			fr.setOwner(owner);
		}
	}

	
	@Override
	public TestObject getParent() {
		return null;
	}


	@Override
	public hudson.tasks.test.TestResult findCorrespondingResult(String id) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getSkipCount() {
		return skipCount;
	}


	@Override
	public int getPassCount() {
		return passCount;
	}


	@Override
	public int getFailCount() {
		return failCount;
	}


	@Override
	public float getDuration() {
		return duration;
	}


	// @Override - this is an interface method
	public String getDisplayName() {
		return "Test Results";
	}
	

	@Override
	public void tally() {
		if (failedScenarioResults == null) {
			failedScenarioResults = new ArrayList<ScenarioResult>();
		}
		else {
			failedScenarioResults.clear();
		}
		if (tagMap == null) {
			tagMap =  HashMultimap.create();
		}
		else {
			tagMap.clear();
		}
		
		passCount = 0;
		failCount = 0;
		skipCount = 0;
		duration = 0.0f;
		
		if (featuresById == null) {
			featuresById = new TreeMap<String, FeatureResult>();
		}
		else {
			featuresById.clear();
		}
		
		for (FeatureResult fr : featureResults) {
			fr.tally();
			passCount += fr.getPassCount();
			failCount += fr.getFailCount();
			skipCount += fr.getSkipCount();
			duration += fr.getDuration();
			failedScenarioResults.addAll(fr.getFailedTests());
			featuresById.put(fr.getSafeName(), fr);
			for (ScenarioResult scenarioResult : fr.getChildren()) {
				for (Tag tag : scenarioResult.getScenario().getTags()) {
					tagMap.put(tag.getName(), scenarioResult);
				}
			}
		}
	}

	SetMultimap<String,ScenarioResult> getTagMap() {
		return tagMap;
	}

	@Override
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
		// TODO Tag support!
		if (token.equals(getId())) {
			return this;
		}
		FeatureResult result = featuresById.get(token);
		if (result != null) {
			return result;
		}
		else {
			return super.getDynamic(token, req, rsp);
		}
	}

}
