package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure generateReportingStructure(String id) {
        Employee rootEmployee = employeeRepository.findByEmployeeId(id);
        return new ReportingStructure(rootEmployee, countNumberOfReportsDFS(rootEmployee));
    }

    /**
     * BFS - left this method and comments here but I replaced it with a DFS below
     *
     * I don't think memory usage is a big concern here - I think the largest the queue will
     * grow to is the number of reports to a single parent + siblings of that parent.
     * I chose to not use recursion because I think it's hard
     * to read and hard to maintain, plus memory usage can be a concern.
     *
     * Assumptions - no cycles
     */
    private int countNumberOfReports(Employee employee) {
        int numberOfReports = 0;
        Queue<Employee> employeeQueue = new ArrayDeque<>();
        employeeQueue.add(employee);
        while(!employeeQueue.isEmpty()) {
            //remove current parent
            Employee currentEmployee = employeeQueue.remove();
            //retrieve all direct reports of the parent
            List<Employee> directReports = retrieveDirectReports(currentEmployee);
            LOG.debug("Number of direct reports for employee [{}] is [{}]", currentEmployee.getLastName(), directReports.size());
            //increment by number of direct reports
            numberOfReports += directReports.size();
            //if we found no direct reports and we don't have any siblings, we are done
            if (directReports.isEmpty() && employeeQueue.isEmpty()) {
                return numberOfReports;
            } else {
                //add all the direct reports to the queue so we can explore their reports
                employeeQueue.addAll(directReports);
            }
        }
        return numberOfReports;
    }

    /**
     * DFS - I was outside working in the garden and my above BFS solution was bothering me, so I implemented below.
     * This should use less memory and be faster than my initial BFS algorithm
     **/
    private int countNumberOfReportsDFS(Employee employee) {
        int numberOfReports = 0;
        Stack<Employee> stack = new Stack<>();
        Employee current = employee;
        stack.push(current);
        while (!stack.isEmpty()) {
            current = stack.pop();
            for (Employee directReport : retrieveDirectReports(current)) {
                numberOfReports++;
                stack.push(directReport);
            }
        }
        return numberOfReports;
    }

    private List<Employee> retrieveDirectReports(Employee employee) {
        List<Employee> directReports = new ArrayList<>();
        if (employee.getDirectReports() != null && !employee.getDirectReports().isEmpty()) {
            for (Employee directReport : employee.getDirectReports()) {
                //Below assumes directReportEmployee will be valid (no null fields), which assumes data input validation was performed
                Employee directReportEmployee = employeeRepository.findByEmployeeId(directReport.getEmployeeId());
                LOG.debug("Employee [{}] is direct report of [{}]", directReportEmployee.getLastName(), employee.getLastName());
                directReports.add(directReportEmployee);
            }
        }
        return directReports;
    }
}
