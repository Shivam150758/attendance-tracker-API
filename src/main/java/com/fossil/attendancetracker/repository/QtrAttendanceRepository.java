package com.fossil.attendancetracker.repository;

import com.fossil.attendancetracker.model.QtrAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QtrAttendanceRepository extends MongoRepository<QtrAttendance, String> {

}
