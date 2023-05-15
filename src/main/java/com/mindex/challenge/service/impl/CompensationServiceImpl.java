package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation");
        //verify referenced employee exists
        //would also add additional validations around employee, salary and effective date
        if (employeeRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId()) != null) {
            compensation.setEmployeeId(compensation.getEmployee().getEmployeeId());
            compensationRepository.insert(compensation);
        } else {
            //In real world I would create custom exceptions with error handling. This would probably return a 404 status code to client.
            throw new RuntimeException ("Employee with id " + compensation.getEmployee().getEmployeeId() + " not found");
        }

        return compensation;
    }

    @Override
    public Compensation read(String id) {
        LOG.debug("Reading compensation for [{}]", id);
        Compensation compensation = compensationRepository.findByEmployeeId(id);

        if (compensation == null) {
            throw new RuntimeException("No compensation found for employeeId: " + id);
        }
        return compensation;
    }
}
