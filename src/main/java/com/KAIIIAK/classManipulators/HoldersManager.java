package com.KAIIIAK.classManipulators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.KAIIIAK.classManipulators.Tools.LazyInit;
import com.KAIIIAK.nullsafety.Opt;

public class HoldersManager {
	
	public static HashMap<String, LazyInit<MandatoryGroup>> groups = new HashMap<>();
	
	public static void registerGroup(String name, MandatoryType type, boolean optional, String... included) {
		if (groups.containsKey(name) && groups.get(name).initialized())
			throw new RuntimeException("Trying to register already existed group with name " + name);
		MandatoryGroup g2register = new MandatoryGroup(name, optional);
		g2register.type = type;
		for (String incGName : Opt.it(included)) {
			g2register.mandatoryChecks.add(new MandatoryGroup.MandatoryCheckObtainer(incGName, groups.computeIfAbsent(incGName, s -> new LazyInit<>())));
		}
		groups.computeIfAbsent(name, s -> new LazyInit<>(g2register)).setIfAbsent(g2register);
	}
	
	public static void applySuccessor2Group(String name, ChangesHolder.Successor successor) {
		if (!groups.containsKey(name) || !groups.get(name).initialized())
			throw new RuntimeException("Trying to add Successor to uninitialazed group with name " + name);
		groups.get(name).get().mandatoryChecks.add(new MandatoryGroup.MandatoryCheckObtainer(successor));
	}
	
	public static void mandatoriesCheck() {
		AtomicReference<String> crashReason = new AtomicReference<>("Mandatory check failed:");
		boolean hasFail = false;
		
		for (Map.Entry<String, LazyInit<MandatoryGroup>> entry : Opt.it(groups.entrySet())) {
			String groupName = entry.getKey();
			LazyInit<MandatoryGroup> lazyGroup = entry.getValue();
			
			if (!lazyGroup.initialized()) {
				crashReason.set(crashReason.get() + "\nGroup '" + groupName + "' NOT INITIALIZED");
				hasFail = true;
				continue;
			}
			
			MandatoryGroup group = lazyGroup.get();
			IMandatoryCheck.CheckState state = group.check();
			
			if (state == IMandatoryCheck.CheckState.FAILED && !group.optional) {
				//crashReason.set(crashReason.get()+"\nGroup '" + groupName + "' FAILED");
				mandatoriesCheckFailedGroupRecursive(crashReason, group, groupName, true);
				hasFail = true;
			}
		}
		
		if (hasFail) throw new RuntimeException(crashReason.get());
	}
	
	
	private static void mandatoriesCheckFailedGroupRecursive(AtomicReference<String> crashReason, IMandatoryCheck checkObj, String groupName, boolean first) {
		if (checkObj instanceof MandatoryGroup.MandatoryCheckObtainer) {
			MandatoryGroup.MandatoryCheckObtainer checkObjMCO = (MandatoryGroup.MandatoryCheckObtainer) checkObj;
			if (!checkObjMCO.wrappedObj.initialized()) {
				crashReason.set(crashReason.get() + "\nGroup '" + groupName + "' rely on UNINITIALIZED group '" + checkObjMCO.groupName + "'");
				return;
			}
			checkObj = checkObjMCO.wrappedObj.get();
		}
		
		if (checkObj instanceof ChangesHolder.Successor) {
			if (checkObj.check() == IMandatoryCheck.CheckState.FAILED) {
				ChangesHolder.Successor checkObjS = (ChangesHolder.Successor) checkObj;
				crashReason.set(crashReason.get() + "\nSuccessor inside group '" + groupName + "' with ChangesHolder '" + checkObjS.relatedChH + "' have failed!");
			}
			return;
		}
		if (checkObj instanceof MandatoryGroup) {
			MandatoryGroup checkObjMG = (MandatoryGroup) checkObj;
			String groupedGroupsName = first ? groupName : groupName + ">>" + checkObjMG.name;
			AtomicReference<String> localCrashReason = new AtomicReference<>("\nGroup '" + groupedGroupsName + "' FAILED");
			boolean hasFail = false;
			for (IMandatoryCheck iMandatoryCheck : Opt.it(checkObjMG.mandatoryChecks)) {
				if (iMandatoryCheck.check() == IMandatoryCheck.CheckState.FAILED) {
					mandatoriesCheckFailedGroupRecursive(localCrashReason, iMandatoryCheck, groupedGroupsName, false);
					hasFail = true;
				}
			}
			if (hasFail) crashReason.set(crashReason.get() + localCrashReason.get());
		}
	}
	
	
	public static class MandatoryGroup implements IMandatoryCheck {
		
		public boolean optional;
		public HashSet<IMandatoryCheck> mandatoryChecks = new HashSet<>();
		public MandatoryType type;
		public String name;
		
		public MandatoryGroup(String name, boolean optional) {
			this.name = name;
			this.optional = optional;
		}
		
		@Override
		public CheckState check() {
			return type.test(mandatoryChecks);
		}
		
		@Override
		public boolean isGroup() {
			return true;
		}
		
		public static class MandatoryCheckObtainer implements IMandatoryCheck {
			
			public LazyInit<? extends IMandatoryCheck> wrappedObj;
			public final boolean isGroup;// final due to HashSet logic it is inside...
			public String groupName = "";// in case of LazyInit<MandatoryGroup> - goupname needed to debug...
			
			public MandatoryCheckObtainer(String incGName, LazyInit<MandatoryGroup> mandatoryGroup) {
				wrappedObj = mandatoryGroup;
				wrappedObj.setFailSetCondition(o -> !(o instanceof MandatoryGroup));
				wrappedObj.setFailSetMSG("It is not allowed to set not MandatoryGroup into LazyInit<MandatoryGroup>");
				isGroup = true;
				setFailSetConditionSuccessor = null;
				groupName = incGName;
			}
			
			final ChangesHolder.Successor setFailSetConditionSuccessor;
			
			public MandatoryCheckObtainer(ChangesHolder.Successor successor) {
				wrappedObj = new LazyInit<>(successor);
				setFailSetConditionSuccessor = successor;
				wrappedObj.setFailSetCondition(o -> !(o instanceof ChangesHolder.Successor) || o != setFailSetConditionSuccessor);
				wrappedObj.setFailSetMSG("It is not allowed to set not Successor into LazyInit<Successor> or set another Successor...");
				isGroup = false;
				
			}
			
			@Override
			public CheckState check() {
				return wrappedObj.get().check();
			}
			
			@Override
			public boolean isGroup() {
				return isGroup;
			}
			
			@Override
			public boolean equals(Object other) {
				if (other instanceof MandatoryCheckObtainer) {
					MandatoryCheckObtainer otherCstd = (MandatoryCheckObtainer) other;
					if (!isGroup() && !otherCstd.isGroup() && wrappedObj.initialized() && otherCstd.wrappedObj.initialized()) {
						return wrappedObj.get().equals(otherCstd.wrappedObj.get());
					}
				}
				return super.equals(other);
			}
			
			@Override
			public int hashCode() {
				if (!isGroup() && wrappedObj.initialized()) {
					return wrappedObj.get().hashCode();
				}
				return super.hashCode();
			}
		}
	}
}
