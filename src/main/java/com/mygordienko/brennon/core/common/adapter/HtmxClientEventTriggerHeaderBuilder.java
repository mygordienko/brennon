package com.mygordienko.brennon.core.common.adapter;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Helps building JSON payload to use with `HX-Trigger` response header to trigger
 * custom event on the client.
 * 
 * Single header is used for one or more events:
 * 
 * {
 *  "eventA" : {"value" : "actual value A"},
 *  "eventB" : {"value" : "actual value B"}
 * }
 * 
 */
public class HtmxClientEventTriggerHeaderBuilder {
    private final Map<String, Value> header = new HashMap<>();

    public void addVersionUpdatedEvent(int newVersion) {
        addEvent(CustomHtmxEvent.VERSION_UPDATED.getEventName(), newVersion);
    }

    private void addEvent(String name, Object value) {
        header.put(name, new Value(value));
    }

    public String toJsonString() {
        return new JsonMapper().writeValueAsString(this.header);
    }

    public static final class Value {
        public Value(Object value) {
            this.value = value;
        }

        @JsonAlias("value")
        @Getter
        private final Object value;
    }

    enum CustomHtmxEvent {
        VERSION_UPDATED("versionUpdated");

        private CustomHtmxEvent(String name) {
            this.name = name;
        }

        private final String name;
        
        public String getEventName() {
            return name;
        }
    }
}
