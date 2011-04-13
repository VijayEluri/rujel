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
    
    public WOActionResults save() {
    	if(dict.count() == 0)
    		return null;
    	EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
    	Holiday hd = (Holiday)dict.removeObjectForKey("selection");
    	if(hd == null) {
    		hd = (Holiday)EOUtilities.createAndInsertInstance(ec, Holiday.ENTITY_NAME);
    		hd.takeValuesFromDictionary(dict);
    		if(hd.end() == null)
    			hd.setEnd(hd.begin());
    		if(notGlobal())
    			hd.setListName(listName);
    	} else if(Various.boolForObject(dict.valueForKey("delete"))) {
    		dict.takeValueForKey(hd.listName(), Holiday.LIST_NAME_KEY);
    		ec.deleteObject(hd);
    		hd = null;
    	} else {
    		if(!hd.name().equals(dict.valueForKey(Holiday.NAME_KEY)))
    			hd.takeValueForKey(dict.valueForKey(Holiday.NAME_KEY), Holiday.NAME_KEY);
    		if(!hd.begin().equals(dict.valueForKey(Holiday.BEGIN_KEY)))
    			hd.takeValueForKey(dict.valueForKey(Holiday.BEGIN_KEY), Holiday.BEGIN_KEY);
    		if(!hd.end().equals(dict.valueForKey(Holiday.END_KEY)))
    			hd.takeValueForKey(dict.valueForKey(Holiday.END_KEY), Holiday.END_KEY);
    	}
    	if(!ec.hasChanges()) {
    		dict.removeAllObjects();
    		return null;
    	}
    	try {
     		ec.saveChanges();
     		if(hd != null)
     			EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,"Saved Holidays changes",hd);
     		else
     			EduPlan.logger.log(WOLogLevel.COREDATA_EDITING,"Deleted Holiday", 
     					new Object[] {session(),dict});
	    	_list = null;
	    	dict.removeAllObjects();
    	} catch (Exception e) {
    		session().takeValueForKey(e.getMessage(), "message");
			EduPlan.logger.log(WOLogLevel.INFO,"Error saving Holidays changes",
					new Object[] {session(),(hd==null)?dict:hd,e});
		}
    	return null;
    }
    
    public Boolean canCreate() {
    	if(dict.valueForKey("selection") != null)
    		return Boolean.FALSE;
    	return (Boolean)session().valueForKeyPath("readAccess.create.Holiday");
    }
    
    public void select() {
    	dict.takeValueForKey(item, "selection");
    	dict.takeValueForKey(item.name(), Holiday.NAME_KEY);
    	dict.takeValueForKey(item.begin(), Holiday.BEGIN_KEY);
    	dict.takeValueForKey(item.end(), Holiday.END_KEY);
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
    		_notGlobal = null;
    		item = null;
    	}
    	super.appendToResponse(aResponse, aContext);
    }
    
    public boolean synchronizesVariablesWithBindings() {
        return false;
	}
}