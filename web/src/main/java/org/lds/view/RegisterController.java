package org.lds.view;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;
import org.lds.rs.dto.ListObject;
import org.lds.rs.util.SortResponse;
import org.lds.stack.ldsaccount.LdsAccountDetails;
import org.lds.util.ConfirmationNumber;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

@Controller
public class RegisterController {
	private static Logger logger = Logger.getLogger(RegisterController.class.getName());
	
	@Inject
	private Provider<LdsAccountDetails> accountDetails;

	@Resource(name="maxAttendence")
	public String max;

	@RequestMapping("/main")
	public String main(HttpServletRequest request) {
		Boolean waiting = new Boolean(false);
		long count = 0;
		BasicDBObject at = new BasicDBObject();
		try {
			long total_att = Long.parseLong(max);
			logger.info("inside main rest call for scheduler");
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			logger.info("grabbed mongodb, db is " + db);
			DBCollection coll = db.getCollection("attendees");

			String accountid = accountDetails.get().getAccountId();
			logger.info("accountid is " + accountid);
			BasicDBObject dbo = new BasicDBObject();
			dbo.put("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			count = cursor.count();
			while (cursor.hasNext()) {
				at = (BasicDBObject)cursor.next();
			}
			// Max Attendees  allowed in schedule page(Home) depending upon conference page maxAttendees field
			if (Boolean.valueOf(at.getString("waiting"))) {
				long total_reg = 0;
				if ((count > 0)) {
					DBCollection collConference = db.getCollection("conference");
					DBObject dbObject = null;
					DBCursor cursorconf = collConference.find();
					while (cursorconf.hasNext()) {
						dbObject = (DBObject) cursorconf.next();
						total_reg = (Long) dbObject.get("maxattendees");

					}
					if ((coll.count() <= total_reg) && (total_reg != 0)) {
						if (at!=null) {
							at.put("waiting", waiting);
							coll.save(at);
						}
					}else if ((coll.count() <= total_att)&& (total_reg == 0)) {
						if (at!=null) {
							at.put("waiting", waiting);
							coll.save(at);
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
        
		logger.info("count is " + count);
		if (count > 0) {
			waiting = at.getBoolean("waiting");
			if (waiting!=null && waiting.booleanValue()) {
				return "full";
			}
			return "schedule";
		} else {
			return "register";
		}
	}
	@RequestMapping(value="/register/userinfo.json")
	public @ResponseBody BasicDBObject getUserInfo() {
		BasicDBObject lo = new BasicDBObject();
		lo.put("username", accountDetails.get().getUsername());
		lo.put("accountid",accountDetails.get().getLdsAccountId());
		lo.put("name",accountDetails.get().getDisplayName());
		String email =  accountDetails.get().getLdsWdEmailAddress();
		if (email == null) {
			email = accountDetails.get().getLdsEmailAddress();
		}
		if (email == null) {
			email = accountDetails.get().getPersonalEmail();
		}
		if (email != null) {
			lo.put("email",email);
		}
		String gender = accountDetails.get().getLdsGender();
		if (gender != null) {
			lo.put("gender",gender);
		}
		return lo;
	}

	@RequestMapping(value="/register/email", method=RequestMethod.GET)
	public @ResponseBody ListObject getUserNameEmail(){
		String email =  accountDetails.get().getLdsEmailAddress();
		if (email == null){
			email = accountDetails.get().getPersonalEmail();
		}
		ListObject lo = new ListObject("email", email);
		return lo;
	}

	/*
	 * Register the User
	 */
	@RequestMapping(value="/register")
	public String initialRegister(
			MultipartHttpServletRequest request,
			@RequestParam(value="address")String address,
			@RequestParam(value="city")String city,
			@RequestParam(value="province")String province,
			@RequestParam(value="country")String country,
			@RequestParam(value="postal")String postal,
			@RequestParam(value="phone")String phone,
			@RequestParam(value="gender")String gender,
			@RequestParam(value="shirtsize")String shirtsize,
			@RequestParam(value="email")String email,
			@RequestParam(value="org")String org,
			@RequestParam(required=false, value="dietneeds") String dietneeds,
			@RequestParam(required=false, value="imagefile") MultipartFile imagefile) {
		Boolean waiting = new Boolean(false);
		try {
			Map<String,String[]> data = request.getParameterMap();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			long total_att = Long.parseLong(max);
			boolean n = true;
			String accountid = accountDetails.get().getLdsAccountId();
			String username = accountDetails.get().getUsername();
		    String name = accountDetails.get().getDisplayName();
			BasicDBObject dbo = new BasicDBObject();
			dbo.put("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			while (cursor.hasNext()) {
				dbo = (BasicDBObject)cursor.next();
				n = false;
			}
			if (n) {
				// FIXME: we should probably use accountid directly.
				// multiple unique keys for the same thing == Bad Idea
				dbo.put("id", new ObjectId().toString());
			}

			// Max Attendees  allowed in schedule page(Home) depending upon conference page maxAttendees field
			long total_reg = 0;
			DBCollection collConference = db.getCollection("conference");
			DBObject dbObject = null;
			DBCursor cursorconf = collConference.find();
			while (cursorconf.hasNext()) {
				dbObject = (DBObject) cursorconf.next();
				total_reg = Double.valueOf(dbObject.get("maxattendees").toString()).longValue();
			}
			if (total_reg != 0) {
				if (coll.count() >= total_reg) {
					waiting = new Boolean(true);
				}
			} else {
				if (coll.count() >= total_att) {
				waiting = new Boolean(true);
				}
			}

			BasicDBList list = new BasicDBList();
			for (Map.Entry<String,String[]> entry:data.entrySet()) {
			    if (entry.getKey().matches("day_.*"))
					list.add(entry.getValue()[0]);
			}

			dbo.put("days", list);
			dbo.put("email", email);
			dbo.put("organization", org);
			dbo.put("name", name);
			dbo.put("username", username);
			dbo.put("accountid", accountid);
			dbo.put("waiting", waiting);
			dbo.put("address", address);
			dbo.put("city", city);
			dbo.put("province", province);
			dbo.put("country", country);
			dbo.put("postal", postal);
			dbo.put("phone", phone);
			dbo.put("gender", gender);
			dbo.put("shirtsize", shirtsize);
			dbo.put("dietneeds", dietneeds);
			if (coll.count()==0) {
				dbo.put("presenter", true);
				dbo.put("approver", true);
				dbo.put("admin", true);
			} else {
				dbo.put("presenter", false);
				dbo.put("approver", false);
				dbo.put("admin", false);
			}
			dbo.put("submitpaperdisable", false);
			coll.save(dbo);
			// Clear restriction to recalculate on next page load
			request.getSession().removeAttribute("userurlrestriction");

			ConfirmationNumber cn = new ConfirmationNumber();

			cn.emailConfirmNumber(dbo, waiting);
		} catch(Exception e) {
			System.out.print(e);
		}
		if (waiting.booleanValue()) {
			return "full";
		}
		return "schedule";
	}

	
	/*
	 * Register the User
	 */
	@RequestMapping(value="/register/batch")
	public String batchRegister(
			MultipartHttpServletRequest request,
			@RequestParam(value="username")String username,
			@RequestParam(value="accountid")String accountid,
			@RequestParam(value="name")String name,
			@RequestParam(value="address")String address,
			@RequestParam(value="city")String city,
			@RequestParam(value="province")String province,
			@RequestParam(value="country")String country,
			@RequestParam(value="postal")String postal,
			@RequestParam(value="phone")String phone,
			@RequestParam(value="gender")String gender,
			@RequestParam(value="shirtsize")String shirtsize,
			@RequestParam(value="email")String email,
			@RequestParam(value="org")String org,
			@RequestParam(required=false, value="dietneeds") String dietneeds,
			@RequestParam(required=false, value="imagefile") MultipartFile imagefile) {
		Boolean waiting = new Boolean(false);
		try {
			Map<String,String[]> data = request.getParameterMap();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			long total_att = Long.parseLong(max);
			boolean n = true;
			//String accountid = accountDetails.get().getLdsAccountId();
			//String username = accountDetails.get().getUsername();
		    //String name = accountDetails.get().getDisplayName();
			
			BasicDBObject dbo = new BasicDBObject();
			dbo.put("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			while (cursor.hasNext()) {
				dbo = (BasicDBObject)cursor.next();
				n = false;
			}
			if (n) {
				// FIXME: we should probably use accountid directly.
				// multiple unique keys for the same thing == Bad Idea
				dbo.put("id", new ObjectId().toString());
			}

			// Max Attendees  allowed in schedule page(Home) depending upon conference page maxAttendees field
			long total_reg = 0;
			DBCollection collConference = db.getCollection("conference");
			DBObject dbObject = null;
			DBCursor cursorconf = collConference.find();
			while (cursorconf.hasNext()) {
				dbObject = (DBObject) cursorconf.next();
				total_reg = Double.valueOf(dbObject.get("maxattendees").toString()).longValue();
			}
			if (total_reg != 0) {
				if (coll.count() >= total_reg) {
					waiting = new Boolean(true);
				}
			} else {
				if (coll.count() >= total_att) {
				waiting = new Boolean(true);
				}
			}

			BasicDBList list = new BasicDBList();
			for (Map.Entry<String,String[]> entry:data.entrySet()) {
			    if (entry.getKey().matches("day_.*"))
					list.add(entry.getValue()[0]);
			}

			dbo.put("days", list);
			dbo.put("email", email);
			dbo.put("organization", org);
			dbo.put("name", name);
			dbo.put("username", username);
			dbo.put("accountid", accountid);
			dbo.put("waiting", waiting);
			dbo.put("address", address);
			dbo.put("city", city);
			dbo.put("province", province);
			dbo.put("country", country);
			dbo.put("postal", postal);
			dbo.put("phone", phone);
			dbo.put("gender", gender);
			dbo.put("shirtsize", shirtsize);
			dbo.put("dietneeds", dietneeds);
			if (coll.count()==0) {
				dbo.put("presenter", true);
				dbo.put("approver", true);
				dbo.put("admin", true);
			} else {
				dbo.put("presenter", false);
				dbo.put("approver", false);
				dbo.put("admin", false);
			}
			dbo.put("submitpaperdisable", false);
			coll.save(dbo);
			// Clear restriction to recalculate on next page load
			request.getSession().removeAttribute("userurlrestriction");

			ConfirmationNumber cn = new ConfirmationNumber();

			cn.emailConfirmNumber(dbo, waiting);
		} catch(Exception e) {
			System.out.print(e);
		}
		if (waiting.booleanValue()) {
			return "full";
		}
		return "schedule";
	}
	
	
	@RequestMapping(value = "/unregister", method=RequestMethod.GET)
	public String unregister(){
		return "unregister";
	}

	@RequestMapping(value="/quit")
	public @ResponseBody SortResponse quit(HttpServletRequest request){
		DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
		String accountid = accountDetails.get().getAccountId();
		BasicDBObject query = new BasicDBObject("accountid", accountid);
		db.getCollection("attendees").remove(query);
		SortResponse sr = new SortResponse();
		// Clear restriction to recalculate on next page load
		request.getSession().removeAttribute("userurlrestriction");
		return sr;
	}

	@RequestMapping(value="/register/{courseId}/{tentative}/", headers = "accept=application/json", method = RequestMethod.PUT)
	public @ResponseBody SortResponse setCourse(
			HttpServletRequest request,
			@PathVariable String courseId,
			@PathVariable String tentative) {
		SortResponse rr = new SortResponse();
		Boolean blTentative = new Boolean(tentative);
		String accountid = accountDetails.get().getLdsAccountId();
		BasicDBObject atFinder = new BasicDBObject();
		atFinder.put("accountid", accountid);
		BasicDBObject current = new BasicDBObject();
		try {
			if (courseId !=null){
				boolean registered = true;
				if (blTentative.booleanValue()) {
					registered = false;
				}

				DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
				DBCollection atColl = db.getCollection("attendees");
				DBCursor atCursor = atColl.find(atFinder);
				BasicDBObject at = new BasicDBObject();
				while (atCursor.hasNext()) {
					at = (BasicDBObject)atCursor.next();
				}

				BasicDBList regList = (BasicDBList)at.get("registered");
				BasicDBList tentList = (BasicDBList)at.get("tentative");

				if (tentList == null) {
					tentList = new BasicDBList();
					at.append("tentative", tentList);
				}
				if (regList == null) {
					regList = new BasicDBList();
					at.append("registered", regList);
				}

				if (registered) {
					if (!regList.contains(courseId)) {
						regList.add(courseId);
					}
						tentList.remove(courseId);
				} else {
					if (!tentList.contains(courseId)) {
						tentList.add(courseId);
					}
					regList.remove(courseId);
				}
				atColl.save(at);
			}
		} catch(Exception e){
			System.out.println(e);
			rr.setGood(false);
			rr.setMessage(e.getMessage());
		}
		return rr;
	}

	@RequestMapping(value="/register/{courseId}/", headers = "accept=application/json", method = RequestMethod.DELETE)
	public @ResponseBody SortResponse setCourse(HttpServletRequest request, @PathVariable String courseId) {
		SortResponse rr = new SortResponse();
		try {
			if (courseId != null){
				String accountid = accountDetails.get().getLdsAccountId();
				BasicDBObject atFinder = new BasicDBObject();
				atFinder.put("accountid", accountid);
				BasicDBObject current = new BasicDBObject();
				DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
				DBCollection coll = db.getCollection("attendees");
				DBCursor cur = coll.find(atFinder);
				while (cur.hasNext()) {
					current = (BasicDBObject)cur.next();
				}

				BasicDBList regList = (BasicDBList)current.get("registered");
				BasicDBList tentList = (BasicDBList)current.get("tentative");

				if (regList == null) {
					regList = new BasicDBList();
				}

				if (tentList == null) {
					tentList = new BasicDBList();
				}

				if (regList.contains(courseId)) {
					regList.remove(courseId);
				}

				if (tentList.contains(courseId)) {
					tentList.remove(courseId);
				}

				current.put("registered", regList);
				current.put("tentative", tentList);

				coll.save(current);
			}
		} catch(Exception e){
			rr.setGood(false);
			rr.setMessage(e.getMessage());
		}
		return rr;
	}

	@RequestMapping(value="/register/unregisterall/", headers = "accept=application/json", method = RequestMethod.DELETE)
	public @ResponseBody SortResponse unregisterAll(HttpServletRequest request){
		//SortResponse sr = registerService.unregisterAll();
		SortResponse rr = new SortResponse();
		try {
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject atFinder = new BasicDBObject();
			atFinder.put("accountid", accountid);
			BasicDBObject current = new BasicDBObject();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor cur = coll.find(atFinder);
			while (cur.hasNext()) {
				current = (BasicDBObject)cur.next();
			}

			BasicDBList regList = new BasicDBList();
			BasicDBList tentList = new BasicDBList();

			current.put("registered", regList);
			current.put("tentative", tentList);
			coll.save(current);
		} catch(Exception e) {
			rr.setGood(false);
			rr.setMessage(e.getMessage());
		}
		return rr;
	}

	@RequestMapping(value="/register/unregisteralltentative/", headers = "accept=application/json", method = RequestMethod.DELETE)
	public @ResponseBody SortResponse unregisterAllTentative(HttpServletRequest request){
		/*SortResponse sr = registerService.unregisterAllTentative();
		return sr;*/
		SortResponse rr = new SortResponse();
		try {
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject atFinder = new BasicDBObject();
			atFinder.put("accountid", accountid);
			BasicDBObject current = new BasicDBObject();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor cur = coll.find(atFinder);
			while (cur.hasNext()) {
				current = (BasicDBObject)cur.next();
			}
			BasicDBList tentList = new BasicDBList();
			current.put("tentative", tentList);
			coll.save(current);
		} catch(Exception e) {
			rr.setGood(false);
			rr.setMessage(e.getMessage());
		}
		return rr;
	}

	@RequestMapping(value="/register/loadProfile")
	public @ResponseBody BasicDBObject show(HttpServletRequest request) {
		Boolean waiting = new Boolean(false);
		BasicDBObject basicDBObject = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject dbo = new BasicDBObject();
			dbo.put("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			GridFS gfsPhoto = new GridFS(db, "attendees");
			if (cursor.hasNext()) {
				basicDBObject = (BasicDBObject)cursor.next();
				BasicDBObject query = new BasicDBObject("filename",basicDBObject.get("id"));
				GridFSDBFile out = gfsPhoto.findOne(query);
				//Save loaded image from database into new image file
				if (out!=null) {
					byte[] bytes = new byte[(int)out.getChunkSize()];
					out.getInputStream().read(bytes);
					basicDBObject.put("photo",bytes);
				}
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return basicDBObject;
	}

	/*
	 * Register the User
	 */
	@RequestMapping(value="/register/update")
	public String updateRegister(
			HttpServletRequest request,
			@RequestParam(value="address")String address,
			@RequestParam(value="city")String city,
			@RequestParam(value="province")String province,
			@RequestParam(value="country")String country,
			@RequestParam(value="postal")String postal,
			@RequestParam(value="phone")String phone,
			@RequestParam(value="gender")String gender,
			@RequestParam(value="shirtsize")String shirtsize,
			@RequestParam(value="email")String email,
			@RequestParam(value="org")String org,
			@RequestParam(required=false,value="dietneeds")String dietneeds,
			@RequestParam(value="presenter")String presenter,
			@RequestParam("imagefile") MultipartFile imagefile,
			@RequestParam(required=false, value="qualification")String qualification,
			@RequestParam(required=false, value="dob")String dob,
			@RequestParam(required=false, value="biography")String biography,
			@RequestParam(value="imageRemoveStatus")boolean imageRemoveStatus
			) {
		Boolean waiting = new Boolean(false);
		try {
			Map<String,String[]> data = request.getParameterMap();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			long total_att = Long.parseLong(max);
			boolean n = true;
			String accountid = accountDetails.get().getLdsAccountId();
			String username = accountDetails.get().getUsername();
		    String name = accountDetails.get().getDisplayName();
			BasicDBObject dbo = new BasicDBObject("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			while (cursor.hasNext()) {
				dbo = (BasicDBObject)cursor.next();
				n = false;
			}
			ObjectId objId = null;
			if (n) {
				objId = new ObjectId();
				dbo.put("id", objId.toString());
			}
			/*
			 * Image Saving into Mongodb
			 * attendees.files()
			 * attendees.chunk()
			 */
			GridFS gfsPhoto = new GridFS(db, "attendees");
			BasicDBObject query = null;
			String ext = null;
			if (!imagefile.isEmpty() && presenter.equalsIgnoreCase("true")) {
				String filename = imagefile.getOriginalFilename();
				String[] pieces = filename.split("\\.");
				ext = pieces[(pieces.length - 1)];
				String newFileName = dbo.getString("id");
				query = new BasicDBObject("filename",dbo.get("id"));
				gfsPhoto.remove(query);
				GridFSInputFile gfsFile = gfsPhoto.createFile(imagefile.getBytes());
				gfsFile.setFilename(newFileName);
				gfsFile.save();
				dbo.put("fileext", ext);
			} else if (imageRemoveStatus || presenter.equalsIgnoreCase("false")) {
				gfsPhoto.remove(query);
				if (dbo.get("fileext")!=null) {
					dbo.remove("fileext");
					dbo.put("fileext","");
				}
			} else {
				ext = (String)dbo.get("fileext");
			}

			if (presenter.equalsIgnoreCase("true")) {
				dbo.put("qualification", qualification);
				dbo.put("dob", dob);
				dbo.put("biography", biography);
			} else {
				dbo.put("dob","");
				dbo.put("qualification","");
				dbo.put("fileext","");
				dbo.put("biography","");
			}

			BasicDBList list = new BasicDBList();

			for (Map.Entry<String,String[]> entry:data.entrySet()) {
			    if (entry.getKey().matches("day_.*"))
					list.add(entry.getValue()[0]);
			}

			dbo.put("days", list);
			dbo.put("accountid", accountid);
			dbo.put("email", email);
			dbo.put("organization", org);
			dbo.put("name", name);
			dbo.put("username", username);
			dbo.put("waiting", waiting);
			dbo.put("address", address);
			dbo.put("city", city);
			dbo.put("province", province);
			dbo.put("country", country);
			dbo.put("postal", postal);
			dbo.put("phone", phone);
			dbo.put("gender", gender);
			dbo.put("shirtsize", shirtsize);
			dbo.put("dietneeds", dietneeds);
			dbo.put("presenter", Boolean.parseBoolean(presenter));
			coll.save(dbo);
		} catch(Exception e) {
			System.out.print(e);
		}
		return "profile";
	}

	/*
	 *  Presenter Info Inserting
	 */
	@RequestMapping(value="/register/presenter")
	public String updatePresenterRegister(HttpServletRequest request, @RequestParam("imagefile") MultipartFile imagefile,@RequestParam(value="qualification")String qualification,@RequestParam(value="dob")String dob,@RequestParam(value="biography")String biography) {
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject dbo = new BasicDBObject("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			while (cursor.hasNext()) {
				dbo = (BasicDBObject)cursor.next();
			}
			/*
			 * Image Saving into Mongodb
			 * attendees.files()
			 * attendees.chunk()
			 */
			GridFS gfsPhoto = new GridFS(db, "attendees");
			BasicDBObject query = null;
				if (!imagefile.isEmpty()) {
					String ext = null;
					String filename = imagefile.getOriginalFilename();
					String[] pieces = filename.split("\\.");
					ext = pieces[(pieces.length - 1)];
					String newFileName = dbo.getString("id");
					query = new BasicDBObject("filename",dbo.get("id"));
					gfsPhoto.remove(query);
					GridFSInputFile gfsFile = gfsPhoto.createFile(imagefile.getBytes());
					gfsFile.setFilename(newFileName);
					gfsFile.save();
					dbo.put("fileext", ext);
				}
				dbo.put("qualification", qualification);
				dbo.put("dob", dob);

				dbo.put("biography", biography);
				dbo.put("presenter", true);
				coll.save(dbo);

		} catch(Exception e) {
			System.out.print(e);
		}
		return "paper";
	}

	@RequestMapping(value="/register/profile", method = RequestMethod.GET)
	public String profile(HttpServletRequest request) {
		 return "profile";
	}

	@RequestMapping(value="/register/menu")
	public @ResponseBody Object[] menu(HttpServletRequest request) {
		Boolean waiting = new Boolean(false);
		List<String> menuList = new ArrayList<String>();
		menuList.add("Logout"+":"+"?signmeout");
		try {
			boolean datelastsubmissioncompleted = verifySubmitPaperEnable(request);
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject dbo = new BasicDBObject();
			dbo.put("accountid", accountid);
			DBCursor cursor = coll.find(dbo);
			BasicDBObject basicDBObject;
			if (cursor.hasNext()) {
				basicDBObject = (BasicDBObject)cursor.next();
				boolean admin = Boolean.valueOf(String.valueOf(basicDBObject.get("admin")));
				boolean approver = Boolean.valueOf(String.valueOf(basicDBObject.get("approver")));
				boolean presenter = Boolean.valueOf(String.valueOf(basicDBObject.get("presenter")));
				(menuList).add("Print"+":"+"course/calendar.pdf");
				(menuList).add("Profile"+":"+"register/profile");
				(menuList).add("Feedback"+":"+"feedback");
				//Allow admins and approvers to submit papers after the deadline, but no one else
				if (!datelastsubmissioncompleted || admin || approver) {
					(menuList).add("Submit Paper"+":"+"paper");
				}
				if (!admin && !approver && presenter) {
					(menuList).add("My Papers"+":"+"course/papers");
				}
				if (admin || approver) {
					(menuList).add("Papers"+":"+"course/papers");
					(menuList).add("Courses"+":"+"courses");
				}
				if (admin) {
					(menuList).add("Conference"+":"+"conference");
					(menuList).add("Attendees"+":"+"attendee/attendees");
					(menuList).add("Dashboard"+":"+"dashboard/view");
				}
			}
			// TODO: make these configurable by conference or by installaltion
			//(menuList).add("Info"+":"+"/wiki/LDSTech_Conference");
			(menuList).add("Schedule"+":"+"");
			// TODO: make these configurable by conference or by installaltion
			//(menuList).add("LDSTech"+":"+"/");
		} catch(Exception e) {
			System.out.print(e);
		}
		return menuList.toArray();
	}

	// Verify the Last Date submission of paper
	// Meaning, return true if the paper submission deadline has passed.
	public boolean verifySubmitPaperEnable(HttpServletRequest request){
		boolean datelastsubmissioncompleted = false;
		try {
			//Retrieve the current date
			Calendar currentDate = Calendar.getInstance();

			//Retrieve the paper submission deadline from Mongo
			//	Set up the connection to Mongo and point to the deadline
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			BasicDBObject dbo = null;
			DBCollection conferenceColl = db.getCollection("conference");
			DBCursor dbconferenceCursor = conferenceColl.find();
			dbo = (BasicDBObject) dbconferenceCursor.next();
			//  Extract and format the date
			SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd");
			DecimalFormat tzFormat = new DecimalFormat("'+'#.#;'-'#.#");
			Calendar deadline = Calendar.getInstance(TimeZone.getTimeZone("GMT" + tzFormat.format(dbo.getDouble("timezone"))));
			deadline.setTime(dateForm.parse(dbo.getString("datelast")));
			int totalOffset = currentDate.getTimeZone().getOffset(currentDate.getTimeInMillis()) -
				deadline.getTimeZone().getOffset(deadline.getTimeInMillis());
			deadline.add(Calendar.MILLISECOND, totalOffset);
			//After all this, the deadline compare won't work unless the day is incremented by one:
			deadline.add(Calendar.DATE, 1);

			//Compare the dates; return true if past the deadline
			if (currentDate.after(deadline)) {
				datelastsubmissioncompleted = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datelastsubmissioncompleted;
	}
}
