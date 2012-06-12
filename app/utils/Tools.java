package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.JsonNode;

import play.api.libs.Crypto;
import play.libs.Json;

public class Tools {

	public static String md5Encode(String input) {
		if( input == null ) return "";
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			byte[] out = m.digest(input.getBytes());
			final String result = new String(Hex.encodeHex(out));
			return result;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		} catch(NullPointerException e) {
			return "";
		}
		
	}
	/**
	 * generate random password
	 * @return
	 */
	public static String generateRandomPassword() {
		String uuid = UUID.randomUUID().toString().substring(0, 10);
		return uuid;
	}

	public static String base64Encode(String input) {
		return utils.Base64.encode( input.getBytes() );
	}
	/**
	 * get the file extension
	 * @param file
	 * @return
	 */
	public static String getFileExtention(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		String ext = name.substring(pos+1);
		return ext;
	}
	/**
	 * get the file extension
	 * @param name
	 * @return
	 */
	public static String getFileExtention(String name) {
		int pos = name.lastIndexOf('.');
		String ext = name.substring(pos+1);
		return ext;
	}
	public static String join(ArrayList<String> data, String delimiter) {
	    StringBuffer buffer = new StringBuffer();
	    Iterator<String> iter = data.iterator();
	    while (iter.hasNext()) {
	        buffer.append(iter.next());
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}
}
