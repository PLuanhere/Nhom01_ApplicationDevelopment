package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Snapshot bất biến của một dòng báo giá tại thời điểm phát hành hóa đơn. */
public final class ChiTietHoaDon {
    private final String maHoaDon;
    private final int viTri;
    private final String tenHangMuc;
    private final String loai;
    private final String donVi;
    private final double soLuong;
    private final long donGia;
    private final long thanhTien;

    /** Tạo dòng chi tiết và tự tính thành tiền theo số lượng, đơn giá. */
    public ChiTietHoaDon(String maHoaDon, int viTri, String tenHangMuc, String loai, String donVi,
            double soLuong, long donGia) {
        this.maHoaDon = batBuoc(maHoaDon, "Mã hóa đơn");
        if (viTri < 0) {
            throw new IllegalArgumentException("Vị trí chi tiết không được âm.");
        }
        this.viTri = viTri;
        this.tenHangMuc = batBuoc(tenHangMuc, "Tên hạng mục");
        this.loai = batBuoc(loai, "Loại hạng mục");
        this.donVi = batBuoc(donVi, "Đơn vị");
        if (!(soLuong > 0) || soLuong != Math.round(soLuong * 1000) / 1000.0 || donGia < 0) {
            throw new IllegalArgumentException("Số lượng hoặc đơn giá hóa đơn không hợp lệ.");
        }
        if (("cái".equalsIgnoreCase(donVi) || "bộ".equalsIgnoreCase(donVi)) && soLuong % 1 != 0) {
            throw new IllegalArgumentException("Số lượng của đơn vị cái/bộ phải là số nguyên.");
        }
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = tinhThanhTien(soLuong, donGia);
    }

    /** Tính thành tiền nguyên đồng và từ chối kết quả vượt giới hạn long. */
    public static long tinhThanhTien(double soLuong, long donGia) {
        if (!(soLuong > 0) || donGia < 0) {
            throw new IllegalArgumentException("Số lượng hoặc đơn giá hóa đơn không hợp lệ.");
        }
        BigDecimal tien = BigDecimal.valueOf(soLuong).multiply(BigDecimal.valueOf(donGia))
                .setScale(0, RoundingMode.HALF_UP);
        try {
            return tien.longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Thành tiền vượt giới hạn.", ex);
        }
    }

    /** Tính tổng các dòng chi tiết bằng tiền nguyên đồng. */
    public static long tinhTong(List<ChiTietHoaDon> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn cần ít nhất một dòng chi tiết.");
        }
        long tong = 0;
        for (ChiTietHoaDon dong : danhSach) {
            if (dong == null) {
                throw new IllegalArgumentException("Chi tiết hóa đơn không được null.");
            }
            tong = HoaDon.congTien(tong, dong.getThanhTien());
        }
        return tong;
    }

    /** Trả bản sao danh sách để tầng ngoài không giữ tham chiếu collection gốc. */
    public static List<ChiTietHoaDon> saoChep(List<ChiTietHoaDon> danhSach) {
        return danhSach == null ? new ArrayList<>() : new ArrayList<>(danhSach);
    }

    private static String batBuoc(String giaTri, String ten) {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new IllegalArgumentException(ten + " không được để trống.");
        }
        return giaTri.trim();
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public int getViTri() {
        return viTri;
    }

    public String getTenHangMuc() {
        return tenHangMuc;
    }

    public String getLoai() {
        return loai;
    }

    public String getDonVi() {
        return donVi;
    }

    public double getSoLuong() {
        return soLuong;
    }

    public long getDonGia() {
        return donGia;
    }

    public long getThanhTien() {
        return thanhTien;
    }
}