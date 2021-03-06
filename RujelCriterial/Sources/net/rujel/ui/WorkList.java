// WorkList.java: Class file for WO Component 'WorkList'

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

import java.util.Date;
import java.util.Enumeration;

import net.rujel.reusables.*;
import net.rujel.interfaces.*;
import net.rujel.base.MyUtility;
import net.rujel.base.SettingsBase;
import net.rujel.criterial.*;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class WorkList extends LessonList {

    public WorkList(WOContext context) {
        super(context);
    }

 	public Work work() {
		return (Work)lessonItem;
	}
	
    /** @TypeInfo Criterion */
    private NSKeyValueCoding critItem;
	protected EOEnterpriseObject _itemMask;

	public void reset() {
		super.reset();
		_itemMask = null;
		_criteria = null;
	}
	
    /** @TypeInfo Criterion */
	public NSKeyValueCoding critItem() {
		return critItem;
	}
	
	public void setCritItem(NSKeyValueCoding item) {
		critItem = item;
		_itemMask = null;
		if(item == null || work() == null)
			return;
		Integer criter = (Integer)critItem.valueForKey("criterion");
		_itemMask = work().getCriterMask(criter);
	}

	
    public Object criterMax() {
        if(_itemMask == null) return null;
//		if(work() == null)
//			return null;
//		Integer criterion = null;
//		if(critItem == null)
//			criterion = new Integer(0);
//		else 
		Integer criterion = (Integer)critItem.valueForKey("criterion");
		return work().maxForCriter(criterion);
   }
    
    /*
    public void setCriterMax(Number newCriterMax) {
		boolean weightToMax = SettingsReader.boolForKeyPath("edu.weightToMax",true);
        if(_itemMask == null) { // create new criterMask
			if(newCriterMax == null) return;
			_itemMask = EOUtilities.createAndInsertInstance(work().editingContext(),"CriterMask");
			work().addObjectToBothSidesOfRelationshipWithKey(_itemMask,"criterMask");
			_itemMask.takeValueForKey(critItem,"criterion");
			if(weightToMax)
				_itemMask.takeValueForKey(newCriterMax,"weight");
			else
				_itemMask.takeValueForKey(new Integer(1),"weight");
			
			_itemMask.takeValueForKey(newCriterMax,"max");
		} else if(newCriterMax == null) { // remove criter max
			work().removeObjectFromBothSidesOfRelationshipWithKey(_itemMask,"criterMask");
			_itemMask = null;
			return;
		} else if(newCriterMax.intValue() != criterMax().intValue()){  // update criter max
			
			if(weightToMax && (criterWeight()==null || criterWeight().equals(criterMax())))
				_itemMask.takeValueForKey(newCriterMax,"weight");
			_itemMask.takeValueForKey(newCriterMax,"max");
		}
    }
	
    public Number criterWeight() {
        if(_itemMask == null) return null;
		return (Number)_itemMask.valueForKey("max");
    }
    public void setCriterWeight(Number newCriterWeight) {
        if(_itemMask == null || newCriterWeight == null) {
			return;
		}
       if(newCriterWeight.intValue() != criterWeight().intValue())
		   _itemMask.takeValueForKey(newCriterWeight,"weight");
    }
*/
	public boolean dispalayPopup () {
		if(work() == null) return false;
		EOEditingContext ec = work().editingContext();
		if(ec == null) return true;
		if(ec.insertedObjects().containsObject(work())) return true;
		/*
		NSArray tasks = work().homeTasks();
		if(tasks == null || tasks.count() == 0) return false;
		Enumeration en = tasks.objectEnumerator();
		while (en.hasMoreElements()) {
			if(ec.insertedObjects().containsObject(en.nextElement())) return true;
		}*/
		return false;
	}
	
	public String announce() {
		return dateFormat().format(work().announce());
	}
	
	public void setAnnounce(String newDate) {
		if(newDate != null) {
			Date aDate = (Date)dateFormat().parseObject(newDate,
					new java.text.ParsePosition(0));
			if(aDate != null) {
				Integer eduYear = (Integer)session().valueForKey("eduYear");
				aDate = MyUtility.dateToEduYear(aDate, eduYear);
				work().setAnnounce(new NSTimestamp(aDate));
			}
		}
	}
	
	public String theme() {
		return work().theme();
	}
	
	public void setTheme(String aTheme) {
		if(aTheme != null && aTheme.equals(work().theme()))
			return;
		NSDictionary snapshot = work().editingContext().currentEventSnapshotForObject(work());
		if(snapshot == null) {
			work().setTheme(aTheme);
			return;
		}
		Object oldThemeSnapshot = snapshot.valueForKey("theme");
		String oldTheme = null;
		if(oldThemeSnapshot instanceof String && ((String)oldThemeSnapshot).length() > 0)
			oldTheme = (String)oldThemeSnapshot;
		if(oldTheme == null && aTheme == null)
			return;
		if(oldTheme == null || !oldTheme.equals(aTheme)) {
			work().setTheme(aTheme);
		}
	}
	
	public String rowClass() {
		if(lessonItem == valueForBinding("currLesson")) return "selection";
		return null; //work().styleClass();
	}
	
	public String rowColor() {
		if(lessonItem == valueForBinding("currLesson")) return null;
		return work().color();
	}
	/*
	public boolean wide() {
		return (Various.boolForObject(valueForBinding("showHomeTask")) && (valueForBinding("student") == null));
	}*/
		
	public Work currLesson() {
		return (Work)valueForBinding("currLesson");
	}
	
	private NSArray _criteria;
    public NSArray criteria() {
		if(_criteria == null) {
			EduCourse course = (EduCourse)valueForBinding("course");
			_criteria = CriteriaSet.criteriaForCourse(course);
		}
		return _criteria;
    }
	
    public WOActionResults inspectorPopup() {
    	WOComponent nextPage = pageWithName("WorkInspector");
    	nextPage.takeValueForKey(context().page(), "returnPage");
//    	EOEditingContext tmpEc = new EOEditingContext(lessonItem.editingContext());
//    	EOEnterpriseObject editWork = EOUtilities.localInstanceOfObject(tmpEc, lessonItem);
    	nextPage.takeValueForKey(lessonItem, "work");
    	return nextPage;
    }
    
    public WOActionResults addWork() {
    	valueForBinding("save");
    	WOComponent nextPage = pageWithName("WorkInspector");
    	nextPage.takeValueForKey(context().page(), "returnPage");
    	if(session().valueForKey("message") != null)
    		nextPage.takeValueForKey(valueForBinding("currLesson"), "work");
    	return nextPage;
    }
    /*
    public boolean lessonIsNew() {
    	lessonItem = (Work)valueForBinding("currLesson");
    	if(lessonItem == null)
    		return false;
    	EOEditingContext ec = lessonItem.editingContext();
    	return (ec.globalIDForObject(lessonItem).isTemporary());
    }*/

	public String dateStyle() {
		StringBuffer buf = new StringBuffer("text-align:center;width:");
		if(Various.boolForObject(valueForBinding("wide")))
			buf.append("11");
		else
			buf.append('5');
		buf.append("ex;");
		return buf.toString();
	}

	public String announceStyle() {
		if(expiredDate(work().announce()))
			return "grey";
		return null;
	}
	
	public Boolean hideRow() {
		Boolean hide = canEdit();
		if(hide.booleanValue())
			return hide;
		Student student = (Student)valueForBinding("student");
		if(student == null)
			return Boolean.FALSE;
		hide = (Boolean)session().objectForKey("hideMarkless");
		if(hide != null && !hide.booleanValue())
			return hide;
		return new Boolean((work().criterMask() == null || work().criterMask().count() == 0) &&
				work().noteForStudent(student) == null);
	}
	
	public String criterlessMax() {
		if(work() == null)
			return null;
		if(!work().noCriteria())
			return null;
		StringBuilder buf = new StringBuilder("<td");
		if(criteria() != null && criteria().count() > 1)
			buf.append(" colspan = \"").append(criteria().count()).append('"');
		else
			buf.append(" style = \"font-weight:bold;\"");
		buf.append('>');
		Object max = work().maxForCriter(Integer.valueOf(0));
		if(max == null)
			buf.append('-');
		else
			buf.append(max);
		buf.append("</td>");
		return buf.toString();
	}
	
	public static NSArray filterWorks(NSArray works, Object filter) {
		if(works == null || works.count() == 0 || filter == null)
			return works;
		int level = -1;
		if(filter instanceof NSKeyValueCoding)
			filter = ((NSKeyValueCoding) filter).valueForKey("level");
		if(filter instanceof Number)
			level = ((Number) filter).intValue();
		if(filter instanceof String) {
			NSArray list = (NSArray) WOApplication.application().valueForKeyPath(
					"strings.RujelCriterial_Strings.worksTab.filter");
			NSDictionary dict = (NSDictionary)list.objectAtIndex(0);
			list = (NSArray) dict.valueForKey("select");
			level = list.indexOf(filter);
		}
		if(level <= 0)
			return works;
		NSMutableArray result = new NSMutableArray();
		Enumeration enu = works.objectEnumerator();
		Integer lag = null;
		while (enu.hasMoreElements()) {
			Work w = (Work) enu.nextElement();
			if(level > 3 && !w.hasWeight())
				continue;
			boolean compulsory = (w.isCompulsory() && w.hasWeight()); 
			if(compulsory) {
				if(lag == null) {
					lag = SettingsBase.numericSettingForCourse("countMarklessWorkAfter", 
							w.course(), w.editingContext());
					if(lag == null)
						lag = Integer.valueOf(-1);
				}
				if(lag.intValue() < 0 || 
						EOPeriod.Utility.countDays(w.date(), null) > lag.intValue()) {
					result.addObject(w);
					continue;
				}
			}
			if(w.criterMask() == null || w.criterMask().count() == 0) {
				CriteriaSet critSet = w.critSet();
				if(critSet == null || critSet.criterionForNum(Integer.valueOf(0)) == null)
					continue;
			} else if(level < 2) {
				result.addObject(w);
				continue;
			}
			if(w.marks() == null || w.marks().count() == 0) {
				if(compulsory) {
					if(level > 3)
						continue;
				} else {
					if(level > 2 || (level == 2 && w.isHometask()))
						continue;
				}
			}
			result.addObject(w);
		}
		return result;
	}
}
	