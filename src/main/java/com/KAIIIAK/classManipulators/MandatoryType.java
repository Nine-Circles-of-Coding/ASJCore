package com.KAIIIAK.classManipulators;

import com.KAIIIAK.classManipulators.IMandatoryCheck.CheckState;

import java.util.Set;

import static com.KAIIIAK.classManipulators.IMandatoryCheck.CheckState.*;

public enum MandatoryType {
	IF_GROUPS_ALL_AND_SUCCESSORS_ANY {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			
			boolean atLeastOneSuccessorIsWaiting = false;
			boolean atLeastOneGroupIsWaiting = false;
			boolean atLeastOneSuccessorIsApplied = false;
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsWaiting = true;
					} else {
						atLeastOneSuccessorIsWaiting = true;
					}
				}
				if (state == FAILED) {
					if (iMandatoryCheck.isGroup()) {
						return FAILED;
					}
				}
				if (state == APPLIED) {
					if (!iMandatoryCheck.isGroup()) {
						atLeastOneSuccessorIsApplied = true;
					}
				}
			}
			if (atLeastOneGroupIsWaiting && (atLeastOneSuccessorIsWaiting || atLeastOneSuccessorIsApplied))
				return WAITING;
			if (!atLeastOneGroupIsWaiting && atLeastOneSuccessorIsApplied) return APPLIED;
			return FAILED;
		}
	},
	IF_GROUPS_ANY_AND_SUCCESSORS_ALL {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			
			boolean atLeastOneSuccessorIsWaiting = false;
			boolean atLeastOneGroupIsWaiting = false;
			boolean atLeastOneGroupIsApplied = false;
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsWaiting = true;
					} else {
						atLeastOneSuccessorIsWaiting = true;
					}
				}
				if (state == FAILED) {
					if (!iMandatoryCheck.isGroup()) {
						return FAILED;
					}
				}
				if (state == APPLIED) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsApplied = true;
					}
				}
			}
			if ((atLeastOneGroupIsWaiting || atLeastOneGroupIsApplied) && atLeastOneSuccessorIsWaiting) return WAITING;
			if (!atLeastOneSuccessorIsWaiting && atLeastOneGroupIsApplied) return APPLIED;
			return FAILED;
		}
	},
	IF_GROUPS_ANY_AND_SUCCESSORS_ANY {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			
			boolean atLeastOneSuccessorIsWaiting = false;
			boolean atLeastOneGroupIsWaiting = false;
			boolean atLeastOneSuccessorIsApplied = false;
			boolean atLeastOneGroupIsApplied = false;
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) {
					if (iMandatoryCheck.isGroup())
						atLeastOneGroupIsWaiting = true;
					else
						atLeastOneSuccessorIsWaiting = true;
				}
				if (state == APPLIED) {
					
					//GROUPS SUCCESSORS A W F  (AAA)(AAA) (AAA)(WWW) (AAA)(FFF) (AAA)(AFF) (AAA)(AWW) (AAA)(WFF) (AAA)(AWF)
					//GROUPS SUCCESSORS A W F  (WWW)(AAA) (WWW)(WWW) (WWW)(FFF) (WWW)(AFF) (WWW)(AWW) (WWW)(WFF) (WWW)(AWF)
					//GROUPS SUCCESSORS A W F  (FFF)(AAA) (FFF)(WWW) (FFF)(FFF) (FFF)(AFF) (FFF)(AWW) (FFF)(WFF) (FFF)(AWF)
					//GROUPS SUCCESSORS A W F  (AFF)(AAA) (AFF)(WWW) (AFF)(FFF) (AFF)(AFF) (AFF)(AWW) (AFF)(WFF) (AFF)(AWF)
					//GROUPS SUCCESSORS A W F  (AWW)(AAA) (AWW)(WWW) (AWW)(FFF) (AWW)(AFF) (AWW)(AWW) (AWW)(WFF) (AWW)(AWF)
					//GROUPS SUCCESSORS A W F  (WFF)(AAA) (WFF)(WWW) (WFF)(FFF) (WFF)(AFF) (WFF)(AWW) (WFF)(WFF) (WFF)(AWF)
					//GROUPS SUCCESSORS A W F  (AWF)(AAA) (AWF)(WWW) (AWF)(FFF) (AWF)(AFF) (AWF)(AWW) (AWF)(WFF) (AWF)(AWF)
					if (iMandatoryCheck.isGroup()) {
						if (atLeastOneSuccessorIsApplied) return APPLIED;
						atLeastOneGroupIsApplied = true;
					} else {
						if (atLeastOneGroupIsApplied) return APPLIED;
						atLeastOneSuccessorIsApplied = true;
					}
				}
			}
			
			//GROUPS SUCCESSORS A W F             (AAA)(WWW) (AAA)(FFF)                       (AAA)(WFF)
			//GROUPS SUCCESSORS A W F  (WWW)(AAA) (WWW)(WWW) (WWW)(FFF) (WWW)(AFF) (WWW)(AWW) (WWW)(WFF) (WWW)(AWF)
			//GROUPS SUCCESSORS A W F  (FFF)(AAA) (FFF)(WWW) (FFF)(FFF) (FFF)(AFF) (FFF)(AWW) (FFF)(WFF) (FFF)(AWF)
			//GROUPS SUCCESSORS A W F             (AFF)(WWW) (AFF)(FFF)                       (AFF)(WFF)
			//GROUPS SUCCESSORS A W F             (AWW)(WWW) (AWW)(FFF)                       (AWW)(WFF)
			//GROUPS SUCCESSORS A W F  (WFF)(AAA) (WFF)(WWW) (WFF)(FFF) (WFF)(AFF) (WFF)(AWW) (WFF)(WFF) (WFF)(AWF)
			//GROUPS SUCCESSORS A W F             (AWF)(WWW) (AWF)(FFF)                       (AWF)(WFF)
			if ((atLeastOneGroupIsWaiting || atLeastOneGroupIsApplied) && (atLeastOneSuccessorIsWaiting || atLeastOneSuccessorIsApplied))
				return WAITING;
			//GROUPS SUCCESSORS A W F                        (AAA)(FFF)
			//GROUPS SUCCESSORS A W F                        (WWW)(FFF)
			//GROUPS SUCCESSORS A W F  (FFF)(AAA) (FFF)(WWW) (FFF)(FFF) (FFF)(AFF) (FFF)(AWW) (FFF)(WFF) (FFF)(AWF)
			//GROUPS SUCCESSORS A W F                        (AFF)(FFF)
			//GROUPS SUCCESSORS A W F                        (AWW)(FFF)
			//GROUPS SUCCESSORS A W F                        (WFF)(FFF)
			//GROUPS SUCCESSORS A W F                        (AWF)(FFF)
			
			
			return FAILED;
		}
	},
	IF_GROUPS_ANY_OR_SUCCESSORS_ALL {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			
			boolean atLeastOneSuccessorIsWaiting = false;
			boolean atLeastOneGroupIsWaiting = false;
			boolean atLeastOneSuccessorIsFailed = false;
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsWaiting = true;
					} else {
						atLeastOneSuccessorIsWaiting = true;
					}
				}
				if (state == FAILED) {
					if (!iMandatoryCheck.isGroup()) {
						atLeastOneSuccessorIsFailed = true;
					}
				}
				if (state == APPLIED) {
					if (iMandatoryCheck.isGroup()) {
						return APPLIED;
					}
				}
			}
			if (!atLeastOneSuccessorIsWaiting && !atLeastOneSuccessorIsFailed) return APPLIED;
			if (!atLeastOneGroupIsWaiting && (!atLeastOneSuccessorIsWaiting || atLeastOneSuccessorIsFailed))
				return FAILED;
			return WAITING;
		}
	},
	IF_GROUPS_ALL_OR_SUCCESSORS_ANY {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			boolean atLeastOneSuccessorIsWaiting = false;
			boolean atLeastOneGroupIsWaiting = false;
			boolean atLeastOneGroupIsFailed = false;
			
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsWaiting = true;
					} else {
						atLeastOneSuccessorIsWaiting = true;
					}
				}
				if (state == FAILED) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsFailed = true;
					}
				}
				if (state == APPLIED) {
					if (!iMandatoryCheck.isGroup()) {
						//SUCCESSORS A W F  (AAA) (WWW) (FFF) (AFF) (AWW) (WFF) (AWF)
						return APPLIED;
						//SUCCESSORS A W F (WWW) (FFF) (WFF)
					}
				}
			}
			///GROUPS A W F  (AAA) (WWW) (FFF) (AFF) (AWW) (WFF) (AWF)
			if (!atLeastOneGroupIsWaiting && !atLeastOneGroupIsFailed) return APPLIED;
			///GROUPS A W F  (WWW) (FFF) (AFF) (AWW) (WFF) (AWF)
			if (!atLeastOneSuccessorIsWaiting && (!atLeastOneGroupIsWaiting || atLeastOneGroupIsFailed)) return FAILED;
			///GROUPS A W F  (WWW) (AWW)
			//SUCCESSORS A W F (WWW) (WFF)
			return WAITING;
		}
	},
	IF_GROUPS_ALL_OR_SUCCESSORS_ALL {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			boolean atLeastOneSuccessorIsWaiting = false;
			boolean atLeastOneGroupIsWaiting = false;
			boolean atLeastOneSuccessorIsFailed = false;
			boolean atLeastOneGroupIsFailed = false;
			
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) {
					if (iMandatoryCheck.isGroup()) {
						atLeastOneGroupIsWaiting = true;
					} else {
						atLeastOneSuccessorIsWaiting = true;
					}
				}
				if (state == FAILED) {
					//GROUPS SUCCESSORS A W F  (AAA)(AAA) (AAA)(WWW) (AAA)(FFF) (AAA)(AFF) (AAA)(AWW) (AAA)(WFF) (AAA)(AWF)
					//GROUPS SUCCESSORS A W F  (WWW)(AAA) (WWW)(WWW) (WWW)(FFF) (WWW)(AFF) (WWW)(AWW) (WWW)(WFF) (WWW)(AWF)
					//GROUPS SUCCESSORS A W F  (FFF)(AAA) (FFF)(WWW) (FFF)(FFF) (FFF)(AFF) (FFF)(AWW) (FFF)(WFF) (FFF)(AWF)
					//GROUPS SUCCESSORS A W F  (AFF)(AAA) (AFF)(WWW) (AFF)(FFF) (AFF)(AFF) (AFF)(AWW) (AFF)(WFF) (AFF)(AWF)
					//GROUPS SUCCESSORS A W F  (AWW)(AAA) (AWW)(WWW) (AWW)(FFF) (AWW)(AFF) (AWW)(AWW) (AWW)(WFF) (AWW)(AWF)
					//GROUPS SUCCESSORS A W F  (WFF)(AAA) (WFF)(WWW) (WFF)(FFF) (WFF)(AFF) (WFF)(AWW) (WFF)(WFF) (WFF)(AWF)
					//GROUPS SUCCESSORS A W F  (AWF)(AAA) (AWF)(WWW) (AWF)(FFF) (AWF)(AFF) (AWF)(AWW) (AWF)(WFF) (AWF)(AWF)
					if (iMandatoryCheck.isGroup()) {
						if (atLeastOneSuccessorIsFailed) return FAILED;
						atLeastOneGroupIsFailed = true;
					} else {
						if (atLeastOneGroupIsFailed) return FAILED;
						atLeastOneSuccessorIsFailed = true;
					}
					//GROUPS SUCCESSORS A W F  (AAA)(AAA) (AAA)(WWW) (AAA)(FFF) (AAA)(AFF) (AAA)(AWW) (AAA)(WFF) (AAA)(AWF)
					//GROUPS SUCCESSORS A W F  (WWW)(AAA) (WWW)(WWW) (WWW)(FFF) (WWW)(AFF) (WWW)(AWW) (WWW)(WFF) (WWW)(AWF)
					//GROUPS SUCCESSORS A W F  (FFF)(AAA) (FFF)(WWW)                       (FFF)(AWW)
					//GROUPS SUCCESSORS A W F  (AFF)(AAA) (AFF)(WWW)                       (AFF)(AWW)
					//GROUPS SUCCESSORS A W F  (AWW)(AAA) (AWW)(WWW) (AWW)(FFF) (AWW)(AFF) (AWW)(AWW) (AWW)(WFF) (AWW)(AWF)
					//GROUPS SUCCESSORS A W F  (WFF)(AAA) (WFF)(WWW)                       (WFF)(AWW)
					//GROUPS SUCCESSORS A W F  (AWF)(AAA) (AWF)(WWW)                       (AWF)(AWW)
				}
			}
			
			if ((!atLeastOneSuccessorIsWaiting && !atLeastOneSuccessorIsFailed) || (!atLeastOneGroupIsWaiting && !atLeastOneGroupIsFailed))
				return APPLIED;
			
			//GROUPS SUCCESSORS A W F
			//GROUPS SUCCESSORS A W F             (WWW)(WWW) (WWW)(FFF) (WWW)(AFF) (WWW)(AWW) (WWW)(WFF) (WWW)(AWF)
			//GROUPS SUCCESSORS A W F             (FFF)(WWW)                       (FFF)(AWW)
			//GROUPS SUCCESSORS A W F             (AFF)(WWW)                       (AFF)(AWW)
			//GROUPS SUCCESSORS A W F             (AWW)(WWW) (AWW)(FFF) (AWW)(AFF) (AWW)(AWW) (AWW)(WFF) (AWW)(AWF)
			//GROUPS SUCCESSORS A W F             (WFF)(WWW)                       (WFF)(AWW)
			//GROUPS SUCCESSORS A W F             (AWF)(WWW)                       (AWF)(AWW)
			
			return WAITING;
		}
	},
	IF_ANY // default
		{
			@Override
			public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
				if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
				boolean atLeastOneIsWaiting = false;
				for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
					CheckState state = iMandatoryCheck.check();
					if (state == APPLIED) return APPLIED;
					if (state == WAITING) atLeastOneIsWaiting = true;
				}
				if (atLeastOneIsWaiting) return WAITING;
				return FAILED;
			}
		},
	IF_ALL {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			boolean atLeastOneIsWaiting = false;
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) atLeastOneIsWaiting = true;
				if (state == FAILED) return FAILED;
			}
			if (atLeastOneIsWaiting) return WAITING;
			return APPLIED;
		}
	},
	IF_GROUPS_ANY_OR_SUCCESSORS_ANY {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			return IF_ANY.test(mandatoryChecks);
		}
	},
	IF_GROUPS_ALL_AND_SUCCESSORS_ALL {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			return IF_ALL.test(mandatoryChecks);
		}
	},
	IF_ALL_OR_NONE {
		@Override
		public CheckState test(Set<IMandatoryCheck> mandatoryChecks) {
			if (mandatoryChecks == null || mandatoryChecks.isEmpty()) return FAILED;
			boolean atLeastOneFailed = false;
			boolean atLeastOneApplied = false;
			boolean atLeastOneIsWaiting = false;
			for (IMandatoryCheck iMandatoryCheck : mandatoryChecks) {
				CheckState state = iMandatoryCheck.check();
				if (state == WAITING) atLeastOneIsWaiting = true;
				if (state == FAILED) atLeastOneFailed = true;
				if (state == APPLIED) atLeastOneApplied = true;
				if (atLeastOneApplied && atLeastOneFailed) return FAILED;
			}
			if (atLeastOneIsWaiting) return WAITING;
			return APPLIED;
		}
	};
	
	
	public abstract CheckState test(Set<IMandatoryCheck> mandatoryChecks);
}
