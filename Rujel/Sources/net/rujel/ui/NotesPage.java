// NotesPage.java: Class file for WO Component 'NotesPage'

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

import net.rujel.base.GenericAddOn;
import net.rujel.interfaces.*;
import net.rujel.reusables.*;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.Enumeration;

public class NotesPage extends WOComponent {
	protected static final String presenter = SettingsReader.stringForKeyPath("ui.presenter.note","NotePresenter");
// 	protected static final String selectorPresenter = SettingsReader.stringForKeyPath("ui.presenter.selector",null);
    public PerPersonLink lessonItem;
    public Student studentItem;
	//public String selectStudentAction;
	
//	public EduCourse course;
	
    /** @TypeInfo PerPersonLink */
    //public NSArray lessonsList;
    /** @TypeInfo Student */
    //public NSArray studentsList;
	
	//public NSKeyValueCoding present;
	
    public NotesPage(WOContext context) {
        super(context);
    }
	
	protected Boolean _single;
	public boolean single() {
		if(_single == null) {
			Object tmp = valueForBinding("single");
			if(tmp != null && tmp instanceof Boolean) {
				_single = (Boolean)tmp;
			} else {
				_single = new Boolean(Various.boolForObject(tmp));
			}
		}
		return _single.booleanValue();
	}
	/*
	public void setSingle(Object val) {
		single = Various.boolForObject(val);
	}*/
						  
	public NSArray lessonsListing() {
		if(single()) {
			if(currLesson() != null)
				return new NSArray(currLesson());
			else
				return null;
		} 
		return (NSArray)valueForBinding("lessonsList");
		//return lessonsList;
	}
	/*
	public String selectorPresenter() {
		if (single) return null;
		return selectorPresenter;
	}*/
	
	protected PerPersonLink _currLesson;
	public PerPersonLink currLesson() {
		if(_currLesson == null) {
			_currLesson = (PerPersonLink)valueForBinding("currLesson");
		}
		return _currLesson;
	}

	protected String _currPresenter;
	public String presenter() {
		if(_currPresenter == null) {
			NSKeyValueCoding present = (NSKeyValueCoding)valueForBinding("present");
			if(present == null) {
				_currPresenter = presenter;
				return presenter;
			}
			_currPresenter = (String)present.valueForKey("presenter");
			if(currLesson() != null) {
				String entName = (String)present.valueForKey("entityName");
				String currEnt = null;
				if(currLesson() instanceof EOEnterpriseObject) {
					currEnt = ((EOEnterpriseObject)currLesson()).entityName();
				} else {
					currEnt = currLesson().getClass().getName();
					currEnt = currEnt.substring(currEnt.lastIndexOf('.') + 1);
				}
				if(!currEnt.equals(entName)) {
					entName = (String)present.valueForKey(currEnt);
					if(entName == null)
						entName = (String)present.valueForKey("tmpPresenter");
					if(entName != null)
						_currPresenter = entName;
				}
			}
			if(_currPresenter == null)
				_currPresenter = presenter;
		}
		return _currPresenter;
	}

	public String studentStyle() {
		Object selectStudent = valueForBinding("selectStudent");
		if(studentItem == null) {
			if(selectStudent != null && selectStudent == lessonItem && !single())
				return "selection";
			else
				return "grey";
		}
		if(currLesson() == null && hasBinding("selectStudentAction")) {
			if(selectStudent != null && selectStudent.equals(studentItem))
				return "selection";
		}
		Boolean sex = (Boolean)valueForKeyPath("studentItem.person.sex");
		if(sex == null) return "grey";
		if (sex.booleanValue())
			return "male";
		else
			return "female";
	}
	
	public String onmouseover() {
		if(hasBinding("selectStudentAction")) {
			return "this.className = '" + studentStyle() + "Dim';";
		} else
			return null;
	}
	
	public String onclick() {
		if(hasBinding("selectStudentAction")) {
			String href = context().componentActionURL();
			String result = "return checkRun('" + href + "');";
			return result;
		} else
			return null;
	}
	
    
	public NSArray unmentionedStudents() {
		NSArray lessonsList = (NSArray)valueForBinding("lessonsList");
		NSArray studentsList = (NSArray)valueForBinding("studentsList");

		if(lessonsList == null || lessonsList.count() == 0 || !(lessonsList.objectAtIndex(0) instanceof EduLesson)) return null;
		NSMutableSet unmentionedSet = new NSMutableSet();
		NSSet mentioned = new NSSet(studentsList);
		Enumeration lessEnum = lessonsList.objectEnumerator();
		EduLesson les;
		//Enumeration noteEnum;
		//EOEnterpriseObject note;
		//Object stu;
		while (lessEnum.hasMoreElements()) {
			les = (EduLesson)lessEnum.nextElement();
			unmentionedSet.addObjectsFromArray(les.students());
			/*noteEnum = les.notes().objectEnumerator();
			while (noteEnum.hasMoreElements()) {
				note = (EOEnterpriseObject)noteEnum.nextElement();
				stu = note.storedValueForKey("student");
				if(!studentsList.containsObject(stu))
					unmentionedSet.addObject(stu);
			}*/
		}
		unmentionedSet.subtractSet(mentioned);
		if(unmentionedSet.count() < 1)
			return null;
		NSArray tmp = unmentionedSet.allObjects();
		if(tmp.count() == 1)
			return tmp;
		
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(tmp,Person.sorter);
	}
	
//	public Object selectStudent;
	
    public Integer idx;

    /** @TypeInfo com.webobjects.foundation.NSKeyValueCoding */
    public NSArray allAddOns;
    public NSKeyValueCoding addOnItem;
    /** @TypeInfo java.lang.String */
    public NSMutableArray activeAddOns;
//    public String activeAddOnItem;
	
	public Integer idx() {
		if(idx == null)return null;
		return new Integer(idx.intValue() + 1);
	}
	
	public void selectLesson() {
		_currLesson = lessonItem;
			setValueForBinding(_currLesson,"currLesson");
		//selectStudent = studentItem;
		if(hasBinding("selectStudent"))
			setValueForBinding(studentItem,"selectStudent");
		if(lessonItem instanceof EOEnterpriseObject) {
			EOEditingContext ec = ((EOEnterpriseObject)lessonItem).editingContext();
			if (ec.hasChanges()) ec.revert();
		}
    }
	
	public WOActionResults studentSelection() {
		//selectStudent = studentItem;
		if(hasBinding("selectStudent"))
			setValueForBinding(studentItem,"selectStudent");
		EOEditingContext ec = studentItem.editingContext();
		if (ec.hasChanges()) ec.revert();
		return (WOActionResults)valueForBinding("selectStudentAction");
		//performParentAction(selectStudentAction);//
	}
	
	public String cellID () {
		Object selectStudent = valueForBinding("selectStudent");
		if(selectStudent == null || studentItem == null || !selectStudent.equals(studentItem))
			return null;
		else
			return "focus";
	}
	
	public void reset() {
//		selectStudent = null;
		_currLesson = null;
		_currPresenter = null;
		lessonItem = null;
		studentItem = null;
//		lessonsList = null;
//		studentsList = null;
		_single = null;
//		present = null;
		addOnItem = null;
		allAddOns = null;
		activeAddOns = null;
	}
	
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
    public WOActionResults save() {
        WOActionResults result = (WOActionResults)parent().valueForKey("save");
		_currLesson = (EduLesson)valueForBinding("currLesson");
		return result; 
/*		currLesson = null;
		setValueForBinding(currLesson,"currLesson");
		return null;*/
    }
	
	public boolean isSelected() {
		return (currLesson() != null && lessonItem == currLesson());
	}
/*
	public String lessonTitle() {
		if(lessonItem==null)return null;
        String result = lessonItem.title();
		if(result != null)
			return result;
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(lessonItem.date());
		int day = cal.get(GregorianCalendar.DAY_OF_MONTH);
		int month = cal.get(GregorianCalendar.MONTH);
		NSArray months = (NSArray)application().valueForKeyPath("strings.presets.monthShort");
		return String.format("<small>%1$s</small><br/><b>%2$d</b>",months.objectAtIndex(month),day);
	}
	
	public String tdStyle() {
		if(lessonItem == currLesson)
			return "selection";
		else
			return studentStyle();
    }
	
    public void setNoteForStudent(String newNoteForStudent) {
        lessonItem.setNoteForStudent(newNoteForStudent,studentItem);
    }
	
    public String noteForStudent() {
		if(studentItem == null) return null;
        return lessonItem.noteForStudent(studentItem);
    }
	
	public String shortNoteForStudent() {
		String theNote = noteForStudent();
		if (theNote == null || theNote.length() <= 3)
			return theNote;
		String url = application().resourceManager().urlForResourceNamed("text.png",null,null,context().request());
		return "<img src=\"" + url + "\" alt=\"" + theNote + "\" height=\"16\" width=\"16\">";
	}
	
	public String fullNoteForStudent() {
		if(studentItem == null)
			return lessonItem.theme();
		String theNote = noteForStudent();
		if (theNote == null || theNote.length() <= 3)
			return null;
		return theNote;
	}
	
	public String onClick() {
		if(currLesson == null || lessonItem != currLesson) {
			String href = context().componentActionURL();
			return "checkRun('" + href + "');";
		}
		else
			return null;
    } */
	
	public void setStudentItem(Object item) {
		if(item instanceof Student) {
			studentItem = (Student)item;
		} else {
			studentItem = null;
			
		}
	}
	
	public NSArray allAddOns() {
		if((single() && currLesson() != null) || session()==null) return null;
		if(allAddOns == null) {
			allAddOns = (NSArray)valueForBinding("allAddOns");
		}
		if(allAddOns == null) {
			allAddOns = (NSArray)session().valueForKeyPath("modules.notesAddOns");
			if(allAddOns == null)
				allAddOns = NSArray.EmptyArray;
			setValueForBinding(allAddOns,"allAddOns");
		}
		return allAddOns;
	}
	
	public NSMutableArray activeAddOns() {
		if((single() && currLesson() != null) || session()==null) return null;
		if(activeAddOns == null) {
			activeAddOns = (NSMutableArray)valueForBinding("activeAddOns");
		}
		if(activeAddOns == null) {
			activeAddOns = new NSMutableArray();
			if(allAddOns() != null && allAddOns.count() > 0) {
				Enumeration en  = allAddOns.objectEnumerator();
				while (en.hasMoreElements()) {
					NSKeyValueCoding curr = (NSKeyValueCoding)en.nextElement();
					if(Various.boolForObject(curr.valueForKey("defaultOn"))) {
						activeAddOns.addObject(curr);
					}
				}
			}
			setValueForBinding(activeAddOns,"activeAddOns");
		} else {
			if(activeAddOns.count() > 0 && allAddOns().count() > 0) {
				if(activeAddOns.objectAtIndex(0) instanceof String) {
					NSMutableArray result = new NSMutableArray();
					Enumeration enu = activeAddOns.objectEnumerator();
					while (enu.hasMoreElements()) {
						String id = (String)enu.nextElement();
						NSKeyValueCoding addOn = GenericAddOn.addonForID(allAddOns,id);
						if(addOn != null) {
							addOn = GenericAddOn.activeAddOn(addOn);
							result.addObject(addOn);
						}
					}
					activeAddOns.setArray(result);
				}
			}
			
		}
		return activeAddOns;
	}
	
	/*
	public boolean single() {
		return (lessonsList != null && lessonsList.count() == 1);
	}*/
    /** @TypeInfo NSKeyValueCoding */
}
