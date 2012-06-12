package models;

import java.util.Date;

import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import play.modules.mongodb.jackson.MongoDB;

//TODO: ensureIndex, indexes, query index in Mongo
@MongoCollection(name = "recorded_location")
public class RecordedLocation {

	public static JacksonDBCollection<RecordedLocation, String> coll = MongoDB.getCollection("recorded_location", RecordedLocation.class, String.class);
	
	@ObjectId
	@Id
	public String _id;
	
	@ObjectId
	public String sessionId;
	
	public Date startedAt;
	public Date lastActionAt;
	
	//TODO: separate domain, path
	public String location;
	
	public WriteResult<RecordedLocation, String> save() {
		return coll.save(this);
	}
	
}
