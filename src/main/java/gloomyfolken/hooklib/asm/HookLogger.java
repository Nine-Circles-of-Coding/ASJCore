package gloomyfolken.hooklib.asm;

import alexsocol.patcher.PatcherConfigHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public interface HookLogger {
	
	void debug(String message);
	
	void warning(String message);
	
	void error(String message);
	
	void error(String message, Throwable cause);
	
	class SystemOutLogger implements HookLogger {
		
		private String tag;
		private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		public SystemOutLogger(String tag) {
			this.tag = tag;
		}
		
		@Override
		public void debug(String message) {
			if (PatcherConfigHandler.INSTANCE.getLogDebug())
				System.out.printf("[%s] [%s/DEBUG] [%s]: %s%n", sdf.format(new Date()), thread(), tag, message);
		}
		
		@Override
		public void warning(String message) {
			System.out.printf("[%s] [%s/WARNING] [%s]: %s%n", sdf.format(new Date()), thread(), tag, message);
		}
		
		@Override
		public void error(String message) {
			System.err.printf("[%s] [%s/ERROR] [%s]: %s%n", sdf.format(new Date()), thread(), tag, message);
		}
		
		@Override
		public void error(String message, Throwable cause) {
			error(message);
			cause.printStackTrace();
		}
		
		private String thread() {
			return Thread.currentThread().getName();
		}
	}
	
	class VanillaLogger implements HookLogger {
		
		private Logger logger;
		
		public VanillaLogger(Logger logger) {
			this.logger = logger;
		}
		
		@Override
		public void debug(String message) {
			logger.fine(message);
		}
		
		@Override
		public void warning(String message) {
			logger.warning(message);
		}
		
		@Override
		public void error(String message) {
			logger.severe(message);
		}
		
		@Override
		public void error(String message, Throwable cause) {
			logger.log(Level.SEVERE, message, cause);
		}
	}
}
