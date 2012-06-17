package controllers;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import net.vz.mongodb.jackson.DBQuery;

import com.mongodb.BasicDBObject;

import models.RecordedLocation;
import models.TrackedAction;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.preview.*;

public class Preview extends Controller {

	public static Result view(String locId) {
		RecordedLocation.Model location;
		TrackedAction.Model firstAction; 
		try {
			location = RecordedLocation.coll.findOneById(locId);
			firstAction = TrackedAction.coll.find( DBQuery.is("recLocId", new ObjectId( location._id ) ) ).limit(1).sort( new BasicDBObject("ts", 1) ).next();
		} catch( Exception e) {
			return badRequest();
		}
		System.out.println( firstAction.w );
		System.out.println( firstAction.h );
		return ok( view.render( location, firstAction ) );
	}
	
	public static Result proxy(String locId) {
		RecordedLocation.Model location;
		TrackedAction.Model firstAction; 
		try {
			location = RecordedLocation.coll.findOneById(locId);
			firstAction = TrackedAction.coll.find( DBQuery.is("recLocId", new ObjectId( location._id ) ) ).limit(1).sort( new BasicDBObject("ts", 1) ).next();
		} catch( Exception e) {
			return badRequest();
		}
		StringBuffer page = new StringBuffer();
		Document doc;
		try {
			doc = Jsoup.connect( location.location ).get();
		} catch( IOException e ) {
			//TODO: send tonification
			return internalServerError("IOException");
		}
		return ok( doc.outerHtml() );
	}
	
}
