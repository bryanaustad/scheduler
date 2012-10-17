package org.lds.listener;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

public class LdsServletContextListener implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();
		for(Map.Entry<String, String> entry :System.getenv().entrySet()) {
			System.out.println(entry.getKey()+":"+entry.getValue());
		}
		Mongo mongo = null;
		try {
			//mongo = new Mongo("localhost", 27017);
			MongoURI uri = new MongoURI("mongodb://edae78db-c94b-4c41-879e-56117c6ed59a:7ba5574a-6991-4486-92ee-161247d963ee@172.30.48.68:25157/db");
			mongo = new Mongo(uri);
			if (mongo != null) {
				sc.setAttribute("mongo", mongo);
			}
			DB db = mongo.getDB("db");
			db.authenticate(uri.getUsername(), uri.getPassword());
			if (db != null) {
				System.out.println("Grabbed Mongo Database!!!");
				sc.setAttribute("mongodb", db);
			}
			DBCollection coll = db.getCollection("conference");
			DBCursor cursor =  coll.find();
			if (cursor.hasNext()) {
				// TODO: find next occurring conference to handle multiples
				DBObject dbObject = (DBObject)cursor.next();
				sc.setAttribute("conferencename", dbObject.get("name"));
			}
		} catch(Exception e) {
			System.out.println("Errored on grabbing Mongo Database!!!");
			e.printStackTrace();
			System.out.println(e);
		}
	}
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();
		try {
			Mongo mongo=(Mongo)sc.getAttribute("mongo");
			if (mongo!=null)
				mongo.close();
			sc.removeAttribute("mongodb");
			sc.removeAttribute("mongo");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
