#!/bin/sh

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

echo "

#####################################################################
#                                                                   #
#           Welcome to the Code generator of the MPI_TEAM           #    
#                                                                   #
#####################################################################
";


if [ $# -eq 3 ]; then
    if [ $1 = "NOT_DEFINED" ]; then
	echo "
Please enter a msm file.

To run the project :

                make test-pour-fk NAME=DIR/<NAME> TIME=<TIME>

The file must be in this format : <NAME>-main.msm
DIR is the directory where the file is.
You can also generate a dot file with :

                make dot NAME=DIR/<NAME>  TIME=<TIME>

<TIME> is optional and will be set at 20 secondes by default.
";	
	exit 1;
    else
	if [ -f "$1-main.msm" ];  then
	    ./generator_to_language anlzed-pn-main.msf $1-main.msm;
	    
	    mv generated.c src/`basename $1`.c;
	    mv globals.h include/globals.h;
	    
	    sh test_generator.sh `basename $1` $2 $3;
	else
	    echo "
I can't find the file, please open an existing msm file.

To run the project :

                make test-pour-fk NAME=DIR/<NAME>  TIME=<TIME>

The file must be in this format : <NAME>-main.msm
DIR is the directory where the file is.
You can also generate a dot file with :

                make dot NAME=DIR/<NAME>  TIME=<TIME>

<TIME> is optional and will be set at 20 secondes by default.
";
	    exit 1;
	fi;
    fi;
fi;