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
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class PerformanceTest {
    private int numTimes;
    private String file;

    /**
     * Try to parse the given page the given no of times
     * Print average time taken
     * @param file File to be parsed
     * @param numTimes number of times the test should be repeated
     */
    public PerformanceTest(String file, int numTimes) {
        this.file = file;
        this.numTimes = numTimes;
    }

    public void beginTestWithoutScanners() throws ParserException {
        Parser parser;
        long sumTimes=0;
        double avg=0;
        System.out.println("***************************************");
        System.out.println("*  Test Without Scanners Registered   *");
        System.out.println("***************************************");
        for (int i=0;i<=numTimes;i++) {
            // Create the parser object
            parser = new Parser(file,new DefaultParserFeedback());
            long start=System.currentTimeMillis();
            for (NodeIterator e = parser.elements();e.hasMoreNodes();)
                e.nextNode();
            long elapsedTime=System.currentTimeMillis()-start;
            if (i!=0)
            sumTimes += elapsedTime;
            System.out.print("Iteration "+i);
            if (i==0) System.out.print(" (not counted)");
            System.out.println(" : time taken = "+elapsedTime+" ms");
        }
        avg = sumTimes/(float)numTimes;
        System.out.println("***************************************");
        System.out.println("Average Time : "+avg+" ms");
        System.out.println("***************************************");
    }

    public void beginTestWithScanners() throws ParserException {
        Parser parser;
        long sumTimes=0;
        double avg=0;
        System.out.println("***************************************");
        System.out.println("*    Test With Scanners Registered    *");
        System.out.println("***************************************");
        for (int i=0;i<=numTimes;i++) {
            // Create the parser object
            parser = new Parser(file,new DefaultParserFeedback());
            long start=System.currentTimeMillis();
            for (NodeIterator e = parser.elements();e.hasMoreNodes();)
                e.nextNode();
            long elapsedTime=System.currentTimeMillis()-start;
            if (i!=0)
            sumTimes += elapsedTime;
            System.out.print("Iteration "+i);
            if (i==0) System.out.print(" (not counted)");
            System.out.println(" : time taken = "+elapsedTime+" ms");
        }
        avg = sumTimes/(float)numTimes;
        System.out.println("***************************************");
        System.out.println("Average Time : "+avg+" ms");
        System.out.println("***************************************");
    }

    public static void main(String[] args) {
        if (args.length<2) {
            System.err.println("Syntax Error.");
            System.err.println("Params needed for test : <file/url to be parsed> <number of iterations>");
            System.exit(-1);
        }
        String file = args[0];
        String numTimesString = args[1];
        int numTimes = Integer.decode(numTimesString).intValue();
        PerformanceTest pt = new PerformanceTest(file,numTimes);
        try {
            pt.beginTestWithoutScanners();
            pt.beginTestWithScanners();
        }
        catch (ParserException e) {
            e.printStackTrace();
        }
    }
}

