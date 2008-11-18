// AddOnPresenter.java

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

import net.rujel.reusables.*;
import net.rujel.interfaces.*;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class AddOnPresenter extends WOComponent {

    public AddOnPresenter(WOContext context) {
        super(context);
    }
	
	
	protected NSKeyValueCoding _currAddOn;
	private EduCourse _course;
//	private EduPeriod _period;
	private Student _student;
	
	public NSKeyValueCoding currAddOn() {
		if(_currAddOn == null) {
			_currAddOn = (NSKeyValueCoding)valueForBinding("currAddOn");
		}
		return _currAddOn;
	}
	
	public NamedFlags access() {
		return (NamedFlags)currAddOn().valueForKey("access");
	}
	
	public EduCourse course() {
		if(_course == null) {
			_course = (EduCourse)valueForBinding("course");
		}
		return _course;
	}
	/*
	public EduPeriod eduPeriod() {
		if(_period == null) {
			_period = (EduPeriod)currAddOn().valueForKey("eduPeriod");
			if(_period == null) {
				NSArray pertypes = PeriodType.periodTypesForCourse(course());
				if(pertypes == null) return null;
				PeriodType pertype = (PeriodType)pertypes.objectAtIndex(0);
				_period = pertype.currentPeriod();
			}
		}
		return _period;
	}*/
	
	public Student student() {
		if(_student == null) {
			_student = (Student)valueForBinding("student");
		}
		return _student;
	}
		
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public boolean isStateless() {
		return true;
	}
	
	public void reset() {
		super.reset();
		_currAddOn = null;
		_course = null;
		//		_period = null;
		_student = null;
		_isActive = null;
	}
	
	public WOActionResults messagePopup(String message) {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		response.appendContentString("<div class=\"attention\" style=\"cursor:pointer;\" onclick=\"this.style.display='none';\">");
		response.appendContentString(message);
		response.appendContentString("</div>");
		return response;
	}
	
	protected Boolean _isActive;
	protected boolean isActive() {
		if(_isActive == null) {
			NamedFlags access = access();
			if(access == null || access.getFlag(0))
				_isActive = Boolean.TRUE;
			else
				_isActive = Boolean.FALSE;
		}
		return _isActive.booleanValue();
	}

	public String onmouseover() {
		if(isActive())
			return "dim(this)";
		else
			return null;
	}
	public String onmouseout() {
		if(isActive())
			return "unDim(this)";
		else
			return null;
	}
	public String onclick() {
		if(isActive())
			return (String)session().valueForKey("ajaxPopup");
		else
			return null;
	}
	
}
