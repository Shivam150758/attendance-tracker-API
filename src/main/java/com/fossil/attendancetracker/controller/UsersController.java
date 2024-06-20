package com.fossil.attendancetracker.controller;

import com.fossil.attendancetracker.model.Users;
import com.fossil.attendancetracker.repository.SearchRepository;
import com.fossil.attendancetracker.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UsersController {

    @Autowired
    UsersRepository usersRepo;

    @Autowired
    SearchRepository searchRepo;

    @GetMapping(value = "/allUsers")
    public List<Users> getAllUsers() {
        return usersRepo.findAll();
    }

    @GetMapping(value = "/searchByEmail/{email}")
    public List<Users> searchUserByEmail(@PathVariable String email) {
        return searchRepo.findUserbyEmail(email);
    }

    @GetMapping(value = "/searchByName/{name}")
    public List<Users> searchUserByName(@PathVariable String name) {
        return searchRepo.findUserbyName(name);
    }

    @PostMapping(value = "/addUser")
    public ResponseEntity<?> addUser(@RequestBody Users user) {
        return searchRepo.createUser(user);
    }

    @GetMapping(value = "/distTeams")
    public List<String> findDistTeam() {
        return searchRepo.findAllDistinctTeams();
    }

    @GetMapping(value = "/distSubDepartment")
    public List<String> findDistSubDepartment() {
        return searchRepo.findAllDistinctSubDept();
    }

    @GetMapping(value = "/distDepartment")
    public List<String> findDistDepartment() {
        return searchRepo.findAllDistinctDepartment();
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> checkUserLogin(@RequestBody Users cred) {
        return searchRepo.checkUserCred(cred);
    }

    @PostMapping("/subordinates")
    public List<Users> getSubordinates(@RequestBody Users managerId) {
        return searchRepo.getSubordinates(managerId);
    }

    @PostMapping("/resetPassword")
    public Users resetUserPassword(@RequestBody Users user) {
        return searchRepo.resetUsersPassword(user);
    }
}
