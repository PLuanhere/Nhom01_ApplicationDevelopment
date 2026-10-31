package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Lệnh sửa chữa và các công việc; DAO trả bản sao để UI không sửa dữ liệu đang lưu.
public final class PhieuSuaChua {
    private final String maPhieu;
    private final String maBaoGia;
    private final String bienSo;

    // Phân công, hạn hoàn thành theo giờ garage và nội dung thực hiện
    private String kyThuatChinh;
    private String kyThuatPhoiHop;
    private LocalDateTime hanHoanThanh;
    private String ghiChu;

    // Tiến độ lệnh, các công việc và đề xuất phát sinh hiện tại.
    private TrangThaiSuaChua trangThai;
    private final List<HangMucSuaChua> danhSachHangMuc;
    private PhatSinhSuaChua phatSinh;

    private String[] ketQuaHoanTat = {"", "", ""};
    private LocalDateTime thoiDiemHoanTat;
    private String nguoiPhatHanh = "";
    private LocalDateTime thoiDiemPhatHanh;

    public PhieuSuaChua(String maPhieu, String maBaoGia, String bienSo, String kyThuatChinh,
            String kyThuatPhoiHop, LocalDateTime hanHoanThanh, String ghiChu, TrangThaiSuaChua trangThai,
            List<HangMucSuaChua> danhSachHangMuc, PhatSinhSuaChua phatSinh) {
        this.maPhieu = Objects.requireNonNull(maPhieu);
        this.maBaoGia = Objects.requireNonNull(maBaoGia);
        this.bienSo = Objects.requireNonNull(bienSo);
        this.kyThuatChinh = Objects.requireNonNull(kyThuatChinh);
        this.kyThuatPhoiHop = Objects.requireNonNull(kyThuatPhoiHop);
        this.hanHoanThanh = Objects.requireNonNull(hanHoanThanh);
        this.ghiChu = Objects.requireNonNull(ghiChu);
        this.trangThai = Objects.requireNonNull(trangThai);
        this.danhSachHangMuc = new ArrayList<HangMucSuaChua>(danhSachHangMuc);
        this.phatSinh = phatSinh;
        if (maPhieu.isBlank() || maBaoGia.isBlank() || bienSo.isBlank() || danhSachHangMuc.isEmpty()) {
            throw new IllegalArgumentException("Thông tin lệnh sửa chữa chưa hợp lệ.");
        }
    }

    /** Giữ constructor cũ cho kỹ thuật; lệnh tiếp nhận mới có thêm dấu vết phát hành. */
    public PhieuSuaChua(String maPhieu, String maBaoGia, String bienSo, String kyThuatChinh,
            String kyThuatPhoiHop, LocalDateTime hanHoanThanh, String ghiChu,
            List<HangMucSuaChua> hangMuc, String nguoiPhatHanh, LocalDateTime thoiDiemPhatHanh) {
        this(maPhieu, maBaoGia, bienSo, kyThuatChinh, kyThuatPhoiHop, hanHoanThanh,
                ghiChu, TrangThaiSuaChua.CHO_SUA_CHUA, hangMuc, null);
        if (nguoiPhatHanh == null || nguoiPhatHanh.isBlank() || thoiDiemPhatHanh == null) {
            throw new IllegalArgumentException("Cần người và thời điểm phát hành lệnh.");
        }
        this.nguoiPhatHanh = nguoiPhatHanh.strip();
        this.thoiDiemPhatHanh = thoiDiemPhatHanh;
    }

    public PhieuSuaChua(PhieuSuaChua phieu) {
        this(phieu.maPhieu, phieu.maBaoGia, phieu.bienSo, phieu.kyThuatChinh, phieu.kyThuatPhoiHop,
                phieu.hanHoanThanh, phieu.ghiChu, phieu.trangThai, phieu.danhSachHangMuc, phieu.phatSinh);
        ketQuaHoanTat = phieu.ketQuaHoanTat.clone();
        thoiDiemHoanTat = phieu.thoiDiemHoanTat;
        nguoiPhatHanh = phieu.nguoiPhatHanh;
        thoiDiemPhatHanh = phieu.thoiDiemPhatHanh;
    }

    public String getNguoiPhatHanh() { return nguoiPhatHanh; }
    public LocalDateTime getThoiDiemPhatHanh() { return thoiDiemPhatHanh; }

    public String getMaPhieu() { return maPhieu; }
    public String getMaBaoGia() { return maBaoGia; }
    public String getBienSo() { return bienSo; }
    public String getKyThuatChinh() { return kyThuatChinh; }
    public String getKyThuatPhoiHop() { return kyThuatPhoiHop; }
    public LocalDateTime getHanHoanThanh() { return hanHoanThanh; }
    public String getGhiChu() { return ghiChu; }
    public TrangThaiSuaChua getTrangThai() { return trangThai; }
    public List<HangMucSuaChua> getDanhSachHangMuc() { return new ArrayList<HangMucSuaChua>(danhSachHangMuc); }
    public PhatSinhSuaChua getPhatSinh() { return phatSinh; }
    public String[] getKetQuaHoanTat() { return ketQuaHoanTat.clone(); }
    public LocalDateTime getThoiDiemHoanTat() { return thoiDiemHoanTat; }

    /** @param banSua thông tin phân công mới của cùng lệnh */
    // Cập nhật thông tin phân công khi lệnh chưa bắt đầu thực hiện.
    public void capNhatPhanCong(PhieuSuaChua banSua) {
        if (trangThai != TrangThaiSuaChua.CHO_SUA_CHUA) {
            throw new IllegalStateException("Lệnh đã bắt đầu, không sửa phân công trực tiếp.");
        }
        kyThuatChinh = banSua.kyThuatChinh;
        kyThuatPhoiHop = banSua.kyThuatPhoiHop;
        hanHoanThanh = banSua.hanHoanThanh;
        ghiChu = banSua.ghiChu;
        danhSachHangMuc.clear();
        danhSachHangMuc.addAll(banSua.danhSachHangMuc);
    }

    /**
     * @param viTri vị trí công việc
     * @param hangMuc kết quả mới của công việc
     */
    // Thay nội dung một công việc và đánh dấu lệnh đã bắt đầu thực hiện.
    public void capNhatHangMuc(int viTri, HangMucSuaChua hangMuc) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành, không sửa tiến độ trực tiếp.");
        }
        danhSachHangMuc.set(viTri, hangMuc);
        trangThai = TrangThaiSuaChua.DANG_SUA;
    }

    /** @param deXuat đề xuất mới chưa được khách duyệt */
    // Ghi nhận một đề xuất phát sinh đang chờ phản hồi từ khách.
    public void ghiNhanPhatSinh(PhatSinhSuaChua deXuat) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH || phatSinh != null) {
            throw new IllegalStateException("Không thể gửi thêm đề xuất trong trạng thái hiện tại.");
        }
        phatSinh = Objects.requireNonNull(deXuat);
    }

    /**
     * @param ketQua kết luận, bảo hành và khuyến nghị
     * @param thoiDiem thời điểm hoàn tất
     */
    // Lưu kết luận và chuyển lệnh sang hoàn thành sau khi DAO kiểm tra điều kiện.
    public void hoanTat(String[] ketQua, LocalDateTime thoiDiem) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH || ketQua.length != 3 || ketQua[0].isBlank()) {
            throw new IllegalStateException("Kết luận hoặc trạng thái hoàn tất chưa hợp lệ.");
        }
        ketQuaHoanTat = ketQua.clone();
        thoiDiemHoanTat = Objects.requireNonNull(thoiDiem);
        trangThai = TrangThaiSuaChua.HOAN_THANH;
    }
}