package com.example.tasko.service;

import com.example.tasko.model.Attendance;
import com.example.tasko.model.Enterprise;
import com.example.tasko.model.User;
import com.example.tasko.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Transactional
    public Attendance logAttendance(Attendance attendance) {
        // Check if attendance already exists for the user on this date
        Attendance existingAttendance = attendanceRepository.findByUserAndDate(
            attendance.getUser(), attendance.getDate());
        
        if (existingAttendance != null) {
            throw new RuntimeException("Attendance already logged for this date");
        }
        
        return attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getUserAttendance(User user, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getEnterpriseAttendance(Enterprise enterprise, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserEnterpriseAndDateBetween(enterprise, startDate, endDate);
    }

    public long countTodayAttendanceByEnterprise(Enterprise enterprise) {
        return attendanceRepository.countByUserEnterpriseAndDate(enterprise, LocalDate.now());
    }

    public List<Attendance> getRecentAttendanceByEnterprise(Enterprise enterprise) {
        return attendanceRepository.findTop5ByUserEnterpriseOrderByDateDesc(enterprise);
    }

    public Attendance getTodayAttendance(User user) {
        return attendanceRepository.findByUserAndDate(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public boolean hasUserLoggedAttendanceToday(User user) {
        return attendanceRepository.existsByUserAndDate(user, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Attendance> getMonthlyAttendance(LocalDate month) {
        LocalDate startOfMonth = month.withDayOfMonth(1);
        LocalDate endOfMonth = month.withDayOfMonth(month.lengthOfMonth());
        return attendanceRepository.findByDateBetween(startOfMonth, endOfMonth);
    }
}