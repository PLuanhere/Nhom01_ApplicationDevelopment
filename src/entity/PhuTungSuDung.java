package entity;

import java.math.BigDecimal;
import java.util.Objects;

// Bản ghi phụ tùng bất biến; DAO tạo bản mới khi sửa hoặc xác nhận
public final class PhuTungSuDung {
    private final String maGhiNhan;
    private final String maLenhSuaChua;
    private final PhuTung phuTung;
    private final BigDecimal soLuong;
    private final String nguoiThucHien;
    private final String ghiChu;
    private final TrangThaiSuDung trangThai;

    /**
     * Tạo một bản ghi hợp lệ về giá trị nội tại.
     * @param maGhiNhan mã ghi nhận không rỗng
     * @param maLenhSuaChua mã lệnh không rỗng
     * @param phuTung phụ tùng được dùng
     * @param soLuong lượng dương đúng đơn vị
     * @param nguoiThucHien tên người thực hiện
     * @param ghiChu ghi chú, cho phép trống
     * @param trangThai trạng thái ghi nhận
     * @throws IllegalArgumentException nếu mã hoặc số lượng sai
     */
    public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, BigDecimal soLuong,
            String nguoiThucHien, String ghiChu, TrangThaiSuDung trangThai) {
        this.maGhiNhan = Objects.requireNonNull(maGhiNhan).trim();
        this.maLenhSuaChua = Objects.requireNonNull(maLenhSuaChua).trim();
        this.phuTung = Objects.requireNonNull(phuTung);
        phuTung.kiemTraSoLuong(soLuong);
        this.soLuong = soLuong;
        this.nguoiThucHien = Objects.requireNonNull(nguoiThucHien);
        this.ghiChu = Objects.requireNonNull(ghiChu);
        this.trangThai = Objects.requireNonNull(trangThai);
        if (this.maGhiNhan.isEmpty() || this.maLenhSuaChua.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã ghi nhận hoặc mã lệnh.");
        }
    }

    public String getMaGhiNhan() {
        return maGhiNhan;
    }

    public String getMaLenhSuaChua() {
        return maLenhSuaChua;
    }

    public PhuTung getPhuTung() {
        return phuTung;
    }

    public BigDecimal getSoLuong() {
        return soLuong;
    }

    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public TrangThaiSuDung getTrangThai() {
        return trangThai;
    }
}