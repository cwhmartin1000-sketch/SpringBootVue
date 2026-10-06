package com.bezkoder.spring.security.login.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.security.login.models.Employee;
import com.bezkoder.spring.security.login.payload.request.EmployeeeRequest;
import com.bezkoder.spring.security.login.payload.response.MessageResponse;
import com.bezkoder.spring.security.login.repository.EmployeeRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/emp")
public class EmployeeController {
  private final EmployeeRepository employeeRepository;

  public EmployeeController(EmployeeRepository employeeRepository) {
    this.employeeRepository = employeeRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
  public List<Employee> getEmployees() {
    return employeeRepository.findAll();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
  public ResponseEntity<?> getEmployee(@PathVariable Long id) {
    return employeeRepository.findById(id)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new MessageResponse("Employee not found.")));
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeeRequest request) {
    if (employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new MessageResponse("Employee ID already exists."));
    }

    Employee employee = new Employee();
    copyEmployeeFields(request, employee);
    return ResponseEntity.status(HttpStatus.CREATED).body(employeeRepository.save(employee));
  }

  @PutMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> updateEmployee(
      @PathVariable Long id, @Valid @RequestBody EmployeeeRequest request) {
    return employeeRepository.findById(id).map(employee -> {
      if (employeeRepository.existsByEmployeeIdAndIdNot(request.getEmployeeId(), id)) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new MessageResponse("Employee ID already exists."));
      }

      copyEmployeeFields(request, employee);
      return ResponseEntity.ok(employeeRepository.save(employee));
    }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new MessageResponse("Employee not found.")));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
    if (!employeeRepository.existsById(id)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new MessageResponse("Employee not found."));
    }

    employeeRepository.deleteById(id);
    return ResponseEntity.ok(new MessageResponse("Employee deleted."));
  }

  private void copyEmployeeFields(EmployeeeRequest request, Employee employee) {
    employee.setEmployeeId(request.getEmployeeId().trim());
    employee.setEmail(request.getEmail().trim());
    employee.setFirstName(request.getFirstName().trim());
    employee.setLastName(request.getLastName().trim());
    employee.setDepartment(request.getDepartment().trim());
    employee.setSalary(request.getSalary());
  }
}
