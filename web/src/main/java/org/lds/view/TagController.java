package org.lds.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lds.rs.dto.AutoCompleteDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

@Controller
@RequestMapping("/tag/")
public class TagController {

	@RequestMapping(value="list.json", headers="accept=application/json", method=RequestMethod.GET)
	public @ResponseBody Collection listTags(HttpServletRequest request, @RequestParam(value="term") String term){
		Map<String,AutoCompleteDTO> set = new HashMap<String,AutoCompleteDTO>();
		try {
			DB db = (DB)request.getSession().getServletContext().getAttribute("mongodb");
			DBCollection coll = db.getCollection("courses");
			Pattern wild = Pattern.compile(term, Pattern.CASE_INSENSITIVE);
			BasicDBObject query = new BasicDBObject("tags", wild);

			DBCursor cursor = coll.find(query);

			while (cursor.hasNext()) {
				BasicDBObject dbo = (BasicDBObject)cursor.next();
				BasicDBList list = (BasicDBList)dbo.get("tags");
				Iterator ite = list.iterator();

				while (ite.hasNext()) {
					String value = (String)ite.next();
					Matcher ma = wild.matcher(value.trim());
					if (ma.lookingAt()) {
						AutoCompleteDTO dto = new AutoCompleteDTO();
						dto.setId(value);
						dto.setLabel(value);
						dto.setValue(value);
						set.put(value,dto);
					}
				}
			}
		} catch(Exception e) {
			System.out.print(e);
		}
		return set.values();
	}
}
