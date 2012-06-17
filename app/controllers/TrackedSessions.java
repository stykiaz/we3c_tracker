package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;

import ch.qos.logback.core.Context;

import utils.BasicRequests;
import models.Administrator;
import models.TrackSession;
import models.User;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.trackedsessions.*;
import static play.libs.Json.toJson;
import play.mvc.Security;

@Security.Authenticated(AdminSecurity.class)
public class TrackedSessions extends Controller {

	public static class deleteRequest {
		public Long id;
		public deleteRequest() {}
	}
	
	public static class listingRequest extends utils.BasicRequests.listingRequest {
		public String userId;
	}
	
	public static Result index() {
		Http.Context.current().args.put("admin_module", "Sessions");
		Http.Context.current().args.put("admin_parent_section", "sessions");
		
		listingRequest params = form( listingRequest.class ).bindFromRequest().get();
		//Listview init
	
		Query adminQuery = DBQuery.exists("host");
		if( params.userId != null && !params.userId.isEmpty() )  {
			adminQuery.is("userId", new ObjectId( params.userId ) );
		}
		net.vz.mongodb.jackson.DBCursor<TrackSession.Model> admins = models.TrackSession.coll.find( adminQuery )
															  .limit( params.resultsPerPage ).skip( params.resultsPerPage * params.p - params.resultsPerPage )
															  .sort( new BasicDBObject("lastActionAt", -1) );
		
		params.setTotalResults( models.TrackSession.coll.find( adminQuery ).count() );
		
		return ok( listview.render( params , admins ) );
	}
	
	
}
