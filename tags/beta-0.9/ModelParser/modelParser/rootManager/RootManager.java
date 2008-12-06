package modelParser.rootManager;

import java.util.*;
import java.lang.*;

/* Pour l'utilisation de getArcInfos */

class ArcInfos {
    
    NProcess belong;
    int startType = NodeType.NAN;
    int endType = NodeType.NAN;

    public ArcInfos(){};

    public ArcInfos(NProcess nproc, int startType, int endType) {
	belong = nproc;
	this.startType = startType;
	this.endType = endType;	
    }

}

public class RootManager {
    
    String name;
    LinkedList<NProcess>nProcs = new LinkedList<NProcess>();
    LinkedList<Channel> channels = new LinkedList<Channel>();
    LinkedList<Sync> allSyncs = new LinkedList<Sync>();

    public RootManager(String name) {
	this.name = name;
    }

    public RootManager() {
	this("");
    }    

    /* Getters */
    public LinkedList<NProcess> getNProcs() {
	return nProcs;
    }

    public LinkedList<Channel> getChannels() {
	return channels;
    }
    
    public LinkedList<Sync> getSyncs() {
	return allSyncs;
    }

    /**
       Teste si nodeName correspond a un channel
       sachant qu'une place ou transition doit ce nommer :
       <nom NProcess>_<suite du nom>, tous le reste sont des channels
       @param nodeName nom de la place ou transition
       @return true ou false
    */

    private boolean isChannel(String nodeName) {

	return !nodeName.matches(".*_.*");

    }

    /**
       Teste si transName correspond a une synchronisation
       transition doit etre nommee sans underscore
       @param nodeName nom de la place ou transition
       @return true ou false
    */

    private boolean isSync(String transName) {

	return (isChannel(transName) || // Les deux fonctions sont equivalentes
		transName.matches(".*_.*[_.*]+.*"));
    }

    private boolean nProcessExist(String nProcName) {

	ListIterator li  = nProcs.listIterator(0);
	NProcess tmp;

	while(li.hasNext()) {
	    tmp = (NProcess)li.next();
	    if(nProcName.equals(tmp.name.toString()))
		return true;
	}

	return false;	
	    
    }

    private NProcess getNProcess(String nProcName) {

	ListIterator li  = nProcs.listIterator(0);
	NProcess tmp;

	while(li.hasNext()) {
	    tmp = (NProcess)li.next();
	    if(nProcName.equals(tmp.name.toString()))
		return tmp;
	}
	
	return null;

    }

    /**
       Retourne le nom du NProcess correspodant,
       sachant qu'une place ou transition doit se nommer :
       <nom NProcess>_<suite du nom>
       @param name nom de la place ou transition
       @return le nom du NProcess
    */
    
    private String getNProcessName(String name) throws Exception {

	String nodeNameSplit[];

	nodeNameSplit = name.split("_");

	if(nodeNameSplit.length == 0)
	    throw(new Exception("nodeNameSplit.length == 0"));

	//System.out.println("nodeNameSplit[0] : " + nodeNameSplit[0]);
	return nodeNameSplit[0];

    }

    /**
       Ajout d'un noeud qui peut etre soit une place, soit un channel
       @param name nom du noeud
       @param id id du noeud
       @param marks nombre de jetons
    */

    public void addNode(String name, int id, int marks) throws Exception {

	String nProcName;
	NProcess nProc = null;
	ListIterator li;
	Sync tmp_sync;

	/* Si channel */
	if(isChannel(name)) {
	    channels.add(new Channel(this, name, id, marks));
	}
	/* Si place locale */
	else {
	    /* On verifie si on doit ajouter un NProcess */
	    nProcName = getNProcessName(name);

	    if(!nProcessExist(nProcName)) {
		//System.out.println(nProcName + " n'existe pas, ajout.");
		nProc = new NProcess(this, nProcName);
		nProcs.add(nProc);
		// Il faut eventuellement ajouter les synchros le concernant 
		li = allSyncs.listIterator();
		while(li.hasNext()) {
		    tmp_sync = (Sync)li.next();
		    nProc.addSync(tmp_sync);
		}
		
	    }
	    else
		nProc = getNProcess(nProcName);

	    /* Ajout de la place dans le bon NProcess */
	    if(nProc == null)
		throw(new Exception("nProc == null"));

	    nProc.addPlace(name, id, marks);
	}

    }    

    /**
       Retourne le NProcess correspondant ayant le nom name
       @param name nom du NProcess
       @return le NProcess
    */

    private NProcess findNProcessByName(String name) {
	
	NProcess tmp_nproc;
	ListIterator li;

	li = nProcs.listIterator();
	while(li.hasNext()) {
	    tmp_nproc = (NProcess)li.next();
	    if(tmp_nproc.name.equals(name.toString()))
		return tmp_nproc;
	}
	
	return null;

    }


    /**
       Ajoute une synchro a tous les NProcess qui la contiennent
       @param name nom "Model" de la synchro
       @param id id "Model" de la synchro
    */

    private void addSyncToNProcess(String name, int id) throws Exception {
	
	String nameSplit[];
	NProcess tmp_nproc;
	ListIterator li;

	nameSplit = name.split("_");

	/* On doit ajouter la synchro a tous les NProcess */
	if(nameSplit.length == 1) {
	    System.out.println("ajout de " + name + " a TOUS");
	    li = nProcs.listIterator();
	    while(li.hasNext()) {
		tmp_nproc = (NProcess)li.next();
		tmp_nproc.addSync(name, id);
	    }
	}
	/* On doit ajouter la synchro seulement a certains NProcess */
	else if(nameSplit.length > 2) {
	    System.out.println("ajout de " + name + " a certains");
	    for(int i=1; i<nameSplit.length; i++) {
		System.out.println("ajout de " + name + " a " + nameSplit[i]);
		tmp_nproc = findNProcessByName(nameSplit[i]);
		tmp_nproc.addSync(name, id);
	    }
	}
	else
	    throw(new Exception("nb '_' == 1 => transition et non synchro"));

    }

    /**
       Ajout d'une transition ou d'un synchro
       @param name nom
       @param id id du .model
    */

    public void addTrans(String name, int id) throws Exception {
	
	String nProcName;
	NProcess nProc = null;

	/* Transition synchronisee */

	if(isSync(name)) {
	    System.out.println(name + " is sync");
	    addSyncToNProcess(name, id);
	    allSyncs.add(new Sync(name, id));
	    return;
	} else
	    System.out.println(name + " is NOT sync");

	/* Transition locale */

	/* On verifie si on doit ajouter un NProcess */
	nProcName = getNProcessName(name);

	if(!nProcessExist(nProcName)) {
	    //System.out.println(nProcName + " n'existe pas, ajout.");
	    nProc = new NProcess(this, nProcName);
	    nProcs.add(nProc);
	}
	else
	    nProc = getNProcess(nProcName);

	nProc.addTrans(name, id);

    }

    /**
       Renvoie les informations sur l'arc tel le processus qui le contient, le type du noeud de depart
       et le type du noeud d'arrivee
       @param startId id du node de depart du .model
       @param endId id du node d'arrivee du .model
       @return les informations ou null si incoherence
     */

    public ArcInfos getArcInfos(int startId, int endId) {

	ListIterator liNProc, li;
	NProcess tmp_nproc;
	Place tmp_place;
	Trans tmp_trans;
	Channel tmp_channel;
	Sync tmp_sync;
	ArcInfos arcInfos = new ArcInfos();
	
	/* Pour tout les NProcess */
	liNProc  = nProcs.listIterator(0);	

	while(liNProc.hasNext()) {
	    tmp_nproc = (NProcess)liNProc.next();

	    /* Recherche dans les places locales */
	    li = tmp_nproc.places.listIterator(0);
	    
	    while(li.hasNext()) {
		tmp_place = (Place)li.next();
		
		if((tmp_place.id == startId) || (tmp_place.id == endId)) {
		     arcInfos.belong = tmp_nproc;
		     if(tmp_place.id == startId)
			 arcInfos.startType = NodeType.PLACE;
		     else
			 arcInfos.endType = NodeType.PLACE;
		}
	    }

	    /* Si on a le resultat */
	    if((arcInfos.startType != NodeType.NAN) && (arcInfos.endType != NodeType.NAN))
		return arcInfos;

	    /* Recherche dans les transitions locales */
	    li = tmp_nproc.transs.listIterator(0);
	    
	    while(li.hasNext()) {
		tmp_trans = (Trans)li.next();
		
		if((tmp_trans.id == startId) || (tmp_trans.id == endId)) {
		    arcInfos.belong = tmp_nproc;
		    if(tmp_trans.id == startId)
			arcInfos.startType = NodeType.TRANS;
		    else
			arcInfos.endType = NodeType.TRANS;
		}
	    }

	    /* Recherche dans les synchro  */
	    li = tmp_nproc.syncs.listIterator(0);
	    
	    while(li.hasNext()) {
		tmp_sync = (Sync)li.next();

		if((tmp_sync.id == startId) || (tmp_sync.id == endId)) {
		    if(tmp_sync.id == startId)
			arcInfos.startType = NodeType.SYNC;
		    else
			arcInfos.endType = NodeType.SYNC;
		}
	    }

	}

	/* Si on a le resultat */
	if((arcInfos.startType != NodeType.NAN) && (arcInfos.endType != NodeType.NAN))
	    return arcInfos;

	/* Recherche dans les channels  */
	li = channels.listIterator(0);
	    
	while(li.hasNext()) {
	    tmp_channel = (Channel)li.next();

	    if((tmp_channel.id == startId) || (tmp_channel.id == endId)) {
		if(tmp_channel.id == startId)
		    arcInfos.startType = NodeType.CHANNEL;
		else
		    arcInfos.endType = NodeType.CHANNEL;
	    }
	}

	/* Si on a le resultat */
	if((arcInfos.startType != NodeType.NAN) && (arcInfos.endType != NodeType.NAN))
	    return arcInfos;	

	/* Si on a le resultat */
	if((arcInfos.startType != NodeType.NAN) && (arcInfos.endType != NodeType.NAN))
	    return arcInfos;
	else
	    return null;
    }    

    /**
       Ajout d'un arc a un NProcess
       @param startId id du node de depart du .model
       @param endId id du node d'arrivee du .model
    */
    
    public void addArc(int startId, int endId) throws Exception {

	ArcInfos arcInfos = getArcInfos(startId, endId);

	if(arcInfos == null)
	    throw(new Exception("arcInfos == null"));

	arcInfos.belong.addArc(startId, endId, arcInfos.startType, arcInfos.endType);	
    }    

    public String toString() {
	
	String res = "RootManager\n";
	ListIterator li;
	NProcess tmp_nProc;
	Channel tmp_channel;
	Sync tmp_sync;
	int nbSync;
	
	res += "name : " + name + "\n";

	/* Les NProcess */

	res += "nombre de NProcess = " + nProcs.size() + "\n\n";

	li = nProcs.listIterator(0);
	while(li.hasNext()) {
	    tmp_nProc = (NProcess)li.next();
	    res += tmp_nProc.toString() + "\n";
	}

	/* Les channels */

	res += "Channels\n";

	res += "nombre de channels = " + channels.size() + "\n";

	li = channels.listIterator(0);
	while(li.hasNext()) {
	    tmp_channel = (Channel)li.next();
	    res += tmp_channel.toString() + "\n";
	}

	/* Les synchros */

	res += "\nSynchronisations\n";

	res += "nombre de synchros = " + allSyncs.size() + "\n";

	li = allSyncs.listIterator(0);
	while(li.hasNext()) {
	    tmp_sync = (Sync)li.next();
	    res += tmp_sync.toString() + "\n";
	}

	return res;
	
    }
    
}
