package edu.cit.abella.citcare.appointments;

public class RejectionRequest {
    private Long staffId;
    private String reason;

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
