// StudentReporter.java

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

package net.rujel.ui;

import java.util.Enumeration;

import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.Person;
import net.rujel.interfaces.Student;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.Period;
import net.rujel.reusables.Various;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Sep 10, 2008 10:31:40 AM
public class StudentReporter extends com.webobjects.appserver.WOComponent {

	public StudentReporter(WOContext context) {
        super(context);
    }
    
	public NSMutableArray courses;

	public Student student;
	public NSTimestamp since;
	public NSTimestamp to;
	public Period period;
	
	public NSArray reports;
    
	protected static NSTimestamp date2timestamp(java.util.Date date) {
		if(date instanceof NSTimestamp)
			return (NSTimestamp)date;
		else
			return new NSTimestamp(date);
	}


	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public void reset() {
		student = null;
		since = null;
		to = null;
		period = null;
//		allWorks = null;
		courses = null;
		reports = null;
		report = null;
		courseItem = null;
		_reportItem = null;
	}

	
	public void appendToResponse(WOResponse aResponse,WOContext aContext) {
		student = (Student)valueForBinding("student");
		since = (NSTimestamp)valueForBinding("since");
		to = (NSTimestamp)valueForBinding("to");
		period = (Period)valueForBinding("period");
		if(period != null) {
			if(since == null)
				since = date2timestamp(period.begin());
			if(to == null)
				to = date2timestamp(period.end());
		}
		//EOEditingContext ec = student.editingContext();

		courses = ((NSArray)valueForBinding("courses")).mutableClone();
		EOQualifier q;
		/*NSMutableArray quals = new NSMutableArray();
		q = new EOKeyValueQualifier("eduYear",EOQualifier.QualifierOperatorEqual,session().valueForKey("eduYear"));
		quals.addObject(q);*/
		q = new EOKeyValueQualifier("groupList",EOQualifier.QualifierOperatorContains,student);
		/*quals.addObject(q);
		q = new EOAndQualifier(quals);*/
		EOQualifier.filterArrayWithQualifier(courses,q);
		NSArray sorter = new NSArray(EOSortOrdering.sortOrderingWithKey("cycle",EOSortOrdering.CompareCaseInsensitiveAscending));
		EOSortOrdering.sortArrayUsingKeyOrderArray(courses,sorter);

		
		NSMutableDictionary reportSettings = new NSMutableDictionary(student, "student");
		reportSettings.takeValueForKey(since,"since");
		reportSettings.takeValueForKey(to,"to");
		if(period != null)
			reportSettings.takeValueForKey(period,"period");
		reportSettings.takeValueForKey(courses.immutableClone(), "courses");

		WOSession ses = aContext.session();
		reports = (NSArray)ses.objectForKey("reportSettingsForStudent");
		if(reports == null) {
			reports = (NSArray)ses.valueForKeyPath("modules.reportSettingsForStudent");
			ses.setObjectForKey(reports, "reportSettingsForStudent");
		}
		Enumeration enu = reports.objectEnumerator();
		while (enu.hasMoreElements()) {
			NSDictionary mod = (NSDictionary) enu.nextElement();
			if(!Various.boolForObject(mod.valueForKey("on")))
				continue;
			NSArray options = (NSArray)mod.valueForKey("options");
			NamedFlags flags = new NamedFlags();
			NSMutableArray keys = new NSMutableArray();
			for (int i = 0; i < options.count(); i++) {
				NSDictionary flag = (NSDictionary) options.objectAtIndex(i);
				Object key = flag.valueForKey("flag");
				keys.addObject(key);
				boolean value = Various.boolForObject(flag.valueForKey("on"));
				flags.setFlag(i, value);
			}
			flags.setKeys(keys);
			Object key = mod.valueForKey("title");
			reportSettings.setObjectForKey(flags, key);
		}
		ses.setObjectForKey(reportSettings,"reportForStudent");
		reports = (NSArray)ses.valueForKeyPath("modules.reportForStudent");
		ses.removeObjectForKey("reportForStudent");
		super.appendToResponse(aResponse,aContext);
	}
	
	public EduCourse courseItem;
	public NSKeyValueCoding report;
	protected NSDictionary _reportItem;
	
	public void setReportItem(NSDictionary reportItem) {
		_reportItem = reportItem;
		if(reportItem == null || courseItem == null) {
			report = null;
		} else {
			report = (NSKeyValueCoding)reportItem.objectForKey(courseItem);
		}
	}
	
	public NSDictionary reportItem() {
		return _reportItem;
	}
	
	public String teacher() {
		Person t = (Person)valueForKeyPath("courseItem.teacher.person");
		if(t == null)
			return null;
		return Person.Utility.fullName(t,true,2,2,2);
	}
}