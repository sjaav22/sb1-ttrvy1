package com.example.tasko.service;

import com.example.tasko.model.Enterprise;
import com.example.tasko.model.Task;
import com.example.tasko.model.User;
import com.example.tasko.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<Task> getAllTasksByEnterprise(Enterprise enterprise) {
        return taskRepository.findByEnterprise(enterprise);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByUser(User user) {
        return taskRepository.findByAssignedUsersContaining(user);
    }

    @Transactional(readOnly = true)
    public Page<Task> getTasksPaginated(Pageable pageable, Enterprise enterprise, String sort, String filter) {
        return taskRepository.findByEnterprise(enterprise, pageable);
    }

    @Transactional
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public Task completeTask(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setCompleted(true);
        return taskRepository.save(task);
    }

    public long countActiveTasksByEnterprise(Enterprise enterprise) {
        return taskRepository.countByEnterpriseAndCompletedFalse(enterprise);
    }

    public List<Task> getRecentTasksByEnterprise(Enterprise enterprise) {
        return taskRepository.findTop5ByEnterpriseOrderByDueDateDesc(enterprise);
    }

    public List<Task> getRecentTasksByUser(User user) {
        return taskRepository.findTop5ByAssignedUsersContainingOrderByDueDateAsc(user);
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }
}