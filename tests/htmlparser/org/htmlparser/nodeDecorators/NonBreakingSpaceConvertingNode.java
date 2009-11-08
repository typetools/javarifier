// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Joshua Kerievsky
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

package org.htmlparser.nodeDecorators;

import org.htmlparser.Text;

/**
 * @deprecated Use direct subclasses or dynamic proxies instead.
 * <p>Use either direct subclasses of the appropriate node and set them on the
 * {@link org.htmlparser.PrototypicalNodeFactory PrototypicalNodeFactory},
 * or use a dynamic proxy implementing the required node type interface.</p>
 * @see AbstractNodeDecorator
 */
public class NonBreakingSpaceConvertingNode extends AbstractNodeDecorator {
    public NonBreakingSpaceConvertingNode(Text newDelegate) {
        super(newDelegate);
    }

    public String toPlainTextString() {
        String result = delegate.toPlainTextString();
        return result.replace ('\u00a0',' ');
    }
}
