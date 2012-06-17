package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.DBCursor;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;

import ch.qos.logback.core.Context;

import utils.BasicRequests;
import models.Administrator;
import models.User;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.users.*;
import static play.libs.Json.toJson;
import play.mvc.Security;

@Security.Authenticated(AdminSecurity.class)
public class Users extends Controller {

	public static String module = "Users";
	public static String basePath = controllers.routes.Users.index().toString();
	
	public static class deleteRequest {
		public Long id;
		public deleteRequest() {}
	}
	
	public static Result index() {
		Http.Context.current().args.put("admin_module", "Users");
		Http.Context.current().args.put("admin_parent_section", "users");
		
		utils.BasicRequests.listingRequest params = form( utils.BasicRequests.listingRequest.class ).bindFromRequest().get();
		//Listview init
	
		Query adminQuery = DBQuery.exists("username");
		net.vz.mongodb.jackson.DBCursor<models.User.Model> users = models.User.coll.find( adminQuery )
															  .limit( params.resultsPerPage ).skip( params.resultsPerPage * params.p - params.resultsPerPage );
		
		params.setTotalResults( models.User.coll.find( adminQuery ).count() );
		
		return ok( listview.render( params , users ) );
	}
	
	public static Result edit(String id) {
		models.User.Model user;
		try {
			user = models.User.coll.findOneById( id );
		} catch( IllegalArgumentException e) {
			return redirect(routes.Users.index());
		}
		Form<User.Model> form = form(User.Model.class).fill(user);
		System.out.println( form.get()._id );
		return ok( views.html.users.form.render("Edit User", form) );
	}
	
	public static Result create() {
		Form<User.Model> form = form(User.Model.class);		
		return ok( views.html.users.form.render("New User", form) );
	}
	
	public static Result delete() {
		Form<BasicRequests.deleteRequestMongoModel> req = form(BasicRequests.deleteRequestMongoModel.class).bindFromRequest();
		User.coll.removeById(req.get().id);
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put("ret_code", "0");
		ret.put("id", ""+req.get().id);
		return ok( toJson( ret ) );
	}
	
	public static Result save() {
		Form<User.Model> formAdmin = form(User.Model.class).bindFromRequest();
		
		if( formAdmin.field("username").value().isEmpty() ) formAdmin.reject("username", "Username can not be empty!");
		if( formAdmin.hasErrors() ) {
			flash().put("form_error", "Please fill in all fields properly");
			formAdmin.reject("username", "Required");
		}
		try {
			if( formAdmin.field("_id") != null && !formAdmin.field("_id").value().isEmpty() && User.coll.find( DBQuery.is("username", formAdmin.field("username").valueOr("").trim() ).notEquals("_id", new ObjectId( formAdmin.field("_id").valueOr("-1") ) ) ).count() > 0  ) {
				flash().put("form_error", "Username is taken, please provide a unique one!");
				flash().put("error_username", "Username is taken!");
				formAdmin.reject("username", "Username is taken!");
			}
		} catch(IllegalArgumentException e) {
			formAdmin.reject("username", "Required");
		}
		if( formAdmin.field("domainsString").value().isEmpty() ) formAdmin.reject("domainsString", "Domains are required");
		if( formAdmin.hasErrors() ) {
			return badRequest( views.html.users.form.render( formAdmin.field("_id").valueOr("").isEmpty() ? "New User" : "Edit User", formAdmin) );
		}
		formAdmin.get().username = formAdmin.get().username.trim();
		String[] domains = formAdmin.field("domainsString").valueOr("").split(",");
		formAdmin.get().domains = new ArrayList<String>();
		for(int i = 0; i < domains.length; i++) formAdmin.get().domains.add( domains[i] );
		
		
		String id; 
		if( formAdmin.field("_id").valueOr("").isEmpty() ) {
			formAdmin.get().createdAt = new Date();
			id = User.save( formAdmin.get() ).getSavedId();
		} else {
			id = formAdmin.field("_id").valueOr("");
			formAdmin.get().lastUpdatedAt = new Date();
			User.update(formAdmin.field("_id").valueOr(""), formAdmin.get() );
		}
		
		flash().put("form_success", "User is saved!");
		return redirect( controllers.routes.Users.edit( id ) );
	}
	
}
