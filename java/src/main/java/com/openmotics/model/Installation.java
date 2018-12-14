package com.openmotics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

/**
 * Created by svanscho on 11/12/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Installation {
    public String id;
    public String name;
    public String role;
    public String version;
}
