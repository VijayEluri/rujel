// PrognosisPopup.java: Class file for WO Component 'PrognosisPopup'

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

import java.math.BigDecimal;
import java.util.logging.Logger;

import net.rujel.base.MyUtility;
import net.rujel.interfaces.*;
import net.rujel.reusables.*;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

// Generated by the WOLips Templateengine Plug-in at May 18, 2008 10:06:02 PM
public class PrognosisPopup extends com.webobjects.appserver.WOComponent {
    public PrognosisPopup(WOContext context) {
        super(context);
    }
    
    public Prognosis prognosis;
    public PrognosesAddOn addOn;
    public EduCourse course;
    public Student student;
    public AutoItog eduPeriod;
    public WOComponent returnPage;
    
    public String mark;
    public NamedFlags flags;
    
    public String bonusPercent;
    public String bonusText;
    public boolean hasBonus;
    public boolean noEditBonusText;
 	public boolean ifArchive;
 	public String changeReason;
    
    protected boolean calculation;
    public boolean noCancel = false;
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(addOn == null)
    		addOn = new PrognosesAddOn(session());
    	addOn.setCourse(course);
    	addOn.setStudent(student);
    	addOn.setPeriodItem(eduPeriod);
    	calculation = (eduPeriod.calculator() != null);
    	flags = new NamedFlags(Prognosis.flagNames);
       	ifArchive = (eduPeriod.namedFlags().flagForKey("manual") &&
       			SettingsReader.boolForKeyPath("markarchive.Prognosis", false));
    	if(prognosis == null)  {
    		Calculator calc = eduPeriod.calculator();
    		if(calc != null) {
    			EOEditingContext ec = course.editingContext();
    			ec.lock();
    			prognosis = calc.calculateForStudent(student, course, eduPeriod,
    					eduPeriod.relatedForCourse(course));
    			if(prognosis != null) {
    				prognosis.updateFireDate(addOn.courseTimeout());
    				noCancel = true;
    				/*if(ifArchive) {
    					EOEnterpriseObject archive = EOUtilities.createAndInsertInstance(ec,"MarkArchive");
    					archive.takeValueForKey(prognosis, "object");
    					String calcName = addOn.usage().calculatorName();
						//calcName = calcName.substring(calcName.lastIndexOf('.') +1);
						archive.takeValueForKey(calcName, "reason");
    				}
    				try {
    					ec.saveChanges();
    					//addOn.setPrognosis(prognosis);
    					noCancel = true;
    				} catch (Exception e) {
    					Logger.getLogger("rujel.autoitog").log(WOLogLevel.WARNING,
    							"Error creating single prognosis");
    					ec.revert();
    				}*/
    			}
    			ec.unlock();
    		}
    	} // prognosis == null
		hasBonus = false;
		bonusPercent = null;
		if(prognosis != null) {
			flags.setFlags(prognosis.flags().intValue());
			mark= prognosis.mark();
			addOn.setPrognosis(prognosis);
			if(eduPeriod.calculator() != null) {
				Bonus bonus = prognosis.bonus();
				if(bonus != null)
					bonusText = bonus.reason();
				BigDecimal bonusValue = (bonus == null)?Bonus.calculateBonus(prognosis,null,false)
						:bonus.calculateValue(prognosis, false);
				hasBonus = (bonus != null && 
						bonus.value().compareTo(bonusValue) >= 0);
				NamedFlags accessBonus = (NamedFlags)session().valueForKeyPath("readAccess.FLAGS.Bonus");
				if(!((bonus == null)?accessBonus.flagForKey("create"):accessBonus.flagForKey("read")))
					bonusValue = null;
				bonusPercent = (bonusValue == null)?null:fractionToPercent(bonusValue);
				//String param = (hasBonus)?"Bonus":"BonusText";
				noEditBonusText = !accessBonus.flagForKey(
						(bonus != null && bonus.submitted())?"edit":"create");
			}
		}
     	super.appendToResponse(aResponse, aContext);
    }
    
    public boolean showPercent() {
    	if(prognosis == null || !calculation)
    		return false;
    	return true;
    }

    public boolean cantChange() {
    	String flag = (prognosis == null)?"create":"edit";
    	return !access().flagForKey(flag);
    }

    public NamedFlags access() {
    	return addOn.access();
    }
    
    public WOActionResults save() {
    	if(prognosis != null && prognosis.editingContext() == null)
    		prognosis = null;
    	if(prognosis !=null || mark != null) {
    		EOEditingContext ec = course.editingContext();
	    	Logger logger = Logger.getLogger("rujel.autoitog");
    		ec.lock();
    		if(mark == null && !calculation) {
    			if(ec.globalIDForObject(prognosis).isTemporary())
    				ec.revert();
    			else
    				ec.deleteObject(prognosis);
    			prognosis = null;
    		} else {
    			if(mark == null) {
    				prognosis = eduPeriod.calculator().calculateForStudent(student, course, 
    						eduPeriod, eduPeriod.relatedForCourse(course));
    				changeReason = eduPeriod.calculatorName();
    			} else if(prognosis == null) {
    				prognosis = (Prognosis)EOUtilities.createAndInsertInstance(ec, "Prognosis");
       				prognosis.setStudent(student);
       				prognosis.setCourse(course);
       				prognosis.setAutoItog(eduPeriod);
     			}
    			if(mark != null && !mark.equals(prognosis.mark()) &&
    					eduPeriod.namedFlags().flagForKey("manual")) {
    				prognosis.setMark(mark);
    			}
   				prognosis.updateFireDate();
	    		Bonus bonus = prognosis.bonus();
    	    	Object[] args = new Object[] {session(),bonus,prognosis};
    	    	if(hasBonus) {
    	    		if(bonus == null) {
    	    			bonus = (Bonus)EOUtilities.createAndInsertInstance(ec, Bonus.ENTITY_NAME);
//    	    			bonus.initBonus(prognosis, true);
    	    			args[1] = bonus;
    	    			logger.log(WOLogLevel.UNOWNED_EDITING,"Adding bonus to prognosis",args);
    	    		} else if(!bonus.submitted()) {
    	    			bonus.calculateValue(prognosis, true);
    	    			logger.log(WOLogLevel.UNOWNED_EDITING,"Submitting bonus for prognosis",args);
    	    		}
    	    	} else {
    	    		if(bonus != null) {
    	    			bonus.zeroBonus();
    	    			prognosis.updateMarkFromValue();
    	    			logger.log(WOLogLevel.UNOWNED_EDITING,"Unsubmitting bonus from prognosis",args);
    	    		}
	    			flags.setFlagForKey(false, "keepBonus");
    			}
    	    	if(bonusText != null) {
    	    		if(bonus == null) {
    	    			bonus = (Bonus)EOUtilities.createAndInsertInstance(ec, Bonus.ENTITY_NAME);
//    	    			bonus.initBonus(prognosis, false);
    	    			prognosis.addObjectToBothSidesOfRelationshipWithKey(bonus, 
    	    					Prognosis.BONUS_KEY);
    	    			Bonus.calculateBonus(prognosis, bonus, false);
    	    			args[1] = bonus;
    	    			logger.log(WOLogLevel.UNOWNED_EDITING,"Requesting bonus for prognosis",args);
    	    		}
    	    		bonus.setReason(bonusText);
    	    	} else if(bonus != null) {
    	    		if(Various.boolForObject(session().valueForKeyPath("readAccess._delete.Bonus"))) {
    	    			Object message = application().valueForKeyPath(
    					"strings.RujelAutoItog_AutoItog.ui.cantDeleteBonus");
    	    			session().takeValueForKey(message, "message");
    	    		} else {
    	    			prognosis.removeObjectFromBothSidesOfRelationshipWithKey(bonus, "bonus");
    	    			ec.deleteObject(bonus);
    	    			logger.log(WOLogLevel.UNOWNED_EDITING,"Removing bonus from prognosis",args);  
    	    		}
    	    	}
   			prognosis.setNamedFlags(flags);
    		} //  if !(mark == null && !calculation)
    		try {
    			if(ec.hasChanges()) {
    				ec.saveChanges();
    				if(ifArchive) {
    					EOEnterpriseObject archive = EOUtilities.createAndInsertInstance(ec,"MarkArchive");
    					if(prognosis != null) {
    						archive.takeValueForKey(prognosis, "object");
    						if(prognosis.bonus() != null) {
    							archive.takeValueForKey(bonusText,"@bonusText");
    							archive.takeValueForKey(prognosis.bonus().value(),"@bonusValue");
    						}
    					} else {
    						archive.takeValueForKey(identifierDictionary(), "identifierDictionary");
    						archive.takeValueForKey(null,"@mark");
    					}
    					archive.takeValueForKey(changeReason, "reason");
        				ec.saveChanges();
    				}
					EOEnterpriseObject grouping = PrognosesAddOn.getStatsGrouping(course, eduPeriod.itogContainer());
					if(grouping != null) {
						NSArray prognoses = Prognosis.prognosesArrayForCourseAndPeriod(
								course, eduPeriod.itogContainer(),false);
						//NSDictionary stats = PrognosesAddOn.statCourse(course, eduPeriod);
						prognoses = MyUtility.filterByGroup(prognoses, "student", 
								course.groupList(), true);
						grouping.takeValueForKey(prognoses, "array");
						ec.saveChanges();
					}
    			}
    			addOn.setPrognosis(prognosis);
    		} catch (Exception e) {
    			logger.log(WOLogLevel.WARNING,"Error saving prognosis",e);
    			session().takeValueForKey(e.getMessage(), "message");
				ec.revert();
			} finally {
				ec.unlock();
			}
    	}
    	returnPage.ensureAwakeInContext(context());
    	return returnPage;
    }
    
    public WOActionResults delete() {
    	mark = null;
    	calculation = false;
    	return save();
    }
    
    public static String fractionToPercent(BigDecimal decimal) {
    	if(decimal == null || decimal.compareTo(BigDecimal.ZERO) == 0)
    		return "0";
    	decimal = decimal.movePointRight(2).stripTrailingZeros();
    	if(decimal.scale() < 0)
    		decimal = decimal.setScale(0);
    	return decimal.toString() + " %";
    }
    
    public String completePercent() {
    	return fractionToPercent(prognosis.complete());
    }
    public String valuePercent() {
    	return fractionToPercent(prognosis.value());
    }
        
    public String bonusTitle() {
    	String key = "requestBonus";
    	if(hasBonus) {
    		key = "addedBonus";
    	} else if (prognosis.bonus() != null) {
			key = "requestedBonus";
		}
    	String result = (String)application().valueForKeyPath(
    			"strings.RujelAutoItog_AutoItog.ui." + key);
    	if(result == null)
    		result = key;
    	return result;
    }
    
 	public NSMutableDictionary identifierDictionary() {
		if(student == null || eduPeriod == null || course == null)
    		return null;
		NSMutableDictionary ident = new NSMutableDictionary("Prognosis","entityName");
		ident.takeValueForKey(eduPeriod.itogContainer(),"itog");
		ident.takeValueForKey(student, "student");
		ident.takeValueForKey(course,"course");
		ident.takeValueForKey(course.editingContext(), "editingContext");
		return ident;
    }
 	
	public String onkeypress() {
		if(!ifArchive || mark == null)
			return null;
		return "showObj('prognosChangeReason');form.changeReason.onkeypress();";
	}

	public EOEnterpriseObject item;
    public NSMutableDictionary archDict;
    
    public void setItem(EOEnterpriseObject newItem) {
    	item = newItem;
    	if(item == null)
    		return;
    	if(archDict == null)
    		archDict = new NSMutableDictionary();
    	else
    		archDict.removeAllObjects();
    	String flagsString = (String)item.valueForKey("@flags");
    	NamedFlags archFlags = (flagsString == null)? DegenerateFlags.ALL_FALSE:
    		new NamedFlags(Integer.parseInt(flagsString),Prognosis.flagNames);
    	archDict.takeValueForKey(archFlags, "flags");
    	if(archFlags.flagForKey("disable"))
    		archDict.takeValueForKey("grey", "styleClass");
    	if(archFlags.flagForKey("keep"))
    		archDict.takeValueForKey("font-weight:bold;","style");

    	String  bonusReason = (String)item.valueForKey("@bonusText");
    	if(bonusReason != null) {
        	StringBuffer title = new StringBuffer((String)item.valueForKey("@value"));
    		BigDecimal bonusValue = new BigDecimal((String)item.valueForKey("@bonusValue"));
    		StringBuffer bonus = new StringBuffer("<span style = \"color:#ff0000;\">+");
    		if(bonusValue.compareTo(BigDecimal.ZERO) > 0) {
    			if(archFlags.flagForKey("keepBonus"))
    				bonus.append("<strong>!</strong>");
    			else
    				bonus.append('!');
    			title.append(" + ").append(bonusValue);
    		} else {
    			bonus.append('?');
    		}
    		bonus.append("</span>");
    		archDict.takeValueForKey(bonus.toString(),"bonusString");
    		title.append(" (").append(WOMessage.stringByEscapingHTMLAttributeValue(bonusReason));
    		title.append(')');
    		archDict.takeValueForKey(title.toString(),"title");
    	} else {
    		archDict.takeValueForKey(item.valueForKey("@value"),"title");
    	}
    	
    	
    }
}