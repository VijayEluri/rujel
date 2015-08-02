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

import java.util.Enumeration;

import net.rujel.base.SettingsBase;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

// Generated by the WOLips Templateengine Plug-in at Jul 16, 2009 12:03:22 PM
public class HolidaysList extends com.webobjects.appserver.WOComponent {
	
	protected String listName;
	public Holiday item;
	protected NSArray _list;
	public NSMutableArray suggestions;
//	public Holiday curr;
	public int totalDays = 0;
	public NSMutableDictionary dict = new NSMutableDictionary();
	
    public HolidaysList(WOContext context) {
        super(context);
    }
    
    public NSArray list() {
    	if(_list == null) {
        	EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
    		_list = Holiday.holidaysForList(listName, ec);
    		if(listName == null)
    			return _list;
    		Object canCreate = session().valueForKeyPath("readAccess._create.Holiday");
    		if(!Various.boolForObject(canCreate)) {
    			// TODO: suggestions
    		}
    	}
    	return _list;
    }
    
    public boolean selected() {
    	return (item == dict.valueForKey("selection"));
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
    		if(item.valueForKey(Holiday.LIST_NAME_KEY) != null)
    			return "female";
    		return "gerade";
    }
    
    private static final String[] keys = new String[] {
    	Holiday.NAME_KEY,Holiday.BEGIN_KEY,Holiday.END_KEY};
    public WOActionResults save() {
    	if(dict.count() == 0)
    		return null;
    	EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
    	Holiday hd = (Holiday)dict.removeObjectForKey("selection");
		boolean forAll = Various.boolForObject(dict.removeObjectForKey("forAll"));
		SettingsBase base = SettingsBase.baseForKey(EduPeriod.ENTITY_NAME, ec, false);
    	if(hd == null) { // creating
    		hd = (Holiday)EOUtilities.createAndInsertInstance(ec, Holiday.ENTITY_NAME);
    		hd.takeValuesFromDictionary(dict);
    		if(hd.end() == null)
    			hd.setEnd(hd.begin());
    		if(!forAll)
    			hd.setListName(listName);
    		
    	} else if(Various.boolForObject(dict.removeObjectForKey("delete"))) { // deleting
    		String hdList = hd.listName();
    		if(hdList != null || forAll) {
    			ec.deleteObject(hd);
    		} else {
        		Integer eduYear = (Integer)session().valueForKey("eduYear");
    			NSMutableArray names = (NSMutableArray) base.availableValues(eduYear, 
        				SettingsBase.TEXT_VALUE_KEY);
    			if(names.count() < 2) {
    				ec.deleteObject(hd);
    			} else {
    				Enumeration enu = names.objectEnumerator();
    				while (enu.hasMoreElements()) {
    					String ln = (String)enu.nextElement();
    					if(ln.equals(listName))
    						continue;
    					if(hd == null) {
    						hd = (Holiday)EOUtilities.createAndInsertInstance(ec,
    								Holiday.ENTITY_NAME);
    						hd.takeValuesFromDictionary(dict);
    					}
    					hd.setListName(ln);
    					hd = null;
    				}
    			}

    		}
    		dict.takeValueForKey(hdList, Holiday.LIST_NAME_KEY);
    		if(notGlobal())
    			dict.takeValueForKey(listName, "listName");
    		hd = null;
    		
    	} else { // editing
        	for (int i = 0; i < keys.length; i++) {
        		Object hdValue = hd.valueForKey(keys[i]);
        		Object dictValue = dict.valueForKey(keys[i]);
        		if(!hdValue.equals(dictValue)) {
            		if(hd.listName() == null && (!forAll || notGlobal())) {
                		hd = (Holiday)EOUtilities.createAndInsertInstance(ec, Holiday.ENTITY_NAME);
                		hd.takeValuesFromDictionary(dict);
            			hd.setListName(listName);
            			break;
            		} else {
            			hd.takeValueForKey(dictValue, keys[i]);
            		}
        		}
    		}
    	}
    	if(ec.hasChanges()) {
    	try {
     		ec.saveChanges();
     		if(hd != null)
     			EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,"Saved Holidays changes",hd);
     		else
     			EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,"Deleted Holiday", 
     					new Object[] {session(),dict});
	    	_list = null;
    	} catch (Exception e) {
    		session().takeValueForKey(e.getMessage(), "message");
			EduPlan.logger.log(WOLogLevel.INFO,"Error saving Holidays changes",
					new Object[] {session(),(hd==null)?dict:hd,e});
			ec.revert();
			return null;
		}
    	}
    	if(forAll || (hd != null && hd.listName() != null)) {
    		if(forAll) {
    			_list = EOUtilities.objectsMatchingKeyAndValue(ec, Holiday.ENTITY_NAME, 
    					Holiday.NAME_KEY, dict.valueForKey(Holiday.NAME_KEY));
    		} else {
    			_list = EOUtilities.objectsMatchingValues(ec, Holiday.ENTITY_NAME, dict);
    		}
    		Integer eduYear = (Integer)session().valueForKey("eduYear");
    		NSMutableArray names = (NSMutableArray) base.availableValues(eduYear, 
    				SettingsBase.TEXT_VALUE_KEY);
    		if(forAll || _list.count() >= names.count()) {
    			Enumeration enu = _list.objectEnumerator();
    			String baseLN = base.textValue();
    			while (enu.hasMoreElements()) {
    				Holiday holiday = (Holiday) enu.nextElement();
    				String ln = holiday.listName();
    				if(ln == null) {
    					if(baseLN != null) {
    						baseLN = null;
    						continue;
    					}
    				} else if(!names.removeObject(ln))
    					continue;
    				if(baseLN != null && baseLN.equals(ln)) {
    					hd = holiday;
    					hd.setListName(null);
    					baseLN = null;
    				} else {
    					ec.deleteObject(holiday);
    				}
    			}
    			if(!forAll && names.count() > 0) {
    				ec.revert();
    			} else {
    				try {
    					ec.saveChanges();
    					EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,
    							"Merging same Holidays in all regimes", hd);
    				} catch (Exception e) {
    					EduPlan.logger.log(WOLogLevel.INFO,
    							"Failed merging same Holidays in all regimes",
    							new Object[] {session(),hd,e});
    					ec.revert();
    				}
    			}
    		} // (_list.count() >= names.count())
    		_list = null;
    	}
    	dict.removeAllObjects();
		dict.takeValueForKey(Boolean.TRUE, "forAll");
    	return null;
    }
    
    public Boolean canCreate() {
    	if(dict.valueForKey("selection") != null)
    		return Boolean.FALSE;
    	return (Boolean)session().valueForKeyPath("readAccess.create.Holiday");
    }
    
    public void select() {
    	dict.takeValueForKey(item, "selection");
    	for (int i = 0; i < keys.length; i++) {
        	dict.takeValueForKey(item.valueForKey(keys[i]), keys[i]);
		}
    	dict.takeValueForKey(Boolean.valueOf(!notGlobal() && 
    			item.valueForKey(Holiday.LIST_NAME_KEY) == null), "forAll");
    	dict.removeObjectForKey("delete");
    }
	
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	Object ln = valueForBinding("listName");
    	boolean reset = ((ln==null)?listName!=null:!ln.equals(listName));
    	if(!reset)
    		reset = Various.boolForObject(valueForBinding("shouldReset"));
    	if(reset) {
    		listName = (String)ln;
    		_list = null;
    		dict.removeAllObjects();
    		dict.takeValueForKey(!notGlobal(), "forAll");
    		_notGlobal = null;
    		item = null;
    	}
    	super.appendToResponse(aResponse, aContext);
    }
    
    public boolean synchronizesVariablesWithBindings() {
        return false;
	}
}