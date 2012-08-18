package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import net.vz.mongodb.jackson.DBQuery;

import com.mongodb.BasicDBObject;
import models.RecordedLocation;
import models.TrackSession;
import models.TrackedAction;
import static play.libs.Json.toJson;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import setups.AppConfig;
import utils.Base64;
import utils.Tools;
import views.html.preview.*;

public class Preview extends Controller {

	public static Result view(String locId) {
		RecordedLocation.Model location;
		TrackedAction.Model firstAction;
		TrackSession.Model sess;
		try {
			location = RecordedLocation.coll.findOneById(locId);
			sess = TrackSession.coll.findOneById( location.sessionId );
			firstAction = TrackedAction.coll.find( DBQuery.is("recLocId", new ObjectId( location._id ) ) ).limit(1).sort( new BasicDBObject("ts", 1) ).next();
		} catch( Exception e) {
			return badRequest();
		}
		
		return ok( view.render( sess, location, firstAction, TrackSession.getLocations( sess ), getDataString( location._id ) ) );
	}
	
	public static Result download(String locId) {
		RecordedLocation.Model location;
		TrackedAction.Model firstAction;
		TrackSession.Model sess;
		try {
			location = RecordedLocation.coll.findOneById(locId);
			sess = TrackSession.coll.findOneById( location.sessionId );
			firstAction = TrackedAction.coll.find( DBQuery.is("recLocId", new ObjectId( location._id ) ) ).limit(1).sort( new BasicDBObject("ts", 1) ).next();
		} catch( Exception e) {
			return badRequest();
		}
		
		String pageMap;
		pageMap = AppConfig.temporaryFilesDirectory + Tools.md5Encode(location.location)+".jpg";
		File pmap = new File(pageMap);
		
		if( !(pmap.exists() && pmap.isFile() && new Date().getTime() - pmap.lastModified() < 86400000 ) ) {
			String command = AppConfig.pathToHtmlToImageGenerator + " " + location.location + " " + pageMap;
			try {
				System.out.println( command );
				Process p = Runtime.getRuntime().exec( command );
				p.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
				return internalServerError();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return internalServerError();
			}
		}
		pmap = new File(pageMap);
		byte[] pmapBytes;
		try {
			pmapBytes = FileUtils.readFileToByteArray(pmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError();
		}
		InputStream previewJs = Play.application().resourceAsStream("/public/javascripts/preview.js");
		String previewJsStr;
		InputStream jqueryJs = Play.application().resourceAsStream("/public/javascripts/jquery-1.7.1.min.js");
		String jqueryJsStr;
		
		StringWriter writer;
		try {
			writer = new StringWriter();
			IOUtils.copy(jqueryJs, writer);
			jqueryJsStr = writer.toString();
			writer = new StringWriter();
			IOUtils.copy(previewJs, writer);
			previewJsStr = writer.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError();
		}
		String downloadIframe = download_iframe.render( utils.Base64.encode( pmapBytes ) ).toString();
		try {
			String zipFileName = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".zip";
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream( zipFileName ) );
			out.putNextEntry(new ZipEntry("index.html"));
			byte[] byteArray = download.render( sess, location, firstAction, TrackSession.getLocations( sess ), getDataString( location._id ), jqueryJsStr + "\n" + previewJsStr ).toString().getBytes();
			out.write(byteArray, 0, byteArray.length);
			
			out.putNextEntry(new ZipEntry("iframe.html"));
			byte[] byteArray1 = downloadIframe.getBytes();
			out.write(byteArray1);
//			out.putNextEntry(new ZipEntry("site.jpg"));
//			out.write(pmapBytes);
			
			out.close();
			
			response().setHeader("Content-Disposition", "attachment; filename=replay.zip");
			return ok( new File( zipFileName ) );
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return internalServerError();
//		return ok( download.render( sess, location, firstAction, TrackSession.getLocations( sess ), getDataString( location._id ), utils.Tools.base64Encode( downloadIframe ), jqueryJsStr + "\n" + previewJsStr ) );
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
	public static String getDataString( String locId ) {
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
		return toJson(data).toString();
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
		//Fix Paths
		for(Element elem : doc.select("script")){
			if( elem.attributes().hasKey("src") ) elem.attr("src", elem.absUrl("src"));
			else {
				if( elem.html().indexOf("we3cres") >= 0 && elem.html().indexOf("_we3ctr") >= 0 ) elem = elem.html("");
				else if( elem.html().indexOf("google-analytics.com") >= 0 ) elem = elem.html("");
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
