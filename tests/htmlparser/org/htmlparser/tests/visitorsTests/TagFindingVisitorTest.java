// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/visitorsTests/TagFindingVisitorTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:35 $
// $Revision: 1.1 $
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

import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.visitors.TagFindingVisitor;

public class TagFindingVisitorTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.visitorsTests.TagFindingVisitorTest", "TagFindingVisitorTest");
    }

    private String html =
        "<HTML><HEAD><TITLE>This is the Title</TITLE></HEAD>" +
        "<BODY>Hello World, this is an excellent parser</BODY>" +
        "<UL><LI><LI></UL>" +
        "<A href=\"http://www.industriallogic.com\">Industrial Logic</a>" +
        "</HTML>";

    public TagFindingVisitorTest(String name) {
        super(name);
    }

    public void setUp() {
        createParser(html);
    }

    public void testTagFound() throws Exception {
        TagFindingVisitor visitor = new TagFindingVisitor(new String[] {"HEAD"});
        parser.visitAllNodesWith(visitor);
        assertEquals("HEAD found", 1, visitor.getTagCount(0));
    }

    public void testTagsFound() throws Exception {
        TagFindingVisitor visitor = new TagFindingVisitor(new String [] {"LI"});
        parser.visitAllNodesWith(visitor);
        assertEquals("LI tags found", 2, visitor.getTagCount(0));
    }

    public void testMultipleTags() throws Exception {
        TagFindingVisitor visitor =
            new TagFindingVisitor(
                new String [] {
                    "LI","BODY","UL","A"
                }
            );
        parser.visitAllNodesWith(visitor);
        assertEquals("LI tags found", 2, visitor.getTagCount(0));
        assertEquals("BODY tag found", 1, visitor.getTagCount(1));
        assertEquals("UL tag found", 1, visitor.getTagCount(2));
        assertEquals("A tag found", 1, visitor.getTagCount(3));
    }

    public void testEndTags() throws Exception {
        TagFindingVisitor visitor =
            new TagFindingVisitor(
                new String [] {
                    "LI","BODY","UL","A"
                },
                true
            );
        parser.visitAllNodesWith(visitor);
        assertEquals("LI tags found", 2, visitor.getTagCount(0));
        assertEquals("BODY tag found", 1, visitor.getTagCount(1));
        assertEquals("UL tag found", 1, visitor.getTagCount(2));
        assertEquals("A tag found", 1, visitor.getTagCount(3));
        assertEquals("BODY end tag found", 1, visitor.getEndTagCount(1));
    }


    public void assertTagNameShouldBe(String message, Node node, String expectedTagName) {
        Tag tag = (Tag)node;
        assertStringEquals(message,expectedTagName,tag.getTagName());
    }
}

