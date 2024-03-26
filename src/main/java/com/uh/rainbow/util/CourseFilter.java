package com.uh.rainbow.util;

import com.uh.rainbow.entities.timeblock.TimeBlock;
import com.uh.rainbow.entities.timeblock.simple.SimpleTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> CourseFilter.java
 * <p>
 * <b>Description:</b> Filters courses when parsing tables
 *
 * @author Derek Garcia
 */
public class CourseFilter {

    private final static String TIME_INPUT = "HHmm";
    private final static String TIME_OUTPUT = "hhmm a";
    private final static DateFormat INPUT = new SimpleDateFormat(TIME_INPUT);
    private final static DateFormat OUTPUT = new SimpleDateFormat(TIME_OUTPUT);

    private Set<String> crns = null;
    private Set<String> codes = null;
    private Set<String> subjects = null;
    private SimpleTime start_after = null;
    private SimpleTime end_before = null;
    private int online = -1;
    private Set<String> instructors = null;
    private List<String> keywords = null;

    public CourseFilter(
            List<String> crns,
            List<String> codes,
            List<String> subjects,
            String start_after,
            String end_before,
            String online,
            List<String> instructors,
            List<String> keywords) throws ParseException {
        
        if(crns != null)
            this.crns = new HashSet<>(crns);

        if(codes != null)
            this.codes = new HashSet<>(codes);



        if(subjects != null){
            subjects.replaceAll(String::toUpperCase);
            this.subjects = new HashSet<>(subjects);
        }

        if(start_after != null)
            this.start_after = new SimpleTime(start_after);

        if(end_before != null)
            this.end_before = new SimpleTime(end_before);

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

    public boolean validTimes(String times){

        if(this.start_after == null && this.end_before == null)
            return true;

        if(times.equalsIgnoreCase("TBA"))
            return false;

        try{
            TimeBlock timeBlock = new TimeBlock(times, "TBA");

            if(this.start_after != null && timeBlock.getStartTime().beforeOrEqual(this.start_after) == 1)
                return false;

            if(this.end_before != null && timeBlock.getEndTime().afterOrEqual(this.end_before) == 1)
                return false;

            return true;
        } catch (ParseException e){
            return false;
        }
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
