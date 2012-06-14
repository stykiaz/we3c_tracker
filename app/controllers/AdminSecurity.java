package controllers;

import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;


public class AdminSecurity extends Security.Authenticator {

	@Override
    public String getUsername(Context ctx) {
        return ctx.session().get("admin_username");
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(controllers.routes.Application.login());
    }
	
}
