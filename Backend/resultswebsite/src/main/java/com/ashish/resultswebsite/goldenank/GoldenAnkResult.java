package com.ashish.resultswebsite.goldenank;

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
        name = "golden_ank_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_golden_ank_results_date",
                columnNames = "result_date"
        )
)
public class GoldenAnkResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

    @Column(name = "ank_one", nullable = false, length = 1)
    private String ankOne;

    @Column(name = "ank_two", nullable = false, length = 1)
    private String ankTwo;

    @Column(name = "ank_three", nullable = false, length = 1)
    private String ankThree;

    @Column(name = "ank_four", nullable = false, length = 1)
    private String ankFour;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public LocalDate getResultDate() {
        return resultDate;
    }

    public void setResultDate(LocalDate resultDate) {
        this.resultDate = resultDate;
    }

    public String getAnkOne() {
        return ankOne;
    }

    public void setAnkOne(String ankOne) {
        this.ankOne = ankOne;
    }

    public String getAnkTwo() {
        return ankTwo;
    }

    public void setAnkTwo(String ankTwo) {
        this.ankTwo = ankTwo;
    }

    public String getAnkThree() {
        return ankThree;
    }

    public void setAnkThree(String ankThree) {
        this.ankThree = ankThree;
    }

    public String getAnkFour() {
        return ankFour;
    }

    public void setAnkFour(String ankFour) {
        this.ankFour = ankFour;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
