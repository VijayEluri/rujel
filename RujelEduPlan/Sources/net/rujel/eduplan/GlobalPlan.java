// GlobalPlan.java: Class file for WO Component 'GlobalPlan'

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


import java.text.DecimalFormat;
import java.util.Enumeration;

import net.rujel.base.MyUtility;
import net.rujel.base.ReadAccess;
import net.rujel.base.SchoolSection;
import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EduGroup;
import net.rujel.reusables.*;
import net.rujel.ui.RedirectPopup;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.foundation.NSComparator.ComparisonException;
import com.webobjects.appserver.WOActionResults;

// Generated by the WOLips Templateengine Plug-in at Jul 15, 2008 8:08:00 PM
public class GlobalPlan extends com.webobjects.appserver.WOComponent {
	
	public ReadAccess globalAccess;
	protected ReadAccess _access;
	public ReadAccess access() {
		if(_access == null) {
			_access = new ReadAccess(session());
			_access.relObject = this;
			_access.sectionPath = "inSection";
			_access.initKey = "EduPlan";
		}
		return _access;
	}
	
	public GlobalPlan(WOContext context) {
        super(context);
//        	session().savePageInPermanentCache(this);
        showTotal = SettingsReader.intForKeyPath("edu.showTotal", 0);
    }
	
    public boolean synchronizesVariablesWithBindings() {
        return false;
	}
    
    protected NSArray prepareGrades() {
    	if(inSection == null)
    		return sections;
		NSMutableArray prepareGrades = new NSMutableArray();
		int maxGrade = inSection.maxGrade();
		int minGrade = inSection.minGrade();
		for (int i = minGrade; i <= maxGrade; i++) {
			Integer grade = new Integer(i);
			prepareGrades.addObject(grade);
		}
        return prepareGrades.immutableClone();
	}
	
	public EOEditingContext ec;
	public NSArray grades;
	
	public NSMutableDictionary subjectItem;
	public Object gradeItem;
//	public boolean editable = true;
	public int showTotal;
	public NSMutableArray subjects;
	public int index;
	public Boolean noDetails;
	public SchoolSection inSection;
	public NSArray sections;
	public NSKeyValueCoding item;
	public int[] summary;
	public double[] sumAdditions;
	
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		if(globalAccess == null) {
			globalAccess = new ReadAccess(session());
			globalAccess.relObject = null;
			globalAccess.initKey = "EduPlan";
		}
		if(ec == null || Various.boolForObject(valueForBinding("shouldReset"))) {
	        ec = (EOEditingContext)aContext.page().valueForKey("ec");
	        if(ec.hasChanges())
	        	ec.revert();
	        sections = SchoolSection.listSections(ec, false);
	        if(grades != null && grades.objectAtIndex(0) instanceof SchoolSection) {
	        	grades = sections;
	        } else {
	        	if(inSection == null) {
	        		inSection = (SchoolSection)session().valueForKeyPath("state.section");
	        		if(inSection != null)
	        			inSection = (SchoolSection)EOUtilities.localInstanceOfObject(ec, inSection);
	        	}
	        	if(inSection == null) {
	        		inSection = (SchoolSection)sections.objectAtIndex(0);
	        	}
	        	grades = prepareGrades();
	        }
		  	subjects = prepareAgregate();
		  	subjectItem = null;
			setValueForBinding(Boolean.FALSE, "shouldReset");
		} else if(ec.hasChanges()) {
        	ec.revert();
		}
		index = 0;
        summary = new int[grades.count()];
        sumAdditions = null;
		super.appendToResponse(aResponse, aContext);
	}
	
	public void setIndex(Number value) {
		index = (value == null)?-1:value.intValue();
	}
/*
	public EOEnterpriseObject area() {
		Boolean show = (Boolean)valueForKeyPath("subjectItem.showArea");
		if(show == null || (showAll && !show.booleanValue()))
			return null;
		return (EOEnterpriseObject)subjectItem.valueForKeyPath("subjectEO.area");
	}*/
	
	protected NSMutableArray prepareAgregate() {
		noDetails = Boolean.TRUE;
		if(inSection != null && globalAccess.cachedAccessForObject("PlanDetail", 
				inSection.sectionID()).flagForKey("read") &&
				SettingsBase.baseForKey(EduPeriod.ENTITY_NAME, ec, false) != null) {
			String listName = SettingsBase.stringSettingForCourse(EduPeriod.ENTITY_NAME, null, ec);
			NSArray found = EduPeriod.periodsInList(listName, ec);
			noDetails = Boolean.valueOf(found == null || found.count() == 0);
		}
		NSArray planHours;
		{
			EOQualifier qual = (inSection == null)? null: 
				new EOKeyValueQualifier(PlanHours.SECTION_KEY,
					EOQualifier.QualifierOperatorEqual, inSection);
			NSArray sorter = new NSArray(new EOSortOrdering("subjectID",
					EOSortOrdering.CompareAscending));
			EOFetchSpecification fs = new EOFetchSpecification(PlanHours.ENTITY_NAME,qual,sorter);
			planHours = ec.objectsWithFetchSpecification(fs);
		}
		NSMutableArray agregate = new NSMutableArray();
		Enumeration areas = null;
	  	if(planHours == null || planHours.count() == 0) {
	  		EOQualifier qual = new EOKeyValueQualifier(Subject.FLAGS_KEY,
	  				EOQualifier.QualifierOperatorLessThan,Integer.valueOf(32));
	  		if(inSection != null) {
	  			EOQualifier[] quals = new EOQualifier[2];
	  			quals[0] = new EOKeyValueQualifier(Subject.SECTION_KEY,
	  					EOQualifier.QualifierOperatorEqual, inSection);
	  			quals[1] = new EOKeyValueQualifier(Subject.SECTION_KEY,
	  					EOQualifier.QualifierOperatorEqual, NullValue);
	  			quals[0] = new EOOrQualifier(new NSArray(quals));
	  			quals[1] = qual;
	  			qual = new EOAndQualifier(new NSArray(quals));
	  		}
			EOFetchSpecification fs = new EOFetchSpecification(Subject.ENTITY_NAME,
					qual,Subject.sorter);
			NSArray found = ec.objectsWithFetchSpecification(fs);
		  	Enumeration enu = found.objectEnumerator();
		  	while (enu.hasMoreElements()) {
				Subject subj = (Subject) enu.nextElement();
				NSMutableDictionary dict = new NSMutableDictionary(
						subj, Subject.ENTITY_NAME);
//				dict.takeValueForKey(subj.subject(), Subject.SUBJECT_KEY);
				dict.takeValueForKey(new Object[grades.count()], "planHours");
//				dict.takeValueForKey(new Counter(0), "counter");
				agregate.addObject(dict);
			}
			fs = new EOFetchSpecification("SubjectArea",null,MyUtility.numSorter);
			areas = ec.objectsWithFetchSpecification(fs).objectEnumerator();
	  	} else { // if(planHours == null || planHours.count() == 0)
		  	Enumeration enu = planHours.objectEnumerator();
			Subject subj = null;
		  	Object[] hours = null;
		  	while (enu.hasMoreElements()) {
		  		PlanHours ph = (PlanHours) enu.nextElement();
		  		if(ph.eduSubject() != subj) {
		  			subj = ph.eduSubject();
		  			if(forcedSubjects != null)
		  				forcedSubjects.removeObject(subj);
					NSMutableDictionary dict = new NSMutableDictionary(subj, Subject.ENTITY_NAME);
		  			hours = new Object[grades.count()];
					dict.takeValueForKey(hours, "planHours");
//					dict.takeValueForKey(new Counter(0), "counter");
					dict.takeValueForKey(Boolean.TRUE, PlanHours.ENTITY_NAME);
					agregate.addObject(dict);
		  		}
		  		int idx = grades.indexOf((inSection==null)?ph.section():ph.grade());
		  		if(idx < 0) {
		  			// oops!
		  		} else if(inSection==null) {
		  			if(hours[idx] instanceof Counter)
		  				((Counter)hours[idx]).raise();
			  		else
		  				hours[idx]=new Counter(1);
		  		} else { 
		  			hours[idx] = ph;
		  		}
		  	}
		  	if(forcedSubjects != null && forcedSubjects.count() > 0) {
		  		enu = forcedSubjects.objectEnumerator();
		  		while (enu.hasMoreElements()) {
					EOEnterpriseObject s = (EOEnterpriseObject) enu.nextElement();
					if(s.editingContext() == null)
						continue;
					NSMutableDictionary dict = new NSMutableDictionary(s, Subject.ENTITY_NAME);
					dict.takeValueForKey(new PlanHours[grades.count()], "planHours");
					agregate.addObject(dict);
//					Various.addToSortedList(dict, agregate,
//							Subject.ENTITY_NAME, EOSortOrdering.CompareAscending);
				}
		  	}
		  	if(Various.boolForObject(context().userInfoForKey("SubjectArea"))) {
				EOFetchSpecification fs = new EOFetchSpecification(
						"SubjectArea",null,MyUtility.numSorter);
				areas = ec.objectsWithFetchSpecification(fs).objectEnumerator();
		  	}
	  	} // if (planHours.count() > 0)
	  	NSArray sorter = new NSArray(new EOSortOrdering(Subject.ENTITY_NAME, 
	  			EOSortOrdering.CompareAscending));
	  	EOSortOrdering.sortArrayUsingKeyOrderArray(agregate, sorter);
	  	processAgregate(agregate, areas);
	  	return agregate;
	}

	protected void processAgregate(NSMutableArray agregate, Enumeration areas) {
		EOEnterpriseObject currarea = null;
		SubjectGroup currGroup = null;
		Counter groupCounter = null;
		for (int i = 0; i < agregate.count(); i++) {
			NSMutableDictionary dict = (NSMutableDictionary) agregate.objectAtIndex(i);
			Subject subjectEO = (Subject)dict.valueForKey(Subject.ENTITY_NAME);
			Object[] hours = (Object[])dict.valueForKey("planHours");
			Counter cnt = new Counter();
			if(hours != null) {
				for (int j = 0; j < hours.length; j++) {
					if(hours[j] != null)
						cnt.raise();
				}
			}
			while(subjectEO.area() != currarea) {
				if(areas != null && areas.hasMoreElements())
					currarea = (EOEnterpriseObject) areas.nextElement();
				else
					currarea = subjectEO.area();
				NSMutableDictionary areaDict = new NSMutableDictionary(currarea,Subject.AREA_KEY);
				areaDict.takeValueForKey("orange", "styleClass");
				areaDict.takeValueForKey(Boolean.TRUE, "noData");
				currGroup = (SubjectGroup)currarea.valueForKey("subjectGroup");
				areaDict.takeValueForKey(currGroup, Subject.SUBJECT_GROUP_KEY);
				groupCounter = new Counter(1);
				areaDict.takeValueForKey(groupCounter, "rowspan");
				areaDict.takeValueForKey(MyUtility.getID(currarea), "anchor");
				agregate.insertObjectAtIndex(areaDict, i);
//				if(cnt.intValue() == 0 && currarea == subjectEO.area())
//					areaDict.takeValueForKey(Boolean.TRUE, "showUnused");
				i++;
			}
			if(subjectEO.subjectGroup() != currGroup) {
				SubjectGroup group = subjectEO.subjectGroup();
				dict.takeValueForKey(group, Subject.SUBJECT_GROUP_KEY);
				SubjectGroup parent = group.parent();
				int j = i;
				while(parent != null && (currGroup == null || !currGroup.path().containsObject(parent))) {
					NSMutableDictionary groupDict = new NSMutableDictionary(
							parent,Subject.SUBJECT_GROUP_KEY);
					groupDict.takeValueForKey(Boolean.TRUE, "noData");
					agregate.insertObjectAtIndex(groupDict, j);
					i++;
					parent = parent.parent();
				}
				currGroup = group;
				groupCounter = new Counter(1);
				dict.takeValueForKey(groupCounter, "rowspan");
				dict.takeValueForKey(null, "noGroup");
			} else {
				groupCounter.raise();
				dict.takeValueForKey(Boolean.TRUE, "noGroup");
			}
			String styleClass = "female";
			if(subjectEO.namedFlags().flagForKey("hidden"))
				styleClass = "grey";
			else if(subjectEO.section() == null)
				styleClass = "ungerade";
			else if(inSection == null || subjectEO.section() == inSection)
				styleClass = "gerade";
//			else
//				styleClass = "female";
			dict.takeValueForKey(styleClass, "styleClass");
		}
		if(areas != null) {
			while (areas.hasMoreElements()) {
				EOEnterpriseObject area = (EOEnterpriseObject) areas.nextElement();
				NSMutableDictionary areaDict = new NSMutableDictionary(area,Subject.AREA_KEY);
				areaDict.takeValueForKey("orange", "styleClass");
				areaDict.takeValueForKey(Boolean.TRUE, "noData");
				areaDict.takeValueForKey(area.valueForKey("subjectGroup"), Subject.SUBJECT_GROUP_KEY);
				areaDict.takeValueForKey(new Counter(1), "rowspan");
				areaDict.takeValueForKey(MyUtility.getID(area), "anchor");
				agregate.addObject(areaDict);
			}
		}
	}
	
	public boolean hasSection() {
		return (inSection != null);
	}
	
	public String sectionClass() {
		if(inSection == item)
			return "selection";
		else if(item == null)
			return "grey";
		return "gerade";
	}
	
	public WOActionResults selectSection() {
		inSection = (SchoolSection)item;
		grades = prepareGrades();
	  	subjects = prepareAgregate();
	  	subjectItem = null;
	  	forcedSubjects.removeAllObjects();;
		return null;
	}
	
	protected NSMutableSet forcedSubjects = new NSMutableSet();
//	public boolean showAll = false;
	public boolean showRow() {
		if(Various.boolForObject(subjectItem.valueForKey("counter")))
			return true;
		return (forcedSubjects != null && forcedSubjects.containsObject(subjectItem));
	}
	
	public void setForced(Object forced) {
		forcedSubjects.addObject(forced);
	}

	public Boolean editable() {
		if(inSection == null)
			return Boolean.FALSE;
		if(planHoursEO() == null) {
			return (Boolean)access().valueForKey("create");
		} else {
			return (Boolean)access().valueForKey("edit");
		}
	}
	
	protected static DecimalFormat fmt = new DecimalFormat("0.0#");
	public String planHours() {
		if(inSection == null) {
			Counter cnt = (Counter)planHoursObject();
			return (cnt==null)?null:Integer.toString(cnt.intValue());
		}
		PlanHours ph = planHoursEO();
		if(ph == null)
			return null;
		Integer year = (Integer)session().valueForKey("eduYear"); 
		int[] wd = ph.weeksAndDays(year);
		Integer total = (Integer)ph.valueForKey("totalHours");
		Integer weekly = (Integer)ph.valueForKey("weeklyHours");
		if (showTotal > 0) {
			if(total == null || total.intValue() <= 0) {
				if(weekly == null || weekly.intValue() <= 0)
					return null;
				int count = weekly.intValue();
				if(count > 1) {
					count = wd[0]*count + count*wd[1]/wd[2];
				} else {
					count = wd[0];
				}
				StringBuilder buf = new StringBuilder(5);
				buf.append(' ').append(count).append(' ');
				summary[index] +=count;
				return buf.toString();
			} else {
				summary[index] += total.intValue();
				return total.toString();
			}
		} else {
			if(weekly != null && weekly.intValue() > 0) {
				summary[index] += weekly.intValue();
				return weekly.toString();
			} else {
				if(total == null || total.intValue() <= 0)
					return null;
				double hours = total.doubleValue();
				double weeks = (double)wd[0] + (double)wd[1]/wd[2];
				hours = hours/weeks;
				if(sumAdditions == null)
					sumAdditions = new double[grades.count()];
				sumAdditions[index] += hours;
				return fmt.format(hours);
			}
		}
	}
	
	public void setPlanHours(String aHours) {
		Object[] planHours = (Object[])subjectItem.valueForKey("planHours");
		Integer hours = null;
		if(aHours != null) {
			try {
				hours = Integer.decode(aHours);
				if(hours.intValue() == 0)
					hours = null;
			} catch (NumberFormatException e) {
				return;
			}
		}
		PlanHours ph = (PlanHours)planHours[index];
		if(hours != null) {
			if(ph == null) { // create cycle
				ph = PlanHours.getPlanHours(inSection, (Subject)
						subjectItem.valueForKey(Subject.ENTITY_NAME), (Integer)gradeItem, true);
				subjectItem.valueForKeyPath("counter.raise");
				planHours[index]=ph;
			}
			String key = (showTotal == 0)? "weeklyHours" : "totalHours";
			String keyNot = (showTotal != 0)? "weeklyHours" : "totalHours";
			ph.takeValueForKey(hours, key);
			ph.takeValueForKey(new Integer(0), keyNot);
			subjectItem.takeValueForKey(Boolean.TRUE, PlanHours.ENTITY_NAME);
		} else { // hours == null
			if(ph != null) { // delete planHours
				ec.deleteObject(ph);
				subjectItem.valueForKeyPath("counter.lower");
//				if(count == null || count.intValue() <= 0)
					setValueForBinding(Boolean.TRUE, "shouldReset");
			}
		}
	}
	
	public String sumHours() {
		if (sumAdditions == null) {
			if(summary[index] > 0)
				return Integer.toString(summary[index]);
		} else {
			double value = sumAdditions[index] + summary[index];
			if(value > 0)
				return fmt.format(value);
		}
		return null;
	}
	
	public void save() {
		try {			
			/*NSMutableArray changes = new NSMutableArray();
			NSArray tmp = ec.insertedObjects();
			if(tmp != null && tmp.count() > 0) {
				changes.addObject("added");
				changes.addObjectsFromArray(ec.insertedObjects());
			}
			tmp = ec.updatedObjects();
			if(tmp != null && tmp.count() > 0) {
				changes.addObject("updated");
				changes.addObjectsFromArray(ec.insertedObjects());
			}
			tmp = ec.deletedObjects();
			if(tmp != null && tmp.count() > 0) {
				changes.addObject("deleted");
				changes.addObjectsFromArray(ec.insertedObjects());
			}
			Object[] args = new Object[] {session(),changes};*/
			if(ec.hasChanges()) {
				ec.saveChanges();
				EduPlan.logger.log(WOLogLevel.EDITING,"Saved changes in EduPlan",session());
			}
			EOQualifier qual = new EOKeyValueQualifier(PlanHours.ENTITY_NAME,
					EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
			EOQualifier.filterArrayWithQualifier(subjects, qual);
			processAgregate(subjects, null);
		} catch (Exception ex) {
			Object[] args = new Object[] {session(),ex};
			EduPlan.logger.log(WOLogLevel.WARNING,"Failed to save changes",args);
			String message = (String)application().
					valueForKeyPath("strings.Strings.messages.error") + "<br/>" + ex.toString();
			session().takeValueForKey(message, "message");
		}
	}
	
	public String gradeTitle() {
		if(gradeItem == null)
			return null;
		if(gradeItem instanceof SchoolSection)
			return ((SchoolSection)gradeItem).sectionID().toString();
		else
			return gradeItem.toString();
	}
	
	public String cellClass() {
		if(inSection == null) {
			Object ph = planHoursObject();
			if(ph == null)
				return "grey";
			Subject subj = (Subject)subjectItem.valueForKey(Subject.ENTITY_NAME);
			SchoolSection sect = subj.section();
			if(sect != null && sect != gradeItem)
				return "female";
		}
		return null;
	}
	
	
	protected PlanHours planHoursEO() {
		Object ph = planHoursObject();
		if(ph instanceof PlanHours)
			return (PlanHours)ph;
		else
			return null;
	}
	
	protected Object planHoursObject() {
		if(subjectItem == null || index < 0 || index >= grades.count())
			return null;
		Object[] planHours = (Object[])subjectItem.valueForKey("planHours");
		return planHours[index];
	}
	
	public String cellStyle() {
		if(!editable()) {
			EOEnterpriseObject ph = planHoursEO();
			if(ph == null)
				return null;
			String key = (showTotal == 0)? "weeklyHours": "totalHours";
			Integer value = (Integer)ph.valueForKey(key);
			if(value == null || value.intValue() <= 0) {
				 return "color:#999999;";
			}
		}
		return null;
	}
	
	public String fieldStyle() {
		EOEnterpriseObject ph = planHoursEO();
		if(ph == null)
			return null;
		if(gradeItem instanceof EduGroup && ph.valueForKey("specClass") == null)
			return "style = \"color:#999999;\" onfocus = \"style.color='#000000';\" onblur = \"if(value==defaultValue)style.color='#999999';\"";
		String key = (showTotal == 0)? "weeklyHours": "totalHours";
		Integer value = (Integer)ph.valueForKey(key);
		if(value == null || value.intValue() <= 0)
			return "style = \"color:#999999;\" onfocus = \"style.color='#000000';\" onblur = \"if(value==defaultValue)style.color='#999999';\"";
		return null;
	}
	
	public WOActionResults showUnusedSubjects() {
		EOEnterpriseObject area = (EOEnterpriseObject)valueForKeyPath("subjectItem.area");
		EOQualifier qual =  new EOKeyValueQualifier(Subject.AREA_KEY, 
				EOQualifier.QualifierOperatorEqual, area);
		if(inSection != null) {
			EOQualifier[] quals = new EOQualifier[2];
			quals[0] = new EOKeyValueQualifier(Subject.SECTION_KEY,
					EOQualifier.QualifierOperatorEqual, inSection);
			quals[1] = new EOKeyValueQualifier(Subject.SECTION_KEY,
					EOQualifier.QualifierOperatorEqual, NullValue);
			quals[0] = new EOOrQualifier(new NSArray(quals));
			quals[1] = qual;
			qual = new EOAndQualifier(new NSArray(quals));
		}
		EOFetchSpecification fs = new EOFetchSpecification(Subject.ENTITY_NAME,qual,Subject.sorter);
		NSArray found = ec.objectsWithFetchSpecification(fs);
		if(found.count() > 1) {
			try {
				found = found.sortedArrayUsingComparator(new SubjectComparator());
			} catch (ComparisonException e) {
				EduPlan.logger.log(WOLogLevel.WARNING, "Error sorting subjects",
						new Object[] {session(),e});
			}
		}
	  	Enumeration enu = found.objectEnumerator();

	  	int idx = subjects.indexOfIdenticalObject(subjectItem) +1;
	  	NSMutableDictionary row = (subjects.count() <= idx)?null: 
	  			(NSMutableDictionary)subjects.objectAtIndex(idx);
		SubjectGroup currGroup = (SubjectGroup)area.valueForKey("subjectGroup");
		Counter groupCounter = (Counter)subjectItem.valueForKey("rowspan");
		NSMutableArray passed = new NSMutableArray();
	  	final int oldCount = subjects.count();
	  	while (enu.hasMoreElements()) {
			Subject subj = (Subject) enu.nextElement();
			NSMutableDictionary dict = new NSMutableDictionary(subj, Subject.ENTITY_NAME);
			if(row != null) {
				try {
					while (isPrevRowForSubject(row, subj,passed)) {
						SubjectGroup gr = (SubjectGroup)row.valueForKey(Subject.SUBJECT_GROUP_KEY);
						if(gr != null) {
							groupCounter = (Counter)row.valueForKey("rowspan");
							currGroup = gr;
						}
						idx++;
//						if(idx >= subjects.count()) {
//							row = null;
//							break;
//						}
						row = (NSMutableDictionary)subjects.objectAtIndex(idx);
					}
					Subject rowSubj = (Subject)row.valueForKey(Subject.ENTITY_NAME);
					if(subj == rowSubj || passed.containsObject(subj)
							||(subj.namedFlags().flagForKey("hidden") && 
							  !globalAccess.cachedAccessForObject(subj,null).flagForKey("edit"))) {
						continue;
					} else if (rowSubj == null) {
						if(row.valueForKey(Subject.AREA_KEY) != null)
							row = null;
						else if (subj.subjectGroup() == row.valueForKey(Subject.SUBJECT_GROUP_KEY)) {
							dict = row;
							dict.takeValueForKey(subj, Subject.ENTITY_NAME);
							dict.takeValueForKey(Boolean.FALSE, "noData");
						}
				  	} else if(subj.subjectGroup() == row.valueForKey(Subject.SUBJECT_GROUP_KEY)) {
						row.takeValueForKey(Boolean.TRUE, "noGroup");
				  		currGroup = subj.subjectGroup();
				  		groupCounter = (Counter)row.removeObjectForKey("rowspan");
				  		dict.takeValueForKey(groupCounter, "rowspan");
				  		row.removeObjectForKey(Subject.SUBJECT_GROUP_KEY);
				  		dict.takeValueForKey(currGroup, Subject.SUBJECT_GROUP_KEY);
				  	}
				} catch (ComparisonException e) {
					EduPlan.logger.log(WOLogLevel.WARNING, "Error comparing subjects",
							new Object[] {session(),e});
				} catch (IllegalArgumentException e) {
					// came to the end of list. ok
					if(idx >= subjects.count())
						row = null;
					else throw e;
				}
			}
			Object[] planHours = new Object[grades.count()];
			dict.takeValueForKey(planHours, "planHours");
			if(inSection == null && subj.section() != null) {
				int i = grades.indexOfIdenticalObject(subj.section());
				if(i >= 0)
					planHours[i] = new Counter(0);
			}
//			dict.takeValueForKey(new Counter(0), "counter");
			String styleClass = "female";
			if(subj.namedFlags().flagForKey("hidden"))
				styleClass = "grey";
			else if(subj.section() == null)
				styleClass = "ungerade";
			else if(inSection == null || subj.section() == inSection)
				styleClass = "gerade";
			dict.takeValueForKey(styleClass, "styleClass");
			if(dict == row)
				continue;
			if(subj.subjectGroup() != currGroup) {
				SubjectGroup group = subj.subjectGroup();
				dict.takeValueForKey(group, Subject.SUBJECT_GROUP_KEY);
				SubjectGroup parent = group.parent();
				int j = idx;
				while(parent != null && (currGroup == null || !currGroup.path().containsObject(parent))) {
					NSMutableDictionary groupDict = new NSMutableDictionary(
							parent,Subject.SUBJECT_GROUP_KEY);
					groupDict.takeValueForKey(Boolean.TRUE, "noData");
					subjects.insertObjectAtIndex(groupDict, j);
					idx++;
					parent = parent.parent();
				}
				currGroup = group;
				groupCounter = new Counter(1);
				dict.takeValueForKey(groupCounter, "rowspan");
			} else {
				groupCounter.raise();
				if(dict.valueForKey("rowspan") == null)
					dict.takeValueForKey(Boolean.TRUE, "noGroup");
			}
			if(idx >= subjects.count())
				subjects.addObject(dict);
			else
				subjects.insertObjectAtIndex(dict, idx);
			idx++;
		} //found subjects in Area enumeration
//	  	subjectItem.takeValueForKey(Boolean.TRUE, "showUnused");
	  	if(subjects.count() > oldCount) {
	  		return RedirectPopup.getRedirect(context(), context().page());
	  	} else {
	  		return addSubject();
	  	}
	}
	
	private boolean isPrevRowForSubject(NSDictionary row,Subject subj,NSMutableArray passed)
			throws ComparisonException {
	  	Subject rowSubj = (Subject)row.valueForKey(Subject.ENTITY_NAME);
	  	if(rowSubj == null) {
	  		if(row.valueForKey(Subject.AREA_KEY) != null)
	  			return false;
	  		SubjectGroup sg = (SubjectGroup)row.valueForKey(Subject.SUBJECT_GROUP_KEY);
	  		if(sg == null)
	  			return false;
	  		return (SubjectGroupComparator.compare(sg, subj.subjectGroup()) < 0);
	  	} else if(rowSubj == subj) {
				return false;
	  	} else {
	  		int val = SubjectComparator.compare(rowSubj, subj);
	  		if(val == 0) {
	  			passed.addObject(rowSubj);
	  		} else if (passed.count() > 0) {
	  			Subject ps = (Subject)passed.lastObject();
	  			if(SubjectComparator.compare(ps, subj) != 0)
	  				passed.removeAllObjects();
	  		}
	  		return val <= 0;
	  	}
	}
	
	public WOComponent editSubject() {
		WOComponent popup = pageWithName("SubjectEditor");
		Object subj = subjectItem.valueForKey(Subject.ENTITY_NAME);
		setForced(subj);
		popup.takeValueForKey(subj, "subject");
		popup.takeValueForKey(context().page(), "returnPage");
//		popup.takeValueForKey(globalAccess.valueForKey("_edit.subjectItem.Subject"), "cantChange");
		subjectItem = null;
		return popup;
	}
	/*
	public String addSubjectOnclick() {
		String href = context().componentActionURL();
		if(Various.boolForObject(valueForKeyPath("subjectItem.showUnused")))
			return "ajaxPopupAction('" + href + "');";
		else
			return "return checkRun('" + href + '#' + 
					valueForKeyPath("subjectItem.anchor") + "');";
	}*/
	
	public WOActionResults addSubject() {
		Subject subject = (Subject)EOUtilities.createAndInsertInstance(ec, "Subject");
		subject.setSection(inSection);
		subject.setArea((EOEnterpriseObject)subjectItem.valueForKey(Subject.AREA_KEY));
		SubjectGroup sg = (SubjectGroup)subjectItem.valueForKey(Subject.SUBJECT_GROUP_KEY);
		if(sg != null) {
			subject.setSubjectGroup(sg);
			subject.setSubject(sg.name());
		}
		setForced(subject);
		WOComponent popup = pageWithName("SubjectEditor");
		popup.takeValueForKey(subject, "subject");
		popup.takeValueForKey(context().page(), "returnPage");
		return popup;
	}

	public WOComponent editArea() {
		WOComponent popup = pageWithName("AreaEditor");
		popup.takeValueForKey(context().page(), "returnPage");
		popup.takeValueForKey(subjectItem.valueForKey(Subject.AREA_KEY), "area");
		subjectItem = null;
		return popup;
	}
	public WOComponent addArea() {
		EOFetchSpecification fs = new EOFetchSpecification("SubjectArea",null,MyUtility.numSorter);
		NSArray areas = ec.objectsWithFetchSpecification(fs);
		int count = areas.count();
		Enumeration enu = subjects.objectEnumerator();
		while (enu.hasMoreElements()) {
			NSDictionary dict = (NSDictionary) enu.nextElement();
			if(dict.valueForKey(Subject.AREA_KEY) != null)
				count--;
		}
		if(count > 0) {
			EOQualifier qual = new EOKeyValueQualifier(Subject.ENTITY_NAME,
					EOQualifier.QualifierOperatorNotEqual, NullValue);
			EOQualifier.filterArrayWithQualifier(subjects, qual);
			processAgregate(subjects,areas.objectEnumerator());
			return RedirectPopup.getRedirect(context(), context().page());
		} else {
			WOComponent popup = pageWithName("AreaEditor");
			popup.takeValueForKey(context().page(), "returnPage");
			popup.takeValueForKey(ec, "editingContext");
			popup.takeValueForKey(context().page(),"returnPage");
			subjectItem = null;
			return popup;
		}
	}

	public int colspan() {
		return (grades.count() + 4); 
	}
	/*
	public String toggleAllTitle() {
		String result = null;
		if(showAll) {
			result = (String)application().valueForKeyPath("strings.RujelEduPlan_EduPlan.hideUnused");
			if(result == null)
				result = "Hide unused";
		} else {
			result = (String)application().valueForKeyPath("strings.RujelEduPlan_EduPlan.showAll");
			if(result == null)
				result = "Show all";
		}
		return result;
	}*/
	
	public WOActionResults details() {
		NSMutableDictionary dict = new NSMutableDictionary("PlanDetails","component");
		dict.takeValueForKey(session().valueForKeyPath(
				"strings.RujelEduPlan_EduPlan.PlanDetails"),"title");
		dict.takeValueForKey(valueForKeyPath("subjectItem.Subject"), "selection");
		dict.takeValueForKey(new Integer(showTotal), "showTotal");
		dict.takeValueForKey(inSection, "inSection");
//		parent().takeValueForKey(dict, "currTab");
		setValueForBinding(dict, "dict");
		dict.takeValueForKey(_access, "access");
//		session().takeValueForKey(context().page(), "pushComponent");
		return performParentAction("revertEc");
	}
	
	public WOActionResults subjectGroups() {
		NSMutableDictionary dict = new NSMutableDictionary("SubjectGroupEditor","component");
		dict.takeValueForKey(session().valueForKeyPath(
				"strings.RujelEduPlan_EduPlan.properties.subjectGroup"),"title");
		setValueForBinding(dict, "dict");
		dict.takeValueForKey(globalAccess, "access");
		dict.takeValueForKey("RujelBase", "resourcesFramework");
		dict.takeValueForKey("sorting.js", "scriptResource");
		return performParentAction("revertEc");
	}
	
	public WOActionResults setupSections() {
		WOComponent result = pageWithName("SectionsSetup");
		result.takeValueForKey(context().page(), "returnPage");
		return result;
	}
	
	public Boolean noSections() {
		if (sections == null || sections.count() < 2)
			return Boolean.TRUE;
		Integer sect = null;
		if(item instanceof SchoolSection)
			sect = ((SchoolSection)item).sectionID();
		return !globalAccess.cachedAccessForObject("EduPlan", sect).flagForKey("read");
	}

	public Integer headerColspan() {
		int colspan = 1;
		if(hasSection())
			colspan++;
		colspan+=grades.count();
		if(!noDetails)
			colspan++;
		return Integer.valueOf(colspan);
	}
	
	public String gradeHover() {
		if(gradeItem instanceof SchoolSection)
			return ((SchoolSection)gradeItem).name();
		return null;
	}
}