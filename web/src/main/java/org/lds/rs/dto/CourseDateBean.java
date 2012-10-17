package org.lds.rs.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseDateBean implements Comparable<CourseDateBean> {

	private Date coursedate = new Date();
	private List<ScheduleCourseDTO> scheduleCourseDTOColl = new ArrayList<ScheduleCourseDTO>();

	public Date getCoursedate() {
		return coursedate;
	}
	public void setCoursedate(Date coursedate) {
		this.coursedate.setTime(coursedate.getTime());
	}
	public List<ScheduleCourseDTO> getScheduleCourseDTOColl() {
		return scheduleCourseDTOColl;
	}
	public void setScheduleCourseDTOColl(List<ScheduleCourseDTO> scheduleCourseDTOColl) {
		this.scheduleCourseDTOColl = scheduleCourseDTOColl;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coursedate == null) ? 0 : coursedate.hashCode());
		result = prime * result + ((scheduleCourseDTOColl == null) ? 0 : scheduleCourseDTOColl.hashCode());
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
		CourseDateBean other = (CourseDateBean) obj;
		if (coursedate == null) {
			if (other.coursedate != null)
				return false;
		}
		else if (!coursedate.equals(other.coursedate))
			return false;
		if (scheduleCourseDTOColl == null) {
			if (other.scheduleCourseDTOColl != null)
				return false;
		}
		else if (!scheduleCourseDTOColl.equals(other.scheduleCourseDTOColl))
			return false;
		return true;
	}
	@Override
	public int compareTo(CourseDateBean o) {
		return this.coursedate.compareTo(o.getCoursedate());
	}
}