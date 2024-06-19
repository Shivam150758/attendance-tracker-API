package com.fossil.attendancetracker.repositoryImpl;

import com.fossil.attendancetracker.model.ApprovalList;
import com.fossil.attendancetracker.model.MonthlyAttendance;
import com.fossil.attendancetracker.model.QtrAttendance;
import com.fossil.attendancetracker.repository.AdminMethodsRepository;
import com.fossil.attendancetracker.repository.MonthlyAttendanceRepository;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class AdminRepositoryImpl implements AdminMethodsRepository {

    @Autowired
    MongoClient client;

    @Autowired
    MonthlyAttendanceRepository monthlyAttendanceRepository;

    @Override
    public List<QtrAttendance> getAllUserAttendance(String quarter, String year) {

        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("qtrAttendance");

        Bson filter = Filters.and(
                Filters.eq("year", year),
                Filters.eq("quarter", quarter)
        );

        List<QtrAttendance> attendanceList = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                QtrAttendance attendance = new QtrAttendance();
                attendance.setYear(doc.getString("year"));
                attendance.setName(doc.getString("name"));
                attendance.setQuarter(doc.getString("quarter"));
                attendance.setEmailId(doc.getString("emailId"));
                attendance.setWfh(doc.getInteger("wfh"));
                attendance.setWfo(doc.getInteger("wfo"));
                attendance.setWfoFriday(doc.getInteger("wfoFriday"));
                attendance.setWfhFriday(doc.getInteger("wfhFriday"));
                attendance.setLeaves(doc.getInteger("leaves"));
                attendance.setHolidays(doc.getInteger("holidays"));
                attendanceList.add(attendance);
            }
        }
        return attendanceList;
    }

    @Override
    public List<MonthlyAttendance> getAllUserMthAttendance(String month, String year) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("monthlyAttendance");

        Bson filter = Filters.and(
                Filters.eq("year", year),
                Filters.eq("month", month)
        );

        List<MonthlyAttendance> attendanceList = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                MonthlyAttendance attendance = new MonthlyAttendance();
                attendance.setYear(doc.getString("year"));
                attendance.setName(doc.getString("name"));
                attendance.setMonth(doc.getString("month"));
                attendance.setEmailId(doc.getString("emailId"));
                attendance.setWfh(doc.getInteger("wfh"));
                attendance.setWfo(doc.getInteger("wfo"));
                attendance.setWfoFriday(doc.getInteger("wfoFriday"));
                attendance.setWfhFriday(doc.getInteger("wfhFriday"));
                attendance.setLeaves(doc.getInteger("leaves"));
                attendance.setHolidays(doc.getInteger("holidays"));
                attendanceList.add(attendance);
            }
        }
        return attendanceList;
    }

    @Override
    public List<String> getAllDistinctYears() {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("qtrAttendance");

        return collection.distinct("year", String.class).into(new ArrayList<>());
    }

    @Override
    public List<String> getAllDistinctQuarters() {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("qtrAttendance");

        return collection.distinct("quarter", String.class).into(new ArrayList<>());
    }

    @Override
    public List<String> getAllDistinctMonths() {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("monthlyAttendance");

        return collection.distinct("month", String.class).into(new ArrayList<>());
    }

    @Override
    public ResponseEntity<?> addUserMonthlyAttendance(MonthlyAttendance attendance) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection2 = database.getCollection("monthlyAttendance");
        Document query = new Document("_id", attendance.getId());
        Document found = collection2.find(query).first();

        if (found != null) {
            String input = attendance.getAttendance();
            switch (input) {
                case "Work From Home - Friday" -> {
                    int newWfhCount = found.getInteger("wfhFriday", 0) + 1;
                    Document update = new Document("$set", new Document("wfhFriday", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfh", newWfhCount);
                }
                case "Work From Office - Friday" -> {
                    int newWfhCount = found.getInteger("wfoFriday", 0) + 1;
                    Document update = new Document("$set", new Document("wfoFriday", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfh", newWfhCount);
                }
                case "Leave" -> {
                    int newWfhCount = found.getInteger("leaves", 0) + 1;
                    Document update = new Document("$set", new Document("leaves", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfh", newWfhCount);
                }
                case "Public Holiday" -> {
                    int newWfhCount = found.getInteger("holidays", 0) + 1;
                    Document update = new Document("$set", new Document("holidays", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfh", newWfhCount);
                }
                case "Work From Office" -> {
                    int newWfhCount = found.getInteger("wfo", 0) + 1;
                    Document update = new Document("$set", new Document("wfo", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfh", newWfhCount);
                }
                case "Work From Home" -> {
                    int newWfhCount = found.getInteger("wfh", 0) + 1;
                    Document update = new Document("$set", new Document("wfh", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfh", newWfhCount);
                }
            }
            return ResponseEntity.ok(HttpStatus.ACCEPTED);
        } else {
            MonthlyAttendance monthlyAttendance = setAttendanceForNewUser(attendance);
            monthlyAttendanceRepository.save(monthlyAttendance);
            return ResponseEntity.ok(monthlyAttendance);
        }
    }

    private static MonthlyAttendance setAttendanceForNewUser(MonthlyAttendance attendance) {
        MonthlyAttendance monthlyAttendance = new MonthlyAttendance();
        monthlyAttendance.setId(attendance.getId());
        monthlyAttendance.setEmailId(attendance.getEmailId());
        monthlyAttendance.setQuarter(attendance.getQuarter());
        monthlyAttendance.setYear(attendance.getYear());
        monthlyAttendance.setMonth(attendance.getMonth());
        monthlyAttendance.setName(attendance.getName());

        switch (attendance.getAttendance()) {
            case "Work From Home" -> {
                monthlyAttendance.setWfh(1);
                monthlyAttendance.setWfo(0);
                monthlyAttendance.setWfhFriday(0);
                monthlyAttendance.setWfoFriday(0);
                monthlyAttendance.setHolidays(0);
                monthlyAttendance.setLeaves(0);
            }
            case "Work From Office" -> {
                monthlyAttendance.setWfh(0);
                monthlyAttendance.setWfo(1);
                monthlyAttendance.setWfhFriday(0);
                monthlyAttendance.setWfoFriday(0);
                monthlyAttendance.setHolidays(0);
                monthlyAttendance.setLeaves(0);
            }
            case "Public Holiday" -> {
                monthlyAttendance.setWfh(0);
                monthlyAttendance.setWfo(0);
                monthlyAttendance.setWfhFriday(0);
                monthlyAttendance.setWfoFriday(0);
                monthlyAttendance.setHolidays(1);
                monthlyAttendance.setLeaves(0);
            }
            case "Leave" -> {
                monthlyAttendance.setWfh(0);
                monthlyAttendance.setWfo(0);
                monthlyAttendance.setWfhFriday(0);
                monthlyAttendance.setWfoFriday(0);
                monthlyAttendance.setHolidays(0);
                monthlyAttendance.setLeaves(1);
            }
            case "Work From Office - Friday" -> {
                monthlyAttendance.setWfh(0);
                monthlyAttendance.setWfo(0);
                monthlyAttendance.setWfhFriday(0);
                monthlyAttendance.setWfoFriday(1);
                monthlyAttendance.setHolidays(0);
                monthlyAttendance.setLeaves(0);
            }
            case "Work From Home - Friday" -> {
                monthlyAttendance.setWfh(0);
                monthlyAttendance.setWfo(0);
                monthlyAttendance.setWfhFriday(1);
                monthlyAttendance.setWfoFriday(0);
                monthlyAttendance.setHolidays(0);
                monthlyAttendance.setLeaves(0);
            }
        }
        return monthlyAttendance;
    }

    @Override
    public MonthlyAttendance getUserMonthlyAttendance(MonthlyAttendance monthlyAttendance) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection2 = database.getCollection("monthlyAttendance");
        Document found = collection2.find(Filters.eq("_id", monthlyAttendance.getId())).first();

        if (found != null) {
            return new MonthlyAttendance(found);
        } else {
            return null;
        }
    }

    @Override
    public ApprovalList getApprovalList(String raisedBy, String raisedTo) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("approvalList");

        List<Document> raisedByList = new ArrayList<>();
        List<Document> raisedToList = new ArrayList<>();

        FindIterable<Document> raisedByResults = collection.find(new Document("raisedBy", raisedBy));
        for (Document doc : raisedByResults) {
            raisedByList.add(doc);
        }

        FindIterable<Document> raisedToResults = collection.find(new Document("raisedTo", raisedTo));
        for (Document doc : raisedToResults) {
            raisedToList.add(doc);
        }

        ApprovalList approvalList = new ApprovalList();
        approvalList.setRaisedByList(raisedByList);
        approvalList.setRaisedToList(raisedToList);

        return approvalList;
    }

    @Override
    public String saveApprovalList(ApprovalList approvalList) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("approvalList");

        Document query = new Document("_id", approvalList.getId());
        Document existingDocument = collection.find(query).first();

        if (existingDocument != null) {
            return "Error: ApprovalList with this ID already exists.";
        }

        Document document = new Document("_id", approvalList.getId())
                .append("date", approvalList.getDate())
                .append("year", approvalList.getYear())
                .append("quarter", approvalList.getQuarter())
                .append("month", approvalList.getMonth())
                .append("raisedBy", approvalList.getRaisedBy())
                .append("raisedTo", approvalList.getRaisedTo())
                .append("comments", approvalList.getComments())
                .append("status", approvalList.getStatus())
                .append("type", approvalList.getType())
                .append("prevAttendance", approvalList.getPrevAttendance())
                .append("prevShift", approvalList.getPrevShift())
                .append("newAttendance", approvalList.getNewAttendance())
                .append("newShift", approvalList.getNewShift());

        collection.insertOne(document);

        return "ApprovalList saved successfully.";
    }
}
