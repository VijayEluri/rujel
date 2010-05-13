package net.rujel.complete;

import java.io.File;
import java.util.Enumeration;

import net.rujel.base.MyUtility;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.Person;
import net.rujel.reusables.SettingsReader;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

// Generated by the WOLips Templateengine Plug-in at Jun 22, 2009 1:21:03 PM
public class CoursesCatalog extends com.webobjects.appserver.WOComponent {
	protected static final EOSortOrdering[] orders = new EOSortOrdering[] {
		new EOSortOrdering("eduGroup",EOSortOrdering.CompareAscending),
		new EOSortOrdering("cycle",EOSortOrdering.CompareAscending),
		new EOSortOrdering("teacher",EOSortOrdering.CompareAscending)
	};
	public static final NSArray byClass = new NSArray(orders);
	public static final NSArray bySubject = new NSArray(
			new EOSortOrdering[] {orders[1],orders[0],orders[2]});
	public static final NSArray byTeacher = new NSArray(
			new EOSortOrdering[] {orders[2],orders[0],orders[1]});
	
	public EOEditingContext ec;
	public NSArray allCourses;
	public String grouping;
	protected Object currGrouping;
	public EduCourse item;
	public Integer courseID;
	public String grHead;
	
    public CoursesCatalog(WOContext context) {
        super(context);
    }
    
    public NSArray allCourses() {
    	if(allCourses != null)
    		return allCourses;
    	allCourses = EOUtilities.objectsMatchingKeyAndValue(ec,
    			EduCourse.entityName, "eduYear", session().valueForKey("eduYear"));
    	if(allCourses != null && allCourses.count() > 1)
    		sort();
     	return allCourses;
    }
    
    protected void sort() {
  		if(grouping.equals("eduGroup"))
			allCourses = EOSortOrdering.sortedArrayUsingKeyOrderArray(allCourses, byClass);
		else if(grouping.equals("cycle"))
			allCourses = EOSortOrdering.sortedArrayUsingKeyOrderArray(allCourses, bySubject);
		else if(grouping.equals("teacher"))
			allCourses = EOSortOrdering.sortedArrayUsingKeyOrderArray(allCourses, byTeacher);
    }
    
    public void setGrouping(String newGrouping) {
    	grouping = newGrouping;
    	if(allCourses != null && allCourses.count() > 1)
    		sort();
    }
    
    public void setItem(EduCourse course) {
    	grHead = null;
    	if(course == null) {
    		courseID = null;
    		item = null;
    		return;
    	}
    	EOKeyGlobalID gid = (EOKeyGlobalID)ec.globalIDForObject(course);
    	courseID = (Integer)gid.keyValues()[0];
		if(item != null && grouping.equals("cycle")){
			if(item.cycle().subject().equals(course.cycle().subject())) {
				item = course;
				return;
			}
		}
    	if(item == null || item.valueForKey(grouping) != course.valueForKey(grouping)) {
    		StringBuilder buf = new StringBuilder("</div>");
    		buf.append("\n<div class=\"gr\"");
    		buf.append(" onmouseover = \"dim(this);\" onmouseout = \"unDim(this);\"");
    		buf.append(" onclick = \"toggleObj('gr").append(courseID).append("');\">");
    		if(grouping.equals("eduGroup"))
    			buf.append(course.eduGroup().name());
    		else if(grouping.equals("cycle"))
    			buf.append(course.cycle().subject());
    		else if(grouping.equals("teacher"))
    			buf.append(Person.Utility.fullName(course.teacher(), true, 2, 1, 1));
    		buf.append("</div>\n<div style=\"display:none;padding-left:1em;\" id=\"gr");
    		buf.append(courseID).append("\">\n");
    		grHead = buf.toString();
    	}
    	item = course;
    }
    
    public String link() {
    	if(courseID == null)
    		return null;
       	return courseID.toString() + "/index.html";
    }
    
    public String present() {
    	StringBuilder buf = new StringBuilder(20);
		buf.append("<span style=\"float:left;\">");
    	if(!grouping.equals("eduGroup"))
    		buf.append(item.eduGroup().name());
    	if(!grouping.equals("cycle")) {
    		if(grouping.equals("teacher"))
    			buf.append("</span>\n<span style=\"float:right;\">");
    		buf.append(item.subjectWithComment());
    	} else if(item.comment() != null) {
    			buf.append(" (").append(item.comment()).append(')');
    	}
    	if(!grouping.equals("teacher")) {
    		buf.append("</span>\t<span class=\"teach\">");
    		buf.append(Person.Utility.fullName(item.teacher(), true, 2, 1, 1));
    	}
    	buf.append("</span>\n");
    	return buf.toString();
    }
    
    public static void prepareCourses(File folder, WOContext ctx, boolean writeReports) {
    	EOEditingContext ec = ctx.session().defaultEditingContext();
		Integer year = (Integer) ctx.session().valueForKey("eduYear");
//		if(folder.getName().equals(year.toString())) {
		if(SettingsReader.stringForKeyPath("edu.coursesCompleteDir", null) == null)
			folder = new File(folder,"courses");
//		} else {
//			folder = new File(folder,year.toString());
//		}
		if(!folder.exists())
			folder.mkdirs();
		Executor.createIndex(folder, MyUtility.presentEduYear(year), "eduGroup.html");
		Executor.copyResource(folder,"scripts.js");
		Executor.copyResource(folder,"styles.css");

		WOComponent page = WOApplication.application().pageWithName("CoursesCatalog", ctx);
		page.takeValueForKey(ec, "ec");
		page.takeValueForKey("teacher", "grouping");
		Executor.writeFile(folder, "teacher.html", page,false);
		page.takeValueForKey("cycle", "grouping");
		Executor.writeFile(folder, "cycle.html", page,false);
		page.takeValueForKey("eduGroup", "grouping");
		Executor.writeFile(folder, "eduGroup.html", page,false);
		
		NSArray courses = (NSArray)page.valueForKey("allCourses");
		NSArray reports = (NSArray)ctx.session().valueForKeyPath("modules.courseComplete");
		Enumeration enu = courses.objectEnumerator();
		while (enu.hasMoreElements()) {
			EduCourse course = (EduCourse) enu.nextElement();
			EOKeyGlobalID gid = (EOKeyGlobalID)ec.globalIDForObject(course);
			File cDir = new File(folder,gid.keyValues()[0].toString());
			if(!cDir.exists())
				cDir.mkdir();
			page = WOApplication.application().pageWithName("CoursePage", ctx);
			page.takeValueForKey(course, "course");
			page.takeValueForKey(reports, "reports");
			Executor.writeFile(cDir, "index.html", page,false);
			if(writeReports)
				printCourseReports(course, cDir, ctx, reports, false);
		}
    }

    public static void printCourseReports(EduCourse course, File cDir, WOContext ctx,
    		NSArray reports, boolean overwrite) {
    	WOComponent page = WOApplication.application().pageWithName("PrintLessons", ctx);
    	page.takeValueForKey(course, "course");
		Executor.writeFile(cDir, "lessons.html", page,overwrite);
    	if(reports == null || reports.count() == 0)
    		return;
    	Enumeration enu = reports.objectEnumerator();
    	while (enu.hasMoreElements()) {
			NSKeyValueCoding rep = (NSKeyValueCoding) enu.nextElement();
			String name = (String)rep.valueForKey("component");
			page = WOApplication.application().pageWithName(name,ctx);
//			page.takeValueForKey(rep,"reporter");
			page.takeValueForKey(course,"course");
			name = rep.valueForKey("id") + ".html";
			Executor.writeFile(cDir, name, page,overwrite);
		}
    }
}