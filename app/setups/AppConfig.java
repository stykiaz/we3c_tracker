package setups;

import play.Play;

public class AppConfig {

	public static String uploadDirectory;
	public static String uploadDirectoryCache;
	public static String temporaryFilesDirectory;
	public static String appRootDirectory;
	public static String domain;
	
	
	public static String mail_smtpPassword; 
	public static String mail_smtpUsername; 
	public static String mail_smtpHost; 
	public static String mail_smtpPort; 
	
	public static String mail_fromEmail = "stan@wethreecreatives.com";
	public static String developerEmail = "stan@wethreecreatives.com";
	public static String supportEmail = "stan@wethreecreatives.com";
	
	public static String pathToHtmlToImageGenerator;

	public static String googleAnalyticsCode;
	
	public static boolean isProd() {
		return Play.application().configuration().getString("app.envirement").equals("prod");
	}
	public static boolean isTest() {
		return Play.application().configuration().getString("app.envirement").equals("test");
	}
	public static boolean isDev() {
		return Play.application().configuration().getString("app.envirement").equals("dev");
	}
	
	public static void setupDevEnv() {
		appRootDirectory = Play.application().path().getAbsolutePath()+"/";//"/www/sites/superyachts.com/v2/dev-properties/";
		uploadDirectory = "/media/ext3/www/htdocs/work/we3c/uploads/";
		uploadDirectoryCache = "/media/ext3/www/htdocs/work/we3c/uploads/cache/";
		temporaryFilesDirectory = "/media/ext3/www/htdocs/work/we3c/tracker_tmp/";
		pathToHtmlToImageGenerator = "/media/ext3/www/htdocs/work/we3c/wkhtmltoimage-i386";
		domain = "localhost:9001";
		
		mail_smtpUsername = "stan@wethreecreatives.com";
		mail_smtpPassword = "alfanero3";
		mail_smtpHost = "smtpout.europe.secureserver.net";
		mail_smtpPort = "465";
		
		googleAnalyticsCode = "";
		
	}
	public static void setupTestEnv() {
		appRootDirectory = Play.application().path().getAbsolutePath()+"/";//"/www/sites/superyachts.com/v2/dev-properties/";
		uploadDirectory = "/media/ext3/www/htdocs/work/we3c/uploads/";
		uploadDirectoryCache = "/media/ext3/www/htdocs/work/we3c/uploads/cache/";
		temporaryFilesDirectory = "/media/ext3/www/htdocs/work/we3c/tracker_tmp/";
		
		pathToHtmlToImageGenerator = "/media/ext3/www/htdocs/work/we3c/wkhtmltoimage-i386";
		domain = "clickheat.wethreecreatives.com";
		
		mail_smtpUsername = "stan@wethreecreatives.com";
		mail_smtpPassword = "alfanero3";
		mail_smtpHost = "smtpout.europe.secureserver.net";
		mail_smtpPort = "465";
		
		googleAnalyticsCode = "";
		
	}
	public static void setupProdEnv() {
		appRootDirectory = Play.application().path().getAbsolutePath()+"/";//"/www/sites/superyachts.com/v2/dev-properties/";
		uploadDirectory = "/media/ext3/www/htdocs/work/we3c/uploads/";
		uploadDirectoryCache = "/media/ext3/www/htdocs/work/we3c/uploads/cache/";
		temporaryFilesDirectory = "/media/ext3/www/htdocs/work/we3c/tracker_tmp/";
		pathToHtmlToImageGenerator = "/media/ext3/www/htdocs/work/we3c/wkhtmltoimage-i386";
		domain = "clickheat.wethreecreatives.com";
		
		mail_smtpUsername = "stan@wethreecreatives.com";
		mail_smtpPassword = "alfanero3";
		mail_smtpHost = "smtpout.europe.secureserver.net";
		mail_smtpPort = "465";
		
		googleAnalyticsCode = "";
	}
	
}
