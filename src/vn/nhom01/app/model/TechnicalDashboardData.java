package vn.nhom01.app.model;

import java.time.LocalDate;
import java.util.List;

public class TechnicalDashboardData {
    private String technicianName;
    private LocalDate dashboardDate;
    private int repairingCount;
    private int waitingPartCount;
    private int waitingApprovalCount;
    private int completedCount;
    private String notice;
    private List<DailyCompletion> dailyCompletions;
    private List<CompletionDetail> completionDetails;

    public TechnicalDashboardData(String technicianName, LocalDate dashboardDate, int repairingCount,
            int waitingPartCount, int waitingApprovalCount, int completedCount, String notice,
            List<DailyCompletion> dailyCompletions, List<CompletionDetail> completionDetails) {
        this.technicianName = technicianName;
        this.dashboardDate = dashboardDate;
        this.repairingCount = repairingCount;
        this.waitingPartCount = waitingPartCount;
        this.waitingApprovalCount = waitingApprovalCount;
        this.completedCount = completedCount;
        this.notice = notice;
        this.dailyCompletions = dailyCompletions;
        this.completionDetails = completionDetails;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public LocalDate getDashboardDate() {
        return dashboardDate;
    }

    public int getRepairingCount() {
        return repairingCount;
    }

    public int getWaitingPartCount() {
        return waitingPartCount;
    }

    public int getWaitingApprovalCount() {
        return waitingApprovalCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public String getNotice() {
        return notice;
    }

    public List<DailyCompletion> getDailyCompletions() {
        return dailyCompletions;
    }

    public List<CompletionDetail> getCompletionDetails() {
        return completionDetails;
    }

    public static class DailyCompletion {
        private LocalDate date;
        private int count;

        public DailyCompletion(LocalDate date, int count) {
            this.date = date;
            this.count = count;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getCount() {
            return count;
        }
    }

    public static class WorkGroupStat {
        private String workGroup;
        private int completedCount;

        public WorkGroupStat(String workGroup, int completedCount) {
            this.workGroup = workGroup;
            this.completedCount = completedCount;
        }

        public String getWorkGroup() {
            return workGroup;
        }

        public int getCompletedCount() {
            return completedCount;
        }
    }

    public static class CompletionDetail {
        private LocalDate date;
        private List<WorkGroupStat> workGroupStats;

        public CompletionDetail(LocalDate date, List<WorkGroupStat> workGroupStats) {
            this.date = date;
            this.workGroupStats = workGroupStats;
        }

        public LocalDate getDate() {
            return date;
        }

        public List<WorkGroupStat> getWorkGroupStats() {
            return workGroupStats;
        }
    }
}