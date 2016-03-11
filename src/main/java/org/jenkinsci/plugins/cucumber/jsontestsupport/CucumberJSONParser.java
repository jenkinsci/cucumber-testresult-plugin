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

import gherkin.JSONParser;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Parser that understands Cucumbers <a href="http://cukes.info/reports.html#json">JSON</a> notation and will
 * generate {@link hudson.tasks.test.TestResult} so that Jenkins will display the results.
 */
@Extension
public class CucumberJSONParser extends DefaultTestResultParserImpl {

    private static final long serialVersionUID = -296964473181541824L;
    private boolean ignoreBadSteps;

    public CucumberJSONParser() {
    }

    public CucumberJSONParser(boolean ignoreBadSteps){
        this.ignoreBadSteps = ignoreBadSteps;
    }

    @Override
    public String getDisplayName() {
        return "Cucumber JSON parser";
    }

    @Override
    protected CucumberTestResult parse(List<File> reportFiles, TaskListener listener) throws InterruptedException, IOException {

        CucumberTestResult result = new CucumberTestResult();
        GherkinCallback callback = new GherkinCallback(result, listener, ignoreBadSteps);
        listener.getLogger().println("[Cucumber Tests] Parsing results.");
        JSONParser jsonParser = new JSONParser(callback, callback);

        try {
            for (File f : reportFiles) {
                String s = FileUtils.readFileToString(f, "UTF-8");
                // if no scenarios where executed for a feature then a json file may still exist.
                if (s.isEmpty()) {
                    listener.getLogger().println("[Cucumber Tests] ignoring empty file (" + f.getName() + ")");
                }
                else {listener.getLogger().println("[Cucumber Tests] parsing " + f.getName());
                    jsonParser.parse(s);
                }
            }
        }
        catch (CucumberModelException ccm) {
            throw new AbortException("Failed to parse Cucumber JSON: " + ccm.getMessage());
        }
        finally {
            // even though this is a noop prevent an eclipse warning.
            callback.close();
        }
        result.tally();
        return result;
    }


    @Override
    public CucumberTestResult parseResult(final String testResultLocations,
                                          final Run<?, ?> build,
                                          final FilePath workspace,
                                          final Launcher launcher,
                                          final TaskListener listener) throws InterruptedException, IOException {
        // overridden so we tally and set the owner on the master.
        CucumberTestResult result = (CucumberTestResult) super.parseResult(testResultLocations, build, workspace, launcher, listener);
        result.tally();
        result.setOwner(build);
        return result;
    }
}