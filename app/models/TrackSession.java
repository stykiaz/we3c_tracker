package models;

import java.util.Date;

import com.mongodb.BasicDBObject;

import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import nl.bitwalker.useragentutils.UserAgent;

@MongoCollection(name = "track_session")
public class TrackSession {

	public static JacksonDBCollection<TrackSession.Model, String> coll = MongoDB.getCollection("track_session", TrackSession.Model.class, String.class);
	
	public static class Model {
		@ObjectId
		@Id
		public String _id;
		public Date startedAt;
		public Date firstActionAt;
		public Date lastActionAt;
		public String userAgent;
		public String ip;
		public String country;
		public String language;
		public String host;
		//Extracted for possible analytical functions
		public String browser;
		public String os;
		public String mainLanguage;
		
		@ObjectId
		public String userId;
	}
	public static WriteResult<TrackSession.Model, String> save(TrackSession.Model ob) {
		WriteResult<TrackSession.Model, String> tmp = coll.save(ob);
		coll.ensureIndex( new BasicDBObject("userId", 1) );
		return tmp;
	}
	
	public static net.vz.mongodb.jackson.DBCursor<RecordedLocation.Model> getLocations( Model ob ) {
		return RecordedLocation.coll.find( DBQuery.is("sessionId", new org.bson.types.ObjectId( ob._id )) ).sort( new BasicDBObject("lastActionAt", -1) );
	}
	
	public static int getLocationsCount( Model ob ) {
		return RecordedLocation.coll.find(DBQuery.is("sessionId",  new org.bson.types.ObjectId( ob._id ) )).size();
	}

	//wrong way of doing it, silly me ...
	@Deprecated
	public static Long getDurationSeconds(Model ob) {
		return ( ( ob.lastActionAt.getTime() - ( ob.firstActionAt == null ? ob.startedAt.getTime() : ob.firstActionAt.getTime() ) ) / 1000 );
	}
	@Deprecated
	public static String getDuration(Model ob) {
		Long seconds = getDurationSeconds(ob);
		Long min = seconds / 60;
		Long leftSeconds = seconds % 60 ;
		return min+":"+leftSeconds;
	}
	
	public static String getBrowser(Model ob) {
		UserAgent userAgent = UserAgent.parseUserAgentString( ob.userAgent );
		return userAgent.getBrowser().getName();
	}
	public static String getLanguage(Model ob) {
		String[] languages = ob.language.split(",");
		if( languages.length > 1 ) return languages[0];
		return ob.language;
	}
	public static String getOS(Model ob) {
		UserAgent userAgent = UserAgent.parseUserAgentString( ob.userAgent );
		return userAgent.getOperatingSystem().getName();
	}
	
}
