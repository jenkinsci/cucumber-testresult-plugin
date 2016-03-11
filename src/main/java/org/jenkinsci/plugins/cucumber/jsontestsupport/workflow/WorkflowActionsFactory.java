package org.jenkinsci.plugins.cucumber.jsontestsupport.workflow;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResultAction;

/**
 * Created by haoyuwang on 3/11/16.
 */
@Extension
public class WorkflowActionsFactory extends TransientActionFactory<Job> {

    @Override
    public Class<Job> type() {
        return Job.class;
    }

    @Override
    public Collection<? extends Action> createFor(Job j) {
        List<Action> actions = new LinkedList<Action>();
        if (j.getClass().getCanonicalName().startsWith("org.jenkinsci.plugins.workflow")) {
            final Run<?, ?> r = j.getLastSuccessfulBuild();
            if (r != null) {
                // If reports are being saved on the build level (keep for all builds)
                List<CucumberTestResultAction> reports = r.getActions(CucumberTestResultAction.class);
                for (CucumberTestResultAction report : reports) {
                    actions.add(report);
                }
            }
        }
        return actions;
    }
}
