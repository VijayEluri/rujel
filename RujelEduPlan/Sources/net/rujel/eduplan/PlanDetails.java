// PlanDetails.java: Class file for WO Component 'PlanDetails'

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

import java.io.InputStream;
import java.util.Enumeration;

import net.rujel.base.CourseInspector;
import net.rujel.base.SettingsBase;
import net.rujel.interfaces.*;
import net.rujel.reusables.Counter;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;
import net.rujel.ui.TeacherSelector;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Jul 21, 2009 5:45:43 PM
public class PlanDetails extends com.webobjects.appserver.WOComponent {
 	public EOEditingContext ec;
 	public NSArray subjects;
 	public NSArray cycles;
 	public Object item;
 	public Object selection;
 	public NSMutableArray listNames;
 	public NSMutableDictionary periodsForList;
 	public NSMutableDictionary cycleItem;
 	public NSMutableDictionary rowItem;
 	public String listItem;
 	public NSMutableDictionary listDict;
 	public EOEnterpriseObject pdItem;
 	public Integer courseIndex;
	public int showTotal;
	public Integer onLevel = new Integer(0);
 	
	public PlanDetails(WOContext context) {
        super(context);
//		ec = (EOEditingContext)context.page().valueForKey("ec");
        periodsForList = new NSMutableDictionary(new Counter(),"periodsCounter");
//		context().page().takeValueForKey(this, "toReset");
    }
	
	public void select() {
		setSelection(item);
	}
	
	public void selectSubject() {
		setSelection(cycleItem.valueForKeyPath("cycle.subjectEO"));
	}
	
	public void selectEduGroup() {
		setSelection(rowItem.valueForKey("eduGroup"));
	}
	
	public NSArray subjects() {
		if(subjects == null) {
	        subjects = PlanCycle.allSubjects(ec);
		}
		return subjects;
	}
	
	public boolean hideGroups() {
		return (selection instanceof Subject);
	}
	
	public boolean hideSubjects() {
		return (selection instanceof EduGroup);
	}

	public void setSelection(Object sel) {
		if(listNames == null)
			listNames = new NSMutableArray();
		else
			listNames.removeAllObjects();
		ec.lock();
		try {
			if(ec.hasChanges())
				ec.revert();
			selection = sel;
			if(sel == null) {
				cycles = null;
				return;
			}
			Integer eduYear = (Integer)session().valueForKey("eduYear");
			NSArray groups = null;
			if (sel instanceof EduGroup) {
				EduGroup gr = (EduGroup) sel;
				cycles = PlanCycle.cyclesForEduGroup(gr);
//				values.takeValueForKey(gr, "eduGroup");
//				groups = new NSArray(sel);
			} else if (sel instanceof Subject) {
				Subject subject = (Subject) sel;
				cycles = PlanCycle.cyclesForSubject(subject,onLevel);
				EOSortOrdering so =new EOSortOrdering("grade",EOSortOrdering.CompareAscending);
				cycles = EOSortOrdering.sortedArrayUsingKeyOrderArray(cycles, new NSArray(so));
				NSTimestamp date = (NSTimestamp)session().valueForKey("today");
				groups = EduGroup.Lister.listGroups(date, ec);
			} else {
				return;
			}
			if(cycles == null || cycles.count() == 0)
				return;
			NSMutableArray cycleDicts = new NSMutableArray();
			NSMutableDictionary values = new NSMutableDictionary(eduYear,"eduYear");
			Enumeration enu = cycles.objectEnumerator();
			while (enu.hasMoreElements()) {
				PlanCycle cycle = (PlanCycle) enu.nextElement();
				values.takeValueForKey(cycle, "cycle");
				NSMutableDictionary dict = observeValue(new NSDictionary(
						new Object[] {cycle,eduYear},
						new String[] {"cycle","eduYear"}));
				dict = new NSMutableDictionary(dict, "listName");
				dict.takeValueForKey(cycle,"cycle");
//				dict.takeValueForKey(new Integer(cycle.weekly()), "weekly");
				NSMutableArray courses = new NSMutableArray();
				if(sel instanceof EduGroup) {
					dict.takeValueForKey(cycle.subject(), "title");
					prepareCourses((EduGroup)sel, cycle, values, courses);
				} else {
					dict.takeValueForKey(cycle.grade(), "title");
					EOQualifier qual = new EOKeyValueQualifier("grade",
							EOQualifier.QualifierOperatorEqual,cycle.grade());
					NSArray relatedGroups = EOQualifier.filteredArrayWithQualifier(groups, qual);
					Enumeration grEnu = relatedGroups.objectEnumerator();
					while (grEnu.hasMoreElements()) {
						EduGroup gr = (EduGroup) grEnu.nextElement();
						prepareCourses(gr, cycle, values, courses);
					}
				}
				dict.takeValueForKey(courses, "courses");
				cycleDicts.addObject(dict);
			}
			cycles = cycleDicts;
		} catch (Exception e) {
			EduPlan.logger.log(WOLogLevel.WARNING,"Error preparing details list",
					new Object[] {session(),e});
			cycles = null;
		} finally {
			ec.unlock();
		}
	}

	protected void prepareCourses(EduGroup gr, PlanCycle cycle, NSMutableDictionary values,
			NSMutableArray result) {
		values.takeValueForKey(gr, "eduGroup");
		NSArray courses = EOUtilities.objectsMatchingValues(ec,
				EduCourse.entityName, values);
		courses = EOSortOrdering.sortedArrayUsingKeyOrderArray(courses, EduCourse.sorter);
		int count = cycle.subgroups().intValue();
		if(courses != null && courses.count() > 0) {
			Enumeration enu = courses.objectEnumerator();
			while (enu.hasMoreElements()) {
				EduCourse course = (EduCourse) enu.nextElement();
				result.addObject(courseRow(course));
				count--;
			}
		}
		while(count > 0) {
			result.addObject(courseRow(values));
//			NSMutableDictionary crow = new NSMutableDictionary(gr, "eduGroup"); 
//			result.addObject(crow);
			count--;
		}
	}
	
	protected NSMutableDictionary courseRow(NSKeyValueCodingAdditions course) {
		NSMutableDictionary listSetting = observeValue(course);
		NSMutableDictionary result = new NSMutableDictionary(listSetting, "listSetting");
		EduGroup group = (EduGroup)course.valueForKey("eduGroup");
		result.takeValueForKey(group, "eduGroup");
		PlanCycle cycle = (PlanCycle)course.valueForKey("cycle");
		EOEnterpriseObject planHours = cycle.planHours(group);
		Integer hours = (Integer)planHours.valueForKey("weeklyHours");
		int weekly = (hours == null)?0:hours.intValue();
		hours = (Integer)planHours.valueForKey("totalHours");
		int total = (hours == null)?0:hours.intValue();
		boolean calculatedTotal = (total <= 0);
//		String indication = "grey";
		result.takeValueForKey("grey", "defaultIndication");
		if(weekly <= 0) {
			result.takeValueForKey(hours,"planTotal");
			int weeks = ((Integer)listSetting.valueForKey("weeks")).intValue();
			if(weeks > 0)
				weekly = total/weeks;
			int extra = (weeks==0)?total : total%weeks;
			if(extra > weekly && weeks > 0) {
				weekly++;
//				indication = "highlight2";
				result.takeValueForKey("highlight2", "defaultIndication");
			} else if(extra > 0) {
				Integer extraDays = (Integer)listSetting.valueForKey("extraDays");
				if(extra > extraDays.intValue())
//					indication = "warning";
					result.takeValueForKey("warning", "defaultIndication");
			}
		}
		int days = ((Integer)listSetting.valueForKey("days")).intValue();
		int week = ((Integer)listSetting.valueForKey("week")).intValue();
		total = weekly * days / week;
		result.takeValueForKey(new Integer(total), "defaultTotal");
		if(calculatedTotal)
			result.takeValueForKey(new Integer(total), "planTotal");
		result.takeValueForKey(new Integer(weekly), "weekly");
		if(course instanceof EduCourse) {
			result.takeValueForKey(course,"course");
			NSArray details = EOUtilities.objectsMatchingKeyAndValue(ec, "PlanDetail",
					"course", course);
			if(details != null && details.count() > 0) {
				NSMutableDictionary detailsDict = new NSMutableDictionary();
				Enumeration enu = details.objectEnumerator();
//				int planTotal = total;
//				total = 0;
				while (enu.hasMoreElements()) {
					EOEnterpriseObject detail = (EOEnterpriseObject) enu.nextElement();
					EduPeriod per = (EduPeriod)detail.valueForKey("eduPeriod");
					detailsDict.setObjectForKey(detail,per);
					int dHours = ((Integer)detail.valueForKey("hours")).intValue();
					if(dHours > 0) {
//						total += dHours;
					} else {
						calculatedTotal = true;
						dHours = ((Integer)detail.valueForKey("weekly")).intValue();
//						int days = ((Integer)listSetting.objectForKey(per)).intValue();
//						total += dHours * days / week;
					}
				}
				result.takeValueForKey(detailsDict, "details");
				if(!calculatedTotal)
					weekly = 0;
//				if(total - planTotal > weekly)
//					indication = "highlight2";
//				else if(planTotal - total > weekly)
//					indication = "warning";
			}
			result.takeValueForKey("green", "styleClass");
		} else {
			result.takeValueForKey("grey", "styleClass");
		}
		return result;
	}
	
	protected NSMutableDictionary observeValue(NSKeyValueCodingAdditions course) {
		NSKeyValueCoding sb = SettingsBase.settingForCourse(EduPeriod.ENTITY_NAME,course, ec);
		if(sb == null)
			return null;
		String listName = (String)sb.valueForKey(SettingsBase.TEXT_VALUE_KEY);
		Integer week = (Integer)sb.valueForKey(SettingsBase.NUMERIC_VALUE_KEY);
		if(week == null)
			week = new Integer(7);
		NSMutableDictionary dict = (NSMutableDictionary)periodsForList.valueForKey(listName);
		if(dict == null) {
			dict = new NSMutableDictionary(listName,"listName");
			dict.takeValueForKey(week, "week");
			NSArray periods = EduPeriod.periodsInList(listName, ec);
			dict.takeValueForKey(periods, "periods");
//			int days = EduPeriod.daysForList(listName, null, periods);
			int days = 0;
			Enumeration enu = periods.objectEnumerator();
			while (enu.hasMoreElements()) {
				EduPeriod per = (EduPeriod) enu.nextElement();
				int pDays = per.daysInPeriod(null, listName);
				dict.setObjectForKey(new Integer(pDays), per);
				days += pDays; 
			}
			dict.takeValueForKey(new Integer(days), "days");
			dict.takeValueForKey(new Integer(days/week), "weeks");
			dict.takeValueForKey(new Integer(days%week), "extraDays");
			periodsForList.takeValueForKey(dict, listName);
			periodsForList.takeValueForKeyPath(new Integer(periods.count()),
					"periodsCounter.add");
			listNames.addObject(listName);
		} else if(!listNames.containsObject(listName)) {
			listNames.addObject(listName);
		}
		return dict;
	}
	
	public void setListItem(String li) {
		listItem = li;
		if(periodsForList == null)
			return;
		listDict = (li==null)?null:(NSMutableDictionary)periodsForList.valueForKey(li);
		if (rowItem != null) {
			NSDictionary details = (NSDictionary) rowItem
					.valueForKey("details");
			if (details != null && details.count() == 0)
				rowItem.removeObjectForKey("details");
		}
	}

	protected int perDays = -1;
	public EduPeriod periodItem() {
		if(item instanceof EduPeriod)
			return (EduPeriod)item;
		return null;
	}

	public void setPeriodItem(EduPeriod periodItem) {
		item = periodItem;
		pdItem = null;
		if(periodItem != null) {
			perDays = ((Integer)listDict.objectForKey(periodItem)).intValue();
			//periodItem.daysInPeriod(null, listItem);
			if(rowItem != null) {
				NSDictionary details = (NSDictionary)rowItem.valueForKey("details");
				if(details != null)
					pdItem = (EOEnterpriseObject)details.objectForKey(periodItem);
			}
		} else {
			perDays = -1;
		}
	}

	public String perWeeksDays() {
		if(periodItem() == null)
			return null;
//		int days = periodItem().daysInPeriod(null, listItem);
		Integer week = (Integer)valueForKeyPath("listDict.week");
		int wd = (week==null)?7:week.intValue();
		StringBuilder buf = new StringBuilder(7);
		buf.append(perDays/wd).append(' ').append('(').append(perDays%wd).append(')');
		return buf.toString();
	}
		
	public Boolean showFields() {
		if(listItem == null || rowItem.valueForKey("course") == null ||
				!listItem.equals(rowItem.valueForKeyPath("listSetting.listName")))
			return Boolean.FALSE;
		return Boolean.TRUE;
	}

	public String perCellClass() {
		if(listItem == null || rowItem.valueForKey("course") == null ||
				!listItem.equals(rowItem.valueForKeyPath("listSetting.listName")))
			return null;
		if(pdItem == null) {
			if(Various.boolForObject(valueForKeyPath("rowItem.details.count")))
				return "grey";
			else
				return null;
		}
		Integer weekly = (Integer)pdItem.valueForKey("weekly");
		if(weekly == null)
			return "orange";
		Integer hours = (Integer)pdItem.valueForKey("hours");
		if(hours == null)
			return "orange";
		if(hours.intValue() <= 0)
			return null;
		Integer week = (Integer)listDict.valueForKeyPath("week");
		int weeks = perDays / week.intValue();
		int total = weeks * weekly.intValue();
		if (hours.intValue() - total > weekly.intValue())
			return "warning";
		if (total > hours.intValue())
			return "highlight2";
		return null;
	}

	public String weeklyStyle() {
		StringBuilder buf = new StringBuilder("style = \"text-align:center;float:right;");
		if(showTotal > 0)
			buf.append("width:1.6ex;");
		else
			buf.append("width:1em;");
		if(pdItem == null)
			buf.append("color:#999999;\" onfocus = \"style.color='#000000';\" onblur = \"if(value==defaultValue)style.color='#999999';\"");
		else
			buf.append('"');
		return buf.toString();
	}
	
	public String weeklyHours() {
		if(listItem == null || 
				!listItem.equals(rowItem.valueForKeyPath("listSetting.listName")))
			return null;
		if(pdItem != null)
			return pdItem.valueForKey("weekly").toString();
//		if(Various.boolForObject(valueForKeyPath("rowItem.details.count")))
		if(rowItem.valueForKey("details") != null)
			return null;
		return rowItem.valueForKey("weekly").toString();
	}

	public void setWeeklyHours(String weeklyHours) {
		NSMutableDictionary details = (NSMutableDictionary)rowItem.valueForKey("details");
		if(pdItem != null) {
			Integer hours = (Integer)pdItem.valueForKey("hours");
			if(weeklyHours == null) {
				boolean isNew = ec.globalIDForObject(pdItem).isTemporary();
				if(showTotal == 0 || hours == null || hours.intValue() <= 0) {
					ec.deleteObject(pdItem);
					details.removeObjectForKey(periodItem());
					pdItem = null;
				} else {
					if(!isNew && pdItem.valueForKey("weekly") == null)
						pdItem.takeValueForKey(new Integer(0), "weekly");
				}
			} else {
				Integer weekly = new Integer(weeklyHours);
				pdItem.takeValueForKey(weekly, "weekly");
				if(hours == null || hours.intValue() <= 0) {
					Integer week = (Integer)listDict.valueForKeyPath("week");
					int total = - weekly.intValue() * perDays / week;
					pdItem.takeValueForKey(new Integer(total), "hours");
				}
			}
			return;
		} else if(details != null) {
			if(weeklyHours == null)
				return;
		} else if(rowItem.valueForKey("weekly").toString().equals(weeklyHours)) {
			return;
		}
		createDetail();
		details = (NSMutableDictionary)rowItem.valueForKey("details");
		if(weeklyHours == null) {
			ec.deleteObject(pdItem);
			details.removeObjectForKey(periodItem());
			pdItem = null;
		} else {
			Integer weekly = new Integer(weeklyHours);
			pdItem.takeStoredValueForKey(weekly, "weekly");
			Integer week = (Integer)listDict.valueForKeyPath("week");
			int total = -weekly.intValue() * perDays / week.intValue();
			pdItem.takeValueForKey(new Integer(total), "hours");
		}
	}

	public String totalStyle() {
		StringBuilder buf = new StringBuilder("style = \"width:3ex;float:left;text-align:center;");
		if(pdItem == null) {
			buf.append('"');
		} else {
			Integer hours = (Integer)pdItem.valueForKey("hours");
			if(hours == null || hours.intValue() <= 0)
				buf.append("color:#cccccc;\" onfocus = \"style.color='#000000';\" onblur = \"if(value==defaultValue)style.color='#cccccc';\"");
			else
				buf.append('"');
		}
		return buf.toString();
	}
	
	public Integer totalHours() {
		if(periodItem() == null) {
			PlanCycle cycle = (PlanCycle) cycleItem.valueForKey("cycle");
			return cycle.hours((NSKeyValueCoding)rowItem.valueForKey("course"));
		}
		if(pdItem == null)
			return null;
		Integer hours = (Integer)pdItem.valueForKey("hours");
		if(hours != null && hours.intValue() > 0)
			return hours;
		Integer week = (Integer)listDict.valueForKeyPath("week");
		Integer weekly = (Integer)pdItem.valueForKey("weekly");
		return new Integer (weekly.intValue()*perDays/week.intValue());
	}

	public void setTotalHours(Integer totalHours) {
		if(pdItem != null) {
			if(totalHours == null) {
				totalHours = new Integer(0);
			} else if (totalHours.equals(totalHours())) {
				return;
			}
			pdItem.takeValueForKey(totalHours, "hours");
			return;
		} else if(totalHours == null)
			return;
		createDetail();
		pdItem.takeValueForKey(totalHours, "hours");
		Integer week = (Integer)listDict.valueForKeyPath("week");
		int weeks = perDays / week.intValue();
		int weekly = totalHours.intValue() / weeks;
		pdItem.takeValueForKey(new Integer(weekly), "weekly");
	}

	protected EOEnterpriseObject createDetail() {
		if(pdItem != null)
			return pdItem;
		EduCourse course = (EduCourse)rowItem.valueForKey("course");
		if(course == null)
			return null;
		NSMutableDictionary details = (NSMutableDictionary)rowItem.valueForKey("details");
		/*if(details == null) {
			details = new NSMutableDictionary();
			rowItem.setObjectForKey(details, "details");
		}*/
		if(details != null) {
			pdItem = (EOEnterpriseObject)details.objectForKey(periodItem());
			if(pdItem != null)
				return pdItem;
			pdItem = EOUtilities.createAndInsertInstance(ec, "PlanDetail");
			pdItem.addObjectToBothSidesOfRelationshipWithKey(periodItem(), "eduPeriod");
			pdItem.takeValueForKey(course, "course");
			details.setObjectForKey(pdItem, periodItem());
		} else {
			details = new NSMutableDictionary();
			rowItem.setObjectForKey(details, "details");
			Enumeration enu = ((NSArray)listDict.valueForKey("periods")).objectEnumerator();
			Integer weekly = (Integer)rowItem.valueForKey("weekly");
			Integer week = (Integer)listDict.valueForKeyPath("week");
			while (enu.hasMoreElements()) {
				EduPeriod per = (EduPeriod) enu.nextElement();
				EOEnterpriseObject pd = EOUtilities.createAndInsertInstance(ec, "PlanDetail");
				pd.takeValueForKey(per, "eduPeriod");
				pd.takeValueForKey(course, "course");
				pd.takeValueForKey(weekly, "weekly");
				int days = ((Integer)listDict.objectForKey(per)).intValue();
				int total = -weekly.intValue() * days / week.intValue();
				pd.takeValueForKey(new Integer(total), "hours");
				details.setObjectForKey(pd, per);
				if(per == item)
					pdItem = pd;
			}
		}
		return pdItem;
	}
	
	public void setRowItem(NSMutableDictionary row) {
		rowItem = row;
		if(row == null)
			return;
		if(rowItem.valueForKey("showTotal") != null)
			return;
		NSDictionary details = (NSDictionary)row.valueForKey("details");
		if(details == null) {
			row.takeValueForKey(row.valueForKey("defaultTotal"),"total");
			row.takeValueForKey(row.valueForKey("defaultIndication"),"indication");
			row.takeValueForKey(Boolean.TRUE, "noDetails");
			return;
		}
		row.takeValueForKey(session().valueForKeyPath(
				"readAccess._delete.PlanDetail"), "noDetails");
		NSDictionary listSetting = (NSDictionary)row.valueForKey("listSetting");
		int total = 0;
		int min = 0;
		int cap = 0;
		Enumeration enu = details.objectEnumerator();
		int week = ((Integer)listSetting.valueForKey("week")).intValue();
		while (enu.hasMoreElements()) {
			EOEnterpriseObject detail = (EOEnterpriseObject) enu.nextElement();
			int dHours = ((Integer)detail.valueForKey("hours")).intValue();
			if(dHours > 0) {
				total += dHours;
				min += dHours;
			} else {
				dHours = ((Integer)detail.valueForKey("weekly")).intValue();
				int days = ((Integer)listSetting.objectForKey(
						detail.valueForKey("eduPeriod"))).intValue();
				total += dHours * days / week;
				min += (days / week) * dHours;
				if(days % week > 0)
					cap += dHours;
			}
		}
		row.takeValueForKey(new Integer(total),"total");
		int minPlan = 0;
		int planCap = 0;
		PlanCycle cycle = (PlanCycle) cycleItem.valueForKey("cycle");
		EduGroup group = (EduGroup)row.valueForKey("eduGroup");
		EOEnterpriseObject planHours = cycle.planHours(group);
		Integer hours = (Integer)planHours.valueForKey("weeklyHours");
		if(hours != null && hours.intValue() > 0) {
			planCap = hours.intValue();
			int weeks = ((Integer)listSetting.valueForKey("weeks")).intValue();
			minPlan = weeks * planCap;
			if(!Various.boolForObject(listSetting.valueForKey("extraDays")))
				planCap = 0;
		} else {
			hours = (Integer)planHours.valueForKey("totalHours");
			minPlan = hours.intValue();
		}
		if(min > minPlan + planCap)
			row.takeValueForKey("highlight2","indication");
		else if(min + cap < minPlan)
			row.takeValueForKey("warning","indication");
		else
			row.takeValueForKey("green","indication");
	}

	public void save() {
		if(!ec.hasChanges())
			return;
	   	ec.lock();
    	try {
			ec.saveChanges();
			EduPlan.logger.log(WOLogLevel.COREDATA_EDITING, "Saved PlanDetails", session());
		} catch (Exception e) {
			EduPlan.logger.log(WOLogLevel.INFO,"Error saving plan details",
					new Object[] {session(),e});
			session().takeValueForKey(e.getMessage(), "message");
//			ec.revert();
		} finally {
			ec.unlock();
		}
	}
	
	public String teacherString() {
		if(rowItem == null)
			return null;
		EduCourse course = (EduCourse)rowItem.valueForKey("course");
		if(course == null)
			return (String)application().valueForKeyPath(
					"strings.Reusables_Strings.uiElements.Add");
		Teacher teacher = course.teacher();
		if(teacher == null)
			return "<em>" + (String)application().valueForKeyPath(
					"strings.RujelBase_Base.vacant") + "</em>";
		return Person.Utility.fullName(teacher, true, 2, 1, 1);
	}
	
	public NamedFlags courseAccess() {
		if(rowItem == null || rowItem.valueForKey("course") == null)
			return (NamedFlags)session().valueForKeyPath(
					"readAccess.FLAGS." + EduCourse.entityName);
		else
			return (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.rowItem.course");
	}
	
	public WOActionResults help() {
		WOResponse response = application().createResponseInContext(context());
        try {
			InputStream stream = (InputStream)session().valueForKeyPath(
				"strings.@RujelEduPlan_DetailsHelp.html");
			response.setContentStream(stream, 1024, (long)stream.available());
			return response;
		} catch (Exception e) {
			Object[] args = new Object[] {session(),e};
			EduPlan.logger.log(WOLogLevel.WARNING,
					"Error reading DetailsHelp.html",args);
			session().takeValueForKey(e.getMessage(), "message");
		}
		return null;
	}
	
	public WOActionResults selectTeacher() {
		if(rowItem.valueForKey("course") == null) {
			TeacherGetter getter = new TeacherGetter(rowItem,ec,
					(PlanCycle)cycleItem.valueForKey("cycle"),session());
			WOComponent selector = TeacherSelector.selectorPopup(context().page(), getter,
					"teacher", ec);
			return selector;
		}
		WOComponent result = pageWithName("CourseInspector");
		result.takeValueForKey(context().page(), "returnPage");
		result.takeValueForKey(rowItem.valueForKey("course"), "course");
		result.takeValueForKey(new CourseInspector.Updater() {
			final NSMutableDictionary row = rowItem;
			public void update() {
				NSDictionary details = (NSDictionary)row.valueForKey("details");
				if(details != null) {
					Enumeration enu = details.objectEnumerator();
					while (enu.hasMoreElements()) {
						EOEnterpriseObject detail = (EOEnterpriseObject) enu.nextElement();
						ec.deleteObject(detail);
					}
					ec.saveChanges();
					row.removeObjectForKey("details");
				}
				row.removeObjectForKey("course");
				row.takeValueForKey("grey", "styleClass");
			}
		}, "updater");
		return result;
	}
	
	protected static class TeacherGetter { 
		protected NSMutableDictionary row;
		protected EOEditingContext ec;
		protected PlanCycle cycle;
		protected WOSession ses;

		public TeacherGetter (NSMutableDictionary rowItem, EOEditingContext editingContext, 
				PlanCycle eduCycle, WOSession session) {
			super();
			row = rowItem;
			ec = editingContext;
			cycle = eduCycle;
			ses = session;
		}

		public void setTeacher(Object teacher) {
			ec.lock();
			try {
				EduCourse course = (EduCourse)EOUtilities.createAndInsertInstance(ec,
						EduCourse.entityName);
				course.takeValueForKey(ses.valueForKey("eduYear"), "eduYear");
				course.addObjectToBothSidesOfRelationshipWithKey(cycle, "cycle");
				course.takeValueForKey(row.valueForKey("eduGroup"), "eduGroup");
				if(teacher instanceof Teacher)
					course.setTeacher((Teacher)teacher);
				ec.saveChanges();
				row.takeValueForKey(course, "course");
				row.takeValueForKey("green", "styleClass");
				EduPlan.logger.log(WOLogLevel.COREDATA_EDITING, "Created EduCourse",
						new Object[] {ses, course});
			} catch (Exception e) {
				EduPlan.logger.log(WOLogLevel.INFO,"Error saving EduCourse",
						new Object[] {ses,e});
				ses.takeValueForKey(e.getMessage(), "message");
				ec.revert();
			} finally {
				ec.unlock();
			}
		}
	}

	public void deleteDetails() {
		if(rowItem == null)
			return;
		NSDictionary details = (NSDictionary)rowItem.valueForKey("details");
		if(details == null)
			return;
		Enumeration enu = details.objectEnumerator();
		ec.lock();
		try {
			while (enu.hasMoreElements()) {
				EOEnterpriseObject pd = (EOEnterpriseObject) enu.nextElement();
				ec.deleteObject(pd);
			}
			ec.saveChanges();
			rowItem.removeObjectForKey("details");
		} catch (Exception e) {
			ec.revert();
			EduPlan.logger.log(WOLogLevel.WARNING,"Error deleting plan details",
					new Object[] {session(),rowItem,e});
		} finally {
			ec.unlock();
		}
	}

	
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		if(Various.boolForObject(valueForBinding("shouldReset"))) {
			ec = (EOEditingContext)context().page().valueForKey("ec");
			periodsForList = new NSMutableDictionary(new Counter(),"periodsCounter");
			subjects = null;
			listNames = null;
			listDict = null;
			NSDictionary dict = (NSDictionary)valueForBinding("dict");
			if(dict == null) {
				selection = null;
				cycles = null;
				showTotal = 0;
			} else {
				showTotal = ((Integer)dict.valueForKey("showTotal")).intValue();
				onLevel = (Integer)dict.valueForKey("onLevel");
				setSelection(dict.valueForKey("selection"));
			}
			setValueForBinding(Boolean.FALSE, "shouldReset");
		}
		super.appendToResponse(aResponse, aContext);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
}