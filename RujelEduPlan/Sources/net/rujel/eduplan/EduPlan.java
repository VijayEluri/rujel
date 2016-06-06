// EduPlan.java: Class file for WO Component 'EduPlan'

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
import java.util.logging.Logger;

import net.rujel.base.SchoolSection;
import net.rujel.interfaces.EOInitialiser;
import net.rujel.reusables.PlistReader;
import net.rujel.reusables.SessionedEditingContext;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

// Generated by the WOLips Templateengine Plug-in at Jul 21, 2009 3:24:54 PM
public class EduPlan extends com.webobjects.appserver.WOComponent {
	
	public static Logger logger = Logger.getLogger("rujel.eduplan");

	public NSArray tablist;
	public NSKeyValueCoding currTab;
	public EOEditingContext ec;
//	public WOComponent toReset;
	public Boolean shouldReset;
		
    public EduPlan(WOContext context) {
        super(context);
        ec = new SessionedEditingContext(context.session());
        tablist = (NSArray)session().valueForKeyPath("modules.planTabs");
        currTab = (NSKeyValueCoding)tablist.objectAtIndex(0);
    }
    
    public String title() {
    	String title = (String)valueForKeyPath("currTab.titie");
    	if(title == null)
    		title = (String)session().valueForKeyPath(
    				"strings.RujelEduPlan_EduPlan.EduPlan");
    	if(title == null)
    		title = "Edu plan";
    	return title;
    }

    public WOActionResults revertEc() {
		ec.lock();
		try {
/*			if(toReset != null) {
				toReset.reset();
				String tmp = toReset.name();
				if(!tmp.endsWith((String)currTab.valueForKey("component")))
					toReset = null;
			}*/
			if(ec.hasChanges())
				ec.revert();
			shouldReset = Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ec.unlock();
		}
		return this;
    }
    
	public static Object init(Object obj, WOContext ctx) {
		if(obj == null || obj.equals("init")) {
			return init();
//		} else if(obj.equals("yearChanged")) {
//			return yearChanged(ctx);
		} else if(obj.equals("regimes")) {
//			if(Various.boolForObject(ctx.session().valueForKeyPath("readAccess._read.EduPlan")))
//				return null;
			return WOApplication.application().valueForKeyPath(
					"strings.RujelEduPlan_EduPlan.planRegime");
		} else if("planTabs".equals(obj)) {
			return planTabs(ctx);
		} else if("lessonTabs".equals(obj)) {
			return EduPeriod.lessonTabs(ctx);
		} else if("periods".equals(obj)) {
			return EduPeriod.periods(ctx);
		} else if("settingQualifiers".equals(obj)) {
			return settingQualifiers(ctx);
		} else if("settingEditors".equals(obj)) {
			return ctx.session().valueForKeyPath(
				"strings.RujelEduPlan_EduPlan.settingEditors");
		} else if("xmlGeneration".equals(obj)) {
			return xmlGeneration(ctx);
		} else if("adminModules".equals(obj)) {
			return ctx.session().valueForKeyPath("strings.RujelEduPlan_EduPlan.adminModules");
		} else if("usedModels".equals(obj)) {
			return new NSArray(new String[] {"EduPlanModel","EduPlanYearly"});
		} else if("initialData".equals(obj)) {
			return InitialDataGenerator.initialData(ctx);
		}
		return null;
	}
	
	public static Object planTabs(WOContext ctx) {
		NSArray tabs = (NSArray)WOApplication.application().valueForKeyPath(
			"strings.RujelEduPlan_EduPlan.planTabs");
		Enumeration enu = tabs.objectEnumerator();
		NSMutableArray result = null;
		while (enu.hasMoreElements()) {
			NSKeyValueCoding tab = (NSKeyValueCoding) enu.nextElement();
			String component = (String)tab.valueForKey("component");
			if(!Various.boolForObject(ctx.session().valueForKeyPath("readAccess.read." 
					+ component))) {
				if(result == null)
					result = tabs.mutableClone();
				result.removeIdenticalObject(tab);
			}
		}
		if(result == null)
			return tabs;
		if(result.count() == 0)
			return null;
		return result;
	}
	
	public static Object init() {
		try {
			Object access = PlistReader.readPlist("access.plist", "RujelEduPlan", null);
			WOApplication.application().takeValueForKey(access, "defaultAccess");
		} catch (NSKeyValueCoding.UnknownKeyException e) {
			// default access not supported
		}
		try {
			EOInitialiser.initialiseRelationship("EduPeriod","relatedItog",false,
					"itogID","ItogContainer");
		} catch (Exception e) {
			logger.log(WOLogLevel.INFO,"Failed to link EduPeriod to ItogContainer",e);
		}

		EOSortOrdering.ComparisonSupport.setSupportForClass(
				new SubjectComparator.ComparisonSupport(), Subject.class);
		EOSortOrdering.ComparisonSupport.setSupportForClass(
				new PlanCycle.ComparisonSupport(), PlanCycle.class);
		SchoolSection.listSections(new EOEditingContext(), true);
		return null;
	}
	
	public static Object yearChanged(WOContext ctx) {
		WOSession ses = ctx.session();
		boolean terminating = ses.isTerminating();
		if(terminating || ctx == null)
			return null;
		NSMutableDictionary state =  (NSMutableDictionary)ses.valueForKey("state");
		NSArray sections = SectionsSetup.updateSession(ses);
		if(state.valueForKey("section") == null) {
			if(sections != null) {
				NSDictionary dict = (NSDictionary)sections.objectAtIndex(0);
				state.takeValueForKey(dict, "section");
			} else {
				state.takeValueForKey(new NSDictionary(new Integer(0),"idx"), "section");
			}
		}
		return null;
	}
	
	public static Object settingQualifiers(WOContext ctx) {
		NSKeyValueCodingAdditions source = null;
		if(ctx.hasSession())
			source = ctx.session();
		else
			source = WOApplication.application();
		NSDictionary dict = (NSDictionary)source.valueForKeyPath(
			"strings.RujelEduPlan_EduPlan.settingQualifiers");
		if(dict == null)
			return null;
		return dict.allValues();
	}
	
	public static Object xmlGeneration(WOContext ctx) {
		NSDictionary options = (NSDictionary)ctx.session().objectForKey("xmlGeneration");
		{
			NSDictionary settings = (NSDictionary)options.valueForKeyPath("reporter.settings");
			if(settings != null && 
					!Various.boolForObject(settings.valueForKeyPath("eduPlan.active")))
				return null;
		}
		return new EduPlanXML(options);
	}

}