package com.example.webapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class SortResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sortType;

    private String originalNumbers;

    private String sortedNumbers;

    private LocalDateTime executionTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public String getOriginalNumbers() {
        return originalNumbers;
    }

    public void setOriginalNumbers(String originalNumbers) {
        this.originalNumbers = originalNumbers;
    }

    public String getSortedNumbers() {
        return sortedNumbers;
    }

    public void setSortedNumbers(String sortedNumbers) {
        this.sortedNumbers = sortedNumbers;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
}
