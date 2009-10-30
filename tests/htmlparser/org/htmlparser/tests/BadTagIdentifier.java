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

package org.htmlparser.tests;

import org.htmlparser.Parser;
import org.htmlparser.visitors.TagFindingVisitor;

public class BadTagIdentifier {

    public BadTagIdentifier() {
        super();
    }

    public static void main(String[] args)
        throws Exception {
        BadTagIdentifier badTags =
            new BadTagIdentifier();
        badTags.identify("http://www.amazon.com");
    }

    private void identify(String url)
        throws Exception{
        String [] tagsBeingChecked =
        {"TABLE","DIV","SPAN"};

        Parser parser =
            new Parser(url);
        TagFindingVisitor tagFinder =
            new TagFindingVisitor(tagsBeingChecked, true);
        parser.visitAllNodesWith(tagFinder);
        for (int i=0;i<tagsBeingChecked.length;i++) {
            System.out.println(
                "Number of "+tagsBeingChecked[i]+" begin tags = "+
            tagFinder.getTagCount(i));
            System.out.println(
                "Number of "+tagsBeingChecked[i]+" end tags = "+
                tagFinder.getEndTagCount(i));
        }

    }
}
