package com.oscar.tradeWorkerBackend.metrics.context.repository;

import com.oscar.tradeWorkerBackend.metrics.context.model.DailyMetricsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyMetricsSnapshotRepository extends JpaRepository<DailyMetricsSnapshot, Long> {

    Optional<DailyMetricsSnapshot> findTopByOrderByAsOfDesc();
}
