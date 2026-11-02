package entity.suachua;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private String[] ketQuaHoanTat = { "", "", "" };
    private LocalDateTime thoiDiemHoanTat;
    private String nguoiPhatHanh = "";
    private LocalDateTime thoiDiemPhatHanh;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuSuaChua(String maPhieu, String maBaoGia, String bienSo, String kyThuatChinh, String kyThuatPhoiHop,
            LocalDateTime hanHoanThanh, String ghiChu, TrangThaiSuaChua trangThai, List<HangMucSuaChua> danhSachHangMuc,
            PhatSinhSuaChua phatSinh) {
        if (maPhieu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.maPhieu = maPhieu;
        if (maBaoGia == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.maBaoGia = maBaoGia;
        if (bienSo == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.bienSo = bienSo;
        if (kyThuatChinh == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.kyThuatChinh = kyThuatChinh;
        if (kyThuatPhoiHop == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.kyThuatPhoiHop = kyThuatPhoiHop;
        if (hanHoanThanh == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.hanHoanThanh = hanHoanThanh;
        if (ghiChu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.ghiChu = ghiChu;
        if (trangThai == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.trangThai = trangThai;
        this.danhSachHangMuc = new ArrayList<HangMucSuaChua>(danhSachHangMuc);
        this.phatSinh = phatSinh;
        if (maPhieu.isBlank() || maBaoGia.isBlank() || bienSo.isBlank() || danhSachHangMuc.isEmpty()) {
            throw new IllegalArgumentException("Thông tin lệnh sửa chữa chưa hợp lệ.");
        }
    }

    /** Giữ constructor cũ cho kỹ thuật; lệnh tiếp nhận mới có thêm dấu vết phát hành. */
    public PhieuSuaChua(String maPhieu, String maBaoGia, String bienSo, String kyThuatChinh, String kyThuatPhoiHop,
            LocalDateTime hanHoanThanh, String ghiChu, List<HangMucSuaChua> hangMuc, String nguoiPhatHanh,
            LocalDateTime thoiDiemPhatHanh) {
        this(maPhieu, maBaoGia, bienSo, kyThuatChinh, kyThuatPhoiHop, hanHoanThanh, ghiChu,
                TrangThaiSuaChua.CHO_SUA_CHUA, hangMuc, null);
        if (nguoiPhatHanh == null || nguoiPhatHanh.isBlank() || thoiDiemPhatHanh == null) {
            throw new IllegalArgumentException("Cần người và thời điểm phát hành lệnh.");
        }
        this.nguoiPhatHanh = nguoiPhatHanh.trim();
        this.thoiDiemPhatHanh = thoiDiemPhatHanh;
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuSuaChua(PhieuSuaChua phieu) {
        this(phieu.maPhieu, phieu.maBaoGia, phieu.bienSo, phieu.kyThuatChinh, phieu.kyThuatPhoiHop, phieu.hanHoanThanh,
                phieu.ghiChu, phieu.trangThai, phieu.danhSachHangMuc, phieu.phatSinh);
        ketQuaHoanTat = phieu.ketQuaHoanTat.clone();
        thoiDiemHoanTat = phieu.thoiDiemHoanTat;
        nguoiPhatHanh = phieu.nguoiPhatHanh;
        thoiDiemPhatHanh = phieu.thoiDiemPhatHanh;
    }

    /** Trả về nguoi phat hanh. */
    public String getNguoiPhatHanh() {
        return nguoiPhatHanh;
    }

    /** Trả về thoi diem phat hanh. */
    public LocalDateTime getThoiDiemPhatHanh() {
        return thoiDiemPhatHanh;
    }

    /** Trả về ma phieu. */
    public String getMaPhieu() {
        return maPhieu;
    }

    /** Trả về ma bao gia. */
    public String getMaBaoGia() {
        return maBaoGia;
    }

    /** Trả về bien so. */
    public String getBienSo() {
        return bienSo;
    }

    /** Trả về ky thuat chinh. */
    public String getKyThuatChinh() {
        return kyThuatChinh;
    }

    /** Trả về ky thuat phoi hop. */
    public String getKyThuatPhoiHop() {
        return kyThuatPhoiHop;
    }

    /** Trả về han hoan thanh. */
    public LocalDateTime getHanHoanThanh() {
        return hanHoanThanh;
    }

    /** Trả về ghi chu. */
    public String getGhiChu() {
        return ghiChu;
    }

    /** Trả về trang thai. */
    public TrangThaiSuaChua getTrangThai() {
        return trangThai;
    }

    /** Trả về danh sach hang muc. */
    public List<HangMucSuaChua> getDanhSachHangMuc() {
        return new ArrayList<HangMucSuaChua>(danhSachHangMuc);
    }

    /** Trả về phat sinh. */
    public PhatSinhSuaChua getPhatSinh() {
        return phatSinh;
    }

    /** Trả về ket qua hoan tat. */
    public String[] getKetQuaHoanTat() {
        return ketQuaHoanTat.clone();
    }

    /** Trả về thoi diem hoan tat. */
    public LocalDateTime getThoiDiemHoanTat() {
        return thoiDiemHoanTat;
    }

    // Cập nhật thông tin phân công khi lệnh chưa bắt đầu thực hiện.
    /** Thực hiện cap nhat phan cong cho thành phần hiện tại. */
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

    // Thay nội dung một công việc và đánh dấu lệnh đã bắt đầu thực hiện.
    /** Thực hiện cap nhat hang muc cho thành phần hiện tại. */
    public void capNhatHangMuc(int viTri, HangMucSuaChua hangMuc) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành, không sửa tiến độ trực tiếp.");
        }
        danhSachHangMuc.set(viTri, hangMuc);
        trangThai = TrangThaiSuaChua.DANG_SUA;
    }

    // Ghi nhận một đề xuất phát sinh đang chờ phản hồi từ khách.
    /** Thực hiện ghi nhan phat sinh cho thành phần hiện tại. */
    public void ghiNhanPhatSinh(PhatSinhSuaChua deXuat) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH || phatSinh != null) {
            throw new IllegalStateException("Không thể gửi thêm đề xuất trong trạng thái hiện tại.");
        }
        if (deXuat == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        phatSinh = deXuat;
    }

    // Lưu kết luận và chuyển lệnh sang hoàn thành sau khi DAO kiểm tra điều kiện.
    /** Thực hiện hoan tat cho thành phần hiện tại. */
    public void hoanTat(String[] ketQua, LocalDateTime thoiDiem) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH || ketQua.length != 3 || ketQua[0].isBlank()) {
            throw new IllegalStateException("Kết luận hoặc trạng thái hoàn tất chưa hợp lệ.");
        }
        ketQuaHoanTat = ketQua.clone();
        if (thoiDiem == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        thoiDiemHoanTat = thoiDiem;
        trangThai = TrangThaiSuaChua.HOAN_THANH;
    }
}