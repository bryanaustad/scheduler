/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lds.rs.dto;

import java.util.Set;
import java.util.TreeSet;

public class OrganizationWiseAttendeeDTO {


    private String organization;
    private int totalAttendees;
    private Set<CourseCountDTO> courseList;

    public OrganizationWiseAttendeeDTO() {
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public int getTotalAttendees() {
        return totalAttendees;
    }

    public void setTotalAttendees(int totalAttendees) {
        this.totalAttendees = totalAttendees;
    }

    public Set<CourseCountDTO> getCourseList() {
        return courseList;
    }

    public void setCourseList(Set<CourseCountDTO> courseList) {
        if(courseList!=null){
            this.courseList = new TreeSet<CourseCountDTO>();
            for (CourseCountDTO ccdto:courseList){
                this.courseList.add(new CourseCountDTO(ccdto.getCourseid(),ccdto.getName(),ccdto.getPostname(),ccdto.isGeneral(),ccdto.getRegistiveCount()));
            }
        }
    }


}
