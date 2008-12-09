#!/bin/sh

EX=example;

# generate the directories if it doesn't exist

if [ ! -d "example" ]; then
    mkdir example;
fi;

if [ ! -d "dot" ]; then
    mkdir dot;
fi;

if [ ! -d "generator" ]; then
    mkdir generator;
fi;

if [ ! -d "src" ]; then
    mkdir src;
fi;

if [ ! -d "bin" ]; then
    mkdir bin;
fi;

if [ ! -d "obj" ]; then
    mkdir obj;
fi;

if [ ! -d "include" ]; then
    mkdir include;
fi;

FICH=`ls example | wc -l`;

echo "

#####################################################################
#                                                                   #
#           Welcome to the Code generator of the MPI_TEAM           #    
#                                                                   #
#####################################################################
";

if [ $FICH -eq 0 ]; then
    echo "
You need to add examples of petri net in the example directory. 
You can generate one by using Coloane and our ModelParser.
";
    exit 1;
fi;

if [ $# -ne 0 ]; then
    if [ -f "$EX/$1-main.msm" ]; then
	./generator_to_language anlzed-pn-main.msf $EX/$1-main.msm;

	cp runtime.c src;
	cp runtime.h include;
	mv generated.c src/$1.c;
	mv globals.h include/globals.h;

	if [ $# -ne 1 ] && [ $2 = "DOT" ]; then
	    sh test_generator.sh $1 DOT;
	else
	    sh test_generator.sh $1;
	fi;
    else
	   echo "
I can't find the file, please open a existing msm file from the example
directory.
The file must be in this format : NAME-main.msm
";
    fi;
fi;