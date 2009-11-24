# Very rough testing framework for the Javarifier that works for current
# Javarifier setup.  Running 'make all' (this Makefile should be in the
# javarifier/tests directory) will look for all myClass.goal files
# in this directory, run the Javarifier on the corresponding
# myClass.class file, and then output the difference in a
# myClass.diff file in this directory.
#
# To test just one class file, use 'make myPackage.myClass.diff'.

SHELL = /bin/sh

TOPLEVEL_DIFFS := $(patsubst %.goal, %.diff, $(wildcard *.goal))

JAVA_FILES := $(wildcard *.java)

JAVARIFIER ?= ../scripts/javarifier
JAVAC ?= ../../jsr308-langtools/dist/bin/javac

.PHONY: default
default : all

.PHONY: all
all : toplevel jolden htmlparser tinySQL eclipsec results

.PHONY: toplevel
toplevel: $(TOPLEVEL_DIFFS)


## I'm hoping this is not necessary, because a change to any file then
## forces every test to be re-run.  -MDE 8/22/2009
# # Compile all the test cases together to resolve dependencies.
# compile : .compile-timestamp
# .compile-timestamp : ${JAVA_FILES}
# 	javac -cp . -g -d . ${JAVA_FILES}

%.class : %.java
	$(JAVAC) -cp . -g -d . $^

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
	@-java -cp . VerifyDiffs --show_all

# Remakes the little java program that checks and compares diffs
VerifyDiffs.class : VerifyDiffs.java
	@javac VerifyDiffs.java

# Actually runs the Javarifier to create the inferences.
.PRECIOUS: %.output
%.output: %.class ../javarifier.jar
	${JAVARIFIER} \
	-programCPEntries . \
	-output $@ \
	-outputFormat annotationIndexFile \
	-outputMainClassFirst \
	$* \
	2>&1 | tee $*.log

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
	rm -f *.class
	cd jolden && $(MAKE) clean
	cd htmlparser && $(MAKE) clean
	cd tinySQL && $(MAKE) clean
	cd eclipsec && $(MAKE) clean
