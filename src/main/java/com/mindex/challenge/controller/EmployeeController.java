package com.mindex.challenge.controller;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @PostMapping("/employee")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    @GetMapping("/employee/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee read request for id [{}]", id);

        return employeeService.read(id);
    }

    @PutMapping("/employee/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee create request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        //I would add more validation here. For example, DirectReports should be valid employees, and direct reports should not be circular
        return employeeService.update(employee);
    }

    /**
     * I am keeping this endpoint in the Employee controller because it is pulling data from the Employee repository,
     * and providing enrichment to the employee data.
     * however, because it is not returning an Employee object, I considered
     * making a new controller, and think that could be valid.
     * it depends on company norms.
     **/
    @GetMapping("/employee/{id}/reporting-structure")
    public ReportingStructure readReportingStructure(@PathVariable String id) {
        LOG.debug("Received employee reporting structure read request for id [{}]", id);

        return reportingStructureService.generateReportingStructure(id);
    }
}
