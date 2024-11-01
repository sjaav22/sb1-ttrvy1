package com.example.tasko.repository;

import com.example.tasko.model.Enterprise;
import com.example.tasko.model.Task;
import com.example.tasko.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEnterprise(Enterprise enterprise);
    Page<Task> findByEnterprise(Enterprise enterprise, Pageable pageable);
    List<Task> findByAssignedUsersContaining(User user);
    long countByEnterpriseAndCompletedFalse(Enterprise enterprise);
    List<Task> findTop5ByEnterpriseOrderByDueDateDesc(Enterprise enterprise);
    List<Task> findTop5ByAssignedUsersContainingOrderByDueDateAsc(User user);
}