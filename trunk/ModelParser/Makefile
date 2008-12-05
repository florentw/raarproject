JC=javac
JRUN=java
PARSER_DIR=modelParser
ROOTMANAGER_DIR=$(PARSER_DIR)/rootManager
ROOTOMSM_DIR=$(PARSER_DIR)/rootToMSM

PARSER_SRC=$(wildcard $(PARSER_DIR)/*.java)
ROOTMANAGER_SRC=$(wildcard $(ROOTMANAGER_DIR)/*.java)
ROOTOMSM_SRC=$(wildcard $(ROOTOMSM_DIR)/*.java)

# xerces.jar necessaire
XERCES_JAR=~/xerces-1_4_4/xerces.jar

USAGE="Usage : make exec M=<model> MSM=<msm>"

.PHONY : all clean

.SUFFIXES: .java .class

all : parser

parser : $(PARSER_DIR)/ModelParser.class
manager : $(ROOTMANAGER_DIR)/RootManager.class
msm : $(ROOTOMSM_DIR)/RootToMSM.class

$(PARSER_DIR)/ModelParser.class : $(PARSER_SRC) $(ROOTMANAGER_SRC) $(ROOTOMSM_SRC)
	$(JC) $(@:.class=.java)

$(ROOTMANAGER_DIR)/RootManager.class : $(ROOTMANAGER_SRC)
	CLASSPATH=$$CLASSPATH:. $(JC) $(@:.class=.java)

$(ROOTOMSM_DIR)/RootToMSM.class : $(ROOTOMSM_SRC) $(ROOTMANAGER_SRC)
	CLASSPATH=$$CLASSPATH:. $(JC) $(@:.class=.java)

exec : $(PARSER_DIR)/ModelParser.class
ifndef M
	@echo $(USAGE)
	@exit 1	
endif
ifndef MSM
	@echo $(USAGE)
	@exit 1	
endif
	CLASSPATH=$$CLASSPATH:$(XERCES_JAR) $(JRUN) modelParser.ModelParser $(M) $(MSM)

clean :
	rm -f *~ $(PARSER_DIR)/*~ $(PARSER_DIR)/*.class \
	$(ROOTMANAGER_DIR)/*~ $(ROOTMANAGER_DIR)/*.class \
	$(ROOTOMSM_DIR)/*~ $(ROOTOMSM_DIR)/*.class
