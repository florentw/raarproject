package modelParser.rootManager;

public class Trans {

    NProcess belong;
    public String name;
    int id;

    public String msmNameId;
   
    public Trans(NProcess nproc, String name, int id) {
	belong = nproc;
	this.name = name;
	this.id = id;

	try {
	    msmNameId = makeMsmNameId(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
       Construit le nom "MetaScribe" de la transition locale (PX_TY = pXtY)
       @param name nom dans le model
       @return nom MetaScribe
    */

    private String makeMsmNameId(String name) throws Exception {	
	
	String res = belong.name + "t";
	String split[] = name.split("_T");

	if(split.length < 2) {
	    System.err.println("name = " + name);
	    throw(new Exception("Place name != PX_TY"));
	}

	res += split[1];

	return res;
	
    }

    public String toString() {
	
	String res = name;
	
	res += "(id=" + id + ") = " + msmNameId;

	return res;

    }

}
