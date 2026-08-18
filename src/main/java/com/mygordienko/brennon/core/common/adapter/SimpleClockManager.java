package com.mygordienko.brennon.core.common.adapter;

import java.time.Instant;

import com.mygordienko.brennon.core.common.ClockManager;

public class SimpleClockManager implements ClockManager {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
