package models;

import java.util.Date;
import java.util.List;

import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

@MongoCollection(name = "track_session")
public class TrackSession {

	public static JacksonDBCollection<TrackSession.Model, String> coll = MongoDB.getCollection("track_session", TrackSession.Model.class, String.class);
	
	public static class Model {
		@ObjectId
		@Id
		public String _id;
		public Date startedAt;
		public Date lastActionAt;
		public String userAgent;
		public String ip;
		public String country;
		public String language;
		public String host;
		
		@ObjectId
		public String userId;
	}
	public static WriteResult<TrackSession.Model, String> save(TrackSession.Model ob) {
		return coll.save(ob);
	}
	
	public static int getLocationsCount( Model ob ) {
		return RecordedLocation.coll.find(DBQuery.is("sessionId",  new org.bson.types.ObjectId( ob._id ) )).count();
	}
	
}
