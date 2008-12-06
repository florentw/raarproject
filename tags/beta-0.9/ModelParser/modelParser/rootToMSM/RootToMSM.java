package modelParser.rootToMSM;

import java.io.*;
import java.util.*;

import modelParser.rootManager.*;

public class RootToMSM {

    private RootManager		rootManager;
    private String		name;

    private BufferedWriter	out;

    private final String ind = "  "; // indentation

    public RootToMSM(RootManager rootManager, String name) {
	this.rootManager = rootManager;
	this.name = name;
    }

    public void writeMSM() throws Exception {
	
	ListIterator li, li_2;
	Sync tmp_sync;
	Channel tmp_channel;
	NProcess tmp_nproc;
	Place tmp_place;
	Trans tmp_trans;
	Arc tmp_arc;
	LinkedList<NProcess>nProcs = rootManager.getNProcs();
	LinkedList<Channel> channels = rootManager.getChannels();
	LinkedList<Sync> allSyncs = rootManager.getSyncs();

	Boolean notFirst;

	out = new BufferedWriter(new FileWriter(name + ".msm"));

	/* Preambule */

	out.write("main_file");
	out.newLine();
	out.write("formalism ( 'ANLZED_PN' ) ;");
	out.newLine();
	out.write("// Attributs globaux");
	out.newLine();
	out.write("where (attribute NAME => '" + name + "_model') ;");
	out.newLine();
	out.newLine();
	
	/* Transitions de synchronisation */	
	if(allSyncs.size() == 0)
	    out.write("// Pas de transitions de synchronisation");
	else
	    out.write("// Transitions de synchronisation");
	out.newLine();
	
	li = allSyncs.listIterator();
	while(li.hasNext()) {
	    tmp_sync = (Sync)li.next();
	    out.write("node '" + tmp_sync.msmNameId + "' is SYNC_TRANS");
	    out.newLine();
	    out.write(ind + "where (attribute NAME => '" + tmp_sync.name + "');");
	    out.newLine();
	}
	
	out.newLine();

	/* Places de communication */
	if(channels.size() == 0)
	    out.write("// Pas de places de communication");
	else
	    out.write("// Places de communication");
	out.newLine();

	li = channels.listIterator();
	while(li.hasNext()) {
	    tmp_channel = (Channel)li.next();
	    out.write("node '" + tmp_channel.msmNameId + "' is COMM_PLACE");
	    out.newLine();
	    out.write(ind + "where (attribute NAME => '" + tmp_channel.name + "',");
	    out.newLine();
	    out.write(ind+ind + "attribute NB_IN_TOKENS => " + tmp_channel.marks + ");");
	    out.newLine();
	}
	out.newLine();

	/* Processus */
	
	li = nProcs.listIterator();
	while(li.hasNext()) {
	    tmp_nproc = (NProcess)li.next();
	    out.write("// Processus " + tmp_nproc.name);
	    out.newLine();
	    out.newLine();
	    out.write("node '" + tmp_nproc.msmNameId + "' is PROCESS");
	    out.newLine();
	    out.write(ind + "where (attribute NAME => 'processus_" + tmp_nproc.name + "',");
	    out.newLine();
	    /* Instances */
	    out.write(ind+ind + "attribute INSTANCES => sy_node (INSTANCE_LIST:");
	    out.newLine();
	    
	    notFirst = false; // Pour la virgule
	    li_2 = tmp_nproc.getPlaces().listIterator();
	    while(li_2.hasNext()) {
		tmp_place = (Place)li_2.next();
		for(int i=0; i<tmp_place.marks; i++) {
		    if(notFirst) {
			out.write(",");
			out.newLine();
		    } else
			notFirst = true;
		    out.write(ind+ind+ind + "sy_node (ONE_INSTANCE:");
		    out.newLine();
		    out.write(ind+ind+ind + "sy_leaf ('" + tmp_place.name + "'))");
		}		
	    }
	    out.write("));");
	    out.newLine();
	    out.newLine();

	    /* Places locales */

	    li_2 = tmp_nproc.getPlaces().listIterator();
	    while(li_2.hasNext()) {
		tmp_place = (Place)li_2.next();
		out.write("node '" + tmp_place.msmNameId + "' is STT_PLACE");
		out.newLine();
		out.write(ind + "where (attribute NAME => '" + tmp_place.name + "');");
		out.newLine();
		out.newLine();
	    }
	    
	    /* Transitions locales */
	    
	    li_2 = tmp_nproc.getTranss().listIterator();
	    while(li_2.hasNext()) {
		tmp_trans = (Trans)li_2.next();
		out.write("node '" + tmp_trans.msmNameId + "' is LOC_TRANS");
		out.newLine();
		out.write(ind + "where (attribute NAME => '" + tmp_trans.name + "');");
		out.newLine();
		out.newLine();
	    }

	    /* Belong */

	    out.write("link '" + tmp_nproc.name + "bl1' is BELONG");
	    out.newLine();
	    out.write(ind + "where(none)");
	    out.newLine();
	    out.write(ind + "relate together");
	    
	    notFirst = false; // Pour la virgule

	    li_2 = tmp_nproc.getPlaces().listIterator(); // Belong places
	    while(li_2.hasNext()) {
		tmp_place = (Place)li_2.next();
		if(notFirst) {
		    out.write(",");
		    out.newLine();
		    out.write(ind+ind+ind+ind+ind+ind+ind+ind+ind+ind+ind);
		} else
		    notFirst = true;
		out.write(" STT_PLACE:'" + tmp_place.msmNameId + "'");
	    }
	    
	    li_2 = tmp_nproc.getTranss().listIterator(); // Belong tansitions locales
	    while(li_2.hasNext()) {
		tmp_trans = (Trans)li_2.next();
		if(notFirst) {
		    out.write(",");
		    out.newLine();
		    out.write(ind+ind+ind+ind+ind+ind+ind+ind+ind+ind+ind);
		} else
		    notFirst = true;
		out.write(" LOC_TRANS:'" + tmp_trans.msmNameId + "'");
	    }
	    
	    li_2 = tmp_nproc.getSyncs().listIterator(); // Belong synchros
	    while(li_2.hasNext()) {
		tmp_sync = (Sync)li_2.next();
		if(notFirst) {
		    out.write(",");
		    out.newLine();
		    out.write(ind+ind+ind+ind+ind+ind+ind+ind+ind+ind+ind);
		} else
		    notFirst = true;
		out.write(" SYNC_TRANS:'" + tmp_sync.msmNameId + "'");
	    }
	    
	    if(notFirst) { // Belong process
		    out.write(",");
		    out.newLine();
		    out.write(ind+ind+ind+ind+ind+ind+ind+ind+ind+ind+ind);
	    }
	    out.write(" PROCESS:'" + tmp_nproc.msmNameId + "'");
	    

	    out.write(";");
	    out.newLine();
	    out.newLine();

	    /* Arcs */
	    out.write("// Nombre d'arcs = " + tmp_nproc.getArcs().size());
	    out.newLine();
	    out.newLine();

	    li_2 = tmp_nproc.getArcs().listIterator();
	    while(li_2.hasNext()) {
		tmp_arc = (Arc)li_2.next();

		if(tmp_arc.arcType == modelParser.rootManager.Arc.ArcType.COMM_ARC)
		    out.write("link '" + tmp_arc.msmNameId + "' is COMM_ARC");		
		else
		    out.write("link '" + tmp_arc.msmNameId + "' is ARC");

		out.newLine();
		out.write(ind + "where(none)");
		out.newLine();
		out.write(ind+"relate ");

		if(tmp_arc.startType == NodeType.PLACE)
		    out.write("STT_PLACE");		    
		else if(tmp_arc.startType == NodeType.TRANS)
		    out.write("LOC_TRANS");
		else if(tmp_arc.startType == NodeType.SYNC)
		    out.write("SYNC_TRANS");
		else if(tmp_arc.startType == NodeType.CHANNEL)
		    out.write("COMM_PLACE");
		
		out.write(":'" + tmp_arc.startMSMId + "' to ");
		
		if(tmp_arc.endType == NodeType.PLACE)
		    out.write("STT_PLACE");		    
		else if(tmp_arc.endType == NodeType.TRANS)
		    out.write("LOC_TRANS");
		else if(tmp_arc.endType == NodeType.SYNC)
		    out.write("SYNC_TRANS");
		else if(tmp_arc.endType == NodeType.CHANNEL)
		    out.write("COMM_PLACE");
		
		out.write(":'" + tmp_arc.endMSMId + "';");
		out.newLine();
		out.newLine();		
	    }
	}

	out.close();
	
    }


}
