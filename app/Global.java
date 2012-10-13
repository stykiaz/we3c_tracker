import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import akka.util.Duration;
import play.Application;
import play.GlobalSettings;
import play.Play;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import setups.AppConfig;


public class Global extends GlobalSettings {

	@Override
	public Result onError(RequestHeader arg1, Throwable arg0) {
		
		if( !AppConfig.isDev() ) {

			StringWriter errors = new StringWriter();
			arg0.printStackTrace(new PrintWriter(errors));
			//			System.out.println( "stack: " + errors );

			java.util.Properties props = new java.util.Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.host", AppConfig.mail_smtpHost);
			props.put("mail.smtp.port", AppConfig.mail_smtpPort);

			Session session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(AppConfig.mail_smtpUsername, AppConfig.mail_smtpPassword);
				}
			});
			try {

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress( AppConfig.mail_fromEmail ));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse( AppConfig.developerEmail ));
				message.setSubject("Server error in Tracker - " + ((play.api.PlayException.UsefulException)arg0).id());

				String text = "mode: " + Play.application().configuration().getString("app.envirement") + "id: " + ((play.api.PlayException.UsefulException)arg0).id() + "\n title: " + ((play.api.PlayException.UsefulException)arg0).title() + "\n" + "descr: " + ((play.api.PlayException.UsefulException)arg0).description() + "\n " + "msg: " + arg0.getMessage() + "\nstack: " + errors;

				message.setText( text );
				Transport.send( message );

			} catch (MessagingException e) {
			}
			return super.onError(arg1, arg0);
			//			return Results.internalServerError( views.html.custom_500.render( ((play.api.PlayException.UsefulException)arg0).id() ) );
		} else {
			return super.onError(arg1, arg0);
		}
	}

	@Override
	public Result onHandlerNotFound(play.mvc.Http.RequestHeader arg0) {
		if( AppConfig.isProd() ) {
			return super.onHandlerNotFound(arg0);
		} else {
			return super.onHandlerNotFound(arg0);
		}
	}

	@Override
	public void onStart(Application app) {
		if( Play.application().configuration().getString("app.envirement").equals("dev") ) {
			AppConfig.setupDevEnv();
		} else if( Play.application().configuration().getString("app.envirement").equals("test") ) {
			AppConfig.setupTestEnv();
		} else if( Play.application().configuration().getString("app.envirement").equals("prod") ) {
			AppConfig.setupProdEnv();
		}

		play.libs.Akka.system().scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS), 
				Duration.create(1, TimeUnit.DAYS),
				new Runnable() {
					public void run() {
						System.out.println("tick");
						File dir = new File( AppConfig.temporaryFilesDirectory );
						for( String fname : dir.list() ) {
							if( fname.equals(".") || fname.equals("..") ) continue;
							File tmp = new File( AppConfig.temporaryFilesDirectory + fname );
							if( tmp.exists() && tmp.isFile() && ( new Date().getTime() - tmp.lastModified() ) > ( 30L * 86400L * 1000L ) ) {
								tmp.delete(); 
							}
						}
					}
				}
		);

		super.onStart(app);
	}

}
