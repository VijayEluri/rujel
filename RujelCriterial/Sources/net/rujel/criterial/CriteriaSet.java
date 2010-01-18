// CriteriaSet.java

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

package net.rujel.criterial;


import java.util.Enumeration;

import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EduCourse;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.EOUtilities;

public class CriteriaSet extends _CriteriaSet
{
	public static final NSArray sorter = new NSArray(EOSortOrdering.sortOrderingWithKey("criterion",EOSortOrdering.CompareAscending));
    public CriteriaSet() {
        super();
    }
	
	/*
	 // If you add instance variables to store property values you
	 // should add empty implementions of the Serialization methods
	 // to avoid unnecessary overhead (the properties will be
	 // serialized for you in the superclass).
	 private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
	 }
	 
	 private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
	 }
	 */
	
	public NSArray sortedCriteria() {
		return EOSortOrdering.sortedArrayUsingKeyOrderArray(criteria(),sorter);
	}
	
	public EOEnterpriseObject criterionNamed(String critName) {
		if(criteria() == null || criteria().count() == 0)
			return null;
		Enumeration enu = criteria().objectEnumerator();
		while (enu.hasMoreElements()) {
			EOEnterpriseObject crit = (EOEnterpriseObject) enu.nextElement();
			if(critName.equals(crit.valueForKey("title")))
				return crit;
		}
		return null;
	}

	public static String critNameForNum(Integer criter, CriteriaSet set) {
		if(criter.intValue() == 0)
			return "#";
		if(set == null)
			return Character.toString((char)('A' + criter.intValue() -1));
		return set.critNameForNum(criter);
	}

	public String critNameForNum(Integer criterion) {
		EOEnterpriseObject criter = criterionForNum(criterion);
		return (criter==null)?null:(String)criter.valueForKey("title");
	}
	
	public EOEnterpriseObject criterionForNum(Integer criterion) {
		if(criteria() == null || criteria().count() == 0)
			return null;
		Enumeration enu = criteria().objectEnumerator();
		while (enu.hasMoreElements()) {
			EOEnterpriseObject crit = (EOEnterpriseObject) enu.nextElement();
			if(criterion.equals(crit.valueForKey("criterion")))
				return crit;
		}
		return null;
	}
	
	public Integer criterionForName(String name) {
		if(name == null)
			return new Integer(0);
		EOEnterpriseObject crit = criterionNamed(name);
		return (crit == null)?null:(Integer)crit.valueForKey("criterion");
	}
	
	public void addCriterion() {
		EOEnterpriseObject criterion = EOUtilities.createAndInsertInstance(editingContext(),"Criterion");
		addObjectToBothSidesOfRelationshipWithKey(criterion,"criteria");
		Number num = (Number)criteria().valueForKeyPath("@max.sort");
		num = (num==null)?new Integer(1):new Integer(num.intValue() + 1);
		criterion.takeValueForKey(num,"sort");
	}
	
	public static CriteriaSet critSetForCourse(EduCourse course) {
		EOEditingContext ec = course.editingContext();
		Integer set = SettingsBase.numericSettingForCourse(ENTITY_NAME, course,ec);
		if(set == null || set.intValue() == 0)
			return null;
		return (CriteriaSet)EOUtilities.objectWithPrimaryKeyValue(ec, ENTITY_NAME, set);
	}
	
	public static NSArray criteriaForCourse(EduCourse course) {
		CriteriaSet set = critSetForCourse(course);
		if (set!=null)
			return set.sortedCriteria();
		int maxCriter = maxCriterionForCourse(course);
		return criteriaForMax(maxCriter);
	}
	
	public static NSArray criteriaForMax(int maxCriter) {
		if(maxCriter == 0)
			return NSArray.EmptyArray;
		char first = 'A';
		NSDictionary[] result = new NSDictionary[maxCriter];
		for (int i = 0; i < maxCriter; i++) {
			String title = Character.toString((char)(first + i));
			NSDictionary critDict = new NSDictionary( new Object[]
					{title, new Integer(i + 1)} , new String[] {"title","criterion"});
			result[i] = critDict;
		}
		return new NSArray(result);
	}
	
	public static String titleForCriterion(int criterion) {
		if(criterion == 0)
			return "#";
		return Character.toString((char)('A' + criterion -1));
	}
	
	public static int maxCriterionForCourse(EduCourse course) {
		CriteriaSet set = critSetForCourse(course);
		if(set != null) {
			Integer max = (Integer)set.criteria().valueForKey("@max.criterion");
			return (max == null)?0:max.intValue();
		}
		NSArray works = EOUtilities.objectsMatchingKeyAndValue(course.editingContext(), 
				Work.ENTITY_NAME, "course", course);
		return maxCriterionInWorks(works);
	}
	
	public static int maxCriterionInWorks (NSArray works) {
		if(works == null || works.count() == 0)
			return 0;
		int max = 0;
		Enumeration enu = works.objectEnumerator();
		while (enu.hasMoreElements()) {
			Work work = (Work) enu.nextElement();
			NSArray mask = work.criterMask();
			if(mask != null && mask.count() > 0) {
				Integer wMax = (Integer)mask.valueForKeyPath("@max.criterion");
				if(wMax.intValue() > max)
					max = wMax.intValue();
			}
		}
		return max;
	}
}
