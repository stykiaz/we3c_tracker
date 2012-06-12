package controllers;

import java.util.Date;

import models.RecordedLocation;
import models.TrackSession;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	public static Result index() {

		TrackSession sess = new TrackSession();
		sess.startedAt = new Date();
		sess.lastActionAt = new Date();
		sess._id = sess.save().getSavedId();
		
//		return ok( sess._id );
		
		RecordedLocation location = new RecordedLocation();
		location.sessionId = sess._id;
		location.startedAt = new Date();
		location.lastActionAt = new Date();
		location.location = "http://...";
		location.save();

		return ok(index.render("Your new application is ready."));
	}

}