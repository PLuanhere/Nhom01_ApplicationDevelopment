package nhom01.app.model;

public class InspectionQueueItem {
    private final String receptionNumber;
    private final String licensePlate;
    private final String vehicleName;
    private final int mileage;
    private final String customerRequest;
    private final String initialCondition;
    private final String status;
    private final boolean valid;

    public InspectionQueueItem(String receptionNumber, String licensePlate, String vehicleName, int mileage,
            String customerRequest, String initialCondition, String status, boolean valid) {
        this.receptionNumber = receptionNumber;
        this.licensePlate = licensePlate;
        this.vehicleName = vehicleName;
        this.mileage = mileage;
        this.customerRequest = customerRequest;
        this.initialCondition = initialCondition;
        this.status = status;
        this.valid = valid;
    }

    public String getReceptionNumber() {
        return receptionNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public int getMileage() {
        return mileage;
    }

    public String getCustomerRequest() {
        return customerRequest;
    }

    public String getInitialCondition() {
        return initialCondition;
    }

    public String getStatus() {
        return status;
    }

    public boolean isValid() {
        return valid;
    }
}