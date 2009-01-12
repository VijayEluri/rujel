// WorkInspector.java

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

import net.rujel.criterial.*;
import net.rujel.reusables.SettingsReader;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Aug 28, 2008 8:22:47 PM
public class WorkInspector extends com.webobjects.appserver.WOComponent {

	public WOComponent returnPage;
	public Work work;
	
	public Integer hours;
	public Integer minutes;
	
	public EOEditingContext tmpEC;

    public WorkInspector(WOContext context) {
        super(context);
    }
    
    public void setWork(Work newWork) {
    	work = newWork;
    	if(work.load() != null) {
    		int mins = work.load().intValue();
    		int hrs = mins / 60;
    		mins = mins % 60;
    		if(mins > 0)
    			minutes = new Integer(mins);
    		if(hrs > 0)
    			hours = new Integer(hrs);
    	}
    }

    public String lessonTitle() {
    	return NotePresenter.titleForLesson(work);
    }
    
    public NSArray types() {
    	return Work.workTypes();
    }
    
    public WOActionResults save() {
    	int load = 0;
    	if(hours != null)
    		load = hours.intValue() * 60;
    	if(minutes != null)
    		load = load + minutes;
    	if(work.load() == null || work.load().intValue() != load)
    		work.setLoad(new Integer(load));

    	returnPage.ensureAwakeInContext(context());
    	WOActionResults result = null;
    	try {
    		if(tmpEC != null) {
    			tmpEC.lock();
    			tmpEC.saveChanges();
        		/*if(shouldNullify)
        			work.nullify();
    			EOEditingContext ec = (EOEditingContext)returnPage.valueForKey("ec");
    			work = (Work)EOUtilities.localInstanceOfObject(ec, work);*/
    			returnPage.takeValueForKey(work, "currLesson");
    			returnPage.takeValueForKeyPath("MarksPresenter", "present.tmpPresenter");
    		}
    		if(shouldNullify)
    			work.nullify();
    		result = (WOActionResults)returnPage.valueForKey("saveNoreset");
    	} catch (NSKeyValueCoding.UnknownKeyException e) {
    		session().takeValueForKey(application().valueForKeyPath
    				("extStrings.RujelCriterial_Strings.messages.notSaved"), "message");
    	} catch (Exception e) {
    		session().takeValueForKey(e.getMessage(), "message");
    	} finally {
    		if(tmpEC != null)
    			tmpEC.unlock();
    		//ec.unlock();    		
    	}
    	if(result == null)
    		result = returnPage;
		session().removeObjectForKey("lessonProperies");
    	return result;
    }

	private NSArray _criteria;
    public NSArray criteria() {
		if(_criteria == null) {
			_criteria = CriteriaSet.criteriaForCycle(work.course().cycle());
		}
		return _criteria;
    }

    /** @TypeInfo Criterion */
    public EOEnterpriseObject critItem;

   protected EOEnterpriseObject itemMask() {
    	if(work == null)
    		return null;
    	if(critItem == null)
    		return null;

    	NSArray mask = work.criterMask();
    	if(mask == null || mask.count() == 0) 
    		return null;

    	EOQualifier qual = new EOKeyValueQualifier("criterion",EOQualifier.QualifierOperatorEqual,critItem);
    	NSArray result = EOQualifier.filteredArrayWithQualifier(mask,qual);
    	if(result != null && result.count() > 0)
    		return (EOEnterpriseObject)result.objectAtIndex(0);
    	else
    		return null;
    }

	
    public Number criterMax() {
    	EOEnterpriseObject _itemMask = itemMask();
        if(_itemMask == null) return null;
		return (Number)_itemMask.valueForKey("max");
    }
    
    private boolean shouldNullify = false;
    public void setCriterMax(Number newCriterMax) {
		boolean weightToMax = SettingsReader.boolForKeyPath("edu.weightToMax",true);
		EOEnterpriseObject _itemMask = itemMask();
        if(_itemMask == null) { // create new criterMask
			if(newCriterMax == null) return;
			_itemMask = EOUtilities.createAndInsertInstance(work.editingContext(),"CriterMask");
			work.addObjectToBothSidesOfRelationshipWithKey(_itemMask,"criterMask");
			_itemMask.takeValueForKey(critItem,"criterion");
			if(weightToMax)
				_itemMask.takeValueForKey(newCriterMax,"weight");
			else
				_itemMask.takeValueForKey(new Integer(1),"weight");
			
			_itemMask.takeValueForKey(newCriterMax,"max");
			shouldNullify = true;
		} else if(newCriterMax == null) { // remove criter max
			work.removeObjectFromBothSidesOfRelationshipWithKey(_itemMask,"criterMask");
			_itemMask = null;
			shouldNullify = true;
			return;
		} else if(newCriterMax.intValue() != criterMax().intValue()){  // update criter max
			Object oldWeight = _itemMask.valueForKey("weight");
			Object oldMax = _itemMask.valueForKey("max");
			if(weightToMax && (oldWeight == null || oldMax == null || oldWeight.equals(oldMax)))
				_itemMask.takeValueForKey(newCriterMax,"weight");
			_itemMask.takeValueForKey(newCriterMax,"max");
		}
    }

    public Number criterWeight() {
    	EOEnterpriseObject _itemMask = itemMask();
    	if(_itemMask == null) return null;
    	return (Number)_itemMask.valueForKey("weight");
    }
    
    public void setCriterWeight(Number newCriterWeight) {
    	EOEnterpriseObject _itemMask = itemMask();
    	if(_itemMask == null || newCriterWeight == null) {
    		return;
    	}
    	if(newCriterWeight.intValue() != criterWeight().intValue())
    		_itemMask.takeValueForKey(newCriterWeight,"weight");
    }

}