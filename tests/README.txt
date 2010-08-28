The test suite consists of a set of program classes whose source files are in
tests/ .  The Javarifier is run on each class X, and its output is sent to
tests/X.jaif.  This is compared with expected output in tests/X.jaif.goal,
and the test is considered to succeed if the output matches.

To run the test suite, run
	make
in tests/ .  When the test suite finishes, a summary of which cases the
Javarifier passed and failed will be printed.


This is the initial framework for running Javarifier tests.  To run all tests, simply go into javarifier/tests and type 'make all'.  This command will:
     - search this directory for every file of the form myClass.goal
     - will run the Javarifier on the matching myClass.class file
     - compute difference between Javarifier output (myClass.jaif) and
        goal file, and put this in a (myClass.jaif.diff) file
     - output which tests failed and which tests passed

To run just one individual test, type 'make myClass.diff'
To remove all generated output, type 'make clean'

To add a test for the class myClass, do the following:
     - Put the file myClass.class into this directory (javarifier/tests).
	   To generate this file from a java source file using the javac 1.5
	   compiler, use the -g debugging option, i.e. 'javac -g myClass.java'
     - Put a file corresponding to the output you expect from the javarifier
	   into a file named myClass.goal in this same directory
     - Also, don't forget to add these tests to the cvs repository to make
           them part of the test suite.

Note all classes must be in the default package.  This will change soon.

To remove a test, simply delete the corresponding .goal and .class files.

Improvements coming soon:
Class files may be in a package other than the default.
There will be a distinction between tests that the javarifier must pass (nightly regression tests) and those that it should ideally pass.
Automatic system to run nightly tests.
