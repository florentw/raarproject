package modelParser.rootManager;

import java.util.*;

public class NProcess {

    static int msmIdCpt = 1;

    RootManager root;
    public String name;

    public String msmNameId;
    public int msmId;

    /* Pour creer des nom MetaScribe humainement logique */
    int idArcCpt = 1;
    int idCommArcCpt = 1;

    LinkedList<Place> places = new LinkedList<Place>();
    LinkedList<Trans> transs = new LinkedList<Trans>();
    LinkedList<Arc> arcs = new LinkedList<Arc>();
    LinkedList<Sync> syncs = new LinkedList<Sync>();

    /**
       Constructeur
       @param root le root manager parent
       @param name le nom du NProcess
    */

    public NProcess(RootManager root, String name) {
	this.root = root;
	this.name = name;

	msmId = msmIdCpt++;
	msmNameId = "proc_" + name;
    }    

    /* Getters */
    
    public LinkedList<Place> getPlaces() {
	return places;
    }

    public LinkedList<Trans> getTranss() {
	return transs;
    }

    public LinkedList<Arc> getArcs() {
	return arcs;
    }

    public LinkedList<Sync> getSyncs() {
	return syncs;
    }

    /* Ajout d'une place */

    public void addPlace(String name, int id, int marks) {
	places.add(new Place(this, name, id, marks));
    }

    public void addPlace(String name, int id) {
	addPlace(name, id, 0);
    }

    /* Ajout d'une transition */

    public void addTrans(String name, int id) {
	transs.add(new Trans(this, name, id));
    }

    /* Ajout d'un arc */
    public void addArc(int startId, int endId, int startType, int endType) {
	arcs.add(new Arc(this, startId, endId, startType, endType));
    }

    /* Ajout d'une synchro */
    public void addSync(String name, int id) {

	System.out.println(this.name + " addSync de " + name);

	if(!syncExist(name))	
	    syncs.add(new Sync(this, name, id));

    }

    public void addSync(Sync sync) {
	addSync(sync.name, sync.id);
    }
    
    /**
       Teste si une synchro est deja presente
       @param syncName nom de la synchro
       @return true si presente, false sinon
    */

    private Boolean syncExist(String syncName) {

	ListIterator li;
	Sync tmp_sync;

	/* On verifie qu'on a pas deja cette synchro */
	li = syncs.listIterator();
	while(li.hasNext()) {
	    tmp_sync = (Sync)li.next();
	    if(tmp_sync.name.equals(syncName.toString()))
		return true;
	}

	return false;

    }


    /**
       Recuperation de l'id MSM depuis l'id .model
       @param l'id model
       @return l'id MSM
    */
    String getMSMIdFromId(int id) {
	
	ListIterator li;
	Place tmp_place;
	Trans tmp_trans;
	Channel tmp_channel;
	Sync tmp_sync;
	
	li = places.listIterator(0);
	while(li.hasNext()) {
	    tmp_place = (Place)li.next();
	    if(tmp_place.id == id) {
		return tmp_place.msmNameId;
	    }
	}

	li = transs.listIterator(0);
	while(li.hasNext()) {
	    tmp_trans = (Trans)li.next();
	    if(tmp_trans.id == id) {
		return tmp_trans.msmNameId;
	    }
	}

	li = root.channels.listIterator(0);
	while(li.hasNext()) {
	    tmp_channel = (Channel)li.next();
	    if(tmp_channel.id == id) {
		return tmp_channel.msmNameId;
	    }
	}

	li = syncs.listIterator(0);
	while(li.hasNext()) {
	    tmp_sync = (Sync)li.next();
	    if(tmp_sync.id == id) {
		return tmp_sync.msmNameId;
	    }
	}

	return null;

    }

    public String toString() {
	
	String res = "NProcess = " + msmNameId + "\n";
	ListIterator li;
	Place tmp_place;
	Trans tmp_trans;
	Arc tmp_arc;
	Sync tmp_sync;

	res += "name : " + name + "\n";

	/* Les places locales */

	res += "nombre de places = " + places.size() + "\n";

	li = places.listIterator(0);
	while(li.hasNext()) {
	    tmp_place = (Place)li.next();
	    res += tmp_place.toString() + "\n";
	}

	/* Les transitions */

	res += "nombre de transitions = " + transs.size() + "\n";

	li = transs.listIterator(0);
	while(li.hasNext()) {
	    tmp_trans = (Trans)li.next();
	    res += tmp_trans.toString() + "\n";
	}

	/* Les synchros */

	res += "nombre de transitions synchronisees = " + syncs.size() + "\n";

	li = syncs.listIterator(0);
	while(li.hasNext()) {
	    tmp_sync = (Sync)li.next();
	    res += tmp_sync.toString() + "\n";
	}

	/* Les arcs */

	res += "nombre d'arcs = " + arcs.size() + "\n";

	li = arcs.listIterator(0);
	while(li.hasNext()) {
	    tmp_arc = (Arc)li.next();
	    res += tmp_arc.toString() + "\n";
	}

	return res;
	
    }
}
