# Testing framework to run Javarifier on htmlparser and compare the results.
# Run 'make all' to perform the test and show the results.


SHELL = /bin/sh

DIFFS := $(patsubst %.goal, %.diff, $(wildcard *.goal))

JAVARIFIER ?= ../../scripts/javarifier

default : all

.PHONY: all
all : clean $(DIFFS) results

# Display results of all .diff files.
.PHONY: results
results: ../VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@-java -cp ../ VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
../VerifyDiffs.class : ../VerifyDiffs.java
	@cd .. && javac VerifyDiffs.java

# Actually runs the Javarifier to create the inferences.
htmlparser.output:
	${JAVARIFIER} \
	-programCPEntries . \
	-output htmlparser.output \
	-outputFormat annotationIndexFile \
	-stubs ${CLASSPATH} \
	org.htmlparser.Parser 2>&1 \
	| tee htmlparser.log


# Compare the output of the Javarifier and the goal file.
%.diff: %.output %.goal
	-diff -u $*.output $*.goal >& $*.diff

# Remove all .diff, .log files from the tests directory,
# and all output from the javarifierOutput directory.
.PHONY: clean
clean :
	rm -f *.diff
	rm -f *.log
	rm -f *.output
