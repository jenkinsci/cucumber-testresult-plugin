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

/**
 * The implementation that gets called back by the Gherkin parser.
 * 
 * @author James Nord
 */
class GherkinCallback implements Formatter, Reporter {

	private boolean debug = false;
	
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
		if (debug) { System.out.println("URI: " + uri); }
		if (currentURI != null) {
			throw new CucumberModelException("URI received before previous uri handled");
		}
		currentURI = uri;
	}


	public void feature(Feature feature) {
		if (debug) {
			System.out.println("Feature: " + feature.getKeyword() + feature.getName());
			List<Tag> tags = feature.getTags();
			for (Tag tag : tags) {
				System.out.println("         " + tag.getName());
			}
			System.out.println("         " + feature.getDescription());
		}
		// a new feature being received signals the end of the previous feature
		currentFeatureResult = new FeatureResult(currentURI, feature);
		currentURI = null;
		testResult.addFeatureResult(currentFeatureResult);
	}


	// applies to a scenario
	public void background(Background background) {
		if (debug) {System.out.println("Background: " + background.getName());}
		if (currentBackground != null) {
			throw new CucumberModelException("Background received before previous background handled");
		}
		currentBackground = new BackgroundResult(background);
	}


	public void scenario(Scenario scenario) {
		if (debug) {
			System.out.println("Scenario: " + scenario.getKeyword() + " " + scenario.getName());
			List<Tag> tags = scenario.getTags();
			for (Tag tag : tags) {
				System.out.println("         " + tag.getName());
			}
			System.out.println("          " + scenario.getDescription());
			System.out.println("          " + scenario.getComments());
		}
		// a new scenario signifies that the previous scenario has been handled.
		currentScenarioResult = new ScenarioResult(scenario, currentBackground);
		currentBackground = null;
		currentFeatureResult.addScenarioResult(currentScenarioResult);
	}


	// appears to not be called.
	public void scenarioOutline(ScenarioOutline scenarioOutline) {
		if (debug) {
			System.out.println("ScenarioOutline: " + scenarioOutline.getName());
		}
	}


	// appears to not be called.
	public void examples(Examples examples) {
		// not stored in the json - used in the Gherkin only
		if (debug) {
			System.out.println("Examples: " + examples.getName());
		}
	}


	// A step has been called - could be in a background or a Scenario
	public void step(Step step) {
		if (debug) {
			System.out.println("Step: " + step.getKeyword() + " " + step.getName());
			System.out.println("      " + step.getRows());
			// System.out.println("      " + step.getStackTraceElement());
		}
		if (currentStep != null) {
			throw new CucumberModelException("Step received before previous step handled!");
		}
		currentStep = step;
	}


	// marks the end of a feature
	public void eof() {
		if (debug) {
			System.out.println("eof");
		}

		currentFeatureResult = null;
		currentScenarioResult = null;
		currentBackground = null;
		currentStep = null;
		currentURI = null;
	}


	public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
		if (debug) {System.out.println("syntaxError: ");}
		StringBuilder sb = new StringBuilder("Failed to parse Gherkin json file.");
		sb.append("\tline: ").append(line);
		sb.append("\turi: ").append(uri);
		sb.append("\tState: ").append(state);
		sb.append("\tEvent: ").append(event);
		throw new CucumberModelException(sb.toString());
	}


	public void done() {
		// appears to not be called?
		if (debug) {
			System.out.println("done");
		}
	}


	public void close() {
		// appears to not be called?
		if (debug) {
			System.out.println("close:");
		}
	}


	// Reporter implementation.

	// applies to a scenario - any code that is tagged as @Before
	public void before(Match match, Result result) {
		if (debug) {
			System.out.println("rep  before match: " + match.getLocation());
			System.out.println("rep        result : " + "(passed) " + Result.PASSED.equals(result.getStatus()));
			System.out.println("rep        result : " + result.getDuration());
			System.out.println("rep        result : " + result.getErrorMessage());
			System.out.println("rep        result : " + result.getError());
		}
		currentScenarioResult.addBeforeResult(new BeforeAfterResult(match, result));
	}


	// applies to a step, may be in a scenario or a background
	public void result(Result result) {
		if (debug) {
			System.out.println("rep  result: " + "(passed) " + Result.PASSED.equals(result.getStatus()));
			System.out.println("rep          " + result.getDuration());
			System.out.println("rep          " + result.getErrorMessage());
			System.out.println("rep          " + result.getError());
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
		if (debug) {
			System.out.println("rep  after match  : " + match.getLocation());
			System.out.println("rep        result : " + "(passed) " + Result.PASSED.equals(result.getStatus()));
			System.out.println("rep        result : " + result.getDuration());
			System.out.println("rep        result : " + result.getErrorMessage());
			System.out.println("rep        result : " + result.getError());
		}
		currentScenarioResult.addAfterResult(new BeforeAfterResult(match, result));
	}


	// applies to a step
	public void match(Match match) {
		// applies to a step.
		if (debug) {System.out.println("rep  match: " + match.getLocation());}
		if (currentMatch != null) {
			throw new CucumberModelException("Match received before previous Match handled");
		}
		currentMatch = match;
	}


	public void embedding(String mimeType, byte[] data) {
		if (debug) {System.out.println("rep  embedding: " + mimeType);}
	}


	public void write(String text) {
		if (debug) {
			System.out.println("rep  write: " + text);
		}
	}

}
