package nhom01.app.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import nhom01.app.model.InspectionQueueItem;
import nhom01.app.model.InspectionReport;

public class InspectionService {
    private final List<InspectionReport> reports;
    private final List<InspectionQueueItem> queueItems;
    private boolean saveErrorTriggered;

    public InspectionService() {
        reports = new ArrayList<>();
        queueItems = new ArrayList<>();
        createMockData();
    }

    // Tạo dữ liệu mẫu để chạy feature trong lúc chưa kết nối SQL Server
    private void createMockData() {
        reports.add(new InspectionReport("KT-2026-0112", "TN-2026-0111", "51K-001.23", "Chung Ngô Minh Hoàng",
                LocalDate.of(2026, 10, 5), "Điều hòa", "Làm lạnh yếu", "Lọc gió bẩn",
                "Vệ sinh lọc gió và đo áp suất", "Nháp"));

        InspectionReport completed = new InspectionReport("KT-2026-0108", "TN-2026-0108", "51H-246.80",
                "Chung Ngô Minh Hoàng", LocalDate.of(2026, 10, 5), "Tổng hợp", "Đã hoàn tất kiểm tra", "",
                "Bảo dưỡng định kỳ, thay dầu/lọc dầu, vệ sinh phanh", "Hoàn tất");

        completed.setBrakeResult("Bụi bám, má phanh còn đạt");
        completed.setOilResult("Đến kỳ thay");
        completed.setElectricalResult("Bình thường");
        completed.setDiagnosticCode("Không ghi nhận mã lỗi.");

        reports.add(completed);

        queueItems.add(new InspectionQueueItem("TN-2026-0108", "51H-246.80", "Toyota Camry", 45280,
                "Bảo dưỡng định kỳ, kiểm tra tiếng kêu phanh trước.",
                "Tiếng kêu nhẹ khi phanh; thân xe không va quẹt mới.", "Chờ kiểm tra", true));

        queueItems.add(new InspectionQueueItem("TN-2026-0111", "51K-001.23", "Honda City", 38120,
                "Kiểm tra điều hòa", "Điều hòa làm lạnh yếu, chưa phát hiện bất thường khác.",
                "Chờ kiểm tra", false));
    }

    // Lấy danh sách báo cáo dưới dạng bản sao
    public List<InspectionReport> getReports() {
        List<InspectionReport> result = new ArrayList<>();

        for (InspectionReport report : reports) {
            result.add(report.copy());
        }

        return result;
    }

    // Tìm báo cáo theo từ khóa và trạng thái
    public List<InspectionReport> searchReports(String keyword, String status) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        String selectedStatus = status == null ? "Tất cả" : status;
        List<InspectionReport> result = new ArrayList<>();

        for (InspectionReport report : reports) {
            boolean matchKey = key.isEmpty()
                    || report.getReportNumber().toLowerCase().contains(key)
                    || report.getReceptionNumber().toLowerCase().contains(key)
                    || report.getLicensePlate().toLowerCase().contains(key);

            boolean matchStatus = "Tất cả".equals(selectedStatus) || selectedStatus.equals(report.getStatus());

            if (matchKey && matchStatus) {
                result.add(report.copy());
            }
        }

        return result;
    }

    // Lấy danh sách phiếu đang chờ kiểm tra
    public List<InspectionQueueItem> getInspectionQueue() {
        return new ArrayList<>(queueItems);
    }

    // Tìm phiếu tiếp nhận theo mã phiếu
    public InspectionQueueItem findQueueItem(String receptionNumber) {
        String normalized = normalizeReceptionNumber(receptionNumber);

        for (InspectionQueueItem item : queueItems) {
            if (item.getReceptionNumber().equals(normalized)) {
                return item;
            }
        }

        return null;
    }

    // Tìm báo cáo theo số báo cáo
    public InspectionReport findReport(String reportNumber) {
        for (InspectionReport report : reports) {
            if (report.getReportNumber().equals(reportNumber)) {
                return report.copy();
            }
        }

        return null;
    }

    // Xem trước số báo cáo kế tiếp
    public String previewNextReportNumber() {
        return createNextReportNumber();
    }

    // Kiểm tra các trường bắt buộc trước khi lưu báo cáo
    public String validateReport(InspectionReport report) {
        if (isBlank(report.getReceptionNumber())) return "RECEPTION";
        if (report.getInspectionDate() == null) return "DATE";
        if (isBlank(report.getDepartment())) return "DEPARTMENT";
        if (isBlank(report.getInspectionResult())) return "RESULT";
        if (isBlank(report.getRepairProposal())) return "PROPOSAL";

        return null;
    }

    // Thêm báo cáo mới ở trạng thái Nháp
    public InspectionReport createReport(InspectionReport report) {
        InspectionReport saved = report.copy();

        saved.setReportNumber(createNextReportNumber());
        saved.setReceptionNumber(normalizeReceptionNumber(saved.getReceptionNumber()));
        saved.setStatus("Nháp");

        InspectionQueueItem queueItem = findQueueItem(saved.getReceptionNumber());

        if (queueItem != null) {
            saved.setLicensePlate(queueItem.getLicensePlate());
        } else if (isBlank(saved.getLicensePlate())) {
            saved.setLicensePlate("51K-001.23");
        }

        reports.add(saved);

        return saved.copy();
    }

    // Cập nhật báo cáo nháp đang tồn tại
    public boolean updateReport(InspectionReport updated) {
        for (int i = 0; i < reports.size(); i++) {
            InspectionReport current = reports.get(i);

            if (!current.getReportNumber().equals(updated.getReportNumber())
                    || !"Nháp".equals(current.getStatus())) {
                continue;
            }

            InspectionReport saved = updated.copy();

            saved.setReceptionNumber(normalizeReceptionNumber(saved.getReceptionNumber()));
            saved.setLicensePlate(current.getLicensePlate());
            saved.setStatus(current.getStatus());

            reports.set(i, saved);

            return true;
        }

        return false;
    }

    // Kiểm tra báo cáo có được phép xóa hay không
    public boolean canDelete(InspectionReport report) {
        return report != null && "Nháp".equals(report.getStatus());
    }

    // Xóa một báo cáo nháp theo số báo cáo
    public boolean deleteReport(String reportNumber) {
        for (int i = 0; i < reports.size(); i++) {
            InspectionReport report = reports.get(i);

            if (report.getReportNumber().equals(reportNumber) && canDelete(report)) {
                reports.remove(i);
                return true;
            }
        }

        return false;
    }

    // Hoàn tất kiểm tra và sinh báo cáo chính thức
    public InspectionReport completeInspection(InspectionQueueItem item, String brakeResult, String oilResult,
            String electricalResult, String diagnosticCode, String proposal) {
        if (item == null || !item.isValid()) return null;
        if (isBlank(brakeResult) || isBlank(proposal)) return null;

        if ("SAVE_ERROR".equalsIgnoreCase(diagnosticCode == null ? "" : diagnosticCode.trim())
                && !saveErrorTriggered) {
            saveErrorTriggered = true;
            return null;
        }

        InspectionReport report = new InspectionReport(createNextReportNumber(), item.getReceptionNumber(),
                item.getLicensePlate(), "Chung Ngô Minh Hoàng", LocalDate.of(2026, 10, 5), "Tổng hợp",
                "Đã hoàn tất kiểm tra", "", proposal, "Hoàn tất");

        report.setBrakeResult(brakeResult);
        report.setOilResult(oilResult);
        report.setElectricalResult(electricalResult);
        report.setDiagnosticCode(diagnosticCode);

        reports.add(report);

        return report.copy();
    }

    // Sinh số báo cáo kế tiếp từ dữ liệu hiện có
    private String createNextReportNumber() {
        int max = 0;
        for (InspectionReport report : reports) {
            String number = report.getReportNumber();
            int index = number.lastIndexOf('-');
            if (index < 0 || index == number.length() - 1) {
                continue;
            }

            try {
                max = Math.max(max, Integer.parseInt(number.substring(index + 1)));
            } catch (NumberFormatException e) {
                max = Math.max(max, 0);
            }
        }

        return String.format("KT-2026-%04d", max + 1);
    }

    // Bỏ phần mô tả trạng thái phía sau mã phiếu nếu có
    private String normalizeReceptionNumber(String value) {
        if (value == null) return "";
        int index = value.indexOf('·');

        return index >= 0 ? value.substring(0, index).trim() : value.trim();
    }

    // Kiểm tra chuỗi null hoặc rỗng
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}