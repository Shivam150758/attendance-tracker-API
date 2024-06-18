package com.fossil.attendancetracker.repositoryImpl;

import com.fossil.attendancetracker.model.ErrorMessage;
import com.fossil.attendancetracker.model.Users;
import com.fossil.attendancetracker.repository.SearchRepository;
import com.fossil.attendancetracker.repository.UsersRepository;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SearchRepositoryImpl implements SearchRepository {

    @Autowired
    MongoClient client;

    @Autowired
    MongoConverter converter;

    @Autowired
    UsersRepository usersRepo;

    @Override
    public List<Users> findUserbyEmail(String text) {
        List<Users> user = new ArrayList<>();
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        AggregateIterable<Document> result = collection.aggregate(List.of(new Document("$match", new Document("emailId", text))));

        result.forEach(doc -> user.add(converter.read(Users.class, doc)));
        return user;
    }

    @Override
    public List<Users> findUserbyName(String text) {

        List<Users> users = new ArrayList<>();
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        Bson caseInsensitiveSearch = Aggregates.match(Filters.regex("name", Pattern.quote(text), "i"));

        AggregateIterable<Document> result = collection.aggregate(List.of(caseInsensitiveSearch));

        result.forEach(doc -> users.add(converter.read(Users.class, doc)));
        return users;
    }

    @Override
    public List<String> findAllDistinctDepartment() {

        List<String> teams = new ArrayList<>();
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        collection.distinct("department", String.class).iterator().forEachRemaining(teams::add);

        return teams;
    }

    @Override
    public List<String> findAllDistinctSubDept() {

        List<String> teams = new ArrayList<>();
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        collection.distinct("subDep", String.class).iterator().forEachRemaining(teams::add);

        return teams;
    }

    @Override
    public List<String> findAllDistinctTeams() {

        List<String> teams = new ArrayList<>();
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        collection.distinct("team", String.class).iterator().forEachRemaining(teams::add);

        return teams;
    }

    @Override
    public ResponseEntity<?> createUser(Users user) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        Document found = collection.find(new Document("emailId", user.getEmailId())).first();

        if (found == null) {
            Users authenticatedUser = getUsers(user);
            usersRepo.save(authenticatedUser);
            return ResponseEntity.ok(authenticatedUser);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessage("User already exist"));
        }
    }

    private static Users getUsers(Users user) {
        Users authenticatedUser = new Users();
        authenticatedUser.setId(user.getEmailId());
        authenticatedUser.setEmailId(user.getEmailId());
        authenticatedUser.setName(user.getName());
        authenticatedUser.setTeam(user.getTeam());
        authenticatedUser.setPassword(user.getPassword());
        authenticatedUser.setManagerId(user.getManagerId());
        authenticatedUser.setLastLogin(new Date());
        return authenticatedUser;
    }

    @Override
    public ResponseEntity<?> checkUserCred(Users user) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        Document found = collection.find(eq("emailId", user.getEmailId())).first();

        if (found != null) {
            String storedPassword = found.getString("password");
            if (storedPassword.equals(user.getPassword())) {
                collection.updateOne(eq("emailId", user.getEmailId()), Updates.set("lastLogin", new Date()));
                Users authenticatedUser = new Users();
                authenticatedUser.setName(found.getString("name"));
                authenticatedUser.setTeam(found.getString("team"));
                authenticatedUser.setEmailId(found.getString("emailId"));
                authenticatedUser.setPassword(found.getString("password"));
                authenticatedUser.setLastLogin(new Date());

                return ResponseEntity.ok(authenticatedUser);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorMessage("Invalid credentials"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage("User not found"));
        }
    }

    @Override
    public List<Users> getSubordinates(Users manager) {
        List<Users> subordinates = usersRepo.findByManagerId(manager.getEmailId());
        List<Users> allSubordinates = new ArrayList<>(subordinates);

        for (Users subordinate : subordinates) {
            List<Users> nestedSubordinates = getSubordinates(subordinate);
            allSubordinates.addAll(nestedSubordinates);
        }
        return allSubordinates;
    }

    @Override
    public Users resetUsersPassword(Users user) {
        MongoDatabase database = client.getDatabase("digital-GBS");
        MongoCollection<Document> collection = database.getCollection("users");

        collection.updateOne(eq("emailId", user.getEmailId()), set("password", user.getPassword()));

        return user;
    }
}