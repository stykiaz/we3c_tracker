package models;

import java.util.Date;

import play.data.validation.Constraints.Required;
import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.MongoCollection;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;

@MongoCollection(name = "administrators")
public class Administrator {

	public static JacksonDBCollection<Administrator, String> coll = MongoDB.getCollection("administrators", Administrator.class, String.class);
	
	@ObjectId
	@Id
	public String _id;
	
	public Date createdAt;
	public Date lastLoginAt;
	public Date lastUpdatedAt;
	
	@Required
	public String username;
	public String password;
	
	public WriteResult<Administrator, String> save() {
		return coll.save(this);
	}
	
}
