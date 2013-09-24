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


public class ScenarioToHTML {
	
	private int indent = 0;
	
	private ScenarioResult scenarioResult;
	
	public ScenarioToHTML(ScenarioResult scenarioResult) {
		this.scenarioResult = scenarioResult;
	}
	
	public static String getHTML(ScenarioResult scenarioResult) {
		return new ScenarioToHTML(scenarioResult).getHTML();
	}
	
	/**
	 * Builds a Gherkin file from the results of the parsing and formats it for HTML.
	 * XXX this should be moved elsewhere!
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" bgcolor=\"#ffffff\">\n");
		sb.append("<tbody><tr>\n");
		sb.append("<td nowrap=\"nowrap\" valign=\"top\" align=\"left\">\n");
		sb.append("<code>");
		// being gherkin output...
		
		addBasicStatement(sb, scenarioResult.getParent().getFeature());
		indent++;
		
		for (BeforeAfterResult before : scenarioResult.getBeforeResults()) {
			addBeforeAfterResult(sb, "before",  before);
		}
		addBackgroundResult(sb, scenarioResult.getBackgroundResult());
		
		addDescribedStatement(sb, scenarioResult.getScenario());
		indent++;
		
		for (StepResult stepResult : scenarioResult.getStepResults()) {
			addStepResult(sb, stepResult);
		}
		for (BeforeAfterResult after: scenarioResult.getAfterResults()) {
			addBeforeAfterResult(sb, "after", after);
		}
		// end gherkin output...
		sb.append("</code>");
		sb.append("</td></tbody></table>");
		return sb.toString();
	}


	private StringBuilder addBasicStatement(StringBuilder sb, TagStatement tagStatement) {
		sb.append("<!-- addBasicStatement -->");
		for (Comment comment : tagStatement.getComments()) {
			createLine(sb, comment.getLine());
			sb.append(comment.getValue());
		}
		for (Tag tag : tagStatement.getTags()) {
			createLine(sb, tag.getLine());
			sb.append(tag.getName());
		}
		createLine(sb, tagStatement.getLine());
		appendKeyword(sb, tagStatement.getKeyword()).append(' ').append(tagStatement.getName());
		return sb;
	}
	
	private StringBuilder createLine(StringBuilder sb, Integer line) {
		String lineStr = String.format("%03d", line);
		return createLine(sb, lineStr);
	}
	
	private StringBuilder createLine(StringBuilder sb, String str) {
		sb.append("\n<tr><td><a style=\"color:#808080\" name=\"").append(str).append("\">");
		sb.append(str);
		sb.append("</a><td>");
		addIndent(sb);
		return sb;
	}
	
	public StringBuilder addComment(StringBuilder sb, Comment comment) {
		createLine(sb, comment.getLine());
		sb.append(comment.getValue());
		return sb;
	}
	
	public StringBuilder addDescribedStatement(StringBuilder sb, DescribedStatement ds) {
		for (Comment comment : ds.getComments()) {
			addComment(sb, comment);
		}
		createLine(sb, ds.getLine());
		appendKeyword(sb, ds.getKeyword());
		sb.append(' ');
		sb.append(ds.getName());
		return sb;
	}
	
	public StringBuilder appendKeyword(StringBuilder sb, String keyword) {
		sb.append("<span style=\"font-weight: bold\">").append(keyword).append("</span>");
		return sb;	
	}
	
	public StringBuilder addBeforeAfterResult(StringBuilder sb, String beforeOrAfter, BeforeAfterResult beforeAfter) {
		sb.append("<!-- addBeforeAfterResult -->");
		Match m = beforeAfter.getMacth();
		Result r = beforeAfter.getResult();
		createLine(sb, beforeOrAfter);
		sb.append(m.getLocation()).append(' ');
		addFailure(sb, r);
		// XXX add argument formatting
		List<Argument> args = m.getArguments();
		return sb;
	}
	
	public StringBuilder addFailure(StringBuilder sb, Result result) {
		if (Result.FAILED.equals(result.getStatus())) {
			createLine(sb, "Failure");
			sb.append("<div style=\"background-color: #ffcccc\">");
			String[] stack = result.getErrorMessage().split("\n");
			
			sb.append(stack[0]).append("<br>");
			for (int i = 1; i < stack.length; i++) {
				addIndent(sb);
				sb.append(stack[i].replaceAll("\t", "&nbsp;&nbsp;"));
				sb.append("<br>");
			}
			sb.append("</div>");
			// Error is always null (only non null when invoked direct as part of the test).
			/*
			Throwable t = result.getError();
			if (t != null) {
				StackTraceElement stack[] = t.getStackTrace();
				for (StackTraceElement ste : stack) {
					sb.append(ste.toString()).append("<br>");
				}
			}
			*/
		}
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
		createLine(sb, step.getLine());
		appendKeyword(sb, step.getKeyword());
		sb.append(' ');
		sb.append(step.getName());
		if (step.getRows() != null) {
			indent++;
			for (DataTableRow dtr : step.getRows()) {
				List<Comment> comments = dtr.getComments();
				if (comments != null) {
					for (Comment comment : comments) {
						addComment(sb, comment);
					}
				}
				createLine(sb, dtr.getLine());
				for (String cell : dtr.getCells()) {
					sb.append(" | ").append(cell);
				}
				sb.append(" |");
			}
			indent--;
		}
		// TODO add support for table rows...
		addFailure(sb, stepResult.getResult());
		return sb;
   }
	
	public StringBuilder addIndent(StringBuilder sb) {
		return addIndent(sb, indent);
	}
	
	public StringBuilder addIndent(StringBuilder sb, int level) {
		for (int i=0; i < level; i++) {
			sb.append("&nbsp;&nbsp;&nbsp;&nbsp");
		}
		return sb;
	}
	
}
