// EduResultPresenter.java: Class file for WO Component 'EduResultPresenter'

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

package net.rujel.eduresults;

import net.rujel.interfaces.*;
import net.rujel.reusables.*;

import com.webobjects.appserver.*;

public class EduResultPresenter extends WOComponent {

    public EduResultPresenter(WOContext context) {
        super(context);
    }
	
	private ItogMark _itog;
	public ItogMark itog() {
		if(_itog == null) {
			EduPeriod lesson = (EduPeriod)valueForBinding("lesson");
			Student student = (Student)valueForBinding("student");
			_itog = lesson.forPersonLink(student);
		}
		return _itog;
	}
	
	public boolean isSelected () {
		return Various.boolForObject(valueForBinding("isSelected"));
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}

		public boolean isStateless() {
		return true;
	}
	
	public void reset() {
		_itog = null;
	}
    public String hover() {
		if(itog() == null) return null;
 		//if(isSelected()) {
			return itog().value().toString();
		/*} else {
			return itog().comment();
		}*/
    }
	
    public boolean canEdit() {
		if(itog() == null) return false;

        return (isSelected());
    }
	/*
    public boolean canComment() {
		if(itog() == null) return false;
        return (isSelected());
    } */
}
