package edu.cit.abella.citcare.dto;

public class RoleUpdateRequest {
    private String role; // e.g., "STAFF" or "ADMIN"

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}