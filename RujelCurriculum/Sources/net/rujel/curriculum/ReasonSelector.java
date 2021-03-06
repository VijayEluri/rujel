// ReasonSelector.java: Class file for WO Component 'ReasonSelector'

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

package net.rujel.curriculum;

import java.text.FieldPosition;
import java.text.Format;
import java.util.logging.Logger;

import net.rujel.base.MyUtility;
import net.rujel.interfaces.*;
import net.rujel.reusables.SettingsReader;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Jan 29, 2009 4:14:28 PM
public class ReasonSelector extends com.webobjects.appserver.WOComponent {
	protected static final Logger logger = Logger.getLogger("rujel.curriculum");
	protected EOEditingContext ec;
	public Reason reason;
	public Number reasonID;
	protected NSTimestamp _date;
	protected EduCourse _course;
	public Reason rItem;
	public String reasonText;
	//public boolean withTeacher = false;
	//public boolean withEduGroup = false;
	public int relation = 1; 
	public String aDate;
	public String begin;
	public String end;
	public final boolean ifArchive = SettingsReader.boolForKeyPath("markarchive.Reason", 
			SettingsReader.boolForKeyPath("markarchive.archiveAll", false));
	
	public final String teacherRelated = (String)application().valueForKeyPath(
			"strings.RujelCurriculum_Curriculum.Reason.relatedTo") + ' '
			+ application().valueForKeyPath(
					"strings.RujelCurriculum_Curriculum.Reason.withTeacher");
	public final String groupRelated = (String)application().valueForKeyPath(
	"strings.RujelCurriculum_Curriculum.Reason.relatedTo") + ' '
	+ application().valueForKeyPath(
			"strings.RujelCurriculum_Curriculum.Reason.withEduGroup");

    public ReasonSelector(WOContext context) {
        super(context);
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(date() != null)
    		aDate = MyUtility.dateFormat().format(date());
    	ec = course().editingContext();
    	reason = (Reason)valueForBinding("reason");
    	if(reason != null) {
    		EOGlobalID rgid = ec.globalIDForObject(reason);
    		if(rgid.isTemporary())
    			reasonID = -1;
    		else
    			reasonID = (Number)((EOKeyGlobalID)rgid).keyValues()[0];
    		reasonText = reason.reason();
    		if(reason.namedFlags().flagForKey("forTeacher"))
    			relation = 1;
    		else if (reason.namedFlags().flagForKey("forEduGroup"))
    			relation = 2;
    		else if (reason.grade() != null)
    			relation = 3;
    		else
    			relation = 0;
    		//withTeacher = (reason.teacher() != null);
    		//withEduGroup = (reason.eduGroup() != null);
    		begin = MyUtility.dateFormat().format(reason.begin());
    		if(reason.end() != null)
    			end = MyUtility.dateFormat().format(reason.end());
    	} else {
    		relation = 1;
    	}
    	super.appendToResponse(aResponse, aContext);
    }
    
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
    	ec = course().editingContext();
       	reason = (Reason)valueForBinding("reason");
    	super.takeValuesFromRequest(aRequest, aContext);
    	if(reasonID == null)
    		return;
    	if(reasonID.intValue() == 0) {
    		if(reasonText == null)
    			return;
    		reason = (Reason)EOUtilities.createAndInsertInstance(ec, Reason.ENTITY_NAME);
//    		logger.log(WOLogLevel.OWNED_EDITING,"Creating Reason",session());
    		if(Various.boolForObject(session().valueForKeyPath("sections.hasSections"))) {
    			reason.setSection(session().valueForKeyPath("state.section"));
    		}
    		aDate = null;
//        	setValueForBinding(reason, "reason");
    	} else if(reasonID.intValue() != -1) {
    		try {
				reason = (Reason) EOUtilities.objectWithPrimaryKeyValue(ec,
						Reason.ENTITY_NAME, reasonID);
				if(reason != (Reason)valueForBinding("reason"))
					reasonText = null;
			} catch (Exception e) {
				Object[] args = new Object[] {session(),e};
				logger.log(WOLogLevel.WARNING,"Error restoring Reason for key: " + reasonID,args);
				session().takeValueForKey(application().valueForKeyPath
						("strings.Strings.messages.error")+ "<br/>\n" + e.toString(), "message");
				return;
			}
    	}
    	if(aDate == null && reasonText != null && reason.verification() == null) {
    		//boolean hasChanges = !ifArchive;
    		boolean hasChanges = (!reasonText.equals(reason.reason()));
    		if(hasChanges)
    			reason.setReason(reasonText);
    		hasChanges = (hasChanges || (reason.namedFlags().flagForKey("forTeacher")^(relation==1)));
    		if(hasChanges) {
    			reason.setTeacher((relation==1)?course().teacher(date()):null);
    			reason.namedFlags().setFlagForKey((relation==1), "forTeacher");
    		}
    		hasChanges = (hasChanges || (reason.namedFlags().flagForKey("forEduGroup")^(relation==2)));
    		if(hasChanges) {
    			reason.setEduGroup((relation==2)?course().eduGroup():null);
    			reason.namedFlags().setFlagForKey((relation==2), "forEduGroup");
    		}
    		hasChanges = (hasChanges || ((reason.grade() != null)^(relation==3)));
    		if(hasChanges) {
    			reason.setGrade((relation==3)?course().cycle().grade():null);
    		}
    		NSTimestamp tmpDate = MyUtility.parseDate(begin);
    		if(tmpDate == null) {
    			tmpDate = date();
    		} else if (tmpDate.compareTo(date()) > 0) {
    			session().takeValueForKey(application().valueForKeyPath(
    				"strings.RujelCurriculum_Curriculum.messages.wrongDates"), "message");
    			tmpDate = date();
    		}
    		hasChanges = (hasChanges || (!reason.begin().equals(tmpDate)));
    		if(hasChanges)
    			reason.setBegin(tmpDate);
    		if(end != null) {
    			tmpDate = MyUtility.parseDate(end);
    			if(tmpDate == null) {
    				tmpDate = date();
        		} else if (tmpDate.compareTo(date()) < 0) {
        			session().takeValueForKey(application().valueForKeyPath(
        				"strings.RujelCurriculum_Curriculum.messages.wrongDates"), "message");
        			tmpDate = date();
        		}
    			hasChanges = (hasChanges || reason.end() == null || (!reason.end().equals(tmpDate)));
    			if(hasChanges)
    				reason.setEnd(tmpDate);
    		} else {
    			hasChanges = (hasChanges || reason.end() != null);
    			if(hasChanges)
    				reason.setEnd(null);
    		}
    		if(ifArchive && hasChanges) {
    			EOEnterpriseObject archive = EOUtilities.createAndInsertInstance(ec,"MarkArchive");
    			archive.takeValueForKey(new Integer((reasonID.intValue() == 0)?1:2), "actionType");
    			archive.takeValueForKey(reason, "objectIdentifier");
    			archive.takeValueForKey(reasonText,"@reason");
    			archive.takeValueForKey(begin,"@begin");
    			archive.takeValueForKey(end,"@end");
    			if(reason.namedFlags().flagForKey("forTeacher"))
    				archive.takeValueForKey(Person.Utility.fullName(
    						reason.teacher(), true, 2, 1, 1),"@teacher");
    			if(reason.namedFlags().flagForKey("forEduGroup"))
    				archive.takeValueForKey(reason.eduGroup().name(),"@eduGroup");
    		}
    	}
    	setValueForBinding(reason, "reason");
    }

    public EduCourse course() {
    	if(_course == null) {
    		_course = (EduCourse)valueForBinding("course");
    	}
    	return _course;
    }
    
    public Teacher teacher() {
    	return course().teacher(date());
    }
    
    public NSTimestamp date() {
    	if(_date == null) {
    		_date = (NSTimestamp)valueForBinding("date");
    	}
    	return _date;
    }
    
    public NSArray reasonList() {
//    	if (Various.boolForObject(valueForBinding("readOnly")))
//    		return ((reason == null)?null:new NSArray(reason));
    	NSArray reasons = Reason.reasons(date(), course());
    	if(reason == null || reasons.contains(reason))
    		return reasons;
    	Object[] args = new Object[] {session(),reason,date()};
    	logger.log(WOLogLevel.WARNING,"Current Reason not found on date",args);
    	NSMutableArray result = new NSMutableArray(reason);
    	result.addObjectsFromArray(reasons);
    	return result;
    }
    
    public String styleClass() {
    	if(rItem == null)
    		return "grey";
    	if(rItem == reason)
    		return "selection";
    	return rItem.styleClass();
/*  	if(rItem.unverified())
    		return "ungerade";
    	return "gerade";
*/    }
    
/*    public Integer colspan() {
    	if(rItem == null)
    		return null;
    	if(rItem.teacher() == null && rItem.eduGroup() == null)
    		return new Integer(3);
    	return null;
    }*/
    
    public String title() {
    	if(rItem == null)
    		return null;
    	Format dateFormat = MyUtility.dateFormat();
    	if(rItem.begin().equals(rItem.end()))
    		return dateFormat.format(rItem.begin());
    	StringBuffer result = new StringBuffer(24);
    	FieldPosition fp = new FieldPosition(0);
    	dateFormat.format(rItem.begin(), result, fp);
    	result.append(" - ");
    	if(rItem.end() == null)
    		result.append("...");
    	else
        	dateFormat.format(rItem.end(), result, fp);
    	return result.toString();
    }
    
    public Object itemID() {
    	if(rItem == null)
    		return null;
		EOGlobalID rgid = ec.globalIDForObject(rItem);
		if(rgid.isTemporary())
			return new Integer(-1);
		else
		return ((EOKeyGlobalID)rgid).keyValues()[0];
    }
    
    public boolean canEditReason() {
    	if(reason == null || rItem != reason || reason.verification() != null ||
    			Various.boolForObject(valueForBinding("readOnly")))
    		return false;
    	return Various.boolForObject(valueForKeyPath("session.readAccess.edit.reason"));
    }
    
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}

	public void reset() {
		_course = null;
		_date = null;
		reason = null;
		reasonID = null;
		ec = null;
		rItem = null;
		reasonText = null;
		aDate = null;
		begin = null;
		end  = null;
		
	}
}