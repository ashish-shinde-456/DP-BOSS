package com.ashish.resultswebsite.rajdhani;

import java.time.LocalTime;

public enum RajdhaniMarket {
    RAJDHAN_DAY("RAJSHREE DAY", LocalTime.of(15, 15), LocalTime.of(17, 15)),
    RAJDHAN_NIGHT("RAJSHREE NIGHT", LocalTime.of(21, 35), LocalTime.of(23, 45));

    private final String displayName;
    private final LocalTime openTime;
    private final LocalTime closeTime;

    RajdhaniMarket(String displayName, LocalTime openTime, LocalTime closeTime) {
        this.displayName = displayName;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }
}
