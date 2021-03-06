// SettingsByCourse.java: Class file for WO Component 'SettingsByCourse'

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

import net.rujel.base.QualifiedSetting;
import net.rujel.base.ReadAccess;
import net.rujel.base.SchoolSection;
import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EduCourse;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class SettingsByCourse extends WOComponent {
	protected NSArray _byCourse;
	public EOEnterpriseObject item;
//	public EOEditingContext ec;
	public Object selector;
	protected SettingsBase base;
	public Integer rowspan;
	public NSMutableArray editors;
	public Boolean nextSection;
	
    public SettingsByCourse(WOContext context) {
        super(context);
    }
/*
    public String key() {
    	if(base == null)
    		return null;
    	return base.key();
    }
    
    public void setKey(String key) {
    	if(base == null || ! base.key().equals(key)) {
    		if(ec == null)
    			ec = (EOEditingContext)parent().valueForKey("ec");
    		base = SettingsBase.baseForKey(key, ec, false);
    	}
    }*/
    
    public SettingsBase base() {
    	if(hasBinding("base")) {
    		SettingsBase bound = (SettingsBase)valueForBinding("base");
    		if(bound == base)
    			return base;
    		if(bound != null) {
    			reset();
    			base = bound;
    			return base;
    		}
    	}
    	String key = (String)valueForBinding("key");
    	if(base != null && base.key().equals(key))
    		return base;
    	reset();
    	if(base == null && hasBinding("key")) {
    		EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
    		if(ec == null)
    			ec = (EOEditingContext)parent().valueForKey("ec");
    		base = SettingsBase.baseForKey(key, ec,
    				Various.boolForObject(valueForBinding("create")));
    	}
    	return base;
    }
    
	public NSArray byCourse() {
		String sel = (String)valueForBinding("selector");
		if(sel != null) {
			Object val = valueForBinding(sel);
	    	if(_byCourse != null && ((val == null)?selector == null : val.equals(selector)))
	    		return _byCourse;
			selector = val;
	    	_byCourse = base().settingUsage(sel, val, session().valueForKey("eduYear"));
	    	if(sel.equals(SettingsBase.TEXT_VALUE_KEY))
	    		sel = SettingsBase.NUMERIC_VALUE_KEY;
	    	else
	    		sel = SettingsBase.TEXT_VALUE_KEY;	
	    	if(canSetValueForBinding(sel)) {
	    		if(_byCourse.count() > 0) {
	    			EOEnterpriseObject bc = (EOEnterpriseObject)_byCourse.objectAtIndex(0);
	    			val = bc.valueForKey(sel);
	    		} else {
	    			val = null;
	    		}
    			setValueForBinding(val, sel);
	    	}
		}
		if(hasBinding("editList")) {
			if(sel == null)
				_byCourse = (NSArray)valueForBinding("editList");
			else
				setValueForBinding(_byCourse, "editList");
		}
		if(_byCourse == null) {
			if(base() != null)
				_byCourse = base().byCourseSorted(
						(Integer)session().valueForKey("eduYear"));
			if(_byCourse == null)
				_byCourse = new NSMutableArray();
		}
		return _byCourse;
	}
    
	public Boolean hideDetails() {
		rowspan = null;
		if(!Various.boolForObject(valueForBinding("hideEmptyDetails")))
			return Boolean.FALSE;
		int cnt = byCourse().count();
		if (cnt > 1)
			return Boolean.FALSE;
		if(cnt == 0)
			return Boolean.TRUE;
		return Boolean.valueOf(valueForBinding("selector") != null);
	}
	
	public String editorHead() {
    	if(Various.boolForObject(valueForBinding("readOnly")))
    		return null;
    	NamedFlags access = (NamedFlags)valueForBinding("access");
    	if(access == null)
    		access = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.QualifiedSetting");
    	if(access.flagForKey("edit") || access.flagForKey("delete")) {
    		if(access.flagForKey("edit") && access.flagForKey("delete"))
    			return "<td colspan = \"2\"></td>";
    		else
    			return "<td />";
    	}
    	return null;
	}
	
	public String colspan() {
    	if(Various.boolForObject(valueForBinding("readOnly")))
    		return "4";
    	NamedFlags access = (NamedFlags)valueForBinding("access");
    	if(access == null)
    		access = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.QualifiedSetting");
    	if(access.flagForKey("edit") || access.flagForKey("delete")) {
    		if(access.flagForKey("edit") && access.flagForKey("delete"))
    			return "6";
    		else
    			return "5";
    	}
    	return "4";
	}
	
    public WOActionResults addByCourse() {
    	WOComponent editor = pageWithName("ByCourseEditor");
    	editor.takeValueForKey(context().page(), "returnPage");
    	editor.takeValueForKey(_byCourse, "editList");
    	editor.takeValueForKey(base(), "base");
    	if(hasBinding("textValue")) {
    		Object value = valueForBinding("textValue");
    		if(value == null)
    			value = NullValue;
    		editor.takeValueForKeyPath(value, "tmpValues.textValue");
    	}
    	if(hasBinding("numericValue")) {
    		Object value = valueForBinding("numericValue");
    		if(value == null)
    			value = NullValue;
    		editor.takeValueForKeyPath(value, "tmpValues.numericValue");
    	}
    	if(hasBinding("pushByCourse")) {
    		editor.takeValueForKey(this, "resultGetter");
    		editor.takeValueForKey("^pushByCourse", "pushToKeyPath");
    	}
		editor.takeValueForKey(null, "byCourse");
    	return editor;
    }
    
    public void setItem(EOEnterpriseObject obj) {
    	if(obj instanceof QualifiedSetting) {
    		SchoolSection nSect = ((QualifiedSetting)obj).section();
    		if(nSect == null) {
    			nextSection = Boolean.FALSE;
    		} else {
    			if(item instanceof QualifiedSetting) {
    	    		SchoolSection pSect = ((QualifiedSetting)item).section();
    	    		nextSection = Boolean.valueOf(pSect == null || pSect != nSect);
    			} else {
    				nextSection = Boolean.TRUE;
    			}
    		}
    	} else {
    		nextSection = null;
    	}
    	item = obj;
    	if(canSetValueForBinding("item"))
    		setValueForBinding(item, "item");
    	if(canSetValueForBinding("itemAccess"))
    		setValueForBinding(access(), "itemAccess");
    }
    
    public Boolean canEdit() {
    	if(Various.boolForObject(valueForBinding("readOnly")))
    		return Boolean.FALSE;
    	NamedFlags access = (NamedFlags)valueForBinding("access");
    	if(access != null)
    		return Boolean.valueOf(access.flagForKey("create"));
    	else
    		return (Boolean)session().valueForKeyPath("readAccess.create.QualifiedSetting");
    }

	public Boolean cantSetBase() {
		if(!Various.boolForObject(valueForBinding("canSetBase")))
			return Boolean.TRUE;
		if(Various.boolForObject(valueForBinding("readOnly")))
			return Boolean.TRUE;
		NamedFlags access = (NamedFlags)valueForBinding("access");
		if(access != null && !access.flagForKey("edit"))
			return Boolean.TRUE;
		String sel = (String)valueForBinding("selector");
		if(sel == null)
			return Boolean.TRUE;
		Object val = valueForBinding(sel);
		Object bs = base().valueForKey(sel);
		if((val==null)?bs==null:val.equals(bs))
			return Boolean.TRUE;
		ReadAccess readAccess = (ReadAccess)session().valueForKey("readAccess");
		return (Boolean)readAccess.cachedAccessForObject(
				"SettingsBase", (Integer)null).valueForKey("_edit");
//		return (Boolean)session().valueForKeyPath("readAccess._edit.SettingsBase");
	}
	
	public WOActionResults makeBase() {
//		String sel = (String)valueForBinding("selector");
//		base().takeValueForKey(valueForBinding(sel), sel);
    	if(hasBinding("textValue"))
    		base().takeValueForKey(valueForBinding("textValue"), "textValue");
    	if(hasBinding("numericValue"))
    		base().takeValueForKey(valueForBinding("numericValue"), "numericValue");
		EOEditingContext ec = base().editingContext();
		try {
			ec.saveChanges();
//			((NSMutableArray)_byCourse).insertObjectAtIndex(base(), 0);
			ByCourseEditor.logger.log(WOLogLevel.COREDATA_EDITING,"Changed BaseSettings",
					new Object[] {session(),base()});
		} catch (Exception e) {
			ec.revert();
			session().takeValueForKey(e.getMessage(), "message");
			ByCourseEditor.logger.log(WOLogLevel.WARNING,"Error modifying BaseSettings",
					new Object[] {session(),base(),e});
		}
		return null;
	}
	
	public Boolean canSort() {
		if(Various.boolForObject(valueForBinding("cantSort")) || 
				valueForBinding("selector") != null)
			return Boolean.FALSE;
		if(byCourse() == null || byCourse().count() < 2)
			return Boolean.FALSE;
		return (Boolean)session().valueForKeyPath("readAccess.edit.QualifiedSetting");
 	}
	
	public WOActionResults saveSetting() {
		EOEditingContext ec = (EOEditingContext)valueForBinding("ec");
		if(!ec.hasChanges())
			return null;
		try {
			ec.saveChanges();
			ByCourseEditor.logger.log(WOLogLevel.CONFIG,"Saved configuration for '" + 
					base.key() + '\'', new Object[] {session(),base()});
		} catch (Exception e) {
			session().takeValueForKey(e.getMessage(), "message");
			ByCourseEditor.logger.log(WOLogLevel.WARNING,"Failed saving configuration for '" + 
					base.key() + '\'', new Object[] {session(),base(),e});
			ec.revert();
		}
		return null;
	}
	
    public boolean omitCell() {
    	return hasBinding("rowspan") || !hasBinding("title");
    }
    
    public void setRowspan(Integer value) {
    	if(canSetValueForBinding("rowspan"))
    		setValueForBinding(value, "rowspan");
    	rowspan = value;
    }
    
    public String cellClass() {
    	if(selector instanceof EduCourse) {
    		if(item == null || item == base) {
    			if(base.forCourse((EduCourse)selector) == base)
    				return "selection";
    		} else if (((QualifiedSetting)item).evaluateWithObject((EduCourse)selector)) {
    			if(item == base.forCourse((EduCourse)selector))
    				return "selection";
    			return "highlight";
    		}
    	}
    	return null;
    }
    
    public String title() {
    	Object binding = valueForBinding("title");
    	if(!hideDetails())
    		setItem(null);
    	if(binding == null)
    		return null;
    	if(binding instanceof CharSequence) {
    		String title = binding.toString();
    		if(title.charAt(0) == '<')
    			return title;
    		return "<th>" + title + "</th>";
    	}
    	if(binding instanceof NSArray) {
    		StringBuilder buf = new StringBuilder();
    		Enumeration enu = ((NSArray)binding).objectEnumerator();
    		while (enu.hasMoreElements()) {
				Object object = enu.nextElement();
				buf.append("<th>").append(object).append("</th>");
			}
    		return buf.toString();
    	}
    	return binding.toString();
    }
    
    public Boolean showBase() {
    	String sel = (String)valueForBinding("selector");
    	if(sel == null) {
			setItem(base());
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
		/*
    	Object val = valueForBinding(sel);
    	Object bVal = base().valueForKey(sel);
    	return Boolean.valueOf((val==null)?bVal==null:val.equals(bVal));*/
    }
    
    public WOActionResults testCourse() {
    	WOComponent result = CourseSelector.selectorPopup(context().page(), this,
    			"selector", base.editingContext());
    	result.takeValueForKey(session().valueForKeyPath(
    			"strings.RujelBase_Base.SettingsBase.testSettings"), "title");
    	return result;
    }
    
    public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public boolean isStateless() {
		return false;
	}
	
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
//		if(Various.boolForObject(valueForBinding("readOnly")))
			_byCourse = null;
    	if(hasBinding("rowspan"))
    		setValueForBinding(rowspan, "rowspan");
    	if (selector instanceof EduCourse)
    		setItem(base().forCourse((EduCourse)selector));
    	else
    		setItem(null);
		super.appendToResponse(aResponse, aContext);
		setItem(null);
		if (selector instanceof EduCourse)
			selector = null;
	}
	
	public void reset() {
		super.reset();
		base = null;
		_byCourse = null;
		item = null;
		rowspan = null;
		editors = null;
		nextSection = null;
		setValueForBinding(null, "item");
	}

	public Integer sectionColspan() {
		int count = 4;
    	Object title = valueForBinding("title");
    	if(title instanceof CharSequence) {
    		String text = title.toString();
    		if(text.charAt(0) == '<'){
    			String[] split = text.split("<t[dh][\\s>]");
    			count += split.length;
    		} else {
    			count++;
    		}
    	}
    	if(canSort())
    		count++;
    	if(title instanceof NSArray) {
    		count+= ((NSArray)title).count();
    	}
    	if(!Various.boolForObject(valueForBinding("readOnly"))) {
    		/*NamedFlags access = (NamedFlags)valueForBinding("access");
    		if(access == null)
    			access = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.QualifiedSetting");
    		if(access.flagForKey("edit"))
    			count++;
    		if(access.flagForKey("delete"))
    			count++;*/
    		count += 2;
    	}
		return Integer.valueOf(count);
	}
	
	public NamedFlags access() {
		NamedFlags access = (NamedFlags)valueForBinding("access");
		if(access != null)
			return access;
		ReadAccess readAccess = (ReadAccess)session().valueForKey("readAccess");
		String checkAccess = (String)valueForBinding("checkAccess");
		Integer section = null;
		if(item instanceof QualifiedSetting) {
			section = (Integer)item.valueForKeyPath("section.sectionID");
			if(checkAccess == null)
				checkAccess = QualifiedSetting.ENTITY_NAME;		}
		if(checkAccess == null)
			checkAccess = SettingsBase.ENTITY_NAME;
		return readAccess.cachedAccessForObject(checkAccess, section);
	}
}