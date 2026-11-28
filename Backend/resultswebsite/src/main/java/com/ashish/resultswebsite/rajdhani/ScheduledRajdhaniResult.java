package com.ashish.resultswebsite.rajdhani;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "scheduled_rajdhani_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_rajdhani_result_market_date",
                columnNames = {"market_name", "result_date"}
        )
)
public class ScheduledRajdhaniResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_name", nullable = false, length = 32)
    private RajdhaniMarket market;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

    @Column(name = "open_pana", nullable = false, length = 3)
    private String openPana;

    @Column(name = "open_digit", nullable = false, length = 1)
    private String openDigit;

    @Column(name = "close_pana", length = 3)
    private String closePana;

    @Column(name = "close_digit", length = 1)
    private String closeDigit;

    @Column(name = "open_publish_at", nullable = false)
    private Instant openPublishAt;

    @Column(name = "publish_at", nullable = false)
    private Instant publishAt;

    @Column(name = "open_published", nullable = false)
    private boolean openPublished;

    @Column(name = "open_published_at")
    private Instant openPublishedAt;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public RajdhaniMarket getMarket() {
        return market;
    }

    public void setMarket(RajdhaniMarket market) {
        this.market = market;
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

    public Instant getOpenPublishAt() {
        return openPublishAt;
    }

    public void setOpenPublishAt(Instant openPublishAt) {
        this.openPublishAt = openPublishAt;
    }

    public Instant getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(Instant publishAt) {
        this.publishAt = publishAt;
    }

    public boolean isOpenPublished() {
        return openPublished;
    }

    public void setOpenPublished(boolean openPublished) {
        this.openPublished = openPublished;
    }

    public Instant getOpenPublishedAt() {
        return openPublishedAt;
    }

    public void setOpenPublishedAt(Instant openPublishedAt) {
        this.openPublishedAt = openPublishedAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
