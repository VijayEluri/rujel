// _SyncIndex.java

/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	o	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	o	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	o	Neither the name of the RUJEL nor the names of its contributors may be used
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

// Created by eogenerator
// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to SyncIndex.java instead.

package net.rujel.io;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.BigDecimal;

@SuppressWarnings("all")
public abstract class _SyncIndex extends EOGenericRecord {
	public static final String ENTITY_NAME = "SyncIndex";

	// Attributes
	public static final String EDU_YEAR_KEY = "eduYear";
	public static final String INDEX_NAME_KEY = "indexName";

	// Relationships
	public static final String EXT_BASE_KEY = "extBase";
	public static final String EXT_SYSTEM_KEY = "extSystem";
	public static final String INDEX_MATCHES_KEY = "indexMatches";

  public Integer eduYear() {
    return (Integer) storedValueForKey(EDU_YEAR_KEY);
  }

  public void setEduYear(Integer value) {
    takeStoredValueForKey(value, EDU_YEAR_KEY);
  }

  public String indexName() {
    return (String) storedValueForKey(INDEX_NAME_KEY);
  }

  public void setIndexName(String value) {
    takeStoredValueForKey(value, INDEX_NAME_KEY);
  }

  public net.rujel.io.ExtBase extBase() {
    return (net.rujel.io.ExtBase)storedValueForKey(EXT_BASE_KEY);
  }

  public void setExtBase(EOEnterpriseObject value) {
    	takeStoredValueForKey(value, EXT_BASE_KEY);
  }
  
  public net.rujel.io.ExtSystem extSystem() {
    return (net.rujel.io.ExtSystem)storedValueForKey(EXT_SYSTEM_KEY);
  }

  public void setExtSystem(EOEnterpriseObject value) {
    	takeStoredValueForKey(value, EXT_SYSTEM_KEY);
  }
  
  public NSArray indexMatches() {
    return (NSArray)storedValueForKey(INDEX_MATCHES_KEY);
  }
 
  public void setIndexMatches(NSArray value) {
    takeStoredValueForKey(value, INDEX_MATCHES_KEY);
  }
  
  public void addToIndexMatches(EOEnterpriseObject object) {
    includeObjectIntoPropertyWithKey(object, INDEX_MATCHES_KEY);
  }

  public void removeFromIndexMatches(EOEnterpriseObject object) {
    excludeObjectFromPropertyWithKey(object, INDEX_MATCHES_KEY);
  }

}
