//VariationsPlugin.java: Class file for WO Component 'VariationsPlugin'

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

package net.rujel.curriculum;

import net.rujel.eduplan.PlanCycle;
import net.rujel.interfaces.EduCourse;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Feb 10, 2009 3:17:25 PM
public class VariationsPlugin extends com.webobjects.appserver.WOComponent {
    protected EduCourse _course;
    public int plan;
	public String styleClass;
	public int lessonsCount;


	public VariationsPlugin(WOContext context) {
        super(context);
    }

/*	public EduCourse course() {
		if(_course == null) {
			_course = (EduCourse)valueForBinding("course");
		}
		return _course;
	}*/
	
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	NSTimestamp today = (NSTimestamp)session().valueForKey("today");
    	EduCourse course = (EduCourse)valueForBinding("course");
    	
    	plan = Variation.yearPlanWithVariations(course, null, today);

    	NSArray lessons = course.lessons();
    	lessonsCount = (lessons == null)?0:lessons.count();
    	if(lessonsCount > 0) {
    		if(today.compareTo((NSTimestamp)lessons.valueForKeyPath("@max.date")) < 0) {
    			EOQualifier qual = new EOKeyValueQualifier("date",
    					EOQualifier.QualifierOperatorLessThanOrEqualTo,today);
    			lessons = EOQualifier.filteredArrayWithQualifier(lessons, qual);
    			lessonsCount = (lessons == null)?0:lessons.count();
    		}
    		int totalPlan = PlanCycle.wholeYearPlanLessons(course, null, today);
    		NSArray vars = Variation.variations(course, null, today, null);
    		if(vars != null && vars.count() > 0) {
    			Number shift = (Number)vars.valueForKeyPath("@sum.value");
    			totalPlan += shift.intValue();
    		}
    		totalPlan -= lessonsCount;
    		// TODO: more smart validation;
    		if(totalPlan > 2)
    			styleClass = "warning";
    		else if(totalPlan < -1)
    			styleClass = "highlight2";
    		else
    			styleClass = "gerade";
    		//styleClass = (Math.abs(totalPlan) > 2)?"warning":"gerade";
    	}
    	super.appendToResponse(aResponse, aContext);
    }
	

	
	public WOActionResults popup() {
		WOComponent popup = pageWithName("VariationsList");
		popup.takeValueForKey(valueForBinding("course"), "course");
		popup.takeValueForKey(context().page(), "returnPage");
//		popup.takeValueForKey(valueForBinding("currLesson"), "currLesson");
//		popup.takeValueForKey(valueForBinding("currTab"), "currTab");
		return popup;
	}

	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	public void reset() {
		_course = null;	
		super.reset();
	}
}