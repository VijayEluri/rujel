// HolidaysList.java: Class file for WO Component 'HolidaysList'

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

package net.rujel.eduplan;

import java.util.Calendar;
import java.util.Enumeration;

import net.rujel.base.MyUtility;
import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EOPeriod;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

// Generated by the WOLips Templateengine Plug-in at Jul 16, 2009 12:03:22 PM
public class HolidaysList extends com.webobjects.appserver.WOComponent {
	
	protected String listName;
	public NSKeyValueCoding item;
	protected NSMutableArray _list;
//	public Holiday curr;
	protected boolean selected;
	public int totalDays = 0;
	
    public HolidaysList(WOContext context) {
        super(context);
    }
    
    public NSMutableArray list() {
    	if(_list == null) {
    		Object existing = session().valueForKeyPath("readAccess._create.Holiday");
    		if(!Various.boolForObject(existing)) {
    			existing = valueForBinding("existing");
    			if(existing != null && !(existing instanceof Boolean))
    				existing = new Boolean(Various.boolForObject(existing));
    		}
    		_list = holidaysListForYear((Boolean)existing);
    	}
    	return _list;
    }
    
    public boolean exists() {
    	return (item instanceof Holiday);
    }
    
    private Boolean _notGlobal;
    public boolean notGlobal() {
    	if(_notGlobal == null) {
//    		String listName = (String)valueForBinding("listName");
    		if(listName != null) {
    			EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
    			String baseList = SettingsBase.stringSettingForCourse(
    					EduPeriod.ENTITY_NAME, null, ec);
    			_notGlobal = new Boolean(!listName.equals(baseList));
    		} else {
    			_notGlobal = Boolean.FALSE;
    		}
    	}
    	return _notGlobal.booleanValue();
    }
    
    public String rowClass() {
    	if(exists()) {
    		if(item.valueForKey(Holiday.LIST_NAME_KEY) != null)
    			return "female";
    		return "gerade";
    	}
    	if(item.valueForKey(Holiday.ENTITY_NAME) != null)
    		return "selection";
    	if(Various.boolForObject(valueForKeyPath("item.disabled")))
    		return "grey dimtext";
    	return "ungerade";
    }
    
    public WOActionResults save() {
//    	if(add())
//    		return null;
    	add();
    	EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
    	ec.lock();
    	try {
    		convert(ec);
     		ec.saveChanges();
			EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,"Saved Holidays changes",
					session());
    	} catch (Exception e) {
    		session().takeValueForKey(e.getMessage(), "message");
			EduPlan.logger.log(WOLogLevel.INFO,"Error saving Holidays changes",
					new Object[] {session(),e});
		} finally {
			ec.unlock();
		}
//    	EOSortOrdering.sortArrayUsingKeyOrderArray(_list, Holiday.sorter);
    	return null;
    }
    
    public Boolean canCreate() {
    	if(selected)
    		return Boolean.FALSE;
    	return (Boolean)session().valueForKeyPath("readAccess.create.Holiday");
    }
    
    public void select() {
    	if(item instanceof Holiday) {
    		Holiday curr = (Holiday)item;
    		int idx = _list.indexOfIdenticalObject(curr);
    		NSMutableDictionary holiday = new NSMutableDictionary(curr,Holiday.ENTITY_NAME);
    		holiday.takeValueForKey(curr.holidayType(),Holiday.HOLIDAY_TYPE_KEY);
    		holiday.takeValueForKey(curr.begin(),Holiday.BEGIN_KEY);
    		holiday.takeValueForKey(curr.end(),Holiday.END_KEY);
    		holiday.takeValueForKey(curr.days(),"days");
    		_list.replaceObjectAtIndex(holiday, idx);
    		selected = true;
    	} else {
			HolidayType type = (HolidayType) item.valueForKey(Holiday.HOLIDAY_TYPE_KEY);
    		item.takeValueForKey(type.name(),HolidayType.NAME_KEY);
    	}
    }
    
    protected void convert(EOEditingContext ec) {
    	totalDays = 0;
    	if(list() == null || list().count() == 0)
    		return;
    	Object[] list = list().objects();
    	Integer year = (Integer)session().valueForKey("eduYear");
//		String listName = (String)valueForBinding("listName");
    	for (int i = 0; i < list.length; i++) {
			if(list[i] instanceof Holiday) {
				totalDays += ((Holiday)list[i]).days();
				continue;
			}
			NSKeyValueCoding dict = (NSKeyValueCoding)list[i];
			Holiday hd = (Holiday)dict.valueForKey(Holiday.ENTITY_NAME);
			if(selected && hd == null)
				continue;
			boolean disabled = Various.boolForObject(dict.valueForKey("disabled"));
			NSTimestamp begin = (NSTimestamp)dict.valueForKey(Holiday.BEGIN_KEY);
			NSTimestamp end = (NSTimestamp)dict.valueForKey(Holiday.END_KEY);
			if(begin == null || end == null)
				disabled = true;
			HolidayType type = (HolidayType) dict.valueForKey(Holiday.HOLIDAY_TYPE_KEY);
			if(type != null) {
				String name = (String) dict.valueForKey("name");
				if (name != null)
					type.setName(name);
				dict.takeValueForKey(null,"name");
			}
			if(disabled) {
				begin = HolidayType.dateFromPreset(year, type.beginMonth(), type.beginDay());
				dict.takeValueForKey(begin, Holiday.BEGIN_KEY);
				end = HolidayType.dateFromPreset(year, type.endMonth(), type.endDay());
				dict.takeValueForKey(end, Holiday.END_KEY);
				dict.takeValueForKey(Boolean.TRUE, "disabled");
				if(hd != null) {
					String message = "Deleting holiday";
					if(notGlobal()) {
						message = message + " from list '" + listName + '\'';
					}
					EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,
							message,new Object[] {session(),hd});
					if(!notGlobal() || listName.equals(hd.listName())) {
						ec.deleteObject(hd);
					} else {
						String baseList = SettingsBase.stringSettingForCourse(
								EduPeriod.ENTITY_NAME, null, ec);
						hd.setListName(baseList);
						EOQualifier[] quals = new EOQualifier[2];
						quals[0] = new EOKeyValueQualifier("listName",
								EOQualifier.QualifierOperatorNotEqual,listName);
						quals[1] = new EOKeyValueQualifier("listName",
								EOQualifier.QualifierOperatorNotEqual,baseList);
						quals[0] = new EOAndQualifier(new NSArray(quals));
						EOFetchSpecification fs = new EOFetchSpecification(
								"PeriodList",quals[0],null);
						NSArray others = ec.objectsWithFetchSpecification(fs);
						if(others != null && others.count() > 0) {
							Enumeration enu = others.objectEnumerator();
							NSMutableSet set = new NSMutableSet();
							while (enu.hasMoreElements()) {
								EOEnterpriseObject pl = (EOEnterpriseObject) enu.nextElement();
								String otherList = (String)pl.valueForKey("listName");
								if(!set.containsObject(otherList)) {
									set.addObject(otherList);
									Holiday exHd = (Holiday)EOUtilities.
										createAndInsertInstance(ec,Holiday.ENTITY_NAME);
									exHd.addObjectToBothSidesOfRelationshipWithKey(
											type, Holiday.HOLIDAY_TYPE_KEY);
									exHd.setBegin(hd.begin());
									exHd.setEnd(hd.end());
									exHd.setListName(otherList);
								}
							}
						}
					} // local delete (creating for other ListNames)
					dict.takeValueForKey(null, Holiday.ENTITY_NAME);
					dict.takeValueForKey(null, HolidayType.NAME_KEY);
				}// deleting Holiday
				continue;
			} // disabled
			if (hd == null) {
				if (type == null) {
					String name = (String) dict.valueForKey("name");
					if (name == null)
						continue;
					type = (HolidayType) EOUtilities.createAndInsertInstance(
							ec, HolidayType.ENTITY_NAME);
					type.setName(name);
				} // type == null (creating new)
				hd = (Holiday) EOUtilities.createAndInsertInstance(ec,Holiday.ENTITY_NAME);
				hd.addObjectToBothSidesOfRelationshipWithKey(type, Holiday.HOLIDAY_TYPE_KEY);
				if(notGlobal())
					hd.setListName(listName);
			}// hd == null (creating new)
			hd.setBegin(MyUtility.validateDateInEduYear(begin, year, "begin"));
			hd.setEnd(MyUtility.validateDateInEduYear(end, year, "end"));
			// update type
			Calendar cal = Calendar.getInstance();
			cal.setTime(begin);
			int month = cal.get(Calendar.MONTH);
			month += 12 * (cal.get(Calendar.YEAR) - year.intValue());
			type.setBeginMonth(new Integer(month));
			type.setBeginDay(new Integer(cal.get(Calendar.DAY_OF_MONTH)));
			cal.setTime(end);
			month = cal.get(Calendar.MONTH);
			month += 12 * (cal.get(Calendar.YEAR) - year.intValue());
			type.setEndMonth(new Integer(month));
			type.setEndDay(new Integer(cal.get(Calendar.DAY_OF_MONTH)));
			
			totalDays += hd.days();
			list[i] = hd;
		}
    	_list = new NSMutableArray(list);
    	selected = false;
    	EOSortOrdering.sortArrayUsingKeyOrderArray(_list, Holiday.sorter);
    }
    
    protected NSMutableDictionary _newDict;
    public NSKeyValueCoding newDict() {
    	if(_newDict == null)
    		_newDict = new NSMutableDictionary();
    	return _newDict;
    }
    
    public boolean add() {
    	if(_newDict == null)
    		return false;
    	NSTimestamp begin = (NSTimestamp)_newDict.valueForKey(Holiday.BEGIN_KEY);
    	if( begin == null || _newDict.valueForKey("name") == null)
    		return false;
    	NSTimestamp end = (NSTimestamp)_newDict.valueForKey(Holiday.END_KEY);
    	if(end == null) {
    		end = begin;
    		_newDict.takeValueForKey(end,Holiday.END_KEY);
    	}
		_newDict.takeValueForKey(EOPeriod.Utility.countDays(begin, end), "days");
    	list().addObject(_newDict);
    	EOSortOrdering.sortArrayUsingKeyOrderArray(_list, Holiday.sorter);
    	_newDict = null;
    	return true;
    }
    
	protected NSMutableArray holidaysListForYear(Boolean existing) {
		EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
		totalDays = 0;
		NSArray types = EOUtilities.objectsForEntityNamed(ec, HolidayType.ENTITY_NAME);
		if(types == null || types.count() == 0)
			return new NSMutableArray();
		Integer year = (Integer)session().valueForKey("eduYear");
//		String listName = (String)valueForBinding("listName");
		Enumeration enu = types.objectEnumerator();
		NSMutableArray result = new NSMutableArray();
		while (enu.hasMoreElements()) {
			HolidayType type = (HolidayType) enu.nextElement();
			NSKeyValueCoding holiday = null;
			NSArray holidays = type.holidays();
			if(holidays != null && holidays.count() > 0 && 
					(existing == null || existing.booleanValue())) {
				holidays = EOSortOrdering.sortedArrayUsingKeyOrderArray(holidays, 
						Holiday.sorter);
				Enumeration henu = holidays.objectEnumerator();
				while (henu.hasMoreElements()) {
					Holiday hd = (Holiday) henu.nextElement();
					if(hd.listName() != null && !hd.listName().equals(listName)) {
						holidays = NSArray.EmptyArray;
						continue;
					}
					if(year.equals(MyUtility.eduYearForDate(hd.begin()))) {
						holiday = hd;
						totalDays += hd.days();
						result.addObject(hd);
					}
				}
			}
			if(holiday == null && (existing == null || !existing.booleanValue())) {
				holiday = preset(type, year);
				if(notGlobal() || holidays == NSArray.EmptyArray)
					holiday.takeValueForKey(Boolean.TRUE, "disabled");
				result.addObject(holiday);
			}
		}
		if(result.count() > 1)
			EOSortOrdering.sortArrayUsingKeyOrderArray(result, Holiday.sorter);
		return result;
	}
	
	public static NSMutableDictionary preset(HolidayType type, Integer year) {
		NSMutableDictionary holiday = new NSMutableDictionary(type,Holiday.HOLIDAY_TYPE_KEY);
		NSTimestamp begin = HolidayType.dateFromPreset(year, type.beginMonth(), type.beginDay());
		holiday.takeValueForKey(begin, Holiday.BEGIN_KEY);
		NSTimestamp end = HolidayType.dateFromPreset(year, type.endMonth(), type.endDay());
		holiday.takeValueForKey(end, Holiday.END_KEY);
		holiday.takeValueForKey(EOPeriod.Utility.countDays(begin, end), "days");
		return holiday;
	}
	
	public String crossStyle() {
		if(item == null)
			return null;
		if(Various.boolForObject(item.valueForKey("disabled")))
			return "display:none;";
		else
			return null;
	}
	
	public String plusStyle() {
		if(item == null)
			return null;
		if(Various.boolForObject(item.valueForKey("disabled")))
			return "color:#009900;";
		else
			return "color:#009900;display:none;";
	}
	
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	Object ln = valueForBinding("listName");
    	boolean reset = ((ln==null)?listName!=null:!ln.equals(listName));
    	if(!reset)
    		reset = Various.boolForObject(valueForBinding("shouldReset"));
    	if(reset) {
    		listName = (String)ln;
    		_list = null;
    		_newDict = null;
    		_notGlobal = null;
    		item = null;
    		selected = false;
    	}
    	super.appendToResponse(aResponse, aContext);
    }
    
    public boolean synchronizesVariablesWithBindings() {
        return false;
	}
}