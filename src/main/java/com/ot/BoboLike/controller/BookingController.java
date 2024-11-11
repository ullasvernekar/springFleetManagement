package com.ot.BoboLike.controller;

import com.ot.BoboLike.dto.Booking;
import com.ot.BoboLike.dto.ResponseStructure;
import com.ot.BoboLike.dto.request.BookingInitiate;
import com.ot.BoboLike.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/save")
    public ResponseEntity<ResponseStructure<Booking>> createBooking(@RequestBody BookingInitiate booking) {
        return bookingService.assignDriver(booking);
    }

    @PutMapping("/complete/{bookingId}")
    public ResponseEntity<ResponseStructure<Booking>> completeBooking(@PathVariable long bookingId) {
        return bookingService.completeBooking(bookingId);
    }

    @GetMapping("/findBookingByDriverId")
    public ResponseEntity<ResponseStructure<List<Booking>>> findByDriverId(@RequestParam long driverId){
        return bookingService.findBookingByDriverId(driverId);
    }

    @GetMapping("/findAll")
    public ResponseEntity<ResponseStructure<Page<Booking>>> findAll(@RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "5") int pageSize,
                                                                    @RequestParam(defaultValue = "id") String field){
        return bookingService.findAll(offset, pageSize, field);
    }
}
