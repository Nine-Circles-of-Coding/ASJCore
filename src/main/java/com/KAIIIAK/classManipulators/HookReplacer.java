package com.KAIIIAK.classManipulators;

public @interface HookReplacer {
	
	String targetMethod() default "";
	boolean correctStaticIndexes() default false;
	
	@SuppressWarnings("unused")
	class Replacer {
		public static void POP(Object obj) {}
		
		public static void POP(byte obj) {}
		
		public static void POP(short obj) {}
		
		public static void POP(int obj) {}
		
		public static void POP(long obj) {}
		
		public static void POP(float obj) {}
		
		public static void POP(double obj) {}
		
		public static void POP(boolean obj) {}
		
		public static void POP(char obj) {}
		
		public static void POPLine() {}
		
		public static void startFROM() {}
		
		public static void startTO() {}
		
		public static void stop() {}
		
		public static int ILOAD(String var) { return 0; }
		
		public static long LLOAD(String var) { return 0L; }
		
		public static float FLOAD(String var) { return 0f; }
		
		public static double DLOAD(String var) { return 0d; }
		
		public static <T> T ALOAD(String var) { return null; }
		
		public static void ISTORE(String var) {}
		
		public static void LSTORE(String var) {}
		
		public static void FSTORE(String var) {}
		
		public static void DSTORE(String var) {}
		
		public static void ASTORE(String var) {}
	}
}
