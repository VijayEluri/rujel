// ByCourseEditor.java: Class file for WO Component 'ByCourseEditor'

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
import java.util.logging.Logger;

import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.EduCycle;
import net.rujel.interfaces.EduGroup;
import net.rujel.reusables.AdaptingComparator;
import net.rujel.reusables.SettingsReader;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Jul 18, 2009 2:20:41 PM
public class ByCourseEditor extends com.webobjects.appserver.WOComponent {
	public static final Logger logger = Logger.getLogger("rujel.base");
	
	public WOComponent returnPage;
	protected SettingsBase base;
	public NSMutableArray editList;
	protected NSKeyValueCoding byCourse;
	public NSArray grades;
	protected NSMutableDictionary groupsByGrade;
	public Integer gradeItem;
	public EduGroup groupItem;
	public NSArray cycles;
	public EduCycle cycleItem;
	protected NSMutableDictionary coursesByCycle;
	public EduCourse courseItem;
	public String pushToKeyPath;
	public NSKeyValueCodingAdditions resultGetter;
	
	
    public ByCourseEditor(WOContext context) {
        super(context);
		int maxGrade = SettingsReader.intForKeyPath("edu.maxGrade", 11);
		int minGrade = SettingsReader.intForKeyPath("edu.minGrade", 1);
		Integer[] grds = new Integer[maxGrade - minGrade + 1];
		NSMutableArray[] grps = new NSMutableArray[grds.length];
		for (int i = 0; i < grds.length; i++) {
			grds[i] = new Integer(minGrade + i);
			grps[i] = new NSMutableArray();
		}
		grades = new NSArray(grds);
		groupsByGrade = new NSMutableDictionary(grps,grds);
    }
    
    public void setPushToKeyPath(String path) {
    	if(resultGetter instanceof WOComponent) {
    		WOComponent getter = (WOComponent)resultGetter;
    		while (path.charAt(0) == '^') {
    			path = path.substring(1);
				path = (String)getter.valueForBinding(path);
				getter = getter.parent();
			}
    		resultGetter = getter;
    	}
    	pushToKeyPath = path;
    }
    
    public void setBase(SettingsBase b) {
    	base = b;
    	EOEditingContext ec = base.editingContext();
    	NSArray groups = EduGroup.Lister.listGroups(
    			(NSTimestamp)session().valueForKey("today"), ec);
    	if(groups != null && groups.count() > 0) {
    		Enumeration enu = groups.objectEnumerator();
    		Integer grade = null;
    		NSMutableArray byGrade = null;
    		while (enu.hasMoreElements()) {
				EduGroup gr = (EduGroup) enu.nextElement();
				if(grade == null || !grade.equals(gr.grade())) {
					grade = gr.grade();
					byGrade = (NSMutableArray)groupsByGrade.objectForKey(grade);
					if(byGrade == null) {
						grades = grades.arrayByAddingObject(grade);
						byGrade = new NSMutableArray();
						groupsByGrade.setObjectForKey(byGrade, grade);
					}
				}
				byGrade.addObject(gr);
			}
    	}
    }
    
	public NSArray groups() {
		if(gradeItem == null || groupsByGrade == null)
			return null;
		NSArray result = (NSArray)groupsByGrade.objectForKey(gradeItem);
		return result;
	}

    public NSKeyValueCoding byCourse() {
    	if(byCourse == null)
    		byCourse = new NSMutableDictionary();
    	return byCourse;
    }
    
    public void setByCourse(NSKeyValueCoding set) {
    	byCourse = set;
    	if(base == null)
    		setBase((SettingsBase)byCourse.valueForKey("settingsBase"));
    	if(set == null)
    		return;
    	groupItem = (EduGroup)byCourse.valueForKey("eduGroup");
    	if(groupItem != null) {
    		prepareCourses(groupItem);
    		groupItem = null;
    	} else {
    		coursesByCycle = null;
    	}
    	gradeItem = (Integer)byCourse.valueForKey("grade");
    	if(gradeItem != null) {
    		cycles = EduCycle.Lister.cyclesForGrade(gradeItem, base.editingContext());
    		gradeItem = null;
    	} else {
    		cycles = null;
    	}
    }
    
    public void selectGrade() {
    	if((gradeItem == null || gradeItem.equals(byCourse().valueForKey("grade")))
    			&& byCourse().valueForKey("cycle") == null ) {
    		byCourse().takeValueForKey(null, "grade");
    		if(byCourse().valueForKey("eduGroup") == null) {
    			cycles = null;
    			coursesByCycle = null;
    		}
    	} else {
    		byCourse().takeValueForKey(gradeItem, "grade");
    		byCourse().takeValueForKey(null, "eduGroup");
    		byCourse().takeValueForKey(null, "course");
    		byCourse().takeValueForKey(null, "cycle");
    		byCourse().takeValueForKey(null, "eduYear");
    		cycles = EduCycle.Lister.cyclesForGrade(gradeItem, base.editingContext());
    		coursesByCycle = null;
    	}
    }
    
    public void selectGroup() {
    	if(groupItem == null || groupItem.equals(byCourse().valueForKey("eduGroup"))
    			&& byCourse().valueForKey("course") == null) {
        	byCourse().takeValueForKey(null, "eduGroup");
        	coursesByCycle = null;
        	return;
    	}
    	if(!groupItem.grade().equals(byCourse().valueForKey("grade"))) {
    		byCourse().takeValueForKey(groupItem.grade(), "grade");
    		byCourse().takeValueForKey(null, "cycle");
    	}
    	byCourse().takeValueForKey(groupItem, "eduGroup");
    	byCourse().takeValueForKey(groupItem.eduYear(), "eduYear");
		byCourse().takeValueForKey(null, "course");
		prepareCourses(groupItem);
    }
    
    protected void prepareCourses(EduGroup group) {
    	cycles = EduCycle.Lister.cyclesForEduGroup(group);
		NSArray args = new NSArray(new Object[] {session().valueForKey("eduYear"), group});
		NSArray existingCourses = EOUtilities.objectsWithQualifierFormat(base.editingContext(),
				EduCourse.entityName,"eduYear = %d AND eduGroup = %@",args);
		if(existingCourses != null && existingCourses.count() > 0) {
			coursesByCycle = new NSMutableDictionary();
			Enumeration enu = existingCourses.objectEnumerator();
			AdaptingComparator comparator = new AdaptingComparator();
			while (enu.hasMoreElements()) {
				EduCourse crs = (EduCourse) enu.nextElement();
				NSMutableArray byCycle = (NSMutableArray)
								coursesByCycle.objectForKey(crs.cycle());
				if(byCycle == null) {
					byCycle = new NSMutableArray(crs);
					coursesByCycle.setObjectForKey(byCycle, crs.cycle());
				} else {
					byCycle.addObject(crs);
					try {
						byCycle.sortUsingComparator(comparator);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} // init coursesByCycle
		} else { // existingCourses.count() > 0
			coursesByCycle = null;
		}
    }
    
    public NSArray courses() {
    	if(coursesByCycle == null || cycleItem == null)
    		return null;
    	return (NSArray)coursesByCycle.objectForKey(cycleItem);
    }
    
    public void selectCycle() {
    	if(cycleItem == null || cycleItem.equals(byCourse().valueForKey("cycle"))) {
        	byCourse().takeValueForKey(null, "cycle");
    	} else {
    		byCourse().takeValueForKey(cycleItem.grade(), "grade");
    		byCourse().takeValueForKey(cycleItem, "cycle");
    	}
		byCourse().takeValueForKey(null, "course");
		byCourse().takeValueForKey(valueForKeyPath("byCourse.eduGroup.eduYear"), "eduYear");
    }
    
    public void selectCourse() {
    	if(courseItem == null || cycleItem.equals(byCourse().valueForKey("course"))) {
    		byCourse().takeValueForKey(null, "course");
    	} else {
    		byCourse().takeValueForKey(courseItem, "course");
    		byCourse().takeValueForKey(courseItem.eduGroup(), "eduGroup");
    		cycleItem = courseItem.cycle();
    		byCourse().takeValueForKey(cycleItem.grade(), "grade");
    		byCourse().takeValueForKey(cycleItem, "cycle");
      		byCourse().takeValueForKey(courseItem.eduYear(), "eduYear");
    	}
    }
    
    public String gradeClass() {
    	if(gradeItem == null)
    		return null;
    	if(gradeItem.equals(byCourse().valueForKey("grade")))
    		return "selection";
    	if((gradeItem.intValue() & 1) == 0)
    		return "gerade";
    	else
    		return "ungerade";
    }
    
    public String groupClass() {
    	if(groupItem == null)
    		return null;
    	if(groupItem.equals(byCourse().valueForKey("eduGroup")))
    		return "selection";
    	if((groupItem.grade().intValue() & 1) == 0)
    		return "gerade";
    	else
    		return "ungerade";
    }
    
    public String cycleClass() {
    	if(cycleItem == null)
    		return null;
    	if(cycleItem.equals(byCourse().valueForKey("cycle")))
    		return "selection";
    	return "grey";
     }
    
    public String courseClass() {
    	if(courseItem == null)
    		return null;
    	if(courseItem.equals(byCourse().valueForKey("course")))
    		return "selection";
    	return "male";
     }
    
    public String onclick() {
		String href = context().componentActionURL();
		String result = "ajaxPopupAction('" + href + "');";
		return result;
    }
    
    public boolean forceEduYear() {
    	return (byCourse().valueForKey("course") != null || 
    			valueForKeyPath("byCourse.eduGroup.eduYear") != null);
    }
    
    public WOActionResults save() {
    	returnPage.ensureAwakeInContext(context());
    	if(byCourse == null)
    		return returnPage;
    	EOEditingContext ec = base.editingContext();
    	ec.lock();
    	try {
    		NSComparator comparator = new SettingsBase.Comparator();
    		if(base.byCourse() != null && base.byCourse().count() > 0) {
    			Enumeration enu = base.byCourse().objectEnumerator();
    			Integer eduYear = (Integer)session().valueForKey("eduYear");
    			while (enu.hasMoreElements()) {
					EOEnterpriseObject bc = (EOEnterpriseObject) enu.nextElement();
					if(bc == byCourse)
						continue;
					if(bc.valueForKey("eduYear") != null 
							&& !eduYear.equals(bc.valueForKey("eduYear")))
						continue;
					if(comparator.compare(bc, byCourse) == NSComparator.OrderedSame) {
    					session().takeValueForKey(application().valueForKeyPath(
    					"strings.RujelBase_Base.SettingsBase.duplicateByCourse"), "message");
    					if(ec.hasChanges())
    						ec.revert();
//    					ec.unlock();
    					return returnPage;
					}
				}
    		} // search for same
			if(byCourse instanceof NSMutableDictionary) {
				EOEnterpriseObject bc = EOUtilities.createAndInsertInstance(ec,"SettingByCourse");
				String text = (String)byCourse.valueForKey(SettingsBase.TEXT_VALUE_KEY);
				Integer num = (Integer)byCourse.valueForKey(SettingsBase.NUMERIC_VALUE_KEY);
				if(text == null || null == null) {
					Integer year = (Integer)byCourse.valueForKey("eduYear");
					if(year == null)
						year = (Integer)session().valueForKey("eduYear");
					Object value = byCourse.valueForKey("course");
					if(value == null)
						value = byCourse;
					EOEnterpriseObject parent = base.forValue(value, year);
					if(text == null) {
						text = (String)parent.valueForKey(SettingsBase.TEXT_VALUE_KEY);
						bc.takeValueForKey(text, SettingsBase.TEXT_VALUE_KEY);
					}
					if(num == null) {
						num = (Integer)parent.valueForKey(SettingsBase.NUMERIC_VALUE_KEY);
						bc.takeValueForKey(num, SettingsBase.NUMERIC_VALUE_KEY);
					}					
				}
				bc.addObjectToBothSidesOfRelationshipWithKey(base, "settingsBase");
				bc.takeValuesFromDictionary((NSDictionary)byCourse);
				byCourse = bc;
			} // create from dict
			if(editList != null) {
				if(editList.containsObject(byCourse)) {
					editList.sortUsingComparator(comparator);
				} else {
					boolean done = false;
					for (int i = 0; i < editList.count(); i++) {
						int res = comparator.compare(editList.objectAtIndex(i), byCourse);
						if(res == NSComparator.OrderedDescending) {
							editList.insertObjectAtIndex(byCourse, i);
							done = true;
							break;
						}
					}
					if(!done)
						editList.addObject(byCourse);
				} // insert into list
			}
			if(pushToKeyPath != null) {
				if(resultGetter == null)
					resultGetter = returnPage;
				resultGetter.takeValueForKeyPath(byCourse, pushToKeyPath);
			}
			ec.saveChanges();
			logger.log(WOLogLevel.COREDATA_EDITING,"Edited SettingByCourse: " + base.key(),
					new Object[] {session(),byCourse});
    	} catch (Exception e) {
			logger.log(WOLogLevel.INFO,"Failed editing SettingByCourse: " + base.key(),
					new Object[] {session(),byCourse,e});
    		session().takeValueForKey(e.getMessage(), "message");
    		ec.revert();
    	} finally {
    		ec.unlock();
    	}
    	return returnPage;
    }
}