package controllers;

import java.util.NoSuchElementException;

import net.vz.mongodb.jackson.DBQuery;
import models.Administrator;
import play.*;
import play.data.Form;
import play.mvc.*;

import utils.Tools;
import views.html.*;

public class Application extends Controller {

	public static class AuthenticateReq {
		public String username;
		public String password;
	}
	
	public static Result login() {
		return ok( login.render() );
	}
	public static Result logout() {
		session().clear();
		return redirect( controllers.routes.Application.login() );
	}
	
	public static Result authenticate() {
		Form<AuthenticateReq> authRequest = form(AuthenticateReq.class).bindFromRequest();
		models.Administrator.Model administrator = null;
		
		if( authRequest.field("username").valueOr("").isEmpty() || authRequest.field("password").valueOr("").isEmpty() ) {
			flash().put("form_error", "Bad username/password !");
			return redirect( controllers.routes.Application.login() );
		}
		
		try {
			administrator = Administrator.coll.find(DBQuery.is("username", authRequest.get().username).is("password", Tools.md5Encode( authRequest.get().password ))).next();
		} catch(NoSuchElementException e) {
			e.printStackTrace();
			administrator = null;
		}
		
		if( administrator != null ) {
			session().put("admin_username", administrator.username);
			session().put("admin_id", administrator._id);
			return redirect( controllers.routes.Application.home() );
		}
		flash().put("form_error", "Bad username/password !");
		return redirect( controllers.routes.Application.login() );
	}

	@Security.Authenticated(AdminSecurity.class)
	public static Result home() {
		Http.Context.current().args.put("admin_module", "Application");
		Http.Context.current().args.put("admin_parent_section", "dashboard");
		
//		List<String> domains = new ArrayList<String>();
//		domains.add(".example.com");
//		domains.add("www.example1.com");
//		domains.add("example2.com");
//		String curr = "example.com";
//		if( domains.contains(curr) ) return ok("ok");
//		else {
//			for(String in : domains) {
//				if( in.substring(0, 1).equals(".") ) {
//					if( curr.contains(in) || ( "."+curr ).contains( in ) ) return ok("ok");
//				}
//			}
//		}
//		
//		Users user = new Users();
//		user.domsins = new ArrayList<String>();
//		user.domsins.add("111");
//		user.domsins.add("222");
//		user.domsins.add("333");
//		user.save();
		
		return ok( home.render() );
	}
	
	public static Result javascriptRoutes() {
	      response().setContentType("text/javascript");
	      return ok(
	          Routes.javascriptRouter("jsRoutes",
	              // Routes for Properties
	              controllers.routes.javascript.Preview.getData(),
	              controllers.routes.javascript.Preview.view()
	          )
	      );
	  }
	
	public static Result index() {

//		TrackSession sess = new TrackSession();
//		sess.startedAt = new Date();
//		sess.lastActionAt = new Date();
//		sess._id = sess.save().getSavedId();
//		
////		return ok( sess._id );
//		
//		RecordedLocation location = new RecordedLocation();
//		location.sessionId = sess._id;
//		location.startedAt = new Date();
//		location.lastActionAt = new Date();
//		location.location = "http://...";
//		location.save();

		return ok(index.render("Your new application is ready."));
	}

}