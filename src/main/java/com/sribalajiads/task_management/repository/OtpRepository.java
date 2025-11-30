package com.sribalajiads.task_management.repository;

import com.sribalajiads.task_management.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, String> {
}