package net.rujel.complete;

import net.rujel.interfaces.EduGroup;
import net.rujel.interfaces.Student;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;

// Generated by the WOLips Templateengine Plug-in at Jun 17, 2009 8:06:10 PM
public class StudentCatalog extends com.webobjects.appserver.WOComponent {
	
	public NSArray eduGroups;
	public EduGroup group;
	public Object groupID;
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
    	groupID = gid.keyValues()[0];
    }
    
    public String studentLink() {
    	if(student == null)
    		return null;
       	EOKeyGlobalID gid = (EOKeyGlobalID) ec.globalIDForObject(student); 
       	StringBuilder result = new StringBuilder(30);
       	result.append(group.grade()).append('_');
       	result.append(groupID).append('/');
       	result.append(gid.keyValues()[0]).append("/index.html");
       	return result.toString();
    }
    
    public String onclick() {
    	return "toggleObj('" + groupID + "');";
    }
}