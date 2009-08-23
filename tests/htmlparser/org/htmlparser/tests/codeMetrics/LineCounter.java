// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/codeMetrics/LineCounter.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:14 $
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

package org.htmlparser.tests.codeMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

public class LineCounter {

    public int count(File file) {
        System.out.println("Handling "+file.getName());
        int count = 0;
        // Get all files in current directory
        if (file.isDirectory()) {
            // Get the listing in this directory
            count = recurseDirectory(file, count);
        } else {
            // It is a file
            count = countLinesIn(file);
        }
        return count;
    }

    /**
     * Counts code excluding comments and blank lines in the given file
     * @param file
     * @return int
     */
    public int countLinesIn(File file) {
        int count = 0;
        System.out.println("Counting "+file.getName());
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            String line = null;
            do {
                line = reader.readLine();
                if (line!=null &&
                    line.indexOf("*")==-1 &&
                    line.indexOf("//")==-1 &&
                    line.length()>0
                ) count++;
            }
            while (line!=null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int recurseDirectory(File file, int count) {
        File [] files = file.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.getName().indexOf(".java")!=-1 || file.isDirectory()) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        for (int i=0;i<files.length;i++) {
            count += count(files[i]);
        }
        return count;
    }

    public static void main(String [] args) {
        LineCounter lc = new LineCounter();
        System.out.println("Line Count = "+lc.count(new File(args[0])));
    }
}
