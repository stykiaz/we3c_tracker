package models;

import java.util.Date;
import java.util.List;

import play.data.validation.Constraints.Required;
import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

@MongoCollection(name = "users")
public class User {

	public static JacksonDBCollection<User.Model, String> coll = MongoDB.getCollection("users", User.Model.class, String.class);

	public static class Model {
		@ObjectId
		@Id
		public String _id;
	
		@Required
		public String username;
		public String password;
		
		public Date createdAt;
		public Date lastLoginAt;
		public Date lastUpdatedAt;
		
		public String email;
		
		public String domainsString;
		public List<String> domains;
	}
	
	public static WriteResult<User.Model, String> save(User.Model ob) {
		return coll.save(ob);
	}
	public static WriteResult<User.Model, String> update(String id, User.Model ob) {
		return coll.updateById(id , ob);
	}
	
	public static Boolean isDomainTrackable(String domain, User.Model ob) {
		if( ob.domains.contains(domain) ) return true;
		for(String in : ob.domains) {
			if( in.substring(0, 1).equals(".") ) {
				if( domain.contains(in) || ( "."+domain ).contains( in ) ) return true;
			}
		}
		return false;
	}
	
	public static Integer getSessionsCount(User.Model ob) {
		return TrackSession.coll.find( DBQuery.is("userId", new org.bson.types.ObjectId( ob._id ) ) ).count();
	}
	
}
