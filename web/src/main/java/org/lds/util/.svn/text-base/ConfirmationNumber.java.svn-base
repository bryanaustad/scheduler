package org.lds.util;

import java.net.URL;
import java.util.Date;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.mail.HtmlEmail;

import com.mongodb.BasicDBObject;

public class ConfirmationNumber {

	@Resource(name="emailHost")
	private String host;
	@Resource(name="emailFromAddress")
	private String emailFromAddress;
	@Resource(name="emailFromTitle")
	private String emailFromTitle;
	@Resource(name="emailSubject")
	private String emailSubject;
	@Resource(name="emailLogoURL")
	private String emailLogoURL;
	@Resource(name="emailLogoDescription")
	private String emailLogoDescription;
	@Resource(name="emailTitle")
	private String emailTitle;
	@Resource(name="conferenceTitle")
	private String conferenceTitle;
	@Resource(name="conferenceDate")
	private String conferenceDate;
	@Resource(name="unregisterURL")
	private String unregisterURL;

	public String genConfirmNumber() {
		Date date = new Date();
		return "" + date.getTime();
	}

	public String emailConfirmNumber(BasicDBObject attendee, Boolean waiting) {
		String host = null;
		String emailFromAddress = null;
		String emailFromTitle = null;
		String emailSubject = null;
		String emailLogoURL = null;
		String emailLogoDescription = null;
		String emailTitle = null;
		String conferenceTitle = null;
		String conferenceDate = null;
		String unregisterURL = null;
		try {
			// FIXME: all these should come from the conference collection
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			host = (String) envCtx.lookup("EmailHost");
			emailFromAddress = (String)envCtx.lookup("EmailFromAddress");
			emailFromTitle = (String)envCtx.lookup("EmailFromTitle");
			emailSubject = (String)envCtx.lookup("EmailSubject");
			emailLogoURL = (String)envCtx.lookup("EmailLogoURL");
			emailLogoDescription = (String)envCtx.lookup("EmailLogoDescription");
			emailTitle = (String)envCtx.lookup("EmailTitle");
			conferenceTitle = (String)envCtx.lookup("ConferenceTitle");
			conferenceDate = (String)envCtx.lookup("ConferenceDate");
			unregisterURL = (String)envCtx.lookup("UnRegisterURL");
		}
		catch (NamingException e) {
			e.printStackTrace();
		}

		String confirm = genConfirmNumber();
		try {
			// Create the email message
			HtmlEmail email = new HtmlEmail();
			email.setHostName(host);
			email.addTo(attendee.getString("email"), attendee.getString("name"));

			email.setFrom(emailFromAddress, emailFromTitle);
			email.setSubject(emailSubject);

			// embed the image and get the content id
			URL url = new URL(emailLogoURL);
			String cid = email.embed(url, emailLogoDescription);
			if(waiting!=null){
				if(!waiting.booleanValue()){
					// set the html message
					email.setHtmlMsg("<html><img src=\"cid:"
							+ cid
							+ "\">"
							+ "<br/><br/>"
							+ "<h2>" + emailTitle + "</h2>"
							+ "<br/>"
							+ "<h3>Thank you for registering for "+ conferenceTitle + ", " + conferenceDate + ".  Your confirmation number is "
							+ confirm
							+ ".</h3>"
							+ "<p/>"
							+ "<br/>" + "<h3>Here is the registration information that you've provided us: </h3>"
							+ "<br/>" + "Name: <b>" + attendee.getString("name") + "</b><br/>" + "Email: <b>"
							+ attendee.getString("email") + "</b><br/>" + "Organization: <b>" + attendee.getString("organization")
							+ "</b><br/>");
							//+ "To unregister click this link: <a href=\"" + unregisterURL + attendee.getString("id") + "\">unregister me</a>");
					// set the alternative message
					email.setTextMsg("Thank you for registering with " + conferenceTitle + ", " + conferenceDate + ".  Your confirmation number is " + confirm);
				}else{
					//set a new message
					// set the html message
					email.setHtmlMsg("<html><img src=\"cid:"
							+ cid
							+ "\">"
							+ "<br/><br/>"
							+ "<h2>" + emailTitle + "</h2>"
							+ "<br/>"
							+ "<h3>We are sorry.  We are at capacity for what we can handle for "+ conferenceTitle + ", " + conferenceDate + ".  We've put you on a waiting list in case things change."
							+ " Thank you for your interest and desire to share your talents with us."
							+ ".</h3>"
							+ "<p/>"
							+ "<br/>" + "<h3>Here is the registration information that you've provided us: </h3>"
							+ "<br/>" + "Name: <b>" + attendee.getString("name") + "</b><br/>" + "Email: <b>"
							+ attendee.getString("email") + "</b><br/>" + "Organization: <b>" + attendee.getString("organization")
							+ "</b><br/>");
					// set the alternative message
					email.setTextMsg("We are sorry. We are at capacity for what we can handle for " + conferenceTitle + ", " + conferenceDate + ".  We've put you on a waiting list in case things change. Thank you for your interest and desire to share your talents with us.");
				}
			}
			// send the email
			email.send();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return confirm;
	}
}
