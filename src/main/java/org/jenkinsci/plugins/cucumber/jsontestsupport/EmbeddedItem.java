/*
 * The MIT License
 *
 * Copyright (c) 2014, Cisco Systems, Inc., a California corporation
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

import java.io.Serializable;

/**
 * An EmbeddedItem represents an item that has been embedded in a test report.
 * The actual copying of the item from the JSON (parsed on the slave) to the master happens in 
 * {@linnk CucumberJSONParser.parse(String, AbstractBuild, Launcher, TaskListener)}
 * @author James Nord
 *
 */
public class EmbeddedItem implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The mimetype of the object */
	private String mimetype;
	
	/** The name if the embedded file on disk */
	private String filename;

	public EmbeddedItem(String mimetype, String filename) {
		this.mimetype = mimetype;
		this.filename = filename;
	}

	protected String getFilename() {
		return filename;
	}

	protected String getMimetype() {
		return mimetype;
	}

}
