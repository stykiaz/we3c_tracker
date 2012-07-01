package controllers;

import java.awt.Point;
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
import models.TrackedAction;
import play.mvc.Controller;
import play.mvc.Result;
import setups.AppConfig;
import utils.HeatMap;

public class Heatmaps extends Controller {

	public static Result mousePositions(String locId) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		locations = RecordedLocation.coll.find( DBQuery.is("location", location.location) );
		List<Point> points = new ArrayList<Point>();
//		while( locations.hasNext() ) {
//			RecordedLocation.Model currLoc = locations.next();
			action = TrackedAction.coll.find( DBQuery.exists("w").exists("x").exists("y").in("recLocId", locationsCollection )/*.is("e", 1)*/ )
										.sort( new BasicDBObject("x", 1) ).sort( new BasicDBObject("y", 1) );
			while(action.hasNext()) {
				TrackedAction.Model curract = action.next();
				Short diff = (short) ((maxWidth - curract.w) / 2);
				points.add( new Point(curract.x + diff, curract.y) );
			}
//		}
		String heatMapOutput = AppConfig.temporaryFilesDirectory + UUID.randomUUID().toString()+".png";
		HeatMap hmap = new HeatMap(points, heatMapOutput, pageMap);
		hmap.createHeatMap(10.0F);
		
		System.out.println( "Points: " + points.size() );
		System.out.println( "Maxwidth: " + maxWidth );
		
		new File(pageMap).delete();
		return ok( new File( heatMapOutput ) );
	}
	
}
