#!/bin/bash
# javarify: command-line interface for the Javarifier

# usage: javarify.sh <program-classpath> <options-and-classes>

# The program-classpath points to where Soot will look for the application
# classes on which Javarifier is running.

# So that user can call javarify.sh from anywhere, assume that
# javarify.sh and the right build.xml file lie in the same directory.
DIRNAME=$(dirname $0)
SOOT_PROGRAMS="$1"
shift 1

set -x
ant -f ${DIRNAME}/build.xml -e javarify \
	 -Duser-soot-programs=${SOOT_PROGRAMS} \
	 -Duser-soot-stubs=${SOOT_STUBS} \
	 -Duser-soot-worlds=${SOOT_WORLD} \
	 -Duser-args=\""$@\""

# The \""$@"\" quotes around all the remaining args.  This entire quoted
# list is passed directly to javarifier.Main, which correctly parses the
# remaining arguments.
