# Testing framework for Javarifier.
# Run 'make all' to perform the test and show the results.

TESTNAME = eclipsec
MAINCLASS = org.eclipse.jdt.internal.compiler.Compiler

SHELL = /bin/sh

DIFFS := $(patsubst %.goal, %.diff, $(wildcard *.goal))

JAVARIFIER ?= ../../scripts/javarifier

default : all

.PHONY: all
all : $(DIFFS) results

# Display results of all .diff files.
.PHONY: results
results: ../VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@-java -cp .. VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
../VerifyDiffs.class : ../VerifyDiffs.java
	${MAKE} -C .. VerifyDiffs.class

# Actually runs the Javarifier to create the inferences.
${TESTNAME}.output:
	${JAVARIFIER} \
	-programCPEntries . \
	-output $@ \
	-outputFormat annotationIndexFile \
	-stubs ${CLASSPATH} \
	${MAINCLASS} \
	2>&1 | tee ${TESTNAME}.log


# Compare the output of the Javarifier and the goal file.
%.diff: %.goal %.output
	-diff -u $*.goal $*.output > $*.diff 2>&1

# Remove all .diff, .log files from the tests directory,
# and all output from the javarifierOutput directory.
.PHONY: clean
clean :
	rm -f *.diff
	rm -f *.log
	rm -f *.output
