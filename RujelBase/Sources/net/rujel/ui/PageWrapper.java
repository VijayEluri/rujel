// PageWrapper.java: Class file for WO Component 'PageWrapper'

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

import net.rujel.base.MyUtility;
import net.rujel.reusables.Various;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class PageWrapper extends WOComponent {
    public WOComponent pathItem;

    public PageWrapper(WOContext context) {
        super(context);
    }
    
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendToResponse(aResponse, aContext);
		if(aContext.hasSession())
			aContext.session().takeValueForKey(null, "message");
	}
	
	public String eduYear() {
		Integer year = (Integer)session().valueForKey("eduYear");
		return MyUtility.presentEduYear(year);
	}
	
	public WOComponent chooseRegime() {
    	WOComponent nextPage = (WOComponent)session().objectForKey("ChooseRegime");
    	if (nextPage == null) {
    		nextPage = pageWithName("ChooseRegime");
    		session().setObjectForKey(nextPage, "ChooseRegime");
    	} else {
    		nextPage.ensureAwakeInContext(context());
    	}
    	nextPage.takeValueForKey(context().page(), "returnPage");
    	return nextPage;
    }
	
    public WOComponent goTo() {
        NSMutableArray list = (NSMutableArray)session().valueForKey("pathStack");
		int idx = list.indexOfIdenticalObject(pathItem);
		pathItem.ensureAwakeInContext(context());
		if(idx > 0) {
			NSRange pastComponents = new NSRange(idx,list.count() - idx);
			list.removeObjectsInRange(pastComponents);
		} else {
			list.removeAllObjects();
		}
		session().takeValueForKey(Boolean.FALSE,"prolong");
		return pathItem;
    }
	/*
    public WOActionResults exit() {
		session().terminate();
		WORedirect result = new WORedirect(context());
		String url = context().directActionURLForActionNamed("", null);
		result.setUrl(url);
		return result; 
    }
	*/
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public String onLoad() {
		String required = "countdown();";
		String onLoad = (String)valueForBinding("onLoad");
		if(onLoad == null)
			return required;
		if(onLoad.endsWith(";"))
			return onLoad + required;
		else
			return onLoad + ';' + required;
	}
	
	public String timeoutStyle() {
		if(application().isRefusingNewSessions()) {
			return "color:#ff6600;";
		} else {
			return "color:#cccccc;";
		}
	}
	
	public String logout() {
		String result = context().directActionURLForActionNamed("logout", null);
		result = Various.cleanURL(result);
		result = "return checkRun('" + result + "');";
		return result;
	}
}
