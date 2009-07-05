// EduPeriod.java

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

package net.rujel.eduresults;

import net.rujel.reusables.*;
import net.rujel.interfaces.*;
import net.rujel.base.MyUtility;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.WOApplication;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import com.webobjects.eoaccess.EOUtilities;

public class EduPeriod extends _EduPeriod implements PerPersonLink,EOPeriod
{

	public EduPeriod() {
        super();
    }
	
	/*
	 // If you add instance variables to store property values you
	 // should add empty implementions of the Serialization methods
	 // to avoid unnecessary overhead (the properties will be
	 // serialized for you in the superclass).
	 private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
	 }
	 
	 private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
	 }
	 */
	
	public int countInYear() {
		PeriodType type = periodType();
		if(type != null && type.inYearCount() != null)
			return type.inYearCount().intValue();
		return 0;
	}
	
	public boolean contains(Date date) {
		return (date.compareTo(begin()) >= 0 && date.compareTo(end()) <= 0);
	}
	
	public String typeID() {
		return "EduPeriod." + periodType().name();
	}
	
	public EduPeriod nextPeriod() {
		Number year = eduYear();
		Number num = num();
		if(year == null || num == null || num.intValue() >= countInYear())
			return null;
		NSMutableDictionary attrs = new NSMutableDictionary(year,"eduYear");
		Integer nextNum = new Integer(num.intValue() + 1);
		attrs.setObjectForKey(nextNum,"num");
		attrs.setObjectForKey(periodType(),"periodType");
		try {
			return (EduPeriod)EOUtilities.objectMatchingValues(editingContext(),"EduPeriod",attrs);
		} catch (Exception ex) {
			if(ex instanceof EOUtilities.MoreThanOneException) {
				/// log
			}
			return null;
		}
	}
	
	public Number validateNum(Object aValue) throws NSValidation.ValidationException {
		Integer numberValue;
		if (aValue instanceof String) {
			// Convert the String to an Integer.
			try {
				numberValue = new Integer((String)aValue);
			} catch (NumberFormatException numberFormatException) {
				throw new NSValidation.ValidationException("Validation exception: Unable to convert the String " + aValue + " to an Integer");
			}
		} else if (aValue instanceof Number) {
			numberValue = new Integer(((Number)aValue).intValue());
		} else {
			throw new NSValidation.ValidationException("Validation exception: Unable to convert the Object " + aValue + " to an Integer");
		}
		
		int num = numberValue.intValue();
		if (num <= 0 || num > countInYear()) {
			String message = String.format((String)WOApplication.application().valueForKeyPath("strings.RujelEduResults_EduResults.invalidPeriodNum"),periodType().inYearCount());
			throw new NSValidation.ValidationException(message, this, "num");
		}
		return numberValue;
	}
	
	public void validateForSave() throws NSValidation.ValidationException {
		super.validateForSave();
		if(begin().compare(end()) >= 0) {
			String message = (String)WOApplication.application().valueForKeyPath("strings.RujelEduResults_EduResults.beginEndPeriod");
			throw new NSValidation.ValidationException(message);
		}
	}
	
	/*
	 public static EduPeriod currentPeriodOfType(PeriodType type, NSTimestamp date, EOEditingContext ec) {
		 EOQualifier qual = new EOKeyValueQualifier("periodType",EOQualifier.QualifierOperatorEqual,type);
		 NSMutableArray quals = new NSMutableArray(qual);
		 qual = new EOKeyValueQualifier("begin",EOQualifier.QualifierOperatorLessThanOrEqualTo,date);
		 quals.addObject(qual);
		 qual = new EOKeyValueQualifier("end",EOQualifier.QualifierOperatorGreaterThanOrEqualTo,date);
		 quals.addObject(qual);
		 qual = new EOAndQualifier(quals);
		 EOFetchSpecification fspec = new EOFetchSpecification("EduPeriod",qual,null);
		 NSArray result = ec.objectsWithFetchSpecification(fspec);
		 if(result == null || result.count() == 0) return null;
		 if(result.count() > 1) {
			 /// log
		 }
		 return (EduPeriod)result.objectAtIndex(0);
	 }
	 public static EduPeriod currentPeriodOfType(PeriodType type, EOEditingContext ec) {
		 NSTimestamp today = null;
		 if(ec instanceof SessionedEditingContext) {
			 today = (NSTimestamp)((SessionedEditingContext)ec).session().valueForKey("today");
		 }
		 if(today == null) today = new NSTimestamp();
		 
		 return currentPeriodOfType(type,today,ec);
	 }*/
	
	public static NSArray periodsForCourse(EduCourse course) {
		NSArray types = PeriodType.periodTypesForCourse(course);
		if(types == null || types.count() == 0) return null;
		EOEditingContext ec = course.editingContext();
		NSMutableArray result = new NSMutableArray();
		Enumeration enu = types.objectEnumerator();
		NSMutableDictionary dict = new NSMutableDictionary(course.eduYear(),"eduYear");
		while(enu.hasMoreElements()) {
			PeriodType perType = (PeriodType)enu.nextElement();
			dict.setObjectForKey(perType,"periodType");
			NSArray found = EOUtilities.objectsMatchingValues(ec,"EduPeriod",dict);
			if((found == null || found.count() == 0) && !ec.hasChanges()) {
				if(!SettingsReader.boolForKeyPath("edu.autogenerateEduPeriods", true))
					continue;
				try {
					ec.lock();
					found = perType.generatePeriodsFromTemplates(course.eduYear().intValue());
					ec.saveChanges();
				} catch (Exception ex) {
					ec.revert();
				} finally {
					ec.unlock();
				}
			}
			if(found != null && found.count() > 0)
				result.addObjectsFromArray(found);
		}
		EOSortOrdering.sortArrayUsingKeyOrderArray(result,sorter);
		return result;
		/*
		EOQualifier qual = Various.getEOInQualifier("periodType",types);
		NSMutableArray arr = new NSMutableArray(qual);
		qual = new EOKeyValueQualifier("eduYear",EOQualifier.QualifierOperatorEqual,course.eduYear());
		arr.addObject(qual);
		qual = new EOAndQualifier(arr);
		
		arr.removeAllObjects();
		arr.addObject(EOSortOrdering.sortOrderingWithKey("end",EOSortOrdering.CompareAscending));
		arr.addObject(EOSortOrdering.sortOrderingWithKey("begin",EOSortOrdering.CompareDescending));
		
		EOFetchSpecification fspec = new EOFetchSpecification("EduPeriod",qual,sorter);
		return course.editingContext().objectsWithFetchSpecification(fspec);*/
	}
	
	public static NSArray periodsInYear(Number eduYear, EOEditingContext ec) {
		NSArray result = EOUtilities.objectsMatchingKeyAndValue(ec, 
				ENTITY_NAME, EDU_YEAR_KEY, eduYear);
		if(result == null || result.count() < 2)
			return result;
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(result,sorter);
	}
	
	public static EduPeriod defaultCurrentPeriod(NSTimestamp moment, EOEditingContext ec) {
		Number eduYear = MyUtility.eduYearForDate(moment);
		NSArray typeUsage = EOUtilities.objectsWithQualifierFormat(ec,"PeriodTypeUsage",
				"(eduYear = %d OR eduYear = 0) AND eduGroup = nil AND course = nil",
							new NSArray(eduYear));
		if(typeUsage == null || typeUsage.count() == 0)
			return null;
		PeriodType pertype = null;
		if(typeUsage.count() > 1) {
			typeUsage = PeriodType.filterTypeUsageArray(typeUsage,eduYear);
			typeUsage = (NSArray)typeUsage.valueForKey("periodType");
			NSMutableArray res = (typeUsage instanceof NSMutableArray)?(NSMutableArray)typeUsage:typeUsage.mutableClone();
			EOSortOrdering so = EOSortOrdering.sortOrderingWithKey("inYearCount",EOSortOrdering.CompareDescending);
			EOSortOrdering.sortArrayUsingKeyOrderArray(res,new NSArray(so));
			typeUsage = res;
			Enumeration enu = typeUsage.objectEnumerator();
			while (enu.hasMoreElements()) {
				pertype = (PeriodType) enu.nextElement();
				EduPeriod result = pertype.currentPeriod(moment);
				if(result != null)
					return result;
			}
		} else {
			pertype = (PeriodType)((EOEnterpriseObject)typeUsage.objectAtIndex(0)).valueForKey("periodType");
		}
		return pertype.currentPeriod(moment);
	}
	
	//EduLesson compilance
	protected transient EduCourse course;
//	protected transient Number lesNum;
	protected transient NSArray itogs;
	
	
	public EduCourse course() {
		return course;
	}
	public void setCourse(EduCourse newCourse) {
		itogs = null;
		course = newCourse;
	}
	
	public String title() {
		if(countInYear() > 1) {
			return Various.makeRoman(num().intValue()) + ' ' + periodType().title();
		}
		return periodType().title();
	}
	
	public String name() {
		if(countInYear() > 1) {
			return Various.makeRoman(num().intValue()) + ' ' + periodType().name();
		}
		return periodType().name();
	}

	/*
	public Number number() {
		if(lesNum == null)
			return num();
		return lesNum;
	}
	public void setNumber(Number newNumber) {
		lesNum = newNumber;
	}
	
	public NSTimestamp date() {
		return end();
	}
	public void setDate(NSTimestamp newDate) {
		throw new UnsupportedOperationException("You can't change date here");
	}
	
	public void setTitle(String newTitle) {
		throw new UnsupportedOperationException("You can't change title here");
	}	
	
	public String theme() {
		return periodType().name();
	}
	public void setTheme(String newTheme) {
		throw new UnsupportedOperationException("You can't change theme here");
	}
	
	
	public String homeTask() {
		return null;
	}
	public void setHomeTask(String newTask) {
		throw new UnsupportedOperationException("Hometask is unavalable for itogs");
	}	
	
	public NSArray notes() {
		if(itogs == null) {
			if(course == null)
				throw new IllegalStateException("Course is not set");
			itogs = ItogMark.getItogMarks(course.cycle(),this,null);
		}
		return itogs;
	}
	public NSArray students() {
		return course.groupList();
	}
	
	public String noteForStudent(Student student) {
		return itogForStudent(student).comment();
	}
	public void setNoteForStudent(String note, Student student) {
		itogForStudent(student).setComment(note);
	}
	*/
	
	public NSArray allValues() {
		if(itogs == null) {
			if(course == null)
				throw new IllegalStateException("Course is not set");
			itogs = ItogMark.getItogMarks(course.cycle(),this,null);
		}
		return itogs;
	}
	public int count() {
		return allValues().count();
	}
	
	public ItogMark forPersonLink(PersonLink student) {
		return ItogMark.getItogMark(course.cycle(),this,(Student)student,allValues());
	}
	
	public Number sort() {
		return new Integer (100*(100 - countInYear()) + (num().intValue()));
	}
	
	public int code() {
		return 10000*eduYear().intValue() + sort().intValue();
	}
	
	public String presentEduYear() {
		if(eduYear() == null)
			return null;
		int year = eduYear().intValue();
		return MyUtility.presentEduYear(year);
	}
	
	public int daysInPeriod(NSTimestamp toDate) {
		Calendar begin = Calendar.getInstance();
		begin.setTime(begin());
		Calendar end = Calendar.getInstance();
		end.setTime(end());
		end.add(Calendar.DATE, 1);
		if(toDate != null){
			if(toDate.getTime() < begin.getTimeInMillis())
				return 0;
			if(toDate.getTime() < end.getTimeInMillis())
				end.setTime(toDate);
		}
		int day = end.get(Calendar.DAY_OF_YEAR) - begin.get(Calendar.DAY_OF_YEAR);
		while (begin.get(Calendar.YEAR) < end.get(Calendar.YEAR)) {
			day += begin.getActualMaximum(Calendar.DAY_OF_YEAR);
			begin.add(Calendar.YEAR, 1);
		}
		return day;
	}
}