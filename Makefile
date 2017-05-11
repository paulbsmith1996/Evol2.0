# Final Project: Darwinian Evolution
# Makefile

####################
## UNIVERSAL ARGS ##
####################

JC=javac
RM=rm -f

SRC=Evol.java
OBJ=$(SRC:.java=.class)
CLASSES=$(wildcard *.class)

######################
## RULE DEFINITIONS ##
######################

# Necessary for specifying which rule targets don't actually refer to filenames,
# but are just commands instead.
.PHONY: all run evol clean fresh

# Compile everything (e.g., `make`).
all: clean evol

# Launch the applet (e.g., `make run`).
run: clean evol
	appletviewer Evol.java

# Compile Evol code (e.g., `make evol`).
evol:
	$(JC) $(SRC)

# Remove .class files.
clean:
	$(RM) $(CLASSES)
