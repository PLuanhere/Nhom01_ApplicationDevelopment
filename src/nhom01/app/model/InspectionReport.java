package nhom01.app.model;

import java.time.LocalDate;

public class InspectionReport {
    private String reportNumber;
    private String receptionNumber;
    private String licensePlate;
    private String technicianName;
    private LocalDate inspectionDate;
    private String department;
    private String inspectionResult;
    private String detectedDamage;
    private String repairProposal;
    private String status;

    private String brakeResult;
    private String oilResult;
    private String electricalResult;
    private String diagnosticCode;

    public InspectionReport(String reportNumber, String receptionNumber, String licensePlate, String technicianName,
            LocalDate inspectionDate, String department, String inspectionResult, String detectedDamage,
            String repairProposal, String status) {
        this.reportNumber = reportNumber;
        this.receptionNumber = receptionNumber;
        this.licensePlate = licensePlate;
        this.technicianName = technicianName;
        this.inspectionDate = inspectionDate;
        this.department = department;
        this.inspectionResult = inspectionResult;
        this.detectedDamage = detectedDamage;
        this.repairProposal = repairProposal;
        this.status = status;
    }

    // Tạo bản sao để giao diện không sửa trực tiếp dữ liệu đang giữ trong Service
    public InspectionReport copy() {
        InspectionReport copy = new InspectionReport(reportNumber, receptionNumber, licensePlate, technicianName,
                inspectionDate, department, inspectionResult, detectedDamage, repairProposal, status);

        copy.setBrakeResult(brakeResult);
        copy.setOilResult(oilResult);
        copy.setElectricalResult(electricalResult);
        copy.setDiagnosticCode(diagnosticCode);

        return copy;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getReceptionNumber() {
        return receptionNumber;
    }

    public void setReceptionNumber(String receptionNumber) {
        this.receptionNumber = receptionNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDate inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getInspectionResult() {
        return inspectionResult;
    }

    public void setInspectionResult(String inspectionResult) {
        this.inspectionResult = inspectionResult;
    }

    public String getDetectedDamage() {
        return detectedDamage;
    }

    public void setDetectedDamage(String detectedDamage) {
        this.detectedDamage = detectedDamage;
    }

    public String getRepairProposal() {
        return repairProposal;
    }

    public void setRepairProposal(String repairProposal) {
        this.repairProposal = repairProposal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrakeResult() {
        return brakeResult;
    }

    public void setBrakeResult(String brakeResult) {
        this.brakeResult = brakeResult;
    }

    public String getOilResult() {
        return oilResult;
    }

    public void setOilResult(String oilResult) {
        this.oilResult = oilResult;
    }

    public String getElectricalResult() {
        return electricalResult;
    }

    public void setElectricalResult(String electricalResult) {
        this.electricalResult = electricalResult;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }

    public void setDiagnosticCode(String diagnosticCode) {
        this.diagnosticCode = diagnosticCode;
    }
}