package utils;

import java.io.BufferedReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;

import play.api.libs.Crypto;
import play.libs.Json;
import setups.AppConfig;

public class IO {

	public static String toString( InputStream is ) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in;
		try {
			in = new InputStreamReader(is, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "";
		}
		try {
		  int read;
		  do {
		    read = in.read(buffer, 0, buffer.length);
		    if (read > 0) {
		      out.append(buffer, 0, read);
		    }
		  } while (read>=0);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} finally {
		  in.close();
		}
		String result = out.toString();
		return result;
	}
	
	public static void writeToRemoteFiler(File file, String path, String name) throws Exception {
		HttpClient httpclient;
		httpclient = new DefaultHttpClient();
		if( AppConfig.isDev() ) {
			httpclient = WebClientDevWrapper.wrapClient(httpclient);
		}
		try {
            HttpPost httppost = new HttpPost("https://" + AppConfig.remoteFilerService + "/filer/add");

            FileBody bin = new FileBody( file );
            StringBody pathParam = new StringBody( path );
            StringBody originalName = new StringBody( name );
            String phrase = UUID.randomUUID().toString();
            StringBody passPhrase = new StringBody( phrase );
            StringBody signedPassPhrase = new StringBody( Crypto.sign(phrase) );

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file", bin);
            reqEntity.addPart("path", pathParam);
            reqEntity.addPart("originalName", originalName);
            reqEntity.addPart("passPhrase", passPhrase);
            reqEntity.addPart("signedPassPhrase", signedPassPhrase);

            httppost.setEntity(reqEntity);

//            System.out.println("executing request " + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

//            System.out.println("----------------------------------------");
//            System.out.println(response.getStatusLine());
            if (resEntity != null) {
//                System.out.println("Response content length: " + resEntity.getContentLength());
                String output = IO.toString( resEntity.getContent() ).trim();
//                System.out.println( output );
                JsonNode node = Json.parse( output );
                if( node.get("return_code") == null ) {
                	throw new Exception("Error with remote filer service !");
                } else if( !node.get("return_code").getTextValue().equals("200") ) {
                	throw new Exception("Error with remote filer service ( "+node.get("return_code").getTextValue()+" ) !");
                } 
            } else {
            	throw new Exception("Error with remote filer service !");
            }
            EntityUtils.consume(resEntity);
        } finally {
            try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignore) {}
        }
	}
	
	/**
	 * Read file content to a String
	 * @param file The file to read
	 * @return The String content
	 * @throws IOException 
	 */
	public static String readContentAsString(File file, String encoding) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			StringWriter result = new StringWriter();
			PrintWriter out = new PrintWriter(result);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
			String line = null;
			while ((line = reader.readLine()) != null) {
				out.println(line);
			}
			return result.toString();
		} catch(IOException e) {
			throw new IOException();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(Exception e) {
					//
				}
			}
		}
	}



	/**
	 * Read binary content of a file (warning does not use on large file !)
	 * @param file The file te read
	 * @return The binary data
	 * @throws IOException 
	 */
	public static byte[] readContent(File file) throws IOException {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			byte[] result = new byte[(int) file.length()];
			is.read(result);
			return result;
		} catch(IOException e) {
			throw new IOException();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(Exception e) {
					//
				}
			}
		}
	}

	/**
	 * Read binary content of a stream (warning does not use on large file !)
	 * @param is The stream to read
	 * @return The binary data
	 * @throws IOException 
	 */
	public static byte[] readContent(InputStream is) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = 0;
			byte[] buffer = new byte[8096];
			while ((read = is.read(buffer)) > 0) {
				baos.write(buffer, 0, read);
			}
			return baos.toByteArray();
		} catch(IOException e) {
			throw new IOException();
		}
	}

	/**
	 * Write String content to a stream (always use utf-8)
	 * @param content The content to write
	 * @param os The stream to write
	 * @throws IOException 
	 */
	public static void writeContent(CharSequence content, OutputStream os) throws IOException {
		writeContent(content, os, "utf-8");
	}

	/**
	 * Write String content to a stream (always use utf-8)
	 * @param content The content to write
	 * @param os The stream to write
	 * @throws IOException 
	 */
	public static void writeContent(CharSequence content, OutputStream os, String encoding) throws IOException {
		try {
			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os, encoding));
			printWriter.println(content);
			printWriter.flush();
			os.flush();
		} catch(IOException e) {
			throw new IOException();
		} finally {
			try {
				os.close();
			} catch(Exception e) {
				//
			}
		}
	}


	/**
	 * Write String content to a file (always use utf-8)
	 * @param content The content to write
	 * @param file The file to write
	 * @throws IOException 
	 */
	public static void writeContent(CharSequence content, File file, String encoding) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os, encoding));
			printWriter.println(content);
			printWriter.flush();
			os.flush();
		} catch(IOException e) {
			throw new IOException();
		} finally {
			try {
				if(os != null) os.close();
			} catch(Exception e) {
				//
			}
		}
	}

	/**
	 * Write binay data to a file
	 * @param data The binary data to write
	 * @param file The file to write
	 * @throws IOException 
	 */
	public static void write(byte[] data, File file) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(data);
			os.flush();
		} catch(IOException e) {
			throw new IOException();
		} finally {
			try {
				if(os != null) os.close();
			} catch(Exception e) {
				//
			}
		}
	}

	/**
	 * Copy an stream to another one.
	 * @throws IOException 
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		try {
			int read = 0;
			byte[] buffer = new byte[8096];
			while ((read = is.read(buffer)) > 0) {
				os.write(buffer, 0, read);
			}
		} catch(IOException e) {
			throw new IOException();
		} finally {
			try {
				is.close();
			} catch(Exception e) {
				//
			}
		}
	}

	/**
	 * Copy an stream to another one.
	 * @throws IOException 
	 */
	public static void write(InputStream is, OutputStream os) throws IOException {
		try {
			int read = 0;
			byte[] buffer = new byte[8096];
			while ((read = is.read(buffer)) > 0) {
				os.write(buffer, 0, read);
			}
		} catch(IOException e) {
			throw new IOException();
		} finally {
			try {
				is.close();
			} catch(Exception e) {
				//
			}
			try {
				os.close();
			} catch(Exception e) {
				//
			}
		}
	}

	/**
	 * Copy an stream to another one.
	 * @throws IOException 
	 */
	public static void write(InputStream is, File f) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(f);
			int read = 0;
			byte[] buffer = new byte[8096];
			while ((read = is.read(buffer)) > 0) {
				os.write(buffer, 0, read);
			}
		} catch(IOException e) {
			throw new IOException(e.getMessage());
		} finally {
			try {
				is.close();
			} catch(Exception e) {
				//
			}
			try {
				if(os != null) os.close();
			} catch(Exception e) {
				//
			}
		}
	}

	// If targetLocation does not exist, it will be created.
	public static void copyDirectory(File source, File target) throws IOException {
		if (source.isDirectory()) {
			if (!target.exists()) {
				target.mkdir();
			}
			for (String child: source.list()) {
				copyDirectory(new File(source, child), new File(target, child));
			}
		} else {
			try {
				write(new FileInputStream(source),  new FileOutputStream(target));
			} catch (IOException e) {
				throw new IOException();
			}
		}
	}

}
