// StudentCatalog.java: Class file for WO Component 'StudentCatalog'

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

import java.io.File;
import java.util.Enumeration;

import net.rujel.interfaces.*;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

// Generated by the WOLips Templateengine Plug-in at Jun 17, 2009 8:06:10 PM
public class StudentCatalog extends com.webobjects.appserver.WOComponent {
	
	public NSArray eduGroups;
	public EduGroup group;
	public String groupID;
	public Student student;
	public EOEditingContext ec;
	
    public StudentCatalog(WOContext context) {
        super(context);
    }
    
    public void setGroup(EduGroup newGroup) {
    	group = newGroup;
    	if(group == null) {
    		groupID = null;
    		return;
    	}
    	if(ec == null)
    		ec = group.editingContext();
    	EOKeyGlobalID gid = (EOKeyGlobalID) ec.globalIDForObject(group); 
    	groupID = "gr" + gid.keyValues()[0];
    }
    
    public String studentLink() {
    	if(student == null)
    		return null;
       	EOKeyGlobalID gid = (EOKeyGlobalID) ec.globalIDForObject(student); 
       	StringBuilder result = new StringBuilder(30);
       	result.append(group.grade()).append('_');
       	result.append(groupID.substring(2)).append('/');
       	result.append(gid.keyValues()[0]).append("/index.html");
       	return result.toString();
    }
    
    public String onclick() {
    	return "toggleObj('" + groupID + "');";
    }

    public NSArray list() {
    	return group.list();
 /*   	if(group == null)
    		return null;
    	NSArray list = group.fullList();
    	if(list == null || list.count() < 2)
    		return list;
    	return EOSortOrdering.sortedArrayUsingKeyOrderArray(list, Person.sorter);*/
    }
    
    public static void prepareStudents(File folder, WOContext ctx) {
    	Executor.prepareFolder(folder, ctx, "list.html");
    	EOEditingContext ec = ctx.session().defaultEditingContext();
		NSArray groups = EduGroup.Lister.listGroups(
				(NSTimestamp)ctx.session().valueForKey("today"), ec);
		WOComponent page = WOApplication.application().pageWithName("StudentCatalog", ctx);
		page.takeValueForKey(ec, "ec");
		page.takeValueForKey(groups, "eduGroups");
		
		Executor.writeFile(folder, "list.html", page,false);
		Enumeration grenu = groups.objectEnumerator();
		NSMutableArray reports = (NSMutableArray)ctx.session().valueForKeyPath(
				"modules.studentReporter");
		reports.insertObjectAtIndex(WOApplication.application().valueForKeyPath(
				"strings.Strings.Overview.defaultReporter"),0);
		Integer year = (Integer) ctx.session().valueForKey("eduYear");
		while (grenu.hasMoreElements()) {
			EduGroup gr = (EduGroup) grenu.nextElement();
			File grDir = new File(folder,groupDirName(gr));
			Enumeration stenu = gr.list().objectEnumerator();
			NSArray args = new NSArray(new Object[] {year, gr });
			NSArray existingCourses = EOUtilities.objectsWithQualifierFormat(ec,
					EduCourse.entityName,"eduYear = %d AND eduGroup = %@",args);
			while (stenu.hasMoreElements()) {
				Student student = (Student) stenu.nextElement();
		    	EOKeyGlobalID gid = (EOKeyGlobalID)student.editingContext().globalIDForObject(student);
				File stDir = new File(grDir,gid.keyValues()[0].toString());
				completeStudent(gr, student, reports, existingCourses, stDir, ctx, false);
			}
		}
	}

    public static String groupDirName(EduGroup gr) {
		EOKeyGlobalID gid = (EOKeyGlobalID)gr.editingContext().globalIDForObject(gr);
		StringBuilder filename = new StringBuilder(12);
		filename.append(gr.grade()).append('_');
		filename.append(gid.keyValues()[0]);
		return filename.toString();
//		return new File(folder,filename.toString());
    }
    /*
    public static void writeGroup(EduGroup gr,NSArray students, NSArray reports,
    		File folder,Integer year, WOContext ctx) {
    	EOEditingContext ec = gr.editingContext();
		if(students == null)
			students = gr.list();
		Enumeration stenu = students.objectEnumerator();
		NSArray args = new NSArray(new Object[] {year, gr });
		NSArray existingCourses = EOUtilities.objectsWithQualifierFormat(ec,
				EduCourse.entityName,"eduYear = %d AND eduGroup = %@",args);
		File grDir = groupDir(folder, gr);
		while (stenu.hasMoreElements()) {
			Student student = (Student) stenu.nextElement();
			completeStudent(gr, student, reports, existingCourses, grDir, ctx, false);
		}
    } */
    
    public static void completeStudent(EduGroup gr, Student student, NSArray reports,
    		NSArray existingCourses, File stDir, WOContext ctx, boolean overwrite) {
		if(!stDir.exists())
			stDir.mkdirs();
		WOComponent page = WOApplication.application().pageWithName("StudentPage", ctx);
		page.takeValueForKey(student, "student");
		page.takeValueForKey(gr, "eduGroup");
		page.takeValueForKey(reports, "reports");
		Executor.writeFile(stDir, "index.html", page, overwrite);
/*		reportsForStudent(reports, student, ctx, existingCourses, stDir, overwrite);
    }
    private static void reportsForStudent(NSArray reports, Student student, WOContext ctx,
    		NSArray existingCourses, File stDir, boolean overwrite) { */
		Enumeration repEnu = reports.objectEnumerator();
		NSArray array = new NSArray(student);
		while (repEnu.hasMoreElements()) {
			NSDictionary reporter = (NSDictionary) repEnu.nextElement();
			page = WOApplication.application().pageWithName("PrintReport",ctx);
			page.takeValueForKey(reporter,"reporter");
			page.takeValueForKey(existingCourses,"courses");
			page.takeValueForKey(array,"students");
			String filename = reporter.valueForKey("id") + ".html";
			Executor.writeFile(stDir, filename, page,overwrite);
		}
    }
}