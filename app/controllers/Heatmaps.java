package controllers;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import models.RecordedLocation;
import models.TrackSession;
import models.TrackedAction;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import setups.AppConfig;
import utils.HeatMap;

public class Heatmaps extends Controller {
	
	public static class HeatMapRequest {
		public int multiplier;
		public HeatMapRequest() {
			multiplier = 100;
		}
		
	}
	
//http://www.intuit.com/website-building-software/blog/2011/10/how-to-use-heat-maps-to-maximize-your-sites-success/
	public static Result filter( String locId ) {
		RecordedLocation.Model location = RecordedLocation.coll.findOneById(locId);
		if( location == null ) return badRequest();
		TrackSession.Model session = TrackSession.coll.findOneById( location.sessionId );
		if( session == null ) return badRequest();
		User.Model user = User.coll.findOneById( session.userId );
		if( user == null ) return badRequest();
		
		return ok( views.html.heatmaps.filter.render( location, session, user ) );
	}
	
	public static Result mouseFollowHeat(String locId) {
		Form<HeatMapRequest> heatRequest = form(HeatMapRequest.class).bindFromRequest();
		
		
		RecordedLocation.Model location = RecordedLocation.coll.findOneById(locId);
		
		DBCursor<RecordedLocation.Model> locations = RecordedLocation.coll.find( DBQuery.is("location", location.location) );
		Short maxWidth = 0;
		DBCursor< TrackedAction.Model > action;
		Collection<ObjectId> locationsCollection = new ArrayList<ObjectId>();
		while( locations.hasNext() ) {
			RecordedLocation.Model currLoc = locations.next();
			locationsCollection.add( new ObjectId( currLoc._id ) );
			action = TrackedAction.coll.find( DBQuery.exists("w").is("recLocId", new ObjectId( currLoc._id )) ).limit(1).sort( new BasicDBObject("w", -1) );
			maxWidth = (short) Math.max(maxWidth, action.next().w);
		}
		
		String pageMap;
//		if( !new File("/media/ext3/www/htdocs/work/we3c/tracker_tmp/16601f1c-d7f3-4326-b75b-cbc6708a554e.jpg").exists() ) {
			pageMap = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".jpg"; 
			String command = AppConfig.pathToHtmlToImageGenerator + " --width "+maxWidth + " " + location.location + " " + pageMap;
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
//		} else pageMap =  "/media/ext3/www/htdocs/work/we3c/tracker_tmp/16601f1c-d7f3-4326-b75b-cbc6708a554e.jpg";
		
		List<GeneralPath> points = new ArrayList<GeneralPath>();
		GeneralPath path = null;

		action = TrackedAction.coll.find( DBQuery.exists("w").exists("x").exists("y").in("recLocId", locationsCollection ).is("e", 2) )
										.sort( new BasicDBObject("recLocId", 1) )
										.sort( new BasicDBObject("sessionId", 1) )
										.sort( new BasicDBObject("ts", 1) );
		String currLocId = null;
		Point prevPoint = null;
		int radius = 32;
		while(action.hasNext()) {
			TrackedAction.Model curract = action.next();
			Short diff = (short) ((maxWidth - curract.w) / 2);
			if( currLocId == null || !currLocId.equals( curract.recLocId ) ) {
				if( path != null ) {
					points.add( path );
				}
				path = new GeneralPath();
				path.moveTo(curract.x + diff, curract.y);
				prevPoint = new Point( curract.x + diff, curract.y );
				currLocId = curract.recLocId;
			} else {
				if( Math.asin( prevPoint.x - (curract.x + diff) ) < radius && Math.abs( prevPoint.y - curract.y ) < radius ) continue;
				path.lineTo(curract.x + diff, curract.y);
				prevPoint = new Point( curract.x + diff, curract.y );
			}
		}
		if( path != null ) points.add( path );
		System.out.println( points.size() );
		String heatMapOutput = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".png";
		HeatMap hmap = new HeatMap(heatMapOutput, pageMap);

		hmap.createLinesHeatMap(0.0F + (float)heatRequest.get().multiplier / 100F, points);
		
		
//		System.out.println( "Points: " + points.size() );
//		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
		return ok( new File( heatMapOutput ) );
	}
	
	public static Result siteFoldHeat(String locId) {
		Form<HeatMapRequest> heatRequest = form(HeatMapRequest.class).bindFromRequest();
		
		RecordedLocation.Model location = RecordedLocation.coll.findOneById(locId);
		
		DBCursor<RecordedLocation.Model> locations = RecordedLocation.coll.find( DBQuery.is("location", location.location) );
		Short maxWidth = 0;
		DBCursor< TrackedAction.Model > action;
		Collection<ObjectId> locationsCollection = new ArrayList<ObjectId>();
		while( locations.hasNext() ) {
			RecordedLocation.Model currLoc = locations.next();
			locationsCollection.add( new ObjectId( currLoc._id ) );
			action = TrackedAction.coll.find( DBQuery.exists("w").is("recLocId", new ObjectId( currLoc._id )) ).limit(1).sort( new BasicDBObject("w", -1) );
			maxWidth = (short) Math.max(maxWidth, action.next().w);
		}
		String pageMap;
		//TODO: check for cached recent snapshot
//		if( !new File("/media/ext3/www/htdocs/work/we3c/tracker_tmp/2823850d-af7e-40e8-a5f7-9bdb6ca525d4.jpg").exists() ) {
			pageMap = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".jpg"; 
			//Execute custom JS to create the required overlay
			String command = AppConfig.pathToHtmlToImageGenerator + " --width "+maxWidth + " " + location.location + " " + pageMap;
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
//		} else pageMap =  "/media/ext3/www/htdocs/work/we3c/tracker_tmp/2823850d-af7e-40e8-a5f7-9bdb6ca525d4.jpg";
		
		List<Rectangle> points = new ArrayList<Rectangle>();
		Iterator< ObjectId > locationsIterator = locationsCollection.iterator();
		Rectangle tmpRectangle = null;
		Short wDiff;
		while( locationsIterator.hasNext() ) {
			ObjectId locId1 = locationsIterator.next();
			//TODO: get the real max values
			action = TrackedAction.coll.find( DBQuery.exists("w").exists("h").is("recLocId", locId1 ).is("e", 0) )
													.sort( new BasicDBObject("h", -1) ).limit(1);
			int h = 0, t = 0, w = 0;;
			if( action.size() > 0 )  {
				TrackedAction.Model currentAction = action.next();
				h = currentAction.h; 
				w= currentAction.w; 
			}
			wDiff = (short) ((maxWidth - w) / 2);
			tmpRectangle = new Rectangle(wDiff, 0, w, h);
			points.add( tmpRectangle );
			
		}

		String heatMapOutput = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".png";
		HeatMap hmap = new HeatMap(heatMapOutput, pageMap);

		hmap.buildData( points );
		hmap.createFoldHeatMap( 0.0F + heatRequest.get().multiplier / 10f );
		
//		System.out.println( "Points: " + points.size() );
//		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
		return ok( new File( heatMapOutput ) );
		
	}
	
	public static Result clickHeat(String locId) {
		
		Form<HeatMapRequest> heatRequest = form(HeatMapRequest.class).bindFromRequest();
		
		RecordedLocation.Model location = RecordedLocation.coll.findOneById(locId);
		
		DBCursor<RecordedLocation.Model> locations = RecordedLocation.coll.find( DBQuery.is("location", location.location) );
		Short maxWidth = 0;
		DBCursor< TrackedAction.Model > action;
		Collection<ObjectId> locationsCollection = new ArrayList<ObjectId>();
		while( locations.hasNext() ) {
			RecordedLocation.Model currLoc = locations.next();
			locationsCollection.add( new ObjectId( currLoc._id ) );
			action = TrackedAction.coll.find( DBQuery.exists("w").is("recLocId", new ObjectId( currLoc._id )) ).limit(1).sort( new BasicDBObject("w", -1) );
			maxWidth = (short) Math.max(maxWidth, action.next().w);
		}
		
		String pageMap;
//		if( !new File("/media/ext3/www/htdocs/work/we3c/tracker_tmp/9498de47-fa45-4019-aa77-e9a3fcac49c3.jpg").exists() ) {
			pageMap = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".jpg"; 
			//Execute custom JS to create the required overlay
			String command = AppConfig.pathToHtmlToImageGenerator + " --width "+maxWidth + " " + location.location + " " + pageMap;
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
//		} else pageMap =  "/media/ext3/www/htdocs/work/we3c/tracker_tmp/9498de47-fa45-4019-aa77-e9a3fcac49c3.jpg";
		
		List<Point> points = new ArrayList<Point>();

			action = TrackedAction.coll.find( DBQuery.exists("w").exists("x").exists("y").in("recLocId", locationsCollection ).is("e", 1) )
										.sort( new BasicDBObject("x", 1) ).sort( new BasicDBObject("y", 1) );
			while(action.hasNext()) {
				TrackedAction.Model curract = action.next();
				Short diff = (short) ((maxWidth - curract.w) / 2);
				points.add( new Point(curract.x + diff, curract.y) );
			}
		String heatMapOutput = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".png";
		HeatMap hmap = new HeatMap(points, heatMapOutput, pageMap);

		hmap.createHeatMap(0.0F + heatRequest.get().multiplier / 10 );
		
//		System.out.println( "Points: " + points.size() );
//		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
		return ok( new File( heatMapOutput ) );
	}
	
}
