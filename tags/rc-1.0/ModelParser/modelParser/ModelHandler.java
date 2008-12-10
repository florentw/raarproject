package modelParser;

import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

import java.lang.*;

import modelParser.rootManager.*;

/* Pour savoir ce que l'on parse */
enum Job {
    NODE, // node + nodetype='place'
	TRANSITION, // node + nodetype='transition'
	ARC, // arcs
	NAN // rien a parser
}

/* Pour savoir ce qu'est le texte */
enum Texte {
    MARK, // Jeton
	NAME, // Nom
	VALUATION, // Valuation d'un arc
	NAN // rien
}

public class ModelHandler implements ContentHandler {

    private Locator locator;
    private RootManager rootManager;

    /* Parsing en cours */
    private	Job job = Job.NAN;
    private	Texte texte = Texte.NAN;
    /* Parametres */
    private	int mark = 0;
    private	int valuation = 0;
    private	int startid, endid;
    private	int id;
    private	String name;

    /**
     * Constructeur par defaut. 
     */
    public ModelHandler() {
	super();
	// On definit le locator par defaut.
	locator = new LocatorImpl();
    }

    public ModelHandler(RootManager rootManager) {
	this();
	this.rootManager = rootManager;
    }

    /**
     * Definition du locator qui permet a tout moment pendant l'analyse, de localiser
     * le traitement dans le flux. Le locator par defaut indique, par exemple, le numero
     * de ligne et le numero de caractere sur la ligne.
     * @author smeric
     * @param value le locator a utiliser.
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator value) {
	locator =  value;
    }

    /**
     * Evenement envoye au demarrage du parse du flux xml.
     * @throws SAXException en cas de probleme quelquonque ne permettant pas de
     * se lancer dans l'analyse du document.
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
	//System.out.println("Debut de l'analyse du document");
	/*System.out.println("main_file");
	System.out.println("formalism ( \'ANLZED_PN\' );");
	System.out.println("// Attributs globaux");
	System.out.println("where (attribute NAME => 'full_synchro');")*/
    }

    /**
     * Evenement envoye a la fin de l'analyse du flux xml.
     * @throws SAXException en cas de probleme quelquonque ne permettant pas de
     * considerer l'analyse du document comme etant complete.
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
	System.out.println("Fin de l'analyse du document" );
    }

    /**
     * Debut de traitement dans un espace de nommage.
     * @param prefixe utilise pour cet espace de nommage dans cette partie de l'arborescence.
     * @param URI de l'espace de nommage.
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String URI) throws SAXException {
	//System.out.println("Traitement de l'espace de nommage : " + URI + ", prefixe choisi : " + prefix);
    }

    /**
       Retourne la valeur de l'attribut selon son nom
       @param attributs liste des attributs
       @param attrName nom de l'attribut a rechercher
       @return la valeur
    */

    private String getAttr(Attributes attributs, String attrName) {

	for (int index = 0; index < attributs.getLength(); index++) {
	    if(attributs.getLocalName(index).equals(attrName))
		return attributs.getValue(index);
	}

	return null;

    }

    /**
     * Fin de traitement de l'espace de nommage.
     * @param prefixe le prefixe choisi a l'ouverture du traitement de l'espace nommage.
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
	//System.out.println("Fin de traitement de l'espace de nommage : " + prefix);
    }

    /**
     * Evenement recu a chaque fois que l'analyseur rencontre une balise xml ouvrante.
     * @param nameSpaceURI l'url de l'espace de nommage.
     * @param localName le nom local de la balise.
     * @param rawName nom de la balise en version 1.0 <code>nameSpaceURI + ":" + localName</code>
     * @throws SAXException si la balise ne correspond pas a ce qui est attendu,
     * comme par exemple non respect d'une dtd.
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String nameSpaceURI, String localName, String rawName, Attributes attributs) throws SAXException {
	
	/* nodes */
	if(localName.equals("nodes")) {
	    System.out.println("Parsing nodes...");
	}
	/* node */
	else if(localName.equals("node")) {
	    //System.out.println("Parsing 1 node...");
	    name = null;
	    id = Integer.parseInt(getAttr(attributs, "id"));
	    /* NODE */
	    if(getAttr(attributs, "nodetype").equals("place")) {
		mark = 0;
		job = Job.NODE;
	    }
	    /* TRANSITION */
	    else {
		job = Job.TRANSITION;
	    }
		
	}
	/* arcs */
	else if(localName.equals("arcs")) {	    
	    System.out.println("Parsing arcs...");
	    job = Job.ARC;
	}
	/* arc */
	else if(localName.equals("arc")) {
	    valuation = 0;
	    startid = Integer.parseInt(getAttr(attributs, "startid"));
	    endid = Integer.parseInt(getAttr(attributs, "endid"));
	    job = Job.ARC;
	    //System.out.println("Parsing 1 arc...");
	    /*try {
	      rootManager.addArc(Integer.parseInt(getAttr(attributs, "startid")),
	      Integer.parseInt(getAttr(attributs, "endid")));
	    } catch(Exception e) {
		e.printStackTrace();		
	    }*/
	}

	/* attribute */
	else if(localName.equals("attribute")) {
	    //System.out.println("Parsing attribute...");
	    /* NODE */
	    if(job == Job.NODE) {
		if(getAttr(attributs, "name").equals("marking"))
		    texte = Texte.MARK;
		else if(getAttr(attributs, "name").equals("name"))
		    texte = Texte.NAME;
	    }
	    /* TRANSITION */
	    else if(job == Job.TRANSITION) {
		texte = Texte.NAME;
	    }
	    /* ARC */
	    else if(job == Job.ARC) {
		texte = Texte.VALUATION;
	    }
	}

    }

    /**
     * Evenement recu a chaque fermeture de balise.
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String nameSpaceURI, String localName, String rawName) throws SAXException {


	try {
	    /* nodes */
	    if(localName.equals("nodes")) {
		job = Job.NAN;
	    }
	    /* node */
	    else if(localName.equals("node")) {
		if(job == Job.NODE) {
		    //System.out.println("ajout du noeud "+name);
		    rootManager.addNode(name, id, mark);
		}
		else if(job == Job.TRANSITION) {		    
		    rootManager.addTrans(name, id);
		}
		job = Job.NAN;
	    }
	    /* arc */
	    else if(localName.equals("arc")) {
		try {
		    rootManager.addArc(startid, endid, valuation);
		} catch(Exception e) {
		    e.printStackTrace();		
		}
	    }
	    /* attibute */
	    else if(localName.equals("attribute")) {
		texte = Texte.NAN;
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Evenement recu a chaque fois que l'analyseur rencontre des caracteres (entre
     * deux balises).
     * @param ch les caracteres proprement dits.
     * @param start le rang du premier caractere a traiter effectivement.
     * @param end le rang du dernier caractere a traiter effectivement
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int end) throws SAXException {
	
	String val = new String(ch, start, end);

	switch(texte) {
	case MARK : {
	    mark = Integer.parseInt(val);
	    break;
	}
	case NAME : {
	    name = val;
	    break;
	}
	case VALUATION : {
	    valuation = Integer.parseInt(val);
	}
	}

	//System.out.println("texte : " + new String(ch, start, end));
    }

    /**
     * Recu chaque fois que des caracteres d'espacement peuvent etre ignores au sens de
     * XML. C'est a dire que cet evenement est envoye pour plusieurs espaces se succedant,
     * les tabulations, et les retours chariot se succedants ainsi que toute combinaison de ces
     * trois types d'occurrence.
     * @param ch les caracteres proprement dits.
     * @param start le rang du premier caractere a traiter effectivement.
     * @param end le rang du dernier caractere a traiter effectivement
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
	System.out.println("espaces inutiles rencontres : ..." + new String(ch, start, end) +  "...");
    }

    /**
     * Rencontre une instruction de fonctionnement.
     * @param target la cible de l'instruction de fonctionnement.
     * @param data les valeurs associees a cette cible. En general, elle se presente sous la forme 
     * d'une serie de paires nom/valeur.
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
	System.out.println("Instruction de fonctionnement : " + target);
	System.out.println("  dont les arguments sont : " + data);
    }

    /**
     * Recu a chaque fois qu'une balise est evitee dans le traitement a cause d'un
     * probleme non bloque par le parser. Pour ma part je ne pense pas que vous
     * en ayez besoin dans vos traitements.
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String arg0) throws SAXException {
	// Je ne fais rien, ce qui se passe n'est pas franchement normal.
	// Pour eviter cet evenement, le mieux est quand meme de specifier une dtd pour vos
	// documents xml et de les faire valider par votre parser.              
    }


}
