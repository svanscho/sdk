package com.openmotics.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.openmotics.model.OutputStatus;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by svanscho on 11/12/2018.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class OutputStatusResponse {
    private List<OutputStatus> status;
    private Boolean success;
}
