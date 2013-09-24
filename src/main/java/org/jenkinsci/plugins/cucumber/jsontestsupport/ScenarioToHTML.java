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

import gherkin.formatter.Argument;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DescribedStatement;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.util.List;
import java.util.Locale;

public class ScenarioToHTML {

	private enum RESULT_TYPE {

		/** Step failed as it was not defined */
		UNDEFINED(""),
		/** step passed */
		PASSED("background-color: #e6ffcc;"),
		/** step failed */
		FAILED("background-color: #ffeeee;"),
		/** step skipped due to previous failure */
		SKIPPED("background-color: #ffffcc;"),
		/** line does not have a result */
		NO_RESULT("");

		public final String css;


		RESULT_TYPE(String css) {
			this.css = css;
		}


		public static RESULT_TYPE typeFromResult(Result r) {
			return RESULT_TYPE.valueOf(r.getStatus().toUpperCase(Locale.UK));
		}
	}

	private int indent = 0;

	private ScenarioResult scenarioResult;


	public ScenarioToHTML(ScenarioResult scenarioResult) {
		this.scenarioResult = scenarioResult;
	}


	public static String getHTML(ScenarioResult scenarioResult) {
		return new ScenarioToHTML(scenarioResult).getHTML();
	}


	/**
	 * Builds a Gherkin file from the results of the parsing and formats it for HTML. XXX this should be moved
	 * elsewhere!
	 */
	public String getHTML() {
		// we will be pretty big so start of large to avoild re-allocation.
		StringBuilder sb = new StringBuilder(20 * 1024);

		sb.append("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bgcolor=\"#ffffff\">\n");
		sb.append("<tbody>\n");
		// being gherkin output...

		addBasicStatement(sb, scenarioResult.getParent().getFeature());
		indent++;

		for (BeforeAfterResult before : scenarioResult.getBeforeResults()) {
			addBeforeAfterResult(sb, "before", before);
		}
		addBackgroundResult(sb, scenarioResult.getBackgroundResult());

		addDescribedStatement(sb, scenarioResult.getScenario());
		indent++;

		for (StepResult stepResult : scenarioResult.getStepResults()) {
			addStepResult(sb, stepResult);
		}
		for (BeforeAfterResult after : scenarioResult.getAfterResults()) {
			addBeforeAfterResult(sb, "after", after);
		}
		// end gherkin output...
		sb.append("</tbody></table>");
		return sb.toString();
	}


	private StringBuilder addBasicStatement(StringBuilder sb, TagStatement tagStatement) {
		for (Comment comment : tagStatement.getComments()) {
			addComment(sb, comment);
		}
		for (Tag tag : tagStatement.getTags()) {
			createLine(sb, tag.getLine(), RESULT_TYPE.NO_RESULT);
			sb.append(tag.getName());
		}
		createLine(sb, tagStatement.getLine(), RESULT_TYPE.NO_RESULT);
		appendKeyword(sb, tagStatement.getKeyword()).append(' ').append(tagStatement.getName());
		endLine(sb);
		return sb;
	}


	private StringBuilder createLine(StringBuilder sb, Integer line, RESULT_TYPE type) {
		String lineStr = String.format("%03d", line);
		return createLine(sb, lineStr, type);
	}


	private StringBuilder createLine(StringBuilder sb, String str, RESULT_TYPE type) {
		sb.append("\n<tr><td valign=\"top\" align=\"right\"><a style=\"color:#808080\" name=\"").append(str).append("\">");
		sb.append(str);
		sb.append("</a></td>");
		sb.append("<td nowrap=\"nowrap\" valign=\"top\" align=\"left\" style=\"").append(type.css).append("\">");
		sb.append("<div style=\"padding-left: ").append(indent).append("em;");
		sb.append(type.css);
		sb.append("\">");
		return sb;
	}


	private StringBuilder endLine(StringBuilder sb) {
		return sb.append("</div></td>");
	}


	public StringBuilder addComment(StringBuilder sb, Comment comment) {
		createLine(sb, comment.getLine(), RESULT_TYPE.NO_RESULT);
		sb.append(comment.getValue());
		endLine(sb);
		return sb;
	}


	public StringBuilder addDescribedStatement(StringBuilder sb, DescribedStatement ds) {
		for (Comment comment : ds.getComments()) {
			addComment(sb, comment);
		}
		createLine(sb, ds.getLine(), RESULT_TYPE.NO_RESULT);
		appendKeyword(sb, ds.getKeyword());
		sb.append(' ');
		sb.append(ds.getName());
		endLine(sb);
		return sb;
	}


	public StringBuilder appendKeyword(StringBuilder sb, String keyword) {
		sb.append("<span style=\"font-weight: bold\">").append(keyword).append("</span>");
		return sb;
	}


	public StringBuilder addBeforeAfterResult(StringBuilder sb,
	                                          String beforeOrAfter,
	                                          BeforeAfterResult beforeAfter) {
		Match m = beforeAfter.getMacth();
		Result r = beforeAfter.getResult();
		createLine(sb, beforeOrAfter, RESULT_TYPE.typeFromResult(r));
		sb.append(m.getLocation()).append(' ');
		addFailure(sb, r);
		// XXX add argument formatting
		List<Argument> args = m.getArguments();
		endLine(sb);
		return sb;
	}


	public StringBuilder addFailure(StringBuilder sb, Result result) {
		if (Result.FAILED.equals(result.getStatus())) {
			createLine(sb, "Failure", RESULT_TYPE.FAILED);
			String[] stack = result.getErrorMessage().split("\n");

			sb.append(stack[0]).append("<br>");
			for (int i = 1; i < stack.length; i++) {
				sb.append(stack[i].replaceAll("\t", "&nbsp;&nbsp;"));
				sb.append("<br>");
			}
			// Error is always null (only non null when invoked direct as part of the test).
			/*
			 * Throwable t = result.getError(); if (t != null) { StackTraceElement stack[] = t.getStackTrace();
			 * for (StackTraceElement ste : stack) { sb.append(ste.toString()).append("<br>"); } }
			 */
		}
		endLine(sb);
		return sb;
	}


	public StringBuilder addBackgroundResult(StringBuilder sb, BackgroundResult backgroundResult) {
		if (backgroundResult != null) {
			Background background = backgroundResult.getBackground();
			addDescribedStatement(sb, background);
			for (StepResult step : backgroundResult.getStepResults()) {
				addStepResult(sb, step);
			}
		}
		return sb;
	}


	public StringBuilder addStepResult(StringBuilder sb, StepResult stepResult) {
		Step step = stepResult.getStep();
		{
			List<Comment> comments = step.getComments();
			if (comments != null) {
				for (Comment c : comments) {
					addComment(sb, c);
				}
			}
		}
		createLine(sb, step.getLine(), RESULT_TYPE.typeFromResult(stepResult.getResult()));
		appendKeyword(sb, step.getKeyword());
		sb.append(' ');
		sb.append(step.getName());
		if (step.getRows() != null) {
			indent++;

			boolean firstRow = true;
			for (DataTableRow dtr : step.getRows()) {
				List<Comment> comments = dtr.getComments();
				if (comments != null) {
					for (Comment comment : comments) {
						addComment(sb, comment);
					}
				}
				createLine(sb, dtr.getLine(), RESULT_TYPE.NO_RESULT);
				int colwidth = 100 / (dtr.getCells().size());
				// these span multiple lines and divs don't wrap if the argument is too long
				// so use a table per row with the same sizes for each column. ugly but works...
				// having a large colspan would be nice but then we need to compute all the possibilities up
				// front.
				sb.append("<table width=\"80%\">");
				sb.append("<tr>");
				for (String cell : dtr.getCells()) {
					if (firstRow) {
						sb.append("<th width=\"").append(colwidth).append("%\">");
						sb.append(cell);
						sb.append("</th>");
						continue;
					}
					sb.append("<td width=\"").append(colwidth).append("%\">");
					sb.append(cell);
					sb.append("</td>");
				}
				sb.append("</tr></table>");

				firstRow = false;
				endLine(sb);
			}
			indent--;
		}
		endLine(sb);
		// TODO add support for table rows...
		addFailure(sb, stepResult.getResult());
		return sb;
	}

}
