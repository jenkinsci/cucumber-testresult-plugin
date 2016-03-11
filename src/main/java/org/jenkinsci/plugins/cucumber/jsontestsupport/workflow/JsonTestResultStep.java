package org.jenkinsci.plugins.cucumber.jsontestsupport.workflow;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by haoyuwang on 3/10/16.
 */
public class JsonTestResultStep extends AbstractStepImpl {

    private final String target;

    @DataBoundConstructor
    public JsonTestResultStep(@CheckForNull String target) {
        this.target = target;
    }

    @CheckForNull
    public String getTarget() {
        return target;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(JsonTestResultStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "publishJsonTestResult";
        }

        @Override
        public String getDisplayName() {
            return "Publish Json reports";
        }
    }
}
