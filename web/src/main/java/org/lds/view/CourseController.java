package org.lds.view;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.lds.rs.dto.AutoCompleteDTO;
import org.lds.rs.dto.CourseDateBean;
import org.lds.rs.dto.ScheduleCourseDTO;
import org.lds.stack.ldsaccount.LdsAccountDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
@RequestMapping("/course/")
public class CourseController {
	@Inject
	private Provider<LdsAccountDetails> accountDetails;
	private static final String THANK_YOU_AND_LOGOUT = "thankyou";

	//Submit course feedback
	@RequestMapping(value="feedback")
	public String submitCourseFeedback(
		HttpServletResponse res,
		HttpServletRequest request,
		@RequestParam("source") String sourceName,
		@RequestParam("sourceId") String sourceId,
		@RequestParam("name") String courseName,
		@RequestParam("courseid") String courseId,
		@RequestParam("authorlist") String presenterNames,
		@RequestParam("content-quality") String contentRating,
		@RequestParam("presenter-quality") String presenterRating,
		@RequestParam("like") String like,
		@RequestParam("suggest") String suggest) {
	String submitterName = "";
	try {
		BasicDBObject feedback = new BasicDBObject();

		DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
		DBCollection atCol = db.getCollection("attendees");
		DBCollection fColl = db.getCollection("feedback");
		String submitterId = accountDetails.get().getLdsAccountId();
		BasicDBObject atFinder = new BasicDBObject();
		atFinder.put("accountid", submitterId);
		DBCursor atCursor = atCol.find(atFinder);

		while (atCursor.hasNext()) {
			BasicDBObject temp = (BasicDBObject)atCursor.next();
			submitterName = (String)temp.get("name");
		}

		BasicDBObject fFinder = new BasicDBObject();
		fFinder.put("sourceId", sourceId);
		fFinder.put("courseId", courseId);
		DBCursor fCursor = fColl.find(fFinder);

		while (fCursor.hasNext()) {
			feedback = (BasicDBObject)fCursor.next();
		}

		String[] parseP = presenterNames.split(";");
		BasicDBList listP = new BasicDBList();

		for (String p:parseP) {
			listP.add(p.trim());
		}

		feedback.put("sourceId", sourceId);
		feedback.put("sourceName", sourceName);
		feedback.put("submitterId", submitterId);
		feedback.put("submitterName", submitterName);
		feedback.put("courseId", courseId);
		feedback.put("courseName", courseName);
		feedback.put("presenterNames", listP);
		feedback.put("contentRating", contentRating);
		feedback.put("presenterRating", presenterRating);
		feedback.put("like", like);
		feedback.put("suggest", suggest);

		fColl.save(feedback);
	} catch(Exception e) {
		e.printStackTrace();
	}
	return THANK_YOU_AND_LOGOUT;
}

//Submit conference feedback
@RequestMapping(value="confeedback")
public String submitConfFeedback(
		HttpServletRequest request,
		@RequestParam("sourceId") String sourceId) {
	String submitterName = "";
	try {
		Map<String,String[]> data = request.getParameterMap();
		BasicDBObject feedback = new BasicDBObject();

		DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
		DBCollection attendees = db.getCollection("attendees");
		String submitterId = accountDetails.get().getLdsAccountId();
		BasicDBObject atFinder = new BasicDBObject();
		atFinder.put("accountid", submitterId);
		DBCursor atCursor = attendees.find(atFinder);

		if (!atCursor.hasNext()) {
			return "conferencefeedback";
		}
		BasicDBObject attendee = (BasicDBObject)atCursor.next();
		submitterName = attendee.getString("name");

		if (!attendee.getBoolean("admin") && !attendee.getBoolean("approver") && !submitterId.equals(sourceId) ) {
			return "conferencefeedback";
		}

		BasicDBObject fFinder = new BasicDBObject();
		fFinder.put("sourceId", sourceId);
		DBCollection fColl = db.getCollection("conf_feedback");
		DBCursor fCursor = fColl.find(fFinder);

		if (fCursor.hasNext()) {
			feedback = (BasicDBObject)fCursor.next();
		}

		feedback.put("submitterId", submitterId);
		feedback.put("submitterName", submitterName);
		for (Map.Entry<String,String[]> entry:data.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue()[0];
			feedback.put(key,value);
		}

		fColl.save(feedback);
	} catch(Exception e) {
		e.printStackTrace();
	}
	return THANK_YOU_AND_LOGOUT;
}

//Get feedback for viewing/editing
//	Conference
@RequestMapping(value="getfeedback/conf.json")
public @ResponseBody BasicDBObject getConfFeedback(
			HttpServletRequest request,
			@RequestParam("sourceId") String sourceId) {
		BasicDBObject feedback = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("conf_feedback");
			DBCursor cursor;
			BasicDBObject query;
			cursor = coll.find(new BasicDBObject("sourceId", sourceId));
			if (cursor.hasNext()) {
				feedback = (BasicDBObject)cursor.next();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return feedback;
	}
	
	//	Course
	@RequestMapping(value="getfeedback/course.json")
	public @ResponseBody BasicDBObject getCourseFeedback(
			HttpServletRequest request,
			@RequestParam("sourceId") String sourceId,
			@RequestParam("courseId") String courseId) {
		BasicDBObject feedback = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("feedback");
			DBCursor cursor;
			BasicDBObject query;
			query = new BasicDBObject("sourceId", sourceId);
			query.put("courseId", courseId);
			cursor = coll.find(query);
			if (cursor.hasNext()) {
				feedback = (BasicDBObject)cursor.next();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return feedback;
	}
	
	//Call to view feedback
	@RequestMapping(value="viewfeedback")
	public String viewFeedback() {
		return "viewfeedback";
	}
	
	//Retrieve courses with feedback and feedback sources
	@RequestMapping(value="/feedbackinfo.json")
	public @ResponseBody BasicDBObject getFeedbackInfo(
		HttpServletRequest request,
		@RequestParam(required=false, value="course_id") String courseIdParam,
		@RequestParam(required=false, value="sourceId") String sourceIdParam){ 
		BasicDBObject data = new BasicDBObject();
		try {
			Map<String, String> courses = new TreeMap<String, String>();
			Map<String, String> sources = new TreeMap<String, String>();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection courseFeedbackColl = db.getCollection("feedback");
			DBCollection confFeedbackColl = db.getCollection("conf_feedback");
			DBCursor cursor = courseFeedbackColl.find();
			DBCursor confCursor = confFeedbackColl.find();
			String confName = (String) db.getCollection("conference").findOne().get("name");
			
			//Conference feedback		
			while (confCursor.hasNext()) {
				BasicDBObject tempBDBO = (BasicDBObject)confCursor.next();
				String sourceId = tempBDBO.getString("sourceId");
				if((sourceIdParam == null) || (sourceIdParam.equals(sourceId))) {
					courses.put("conf", confName);
				}
				if((courseIdParam == null) || (courseIdParam.equals("conf")))	{
					sources.put(sourceId, tempBDBO.getString("source"));
				}
			}
			//Course feedback	
			while (cursor.hasNext()) {
				BasicDBObject tempBDBO = (BasicDBObject)cursor.next();
				String courseId = tempBDBO.getString("courseId");
				String sourceId = tempBDBO.getString("sourceId");
				if((sourceIdParam == null) || (sourceIdParam.equals(sourceId))) {
					courses.put(courseId, tempBDBO.getString("courseName"));
				}
				if((courseIdParam == null) || (courseIdParam.equals(courseId)))	{
					sources.put(sourceId, tempBDBO.getString("sourceName"));
				}
			}
			data.put("courses", courses);
			data.put("sources", sources);
		} catch (Exception e) {
			if (1 == 1)
			{
				int temp = 1;
			}
		}
		return data;
	}
	
	@RequestMapping(value="courses.json")
	public @ResponseBody Collection<BasicDBObject> getCourses(
			HttpServletRequest request){
		Map<String,BasicDBObject> results = new HashMap<String,BasicDBObject>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection attendees = db.getCollection("attendees");
			DBCollection courses = db.getCollection("courses");
			DBCursor cCursor = courses.find();

			BasicDBList regList = new BasicDBList();
			BasicDBList tentList = new BasicDBList();

			while (cCursor.hasNext()) {
				BasicDBObject course = (BasicDBObject)cCursor.next();
				// add registered and tentative counts
				Pattern idpattern = Pattern.compile(course.get("id")+"_.*");
				course.put("registered", attendees.find(new BasicDBObject("registered", idpattern)).count());
				course.put("tentative", attendees.find(new BasicDBObject("tentative", idpattern)).count());
				results.put(String.valueOf(course.get("id")),course);
					}
		} catch(Exception e) {
			System.out.print(e);
		}
		return results.values();
	}

	@RequestMapping(value="list.json")
	public @ResponseBody DBObject getCourseList(
			HttpServletRequest request) {
		BasicDBList list = new BasicDBList();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection atCol = db.getCollection("attendees");
			DBCollection cCol = db.getCollection("courses");
			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject atFinder = new BasicDBObject();
			atFinder.put("accountid", accountid);
			DBCursor atCursor = atCol.find(atFinder);
			DBCursor cCursor = cCol.find();

			BasicDBList regList = new BasicDBList();
			BasicDBList tentList = new BasicDBList();

			while (atCursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)atCursor.next();
				regList = (BasicDBList)dbo.get("registered");
				tentList = (BasicDBList)dbo.get("tentative");
			}

			while (cCursor.hasNext()) {
				BasicDBObject c = (BasicDBObject)cCursor.next();
				if (regList!=null) {
					if (regList.contains(c.get("id"))) {
						c.put("registered", true);
					} else {
						c.put("registered", false);
					}
				}
				if (tentList!=null) {
					if (tentList.contains(c.get("id"))) {
						c.put("tentative", true);
					} else {
						c.put("tentative", false);
					}
				}
				list.add(c);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return list;
	}

	@RequestMapping(value="listapproved.json")
	public @ResponseBody DBObject getApprovedCourseList(
			HttpServletRequest request) {
		BasicDBList list = new BasicDBList();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection conferenceCol = db.getCollection("conference");
			DBCursor conferenceCursor =  conferenceCol.find();
			if (!conferenceCursor.hasNext())
				return list;
			BasicDBObject conference = (BasicDBObject)conferenceCursor.next();
			long timezone = (long)(conference.getDouble("timezone")*3600000);

			DBCollection attendees = db.getCollection("attendees");
			DBCursor atCursor = attendees.find(new BasicDBObject("accountid",accountDetails.get().getLdsAccountId()));
			if (!atCursor.hasNext())
				return list;
			BasicDBObject attendee = (BasicDBObject)atCursor.next();
			BasicDBList regList = (BasicDBList)attendee.get("registered");
			BasicDBList	tentList = (BasicDBList)attendee.get("tentative");
			BasicDBList	conferencedaysList = (BasicDBList)attendee.get("days");

			DBCollection courses = db.getCollection("courses");
			// admin
			DBCursor cCursor = null;
			if (attendee.getBoolean("admin") || attendee.getBoolean("approver")) {
				// admins and approvers get entries that are not yet approve_tech
				cCursor = courses.find();
			} else
				cCursor = courses.find(new BasicDBObject("approve_tech", true));
			while (cCursor.hasNext()) {
				BasicDBObject course = (BasicDBObject)cCursor.next();
				if (!course.getBoolean("approve_cor") && !attendee.getBoolean("admin") && !attendee.getBoolean("approver")) {
					// don't show non-admin/approver slides that are not cor approved
					course.removeField("filedisplayname");
					course.removeField("filename");
				}
				if (course.containsField("sessions")) {
					BasicDBList sessions=(BasicDBList)course.get("sessions");
					for (int i=0;i<sessions.size();i++) {
						BasicDBObject session = (BasicDBObject)course.clone();
						session.removeField("sessions");
						session.put("id", (String)course.get("id") + '_' + i);
						BasicDBObject times = (BasicDBObject)sessions.get(i);
						session.putAll((Map)times);
						if (times.containsField("postname")) {
							session.put("name", (String)course.get("name")+times.get("postname"));
							session.removeField("postname");
						}
						if (regList!=null && regList.contains(session.get("id"))) {
							session.put("registered", true);
						} else {
							session.put("registered", false);
						}
						if (tentList!=null && tentList.contains(session.get("id"))) {
							session.put("tentative", true);
						} else {
							session.put("tentative", false);
						}
						list.add(session);
					}
				} else {
					list.add(course);
				}
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return list;
	}

	public Date getUTCDate(Date inputDate) {
		Date d = null;
		try {
			if (inputDate == null) return d;
			d = new Date();
			SimpleDateFormat sourceFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String sourceDate = sourceFormatter.format(inputDate);
			SimpleDateFormat destinationFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			destinationFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			String result = destinationFormatter.format(inputDate);
			d = sourceFormatter.parse(result);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return d;
	}

	@RequestMapping(value="autolist.json")
	public @ResponseBody Collection<AutoCompleteDTO> getAutoCourseList(
			HttpServletRequest request,
			@RequestParam(required=false, value="term") String term){
		Map<String,AutoCompleteDTO> results = new HashMap<String,AutoCompleteDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");

			BasicDBObject coursequery = new BasicDBObject();
			if (term != null) {
				coursequery.put("name", Pattern.compile(term, Pattern.CASE_INSENSITIVE));
			}

			String accountid = accountDetails.get().getAccountId();
			DBCollection attendees = db.getCollection("attendees");
			DBCursor cursor = attendees.find(new BasicDBObject("accountid", accountid));
			if (cursor.hasNext()) {
				BasicDBObject attendee = (BasicDBObject)cursor.next();
				if (!attendee.getBoolean("admin") && !attendee.getBoolean("approver"))
					coursequery.put("accountid", accountid);
			}

			DBCollection courses = db.getCollection("courses");
			cursor = courses.find(coursequery);
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				AutoCompleteDTO dto = new AutoCompleteDTO();
				dto.setId(dbo.getString("id"));
				dto.setLabel(dbo.getString("name"));
				dto.setValue(dbo.getString("name"));
				results.put(dbo.getString("id"),dto);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return results.values();
	}

	@RequestMapping(value="approvedautolist.json")
	public @ResponseBody Collection<AutoCompleteDTO> getApprovedAutoCourseList(
			HttpServletRequest request, 
			@RequestParam(value="term") String term){
		Map<String,AutoCompleteDTO> results = new HashMap<String,AutoCompleteDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");

			DBCollection courses = db.getCollection("courses");
			BasicDBObject query = new BasicDBObject("approve_tech", true);
			query.put("name", Pattern.compile(term, Pattern.CASE_INSENSITIVE));
			DBCursor cursor = courses.find(query);

			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				AutoCompleteDTO dto = new AutoCompleteDTO();
				dto.setId(dbo.getString("id"));
				dto.setLabel(dbo.getString("name"));
				dto.setValue(dbo.getString("name"));
				results.put(dbo.getString("id"),dto);
			}
		} catch(Exception e) {
			System.out.print(e);
		}

		return results.values();
	}

	@RequestMapping(value="list.single.json")
	public @ResponseBody DBObject getSingleCourse(
			HttpServletRequest request,
			@RequestParam(required=false, value="name") String name,
			@RequestParam(required=false, value="id") String id){
		DBObject course = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			String accountid = accountDetails.get().getAccountId();

			BasicDBObject coursequery = new BasicDBObject();
			DBCollection courses = db.getCollection("courses");
			if (id != null && id != "")
				coursequery.put("id", id);
			else if (name!=null && name!="")
				coursequery.put("name", name);
			else
				return course;

			DBCollection attendees = db.getCollection("attendees");
			DBCursor cursor = attendees.find(new BasicDBObject("accountid", accountid));
			if (!cursor.hasNext()) {
				return course;
			}

			BasicDBObject attendee = (BasicDBObject)cursor.next();
			if (!attendee.getBoolean("admin") && !attendee.getBoolean("approver")) {
				// if we're not an admin or approver restrict to our accountid or approved_tech 
				BasicDBObject approvequery = (BasicDBObject)coursequery.clone();
				coursequery.put("accountid", accountid);
				BasicDBList coursequerylist = new BasicDBList();
				coursequerylist.add(coursequery);
				approvequery.put("approve_tech", true);
				coursequerylist.add(approvequery);
				cursor = courses.find(new BasicDBObject("$or", coursequerylist));
			} else {
				cursor = courses.find(coursequery);
			}
			if (cursor.hasNext()) {
				course = (BasicDBObject) cursor.next();
				// add registered and tentative counts
				Pattern idpattern = Pattern.compile(course.get("id")+"_.*");
				course.put("registered", attendees.find(new BasicDBObject("registered", idpattern)).count());
				course.put("tentative", attendees.find(new BasicDBObject("tentative", idpattern)).count());
				if (course.containsField("sessions")) {
					BasicDBList sessions=(BasicDBList)course.get("sessions");
					for (int i=0;i<sessions.size();i++) {
						BasicDBObject session = (BasicDBObject)sessions.get(i);
						session.put("registered", attendees.find(new BasicDBObject("registered", course.get("id")+"_"+i)).count());
						session.put("tentative", attendees.find(new BasicDBObject("tentative", course.get("id")+"_"+i)).count());						
					}
				}
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return course;
	}

	@RequestMapping(value="{id}/{approveType}/unapprovecourse.json")
	public @ResponseBody DBObject UnApproveCourse(
			HttpServletRequest request,
			@PathVariable String id,
			@PathVariable String approveType) {
		DBObject dbo = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("courses");
			BasicDBObject query = new BasicDBObject("id", id);

			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				dbo = cursor.next();
			}

			if (approveType !=null) {
				if (approveType.equals("approve_tech")) {
					dbo.put("approve_tech", new Boolean(false));
				} else if (approveType.equals("approve_ip")) {
					dbo.put("approve_ip", new Boolean(false));
				} else if (approveType.equals("approve_cor")) {
					dbo.put("approve_cor", new Boolean(false));
				} else if (approveType.equals("general")) {
					dbo.put("general", new Boolean(false));
				}
			}
			coll.save(dbo);
		} catch(Exception e) {
			System.out.print(e);
		}
		return dbo;
	}

	@RequestMapping(value="{id}/{approveType}/approvecourse.json")
	public @ResponseBody DBObject approveCourse(
			HttpServletRequest request,
			@PathVariable String id,
			@PathVariable String approveType) {
		DBObject dbo = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("courses");
			BasicDBObject query = new BasicDBObject("id", id);

			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				dbo = cursor.next();
			}
			if (approveType !=null) {
				if (approveType.equals("approve_tech")) {
					dbo.put("approve_tech", new Boolean(true));
				} else if (approveType.equals("approve_ip")) {
					dbo.put("approve_ip", new Boolean(true));
				} else if (approveType.equals("approve_cor")) {
					dbo.put("approve_cor", new Boolean(true));
				} else if (approveType.equals("general")) {
					dbo.put("general", new Boolean(true));
				}
			}
			coll.save(dbo);
		} catch(Exception e) {
			System.out.print(e);
		}
		return dbo;
	}

	@RequestMapping(value="crud")
	public String setCourse(
			HttpServletRequest request,
			@RequestParam("name") String name,
			@RequestParam("id") String id,
			@RequestParam("desc") String desc,
			@RequestParam(required=false, value="url") String url,
			@RequestParam("authorlist") String authorlist,
			@RequestParam("email") String email,
			@RequestParam("accountid") String accountid,
			@RequestParam("taglist") String taglist,
			@RequestParam("audiencelist") String audiencelist,
			@RequestParam("track") String track,
			@RequestParam(required=false, value="general") boolean general,
			@RequestParam(required=false, value="approve_tech") boolean approve_tech,
			@RequestParam(required=false, value="approve_ip") boolean approve_ip,
			@RequestParam(required=false, value="approve_cor") boolean approve_cor,
			@RequestParam(required=false, value="datafile") MultipartFile datafile) {
		BasicDBObject current = null;
		Map<String,String[]> data = request.getParameterMap();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection conferenceCol = db.getCollection("conference");
			DBCursor conferenceCursor =  conferenceCol.find();
			long timezone = 0;
			String tz = "UTC";
			while (conferenceCursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)conferenceCursor.next();
				timezone = (long)(dbo.getDouble("timezone")*3600000);
				// FIXME: we assume the first entry. We should store the actual entry someplace
				tz=TimeZone.getAvailableIDs((int)timezone)[0];
			}

			DBCollection coll = db.getCollection("courses");
			BasicDBObject query = new BasicDBObject("id", id);
			DBCursor cur = coll.find(query);
			if (cur.hasNext()) {
				current = (BasicDBObject)cur.next();
				current.put("accountid", accountid);
				current.put("email", email);
			} else {
				ObjectId objId = new ObjectId();
				current = new BasicDBObject();
				id = objId.toString();
				current.put("id", id);

				if (accountid == null || accountid.isEmpty()) {
					accountid = accountDetails.get().getLdsAccountId();
				}
				current.put("accountid", accountid);

				DBCollection attendeesCol = db.getCollection("attendees");

				if (email == null || email.isEmpty()) {
					email = "";
					DBCursor attCursor = attendeesCol.find(new BasicDBObject("accountid",accountid));
					if (attCursor.hasNext()) {
						BasicDBObject basicDBObject = (BasicDBObject) attCursor.next();
						email = basicDBObject.getString("email");
					}
				}
				current.put("email", email);
			}

			String[] parseAuthor = authorlist.split(";");
			BasicDBList listA = new BasicDBList();
			for (String a:parseAuthor) {
				if (!a.trim().isEmpty())
					listA.add(a.trim());
			}
			current.put("authors", listA);

			String[] parseTag = taglist.split(";");
			BasicDBList listT = new BasicDBList();
			for (String t:parseTag) {
				if (!t.trim().isEmpty())
					listT.add(t.trim());
			}
			current.put("tags", listT);

			String[] audienceTag = audiencelist.split(";");
			BasicDBList listAudience = new BasicDBList();
			for (String a:audienceTag) {
				if (!a.trim().isEmpty())
					listAudience.add(a.trim());
			}
			current.put("audience", listAudience);

			current.put("name", name.trim());
			current.put("desc", desc.trim());
			if (url!=null)
				current.put("url", url.trim());
			current.put("general", general);
			current.put("approve_tech", approve_tech);
			current.put("track", track.trim());
			current.put("approve_ip", approve_ip);
			current.put("approve_cor", approve_cor);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d h:mm aa");
			formatter.setTimeZone(TimeZone.getTimeZone(tz));
			ArrayList<BasicDBObject> listSession = new ArrayList<BasicDBObject>();
			// Forming the session list to save into DB
			for (int i=0;data.containsKey("day_"+i);i++) {
				BasicDBObject dbo = new BasicDBObject();
				Date start=new Date();
				Date end=new Date();
				if (data.get("day_"+i)!=null && data.get("start_"+i)!=null) {
					start=formatter.parse(data.get("day_"+i)[0]+" "+data.get("start_"+i)[0]);
					dbo.put("start",start.getTime());
				}
				if (data.get("day_"+i)!=null && data.get("end_"+i)!=null) {
					end=formatter.parse(data.get("day_"+i)[0]+" "+data.get("end_"+i)[0]);
					dbo.put("end",end.getTime());
				}
				dbo.put("track_len",(end.getTime() - start.getTime())/60000);
				if (data.get("room_"+i)!=null)
					dbo.put("room",data.get("room_"+i)[0]);
				// FIXME: should add part names after we sort by date
				// saving twice should work
				if (data.get("day_1")!=null)
					dbo.put("postname", "-PART "+(i+1));
				listSession.add(dbo);
			}
			Collections.sort(listSession, new Comparator<BasicDBObject>() {
				public int compare(BasicDBObject a, BasicDBObject b) {
					return (int)((Long)a.get("start") - (Long)b.get("start"));
				}
			});
			current.put("sessions", listSession);

			if (datafile != null && !datafile.isEmpty()) {
				GridFS gfsFile = new GridFS(db, "courses");
				String oldfilename = current.getString("filename");
				if (oldfilename != null)
					gfsFile.remove(oldfilename);
				String filedisplayname = datafile.getOriginalFilename();
				String[] pieces = filedisplayname.split("\\.");
				String ext = pieces[(pieces.length - 1)];
				String filename = id.toString() + "." + ext;
				if (filedisplayname.length() > 0)
					current.append("filedisplayname", filedisplayname);
				current.append("filename", filename);
				GridFSInputFile newgfsFile = gfsFile.createFile(datafile.getBytes());
				newgfsFile.setFilename(filename);
				newgfsFile.setMetaData(new BasicDBObject("fileext",ext));
				newgfsFile.save();
			}
			coll.save(current);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "courses";
	}

	@RequestMapping(value="delete/{id}")
	public String deleteCourse(
				HttpServletRequest request,
				@PathVariable String id) {
		DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
		BasicDBObject query = new BasicDBObject("id", id);
		// FIXME: check that either we match the accountid or we are an admin
		DBObject courses = db.getCollection("courses").findAndRemove(query);
		//Remove the attachment (if there is one)
		if (courses != null) {
			String filename = (String) courses.get("filename");
			if (filename != null) {
				GridFS gfsCollection = new GridFS(db, "courses");
				gfsCollection.remove(filename);
			}
		}
		return "courses";
	}

	/* Generate PDF for schedule courses */
	@RequestMapping(value="calendar.pdf")
	public void getConferenceSchedulePDF(HttpServletRequest request,HttpServletResponse response) {
		try {
			String accountid = accountDetails.get().getLdsAccountId();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");

			BasicDBObject dbo = null;

			long timezone = 0;
			String conferencename = null;
			DBCollection conferenceColl = db.getCollection("conference");
			DBCursor conferenceCursor = conferenceColl.find();

			if (!conferenceCursor.hasNext()) {
				return;
			}

				dbo = (BasicDBObject)conferenceCursor.next();
				timezone = (long)(dbo.getDouble("timezone")*3600000);
				conferencename = dbo.getString("name");

			BasicDBList regList = new BasicDBList();
			BasicDBList tentList = new BasicDBList();
			String username = "";
			DBCollection attendeesColl = db.getCollection("attendees");
			BasicDBObject accountidQuery = new BasicDBObject("accountid", accountid);
			DBCursor attendeesCursor = attendeesColl.find(accountidQuery);
			BasicDBList conferencedaysList = new BasicDBList();

			while (attendeesCursor.hasNext()) {
					dbo = (BasicDBObject)attendeesCursor.next();
					regList = (BasicDBList)dbo.get("registered");
					tentList = (BasicDBList) dbo.get("tentative");
				username = dbo.getString("username");
					conferencedaysList = (BasicDBList)dbo.get("days");
			}

			List<CourseDateBean> courseInfoforUser = getScheduleforUser(db, regList, tentList, timezone,conferencedaysList);
			byte[] imagebytes =getLogo(db);
			sendResponseSchedulePDF(request, response, courseInfoforUser, accountid, conferencename, username, imagebytes);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	private byte[] getLogo(DB db) {
		DBCollection coll = db.getCollection("conference");
		DBCursor cursor =  coll.find();
		byte[] bytes = null;
		try {
			GridFS gfsPhoto = new GridFS(db, "conference");
			BasicDBObject query = new BasicDBObject("filename","logo");
			GridFSDBFile out = gfsPhoto.findOne(query);
			if (out!=null) {
				 bytes = new byte[(int)out.getChunkSize()];
				 out.getInputStream().read(bytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	private List<CourseDateBean> getScheduleforUser(
			DB db,
			BasicDBList regList,
			BasicDBList tentList,
			long timezone,
			BasicDBList conferencedaysList) {
		List<CourseDateBean> courseDateList = new ArrayList<CourseDateBean>();
		DBCursor cursor = null;

		try {
			DBCollection courses = db.getCollection("courses");
			cursor = courses.find();
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				if (Boolean.valueOf(dbo.getString("approve_tech"))) {
					if (dbo.containsField("sessions")) {
						BasicDBList sessions=(BasicDBList)dbo.get("sessions");
						for (int i=0;i<sessions.size();i++) {
							BasicDBObject session = (BasicDBObject)sessions.get(i);
							String state = null;
							if (dbo.getBoolean("general")) {
								state = "General";
							} else {
								for (Object obj:tentList) {
									if (String.valueOf(obj).equalsIgnoreCase(dbo.getString("id")+'_'+i)) {
										state = "Tentative";
									}
								}
								for (Object obj:regList) {
									if (String.valueOf(obj).equalsIgnoreCase(dbo.getString("id")+'_'+i)) {
										state = "Register";
									}
								}
							}
							if (state != null) {
								ScheduleCourseDTO scheduleCourseDTO = new ScheduleCourseDTO();
								if (session.containsField("postname"))
									scheduleCourseDTO.setCourseName(dbo.getString("name")+" "+session.getString("postname"));
								else
									scheduleCourseDTO.setCourseName(dbo.getString("name"));
								scheduleCourseDTO.setPresenter(dbo.getString("authors"));
								scheduleCourseDTO.setRoomNo(session.getString("room"));
								scheduleCourseDTO.setCourseid(dbo.getString("id")+'_'+i);
								scheduleCourseDTO.setSessionstate(state);
								Date sdate = new Date(Long.valueOf(session.getLong("start"))+timezone);
								sdate = getUTCDate(sdate);
								scheduleCourseDTO.getStart().setTimeInMillis(sdate.getTime());
								Date edate = new Date(Long.valueOf(session.getLong("end"))+timezone);
								edate = getUTCDate(edate);
								scheduleCourseDTO.getEnd().setTimeInMillis(edate.getTime());
								CourseDateBean courseDateBean = null;
								Date date = new Date(Long.valueOf(session.getLong("start"))+timezone);
								date = getUTCDate(date);
								date.setHours(0);
								date.setMinutes(0);
								date.setSeconds(0);
								boolean found = false;
								for (CourseDateBean cBean : courseDateList) {
									if (date.compareTo(cBean.getCoursedate())==0) {
										found = true;
										courseDateBean = cBean;
										break;
									}
								}
								if (!found) {
									courseDateBean = new CourseDateBean();
									courseDateBean.setCoursedate(date);
									courseDateList.add(courseDateBean);
								}
								courseDateBean.getScheduleCourseDTOColl().add(scheduleCourseDTO);
								Collections.sort(courseDateBean.getScheduleCourseDTOColl());
							}
						}
					}
				}
			} //while end
			Collections.sort(courseDateList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return courseDateList;
	}

	private void sendResponseSchedulePDF(HttpServletRequest request, HttpServletResponse response,
			List<CourseDateBean> courseInfoforUser, String accountid, String conferencename, String username,
			byte[] imagebytes) throws IOException {
		InputStream inputStream = null;
		BufferedInputStream bufferedInputStream = null;
		ServletOutputStream servletOutputStream = null;

		try {
			ByteArrayOutputStream createPDF = createPDF(courseInfoforUser, conferencename, username, imagebytes);
			response.reset();
			response.setContentLength((int) createPDF.toByteArray().length);
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\"schedule_" + username + ".pdf\"");
			//response.setHeader("Pragma", "public");
			response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
			//response.setHeader("Cache-Control", "no-store");
			//response.addDateHeader("Expires", 0);
			servletOutputStream = response.getOutputStream();

			bufferedInputStream = new BufferedInputStream(inputStream);
			byte[] outputByte = createPDF.toByteArray();

			servletOutputStream.write(outputByte);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedInputStream != null) {
				bufferedInputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
			if (servletOutputStream != null) {
				servletOutputStream.flush();
				servletOutputStream.close();
			}
		}
	}

	private ByteArrayOutputStream createPDF(List<CourseDateBean> courseDateList,String conferencename,String username,byte[] imagebytes) throws IOException {
		Document document=null;
		ByteArrayOutputStream pdfFileOutputStream = null;
		try {
			document=new Document(PageSize.A4);
			pdfFileOutputStream = new ByteArrayOutputStream();
			PdfWriter.getInstance(document,pdfFileOutputStream);
			document.open();
			Font titleFont = new Font( Font.TIMES_ROMAN, 14 );
			PdfPTable  outerTable=new PdfPTable(new float [] { 1.0f, 3.0f });
			outerTable.setWidthPercentage(100);
			PdfPCell outerCell = new PdfPCell ();
			if (imagebytes!=null ) {
				Image image1 = Image.getInstance(imagebytes);
				outerCell.addElement(image1);
			}
			outerCell.setBorder(Rectangle.NO_BORDER);
			outerCell.setHorizontalAlignment (Element.ALIGN_LEFT);
			outerTable.addCell(outerCell);
			PdfPTable innerTable=new PdfPTable(1);
			innerTable.setWidthPercentage(100);
			Paragraph paragraph = new Paragraph (conferencename,titleFont);
			paragraph.getFont().setStyle(Font.UNDERLINE | Font.BOLD);
			outerCell = new PdfPCell (paragraph);
			outerCell.setHorizontalAlignment (Element.ALIGN_RIGHT);
			outerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			outerCell.setBorder(Rectangle.NO_BORDER);
			innerTable.addCell(outerCell);
			titleFont = new Font( Font.TIMES_ROMAN, 10 );
			outerCell = generatePDFCell("Schedule for "+username+" ",titleFont,Rectangle.NO_BORDER,Element.ALIGN_CENTER);
			outerCell.setHorizontalAlignment (Element.ALIGN_RIGHT);
			outerCell.setBorder(Rectangle.NO_BORDER);
			innerTable.addCell(outerCell);
			outerCell = new PdfPCell ();
			outerCell.setBorder(Rectangle.NO_BORDER);
			outerCell.addElement(innerTable);
			outerTable.addCell(outerCell);
			document.add(outerTable);
			titleFont = new Font( Font.TIMES_ROMAN, 10 );
			outerTable=new PdfPTable(new float [] { 1.0f, 3.0f });
			outerTable.setWidthPercentage(100);
			outerCell = generatePDFCell("Date",titleFont);
			outerCell.setPaddingBottom(5f);
			outerTable.addCell(outerCell);
			outerCell = generatePDFCell("Course Details",titleFont);
			outerCell.setPaddingBottom(5f);
			outerTable.addCell(outerCell);

			if (courseDateList!=null && courseDateList.size()>0) {
				for (CourseDateBean courseDateBean:courseDateList) {
					outerTable.addCell(generatePDFCell(getDateFromDate(courseDateBean.getCoursedate()),titleFont));
					innerTable=new PdfPTable(1);
					innerTable.setWidthPercentage(100);
					if (courseDateBean.getScheduleCourseDTOColl()!=null && courseDateBean.getScheduleCourseDTOColl().size()>0) {
						for (ScheduleCourseDTO scheduleCourseDTO:courseDateBean.getScheduleCourseDTOColl()) {
							PdfPCell innerCell = new PdfPCell (innerTablePDF(scheduleCourseDTO));
							innerCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
							innerTable.addCell(innerCell);
						}
						innerTable.setSplitLate(false);
						outerCell = new PdfPCell (innerTable);
						outerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						outerCell.setPaddingTop(5f);
						outerCell.setPaddingBottom(10f);
						outerCell.setPaddingRight(2f);
						outerCell.setPaddingLeft(2f);
						outerCell.setBorder(com.lowagie.text.Rectangle.BOX);
						outerCell.setVerticalAlignment(Element.ALIGN_TOP);
						outerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
						outerTable.addCell(outerCell);
					}
				}
			}
			outerTable.setSplitLate(false);
			document.add(outerTable);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (document!=null) {
				document.close();
			}
			if (pdfFileOutputStream!=null) {
				pdfFileOutputStream.close();
			}
		}
		return pdfFileOutputStream;
	}

	private PdfPCell generatePDFCell(String text,Font fontstyle,int border,int textalign) {
		PdfPCell outerCell = new PdfPCell (new Paragraph (text,fontstyle));
		outerCell.setHorizontalAlignment (textalign);
		outerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		outerCell.setBorder(border);
		outerCell.setPadding(5f);
		return outerCell;
	}

	private PdfPCell generatePDFCell(String text,Font fontstyle) {
		PdfPCell outerCell = new PdfPCell (new Paragraph (text,fontstyle));
		outerCell.setHorizontalAlignment (Element.ALIGN_CENTER);
		outerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		return outerCell;
	}

	private PdfPTable innerTablePDF(ScheduleCourseDTO scheduleCourseDTO) {
		PdfPTable innerTable=new PdfPTable(new float [] { 1.0f, 3.0f });
		innerTable.setWidthPercentage(100);
		innerTable.getDefaultCell().setPadding(0f);
		Font myFont = new Font( Font.TIMES_ROMAN, 8 );
		Font titleFont = new Font( Font.TIMES_ROMAN, 8 );
		PdfPCell innercell = new PdfPCell (
					new Paragraph ( getTimeFromDate(scheduleCourseDTO.getStart())+" to "+
							getTimeFromDate(scheduleCourseDTO.getEnd())
							,titleFont));
		if (scheduleCourseDTO.getSessionstate().equalsIgnoreCase("general")) {
			innercell.setBackgroundColor (new Color (245, 229, 8));
		}else if (scheduleCourseDTO.getSessionstate().equalsIgnoreCase("register")) {
			innercell.setBackgroundColor (new Color (120,165,194));
		}else{
			innercell.setBackgroundColor (new Color (199,199,199));
		}
		innercell.setHorizontalAlignment (Element.ALIGN_CENTER);
		innercell.setColspan (2);
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innercell.setPaddingBottom(5f);
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph("Course Name",titleFont));
		innercell.setHorizontalAlignment (Element.ALIGN_LEFT);
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph(scheduleCourseDTO.getCourseName(),myFont));
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph ("Presenter",titleFont));
		innercell.setHorizontalAlignment (Element.ALIGN_LEFT);
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph(scheduleCourseDTO.getPresenter(),myFont));
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph ("Room Number",titleFont));
		innercell.setHorizontalAlignment (Element.ALIGN_LEFT);
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph(scheduleCourseDTO.getRoomNo(),myFont));
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph ("Session type",titleFont));
		innercell.setHorizontalAlignment (Element.ALIGN_LEFT);
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innerTable.addCell(innercell);
		innercell = new PdfPCell (new Paragraph(scheduleCourseDTO.getSessionstate(),myFont));
		innercell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		innerTable.addCell(innercell);
		return innerTable;
	}

	public final String getDateFromDate(Date gc) {
		StringBuffer ret = new StringBuffer();
		ret.append(getDayString(gc.getDay()));
		ret.append(",");
		String[] strMonths = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		ret.append(strMonths[gc.getMonth()]);
		ret.append(" ");
		ret.append(gc.getDate());
		ret.append(",");
		ret.append(1900+gc.getYear());
		return ret.toString();
	}

	private  final String getDayString(int day) {
		String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		if ((day >= 0) && (day < 7)) {
			return daysOfWeek[day];
		} else {
			return "";
		}
	}

	public  final String getTimeFromDate(GregorianCalendar gc) {
		try {
			StringBuffer ret = new StringBuffer();
			int minutes =gc.get(Calendar.MINUTE);
			if (minutes ==59 || minutes ==44 || minutes ==29 || minutes ==14 ) {
				gc.add(Calendar.MINUTE, 1);
			}
			if (gc.get(Calendar.HOUR)!=0) {
				ret.append((gc.get(Calendar.HOUR)<10)?"0":"");
				ret.append(gc.get(Calendar.HOUR));
			}else {
				ret.append("12");
			}
			ret.append(":");
			ret.append((gc.get(Calendar.MINUTE)<10)?"0":"");
			ret.append(gc.get(Calendar.MINUTE));
			ret.append(" ");
			if (gc.get(Calendar.AM_PM) == 0) {
				ret.append("AM");
			} else {
				ret.append("PM");
			}
			return ret.toString();
		} catch(Exception e) {
			return "";
		}
	}

	@RequestMapping(value="papers.json")
	public @ResponseBody DBObject getMyPaperList(HttpServletRequest request) {
		BasicDBList list = new BasicDBList();
		Boolean allpapers=false;
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");

			String accountid = accountDetails.get().getLdsAccountId();
			BasicDBObject atFinder = new BasicDBObject();
			atFinder.put("accountid", accountid);

			DBCollection atCol = db.getCollection("attendees");
			DBCursor atCursor = atCol.find(atFinder);
			if (atCursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)atCursor.next();
				allpapers=(dbo.getBoolean("admin") || dbo.getBoolean("approver"));
			}

			DBCollection cCol = db.getCollection("courses");
			DBCursor cCursor = cCol.find(atFinder);
			if (allpapers)
				cCursor = cCol.find();

			while (cCursor.hasNext()) {
				BasicDBObject c = (BasicDBObject)cCursor.next();
				list.add(c);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return list;
	}

	@RequestMapping(value = "papers")
	public String papers(){
		return "papers";
	}

	/**
	 * download server data
	 * For testing purposes
	 * This method should be removed
	 */
	@RequestMapping(value="getserverdata")
	public void downloadJson(HttpServletRequest request,HttpServletResponse response) {
		BasicDBList list = new BasicDBList();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection attCol = db.getCollection("attendees");
			DBCursor attCursor = attCol.find();
			list.addAll(attCursor.toArray());

			DBCollection conferenceCol = db.getCollection("conference");
			DBCursor conferenceCursor = conferenceCol.find();
			list.addAll(conferenceCursor.toArray());

			DBCollection coursesCol = db.getCollection("courses");
			DBCursor coursecursor = coursesCol.find();
			list.addAll(coursecursor.toArray());

			response.setContentType("application/octet-stream");
			GregorianCalendar gc = new GregorianCalendar();
			response.setHeader("Content-Disposition","attachment; filename=attendees_"+(1900+gc.getTime().getYear())+(gc.getTime().getMonth()+1)+gc.getTime().getDate()+".json");
			ServletOutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(list.toString().getBytes("UTF-8"));
			byte[] outputByte = new byte[4096];
			//copy binary content to output stream
			int numBytes = 0;
			while ((numBytes = in.read(outputByte, 0, 4096)) != -1) {
				out.write(outputByte, 0, numBytes);
			}
			in.close();
			out.flush();
			out.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Download Course slide deck (leave .ext on name)
	@RequestMapping(value="paper/{filename:.*}")
	public @ResponseBody void paper(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable String filename) {
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			GridFS gfsFile = new GridFS(db, "courses");
			// FIXME: don't return paper to non-admin/approver/owner if it's not yet approved_cor
			BasicDBObject query = new BasicDBObject("filename",filename);
			GridFSDBFile out = gfsFile.findOne(query);
			response.reset();
			String contenttype=URLConnection.guessContentTypeFromName(filename);
			if (contenttype==null) {
				String ext = (String)out.getMetaData().get("fileext");
				if (ext.equalsIgnoreCase("pptx")) {
					contenttype="application/vnd.openxmlformats-officedocument.presentationml.presentation";
				} else {
					// FIXME: server is lame, force binary type
					contenttype="application/octet-stream";
				}
			}
			response.setContentType(contenttype);
			response.setContentLength((int)out.getLength());
			response.addHeader("Cache-control", "private");
			ServletOutputStream servletOutputStream = response.getOutputStream();
			byte[] outputByte = new byte[(int)out.getChunkSize()];
			int numBytes = 0;
			InputStream in = out.getInputStream();
			while ((numBytes = in.read(outputByte)) != -1) {
				servletOutputStream.write(outputByte);
			}
			in.close();
			servletOutputStream.flush();
			servletOutputStream.close();
		} catch(Exception e) {
			System.out.print(e);
		}
	}
}
