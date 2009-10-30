// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Jim Arnell
//
//
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.tests.visitorsTests;

import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.visitors.NodeVisitor;

public class ScriptCommentTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.visitorsTests.ScriptCommentTest", "ScriptCommentTest");
    }

    private String workingScriptTag =
        "<script language='javascript'>"
        + "// I cant handle single quotations\n"
        + "</script>";

    private String workingHtml =
        this.workingScriptTag
        + "<HTML>"
        + "</HTML>";

    private String failingScriptTag =
        "<script language='javascript'>"
        + "// I can't handle single quotations.\n"
        + "</script>";

    private String failingHtml =
        this.failingScriptTag
        + "<HTML>"
        + "</HTML>";

    private String failingHtml2 =
        "<HTML>"
        + this.failingScriptTag
        + "</HTML>";

    private String anotherFailingScriptTag =
        "<script language='javascript'>"
        + "/* I can't handle single quotations. */"
        + "</script>";

    public ScriptCommentTest(String name) {
        super(name);
    }

    public void testTagWorking() throws Exception {
        createParser(this.workingHtml);
        ScriptVisitor visitor = new ScriptVisitor();
        this.parser.visitAllNodesWith(visitor);
        String scriptNodeHtml = visitor.scriptTag.toHtml();
        assertEquals("Script parsing worked", this.workingScriptTag, scriptNodeHtml);
    }

    public void testScriptTagNotWorkingOuter() throws Exception {
        createParser(this.failingHtml);
        ScriptVisitor visitor = new ScriptVisitor();
        this.parser.visitAllNodesWith(visitor);
        String scriptNodeHtml = visitor.scriptTag.toHtml();
        assertEquals("Script parsing not working", this.failingScriptTag, scriptNodeHtml);
    }

    public void testScriptTagNotWorkingInner() throws Exception {
        createParser(this.failingHtml2);
        ScriptVisitor visitor = new ScriptVisitor();
        this.parser.visitAllNodesWith(visitor);
        String scriptNodeHtml = visitor.scriptTag.toHtml();
        assertEquals("Script parsing not working", this.failingScriptTag, scriptNodeHtml);
    }

    public void testScriptTagNotWorkingMultiLine() throws Exception {
        createParser(this.anotherFailingScriptTag);
        ScriptVisitor visitor = new ScriptVisitor();
        this.parser.visitAllNodesWith(visitor);
        String scriptNodeHtml = visitor.scriptTag.toHtml();
        assertEquals("Script parsing not working", this.anotherFailingScriptTag, scriptNodeHtml);
    }

    /**
     * Implement test case NodeVisitor.
     */
    public final class ScriptVisitor extends NodeVisitor {

        /** Keps the only script tag. */
        public ScriptTag scriptTag;

        /**
         * Creates a new ScriptVisitor object.
         */
        public ScriptVisitor() {
            super(true, true);
        }

        /**
         * @see org.htmlparser.visitors.NodeVisitor
         */
        public void visitTag(final Tag n) {
            if ((null != n.getParent())
                || ((n instanceof CompositeTag)
                    && (null == ((CompositeTag) n).getEndTag()))) {

                if (n instanceof ScriptTag) {
                    this.scriptTag = (ScriptTag) n;
                }
            } else {
                if (n instanceof ScriptTag) {
                    this.scriptTag = (ScriptTag) n;
                }
            }
        }
    }
}
