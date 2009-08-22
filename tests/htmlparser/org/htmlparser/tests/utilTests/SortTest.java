// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/utilTests/SortTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:33 $
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

import java.io.File;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.sort.Ordered;
import org.htmlparser.util.sort.Sort;
import org.htmlparser.util.sort.Sortable;

/**
 * Sort testing.
 */
public class SortTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.SortTest", "SortTest");
    }

    /**
     * Creates a new instance of SortTest
     */
    public SortTest (String name)
    {
        super (name);
    }

    /**
     * A class implementing the Ordered interface.
     */
    class Item implements Ordered
    {
        String mData;

        public Item (String data)
        {
            mData = data;
        }

        public int compare (Object o)
        {
            return (mData.compareTo (((Item)o).mData));
        }

        public String toString ()
        {
            return (mData);
        }
    }

    /**
     * A class implementing the Sortable interface.
     */
    class List extends Vector implements Sortable
    {
        List (String words)
        {
            StringTokenizer toks;

            toks = new StringTokenizer (words);
            outer:
            while (toks.hasMoreTokens ())
               addElement (new Item (toks.nextToken ()));
            Sort.QuickSort ((Sortable)this);
        }

        //
        // Sortable interface
        //

        public int first ()
        {
            return (0);
        }
        public int last ()
        {
            return (size () - 1);
        }
        public Ordered fetch (int index, Ordered reuse)
        {
            return ((Ordered)elementAt (index));
        }
        public void swap (int i, int j)
        {
            Object o = elementAt (i);
            setElementAt (elementAt (j), i);
            setElementAt (o, j);
        }
    }

    /**
     * A subclass implementing the Ordered interface.
     */
    class SortableFile extends File implements Ordered
    {
        public SortableFile (String name)
        {
            super (name);
        }

        public SortableFile (File dir, String name)
        {
            super (dir, name);
        }

        public int compare (Object o)
        {
            long ret;

            File f = (File)o;

            ret = lastModified () - f.lastModified ();
            if (ret < Integer.MIN_VALUE)
                ret = Integer.MIN_VALUE;
            if (ret > Integer.MAX_VALUE)
                ret = Integer.MAX_VALUE;
            if (0 == ret)
                ret = getAbsolutePath ().hashCode ()
                    - f.getAbsolutePath ().hashCode ();

            return ((int)ret);
        }
        
        public String toString ()
        {
            StringBuffer ret;

            ret = new StringBuffer (128);
            ret.append (this.getAbsolutePath ());
            ret.append ('@');
            ret.append (this.lastModified ());

            return (ret.toString ());
        }
    }

    /**
     * Test the operation of the static quicksort algorithm.
     */
    public void testQuickSort ()
    {
        Item[] words =
        {
            new Item ("gazelle"),
            new Item ("infant"),
            new Item ("toenail"),
            new Item ("breast"),
            new Item ("Derrick"),
            new Item ("toast"),
            new Item ("caretaker"),
        };

        Sort.QuickSort (words);


        assertEquals ("element 0 wrong ", "Derrick", words[0].mData);
        assertEquals ("element 1 wrong ", "breast", words[1].mData);
        assertEquals ("element 2 wrong ", "caretaker", words[2].mData);
        assertEquals ("element 3 wrong ", "gazelle", words[3].mData);
        assertEquals ("element 4 wrong ", "infant", words[4].mData);
        assertEquals ("element 5 wrong ", "toast", words[5].mData);
        assertEquals ("element 6 wrong ", "toenail", words[6].mData);

    }

    /**
     * Test the operation of quicksort on a sortable list.
     */
    public void testSortList ()
    {
        List list = new List (
            "'Twas brillig and the slithy toves " +
            "Did gyre and gimble in the wabe " +
            "All mimsy were the borogroves " +
            "And the mome raths outgrabe.");
        StringBuffer b = new StringBuffer ();
        for (Enumeration e = list.elements (); e.hasMoreElements ();)
        {
            if (0 != b.length ())
                b.append (' ');
            b.append (e.nextElement ());
        }
        assertEquals ("wrong ordering",
              "'Twas All And Did and and borogroves "
            + "brillig gimble gyre in mimsy mome outgrabe. "
            + "raths slithy the the the the toves wabe were",
            b.toString ());

    }

    /**
     * Test the operation of quicksort on a vector of ordered items.
     */
    public void testSortVector ()
    {
        // sort a directory by date (oldest first)
        Vector directory = new Vector ();
        File dir = new File (".");
        String[] listing = dir.list ();
        for (int i = 0; i < listing.length; i++)
        {
            File f = new SortableFile (dir, listing[i]);
            if (f.isFile ())
                directory.addElement (f);
        }

        Sort.QuickSort (directory);

        // pull one out and test it's insertion ordinal
        int index = directory.size () * 2 / 3;
        SortableFile test =
            (SortableFile)directory.elementAt (index);
        directory.removeElementAt (index);
        int ordinal = Sort.bsearch (directory, test);
        if (index != ordinal)
        {
            for (int i = 0; i < directory.size (); i++)
            {
                if (index == i)
                    System.out.print ('-');
                else if (ordinal == i)
                    System.out.print ('+');
                else
                    System.out.print (' ');
                System.out.println (directory.elementAt (i));
            }
            fail ("ordinal not correct value, expected " + index + ", was " + ordinal);
        }

        // test the ordering of the objects
        directory.insertElementAt (test, ordinal);
        Date last = null;
        for (int i = 0; i < directory.size (); i++)
        {
            File f = (File)directory.elementAt (i);
            String name = f.getName ();
            Date date = new Date (f.lastModified ());
            if (null != last)
                assertTrue ("file " + name + " has a date before", !date.before (last));
            last = date;
        }
    }
}
