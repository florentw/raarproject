Welome to the RAAR project of the MPI TEAM.

This readme will give the instructions on how to run the RAAR project.

If you want to add ip adress or host name in bhost, you need to create or/and to write into bhost the ip adress or hostnames. If the number of them are lower than the number of nodes required, the script will override bhost and put the localhost with the number of cpu that represents the number of nodes that we need.


In order to run this project, you'll have to execute in the bash, the command make without argument.

This make will compile the metascribe and generate a binary file : generator_to_language which contains all the rules (semantic and syntactical rules) that we need to make the source, header and script to run the project.

If we want to run the test for the project, we need to use the rule : test-pour-fk.

Another important thing to remind is that all the msm files must be in JDT Directory and we only have to give the name to run it. The script will automatically find it in the directory JDT.
All the file must be like : <NAME>-main.msm.
If there are no file in the JDT directory, we will get an error message.

So to launch the test, we need to execute : 
 
make test-pour-fk NAME=<NAME> TIME=<TIME>

where <NAME> is the name without -main.msm and <TIME> is optional.
If you want to give the time of the execution, you can set it with TIME. Otherwise, without it, the program will work perfecty.

To generate a dot file of the execution, we will have to execute :

make dot NAME=<NAME> TIME=<TIME>

To sum up : 

make
create or write ip adress in the bhost
make test-pour-fk NAME=<NAME> or make dot NAME=<NAME> with or without TIME=<TIME>

If you want to modify the source.c, you'll have to run

make test-generator NAME=<NAME> NUM=<NUM> TIME=<TIME>

where <NUM> is the number of nodes to run.





