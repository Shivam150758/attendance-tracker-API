package com.fossil.attendancetracker.repositoryImpl;

import com.fossil.attendancetracker.model.Attendance;
import com.fossil.attendancetracker.model.QtrAttendance;
import com.fossil.attendancetracker.repository.Attendance2Repository;
import com.fossil.attendancetracker.repository.AttendanceRepository;
import com.fossil.attendancetracker.repository.DateWiseRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QtrAttendanceRepositoryImpl implements DateWiseRepository {

    @Autowired
    MongoClient client;
    @Autowired
    AttendanceRepository attendanceRepo;
    @Autowired
    Attendance2Repository attendance2Repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private static QtrAttendance setAttendanceForNewUser(QtrAttendance attendance) {
        QtrAttendance qtrAttendance = new QtrAttendance();
        qtrAttendance.setId(attendance.getId());
        qtrAttendance.setEmailId(attendance.getEmailId());
        qtrAttendance.setQuarter(attendance.getQuarter());
        qtrAttendance.setYear(attendance.getYear());
        qtrAttendance.setName(attendance.getName());

        switch (attendance.getAttendance()) {
            case "Work From Home" -> {
                qtrAttendance.setWfh(1);
                qtrAttendance.setWfo(0);
                qtrAttendance.setWfhFriday(0);
                qtrAttendance.setWfoFriday(0);
                qtrAttendance.setHolidays(0);
                qtrAttendance.setLeaves(0);
            }
            case "Work From Office" -> {
                qtrAttendance.setWfh(0);
                qtrAttendance.setWfo(1);
                qtrAttendance.setWfhFriday(0);
                qtrAttendance.setWfoFriday(0);
                qtrAttendance.setHolidays(0);
                qtrAttendance.setLeaves(0);
            }
            case "Public Holiday" -> {
                qtrAttendance.setWfh(0);
                qtrAttendance.setWfo(0);
                qtrAttendance.setWfhFriday(0);
                qtrAttendance.setWfoFriday(0);
                qtrAttendance.setHolidays(1);
                qtrAttendance.setLeaves(0);
            }
            case "Leave" -> {
                qtrAttendance.setWfh(0);
                qtrAttendance.setWfo(0);
                qtrAttendance.setWfhFriday(0);
                qtrAttendance.setWfoFriday(0);
                qtrAttendance.setHolidays(0);
                qtrAttendance.setLeaves(1);
            }
            case "Work From Office - Friday" -> {
                qtrAttendance.setWfh(0);
                qtrAttendance.setWfo(0);
                qtrAttendance.setWfhFriday(0);
                qtrAttendance.setWfoFriday(1);
                qtrAttendance.setHolidays(0);
                qtrAttendance.setLeaves(0);
            }
            case "Work From Home - Friday" -> {
                qtrAttendance.setWfh(0);
                qtrAttendance.setWfo(0);
                qtrAttendance.setWfhFriday(1);
                qtrAttendance.setWfoFriday(0);
                qtrAttendance.setHolidays(0);
                qtrAttendance.setLeaves(0);
            }
        }
        return qtrAttendance;
    }

    @Override
    public ResponseEntity<?> findByUserId(Attendance attendance) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("attendance");
        Document query = new Document("_id", attendance.getId());
        Document found = collection.find(query).first();

        if (found != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Attendance record already exists for this user.");
        } else {
            Attendance savedAttendance = attendanceRepo.save(attendance);
            return ResponseEntity.ok(savedAttendance);
        }
    }

    @Override
    public ResponseEntity<?> addUserAttendance(QtrAttendance attendance) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection2 = database.getCollection("qtrAttendance");
        Document query = new Document("_id", attendance.getId());
        Document found = collection2.find(query).first();

        if (found != null) {
            String input = attendance.getAttendance();
            switch (input) {
                case "Work From Home - Friday" -> {
                    int newWfhCount = found.getInteger("wfhFriday", 0) + 1;
                    Document update = new Document("$set", new Document("wfhFriday", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfhFriday", newWfhCount);
                }
                case "Work From Office - Friday" -> {
                    int newWfhCount = found.getInteger("wfoFriday", 0) + 1;
                    Document update = new Document("$set", new Document("wfoFriday", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfoFriday", newWfhCount);
                }
                case "Leave" -> {
                    int newWfhCount = found.getInteger("leaves", 0) + 1;
                    Document update = new Document("$set", new Document("leaves", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("leaves", newWfhCount);
                }
                case "Public Holiday" -> {
                    int newWfhCount = found.getInteger("holidays", 0) + 1;
                    Document update = new Document("$set", new Document("holidays", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("holidays", newWfhCount);
                }
                case "Work From Office" -> {
                    int newWfhCount = found.getInteger("wfo", 0) + 1;
                    Document update = new Document("$set", new Document("wfo", newWfhCount));
                    collection2.updateOne(query, update);
                    found.put("wfo", newWfhCount);
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
            QtrAttendance qtrAttendance = setAttendanceForNewUser(attendance);
            attendance2Repository.save(qtrAttendance);
            return ResponseEntity.ok(qtrAttendance);
        }
    }

    @Override
    public QtrAttendance getUserAttendance(QtrAttendance qtrAttendance) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection2 = database.getCollection("qtrAttendance");
        Document query = new Document("_id", qtrAttendance.getId());
        Document found = collection2.find(query).first();

        if (found != null) {
            QtrAttendance att = new QtrAttendance();
            att.setYear(found.getString("year"));
            att.setQtr(found.getString("quarter"));
            att.setName(found.getString("name"));
            att.setWfo(found.getInteger("wfo"));
            att.setWfoFriday(found.getInteger("wfoFriday"));
            att.setWfh(found.getInteger("wfh"));
            att.setWfhFriday(found.getInteger("wfhFriday"));
            att.setLeaves(found.getInteger("leaves"));
            att.setHolidays(found.getInteger("holidays"));
            return att;
        } else {
            return null;
        }
    }

    @Override
    public List<Attendance> findByAttendanceType(String emailId, String quarter, String year, String attendance) {
        Query query = new Query();
        query.addCriteria(Criteria.where("emailId").is(emailId).and("quarter").is(quarter).and("year").is(year).and("attendance").is(attendance));

        return mongoTemplate.find(query, Attendance.class);
    }
}
