// ReportTable.java: Class file for WO Component 'ReportTable'

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

package net.rujel.reports;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import net.rujel.reusables.DisplayAny;
import net.rujel.reusables.Various;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.*;
import com.webobjects.appserver.WOActionResults;

// Generated by the WOLips Templateengine Plug-in at Apr 10, 2009 9:49:59 AM
public class ReportTable extends com.webobjects.appserver.WOComponent {
	public NSArray list;
	public NSArray properties;
	public NSKeyValueCodingAdditions itemDict;
	public Object itemRow;
	public Object item;
	public NSKeyValueCodingAdditions subDict;
	public Object extra;
	public String filenameFormatter;

	public NSKeyValueCodingAdditions valueOf = new DisplayAny.ValueReader(this);;
	
	public ReportTable(WOContext context) {
        super(context);
    }

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		setItemRow(null);
		setItemDict(null);
		if(parent() != null) {
			list = (NSArray)valueForBinding("list");
			properties = (NSArray)valueForBinding("properties");
			if(hasBinding("index"))
				setValueForBinding(null, "index");
			super.appendToResponse(aResponse, aContext);
		} else { // exporting
			if(extra != null) {
				if(extra instanceof NSArray) {
					properties = (properties == null)?(NSArray)extra:
							properties.arrayByAddingObjectsFromArray((NSArray)extra);
				} else if (extra instanceof NSKeyValueCodingAdditions) {
					properties = (properties == null)? new NSArray(extra):
						properties.arrayByAddingObject(extra);
				}
			}
			if(properties == null || properties.count() == 0)
				return;
			appendRowToResponse(aResponse,aContext);
			if(list == null || list.count() == 0)
				return;
			Enumeration rows = list.objectEnumerator();
			while (rows.hasMoreElements()) {
				itemRow = (NSKeyValueCodingAdditions) rows.nextElement();
				if((itemRow instanceof EOEnterpriseObject) && 
						((EOEnterpriseObject)itemRow).editingContext() == null)
					continue;
				appendRowToResponse(aResponse,aContext);
			} // rows
	    	aResponse.setHeader("application/octet-stream","Content-Type");
	    	StringBuffer buf = new StringBuffer("attachment; filename=\"");
//	    	String filenameFormatter = (export == null)?null:
//	    				(String)export.valueForKey("filenameFormatter");
	    	try {
	    		SimpleDateFormat sdf = new SimpleDateFormat(
	    				(filenameFormatter == null) ? "yyyyMMdd" : filenameFormatter);
	    		sdf.format(new Date(), buf, new FieldPosition(SimpleDateFormat.YEAR_FIELD));
	    	} catch (Exception e) {
	    		buf.append(filenameFormatter);
	    	}
			buf.append(".csv\"");
			aResponse.setHeader(buf.toString(),"Content-Disposition");
		} // end exporting
	}
	
	protected void appendRowToResponse(WOResponse aResponse, WOContext aContext) {
		Enumeration props = properties.objectEnumerator();
		while (props.hasMoreElements()) {
			NSKeyValueCodingAdditions tmp = (NSKeyValueCodingAdditions)props.nextElement();
			if(Various.boolForObject(tmp.valueForKey("skipExport")))
				continue;
			setItemDict(tmp);
			tmp = (NSKeyValueCodingAdditions)itemDict.valueForKey("exportDict");
			if(tmp != null)
				itemDict = tmp;
			NSArray subParams = (NSArray)itemDict.valueForKey("subParams");
			if(subParams == null) { // no subs
				aResponse.appendContentCharacter('"');
				if(itemRow == null) {
					Object value = valueOf.valueForKeyPath("item.itemDict.title");
					if(value != null)
						aResponse.appendContentString(value.toString());
				} else {
					NSDictionary bindings = new NSDictionary(
							new Object[] {WOAssociation.associationWithValue(item),
									WOAssociation.associationWithValue(itemDict)},
									new String[] {"value","dict"});
					WOElement display = application().dynamicElementWithName(
							"DisplayAny", bindings, null, null);
					display.appendToResponse(aResponse, aContext);
				}
				aResponse.appendContentCharacter('"');
			} else { // subs
				Enumeration subs = subParams.objectEnumerator();
				while (subs.hasMoreElements()) { // subs
					subDict = (NSKeyValueCodingAdditions) subs.nextElement();
					aResponse.appendContentCharacter('"');
					if(Various.boolForObject(subDict.valueForKey("skipExport")))
						continue;
					tmp = (NSKeyValueCodingAdditions)subDict.valueForKey("exportDict");
					if(tmp != null)
						subDict = tmp;
					if(itemRow == null) {
						Object value = valueOf.valueForKeyPath("item.subDict.title");
						if(value != null)
							aResponse.appendContentString(value.toString());
					} else {
						Object value = item;
						if(subDict.valueForKey("value") != null)
							value = valueOf.valueForKeyPath("item.subDict.value");
						NSDictionary bindings = new NSDictionary(
								new Object[] {WOAssociation.associationWithValue(value),
										WOAssociation.associationWithValue(subDict)},
										new String[] {"value","dict"});
						WOElement display = application().dynamicElementWithName(
								"DisplayAny", bindings, null, null);
						display.appendToResponse(aResponse, aContext);
					}
					aResponse.appendContentCharacter('"');
					if(subs.hasMoreElements())
						aResponse.appendContentCharacter(',');
				}
			} // subs
			if(props.hasMoreElements())
				aResponse.appendContentCharacter(',');
		} // properties.objectEnumerator()
		aResponse.appendContentCharacter('\r');
	}
	
	public Object nextValue() {
		if(itemDict == null)
			return null;
		Enumeration valuesEnumeration = (Enumeration)itemDict.valueForKey("valuesEnumeration");
		if(valuesEnumeration.hasMoreElements())
			return valuesEnumeration.nextElement();
		return null;
	}

	public String rowID() {
		if(itemRow == null)
			return null;
    	if(itemRow.equals(valueForBinding("currObject")))
    		return "curr";
    	return null;
    }
	
	public void setItemRow(Object nextRow) {
		itemRow = nextRow;
    	setValueForBinding(itemRow, "itemRow");
	}
	
    public void setItemDict(NSKeyValueCodingAdditions newDict) {
    	itemDict = newDict;
    	item = valueFromDict(itemDict,itemRow,this);
    	if(parent() != null) {
    		setValueForBinding(itemDict, "itemDict");
    		setValueForBinding(item, "item");
    	}
    }	
	
    public static Object valueFromDict(NSKeyValueCodingAdditions itemDict, 
    		Object itemRow,WOComponent page) {
    	Object item = null;
    	if(itemRow == null || itemDict == null) {
    		item = null;
    	} else if(itemDict.valueForKey("value") != null) {
    		item = DisplayAny.ValueReader.evaluateValue(itemDict.valueForKey("value"),
    				itemRow, page); //valueOf.valueForKeyPath("itemRow.itemDict.value");
    	} else {
    		String keyPath = (String)itemDict.valueForKey("keyPath");
    		if(keyPath == null || keyPath.equals("."))
    			item = itemRow;
    		else
    			item = NSKeyValueCodingAdditions.Utility.valueForKeyPath(itemRow, keyPath);
    	}
    	return item;
     }
    
/*    public void setFilenameFormatter(String value) {
    	if(export == null)
    		export = new NSMutableDictionary(value,"filenameFormatter");
    	else
    		export.takeValueForKey(value, "filenameFormatter");
    }*/
	
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	public void reset() {
		list = null;
		properties = null;
		itemRow = null;
		item = null;
		super.reset();
	}

	public String onClick() {
		if(hasBinding("onClick"))
			return (String)valueForBinding("onClick");
		if(subDict != null)
			return (String)valueOf.valueForKeyPath("item.subDict.onclick");
		return (String)valueOf.valueForKeyPath("item.itemDict.onclick");
	}
	
	public WOActionResults select() {
		if(hasBinding("selectAction"))
			return (WOActionResults) valueForBinding("selectAction");
		if(itemDict instanceof NSMutableDictionary)
			DisplayAny.ValueReader.clearResultCache(
					(NSMutableDictionary)itemDict, itemRow, true);
		String nextPage = (String)valueForKeyPath("subDict.nextPage");
		NSKeyValueCodingAdditions pageDict = (nextPage==null)?itemDict:subDict;
		if(nextPage == null)
			nextPage = (String)valueForKeyPath("itemDict.nextPage");
		if(nextPage != null) {
			WOComponent result = pageWithName(nextPage);
			boolean popup = Various.boolForObject(pageDict.valueForKey("popup"));
			if(popup) {
				result.takeValueForKey(context().page(), "returnPage");
			} else {
				session().takeValueForKey(context().page(),"pushComponent");
			}
			pageDict = (NSDictionary)pageDict.valueForKey("pageParams");
			Enumeration enu = ((NSDictionary)pageDict).keyEnumerator();
			while (enu.hasMoreElements()) {
				String key = (String) enu.nextElement();
				Object value = DisplayAny.ValueReader.evaluateValue(
						pageDict.valueForKey(key), "item", this);
				result.takeValueForKey(value, key);
			}

			return result;
		}
		if(valueForKeyPath("subDict.invokeAction") != null)
			return (WOActionResults)valueOf.valueForKeyPath("item.subDict.invokeAction");
		return (WOActionResults)valueOf.valueForKeyPath("item.itemDict.invokeAction");
	}
	
	public Boolean disabledClick() {
		if(Various.boolForObject(valueForBinding("disabled")))
			return Boolean.TRUE;
		if(subDict != null) {
			if(Various.boolForObject(valueOf.valueForKeyPath("item.subDict.disabled")))
				return Boolean.TRUE;
		}
		if(Various.boolForObject(valueOf.valueForKeyPath("item.itemDict.disabled")))
			return Boolean.TRUE;
		if(onClick() != null)
			return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	public String cellClass() {
		if(hasBinding("cellClass"))
			return (String)valueForBinding("cellClass");
		if(subDict != null) {
			String result = (String)valueOf.valueForKeyPath("item.subDict.class");
			if(result != null)
				return result;
		}
		return (String)valueOf.valueForKeyPath("item.itemDict.class");
	}
	
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		setItemRow(null);
		setItemDict(null);
		if(hasBinding("index"))
			setValueForBinding(null, "index");
		list = (NSArray)valueForBinding("list");
		properties = (NSArray)valueForBinding("properties");
		return super.invokeAction(aRequest, aContext);
	}
	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		setItemRow(null);
		setItemDict(null);
		if(hasBinding("index"))
			setValueForBinding(null, "index");
		list = (NSArray)valueForBinding("list");
		properties = (NSArray)valueForBinding("properties");
		super.takeValuesFromRequest(aRequest, aContext);
	}
}