#!/bin/bash
# usage: run-test.sh <TestClassName>

set -e
# Go up to the Javarifier directory
cd ..

# Get the locations of the things we need
TESTS=tests
ANNO_JDK_BIN=$(pathtool --get-project-location annotated-jdk)/bin
JAVARI_BUILTINS_BIN=$(pathtool --get-project-location javari-builtins)/bin
NONNULL_BUILTINS_BIN=$(pathtool --get-project-location nonnull-builtins)/bin
# The JDK isn't really a project, but pathtool lets people customize just the same.
# *** Configure the location of your JDK 1.6.0 in locations.xml ***
JDK=$(pathtool --get-project-location jdk1.6.0)/jre/lib/rt.jar

# Collect classpaths
SOOT_PROGRAMS="$TESTS"
SOOT_STUBS="$ANNO_JDK_BIN:$JAVARI_BUILTINS_BIN:$NONNULL_BUILTINS_BIN"
SOOT_WORLDS="$JDK"

OUT_FILE="tests/$1.jaif"

# Load test-specific options
TS_OPTIONS_FILE="tests/$1.options"
if [ -r "$TS_OPTIONS_FILE" ]; then
	TS_OPTIONS="$(< "$TS_OPTIONS_FILE")"
else
	TS_OPTIONS=""
fi

set -x

./javarify.sh "$SOOT_PROGRAMS" "$SOOT_STUBS" "$SOOT_WORLDS" \
	-output "$OUT_FILE" -outputFormat scenePrinter \
	-outputMainClassFirst $TS_OPTIONS "$@"
