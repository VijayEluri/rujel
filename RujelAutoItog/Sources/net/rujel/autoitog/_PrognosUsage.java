// _PrognosUsage.java

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

package net.rujel.autoitog;


import com.webobjects.eocontrol.*;

public abstract class _PrognosUsage extends EOGenericRecord {

    public _PrognosUsage() {
        super();
    }

/*
    // If you add instance variables to store property values you
    // should add empty implementions of the Serialization methods
    // to avoid unnecessary overhead (the properties will be
    // serialized for you in the superclass).
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    }
*/

    public String calculatorName() {
        return (String)storedValueForKey("calculatorName");
    }

    public void setCalculatorName(String aValue) {
        takeStoredValueForKey(aValue, "calculatorName");
    }

    public Number eduYear() {
        return (Number)storedValueForKey("eduYear");
    }

    public void setEduYear(Number aValue) {
        takeStoredValueForKey(aValue, "eduYear");
    }

    public Number flags() {
        return (Number)storedValueForKey("flags");
    }

    public void setFlags(Number aValue) {
        takeStoredValueForKey(aValue, "flags");
    }

    public net.rujel.criterial.BorderSet borderSet() {
        return (net.rujel.criterial.BorderSet)storedValueForKey("borderSet");
    }

    public void setBorderSet(net.rujel.criterial.BorderSet aValue) {
        takeStoredValueForKey(aValue, "borderSet");
    }

    public net.rujel.eduresults.PeriodType periodType() {
        return (net.rujel.eduresults.PeriodType)storedValueForKey("periodType");
    }

    public void setPeriodType(net.rujel.eduresults.PeriodType aValue) {
        takeStoredValueForKey(aValue, "periodType");
    }
}
