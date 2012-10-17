package org.lds.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lds.rs.dto.CountryWiseAttendeeDTO;
import org.lds.rs.dto.CourseCountDTO;
import org.lds.rs.dto.OrganizationWiseAttendeeDTO;
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
@RequestMapping("/dashboard")
public class DashBoardController {

	@RequestMapping(value = "/view", method=RequestMethod.GET)
	public String dashboardView(){
		return "dashboard";
	}

	// * Get the course list from Course Collection
	@RequestMapping(value="/optionlist.json", headers="accept=application/json", method=RequestMethod.GET)
	public @ResponseBody BasicDBObject listforDashBoardSearchOptions(HttpServletRequest request){
		BasicDBObject basicDBObject = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			Set<String> organizationList = new TreeSet<String>();
			Set<String> countryList = new TreeSet<String>();
			TreeSet<CourseCountDTO> courseList = new TreeSet<CourseCountDTO>();

			/*
			 * Get the Course ID, Name and Post name of list
			 */
			DBCollection coll = db.getCollection("courses");
			DBCursor cursor = coll.find(new BasicDBObject("approve_tech",new Boolean(true)));
						CourseCountDTO courseCountDTO = null;
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				courseCountDTO = new CourseCountDTO();
				courseCountDTO.setCourseid(dbo.getString("id"));
				courseCountDTO.setName(dbo.getString("name"));
				courseCountDTO.setPostname(dbo.getString("postname"));
				courseList.add(courseCountDTO);
			}
			/*
			 * Get the Organization list and country list
			 */
			coll = db.getCollection("attendees");
			cursor = coll.find();
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				organizationList.add(dbo.getString("organization"));
				countryList.add(dbo.getString("country"));
			}

			basicDBObject.put("countryList", countryList.toArray());
			basicDBObject.put("organizationList", organizationList.toArray());
			basicDBObject.put("courseList", courseList.toArray());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return basicDBObject;
	}

	// Get the Attendee count for all the Country(s) and organization(s)
	@RequestMapping(value="/getAttendeesCount.json")
	public @ResponseBody ArrayList<CountryWiseAttendeeDTO> getCountryOrganizationDashboardReport(HttpServletRequest request) {
		ArrayList<CountryWiseAttendeeDTO> countryDTOColl = new ArrayList<CountryWiseAttendeeDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			Set<String> organizationList = new TreeSet<String>();
			Set<String> countryList = new TreeSet<String>();
			TreeSet<CourseCountDTO> courseList = new TreeSet<CourseCountDTO>();
			int totalnoofattendee = 0;

			// Get the Organization list and country list
			DBCollection atttendeeColl = db.getCollection("attendees");
			DBCursor cursor = atttendeeColl.find();
			totalnoofattendee = cursor.size();
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				String organization = dbo.getString("organization");
				if (organization != null) {
					organizationList.add(organization);
				}
				String country = dbo.getString("country");
				if (country != null) {
					countryList.add(country);
				}
			}

			// Get the Course ID, Name and Post name of list
			DBCollection coursesColl = db.getCollection("courses");
			cursor = coursesColl.find(new BasicDBObject("approve_tech",new Boolean(true)));
			CourseCountDTO courseCountDTO = null;
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				courseCountDTO = new CourseCountDTO();
				courseCountDTO.setCourseid(dbo.getString("id"));
				courseCountDTO.setName(dbo.getString("name"));
				courseCountDTO.setPostname(dbo.getString("postname"));
				if(Boolean.valueOf(dbo.getString("general"))) {
					courseCountDTO.setGeneral(true);
				}
				courseList.add(courseCountDTO);
			}

			BasicDBObject query = null;
			Collection<OrganizationWiseAttendeeDTO> organizationDTOList = null;
			OrganizationWiseAttendeeDTO organizationWiseAttendeeDTO = null;
			CountryWiseAttendeeDTO countryOrganizationDTO = null;
			BasicDBObject dbo = null;

			for (String country:countryList) {
				countryOrganizationDTO = new CountryWiseAttendeeDTO();
				organizationDTOList = new ArrayList<OrganizationWiseAttendeeDTO>();
				countryOrganizationDTO.setCountry(country);
				countryOrganizationDTO.setOrganizationList(organizationDTOList);
				countryDTOColl.add(countryOrganizationDTO);
				for (String organization:organizationList) {
					organizationWiseAttendeeDTO = new OrganizationWiseAttendeeDTO();
					organizationDTOList.add(organizationWiseAttendeeDTO);
					organizationWiseAttendeeDTO.setOrganization(organization);
					if (courseList!=null) {
						organizationWiseAttendeeDTO.setCourseList(courseList);
						// Getting Attendees Object based on country and organization
						DBCursor attCursor = atttendeeColl.find();
						while (attCursor.hasNext()) {
							BasicDBObject attBasicDBObject = (BasicDBObject)attCursor.next();
							if (attBasicDBObject.getString("country").equalsIgnoreCase(country)
									&& attBasicDBObject.getString("organization").equalsIgnoreCase(organization)) {
								organizationWiseAttendeeDTO.setTotalAttendees(organizationWiseAttendeeDTO.getTotalAttendees()+1);
								for (CourseCountDTO courseCountBean:organizationWiseAttendeeDTO.getCourseList()) {
									if (!courseCountBean.isGeneral()) {
										if (attBasicDBObject.get("registered") != null
												&& ((List<String>)attBasicDBObject.get("registered")).contains(courseCountBean.getCourseid())){
											 courseCountBean.setRegistiveCount(courseCountBean.getRegistiveCount()+1);
										} else if (attBasicDBObject.get("tentative") != null
												&& ((List<String>)attBasicDBObject.get("tentative")).contains(courseCountBean.getCourseid())){
											 courseCountBean.setTentativeCount(courseCountBean.getTentativeCount()+1);
										}
									} else {
										courseCountBean.setRegistiveCount(organizationWiseAttendeeDTO.getTotalAttendees());
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return countryDTOColl;
	}

	// Get the course count for country and organization
	@RequestMapping(value="/search.json")
	public @ResponseBody Collection<CourseCountDTO> getCourseForCountryOrganization(
			HttpServletRequest request,
			@RequestParam("organization") String organization,
			@RequestParam("country") String country,
			@RequestParam("courseid") String courseid) {
		return getCourseList(request, organization, country, courseid);
	}

	// Get the Course information for related country, organization and courseid
	private List<CourseCountDTO> getCourseList(
			HttpServletRequest request,
			String organization,
			String country,
			String courseid) {
		ArrayList<CourseCountDTO> courseList = new ArrayList<CourseCountDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor	cursor = coll.find();
			int totalAttendees = cursor.size();

			coll = db.getCollection("courses");

			cursor = null;
			CourseCountDTO courseCountDTO = null;
			BasicDBObject queryattendee =null;
			BasicDBObject inquery = null;

			if (courseid.equalsIgnoreCase("all") || courseid.equalsIgnoreCase("0")) {
				coll = db.getCollection("courses");
				cursor = coll.find();
			} else {
				BasicDBList courselist = new BasicDBList();
				courselist.add(Pattern.compile(courseid+"_.*", Pattern.CASE_INSENSITIVE));
				inquery = new BasicDBObject("$in",courselist);
				BasicDBList idlist = new BasicDBList();
				idlist.add(new BasicDBObject("registered",inquery));
				idlist.add(new BasicDBObject("tentative",inquery));
				queryattendee = new BasicDBObject("$or",idlist);
				coll = db.getCollection("courses");
				cursor = coll.find(new BasicDBObject("id",courseid));
			}
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				if (Boolean.valueOf(dbo.getString("approve_tech"))) {
					if (dbo.containsField("sessions")) {
						BasicDBList sessions=(BasicDBList)dbo.get("sessions");
						for (int i=0;i<sessions.size();i++) {
							courseCountDTO = new CourseCountDTO();
							BasicDBObject session = (BasicDBObject)sessions.get(i);
							courseCountDTO.setCourseid(dbo.getString("id")+'_'+i);
							courseCountDTO.setName(dbo.getString("name"));
							courseCountDTO.setPostname(session.getString("postname"));
							courseCountDTO.setGeneral(Boolean.valueOf(dbo.getString("general")));
							courseList.add(courseCountDTO);
						}
					} else {
						courseCountDTO = new CourseCountDTO();
						courseCountDTO.setCourseid(dbo.getString("id"));
						courseCountDTO.setName(dbo.getString("name"));
						courseCountDTO.setPostname(dbo.getString("postname"));
						courseCountDTO.setGeneral(Boolean.valueOf(dbo.getString("general")));
						courseList.add(courseCountDTO);
					}
				}
			}

			coll = db.getCollection("attendees");
			if (queryattendee!=null) {
				cursor = coll.find(queryattendee);
			} else {
				cursor = coll.find();
			}

			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				String countrystr = dbo.getString("country");
				String organizationstr = dbo.getString("organization");
				if ((country.equalsIgnoreCase("all") || countrystr.equalsIgnoreCase(country))
						&& (organization.equalsIgnoreCase("all") || organizationstr.equalsIgnoreCase(organization))) {
					for (CourseCountDTO courseCountBean:courseList) {
						if (!courseCountBean.isGeneral()) {
							if (dbo.get("registered") != null && ((List<String>)dbo.get("registered")).contains(courseCountBean.getCourseid())) {
								courseCountBean.setRegistiveCount(courseCountBean.getRegistiveCount()+1);
							} else if (dbo.get("tentative") != null && ((List<String>)dbo.get("tentative")).contains(courseCountBean.getCourseid())){
								courseCountBean.setTentativeCount(courseCountBean.getTentativeCount()+1);
							}
						} else { //general count
								courseCountBean.setRegistiveCount(courseCountBean.getRegistiveCount()+1);
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return courseList;
	}

	// Download CSV File
	@RequestMapping(value="/downloadcourseregistrant.json", method=RequestMethod.GET)
	public void downloadcourseregistrant(HttpServletRequest request,HttpServletResponse response){
		try {
			String organization = String.valueOf(request.getParameter("organization")).trim();
			String country = String.valueOf(request.getParameter("country")).trim();
			String courseid = String.valueOf(request.getParameter("course")).trim();

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Course Name");
			stringBuilder.append(",Register");
			stringBuilder.append(",Tentative");
			stringBuilder.append(",Total Attendees");
			stringBuilder.append("\n");

			for (CourseCountDTO courseCountBean:getCourseList(request, organization, country, courseid)) {
				stringBuilder.append("\""+courseCountBean.getName()+courseCountBean.getPostname()+"\"");
				stringBuilder.append(",\""+courseCountBean.getRegistiveCount()+"\"");
				stringBuilder.append(",\""+courseCountBean.getTentativeCount()+"\"");
				stringBuilder.append(",\""+(courseCountBean.getRegistiveCount()+courseCountBean.getTentativeCount())+"\"");
				stringBuilder.append("\n");
			}
			response.reset();
			response.setContentType("application/octet-stream");
			GregorianCalendar gc = new GregorianCalendar();
			response.setHeader("Content-Disposition","attachment;filename=attendees_"+(1900+gc.getTime().getYear())+(gc.getTime().getMonth()+1)+gc.getTime().getDate()+".csv");
			response.setHeader("Cache-control", "private");
			response.addHeader("Pragma", "no-cache");
			response.setContentLength(stringBuilder.length());
			ServletOutputStream out = response.getOutputStream();
			InputStream in = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
			byte[] outputByte = new byte[4096];
			//copy binary contect to output stream
			int numBytes = 0;
			while ((numBytes = in.read(outputByte, 0, 4096)) != -1) {
				out.write(outputByte, 0, numBytes);
			}
			in.close();
			out.flush();
			out.close();
		} catch(Exception e) {
			System.out.print(e);
		}
	}
}
