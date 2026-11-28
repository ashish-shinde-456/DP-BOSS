package com.ashish.resultswebsite.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "result_change_audit")
public class ResultChangeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_phone", nullable = false, length = 10)
    private String actorPhone;

    @Column(name = "actor_name", nullable = false, length = 80)
    private String actorName;

    @Column(name = "market_name", nullable = false, length = 32)
    private String marketName;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @Column(name = "previous_result", length = 16)
    private String previousResult;

    @Column(name = "new_result", nullable = false, length = 16)
    private String newResult;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public Long getId() {
        return id;
    }

    public String getActorPhone() {
        return actorPhone;
    }

    public void setActorPhone(String actorPhone) {
        this.actorPhone = actorPhone;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPreviousResult() {
        return previousResult;
    }

    public void setPreviousResult(String previousResult) {
        this.previousResult = previousResult;
    }

    public String getNewResult() {
        return newResult;
    }

    public void setNewResult(String newResult) {
        this.newResult = newResult;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }
}
