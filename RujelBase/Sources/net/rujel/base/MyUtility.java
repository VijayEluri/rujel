//MyUtility.java

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

package net.rujel.base;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.*;
import com.webobjects.appserver.WOApplication;

import java.text.Format;
import java.util.Date;
import java.util.GregorianCalendar;

import net.rujel.interfaces.EduLesson;
import net.rujel.reusables.SettingsReader;

public class MyUtility {
	private static final int newYearMonth = SettingsReader.intForKeyPath("edu.newYearMonth",GregorianCalendar.JULY);
	private static final int newYearDay = SettingsReader.intForKeyPath("edu.newYearDay",1);

	// TODO : replace NSTimestampFormatter with java.text.SimpleDateFormat

	public static Format dateFormat() {
		return new NSTimestampFormatter(SettingsReader.stringForKeyPath("ui.dateFormat","%Y-%m-%d"));
	}

	public static Integer eduYearForSession(com.webobjects.appserver.WOSession session,String dateKey) {
		NSTimestamp today = null;
		if(dateKey != null)
			today = (NSTimestamp)session.valueForKey(dateKey);
		if(today == null) today = new NSTimestamp();
		return eduYearForDate(today);
	}

	public static Integer eduYearForDate(Date date) {
		if (date == null) return null;
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTime(date);
		int year = gcal.get(GregorianCalendar.YEAR);
		int month = gcal.get(GregorianCalendar.MONTH);
		if(month < newYearMonth || 
				(month == newYearMonth && gcal.get(GregorianCalendar.DAY_OF_MONTH) < newYearDay)) {
			year--;
		}
		return new Integer(year);
	}

	public static Date dateToEduYear(Date date, Integer eduYear) {
		if(!eduYear.equals(eduYearForDate(date))) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			int year = eduYear.intValue();
			int month = cal.get(GregorianCalendar.MONTH);
			int day = cal.get(GregorianCalendar.DAY_OF_MONTH);
			if(month < newYearMonth || (month == newYearMonth && day < newYearDay)) {
				year++;
			}
			cal.set(GregorianCalendar.YEAR, year);
			return cal.getTime();
		}
		return date;
	}

	public static String presentEduYear(int year) {
		StringBuffer buf = new StringBuffer(Integer.toString(year));
		buf.append('/');
		year = (year % 100) + 1;
		if(year < 10)
			buf.append('0');
		buf.append(year);
		return buf.toString();
	}

	public static java.util.Date yearStart(int eduYear) {
		GregorianCalendar cal = new GregorianCalendar(eduYear,newYearMonth,newYearDay);
		return cal.getTime();
	}

	public static NSTimestamp validateDateInEduYear(Object aDate, Integer eduYear, String key) 
	throws NSValidation.ValidationException {
		NSTimestamp date = null;
		if(aDate instanceof NSTimestamp) {
			date = (NSTimestamp)aDate;
		} else if(aDate instanceof Date) {
			date = new NSTimestamp((Date)aDate);
		} else if(aDate instanceof String) {
			try {
				date = (NSTimestamp)MyUtility.dateFormat().parseObject(
						(String)aDate, new java.text.ParsePosition(0));
			} catch (Exception e) {
				throw new NSValidation.ValidationException(
						"Could not parse string to date",aDate,key);
			}
		}
		if(date == null)
			throw new NSValidation.ValidationException(
					"Null value or could not coerce",aDate,key);
		if(eduYear != null && !eduYear.equals(MyUtility.eduYearForDate(date))) {
			String message = (String)WOApplication.application().valueForKeyPath(
			"extStrings.RujelBase_Base.notInEduYear");
			if(message == null)
				message = "Date is not in a eduYear";
			throw new NSValidation.ValidationException(message,aDate,key);
		}
		return date;
	}

	public static String stringForPath(String path) {
		NSDictionary strings = (NSDictionary)WOApplication.application().valueForKey("strings");
		return (String)strings.valueForKeyPath(path);
	}

	public static Object validateAttributeValue(String attr,Object value,
			Class valueType,boolean notNull,int maxLenth) 
	throws NSValidation.ValidationException {
		// TODO: review validation localisation
		//String attributeName = attr.substring(attr.lastIndexOf('.') + 1);
		if(value == null) {
			if(notNull)
				throw new NSValidation.ValidationException(String.format(stringForPath("messages.nullProhibit"),stringForPath("properties." + attr)),value,attr);
			else
				return value;
		}
		if(valueType != null && !(valueType.isInstance(value)))
			throw new NSValidation.ValidationException(String.format(stringForPath("messages.invalidValue"),stringForPath("properties." + attr)),value,attr);

		if(maxLenth > 0 && ((String)value).length() > maxLenth)
			throw new NSValidation.ValidationException(String.format(stringForPath("messages.longString"),stringForPath("properties." + attr),maxLenth),value,attr);

		return value;
	}

	public static Integer setNumberToNewLesson(EduLesson currLesson) {
		EOEditingContext ec = currLesson.editingContext();
		NSMutableArray allLessons = EOUtilities.
		objectsMatchingKeyAndValue(ec, currLesson.entityName(),
				"course", currLesson.course()).mutableClone();
		if(allLessons != null && allLessons.count() > 0) {
			allLessons.removeIdenticalObject(currLesson);
			EOSortOrdering.sortArrayUsingKeyOrderArray(allLessons, EduLesson.sorter);
		}
		if(allLessons == null || allLessons.count() == 0) {
			Integer num = new Integer(1);
			currLesson.setNumber(num);
			return num;
		}
		int idx = allLessons.count() + 1;
		boolean inProgress = true;
		for (int i = idx-2; i >= 0; i--) {
			EduLesson lesson = (EduLesson)allLessons.objectAtIndex(i);
			if(inProgress && lesson.date().compare(currLesson.date()) <= 0) {
				currLesson.setNumber(idx);
				inProgress = false;
				idx--;
			}
			if(lesson.number().intValue() != idx)
				lesson.setNumber(idx);
			idx--;
		}
		Integer num = new Integer(idx);
		if(inProgress) 
			currLesson.setNumber(num);
		return num;
	}
}
