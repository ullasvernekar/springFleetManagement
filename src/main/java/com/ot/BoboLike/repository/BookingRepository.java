package com.ot.BoboLike.repository;

import com.ot.BoboLike.dto.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    public List<Booking> findBookingByDriverId(long driverId);

}