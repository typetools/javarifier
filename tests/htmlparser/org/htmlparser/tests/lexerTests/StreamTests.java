// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/lexerTests/StreamTests.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:17 $
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

package org.htmlparser.tests.lexerTests;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.htmlparser.lexer.Stream;
import org.htmlparser.tests.ParserTestCase;

public class StreamTests extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.lexerTests.StreamTests", "StreamTests");
    }

    /**
     * Test the first level stream class.
     */
    public StreamTests (String name)
    {
        super (name);
    }

    /**
     * Test initialization with a null value.
     */
    public void testNull () throws IOException
    {
        Stream stream;

        stream = new Stream (null);
        assertTrue ("erroneous character", -1 == stream.read ());
    }

    /**
     * Test initialization with an empty input stream.
     */
    public void testEmpty () throws IOException
    {
        Stream stream;

        stream = new Stream (new ByteArrayInputStream (new byte[0]));
        assertTrue ("erroneous character", -1 == stream.read ());
    }

    /**
     * Test initialization with an input stream having only one byte.
     */
    public void testOneByte () throws IOException
    {
        Stream stream;

        stream = new Stream (new ByteArrayInputStream (new byte[] { (byte)0x42 }));
        assertTrue ("erroneous character", 0x42 == stream.read ());
        assertTrue ("erroneous character", -1 == stream.read ());
    }

    /**
     * Test that the same bytes are returned as with a naked input stream.
     */
    public void testSameBytes () throws IOException
    {
        String link;
        URL url;
        URLConnection connection1;
        URLConnection connection2;
        BufferedInputStream in;
        int b1;
        int b2;
        Stream stream;
        int index;

        // pick a big file
        link = "http://htmlparser.sourceforge.net/HTMLParser_Coverage.html";
        try
        {
            url = new URL (link);
            connection1 = url.openConnection ();
            connection1.connect ();
            in = new BufferedInputStream (connection1.getInputStream ());
            connection2 = url.openConnection ();
            connection2.connect ();
            stream = new Stream (connection2.getInputStream ());
            index = 0;
            while (-1 != (b1 = in.read ()))
            {
                b2 = stream.read ();
                if (b1 != b2)
                    fail ("bytes differ at position " + index + ", expected " + b1 + ", actual " + b2);
                index++;
            }
            b2 = stream.read ();
            stream.close ();
            in.close ();
            assertTrue ("extra bytes", b2 == -1);
        }
        catch (MalformedURLException murle)
        {
            fail ("bad url " + link);
        }
    }

    /**
     * Test that threading works and is faster than a naked input stream.
     * This, admittedly contrived, test illustrates the following principles:
     * <li>the underlying network code is already multi-threaded, so there may
     * not be a need to use application level threading in most cases</li>
     * <li>results may vary based on network connection speed, JVM, and
     * especially application usage pattterns</li>
     * <li>issues only show up with large files, in my case greater than
     * about 72,400 bytes, since the underlying network code reads that far
     * into the socket before throttling back and waiting</li>
     * <li>this is only applicable to TCP/IP usage, disk access would not
     * have this problem, since the cost of reading disk is much less than
     * the round-trip cost of a TCP/IP handshake</li>
     * So, what does it do? It sets up to read a URL two ways, once with a
     * naked input stream, and then with the Stream class. In each case, before
     * reading, it delays about 2 seconds (for me anyway) to allow the java.net
     * implementation to read ahead and then throttle back. The threaded Stream
     * though keeps reading while this delay is going on and hence gets a big
     * chunk of the file in memory. This advantage translates to a faster
     * spin through the bytes after the delay.
     */
    public void testThreaded () throws IOException
    {
        String link;
        URL url;
        URLConnection connection;
        BufferedInputStream in;
        int index;
        long begin;
        double bytes_per_second;
        int delay;
        Stream stream;
        long time1;
        long time2;
        Thread thread;
        long available1;
        long available2;

        // pick a big file
        link = "http://htmlparser.sourceforge.net/javadoc_1_3/index-all.html";
        try
        {
            url = new URL (link);

            // estimate the connection speed
            System.gc ();
            index = 0;
            connection = url.openConnection ();
            connection.connect ();
            in = new BufferedInputStream (connection.getInputStream ());
            begin = System.currentTimeMillis ();
            while (-1 != in.read ())
                index++;
            bytes_per_second = 1000.0 * index / (System.currentTimeMillis () - begin);
            in.close ();

            delay = (int)(1.5 * 1000 * bytes_per_second / 72400); // 72400 is the throttle limit on my machine

            // try the naked input stream
            System.gc ();
            index = 0;
            available1 = 0;
            connection = url.openConnection ();
            connection.connect ();
            in = new BufferedInputStream (connection.getInputStream ());
            try
            {
                Thread.sleep (delay);
            }
            catch (Exception e)
            {
                e.printStackTrace ();
            }
            begin = System.currentTimeMillis ();
            do
            {
                index++;
                if (0 == index % 1000)
                    available1 += in.available ();
            }
            while (-1 != in.read ());
            time1 = System.currentTimeMillis () - begin;
            in.close ();

            // try a threaded stream
            System.gc ();
            index = 0;
            available2 = 0;
            connection = url.openConnection ();
            connection.connect ();
            int length = connection.getContentLength ();
            stream = new Stream (connection.getInputStream (), length);
            thread = new Thread (stream);
            thread.setPriority (Thread.NORM_PRIORITY - 1);
            thread.start ();
            try
            {
                Thread.sleep (delay);
            }
            catch (Exception e)
            {
                e.printStackTrace ();
            }
            begin = System.currentTimeMillis ();
            do
            {
                index++;
                if (0 == index % 1000)
                    available2 += stream.available ();
            }
            while (-1 != stream.read ());
            time2 = System.currentTimeMillis () - begin;

//            System.out.println ("fills: " + stream.fills);
//            System.out.println ("reallocations: " + stream.reallocations);
//            System.out.println ("synchronous: " + stream.synchronous);
//            System.out.println ("buffer size: " + stream.mBuffer.length);
//            System.out.println ("bytes: " + stream.mLevel);
            stream.close ();

//            System.out.println ("time (" + time2 + ") vs. (" + time1 + ") for " + index + " bytes");
            double samples = index / 1000;
//            System.out.println ("average available bytes (" + available2/samples + ") vs. (" + available1/samples + ")");

            assertTrue ("slower (" + time2 + ") vs. (" + time1 + ")", time2 < time1);
            assertTrue ("average available bytes not greater (" + available2/samples + ") vs. (" + available1/samples + ")", available2 > available1);
        }
        catch (MalformedURLException murle)
        {
            fail ("bad url " + link);
        }
    }

    /**
     * Test that mark and reset work as per the contract.
     */
    public void testMarkReset () throws IOException
    {
        String link;
        ArrayList bytes1;
        ArrayList bytes2;
        URL url;
        URLConnection connection;
        Stream stream;
        int b;
        int index;

        // pick a small file > 2000 bytes
        link = "http://htmlparser.sourceforge.net/javadoc_1_3/overview-summary.html";
        bytes1 = new ArrayList ();
        bytes2 = new ArrayList ();
        try
        {
            url = new URL (link);
            connection = url.openConnection ();
            connection.connect ();
            stream = new Stream (connection.getInputStream ());
            assertTrue ("mark not supported", stream.markSupported ());

            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes1.add (new Byte ((byte)b));
            }
            stream.reset ();
            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes2.add (new Byte ((byte)b));
            }

            index = 0;
            while (index < bytes1.size ())
            {
                assertEquals ("bytes differ at position " + index, bytes1.get (index), bytes2.get (index));
                index++;
            }

            bytes1.clear ();
            bytes2.clear ();

            stream.mark (1000); // the 1000 is ignored
            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes1.add (new Byte ((byte)b));
            }
            stream.reset ();
            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes2.add (new Byte ((byte)b));
            }
            stream.close ();

            index = 0;
            while (index < bytes1.size ())
            {
                assertEquals ("bytes differ at position " + (index + 1000), bytes1.get (index), bytes2.get (index));
                index++;
            }
        }
        catch (MalformedURLException murle)
        {
            fail ("bad url " + link);
        }
    }

    /**
     * Test that mark and reset work as per the contract when threaded.
     */
    public void testMarkResetThreaded () throws IOException
    {
        String link;
        ArrayList bytes1;
        ArrayList bytes2;
        URL url;
        URLConnection connection;
        Stream stream;
        int b;
        int index;

        // pick a small file > 2000 bytes
        link = "http://htmlparser.sourceforge.net/javadoc_1_3/overview-summary.html";
        bytes1 = new ArrayList ();
        bytes2 = new ArrayList ();
        try
        {
            url = new URL (link);
            connection = url.openConnection ();
            connection.connect ();
            stream = new Stream (connection.getInputStream ());
            (new Thread (stream)).start ();
            assertTrue ("mark not supported", stream.markSupported ());

            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes1.add (new Byte ((byte)b));
            }
            stream.reset ();
            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes2.add (new Byte ((byte)b));
            }

            index = 0;
            while (index < bytes1.size ())
            {
                assertEquals ("bytes differ at position " + index, bytes1.get (index), bytes2.get (index));
                index++;
            }

            bytes1.clear ();
            bytes2.clear ();

            stream.mark (1000); // the 1000 is ignored
            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes1.add (new Byte ((byte)b));
            }
            stream.reset ();
            for (int i = 0; i < 1000; i++)
            {
                b = stream.read ();
                bytes2.add (new Byte ((byte)b));
            }
            stream.close ();

            index = 0;
            while (index < bytes1.size ())
            {
                assertEquals ("bytes differ at position " + (index + 1000), bytes1.get (index), bytes2.get (index));
                index++;
            }
        }
        catch (MalformedURLException murle)
        {
            fail ("bad url " + link);
        }
    }

    /**
     * Test close.
     */
    public void testClose () throws IOException
    {
        Stream stream;

        stream = new Stream (new ByteArrayInputStream (new byte[] { (byte)0x42, (byte)0x78 }));
        assertTrue ("erroneous character", 0x42 == stream.read ());
        stream.close ();
        assertTrue ("not closed", -1 == stream.read ());
   }
}
