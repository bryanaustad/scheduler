

package org.lds.rs.dto;


public class CourseCountDTO implements Comparable<CourseCountDTO>{

    private String courseid;
    private String postname="";
    private String name;
    private boolean general;
    private int tentativeCount;
    private int registiveCount;

    public CourseCountDTO() {
        tentativeCount =0;
        registiveCount =0;
        general = false;
    }

    public CourseCountDTO(String courseid, String name,String postname,boolean general,int registiveCount) {
        this.courseid = courseid;
        this.postname = postname==null?"":postname;
        this.name = name;
        this.tentativeCount =0;
        this.registiveCount =(general)?registiveCount:0;
        this.general = general;
    }


    public String getCourseid() {
        return courseid;
    }

    public void setCourseid(String courseid) {
        this.courseid = courseid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostname() {
        if (postname==null) return "";
        return postname;
    }

    public void setPostname(String postname) {
        this.postname = postname==null?"":postname;
    }

    public int getRegistiveCount() {
        return registiveCount;
    }

    public void setRegistiveCount(int registiveCount) {
        this.registiveCount = registiveCount;
    }

    public int getTentativeCount() {
        return tentativeCount;
    }

    public void setTentativeCount(int tentativeCount) {
        this.tentativeCount = tentativeCount;
    }

    public int compareTo(CourseCountDTO obj) {
        int namecmp = this.getName().compareTo(obj.getName());
        return (namecmp!=0)?namecmp: this.getPostname().compareTo(obj.getPostname());
    }

	public boolean isGeneral() {
		return general;
	}

	public void setGeneral(boolean general) {
		this.general = general;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((courseid == null) ? 0 : courseid.hashCode());
		result = prime * result + (general ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((postname == null) ? 0 : postname.hashCode());
		result = prime * result + registiveCount;
		result = prime * result + tentativeCount;
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
		CourseCountDTO other = (CourseCountDTO) obj;
		if (courseid == null) {
			if (other.courseid != null)
				return false;
		}
		else if (!courseid.equals(other.courseid))
			return false;
		if (general != other.general)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (postname == null) {
			if (other.postname != null)
				return false;
		}
		else if (!postname.equals(other.postname))
			return false;
		if (registiveCount != other.registiveCount)
			return false;
		if (tentativeCount != other.tentativeCount)
			return false;
		return true;
	}
}
