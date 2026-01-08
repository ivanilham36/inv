/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.inventaris.model;

public class AuditLog {

    private String timestamp;
    private String userName;
    private String action;
    private String description;
    private String ipAddress;
    private String status;

    public AuditLog(
            String timestamp,
            String userName,
            String action,
            String description,
            String ipAddress,
            String status
    ) {
        this.timestamp = timestamp;
        this.userName = userName;
        this.action = action;
        this.description = description;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getStatus() {
        return status;
    }
}

