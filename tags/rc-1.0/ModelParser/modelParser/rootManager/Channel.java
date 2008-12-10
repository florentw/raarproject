package modelParser.rootManager;

public class Channel {

    static int msmIdCpt = 1;

    RootManager root;
    public String name;
    int id;
    public int marks = 0;

    public String msmNameId;
    int msmId;

    public Channel(RootManager root, String name, int id, int marks) {
	this.root = root;
	this.name = name;
	this.marks = marks;
	this.id = id;

	msmId = msmIdCpt++;
	msmNameId = "cp" + msmId;
    }

    public Channel(RootManager root, String name, int id) {
	this(root, name, id, 0);
    }

    public String toString() {
	
	String res = name;

	res += "(id=" + id + " marks=" + marks + ") = " + msmNameId;

	return res;
	
    }

}
