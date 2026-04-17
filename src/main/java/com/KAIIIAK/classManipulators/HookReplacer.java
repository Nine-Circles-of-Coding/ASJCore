package com.KAIIIAK.classManipulators;

public @interface HookReplacer {
	
	String targetMethod() default "";
	
	boolean correctStaticIndexes() default false;
	
	/**
	 * matchIndex values logic:
	 * if (matchIndex == {}) - replace all match
	 * if (matchIndex == {1, 3}) - replace first and third found
	 */
	int[] matchIndex() default {};
	
	/**
	 * earlier < 0 < later
	 */
	int priority() default 0;
	
	boolean isMandatory() default true;
	
	/**
	 * if (removePop) start(); someMethodWithReturn(); stop(); is equivalent to start(); POP(someMethodWithReturn()); stop();
	 */
	boolean removePop() default false;
	
	boolean ignoreLines() default true;
	
	boolean ignoreLabels() default true;
	
	@SuppressWarnings("unused")
	class Replacer {
		
		//zone control methods
		public static void startFROM() {}
		
		public static void startTO() {}
		
		public static void stop() {}
		
		//used to remove InsnNode.opcode == POP from call list("from" and "to" lists)
		public static void POP(Object obj) {}
		
		public static void POP(byte obj) {}
		
		public static void POP(short obj) {}
		
		public static void POP(int obj) {}
		
		public static void POP(long obj) {}
		
		public static void POP(float obj) {}
		
		public static void POP(double obj) {}
		
		public static void POP(boolean obj) {}
		
		public static void POP(char obj) {}
		
		//used to remove LineNumberNode from call list("from" and "to" lists)
		public static void POPLine() {}
		
		//used to make a corresponding LOAD call from var index
		public static int ILOAD(String var) {
			return 0;
		}
		
		public static long LLOAD(String var) {
			return 0L;
		}
		
		public static float FLOAD(String var) {
			return 0f;
		}
		
		public static double DLOAD(String var) {
			return 0d;
		}
		
		public static <T> T ALOAD(String var) {
			return null;
		}
		
		//used to make a corresponding STORE call for var index
		public static void ISTORE(String var) {}
		
		public static void LSTORE(String var) {}
		
		public static void FSTORE(String var) {}
		
		public static void DSTORE(String var) {}
		
		public static void ASTORE(String var) {}
	}
}