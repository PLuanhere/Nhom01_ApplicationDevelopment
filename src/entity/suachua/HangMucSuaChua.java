package entity.suachua;

// Một công việc được giao; dữ liệu chỉ đọc và được thay mới khi cập nhật tiến độ.
public final class HangMucSuaChua {
    private final String tenHangMuc;
    private final String taiKhoanThucHien;
    private final String ketQua;
    private final TrangThaiSuaChua trangThai;

    private final boolean phatSinh;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public HangMucSuaChua(String tenHangMuc, String taiKhoanThucHien, String ketQua, TrangThaiSuaChua trangThai,
            boolean phatSinh) {
        if (tenHangMuc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.tenHangMuc = tenHangMuc;
        if (taiKhoanThucHien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.taiKhoanThucHien = taiKhoanThucHien;
        if (ketQua == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.ketQua = ketQua;
        if (trangThai == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.trangThai = trangThai;
        this.phatSinh = phatSinh;
        if (tenHangMuc.isBlank() || (trangThai == TrangThaiSuaChua.HOAN_THANH && ketQua.isBlank())) {
            throw new IllegalArgumentException("Tên công việc hoặc kết quả hoàn thành chưa hợp lệ.");
        }
    }

    /** Trả về ten hang muc. */
    public String getTenHangMuc() {
        return tenHangMuc;
    }

    /** Trả về tai khoan thuc hien. */
    public String getTaiKhoanThucHien() {
        return taiKhoanThucHien;
    }

    /** Trả về ket qua. */
    public String getKetQua() {
        return ketQua;
    }

    /** Trả về trang thai. */
    public TrangThaiSuaChua getTrangThai() {
        return trangThai;
    }

    /** Cho biết phat sinh. */
    public boolean isPhatSinh() {
        return phatSinh;
    }
}