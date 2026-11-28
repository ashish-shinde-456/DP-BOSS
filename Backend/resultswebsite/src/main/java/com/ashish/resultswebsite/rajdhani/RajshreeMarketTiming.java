package com.ashish.resultswebsite.rajdhani;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "rajshree_market_timings")
public class RajshreeMarketTiming {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "market_code", length = 32)
    private RajdhaniMarket market;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by_phone", length = 10)
    private String updatedByPhone;

    public RajdhaniMarket getMarket() {
        return market;
    }

    public void setMarket(RajdhaniMarket market) {
        this.market = market;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedByPhone() {
        return updatedByPhone;
    }

    public void setUpdatedByPhone(String updatedByPhone) {
        this.updatedByPhone = updatedByPhone;
    }
}
