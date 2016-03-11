package com.kumarvv.ketl.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpolator {

    public static Interpolator getInstance() {
        return new Interpolator();
    }

    public String interpolate(final String str, final Map<String, Object> values) {
        return interpolate(str, Constants.COLON, values);
    }

    public String interpolate(final String str, final String prefix, final Map<String, Object> values) {
        String istr = str;
        for(String col : values.keySet()) {
            istr = interpolate(istr, prefix + col, values.get(col));
        }
        return istr.trim();
    }

    public String interpolate(final String str, final String key, final Object value) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        String istr = str;
        Pattern p = Pattern.compile(key + "\\b");
        Matcher m = p.matcher(istr);
        istr = m.replaceAll(toInterpolateValue(value));
        return istr;
    }

    protected String toInterpolateValue(Object v) {
        if (v == null) {
            return "null";
        }
        if (v instanceof String ||
                v instanceof Date ||
                v instanceof java.util.Date ||
                v instanceof Timestamp) {
            return Constants.QUOTE_SINGLE + v.toString() + Constants.QUOTE_SINGLE;
        }
        return v.toString();
    }
}
