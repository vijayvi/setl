package com.kumarvv.ketl.model;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
public class Data implements Serializable {
    @JsonIgnore
    private Map<String, Object> values = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getValues() {
        return this.values;
    }

    @JsonAnySetter
    public void setValue(String name, Object value) {
        this.values.put(name, value);
    }

    @JsonIgnore
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
}
