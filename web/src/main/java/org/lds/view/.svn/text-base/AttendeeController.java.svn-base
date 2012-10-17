package org.lds.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lds.rs.dto.AutoCompleteDTO;
import org.lds.rs.dto.ProfileDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

@Controller
@RequestMapping("/attendee/")
public class AttendeeController {

	// List of Attendees and ID's for auto completion
	@RequestMapping(value="autolist.json", headers="accept=application/json", method=RequestMethod.GET)
	public @ResponseBody List<AutoCompleteDTO> listAttendees(HttpServletRequest request, @RequestParam(value="term") String term){
		List<AutoCompleteDTO> results = new ArrayList<AutoCompleteDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			Pattern wild = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
			BasicDBObject query = new BasicDBObject("name", wild);

			DBCursor cursor = coll.find(query);
			while(cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				AutoCompleteDTO dto = new AutoCompleteDTO();
				dto.setId(dbo.getString("id"));
				dto.setLabel(dbo.getString("name"));
				dto.setValue(dbo.getString("name"));
				results.add(dto);
			}
		}catch(Exception e) {
			System.out.print(e);
		}
		return results;
	}

	@RequestMapping(value="attendees", method = RequestMethod.GET)
	public String showListOfAttendees() {
		 return "attendees";
	}

    // Retrieving list of all register users Profile
	@RequestMapping(value="attendees.json")
	public @ResponseBody List<ProfileDTO> listOfAttendees(HttpServletRequest request){
		List<ProfileDTO> results = new ArrayList<ProfileDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor cursor = coll.find();
			while(cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				ProfileDTO pd = new ProfileDTO();
				pd.setName(String.valueOf(dbo.get("name")));
				pd.setUsername(String.valueOf(dbo.get("username")));
				pd.setAccountID(String.valueOf(dbo.get("accountid")));
				pd.setEmail(String.valueOf(dbo.get("email")));
				pd.setPhone(String.valueOf(dbo.get("phone")));
				pd.setCity(String.valueOf(dbo.get("city")));
				pd.setProvince(String.valueOf(dbo.get("province")));
				pd.setCountry(String.valueOf(dbo.get("country")));
				pd.setGender(String.valueOf(dbo.get("gender")));
				pd.setShirtsize(String.valueOf(dbo.get("shirtsize")));
				String role="Attendee";
				if (dbo.getBoolean("admin")) {
					role="Admin";
				} else if (dbo.getBoolean("approver")) {
					role="Approver";
				} else if (dbo.getBoolean("presenter")) {
					role="Presenter";
				} else if (dbo.getBoolean("waiting")) {
					role="Waiting";
				}
				pd.setRole(role);
				results.add(pd);
			}
		}catch(Exception e) {
			System.out.print(e);
		}
		return results;
	}

	// Downloading CSV File
	@RequestMapping(value="attendees.csv")
	public  void downloadlistOfAttendees(HttpServletRequest request, HttpServletResponse response){
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor cursor = coll.find();
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Name");
			stringBuilder.append(",Username");
			stringBuilder.append(",AccountID");
			stringBuilder.append(",Email");
			stringBuilder.append(",Phone");
			stringBuilder.append(",Address");
			stringBuilder.append(",City");
			stringBuilder.append(",State/Province");
			stringBuilder.append(",Country");
			stringBuilder.append(",Postal");
			stringBuilder.append(",Gender");
			stringBuilder.append(",Shirt size");
			stringBuilder.append(",Organization");
			stringBuilder.append(",Days");
			stringBuilder.append(",Role");
			stringBuilder.append(",Dietneeds");
			stringBuilder.append(",DOB");
			stringBuilder.append(",Qualification");
			stringBuilder.append(",Biography");
			stringBuilder.append("\n");
			while(cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				String gender = String.valueOf(dbo.get("gender"));
				if (gender.equalsIgnoreCase("m")) {
					gender = "Male";
				} else {
					gender = "Female";
				}
				BasicDBList days = (BasicDBList)dbo.get("days");
				String day ="";
				for (Object str:days) {
					day += str+";";
				}
				stringBuilder.append("\""+String.valueOf(dbo.get("name"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("username"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("accountid"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("email"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("phone"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("address"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("city"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("province"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("country"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("postal"))+"\"");
				stringBuilder.append(",\""+gender+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("shirtsize"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("organization"))+"\"");
				stringBuilder.append(",\""+day+"\"");
				String role="Attendee";
				if (dbo.getBoolean("admin")) {
					role="Admin";
				} else if (dbo.getBoolean("approver")) {
					role="Approver";
				} else if (dbo.getBoolean("presenter")) {
					role="Presenter";
				} else if (dbo.getBoolean("waiting")) {
					role="Waiting";
				}
				stringBuilder.append(",\""+role+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("dietneeds")==null?"":dbo.get("dietneeds"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("dob")==null?"":dbo.get("dob"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("qualification")==null?"":dbo.get("qualification"))+"\"");
				stringBuilder.append(",\""+String.valueOf(dbo.get("biography")==null?"":String.valueOf(dbo.get("biography"))).replaceAll("\"","")+"\"");
					
				stringBuilder.append("\n");
			}
			response.setContentType("application/octet-stream");
			GregorianCalendar gc = new GregorianCalendar();
			response.setHeader("Content-Disposition","attachment;filename=attendees_"+(1900+gc.getTime().getYear())+(gc.getTime().getMonth()+1)+gc.getTime().getDate()+".csv");
			ServletOutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
			byte[] outputByte = new byte[4096];
			//copy binary contect to output stream
			int numBytes = 0;
			while((numBytes = in.read(outputByte, 0, 4096)) != -1) {
				out.write(outputByte, 0, numBytes);
			}
			in.close();
			out.flush();
			out.close();
		} catch(Exception e) {
			System.out.print(e);
		}
	}

	// Get the count of attendees
	@RequestMapping(value="getattendeesCount", method=RequestMethod.GET)
	public @ResponseBody BasicDBObject getattendeesCount(HttpServletRequest request){
		BasicDBObject basicDBObject = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor cursor = coll.find();
			basicDBObject.put("attendeesCount",cursor.size());
		} catch(Exception e) {
			System.out.print(e);
		}
		return basicDBObject;
	}

	// Remove an attendee from the database
	@RequestMapping(value="unregisterUser", method=RequestMethod.GET)
	public String unregisterUser(HttpServletRequest request, @RequestParam("accountid") String pAccountID)
	{
		DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
		BasicDBObject query = new BasicDBObject();
		query.put("accountid", pAccountID);
		db.getCollection("attendees").remove(query);
		return "attendees";
	}
}
