package com.openmotics.api;

import lombok.Getter;

/**
 * Created by svanscho on 12/12/2018.
 */
public enum Action {
    ERRORS ("get_errors"),
    ERRORS_CLEAR ("master_clear_error_list"),
    GROUP_ACTION ("do_group_action"),
    INSTALLATIONS ("get_installations"),
    INPUT_GET_LAST ("get_last_inputs"),
    LEDS_FLASH ("flash_leds"),
    LIGHTS_ALL_OFF ("set_all_lights_off"),
    LIGHTS_FLOOR_OFF ("set_all_lights_floor_off"),
    LIGHTS_FLOOR_ON ("set_all_lights_floor_on"),
    LOGIN ("login"),
    MASTER_RESET ("reset_master"),
    MODULE_DISCOVER_START ("module_discover_start"),
    MODULE_DISCOVER_STOP ("module_discover_stop"),
    MODULES_GET ("get_modules"),
    OUTPUT_SET ("set_output"),
    OUTPUT_STATUS ("get_output_status"),
    POWER_MODULE_VOLTAGE ("set_power_voltage"),
    POWER_MODULES_GET ("get_power_modules"),
    POWER_MODULES_SET ("set_power_modules"),
    POWER_REALTIME_GET ("get_realtime_power"),
    POWER_TOTAL_ENERGY ("get_total_energy"),
    PULSE_COUNTER_STATUS ("get_pulse_counter_status"),
    THERMOSTAT_STATUS ("get_thermostat_status"),
    THERMOSTAT_SETPOINT ("set_current_setpoint"),
    THERMOSTAT_MODE ("set_thermostat_mode"),
    SENSOR_BRIGHTNESS_STATUS ("get_sensor_brightness_status"),
    SENSOR_HUMIDITY_STATUS ("get_sensor_humidity_status"),
    SENSOR_TEMPERATURE_STATUS ("get_sensor_temperature_status"),
    STATUS ("get_status"),
    VERSION ("get_version");

    @Getter
    private final String path;

    Action(String s) {
        path = s;
    }

    public boolean equals(String otherAction) {
        // null check is not needed because name.equals(null) returns false
        return path.equals(otherAction);
    }

    public boolean equals(Action otherAction) {
        // null check is not needed because name.equals(null) returns false
        return path.equals(otherAction.path);
    }

    public String toString() {
        return this.path;
    }
}