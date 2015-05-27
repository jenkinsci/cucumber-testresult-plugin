/*
 * The MIT License
 * 
 * Copyright (c) 2015, CloudBees, Inc.
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

import java.io.File;
import java.net.URL;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.slaves.DumbSlave;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CucumberJSONSupportPluginIT {

	@Rule
	public JenkinsRule jenkinsRule = new JenkinsRule();

	@Test
	@Issue("JENKINS-28588")
	public void testSerializationOnSlave() throws Exception {
		DumbSlave slave = jenkinsRule.createOnlineSlave();

		SingleFileSCM scm = new SingleFileSCM("test.json",
		                                      getResource("passWithEmbeddedItem.json")
		                                                            .toURI().toURL());

		FreeStyleProject project = jenkinsRule.createFreeStyleProject("cucumber-plugin-IT");
		project.setAssignedNode(slave);
		project.setScm(scm);

		CucumberTestResultArchiver resultArchiver = new CucumberTestResultArchiver("test.json");

		project.getPublishersList().add(resultArchiver);

		project.save();

		FreeStyleBuild build = jenkinsRule.buildAndAssertSuccess(project);
		jenkinsRule.assertLogContains("test.json", build);
		// check we built on the slave not the master...

		assertThat("Needs to build on the salve to check serialization", build.getBuiltOn(), is((Node) slave));
	}
	
	private static URL getResource(String resource) throws Exception {
		URL url = CucumberJSONSupportPluginIT.class.getResource(CucumberJSONSupportPluginIT.class.getSimpleName() + "/" + resource);
		Assert.assertNotNull("Resource " + resource + " could not be found", url);
		return url;
	}
}
