// EduPlan.java: Class file for WO Component 'EduPlan'

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

package net.rujel.eduplan;

import java.util.logging.Logger;

import net.rujel.reusables.SessionedEditingContext;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

// Generated by the WOLips Templateengine Plug-in at Jul 21, 2009 3:24:54 PM
public class EduPlan extends com.webobjects.appserver.WOComponent {
	
	public static Logger logger = Logger.getLogger("eduplan");

	public NSArray tablist;
	public NSKeyValueCoding currTab;
	public EOEditingContext ec;
	public WOComponent toReset;
		
    public EduPlan(WOContext context) {
        super(context);
        ec = new SessionedEditingContext(context.session());
        tablist = (NSArray)session().valueForKeyPath("modules.planTabs");
        currTab = (NSKeyValueCoding)tablist.objectAtIndex(0);
    }
    
    public void revertEc() {
		ec.lock();
		try {
			if(toReset != null) {
				toReset.reset();
				toReset = null;
			}
			if(ec.hasChanges())
				ec.revert();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ec.unlock();
		}
    }
}