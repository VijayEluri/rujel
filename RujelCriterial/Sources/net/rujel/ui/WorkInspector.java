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

import java.math.BigDecimal;
import java.util.logging.Logger;

import net.rujel.base.MyUtility;
import net.rujel.base.SettingsBase;
import net.rujel.criterial.*;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.EduLesson;
import net.rujel.reusables.ModulesInitialiser;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Aug 28, 2008 8:22:47 PM
public class WorkInspector extends com.webobjects.appserver.WOComponent {

	public static final NSArray keys = new NSArray(new String[] {
			Work.NUMBER_KEY, Work.WORK_TYPE_KEY, Work.ANNOUNCE_KEY, Work.DATE_KEY, 
			Work.THEME_KEY, Work.TITLE_KEY, "trimmedWeight",  "homeTask"});

	public WOComponent returnPage;
	public Work work;
	public NSArray types;
	public Object item;
	
	public Integer hours;
	public Integer minutes;
	public NSMutableDictionary dict;
	public EduCourse course;
	public CriteriaSet critSet;
	public NamedFlags namedFlags;
	public String disableMax;
	public String disableWeight;
	public EduLesson lesson;
	protected boolean done = false;

    public WorkInspector(WOContext context) {
        super(context);
    }
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(done) {
    		aResponse.appendContentString(aContext.componentActionURL());
    		return;
    	}
    	if(course == null)
    		course = (EduCourse)returnPage.valueForKey("course");
    	EOEditingContext ec = course.editingContext();
    	critSet = CriteriaSet.critSetForCourse(course);
    	if(critSet != null) {
    		NamedFlags critFlags = critSet.namedFlags();
    		disableMax = (critFlags.flagForKey("fixList"))?"disabled":null;
    		disableWeight = (critFlags.flagForKey("fixWeight"))?"disabled":null;
    	} else {
    		disableMax = null;
    		disableWeight = null;
    	}
    	if(dict == null)
    		dict = new NSMutableDictionary();
    	if(dict.valueForKey("trimmedWeight") == null)
    		dict.takeValueForKey(BigDecimal.ZERO, "trimmedWeight");
    	if(dict.valueForKey(Work.ANNOUNCE_KEY) == null)
    		dict.takeValueForKey(session().valueForKey("today"), Work.ANNOUNCE_KEY);
    	if(dict.valueForKey(Work.DATE_KEY) == null)
    		dict.takeValueForKey(session().valueForKey("today"), Work.DATE_KEY);
    	if(namedFlags == null) {
    		WorkType type = (WorkType)dict.valueForKey(Work.WORK_TYPE_KEY);
    		if(type == null) {
    			type = WorkType.defaultType(ec);
    			dict.takeValueForKey(type, Work.WORK_TYPE_KEY);
    		}
    		namedFlags = (type==null)?new NamedFlags(WorkType.flagNames):
    			type.namedFlags().and(24);
    	}
    	if(types == null) {
    		EOFetchSpecification fs = new EOFetchSpecification(WorkType.ENTITY_NAME,
   				WorkType.activeQualifier, ModulesInitialiser.sorter);
    		types = ec.objectsWithFetchSpecification(fs);
    	}
    	critIdx = -1;
    	super.appendToResponse(aResponse, aContext);
    	session().takeValueForKey(null, "message");
    }
    
    public void setWork(Work newWork) {
    	work = newWork;
    	if(work == null)
    		return;
    	if(work.load() != null) {
    		int mins = work.load().intValue();
    		int hrs = mins / 60;
    		mins = mins % 60;
    		if(mins > 0)
    			minutes = new Integer(mins);
    		if(hrs > 0)
    			hours = new Integer(hrs);
    	}
    	dict = new NSMutableDictionary();
    	for (int i = 0; i < keys.count(); i++) {
			String key = (String)keys.objectAtIndex(i);
			dict.takeValueForKey(work.valueForKey(key), key);
		}
//    	dict = work.valuesForKeys(keys).mutableClone();
    	namedFlags = new NamedFlags(work.flags(),WorkType.flagNames);
    }

    public String lessonTitle() {
    	return NotePresenter.titleForLesson(work);
    }
        
    public WOActionResults save() {
     	EOEditingContext ec = course.editingContext();
    	boolean created = (work == null);
    	if(created) {
    		work = (Work)EOUtilities.createAndInsertInstance(ec, Work.ENTITY_NAME);
    		work.addObjectToBothSidesOfRelationshipWithKey(course, "course");
    	}
    	WorkType type = (WorkType)dict.objectForKey(Work.WORK_TYPE_KEY);
    	if(type != null) {
    		work.setWorkType(type);
    		if(!type.namedFlags().flagForKey("fixHometask"))
    			work.setIsHometask(namedFlags.flagForKey("hometask"));
    		if(!type.namedFlags().flagForKey("fixCompulsory"))
    			work.setIsCompulsory(namedFlags.flagForKey("compulsory"));
    	} else {
    		work.setFlags(namedFlags.toInteger());
    	}
    	for (int i = 0; i < keys.count(); i++) {
			String key = (String)keys.objectAtIndex(i);
			Object value = dict.valueForKey(key);
			if(!created || value != null)
				work.takeValueForKey(value, key);
		}
//    	work.takeValuesFromDictionary(dict);
    	if(created) {
    		MyUtility.setNumberToNewLesson(work);
    		dict.takeValueForKey(work.number(), "number");
    	}
    	int load = 0;
    	if(hours != null)
    		load = hours.intValue() * 60;
    	if(minutes != null)
    		load = load + minutes;
    	if(work.load() == null || work.load().intValue() != load)
    		work.setLoad(new Integer(load));
    	WORequest req = context().request();
//    	NSNumberFormatter frmt = new NSNumberFormatter("0");
    	int critCount = 0;
    	String crCnt = req.stringFormValueForKey("critCount");
    	if(crCnt != null)
    		critCount = Integer.parseInt(crCnt);
    	for (int i = 0; i <= critCount; i++) { // prepare criter mask
    		Integer criterion = new Integer(i);
			EOEnterpriseObject mask = work.getCriterMask(criterion);
			String value = req.stringFormValueForKey("m" + i);
			Integer val = null;
			if(disableMax != null && mask == null) {
				EOEnterpriseObject cr = critSet.criterionForNum(criterion);
				if(cr != null)
					val = (Integer)cr.valueForKey("dfltMax");
			}
			if(value != null && disableMax == null) {
				value = value.trim();
				try {
					val = new Integer(value);
				} catch (NumberFormatException e) {
					if(val == null) {
						if(mask != null) {
							val = (Integer)mask.valueForKey("max");
						} else if (critSet != null) {
							EOEnterpriseObject cr = critSet.criterionForNum(criterion);
							if(cr != null)
								val = (Integer)cr.valueForKey("dfltMax");
						}
						Logger.getLogger("rujel.criterial").log(WOLogLevel.WARNING,
								"Can't read criter max: " + criterion + " = '" + value + '\'',
								new Object[] {session(),work});
						StringBuilder buf = new StringBuilder(); 
						buf.append(session().valueForKeyPath("strings.Strings.messages.illegalFormat"));
						buf.append(' ').append(session().valueForKeyPath(
						"strings.RujelCriterial_Strings.Max"));
						session().takeValueForKey(buf.toString(), "message");
					}
				}
			}
			if(val == null) {
				if(mask != null) {
					work.removeObjectFromBothSidesOfRelationshipWithKey
								(mask,Work.CRITER_MASK_KEY);
					ec.deleteObject(mask);
				}
				continue;
			} else {
				if(mask == null) {
					mask = EOUtilities.createAndInsertInstance(ec, "CriterMask");
					work.addObjectToBothSidesOfRelationshipWithKey(
							mask, Work.CRITER_MASK_KEY);
					mask.takeValueForKey(criterion, "criterion");
				}
				if(!val.equals(mask.valueForKey("max")))
					mask.takeValueForKey(val, "max");
			}
			if(mask != null) {
				value = req.stringFormValueForKey("w" + i);
				if(value != null) value = value.trim();
				try {
					val = (value == null)? null : new Integer(value);
				} catch (NumberFormatException e) {
					val = null;
				}
				Number mWeight = (Number)mask.valueForKey("weight");
				if(mWeight == null || val == null || mWeight.intValue() != val.intValue())
					mask.takeValueForKey(val, "weight");
			}
		} // prepare criter mask
    	try {
    		if(work.workType() == null) {
    			work.setWorkType((EOEnterpriseObject)types.objectAtIndex(0));
    			Logger.getLogger("rujel.criterial").log(WOLogLevel.WARNING,
    					"Autosetting workType",new Object[] {session(),work});
    		}
    		if(!created)
       			work.nullify();
			work.validateForSave();
	    	returnPage.ensureAwakeInContext(context());
			WOActionResults result = (WOActionResults)returnPage.valueForKey("saveNoreset");
			if(result instanceof WOComponent)
				returnPage = (WOComponent)result;
//    		ec.saveChanges();
    	} catch (NSValidation.ValidationException ve) {
    		session().takeValueForKey(ve.getMessage(), "message");
    	} catch (NSKeyValueCoding.UnknownKeyException e) {
    		session().takeValueForKey(application().valueForKeyPath
    				("strings.RujelCriterial_Strings.messages.notSaved"), "message");
    	} catch (Exception e) {
    		session().takeValueForKey(e.getMessage(), "message");
    		Logger.getLogger("rujel.criterial").log(WOLogLevel.WARNING, "error saving", 
    				new Object[] {session(),work,e});
    	}
    	done = (!ec.hasChanges());
       	if(done) {
       		if(lesson != null && lesson != work) {
       			returnPage.takeValueForKey(lesson, "currLesson");
       			returnPage.takeValueForKey(lesson, "selector");
       		}
    		if(Work.ENTITY_NAME.equals(returnPage.valueForKeyPath("present.entityName"))) {
    			if(!ec.hasChanges())
    				returnPage.valueForKey("updateLessonList");
    		} else {
    			returnPage.takeValueForKeyPath("MarksPresenter", "present.tmpPresenter");
    		}
       	}
 		session().removeObjectForKey("lessonProperies");
    	return this;
    }
    
    public WOActionResults returnPage() {
    	if(course.editingContext().hasChanges())
    		course.editingContext().revert();
    	if(work != null)
    		work.nullify();
    	returnPage.ensureAwakeInContext(context());
    	return returnPage;
    }
    
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if(aContext.elementID().equals(aContext.senderID()))
    		return returnPage();
		return super.invokeAction(aRequest, aContext);
	}
/*	private NSArray _criteria;
    public NSArray criteria() {
		if(_criteria == null) {
			_criteria = CriteriaSet.criteriaForCourse(work.course());
			if(_criteria == null)
				_criteria = NSArray.EmptyArray;
		}
		return _criteria;
    }
*/
    public int critIdx = -1;

    public Integer critCount() {
    	Integer count = null;
    	if(work == null) {
     		if(critSet != null) {
    			Integer max = (Integer)critSet.criteria().valueForKey("@max.criterion");
    			if(max != null && max.intValue() > 0)
    				return max;
    		}
     		return new Integer(1);
    	}
    		count = (Integer)work.valueForKeyPath("critSet.criteria.@max.criterion");
    	if (count != null && count.intValue() > 0)
    		return count;
    	NSArray mask = work.criterMask();
    	if(mask == null || mask.count() == 0)
    		return new Integer(1);
    	count = (Integer)mask.valueForKeyPath("@max.criterion");
    	return new Integer (count.intValue() + 1);
    }
    
    protected Integer criterion() {
    	return new Integer(critIdx + 1);
    }
    
    public String critName() {
    	return CriteriaSet.critNameForNum(criterion(),critSet);
    }
    
    public EOEnterpriseObject critItem() {
    	if(critIdx < 0)
    		return null;
    	if(critSet == null)
    		return null;
    	return critSet.criterionForNum(criterion());
    }
    
    protected String fieldName(char prefix) {
//    	if(critIdx < 0)
//    		return null;
    	StringBuilder buf = new StringBuilder(3);
    	buf.append(prefix).append(critIdx + 1);
    	return buf.toString();
    }
    
    public String maxName() {
    	return fieldName('m');
    }
    
    public String weightName() {
    	return fieldName('w');
    }
    
    public String onChange() {
    	if(critIdx < 0)
    		return "return isNumberInput(event);";
    	if(critSet != null) {
    		if(critSet.namedFlags().flagForKey("fixMax") || critItem() != null &&
        			critItem().valueForKey("indexer") != null)
    			return null;
    		return "return isNumberInput(event);";
    	}
    	Integer count = (Integer)valueForKeyPath("work.criterMask.@max.criterion");
    	if(count != null && critIdx < count.intValue())
    		return "return isNumberInput(event);";
    	return "return addCriterion(event);";
    }
    
    public String onClick() {
    	Object crit = criterMax();
    	if(critIdx >= 0 && crit == null)
    		crit = valueForKeyPath("critItem.dfltMax");
    	if(crit == null && critIdx < 0) {
    		int max = SettingsBase.numericSettingForCourse("CriterlessMax",
    				course, course.editingContext(),5);
    		crit = Integer.toString(max);
    	} else if(crit != null && crit.equals(" ")) {
    		crit = null;
    	}
    	if(crit == null)
        	return "select();";
    	StringBuilder buf = new StringBuilder("if(!value){value=(");
    	buf.append(crit).append(");blockCriters(event);}select();");
    	return buf.toString();
    }
    
    public String close() {
    	if(course.editingContext().hasChanges())
    		return (String)session().valueForKey("tryLoad");
    	return "closePopup();";
    }

    public String inputType() {
    	if(critSet == null || critItem() == null || critItem().valueForKey("dfltMax") == null)
    		return "text";
    	if(critSet.namedFlags().flagForKey("fixMax") ||
    			critItem().valueForKey("indexer") != null)
    		return "checkbox";
    	return "text";
    }
    
    public String checked() {
    	if(critSet == null || critItem() == null || critItem().valueForKey("dfltMax") == null ||
    			 (itemMask() == null && disableMax == null))
    		return null;
    	if(critSet.namedFlags().flagForKey("fixMax") ||
    			critItem().valueForKey("indexer") != null)
    		return "checked";
    	return null;
    }
    
    protected EOEnterpriseObject itemMask() {
    	if(work == null)
    		return null;
//    	if(critIdx < 0)
//    		return null;
    	return work.getCriterMask(criterion());
    }
	
    public Number criterMax() {
//    	if(critIdx < 0) 
//    		return null;
    	EOEnterpriseObject _itemMask = itemMask();
        if(_itemMask == null)  {
        	EOEnterpriseObject cr = critItem();
        	if(cr == null)
        		return null;
        	if(critSet.namedFlags().flagForKey("fixMax") || cr.valueForKey("indexer") != null ||
        			critSet.namedFlags().flagForKey("fixList"))
        		return (Number)cr.valueForKey("dfltMax");
        	return null;
        }
		return (Number)_itemMask.valueForKey("max");
    }

    public Number criterWeight() {
    	EOEnterpriseObject _itemMask = itemMask();
    	if(_itemMask == null) return null;
    	return (Number)_itemMask.valueForKey("weight");
    }

    public Boolean cantSave() {
    	if(work != null)
    		return (Boolean)session().valueForKeyPath("readAccess._edit.work");
    	else
    		return (Boolean)session().valueForKeyPath("readAccess._create.Work");
    }
    
    public String typeChange() {
    	if(types == null || types.count() == 0)
    		return null;
    	StringBuilder buf = new StringBuilder("switch(value){");
    	for (int i = 0; i < types.count(); i++) {
			WorkType type = (WorkType) types.objectAtIndex(i);
			buf.append("case '").append(i).append("': setDefaults(");
			buf.append(type.dfltFlags()).append(',').append(type.trimmedWeight());
			buf.append(");break;");
		}
    	buf.append("default: setDefaults(0);};");
    	return buf.toString();
    }
    
    public EduLesson currLesson() {
    	return work;
    }
}