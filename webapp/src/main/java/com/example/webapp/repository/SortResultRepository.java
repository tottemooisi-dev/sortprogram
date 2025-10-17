package com.example.webapp.repository;

import com.example.webapp.entity.SortResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SortResultRepository extends JpaRepository<SortResult, Long> {
}
