package org.lds.view;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.lds.stack.ldsaccount.LdsAccountDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

@Controller
@RequestMapping("/roles/")
public class RoleController {

	@Inject
	private Provider<LdsAccountDetails> accountDetails;

	// set permissions for an attendee
	// TODO: one method to update ALL attendee properties
	@RequestMapping(value ="roleassign.json")
	public String roleassign(
			HttpServletRequest request,
			@RequestParam("accountid") String accountid,
			@RequestParam(required=false, value="admin") String admin,
			@RequestParam(required=false, value="approver") String approver,
			@RequestParam(required=false, value="presenter") String presenter,
			@RequestParam(required=false, value="submitpaperdisable") String submitpaperdisable) {
		BasicDBObject dbo = null;
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection atCol = db.getCollection("attendees");
			dbo = new BasicDBObject("accountid", accountid);
			DBCursor cursor = atCol.find(dbo);
			if (cursor.hasNext()) {
				dbo = (BasicDBObject)cursor.next();
				dbo.put("admin", (admin!=null));
				dbo.put("presenter", (presenter!=null));
				dbo.put("approver", (approver!=null));
				dbo.put("submitpaperdisable", (submitpaperdisable!=null));
				atCol.save(dbo);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "attendees";
	}

	@RequestMapping(value="/get")
	public @ResponseBody BasicDBObject getRole(HttpServletRequest request) {
		BasicDBObject basicDBObject = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject dbo = new BasicDBObject();
			dbo.put("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			if(cursor.hasNext()) {
				basicDBObject = (BasicDBObject)cursor.next();
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return basicDBObject;
	}
}
