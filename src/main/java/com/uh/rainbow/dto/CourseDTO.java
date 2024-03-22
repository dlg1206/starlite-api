package com.uh.rainbow.dto;

import com.uh.rainbow.entities.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> CourseDTO
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class CourseDTO extends ResponseDTO {

    private int errors = 0;     // assume no errors
    private final String source;
    private List<Course> courses = new ArrayList<>();
    public CourseDTO(String instID, String termID, String subjectID){
        super();
        this.source = SourceURLBuilder.build(instID, termID, subjectID);
    }

    public void addError(){
        this.errors += 1;
    }

    public int getErrorCount(){
        return this.errors;
    }

    public String getSource(){ return this.source; }
    public List<Course> getCourses() {
        return this.courses;
    }
    public void setCourses(List<Course> courses){
        this.courses = courses;
    }


}
