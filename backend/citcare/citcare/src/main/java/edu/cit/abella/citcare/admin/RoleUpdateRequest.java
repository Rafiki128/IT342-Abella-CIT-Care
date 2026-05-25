package edu.cit.abella.citcare.admin;

public class RoleUpdateRequest {
    private String role; // NEW, STUDENT, MEDICAL_STAFF, GUIDANCE_STAFF, or ADMIN

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
