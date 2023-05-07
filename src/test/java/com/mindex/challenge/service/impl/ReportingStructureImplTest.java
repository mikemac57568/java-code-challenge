package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeReportingStructureUrl;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    String parentEmployeeId = "";

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        employeeReportingStructureUrl = "http://localhost:" + port + "/employee/{id}/reporting-structure";
        createParent();
    }

    //This method makes sure we have a fresh tree structure for each test case
    private void createParent() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Parent");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create new employee
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();
        parentEmployeeId = createdEmployee.getEmployeeId();
    }

    @Test
    public void testReadReportingStructure_no_reports() {
        ReportingStructure readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, parentEmployeeId).getBody();
        assertEquals(parentEmployeeId, readReportingStructure.getEmployee().getEmployeeId());
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadReportingStructure_add_child() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Child");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create new employee
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();
        //
        Employee parentEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, parentEmployeeId).getBody();
        assertNull(parentEmployee.getDirectReports());
        List<Employee> parentDirectReports = new ArrayList<>();
        parentDirectReports.add(createdEmployee);
        parentEmployee.setDirectReports(parentDirectReports);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedParent =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(parentEmployee, headers),
                        Employee.class,
                        parentEmployee.getEmployeeId()).getBody();

        assertEquals(parentEmployee.getEmployeeId(), updatedParent.getEmployeeId(), parentEmployeeId);

        ReportingStructure readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, parentEmployeeId).getBody();
        assertEquals(parentEmployeeId, readReportingStructure.getEmployee().getEmployeeId());
        assertEquals(1, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadReportingStructure_add_parent() {
        Employee grandParentEmployee = new Employee();
        grandParentEmployee.setFirstName("John");
        grandParentEmployee.setLastName("GrandParent");
        grandParentEmployee.setDepartment("Engineering");
        grandParentEmployee.setPosition("Developer");

        Employee parentEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, parentEmployeeId).getBody();
        List<Employee> directReports = new ArrayList<>();
        directReports.add(parentEmployee);
        grandParentEmployee.setDirectReports(directReports);

        // Create grandparent
        Employee createdGrandParentEmployee = restTemplate.postForEntity(employeeUrl, grandParentEmployee, Employee.class).getBody();

        ReportingStructure readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, parentEmployeeId).getBody();
        assertEquals(parentEmployeeId, readReportingStructure.getEmployee().getEmployeeId());
        assertEquals(0, readReportingStructure.getNumberOfReports());

        readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, createdGrandParentEmployee.getEmployeeId()).getBody();
        assertEquals(1, readReportingStructure.getNumberOfReports());
    }

    //In the real world would not do this, but given database here is static it is useful.
    @Test
    public void testEmployeeDatabaseScenarios() {
        //Lennon
        ReportingStructure readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, "16a596ae-edd3-4847-99fe-c4518e82c86f").getBody();
        assertEquals(4, readReportingStructure.getNumberOfReports());
        //McCartney
        readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, "b7839309-3348-463b-a7e3-5de1c168beb3").getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
        //Starr
        readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, "03aa1462-ffa9-4978-901b-7c001562cf6f").getBody();
        assertEquals(2, readReportingStructure.getNumberOfReports());
        //Best
        readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, "62c1084e-6e34-4630-93fd-9153afb65309").getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
        //Harrison
        readReportingStructure = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, "c0c2293d-16bd-4603-8e08-638a9d18b22c").getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }
}
