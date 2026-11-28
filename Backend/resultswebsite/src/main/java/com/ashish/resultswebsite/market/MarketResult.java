package com.ashish.resultswebsite.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "market_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_market_results_market_date",
                columnNames = {"market_name", "result_date"}
        )
)
public class MarketResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_name", nullable = false)
    private String marketName;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

    @Column(name = "open_pana", length = 3)
    private String openPana;

    @Column(name = "open_digit", length = 1)
    private String openDigit;

    @Column(name = "close_pana", length = 3)
    private String closePana;

    @Column(name = "close_digit", length = 1)
    private String closeDigit;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public LocalDate getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDate resultDate) {
        this.resultDate = resultDate;
    }

    public String getOpenPana() {
        return openPana;
    }

    public void setOpenPana(String openPana) {
        this.openPana = openPana;
    }

    public String getOpenDigit() {
        return openDigit;
    }

    public void setOpenDigit(String openDigit) {
        this.openDigit = openDigit;
    }

    public String getClosePana() {
        return closePana;
    }

    public void setClosePana(String closePana) {
        this.closePana = closePana;
    }

    public String getCloseDigit() {
        return closeDigit;
    }

    public void setCloseDigit(String closeDigit) {
        this.closeDigit = closeDigit;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
