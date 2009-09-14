package net.rujel.curriculum;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.EduLesson;
import net.rujel.reusables.SettingsReader;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

// Generated by the WOLips Templateengine Plug-in at Sep 14, 2009 1:34:50 PM
public class EditJoin extends com.webobjects.appserver.WOComponent {
	protected static Logger logger = Logger.getLogger("rujel.curriculum");
    public EditJoin(WOContext context) {
        super(context);
    }
    
    public WOComponent returnPage;
    public EduLesson lesson;
    public Substitute substitute;

    public NSArray courses;
    public NSArray lessons;
    public Object item;
    public EduCourse selCourse;
    public EduLesson selLesson;
	public Reason reason;
	public Boolean cantEdit = Boolean.TRUE;
	public Boolean newLesson;
	
	public void setLesson(EduLesson l) {
		lesson = l;
		EOQualifier[] quals = new EOQualifier[3];
		EduCourse course = lesson.course();
		quals[0] = new EOKeyValueQualifier("cycle",EOQualifier.QualifierOperatorEqual,
				course.cycle());
		quals[1] = new EOKeyValueQualifier("eduYear",EOQualifier.QualifierOperatorEqual,
				course.eduYear());
		quals[1] = new EOKeyValueQualifier("teacher",EOQualifier.QualifierOperatorNotEqual,
				course.teacher());
		quals[0] = new EOAndQualifier(new NSArray(quals));
		EOFetchSpecification fs = new EOFetchSpecification(EduCourse.entityName,
				quals[0],null);
		courses = lesson.editingContext().objectsWithFetchSpecification(fs);
		if(courses != null && courses.count() > 1)
		courses = EOSortOrdering.sortedArrayUsingKeyOrderArray(courses, EduCourse.sorter);
		String path = "readAccess." + 
				((substitute==null)?"_create.S":"_edit.s") +"ubstitute";
		cantEdit = (Boolean)session().valueForKeyPath(path);
	}
	
	public void setSubstitute(Substitute sub) {
		substitute = sub;
		if(sub != null) {
			selLesson = sub.lesson();
			item = selLesson.course();
			selectCourse();
			item = null;
			reason = sub.reason();
    		cantEdit = (Boolean)session().valueForKeyPath("readAccess._edit.substitute");
		}
	}
	
	public WOActionResults selectCourse() {
		selCourse = (EduCourse)item;
		if(selCourse == null) {
			lessons = null;
			return this;
		}
		if(selLesson.course() != selCourse)
			selLesson = null;
		NSTimestamp date = null;
		if(lesson != null)
			date = lesson.date();
		else if(selLesson != null)
			date = selLesson.date();
		EOQualifier[] quals = new EOQualifier[2];
		quals[0] = new EOKeyValueQualifier("course",EOQualifier.QualifierOperatorNotEqual,
				selCourse);
		quals[1] = new EOKeyValueQualifier("date",EOQualifier.QualifierOperatorNotEqual,
				date);
		quals[0] = new EOAndQualifier(new NSArray(quals));
		EOFetchSpecification fs = new EOFetchSpecification(EduLesson.entityName,
				quals[0],EduLesson.sorter);
		lessons = selCourse.editingContext().objectsWithFetchSpecification(fs);
		newLesson = Boolean.FALSE;
		return this;
	}
	
	public WOActionResults toggleNewLesson() {
		if(selLesson == null) {
			if(selCourse == null)
				newLesson = Boolean.FALSE;
			else
				newLesson = Boolean.TRUE;
		}  else {
			newLesson = Boolean.TRUE;
		}
		return this;
	}

	public WOActionResults save() {
		EOEditingContext ec = lesson.editingContext(); 
		NSArray others = (selLesson == null)?null:
			(NSArray)selLesson.valueForKey("substitutes");
		if(others != null && others.count() > 0) {
			Enumeration enu = others.objectEnumerator();
			while (enu.hasMoreElements()) {
				Substitute sub = (Substitute) enu.nextElement();
				if(sub != substitute && sub.teacher() == lesson.course().teacher()) {
					reason = null;
					session().takeValueForKey(application().valueForKeyPath(
						"strings.RujelCurriculum_Curriculum.messages.duplicateTeacher"), 
						"message");
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
		String action = "saved";
		ec.lock();
		if(substitute == null) {
			substitute = (Substitute)EOUtilities.createAndInsertInstance(ec,
					Substitute.ENTITY_NAME);
			substitute.addObjectToBothSidesOfRelationshipWithKey(lesson, "fromLesson");
			action = "created";
		}
		if(selLesson == null) {
			selLesson = (EduLesson) EOUtilities.createAndInsertInstance(ec,
					EduLesson.entityName);
			selLesson.addObjectToBothSidesOfRelationshipWithKey(selCourse, "course");
			selLesson.setDate(lesson.date());
			selLesson.setTheme(lesson.theme());
			Integer num = (Integer)selCourse.valueForKeyPath("lessons.@max.number");
			num = new Integer(num.intValue() +1); 
			selLesson.setNumber(num);
		}
		substitute.addObjectToBothSidesOfRelationshipWithKey(selLesson, "lesson");
		substitute.addObjectToBothSidesOfRelationshipWithKey(
				lesson.course().teacher(),"teacher");
		substitute.addObjectToBothSidesOfRelationshipWithKey(reason, "reason");
		BigDecimal factor = new BigDecimal(SettingsReader.stringForKeyPath(
				"edu.joinFactor", "0.5"));
		if(others != null) {
			if(!others.containsObject(substitute))
				others = others.arrayByAddingObject(substitute);
			int div = others.count();
			if(div > 1) {
				factor = factor.divide(new BigDecimal(div), 2,BigDecimal.ROUND_HALF_UP);
				Enumeration enu = others.objectEnumerator();
				StringBuilder buf = new StringBuilder();
				while (enu.hasMoreElements()) {
					Substitute sub = (Substitute) enu.nextElement();
					String theme = (String)sub.valueForKeyPath("fromLesson.theme");
					if(theme == null)
						theme = "...";
					if(buf.indexOf(theme) < 0) {
						if(buf.length() > 0)
							buf.append(" / ");
						buf.append(theme);
					}
					sub.setFactor(factor);
				}
				selLesson.setTheme(buf.toString());
			}
		}
		substitute.setFactor(factor);
		return done(action);
	}
	
	public WOActionResults delete() {
		EOEditingContext ec = lesson.editingContext(); 
		ec.lock();
		if(substitute != null && substitute.editingContext() != null) {
			logger.log(WOLogLevel.UNOWNED_EDITING,"Deleting substitute",substitute);
			ec.deleteObject(substitute);
		}
		return done("deleted");
	}
	
	protected WOActionResults done(String action) {
		EOEditingContext ec = lesson.editingContext();
		try {
			ec.saveChanges();
			Object[] args = new Object[] {session(),selLesson};
			logger.log(WOLogLevel.UNOWNED_EDITING,"Join for lesson " + action,args);
		} catch (NSValidation.ValidationException vex) {
			ec.revert();
			String message = vex.getMessage();
			session().takeValueForKey(vex.getMessage(), "message");
			Object[] args = new Object[] {session(),selLesson,message};
			logger.log(WOLogLevel.FINE,"Failed to save "+ action + 
					" Join for lesson ",args);
		} catch (Exception e) {
			ec.revert();
			session().takeValueForKey(e.getMessage(), "message");
			Object[] args = new Object[] {session(),selLesson,e};
			logger.log(WOLogLevel.WARNING,"Failed to save "+ action + 
					" Join for lesson ",args);
		} finally {
			ec.unlock();
		}
		if (returnPage instanceof WOComponent) 
			((WOComponent)returnPage).ensureAwakeInContext(context());
		return returnPage;
	}
}