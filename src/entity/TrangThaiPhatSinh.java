package entity;

// Quyết định của khách đối với đề xuất phát sinh, tách khỏi tiến độ sửa chữa.
public enum TrangThaiPhatSinh {
    CHO_DUYET("Chờ khách duyệt"),
    DA_DUYET("Đã được khách đồng ý"),
    TU_CHOI("Khách từ chối");

    private final String tenHienThi;

    TrangThaiPhatSinh(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}