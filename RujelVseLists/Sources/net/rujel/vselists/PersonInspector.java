// PersonInspector.java: Class file for WO Component 'PersonInspector'

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

import java.util.Enumeration;

import net.rujel.interfaces.Person;
import net.rujel.interfaces.PersonLink;
import net.rujel.reusables.ModulesInitialiser;
import net.rujel.reusables.WOLogLevel;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.appserver.WOActionResults;

// Generated by the WOLips Templateengine Plug-in at Aug 28, 2009 8:05:59 PM
public class PersonInspector extends com.webobjects.appserver.WOComponent {
	public WOComponent returnPage;
	public Person person;
	public NSArray usages;
	public NSKeyValueCoding currUsage;
	public String code;
	public String resultPath;
	public Object resultGetter;
	protected PersonLink plink;
	
    public PersonInspector(WOContext context) {
        super(context);
    }
    
    public NSArray usages() {
    	if(usages == null) {
    		session().setObjectForKey(person, "PersonInspector");
    		usages = (NSArray)session().valueForKeyPath("modules.personInspector");
    		session().removeObjectForKey("PersonInspector");
    	}
    	return usages;
    }
    
    public void setEntity(String entity) {
    	Enumeration enu = usages().objectEnumerator();
    	while (enu.hasMoreElements()) {
			currUsage = (NSKeyValueCoding)enu.nextElement();
			if(entity.equals(currUsage.valueForKey("entity")))
				return;
		}
    	currUsage = null;
    }
    
    public void setPersonLink(PersonLink pl) {
    	plink = pl;
    	person = pl.person();
    	EOEditingContext ec = person.editingContext();
    	if(ec.globalIDForObject((EOEnterpriseObject)pl).isTemporary()) {
    		if(ec.globalIDForObject(person).isTemporary()) {
    			session().setObjectForKey(pl, "PersonInspector");
        		usages = (NSArray)session().valueForKeyPath("modules.personInspector");
        		session().removeObjectForKey("PersonInspector");
        		setEntity(person.entityName());
    		} else {
        		session().setObjectForKey(person, "PersonInspector");
        		usages = (NSArray)session().valueForKeyPath("modules.personInspector");
        		String entity = ((EOEnterpriseObject)pl).entityName();
            	Enumeration enu = usages.objectEnumerator();
            	while (enu.hasMoreElements()) {
        			currUsage = (NSKeyValueCoding)enu.nextElement();
        			if(entity.equals(currUsage.valueForKey("entity")))
        				break;
        			currUsage = null;
        		}
        		if(currUsage != null) {
        			Object usage = currUsage.valueForKey("usage");
        			if(usage instanceof NSMutableArray) {
        				((NSMutableArray)usage).addObject(pl);
        			} else if (usage instanceof NSArray) {
        				usage = ((NSArray)usage).arrayByAddingObject(pl);
        				currUsage.takeValueForKey(usage, "usage");
        			} else {
        				usage = new NSArray(new Object[] {usage,pl});
        				currUsage.takeValueForKey(usage, "usage");
        			}
        		} else {
            		session().setObjectForKey(pl, "PersonInspector");
            		NSArray newUsage = (NSArray)session().valueForKeyPath(
            				"modules.personInspector");
            		if(newUsage != null && newUsage.count() > 0) {
            			usages = usages.arrayByAddingObjectsFromArray(newUsage);
            			if(usages.count() > 1)
            				usages = EOSortOrdering.sortedArrayUsingKeyOrderArray(
            						usages, ModulesInitialiser.sorter);
            			currUsage = (NSKeyValueCoding)newUsage.objectAtIndex(0);
            		}
        		}            	
        		session().removeObjectForKey("PersonInspector");
    		}
    	} else {
        	setEntity(((EOEnterpriseObject)pl).entityName());
    	}
    	code = null;
    	if(currUsage != null) {
    		String codeAttribute = (String)currUsage.valueForKey("codeAttribute");
			if(codeAttribute != null) {
		    	if(ec.globalIDForObject((EOEnterpriseObject)pl).isTemporary()) {
		    		code = getRecentCode();
		    		NSKeyValueCoding.Utility.takeValueForKey(pl, code, codeAttribute);
		    	} else {
		    		String aCode = (String)NSKeyValueCoding.Utility.valueForKey(
		    				pl, codeAttribute);
		    		if(codeIsAbs(aCode)) {
		    			code = aCode;
		    		} else {
		    			code = formAbsCode(aCode, (String)currUsage.valueForKey("title"));
		    		}
		    		return;
		    	}
			}
    	}
    	code = getRecentCode();
    }
    
    protected String getRecentCode() {
		Enumeration enu = usages.objectEnumerator();
		while (enu.hasMoreElements()) {
			NSKeyValueCoding usg = (NSKeyValueCoding) enu.nextElement();
			String aCode = absCodeFromDict(usg);
			if(aCode != null)
				return aCode;
		}
		return null;
    }
    
    protected String absCodeFromDict(NSKeyValueCoding usg) {
		String codeAttribute = (String)usg.valueForKey("codeAttribute");
		if(codeAttribute == null)
			return null;;
		Object usage = usg.valueForKey("usage");
		if(usage == null)
			return null;
		String aCode = null;
		if(usage instanceof NSArray) {
			Enumeration uenu = ((NSArray)usage).objectEnumerator();
			while (uenu.hasMoreElements()) {
				Object u = uenu.nextElement();
				String nextCode = (String)NSKeyValueCoding.Utility.valueForKey(
						u, codeAttribute);
				if(nextCode != null)
					aCode = nextCode;
				if(aCode != null && uenu.hasMoreElements()) {
					try {
						if(((EOEnterpriseObject)u).valueForKey(VseStudent.LEAVE_KEY) == null)
							break;
					} catch (Exception e) {
						// nothing
					}
				}
			}
		} else {
			aCode = (String)NSKeyValueCoding.Utility.valueForKey(
				usage, codeAttribute);
		}
		if(aCode == null || codeIsAbs(aCode))
			return aCode;
		return formAbsCode(aCode, (String)usg.valueForKey("title"));
    }

	public WOActionResults save() {
		EOEditingContext ec = person.editingContext();
		ec.lock();
		String newCode = absCodeFromDict(currUsage);
		if(newCode != null) {
			setCode(newCode);
		} else {
			if(currUsage.valueForKey("usage") == person) {
				if((person.lastName() != null && person.lastName().length() > 1) 
						|| person.firstName() != null) {
					setCode(null);
				} else {
					person.setFirstName(null);
					person.setSecondName(null);
					person.setLastName("?");
				}
			} else {
				
			}
		}
		try {
			ec.saveChanges();
			ListsEditor.logger.log(WOLogLevel.UNOWNED_EDITING, "Saved changes with person",
					new Object[] {session(),person,currUsage.valueForKey("entity")});
		} catch (Exception e) {
			ListsEditor.logger.log(WOLogLevel.WARNING, "Error saving changes with person",
					new Object[] {session(),person,currUsage.valueForKey("entity"),e});
			session().takeValueForKey(e.getMessage(), "message");
			ec.revert();
		} finally {
			ec.unlock();
		}
		returnPage.ensureAwakeInContext(context());
		if(resultPath != null) {
			if(resultGetter == null)
				resultGetter = returnPage;
			if(plink == null)
				plink = person;
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(
					resultGetter, plink, resultPath);
		}
		return returnPage;
	}
	
	public WOActionResults cancel() {
		EOEditingContext ec = person.editingContext();
		ec.lock();
		try {
			ec.revert();
		} finally {
			ec.unlock();
		}
		returnPage.ensureAwakeInContext(context());
		return returnPage;
	}
	
	protected String formAbsCode(String newCode,String title) {
		if(newCode != null && newCode.length() > 0 &&
				!newCode.startsWith(title)) {
			StringBuilder buf = new StringBuilder(28);
			buf.append(title).append(':').append(newCode);
			newCode = buf.toString();
		}
		return newCode;
	}
	
	protected boolean codeIsAbs(String aCode) {
		if(aCode == null || aCode.length() < 2)
			return false;
		Enumeration enu = usages.objectEnumerator();
		while (enu.hasMoreElements()) {
			NSKeyValueCoding usg = (NSKeyValueCoding) enu.nextElement();
			String codeAttribute = (String)usg.valueForKey("codeAttribute");
			if(codeAttribute == null)
				continue;
			if(aCode.startsWith((String)usg.valueForKey("title")))
				return true;
		}
		return false;
	}
	
	public void setCode(String newCode) {
//		newCode = formCode(newCode,currUsage.valueForKey("title").toString());
		if((newCode != null) && newCode.equals(code)) {
			return;
		}
			
		Enumeration enu = usages.objectEnumerator();
		while (enu.hasMoreElements()) {
			NSKeyValueCoding usg = (NSKeyValueCoding) enu.nextElement();
			if(usg == currUsage)
				continue;
			String codeAttribute = (String)usg.valueForKey("codeAttribute");
			if(codeAttribute == null)
				continue;
			Object usage = usg.valueForKey("usage");
			if(usage == null)
				continue;
			if(usage instanceof NSArray) {
				Enumeration uenu = ((NSArray)usage).objectEnumerator();
				while (uenu.hasMoreElements()) {
					Object u = uenu.nextElement();
					setCodeForUsage(newCode, u, codeAttribute);
				}
			} else {
				setCodeForUsage(newCode, usage, codeAttribute);
			}
			NSKeyValueCoding.Utility.takeValueForKey(usage, newCode, codeAttribute);
		}
		code = newCode;
		if(code != null) {
			person.setFirstName(null);
			person.setSecondName(null);
			person.setLastName("?");
		}
	}
	
	protected void setCodeForUsage(String newCode, Object usage, String codeAttribute) {
		if(newCode == null) {
			NSKeyValueCoding.Utility.takeValueForKey(usage, newCode, codeAttribute);
			return;
		}
		String aCode = (String)NSKeyValueCoding.Utility.valueForKey(
				usage, codeAttribute);
		if(aCode == null || codeIsAbs(aCode))
			NSKeyValueCoding.Utility.takeValueForKey(usage, newCode, codeAttribute);
	}
}