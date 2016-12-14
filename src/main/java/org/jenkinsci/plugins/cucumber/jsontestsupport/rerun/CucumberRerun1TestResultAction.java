package org.jenkinsci.plugins.cucumber.jsontestsupport.rerun;

import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResult;


public class CucumberRerun1TestResultAction extends CucumberRerunTestResultAction {

  @Override
  protected int getNumber() {
    return 1;
  }

  public CucumberRerun1TestResultAction(Run<?, ?> owner, CucumberTestResult result, TaskListener listener) {
    super(owner, result, listener);
  }

}
