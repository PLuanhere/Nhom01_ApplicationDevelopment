package ui.screen;

/** Các trang giữ chỗ thuộc phạm vi UI của Luân; chưa đại diện cho quyền người dùng. */
public enum ShellPage {
    HOME("Trang chủ", "Trang chủ • Quản lý"),
    EMPLOYEES("Nhân viên", "Nhân viên"),
    ACCOUNTS("Tài khoản", "Tài khoản và phân quyền"),
    SERVICES("Dịch vụ / bảng giá", "Dịch vụ và bảng giá"),
    PARTS("Phụ tùng", "Danh mục phụ tùng"),
    AUDIT("Lịch sử thao tác", "Lịch sử thao tác");

    private final String navigationLabel;
    private final String title;

    ShellPage(String navigationLabel, String title) {
        this.navigationLabel = navigationLabel;
        this.title = title;
    }

    public String getNavigationLabel() {
        return navigationLabel;
    }

    public String getTitle() {
        return title;
    }
}
