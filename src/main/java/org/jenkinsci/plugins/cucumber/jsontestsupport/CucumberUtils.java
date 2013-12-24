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

import gherkin.formatter.model.Result;
import gherkin.formatter.model.TagStatement;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CucumberUtils {

	private static final Logger LOG = Logger.getLogger(CucumberUtils.class.getName());
	
	/** Get the duration (in seconds) that the result took. */
	static float durationFromResult(Result result) {
		// internally this is in nanosecodes
		Long l = result.getDuration();
		if (l == null) {
			return 0.0f;
		}
		return l.floatValue() / 1000000000.0f;
	}


	/**
	 * Get the ID from the TagStatement. For some reason the authors of cucumber jvm thought the ID should be
	 * private with no getter... TODO - create a patch for cucumber-jvm so we do not need this hack.
	 * 
	 * @param stmt the {@link TagStatement} with the ID>
	 * @return the ID of the {@link TagStatement} - possibly <code>null</code>
	 */
	public static String getId(TagStatement stmt) {
		try {
			Field f = TagStatement.class.getField("id");
			f.setAccessible(true);
			return (String) f.get(stmt);
		}
		catch (NoSuchFieldException e) {
			LOG.log(Level.WARNING, "Could not get ID from statement: " + stmt.getName(), e);
		}
		catch (SecurityException e) {
			LOG.log(Level.WARNING, "Could not get ID from statement: " + stmt.getName(), e);
		}
		catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "Could not get ID from statement: " + stmt.getName(), e);
		}
		catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "Could not get ID from statement: " + stmt.getName(), e);
		}
		return null;
	}

	public enum GherkinState {
		UNDEFINED(false, false, true),
		PASSED(true, false, false),
		FAILED(false, false, true),
		SKIPPED(false, true, false);

		private static final String SKIPPED_STRING = "skipped";
		private static final String UNDEFINED_STRING = "undefined";
		private static final String PASSED_STRING = "passed";
		private static final String FAILED_STRING = "failed";

		private boolean passed;
		private boolean skipped;
		private boolean failure;

		private GherkinState(boolean passed, boolean skipped, boolean failure) {
			// skipped is neither a pass nor a failure!
			this.passed = passed;
			this.skipped = skipped;
			this.failure = failure;
		}

		/**
		 * Return true if this represents a failure of the Gherkin Step.
		 */
		public boolean isFailureState() {
			return failure;
		}

		/**
		 * Return true if this represents a pass of the Gherkin step.
		 */
		public boolean isPassedState() {
			return passed;
		}

		/**
		 * Return true if this represents a skipp of the Gherkin step (which is neither a pass nor a fail.
		 */
		public boolean isSkippedState() {
			return skipped;
		}

		public static GherkinState parseState(String state) {
			if (PASSED_STRING.equals(state)) {
				return PASSED;
			}
			if (FAILED_STRING.equals(state)) {
				return FAILED;
			}
			if (SKIPPED_STRING.equals(state)) {
				return SKIPPED;
			}
			if (UNDEFINED_STRING.equals(state)) {
				return UNDEFINED;
			}
			throw new CucumberModelException("Cucumber sate \"" + state + "\" is not defined.");
		}
	}
}
