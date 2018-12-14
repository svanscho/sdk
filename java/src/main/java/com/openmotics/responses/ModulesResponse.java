package com.openmotics.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by svanscho on 11/12/2018.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ModulesResponse {
    private List<String> outputs;
    private List<String> shutters;
    private List<String> inputs;
    @JsonProperty("can_inputs")
    private List<String> canInputs;
    private Boolean success;
}
