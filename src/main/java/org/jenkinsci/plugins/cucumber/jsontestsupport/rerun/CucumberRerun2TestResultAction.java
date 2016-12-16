package org.jenkinsci.plugins.cucumber.jsontestsupport.rerun;

import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResult;

/**
 * Used to generate rerun results. Accessed with reflection - do not move or rename without
 * modifying {@link org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResultArchiver#getRerunActionClassName(int)}
 */
public class CucumberRerun2TestResultAction extends CucumberRerunTestResultAction {

  public CucumberRerun2TestResultAction(Run<?, ?> owner, CucumberTestResult result, TaskListener listener) {
    super(owner, result, listener);
  }

  @Override
  protected int getNumber() {
    return 2;
  }
}
