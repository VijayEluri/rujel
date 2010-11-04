// ByCourseEditor.java: Class file for WO Component 'ByCourseEditor'

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

import java.util.Enumeration;
import java.util.logging.Logger;

import net.rujel.base.QualifiedSetting;
import net.rujel.base.SettingsBase;
import net.rujel.interfaces.EduCycle;
import net.rujel.interfaces.EduGroup;
import net.rujel.reusables.ModulesInitialiser;
import net.rujel.reusables.SettingsReader;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.WOActionResults;

// Generated by the WOLips Templateengine Plug-in at Jul 18, 2009 2:20:41 PM
public class ByCourseEditor extends com.webobjects.appserver.WOComponent {
	public static final Logger logger = Logger.getLogger("rujel.base");
	
	public WOComponent returnPage;
	public String pushToKeyPath;
	public NSKeyValueCodingAdditions resultGetter;
	
	public SettingsBase base;
	protected QualifiedSetting byCourse;
	public NSMutableSet forceEduYear = new NSMutableSet();
	
	public boolean advanced = false;
	protected NSMutableDictionary[] common;
	public NSMutableDictionary[] qItem;
	public NSMutableArray qList;
	public int index;
	public Integer crIdx;
	public NSMutableDictionary[] currQ;
	public Object item;
	
	public NSMutableArray editors;
	public NSKeyValueCoding currEditor;
	
	public NSMutableArray editList;
	public NSArray grades;
	public NSMutableDictionary tmpValues = new NSMutableDictionary();
	
	public EOEditingContext ec() {
		return base.editingContext();
	}
	
    public ByCourseEditor(WOContext context) {
        super(context);
		int maxGrade = SettingsReader.intForKeyPath("edu.maxGrade", 11);
		int minGrade = SettingsReader.intForKeyPath("edu.minGrade", 1);
		Integer[] grds = new Integer[maxGrade - minGrade + 1];
		for (int i = 0; i < grds.length; i++) {
			grds[i] = new Integer(minGrade + i);
		}
		grades = new NSArray(grds);
		common = new NSMutableDictionary[4];
		qList = new NSMutableArray((Object)common);
    }
    
    public void setQualifier(EOQualifier qual) {
    	if(qual == null)
    		return;
    	NSArray list = null;
		for (int i = 0; i < common.length; i++) {
			common[i] = null;
		}
    	if(qual instanceof EOOrQualifier) {
    		list = ((EOOrQualifier)qual).qualifiers();
    	} else if (qual instanceof EOAndQualifier) { // prepare commons
    		Enumeration enu = ((EOAndQualifier)qual).qualifiers().objectEnumerator();
    		while (enu.hasMoreElements()) {
				EOQualifier q = (EOQualifier) enu.nextElement();
				if (q instanceof EOKeyValueQualifier) {
					common = ByCoursePresenter.identifyQualifier(
							(EOKeyValueQualifier)q, common, session());
					if(common == null) {
		    			advanced = true;
		    			return;
		    		}
				} else if (q instanceof EOOrQualifier) {
		    		if(list != null) {
		    			advanced = true;
		    			return;
		    		} else {
		    			list = ((EOOrQualifier)qual).qualifiers();
		    		}
				} else {
	    			advanced = true;
	    			return;
	    		}
			}
    	} else if(qual instanceof EOKeyValueQualifier) { // use single
			common = ByCoursePresenter.identifyQualifier((EOKeyValueQualifier)qual,
					common, session());
			ifForce((EOKeyValueQualifier)qual);
			advanced = (common == null);
    	}
    	if(list != null) { // prepare alternatives
    		Enumeration enu = list.objectEnumerator();
    		qList = new NSMutableArray();
    		while (enu.hasMoreElements()) {
				EOQualifier q = (EOQualifier) enu.nextElement();
	    		qItem = common.clone();
				if (q instanceof EOKeyValueQualifier) {
					qItem = ByCoursePresenter.identifyQualifier(
							(EOKeyValueQualifier)q, qItem, session());
					ifForce((EOKeyValueQualifier)q);
				} else if (q instanceof EOAndQualifier) {
					Enumeration enu2 = ((EOAndQualifier)q)
							.qualifiers().objectEnumerator();
					while (enu2.hasMoreElements()) {
						EOQualifier q2 = (EOQualifier) enu2.nextElement();
						if (q2 instanceof EOKeyValueQualifier) {
							qItem = ByCoursePresenter.identifyQualifier(
									(EOKeyValueQualifier)q2, qItem, session());
							if(qItem == null) {
				    			advanced = true;
				    			return;
				    		}
							ifForce((EOKeyValueQualifier)q2);
						} else {
			    			advanced = true;
			    			return;
						}
					}
				} else {
	    			advanced = true;
	    			return;
	    		}
				if(qItem == null) {
	    			advanced = true;
	    			return;
	    		}
				qList.addObject(qItem);
			} // process OR list
    	}
    }
    
    protected void ifForce(EOKeyValueQualifier qual) {
    	if(qual == null) return;
    	Object val = qual.value();
    	if(val instanceof EduGroup && ((EduGroup)val).eduYear() != null)
    		forceEduYear.addObject(qual);
    }
    
    protected NSMutableArray qualifiersFromArray(NSMutableDictionary[] matrix) {
    	NSMutableArray quals = new NSMutableArray();
    	for (int i = 0; i < matrix.length; i++) {
			if(matrix[i] == null)
				continue;
			if(i == 0) {
				NSArray omit = null;
				if(matrix != common && common[i] != null) {
					omit = (NSArray)common[i].valueForKey("list");
					if(omit != null)
						omit = (NSArray)omit.valueForKey("qualifier");
				}
				NSMutableArray list = (NSMutableArray)matrix[i].valueForKey("list");
				if(list == null)
					continue;
				list = (NSMutableArray)list.valueForKey("qualifier");
				if(list != null) {
					if(omit != null)
						list.removeObjectsInArray(omit);
					quals.addObjectsFromArray(list);
				}
			} else {
				if(matrix != common && common[i] != null)
					continue;
				EOQualifier qual = (EOQualifier)matrix[i].valueForKey("qualifier");
				if(qual != null && !quals.containsObject(qual))
					quals.addObject(qual);
				qual = (EOQualifier)matrix[i].valueForKey("qualifierLow");
				if(qual != null && !quals.containsObject(qual))
					quals.addObject(qual);
				qual = (EOQualifier)matrix[i].valueForKey("qualifierHigh");
				if(qual != null && !quals.containsObject(qual))
					quals.addObject(qual);
			}
		}
    	return quals;
    }
    
    protected static EOQualifier qualFromList(NSArray quals) {
    	if(quals == null)
    		return null;
    	switch (quals.count()) {
			case 0:
				return null;
			case 1:
				return (EOQualifier)quals.objectAtIndex(0);
			default:
				return new EOAndQualifier(quals);
    	}
    }
    
    public EOQualifier makeQualifier() {
    	NSMutableArray quals = qualifiersFromArray(common);
    	if(qList == null || qList.count() <= 1)
    		return qualFromList(quals);
    	NSMutableArray orList = new NSMutableArray();
    	Enumeration enu = qList.objectEnumerator();
    	while (enu.hasMoreElements()) {
			NSMutableDictionary[] matrix = (NSMutableDictionary[]) enu.nextElement();
			EOQualifier qual = qualFromList(qualifiersFromArray(matrix));
			if(qual != null && !orList.containsObject(qual))
				orList.addObject(qual);
		}
    	EOQualifier qual = null;
    	switch (orList.count()) {
		case 0:
			break;
		case 1:
			qual = (EOQualifier)orList.objectAtIndex(0);
			break;
		default:
			qual = new EOOrQualifier(orList);
			break;
		}
    	if(quals.count() > 0 && qual != null) {
    		quals.addObject(qual);
    		qual = new EOAndQualifier(quals);
    	}
    	return qual;
    }
    
    public NSMutableDictionary dict() {
    	if(qItem == null || index < 0)
    		return null;
    	return qItem[index];
    }
    
    public String cellClass() {
    	if(qItem == currQ && crIdx != null &&crIdx.intValue() == index)
    		return "selection";
    	return (String)valueForKeyPath("dict.styleClass");
    }
    
    public WOActionResults selectCell() {
    	if(qItem == currQ && crIdx != null && crIdx.intValue() == index) {
    		crIdx = null;
    		currQ = null;
    		currEditor = null;
        	return null;
    	}
    	currQ = qItem;
    	if(crIdx != null && crIdx.intValue() == index) {
    		pickEditor();
    		return null;
    	}
    	crIdx = new Integer(index);
    	if(editors == null)
    		editors = new NSMutableArray();
    	else
    		editors.removeAllObjects();
    	NSKeyValueCodingAdditions strings = (NSKeyValueCodingAdditions)
    		session().valueForKeyPath("strings.RujelBase_Base.SettingsBase");
    	switch (index) {
		case ByCoursePresenter.OTHER:
			editors.addObjectsFromArray((NSArray)session().valueForKeyPath(
					"modules.settingEditorsOTHER"));
			break;
		case ByCoursePresenter.GROUP:
			editors.addObject(strings.valueForKey("grade"));
			editors.addObject(strings.valueForKey("eduGroup"));
			editors.addObject(strings.valueForKey("cycle"));
			editors.addObjectsFromArray((NSArray)session().valueForKeyPath(
					"modules.settingEditorsGROUP"));
			break;
		case ByCoursePresenter.SUBJECT:
			editors.addObject(strings.valueForKey("subject"));
			editors.addObject(strings.valueForKey("cycle"));
			editors.addObjectsFromArray((NSArray)session().valueForKeyPath(
					"modules.settingEditorsSUBJECT"));
			break;
		case ByCoursePresenter.TEACHER:
			editors.addObject(strings.valueForKey("teacher"));
			editors.addObjectsFromArray((NSArray)session().valueForKeyPath(
					"modules.settingEditorsTEACHER"));
			break;
		default:
			throw new IllegalStateException("Unknown index");
		}
    	pickEditor();
    	return null;
    }
    
    private void pickEditor() {
		currEditor = null;
    	if(editors == null || currDict() == null) {
    		return;
    	}
    	Enumeration enu = editors.objectEnumerator();
    	String name = (String)currQ[crIdx.intValue()].valueForKey("editor");
    	if(name == null)
    		return;
    	while (enu.hasMoreElements()) {
			NSKeyValueCoding ed = (NSKeyValueCoding) enu.nextElement();
			if(name.equals(ed.valueForKey("editor"))) {
				currEditor = ed;
				break;
			}
		}
    	editorPreload();
    }
    
    public WOActionResults selectEditor() {
    	if(item == null) {
    		setCurrDict(null);
    		return null;
    	}
		currEditor = (NSKeyValueCoding)item;
		editorPreload();
    	return null;
    }
    
    private void editorPreload() {
    	if(currEditor == null)
    		return;
    	if("grade".equals(currEditor.valueForKey("editor"))) { 
    		if("grade".equals(valueForKeyPath("currDict.editor"))) {
    			tmpValues.takeValueForKey(currDict().valueForKey("value"),"value");
    			tmpValues.takeValueForKey(currDict().valueForKey("min"),"min");
    			tmpValues.takeValueForKey(currDict().valueForKey("max"),"max");
    		} else {
    			try {
    				tmpValues.takeValueForKey(valueForKeyPath(
    						"currDict.qualifier.value.grade"),"value");
        			tmpValues.takeValueForKey(null,"min");
        			tmpValues.takeValueForKey(null,"max");
    			} catch (Exception e) {
					tmpValues.takeValueForKey(null, "grade");
				}
    		}
    	} else if("cycle".equals(currEditor.valueForKey("editor"))) {
    		EduCycle curr = (EduCycle)value();
    		if(curr != null) {
        		tmpValues.takeValueForKey(curr.grade(), "currGrade");
    		} else if(currQ[1] != null) {
    			Object val = currQ[1].valueForKey("value");
    			if(val instanceof Integer)
    				tmpValues.takeValueForKey(val, "currGrade");	
    		}   			
    		cyclesForGrade();
    	}
    }
    
    public NSMutableDictionary currDict() {
    	if(currQ == null || crIdx == null)
    		return null;
    	return currQ[crIdx.intValue()];
    }
    
    public void setCurrDict(NSMutableDictionary dict) {
    	currQ[crIdx.intValue()] = dict;
    	doneEditing();
    }
    
    public Object value() {
    	NSMutableDictionary dict = currDict();
    	if(dict == null || currEditor == null ||
    			!currEditor.valueForKey("editor").equals(dict.valueForKey("editor")))
    		return null;
    	EOKeyValueQualifier q = (EOKeyValueQualifier)dict.valueForKey("qualifier");
    	return q.value();
    }
    
    public void setValue(Object value) {
    	if(currEditor == null || currQ == null || crIdx == null)
    		return;
    	NSMutableDictionary dict = currQ[crIdx.intValue()];
    	if(dict == null || currQ != common || !currEditor.valueForKey(
    			"editor").equals(dict.valueForKey("editor"))) {
    		//TODO: dict getting method
    		dict = ((NSDictionary)currEditor).mutableClone();
    		currQ[crIdx.intValue()] = dict;
    	}
    	String key = (String)dict.valueForKey("operator");
    	NSSelector sel = (key==null)?EOQualifier.QualifierOperatorEqual:
    		EOQualifier.operatorSelectorForString(key);
    	key = (String)dict.valueForKey("keyPath");
    	dict.takeValueForKey(new EOKeyValueQualifier(key,sel,value), "qualifier");
    	if(key.equals("cycle.subject")) {
			StringBuilder buf = new StringBuilder();
			buf.append('"').append(value).append('"');
			value = buf.toString();
    	} else {
    		key = (String)dict.valueForKey("presentPath");
    		if(key != null)
    			value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, key);
    	}
    	dict.takeValueForKey(value,"value");
    }
    
    public String editorClass() {
    	if(currEditor == null) {
    		if(item == null)
    			return "selection";
    		else
    			return "grey";
    	} else {
    		if (currEditor == item)
    			return "selection";
    		else
    			return "backfield2"; 
    	}
    }
    
    public WOActionResults doneEditing() {
    	currEditor = null;
    	currQ = null;
    	crIdx = null;
    	return null;
    }
    
    public WOActionResults selectGrade() {
    	Integer value = (Integer)tmpValues.valueForKey("value");
    	NSMutableDictionary dict = currDict();
    	if(dict == null || !"grade".equals(dict.valueForKeyPath("editor")) 
    			|| (currQ != common)) {
    		dict = ((NSDictionary)currEditor).mutableClone();
    		currQ[crIdx.intValue()] = dict;
    	}
		dict.takeValueForKey(value, "value");
    	if(value != null) {
    		EOQualifier qual = new EOKeyValueQualifier("cycle.grade",
    				EOQualifier.QualifierOperatorEqual, value);
    		dict.takeValueForKey(qual, "qualifier");
    		dict.takeValueForKey(value, "value");
    		dict.takeValueForKey(null, "max");
    		dict.takeValueForKey(null, "qualifierLow");
    		dict.takeValueForKey(null, "min");
    		dict.takeValueForKey(null, "qualifierHigh");
    	} else {
    		dict.takeValueForKey(null, "qualifier");
    		value = (Integer)tmpValues.valueForKey("min");
    		boolean none = (value == null);
    		dict.takeValueForKey(value, "min");
    		StringBuilder buf = new StringBuilder(8);
    		if(!none) {
        		EOQualifier qual = new EOKeyValueQualifier("cycle.grade",
        				EOQualifier.QualifierOperatorGreaterThanOrEqualTo, value);
        		dict.takeValueForKey(qual, "qualifierLow");
        		buf.append(value);
    		} else {
        		dict.takeValueForKey(null, "qualifierLow");
    		}
    		value = (Integer)tmpValues.valueForKey("max");
    		dict.takeValueForKey(value, "max");
    		if(value != null) {
    			none = false;
        		EOQualifier qual = new EOKeyValueQualifier("cycle.grade",
        				EOQualifier.QualifierOperatorLessThanOrEqualTo, value);
        		dict.takeValueForKey(qual, "qualifierHigh");
        		if(buf.length() == 0)
        			buf.append("&le;");
        		else
        			buf.append("...");
        		buf.append(value);
    		} else {
        		dict.takeValueForKey(null, "qualifierHigh");
        		if(buf.length() > 0)
        			buf.insert(0, "&ge;");
    		}
    		if(buf.length() > 0)
    			dict.takeValueForKey(buf.toString(), "value");
    		else
    			currQ[crIdx.intValue()] = null;
    	}
    	return doneEditing();
    }
    
    public void setPushToKeyPath(String path) {
    	if(resultGetter instanceof WOComponent) {
    		WOComponent getter = (WOComponent)resultGetter;
    		while (path != null && path.charAt(0) == '^') {
    			path = path.substring(1);
				path = (String)getter.valueForBinding(path);
				getter = getter.parent();
			}
    		resultGetter = getter;
    	}
    	pushToKeyPath = path;
    }
    
    public void setByCourse(QualifiedSetting set) {
    	byCourse = set;
    	if(set == null)
    		return;
    	if(base == null)
    		base = (SettingsBase)byCourse.valueForKey("settingsBase");
    	if(byCourse != null)
    		setQualifier((EOQualifier)byCourse.valueForKey("qualifier"));
    	tmpValues.takeValueForKey(set.eduYear(), QualifiedSetting.EDU_YEAR_KEY);
    }
    
    public String onclick() {
		String href = context().componentActionURL();
		String result = "ajaxPopupAction('" + href + "');";
		return result;
    }
    
    public WOActionResults save() {
    	returnPage.ensureAwakeInContext(context());
    	if(base == null)
    		return returnPage;
//		NSComparator comparator = new SettingsBase.Comparator();
    	EOQualifier qual = makeQualifier();
    	if(qual == null)
    		return returnPage;
    	Integer num = (byCourse == null)?null:byCourse.sort();
    	if(!base.isSingle()) {
    		Enumeration enu = base.qualifiedSettings().objectEnumerator();
    		Integer eduYear = (Integer)session().valueForKey("eduYear");
    		int numb = 0;
    		while (enu.hasMoreElements()) {
    			QualifiedSetting bc = (QualifiedSetting) enu.nextElement();
    			if(bc == byCourse)
    				continue;
    			if(bc.valueForKey("eduYear") != null 
    					&& !eduYear.equals(bc.valueForKey("eduYear")))
    				continue;
    			if(qual.equals(bc.getQualifier())) {
    				session().takeValueForKey(application().valueForKeyPath(
    						"strings.RujelBase_Base.SettingsBase.duplicateByCourse"), "message");
    				return returnPage;
    			}
    			if(num == null && numb < bc.sort().intValue())
    				numb = bc.sort().intValue();
    		}
    		if(num == null)
    			num = new Integer(numb +1);
    	} // search for same
    	EOEditingContext ec = base.editingContext();
    	ec.lock();
    	try {
    		if(byCourse == null) {
				byCourse = (QualifiedSetting)EOUtilities.createAndInsertInstance(
						ec,QualifiedSetting.ENTITY_NAME);
				byCourse.takeValueForKey(tmpValues.valueForKey(
						SettingsBase.NUMERIC_VALUE_KEY), SettingsBase.NUMERIC_VALUE_KEY);
				byCourse.takeValueForKey(tmpValues.valueForKey(
						SettingsBase.TEXT_VALUE_KEY), SettingsBase.TEXT_VALUE_KEY);
				byCourse.addObjectToBothSidesOfRelationshipWithKey(base, "settingsBase");
			} // create from dict
    		byCourse.setQualifier(qual);
    		if(num == null)
    			num = new Integer(1);
    		byCourse.setSort(num);
			byCourse.takeValueForKey(tmpValues.valueForKey(
					QualifiedSetting.EDU_YEAR_KEY), QualifiedSetting.EDU_YEAR_KEY);
			if(editList != null) {
				if(editList.containsObject(byCourse)) {
					EOSortOrdering.sortArrayUsingKeyOrderArray(editList,ModulesInitialiser.sorter);
				} else {
					/*boolean done = false;
					for (int i = 0; i < editList.count(); i++) {
						QualifiedSetting qs = (QualifiedSetting)editList.objectAtIndex(i);
						if(qs.compare(byCourse) < 0) {
							editList.insertObjectAtIndex(byCourse, i);
							done = true;
							break;
						}
					}
					if(!done)*/
						editList.addObject(byCourse);
				} // insert into list
			}
			if(pushToKeyPath != null) {
				if(resultGetter == null)
					resultGetter = returnPage;
				resultGetter.takeValueForKeyPath(byCourse, pushToKeyPath);
			}
			ec.saveChanges();
			logger.log(WOLogLevel.COREDATA_EDITING,"Edited SettingByCourse: " + base.key(),
					new Object[] {session(),byCourse});
    	} catch (Exception e) {
			logger.log(WOLogLevel.INFO,"Failed editing SettingByCourse: " + base.key(),
					new Object[] {session(),byCourse,e});
    		session().takeValueForKey(e.getMessage(), "message");
    		ec.revert();
    	} finally {
    		ec.unlock();
    	}
    	return returnPage;
    }

    public WOActionResults pickGrade() {
    	tmpValues.takeValueForKey(item, "currGrade");
    	cyclesForGrade();
    	return null;
    }
    
    protected void cyclesForGrade() {
    	Integer grade = (Integer)tmpValues.valueForKey("currGrade");
    	if(grade == null) {
    		tmpValues.removeObjectForKey("cycles");
    		return;
    	}
    	NSArray list = EduCycle.Lister.cyclesForGrade(grade, base.editingContext());
    	if(list == null || list.count() == 0) {
    		tmpValues.removeObjectForKey("cycles");
    		return;
    	}
    	Enumeration enu = list.objectEnumerator();
    	NSMutableArray resut = new NSMutableArray();
    	Object currCycle = valueForKeyPath("currDict.qualifier.value");
    	Boolean hasExtra = Boolean.FALSE;
    	while (enu.hasMoreElements()) {
			EduCycle cycle = (EduCycle) enu.nextElement();
			NSMutableDictionary row = new NSMutableDictionary(cycle,"cycle");
			row.takeValueForKey(cycle.subject(), "subject");
			row.takeValueForKey((cycle == currCycle)?"selection":"highlight", "styleClass");
			if(hasExtra != null) {
				try {
					Object extra = cycle.valueForKey("extraInfo");
					if(extra != null) {
						hasExtra = Boolean.TRUE;
						row.takeValueForKey(extra, "extraInfo");
					}
				} catch (NSKeyValueCoding.UnknownKeyException e) {
					hasExtra = null;
				}
			}
			resut.addObject(row);
		}
    	tmpValues.takeValueForKey(hasExtra, "hasExtra");
    	tmpValues.takeValueForKey(resut, "cycles");
    }
    
	public WOActionResults selectCycle() {
    	EduCycle value = (EduCycle)NSKeyValueCoding.Utility.valueForKey(item,"cycle");
    	NSMutableDictionary dict = ((NSDictionary)currEditor).mutableClone();
		dict.takeValueForKey(value.subject(), "value");
		dict.takeValueForKey(new EOKeyValueQualifier("cycle", 
				EOQualifier.QualifierOperatorEqual, value), "qualifier");
		currQ[2] = dict;
		if(crIdx.intValue() == 1 || currQ[1] == null || 
				((String)currQ[1].valueForKey("keyPath")).startsWith("cycle")) {
			dict = dict.mutableClone();
			dict.takeValueForKey(currEditor.valueForKey("styleGrade"),"style");
			dict.takeValueForKey("grade","presentPath");
			dict.takeValueForKey(value.grade(),"value");
			currQ[1] = dict;
		}
		/* //TODO: clean Other qualifiers
		for (int i = 0; i < currQ.length; i++) {
			if(i == 2 || currQ[i] == null)
				continue;
			String path = (String)currQ[i].valueForKey("keyPath");
			if(path.startsWith("cycle")) {
				if(i == 1) {
					currQ[i] = dict.mutableClone();
					currQ[i].takeValueForKey(
							currEditor.valueForKey("styleGrade"),"style");
					currQ[i].takeValueForKey("grade","presentPath");
					currQ[i].takeValueForKey(value.grade(),"value");
				} else
					currQ[i] = null;
			}
		}*/
		return doneEditing();
	}
}