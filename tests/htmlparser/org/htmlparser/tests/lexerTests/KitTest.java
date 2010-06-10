// HTMLParser Library $Name:  $ - A java-based parser for HTML
// Copyright (C) August 26, 2003 Derrick Oswald
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package org.htmlparser.tests.lexerTests;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.Parser;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.lexer.Cursor;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.Translate;

/**
 * Compare output from javax.swing.text.html.HTMLEditorKit with Lexer.
 * This test provides a means of comparing the lexemes from
 * javax.swing.text.html.HTMLEditorKit.Parser class with the lexemes
 * produced by the org.htmlparser.lexer.Lexer class.
 * <blockquote>
 * The differences have eluded automation since the HTMLEditorKit parser
 * adds spurious nodes where it thinks elements need closing or it gets
 * confused.  The intent is to eventually incorporate this into the
 * 'fit test' and run it against lots of HTML pages, but so far you must
 * analyse the differences by hand.
 * </blockquote>
 */
public class KitTest extends ParserCallback
{
    Vector mNodes;
    int mIndex;

    /**
     * Creates a new instance of KitTest
     * @param nodes The list of lexemes from Lexer to compare with the kit lexemes.
     */
    public KitTest (Vector nodes)
    {
        mNodes = nodes;
        mIndex = 0;
    }

    /**
     * Remove whitespace from a string.
     * @param s The string to crunch.
     * @return The string with whitespace characters removed.
     */
    String snowhite (String s)
    {
        int length;
        char ch;
        StringBuffer ret;

        length = s.length ();
        ret = new StringBuffer (length);
        for (int i = 0; i < length; i++)
        {
            ch = s.charAt (i);
            if (!Character.isWhitespace (ch) && !(160 == ch))
                ret.append (ch);
        }

        return (ret.toString ());
    }

    /**
     * Check if two strings match.
     * @param s1 One string.
     * @param s2 The other string.
     * @return <code>true</code> if the strings are equivalent ignoring whitespace.
     */
    boolean match (String s1, String s2)
    {
        s1 = snowhite (Translate.decode (s1));
        s2 = snowhite (Translate.decode (s2));
        return (s1.equalsIgnoreCase (s2));
    }

    /**
     * Callback for a text lexeme.
     * @param data The text extracted from the page.
     * @param pos The position in the page.
     * <em>Note: This differs from the Lexer concept of position which is an
     * absolute location in the HTML input stream. This position is the character
     * position if the text from the page were displayed in a browser.</em>
     */
    public void handleText (char[] data, int pos)
    {
        StringBuffer sb;
        String theirs;
        Node node;
        int match;
        String ours;

        sb = new StringBuffer (data.length);
        for (int i = 0; i < data.length; i++)
        {
            if (160 == data[i])
                sb.append ("&nbsp;");
            else
                sb.append (data[i]);
        }
        theirs = sb.toString ();
        match = -1;
        for (int i = mIndex; i < Math.min (mIndex + 25, mNodes.size ()); i++)
        {
            node = (Node)mNodes.elementAt (i);
            ours = node.getText ();
            if (match (theirs, ours))
            {
                match = i;
                break;
            }
        }
        if (-1 == match)
        {
            node = (Node)mNodes.elementAt (mIndex);
            ours = node.getText ();
            System.out.println ("theirs: " + theirs);
            Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
            System.out.println ("ours " + cursor + ": " + ours);
        }
        else
        {
            boolean skipped = false;
            for (int i = mIndex; i < match; i++)
            {
                ours = ((Node)mNodes.elementAt (i)).toHtml ();
                if (0 != ours.trim ().length ())
                {
                    if (!skipped)
                        System.out.println ("skipping:");
                    System.out.println (ours);
                    skipped = true;
                }
            }
            if (skipped)
            {
                System.out.println ("to match:");
                node = (Node)mNodes.elementAt (match);
                Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
                System.out.println ("@" + cursor + ": " + node.toHtml ());
            }
//            System.out.println (" match: " + theirs);
            mIndex = match + 1;
        }
    }

    /**
     * Callback for a remark lexeme.
     * @param data The text extracted from the page.
     * @param pos The position in the page.
     * <em>Note: This differs from the Lexer concept of position which is an
     * absolute location in the HTML input stream. This position is the character
     * position if the text from the page were displayed in a browser.</em>
     */
    public void handleComment (char[] data, int pos)
    {
        StringBuffer sb;
        String theirs;
        Node node;
        int match;
        String ours;

        sb = new StringBuffer (data.length);
        sb.append (data);
        theirs = sb.toString ();
        match = -1;
        for (int i = mIndex; i < Math.min (mIndex + 25, mNodes.size ()); i++)
        {
            node = (Node)mNodes.elementAt (i);
            ours = node.getText ();
            if (match (theirs, ours))
            {
                match = i;
                break;
            }
        }
        if (-1 == match)
        {
            node = (Node)mNodes.elementAt (mIndex);
            ours = node.getText ();
            System.out.println ("theirs: " + theirs);
            Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
            System.out.println ("ours " + cursor + ": " + ours);
        }
        else
        {
            boolean skipped = false;
            for (int i = mIndex; i < match; i++)
            {
                ours = ((Node)mNodes.elementAt (i)).toHtml ();
                if (0 != ours.trim ().length ())
                {
                    if (!skipped)
                        System.out.println ("skipping:");
                    System.out.println (ours);
                    skipped = true;
                }
            }
            if (skipped)
            {
                System.out.println ("to match:");
                node = (Node)mNodes.elementAt (match);
                Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
                System.out.println ("@" + cursor + ": " + node.toHtml ());
            }
//            System.out.println (" match: " + theirs);
            mIndex = match + 1;
        }
    }

    /**
     * Callback for a start tag lexeme.
     * @param t The tag extracted from the page.
     * @param a The attributes parsed out of the tag.
     * @param pos The position in the page.
     * <em>Note: This differs from the Lexer concept of position which is an
     * absolute location in the HTML input stream. This position is the character
     * position if the text from the page were displayed in a browser.</em>
     */
    public void handleStartTag (HTML.Tag t, MutableAttributeSet a, int pos)
    {
        String theirs;
        Node node;
        int match;
        String ours;

        theirs = t.toString ();
        match = -1;
        for (int i = mIndex; i < Math.min (mIndex + 25, mNodes.size ()); i++)
        {
            node = (Node)mNodes.elementAt (i);
            if (node instanceof Tag)
            {
                ours = ((Attribute)(((Tag)node).getAttributesEx ().elementAt (0))).getName ();
                if (match (theirs, ours))
                {
                    match = i;
                    break;
                }
            }
        }
        if (-1 == match)
        {
            node = (Node)mNodes.elementAt (mIndex);
            ours = node.getText ();
            System.out.println ("theirs: " + theirs);
            Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
            System.out.println ("ours " + cursor + ": " + ours);
        }
        else
        {
            boolean skipped = false;
            for (int i = mIndex; i < match; i++)
            {
                ours = ((Node)mNodes.elementAt (i)).toHtml ();
                if (0 != ours.trim ().length ())
                {
                    if (!skipped)
                        System.out.println ("skipping:");
                    System.out.println (ours);
                    skipped = true;
                }
            }
            if (skipped)
            {
                System.out.println ("to match:");
                node = (Node)mNodes.elementAt (match);
                Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
                System.out.println ("@" + cursor + ": " + node.toHtml ());
            }
//            System.out.println (" match: " + theirs);
            mIndex = match + 1;
        }
    }

    /**
     * Callback for an end tag lexeme.
     * @param t The tag extracted from the page.
     * @param pos The position in the page.
     * <em>Note: This differs from the Lexer concept of position which is an
     * absolute location in the HTML input stream. This position is the character
     * position if the text from the page were displayed in a browser.</em>
     */
    public void handleEndTag (HTML.Tag t, int pos)
    {
        String theirs;
        Node node;
        int match;
        String ours;

        theirs = t.toString ();
        match = -1;
        for (int i = mIndex; i < Math.min (mIndex + 25, mNodes.size ()); i++)
        {
            node = (Node)mNodes.elementAt (i);
            if (node instanceof Tag)
            {
                ours = ((Attribute)(((Tag)node).getAttributesEx ().elementAt (0))).getName ().substring (1);
                if (match (theirs, ours))
                {
                    match = i;
                    break;
                }
            }
        }
        if (-1 == match)
        {
            node = (Node)mNodes.elementAt (mIndex);
            ours = node.getText ();
            System.out.println ("theirs: " + theirs);
            Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
            System.out.println ("ours " + cursor + ": " + ours);
        }
        else
        {
            boolean skipped = false;
            for (int i = mIndex; i < match; i++)
            {
                ours = ((Node)mNodes.elementAt (i)).toHtml ();
                if (0 != ours.trim ().length ())
                {
                    if (!skipped)
                        System.out.println ("skipping:");
                    System.out.println (ours);
                    skipped = true;
                }
            }
            if (skipped)
            {
                System.out.println ("to match:");
                node = (Node)mNodes.elementAt (match);
                Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
                System.out.println ("@" + cursor + ": " + node.toHtml ());
            }
//            System.out.println (" match: " + theirs);
            mIndex = match + 1;
        }
    }

    /**
     * Callback for a non-composite tag.
     * @param t The tag extracted from the page.
     * @param a The attributes parsed out of the tag.
     * @param pos The position in the page.
     * <em>Note: This differs from the Lexer concept of position which is an
     * absolute location in the HTML input stream. This position is the character
     * position if the text from the page were displayed in a browser.</em>
     */
    public void handleSimpleTag (HTML.Tag t, MutableAttributeSet a, int pos)
    {
        String theirs;
        Node node;
        int match;
        String ours;

        theirs = t.toString ();
        match = -1;
        for (int i = mIndex; i < Math.min (mIndex + 25, mNodes.size ()); i++)
        {
            node = (Node)mNodes.elementAt (i);
            if (node instanceof Tag)
            {
                ours = ((Attribute)(((Tag)node).getAttributesEx ().elementAt (0))).getName ();
                if (match (theirs, ours))
                {
                    match = i;
                    break;
                }
                if (match (theirs, ours))
                {
                    match = i;
                    break;
                }
            }
        }
        if (-1 == match)
        {
            node = (Node)mNodes.elementAt (mIndex);
            ours = node.getText ();
            System.out.println ("theirs: " + theirs);
            Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
            System.out.println ("ours " + cursor + ": " + ours);
        }
        else
        {
            boolean skipped = false;
            for (int i = mIndex; i < match; i++)
            {
                ours = ((Node)mNodes.elementAt (i)).toHtml ();
                if (0 != ours.trim ().length ())
                {
                    if (!skipped)
                        System.out.println ("skipping:");
                    System.out.println (ours);
                    skipped = true;
                }
            }
            if (skipped)
            {
                System.out.println ("to match:");
                node = (Node)mNodes.elementAt (match);
                Cursor cursor = new Cursor (((AbstractNode)node).getPage (), node.getStartPosition ());
                System.out.println ("@" + cursor + ": " + node.toHtml ());
            }
//            System.out.println (" match: " + theirs);
            mIndex = match + 1;
        }
    }


    /**
     * Callback for an error condition.
     * @param errorMsg The error condition as a text message.
     * @param pos The position in the page.
     * <em>Note: This differs from the Lexer concept of position which is an
     * absolute location in the HTML input stream. This position is the character
     * position if the text from the page were displayed in a browser.</em>
     */
    public void handleError (String errorMsg, int pos)
    {
        System.out.println ("******* error @" + pos + " ******** " + errorMsg);
    }

    /**
     * Callback for flushing the state, just prior to shutting down the parser.
     */
    public void flush () throws BadLocationException
    {
    }

    /**
     * This is invoked after the stream has been parsed, but before
     * <code>flush</code>. <code>eol</code> will be one of \n, \r
     * or \r\n, which ever is encountered the most in parsing the
     * stream.
     *
     * @since 1.3
     */
    public void handleEndOfLineString (String eol)
    {
    }

//    /**
//     * Get the document data from the URL.
//     * @param rd The reader to read bytes from.
//     * @return The parsed HTML document.
//     */
//    protected static Element[] getData (Reader rd) throws IOException
//    {
//        EditorKit kit;
//        Document doc;
//        Element[] ret;
//
//        ret = null;
//
//        // need this because HTMLEditorKit is not thread safe apparently
//        synchronized (Boolean.TRUE)
//        {
//            kit = new HTMLEditorKit ();
//            doc = kit.createDefaultDocument ();
//            // the Document class does not yet handle charset's properly
//            doc.putProperty ("IgnoreCharsetDirective", Boolean.TRUE);
//
//            try
//            {
//                // parse the HTML
//                kit.read (rd, doc, 0);
//            }
//            catch (BadLocationException ble)
//            {
//                throw new IOException ("parse error " + ble.getMessage ());
//            }
//
//            ret = doc.getRootElements ();
//        }
//
//        return (ret);
//    }

//    public static void scanElements (Element element) throws BadLocationException
//    {
//        int start;
//        int end;
//        String string;
//        ElementIterator it;
//        Element child;
//
//        if (element.isLeaf ())
//        {
//            start = element.getStartOffset ();
//            end = element.getEndOffset ();
//            string = element.getDocument ().getText (start, end - start);
//            System.out.println (string);
//        }
//        else
//            // iterate through the elements of the element
//            for (int i = 0; i < element.getElementCount (); i++)
//            {
//                child = element.getElement (i);
//                scanElements (child);
//            }
//    }

    /**
     * Subclass of HTMLEditorKit to expose getParser().
     */
    class MyKit extends HTMLEditorKit
    {
        public MyKit ()
        {
        }

        public HTMLEditorKit.Parser getParser ()
        {
            return (super.getParser ());
        }
    }

    /**
     * Return a editor kit.
     */
    public MyKit getKit ()
    {
        return (new MyKit ());
    }

    /**
     * Manline for the test.
     * @param args the command line arguments.
     * If present the first array element is used as a URL to parse.
     */
    public static void main (String[] args) throws ParserException, IOException
    {
        String link;
        Lexer lexer;
        Node node;
        Vector nodes;
        KitTest test;
        MyKit kit;
        Parser parser;


        if (0 == args.length)
            link = "http://sourceforge.net/projects/htmlparser";
        else
            link = args[0];
        // pass through it once to read the entire page
        URL url = new URL (link);
        lexer = new Lexer (url.openConnection ());
        nodes = new Vector ();
        while (null != (node = lexer.nextNode ()))
            nodes.addElement (node);

        // reset the reader
        lexer.getPage ().getSource ().reset ();
        test = new KitTest (nodes);
        kit = test.getKit ();
        parser = kit.getParser ();
        parser.parse (lexer.getPage ().getSource (), test, true);
    }
}

/*
 * Revision Control Modification History
 *
 * $Log: KitTest.java,v $
 * Revision 1.1  2008-05-25 04:57:15  jaimeq
 * Added eclipsec, htmlparser and tinySQL to test suite.
 *
 * Revision 1.10  2005/05/15 11:49:05  derrickoswald
 * Documentation revamp part four.
 * Remove some checkstyle warnings.
 *
 * Revision 1.9  2005/04/10 23:20:46  derrickoswald
 * Documentation revamp part one.
 * Deprecated node decorators.
 * Added doSemanticAction for Text and Comment nodes.
 * Added missing sitecapturer scripts.
 * Fixed DOS batch files to work when called from any location.
 *
 * Revision 1.8  2004/07/31 16:42:31  derrickoswald
 * Remove unused variables and other fixes exposed by turning on compiler warnings.
 *
 * Revision 1.7  2004/05/24 16:18:31  derrickoswald
 * Part three of a multiphase refactoring.
 * The three node types are now fronted by interfaces (program to the interface paradigm)
 * with concrete implementations in the new htmlparser.nodes package. Classes from the
 * lexer.nodes package are moved to this package, and obvious references to the concrete
 * classes that got broken by this have been changed to use the interfaces where possible.
 *
 * Revision 1.6  2004/01/14 02:53:47  derrickoswald
 * *** empty log message ***
 *
 * Revision 1.5  2003/10/20 01:28:03  derrickoswald
 * Removed lexer level AbstractNode.
 * Removed data package from parser level tags.
 * Separated tag creation from recursion in NodeFactory interface.
 *
 * Revision 1.4  2003/09/10 03:38:24  derrickoswald
 * Add style checking target to ant build script:
 *     ant checkstyle
 * It uses a jar from http://checkstyle.sourceforge.net which is dropped in the lib directory.
 * The rules are in the file htmlparser_checks.xml in the src directory.
 *
 * Added lexerapplications package with Tabby as the first app. It performs whitespace manipulation
 * on source files to follow the style rules. This reduced the number of style violations to roughly 14,000.
 *
 * There are a few issues with the style checker that need to be resolved before it should be taken too seriously.
 * For example:
 * It thinks all method arguments should be final, even if they are modified by the code (which the compiler frowns on).
 * It complains about long lines, even when there is no possibility of wrapping the line, i.e. a URL in a comment
 * that's more than 80 characters long.
 * It considers all naked integers as 'magic numbers', even when they are obvious, i.e. the 4 corners of a box.
 * It complains about whitespace following braces, even in array initializers, i.e. X[][] = { {a, b} { } }
 *
 * But it points out some really interesting things, even if you don't agree with the style guidelines,
 * so it's worth a look.
 *
 * Revision 1.3  2003/08/27 02:40:24  derrickoswald
 * Testing cvs keyword substitution.
 *
 *
 */
