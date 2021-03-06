// PrognosReport.java

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

import java.util.Enumeration;

import net.rujel.base.MyUtility;
import net.rujel.base.SettingsBase;
import net.rujel.eduresults.ItogContainer;
import net.rujel.eduresults.ItogMark;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.Student;
import net.rujel.reusables.*;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Sep 24, 2008 2:31:29 PM
public class PrognosReport extends com.webobjects.appserver.WOComponent {
	
    public PrognosReport(WOContext context) {
        super(context);
    }
    
	public static NSDictionary reportForStudent(WOSession session) {
		NSDictionary settings = (NSDictionary)session.objectForKey("reportForStudent");
		NSDictionary options = (NSDictionary)settings.valueForKeyPath("reporter.settings.autoitog");	
		if(options == null || !Various.boolForObject(options.valueForKey("active")))
			return null;
		NSTimestamp date = (NSTimestamp)session.valueForKey("today");
		if(date == null)
			return null;
		boolean ifTimeout = Various.boolForObject(options.valueForKey("timeout"));
		Student student = (Student)settings.valueForKey("student");
		EOEditingContext ec = student.editingContext();
//		NSArray courses = (NSArray)settings.valueForKey("courses");
		NSMutableDictionary result = new NSMutableDictionary("autoitog","id");
		result.takeValueForKey("PrognosReport", "component");
		result.takeValueForKey(options.valueForKey("sort"), "sort");
//		ItogContainer eduper = null;
		Period period = (Period)settings.valueForKey("period");
		if(period != null && !period.contains(date)) {
			long millis = period.end().getTime()/2 + period.begin().getTime()/2;
			date = new NSTimestamp(millis);
		}
		EOQualifier[] qual = new EOQualifier[2];
		qual[0] = new EOKeyValueQualifier("student",EOQualifier.QualifierOperatorEqual,student);
		qual[1] = new EOKeyValueQualifier(Prognosis.FIRE_DATE_KEY
				,EOQualifier.QualifierOperatorGreaterThanOrEqualTo,date);
		qual[0] = new EOAndQualifier(new NSArray(qual));
		EOFetchSpecification fs = new EOFetchSpecification(Prognosis.ENTITY_NAME,qual[0],null);
		NSArray prognoses = ec.objectsWithFetchSpecification(fs);
		if(prognoses == null || prognoses.count() == 0)
			return null;
		Enumeration enu = prognoses.objectEnumerator();
		NSArray sorter = new NSArray(new EOSortOrdering(Prognosis.ITOG_CONTAINER_KEY,
				EOSortOrdering.CompareAscending));
		NSMutableDictionary aiCache = new NSMutableDictionary();
		SettingsBase sb = SettingsBase.baseForKey(ItogMark.ENTITY_NAME, ec, false);
		if(sb == null)
			return null;
		int count = 0;
		SettingsBase reportCourses = (SettingsBase)settings.valueForKey("reportCourses");

		while (enu.hasMoreElements()) {
			Prognosis progn = (Prognosis) enu.nextElement();
			if(progn.namedFlags().flagForKey("disabled"))
				continue;
			EduCourse course = progn.course();
			if(reportCourses != null) {
				Integer num = reportCourses.forCourse(course).numericValue();
				if(!Various.boolForObject(num))
					continue;
			}
			ItogContainer eduper = progn.itogContainer();
			String listName = sb.forCourse(course).textValue();
			NSMutableDictionary byItog = (NSMutableDictionary)aiCache.valueForKey(listName);
			if(byItog == null) {
				byItog = new NSMutableDictionary();
				aiCache.takeValueForKey(byItog, listName);
			}
			AutoItog ai = null;
			try {
				ai = (AutoItog)byItog.objectForKey(eduper);
			} catch (ClassCastException e) {
				continue;
			}
			if(ai == null) {
				ai = AutoItog.forListName(listName, eduper);
				if(ai == null) {
					byItog.setObjectForKey(NullValue, eduper);
					continue;
				}
				byItog.setObjectForKey(ai, eduper);
			}
			if(ai.flags().intValue() >= 16) // check disabled
				continue;
			else
				count++;
			NSMutableDictionary dict = new NSMutableDictionary(eduper,"itog");
			dict.takeValueForKey(progn.mark(),"mark");
			dict.takeValueForKey(eduper.title(), "period");
			if(ifTimeout  && !ai.fireDate().equals(progn.fireDate())) {
				StudentTimeout studentTimeout = StudentTimeout.timeoutForStudentAndCourse(
						progn.student(), course, eduper);
				CourseTimeout courseTimeout = CourseTimeout.getTimeoutForCourseAndPeriod(
						course, eduper);
				if(studentTimeout != null || courseTimeout != null) {
					Timeout timeout = Timeout.Utility.chooseTimeout(
							studentTimeout, courseTimeout);
					String title = (timeout.namedFlags().flagForKey("negative"))?
							Timeout.negativeTitle:Timeout.timeoutTitle;
					dict.takeValueForKey(title, "timeoutTitle");
					title = timeout.reason();
					if(timeout == courseTimeout)
						title = title + " (" + courseTimeout.presentBinding() + ')';
					dict.takeValueForKey(title, "reason");
					String dateStr = MyUtility.dateFormat().format(timeout.fireDate());
					dict.setObjectForKey(dateStr, "timeout");
				}
			}
			NSMutableArray forCourse = (NSMutableArray)result.objectForKey(course);
			if(forCourse == null) {
				forCourse = new NSMutableArray(dict);
				result.setObjectForKey(forCourse, course);
			} else {
				forCourse.addObject(dict);
				EOSortOrdering.sortArrayUsingKeyOrderArray(forCourse, sorter);
			}
		}
		/*
		Enumeration enu = courses.objectEnumerator();
		while (enu.hasMoreElements()) {
			EduCourse course = (EduCourse) enu.nextElement();
			// : enu current AutoItogs
	    	String listName = SettingsBase.stringSettingForCourse(
	    			ItogMark.ENTITY_NAME, course, course.editingContext());
			AutoItog autoItog = AutoItog.forListName(listName, eduper);
			NSMutableDictionary dict = null;
			Prognosis progn = (eduper==null)?null:
				Prognosis.getPrognosis(student, course, autoItog, false);
			if(progn != null)
				dict = new NSMutableDictionary(progn.mark(),"mark");
			if(ifTimeout) {
				StudentTimeout studentTimeout = StudentTimeout.activeTimeout(student, course, date);
				if (studentTimeout != null) {
					if (eduper == null) {
						eduper = studentTimeout.itogContainer();
						progn = Prognosis.getPrognosis(student, course, autoItog,
								false);
						if (progn != null)
							dict = new NSMutableDictionary(progn.mark(), "mark");
					} else if (autoItog.fireDate().compareTo(
							studentTimeout.fireDate()) < 0) {
						studentTimeout = StudentTimeout
								.timeoutForStudentAndCourse(student,
										course, eduper);
					}
				}
				CourseTimeout courseTimeout = CourseTimeout.getTimeoutForCourseAndPeriod(course, eduper);
				if(studentTimeout != null || courseTimeout != null) {
					Timeout timeout = Timeout.Utility.chooseTimeout(studentTimeout, courseTimeout);
					if(dict == null)
						dict = new NSMutableDictionary();
					dict.takeValueForKey(timeout.reason(), "reason");
					String dateStr = MyUtility.dateFormat().format(timeout.fireDate());
					dict.setObjectForKey(dateStr, "timeout");
				}
			}
			if(dict != null) {
				dict.takeValueForKey(eduper.title(), "period");
				result.setObjectForKey(dict, course);
			}
		}*/
		if(count == 0)
			return null;
		else
			settings.takeValueForKey(null, "needData");

		return result;
	}

	public boolean isStateless() {
		return true;
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	/*
	public void reset() {
	}
	 */

	public Integer idx;
	public Object item;
	
/*	protected static String timeoutUpTo;
	public String timeoutUpTo() {
		if(timeoutUpTo == null) {
			timeoutUpTo = (String)session()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.ui.generalTimeout") + 
				' ' + (String)session()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.ui.upTo");
		}
		//NSDictionary dict = (NSDictionary)valueForBinding("value");
		return timeoutUpTo;
	}*/
	
	public String prognosTitle() {
		if(idx == null || idx.intValue() > 0)
			return null;
		NSArray list = (NSArray)valueForBinding("value");
		StringBuilder buf = new StringBuilder("<th style = \"font-size:120%;\"");
		if(list != null && list.count() > 1)
			buf.append(" rowspan = \"").append(list.count()).append('"');
		buf.append('>').append(session().valueForKeyPath(
				"strings.RujelAutoItog_AutoItog.properties.Prognosis.this"));
		buf.append("</th>");
		return buf.toString();
	}
}