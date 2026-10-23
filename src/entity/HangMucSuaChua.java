package entity;

import java.util.Objects;

// Một công việc được giao; dữ liệu chỉ đọc và được thay mới khi cập nhật tiến độ.
public final class HangMucSuaChua {
    private final String tenHangMuc;
    private final String taiKhoanThucHien;
    private final String ketQua;
    private final TrangThaiSuaChua trangThai;

    private final boolean phatSinh;

    public HangMucSuaChua(String tenHangMuc, String taiKhoanThucHien, String ketQua,
            TrangThaiSuaChua trangThai, boolean phatSinh) {
        this.tenHangMuc = Objects.requireNonNull(tenHangMuc);
        this.taiKhoanThucHien = Objects.requireNonNull(taiKhoanThucHien);
        this.ketQua = Objects.requireNonNull(ketQua);
        this.trangThai = Objects.requireNonNull(trangThai);
        this.phatSinh = phatSinh;
        if (tenHangMuc.isBlank() || (trangThai == TrangThaiSuaChua.HOAN_THANH && ketQua.isBlank())) {
            throw new IllegalArgumentException("Tên công việc hoặc kết quả hoàn thành chưa hợp lệ.");
        }
    }

    public String getTenHangMuc() { return tenHangMuc; }
    public String getTaiKhoanThucHien() { return taiKhoanThucHien; }
    public String getKetQua() { return ketQua; }
    public TrangThaiSuaChua getTrangThai() { return trangThai; }
    public boolean isPhatSinh() { return phatSinh; }
}