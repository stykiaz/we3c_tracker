package utils;

import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;

import play.mvc.Http;

public class TemplateHelpers {

	public static String getContextValueOr(String key, String altVal){
		try {
			if( !Http.Context.current().args.containsKey( key ) ) return altVal;
			return (String) Http.Context.current().args.get( key );
		} catch( RuntimeException e ) {
			return altVal;
		}
	}
	
		
	public static ArrayList<String> getContextRequiredJs(){
		try {
			
			return ( ArrayList<String> ) Http.Context.current().args.get( "requiredJsScripts" );
		} catch( RuntimeException e ) {
			return null;
		}
	}
	public static ArrayList<String> getContextRequiredCss(){
		try {
			
			return ( ArrayList<String> ) Http.Context.current().args.get( "getContextRequiredCss" );
		} catch( RuntimeException e ) {
			return null;
		}
	}
	
	public static String wrapTextToLength( String input, int length ) {
		input = input.replaceAll("\n", " ").trim();
		if( input.length() <= length ) return input;
		String ret = WordUtils.wrap(input.replaceAll("\n", " ").trim(), length, "\n", true).split("\n")[0] + "..";
		return ret;
	}
	
	public static String getCookieValue( String key ) {
		if( Http.Context.current().request().cookies().get( key ) != null && !Http.Context.current().request().cookies().get(key).value().isEmpty() ) {
//			System.out.println( Http.Context.current().request().cookies().get(key).value() );
			return Http.Context.current().request().cookies().get(key).value();
		} 
		return null;
	}
	
}
