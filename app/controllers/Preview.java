package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import net.vz.mongodb.jackson.DBQuery;

import com.mongodb.BasicDBObject;
import models.RecordedLocation;
import models.TrackedAction;
import static play.libs.Json.toJson;
import play.Play;
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
		
		return ok( view.render( location, firstAction ) );
	}
	
	public static Result getData( String locId ) {
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		net.vz.mongodb.jackson.DBCursor<TrackedAction.Model> currsor = TrackedAction.coll.find(DBQuery.is("recLocId", new ObjectId( locId ) ) ).sort(new BasicDBObject("ts", 1) );
		while(currsor.hasNext()) {
			TrackedAction.Model act = currsor.next();
			HashMap<String, String> tmp = new HashMap<String, String>();
			tmp.put("e", act.e+"");
			tmp.put("ts", act.ts+"");
			if( act.location != null && !act.location.isEmpty() ) tmp.put("location", act.location);
			if( act.d != null && !act.d.isEmpty() ) tmp.put("d", act.d);
			if( act.x != null && act.x >= 0 ) tmp.put("x", act.x+"");
			if( act.y != null && act.y >= 0 ) tmp.put("y", act.y+"");
			if( act.w != null && act.w >= 0 ) tmp.put("w", act.w+"");
			if( act.h != null && act.h >= 0 ) tmp.put("h", act.h+"");
			if( act.t != null && act.t >= 0 ) tmp.put("t", act.t+"");
			if( act.l != null && act.l >= 0 ) tmp.put("l", act.l+"");
			
			data.add(tmp);
			
		}
 		return ok( toJson(data) );
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
//			doc = Jsoup.connect( "http://static.wethreecreatives.com/trees.html" ).get();
		} catch( IOException e ) {
			//TODO: send tonification
			return internalServerError("IOException");
		}
		//Fix Paths
		for(Element elem : doc.select("script")){
			if( elem.attributes().hasKey("src") ) elem.attr("src", elem.absUrl("src"));
			else {
				if( elem.html().indexOf("we3cres") >= 0 && elem.html().indexOf("_we3ctr") >= 0 ) elem = elem.html("");
			}
		}
		for(Element elem : doc.select("link")){
			elem.attr("href", elem.absUrl("href"));
		}
		for(Element elem : doc.select("a")){
			elem.attr("href", elem.absUrl("href"));
		}
		for(Element elem : doc.select("img")){
			elem.attr("src", elem.absUrl("src"));
		}
		response().setHeader("Content-Type", "text/html; charset=utf-8");
		return ok( doc.outerHtml() );
	}
	public static Result getActions( String locId ) {
		return ok();
	}
	
}
