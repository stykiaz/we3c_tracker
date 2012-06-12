package models;

import java.util.Date;
import java.util.List;

import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

@MongoCollection(name = "track_session")
public class TrackSession {

	public static JacksonDBCollection<TrackSession, String> coll = MongoDB.getCollection("track_session", TrackSession.class, String.class);
	
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
	
	public WriteResult<TrackSession, String> save() {
		return coll.save(this);
	}
	
}
