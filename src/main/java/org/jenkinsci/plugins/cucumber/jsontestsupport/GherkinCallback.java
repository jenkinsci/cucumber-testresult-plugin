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

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation that gets called back by the Gherkin parser.
 * 
 * @author James Nord
 */
class GherkinCallback implements Formatter, Reporter {

	private static final Logger logger = Logger.getLogger(GherkinCallback.class.getName());
	
	private FeatureResult currentFeatureResult = null;
	private ScenarioResult currentScenarioResult = null;
	private BackgroundResult currentBackground = null;

	private Step currentStep = null;
	private Match currentMatch = null;

	private String currentURI = null;

	private CucumberTestResult testResult;


	GherkinCallback(CucumberTestResult testResult) {
		this.testResult = testResult;
	}


	// Formatter implementation

	// called before a feature to identify the feature
	public void uri(String uri) {
		logger.fine("URI: " + uri);
		if (currentURI != null) {
			logger.severe("URI received before previous uri handled");
			throw new CucumberModelException("URI received before previous uri handled");
		}
		currentURI = uri;
	}


	public void feature(Feature feature) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Feature: " + feature.getKeyword() + feature.getName());
			List<Tag> tags = feature.getTags();
			for (Tag tag : tags) {
				logger.fine("         " + tag.getName());
			}
			logger.fine("         " + feature.getDescription());
		}
		// a new feature being received signals the end of the previous feature
		currentFeatureResult = new FeatureResult(currentURI, feature);
		currentURI = null;
		testResult.addFeatureResult(currentFeatureResult);
	}


	// applies to a scenario
	public void background(Background background) {
		logger.fine("Background: " + background.getName());
		if (currentBackground != null) {
			logger.severe("Background received before previous background handled");
			throw new CucumberModelException("Background received before previous background handled");
		}
		currentBackground = new BackgroundResult(background);
	}


	public void scenario(Scenario scenario) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Scenario: " + scenario.getKeyword() + " " + scenario.getName());
			List<Tag> tags = scenario.getTags();
			for (Tag tag : tags) {
				logger.fine("         " + tag.getName());
			}
			logger.fine("          " + scenario.getDescription());
			logger.fine("          " + scenario.getComments());
		}
		// a new scenario signifies that the previous scenario has been handled.
		currentScenarioResult = new ScenarioResult(scenario, currentBackground);
		currentBackground = null;
		currentFeatureResult.addScenarioResult(currentScenarioResult);
	}


	// appears to not be called.
	public void scenarioOutline(ScenarioOutline scenarioOutline) {
		logger.fine("ScenarioOutline: " + scenarioOutline.getName());
	}


	// appears to not be called.
	public void examples(Examples examples) {
		// not stored in the json - used in the Gherkin only
		logger.fine("Examples: " + examples.getName());
	}

	// appears to not be called.
	 public void startOfScenarioLifeCycle(Scenario scenario) {
		 logger.fine("startOfScenarioLifeCycle: " + scenario.getName());
	}

	// appears to not be called.
	public void endOfScenarioLifeCycle(Scenario scenario) {
		logger.fine("endOfScenarioLifeCycle: " + scenario.getName());
	}

	// A step has been called - could be in a background or a Scenario
	public void step(Step step) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Step: " + step.getKeyword() + " " + step.getName());
			logger.fine("      " + step.getRows());
			// logger.fine("      " + step.getStackTraceElement());
		}
		if (currentStep != null) {
			logger.severe("Step received before previous step handled!");
			throw new CucumberModelException("Step received before previous step handled!");
		}
		currentStep = step;
	}

	// marks the end of a feature
	public void eof() {
		logger.fine("eof");
		currentFeatureResult = null;
		currentScenarioResult = null;
		currentBackground = null;
		currentStep = null;
		currentURI = null;
	}


	public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
		logger.severe("syntaxError: ");
		StringBuilder sb = new StringBuilder("Failed to parse Gherkin json file.");
		sb.append("\tline: ").append(line);
		sb.append("\turi: ").append(uri);
		sb.append("\tState: ").append(state);
		sb.append("\tEvent: ").append(event);
		throw new CucumberModelException(sb.toString());
	}

	public void done() {
		// appears to not be called?
		logger.fine("done");
	}

	public void close() {
		// appears to not be called?
		logger.fine("close:");
	}


	// Reporter implementation.

	// applies to a scenario - any code that is tagged as @Before
	public void before(Match match, Result result) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("rep  before match: " + match.getLocation());
			logger.fine("rep        result : " + "(passed) " + Result.PASSED.equals(result.getStatus()));
			logger.fine("rep        result : " + result.getDuration());
			logger.fine("rep        result : " + result.getErrorMessage());
			logger.fine("rep        result : " + result.getError());
		}
		currentScenarioResult.addBeforeResult(new BeforeAfterResult(match, result));
	}


	// applies to a step, may be in a scenario or a background
	public void result(Result result) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("rep  result: " + "(passed) " + Result.PASSED.equals(result.getStatus()));
			logger.fine("rep          " + result.getDuration());
			logger.fine("rep          " + result.getErrorMessage());
			logger.fine("rep          " + result.getError());
		}
		StepResult stepResult = new StepResult(currentStep, currentMatch, result);
		if (currentBackground != null) {
			currentBackground.addStepResult(stepResult);
		}
		else {
			currentScenarioResult.addStepResult(stepResult);
		}
		currentStep = null;
		currentMatch = null;
	}


	// applies to a scenario - any code that is tagged as @After
	public void after(Match match, Result result) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("rep  after match  : " + match.getLocation());
			logger.fine("rep        result : " + "(passed) " + Result.PASSED.equals(result.getStatus()));
			logger.fine("rep        result : " + result.getDuration());
			logger.fine("rep        result : " + result.getErrorMessage());
			logger.fine("rep        result : " + result.getError());
		}
		currentScenarioResult.addAfterResult(new BeforeAfterResult(match, result));
	}


	// applies to a step
	public void match(Match match) {
		// applies to a step.
		logger.fine("rep  match: " + match.getLocation());
		if (currentMatch != null) {
			logger.severe("Match received before previous Match handled");
			throw new CucumberModelException("Match received before previous Match handled");
		}
		currentMatch = match;
	}


	public void embedding(String mimeType, byte[] data) {
		logger.fine("rep  embedding: " + mimeType);
	}


	public void write(String text) {
		logger.fine("rep  write: " + text);
	}

}
