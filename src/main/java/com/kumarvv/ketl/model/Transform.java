package com.kumarvv.ketl.model;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "columns"
})
public class Transform {

    @JsonProperty("columns")
    private Set<Column> columns = new HashSet<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The columns
     */
    @JsonProperty("columns")
    public Set<Column> getColumns() {
        return columns;
    }

    /**
     *
     * @param columns
     * The columns
     */
    @JsonProperty("columns")
    public void setColumns(Set<Column> columns) {
        this.columns = columns;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}