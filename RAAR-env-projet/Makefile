###################
# fko, 18/10/2007
###################
## Vous devez positionner cette variable de la sorte si vous
## installez le package du TME dans le m�me r�pertoire que
## le package de MetaScribe.
CONTAINER_TDIR			= ../executables
#CONTAINER_TDIR			= /Users/admin/MetaScribe/executables
#CONTAINER_TDIR			= /Vrac/MetaScribe/executables

EXECUTABLE_DIR			= $(CONTAINER_TDIR)/`uname -s`
SOURCES_DIR				= $(CONTAINER_TDIR)/`uname -s`_CMP

METASCRIBE				= $(EXECUTABLE_DIR)/meta_scribe
SHOW_MODEL				= $(EXECUTABLE_DIR)/show_model
XML_EXPORT				= $(EXECUTABLE_DIR)/xml_export
XML_IMPORT				= $(EXECUTABLE_DIR)/xml_import

SEARCH_DIR_OPTION       = -aL$(SOURCES_DIR) -aI$(SOURCES_DIR)

# REMARQUE: DEBUG est une variable que l'on peut definir en invoquant "make".
# C'est utile pour en savoir plus...

###################################################
# Pour les questions...
###################################################

# Nettoyage + recompilation totale...
generator:
	make superclean
	make rules-generator
	make tengine-generator
	make test-generator

# Invocation de metascribe pour produire les sources du generateur de code
rules-generator:
	$(METASCRIBE) $(DEBUG) anlzed-pn-main.msf generator-main.mssm to-language-main.msst -v "1.0" -a "C_MPI Generator Team"
	gnatchop -w *.ad[sb]
	rm -f smr* syr* smt*
	mv generator_to_language.adb generator_to_language.ads

# compilation de generateur de code (en utilisant les sources de metascribe)
tengine-generator: generator_to_language.ads
	gnatmake generator_to_language.ads -gnatf $(SEARCH_DIR_OPTION) -bargs -static

# On lance les tests
test-generator:
	./generator_to_language anlzed-pn-main.msf anlzed-pn-3.msm
	@echo -e "\n\nThis is the code generated:"
	cat hello.c
	@echo -e "\n\nLaunching LAM..."
	lamboot -v bhost
	@echo -e "\n\nCompiling the code..."
	mpicc -Wall -o hello hello.c
	@echo -e "\n\nExecuting the program..."
	mpirun -v -c 15 hello
	@echo -e "\n\nClosing LAM..."
	lamwipe -v bhost
	@echo -e "\n\nDone!"

# Le rapport que vous devez fournir
rapport.pdf: *.tex *.eps
	latex rapport.tex
	latex rapport.tex
	dvips -f rapport.dvi > rapport.ps
	ps2pdf rapport.ps
	rm rapport.ps

# A faire...
test-pour-fk:
	echo "je genere le code pour $(NAME).msm"
	echo "je compile le code genere pour le modele $(NAME)"
	echo "j'execute le code genere pour $(NAME)"

###################################################
# AUTRES...
###################################################
# On nettoie ce qui a ete construit (en particulier les sources generes par metascribe)
clean:
	rm -f *.ali *.o *.0 *.log *.aux *.dvi *.toc *.out

# Supprimer les fichiers generes par la compilation
bigclean:
	make clean
	rm -f *.ads *.adb hello.c

# supprimer aussi le generateur de code produit par metascribe ainsi que les
superclean:
	make bigclean
	rm -f generator_to_language hello *.xml *.dtd rapport.pdf

# exporte le format MSM en un format XML avec sa DTD pour l'exemple donne
# (regardez l'equivalence entre MSM et XML)
rdpxml1:
	$(XML_EXPORT) -generic -f anlzed-pn-main.msf -m anlzed-pn-1.msm -o exemple
	ls *.xml *.dtd

rdpxml2:
	$(XML_EXPORT) -generic -f anlzed-pn-main.msf -m anlzed-pn-2.msm -o exemple
	ls *.xml *.dtd

# Visualiser un fichir MSM (et le tester par rapport a son MSF)
showinput1:
	$(SHOW_MODEL) anlzed-pn-main.msf anlzed-pn-1.msm
	cat ShowModel.0
	rm ShowModel.0

showinput2:
	$(SHOW_MODEL) anlzed-pn-main.msf anlzed-pn-2.msm
	cat ShowModel.0
	rm ShowModel.0

#construire une distrib pour les etudiants de master
distrib:
	mkdir ARCHIVE
	mkdir ARCHIVE/RAAR-env-projet
	cp frmwrk_msg.ENGLISH *.msm *.msf *.mssm *.msst Makefile ARCHIVE/RAAR-env-projet
	cp rapport.tex logo-sar.eps ARCHIVE/RAAR-env-projet
	cp ../READ_ME-RAAR ARCHIVE
	sh -c '(cd ARCHIVE ;tar czfv ~/Desktop/RAAR-proj-MS.tgz RAAR-env-projet READ_ME-RAAR)'
	rm -rf ARCHIVE
