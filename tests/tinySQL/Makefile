# Testing framework to run Javarifier on tinySQL and compare the results.
# Run 'make all' to perform the test and show the results.


SHELL = /bin/sh

DIFFS := $(wildcard *.goal)
DIFFS := $(patsubst %.goal, %.diff, $(DIFFS))

default : all

.PHONY: all
all : clean $(DIFFS) results

# Display results of all .diff files.
.PHONY: results
results: ../VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@-java -cp ../:${CLASSPATH} VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
../VerifyDiffs.class : ../VerifyDiffs.java
	@cd .. && javac VerifyDiffs.java

# Actually runs the Javarifier to create the inferences.
tinySQL.output:
	javarifier \
	-programCPEntries ${PWD} \
	-output ${PWD}/tinySQL.output \
	-outputFormat annotationIndexFile \
	-stubs ${CLASSPATH} \
	ORG.as220.tinySQL.tinySQL 2>&1 \
	| tee ${PWD}/tinySQL.log


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
