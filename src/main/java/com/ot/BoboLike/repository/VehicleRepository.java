package com.ot.BoboLike.repository;

import com.ot.BoboLike.dto.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface VehicleRepository extends JpaRepository<Vehicle,Long> {

    public List<Vehicle> findByVehicleNumberPlate(String vehicleNumberPlate);

    public List<Vehicle> findVehicleByDriverId(long driverId);
}
