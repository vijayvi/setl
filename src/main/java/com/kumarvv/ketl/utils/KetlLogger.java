package com.kumarvv.ketl.utils;

import com.kumarvv.ketl.model.Def;
import org.apache.log4j.Logger;

import javax.sql.RowSet;
import javax.sql.rowset.JdbcRowSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static com.kumarvv.ketl.utils.Constants.*;
import static com.kumarvv.ketl.utils.Constants.SQUARE_CLOSE;
import static com.kumarvv.ketl.utils.Constants.SQUARE_OPEN;

public class KetlLogger {

	private Logger log;

	private KetlLogger() {
	}

	public static KetlLogger getLogger(Class<?> clazz) {
		KetlLogger logger = new KetlLogger();
		logger.log = Logger.getLogger(clazz);
		return logger;
	}

	protected String buildMsg(Def def, String...args) {
		StringBuilder sb = new StringBuilder();
		if (def != null) {
			sb.append(SQUARE_OPEN).append(def.getName()).append(SQUARE_CLOSE);
		}
		if (args != null && args.length > 0) {
			for (String s : args) {
				sb.append(SPACE).append(s);
			}
		}
		return sb.toString();
	}

	public void trace(Def def, Throwable t) {
		if (log.isTraceEnabled()) {
			log.trace(buildMsg(def, t.getMessage()));
			log.trace(t);
		}
	}

	public void trace(Def def, String...args) {
		if (log.isTraceEnabled()) {
			log.trace(buildMsg(def, args));
		}
	}

	public void trace(String...args) {
		trace(null, args);
	}

	public void debug(Def def, Throwable t) {
		if (log.isDebugEnabled()) {
			log.debug(buildMsg(def, t.getMessage()));
			log.debug(t);
		}
	}

	public void debug(Def def, String...args) {
		if (log.isDebugEnabled()) {
			log.debug(buildMsg(def, args));
		}
	}

	public void debug(String...args) {
		debug(null, args);
	}

	public void info(Def def, String...args) {
		if (log.isInfoEnabled()) {
			log.info(buildMsg(def, args));
		}
	}

	public void info(String...args) {
		info(null, args);
	}

	public void warn(Def def, String...args) {
		log.warn(buildMsg(def, args));
	}

	public void warn(String...args) {
		warn(null, args);
	}

	public void error(Def def, Throwable t) {
		log.error(buildMsg(def, t.getMessage()));
		log.error(t);
	}

	public void error(Def def, String...args) {
		log.error(buildMsg(def, args));
	}

	public void error(String...args) {
		error(null, args);
	}

	public void error(Throwable t) {
		log.error(t);
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	public void traceRowData(RowSet jrs) {
		if (log.isTraceEnabled()) {
			log.trace(formatRowData(jrs));
		}
	}

	public void debugRowData(RowSet jrs) {
		if (log.isDebugEnabled()) {
			log.debug(formatRowData(jrs)); 
		}
	}

	public String formatRowData(RowSet jrs) { 
		StringBuilder rowStr = new StringBuilder(CURLY_OPEN);
		if (jrs != null) { 
			try {
				ResultSetMetaData meta = ((JdbcRowSet) jrs).getMetaData();
				if (meta != null) { 
					for (int col = 1; col <= meta.getColumnCount(); col++) {
						rowStr.append(jrs.getObject(col)); 
						if (col != meta.getColumnCount()) { 
							rowStr.append(Constants.COMMA);
						}
					}
				}
			} catch (SQLException e) { // ignore
			}
		}
		rowStr.append(Constants.CURLY_CLOSE);
		return rowStr.toString();
	}
}
