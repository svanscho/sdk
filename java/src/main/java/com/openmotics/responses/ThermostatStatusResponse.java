package com.openmotics.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openmotics.model.ThermostatStatus;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Created by svanscho on 11/12/2018.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ThermostatStatusResponse {
    @JsonProperty("thermostats_on")
    private Boolean thermostatsOn;
    private Boolean automatic;
    private Boolean cooling;
    private Integer setpoint;
    private List<ThermostatStatus> status;
    private Boolean success;
}
