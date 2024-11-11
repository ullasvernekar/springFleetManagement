package com.ot.BoboLike.service;

import com.ot.BoboLike.dao.DriverDao;
import com.ot.BoboLike.dao.VehicleDao;
import com.ot.BoboLike.dto.Driver;
import com.ot.BoboLike.dto.ResponseStructure;
import com.ot.BoboLike.dto.Vehicle;
import com.ot.BoboLike.dto.request.DriverRegister;
import com.ot.BoboLike.repository.AdminRepository;
import com.ot.BoboLike.repository.DriverRepository;
import com.ot.BoboLike.util.Aes;
import com.ot.BoboLike.util.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    @Autowired
    public DriverDao driverDao;

    @Autowired
    public DriverRepository driverRepository;

    @Autowired
    private EmailSender mailSender;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private VehicleDao vehicleDao;

    public ResponseEntity<ResponseStructure<Driver>> save(DriverRegister driverRegister) {
        logger.info("Attempting to save driver with email: {}", driverRegister.getEmail());
        ResponseStructure<Driver> responseStructure = new ResponseStructure<>();
        try {
            if (driverDao.findByEmail(driverRegister.getEmail()) != null ||
                    driverDao.findByPhone(driverRegister.getPhone()) != null) {
                logger.warn("Driver with email {} or phone {} already exists", driverRegister.getEmail(), driverRegister.getPhone());
                responseStructure.setStatus(HttpStatus.CONFLICT.value());
                responseStructure.setMessage("Driver Could Not Be Saved, Already Exists");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.CONFLICT);
            }
            Driver driver = new Driver();
            driver.setFirstName(driverRegister.getFirstName());
            driver.setLastName(driverRegister.getLastName());
            driver.setName(driverRegister.getFirstName() + " " + driverRegister.getLastName());
            driver.setLicenseNumber(driverRegister.getLicenseNumber());
            driver.setPhone(driverRegister.getPhone());
            driver.setEmail(driverRegister.getEmail());
            driver.setAddress(driverRegister.getAddress());
            driver.setPassportNumber(driverRegister.getPassportNumber());
            driver.setLicenseImage(driverRegister.getLicenseImage());
            driver.setPassportImage(driverRegister.getPassportImage());
            driver.setPassword(Aes.encrypt(driverRegister.getPassword()));
            driver.setNationality(driverRegister.getNationality());
            if (driverRegister.getVehicleId() != 0) {
                Vehicle vehicle = vehicleDao.findById(driverRegister.getVehicleId());
                if (vehicle != null) {
                    driver.setVehicle(vehicle);
                } else {
                    logger.warn("Vehicle with ID {} not found", driverRegister.getVehicleId());
                    responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                    responseStructure.setMessage("Vehicle with ID " + driverRegister.getVehicleId() + " not found.");
                }
            }
            Driver savedDriver = driverRepository.save(driver);
            logger.info("Driver saved successfully with ID: {}", savedDriver.getId());
            responseStructure.setStatus(HttpStatus.CREATED.value());
            responseStructure.setMessage("Driver Saved Successfully");
            responseStructure.setData(savedDriver);
            return new ResponseEntity<>(responseStructure, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to save driver: {}", e.getMessage());
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage("Failed to save Driver: " + e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<Driver>> findById(long id) {
        logger.info("Attempting to find driver by ID: {}", id);
        ResponseStructure<Driver> responseStructure = new ResponseStructure<>();
        Driver optionalDriver = driverDao.findById(id);
        if (optionalDriver == null) {
            logger.warn("Driver not found with ID: {}", id);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver does not exist to be found by ID ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
        logger.info("Driver found with ID: {}", id);
        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("Driver found by ID = " + id);
        responseStructure.setData(optionalDriver);
        return new ResponseEntity<>(responseStructure, HttpStatus.OK);
    }

    public ResponseEntity<ResponseStructure<Driver>> delete(long id) {
        logger.info("Attempting to delete driver by ID: {}", id);
        ResponseStructure<Driver> responseStructure = new ResponseStructure<>();
        Driver optionalDriver = driverDao.findById(id);
        if (optionalDriver != null) {
            driverDao.delete(optionalDriver);
            logger.info("Driver deleted successfully with ID: {}", id);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Driver deleted successfully ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("Driver not found to delete with ID: {}", id);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver not found to delete ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<Page<Driver>>> findAll(int offset, int pageSize, String field) {
        logger.info("Attempting to find all drivers with offset: {}, pageSize: {}, and field: {}", offset, pageSize, field);
        ResponseStructure<Page<Driver>> responseStructure = new ResponseStructure<>();
        Page<Driver> driverPage = driverDao.findAll(offset, pageSize, field);
        if (!driverPage.isEmpty()) {
            logger.info("Found drivers data");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("All Driver found ");
            responseStructure.setData(driverPage);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("No driver data exists");
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No Driver Found ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }
}
