package org.jenkinsci.plugins.cucumber.jsontestsupport;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class BuildResultTest  {


  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"all_fail.json","failIfAny",Result.FAILURE},
        {"all_fail.json","failIfAll",Result.FAILURE},
        {"all_fail.json","unstable",Result.UNSTABLE},
        {"1_fail.json","failIfAny",Result.FAILURE},
        {"1_fail.json","failIfAll",Result.UNSTABLE},
        {"1_fail.json","unstable",Result.UNSTABLE},
        {"all_pass.json","failIfAny",Result.SUCCESS},
        {"all_pass.json","failIfAll",Result.SUCCESS},
        {"all_pass.json","unstable",Result.SUCCESS}
    });
  }

  private String jsonFile;

  private String setResult;

  private Result expectedResult;


  public BuildResultTest(String jsonFile, String setResult, Result expectedResult) {
    this.jsonFile = jsonFile;
    this.setResult = setResult;
    this.expectedResult = expectedResult;
  }


  @Rule
  public JenkinsRule jenkinsRule = new JenkinsRule();

  @Test
  public void first() throws Exception {
    FreeStyleProject project = jenkinsRule.createFreeStyleProject();

    SingleFileSCM scm = new SingleFileSCM("test.json",
                                          getResource(jsonFile)
                                              .toURI().toURL());
    project.setScm(scm);

    CucumberTestResultArchiver resultArchiver = new CucumberTestResultArchiver("test.json", true, setResult);
    
    project.getPublishersList().add(resultArchiver);

    project.save();


    FreeStyleBuild build = project.scheduleBuild2(0).get();
    assertThat(build.getResult(), is(expectedResult));

  }


  private static URL getResource(String resource) throws Exception {
    URL url = BuildResultTest.class.getResource(BuildResultTest.class.getSimpleName() + "/" + resource);
    Assert.assertNotNull("Resource " + resource + " could not be found", url);
    return url;
  }
}
