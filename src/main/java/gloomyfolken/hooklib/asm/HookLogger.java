package gloomyfolken.hooklib.asm;

import alexsocol.patcher.PatcherPreConfigHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface HookLogger {
	
	void trace(String message);
	
	void debug(String message);
	
	void warning(String message);
	
	void severe(String message);
	
	void severe(String message, Throwable cause);
	
	default void error(String message) {
		severe(message);
	}
	
	default void error(String message, Throwable cause) {
		severe(message, cause);
	}
	
	class SystemOutLogger implements HookLogger {
		
		@Override
		public void trace(String message) {
			if (PatcherPreConfigHandler.INSTANCE.getLogTrace())
				System.out.println("[TRACE] " + message);
		}
		
		@Override
		public void debug(String message) {
			if (PatcherPreConfigHandler.INSTANCE.getLogDebug())
				System.out.println("[DEBUG] " + message);
		}
		
		@Override
		public void warning(String message) {
			System.out.println("[WARNING] " + message);
		}
		
		@Override
		public void severe(String message) {
			System.out.println("[SEVERE] " + message);
		}
		
		@Override
		public void severe(String message, Throwable cause) {
			severe(message);
			cause.printStackTrace();
		}
	}
	
	class VanillaLogger implements HookLogger {
		
		private java.util.logging.Logger logger;
		
		public VanillaLogger(java.util.logging.Logger logger) {
			this.logger = logger;
		}
		
		@Override
		public void trace(String message) {
			if (PatcherPreConfigHandler.INSTANCE.getLogTrace())
				logger.finest(message);
		}
		
		@Override
		public void debug(String message) {
			if (PatcherPreConfigHandler.INSTANCE.getLogDebug())
				logger.fine(message);
		}
		
		@Override
		public void warning(String message) {
			logger.warning(message);
		}
		
		@Override
		public void severe(String message) {
			logger.severe(message);
		}
		
		@Override
		public void severe(String message, Throwable cause) {
			logger.log(java.util.logging.Level.SEVERE, message, cause);
		}
	}
	
	class Log4JLogger implements HookLogger {
		
		private final Logger logger;
		
		public Log4JLogger(String loggerName) {
			logger = LogManager.getLogger(loggerName);
		}
		
		@Override
		public void trace(String message) {
			if (PatcherPreConfigHandler.INSTANCE.getLogTrace())
				// piece of inconfigurable shit
				logger.info(message);
		}
		
		@Override
		public void debug(String message) {
			if (PatcherPreConfigHandler.INSTANCE.getLogDebug())
				// piece of inconfigurable shit
				logger.info(message);
		}
		
		@Override
		public void warning(String message) {
			logger.warn(message);
		}
		
		@Override
		public void severe(String message) {
			logger.error(message);
		}
		
		@Override
		public void severe(String message, Throwable cause) {
			logger.error(message, cause);
		}
	}
}