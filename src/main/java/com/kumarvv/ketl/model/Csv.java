package com.kumarvv.ketl.model;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "file",
        "columns"
})
public class Csv {

    @JsonProperty("file")
    private String file;
    @JsonProperty("columns")
    private List<String> columns = new ArrayList<>();
    @JsonIgnore
    private String filePath;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The file
     */
    @JsonProperty("file")
    public String getFile() {
        return file;
    }

    /**
     *
     * @param file
     * The file
     */
    @JsonProperty("file")
    public void setFile(String file) {
        this.file = file;
    }

    /**
     *
     * @return
     * The columns
     */
    @JsonProperty("columns")
    public List<String> getColumns() {
        return columns;
    }

    /**
     *
     * @param columns
     * The columns
     */
    @JsonProperty("columns")
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    /**
     * file path
     * @return
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * file path
     * @return
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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