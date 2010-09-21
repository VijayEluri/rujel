// EditSubstitute.java: Class file for WO Component 'EditSubstitute'

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

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.rujel.interfaces.*;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.*;

import net.rujel.interfaces.Teacher;
import net.rujel.reusables.Various;
import net.rujel.reusables.WOLogLevel;
import net.rujel.ui.TeacherSelector;

// Generated by the WOLips Templateengine Plug-in at Oct 1, 2008 1:23:43 PM
public class EditSubstitute extends com.webobjects.appserver.WOComponent {
	protected static Logger logger = Logger.getLogger("rujel.curriculum");

	public EditSubstitute(WOContext context) {
        super(context);
    }
    
    public WOActionResults returnPage;
    public Substitute substitute;
    public EduLesson lesson;
    public NSArray forcedList;
	public Teacher teacher;
	public Reason reason;
	public Boolean cantSelect;
	public Boolean cantEdit = Boolean.TRUE;
	public Boolean canDelete = Boolean.FALSE;
	public NSArray fromList;
	public EduLesson item;
	public EduLesson fromLesson;
//	public NSArray tabs;
	public int idx;
	public String message;
	protected NSArray others;

   
    public void setLesson(EduLesson aLesson) {
    	lesson = aLesson;
    	EOEditingContext ec = lesson.editingContext();
    	EOGlobalID userGID = (EOGlobalID)session().valueForKey("userPersonGID");
    	if(userGID != null) {
       	   	PersonLink userPerson = (PersonLink)ec.faultForGlobalID(userGID,ec);
       	   	if(userPerson instanceof Teacher && !(userPerson == lesson.course().teacher())) {
       	   		forcedList = new NSArray(userPerson);
       	   		setTeacher((Teacher)userPerson);
       	   		cantSelect = (Boolean)session().valueForKeyPath("readAccess._edit.Substitute");
       	   	}
    	}
    	others = (NSArray)lesson.valueForKey("substitutes");
    }
    
    public EduCourse course() {
    	return (lesson == null)?null: lesson.course();
    }
    
    public void setIdx(Integer index) {
    	if(index == null)
    		idx = 0;
    	else
    		idx = index.intValue();
//    	if(idx > 0)
//    		populateFrom();
//    	if(fromLesson == null || fromList == null || !fromList.contains(fromLesson))
//    		cantEdit = Boolean.TRUE;
    }
    
    protected void populateFrom() {
//    	fromLesson = null;
    	if(teacher == null) {
    		message = "<div style = \"font-size:larger;color:#ff6600;\">" +
    			session().valueForKeyPath(
    				"strings.RujelCurriculum_Curriculum.messages.teacherRequired") +
    				"</div>";
    		return;
    	}
    	EOQualifier[] quals = new EOQualifier[2];
    	quals[0] = new EOKeyValueQualifier("teacher",
    			EOQualifier.QualifierOperatorEqual,teacher);
    	quals[1] = new EOKeyValueQualifier("eduYear",
    			EOQualifier.QualifierOperatorEqual,lesson.course().eduYear());
    	quals[1] = new EOAndQualifier(new NSArray(quals));
    	EOFetchSpecification fs = new EOFetchSpecification(
    			EduCourse.entityName,quals[1],null);
    	EOEditingContext ec = lesson.editingContext();
    	fromList = ec.objectsWithFetchSpecification(fs);
    	quals[1] = new EOKeyValueQualifier("date",
    			EOQualifier.QualifierOperatorEqual,lesson.date());
    	quals[0] = new EOAndQualifier(new NSArray(quals));
    	fs.setEntityName(Substitute.ENTITY_NAME);
    	fs.setQualifier(quals[0]);
    	NSArray subs = ec.objectsWithFetchSpecification(fs);
    	if(fromList != null && fromList.count() > 0) {
    		quals[0] = Various.getEOInQualifier("course", fromList);
        	quals[0] = new EOAndQualifier(new NSArray(quals));
        	fs.setEntityName(EduLesson.entityName);
        	fs.setQualifier(quals[0]);
        	fromList = ec.objectsWithFetchSpecification(fs);
    	}
    	if(subs != null && subs.count() > 0) {
    		NSMutableArray result = (fromList == null)?
    				new NSMutableArray() : fromList.mutableClone();
    		Enumeration enu = subs.objectEnumerator();
    		while (enu.hasMoreElements()) {
				Substitute sub = (Substitute) enu.nextElement();
				EduLesson subLesson = sub.lesson();
				EduLesson subFrom = sub.fromLesson();
				if(subFrom == null) {
					if(subLesson != lesson && !result.containsObject(subLesson))
						result.addObject(subLesson);
				} else if(subFrom != lesson && !result.containsObject(subFrom)) {
					result.addObject(subFrom);
				}
			}
    		subs = new NSArray(EOSortOrdering.sortOrderingWithKey("course", 
    				EOSortOrdering.CompareAscending));
    		EOSortOrdering.sortArrayUsingKeyOrderArray(result, subs);
    		fromList = result.immutableClone();
    	}
    	if(fromList == null || fromList.count() == 0) {
    		fromList = NSArray.EmptyArray;
    		message =  "<div style = \"font-size:larger;color:#ff6600;\">" +
				session().valueForKeyPath(
				"strings.RujelCurriculum_Curriculum.messages.noLessonsForTeacher") +
				"</div>";
    	} else if(fromLesson == null) {
    		message = "<strong>" + session().valueForKeyPath(
			"strings.RujelCurriculum_Curriculum.titles.chooseLesson") + "</strong>";
    	} else {
    		message = null;
    	}
    	if(fromList.count() > 1) {
    		NSArray sorter =  new NSArray(new EOSortOrdering[] {
    				new EOSortOrdering("course",EOSortOrdering.CompareAscending),
    				new EOSortOrdering("number",EOSortOrdering.CompareAscending)
    		});
    		fromList = EOSortOrdering.sortedArrayUsingKeyOrderArray(fromList,sorter);
    	}
    }
    
    public String presentFromLesson() {
    	if(item == null)
    		item = fromLesson;
    	if(item == null)
    		return null;
    	StringBuilder buf = new StringBuilder("<strong style = \"white-space:nowrap;\">");
    	buf.append(item.course().eduGroup().name()).append("</strong> ");
    	if(item.course().cycle() != lesson.course().cycle() &&
    			!item.course().cycle().subject().equals(lesson.course().cycle().subject()))
    		buf.append(item.course().cycle().subject()).append(' ');
    	if(item.course().comment() != null) {
    		buf.append("<em style = \"white-space:nowrap;\">(").append(
    			WOMessage.stringByEscapingHTMLString(item.course().comment())).append(")</em>");
    	}
//    	buf.append(':').append(' ');
//    	buf.append(WOMessage.stringByEscapingHTMLString(item.theme()));
    	return buf.toString();
    }
    
    public String joinBindStyle() {
    	if(idx == 0)
    		return "display:none;";
    	return null;
    }
    
    public String lessonsListStyle() {
    	if(fromLesson != null)
    		return "display:none;";
    	return null;
    }
    
    public String factor() {
    	if(substitute != null)
    		return substitute.factor().stripTrailingZeros().toString();
    	BigDecimal factor = BigDecimal.ONE;
    	if(idx > 0)
    		factor = Substitute.joinFactor();
    	if(others != null && others.count() > 0) {
    		factor = factor.divide(new BigDecimal(others.count() +1),
    				2,BigDecimal.ROUND_HALF_UP);
    	}
    	return factor.toString();
    }

    public void setFactor(String factor) {
    	if(substitute == null)
    		return;
    	substitute.setFactor(new BigDecimal(factor));
    }
    
    public void setSubstitute(Substitute sub) {
    	substitute = sub;
    	if(sub != null) {
    		teacher = sub.teacher();
    		if(forcedList == null || !forcedList.contains(teacher)) {
    			if(forcedList == null)
    				forcedList = new NSArray(teacher);
    			else
    				forcedList = forcedList.arrayByAddingObject(teacher);
    		}
    		reason = substitute.reason();
//    		if(sub.factor().compareTo(BigDecimal.ONE) < 0) {
    		if(sub.fromLesson() != null) {
    			idx = 1;
    		} else {
    			idx = 0;
    		}
    		fromLesson = sub.fromLesson();
			populateFrom();
			session().setObjectForKey(substitute, "readAccess");
    		cantEdit = (Boolean)session().valueForKeyPath("readAccess._edit.session");
    		cantSelect = cantEdit;
    		canDelete = (Boolean)session().valueForKeyPath("readAccess.delete.session");
    		session().removeObjectForKey("readAccess");
    		if(others == null)
    			others = new NSArray(substitute);
    		else if(!others.containsObject(substitute))
    			others = others.arrayByAddingObject(substitute);
    	} else {
    		session().setObjectForKey(new NSDictionary(lesson.course(),"course"), "readAccess");
    		cantSelect = (Boolean)session().valueForKeyPath("readAccess._edit.Substitute");
    		session().removeObjectForKey("readAccess");
    	}
		session().removeObjectForKey("readAccess");
		if(cantSelect.booleanValue() && teacher == null) {
			cantSelect = Boolean.FALSE;
		}
    }
    
    public WOActionResults selectFrom() {
    	if(cantEdit.booleanValue())
    		return this;
    	fromLesson = item;
    	message = null;
//    	cantEdit = Boolean.FALSE;
    	idx = 1;
    	return this;
    }

    public void setTeacher(Teacher aTeacher) {
    	teacher = (Teacher)EOUtilities.localInstanceOfObject(lesson.editingContext(), aTeacher);
    	fromLesson = null;
    	if(teacher == lesson.course().teacher()) {
    		teacher = null;
    		cantEdit = Boolean.TRUE;
    		return;
    	}
    	if(aTeacher != null) {
    		if(substitute == null) {
    			cantEdit = (Boolean)session().valueForKeyPath("readAccess._create.Substitute");
    		} else {
    			session().setObjectForKey(substitute, "readAccess");
    			cantEdit = (Boolean)session().valueForKeyPath("readAccess._edit.session");
    			session().removeObjectForKey("readAccess");
    		}
    	} else {
    		cantEdit = Boolean.TRUE;
    	}
//    	if(idx > 0) {
//        	if(fromLesson == null || fromList == null || !fromList.contains(fromLesson))
//        		cantEdit = Boolean.TRUE;
//    	}
		populateFrom();
    }
    
	public WOActionResults save() {
		EOEditingContext ec = lesson.editingContext(); 
		if(others != null && others.count() > 0) {
			Enumeration enu = others.objectEnumerator();
			while (enu.hasMoreElements()) {
				Substitute sub = (Substitute) enu.nextElement();
				if(sub != substitute && sub.teacher() == teacher) {
					reason = null;
					session().takeValueForKey(application().valueForKeyPath(
						"strings.RujelCurriculum_Curriculum.messages.duplicateTeacher"), "message");
					session().removeObjectForKey("lessonProperies");
					break;
				}
			}
		}
		if(reason == null) {
			if (returnPage instanceof WOComponent) 
				((WOComponent)returnPage).ensureAwakeInContext(context());
			if(ec.hasChanges())
				ec.revert();
			return returnPage;
		}
		String action = "modified";
		ec.lock();
		if(substitute == null) {
			substitute = (Substitute)EOUtilities.createAndInsertInstance(ec, 
					Substitute.ENTITY_NAME);
			substitute.addObjectToBothSidesOfRelationshipWithKey(lesson, "lesson");
			action = "created";
			if(others != null && !others.containsObject(substitute))
				others = others.arrayByAddingObject(substitute);
		}
		substitute.setDate(lesson.date());
		substitute.addObjectToBothSidesOfRelationshipWithKey(teacher,"teacher");
		BigDecimal subFactor = BigDecimal.ONE;
		BigDecimal joinFactor = Substitute.joinFactor();
		if(idx > 0)
			substitute.addObjectToBothSidesOfRelationshipWithKey(fromLesson, "fromLesson");
		else
			substitute.setFromLesson(null);
		if(others != null && others.count() > 1) {
			subFactor = subFactor.divide(new BigDecimal(others.count()),
					2,BigDecimal.ROUND_HALF_UP);
			joinFactor = joinFactor.divide(new BigDecimal(others.count()),
					2,BigDecimal.ROUND_HALF_UP);
			Enumeration enu = others.objectEnumerator();
			while (enu.hasMoreElements()) {
				Substitute sub = (Substitute) enu.nextElement();
				if(sub.fromLesson() == null) {
					sub.setFactor(subFactor);
				} else {
					if(joinFactor.compareTo(sub.factor()) < 0)
						sub.setFactor(joinFactor);
				}
			}
		}
		if(idx > 0) {
			NSArray joins = (NSArray)fromLesson.valueForKey("joins"); 
//				EOUtilities.objectsMatchingKeyAndValue(ec, 
//					Substitute.ENTITY_NAME, "fromLesson",fromLesson);
			if(!joins.containsObject(substitute))
				joins = joins.arrayByAddingObject(substitute);
			if(joins != null && joins.count() > 1) {
				BigDecimal factor = Substitute.joinFactor();
				factor = factor.divide(new BigDecimal(joins.count()),
						2,BigDecimal.ROUND_HALF_UP);
				Enumeration enu = joins.objectEnumerator();
				while (enu.hasMoreElements()) {
					Substitute join = (Substitute) enu.nextElement();
					if(factor.compareTo(join.factor()) < 0)
						join.setFactor(factor);
				}
			} else {
				substitute.setFactor(joinFactor);
			}
		} else {
			substitute.setFactor(subFactor);
		}
		substitute.addObjectToBothSidesOfRelationshipWithKey(reason, "reason");
		return done(action);
	}
	
	public WOActionResults delete() {
		EOEditingContext ec = lesson.editingContext(); 
		ec.lock();
		if(substitute != null && substitute.editingContext() != null) {
			logger.log(WOLogLevel.EDITING,"Deleting substitute",substitute);
			if(others != null && others.count() > 1) {
				Enumeration enu = others.objectEnumerator();
				while (enu.hasMoreElements()) {
					Substitute sub = (Substitute) enu.nextElement();
					if(sub == substitute)
						continue;
					sub.updateFactor(sub.countRelated(null, substitute));
				}
			}
			if(fromLesson != null) {
				NSArray joins = (NSArray)fromLesson.valueForKey("joins");
				if(joins != null && joins.count() > 1) {
					Enumeration enu = joins.objectEnumerator();
					while (enu.hasMoreElements()) {
						Substitute join = (Substitute) enu.nextElement();
						if(join == substitute)
							continue;
						join.updateFactor(join.countRelated(null, substitute));
					}
				}
			}
			ec.deleteObject(substitute);
		}
		return done("deleted");
	}
	
	protected WOActionResults done(String action) {
		EOEditingContext ec = lesson.editingContext();
		try {
			ec.saveChanges();
			Object[] args = new Object[] {session(),lesson};
			logger.log(WOLogLevel.EDITING,"Substitute for lesson " + action,args);
		} catch (NSValidation.ValidationException vex) {
			ec.revert();
	    	others = (NSArray)lesson.valueForKey("substitutes");
			session().takeValueForKey(vex.getMessage(), "message");
			Object[] args = new Object[] {session(),lesson,vex.getMessage()};
			logger.log(WOLogLevel.FINE,"Failed to save "+ action + " Substitute for lesson ",args);
		} catch (Exception e) {
			ec.revert();
	    	others = (NSArray)lesson.valueForKey("substitutes");
			session().takeValueForKey(e.getMessage(), "message");
			Object[] args = new Object[] {session(),lesson,e};
			logger.log(WOLogLevel.WARNING,"Failed to save "+ action + " Substitute for lesson ",args);
		} finally {
			ec.unlock();
		}
		session().removeObjectForKey("lessonProperies");
		if (returnPage instanceof WOComponent) 
			((WOComponent)returnPage).ensureAwakeInContext(context());
		return returnPage;
	}
	
	public String checkSave() {
		if(fromLesson != null)
			return null;
		StringBuilder buf = new StringBuilder(
				"if(selectType[1].checked){alert('");
		buf.append(session().valueForKeyPath(
				"strings.RujelCurriculum_Curriculum.messages.joinRequiresLesson"));
		buf.append("');return false;}");
		return buf.toString();
	}
	
	public WOActionResults review() {
		WOComponent nextPage = pageWithName("LessonNoteEditor");
		nextPage.takeValueForKey(fromLesson,"currLesson");
		nextPage.takeValueForKey(fromLesson.course(),"course");
		nextPage.takeValueForKey(nextPage.valueForKey("currLesson"),"selector");
		session().takeValueForKey(returnPage,"pushComponent");
		return nextPage;
	}
	
	public WOComponent selectTeacher() {
		WOComponent popup = TeacherSelector.selectorPopup(this, "teacher",
				lesson.editingContext());
		popup.takeValueForKeyPath(Boolean.TRUE, "dict.presenterBindings.ajaxReturn");
		popup.takeValueForKeyPath(Boolean.TRUE, "dict.presenterBindings.hideVacant");
		popup.takeValueForKeyPath("ajaxPopup", "dict.onCancel");
		popup.takeValueForKeyPath(lesson.valueForKeyPath("course.cycle.subject"),
				"dict.presenterBindings.subject");
		return popup;
	}
	
	public WOComponent initial() {
		if(cantSelect.booleanValue())
			return this;
		return selectTeacher();
	}
	
	public boolean noSubstitute() {
		return (substitute == null);
	}
 }