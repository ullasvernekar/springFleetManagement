package com.ot.BoboLike.service;

import com.ot.BoboLike.dao.VehicleDao;
import com.ot.BoboLike.dto.ResponseStructure;
import com.ot.BoboLike.dto.Vehicle;
import com.ot.BoboLike.dto.request.VehicleRegister;
import com.ot.BoboLike.repository.DriverRepository;
import com.ot.BoboLike.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    @Autowired
    private VehicleDao vehicleDao;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public ResponseEntity<ResponseStructure<Vehicle>> saveVehicle(VehicleRegister vehicleRegister) {
        logger.info("Starting saveVehicle method with vehicle number plate: {}", vehicleRegister.getVehicleNumber());
        ResponseStructure<Vehicle> responseStructure = new ResponseStructure<>();

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleName(vehicleRegister.getVehicleName());
        vehicle.setVehicleNumberPlate(vehicleRegister.getVehicleNumber());
        vehicle.setModelOfYear(vehicleRegister.getModelOfYear());
        vehicle.setVehicleType(vehicleRegister.getVehicleType());
        vehicle.setFuelType(vehicleRegister.getFuelType());
        vehicle.setOccupancy(vehicleRegister.getOccupancy());

        try {
            logger.debug("Checking if vehicle with number plate {} already exists", vehicle.getVehicleNumberPlate());
            if (vehicleRepository.findByVehicleNumberPlate(vehicle.getVehicleNumberPlate()) != null) {
                logger.warn("Vehicle with number plate {} already exists", vehicle.getVehicleNumberPlate());
                responseStructure.setStatus(HttpStatus.CONFLICT.value());
                responseStructure.setMessage("Vehicle with number plate " + vehicle.getVehicleNumberPlate() + " already exists.");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.CONFLICT);
            }

            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            logger.info("Vehicle saved successfully with ID: {}", savedVehicle.getId());

            responseStructure.setStatus(HttpStatus.CREATED.value());
            responseStructure.setMessage("Vehicle Saved Successfully");
            responseStructure.setData(savedVehicle);
            return new ResponseEntity<>(responseStructure, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to save vehicle: {}", e.getMessage());
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage("Failed to save Vehicle: " + e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<List<Vehicle>>> findVehicleByDriverId(long driverId) {
        logger.info("Attempting to find vehicles for driver with ID: {}", driverId);
        ResponseStructure<List<Vehicle>> responseStructure = new ResponseStructure<>();
        List<Vehicle> vehicles = vehicleDao.findVehicleByDriverId(driverId);

        if (!vehicles.isEmpty()) {
            logger.info("Vehicles found for driver with ID: {}", driverId);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Vehicles found for driver with ID " + driverId);
            responseStructure.setData(vehicles);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("No vehicles found for driver with ID: {}", driverId);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No vehicles found for driver with ID " + driverId);
            responseStructure.setData(Collections.emptyList());
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<List<Vehicle>>> findByVehicleNumberPlate(String numberPlate) {
        logger.info("Attempting to find vehicle with number plate: {}", numberPlate);
        ResponseStructure<List<Vehicle>> responseStructure = new ResponseStructure<>();
        List<Vehicle> vehicle = vehicleDao.findByNumberPlate(numberPlate);

        if (vehicle != null) {
            logger.info("Vehicle found with number plate: {}", numberPlate);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Vehicle found with number plate " + numberPlate);
            responseStructure.setData(vehicle);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("No vehicle found with number plate: {}", numberPlate);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No vehicle found with number plate " + numberPlate);
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<Page<Vehicle>>> findAll(int offset, int pageSize, String field) {
        ResponseStructure<Page<Vehicle>> responseStructure = new ResponseStructure<>();
        Page<Vehicle> vehicles = vehicleDao.findAll(offset, pageSize, field);
        if (vehicles != null) {
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Found all the data of admins ");
            responseStructure.setData(vehicles);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No data exists to find of admins");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }
}