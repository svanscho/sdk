package com.openmotics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by svanscho on 11/12/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@Setter
public class Installation {
    private String id;
    private String name;
    private String role;
    private String version;
}
