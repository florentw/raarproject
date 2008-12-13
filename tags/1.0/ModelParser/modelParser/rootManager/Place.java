package modelParser.rootManager;

public class Place {    

    NProcess belong;
    public String name;

    int id;
    public int marks = 0;

    public String msmNameId;

    public Place(NProcess nproc, String name, int id, int marks) {
	belong = nproc;
	this.name = name;
	this.marks = marks;
	this.id = id;
	
	try {
	    msmNameId = makeMsmNameId(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Place(NProcess nproc, String name, int id) {
	this(nproc, name, id, 0);
    }
    
    /**
       Construit le nom "MetaScribe" de la place (PX_Y = pXpY)
       @param name nom dans le model
       @return nom MetaScribe
    */

    private String makeMsmNameId(String name) throws Exception {	
	
	String res = belong.name + "p";
	String split[] = name.split("_");

	if(split.length < 2)
	    throw(new Exception("Place name != PX_Y"));

	res += split[1];

	return res;
	
    }

    public String toString() {
	
	String res = name;
	
	res += "(id=" + id + " marks=" + marks +") = " + msmNameId;

	return res;

    }

}
