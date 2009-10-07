package net.rujel.autoitog;

import java.math.BigDecimal;
import java.util.Enumeration;

import net.rujel.criterial.Mark;
import net.rujel.criterial.Work;
import net.rujel.eduresults.ItogContainer;
import net.rujel.eduresults.ItogType;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.EduLesson;
import net.rujel.interfaces.Student;
import net.rujel.reusables.Various;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public abstract class WorkCalculator extends Calculator {

	protected static final NSArray reliesOn = new NSArray(new String[] {"Work","Mark"});

	public WorkCalculator() {
		super();
	}

	public NSArray reliesOn() {
		return reliesOn;
	}

	public String reliesOnEntity() {
		return "Work";
	}

	protected static NSArray marksForStudentAndWorks(Student student, NSArray works) {
		NSMutableArray quals = new NSMutableArray(Various.getEOInQualifier("work",works));
		quals.addObject(new EOKeyValueQualifier("student",EOQualifier.QualifierOperatorEqual,student));
		
		EOFetchSpecification fs = new EOFetchSpecification("Mark",new EOAndQualifier(quals),null);
		fs.setRefreshesRefetchedObjects(true);
		return student.editingContext().objectsWithFetchSpecification(fs);
	}
	
	public NSArray collectRelated(EduCourse course, AutoItog autoItog) {
		if(!autoItog.calculatorName().equals(this.getClass().getName()))
			throw new IllegalStateException("Should be applied to AutoItog related to this Calculator");
		EOEditingContext ec = autoItog.editingContext();
		EOQualifier[] quals = new EOQualifier[2];
		quals[0] = new EOKeyValueQualifier(Work.DATE_KEY,(autoItog.evening())?
				EOQualifier.QualifierOperatorLessThanOrEqualTo:
					EOQualifier.QualifierOperatorLessThan,autoItog.fireDate());
		quals[1] = new EOKeyValueQualifier("course",
				EOQualifier.QualifierOperatorEqual,course);
//		if(checkWeight) {
			NSMutableArray allQuals = new NSMutableArray(quals);
			allQuals.addObject(new EOKeyValueQualifier(Work.WEIGHT_KEY,
					EOQualifier.QualifierOperatorGreaterThan,BigDecimal.ZERO));
			quals[0] = new EOAndQualifier(allQuals); 
//		} else {
//			quals[0] = new EOAndQualifier(new NSArray(quals));
//		}
		EOFetchSpecification fs = new EOFetchSpecification(Work.ENTITY_NAME,quals[0],null);
		NSArray works = ec.objectsWithFetchSpecification(fs);
		if(works == null || works.count() == 0)
			return null;
		NSArray aiList = EOUtilities.objectsMatchingKeyAndValue(ec,
				AutoItog.ENTITY_NAME, AutoItog.LIST_NAME_KEY, autoItog.listName());
		NSMutableArray mentioned = new NSMutableArray();
		if(aiList != null && aiList.count() > 0) {
			Enumeration enu = aiList.objectEnumerator();
			ItogContainer itog = autoItog.itogContainer();
			ItogType type = itog.itogType();
			Integer eduYear = itog.eduYear();
			while (enu.hasMoreElements()) {
				AutoItog ai = (AutoItog) enu.nextElement();
				if(ai == autoItog || ai.itogContainer().itogType() != type 
						|| !ai.itogContainer().eduYear().equals(eduYear))
					continue;
				if(!getClass().getName().equals(ai.calculatorName()) && 
						!ai.calculator().reliesOnEntity().equals(reliesOnEntity()))
					continue;
				NSArray found = ai.relKeysForCourse(course);
				if(found != null && found.count() > 0)
				mentioned.addObjectsFromArray((NSArray)found.valueForKey("relKey"));
			}
		}
			/*
		quals[0] = new EOKeyValueQualifier("itogContainer.itogType",
				EOQualifier.QualifierOperatorEqual,autoItog.itogContainer().itogType());
		EOQualifier.filterArrayWithQualifier(aiList, quals[0]);
		aiList.removeObject(autoItog);
//		EOSortOrdering.sortArrayUsingKeyOrderArray(aiList, AutoItog.sorter);
		quals[0] = Various.getEOInQualifier("autoItog", aiList);
		allQuals.removeAllObjects();
		allQuals.addObjects(quals);
		quals[0] = new EOAndQualifier(allQuals);
		fs.setEntityName("ItogRelated");
		fs.setQualifier(quals[0]);
		NSArray mentioned = ec.objectsWithFetchSpecification(fs);
		if(mentioned != null && mentioned.count() > 0)
			mentioned = (NSArray)mentioned.valueForKey("relKey");
		else
			mentioned = null; */
		quals[0] = new EOKeyValueQualifier("autoItog",EOQualifier.QualifierOperatorEqual,
				autoItog);
		allQuals.removeAllObjects();
		allQuals.addObjects(quals);
		quals[0] = new EOAndQualifier(allQuals);
		fs.setEntityName("ItogRelated");
		fs.setQualifier(quals[0]);
		NSArray already = ec.objectsWithFetchSpecification(fs);
		NSMutableDictionary forNum = null;
		if(already != null && already.count() > 0)
			forNum = new NSMutableDictionary(already,(NSArray)already.valueForKey("relKey"));
		Enumeration enu = works.objectEnumerator();
		NSMutableArray result = new NSMutableArray();
		while (enu.hasMoreElements()) {
			Work work = (Work) enu.nextElement();
			EOKeyGlobalID gid = (EOKeyGlobalID)ec.globalIDForObject(work);
			Object key = gid.keyValues()[0];
			if(mentioned != null && mentioned.containsObject(key))
				continue;
			result.addObject(work);
			if(forNum == null || forNum.removeObjectForKey(key) == null) {
				EOEnterpriseObject ir = EOUtilities.createAndInsertInstance(ec, "ItogRelated");
				ir.addObjectToBothSidesOfRelationshipWithKey(autoItog, "autoItog");
				ir.addObjectToBothSidesOfRelationshipWithKey(course, "course");
				ir.takeValueForKey(key, "relKey");
			}
		}
		if(forNum != null && forNum.count() > 0) {
			enu = forNum.objectEnumerator();
			while (enu.hasMoreElements()) {
				EOEnterpriseObject ir = (EOEnterpriseObject) enu.nextElement();
				ec.deleteObject(ir);
			}
		}
		return result;
	}
	
	public Integer relKeyForObject(Object object) {
		Work work = null;
		if (object instanceof Work) {
			work = (Work) object;
		} else if (object instanceof Mark) {
			work = ((Mark)object).work();
		} else if(object instanceof NSDictionary) {
			NSDictionary dict = (NSDictionary)object;
			EduLesson lesson = (EduLesson)dict.valueForKey("lesson");
			if(lesson instanceof Work)
				work = (Work)lesson;
			else
				return null;
		} else {
			return null;
		}
		try {
			if(work.weight().compareTo(BigDecimal.ZERO) == 0)
				return null;
			EOKeyGlobalID gid = (EOKeyGlobalID)work.editingContext().
								globalIDForObject(work);
			return (Integer)gid.keyValues()[0];
		} catch (Exception e) {
			return null;
		}
	}
}