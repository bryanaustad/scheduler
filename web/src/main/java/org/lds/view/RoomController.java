package org.lds.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.lds.rs.dto.AutoCompleteDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

@Controller
@RequestMapping("/room/")
public class RoomController {
	@RequestMapping(value="list.json")
	public @ResponseBody Collection<AutoCompleteDTO> listRooms(
			HttpServletRequest request,
			@RequestParam(required=false, value="term") String term){
		Map<String,AutoCompleteDTO> set = new HashMap<String,AutoCompleteDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("courses");
			DBCursor cursor;
			if (term!=null) {
				Pattern wild = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
				BasicDBObject query = new BasicDBObject("sessions.room", wild);
				cursor = coll.find(query);
			} else {
				cursor = coll.find();
			}
			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				if (dbo.containsField("sessions")) {
					BasicDBList sessions=(BasicDBList)dbo.get("sessions");
					for (int i=0;i<sessions.size();i++) {
						BasicDBObject session = (BasicDBObject)sessions.get(i);
						AutoCompleteDTO dto = new AutoCompleteDTO();
						dto.setId(session.getString("room"));
						dto.setLabel(session.getString("room"));
						dto.setValue(session.getString("room"));
						set.put(session.getString("room"),dto);
					}
				}
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return set.values();
	}
}
