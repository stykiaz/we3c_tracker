package models;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import play.modules.mongodb.jackson.MongoDB;

@MongoCollection(name = "tracked_action")
public class TrackedAction {

	private static JacksonDBCollection<TrackedAction, String> coll = MongoDB.getCollection("tracked_action", TrackedAction.class, String.class);
	
	@Id
	@ObjectId
	public String _id;
	
	@ObjectId
	public String sessionId;
	
	@ObjectId
	public String recLocId; //recorded location id
	
	//event number  { 'init': 0, 'mousedown': 1, 'mousemove': 2, 'resize': 3, 'scroll': 4, 'locationChange': 5 }
	public Byte e;
	public String location;//location
	public String d;//direction	
	public Long ts;
	public Short x, //X coord (click, move) 
				y, //Y coord (click, move)
				w, //width (resize)
				h, //height (resize)
				t, //top scroll
				l; //left scroll
	
	public WriteResult<TrackedAction, String> save() {
		return coll.save(this);
	}
	
}
