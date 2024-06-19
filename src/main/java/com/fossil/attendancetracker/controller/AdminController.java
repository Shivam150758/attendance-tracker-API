package com.fossil.attendancetracker.controller;

import com.fossil.attendancetracker.model.ApprovalList;
import com.fossil.attendancetracker.model.Attendance;
import com.fossil.attendancetracker.model.MonthlyAttendance;
import com.fossil.attendancetracker.model.QtrAttendance;
import com.fossil.attendancetracker.repository.AdminMethodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminController {

    @Autowired
    AdminMethodsRepository adminMethodsRepository;

    @PostMapping(value = "/adminQtrReport")
    public List<QtrAttendance> getAllUserAttendance(@RequestBody Attendance attendance){
        return adminMethodsRepository.getAllUserAttendance(attendance.getQuarter(), attendance.getYear());
    }

    @PostMapping(value = "/adminMthReport")
    public List<MonthlyAttendance> getAllUserMthAttendance(@RequestBody MonthlyAttendance attendance){
        return adminMethodsRepository.getAllUserMthAttendance(attendance.getMonth(), attendance.getYear());
    }

    @GetMapping(value = "/distinctYears")
    public List<String> getDistinctYearValues(){
        return adminMethodsRepository.getAllDistinctYears();
    }

    @GetMapping(value = "/distinctQuarters")
    public List<String> getDistinctQtrValues(){
        return adminMethodsRepository.getAllDistinctQuarters();
    }

    @GetMapping(value = "/distinctMonths")
    public List<String> getDistinctMonthValues() {
        return adminMethodsRepository.getAllDistinctMonths();
    }

    @PostMapping(value = "/userMonthlyAttendance")
    public MonthlyAttendance getUserMonthlyAttendance(@RequestBody MonthlyAttendance monthlyAttendance) {
        return  adminMethodsRepository.getUserMonthlyAttendance(monthlyAttendance);
    }

    @PostMapping(value = "/requestApproval")
    public ApprovalList getApprovalList(@RequestBody ApprovalList approvalList) {
        return adminMethodsRepository.getApprovalList(approvalList.getRaisedBy(), approvalList.getRaisedTo());
    }

    @PostMapping(value = "/saveForApproval")
    public String saveApprovalRequest(@RequestBody ApprovalList approvalList) {
        return adminMethodsRepository.saveApprovalList(approvalList);
    }
}
