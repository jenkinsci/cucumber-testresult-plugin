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

import gherkin.JSONParser;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.junit.Test;

import static org.junit.Assert.*;
import gherkin.formatter.model.Feature;

public class TestResultTest {

	@Test
	public void testParseFile() throws Exception {
		URL url = getClass().getResource("ScenarioResultTest/cucumber-jvm_examples_java-calculator__cucumber-report.json");
		File f = new File(url.toURI());
		
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.scan();
		TestResult tr = new TestResult(0, scanner);
		Feature[] features = tr.parse(f);
		assertEquals("Should have 2 features", 3, features.length);
	}

	
	@Test
	public void testParsing() throws Exception {
		URL url = getClass().getResource("ScenarioResultTest/cucumber-jvm_examples_java-calculator__cucumber-report.json");
		File f = new File(url.toURI());
		String s = FileUtils.readFileToString(f);
		
		JSONParser jsonParser = new JSONParser(new MyReporter(), new MyFormatter());
		jsonParser.parse(s);
		
	}
	
}

class MyFormatter implements Formatter {

	public void uri(String uri) {
		System.out.println(uri);
	}

	public void feature(Feature feature) {
		System.out.println("Feature: " + feature);
	}

	public void background(Background background) {
		System.out.println("Background: " + background);
   }

	public void scenario(Scenario scenario) {
		System.out.println("Scenario: " + scenario);
	   
   }

	public void scenarioOutline(ScenarioOutline scenarioOutline) {
		System.out.println("ScenarioOutline: " + scenarioOutline);
	   
   }

	public void examples(Examples examples) {
		System.out.println("Examples: " + examples);
	   
   }

	public void step(Step step) {
		System.out.println("Step: " + step);
	   
   }

	public void eof() {
		System.out.println("eof");
	   
   }

	public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
		System.out.println("syntaxError: ");
	   
   }

	public void done() {
		System.out.println("done");
	   
   }

	public void close() {
		System.out.println("close:");
   }
}

class MyReporter implements Reporter {

	public void before(Match match, Result result) {
	   // TODO Auto-generated method stub
	   
   }

	public void result(Result result) {
	   // TODO Auto-generated method stub
	   
   }

	public void after(Match match, Result result) {
	   // TODO Auto-generated method stub
	   
   }

	public void match(Match match) {
	   // TODO Auto-generated method stub
	   
   }

	public void embedding(String mimeType, byte[] data) {
	   // TODO Auto-generated method stub
	   
   }

	public void write(String text) {
	   // TODO Auto-generated method stub
	   
   }
	
}