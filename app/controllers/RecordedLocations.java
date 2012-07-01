package controllers;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;

import models.RecordedLocation;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.recordedlocations.*;
import play.mvc.Security;

@Security.Authenticated(AdminSecurity.class)
public class RecordedLocations extends Controller {

	public static class deleteRequest {
		public Long id;
		public deleteRequest() {}
	}
	
	public static class listingRequest extends utils.BasicRequests.listingRequest {
		public String sessId;
	}
	
	public static Result index() {
		Http.Context.current().args.put("admin_module", "Recorded Locations");
		Http.Context.current().args.put("admin_parent_section", "recorded_locations");
		
		listingRequest params = form( listingRequest.class ).bindFromRequest().get();
		//Listview init
	
		Query adminQuery = DBQuery.exists("sessionId");
		if( params.sessId != null && !params.sessId.isEmpty() )  {
			adminQuery.is("sessionId", new ObjectId( params.sessId ) );
		}
		net.vz.mongodb.jackson.DBCursor<RecordedLocation.Model> admins = models.RecordedLocation.coll.find( adminQuery )
															  .skip( params.resultsPerPage * params.p - params.resultsPerPage ).limit( params.resultsPerPage )
															  .sort( new BasicDBObject("lastActionAt", -1) );
		
		params.setTotalResults( models.RecordedLocation.coll.find( adminQuery ).count() );
		
		return ok( listview.render( params , admins ) );
	}
	
	
}
