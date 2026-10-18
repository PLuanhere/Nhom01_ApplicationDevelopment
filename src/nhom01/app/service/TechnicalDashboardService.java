package nhom01.app.service;

import java.time.LocalDate;
import java.util.List;

import nhom01.app.model.TechnicalDashboardData;
import nhom01.app.model.TechnicalDashboardData.CompletionDetail;
import nhom01.app.model.TechnicalDashboardData.DailyCompletion;
import nhom01.app.model.TechnicalDashboardData.WorkGroupStat;

public class TechnicalDashboardService {

    // Tạo dữ liệu mẫu cho dashboard trong lúc chưa kết nối SQL Server
    public TechnicalDashboardData getDashboardData() {
        List<DailyCompletion> dailyCompletions = List.of(
                new DailyCompletion(LocalDate.of(2026, 10, 1), 2),
                new DailyCompletion(LocalDate.of(2026, 10, 2), 3),
                new DailyCompletion(LocalDate.of(2026, 10, 3), 1),
                new DailyCompletion(LocalDate.of(2026, 10, 4), 3),
                new DailyCompletion(LocalDate.of(2026, 10, 5), 4));

        List<CompletionDetail> completionDetails = List.of(
                createCompletionDetail(LocalDate.of(2026, 10, 1), 1, 1, 0),
                createCompletionDetail(LocalDate.of(2026, 10, 2), 1, 1, 1),
                createCompletionDetail(LocalDate.of(2026, 10, 3), 1, 0, 0),
                createCompletionDetail(LocalDate.of(2026, 10, 4), 1, 1, 1),
                createCompletionDetail(LocalDate.of(2026, 10, 5), 2, 1, 1));

        return new TechnicalDashboardData(
                "Chung Ngô Minh Hoàng",
                LocalDate.of(2026, 10, 5),
                3,
                2,
                1,
                4,
                "1 đề xuất phát sinh đang chờ khách duyệt. "
                        + "Chỉ tạm dừng hạng mục liên quan và tiếp tục các phần đã được duyệt.",
                dailyCompletions,
                completionDetails);
    }

    // Tìm dữ liệu chi tiết công việc hoàn tất theo ngày được chọn
    public CompletionDetail findCompletionDetail(TechnicalDashboardData data, LocalDate date) {
        for (CompletionDetail detail : data.getCompletionDetails()) {
            if (detail.getDate().equals(date)) {
                return detail;
            }
        }
        return null;
    }

    // Tính tổng số công việc đã hoàn tất trong phần chi tiết
    public int calculateTotalCompleted(CompletionDetail detail) {
        int total = 0;

        for (WorkGroupStat stat : detail.getWorkGroupStats()) {
            total += stat.getCompletedCount();
        }

        return total;
    }

    // Tính tỷ trọng của một nhóm công việc
    public double calculatePercentage(int count, int total) {
        if (total == 0) {
            return 0;
        }
        return count * 100.0 / total;
    }

    private CompletionDetail createCompletionDetail(LocalDate date, int maintenance, int mechanical,
            int electrical) {
        List<WorkGroupStat> stats = List.of(
                new WorkGroupStat("Bảo dưỡng định kỳ", maintenance),
                new WorkGroupStat("Sửa chữa cơ khí", mechanical),
                new WorkGroupStat("Điện và chẩn đoán", electrical));

        return new CompletionDetail(date, stats);
    }
}