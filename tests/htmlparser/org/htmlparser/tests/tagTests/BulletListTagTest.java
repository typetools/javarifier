// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/BulletListTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:23 $
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

package org.htmlparser.tests.tagTests;

import org.htmlparser.Node;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.Text;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class BulletListTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.BulletListTagTest", "BulletListTagTest");
    }

    public BulletListTagTest (String name)
    {
        super(name);
    }
    
    public void testScan() throws ParserException {
        createParser(
            "<ul TYPE=DISC>" +
                "<ul TYPE=\"DISC\"><li>Energy supply\n"+
                    " (Campbell)  <A HREF=\"/hansard/37th3rd/h20307p.htm#1646\">1646</A>\n"+
                    " (MacPhail)  <A HREF=\"/hansard/37th3rd/h20307p.htm#1646\">1646</A>\n"+
                "</ul><A NAME=\"calpinecorp\"></A><B>Calpine Corp.</B>\n"+
                "<ul TYPE=\"DISC\"><li>Power plant projects\n"+
                    " (Neufeld)  <A HREF=\"/hansard/37th3rd/h20314p.htm#1985\">1985</A>\n"+
                "</ul>" +
            "</ul>"
        );
        parseAndAssertNodeCount(1);

        NodeList nestedBulletLists =
            ((CompositeTag)node[0]).searchFor(
                BulletList.class,
                true
            );
        assertEquals(
            "bullets in first list",
            2,
            nestedBulletLists.size()
        );
        BulletList firstList =
            (BulletList)nestedBulletLists.elementAt(0);
        Bullet firstBullet =
            (Bullet)firstList.childAt(0);
        Node firstNodeInFirstBullet =
            firstBullet.childAt(0);
        assertType(
            "first child in bullet",
            Text.class,
            firstNodeInFirstBullet
        );
        assertStringEquals(
            "expected text",
            "Energy supply\n" +
            " (Campbell)  ",
            firstNodeInFirstBullet.toPlainTextString()
        );
    }

    public void testMissingendtag ()
        throws ParserException
    {
        createParser ("<li>item 1<li>item 2");
        parseAndAssertNodeCount (2);
        assertStringEquals ("item 1 not correct", "item 1", ((Bullet)node[0]).childAt (0).toHtml ());
        assertStringEquals ("item 2 not correct", "item 2", ((Bullet)node[1]).childAt (0).toHtml ());
    }
}
