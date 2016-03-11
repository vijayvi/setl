package com.kumarvv.ketl.enums;

import java.sql.Types;

public enum SqlType {
    BIT(Types.BIT),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    REAL(Types.REAL),
    DOUBLE(Types.DOUBLE),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    CHAR(Types.CHAR),
    VARCHAR(Types.VARCHAR),
    LONGVARCHAR(Types.LONGVARCHAR),
    DATE(Types.DATE),
    TIME(Types.TIME),
    TIMESTAMP(Types.TIMESTAMP),
    BINARY(Types.BINARY),
    VARBINARY(Types.VARBINARY),
    LONGVARBINARY(Types.LONGVARBINARY),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    JAVA_OBJECT(Types.JAVA_OBJECT),
    DISTINCT(Types.DISTINCT),
    STRUCT(Types.STRUCT),
    ARRAY(Types.ARRAY),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    REF(Types.REF),
    DATALINK(Types.DATALINK),
    BOOLEAN(Types.BOOLEAN),
    ROWID(Types.ROWID),
    NCLOB(Types.NCLOB),
    SQLXML(Types.SQLXML);

    SqlType(int sqlTypeInt) {
        this.sqlTypeInt = sqlTypeInt;
    }

    private int sqlTypeInt;

    public static SqlType forValue(int sqlTypeInt) {
        for (SqlType st : values()) {
            if (st.sqlTypeInt == sqlTypeInt) {
                return st;
            }
        }
        return null;
    }

    public Object convert(Object val) {
        if (val == null) {
            return null;
        }
        String strVal = val.toString(); 

        if (sqlTypeInt == Types.BIT || sqlTypeInt == Types.TINYINT || sqlTypeInt == Types.SMALLINT || sqlTypeInt == Types.INTEGER) {
            return Integer.valueOf(strVal);
        } else if (sqlTypeInt == Types.BIGINT) {
            return Long.valueOf(strVal);
        } else if (sqlTypeInt == Types.FLOAT || sqlTypeInt == Types.REAL) {
            return Float.valueOf(strVal);
        } else if (sqlTypeInt == Types.DOUBLE || sqlTypeInt == Types.NUMERIC || sqlTypeInt == Types.DECIMAL) {
            return Double.valueOf(strVal);
        } else if (sqlTypeInt == Types.CHAR) {
            return strVal.charAt(0);
        }
        return strVal;
    }
}
