package entity;

import java.time.LocalDateTime;

// Lưu kết quả của một hạng mục kiểm tra theo cấu trúc SQL của nhóm.
public final class ChiTietKiemTra {
    private final String tenKhuVuc;
    private final String tenHangMuc;
    private final String ketQua;
    private final String mucDo;
    private final String deXuat;
    private final String nguoiGhi;
    private final LocalDateTime thoiDiemGhi;

    public ChiTietKiemTra(String tenKhuVuc, String tenHangMuc, String ketQua, String mucDo, String deXuat) {
        this(tenKhuVuc, tenHangMuc, ketQua, mucDo, deXuat, null, null);
    }

    public ChiTietKiemTra(String tenKhuVuc, String tenHangMuc, String ketQua, String mucDo, String deXuat,
            String nguoiGhi, LocalDateTime thoiDiemGhi) {
        if (tenKhuVuc == null || tenHangMuc == null || ketQua == null || mucDo == null || deXuat == null) {
            throw new IllegalArgumentException("Thông tin hạng mục kiểm tra không được null.");
        }
        this.tenKhuVuc = tenKhuVuc.trim();
        this.tenHangMuc = tenHangMuc.trim();
        this.ketQua = ketQua.trim();
        this.mucDo = mucDo.trim();
        this.deXuat = deXuat.trim();
        this.nguoiGhi = nguoiGhi;
        this.thoiDiemGhi = thoiDiemGhi;
        if (this.tenKhuVuc.length() > 150 || this.tenHangMuc.length() > 200 || this.ketQua.length() > 2000
                || this.mucDo.length() > 50 || this.deXuat.length() > 2000) {
            throw new IllegalArgumentException("Khu vực tối đa 150, hạng mục 200, mức độ 50, kết quả/đề xuất 2000 ký tự.");
        }
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public String getTenHangMuc() {
        return tenHangMuc;
    }

    public String getKetQua() {
        return ketQua;
    }

    public String getMucDo() {
        return mucDo;
    }

    public String getDeXuat() {
        return deXuat;
    }

    public String getNguoiGhi() {
        return nguoiGhi;
    }

    public LocalDateTime getThoiDiemGhi() {
        return thoiDiemGhi;
    }

    // So sánh nội dung để giữ người và thời điểm ghi khi một dòng không thay đổi.
    public boolean cungNoiDung(ChiTietKiemTra khac) {
        return khac != null && tenKhuVuc.equals(khac.tenKhuVuc) && tenHangMuc.equals(khac.tenHangMuc)
                && ketQua.equals(khac.ketQua) && mucDo.equals(khac.mucDo) && deXuat.equals(khac.deXuat);
    }
}
