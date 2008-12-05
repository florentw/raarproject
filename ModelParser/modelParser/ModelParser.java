package modelParser;

import java.io.*;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import modelParser.rootManager.*;
import modelParser.rootToMSM.*;

public class ModelParser {
    
    protected	PrintWriter fOut;

    /**
     * Contructeur.
     */
    public ModelParser(String ficModelName, RootManager rootManager) throws SAXException, IOException {
	XMLReader saxReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
	saxReader.setContentHandler(new ModelHandler(rootManager));
	saxReader.parse(ficModelName);
    }

    /* Main */
    public static void main(String[] args) {

	String		ficModelName, ficMsmName, ficMsmNameSplit[];
	ModelParser	parser;
	RootManager	rootManager;	
	RootToMSM	rootToMSM;

	if (args.length < 2) {
	    System.err.println("Usage : ModelParser <model a parser> <msm a creer>");
	    System.exit(1);
	}

	ficModelName = args[0];
	ficMsmName = args[1];

	if (!ficMsmName.matches(".*\\.msm$")) {
	    System.err.println("Usage : ModelParser <model a parser> <msm a creer>, il manque .msm");
	    System.exit(1);
	}
	ficMsmNameSplit = ficMsmName.split("\\.msm$");	
	ficMsmName = ficMsmNameSplit[0];

	rootManager = new RootManager(ficMsmName);
	rootToMSM = new RootToMSM(rootManager, ficMsmName);

	try {
	    parser = new ModelParser(ficModelName, rootManager);
	} catch (Throwable t) {
	    t.printStackTrace();
	}

	/* Parsing model */
	System.out.println("\n" + rootManager.toString());

	/* Writing msm */
	System.out.println("Eciture dans le fichier " + ficMsmName + ".msm");
	try {
	    rootToMSM.writeMSM();
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }

}
