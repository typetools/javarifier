// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
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

package org.htmlparser.tests.tagTests;

import junit.framework.TestSuite;

import org.htmlparser.tests.ParserTestCase;

public class AllTests extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.AllTests", "AllTests");
    }

    public AllTests(String name) {
        super(name);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Tag Tests");
        suite.addTestSuite(AppletTagTest.class);
        suite.addTestSuite(BaseHrefTagTest.class);
        suite.addTestSuite(BodyTagTest.class);
        suite.addTestSuite(BulletListTagTest.class);
        suite.addTestSuite(BulletTagTest.class);
        suite.addTestSuite(CompositeTagTest.class);
        suite.addTestSuite(DivTagTest.class);
        suite.addTestSuite(DoctypeTagTest.class);
        suite.addTestSuite(EndTagTest.class);
        suite.addTestSuite(FormTagTest.class);
        suite.addTestSuite(FrameSetTagTest.class);
        suite.addTestSuite(FrameTagTest.class);
        suite.addTestSuite(HeadTagTest.class);
        suite.addTestSuite(HtmlTagTest.class);
        suite.addTestSuite(ImageTagTest.class);
        suite.addTestSuite(InputTagTest.class);
        suite.addTestSuite(JspTagTest.class);
        suite.addTestSuite(LabelTagTest.class);
        suite.addTestSuite(LinkTagTest.class);
        suite.addTestSuite(MetaTagTest.class);
        suite.addTestSuite(ObjectCollectionTest.class);
        suite.addTestSuite(OptionTagTest.class);
        suite.addTestSuite(ScriptTagTest.class);
        suite.addTestSuite(SelectTagTest.class);
        suite.addTestSuite(SpanTagTest.class);
        suite.addTestSuite(StyleTagTest.class);
        suite.addTestSuite(TableTagTest.class);
        suite.addTestSuite(TagTest.class);
        suite.addTestSuite(TextareaTagTest.class);
        suite.addTestSuite(TitleTagTest.class);
        return suite;
    }
}
