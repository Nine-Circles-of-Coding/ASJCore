package com.KAIIIAK.classManipulators;

public @interface HookReplacer {
	String targetMethod() default "";
	
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
	}
}
