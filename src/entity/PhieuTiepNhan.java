package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Phiếu chỉ đọc sau xác nhận; giữ snapshot hồ sơ tại thời điểm tiếp nhận. */
public final class PhieuTiepNhan {
    private final String maPhieu, bienSo, yeuCau, tinhTrang, taiKhoanKyThuat;
    private final boolean daXacNhan;
    private final String maXe, maKH, tenKhach, soDienThoai, tenXe, taiSan, nguoiTiepNhan;
    private final long soKm;
    private final LocalDateTime thoiGianNhan, thoiGianXacNhan;

    /** Giữ nguyên hợp đồng constructor mà DAO kiểm tra của nhóm đang dùng. */
    public PhieuTiepNhan(String maPhieu, String bienSo, String yeuCau, String tinhTrang,
            String taiKhoanKyThuat, boolean daXacNhan) {
        if (maPhieu == null || bienSo == null || taiKhoanKyThuat == null || yeuCau == null
                || tinhTrang == null || maPhieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Thông tin phiếu tiếp nhận không hợp lệ.");
        }
        this.maPhieu = maPhieu; this.bienSo = bienSo; this.yeuCau = yeuCau;
        this.tinhTrang = tinhTrang; this.taiKhoanKyThuat = taiKhoanKyThuat; this.daXacNhan = daXacNhan;
        maXe = ""; maKH = ""; tenKhach = ""; soDienThoai = ""; tenXe = "";
        taiSan = ""; nguoiTiepNhan = ""; soKm = 0; thoiGianNhan = null; thoiGianXacNhan = null;
    }

    /** Phiếu mới đã được khách xác nhận; tồn tại/quan hệ/quyền được kiểm tra tại DAO. */
    public PhieuTiepNhan(String maPhieu, Xe xe, KhachHang khach, long soKm,
            LocalDateTime thoiGianNhan, String tinhTrang, String taiSan, String yeuCau,
            String nguoiTiepNhan, LocalDateTime thoiGianXacNhan) {
        this.maPhieu = batBuoc(maPhieu, "Mã phiếu");
        Objects.requireNonNull(xe, "Cần phương tiện.");
        Objects.requireNonNull(khach, "Cần khách hàng.");
        if (!xe.getMaKH().equals(khach.getMaKH())) throw new IllegalArgumentException("Xe không thuộc khách đã chọn.");
        if (soKm < 0) throw new IllegalArgumentException("Kilomet không được âm.");
        this.soKm = soKm;
        this.thoiGianNhan = Objects.requireNonNull(thoiGianNhan, "Cần thời gian nhận.");
        this.thoiGianXacNhan = Objects.requireNonNull(thoiGianXacNhan, "Cần thời điểm xác nhận.");
        this.tinhTrang = batBuoc(tinhTrang, "Tình trạng ban đầu");
        this.yeuCau = batBuoc(yeuCau, "Yêu cầu khách hàng");
        this.taiSan = taiSan == null ? "" : taiSan.strip();
        this.nguoiTiepNhan = batBuoc(nguoiTiepNhan, "Người tiếp nhận");
        maXe = xe.getMaXe(); maKH = khach.getMaKH(); bienSo = xe.getBienSo();
        tenKhach = khach.getHoTen(); soDienThoai = khach.getSoDienThoai();
        tenXe = xe.getHangXe() + " " + xe.getModel();
        taiKhoanKyThuat = ""; daXacNhan = true;
    }

    public String getMaPhieu() { return maPhieu; }
    public String getBienSo() { return bienSo; }
    public String getYeuCau() { return yeuCau; }
    public String getTinhTrang() { return tinhTrang; }
    public String getTaiKhoanKyThuat() { return taiKhoanKyThuat; }
    public boolean isDaXacNhan() { return daXacNhan; }
    public String getMaXe() { return maXe; }
    public String getMaKH() { return maKH; }
    public String getTenKhach() { return tenKhach; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getTenXe() { return tenXe; }
    public String getTaiSan() { return taiSan; }
    public String getNguoiTiepNhan() { return nguoiTiepNhan; }
    public long getSoKm() { return soKm; }
    public LocalDateTime getThoiGianNhan() { return thoiGianNhan; }
    public LocalDateTime getThoiGianXacNhan() { return thoiGianXacNhan; }
    public TrangThaiTiepNhan getTrangThai() { return daXacNhan ? TrangThaiTiepNhan.CHO_KIEM_TRA : TrangThaiTiepNhan.NHAP; }
    @Override public String toString() { return maPhieu + (daXacNhan ? " · Đã xác nhận" : " · Nháp"); }

    private static String batBuoc(String chu, String ten) {
        if (chu == null || chu.isBlank()) throw new IllegalArgumentException(ten + " không được rỗng.");
        return chu.strip();
    }
}
