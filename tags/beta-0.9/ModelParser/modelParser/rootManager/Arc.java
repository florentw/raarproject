package modelParser.rootManager;

import java.util.*;

public class Arc {

    public enum ArcType {
	ARC,COMM_ARC
	    }

    static int msmIdCpt = 1;

    NProcess	belong;
    int		startId, endId;
    public int	startType, endType;
    public String	startMSMId, endMSMId;

    public String msmNameId;
    int msmId;    
    
    public ArcType arcType = ArcType.ARC;

    public Arc(NProcess nproc, int startId, int endId, int startType, int endType) {
	belong = nproc;
	this.startId = startId;
	this.endId = endId;
	this.startType = startType;
	this.endType = endType;
	startMSMId = nproc.getMSMIdFromId(startId);
	endMSMId = nproc.getMSMIdFromId(endId);

	/*msmId = msmIdCpt++;
	  msmNameId = "p" + nproc.msmId + "l" + msmId;*/

	if((startType == NodeType.CHANNEL) || (endType == NodeType.CHANNEL))
	    arcType = ArcType.COMM_ARC;

	msmNameId = makeMsmNameId();
    }

    /**
       Construit le nom "MetaScribe" de l'arc (pXlY)
       @return nom MetaScribe
    */

    private String makeMsmNameId() {
        
        String res;

	if(arcType == ArcType.COMM_ARC) {
	    res = "comm" + belong.name + "l";
	    res += belong.idCommArcCpt;
	    belong.idCommArcCpt++;
	}
	else {
	    res = belong.name + "l";
	    res += belong.idArcCpt;
	    belong.idArcCpt++;
	}

        return res;
        
    }

    private String nodeTypeToString(int nodeType) {

	if(nodeType == NodeType.PLACE)
	    return "PLACE";
	else if(nodeType == NodeType.TRANS)
	    return "TRANS";
	else if(nodeType == NodeType.SYNC)
	    return "SYNC";
	else if(nodeType == NodeType.CHANNEL)
	    return "CHANNEL";
	else 
	    return "NAN";
	
    }

    public String toString() {
	
	String res = msmNameId;
	
	res += "(startId=" + startId + "(" + startMSMId + ", " + nodeTypeToString(startType) + ")"
	    + " endId=" + endId + "(" + endMSMId + ", " + nodeTypeToString(endType) + ")" + ")";

	return res;

    }

}
