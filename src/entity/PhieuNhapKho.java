package entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/** Lưu thông tin lần nhập kho cùng danh sách mặt hàng đã ghi nhận. */
public final class PhieuNhapKho {
    private final String maPhieu;
    private final LocalDateTime ngayNhap;
    private final String nguonNhap;
    private final String chungTuNguon;
    private final String nguoiNhap;
    private final List<ChiTietNhapKho> chiTiet;

    /** Tạo phiếu nhập, sao chép các dòng và kiểm tra tổng tiền. */
    public PhieuNhapKho(String maPhieu, LocalDateTime ngayNhap, String nguonNhap,
        String chungTuNguon, String nguoiNhap, List<ChiTietNhapKho> chiTiet) {
        if (ngayNhap == null) { throw new IllegalArgumentException("Thiếu ngayNhap."); }
        if (chungTuNguon == null) { throw new IllegalArgumentException("Thiếu chungTuNguon."); }
        this.maPhieu = batBuoc(maPhieu, "Mã phiếu");
        this.ngayNhap = ngayNhap;
        this.nguonNhap = batBuoc(nguonNhap, "Nhà cung cấp / nguồn nhập");
        this.chungTuNguon = chungTuNguon.trim();
        this.nguoiNhap = batBuoc(nguoiNhap, "Người nhập");
        if (chiTiet == null) { throw new IllegalArgumentException("Cần ít nhất một mặt hàng."); }
        this.chiTiet = new ArrayList<>();
        for (ChiTietNhapKho dong : chiTiet) {
            if (dong == null) { throw new IllegalArgumentException("Dòng nhập không được null."); }
            this.chiTiet.add(dong);
        }
        if (this.chiTiet.isEmpty()) { throw new IllegalArgumentException("Phiếu nhập cần ít nhất một mặt hàng."); }
        getTongTien();
    }

    /** Kiểm tra trường bắt buộc không rỗng và trả về nội dung đã bỏ khoảng trắng. */
    private static String batBuoc(String chu, String ten) {
        if (chu == null || chu.isBlank()) { throw new IllegalArgumentException(ten + " không được rỗng."); }
        return chu.trim();
    }

    /** Trả về mã phiếu nhập. */
    public String getMaPhieu() { return maPhieu; }

    /** Trả về thời điểm nhập kho. */
    public LocalDateTime getNgayNhap() { return ngayNhap; }

    /** Trả về nguồn hàng hoặc diễn giải nhập kho. */
    public String getNguonNhap() { return nguonNhap; }

    /** Trả về mã hóa đơn hoặc biên nhận nguồn. */
    public String getChungTuNguon() { return chungTuNguon; }

    /** Trả về tên tài khoản nhập kho. */
    public String getNguoiNhap() { return nguoiNhap; }

    /** Trả về bản sao danh sách mặt hàng trên phiếu nhập. */
    public List<ChiTietNhapKho> getChiTiet() { return new ArrayList<>(chiTiet); }

    /** Cộng thành tiền các dòng nhập đã làm tròn và kiểm tra tràn tổng. */
    public long getTongTien() {
        long tong = 0;
        for (ChiTietNhapKho dong : chiTiet) {
            if (Long.MAX_VALUE - tong < dong.getThanhTien()) {
                throw new IllegalArgumentException("Tổng tiền vượt giới hạn cho phép.");
            }
            tong = tong + dong.getThanhTien();
        }
        return tong;
    }
}
