// CoursePage.java: Class file for WO Component 'CoursePage'

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

package net.rujel.complete;

import java.util.Enumeration;

import net.rujel.base.SchoolSection;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.EduCycle;
import net.rujel.reusables.Various;
import net.rujel.reusables.FileWriterUtil;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;

// Generated by the WOLips Templateengine Plug-in at Jun 25, 2009 7:21:34 PM
public class CoursePage extends com.webobjects.appserver.WOComponent {
	public EduCourse course;
	public NSArray reports;
	public NSKeyValueCodingAdditions item;
	public NSKeyValueCoding readyModules;
	public NSArray changes;
	
    public CoursePage(WOContext context) {
        super(context);
    }
    
    public String link() {
    	if(item == null)
    		return null;
    	StringBuilder buf = new StringBuilder();
    	buf.append('<');
    	String id = (String)item.valueForKey("id");
    	boolean ready = true;
    	if(readyModules != null)
    		ready = Various.boolForObject(readyModules.valueForKey(id));
    	if(ready)
    		buf.append("a href = \"").append(id).append(".html\"");
    	else
    		buf.append("span");
    	id = (String)item.valueForKey("hover");
    	if(id != null)
    		buf.append(" title = \"").append(id).append('"');
    	buf.append('>');
    	buf.append(item.valueForKey("title"));
    	if(ready)
    		buf.append("</a>");
    	else
    		buf.append("</span>");
    	return buf.toString();
    }
    
    public static NSMutableDictionary readyModules(EduCourse course, NSArray modules) {
    	NSMutableDictionary result = new NSMutableDictionary();
    	EOEditingContext ec = course.editingContext();
    	EOQualifier qual[] = new EOQualifier[2];
    	qual[0] = new EOKeyValueQualifier("course", EOQualifier.QualifierOperatorEqual,course);
    	qual[1] = new EOKeyValueQualifier(Completion.ASPECT_KEY
    			, EOQualifier.QualifierOperatorEqual,"student");
    	qual[1] = new EOAndQualifier(new NSArray(qual));
    	EOFetchSpecification fs = new EOFetchSpecification(Completion.ENTITY_NAME,qual[1],null);
    	NSArray found = ec.objectsWithFetchSpecification(fs);
    	if(found == null || found.count() == 0) {
    		result.takeValueForKey(Boolean.FALSE,"student");
    		result.takeValueForKey(new Integer(0),"studentsClosed");
    	} else {
    		int closed = 0;
    		Enumeration enu = found.objectEnumerator();
    		while (enu.hasMoreElements()) {
				Completion cpt = (Completion) enu.nextElement();
				if(cpt.closeDate() != null) {
					closed++;
				}
			}
    		if(closed < found.count()) {
    			result.takeValueForKey(Boolean.FALSE,"student");
    			result.takeValueForKey(new Integer(closed),"studentsClosed");
    			if(found.count() > 1)
    				result.takeValueForKey(new Integer(found.count()),"studentsTotal");
    		} else {
    			result.takeValueForKey(Boolean.TRUE,"student");
    		}
    	}
    	qual[1] = new EOKeyValueQualifier(Completion.ASPECT_KEY
    			, EOQualifier.QualifierOperatorNotEqual,"student");
    	qual[1] = new EOAndQualifier(new NSArray(qual));
    	fs.setQualifier(qual[1]);
    	found = ec.objectsWithFetchSpecification(fs);
    	if(found == null || found.count() == 0)
    		return result;
    	Enumeration enu = found.objectEnumerator();
    	while (enu.hasMoreElements()) {
			Completion cpt = (Completion) enu.nextElement();
			result.takeValueForKey(Boolean.valueOf(cpt.closeDate() != null), cpt.aspect());
		}
    	accountDependencies(result, modules);
    	return result;
    }
    
    public static int accountDependencies(NSMutableDictionary result, NSArray modules) {
    	boolean changed = true;
    	int changes = -1;
    	while (changed) {
    		changed = false;
    		changes++;
    		Enumeration enu = modules.objectEnumerator();
    		while (enu.hasMoreElements()) {
    			NSKeyValueCoding mod = (NSKeyValueCoding) enu.nextElement();
    			String id = (String)mod.valueForKey("id");
    			if(result.containsKey(id)) {
    				if(result.valueForKey(id) == Boolean.FALSE) {
    					NSArray precedes = (NSArray)mod.valueForKey("precedes");
    					if(precedes == null || precedes.count() == 0)
    						continue;
    					for (int i = 0; i < precedes.count(); i++) {
    						String oth = (String)precedes.objectAtIndex(i);
    						if(result.valueForKey(oth) != Boolean.FALSE) {
    							result.takeValueForKey(Boolean.FALSE, oth);
    		    				changed = true;
    						}
    					}
    					continue;
    				}
    			} else if(Various.boolForObject(mod.valueForKey("manual"))) {
    				result.takeValueForKey(Boolean.FALSE, id);
    				changed = true;
    				continue;
    			} else {
    				result.takeValueForKey(Boolean.TRUE, id);
    				changed = true;
    			}
    			NSArray requires = (NSArray)mod.valueForKey("requires");
    			if(requires != null) {
    				for (int i = 0; i < requires.count(); i++) {
    					String oth = (String)requires.objectAtIndex(i);
    					if (result.valueForKey(oth) == Boolean.FALSE) {
    						changed = true;
    	    				result.takeValueForKey(Boolean.FALSE, id);
    	    				break;
    					}
    				}
    			}
    		}
		}
    	return changes;
    }
    
    public static void printCourseReports(EduCourse course, FileWriterUtil exec, String cDir,
    		NSArray reports, NSKeyValueCoding ready) {
    	if(cDir == null) {
			EOKeyGlobalID gid = (EOKeyGlobalID)course.editingContext().globalIDForObject(course);
			String key = gid.keyValues()[0].toString();
			cDir = key + '/';
    	}
    	boolean sections = EduCycle.entityName.equals("PlanCycle") && Various.boolForObject(
    			exec.ctx.session().valueForKeyPath("sections.hasSections"));
    	if(sections) {
    		SchoolSection section = (SchoolSection)course.valueForKeyPath("cycle.section");
    		StringBuilder buf = new StringBuilder(4);
    		buf.append('S').append(section.sectionID());
    		exec.enterDir(buf.toString(), false);
    	}
    	exec.enterDir(cDir,false);
		if(reports == null)
			reports = (NSArray)exec.ctx.session().valueForKeyPath("modules.courseComplete");
    	WOComponent page = WOApplication.application().pageWithName("CoursePage", exec.ctx);
		page.takeValueForKey(course, "course");
		page.takeValueForKey(ready, "readyModules");
		page.takeValueForKey(reports, "reports");
		exec.writeFile("index.html", page);
    	if(reports == null || reports.count() == 0)
    		return;
    	Enumeration enu = reports.objectEnumerator();
    	while (enu.hasMoreElements()) {
    		System.gc();
			NSKeyValueCoding rep = (NSKeyValueCoding) enu.nextElement();
			String id = (String)rep.valueForKey("id");
//			File file = new File(cDir,id + ".html");
			if(ready != null && ready.valueForKey(id) == Boolean.FALSE) {
//				if(file.exists())
//					file.delete();
				continue;
			}
			String name = (String)rep.valueForKey("component");
			page = WOApplication.application().pageWithName(name,exec.ctx);
			page.takeValueForKey(course,"course");
			exec.writeFile(id + ".html", page);
//			exec.writeData(id + ".html", com.webobjects.foundation.NSData.EmptyData);
		}
    	exec.leaveDir();
    	if(sections)
    		exec.leaveDir();
    }
    
    public String students() {
    	if(readyModules == null || Various.boolForObject(readyModules.valueForKey("student")))
    		return null;
    	StringBuilder buf = new StringBuilder(
    			"<span style = \"margin-left:2em;color:#000000;background-color:#ffff99;\">");
    	buf.append(session().valueForKeyPath("strings.RujelComplete_Complete.StudentCatalog"));
    	buf.append(':').append(' ').append(readyModules.valueForKey("studentsClosed"));
    	Object total = readyModules.valueForKey("studentsTotal");
    	if(total != null)
    		buf.append(" / ").append(total);
    	buf.append("</span>");
    	return buf.toString();
    }
    
    public void setCourse(EduCourse aCourse) {
    	course = aCourse;
    	/*
		try {
			changes = (NSArray)course.valueForKey("teacherChanges");
			if(changes.count() == 0) {
				changes = null;
				return;
			}
			changes = EOSortOrdering.sortedArrayUsingKeyOrderArray(changes, MyUtility.dateSorter);
		} catch (NSKeyValueCoding.UnknownKeyException e) {
			changes = null;
		}*/
    }
}