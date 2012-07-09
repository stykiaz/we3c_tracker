package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import nl.bitwalker.useragentutils.UserAgent;

import com.mongodb.MongoException;

import models.RecordedLocation;
import models.TrackSession;
import models.TrackedAction;
import models.User;
import play.Play;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import utils.Base64;
import utils.Tools;

public class DataHub extends Controller {

	public static class TrackRequest {
		@Required
		public String d;
		@Required
		public String host;
		@Required
		public String key;
	}
	
	public static Result track() {
		
		response().setContentType( "image/png" );
		InputStream outGifStream = Play.application().resourceAsStream("/public/images/site/blank.png");
		SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		int timeOffset = TimeZone.getDefault().getOffset(new Date().getTime() );
		java.util.Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		httpDateFormat.setCalendar(cal);
		
		Form<TrackRequest> req = form( TrackRequest.class ).bindFromRequest();

		if( req.hasErrors() ) return badRequest( outGifStream );
		
		TrackSession.Model trackSess = null;
		User.Model user = null;
		try {
			 user = User.coll.findOneById( req.get().key );
		} catch( MongoException e ) {
			e.printStackTrace();
			return internalServerError("No User");
		}
		if( user == null || user._id == null || user._id.isEmpty() ) return badRequest( outGifStream );
		
		if( !User.isDomainTrackable( req.get().host, user ) ) {
			return forbidden( outGifStream );
		}
		
		String cookieSessionName = Tools.md5Encode( req.get().host )+"_sess";
		Long systemTs = new Date().getTime( ); 
		
		Http.Cookie storedTrackedSessionId = request().cookies().get( cookieSessionName );
		
		if( /*session().containsKey(Tools.md5Encode( req.get().host )+"_tracked_session_ts") &&
			( systemTs < Long.valueOf( session().get(Tools.md5Encode( req.get().host )+"_tracked_session_ts") ) ) &&
			session().containsKey(Tools.md5Encode( req.get().host )+"_tracked_session") */
			storedTrackedSessionId != null
			) {
// 			trackSess = TrackSession.coll.findOneById( session().get( Tools.md5Encode( req.get().host )+"_tracked_session") );
			trackSess = TrackSession.coll.findOneById( storedTrackedSessionId.value() );
		} 
		if( trackSess == null ) {
			trackSess = new TrackSession.Model();
			trackSess.startedAt = new Date();
			if( Context.current().request().headers().containsKey("USER-AGENT") && Context.current().request().headers().get("USER-AGENT").length > 0 ) {
				trackSess.userAgent = Context.current().request().headers().get("USER-AGENT")[0];
				UserAgent userAgent = UserAgent.parseUserAgentString( trackSess.userAgent );
				trackSess.os = userAgent.getOperatingSystem().name();
				trackSess.browser = userAgent.getBrowser().name();
			}
			if( Context.current().request().headers().containsKey("ACCEPT-LANGUAGE") && Context.current().request().headers().get("ACCEPT-LANGUAGE").length > 0 ) {
				trackSess.language = Context.current().request().headers().get("ACCEPT-LANGUAGE")[0];
				String[] languages = trackSess.language.split(",");
				if( languages.length > 1 ) trackSess.mainLanguage = languages[0];
				else trackSess.mainLanguage = trackSess.language;
			}
			trackSess.host = req.get().host;
			trackSess.userId = user._id;
			trackSess._id =  TrackSession.save(trackSess).getSavedId();
			
// 			session().put( Tools.md5Encode( req.get().host )+"_tracked_session", trackSess._id);
			//TODO: get client IP using http proxy
		}
		
		response().setCookie(cookieSessionName, trackSess._id, (int) (systemTs / 1000 + 3600 + timeOffset / 1000), "/" );
//		System.out.println( cookieSessionName+"="+trackSess._id+"; Expires="+httpDateFormat.format( new Date( systemTs + 3600000 + timeOffset ) ) +"; Path=/" );
//		response().setHeader("Set-Cookie", cookieSessionName+"="+trackSess._id+"; expires="+httpDateFormat.format( new Date( systemTs + 3600000 + timeOffset ) ) +"; Path=/" );
		
		RecordedLocation.Model loc = null;
		String cookiesLocationName = Tools.md5Encode( req.get().host )+"_last_loc"; //last tracked location
		Http.Cookie lastTrackedLocationId = request().cookies().get( cookiesLocationName );
		
		if( lastTrackedLocationId != null /* session().containsKey(Tools.md5Encode( req.get().host )+"_last_tracked_location") */ ) {
// 			loc =  RecordedLocation.coll.findOneById( session().get(Tools.md5Encode( req.get().host )+"_last_tracked_location") );
			loc =  RecordedLocation.coll.findOneById( lastTrackedLocationId.value() );
		}
		
		String actionsString = new String( Base64.decode( req.get().d ) );

		if( actionsString.length() < 5 ) badRequest(outGifStream);
		
		String[] actions = actionsString.split("}");
		Long lastTs = 0L;
		for(int i = 0; i < actions.length; i++) {
			String[] parts = actions[i].split("[|]");
			if( parts.length < 1 ) continue;
			TrackedAction.Model action = null;
			try {
				switch( Byte.valueOf( parts[0] ) ) {
					case 0:
						if( parts.length != 7 ) continue;
						//TODO:Track domains and pageUrl
						action = new TrackedAction.Model();
						action.e = 0;
						action.location = parts[1];
						action.w = Short.valueOf( parts[2] );
						action.h = Short.valueOf( parts[3] );
						action.t = Short.valueOf( parts[4] );
						action.l = Short.valueOf( parts[5] );
						action.ts = Long.valueOf( parts[6] );
						loc = new RecordedLocation.Model();
						loc.sessionId = trackSess._id;
						loc.startedAt = new Date( action.ts );
						loc.location = parts[1];
						loc._id = RecordedLocation.save( loc ).getSavedId();
						if( trackSess.firstActionAt == null ) { 
							trackSess.firstActionAt = new Date( action.ts ); TrackSession.save(trackSess);
						}
// 						session().put(Tools.md5Encode( req.get().host )+"_last_tracked_location", loc._id);
						response().setCookie(cookiesLocationName, loc._id, (int)(systemTs / 1000 + 3600 + timeOffset / 1000 ), "/" );
//						response().setHeader("Set-Cookie", cookiesLocationName+"="+loc._id+"; Expires="+httpDateFormat.format( new Date( systemTs + 3600000 + timeOffset ) )+ "; Path=/" );
//						System.out.println( cookiesLocationName+"="+loc._id+"; Expires="+httpDateFormat.format( new Date( systemTs + 3600000 + timeOffset) )+ "; Path=/" );
						
						break;
					case 1: //mouse down
						//TODO: inspect errors and cases here
						if( loc == null ) return badRequest(outGifStream);
						if( parts.length != 6 ) continue;
						action = new TrackedAction.Model();
						action.e = 1;
						action.x = Short.valueOf( parts[1] );
						action.y = Short.valueOf( parts[2] );
						action.w = Short.valueOf( parts[3] );
						action.h = Short.valueOf( parts[4] );
						action.ts = Long.valueOf( parts[5] );
						break;
					case 2: //move
						//TODO: inspect errors and cases here
						if( loc == null ) return badRequest(outGifStream);
						if( parts.length != 6 ) continue;
						action = new TrackedAction.Model();
						action.e = 2;
						action.x = Short.valueOf( parts[1] );
						action.y = Short.valueOf( parts[2] );
						action.w = Short.valueOf( parts[3] );
						action.h = Short.valueOf( parts[4] );
						action.ts = Long.valueOf( parts[5] );
						break;
					case 3: //resize
						//TODO: inspect errors and cases here
						if( loc == null ) return badRequest(outGifStream);
						if( parts.length != 4 ) continue;
						action = new TrackedAction.Model();
						action.e = 3;
						action.w = Short.valueOf( parts[1] );
						action.h = Short.valueOf( parts[2] );
						action.ts = Long.valueOf( parts[3] );
						break;
					case 4: //scroll
						//TODO: inspect errors and cases here
						if( loc == null ) return badRequest(outGifStream);
						if( parts.length != 5 ) continue;
						action = new TrackedAction.Model();
						action.e = 4;
						action.t = Short.valueOf( parts[1] );
						action.l = Short.valueOf( parts[2] );
						action.d = parts[3];
						action.ts = Long.valueOf( parts[4] );
						break;
					case 5:
						break;
				}
			} catch(NumberFormatException e) {
				continue;
			}

			if( action != null ) {
				action.recLocId = loc._id;
				TrackedAction.save(action);
				lastTs = action.ts;
			}
		}
		if( lastTs > 0 ) {
			loc.lastActionAt = new Date( lastTs );
			RecordedLocation.save( loc );
			trackSess.lastActionAt = new Date( lastTs );
			TrackSession.save( trackSess );
		}
		
		
// 		session().put(Tools.md5Encode( req.get().host )+"_tracked_session_ts", ( systemTs + 3600000 )+"");
		
		
		return ok( outGifStream );
		
	}
	
	public static Result dummy() {/*
		Form<TrackRequest> req = form( TrackRequest.class ).bindFromRequest();
		List<String> dummy = new ArrayList<String>(); 
		dummy.add( "0|http://localhost/work/we3c/static/barebone.html#|1600|503|0|0|1339451636005}2|538|292|1339451636828}2|66|494|1339451638213}2|42|66|1339451638366}2|480|3|1339451638586}2|773|283|1339451638927}1|781|290|1339451639133}2|860|309|1339451639287}2|904|309|1339451639304}2|942|313|1339451639319}2|980|313|1339451639336}2|993|315|1339451639341}2|1350|261|1339451639607}1|1351|260|1339451639706}2|1346|260|1339451639874}2|1253|253|1339451639927}2|1230|255|1339451639935}2|881|246|1339451640021}2|860|249|1339451640033}2|762|247|1339451640078}2|691|275|1339451640209}2|680|275|1339451640225}2|654|278|1339451640271}4|2|0||1339451640322}4|128|0|d|1339451640701}2|563|384|1339451641061}2|532|382|1339451641156}2|523|383|1339451641227}2|485|375|1339451641382}2|398|467|1339451641476}2|369|467|1339451641586}2|340|471|1339451641820}1|339|471|1339451641849}2|336|470|1339451641976}2|227|464|1339451642029}2|198|466|1339451642038}2|0|295|1339451642186}2|218|241|1339451642505}2|470|277|1339451642569}2|503|277|1339451642577}
2|532|279|1339451642585}2|557|279|1339451642591}2|744|310|1339451642663}2|759|310|1339451642672}2|783|315|1339451642686}2|796|315|1339451642694}2|807|320|1339451642701}2|816|320|1339451642712}2|979|398|1339451642935}2|1121|228|1339451643077}2|1121|205|1339451643085}2|1126|171|1339451643099}2|1126|136|1339451643123}2|715|236|1339451643381}2|574|233|1339451643405}2|523|235|1339451643413}2|443|232|1339451643427}2|408|234|1339451643435}2|314|201|1339451643529}2|316|186|1339451643537}" );
		dummy.add( "2|318|165|1339451643554}2|438|60|1339451643664}2|902|266|1339451644115}2|1195|259|1339451644269}4|125|0||1339451645048}4|2|0|t|1339451645366}2|1191|266|1339451645385}4|0|0||1339451645386}2|1185|266|1339451645421}2|1075|263|1339451645460}2|1040|265|1339451645468}2|1007|265|1339451645476}2|974|267|1339451645483}2|861|261|1339451645508}2|746|268|1339451645530}2|680|268|1339451645552}2|603|276|1339451645570}2|580|276|1339451645578}2|525|288|1339451645600}2|514|288|1339451645608}2|503|291|1339451645616}2|494|291|1339451645624}2|389|352|1339451645906}2|376|352|1339451645922}2|362|355|1339451645944}2|340|355|1339451645976}2|331|356|1339451646008}1|331|357|1339451646055}2|328|351|1339451646202}2|200|77|1339451646319}2|216|67|1339451646397}2|479|265|1339451646693}1|482|265|1339451646765}2|758|418|1339451647091}2|795|418|1339451647115}2|888|436|1339451647366}2|925|436|1339451647390}2|936|439|1339451647397}2|1009|425|1339451647585}1|1014|423|1339451647643}2|1249|195|1339451648099}2|1249|185|1339451648132}
1|1248|178|1339451648215}2|736|147|1339451648521}2|713|149|1339451648529}2|521|143|1339451648639}2|209|356|1339451648921}2|200|356|1339451648928}2|149|364|1339451648992}2|117|331|1339451649123}2|301|229|1339451649257}2|619|369|1339451649553}2|1093|172|1339451649873}2|1093|163|1339451649881}2|1098|151|1339451649889}2|1098|142|1339451649897}2|1116|120|1339451649959}2|1253|172|1339451650077}2|1014|246|1339451650311}2|989|246|1339451650319}" );
		dummy.add( "2|945|243|1339451650336}2|497|134|1339451650521}2|488|136|1339451650529}2|479|136|1339451650537}2|470|138|1339451650545}2|459|138|1339451650561}2|405|174|1339451650663}2|386|174|1339451650717}2|291|306|1339451651099}2|349|353|1339451651397}2|367|353|1339451651413}2|379|357|1339451651421}2|398|357|1339451651427}2|468|370|1339451651459}2|487|370|1339451651474}2|529|375|1339451651483}2|569|375|1339451651499}2|588|378|1339451651505}2|609|378|1339451651513}2|653|383|1339451651529}2|699|383|1339451651545}2|722|385|1339451651553}2|743|385|1339451651561}2|766|387|1339451651574}2|1268|277|1339451651913}2|1253|174|1339451652007}2|1257|156|1339451652093}2|1042|407|1339451652295}2|301|238|1339451652671}2|709|177|1339451652857}2|718|180|1339451652865}2|727|180|1339451652874}2|1099|322|1339451653030}2|1130|322|1339451653037}2|1161|324|1339451653045}2|1324|277|1339451653139}2|1324|265|1339451653147}2|1326|253|1339451653155}2|1326|216|1339451653171}2|1328|203|1339451653177}2|1328|146|1339451653201}
2|1332|128|1339451653217}2|1322|105|1339451653373}2|856|238|1339451653499}2|417|159|1339451653663}4|5|0||1339451653815}2|414|153|1339451653836}4|20|0||1339451653844}2|414|154|1339451653861}4|40|0||1339451653866}4|51|0|d|1339451653879}2|415|154|1339451653897}4|64|0||1339451653900}2|415|155|1339451653920}4|78|0||1339451653923}2|414|158|1339451653938}4|87|0||1339451653940}2|413|161|1339451653949}4|95|0||1339451653957}2|410|165|1339451653972}" );
		dummy.add( "4|105|0||1339451653981}2|409|173|1339451653995}4|112|0||1339451654002}2|409|175|1339451654008}4|121|0||1339451654029}2|409|178|1339451654038}4|124|0||1339451654039}4|128|0|d|1339451654078}2|409|179|1339451654233}2|439|190|1339451654358}2|448|190|1339451654406}4|125|0||1339451654495}2|456|190|1339451654510}4|110|0||1339451654520}4|73|0|t|1339451654564}2|460|190|1339451654581}4|61|0||1339451654583}2|462|190|1339451654587}4|48|0||1339451654611}2|469|192|1339451654617}4|42|0||1339451654618}4|37|0|t|1339451654631}2|471|192|1339451654635}4|31|0||1339451654647}2|474|192|1339451654650}4|26|0||1339451654663}2|477|193|1339451654666}4|19|0||1339451654684}2|480|193|1339451654685}4|14|0||1339451654700}2|481|193|1339451654702}4|10|0||1339451654716}2|487|193|1339451654719}4|7|0||1339451654732}2|518|193|1339451654753}4|3|0||1339451654754}2|550|186|1339451654763}4|1|0||1339451654770}2|594|186|1339451654779}4|0|0||1339451654787}2|877|2|1339451654881}3|1541|496|1339451655809}2|932|14|1339451656029}
2|934|26|1339451656037}2|934|41|1339451656045}2|1147|303|1339451656257}2|1192|303|1339451656327}2|1226|309|1339451656373}2|1268|309|1339451656421}2|1304|318|1339451656492}3|1163|496|1339451656805}2|862|313|1339451657217}2|871|313|1339451657398}3|1454|496|1339451657683}2|1210|291|1339451658091}2|948|222|1339451658427}4|2|0||1339451658434}2|947|222|1339451658439}4|9|0||1339451658456}4|27|0|d|1339451658485}" );
		dummy.add( "2|945|222|1339451658516}4|61|0||1339451658524}4|114|0|d|1339451658638}2|943|222|1339451658652}4|121|0||1339451658655}4|135|0|d|1339451658730}4|43|0|t|1339451659112}2|946|223|1339451659116}4|30|0||1339451659136}2|948|227|1339451659144}4|23|0||1339451659151}2|951|229|1339451659163}4|17|0||1339451659166}2|953|230|1339451659179}4|12|0||1339451659181}2|957|231|1339451659185}4|7|0||1339451659197}2|963|234|1339451659201}2|972|240|1339451659213}4|3|0||1339451659214}2|1000|255|1339451659230}4|1|0||1339451659231}2|1029|272|1339451659241}4|0|0||1339451659247}2|1124|306|1339451659295}3|1012|496|1339451659696}2|584|277|1339451659897}2|555|273|1339451659999}4|2|0||1339451660501}4|135|0|d|1339451660783}2|552|272|1339451661062}4|134|0||1339451661182}4|20|0|t|1339451661378}2|555|271|1339451661398}4|14|0||1339451661400}2|559|272|1339451661405}4|10|0||1339451661417}2|572|275|1339451661419}4|7|0||1339451661433}2|588|279|1339451661435}2|604|282|1339451661443}4|4|0||1339451661449}2|633|285|1339451661459}
4|2|0||1339451661466}2|663|293|1339451661475}4|0|0||1339451661482}2|774|335|1339451661655}3|1454|496|1339451661994}2|1250|340|1339451662349}2|572|191|1339451662607}2|532|195|1339451662624}2|496|195|1339451662639}2|421|228|1339451662749}4|3|0||1339451662793}2|423|234|1339451662796}4|18|0||1339451662820}4|47|0|d|1339451662853}2|425|238|1339451662869}4|62|0||1339451662877}2|427|239|1339451662891}" );
		dummy.add( "4|78|0||1339451662900}2|428|241|1339451662914}4|88|0||1339451662922}2|430|244|1339451662937}4|103|0||1339451662948}2|431|245|1339451662955}4|119|0||1339451662996}2|435|253|1339451663017}4|128|0||1339451663021}4|135|0|d|1339451663069}2|436|253|1339451663258}4|129|0||1339451663270}2|437|253|1339451663279}4|117|0||1339451663289}2|440|254|1339451663298}4|102|0||1339451663305}2|445|255|1339451663316}4|89|0||1339451663321}2|450|255|1339451663327}4|65|0||1339451663355}2|457|256|1339451663373}4|52|0||1339451663379}2|462|256|1339451663395}4|42|0||1339451663399}2|466|256|1339451663415}4|34|0||1339451663417}2|474|256|1339451663425}4|30|0||1339451663430}2|482|255|1339451663440}4|26|0||1339451663442}2|489|255|1339451663449}4|22|0||1339451663455}2|496|252|1339451663460}2|510|240|1339451663484}4|14|0||1339451663489}2|525|224|1339451663503}4|8|0||1339451663506}2|538|205|1339451663513}4|6|0||1339451663518}2|552|180|1339451663530}4|4|0||1339451663532}2|562|150|1339451663545}4|2|0||1339451663547}
2|572|123|1339451663564}4|0|0||1339451663565}2|580|63|1339451663591}2|580|50|1339451663599}2|609|5|1339451663718}3|1600|503|1339451664560}2|605|5|1339451664763}2|573|140|1339451664881}1|573|147|1339451664969}2|497|280|1339451665193}2|467|280|1339451665209}2|437|285|1339451665225}2|422|285|1339451665233}2|343|335|1339451665390}1|336|337|1339451665493}2|338|336|1339451665647}2|385|332|1339451665673}" );
		
//		System.out.println( Context.current().request().headers().get("USER-AGENT")[0] );
//		System.out.println( Context.current().request().headers().get("ACCEPT-LANGUAGE")[0] );
//		System.out.println( Context.current().request().headers().get("CONNECTION")[0] );
//		System.out.println( Context.current().request().headers().get("ACCEPT")[0] );
//		System.out.println( Context.current().request().host() );
//		System.out.println( Context.current().request().method() );
//		System.out.println( Context.current().request().path() );
//		System.out.println( Context.current().request().uri() );
//		System.out.println( Context.current().request().acceptLanguages() );
//		System.out.println( Context.current().request().queryString() );
		
		TrackSession trackSess = null;
		
		//TODO:Track Api Keys && Domains
		if( session().containsKey("tracked_session") ) {
			trackSess = TrackSession.coll.findOneById( session().get("tracked_session") );
		} else {
			trackSess = new TrackSession();
			trackSess.startedAt = new Date();
			if( Context.current().request().headers().containsKey("USER-AGENT") && Context.current().request().headers().get("USER-AGENT").length > 0 ) trackSess.userAgent = Context.current().request().headers().get("USER-AGENT")[0];
			if( Context.current().request().headers().containsKey("ACCEPT-LANGUAGE") && Context.current().request().headers().get("ACCEPT-LANGUAGE").length > 0 ) trackSess.language = Context.current().request().headers().get("ACCEPT-LANGUAGE")[0];
			trackSess.host = Context.current().request().host();
			trackSess.userId = "4fdbb93244ae12efb6839f8d";
			trackSess._id = trackSess.save().getSavedId();
			
			session().put("tracked_session", trackSess._id);
			//TODO: get client IP using http proxy
		}
		RecordedLocation loc = null;
		if( session().containsKey("last_tracked_location") ) {
			loc =  RecordedLocation.coll.findOneById( session().get("last_tracked_location") );
		}
		for(int j = 0; j < dummy.size(); j++ ) {
			String[] actions = dummy.get(j).split("}");
			Long lastTs = 0L;
			for(int i = 0; i < actions.length; i++) {
				String[] parts = actions[i].split("[|]");
				if( parts.length < 1 ) continue;
				TrackedAction action = null;
				try {
					switch( Byte.valueOf( parts[0] ) ) {
						case 0:
							if( parts.length != 7 ) continue;
							//TODO:Track domains and pageUrl
							action = new TrackedAction();
							action.e = 0;
							action.location = parts[1];
							action.w = Short.valueOf( parts[2] );
							action.h = Short.valueOf( parts[3] );
							action.t = Short.valueOf( parts[4] );
							action.l = Short.valueOf( parts[5] );
							action.ts = Long.valueOf( parts[6] );
							loc = new RecordedLocation();
							loc.sessionId = trackSess._id;
							loc.startedAt = new Date( action.ts );
							loc.location = parts[1];
							loc._id = loc.save().getSavedId();
							break;
						case 1:
							if( parts.length != 4 ) continue;
							action = new TrackedAction();
							action.e = 1;
							action.x = Short.valueOf( parts[1] );
							action.y = Short.valueOf( parts[2] );
							action.ts = Long.valueOf( parts[3] );
							break;
						case 2:
							if( parts.length != 4 ) continue;
							action = new TrackedAction();
							action.e = 2;
							action.x = Short.valueOf( parts[1] );
							action.y = Short.valueOf( parts[2] );
							action.ts = Long.valueOf( parts[3] );
							break;
						case 3:
							if( parts.length != 4 ) continue;
							action = new TrackedAction();
							action.e = 3;
							action.w = Short.valueOf( parts[1] );
							action.h = Short.valueOf( parts[2] );
							action.ts = Long.valueOf( parts[3] );
							break;
						case 4:
							if( parts.length != 5 ) continue;
							action = new TrackedAction();
							action.e = 4;
							action.t = Short.valueOf( parts[1] );
							action.l = Short.valueOf( parts[2] );
							action.d = parts[3];
							action.ts = Long.valueOf( parts[4] );
							break;
						case 5:
							break;
					}
				} catch(NumberFormatException e) {
					continue;
				}

				if( action != null ) {
					action.recLocId = loc._id;
					action.save();
					lastTs = action.ts;
				}
			}
			if( lastTs > 0 ) {
				loc.lastActionAt = new Date( lastTs );
				trackSess.lastActionAt = new Date( lastTs );
				trackSess.save();
			}
			loc.save();
		}*/
		return ok();
	}
	
}
