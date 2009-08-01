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
import java.util.logging.Logger;

import net.rujel.base.MyUtility;
import net.rujel.eduresults.EduPeriod;
import net.rujel.eduresults.PeriodType;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.Student;
import net.rujel.reusables.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Sep 24, 2008 2:31:29 PM
public class PrognosReport extends com.webobjects.appserver.WOComponent {
    public PrognosReport(WOContext context) {
        super(context);
    }
    
	public static NSDictionary reportForStudent(NSDictionary settings) {
		NamedFlags options = (NamedFlags)settings.valueForKey("autoitog");	
		if(options == null)
			return null;
		boolean ifTimeout = options.flagForKey("timeout");
		Student student = (Student)settings.valueForKey("student");
		//EOEditingContext ec = student.editingContext();
		NSArray courses = (NSArray)settings.valueForKey("courses");
		Period period = (Period)settings.valueForKey("period");
		NSMutableDictionary result = ((NSDictionary)WOApplication.application()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.prognosReport")).mutableClone();
		EduPeriod eduper = null;
		NSTimestamp date = (NSTimestamp)settings.valueForKey("to");
		if(date == null && period != null)
			date = new NSTimestamp(period.end());
		Enumeration enu = courses.objectEnumerator();
		while (enu.hasMoreElements()) {
			EduCourse course = (EduCourse) enu.nextElement();
			if(period instanceof EduPeriod) {
				eduper = (EduPeriod)period;
			} else {
				NSArray pertypes = PeriodType.periodTypesForCourse(course);
				if(pertypes != null && pertypes.count() > 0) {
					PeriodType pt = (PeriodType)pertypes.objectAtIndex(0);
					if(date != null)
						eduper = pt.currentPeriod(date);
/*					if(eduper == null) {
						date = (NSTimestamp)settings.valueForKey("since");
						if(date == null && period != null)
							date = new NSTimestamp(period.begin());
						if(date != null)
							eduper = pt.currentPeriod(date);
					}
*/				}
			}
			NSMutableDictionary dict = null;
			Prognosis progn = (eduper==null)?null:
				Prognosis.getPrognosis(student, course, eduper, false);
			if(progn != null)
				dict = new NSMutableDictionary(progn.mark(),"mark");
			if(ifTimeout) {
				StudentTimeout studentTimeout = StudentTimeout.activeTimeout(student, course, date);
				if (studentTimeout != null) {
					if (eduper == null) {
						eduper = studentTimeout.eduPeriod();
						progn = Prognosis.getPrognosis(student, course, eduper,
								false);
						if (progn != null)
							dict = new NSMutableDictionary(progn.mark(), "mark");
					} else if (eduper.end().compareTo(
							studentTimeout.eduPeriod().end()) < 0) {
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
		}

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

	protected static String timeoutUpTo;
	public String timeoutUpTo() {
		if(timeoutUpTo == null) {
			timeoutUpTo = (String)WOApplication.application()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.ui.generalTimeout") + 
				' ' + (String)WOApplication.application()
				.valueForKeyPath("strings.RujelAutoItog_AutoItog.ui.upTo");
		}
		//NSDictionary dict = (NSDictionary)valueForBinding("value");
		return timeoutUpTo;
	}
}