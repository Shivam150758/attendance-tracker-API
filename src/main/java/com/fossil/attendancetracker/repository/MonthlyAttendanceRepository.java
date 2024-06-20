package com.fossil.attendancetracker.repository;

import com.fossil.attendancetracker.model.MonthlyAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonthlyAttendanceRepository extends MongoRepository<MonthlyAttendance, String> {
}
