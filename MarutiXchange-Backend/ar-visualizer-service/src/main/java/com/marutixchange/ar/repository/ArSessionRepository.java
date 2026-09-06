package com.marutixchange.ar.repository;

import com.marutixchange.ar.model.ArSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArSessionRepository extends JpaRepository<ArSession, Long> {

    Optional<ArSession> findBySessionId(String sessionId);

    List<ArSession> findByCarIdOrderByCreatedAtDesc(Long carId);

    long countByCarId(Long carId);

    @Query("""
        SELECT COUNT(s) FROM ArSession s
        WHERE s.carId = :carId
        AND s.status = 'COMPLETED'
        """)
    long countCompletedSessionsByCarId(@Param("carId") Long carId);

    @Query("""
        SELECT AVG(s.durationSeconds) FROM ArSession s
        WHERE s.carId = :carId
        AND s.durationSeconds IS NOT NULL
        """)
    Double avgDurationByCarId(@Param("carId") Long carId);

    @Query("""
        SELECT COUNT(s) FROM ArSession s
        WHERE s.carId = :carId
        AND s.screenshotTaken = true
        """)
    long countScreenshotsByCarId(@Param("carId") Long carId);
}
