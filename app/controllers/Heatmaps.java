package controllers;

import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import play.mvc.Controller;
import play.mvc.Result;
import setups.AppConfig;
import utils.HeatMap;

public class Heatmaps extends Controller {

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
		
		String pageMap = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".jpg"; 
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
		
		List<GeneralPath> points = new ArrayList<GeneralPath>();
		
			action = TrackedAction.coll.find( DBQuery.exists("w").exists("x").exists("y").in("recLocId", locationsCollection ).is("e", 2) )
											.sort( new BasicDBObject("recLocId", 1) )
											.sort( new BasicDBObject("sessionId", 1) )
											.sort( new BasicDBObject("ts", 1) );
			GeneralPath path = null;
			String currLocId = null;
			while(action.hasNext()) {
				TrackedAction.Model curract = action.next();
				Short diff = (short) ((maxWidth - curract.w) / 2);
				if( currLocId == null || !currLocId.equals( curract.recLocId ) ) {
					if( path != null ) {
						points.add( path );
					}
					path = new GeneralPath();
					path.moveTo(curract.x + diff, curract.y);
				} else {
					path.lineTo(curract.x + diff, curract.y);
				}
			}
			if( path != null ) points.add( path );
		String heatMapOutput = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".png";
		HeatMap hmap = new HeatMap(heatMapOutput, pageMap);
		//TODO: option to adjust that value
		hmap.createLinesHeatMap(0.3F, points);
		
		
		System.out.println( "Points: " + points.size() );
		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
//		return ok( points.size()+"" );
		return ok( new File( heatMapOutput ) );
	}
	
	public static Result siteFoldHeat(String locId) {
		
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
		
		String pageMap = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".jpg"; 
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
		
		List<Point> points = new ArrayList<Point>();
		//TODO: create aggregate query ( GROUP BY ) and think wheather this will change the results
		action = TrackedAction.coll.find( DBQuery.exists("h").in("recLocId", locationsCollection ).is("e", 1) )
											.sort( new BasicDBObject("h", -1) );
		while(action.hasNext()) {
			TrackedAction.Model curract = action.next();
//			System.out.println(curract.h);
			points.add( new Point(0, curract.h) );
		}
		String heatMapOutput = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".png";
		HeatMap hmap = new HeatMap(points, heatMapOutput, pageMap);
		//TODO: option to adjust that value
		hmap.createFoldHeatMap(4.0F);
		
		System.out.println( "Points: " + points.size() );
		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
		return ok( new File( heatMapOutput ) );
		
	}
	
	public static Result clickHeat(String locId) {
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
		
		String pageMap = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".jpg"; 
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
		//TODO: option to adjust that value
		hmap.createHeatMap(10.0F);
		
		System.out.println( "Points: " + points.size() );
		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
		return ok( new File( heatMapOutput ) );
	}
	
}
