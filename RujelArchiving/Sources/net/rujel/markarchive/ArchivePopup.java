//ArchivePopup.java : Class file for WO Component 'ArchivePopup'

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

package net.rujel.markarchive;

import java.util.Enumeration;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.*;

// Generated by the WOLips Templateengine Plug-in at Nov 19, 2008 2:10:06 PM
public class ArchivePopup extends com.webobjects.appserver.WOComponent {
    public ArchivePopup(WOContext context) {
        super(context);
    }
    
    protected EOEnterpriseObject obj;
    protected NSMutableArray keys;
    public NSDictionary presentKeys;
    public NSArray archives;
    public NSMutableDictionary newValues;
    public boolean changeable;
    
    public MarkArchive archItem;
    public String keyItem;
    
    public String presenter;
    public Object initData;

	public NSArray keys() {
		return keys;
	}

	public void setKeys(NSArray keys) {
		this.keys = keys.mutableClone();
	}
	
	public void setObject(EOEnterpriseObject eo) {
		obj = eo;
		if(eo == null) {
			archives = null;
			return;
		}
		archives = MarkArchive.archivesForObject(eo);
		if(archives != null && archives.count() > 0) {
			Enumeration enu = archives.objectEnumerator();
			while (enu.hasMoreElements()) {
				MarkArchive ma = (MarkArchive) enu.nextElement();
				Enumeration maKeys = ma.getArchiveDictionary().keyEnumerator();
				while (maKeys.hasMoreElements()) {
					String key = (String) maKeys.nextElement();
					if(key.equals(MarkArchive.REASON_KEY))
						continue;
					if(!keys.containsObject(key))
						keys.addObject(key);
				}
			}
		} else if(keys == null) {
			keys = eo.attributeKeys().mutableClone();
		}
	}
	
	public NSMutableDictionary newValues() {
		if(newValues == null && changeable) {
			newValues = new NSMutableDictionary();
			Enumeration enu = keys.objectEnumerator();
			while (enu.hasMoreElements()) {
				String key = (String) enu.nextElement();
				newValues.takeValueForKey(obj.valueForKey(key), key);
			}
		}
		return newValues;
	}
	
	public String currKey() {
		if(presentKeys == null || keyItem == null)
			return keyItem;
		String value = (String)presentKeys.valueForKey(keyItem); 
		return (value == null)?keyItem:value;
	}
	
	public String currValue() {
		if(keyItem == null)
			return null;
		if(archItem != null)
			return archItem.getArchiveValueForKey(keyItem);
		if(newValues() != null)
			return (String)newValues().valueForKey(keyItem);
		return null;
	}
	
	public void setCurrValue(String value) {
		if(newValues == null)
			newValues = new NSMutableDictionary(value,keyItem);
		else
			newValues.takeValueForKey(value, keyItem);
	}
}