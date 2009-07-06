# Testing framework customized for the Jolden case study.
# Run 'make all' to execute all the tests and verify the results.
# The output is written to .output files, and then matched against
# the corresponding .goal files, and the diff is left in .diff files.

SHELL = /bin/sh

DIFFS := $(wildcard *.goal)
DIFFS := $(patsubst %.goal, %.diff, $(DIFFS))

default : all

.PHONY: all
all : clean $(DIFFS) results

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
# For the jolden case study, explicitly list all the classes that need to be
# analyzed for each program.
voronoi.output:
	cd voronoi && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/voronoi \
	-output ${PWD}/voronoi.output \
	-outputFormat annotationIndexFile \
	Edge EdgePair MyDouble Vec2 Vertex Voronoi 2>&1 \
	| tee ${PWD}/voronoi.log

health.output:
	cd health && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/health \
	-output ${PWD}/health.output \
	-outputFormat annotationIndexFile \
	Health Hospital List Patient Results Village 2>&1 \
	| tee ${PWD}/health.log

mst.output:
	cd mst && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/mst \
	-output ${PWD}/mst.output \
	-outputFormat annotationIndexFile \
	BlueReturn Graph HashEntry Hashtable MST Vertex 2>&1 \
	| tee ${PWD}/mst.log

perimeter.output:
	cd perimeter && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/perimeter \
	-output ${PWD}/perimeter.output \
	-outputFormat annotationIndexFile \
	BlackNode GreyNode NorthEast NorthWest Perimeter QuadTreeNode Quadrant \
	SouthEast SouthWest WhiteNode 2>&1 \
	| tee ${PWD}/perimeter.log

em3d.output:
	cd em3d && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/em3d \
	-output ${PWD}/em3d.output \
	-outputFormat annotationIndexFile \
	BiGraph Em3d Graph Node 2>&1 \
	| tee ${PWD}/em3d.log

treeadd.output:
	cd treeadd && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/treeadd \
	-output ${PWD}/treeadd.output \
	-outputFormat annotationIndexFile \
	TreeAdd TreeNode 2>&1 \
	| tee ${PWD}/treeadd.log

bh.output:
	cd bh && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/bh \
	-output ${PWD}/bh.output \
	-outputFormat annotationIndexFile \
	BH Body Cell MathVector Node Tree 2>&1 \
	| tee ${PWD}/bh.log

tsp.output:
	cd tsp && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/tsp \
	-output ${PWD}/tsp.output \
	-outputFormat annotationIndexFile \
	TSP Tree 2>&1 \
	| tee ${PWD}/tsp.log

power.output:
	cd power && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/power \
	-output ${PWD}/power.output \
	-outputFormat annotationIndexFile \
	Branch Demand Lateral Leaf Power Root 2>&1 \
	| tee ${PWD}/power.log

bisort.output:
	cd bisort && javac -g *.java
	javarifier \
	-programCPEntries ${PWD}/bisort \
	-output ${PWD}/bisort.output \
	-outputFormat annotationIndexFile \
	BiSort Value 2>&1 \
	| tee ${PWD}/bisort.log

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
