package org.jenkinsci.plugins.cucumber.jsontestsupport.rerun;

import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResult;


public class CucumberRerun2TestResultAction extends CucumberRerunTestResultAction {

  @Override
  protected int getNumber() {
    return 2;
  }

  public CucumberRerun2TestResultAction(Run<?, ?> owner, CucumberTestResult result, TaskListener listener) {
    super(owner, result, listener);
  }
}
