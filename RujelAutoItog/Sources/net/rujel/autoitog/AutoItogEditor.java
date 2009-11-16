// AutoItogEditor.java: Class file for WO Component 'AutoItogEditor'

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

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.rujel.base.MyUtility;
import net.rujel.base.SettingsBase;
import net.rujel.criterial.BorderSet;
import net.rujel.eduresults.ItogContainer;
import net.rujel.eduresults.ItogMark;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.PerPersonLink;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

// Generated by the WOLips Templateengine Plug-in at Sep 11, 2009 11:25:34 AM
public class AutoItogEditor extends com.webobjects.appserver.WOComponent {
	
	public static final Logger logger = Logger.getLogger("rujel.autoitog");
	
	public WOComponent returnPage;
	public ItogContainer itog;
	public AutoItog autoItog;
	public String listName;
	
	public NSArray bsets;
	public NSArray calculators;
	public Object item;
	public NSDictionary calc;
	public String fireDate;
	public String fireTime;
	public BorderSet borderSet;
	public NamedFlags namedFlags;
	
	public Boolean cantChange;
	
    public AutoItogEditor(WOContext context) {
        super(context);
        calculators = (NSArray)context.session().valueForKeyPath(
						"strings.RujelAutoItog_AutoItog.calculators");
    }
    
    public void setItog(ItogContainer itogContainer) {
    	itog = itogContainer;
    	bsets = EOUtilities.objectsForEntityNamed(
    			itog.editingContext(), BorderSet.ENTITY_NAME);
    }

    public void setAutoItog(AutoItog ai) {
    	autoItog = ai;
//     	String pattern = SettingsReader.stringForKeyPath("ui.shortDateFormat","MM-dd");
    	Format format = MyUtility.dateFormat();
    	NSTimestamp dt = (ai == null)?AutoItog.defaultFireDateTime():ai.fireDate();
    	fireDate = format.format(dt);
    	format = new SimpleDateFormat("HH:mm");
    	if(ai != null)
    		dt = ai.fireTime();
    	fireTime = format.format(dt);
       	if(ai == null) {
    		namedFlags = new NamedFlags(2,AutoItog.flagNames);
    		return;
    	}
       	listName = ai.listName();
    	String pattern = autoItog.calculatorName();
    	if(pattern != null) { // resolve className to readable title
    		for (int i = 0; i < calculators.count(); i++) {
    			calc = (NSDictionary)calculators.objectAtIndex(i);
    			if(pattern.equals(calc.valueForKey("className"))) {
    				break;
    			}
    			calc = null;
    		}
    	}
    	borderSet = ai.borderSet();
    	namedFlags = ai.namedFlags();
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(autoItog == null)
    		cantChange = (Boolean)session().valueForKeyPath("readAccess._create.AutoItog");
    	else
    		cantChange = (Boolean)session().valueForKeyPath("readAccess._edit.autoItog");
    	super.appendToResponse(aResponse, aContext);
    }

    public boolean disableManual() {
    	return (calc == null || cantChange.booleanValue());
    }

    public WOActionResults save() {
    	EOEditingContext ec = itog.editingContext();
    	ec.lock();
    	try {
    		if(autoItog == null) {
    			autoItog = (AutoItog)EOUtilities.createAndInsertInstance(
    					ec,AutoItog.ENTITY_NAME);
    			autoItog.setItogContainer(itog);
    			autoItog.setListName(listName);
    		}
    		autoItog.setNamedFlags(namedFlags);
    		autoItog.setBorderSet(borderSet);
    		autoItog.setCalculatorName((String)valueForKeyPath("calc.className"));
//    		String pattern = SettingsReader.stringForKeyPath("ui.shortDateFormat","MM-dd");
    		Format format = MyUtility.dateFormat();
    		if(autoItog.fireDate() == null ||
    				!fireDate.equals(format.format(autoItog.fireDate()))) {
    			try {
    				Date dt = (Date)format.parseObject(fireDate);
    				dt = MyUtility.dateToEduYear(dt, itog.eduYear());
    				autoItog.setFireDate(new NSTimestamp(dt));
    			} catch (ParseException e) {
       				logger.log(WOLogLevel.FINE,"Error parsing fireDate " + fireDate,
    						new Object[] {session(),e});
    			}
    		}
    		format = new SimpleDateFormat("HH:mm");
    		if(autoItog.fireTime() == null ||
    				!fireDate.equals(format.format(autoItog.fireTime()))) {
    			try {
    				Date dt = (Date)format.parseObject(fireTime);
    				autoItog.setFireTime(new NSTimestamp(dt));
    			} catch (ParseException e) {
    				logger.log(WOLogLevel.FINE,"Error parsing fireTime " + fireTime,
    						new Object[] {session(),e});
    			}
    		}
    		ec.saveChanges();
    		SettingsBase base = SettingsBase.baseForKey(ItogMark.ENTITY_NAME, ec, false);
    		NSArray courses = base.coursesForSetting(listName, null, itog.eduYear());
    		Enumeration enu = courses.objectEnumerator();
    		Calculator calculator = autoItog.calculator();
    		while (enu.hasMoreElements()) {
				EduCourse course = (EduCourse) enu.nextElement();
				PerPersonLink prppl = calculator.calculatePrognoses(course, autoItog);
				if(prppl == null || prppl.count() == 0)
					continue;
				CourseTimeout cto = CourseTimeout.
						getTimeoutForCourseAndPeriod(course, itog);
				prppl.allValues().takeValueForKey(cto, "updateWithCourseTimeout");
	    		ec.saveChanges();
			}
    		logger.log(WOLogLevel.COREDATA_EDITING, "Saved AutoItog", 
    				new Object[] {session(),autoItog});
    	} catch (Exception e) {
    		logger.log(WOLogLevel.WARNING, "Error saving AutoItog", 
    				new Object[] {session(),autoItog,e});
    		session().takeValueForKey(e.getMessage(), "message");
    	} finally {
    		ec.unlock();
    	}
    	returnPage.ensureAwakeInContext(context());
    	return returnPage;
    }

    public WOActionResults delete() {
    	EOEditingContext ec = itog.editingContext();
    	ec.lock();
    	try {
    		ec.deleteObject(autoItog);
       		logger.log(WOLogLevel.COREDATA_EDITING,
       				"Deleted AutoItog from itog and listName " + listName, 
    				new Object[] {session(),itog});
    	} catch (Exception e) {
    		logger.log(WOLogLevel.WARNING, "Error deleting AutoItog", 
    				new Object[] {session(),autoItog,e});
    		session().takeValueForKey(e.getMessage(), "message");
    	} finally {
    		ec.unlock();
    	}
    	returnPage.ensureAwakeInContext(context());
    	return returnPage;
    }
}