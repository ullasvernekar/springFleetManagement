package com.ot.BoboLike.service;

import com.ot.BoboLike.dao.BookingDao;
import com.ot.BoboLike.dao.DriverDao;
import com.ot.BoboLike.dto.*;
import com.ot.BoboLike.dto.request.BookingInitiate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingDao bookingDao;

    @Autowired
    private DriverDao driverDao;

    public ResponseEntity<ResponseStructure<Booking>> assignDriver(BookingInitiate initiate) {
        logger.info("Attempting to assign driver with ID: {}", initiate.getDriverId());
        ResponseStructure<Booking> responseStructure = new ResponseStructure<>();
        Driver driver = driverDao.findById(initiate.getDriverId());
        if (Objects.isNull(driver)) {
            logger.warn("Driver not found with ID: {}", initiate.getDriverId());
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver Id Not Found " + initiate.getDriverId());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
        if (driver.getStatus().equals("UNAVAILABLE")) {
            logger.warn("Driver is unavailable: {}", driver.getName());
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver Unavailable " + driver.getName());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
        Booking booking = new Booking();
        booking.setDriver(driver);
        booking.setCustomerName(initiate.getCustomerName());
        booking.setCustomerNumber(initiate.getCustomerNumber());
        booking.setPickupLocationlatitude(initiate.getPickupLocationlatitude());
        booking.setPickupLocationlongitude(initiate.getPickupLocationlongitude());
        booking.setDropLocationLatitude(initiate.getDropLocationLatitude());
        booking.setDropLocationLongitude(initiate.getDropLocationLongitude());
        booking.setStatus(BookingStatus.ASSIGNED);
        bookingDao.save(booking);
        driver.setStatus(DriverStatus.UNAVAILABLE);
        driverDao.save(driver);
        logger.info("Booking created and driver assigned: {}", driver.getName());
        responseStructure.setStatus(HttpStatus.CREATED.value());
        responseStructure.setMessage("Booking Created and Driver Assigned of Name -> " + driver.getName());
        responseStructure.setData(null);
        return new ResponseEntity<>(responseStructure, HttpStatus.CREATED);
    }

    public ResponseEntity<ResponseStructure<Booking>> completeBooking(Long bookingId) {
        logger.info("Attempting to complete booking with ID: {}", bookingId);
        ResponseStructure<Booking> responseStructure = new ResponseStructure<>();
        try {
            Booking booking = bookingDao.findById(bookingId).orElse(null);
            if (booking == null) {
                logger.warn("Booking not found with ID: {}", bookingId);
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("Booking not found for ID: " + bookingId);
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }
            Driver driver = booking.getDriver();
            if (driver == null) {
                logger.warn("Driver not found for Booking ID: {}", bookingId);
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("Driver not found for Booking ID: " + bookingId);
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }
            booking.setStatus(BookingStatus.COMPLETED);
            driver.setStatus(DriverStatus.AVAILABLE);
            bookingDao.save(booking);
            driverDao.save(driver);
            logger.info("Booking completed and driver status updated to available for Booking ID: {}", bookingId);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Booking completed and driver status updated to available.");
            responseStructure.setData(booking);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to complete booking with ID: {}. Error: {}", bookingId, e.getMessage());
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage("Failed to complete booking: " + e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<Booking>> findById(long id) {
        logger.info("Attempting to find booking by ID: {}", id);
        ResponseStructure<Booking> responseStructure = new ResponseStructure<>();
        Optional<Booking> booking = bookingDao.findById(id);
        if (booking.isPresent()) {
            Booking booking1 = booking.get();
            logger.info("Booking found with ID: {}", id);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Booking found by id " + id);
            responseStructure.setData(booking1);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("Booking not found with ID: {}", id);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Booking does not exist ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<Page<Booking>>> findAll(int offset, int pageSize, String field) {
        logger.info("Attempting to find all bookings with offset: {}, pageSize: {}, and field: {}", offset, pageSize, field);
        ResponseStructure<Page<Booking>> responseStructure = new ResponseStructure<>();
        Page<Booking> bookings = bookingDao.findAll(offset, pageSize, field);
        if (bookings != null) {
            logger.info("Found bookings data");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Found all the data of bookings ");
            responseStructure.setData(bookings);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("No data exists to find of bookings");
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No data exists to find of bookings");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<List<Booking>>> findBookingByDriverId(long driverId) {
        logger.info("Attempting to find bookings for driver with ID: {}", driverId);
        ResponseStructure<List<Booking>> responseStructure = new ResponseStructure<>();
        List<Booking> bookings = bookingDao.findByDriverId(driverId);

        if (!bookings.isEmpty()) {
            logger.info("Bookings found for driver with ID: {}", driverId);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Bookings found for driver with ID " + driverId);
            responseStructure.setData(bookings);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            logger.warn("No bookings found for driver with ID: {}", driverId);
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No bookings found for driver with ID " + driverId);
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }
}