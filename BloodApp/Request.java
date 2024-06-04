package com.example.nhs_blood_staff_app;

public class Request {
    private String bloodType;
    private String reqDate;
    private String endDate;

    public Request() {
        // Empty Constructor required by Firebase
    }

    public Request(String bloodType, String reqDate, String endDate) {
        this.bloodType = bloodType;
        this.reqDate = reqDate;
        this.endDate = endDate;
    }

    public String getbloodType() {
        return bloodType;
    }

    public void setbloodType(String bloodtype) {
        this.bloodType = bloodtype;
    }

    public String getreqDate() {
        return reqDate;
    }

    public void setreqDate(String reqdate) {
        this.reqDate = reqdate;
    }

    public String getendDate() {
        return endDate;
    }

    public void setendDate(String enddate) {
        this.endDate = enddate;
    }
}