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

	public static JacksonDBCollection<RecordedLocation.Model, String> coll = MongoDB.getCollection("recorded_location", RecordedLocation.Model.class, String.class);
	
	public static class Model {
	
		@ObjectId
		@Id
		public String _id;
		
		@ObjectId
		public String sessionId;
		
		public Date startedAt;
		public Date lastActionAt;
		
		//TODO: separate domain, path
		public String location;
		
	}
	public static WriteResult<RecordedLocation.Model, String> save(Model ob) {
		return coll.save(ob);
	}
	
	public static String getDuration(Model ob) {
		return ( ( ob.lastActionAt.getTime() - ob.startedAt.getTime() ) / 100 ) + "s";
	}
	
}
