package org.lds.intercepter;

import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lds.stack.ldsaccount.LdsAccountDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class RegisterIntercepter extends HandlerInterceptorAdapter {

	private static Logger logger = Logger.getLogger(RegisterIntercepter.class.getName());
	
	@Inject
	private Provider<LdsAccountDetails> accountDetails;

	// before the actual handler will be executed
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler)
			throws Exception {
		Enumeration headers = request.getHeaderNames();
		//System.out.println("***** request headers *****");
		while (headers.hasMoreElements()) {
			String name = (String)headers.nextElement();
			System.out.println(name + ":" + request.getHeader(name));
		}
		String path = request.getRequestURI();
		//Pattern pat = Pattern.compile("(.[a-z]+.course)|(.[a-z]+.main)|(.[a-z]+.print)|(.[a-z]+.courses)|(.[a-z]+.paper)|(.[a-z]+.coursefeedback)|(.[a-z]+.conferencefeedback)|(.[a-z]+.feedback)|(.[a-z]+.thankyou)|(.[a-z]+.conference)|(.[a-z]+.roles)|(.[a-z]+.dashboard)|(.[a-z]+.papers)|(.[a-z]+.getserverdata)");
		logger.info("Hello request where are you.");
		logger.info("request is " + request);
		Pattern pat = Pattern.compile(urlRestrictionByRole(request));
		logger.info("identified Pattern");
		Matcher mat = pat.matcher(path);

		if (!mat.lookingAt()) {
			// FIXME: add ?signmein if we're not signed in
			response.setHeader("Location", response.encodeRedirectURL("/scheduler/main"));
			response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
			return false;
		}
		return true;
	}

	private String urlRestrictionByRole(HttpServletRequest request) {
		String userurlrestriction = "";
		try {
			//userurlrestriction = String.valueOf(request.getSession().getAttribute("userurlrestriction"));
			if(userurlrestriction ==null || userurlrestriction.equalsIgnoreCase("null") || userurlrestriction.equalsIgnoreCase("(.[a-z]+.main)")) {
				logger.info("URL is: "+request.getRequestURL());
				logger.info("accountDetails is " + accountDetails);
				if(accountDetails != null && accountDetails.get() != null) {
					String accountid = accountDetails.get().getAccountId();
					logger.info("accountid is " + accountid);
					DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
					logger.info("mongo db is " + db);
					DBCollection coll = db.getCollection("attendees");
					logger.info("Mongo collection:attendees is " + coll);
					BasicDBObject dbo = new BasicDBObject("accountid", accountid);
					String admin =
							"(.[a-z]+.conference)|"+
							"(.[a-z]+.dashboard)|"+
							"(.[a-z]+.getserverdata)|"+
							"(.[a-z]+.roles)|";
					String approver =
							"(.[a-z]+.courses)|"+
							"(.[a-z]+.course)|";
					String presenter =
							"(.[a-z]+.papers)|";
					String attendee =
							"(.[a-z]+.conferencefeedback)|"+
							"(.[a-z]+.confirmed)|"+
							"(.[a-z]+.coursefeedback)|"+
							"(.[a-z]+.feedback)|"+
							"(.[a-z]+.main)|"+
							"(.[a-z]+.paper)|"+
							"(.[a-z]+.print)|"+
							"(.[a-z]+.quit)|"+
							"(.[a-z]+.schedule)|"+
							"(.[a-z]+.thankyou)|"+
							"(.[a-z]+.unregister)";
	
					DBCursor cursor = coll.find(dbo);
					logger.info("db cursor is " + cursor);
					if (cursor.hasNext()) {
						dbo = (BasicDBObject)cursor.next();
						if (Boolean.valueOf(dbo.getString("admin"))) {
							userurlrestriction = admin+approver+presenter+attendee;
						} else if (Boolean.valueOf(dbo.getString("approver"))) {
							userurlrestriction = approver+presenter+attendee;
						} else if (Boolean.valueOf(dbo.getString("presenter"))) {
							userurlrestriction = presenter+attendee;
						} else {
							userurlrestriction = attendee;
						}
					} else {
						userurlrestriction =
								"(.[a-z]+.main)|"+
								"(.[a-z]+.register)";
					}
					logger.info("setting userurlrestriction to " + userurlrestriction);
					request.getSession().setAttribute("userurlrestriction", userurlrestriction);
					logger.info("set userurlrestriction");
				}
			}
		} catch (Exception ex) {
			logger.info("errored in urlRestrictionByRole");
			ex.printStackTrace();
		}
		return userurlrestriction;
	}
}
