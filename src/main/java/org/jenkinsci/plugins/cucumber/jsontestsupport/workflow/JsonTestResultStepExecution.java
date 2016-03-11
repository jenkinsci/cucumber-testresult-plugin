package org.jenkinsci.plugins.cucumber.jsontestsupport.workflow;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.inject.Inject;

import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResultArchiver;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

/**
 * Created by haoyuwang on 3/11/16.
 */
public class JsonTestResultStepExecution extends AbstractSynchronousStepExecution<Void> {
    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;

    @Inject
    private transient JsonTestResultStep step;

    @Override
    protected Void run() throws Exception {
        final String target = step.getTarget();
        if (target == null) {
            throw new AbortException("Cannot publish the report. Target is not specified");
        }

        CucumberTestResultArchiver resultArchiver = new CucumberTestResultArchiver(target);
        resultArchiver.publishReport(build, ws, launcher, listener);
        return null;
    }

    private static final long serialVersionUID = 1L;
}
