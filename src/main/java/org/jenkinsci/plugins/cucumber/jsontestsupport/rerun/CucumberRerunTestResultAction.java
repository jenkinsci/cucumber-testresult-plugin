package org.jenkinsci.plugins.cucumber.jsontestsupport.rerun;

import hudson.XmlFile;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResult;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResultAction;

import java.io.File;


public abstract class CucumberRerunTestResultAction extends CucumberTestResultAction {

  protected abstract int getNumber();

  public CucumberRerunTestResultAction(Run<?, ?> owner, CucumberTestResult result, TaskListener listener) {
    super(owner, result, listener);
  }

  @Override
  protected XmlFile getDataFile() {
    return new XmlFile(XSTREAM, new File(run.getRootDir(), "cucumberRerunResult" + getNumber() + ".xml"));
  }

  @Override
  public String getDisplayName() {
    return "Cucumber Rerun " + getNumber() + " Result";
  }

  @Override
  public String getUrlName() {
    return "cucumberRerun" + getNumber();
  }

}
