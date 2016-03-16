/**
 * Copyright (c) 2016 Vijay Vijayaram
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.kumarvv.setl.utils;

import com.kumarvv.setl.model.DS;
import org.pmw.tinylog.Logger;

import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RowSetUtil {

    protected RowSetFactory rowSetFactory;

    public static RowSetUtil getInstance() {
        return new RowSetUtil();
    }

    /**
     * @return builds and returns source row set
     */
    public JdbcRowSet getRowSet(DS ds) throws SQLException {
        if (ds == null) {
            throw new SQLException("Invalid DS");
        }

        if (rowSetFactory == null) {
            rowSetFactory = RowSetProvider.newFactory();
        }

        JdbcRowSet jrs = rowSetFactory.createJdbcRowSet();
        jrs.setUrl(ds.getUrl());
        jrs.setUsername(ds.getUsername());
        jrs.setPassword(ds.getPassword());

        return jrs;
    }

    /**
     * get meta columns list with columnId
     *
     * @param meta
     * @return
     */
    public Map<String, Integer> getMetaColumns(ResultSetMetaData meta) {
        final Map<String, Integer> metaColumns = new HashMap<>();
        if (meta == null) {
            return metaColumns;
        }
        try {
            int colCount = meta.getColumnCount();
            for (int c = 1; c <= colCount; c++) {
                metaColumns.put(meta.getColumnName(c), meta.getColumnType(c));
            }
        } catch (SQLException sqle) {
            Logger.error("error getting metaColumns:", sqle.getMessage());
            Logger.trace(sqle);
        }

        return metaColumns;
    }

    /**
     * get meta columns
     *
     * @param jrs
     * @return
     */
    public Map<String, Integer> getMetaColumns(JdbcRowSet jrs) {
        if (jrs == null) {
            return new HashMap<>();
        }

        try {
            return getMetaColumns(jrs.getMetaData());
        } catch (SQLException sqle) {
            Logger.error("error getting metaColumns:", sqle.getMessage());
            Logger.trace(sqle);
            return new HashMap<>();
        }
    }
}
