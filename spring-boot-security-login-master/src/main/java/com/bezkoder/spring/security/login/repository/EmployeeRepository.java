package com.bezkoder.spring.security.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.spring.security.login.models.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  Optional<Employee> findByFirstName(String firstName);

  boolean existsByEmployeeId(String employeeId);

  boolean existsByEmployeeIdAndIdNot(String employeeId, Long id);
}
