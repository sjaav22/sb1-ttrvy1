package com.example.tasko.controller;

import com.example.tasko.model.*;
import com.example.tasko.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showReports(Model model, Authentication authentication) {
        User currentUser = userService.getUserByUsername(authentication.getName());
        Enterprise enterprise = currentUser.getEnterprise();

        // Calculate metrics
        double taskCompletionRate = calculateTaskCompletionRate(enterprise);
        double attendanceRate = calculateAttendanceRate(enterprise);
        long activeUsers = userService.countUsersByEnterprise(enterprise);

        // Prepare chart data
        Map<String, Object> taskChartData = prepareTaskChartData(enterprise);
        Map<String, Object> attendanceChartData = prepareAttendanceChartData(enterprise);

        // Add data to model
        model.addAttribute("taskCompletionRate", Math.round(taskCompletionRate));
        model.addAttribute("attendanceRate", Math.round(attendanceRate));
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("taskChartData", taskChartData);
        model.addAttribute("attendanceChartData", attendanceChartData);
        model.addAttribute("recentActivities", getRecentActivities(enterprise));

        return "reports/analytics";
    }

    private double calculateTaskCompletionRate(Enterprise enterprise) {
        List<Task> tasks = taskService.getAllTasksByEnterprise(enterprise);
        if (tasks.isEmpty()) return 0;

        long completedTasks = tasks.stream().filter(Task::isCompleted).count();
        return (double) completedTasks / tasks.size() * 100;
    }

    private double calculateAttendanceRate(Enterprise enterprise) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        List<Attendance> attendances = attendanceService.getEnterpriseAttendance(enterprise, startOfMonth, today);
        
        if (attendances.isEmpty()) return 0;

        long presentCount = attendances.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
            .count();
        return (double) presentCount / attendances.size() * 100;
    }

    private Map<String, Object> prepareTaskChartData(Enterprise enterprise) {
        List<Task> tasks = taskService.getAllTasksByEnterprise(enterprise);
        Map<String, Object> data = new HashMap<>();
        
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long pending = tasks.stream().filter(t -> !t.isCompleted()).count();
        
        data.put("completed", completed);
        data.put("pending", pending);
        data.put("inProgress", tasks.size() - (completed + pending));
        
        return data;
    }

    private Map<String, Object> prepareAttendanceChartData(Enterprise enterprise) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        List<Attendance> attendances = attendanceService.getEnterpriseAttendance(enterprise, startOfMonth, today);

        Map<String, Object> data = new HashMap<>();
        // Process attendance data to create chart labels and values
        // Implementation details would depend on how you want to visualize the data
        
        return data;
    }

    private List<Map<String, Object>> getRecentActivities(Enterprise enterprise) {
        // Implement logic to get recent activities (tasks, attendance, etc.)
        // Return a list of activity maps containing date, user, description, and status
        return List.of(); // Placeholder
    }
}