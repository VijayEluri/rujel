package net.rujel.dnevnik;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.axis.types.UnsignedByte;

import ru.mos.dnevnik.*;

import net.rujel.base.BaseCourse;
import net.rujel.base.BaseLesson;
import net.rujel.base.EntityIndex;
import net.rujel.base.IndexRow;
import net.rujel.base.Indexer;
import net.rujel.base.MyUtility;
import net.rujel.base.SettingsBase;
import net.rujel.criterial.CriteriaSet;
import net.rujel.criterial.Work;
import net.rujel.criterial.WorkType;
import net.rujel.eduplan.Subject;
import net.rujel.eduresults.ItogContainer;
import net.rujel.interfaces.EduCourse;
import net.rujel.interfaces.EduCycle;
import net.rujel.interfaces.EduGroup;
import net.rujel.interfaces.Student;
import net.rujel.io.ExtBase;
import net.rujel.io.ExtSystem;
import net.rujel.io.SyncIndex;
import net.rujel.io.SyncMatch;
import net.rujel.markarchive.MarkArchive;
import net.rujel.reusables.NamedFlags;
import net.rujel.reusables.Various;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

public class Sychroniser {
	
	public static final UnsignedByte ZERO = new UnsignedByte(0);
	public static final UnsignedByte ONE = new UnsignedByte(1);
	
	private EOEditingContext ec;
	public Integer eduYear;
	public ExtSystem system;
	public ExtBase localBase;
	public String schoolGuid;
	protected NSDictionary workTypes;
	protected NSDictionary itogTypes;
	
	private EntityIndex lessonEI;
	private EntityIndex workEI;
	private EntityIndex timeslotEI;
	
	protected ImportServiceSoap soap;
	public NSMutableDictionary readyWorks;
	
	protected SettingsBase criteriaSettings;
	protected NSMutableDictionary critSetParams;

	
	public Sychroniser(EOEditingContext ec, Integer eduYear) {
		super();
		this.ec = ec;
		this.eduYear = eduYear;
	}

	public void syncChanges(NSTimestamp since, NSTimestamp to) {
		EOQualifier qual = null;
		if(since != null) {
			if(to == null)
				qual = new EOKeyValueQualifier(MarkArchive.TIMESTAMP_KEY, 
						EOQualifier.QualifierOperatorGreaterThan, since);
			else
				qual = EOQualifier.qualifierWithQualifierFormat(
						"timestamp > %@ AND timestamp <= %@", new NSArray(new Object[] {since,to}));
		} else if (to != null) {
			qual = new EOKeyValueQualifier(MarkArchive.TIMESTAMP_KEY, 
					EOQualifier.QualifierOperatorLessThanOrEqualTo, to);
		}
		NSArray list = new NSArray(
				new EOSortOrdering(MarkArchive.TIMESTAMP_KEY, EOSortOrdering.CompareAscending));
		EOFetchSpecification fs = new EOFetchSpecification(MarkArchive.ENTITY_NAME,qual,list);
		fs.setPrefetchingRelationshipKeyPaths(new NSArray(MarkArchive.USED_ENTITY_KEY));
		list = ec.objectsWithFetchSpecification(fs);
		if(list == null || list.count() == 0)
			return;
		if(system == null)
			system = (ExtSystem)ExtSystem.extSystemNamed("oejd.moscow", ec, true);
		system.setCachesIndexes(true);
		if(localBase == null)
			localBase = ExtBase.localBase(ec);
		if(schoolGuid == null)
			schoolGuid = localBase.extSystem().extDataForKey("schoolGUID", null);
		localBase.extSystem().setCachesIndexes(true);
		if(ec.hasChanges())
			ec.saveChanges();
		Enumeration enu = list.objectEnumerator();
		while (enu.hasMoreElements()) {
			MarkArchive arch = (MarkArchive) enu.nextElement();
			String entity = (String)arch.valueForKeyPath("usedEntity.usedEntity");
			try {
				if("BaseLesson".equals(entity))
					syncLesson(arch);
				else if("BaseNote".equals(entity))
					syncNote(arch);
				else if("Work".equals(entity))
					syncWork(arch);
				else if("Mark".equals(entity))
					syncMark(arch);
				else if("ItogMark".equals(entity))
					syncItogMark(arch);
			} catch (Exception e) {
				throw new NSForwardException(e);
			}
		}
	}
	
	private SyncMatch getMatch(EntityIndex ei, Integer objID) {
		EOQualifier qual = SyncMatch.matchQualifier(system, null, ei, objID, eduYear);
		EOFetchSpecification fs = new EOFetchSpecification(SyncMatch.ENTITY_NAME,qual,null);
		NSArray found = ec.objectsWithFetchSpecification(fs);
		if(found != null && found.count() > 0)
			return (SyncMatch)found.objectAtIndex(0);
		return null;
	}
	
	private long subjectID (EduCourse course) throws RemoteException {
		Subject subj = (Subject)course.valueForKeyPath("cycle.subjectEO");
		return subjectID(subj);
	}
	private long subjectID (Subject subj) throws RemoteException {
		String subjExtid = system.extidForObject(subj, null);
		if(subjExtid == null) {
			SyncIndex indexer = system.getIndexNamed("eduAreas", null, false);
			String areaName = indexer.extForLocal(MyUtility.getID(subj.area()));
			KnowledgeArea area = (areaName == null) ? KnowledgeArea.Common :
					KnowledgeArea.fromString(areaName);
			long result = soap.insertSubject(schoolGuid, subj.subject(), subj.fullName(), area);
			SyncMatch match = system.addMatch(subj, null);
			match.setExtID(Long.toString(result));
			return result;
		}
		return Long.parseLong(subjExtid); 
	}
	
	private long syncLesson(MarkArchive arch) throws RemoteException {
		if(lessonEI == null)
			lessonEI = EntityIndex.indexForEntityName(ec, "BaseLesson", false);
		SyncMatch match = null;
		match = getMatch(lessonEI, arch.getKeyValue("lesson"));
		if(match == null && arch.actionType().intValue() == 3)
			return 0;
		if(arch.actionType().intValue() == 3) {
			long lessonID = Long.parseLong(match.extID());
			soap.deleteLesson(lessonID);
			ec.deleteObject(match);
			return 0;
		}
		EduCourse course = (EduCourse)EOUtilities.objectWithPrimaryKeyValue(ec,
				EduCourse.entityName, arch.getKeyValue("course"));
		String groupGuid = localBase.extidForObject(course.eduGroup());
		NSTimestamp date = (NSTimestamp)arch.valueForKey("@date_date");
		String teacherGuid = localBase.extidForObject(course.teacher(date));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		UnsignedByte number = new UnsignedByte(arch.getArchiveValueForKey("number"));
		Integer timeslot = new Integer(getTimeslot(course, cal, number.intValue()));
		cal.setTime(date);
		if(timeslotEI == null)
			timeslotEI = EntityIndex.indexForEntityName(ec, "Timeslot", true);
		SyncMatch tsMatch = getMatch(timeslotEI, timeslot);
		long subjectID = subjectID(course);
		long[] ids = (tsMatch == null) ? new long[2] : parceIDs(tsMatch.extID());
		if(match == null) {
			match = (SyncMatch)EOUtilities.createAndInsertInstance(ec, SyncMatch.ENTITY_NAME);
			match.setExtSystem(system);
			match.setEntityIndex(lessonEI);
			match.setEduYear(eduYear);
			match.setObjID(arch.getKeyValue("lesson"));
			if(tsMatch == null) {
				InsertLessonResult result = soap.insertLesson(
						groupGuid, subjectID, teacherGuid, cal, number);
				ids[0] = result.getLessonID();
				ids[1] = result.getWorkID();
				tsMatch = (SyncMatch)EOUtilities.createAndInsertInstance(ec, SyncMatch.ENTITY_NAME);
				tsMatch.setExtSystem(system);
				tsMatch.setEntityIndex(timeslotEI);
				tsMatch.setEduYear(eduYear);
				tsMatch.setObjID(timeslot);
				StringBuilder buf = new StringBuilder();
				buf.append(ids[0]).append(' ').append(ids[1]);
				tsMatch.setExtID(buf.toString());
			} else {
				soap.updateLesson(ids[0], subjectID, teacherGuid, cal, number);
			}
			
			match.setExtID(Long.toString(ids[0]));
			/*
			Timeslot timeslot = new Timeslot(course, cal, number);
			timeslot.lessonID = result.getLessonID();
			if(readyWorks == null)
				readyWorks = new NSMutableDictionary(new Long(result.getWorkID()),timeslot);
			else
				readyWorks.setObjectForKey(new Long(result.getWorkID()),timeslot);*/
		} else {
			if(tsMatch == null) { // update timeslot
				NSArray found = EOUtilities.objectsWithQualifierFormat(ec, SyncMatch.ENTITY_NAME, 
						"extID like '&s *'", new NSArray(match.extID()));
				if(found != null && found.count() > 0) {
					tsMatch = (SyncMatch)found.objectAtIndex(0);
					tsMatch.setObjID(timeslot);
					ids = parceIDs(tsMatch.extID());
				} else {
					ids[0] = Long.parseLong(match.extID());
				}
			}
			soap.updateLesson(ids[0], subjectID, teacherGuid, cal, number);
		}
		if(ids[1] != 0)
			soap.updateWork(ids[1], EduWorkType.LessonBehavior, null, ONE, null,
					arch.getArchiveValueForKey("theme"));
		return ids[0];
	}
	
	private MarkArchive getContainerMA(MarkArchive arch, String entity, String key) {
		EOQualifier[] quals = new EOQualifier[4];
		quals[0] = new EOKeyValueQualifier("usedEntity.usedEntity", 
				EOQualifier.QualifierOperatorEqual, entity);
		quals[1] = new EOKeyValueQualifier(MarkArchive.KEY1_KEY, 
				EOQualifier.QualifierOperatorEqual, arch.getKeyValue(key));
		quals[2] = new EOKeyValueQualifier(MarkArchive.TIMESTAMP_KEY, 
				EOQualifier.QualifierOperatorLessThan, arch.timestamp());
		quals[3] = new EOKeyValueQualifier(MarkArchive.ACTION_TYPE_KEY, 
				EOQualifier.QualifierOperatorLessThan, new Integer(3));
		quals[0] = new EOAndQualifier(new NSArray(quals));
		NSArray list = new NSArray(new EOSortOrdering(
				MarkArchive.TIMESTAMP_KEY, EOSortOrdering.CompareDescending));
		EOFetchSpecification fs = new EOFetchSpecification(
				MarkArchive.ENTITY_NAME, quals[0],list);
		fs.setFetchLimit(1);
		list = ec.objectsWithFetchSpecification(fs);
		if(list == null || list.count() == 0)
			return null;
		return (MarkArchive)list.objectAtIndex(0);
	}
	
	private void syncNote(MarkArchive arch) throws RemoteException {
		if(lessonEI == null)
			lessonEI = EntityIndex.indexForEntityName(ec, "BaseLesson", false);
		SyncMatch match = null;
		if(arch.actionType().intValue() != 1) {
			match = getMatch(lessonEI, arch.getKeyValue("lesson"));
			if(match == null && arch.actionType().intValue() == 3)
				return;
		}
		long lessonID;
		if(match == null) {
			MarkArchive la = getContainerMA(arch, "BaseLesson", "lesson");
			if(la == null)
				return;
			lessonID = syncLesson(la);
		} else {
			lessonID = Long.parseLong(match.extID());
		}
		String personGuid = SyncMatch.getMatch(localBase.extSystem(), localBase,
				Student.entityName, arch.getKeyValue("student")).extID();

		if(arch.actionType().intValue() >= 3) {
			soap.updateLessonLogEntry(lessonID, personGuid, EduLessonLogEntryStatus.Attend);
		} else {
			String note = arch.getArchiveValueForKey("text");
			boolean att = (BaseLesson.isSkip(note) == 0);
			soap.updateLessonLogEntry(lessonID, personGuid, 
					(att)?EduLessonLogEntryStatus.Attend : EduLessonLogEntryStatus.Absent);
		}
	}
	
	private NSDictionary[] syncWork(MarkArchive arch) throws RemoteException {
		if(workEI == null)
			workEI = EntityIndex.indexForEntityName(ec, "Work", false);
		SyncMatch match = null;
		if(arch.actionType().intValue() != 1) {
			match = getMatch(workEI, arch.getKeyValue("work"));
			if(match == null && arch.actionType().intValue() == 3)
				return null;
		}
		if(arch.actionType().intValue() == 3) {
			String extID = match.extID();
			int idx2 = extID.lastIndexOf(';');
			while (idx2 > 0) {
				int idx1 = idx2 -1;
				if(Character.isDigit(extID.charAt(idx1))) {
					idx1 = extID.lastIndexOf(' ',idx1);
					long workID = Long.parseLong(extID.substring(idx1,idx2));
					soap.deleteWork(workID);
				}
				idx2 = extID.lastIndexOf(';',idx1);
			}
			ec.deleteObject(match);
			return null;
		}
		EduCourse course = (EduCourse)EOUtilities.objectWithPrimaryKeyValue(ec,
				EduCourse.entityName, arch.getKeyValue("course"));
		String maxVal = arch.getArchiveValueForKey("m0");
		NSMutableDictionary[] criteria = null;
		Integer csID = null;
		if(maxVal == null) {
//			maxVal = "5";
			if(critSetParams == null) {
				criteriaSettings = SettingsBase.baseForKey(CriteriaSet.ENTITY_NAME, ec, false);
				critSetParams = new NSMutableDictionary();
			}
			if(criteriaSettings!=null)
				csID = (Integer) criteriaSettings.forCourse(course).valueForKey(
						SettingsBase.NUMERIC_VALUE_KEY);
			if(csID == null || csID.intValue() == 0) {
				NSDictionary arcDict = arch.getArchiveDictionary();
				Enumeration enu = arcDict.keyEnumerator();
				int max = 0;
				NSMutableDictionary crByNum = new NSMutableDictionary();
				while (enu.hasMoreElements()) {
					String key = (String) enu.nextElement();
					if(key.charAt(0) != 'm' && !Character.isDigit(key.charAt(1)))
						continue;
					int crit = Integer.parseInt(key.substring(1));
					if(crit > max)
						max = crit;
					String crMax = (String)arcDict.valueForKey(key);
					NSMutableDictionary dict = new NSMutableDictionary();
					dict.takeValueForKey(new Integer(crMax), "criterMax");
					Integer criter = new Integer(crit);
					dict.takeValueForKey(CriteriaSet.critNameForNum(criter, null), "criterName");
					crByNum.setObjectForKey(dict, criter);
				}
//				crByNum.takeValueForKey(new Integer(max), "maxCriterion");
				criteria = new NSMutableDictionary[max +1];
				Enumeration cenu = crByNum.keyEnumerator();
				while (cenu.hasMoreElements()) {
					Integer cr = (Integer) cenu.nextElement();
					criteria[cr] = (NSMutableDictionary)crByNum.objectForKey(cr);
				}
			} else {
				NSDictionary[] preset = analyseCriteriaSet(csID);
				int count = 0;
				criteria = new NSMutableDictionary[preset.length];
				for (int i = 0; i < preset.length; i++) {
					String crMax = arch.getArchiveValueForKey("m" + i);
					if(crMax == null)
						continue;
					count++;
					if(preset[i] == null) {
						criteria[i] = new NSMutableDictionary(
								CriteriaSet.critNameForNum(i, null), "criterName");
						criteria[i].takeValueForKey(new Integer(crMax), "criterMax");
					} else {
						criteria[i] = preset[i].mutableClone();
						if(!criteria[i].containsKey("criterMax"))
							criteria[i].takeValueForKey(new Integer(crMax), "criterMax");
					}
				}
				if(count <= 0) {
					criteria = null;
					csID = null;
				}
			}
		} // no criterless max found
		if(criteria == null) {
//			extid.append('#').append(' ').append(maxVal).append(' ');
			NSMutableDictionary dict = new NSMutableDictionary("#","criterName");
			if(maxVal == null)
				dict.takeValueForKey(new Integer(5), "criterMax");
			else
				dict.takeValueForKey(new Integer(maxVal), "criterMax");
			criteria = new NSMutableDictionary[] {dict};
		}
		EduWorkType workType = getWorkType(arch);
		String title = arch.getArchiveValueForKey(Work.THEME_KEY);
		String[] existing = null;
		long lessonID;
		if(match == null) {
			String groupGuid = localBase.extidForObject(course.eduGroup());
			NSTimestamp date = (NSTimestamp)arch.valueForKey("@date_date");
			String teacherGuid = localBase.extidForObject(course.teacher(date));
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			UnsignedByte number = new UnsignedByte(arch.getArchiveValueForKey("number"));
			Integer timeslot = new Integer(getTimeslot(course, cal, number.intValue()));
			cal.setTime(date);
			if(timeslotEI == null)
				timeslotEI = EntityIndex.indexForEntityName(ec, "Timeslot", true);
			SyncMatch tsMatch = getMatch(timeslotEI, timeslot);
			long[] ids;
			if(tsMatch == null) {
				InsertLessonResult result = soap.insertLesson(
						groupGuid, subjectID(course), teacherGuid, cal, number);
				ids = new long[2];
				ids[0] = result.getLessonID();
				ids[1] = result.getWorkID();
				tsMatch = (SyncMatch)EOUtilities.createAndInsertInstance(ec, SyncMatch.ENTITY_NAME);
				tsMatch.setExtSystem(system);
				tsMatch.setEntityIndex(timeslotEI);
				tsMatch.setEduYear(eduYear);
				tsMatch.setObjID(timeslot);
				StringBuilder buf = new StringBuilder();
				buf.append(ids[0]).append(' ').append(ids[1]);
				tsMatch.setExtID(buf.toString());
			} else {
				ids = parceIDs(tsMatch.extID());
			}
			lessonID = ids[0];
			match = (SyncMatch)EOUtilities.createAndInsertInstance(ec, SyncMatch.ENTITY_NAME);
			match.setExtSystem(system);
			match.setEntityIndex(workEI);
			match.setEduYear(eduYear);
			match.setObjID(arch.getKeyValue("work"));
			if(workType == EduWorkType.LessonBehavior) {
				existing = new String[criteria.length];
				for (int i = 0; i < criteria.length; i++) {
					if(criteria[i] != null) {
						existing[i] = "# 5 " + ids[1];
						break;
					}
				}
			}
		} else {
			String extID = match.extID();
			int idx = extID.indexOf('|');
			int idx2 = Math.min(idx, extID.indexOf(' '));
			lessonID = Long.parseLong(extID.substring(0,idx2));
			extID = extID.substring(idx +1);
			existing = extID.split(";",-1);
		}
		for (int i = 0; i < existing.length || i < criteria.length; i++) {
			Long workID = null;
			if(i < existing.length && existing[i] != null && existing[i].length() > 0) {
				int idx = existing[i].lastIndexOf(' ');
				workID = new Long(existing[i].substring(idx +1));
			}
			if(i < criteria.length && criteria[i] != null) {
				String name = title;
				if(i > 0) {
					name = title + " [" + criteria[i].valueForKey("criterName") + ']';
				}
				EduMarkType markType = (EduMarkType)criteria[i].valueForKey("markType");
				if(markType == null) {
					Integer max = (Integer)criteria[i].valueForKey("criterMax");
					markType = typeForMax(max);
					if(markType == null)
						markType = EduMarkType.Mark100;
				}
				if(workID == null) {
					soap.insertWork(lessonID, workType, markType, ONE, name, null);
				} else {
					soap.updateWork(workID.longValue(), workType, markType, ONE, name, null);
				}
				criteria[i].takeValueForKey(workID, "workID");
			} else if(workID != null) {
					soap.deleteWork(workID.longValue());
			}
		}
		StringBuilder extid = new StringBuilder();
		extid.append(lessonID);
		if(csID != null)
			extid.append(' ').append(csID);
		extid.append('|');
		for (int i = 0; i < criteria.length; i++) {
			if(criteria[i] != null) {
				extid.append(criteria[i].valueForKey("criterName")).append(' ');
				extid.append(criteria[i].valueForKey("criterMax")).append(' ');
				extid.append(criteria[i].valueForKey("workID"));
			}
			extid.append(';');
		}
		match.setExtID(extid.toString());
		return criteria;
	}

	public static final NSDictionary[] emptyCritset = 
		new NSDictionary[] {NSDictionary.EmptyDictionary};
	protected NSDictionary[] analyseCriteriaSet(Integer csID) {
		NSDictionary[] preset = (NSDictionary[])critSetParams.objectForKey(csID);
		if(preset != null)
			return preset;
		CriteriaSet set = (CriteriaSet)EOUtilities.objectWithPrimaryKeyValue(
				ec, CriteriaSet.ENTITY_NAME, csID);
		NSArray crits = set.criteria();
		if(crits == null || crits.count() == 0) {
			critSetParams.setObjectForKey(emptyCritset, csID);			
			return emptyCritset;
		}
		Enumeration enu = crits.objectEnumerator();
		int max = 0;
		NSMutableDictionary byNum = new NSMutableDictionary();
		while (enu.hasMoreElements()) {
			EOEnterpriseObject cr = (EOEnterpriseObject) enu.nextElement();
			String title = (String)cr.valueForKey("title");
			Integer criter = (Integer) cr.valueForKey("criterion");
			if(criter.intValue() > max)
				max = criter.intValue();
			if(title == null) title = CriteriaSet.critNameForNum(criter, null);
			title = title.replace(' ', '_').replace(';', ':');
			NSMutableDictionary dict = new NSMutableDictionary(title,"criterName");
			dict.takeValueForKey(cr.valueForKey("dftlMax"), "criterMax");
			byNum.setObjectForKey(dict, criter);
			Indexer indexer = (Indexer)cr.valueForKey("indexer");
			if(indexer == null)
				continue;
			boolean nums = true;
			boolean amer = true;
			boolean plus = false;
			NSMutableDictionary mapping = new NSMutableDictionary();
			int vMax = 0;
			Enumeration ienu = indexer.indexRows().objectEnumerator();
			while (ienu.hasMoreElements()) {
				IndexRow row = (IndexRow) ienu.nextElement();
				mapping.takeValueForKey(row.idx(), row.value());
				String value = row.value().trim();
				if(value.length() > 1) {
					char fin = value.charAt(value.length() -1);
					if(fin == '+' || fin == '-')
						plus = true;
					value = value.substring(0,value.length() -1).trim();
				}
				if(nums) {
					try {
						int val = Integer.parseInt(value);
						if(val > vMax)
							vMax = val;
						amer = false;
					} catch (Exception e) {
						nums = false;
					}
				}
				if(amer) {
					int val = -1;
					if(value.length() == 1) {
						val = "ABCDEF".indexOf(value.toUpperCase());
					}
					amer = (val >= 0);
				}
			} // index rows
			if(amer) {
				dict.takeValueForKey(EduMarkType.MarkAbcdf, "markType");
				dict.takeValueForKey(new Integer(5), "criterMax");
			} else if(nums) {
				EduMarkType markType = typeForMax(vMax);
				if(markType == null) {
					dict.addEntriesFromDictionary(mapping);
				} else {
					dict.takeValueForKey(markType, "markType");
					dict.takeValueForKey(new Integer(vMax), "criterMax");
				}
			} else {
				dict.addEntriesFromDictionary(mapping);
			}
			if(plus)
				dict.takeValueForKey(Boolean.TRUE, "plusMinus");
		} // set ctiteria enumeration
//		byNum.takeValueForKey(new Integer(max), "maxCriterion");
		preset = new NSDictionary[max];
		Enumeration cenu = byNum.keyEnumerator();
		while (cenu.hasMoreElements()) {
			Integer cr = (Integer) cenu.nextElement();
			preset[cr] = (NSMutableDictionary)byNum.objectForKey(cr);
			preset[cr] = preset[cr].immutableClone();
		}
		return preset;
	}
	
	protected EduWorkType getWorkType(MarkArchive arch) {
		if(workTypes == null) {
			SyncIndex indexer = system.getIndexNamed("workType", null, false);
			workTypes = indexer.getDict();
			if(workTypes == null) {
				workTypes = new NSMutableDictionary();
				NSArray types = EOUtilities.objectsForEntityNamed(ec, WorkType.ENTITY_NAME);
				Integer onLesson = (Integer)WorkType.specTypes.valueForKey("onLesson");
				Enumeration enu = types.objectEnumerator();
				while (enu.hasMoreElements()) {
					WorkType type = (WorkType) enu.nextElement();
					if(onLesson.equals(type.dfltFlags()))
						workTypes.takeValueForKey("LessonBehavior", MyUtility.getID(type));
					else if(type.namedFlags().flagForKey("hometask"))
						workTypes.takeValueForKey("LessonHomework", MyUtility.getID(type));
				}
			} // generate defaultTypes
		}
		String workType = arch.getArchiveValueForKey("workType");
		workType = (workType==null)?null:(String)workTypes.valueForKey(workType.substring(9));
		if(workType == null)
			workType = "LessonBehavior";
		return EduWorkType.fromString(workType);
	}
	
	private EduMarkType typeForMax(int max) {
		switch (max) {
		case 5:
			return EduMarkType.Mark5;
		case 6:
			return EduMarkType.MarkOral6Band;
		case 7:
			return EduMarkType.Mark7;
		case 10:
			return EduMarkType.Mark10;
		case 12:
			return EduMarkType.Mark12;
		case 1:
			return EduMarkType.Test;
		case 100:
			return EduMarkType.Mark100;
		default:
			return null;	
		}
	}
	
	private void syncMark(MarkArchive arch) throws RemoteException {
		if(workEI == null)
			workEI = EntityIndex.indexForEntityName(ec, "Work", false);
		SyncMatch match = null;
		match = getMatch(workEI, arch.getKeyValue("work"));
		if(match == null && arch.actionType().intValue() == 3)
			return;
		NSDictionary[] criteria;
		if(match == null) {
			MarkArchive wa = getContainerMA(arch, "Work", "work");
			criteria = syncWork(wa);
		} else {
			String extID = match.extID();
			int idx = extID.indexOf('|');
			int idx2 = extID.lastIndexOf(' ',idx);
			NSDictionary[] preset = null;
			if(idx2 > 0) {
				Integer csID = new Integer(extID.substring(idx2 +1, idx));
				preset = analyseCriteriaSet(csID);
			}
			String[] exist = extID.substring(idx +1).split(";");
			criteria = new NSMutableDictionary[exist.length];
			for (int i = 0; i < exist.length; i++) {
				if(exist == null || exist[i].length() == 0)
					continue;
				String[] vals = exist[i].split(" ",3);
				if(preset[i] == null) {
					criteria[i] = new NSMutableDictionary(vals[0],"criterName");
					criteria[i].takeValueForKey(new Integer(vals[1]), "criterMax");
				} else {
					criteria[i] = preset[i].mutableClone();
					if(!preset[i].containsKey("criterMax"))
						criteria[i].takeValueForKey(new Integer(vals[1]), "criterMax");
				}
				criteria[i].takeValueForKey(new Long(vals[2]), "workID");
			}
		}
		String personGuid = SyncMatch.getMatch(localBase.extSystem(), localBase,
				Student.entityName, arch.getKeyValue("student")).extID();
		int act = arch.actionType().intValue();
		String text = arch.getArchiveValueForKey("text");
		if(".".equals(text))
			text = "";
		for (int i = 0; i < criteria.length; i++) {
			if(criteria[i] == null)
				continue;
			Long workID = (Long)criteria[i].valueForKey("workID");
			if(act >= 3) {
				soap.deleteMark(workID.longValue(), personGuid, ZERO);
				continue;
			}
			String criterName = (String)criteria[i].valueForKey("criterName");
			String value = arch.getArchiveValueForKey(criterName);
			if(".".equals(value)) {
				soap.deleteMark(workID.longValue(), personGuid, ZERO);
				continue;
			}
			EduMarkType markType = (EduMarkType)criteria[i].valueForKey("markType");
			MarkBonusType bonus = MarkBonusType.NotSet;
			BigDecimal mark = null;
			if(Various.boolForObject(criteria[i].valueForKey("plusMinus"))) {
				int l = value.length() -1;
				char last = value.charAt(l);
				if(last == '+') {
					bonus = MarkBonusType.Plus;
					value = value.substring(0,l).trim();
				} else if(last == '-') {
					bonus = MarkBonusType.Minus;
					value = value.substring(0,l).trim();
				}
			}
			if(markType == EduMarkType.MarkAbcdf) {
				mark = new BigDecimal("ABCDEF".indexOf(value.toUpperCase()));
			} else if(markType == null) {
				Integer max = (Integer)criteria[i].valueForKey("criterMax");
				markType = typeForMax(max);
				if(markType == null) {
					markType = EduMarkType.Mark100;
					Integer mapped = (Integer)criteria[i].valueForKey(value);
					if(mapped == null)
						mapped = new Integer(value);
					mark = new BigDecimal(mapped * 100);
					mark = mark.divide(new BigDecimal(max),0,BigDecimal.ROUND_HALF_UP);
				} else {
					mark = new BigDecimal(value);
				}
			} else {
				mark = new BigDecimal(value);
			}
			if(act == 1) {
				try {
					soap.insertMark(workID, personGuid, mark, markType, ZERO, bonus, text);
				} catch (RemoteException e) {
					soap.updateMark(workID, personGuid, ZERO, mark, markType, bonus, text);
				}
			} else {
				if(!soap.updateMark(workID, personGuid, ZERO, mark, markType, bonus, text))
					soap.insertMark(workID, personGuid, mark, markType, ZERO, bonus, text);
			}
//			text = null;
		} // criteria
	}
	
	private void syncItogMark(MarkArchive arch) throws RemoteException {
		String mark = arch.getArchiveValueForKey("march");
		if(mark == null)
			return;
		else
			mark = mark.trim();
		MarkBonusType bonus = MarkBonusType.NotSet;
		{
			char c = mark.charAt(mark.length() -1);
			if(c == '+') {
				bonus = MarkBonusType.Plus;
				mark = mark.substring(0, mark.length() -1).trim();
			} else if(c == '-') {
				bonus = MarkBonusType.Minus;
				mark = mark.substring(0, mark.length() -1).trim();
			}
		}
		EduMarkType markType = EduMarkType.Mark5;
		if(mark.matches("[НнH]\\W*[АаA]\\W?")) {
			markType = EduMarkType.MarkNA;
		} else if (mark.toLowerCase().contains("осв")) {
			markType = EduMarkType.MarkDismiss;
		}
		if(mark.length() > 0) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < mark.length(); i++) {
				char c = mark.charAt(i);
				if(Character.isDigit(c))
					buf.append(c);
			}
			mark = buf.toString();
		}
		BigDecimal value = null;
		try {
			value = new BigDecimal(mark);
		} catch (Exception e) {
			return;
		}
		
		EduCycle cycle = (EduCycle)EOUtilities.objectWithPrimaryKeyValue(ec,
				EduCycle.entityName, arch.getKeyValue("eduCycle"));
		Student student = (Student)EOUtilities.objectWithPrimaryKeyValue(ec,
				Student.entityName, arch.getKeyValue("student"));
		EOQualifier[] quals = new EOQualifier[2];
		quals[0] = new EOKeyValueQualifier("eduYear",
				EOQualifier.QualifierOperatorEqual,eduYear);
		quals[1]  = new EOKeyValueQualifier("cycle",
				EOQualifier.QualifierOperatorEqual,cycle);
		quals[0] = new EOAndQualifier(new NSArray(quals));
		EOFetchSpecification fs = new EOFetchSpecification(EduCourse.entityName,quals[0],null);
		NSArray found = ec.objectsWithFetchSpecification(fs);
		if(found == null || found.count() == 0)
			return;
		EduGroup group = null;
		Enumeration enu = found.objectEnumerator();
		while (enu.hasMoreElements()) {
			EduCourse course = (EduCourse) enu.nextElement();
			if(course.groupList().containsObject(student)) {
				group = course.eduGroup();
				break;
			}
		}
		if(group == null)
			group = student.recentMainEduGroup();
		String groupGuid = localBase.extidForObject(group);
		Subject subj = (Subject)cycle.valueForKey("subjectEO");
		long subjectID = subjectID(subj);
		ItogContainer container = (ItogContainer)EOUtilities.objectWithPrimaryKeyValue(ec,
				ItogContainer.ENTITY_NAME, arch.getKeyValue("container"));
		String studentGuid = SyncMatch.getMatch(localBase.extSystem(), localBase,
				Student.entityName, arch.getKeyValue("student")).extID();
		String type = MyUtility.getID(container.itogType());
		if(itogTypes == null) {
			SyncIndex indexer = system.getIndexNamed("periods", null, false);
			workTypes = indexer.getDict();
			if(itogTypes == null) {
				itogTypes = new NSMutableDictionary();
				int count = container.itogType().inYearCount().intValue();
				switch (count) {
				case 4:
					workTypes.takeValueForKey("Quarter", type);
					break;
				case 3:
					workTypes.takeValueForKey("Trimester", type);
					break;
				case 2:
					workTypes.takeValueForKey("Semester", type);
					break;
				case 1:
					workTypes.takeValueForKey("Year", type);
					break;
				case 0:
					workTypes.takeValueForKey("Exam", type);
					break;
				default:
					workTypes.takeValueForKey("Module", type);
					break;
				}
			}
		}
		type = (String)workTypes.valueForKey(type);
		int act = arch.actionType().intValue();
		if("Exam".equals(type) || "Final".equals(type)) {
			FinalMarkType ftype = FinalMarkType.fromString(type);
			if(act == 1) {
				try {
					soap.insertFinalMark(groupGuid, subjectID, studentGuid,
							markType, ftype, value, bonus, null);
				} catch (RemoteException e) {
					soap.updateFinalMark(groupGuid, subjectID, studentGuid, 
							markType, ftype, value, bonus, null);
				}
			} else {
				if(!soap.updateFinalMark(groupGuid, subjectID, studentGuid, 
							markType, ftype, value, bonus, null))
					soap.insertFinalMark(groupGuid, subjectID, studentGuid,
							markType, ftype, value, bonus, null);
			}
		} else {
			EduReportingPeriodType periodType = EduReportingPeriodType.fromString(type);
			UnsignedByte periodNumber = new UnsignedByte(container.num().intValue() -1);
			if(act == 1) {
				try {
					soap.insertPeriodMark(groupGuid, subjectID, studentGuid,
							markType, periodType, periodNumber, value, bonus, null);
				} catch (RemoteException e) {
					soap.updatePeriodMark(groupGuid, subjectID, studentGuid,
							markType, periodType, periodNumber, value, bonus, null);
				}
			} else {
				if(!soap.updatePeriodMark(groupGuid, subjectID, studentGuid, 
						markType, periodType, periodNumber, value, bonus, null))
					soap.insertPeriodMark(groupGuid, subjectID, studentGuid, 
							markType, periodType, periodNumber, value, bonus, null);
			}
		}
	}
	
	public int getTimeslot(MarkArchive arch) {
		EduCourse course = (EduCourse)EOUtilities.objectWithPrimaryKeyValue(ec,
				EduCourse.entityName, arch.getKeyValue("course"));
		Calendar cal = Calendar.getInstance();
		NSTimestamp date = (NSTimestamp)arch.valueForKey("@date_date");
		cal.setTime(date);
		String number = arch.getArchiveValueForKey("number");
		return getTimeslot(course, cal, Integer.parseInt(number));
	}
	
	public int getTimeslot(EduCourse course, Calendar cal, int num) {
		int result = cal.get(Calendar.DAY_OF_YEAR);
		while(cal.get(Calendar.YEAR) > eduYear.intValue()) {
			cal.add(Calendar.YEAR, -1);
			result += cal.getActualMaximum(Calendar.DAY_OF_YEAR);
		}
		result = (result << 5);
		result += num;
		EOKeyGlobalID gid = (EOKeyGlobalID)ec.globalIDForObject(course);
		Integer key = (Integer)gid.keyValues()[0];
		result += (key.intValue() << 15);
		return result;
	}
	
	public long[] parceIDs(String extid) {
		int idx = extid.indexOf(' ');
		long[] ids = new long[2];
		try {
			ids[0] = Long.parseLong(extid.substring(0,idx));
			ids[1] = Long.parseLong(extid.substring(idx +1));
		} catch (Exception e) {
			throw new IllegalArgumentException("Unsupported extid format: " + extid, e);
		}
		return ids;
	}

	public static class Timeslot implements Comparable {
		public EduCourse course;
		public short year;
		public byte month;
		public byte day;
		public byte number;
		public long lessonID;
		
		public Timeslot(EduCourse course, Calendar cal, UnsignedByte num) {
			this.course = course;
			year = (short)cal.get(Calendar.YEAR);
			month = (byte)cal.get(Calendar.MONTH);
			day = (byte)cal.get(Calendar.DATE);
			number = num.byteValue();
		}

		public boolean equals(Timeslot other) {
			return (course == other.course && number == other.number &&
					day == other.day && month == other.month && year == other.year);
		}

		public static BaseCourse.ComparisonSupport comparator = new BaseCourse.ComparisonSupport();
		public int compareTo(Object arg0) {
			Timeslot other = (Timeslot)arg0;
			if(year > other.year) return 1;
			if(year < other.year) return -1;
			if(month > other.month) return 1;
			if(month < other.month) return -1;
			if(day > other.day) return 1;
			if(day < other.day) return -1;
			if(number > other.number) return 1;
			if(number < other.number) return -1;
			return comparator.compareAscending(course, other.course);
		}
	}
	
	public static String assumeWorkType(WorkType type) {
		if(type.dfltFlags().equals(WorkType.specTypes.valueForKey("onLesson")))
			return "LessonBehavior";
		NamedFlags flags = type.namedFlags();
		if(flags.flagForKey("hometask"))
			return "LessonHomework";
		return null;
	}
}
