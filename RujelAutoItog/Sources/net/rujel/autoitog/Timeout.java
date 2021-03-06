// Timeout.java

/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	•	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	•	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
 * 		to endorse or promote products derived from this software without specific 
 * 		prior written permission.
 * 		
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.rujel.autoitog;


import java.text.DateFormat;
import java.text.FieldPosition;

import net.rujel.base.MyUtility;
import net.rujel.eduresults.ItogContainer;
import net.rujel.eduresults.ItogMark;
import net.rujel.interfaces.*;
import net.rujel.reusables.NamedFlags;

import com.webobjects.foundation.*;
import com.webobjects.appserver.WOApplication;
import com.webobjects.eocontrol.*;

public interface Timeout extends EOEnterpriseObject {
	public static final NSArray sorter = new NSArray(EOSortOrdering.sortOrderingWithKey(
			"fireDate",EOSortOrdering.CompareDescending));
	
	public static final NSArray flagNames = new NSArray(new String[]
	               {"-1-","negative","priority","-8-","-16-","-32-","passed"});

	public static final String timeoutTitle = (String)WOApplication.application()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.ui.generalTimeout");
	public static final String negativeTitle = (String)WOApplication.application()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.ui.negativeTimeout");
	

	public EduCourse course();
	
    public void setCourse(EduCourse aValue);

    public ItogContainer itogContainer();
	
    public NSTimestamp fireDate();
    
    public void setFireDate(NSTimestamp aValue);

    public Number flags();
    
    public NamedFlags namedFlags();

    public String reason();

    public void setReason(String aValue);
    
    public NSArray relatedPrognoses();
    
    public static class Utility {
/*    	public static void setFireDate(Timeout timeout, NSTimestamp date) {
    		NSArray prognoses = timeout.relatedPrognoses();
    		if(date == null || timeout.fireDate() == null) {
    			prognoses.valueForKey("updateFireDate");
    			return;
    		}
    		int compare = date.compare(timeout.fireDate());
    		if(compare == 0)
    			return;
    		timeout.setFireDate(date);
    		if(compare > 0) {
    			prognoses.takeValueForKey(date, "laterFireDate");
    		} else {
    			prognoses.valueForKey("updateFireDate");
    		}
    	}
 */   	
    	public static NSTimestamp chooseDate(StudentTimeout studentTimeout,
    			CourseTimeout courseTimeout) {
    		Timeout timeout = chooseTimeout(studentTimeout, courseTimeout); 
    		return (timeout==null)?null:timeout.fireDate();
    	}

    	public static Timeout chooseTimeout(StudentTimeout studentTimeout,
    			CourseTimeout courseTimeout) {
    		if(studentTimeout == null)
    			return courseTimeout;
    		else if(courseTimeout == null)
    			return studentTimeout;
    		if(studentTimeout.course() != null)
    			return studentTimeout;
    		long stDate = studentTimeout.fireDate().getTime();
    		long crDate = courseTimeout.fireDate().getTime();
    		if(courseTimeout.namedFlags().flagForKey("negative")) {
    			if(studentTimeout.namedFlags().flagForKey("negative")) {
    				if(stDate > crDate) {
    					return courseTimeout;
    				} else {
    					return studentTimeout;
    				}
    			}
    		} else {
    			if(!studentTimeout.namedFlags().flagForKey("negative")) {
    				if(stDate > crDate) {
    					return studentTimeout;
    				} else {
    					return courseTimeout;
    				}
    			}
    		}
    		if(courseTimeout.namedFlags().flagForKey("priority") && 
    				!studentTimeout.namedFlags().flagForKey("priority"))
    			return courseTimeout;
    		return studentTimeout;
    	}
    	
    	private static FieldPosition pos = new FieldPosition(DateFormat.DATE_FIELD);
    	public static String presentTimeout(Timeout timeout) {
			StringBuffer buf = new StringBuffer();
//			buf.append(upTo).append(' ');
			MyUtility.dateFormat().format(timeout.fireDate(), buf, pos);
			buf.append(" : <em>").append(timeout.reason());
			if(timeout instanceof CourseTimeout) {
				buf.append(' ').append('(');
				((CourseTimeout)timeout).presentBinding(buf).append(')');
			}
			buf.append("</em>");
			return buf.toString();
    	}
    	
    	public static void setTimeoutComment(EOEnterpriseObject cmntEO, Timeout timeout) {
			NSMutableDictionary dict = ItogMark.commentsDict(cmntEO);
			boolean changed = false;
			if(timeout == null) {
				changed = ((dict.removeObjectForKey(timeoutTitle) != null) || 
							(dict.removeObjectForKey(negativeTitle) != null));
			} else {
				String comment = Timeout.Utility.presentTimeout(timeout);
				if(timeout.namedFlags().flagForKey("negative")) {
					changed = (dict.removeObjectForKey(timeoutTitle) != null ||
							!comment.equals(dict.objectForKey(negativeTitle)));
					if(changed)
						dict.setObjectForKey(comment, negativeTitle);
				} else {
					changed = (dict.removeObjectForKey(negativeTitle) != null ||
							!comment.equals(dict.objectForKey(timeoutTitle)));
					if(changed)
						dict.setObjectForKey(comment, timeoutTitle);
				}
			}
			if(changed)
				cmntEO.takeValueForKey(NSPropertyListSerialization.
						stringFromPropertyList(dict), "comment");
    	}
    }
}
