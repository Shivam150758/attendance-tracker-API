package com.fossil.attendancetracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fossil.attendancetracker.model.Users;

import java.util.List;

public interface UsersRepository extends MongoRepository<Users, String> {

    List<Users> findByManagerId(String managerId);

}
