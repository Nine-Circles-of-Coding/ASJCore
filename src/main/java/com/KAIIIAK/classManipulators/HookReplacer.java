package com.KAIIIAK.classManipulators;

import java.lang.annotation.Repeatable;

@SuppressWarnings("unused")
public @interface HookReplacer {
	
	String targetClass() default "";
	
	String targetMethod() default "";
	
	boolean correctStaticIndexes() default false;
	
	/**
	 * onlyNthMatches values logic:
	 * if (onlyNthMatches == {}) - replace all match
	 * if (onlyNthMatches == {1, 3}) - replace first and third found
	 */
	int[] onlyNthMatches() default {};
	
	/**
	 * earlier < 0 < later
	 */
	int priority() default 0;
	
	boolean isMandatory() default true;
	
	String[] mandatoryGroups() default {};
	
	/**
	 * if (removePop) start(); someMethodWithReturn(); stop(); is equivalent to start(); POP(someMethodWithReturn()); stop();
	 */
	boolean removePop() default false;
	
	boolean ignoreLines() default true;
	
	boolean ignoreLabels() default true;
	
	@interface HookGroups {
		
		CreateHRG[] value();
	}
	
	@Repeatable(HookGroups.class)
	@interface CreateHRG {
		
		String name();
		
		MandatoryType type() default MandatoryType.IF_ANY;
		
		String[] included() default {};
		
		boolean optional() default false; // if true - will not crash by itself, but may be a reason to crash group that include this one...
	}
	
	
	@SuppressWarnings("unused")
	class Replacer {
		
		// zone control methods
		public static void startFROM() {}
		
		public static void startTO() {}
		
		public static void stop() {}
		
		// used to remove InsnNode.opcode == POP from call list("from" and "to" lists)
		public static void POP(Object obj) {}
		
		public static void POP(byte obj) {}
		
		public static void POP(short obj) {}
		
		public static void POP(int obj) {}
		
		public static void POP(long obj) {}
		
		public static void POP(float obj) {}
		
		public static void POP(double obj) {}
		
		public static void POP(boolean obj) {}
		
		public static void POP(char obj) {}
		
		// used to remove LineNumberNode from call list("from" and "to" lists)
		public static void POPLine() {}
		
		// used to make a corresponding LOAD call from var index
		public static int    ILOAD(String loadIndexLDC) { return 0; }
		
		public static long   LLOAD(String loadIndexLDC) { return 0L; }
		
		public static float  FLOAD(String loadIndexLDC) { return 0f; }
		
		public static double DLOAD(String loadIndexLDC) { return 0d; }
		
		public static <T> T  ALOAD(String loadIndexLDC) { return null; }
		
		// used to make a corresponding STORE call for var index
		public static void ISTORE(String storeIndexLDC) {}
		
		public static void LSTORE(String storeIndexLDC) {}
		
		public static void FSTORE(String storeIndexLDC) {}
		
		public static void DSTORE(String storeIndexLDC) {}
		
		public static void ASTORE(String storeIndexLDC) {}

		// used to inject return opcodes 
		public static void IRETURN() {}

		public static void LRETURN() {}

		public static void FRETURN() {}

		public static void DRETURN() {}

		public static void ARETURN() {}

		public static void RETURN() {}

		// calls to these methods will be removed
		public static void   SKIP()  {  }

		public static int    ISKIP() { return 0; }

		public static long   LSKIP() { return 0L; }

		public static float  FSKIP() { return 0f; }

		public static double DSKIP() { return 0d; }

		public static <T> T  ASKIP() { return null; }



		public static void   ANY()  {  }

		public static int    IANY() { return 0; }

		public static long   LANY() { return 0L; }

		public static float  FANY() { return 0f; }

		public static double DANY() { return 0d; }

		public static <T> T  AANY() { return null; }



		public static void   CAPTURE (String captureIndexLDC) {  }

		public static int    ICAPTURE(String captureIndexLDC) { return 0; }

		public static long   LCAPTURE(String captureIndexLDC) { return 0L; }

		public static float  FCAPTURE(String captureIndexLDC) { return 0f; }

		public static double DCAPTURE(String captureIndexLDC) { return 0d; }

		public static <T> T  ACAPTURE(String captureIndexLDC) { return null; }


		public static void   INVOKESPECIAL(String ownerNameWithSlashes) { }
	}
}