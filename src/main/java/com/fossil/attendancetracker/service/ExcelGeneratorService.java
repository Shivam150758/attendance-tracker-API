package com.fossil.attendancetracker.service;

import com.fossil.attendancetracker.model.Attendance;
import com.fossil.attendancetracker.model.Users;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelGeneratorService {
    public ByteArrayInputStream generateExcel(List<Users> users, List<Attendance> attendances, int year, int month) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance");

            // Header
//            Row headerRow = sheet.createRow(0);
//            String[] columns = {"Emp ID", "SAP ID", "Emp Name", "Region", "Manager", "Work Location", "Shift"};
//            for (int i = 0; i < columns.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(columns[i]);
//                cell.setCellStyle(createHeaderCellStyle(workbook));
//            }

            // Dates row
            Row dateRow = sheet.createRow(0);
            Row dayRow = sheet.createRow(1);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");

            // Static Columns
            String[] columns = {"Emp ID", "SAP ID", "Emp Name", "Region", "Manager", "Work Location", "Shift"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = dayRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(createHeaderCellStyle(workbook));
            }

            // Date and Day Columns
            int dayIndex = columns.length;
            for (int day = 1; day <= getDaysInMonth(year, month); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                Cell dateCell = dateRow.createCell(dayIndex);
                dateCell.setCellValue(date.format(dateFormatter));
                dateCell.setCellStyle(createHeaderCellStyle(workbook));

                Cell dayCell = dayRow.createCell(dayIndex);
                dayCell.setCellValue(date.format(dayFormatter));
                dayCell.setCellStyle(createHeaderCellStyle(workbook));

                dayIndex++;
            }

            // Fill data
            int rowIdx = 2;
            for (Users user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getEmpId());
                row.createCell(1).setCellValue(user.getSapId());
                row.createCell(2).setCellValue(user.getName());
                row.createCell(3).setCellValue(user.getRegion());
                row.createCell(4).setCellValue(user.getManagerName());
                row.createCell(5).setCellValue(user.getWorkLocation());
                row.createCell(6).setCellValue(user.getShift());

                List<Attendance> userAttendances = filterAttendancesByUser(attendances, user.getEmailId());
                dayIndex = 7;
                for (int day = 1; day <= getDaysInMonth(year, month); day++) {
                    String attendanceStatus = getAttendanceStatusForDay(userAttendances, year, month, day);
                    row.createCell(dayIndex++).setCellValue(attendanceStatus);
                }
            }

            for (int i = 0; i < dayIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);
        return cellStyle;
    }

    private int getDaysInMonth(int year, int month) {
        return java.time.YearMonth.of(year, month).lengthOfMonth();
    }

    private List<Attendance> filterAttendancesByUser(List<Attendance> attendances, String emailId) {
        return attendances.stream().filter(att -> att.getEmailId().equals(emailId)).toList();
    }

    private String getAttendanceStatusForDay(List<Attendance> attendances, int year, int month, int day) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");
        String formattedDate = LocalDate.of(year, month, day).format(formatter);

        return attendances.stream()
                .filter(att -> {
                    return att.getDate().equals(formattedDate);
                })
                .map(Attendance::getShift)
                .findFirst()
                .orElse("Not marked");
    }
}
