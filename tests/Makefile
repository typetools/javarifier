# Very rough testing framework for the Javarifier that works for current
# Javarifier setup.  Running 'make all' (this Makefile should be in the
# javarifier/tests directory) will look for all myClass.goal files
# in this directory, run the Javarifier on the corresponding
# myClass.class file, and then output the difference in a
# myClass.diff file in this directory.
#
# To test just one class file, use 'make myPackage.myClass.diff'.

SHELL = /bin/sh

DIFFS := $(wildcard *.goal)
DIFFS := $(patsubst %.goal, %.diff, $(DIFFS))

default : all

.PHONY: all
all : clean compile $(DIFFS) jolden htmlparser tinySQL eclipsec results


# Compile all the tests cases together to resolve dependencies.
compile :
	javac -cp .:${CLASSPATH} -g -d . *.java

# Runs Javarifier on the Jolden case studies by running a separate
# Makefile similar to this one.
.PHONY: jolden
jolden :
	cd jolden && $(MAKE) all

# Runs Javarifier on the htmlparser case studies by running a separate
# Makefile similar to this one.
.PHONY: htmlparser
htmlparser :
	cd htmlparser && $(MAKE) all

# Runs Javarifier on the tinySQL case studies by running a separate
# Makefile similar to this one.
.PHONY: tinySQL
tinySQL :
	cd tinySQL && $(MAKE) all

# Runs Javarifier on the eclipsec case studies by running a separate
# Makefile similar to this one.
.PHONY: eclipsec
eclipsec :
	cd eclipsec && $(MAKE) all

# Runs Javarifier on the annotation scene library as another test case
# just like all the simple test cases.  Operates over two select high-level
# classes so that all classes in the annotation scene library end up
# being analyzed.
# TODO: scene-lib.goal seems to have somehow been removed,
# and needs to be added.
scene-lib.output :
	cd ${ANNOTATION_SCENE_LIB} && ant bin
	cd .. && ./javarify.sh \
	${ANNOTATION_SCENE_LIB}/bin \
	-stubs ${SOOT_STUBS} \
	-world ${JRE1_6_0} \
	-output tests/scene-lib.output \
	-outputFormat annotationIndexFile \
	annotations.io.IndexFileParser annotations.io.IndexFileWriter \
	2>&1 | tee tests/$scene-lib.log

# Display results of all .diff files.
.PHONY: results
results: VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@-java VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
VerifyDiffs.class : VerifyDiffs.java
	@javac VerifyDiffs.java

# Actually runs the Javarifier to create the inferences.
# Use ${PWD}, which is the tests folder, so that javarifier
# executable can be anywhere.
.PRECIOUS: %.output
%.output: %.class
	javarifier \
	-programCPEntries ${PWD} \
	-output ${PWD}/$*.output \
	-outputFormat annotationIndexFile \
	-outputMainClassFirst \
	$* 2>&1 | tee ${PWD}/$*.log

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
#	rm -f *.class
