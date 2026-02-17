package com.tribiz.repository;

import com.tribiz.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByShopId(Long shopId);

    List<Appointment> findByCustomerId(Long customerId);

    List<Appointment> findByServiceId(Long serviceId);
}
