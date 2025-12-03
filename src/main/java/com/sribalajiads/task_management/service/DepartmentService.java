package com.sribalajiads.task_management.service;

import com.sribalajiads.task_management.dto.DepartmentDTO;
import com.sribalajiads.task_management.entity.Department;
import com.sribalajiads.task_management.entity.Role;
import com.sribalajiads.task_management.entity.User;
import com.sribalajiads.task_management.repository.DepartmentRepository;
import com.sribalajiads.task_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Department updateDepartment(Long deptId, DepartmentDTO request) {
        // 1. Fetch Department
        Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // 2. Update Name (Check for duplicates if name changed)
        if (!dept.getName().equals(request.getName())) {
            if (departmentRepository.existsByName(request.getName())) {
                throw new RuntimeException("Department name '" + request.getName() + "' already exists.");
            }
            dept.setName(request.getName());
        }

        // 3. Update Description
        dept.setDescription(request.getDescription());

        // 4. Update Head (Optional - only if provided and different)
        if (request.getHeadUserId() != null) {
            User currentHead = dept.getHeadUser();

            // If the head is changing
            if (currentHead == null || !currentHead.getId().equals(request.getHeadUserId())) {
                User newHead = userRepository.findById(request.getHeadUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Check if new head is already leading another department
                if (departmentRepository.findByHeadUser(newHead).isPresent()) {
                    Department existingDept = departmentRepository.findByHeadUser(newHead).get();
                    if (!existingDept.getId().equals(deptId)) {
                        throw new RuntimeException("User is already the Head of " + existingDept.getName());
                    }
                }

                // Demote old head
                if (currentHead != null) {
                    currentHead.setRole(Role.EMPLOYEE);
                    userRepository.save(currentHead);
                }

                // Promote new head
                newHead.setRole(Role.DEPT_HEAD);
                newHead.setDepartment(dept);
                userRepository.save(newHead);

                // Link to Department
                dept.setHeadUser(newHead);
            }
        }

        return departmentRepository.save(dept);
    }
}