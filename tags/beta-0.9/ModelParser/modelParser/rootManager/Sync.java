package modelParser.rootManager;

import java.util.*;

public class Sync {

    public String name;
    int id;

    public String msmNameId;

    LinkedList<NProcess> belongs = new LinkedList<NProcess>();

    public Sync(String name, int id) {
	
	this.name = name;
	this.id = id;

	msmNameId = getMsmName(name);

    }

    public Sync(NProcess nProc, String name, int id) {
	
	this(name, id);
	belongs.add(nProc);

    }

    private String getMsmName(String name) {

	/*String splitName[];

	splitName = name.split("_");*/

	return "ts_" + name/*splitName[0]*/;

    }

    public String toString() {

	String res = name;

	res += "(id=" + id + ") = " + msmNameId;

	return res;

    }

}
