// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/utilTests/NonEnglishTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:32 $
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

package org.htmlparser.tests.utilTests;

import org.htmlparser.beans.StringBean;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

/**
 * Test case for bug #1161137 Non English Character web page.
 * Submitted by Michael (tilosdcal@users.sourceforge.net)
 */
public class NonEnglishTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.NonEnglishTest", "NonEnglishTest");
    }

    public NonEnglishTest (String name)
    {
        super(name);
    }

    public void testNonEnglishCharacters() throws ParserException 
    {
        StringBean sb;
        
        sb = new StringBean ();
        sb.setURL ("http://www.kobe-np.co.jp/");
        sb.getStrings ();
        sb.setURL ("http://book.asahi.com/"); // this used to throw an exception
        sb.getStrings ();
    }
}
