// Indexer.java

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

import java.math.RoundingMode;
import java.util.Enumeration;

import net.rujel.reusables.Various;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
 
public class Indexer extends _Indexer
{
    public Indexer() {
        super();
    }
    private static EOGlobalID typesGID;
    public static void setTipesGID(EOGlobalID gid) {
    	if(typesGID != null) {
    		if(!gid.equals(typesGID))
    			throw new IllegalStateException("TypesGID is already set");
    	} else {
    		typesGID = gid;
    	}
    }
    
    protected Integer minIndex;
    protected Integer maxIndex;
    protected String indexType;
    
    protected NSMutableArray indexCache;
    protected void initIndex() {
    	if(indexCache == null) {
    		indexCache = indexRows().mutableClone();
    		EOSortOrdering.sortArrayUsingKeyOrderArray(indexCache, MyUtility.numSorter);
    		if(indexCache.count() > 0) {
    			IndexRow row = (IndexRow)indexCache.objectAtIndex(0);
    			minIndex = row.idx();
    			row = (IndexRow)indexCache.lastObject();
    			maxIndex = row.idx();
    		}
    	}
    }
    
    public Integer minIndex() {
    	initIndex();
    	return minIndex;
    }

    public Integer maxIndex() {
    	initIndex();
    	return maxIndex;
    }

    public IndexRow rowForIndex(int index, RoundingMode round) {
    	initIndex();
    	if(indexCache.count() == 0)
    		return null;
		int i = 0;
		IndexRow row = null;
		for(i = 0;i < indexCache.count();i++) {
			row = (IndexRow) indexCache.objectAtIndex(i);
			if(row.num() == index)
				return row;
			if(row.num() > index)
				break;
			row = null;
		}
		if(round == null || round == RoundingMode.UNNECESSARY)
			return null;
    	IndexRow prev = (i==0)?null:(IndexRow)indexCache.objectAtIndex(i -1);
		switch (round) {
		case CEILING:
			return row;
		case FLOOR:
			return prev;
		case UP:
			if(index >= 0)
				return row;
			else
				return prev;
		case DOWN:
			if(index < 0)
				return row;
			else
				return prev;			
		default:
	    	if(i == 0)
	    		return row;
	    	if(row == null)
	    		return prev;
			if((row.num() - index) < (index - prev.num()))
				return row;
			if((index - prev.num()) < (row.num() - index))
				return prev;
		}
		switch (round) {
		case HALF_DOWN:
			return prev;
		case HALF_UP:
			return row;
		case HALF_EVEN:
			if(i % 2 > 0)
				return prev;
			else
				return row;
		default:
			return null;
		}
    }
    
	public String valueForIndex(int index, RoundingMode round) {
		IndexRow row = rowForIndex(index, round);
		if(row == null)
			return defaultValue();
		return row.value();
	}
	
	public String formattedForIndex(int index, RoundingMode round) {
		if(formatString() == null)
			return valueForIndex(index, round);
		IndexRow row = rowForIndex(index, round);
		if(row != null)
			return row.formatted();
		return String.format(formatString(), index, defaultValue(), null);
	}
	
	public Integer indexForValue(String value, boolean ignoreCase, boolean add) {
		Integer result = indexForValue(value, ignoreCase);
		if(add && result == null) {
			result = new Integer(maxIndex().intValue() +1);
			IndexRow row = (IndexRow)EOUtilities.createAndInsertInstance(
					editingContext(), IndexRow.ENTITY_NAME);
			row.setIdx(result);
			row.setValue(value);
			addObjectToBothSidesOfRelationshipWithKey(row, INDEX_ROWS_KEY);
		}
		return result;
	}
	
	public Integer indexForValue(String value, boolean ignoreCase) {
		initIndex();
    	if(indexCache.count() == 0)
    		return null;
		Enumeration enu = indexCache.objectEnumerator();
		while (enu.hasMoreElements()) {
			IndexRow row = (IndexRow) enu.nextElement();
			if(ignoreCase) {
				if(value.equalsIgnoreCase(row.value()))
					return row.idx();
			} else {
				if(value.equals(row.value()))
					return row.idx();
			}
		}
		return null;
	}
	
	public void nullify() {
		minIndex = null;
		maxIndex = null;
		if(indexCache != null) {
			Enumeration enu = indexCache.objectEnumerator();
			while (enu.hasMoreElements()) {
				IndexRow row = (IndexRow) enu.nextElement();
				row.nullify();
			}
			indexCache = null;
		}
		indexType = null;
	}
	
	public void turnIntoFault(EOFaultHandler handler) {
		nullify();
		super.turnIntoFault(handler);
	}
	
	public void setIndexRows(NSArray value) {
		nullify();
		super.setIndexRows(value);
	}

	public void addToIndexRows(EOEnterpriseObject object) {
		includeObjectIntoPropertyWithKey(object, INDEX_ROWS_KEY);
		if(indexCache != null) {
			indexCache.addObject(object);
    		EOSortOrdering.sortArrayUsingKeyOrderArray(indexCache, MyUtility.numSorter);
		}
		Integer idx = (Integer)object.valueForKey(IndexRow.IDX_KEY);
		if(minIndex != null && minIndex.compareTo(idx) > 0)
			minIndex = idx;
		if(maxIndex != null && maxIndex.compareTo(idx) < 0)
			maxIndex = idx;
	}

	public void removeFromIndexRows(EOEnterpriseObject object) {
		excludeObjectFromPropertyWithKey(object, INDEX_ROWS_KEY);
		if(indexCache != null) {
			indexCache.removeObject(object);
			if(indexCache.count() == 0) {
				minIndex = null;
				maxIndex = null;
				return;
			}
		}
		Integer idx = (Integer)object.valueForKey(IndexRow.IDX_KEY);
		if(idx.equals(minIndex)) {
			IndexRow row = (IndexRow)indexCache.objectAtIndex(0);
			minIndex = row.idx();
		}
		if(idx.equals(maxIndex)) {
			IndexRow row = (IndexRow)indexCache.lastObject();
			maxIndex = row.idx();
		}
	}
	
	public IndexRow setValueForIndex(String value, int index) {
		IndexRow row = rowForIndex(index, null);
		if(row == null) {
			row = (IndexRow)EOUtilities.createAndInsertInstance(
					editingContext(), IndexRow.ENTITY_NAME);
			row.setIdx(new Integer(index));
			addObjectToBothSidesOfRelationshipWithKey(row, INDEX_ROWS_KEY);
		}
		return row;
	}
	
	public String indexType() {
		if(typesGID == null)
			return null;
		if(indexType == null) {
			EOEditingContext ec = editingContext();
			Indexer typeIndex = (Indexer)ec.faultForGlobalID(typesGID, ec);
			indexType = typeIndex.valueForIndex(type().intValue(), null);
		}
		return indexType;
	}
	
	public void setIndexType(String type) {
		EOEditingContext ec = editingContext();
		Indexer typeIndex = (Indexer)ec.faultForGlobalID(typesGID, ec);
		Integer idx = typeIndex.indexForValue(type, true,true);
		super.setType(idx);
		indexType = type;
	}
	
	public void setType(Integer type) {
		indexType = null;
		super.setType(type);
	}
	
	public static NSArray indexersOfType(EOEditingContext ec, String type) {
		Indexer typeIndex = (Indexer)ec.faultForGlobalID(typesGID, ec);
		if(type.indexOf('*') < 0 && type.indexOf('?') < 0) {
			Integer typeIdx = typeIndex.indexForValue(type, true);
			return EOUtilities.objectsMatchingKeyAndValue(ec, ENTITY_NAME, TYPE_KEY, typeIdx);
		}
		EOQualifier qual = new EOKeyValueQualifier(IndexRow.VALUE_KEY, 
				EOQualifier.QualifierOperatorCaseInsensitiveLike, type);
		NSArray found = EOQualifier.filteredArrayWithQualifier(typeIndex.indexRows(), qual);
		if(found == null || found.count() == 0)
			return found;
		found = (NSArray)found.valueForKey(IndexRow.IDX_KEY);
		qual = Various.getEOInQualifier(TYPE_KEY, found);
		EOFetchSpecification fs = new EOFetchSpecification(ENTITY_NAME, qual, null);
		return ec.objectsWithFetchSpecification(fs);
	}
}
