// SubjectGroupComparator.java

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

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSComparator.ComparisonException;

public class SubjectGroupComparator extends NSComparator {
	
	public int compare(Object arg0, Object arg1) throws ComparisonException {
		if(arg1 == arg0)
			return OrderedSame;
		if(arg0 == null || arg0 instanceof NSKeyValueCoding.Null)
			return OrderedAscending;
		if(arg1 == null || arg1 instanceof NSKeyValueCoding.Null)
			return OrderedDescending;
		if(!(arg0 instanceof SubjectGroup && arg1 instanceof SubjectGroup))
			throw new ComparisonException("Can only compare SubjectGroups");
		SubjectGroup left = (SubjectGroup)arg0;
		SubjectGroup right = (SubjectGroup)arg1;
		return compare(left, right);
	}
	
	public static int compare(SubjectGroup left, SubjectGroup right) throws ComparisonException {
		if(left.parent() == right.parent())
			return AscendingNumberComparator.compare(left.sort(),right.sort());
		NSArray<SubjectGroup> lPath = left.path();
		NSArray<SubjectGroup> rPath = right.path();
		int len = Math.min(lPath.count(), rPath.count());
		try {
			for (int i = 0; i < len; i++) {
				SubjectGroup l = lPath.objectAtIndex(i);
				SubjectGroup r = rPath.objectAtIndex(i);
				if(l != r)
					return AscendingNumberComparator.compare(l.sort(),r.sort());
			}
			if(rPath.count() > len)
				return OrderedAscending;
			else if (lPath.count() > len)
				return OrderedDescending;
			else
				return OrderedSame;
		} catch (Exception e) {
			ComparisonException ce = new ComparisonException("Error comparing given objects: " + e);
			ce.initCause(e);
			throw ce;
		}
	}

	public static class ComparisonSupport extends EOSortOrdering.ComparisonSupport {
		SubjectGroupComparator comparator = new SubjectGroupComparator();
				
		public int compareAscending(Object left, Object right)  {
			try {
				return comparator.compare(left, right);
			} catch  (ComparisonException ex) {
				throw new NSForwardException(ex,"Error comparing");
			}
		}	
		public int compareCaseInsensitiveAscending(Object left, Object right)  {
			return compareAscending(left, right) ;
		}
		
		public int compareDescending(Object left, Object right)  {
			try {
				return comparator.compare(right,left);
			} catch  (ComparisonException ex) {
				throw new NSForwardException(ex,"Error comparing");
			}
		}
		public int compareCaseInsensitiveDescending(Object left, Object right)  {
			return compareDescending(left, right);
		}

	}
}
