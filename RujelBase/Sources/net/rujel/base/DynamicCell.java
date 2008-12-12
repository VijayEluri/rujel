// DynamicCell.java: Class file for WO Component 'DynamicCell'

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

package net.rujel.base;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class DynamicCell extends WOComponent {

    public DynamicCell(WOContext context) {
        super(context);
    }
	
	protected NSKeyValueCoding _currAddOn;
	public NSKeyValueCoding currAddOn() {
		if(_currAddOn == null) {
			_currAddOn = (NSKeyValueCoding)valueForBinding("currAddOn");
		}
		return _currAddOn;
	}
	
	public String elementName() {
		String elt = (String)valueForBinding("elementName");
		if(elt == null)
			elt = (String)currAddOn().valueForKey("elementName");
		if(elt == null)
			elt = "td";
		return elt;
	}
	
	protected Boolean _disabled;
	public Boolean disabled() {
		if(_disabled == null) {
			_disabled = new Boolean(hasBinding("selectAction") ||
					currAddOn().valueForKey("selectAction") != null);				
		}
		return _disabled;
	}
	
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		String action = (String)currAddOn().valueForKey("selectAction");
		if(action != null)
			return performParentAction(action);
		return (WOActionResults)valueForBinding("selectAction");
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public boolean isStateless() {
		return true;
	}
	
	public void reset() {
		_disabled = null;
		_currAddOn = null;
	}		
}
