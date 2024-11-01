package com.example.tasko.repository;

import com.example.tasko.model.Attendance;
import com.example.tasko.model.Enterprise;
import com.example.tasko.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByUserEnterpriseAndDateBetween(Enterprise enterprise, LocalDate startDate, LocalDate endDate);
    long countByUserEnterpriseAndDate(Enterprise enterprise, LocalDate date);
    List<Attendance> findTop5ByUserEnterpriseOrderByDateDesc(Enterprise enterprise);
    Attendance findByUserAndDate(User user, LocalDate date);
    boolean existsByUserAndDate(User user, LocalDate date);
    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
}