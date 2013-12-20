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

import hudson.model.TaskListener;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CucumberJSONParserTest {

	
//	@Test
//	public void testParsingCMDC() throws Exception {
//		CucumberJSONParser parser = new CucumberJSONParser();
//		
//		File f = new File("C:\\Users\\nordj\\Desktop\\CMDCGlue.json");
//		
//		List<File> files = new ArrayList<File>();
//		files.add(f);
//		
//		TaskListener mockListener = Mockito.mock(TaskListener.class);
//		Mockito.when(mockListener.getLogger()).thenReturn(System.out);
//		
//		CucumberTestResult testresult = parser.parse(files, null, mockListener);
//
//		Assert.assertEquals("incorrect number of features", testresult.getChildren().size(), 3);
//	}
	

	@Test
	public void testParsing() throws Exception {
		CucumberJSONParser parser = new CucumberJSONParser();
		
		File f = getResourceAsFile("ScenarioResultTest/cucumber-jvm_examples_java-calculator__cucumber-report.json");
		
		List<File> files = new ArrayList<File>();
		files.add(f);
		
		TaskListener mockListener = Mockito.mock(TaskListener.class);
		Mockito.when(mockListener.getLogger()).thenReturn(System.out);
		
		CucumberTestResult testresult = parser.parse(files, mockListener);

		Assert.assertEquals("incorrect number of features", testresult.getChildren().size(), 3);
		// TODO some more asserts, also parse some different files.
	}


	
	private static File getResourceAsFile(String resource) throws Exception {
		URL url = CucumberJSONParserTest.class.getResource(resource);
		Assert.assertNotNull("Resource " + resource + " could not be found", url);
		File f = new File(url.toURI());
		return f;
	}
	
}


