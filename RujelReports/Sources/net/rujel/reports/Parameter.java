package net.rujel.reports;

import java.text.Format;
import java.util.Enumeration;

import net.rujel.base.MyUtility;
import net.rujel.reusables.DisplayAny;
import net.rujel.reusables.Various;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSSelector;

// Generated by the WOLips Templateengine Plug-in at Apr 29, 2009 10:31:19 AM
public class Parameter extends com.webobjects.appserver.WOComponent {

	protected NSKeyValueCodingAdditions _itemDict;
	protected NSKeyValueCoding _paramsDict;
    protected String _attrib;
    public Object item;

    public NSKeyValueCodingAdditions valueOf = new DisplayAny.ValueReader(this);;
	
	public Parameter(WOContext context) {
        super(context);
    }
	
	public NSKeyValueCoding paramsDict() {
		if(_paramsDict == null)
			_paramsDict = (NSKeyValueCoding)valueForBinding("paramsDict");
		return _paramsDict;
	}
	
	public NSKeyValueCodingAdditions itemDict() {
		if(_itemDict == null)
			_itemDict = (NSKeyValueCodingAdditions)valueForBinding("itemDict");
		return _itemDict;
	}
    
	public String attribute() {
		if(_attrib != null)
			return _attrib;
		_attrib = (String)valueForBinding("attribute");
		if(_attrib != null)
			return _attrib;
		_attrib = (String)itemDict().valueForKey("attribute");
		return _attrib;
	}
    
    public Object value() {
    	boolean secondSelector = (itemDict().valueForKey("secondSelector") != null);
    	String attribute = attribute();
    	if(secondSelector)
    		attribute = "min_" + attribute;
    	Object value = paramsDict().valueForKey(attribute);
    	if(value != null)
    		return (value == NullValue)?null:value;
    	value = valueOf.valueForKeyPath("paramsDict.itemDict.default" + 
    			((secondSelector)?"Min":"Value"));
    	paramsDict().takeValueForKey((value==null)?NullValue:value, attribute);
    	return value;
    }
    
    public void setValue(Object value) {
    	if(value == null)
    		value = NullValue;
    	boolean secondSelector = (itemDict().valueForKey("secondSelector") != null);
    	String attribute = attribute();
    	if(secondSelector)
    		attribute = "min_" + attribute;
    	paramsDict().takeValueForKey(value, attribute);
    }
    
    public Object secondValue() {
    	String attribute = "max_" + attribute();
    	Object value = paramsDict().valueForKey(attribute);
    	if(value != null)
    		return (value == NullValue)?null:value;
    	value = valueOf.valueForKeyPath("paramsDict.itemDict.defaultMax");
    	paramsDict().takeValueForKey((value==null)?NullValue:value, attribute);
    	return value;    	
    }

    public void setSecondValue(Object value) {
    	if(value == null)
    		value = NullValue;
    	String attribute = "max_" + attribute();
    	paramsDict().takeValueForKey(value, attribute);
    }

    public Boolean showField() {
		if(itemDict().valueForKey("select") != null)
			return Boolean.FALSE;
		if(_itemDict.valueForKey("popup") != null)
			return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	public String onkeypress() {
		String selector = (String)itemDict().valueForKey("qualifierSelector");
		if(selector != null && 
				(selector.equals("like") || selector.equals("caseInsensitiveLike")))
			return null;
		return "return isNumberInput(event,true);";
	}
	
	public Format formatter() {
		Object result = valueOf.valueForKeyPath("paramsDict.itemDict.formatter");
		if(result == null)
			return null;
		if(result instanceof Format)
			return (Format)result;
		if(result.equals("date"))
			return MyUtility.dateFormat();
		if(result instanceof String)
			return new NSNumberFormatter((String)result);
		return null;
	}
	
	public String sign() {
		String selector = (String)itemDict().valueForKey("qualifierSelector");
		if(selector == null || selector.equals(">="))
			return "&le;";
		else if (selector.equals(">"))
			return "&lt;";
		return WOMessage.stringByEscapingHTMLString(selector); 
	}
	public String sign2() {
		String selector = (String)itemDict().valueForKey("secondSelector");
		if(selector == null || selector.equals("<="))
			return "&le;";
		else if (selector.equals("<"))
			return "&lt;";
		return WOMessage.stringByEscapingHTMLString(selector); 
	}
	
	public WOActionResults selectorPopup() {
		WOComponent selector = pageWithName("SelectorPopup");
		selector.takeValueForKey(context().page(), "returnPage");
		selector.takeValueForKey("params." + attribute(), "resultPath");
		selector.takeValueForKey(value(), "value");
		NSMutableDictionary dict = (NSMutableDictionary)itemDict().valueForKey("popup");
		dict.takeValueForKeyPath(valueForBinding("editingContext"),
				"presenterBindings.editingContext");
		selector.takeValueForKey(dict, "dict");
		return selector;
	}
	
	public void clearParam() {
		paramsDict().takeValueForKey(null, attribute());
	}
	    
	public void reset() {
		_paramsDict = null;
		_itemDict = null;
		_attrib = null;
		super.reset();
	}
	
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public static EOQualifier qualForParam(
			NSKeyValueCoding itemDict, NSKeyValueCoding params,WOComponent page) {
		if(itemDict == null)
			return null;
		String selectorString = (String)itemDict.valueForKey("qualifierFormat");
		if (selectorString != null) {
			NSArray args = (NSArray) itemDict.valueForKey("args");
			if (args != null && args.count() > 0) {
				Enumeration enu = args.objectEnumerator();
				args = new NSMutableArray();
				while (enu.hasMoreElements()) {
					Object arg = enu.nextElement();
					Object param = DisplayAny.ValueReader.evaluateValue(arg,
							"params", page);
					if (param == null)
						param = NullValue;
					((NSMutableArray) args).addObject(param);
				}
			}
			EOQualifier qual = EOQualifier.qualifierWithQualifierFormat(
					selectorString, args);
			return qual;
		}
		if(params == null)
			return null;
		String secondSelector = (String)itemDict.valueForKey("secondSelector");
		selectorString = (String)itemDict.valueForKey("qualifierSelector");
		String attrib = (String)itemDict.valueForKey("attribute");
		if(secondSelector != null) {
			NSSelector sel = (selectorString == null)?
						EOQualifier.QualifierOperatorGreaterThanOrEqualTo:
							EOQualifier.operatorSelectorForString(selectorString);;
				
			Object min = params.valueForKey("min_" + attrib);
			Object max = params.valueForKey("max_" + attrib);
			NSMutableArray quals = new NSMutableArray();
			if(min != null && min != NullValue) {
				quals.addObject(new EOKeyValueQualifier(attrib,sel,min));
			}
			if(max != null && max != NullValue) {
				sel = EOQualifier.operatorSelectorForString(secondSelector);
				quals.addObject(new EOKeyValueQualifier(attrib,sel,max));
			}
			switch (quals.count()) {
			case 0:
				return null;
			case 1:
				return (EOQualifier)quals.objectAtIndex(0);
			default:
				return new EOAndQualifier(quals);
			}
		} else {
			NSSelector selector = EOQualifier.QualifierOperatorEqual;
			Object value = params.valueForKey(attrib);
			if(value == null || value == NullValue) {
				if(Various.boolForObject(itemDict.valueForKey("respectNull")))
					return new EOKeyValueQualifier(attrib,selector,NullValue);
				else
					return null;
			}
			if(selectorString != null)
				selector = EOQualifier.operatorSelectorForString(selectorString);
			return new EOKeyValueQualifier(attrib,selector,value);
		}
	}
}