# Very rough testing framework for the Javarifier that works for current
# Javarifier setup.  Running 'make all' (this Makefile should be in the
# javarifier/tests directory) will look for all myClass.jaif.goal files
# in this directory, run the Javarifier on the corresponding
# myClass.class file, and then output the difference in a
# myClass.diff file in this directory.
#
# To test just one class file, use 'make myPackage.myClass.diff'.

# Not /bin/sh because dash does not support "set -o pipefail".
SHELL = /bin/bash

TOPLEVEL_DIFFS := $(patsubst %.goal, %.diff, $(sort $(wildcard *.goal)))
TOPLEVEL_TYPECHECK_DIFFS :=  $(patsubst %.annotated.goal, %.typecheck.diff, $(sort $(wildcard *.annotated.goal)))

JAVA_FILES := $(sort $(wildcard *.java))
JAVARIFIER_JAVA_FILES := $(sort $(wildcard ../src/*.java))

JAVARIFIER ?= ../scripts/javarifier
JRE_HOME ?= $(shell java -cp $(JAVARIFIER)/scripts PrintJreHome)

#Since the Checker Framework is required to run this script
#use the Checker Framework compiler
JAVAC = ${CHECKERS}/binary/javac
ifneq ($(wildcard ${JAVAC}), ${JAVAC})
    $(error "Couldn't find the Checker Framework compiler at ${JAVAC}" )
endif

AFU ?= ../../annotation-tools/annotation-file-utilities

.PHONY: default
default : all

.PHONY: all
all : toplevel jolden htmlparser tinySQL eclipsec results

.PHONY: toplevel
toplevel: toplevel-diffs
toplevel-all: toplevel-diffs toplevel-typecheck
toplevel-diffs: $(TOPLEVEL_DIFFS)
# TODO: add typecheck targets to the subprojects
toplevel-typecheck: $(TOPLEVEL_TYPECHECK_DIFFS)


## I'm hoping this is not necessary, because a change to any file then
## forces every test to be re-run.  -MDE 8/22/2009
# # Compile all the test cases together to resolve dependencies.
# compile : .compile-timestamp
# .compile-timestamp : ${JAVA_FILES}
# 	javac -classpath . -g -d . ${JAVA_FILES}

.PRECIOUS: %.class
%.class : %.java
	$(JAVAC) -classpath .:${CHECKERS}/build:${CHECKERS}/binary/checkers.jar -g -d . $^

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
scene-lib.jaif :
	rm -f tests/scene-lib.jaif
	cd ${ANNOTATION_SCENE_LIB} && ant bin
	cd .. && ./javarify.sh \
	${ANNOTATION_SCENE_LIB}/bin \
	-world ${JRE1_6_0} \
	-output tests/scene-lib.jaif \
	-outputFormat jaif \
	annotations.io.IndexFileParser annotations.io.IndexFileWriter \
	2>&1 | tee tests/$scene-lib.log

# Display results of all .diff files.  Always succeeds.
.PHONY: results
results: VerifyDiffs.class
	@echo ""
	@echo "=== RESULTS ==="
	@echo ""
	@java -classpath . VerifyDiffs --show_all

# Remakes the little Java program that checks and compares diffs.
VerifyDiffs.class : VerifyDiffs.java
	@$(JAVAC) VerifyDiffs.java

# Only compile if a .java file has changed since the last compilation.
# (The Ant file is not smart enough to do this yet.)
../.compile-timestamp: $(JAVARIFIER_JAVA_FILES)
	(cd /homes/gws/mernst/research/types/javarifier; ant -e bin)

# Actually runs the Javarifier to create the inferences.
.PRECIOUS: %.jaif
# Depending on ../bin doesn't work; the directory isn't updated when a file
# in it is updated, only when one is added.
# TODO: Add
#	$(shell cat $*.options) \
# to the below command.  This will only affect OpenWorld.java for now.
%.jaif: %.class ../.compile-timestamp
	rm -f $@
	${JAVARIFIER} \
	-d \
	--programCPEntries . \
	--output $@ \
	--outputFormat jaif \
	--outputMainClassFirst \
	$* \
	2>&1 | tee $*.log

# Compare the output of the Javarifier and the goal file.
%.jaif.diff: %.jaif.goal %.jaif
	-diff -u $*.jaif.goal $*.jaif > $*.jaif.diff 2>&1

# The "%.jaif" in this target does not work.  If it does not exist, "make -d" output just says:
#  Prerequisite `SimpleCell.jaif' of target `SimpleCell.annotated.diff' does not exist.
%.annotated.diff: %.annotated.goal annotated/%.java %.jaif
	-diff -u $*.annotated.goal annotated/$*.java > $*.annotated.diff 2>&1

.PRECIOUS: annotated/%.java
annotated/%.java: %.java %.jaif %.class
	CLASSPATH=${CLASSPATH}:. $(AFU)/scripts/insert-annotations-to-source ${DEBUG_INSERT_ANNOTATIONS} --omit-annotation=@checkers.javari.quals.ThisMutable $*.jaif  $*.java

## Why doesn't -sourcepath version work for finding the JDK??

%.typecheck.diff: annotated/%.java
# ".." in classpath is for helper classes like Cell.
	set -o pipefail && $(JAVAC) -processor checkers.javari.JavariChecker -classpath annotated:${CLASSPATH}:${CHECKERS}/build:${CHECKERS}/binary/checkers.jar -implicit:class annotated/$*.java 2>&1 | tee $*.typecheck.diff


# Remove all .diff, .log files from the tests directory,
# and all output from the javarifierOutput directory.
.PHONY: clean clean-toplevel
clean-toplevel :
	rm -rf annotated
	rm -f *.diff
	rm -f *.log
	rm -f *.jaif
	rm -f *.class
# obsolete files
	rm -f *.output
clean-annotated:
	rm -rf annotated

clean : clean-toplevel
	cd jolden && $(MAKE) clean
	cd htmlparser && $(MAKE) clean
	cd tinySQL && $(MAKE) clean
	cd eclipsec && $(MAKE) clean

.PHONY: showvars
showvars:
	@echo "JAVAC=${JAVAC}"
	@echo "CLASSPATH=${CLASSPATH}"
	@echo "CHECKERS=${CHECKERS}"
	@echo "JRE_HOME=${JRE_HOME}"
	@echo "TOPLEVEL_DIFFS=${TOPLEVEL_DIFFS}"
