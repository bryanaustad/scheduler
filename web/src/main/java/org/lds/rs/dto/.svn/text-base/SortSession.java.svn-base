package org.lds.rs.dto;

import java.util.Date;

import org.lds.rs.util.SortResponse;

public class SortSession {
	public long id;
	public String desc;
	public String name;
	public long start;
	public Date startDate;
	public long end;
	public Date endDate;
	public String[] authors;
	public String[] tags;
	public boolean registered;
	public boolean tentative;
	public int attendeecount;
	public String room;
	public boolean general;
	public Boolean waitingList;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isGeneral() {
		return general;
	}

	public void setGeneral(boolean general) {
		this.general = general;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public SortSession(){}

	public SortSession(long id, String name, String desc, Object start, Object end) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.startDate = (Date)start;
		this.start = this.startDate.getTime();
		this.endDate = (Date)end;
		this.end = this.endDate.getTime();
	}

	public SortSession(long id, String name, String desc, boolean general, Object start, Object end, boolean waitingList) {
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.general = general;
		this.startDate = (Date)start;
		this.start = this.startDate.getTime();
		this.endDate = (Date)end;
		this.end = this.endDate.getTime();
		this.waitingList = waitingList;
	}

	public SortSession(long id, String name, String desc, boolean general, Object start, Object end) {
		this(id, name, desc, general, start, end, true);
	}

	public SortSession(long id, String name, String desc, long start, long end, String[] authors, String[] tags, boolean registered, boolean tentative, int attendeecount, SortResponse sr){
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.start = start;
		this.startDate = new Date(start);
		this.end = end;
		this.endDate = new Date(end);
		this.authors = authors;
		this.tags = tags;
		this.registered = registered;
		this.tentative = tentative;
		this.attendeecount = attendeecount;

	}

	public int getAttendeecount() {
		return attendeecount;
	}

	public void setAttendeecount(int attendeecount) {
		this.attendeecount = attendeecount;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setAuthors(String[] authors) {
		this.authors = authors;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public void setTentative(boolean tentative) {
		this.tentative = tentative;
	}

	public long getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public String[] getAuthors() {
		return authors;
	}

	public String[] getTags() {
		return tags;
	}

	public boolean isRegistered() {
		return registered;
	}

	public boolean isTentative() {
		return tentative;
	}

	public Boolean isWaitingList() {
		return waitingList;
	}

	public void setWaitingList(Boolean waitingList) {
		this.waitingList = waitingList;
	}
}
