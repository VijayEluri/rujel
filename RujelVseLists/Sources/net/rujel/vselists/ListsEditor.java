// ListsEditor.java: Class file for WO Component 'ListsEditor'

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

package net.rujel.vselists;

/*
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
*/
import java.util.Enumeration;
import java.util.logging.Logger;

import net.rujel.base.MyUtility;
import net.rujel.interfaces.Person;
import net.rujel.interfaces.PersonLink;
import net.rujel.reusables.Counter;
import net.rujel.reusables.DegenerateFlags;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.PlistReader;
import net.rujel.reusables.SessionedEditingContext;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Aug 27, 2009 11:13:10 PM
public class ListsEditor extends com.webobjects.appserver.WOComponent {
	public static final Logger logger = Logger.getLogger("rujel.vselists");

	public static final NSArray sorter = new NSArray(new Object[] {
			new EOSortOrdering("delo",EOSortOrdering.CompareAscending),
			new EOSortOrdering("person",EOSortOrdering.CompareAscending)
	});

	public static Object init(Object obj, WOContext ctx) {
		if(obj == null || obj.equals("init")) {
			try {
				Object access = PlistReader.readPlist("access.plist", "RujelVseLists", null);
				WOApplication.application().takeValueForKey(access, "defaultAccess");
			} catch (NSKeyValueCoding.UnknownKeyException e) {
				// default access not supported
			}
			Person.Utility.delegateManager.addDelegate(new PersonDelegate(),30);
		} else if(obj.equals("regimes")) {
			if(ctx != null && ctx.hasSession())
				return ctx.session().valueForKeyPath(
					"strings.RujelVseLists_VseStrings.listRegime");
			return WOApplication.application().valueForKeyPath(
					"strings.RujelVseLists_VseStrings.listRegime");
		} else if(obj.equals("personInspector")) {
			return personInspector(ctx);
		}
		return null;
	}
	
	public EOEditingContext ec;
    public boolean showAll = false;
    public NSKeyValueCodingAdditions item;
    public NamedFlags access;
    public NSArray list;
	public Integer mode;
//	public NSMutableDictionary agregate;
	public NSArray categories;
	public Object selection;
	public Boolean cantAddClass;
	public NSTimestamp date;
	public NSMutableSet ticks = new NSMutableSet();
	public NSArray targets;
	public VseEduGroup target;
    
    public ListsEditor(WOContext context) {
        super(context);
    	ec = new SessionedEditingContext(context.session());
//        switchMode();
    }
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	NSTimestamp sesDate = (NSTimestamp)session().valueForKey("today");
    	if(!sesDate.equals(date) && categories == null) {
    		date = sesDate;
    		switchMode();
    	}
		date = sesDate;
    	super.appendToResponse(aResponse, aContext);
    }
    
    public void toggleAll() {
    	showAll = !showAll;
    	setSelection(selection);
    	ticks.removeAllObjects();
    }
    
    public void switchMode() {
    	if(mode == null)
    		mode = new Integer(0);
    	selection = null;
    	target = null;
    	targets = null;
    	ticks.removeAllObjects();
    	list = NSArray.EmptyArray;
     	if(mode.intValue() > 0) {
//    		agregate = TeacherSelector.populate(ec, session());
//    		categories = (NSArray)agregate.removeObjectForKey("subjects");
    		categories = (NSArray)VseTeacher.agregatedList(ec, date);
        	access = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.VseTeacher");
        	cantAddClass = Boolean.FALSE;
    	} else {
//    		agregate = VseStudent.studentsAgregate(ec, date);
    		categories = VseStudent.agregatedList(ec, date);
        	access = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.VseStudent");
        	cantAddClass = (Boolean)session().valueForKeyPath(
        			"readAccess._create.VseEduGroup");
    	}
//		categories = (NSArray)agregate.removeObjectForKey("list");
    }
    
    public void setSelection(Object sel) {
    	ticks.removeAllObjects();
    	if(sel == null) {
    		list = NSArray.EmptyArray;
    	} else if(sel instanceof VseEduGroup) {
    			setGroup((VseEduGroup)sel);
    	} else if(sel instanceof NSDictionary) {
        	selection = sel;
    		NSDictionary dict = (NSDictionary)sel;
        	boolean studMode = (mode == null || mode.intValue() == 0); 
    		list = (NSArray)dict.valueForKey("list");
    		if(list == null) {
    			EOQualifier qual = (EOQualifier)dict.valueForKey("qualifier");
    			String ent = (studMode)?VseStudent.ENTITY_NAME:VseTeacher.ENTITY_NAME;
    			EOFetchSpecification fs = new EOFetchSpecification(ent,qual,null);
    			list = ec.objectsWithFetchSpecification(fs);
    			if(list == null)
    				list = NSArray.EmptyArray;
    			if(list.count() > 0) {
    				if(studMode) {
    					list = EOQualifier.filteredArrayWithQualifier(list,
    							VseStudent.notInGroup);
    				}
    				dict.takeValueForKey(new Counter(list.count()), "allCount");
    			}
    		}
    		if(list != null && list.count() != 0) {
				if(!showAll) {
					NSArray args = new NSArray(new Object[] {date,date});
					EOQualifier qual = EOQualifier.qualifierWithQualifierFormat(
					  "(enter = nil OR enter <= %@) AND (leave = nil OR leave >= %@)", args);
					list = EOQualifier.filteredArrayWithQualifier(list,qual);
					dict.takeValueForKey(new Counter(list.count()), "currCount");
				}
				if(list.count() > 1)
					list = EOSortOrdering.sortedArrayUsingKeyOrderArray(list, sorter);
    		}
        	if(studMode)
        		access =(NamedFlags)session().valueForKeyPath("readAccess.FLAGS.VseStudent");
        	else
        		access =(NamedFlags)session().valueForKeyPath("readAccess.FLAGS.VseTeacher");
    	} else {
    		selection = null;
    		list = NSArray.EmptyArray;
    		Enumeration enu = categories.objectEnumerator();
    		while (enu.hasMoreElements()) {
				Object object = (Object) enu.nextElement();
				Object name = NSKeyValueCoding.Utility.valueForKey(object, "name");
				if(sel.toString().equals(name.toString())) {
					setSelection(object);
					break;
				}
			}
    	}
    	targets = targets();
    }
    
    protected NSArray targets() {
    	if(selection == null || mode != null && mode.intValue() > 0)
    		return null;
    	Integer absGrade = (Integer)NSKeyValueCoding.Utility.valueForKey(selection,"absGrade");
    	if(absGrade == null)
    		return null;
    	NSMutableArray result = new NSMutableArray();
    	Enumeration enu = categories.objectEnumerator();
    	while (enu.hasMoreElements()) {
			Object cat = enu.nextElement();
			if(cat == selection)
				continue;
			if(cat instanceof VseEduGroup) {
				Integer ag = ((VseEduGroup)cat).absGrade();
				if(ag.equals(absGrade))
					result.addObject(cat);
				else if(ag.compareTo(absGrade) < 0)
					break;
			}
		}
    	if(result.count() == 0)
    		return null;
    	return result;
    }
    
    public VseEduGroup group() {
    	if(selection instanceof VseEduGroup)
    		return (VseEduGroup)selection;
    	else
    		return null;
    }
    
    public Boolean noGroupEdit() {
    	if(selection instanceof VseEduGroup) {
    		return (Boolean)session().valueForKeyPath("readAccess._edit.group");
    	} else {
    		return Boolean.TRUE;
    	}
    }

    public void setGroup(VseEduGroup gr) {
    	selection = gr;
    	access = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.VseList");
    	if(gr == null) {
    		list = null;
    	} else if(showAll) {
    		list = EOSortOrdering.sortedArrayUsingKeyOrderArray(gr.lists(),VseList.sorter);
    	} else {
    		list = gr.vseList();
    	}
    }
    
    public WOActionResults createGroup() {
    	WOComponent result = pageWithName("VseGroupInspector");
    	result.takeValueForKey(this, "returnPage");
    	result.takeValueForKey(ec, "ec");
    	return result;
    }
    
    public WOActionResults editGroup() {
    	WOComponent result = pageWithName("VseGroupInspector");
    	result.takeValueForKey(this, "returnPage");
    	result.takeValueForKey(selection, "currGroup");
    	return result;
    }
    
    public PersonLink plink() {
    	if(item == null)
    		return null;
    	if(item instanceof PersonLink)
    		return (PersonLink)item;
    	return (PersonLink)item.valueForKey("student");
    }
    
	public String rowClass() {
		if (showAll) {
			NSTimestamp enter = (NSTimestamp) item.valueForKey("enter");
			NSTimestamp leave = (NSTimestamp) item.valueForKey("leave");
			if (enter != null || leave != null) {
				NSTimestamp today = (NSTimestamp) session()
						.valueForKey("today");
				if (leave != null && leave.compare(today) < 0)
					return "grey";
				if (enter != null && enter.compare(today) > 0)
					return "grey";
			}
		}
		Boolean sex = (Boolean)valueForKeyPath("plink.person.sex");
		if(sex == null) return "gerade";
		return (sex.booleanValue())?"male":"female";
	}
	
	public WOActionResults editPerson() {
		WOComponent popup = pageWithName("PersonInspector");
		popup.takeValueForKey(this, "returnPage");
		popup.takeValueForKey(plink(), "personLink");
		popup.takeValueForKey("newPerson", "resultPath");
		return popup;
	}
	
	public WOActionResults addPerson() {
		WOComponent popup = pageWithName("SelectorPopup");
		popup.takeValueForKey(this, "returnPage");
		NSMutableDictionary dict = ((NSDictionary)session().valueForKeyPath(
		"strings.RujelVseLists_VseStrings.selectPerson")).mutableClone();
    	String ent = (mode == null || mode.intValue() == 0)?
    			VseStudent.ENTITY_NAME:VseTeacher.ENTITY_NAME; 
		dict.takeValueForKeyPath(ent, "presenterBindings.entity");
		popup.takeValueForKey(dict, "dict");
		popup.takeValueForKey("newPerson", "resultPath");
		return popup;
	}
	
	public void setNewPerson(PersonLink person) {
		boolean student = (mode == null || mode.intValue() == 0);
		if(student && selection == null)
			return;
		if(person == null) {
			ec.revert();
			return;
		}
		person = (PersonLink)EOUtilities.localInstanceOfObject(ec,
				(EOEnterpriseObject)person);
		if (group() != null) {
			Enumeration enu = group().vseList().objectEnumerator();
			while (enu.hasMoreElements()) {
				EOEnterpriseObject vl = (EOEnterpriseObject) enu.nextElement();
				if (vl.valueForKeyPath("student.person") == person.person()) {
//					session().takeValueForKey(session().valueForKeyPath(
//						"strings.RujelVseLists_VseStrings.duplicateEntry"),"message");
					return;
				}
			}
		}
		ec.lock();
		try {
			if (student) {
				if(!(person instanceof VseStudent))
					person = VseStudent.studentForPerson(person.person(), date, true);
				VseStudent aStudent = (VseStudent)person;
				if (selection instanceof VseEduGroup) {
					aStudent.setAbsGrade(group().absGrade());
					EOEnterpriseObject newEntry = EOUtilities
							.createAndInsertInstance(ec, "VseList");
					newEntry.addObjectToBothSidesOfRelationshipWithKey(group(),
							"eduGroup");
					newEntry.addObjectToBothSidesOfRelationshipWithKey(
							aStudent, "student");
					newEntry.takeValueForKey(date, "enter");
					logger.log(WOLogLevel.EDITING, "Added person to group",
							new Object[] {session(),person,selection});
				} else {
					Integer grade = aStudent.absGrade();
					if(grade == null || grade.intValue() <= 0) {
						grade = (Integer)NSKeyValueCoding.Utility.valueForKey(
								selection, "absGrade");
						if(grade.intValue() < 1000) {
							Integer year = MyUtility.eduYearForDate(date);
							grade = new Integer(year.intValue() - grade.intValue());
						}
						aStudent.setAbsGrade(grade);
					}
					if(aStudent.enter() == null)
						aStudent.setEnter(date);
				}
			} else {
				if(!(person instanceof VseTeacher))
					person = VseTeacher.teacherForPerson(person.person(), date, true);
			}
			ec.saveChanges();
//			if(!list.containsObject(person)) {
				if(student) {
					if(person instanceof VseStudent) {
						Object sel = selection;
						setSelection(((VseStudent)person).absGrade());
						setSelection(sel);
					}
//					list = list.arrayByAddingObject(person);
				} else {
					categories = (NSArray)VseTeacher.agregatedList(ec, date);
					setSelection(person.person().lastName().substring(0,1));
//				}
			}
		} catch (RuntimeException e) {
			logger.log(WOLogLevel.WARNING, "Error adding person to group",
					new Object[] {session(),person,selection,e});
			session().takeValueForKey(e.getMessage(), "message");
			ec.revert();
		} finally {
			ec.unlock();
		}
	}
	
	public void save() {
		if(!ec.hasChanges())
			return;
		ec.lock();
		try {
			ec.saveChanges();
			logger.log(WOLogLevel.EDITING, "Changed enter/leave dates",
					new Object[] {session(),selection});
		} catch (Exception e) {
			logger.log(WOLogLevel.WARNING, "Error saving enter/leave dates changes",
					new Object[] {session(),selection,e});
			session().takeValueForKey(e.getMessage(), "message");
			ec.revert();
		} finally {
			ec.unlock();
		}
	}
	
	public Boolean hideAddButton() {
		if(mode == null || mode.intValue() == 0) {
			if(selection == null)
				return Boolean.TRUE;
			if(selection instanceof VseEduGroup)
				return (Boolean)session().valueForKeyPath("readAccess._create.VseList");
			return (Boolean)session().valueForKeyPath("readAccess._create.VseStudent");
		} else {
			return (Boolean)session().valueForKeyPath("readAccess._create.VseTeacher");
		}
	}

	public static Object personInspector(WOContext ctx) {
		WOSession ses = ctx.session();
		EOEnterpriseObject person = (EOEnterpriseObject)ses.objectForKey("PersonInspector");
		if(person == null)
			return null;
		NSMutableArray result = new NSMutableArray();
		EOEditingContext ec = person.editingContext();
		NamedFlags access = DegenerateFlags.ALL_FALSE;
		boolean created = ec.globalIDForObject(person).isTemporary();
		if(created) {
			try {
				Person pers = (Person)person.valueForKey(VseTeacher.PERSON_KEY);
				if(pers != null && ec.globalIDForObject(pers).isTemporary()) {
					access = (NamedFlags)ses.valueForKeyPath(
							"readAccess.FLAGS." + pers.entityName());
				}
			} catch(Exception e) {
				
			}
		} else {
			access = (NamedFlags)ses.valueForKeyPath(
			"readAccess.FLAGS." + person.entityName());
		}
		if(access.getFlag(0)) {
			NSDictionary dict = (NSDictionary)ses.valueForKeyPath(
					"strings.RujelVseLists_VseStrings.inspectors.Person");
			dict = PlistReader.cloneDictionary(dict, true);
			if(created)
				dict.takeValueForKey(person.valueForKey(VseTeacher.PERSON_KEY), "usage");
			else
				dict.takeValueForKey(person, "usage");
			dict.takeValueForKey(access, "access");
			result.addObject(dict);
		}
		access = (NamedFlags)ses.valueForKeyPath("readAccess.FLAGS.VseStudent");
		NSArray list = null;
		if(access.getFlag(0)) {
			if(created) {
					list = null;
			} else { 
				list = EOUtilities.objectsMatchingKeyAndValue(ec,
						VseStudent.ENTITY_NAME, VseStudent.PERSON_KEY, person);
			}
			if((person instanceof VseStudent) || (list != null && list.count() > 0)) {
				NSDictionary dict = (NSDictionary)ses.valueForKeyPath(
						"strings.RujelVseLists_VseStrings.inspectors.Student");
				dict = PlistReader.cloneDictionary(dict, true);
				dict.takeValueForKey(access, "access");
				if(created)
					dict.takeValueForKey(person, "usage");
				else
					dict.takeValueForKey(list, "usage");
				result.addObject(dict);
			}
		}
		access = (NamedFlags)ses.valueForKeyPath("readAccess.FLAGS.VseTeacher");
		if(access.getFlag(0)) { 
			if(created) {
				list = null;
			} else { 
				list = EOUtilities.objectsMatchingKeyAndValue(ec,
						VseTeacher.ENTITY_NAME, VseTeacher.PERSON_KEY, person);
			}
			if((person instanceof VseTeacher) || (list != null && list.count() > 0)) {
				NSDictionary dict = (NSDictionary)ses.valueForKeyPath(
						"strings.RujelVseLists_VseStrings.inspectors.Teacher");
				dict = PlistReader.cloneDictionary(dict, true);
				if(created)
					dict.takeValueForKey(person, "usage");
				else
					dict.takeValueForKey(list, "usage");
				dict.takeValueForKey(access, "access");
				result.addObject(dict);
			}
		}
		if(result.count() == 0)
			return null;
		return result;
	}
	
	public void select() {
		setSelection(item);
	}
/*
	public Object hideRow() {
		if(showAll)
			return Boolean.FALSE;
		if (item instanceof NSDictionary) {
			return NSKeyValueCoding.Utility.valueForKey(item, "hide");
		}
		return Boolean.FALSE;
	}*/

	public String listClass() {
		if(selection == item)
			return "selection";
		if(item instanceof EOEnterpriseObject)
			return "ungerade";
		Object count = item.valueForKey("currCount"); //(showAll)?"allCount":
		if(Various.boolForObject(count))
			return "orange";
		else
			return "grey";
	}
	
	public Number count() {
		if(item instanceof EOEnterpriseObject) {
			if(showAll)
				return (Number)item.valueForKeyPath("lists.count");
			else
				return (Number)item.valueForKey("count");
		}
		return (Number)item.valueForKey((showAll)?"allCount":"currCount");
	}

	public boolean tick() {
		if(item == null)
			return false;
		return ticks.containsObject(item);
	}

	public void setTick(boolean tick) {
		if(item == null)
			return;
		if(tick)
			ticks.addObject(item);
		else
			ticks.removeObject(item);
	}
	
	public String actionTitle() {
		StringBuilder buf = new StringBuilder();
		if(selection instanceof VseEduGroup) {
			buf.append(session().valueForKeyPath(
					"strings.RujelVseLists_VseStrings.actions.transfer"));
		} else {
			buf.append(session().valueForKeyPath(
					"strings.RujelVseLists_VseStrings.actions.employ"));
		}
		buf.append(' ').append(item.valueForKey("name"));
		return buf.toString();
	}
	
	public WOActionResults act() {
		if(ticks.count() == 0)
			return null;
		if(target == null && !(selection instanceof VseEduGroup)) {
			session().setObjectForKey(ticks.allObjects(), "deleteStudents");
			NSArray objections = (NSArray)session().valueForKeyPath("modules.deleteStudents");
			session().removeObjectForKey("deleteStudents");
			if(objections != null && objections.count() > 0) {
				session().takeValueForKey(
						objections.componentsJoinedByString("<br/>\n"), "message");
				return null;
			}
		}
		Enumeration enu = ticks.objectEnumerator();
		while (enu.hasMoreElements()) {
			EOEnterpriseObject row = (EOEnterpriseObject) enu.nextElement();
			if(target == null) {
				if(date == null) {
					ec.deleteObject(row);
				} else {
					row.takeValueForKey(date, VseStudent.LEAVE_KEY);
				}
				continue;
			}
			if(row instanceof VseList) {
				if(date == null) {
					ec.deleteObject(row);
				} else {
					row.takeValueForKey(date.timestampByAddingGregorianUnits(0, 0, -1, 0, 0, 0),
							VseStudent.LEAVE_KEY);
				}
				target.addStudent((VseStudent)row.valueForKey(VseList.STUDENT_KEY), date);
			} else if(row instanceof VseStudent){
				target.addStudent((VseStudent)row, date);
			}
		}
		ticks.removeAllObjects();
		save();
		if(date == null)
			date = (NSTimestamp)session().valueForKey("today");
		Object sel = selection;
		switchMode();
		setSelection(sel);
		return null;
	}
	
	public boolean showTicks() {
		if(mode != null && mode.intValue() > 0)
			return false;
		return list != null && list.count() > 0 && 
			(access == null || access.flagForKey("edit"));
	}
	
	/*
	public InputStream uploadStream;
	public NSMutableDictionary uploadDict = new NSMutableDictionary(); 
		
	public WOActionResults upload() {
		WOResponse response = application().createResponseInContext(context());
		response.appendContentHTMLString(uploadDict.toString());
		if(uploadStream == null) {
			response.appendContentString("null input");
			return response;
		}
		try {
			if(!uploadStream.markSupported())
				uploadStream = new BufferedInputStream(uploadStream,1024);
			uploadStream.mark(512);
			NSArray charsets = new NSArray("cp1251");
			Enumeration cenu = charsets.objectEnumerator();
			CharsetDecoder decoder = Charset.forName("utf8").newDecoder();
			decoder.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					uploadStream,decoder),1024);
			response.appendContentString("<table border = \"1\">\n");
			while(true) {
				String line = null;
				try {
					line = in.readLine();
					uploadStream.mark(1024);
				} catch (java.nio.charset.CharacterCodingException e) {
					if(cenu.hasMoreElements()) {
						String charset = (String)cenu.nextElement();
						uploadStream.reset();
						decoder = Charset.forName(charset).newDecoder();
						decoder.onMalformedInput(CodingErrorAction.REPORT)
							.onUnmappableCharacter(CodingErrorAction.REPORT);
						in = new BufferedReader(
								new InputStreamReader(uploadStream,decoder),1024);
						continue;
					} else {
						response.appendContentString(
								"<tr><td>unsupported character set</td></tr>");
					}
				}
				if(line == null)
					break;
				response.appendContentString("<tr>");
				String[] split = line.split(" ");
				if(split != null && split.length > 0) {
					for (int i = 0; i < split.length; i++) {
						response.appendContentString("<td>");
						response.appendContentHTMLString(split[i]);
						response.appendContentString("</td>\n");
					}
				}
				response.appendContentString("</tr>\n");
			}
			response.appendContentString("</table>");
		} catch (Exception e) {
			logger.log(WOLogLevel.INFO,"Error importing file", new Object[] {session(),e});
//			session().takeValueForKey(e.getMessage(), "message");
			throw new NSForwardException(e);
		}
		uploadStream = null;
		uploadDict.removeAllObjects();
		return response;
	}
	*/
	
}