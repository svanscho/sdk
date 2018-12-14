package com.openmotics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

/**
 * Created by svanscho on 11/12/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ThermostatStatus {
    public String id;
    public String act;
    public String csetp;
    public String output0;
    public String output1;
    public String outside;
    public String mode;
}
