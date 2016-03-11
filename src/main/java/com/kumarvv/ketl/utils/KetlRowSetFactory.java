package com.kumarvv.ketl.utils;

import com.kumarvv.ketl.model.DS;

import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.SQLException;

public class KetlRowSetFactory {

	private static final KetlLogger log = KetlLogger.getLogger(KetlRowSetFactory.class);

	private RowSetFactory rowSetFactory;

	/**
	 * instance
	 */
	public static KetlRowSetFactory getInstance() {
		return new KetlRowSetFactory();
	}

	/**
	 * @return builds and returns source row set
	 */
	public JdbcRowSet getRowSet(DS ds) {
		if (ds == null) {
			return null;
		}

		try {
			if (rowSetFactory == null) {
				rowSetFactory = RowSetProvider.newFactory();
			}

			JdbcRowSet jrs = rowSetFactory.createJdbcRowSet();
			jrs.setUrl(ds.getUrl());
			jrs.setUsername(ds.getUsername());
			jrs.setPassword(ds.getPassword());

			return jrs;
		} catch (SQLException e) {
			log.error(" rowset:", e.getMessage());
		}
		return null;
	}

}
