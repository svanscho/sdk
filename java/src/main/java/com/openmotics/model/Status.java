package com.openmotics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

/**
 * Created by svanscho on 11/12/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Status {
    public String time;
    public String date;
    public Integer mode;
    public String version;
    @JsonProperty("hw_version")
    public Integer hwVersion;

}
