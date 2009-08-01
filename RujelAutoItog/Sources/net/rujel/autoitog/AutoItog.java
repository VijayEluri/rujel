//  AutoItog.java

/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	o	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	o	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	o	Neither the name of the RUJEL nor the names of its contributors may be used
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

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.rujel.base.SettingsBase;
import net.rujel.eduresults.ItogContainer;
import net.rujel.eduresults.ItogMark;
import net.rujel.eduresults.ItogType;
import net.rujel.interfaces.EOInitialiser;
import net.rujel.interfaces.EduCourse;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;

public class AutoItog extends _AutoItog {
	protected static Logger logger = Logger.getLogger("rujel.autoitog");
	
	public static final NSArray sorter = new NSArray( new EOSortOrdering[] {
		EOSortOrdering.sortOrderingWithKey(FIRE_DATE_KEY,EOSortOrdering.CompareAscending),
		EOSortOrdering.sortOrderingWithKey(FIRE_TIME_KEY,EOSortOrdering.CompareAscending),
		EOSortOrdering.sortOrderingWithKey(ITOG_CONTAINER_KEY,EOSortOrdering.CompareAscending)
		});

	public static void init() {
		EOInitialiser.initialiseRelationship("ItogRelated","course",false,"courseID","EduCourse");
		ComparisonSupport.setSupportForClass(new ComparisonSupport(), AutoItog.class);
	}

	public void awakeFromInsertion(EOEditingContext ec) {
		super.awakeFromInsertion(ec);
		setFlags(new Integer(0));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 4);
		cal.set(Calendar.MINUTE, 20);
		cal.set(Calendar.DAY_OF_MONTH, 27);
		cal.add(Calendar.MONTH, 2);
		NSTimestamp date = new NSTimestamp(cal.getTimeInMillis());
		setFireTime(date);
		setFireTime(date);
	}
	
	public static NSTimestamp combineDateAndTime(Date date, Date time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(time);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DAY_OF_YEAR, day);
		return new NSTimestamp(cal.getTimeInMillis());
	}
	
	public NSTimestamp fireDateTime() {
		NSTimestamp date = combineDateAndTime(fireDate(), fireTime());
		return date;
	}
	
    public boolean noCalculator() {
    	String calcName = calculatorName();
    	if(calcName == null || calcName.length() == 0 || calcName.equalsIgnoreCase("none"))
    		return true;
    	return false;
    }

    public static AutoItog forListName(String listName, ItogContainer container) {
    	EOEditingContext ec = container.editingContext();
    	NSDictionary values = new NSDictionary(new Object[] {listName,container},
    			new String[] {LIST_NAME_KEY,ITOG_CONTAINER_KEY});
    	try {
    		return (AutoItog)EOUtilities.objectMatchingValues(ec, ENTITY_NAME, values);
    	} catch (EOObjectNotAvailableException e) {
    		return null;
    	}
    }
    
    public static NSArray currentAutoItogsForCourse(EduCourse course,NSTimestamp date) {
    	EOEditingContext ec = course.editingContext();
    	String listName = SettingsBase.stringSettingForCourse(ItogMark.ENTITY_NAME, course, ec);
    	EOQualifier[] quals = new EOQualifier[2];
    	quals[0] = new EOKeyValueQualifier(LIST_NAME_KEY,
    			EOQualifier.QualifierOperatorEqual,listName);
    	quals[1] = new EOKeyValueQualifier(FIRE_DATE_KEY,
    			EOQualifier.QualifierOperatorGreaterThanOrEqualTo, date);
    	quals[0] = new EOAndQualifier(new NSArray(quals));
    	EOFetchSpecification fs = new EOFetchSpecification(ENTITY_NAME,quals[0],sorter);
    	NSArray found = ec.objectsWithFetchSpecification(fs);
    	if(found == null || found.count() == 0)
    		return null;
    	Enumeration enu = found.objectEnumerator();
    	NSMutableArray result = new NSMutableArray();
    	NSMutableSet types = new NSMutableSet();
    	while (enu.hasMoreElements()) {
			AutoItog ai = (AutoItog) enu.nextElement();
			if(ai.fireDateTime().getTime() < System.currentTimeMillis())
				continue;
			ItogType type = ai.itogContainer().itogType();
			if(types.containsObject(type))
				continue;
			types.addObject(type);
			result.addObject(ai);
		}
    	return result;
    }

    protected Calculator _calculator;
    public Calculator calculator() {
    	if(_calculator == null) {
    		_calculator = Calculator.calculatorForName(calculatorName());
    	}
    	return _calculator;
    }

    public NSArray relatedForCourse(EduCourse course) {
    	NSDictionary values = new NSDictionary(new Object[] {this,course},
    			new String[] {"autoItog","course"});
    	EOEditingContext ec = editingContext();
    	NSArray found = EOUtilities.objectsMatchingValues(ec, "ItogRelated", values);
    	if(found == null || found.count() == 0)
    		return null;
    	NSMutableArray related = new NSMutableArray();
    	String entName = calculator().reliesOnEntity();
    	Enumeration enu = found.objectEnumerator();
    	while (enu.hasMoreElements()) {
			EOEnterpriseObject ir = (EOEnterpriseObject) enu.nextElement();
			Integer relKey = (Integer)ir.valueForKey("relKey");
			try {
				related.addObject(EOUtilities.objectWithPrimaryKeyValue(ec, entName, relKey));
			} catch (RuntimeException e) {
				logger.log(WOLogLevel.WARNING,"Could not get related object: " + entName +
						':' + relKey,new Object[] {this,e});
			}
		}
    	return related.immutableClone();
    }

    public static class ComparisonSupport extends EOSortOrdering.ComparisonSupport {

		public int compareAscending(Object left, Object right)  {
			if(!(left instanceof AutoItog))
				return NSComparator.OrderedAscending;
			AutoItog l = (AutoItog)left;
			if(!(right instanceof AutoItog))
				return NSComparator.OrderedDescending;
			AutoItog r = (AutoItog)right;
			int result = compareValues(l.fireDateTime(), r.fireDateTime(),
					EOSortOrdering.CompareAscending);
			if(result == NSComparator.OrderedSame)
			result = compareValues(l.itogContainer(),r.itogContainer(),
					EOSortOrdering.CompareAscending);
			return result;
		}

		public int compareCaseInsensitiveAscending(Object left, Object right)  {
			return compareAscending(left, right) ;
		}
		public int compareDescending(Object left, Object right)  {
			return compareAscending(right, left) ;
		}
		public int compareCaseInsensitiveDescending(Object left, Object right)  {
			return compareAscending(right, left) ;
		}
	}
}
