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

import net.rujel.base.ReadAccess;
import net.rujel.base.SchoolSection;
import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EduGroup;
import net.rujel.reusables.*;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.WOActionResults;

// Generated by the WOLips Templateengine Plug-in at Jul 15, 2008 8:08:00 PM
public class GlobalPlan extends com.webobjects.appserver.WOComponent {
	
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
		NSArray specGroups = EOUtilities.objectsWithQualifierFormat(ec,
				"PlanHours", "specClass != nil", null);
		if(specGroups != null && specGroups.count() > 0) {
			specGroups = (NSArray)specGroups.valueForKey("specClass");
			NSSet set = new NSSet(specGroups);
			specGroups = EOSortOrdering.sortedArrayUsingKeyOrderArray(set.allObjects(), 
					EduGroup.sorter);
		}
		int grIdx = 0;
		NSMutableArray prepareGrades = new NSMutableArray();
		int maxGrade = (inSection == null)?SettingsReader.intForKeyPath("edu.maxGrade", 11):
			inSection.maxGrade();
		int minGrade = (inSection == null)?SettingsReader.intForKeyPath("edu.minGrade", 1):
			inSection.minGrade();
		for (int i = minGrade; i <= maxGrade; i++) {
			Integer grade = new Integer(i);
			if(specGroups != null) {
				while (grIdx < specGroups.count()) {
					EduGroup gr = (EduGroup)specGroups.objectAtIndex(grIdx);
					if(gr.grade().compareTo(grade) >= 0)
						break;
					prepareGrades.add(gr);
					grIdx++;
				}
			}
			prepareGrades.addObject(grade);
		}
		if(specGroups != null && grIdx < specGroups.count()) {
			NSRange range = new NSRange(grIdx, specGroups.count() - grIdx);
			prepareGrades.addObjectsFromArray(specGroups.subarrayWithRange(range));
		}
        return prepareGrades.immutableClone();
	}
	
	public EOEditingContext ec;
	public NSArray grades;
	
	public NSMutableDictionary subjectItem;
	public Object gradeItem;
//	public boolean editable = true;
	public int showTotal;
	public NSArray subjects;
	public int index;
	public Boolean noDetails;
	public SchoolSection inSection;
	public NSArray sections;
	public NSKeyValueCoding item;
	public int[] summary;
	public double[] sumAdditions;
	
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		if(ec == null || Various.boolForObject(valueForBinding("shouldReset"))) {
	        ec = (EOEditingContext)aContext.page().valueForKey("ec");
	        sections = SchoolSection.listSections(ec, true);
			if(inSection == null) {
				inSection = (SchoolSection)session().valueForKeyPath("state.section");
				if(inSection != null)
					inSection = (SchoolSection)EOUtilities.localInstanceOfObject(ec, inSection);
			}
			if(inSection == null) {
				inSection = (SchoolSection)sections.objectAtIndex(0);
			}
	        grades = prepareGrades();
		  	subjects = prepareAgregate();
		  	subjectItem = null;
		  	forcedSubjects = null;
			setValueForBinding(Boolean.FALSE, "shouldReset");
			noDetails = (Boolean)access().valueForKeyPath("_read.PlanDetail");
			if(!noDetails.booleanValue())
				noDetails = Boolean.valueOf(
						SettingsBase.baseForKey(EduPeriod.ENTITY_NAME, ec, false) == null);
			if(!noDetails.booleanValue()) {
				EOQualifier qual = new EOKeyValueQualifier(EduPeriod.EDU_YEAR_KEY,
						EOQualifier.QualifierOperatorEqual,session().valueForKey("eduYear"));
				EOFetchSpecification fs = new EOFetchSpecification(EduPeriod.ENTITY_NAME,qual,null);
				fs.setFetchLimit(1);
				NSArray found = ec.objectsWithFetchSpecification(fs);
				noDetails = Boolean.valueOf(found == null || found.count() == 0);
			}
		}
		index = 0;
        summary = new int[grades.count()];
        sumAdditions = null;
		super.appendToResponse(aResponse, aContext);
	}
	
	public void setIndex(Number value) {
		index = (value == null)?-1:value.intValue();
	}

	public EOEnterpriseObject area() {
		Boolean show = (Boolean)valueForKeyPath("subjectItem.showArea");
		if(show == null || (showAll && !show.booleanValue()))
			return null;
		return (EOEnterpriseObject)subjectItem.valueForKeyPath("subjectEO.area");
	}
	
	protected NSArray prepareAgregate() {
		NSMutableArray agregate = new NSMutableArray();
	  	EOFetchSpecification fs = new EOFetchSpecification(Subject.ENTITY_NAME
	  			,null,Subject.sorter);
	  	NSArray allSubjects = ec.objectsWithFetchSpecification(fs);
	  	if(allSubjects == null || allSubjects.count() == 0)
	  		return agregate;
	  	// prepare list of subjects
	  	Enumeration enu = allSubjects.objectEnumerator();
	  	while (enu.hasMoreElements()) {
			Subject subj = (Subject) enu.nextElement();
			NSMutableDictionary dict = new NSMutableDictionary(
					subj, PlanCycle.SUBJECT_EO_KEY);
			dict.takeValueForKey(subj.subject(), Subject.SUBJECT_KEY);
			if(inSection == null) {
				dict.takeValueForKey(
						new EOEnterpriseObject[grades.count()][sections.count()], "byGrade");
			} else {
				dict.takeValueForKey(new PlanCycle[grades.count()], "planCycles");
				dict.takeValueForKey(new EOEnterpriseObject[grades.count()], "planHours");
			}
			dict.takeValueForKey(new Counter(), "counter");
			agregate.addObject(dict);
		}
		NSArray allCycles = PlanCycle.allCyclesFor(null, null, inSection, ec);
		if(allCycles != null && allCycles.count() > 0) {
			enu = allCycles.objectEnumerator();
			while (enu.hasMoreElements()) { // put cycles and hours 
				PlanCycle cycle = (PlanCycle) enu.nextElement();
				int idx = allSubjects.indexOf(cycle.subjectEO());
				NSMutableDictionary dict = (NSMutableDictionary)agregate.objectAtIndex(idx);
				idx = grades.indexOf(cycle.grade());
				if(idx < 0)
					continue;
				if(inSection == null) {
					EOEnterpriseObject[][] byGrade = 
						(EOEnterpriseObject[][])dict.valueForKey("byGrade");
					int lvl = 0;
					while(lvl < sections.count()) {
						SchoolSection section = (SchoolSection)sections.objectAtIndex(lvl);
						if(section == cycle.section())
							break;
						lvl++;
					}
					if(lvl >= sections.count())
						continue;
					byGrade[idx][lvl] = cycle.planHours(null);
					if(byGrade[idx][lvl] == null)
						byGrade[idx][lvl] = cycle;
					idx++;
					while(idx < grades.count()) {
						Object next = grades.objectAtIndex(idx);
						if(!(next instanceof EduGroup))
							break;
						EduGroup grp = (EduGroup)next;
						if(!grp.grade().equals(cycle.grade()))
							break;
						byGrade[idx][lvl] = cycle.planHours(grp,false);
						if(byGrade[idx][lvl] == null)
							byGrade[idx][lvl] = cycle;
					}
				} else {
					PlanCycle[] cycles = (PlanCycle[])dict.valueForKey("planCycles");
					EOEnterpriseObject[] planHours = (EOEnterpriseObject[])
					dict.valueForKey("planHours");
					cycles[idx] = cycle;
					planHours[idx] = cycle.planHours(null);
					idx++;
					while(idx < grades.count()) {
						Object next = grades.objectAtIndex(idx);
						if(!(next instanceof EduGroup))
							break;
						EduGroup grp = (EduGroup)next;
						if(!grp.grade().equals(cycle.grade()))
							break;
						planHours[idx] = cycle.planHours(grp);
					}
				}
				NSArray hrs = cycle.planHours();
				if(hrs != null && hrs.count() > 0)
					dict.takeValueForKeyPath(new Integer(hrs.count()), "counter.add");
			}
		}
		enu = agregate.objectEnumerator();
		EOEnterpriseObject currarea = null;
		boolean hidden = false;
		boolean gerade = true;
		while (enu.hasMoreElements()) { //analyse
			NSMutableDictionary dict = (NSMutableDictionary) enu.nextElement();
			Subject subjectEO = (Subject)dict.valueForKey("subjectEO");
			boolean has = Various.boolForObject(dict.valueForKeyPath("counter"));
			if(subjectEO.area() != currarea) {
				hidden = !has;
				dict.takeValueForKey(Boolean.TRUE, "showArea");
				if(has)
					gerade = !gerade;
				currarea = subjectEO.area();
			} else if(hidden && has) {
				hidden = false;
				dict.takeValueForKey(Boolean.FALSE, "showArea");
				gerade = !gerade;
			}
			String styleClass = "unused";
			if(has) {
				if(gerade)
					styleClass = "gerade";
				else
					styleClass = "ungerade";
			}
			dict.takeValueForKey(styleClass, "styleClass");
		}
		return agregate;
	}
	
	public boolean hasSection() {
		return (inSection != null);
	}
	
	public String sectionClass() {
		if(inSection == item)
			return "selection";
		return "gerade";
	}
	
	public WOActionResults selectSection() {
		inSection = (SchoolSection)item;
		grades = prepareGrades();
	  	subjects = prepareAgregate();
	  	subjectItem = null;
	  	forcedSubjects = null;
		return null;
	}
	
	protected NSMutableSet forcedSubjects;
	public boolean showAll = false;
	public boolean showRow() {
		if(showAll)
			return true;
		if(Various.boolForObject(subjectItem.valueForKey("counter")))
			return true;
		return (forcedSubjects != null && forcedSubjects.containsObject(subjectItem));
	}
	
	public void setForced(Object forced) {
		if(forcedSubjects == null)
			forcedSubjects = new NSMutableSet();
		forcedSubjects.addObject(forced);
	}
	public void toggleAll() {
		showAll = !showAll;
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
	
	public Integer cycleLevel() {
		return (Integer)item.valueForKey("sectionID");
	}
	
	public void setCycleLevel(Integer value) {
		if(item == null || inSection != null || sections == null ||
				value == null || value.equals(cycleLevel()))
			return;
		int lvl = sections.indexOfIdenticalObject(item);
		if(lvl < 0) return;
		EOEnterpriseObject[][] byGrade = 
			(EOEnterpriseObject[][])subjectItem.valueForKey("byGrade");
		EOEnterpriseObject ph = byGrade[index][lvl];
		if(ph == null)
			return;
		if(ph instanceof PlanCycle)
			ph.takeValueForKey(value, PlanCycle.SECTION_KEY);
		else
			ph.takeValueForKeyPath(value,"planCycle.section");
	}

	protected static DecimalFormat fmt = new DecimalFormat("0.0#");
	public String planHours() {
		EOEnterpriseObject ph = planHoursEO();
		if(ph == null)
			return (inSection == null)? "<span style = \"color:#cccccc;\">&oslash;</span>":null;
		else if(ph instanceof PlanCycle)
			return "<span style = \"color:#999999;\">0</span>";
		PlanCycle cycle = (PlanCycle)ph.valueForKey("planCycle");
		Integer year = (Integer)session().valueForKey("eduYear"); 
		int[] wd = cycle.weeksAndDays(year);
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
		PlanCycle[] planCycles = (PlanCycle[])subjectItem.valueForKey("planCycles");
		EOEnterpriseObject[] planHours = (EOEnterpriseObject[])
			subjectItem.valueForKey("planHours");
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
		if(hours != null) {
			EduGroup grp = (gradeItem instanceof EduGroup)?(EduGroup)gradeItem:null;
			if(planCycles[index] == null) { // create cycle
				PlanCycle cycle = (PlanCycle)EOUtilities.createAndInsertInstance(
						ec, PlanCycle.ENTITY_NAME);
				Integer grade = (grp == null)?(Integer)gradeItem:grp.grade();
				cycle.setGrade(grade);
				cycle.addObjectToBothSidesOfRelationshipWithKey((Subject)
						subjectItem.valueForKey("subjectEO"), PlanCycle.SUBJECT_EO_KEY);
				cycle.setSection(inSection);
				for (int i = 0; i < planCycles.length; i++) {
					Integer grd = null;
					Object grItem = grades.objectAtIndex(i); 
					if(grItem instanceof EduGroup) {
						grd = ((EduGroup)grItem).grade();
					} else {
						grd = (Integer)grItem;
					}
					if(grd.compareTo(grade) > 0)
						break;
					if(grd.equals(grade))
						planCycles[i] = cycle;
				}
			}
			String key = (showTotal == 0)? "weeklyHours" : "totalHours";
			String keyNot = (showTotal != 0)? "weeklyHours" : "totalHours";
			if(planHours[index] == null ||
					planHours[index].valueForKey("specClass") != grp) { //create hours
				planHours[index] = planCycles[index].createPlanHours(grp);
				if(Various.boolForObject(subjectItem.valueForKey("counter")))
					subjectItem.valueForKeyPath("counter.raise");
				else
					setValueForBinding(Boolean.TRUE, "shouldReset");
			}
			planHours[index].takeValueForKey(hours, key);
			planHours[index].takeValueForKey(new Integer(0), keyNot);
		} else { // hours == null
			if(planHours[index] != null) { // delete planHours
				if(gradeItem instanceof EduGroup) {
					if(planCycles[index].planHours(null) == null) {
						planCycles[index].removeObjectFromBothSidesOfRelationshipWithKey(
								planHours[index], PlanCycle.PLAN_HOURS_KEY);
						ec.deleteObject(planHours[index]);
						planHours[index] = null;
					} else {
						if(planHours[index].valueForKey("specClass") == null) {
							planHours[index] = planCycles[index].createPlanHours(
									(EduGroup)gradeItem);
						} else {
							Integer zero = new Integer(0);
							planHours[index].takeValueForKey(zero, "weeklyHours");
							planHours[index].takeValueForKey(zero, "totalHours");
						}
					}
				} else {
					planCycles[index].removeObjectFromBothSidesOfRelationshipWithKey(
							planHours[index], PlanCycle.PLAN_HOURS_KEY);
					ec.deleteObject(planHours[index]);
					int i = index;
					while (i < planHours.length && planCycles[i] == planCycles[index]) {
						planHours[i] = null;
						i++;
					}
				}
				Number count = (Number)subjectItem.valueForKeyPath("counter.lower");
				if(count == null || count.intValue() <= 0)
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
		if(!ec.hasChanges())
			return;
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
			ec.saveChanges();
			EduPlan.logger.log(WOLogLevel.EDITING,"Saved changes in EduPlan",session());
			if(inSection == null)
			  	subjects = prepareAgregate();
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
		if(gradeItem instanceof EduGroup)
			return ((EduGroup)gradeItem).name();
		else
			return gradeItem.toString();
	}
	
	public String cellClass() {
		if(gradeItem instanceof EduGroup)
			return "special";
		else
			return null;
	}
	
	protected EOEnterpriseObject planHoursEO() {
		if(subjectItem == null)
			return null;
		if(index < 0 || index >= grades.count())
			return null;
		if(inSection == null) {
			if(item == null) return null;
			int lvl = sections.indexOfIdenticalObject(item);
			if(lvl < 0) return null;
			EOEnterpriseObject[][] byGrade = 
				(EOEnterpriseObject[][])subjectItem.valueForKey("byGrade");
			return byGrade[index][lvl];
		}
		EOEnterpriseObject[] planHours = (EOEnterpriseObject[])
			subjectItem.valueForKey("planHours");
		return planHours[index];
	}
	
	public String cellStyle() {
		 String result = null;
		if(area() != null) {
			result = "border-top:#ffcc66 solid 2px;";
		}
		if(inSection == null) {
			if(result == null)
				result = "white-space:nowrap;border-left:#666666 solid 1px;";
			else
				result = result + "white-space:nowrap;border-left:#666666 solid 1px;";
		} else
		if(!editable()) {
			EOEnterpriseObject ph = planHoursEO();
			if(ph == null)
				return result;
			String key = (showTotal == 0)? "weeklyHours": "totalHours";
			Integer value = (Integer)ph.valueForKey(key);
			if(value == null || value.intValue() <= 0) {
				if(result == null)
					result = "color:#999999;";
				else
					result = result + "color:#999999;";
			}
		}
		return result;
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
	
	public WOComponent editSubject() {
		WOComponent popup = pageWithName("SubjectEditor");
		popup.takeValueForKey(subjectItem.valueForKey("subjectEO"), "subject");
		popup.takeValueForKey(context().page(), "returnPage");
		popup.takeValueForKey(access().valueForKey("_edit.Subject"), "cantChange");
		subjectItem = null;
		return popup;
	}
	
	public WOComponent addSubject() {
		Subject subject = (Subject)EOUtilities.createAndInsertInstance(ec, "Subject");
		WOComponent popup = pageWithName("SubjectEditor");
		popup.takeValueForKey(subject, "subject");
		popup.takeValueForKey(context().page(), "returnPage");
		return popup;
	}

	public WOComponent editArea() {
		WOComponent popup = pageWithName("AreaEditor");
		popup.takeValueForKey(context().page(), "returnPage");
		popup.takeValueForKey(subjectItem.valueForKeyPath("subjectEO.area"), "area");
		subjectItem = null;
		return popup;
	}
	public WOComponent addArea() {
		WOComponent popup = pageWithName("AreaEditor");
		popup.takeValueForKey(context().page(), "returnPage");
		popup.takeValueForKey(ec, "editingContext");
		subjectItem = null;
		return popup;
	}
	public Boolean cantEditArea() {
		if(area() == null)
			return Boolean.TRUE;
		return (Boolean)access().valueForKeyPath("_edit.area");
	}
	
	public String areaClass() {
		if(area() != null)
			return "orange";
		return null;
	}
	
	public Boolean canAddArea() {
		if(!showAll)
			return Boolean.FALSE;
		return (Boolean)access().valueForKeyPath("create.SubjectArea");
	}
	public Boolean canAddSubject() {
		if(!showAll)
			return Boolean.FALSE;
		return (Boolean)access().valueForKeyPath("create.Subject");
	}
	
	public int colspan() {
		return (grades.count() + 4); 
	}
	
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
	}
	
	public WOActionResults details() {
		NSMutableDictionary dict = new NSMutableDictionary("PlanDetails","component");
		dict.takeValueForKey(session().valueForKeyPath(
				"strings.RujelEduPlan_EduPlan.PlanDetails"),"title");
		dict.takeValueForKey(valueForKeyPath("subjectItem.subjectEO"), "selection");
		dict.takeValueForKey(new Integer(showTotal), "showTotal");
		dict.takeValueForKey(inSection, "inSection");
//		parent().takeValueForKey(dict, "currTab");
		setValueForBinding(dict, "dict");
		dict.takeValueForKey(_access, "access");
//		session().takeValueForKey(context().page(), "pushComponent");
		return performParentAction("revertEc");
	}

	public WOActionResults setupSections() {
		WOComponent result = pageWithName("SectionsSetup");
		/*
		if(sections == null || sections.count() == 0) {
			Indexer sIndex = Indexer.getIndexer(ec, "eduSections",(String)null, true);
			sIndex.setValueForIndex((String)session().valueForKeyPath(
					"strings.RujelEduPlan_EduPlan.sections.basic"), 0);
			if(ec.globalIDForObject(sIndex).isTemporary()) {
				Logger logger = Logger.getLogger("rujel.eduplan");
				try {
					ec.saveChanges();
					logger.log(WOLogLevel.COREDATA_EDITING,
							"autocreating eduSections indexer and basic section",sIndex);
				} catch (Exception e) {
					logger.log(WOLogLevel.WARNING,"Error autocreating eduSections indexer",
							new Object[] {session(),e});
					ec.revert();
				}
			}
			sections = sIndex.sortedIndex();
		} */
		result.takeValueForKey(context().page(), "returnPage");
//		result.takeValueForKey(ec, "ec");
		return result;
	}
	
	public Boolean noSections() {
		if (sections == null || sections.count() < 2)
			return Boolean.TRUE;
		if(item == null) {
			SchoolSection section = inSection;
			inSection = null;
			Boolean result = (Boolean)access().valueForKey("_edit");
			inSection = section;
			return result;
		}
		return Boolean.FALSE;
	}
}