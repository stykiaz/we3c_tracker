import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import setups.AppConfig;
import utils.Tools;


public class Global extends GlobalSettings {

	@Override
	public Result onError(Throwable arg0) {
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
				message.setSubject("Server error in Properties - " + ((play.api.PlayException.UsefulException)arg0).id());

				String text = "mode: " + Play.application().configuration().getString("app.envirement") + "id: " + ((play.api.PlayException.UsefulException)arg0).id() + "\n title: " + ((play.api.PlayException.UsefulException)arg0).title() + "\n" + "descr: " + ((play.api.PlayException.UsefulException)arg0).description() + "\n " + "msg: " + arg0.getMessage() + "\nstack: " + errors;

				message.setText( text );
				Transport.send( message );

			} catch (MessagingException e) {
			}
			return super.onError(arg0);
//			return Results.internalServerError( views.html.custom_500.render( ((play.api.PlayException.UsefulException)arg0).id() ) );
		} else {
			return super.onError(arg0);
		}
	}

	@Override
	public Result onHandlerNotFound(String uri) {
		if( AppConfig.isProd() ) {
			return super.onHandlerNotFound( uri );
//			return Results.notFound( views.html.custom_404.render() );
		} else {
			return super.onHandlerNotFound( uri );
		}

	}

	@Override
	public void onStart(Application arg0) {
		if( Play.application().configuration().getString("app.envirement").equals("dev") ) {
			AppConfig.setupDevEnv();
		} else if( Play.application().configuration().getString("app.envirement").equals("test") ) {
			AppConfig.setupTestEnv();
		} else if( Play.application().configuration().getString("app.envirement").equals("prod") ) {
			AppConfig.setupProdEnv();
		}
		super.onStart(arg0);
	}

}
