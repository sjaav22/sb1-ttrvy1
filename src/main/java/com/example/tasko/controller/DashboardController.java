package com.example.tasko.controller;

import com.example.tasko.model.*;
import com.example.tasko.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EnterpriseService enterpriseService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName());
        
        if (user == null) {
            return "redirect:/login";
        }

        Enterprise enterprise = user.getEnterprise();
        
        if (enterprise == null) {
            return "redirect:/register?error=noenterprise";
        }

        model.addAttribute("user", user);
        model.addAttribute("enterprise", enterprise);

        // Check user's role and redirect accordingly
        if (user.getRole() == UserRole.ADMIN) {
            return setupAdminDashboard(model, enterprise);
        } else {
            return setupUserDashboard(model, user);
        }
    }

    private String setupAdminDashboard(Model model, Enterprise enterprise) {
        try {
            // Add statistics
            long userCount = userService.countUsersByEnterprise(enterprise);
            long activeTaskCount = taskService.countActiveTasksByEnterprise(enterprise);
            long todayAttendanceCount = attendanceService.countTodayAttendanceByEnterprise(enterprise);

            model.addAttribute("userCount", userCount);
            model.addAttribute("activeTaskCount", activeTaskCount);
            model.addAttribute("todayAttendanceCount", todayAttendanceCount);

            // Add recent activities
            List<Task> recentTasks = taskService.getRecentTasksByEnterprise(enterprise);
            List<Attendance> recentAttendance = attendanceService.getRecentAttendanceByEnterprise(enterprise);

            model.addAttribute("recentTasks", recentTasks);
            model.addAttribute("recentAttendance", recentAttendance);

            return "dashboard/admin";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return "redirect:/error/500";
        }
    }

    private String setupUserDashboard(Model model, User user) {
        try {
            // Get today's attendance
            Attendance todayAttendance = attendanceService.getTodayAttendance(user);
            model.addAttribute("todayAttendance", todayAttendance);

            // Get user's tasks
            List<Task> recentTasks = taskService.getRecentTasksByUser(user);
            model.addAttribute("recentTasks", recentTasks);

            return "dashboard/user";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return "redirect:/error/500";
        }
    }

    @PostMapping("/attendance/log-today")
    public String logTodayAttendance(
            @RequestParam("status") AttendanceStatus status,
            Authentication authentication) {
        try {
            User user = userService.getUserByUsername(authentication.getName());
            
            // Check if attendance already logged today
            if (attendanceService.getTodayAttendance(user) != null) {
                return "redirect:/dashboard?error=alreadylogged";
            }

            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setDate(LocalDate.now());
            attendance.setStatus(status);
            
            attendanceService.logAttendance(attendance);
            return "redirect:/dashboard?success=logged";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard?error=failed";
        }
    }
}