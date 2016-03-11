package com.kumarvv.ketl.model;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "sql",
        "csv"
})
public class PrePost {

    @JsonProperty("sql")
    private String sql;
    @JsonProperty("csv")
    private Csv csv;
    @JsonProperty("data")
    private List<Map<String, Object>> data;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The sql
     */
    @JsonProperty("sql")
    public String getSql() {
        return sql;
    }

    /**
     *
     * @param sql
     * The sql
     */
    @JsonProperty("sql")
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     *
     * @return
     * The csv
     */
    @JsonProperty("csv")
    public Csv getCsv() {
        return csv;
    }

    /**
     *
     * @param csv
     * The csv
     */
    @JsonProperty("csv")
    public void setCsv(Csv csv) {
        this.csv = csv;
    }

    /**
     * data
     * @return
     */
    @JsonProperty("data")
    public List<Map<String, Object>> getData() {
        return data;
    }

    /**
     * @param data
     */
    @JsonProperty("data")
    public void setData(List<Map<String, Object>> data) {
        this.data = data;
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