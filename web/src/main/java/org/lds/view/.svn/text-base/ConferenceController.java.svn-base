package org.lds.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.lowagie.text.pdf.codec.Base64;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

@Controller
@RequestMapping("/conference/")
/*
 * Scheduling conference Dates and Session Times
 */
public class ConferenceController {

	@Resource(name="maxAttendence")
    public String max;
	/*
	 * Retrieving Conference Details
	 */
	@RequestMapping(value="info.json", method = RequestMethod.GET)
	public @ResponseBody DBObject getConference(HttpServletRequest req) {
		DBObject dbObject = null;
		try {
			DB db = (DB)req.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("conference");
			DBCursor cursor =  coll.find();
			if (cursor.hasNext()) {
				dbObject = (DBObject)cursor.next();
				GridFS gfsPhoto = new GridFS(db, "conference");
				BasicDBObject query = new BasicDBObject("filename","logo");
				GridFSDBFile out = gfsPhoto.findOne(query);
				if (out != null) {
					dbObject.put("fileext", out.getMetaData().get("logoext"));
					byte[] bytes = new byte[(int)out.getChunkSize()];
					out.getInputStream().read(bytes);
					dbObject.put("image", bytes);
				}
				return dbObject;
			} else {
				dbObject = new BasicDBObject("maxattendees",Long.parseLong(max));
				return dbObject;
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return dbObject;
	}

	@RequestMapping(value="loadlogo.json")
	public void loadlogo(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("imagefile") MultipartFile file) {
		BasicDBObject basicDBObject = new BasicDBObject();
		try {
			String src = "";
			String ext = "";
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			if (!file.isEmpty()) {
				GridFS gfsPhoto = new GridFS(db, "conference");
				BasicDBObject query = null;
				if (!file.isEmpty()) {
					String filename = file.getOriginalFilename();
					String[] pieces = filename.split("\\.");
					ext = pieces[(pieces.length - 1)];
					String newFileName = "templogo";
					query = new BasicDBObject("filename",newFileName);
					gfsPhoto.remove(query);
					GridFSInputFile gfsFile = gfsPhoto.createFile(file.getBytes());
					gfsFile.setFilename(newFileName);
					gfsFile.setMetaData(new BasicDBObject("tempext",ext));
					gfsFile.save();
					src = "data:image/"+ext+";base64,"+Base64.encodeBytes(file.getBytes());
				}
			}
			String imgtext = "<img id='logo' name='logo' alt='na' src='"+src+"' width='100' height='100'></img>"+
					 "<input type='hidden' id='tempfileext' value ='"+ext+"'></input>"+
					 "<input type='hidden' id='tempfilesize' value ='"+file.getSize()+"'></input>";
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/html");
			response.getOutputStream().println(imgtext);
		} catch(Exception e) {
			System.out.print(e);
		}
	}

	/*
	 * Modifying Conference Details
	 */
	@RequestMapping(value="modify.json")
	public String modifyConference(
			MultipartHttpServletRequest request,
			@RequestParam(value="name") String name,
			@RequestParam(value="datefrom") String datefrom,
			@RequestParam(value="dateto") String dateto,
			@RequestParam(value="datelast") String datelast,
			@RequestParam(value="maxattendees") Long maxattendees,
			@RequestParam(value="timezone") String timezone,
			@RequestParam(value="tracks") String tracks,
			@RequestParam(required=false, value="imagefile") MultipartFile imagefile) {
		DBObject dbObject = new BasicDBObject();
		try {
			Map<String,String[]> data = request.getParameterMap();
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("conference");
			DBCursor cursor = coll.find();
			if (cursor.hasNext()) {
				dbObject =cursor.next();
			}
			dbObject.put("name",name);
			dbObject.put("datefrom",datefrom);
			dbObject.put("dateto",dateto);
			dbObject.put("datelast",datelast);
			dbObject.put("maxattendees",maxattendees);

			String[] timezoneList = timezone.split(";");
			dbObject.put("timezonename",timezoneList[0]);
			double tzNum = Double.valueOf(timezoneList[1]);
			dbObject.put("timezone", tzNum);
			
			//TODO: validation code to make sure tracks can be parsed.
			dbObject.put("tracks", tracks);
			
			
			int timezoneoffset = (int)(tzNum * 3600000);
			String tz=timezoneList[0];

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d h:mm aa");
			formatter.setTimeZone(TimeZone.getTimeZone(tz));
			ArrayList<BasicDBObject> timeList = new ArrayList<BasicDBObject>();
			for (Map.Entry<String,String[]> entry:data.entrySet()) {
			    if (entry.getKey().matches("day_.*")) {
					BasicDBObject dbo = new BasicDBObject();
					String day=entry.getValue()[0];
					if (data.get("start_"+day)!=null) {
						Date start=formatter.parse(day+" "+data.get("start_"+day)[0]);
						dbo.put("start",start.getTime());
					}
					if (data.get("end_"+day)!=null) {
						Date end=formatter.parse(day+" "+data.get("end_"+day)[0]);
						dbo.put("end",end.getTime());
					}
					timeList.add(dbo);
				}
			}
		    Collections.sort(timeList, new Comparator<BasicDBObject>() {
		        public int compare(BasicDBObject a, BasicDBObject b) {
		            return (int)((Long)a.get("start") - (Long)b.get("start"));
		        }
		    });

			dbObject.put("times",timeList);

			GridFS gfsPhoto = new GridFS(db, "conference");
			if (Boolean.valueOf(data.get("imageRemoveStatus")[0])) {
				gfsPhoto.remove(new BasicDBObject("filename","logo"));
			} else if (imagefile != null && !imagefile.getOriginalFilename().equalsIgnoreCase("")){
				gfsPhoto.remove(new BasicDBObject("filename","logo"));
				GridFSInputFile gfsFile = gfsPhoto.createFile(imagefile.getInputStream());
				gfsFile.setFilename("logo");
				gfsFile.setMetaData(new BasicDBObject("filename",imagefile.getOriginalFilename()));
				String[] pieces = imagefile.getOriginalFilename().split("\\.");
				gfsFile.setMetaData(new BasicDBObject("logoext",pieces[(pieces.length - 1)]));
				gfsFile.save();
			}
			request.getSession().getServletContext().setAttribute("conferencename", dbObject.get("name"));

			coll.save(dbObject);
		} catch(Exception e) {
			System.out.print(e);
		}
		return "conference";
	}
	
	
}
