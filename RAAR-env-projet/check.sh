#!/bin/sh

EX=JDT;

# generate the directories if it doesn't exist

if [ ! -d "JDT" ]; then
    mkdir JDT;
    mkdir JDT/image;
    mkdir JDT/coloane;
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

FICH=`ls $EX | wc -l`;

echo "

#####################################################################
#                                                                   #
#           Welcome to the Code generator of the MPI_TEAM           #    
#                                                                   #
#####################################################################
";

if [ $FICH -eq 0 ]; then
    echo "
You need to add examples of petri net in the $EX directory. 
You can generate one by using Coloane and our ModelParser.
";
    exit 1;
fi;

if [ $# -eq 3 ]; then
    if [ $1 = "NOT_DEFINED" ]; then
	echo "
Please open an existing msm file from the $EX directory.

To run the project :

                make test-pour-fk NAME=<NAME> TIME=<TIME>

The file must be in this format : <NAME>-main.msm
You can also generate a dot file with :

                make dot NAME=<NAME>  TIME=<TIME>

<TIME> is optional and will be set at 20 secondes by default.
";	
	exit 1;
    else
	if [ -f "$EX/$1-main.msm" ];  then
	    ./generator_to_language anlzed-pn-main.msf $EX/$1-main.msm;
	    
	    mv generated.c src/$1.c;
	    mv globals.h include/globals.h;
	    
	    sh test_generator.sh $1 $2 $3;
	else
	    echo "
I can't find the file, please open an existing msm file from the $EX
directory.

To run the project :

                make test-pour-fk NAME=<NAME>  TIME=<TIME>

The file must be in this format : <NAME>-main.msm
You can also generate a dot file with :

                make dot NAME=<NAME>  TIME=<TIME>

<TIME> is optional and will be set at 20 secondes by default.
";
	    exit 1;
	fi;
    fi;
fi;