# Testing framework customized for the Jolden case study.
# Run 'make all' to execute all the tests and verify the results.
# The output is written to .output files, and then matched against
# the corresponding .goal files, and the diff is left in .diff files.

SHELL = /bin/sh

DIFFS := $(patsubst %.goal, %.diff, $(wildcard *.goal))

JAVARIFIER ?= ../../javarifier

default : all

.PHONY: all
all : clean $(DIFFS) results

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
# For the jolden case study, explicitly list all the classes that need to be
# analyzed for each program.
voronoi.output:
	cd voronoi && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries voronoi \
	-output voronoi.output \
	-outputFormat annotationIndexFile \
	Edge EdgePair MyDouble Vec2 Vertex Voronoi 2>&1 \
	| tee voronoi.log

health.output:
	cd health && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries health \
	-output health.output \
	-outputFormat annotationIndexFile \
	Health Hospital List Patient Results Village 2>&1 \
	| tee health.log

mst.output:
	cd mst && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries mst \
	-output mst.output \
	-outputFormat annotationIndexFile \
	BlueReturn Graph HashEntry Hashtable MST Vertex 2>&1 \
	| tee mst.log

perimeter.output:
	cd perimeter && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries perimeter \
	-output perimeter.output \
	-outputFormat annotationIndexFile \
	BlackNode GreyNode NorthEast NorthWest Perimeter QuadTreeNode Quadrant \
	SouthEast SouthWest WhiteNode 2>&1 \
	| tee perimeter.log

em3d.output:
	cd em3d && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries em3d \
	-output em3d.output \
	-outputFormat annotationIndexFile \
	BiGraph Em3d Graph Node 2>&1 \
	| tee em3d.log

treeadd.output:
	cd treeadd && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries treeadd \
	-output treeadd.output \
	-outputFormat annotationIndexFile \
	TreeAdd TreeNode 2>&1 \
	| tee treeadd.log

bh.output:
	cd bh && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries bh \
	-output bh.output \
	-outputFormat annotationIndexFile \
	BH Body Cell MathVector Node Tree 2>&1 \
	| tee bh.log

tsp.output:
	cd tsp && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries tsp \
	-output tsp.output \
	-outputFormat annotationIndexFile \
	TSP Tree 2>&1 \
	| tee tsp.log

power.output:
	cd power && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries power \
	-output power.output \
	-outputFormat annotationIndexFile \
	Branch Demand Lateral Leaf Power Root 2>&1 \
	| tee power.log

bisort.output:
	cd bisort && javac -g *.java
	${JAVARIFIER} \
	-programCPEntries bisort \
	-output bisort.output \
	-outputFormat annotationIndexFile \
	BiSort Value 2>&1 \
	| tee bisort.log

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
