package org.lds.view;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lds.rs.dto.AutoCompleteDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

@Controller
@RequestMapping("/author/")
public class AuthorController {
	@RequestMapping(value="list.json")
	public @ResponseBody Collection<AutoCompleteDTO> list(
			HttpServletRequest request,
			@RequestParam(value="term") String term){
		Map<String,AutoCompleteDTO> authorset = new HashMap<String,AutoCompleteDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			//BasicDBList orList = new BasicDBList();
			Pattern wild = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
			BasicDBObject query = new BasicDBObject("name", wild);
			query.put("presenter", true);
			DBCursor cursor = coll.find(query);
			//DBCursor cursor = coll.find(orList);

			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				String value = dbo.getString("name");
				AutoCompleteDTO dto = new AutoCompleteDTO();
				dto.setId(value);
				dto.setLabel(value);
				dto.setValue(value);
				authorset.put(value,dto);
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return authorset.values();
	}

	@RequestMapping(value="details.json")
	public @ResponseBody BasicDBObject details(
			HttpServletRequest request,
			@RequestParam(required=false, value="name") String name,
			@RequestParam(required=false, value="accountid") String accountid) {
		BasicDBObject basicDBObject = new BasicDBObject();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("attendees");
			DBCursor cursor;
			BasicDBObject query;
			if (name != null) {
				query = new BasicDBObject("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE));
				cursor = coll.find(query);
				if (cursor.size()==0) {
					cursor = coll.find(new BasicDBObject("name", name));
				}
			} else if (accountid != null) {
				cursor = coll.find(new BasicDBObject("accountid", accountid));
			} else {
				return basicDBObject;
			}
			if (cursor.hasNext()) {
				GridFS gfsPhoto = new GridFS(db, "attendees");
				basicDBObject = (BasicDBObject)cursor.next();
				query = new BasicDBObject("filename", basicDBObject.get("id"));
				GridFSDBFile out = gfsPhoto.findOne(query);
				// FIXME: provide author/image/{accountid}.{ext} URL and NOT include the image in the json
				//Save loaded image from database into new image file
				if (out!=null) {
					byte[] bytes = new byte[(int)out.getChunkSize()];
					out.getInputStream().read(bytes);
					basicDBObject.put("photo", bytes);
				}
			}
			String role="Attendee";
			if (basicDBObject.getBoolean("admin")) {
				role="Admin";
			} else if (basicDBObject.getBoolean("approver")) {
				role="Approver";
			} else if (basicDBObject.getBoolean("presenter")) {
				role="Presenter";
			} else if (basicDBObject.getBoolean("waiting")) {
				role="Waiting";
			}
			basicDBObject.put("role",role);
		} catch(Exception e) {
			System.out.print(e);
		}
		return basicDBObject;
	}

	// Author picture
	@RequestMapping(value="images/{filename}")
	public @ResponseBody void image(
			HttpServletRequest request,
			HttpServletResponse response,
			@PathVariable String filename) {
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			GridFS gfsFile = new GridFS(db, "attendees");
			// ignore the extension and give what we have
			String[] pieces = filename.split("\\.");
			BasicDBObject query = new BasicDBObject("filename", pieces[0]);
			GridFSDBFile out = gfsFile.findOne(query);
			response.reset();
			response.setContentType(out.getContentType());
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
