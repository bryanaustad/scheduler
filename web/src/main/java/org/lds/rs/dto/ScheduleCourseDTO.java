package org.lds.rs.dto;

import java.util.GregorianCalendar;

public class ScheduleCourseDTO  implements Comparable<ScheduleCourseDTO>
{
	private String courseid;
	private String courseName;
	private String presenter;
	private String roomNo;
	private String sessionstate;
	private GregorianCalendar start = new GregorianCalendar();
	private GregorianCalendar end = new GregorianCalendar();

	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getPresenter() {
		if(presenter!=null) {
			return presenter.replaceAll("\\[","").replaceAll("\\]","").replace("\"", "").trim();
		}
		return presenter;
	}
	public void setPresenter(String presenter) {
		this.presenter = presenter;
	}
	public String getRoomNo() {
		return roomNo;
	}
	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
	}

	public GregorianCalendar getStart() {
		return start;
	}
	public void setStart(GregorianCalendar start) {
		this.start = start;
	}
	public GregorianCalendar getEnd() {
		return end;
	}
	public void setEnd(GregorianCalendar end) {
		this.end = end;
	}
	public String getSessionstate() {
		return sessionstate;
	}
	public void setSessionstate(String sessionstate) {
		this.sessionstate = sessionstate;
	}


	public String getCourseid() {
		return courseid;
	}
	public void setCourseid(String courseid) {
		this.courseid = courseid;
	}
	@Override
	public int compareTo(ScheduleCourseDTO o) {
		int cmp =  this.start.compareTo(o.getStart());
		return (cmp!=0)?cmp: this.end.compareTo(o.getEnd());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((courseName == null) ? 0 : courseName.hashCode());
		result = prime * result + ((courseid == null) ? 0 : courseid.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((presenter == null) ? 0 : presenter.hashCode());
		result = prime * result + ((roomNo == null) ? 0 : roomNo.hashCode());
		result = prime * result + ((sessionstate == null) ? 0 : sessionstate.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScheduleCourseDTO other = (ScheduleCourseDTO) obj;
		if (courseName == null) {
			if (other.courseName != null)
				return false;
		}
		else if (!courseName.equals(other.courseName))
			return false;
		if (courseid == null) {
			if (other.courseid != null)
				return false;
		}
		else if (!courseid.equals(other.courseid))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		}
		else if (!end.equals(other.end))
			return false;
		if (presenter == null) {
			if (other.presenter != null)
				return false;
		}
		else if (!presenter.equals(other.presenter))
			return false;
		if (roomNo == null) {
			if (other.roomNo != null)
				return false;
		}
		else if (!roomNo.equals(other.roomNo))
			return false;
		if (sessionstate == null) {
			if (other.sessionstate != null)
				return false;
		}
		else if (!sessionstate.equals(other.sessionstate))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		}
		else if (!start.equals(other.start))
			return false;
		return true;
	}


}
