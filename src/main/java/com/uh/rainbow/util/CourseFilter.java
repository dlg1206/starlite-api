package com.uh.rainbow.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class CourseFilter {

    private Set<String> crns = null;
    private Set<String> codes = null;
    private Set<String> subjects = null;
    private String start_after = null;
    private String end_after = null;
    private int online = -1;
    private Set<String> instructors = null;
    private List<String> keywords = null;

    public CourseFilter(
            List<String> crns,
            List<String> codes,
            List<String> subjects,
            String start_after,
            String end_after,
            String online,
            List<String> instructors,
            List<String> keywords){
        
        if(crns != null)
            this.crns = new HashSet<>(crns);

        if(codes != null)
            this.codes = new HashSet<>(codes);



        if(subjects != null){
            subjects.replaceAll(String::toUpperCase);
            this.subjects = new HashSet<>(subjects);
        }


        this.start_after = start_after;
        this.end_after = end_after;

        if(online != null){
            if(online.equalsIgnoreCase("true")){
                this.online = 1;
            } else {
                this.online = 0;
            }
        }

        if(instructors != null){
            instructors.replaceAll(String::toLowerCase);
            this.instructors = new HashSet<>(instructors);
        }


        if(keywords != null){
            keywords.replaceAll(String::toLowerCase);
            this.keywords = keywords;
        }

    }


    public boolean validCRN(String crn){
        if(this.crns == null)
            return true;
        return this.crns.contains(crn);
    }

    public boolean validCID(String cid){
        if(this.codes == null)
            return true;
        return this.subjects.contains(cid.split(" ")[0]) || this.codes.contains(cid.split(" ")[1]);
    }

    public boolean validSubject(String subject){
        if(this.subjects == null)
            return true;
        return this.subjects.contains(subject.toUpperCase());
    }

    public boolean validOnline(String room){
        if(this.online == -1)
            return true;

        int isOnline = room.toLowerCase().contains("online") ? 1 : 0;

        return this.online == isOnline;
    }

    public boolean validInstructor(String instructor){
        if(this.instructors == null)
            return true;
        return this.instructors.contains(instructor.toLowerCase());
    }



    public boolean keywordsMatch(String string){
        if(this.keywords == null)
            return true;
        for(String keyword : this.keywords){
            // Match a keyword
            if(string.contains(keyword))
                return true;
        }
        // No keywords matches
        return false;
    }









}
