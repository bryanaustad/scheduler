package org.lds.view;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;
import org.lds.stack.ldsaccount.LdsAccountDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

@Controller
public class PaperController {

	@Inject
	private Provider<LdsAccountDetails> accountDetails;

	@RequestMapping(value = "/paper", method=RequestMethod.GET)
	public String submitpaper(HttpServletRequest request) {
		return "paper";
	}

	@RequestMapping(value="/paper/edit")
	public String editPaper(
			HttpServletRequest request,
			@RequestParam(required=false, value="id") String courseId,
			@RequestParam("name") String name,
			@RequestParam("desc") String desc,
			@RequestParam(required=false, value="url") String url,
			@RequestParam("track") String track,
			@RequestParam("track_len") String track_len,
			@RequestParam("audiencelist") String audiencelist,
			@RequestParam("authorlist") String authorlist,
			@RequestParam("taglist") String taglist,
			@RequestParam(required=false, value="datafile") MultipartFile datafile) {
		BasicDBObject dbo = null;
		String target = "confirmed";
		String email="";
		Boolean admin = false;
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("courses");
			String accountid = accountDetails.get().getAccountId();

			// Check/Make them a presenter
			DBCollection regcoll = db.getCollection("attendees");
			dbo = new BasicDBObject("accountid", accountid);
			DBCursor cursor = regcoll.find(dbo);
			// FIXME: what do we do if we don't find the presenter?!?
			if (cursor.hasNext()) {
				dbo = (BasicDBObject)cursor.next();
				email=dbo.getString("email");
				admin=dbo.getBoolean("admin");
				if (!dbo.getBoolean("presenter")) {
					dbo.put("presenter",true);
					regcoll.save(dbo);
					// clear attribute so it'll get reset
					request.getSession().removeAttribute("userurlrestriction");
					// FIXME: we should ask/require them to fill in presenter fields!
					target = "profile";
				}
			}
			if (courseId == null || courseId.equals("")) {
				courseId = new ObjectId().toString();
				dbo = new BasicDBObject("id", courseId);
				dbo.put("accountid", accountid);
				dbo.put("email", email.trim());
				dbo.put("approve_tech", new Boolean(false));
				dbo.put("approve_ip", new Boolean(false));
				dbo.put("approve_cor", new Boolean(false));
				dbo.put("general", new Boolean(false));
				dbo.put("track_len",track_len);
			} else {
				BasicDBObject query = new BasicDBObject("id", courseId);
				cursor = coll.find(query);
				if (cursor.hasNext()) {
					dbo = (BasicDBObject)cursor.next();
				}
				if (!admin && !(dbo.getString("accountid").equals(accountid))) {
					// non admins can't edit other peoples papers!
					// FIXME: give the user a useful error message
					return target;
				}
			}
			dbo.put("name", name.trim());
			dbo.put("desc", desc.trim());
			if (url!=null)
				dbo.put("url", url.trim());
			dbo.put("track",track.trim());

			String[] authors = authorlist.split(";");
			BasicDBList authorList = new BasicDBList();
			for (String author:authors) {
				authorList.add(author.trim());
			}
			dbo.put("authors", authorList);

			String[] tagArray = taglist.split(";");
			BasicDBList tagList = new BasicDBList();
			for (String tag:tagArray) {
				tagList.add(tag.trim());
			}
			dbo.put("tags", tagList);

			String[] audienceArray = audiencelist.split(";");
			BasicDBList audiencesList = new BasicDBList();
			for (String audiences:audienceArray) {
				audiencesList.add(audiences.trim());
			}
			dbo.append("audience", audiencesList);

			if (!datafile.isEmpty()) {
				GridFS gfsFile = new GridFS(db, "courses");
				String oldfilename = dbo.getString("filename");
				if (oldfilename != null)
					gfsFile.remove(oldfilename);
				String filedisplayname = datafile.getOriginalFilename();
				String[] pieces = filedisplayname.split("\\.");
				String ext = pieces[(pieces.length - 1)];
				String filename = courseId.toString() + "." + ext;
				if (filedisplayname.length() > 0)
					dbo.append("filedisplayname", filedisplayname);
				dbo.append("filename", filename);
				GridFSInputFile newgfsFile = gfsFile.createFile(datafile.getBytes());
				newgfsFile.setFilename(filename);
				newgfsFile.setMetaData(new BasicDBObject("fileext",ext));
				newgfsFile.save();
			}
			coll.save(dbo);	
		} catch(IOException e) {
			System.out.print(e);
		}
		return target;
	}
}
