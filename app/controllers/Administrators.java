package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.mongodb.DBCursor;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;

import ch.qos.logback.core.Context;

import utils.BasicRequests;
import models.Administrator;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.administrators.*;
import static play.libs.Json.toJson;
import play.mvc.Security;

@Security.Authenticated(AdminSecurity.class)
public class Administrators extends Controller {

	public static String module = "Administrators";
	public static String basePath = controllers.routes.Administrators.index().toString();
	
	public static class deleteRequest {
		public Long id;
		public deleteRequest() {}
	}
	
	public static Result index() {
		Http.Context.current().args.put("admin_module", "Administrators");
		Http.Context.current().args.put("admin_parent_section", "settings");
		
		utils.BasicRequests.listingRequest params = form( utils.BasicRequests.listingRequest.class ).bindFromRequest().get();
		//Listview init
	
		Query adminQuery = DBQuery.exists("username");
		net.vz.mongodb.jackson.DBCursor<models.Administrator> admins = models.Administrator.coll.find( adminQuery ).limit( params.resultsPerPage ).skip( params.resultsPerPage * params.p - params.resultsPerPage );
		params.setTotalResults( models.Administrator.coll.find( adminQuery ).count() );
		
		return ok( listview.render( params , admins ) );
	}
	
	public static Result edit(String id) {
		models.Administrator admin = models.Administrator.coll.findOneById(id);
		Form<Administrator> form = form(Administrator.class).fill(admin);		
		return ok( views.html.administrators.form.render("Edit Administrator", form) );
	}
	
	public static Result create() {
		Form<Administrator> form = form(Administrator.class);		
		return ok( views.html.administrators.form.render("New Administrator", form) );
	}
	
	public static Result delete() {
		Form<BasicRequests.deleteRequestMongoModel> req = form(BasicRequests.deleteRequestMongoModel.class).bindFromRequest();
		Administrator.coll.removeById(req.get().id);
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put("ret_code", "0");
		ret.put("id", ""+req.get().id);
		return ok( toJson( ret ) );
	}
	
	public static Result save() {
		Form<Administrator> formAdmin = form(Administrator.class).bindFromRequest();
		
		if( formAdmin.field("username").value().isEmpty() ) formAdmin.reject("username", "Username can not be empty!");
		if( formAdmin.hasErrors() ) {
			flash().put("form_error", "Please fill in all fields properly");
			formAdmin.reject("username", "Required");
		}
		
		if( Administrator.coll.find( DBQuery.is("username", formAdmin.field("username").valueOr("").trim() ).notEquals("_id", formAdmin.field("id").valueOr("-1")) ).count() > 0  ) {
			flash().put("form_error", "Username is taken, please provide a unique one!");
			flash().put("error_username", "Username is taken!");
			formAdmin.reject("username", "Username is taken!");
//			return badRequest( views.html.administrators.form.render( formAdmin.field("id").valueOr("").isEmpty() ? "New Administrator" : "Edit Administrator", formAdmin) );
		}
		if( !formAdmin.field("_id").valueOr("").isEmpty() ) {
			if( !formAdmin.field("password").value().equals( formAdmin.field("password_confirm").value() ) ) {
				flash().put("form_error", "Provided passwords must match!!");
				flash().put("error_password", "Passwords must match!");
				formAdmin.reject("password", "Passwords must match!");
			}
		} else {
			if( !formAdmin.field("password").value().equals( formAdmin.field("password_confirm").value() ) || formAdmin.field("password").valueOr("").isEmpty() ) {
				flash().put("form_error", "Provided passwords must match!!");
				flash().put("error_password", "Passwords must match!");
				formAdmin.reject("password", "Passwords must match!");
			}
		}
		if( formAdmin.hasErrors() ) {
			return badRequest( views.html.administrators.form.render( formAdmin.field("_id").valueOr("").isEmpty() ? "New Administrator" : "Edit Administrator", formAdmin) );
		}
		formAdmin.get().username = formAdmin.get().username.trim();
		
		if( !formAdmin.field("password").valueOr("").isEmpty() ) {
			formAdmin.get().password = utils.Tools.md5Encode( formAdmin.field("password").value() );
		}
		if( formAdmin.field("_id").valueOr("").isEmpty() ) {
			formAdmin.get().createdAt = new Date();
		} else {
			formAdmin.get().lastUpdatedAt = new Date();
		}
		String id = formAdmin.get().save().getSavedId();
		
		flash().put("form_success", "Administrator is saved!");
		return redirect( controllers.routes.Administrators.edit( id ) );
	}
	
}
