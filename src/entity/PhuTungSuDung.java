package entity;

import java.math.BigDecimal;
import java.util.Objects;

// Lưu phụ tùng thực dùng và hạng mục tương ứng bằng bản ghi bất biến.
public final class PhuTungSuDung {
    private final String maGhiNhan;
    private final String maLenhSuaChua;
    private final PhuTung phuTung;
    private final BigDecimal soLuong;
    private final String nguoiThucHien;
    private final String ghiChu;
    private final TrangThaiSuDung trangThai;
    private final String hangMuc;

    public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, BigDecimal soLuong,
            String nguoiThucHien, String ghiChu, TrangThaiSuDung trangThai) {
        this(maGhiNhan, maLenhSuaChua, phuTung, soLuong, nguoiThucHien, ghiChu, trangThai, "");
    }

    public PhuTungSuDung(String maGhiNhan, String maLenhSuaChua, PhuTung phuTung, BigDecimal soLuong,
            String nguoiThucHien, String ghiChu, TrangThaiSuDung trangThai, String hangMuc) {
        this.maGhiNhan = Objects.requireNonNull(maGhiNhan).trim();
        this.maLenhSuaChua = Objects.requireNonNull(maLenhSuaChua).trim();
        this.phuTung = Objects.requireNonNull(phuTung);
        phuTung.kiemTraSoLuong(soLuong);
        this.soLuong = soLuong;
        this.nguoiThucHien = Objects.requireNonNull(nguoiThucHien);
        this.ghiChu = Objects.requireNonNull(ghiChu);
        this.trangThai = Objects.requireNonNull(trangThai);
        this.hangMuc = Objects.requireNonNull(hangMuc);
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

    public String getHangMuc() {
        return hangMuc;
    }
}