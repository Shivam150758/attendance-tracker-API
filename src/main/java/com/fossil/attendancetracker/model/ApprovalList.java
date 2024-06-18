package com.fossil.attendancetracker.model;

import org.springframework.data.annotation.Id;

public class ApprovalList {

    @Id
    private String Id;
    private String raisedBy;
    private String raisedTo;
    private String comments;
    private String status;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getRaisedBy() {
        return raisedBy;
    }

    public void setRaisedBy(String raisedBy) {
        this.raisedBy = raisedBy;
    }

    public String getRaisedTo() {
        return raisedTo;
    }

    public void setRaisedTo(String raisedTo) {
        this.raisedTo = raisedTo;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
