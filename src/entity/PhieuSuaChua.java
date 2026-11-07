package entity;

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
    private final List<ChiTietSuaChua> danhSachHangMuc;
    private PhatSinhSuaChua phatSinh;

    private String[] ketQuaHoanTat = { "", "", "" };
    private LocalDateTime thoiDiemHoanTat;
    private String nguoiPhatHanh = "";
    private LocalDateTime thoiDiemPhatHanh;

    // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
    public PhieuSuaChua(String maPhieu, String maBaoGia, String bienSo, String kyThuatChinh, String kyThuatPhoiHop,
            LocalDateTime hanHoanThanh, String ghiChu, TrangThaiSuaChua trangThai, List<ChiTietSuaChua> danhSachHangMuc,
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
        this.danhSachHangMuc = new ArrayList<ChiTietSuaChua>(danhSachHangMuc);
        this.phatSinh = phatSinh;
        if (maPhieu.isBlank() || maBaoGia.isBlank() || bienSo.isBlank() || danhSachHangMuc.isEmpty()) {
            throw new IllegalArgumentException("Thông tin lệnh sửa chữa chưa hợp lệ.");
        }
    }

    // Giữ constructor cũ cho kỹ thuật; lệnh tiếp nhận mới có thêm dấu vết phát hành.
    public PhieuSuaChua(String maPhieu, String maBaoGia, String bienSo, String kyThuatChinh, String kyThuatPhoiHop,
            LocalDateTime hanHoanThanh, String ghiChu, List<ChiTietSuaChua> hangMuc, String nguoiPhatHanh,
            LocalDateTime thoiDiemPhatHanh) {
        this(maPhieu, maBaoGia, bienSo, kyThuatChinh, kyThuatPhoiHop, hanHoanThanh, ghiChu,
                TrangThaiSuaChua.CHO_SUA_CHUA, hangMuc, null);
        if (nguoiPhatHanh == null || nguoiPhatHanh.isBlank() || thoiDiemPhatHanh == null) {
            throw new IllegalArgumentException("Cần người và thời điểm phát hành lệnh.");
        }
        this.nguoiPhatHanh = nguoiPhatHanh.trim();
        this.thoiDiemPhatHanh = thoiDiemPhatHanh;
    }

    // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
    public PhieuSuaChua(PhieuSuaChua phieu) {
        this(phieu.maPhieu, phieu.maBaoGia, phieu.bienSo, phieu.kyThuatChinh, phieu.kyThuatPhoiHop, phieu.hanHoanThanh,
                phieu.ghiChu, phieu.trangThai, phieu.danhSachHangMuc, phieu.phatSinh);
        ketQuaHoanTat = phieu.ketQuaHoanTat.clone();
        thoiDiemHoanTat = phieu.thoiDiemHoanTat;
        nguoiPhatHanh = phieu.nguoiPhatHanh;
        thoiDiemPhatHanh = phieu.thoiDiemPhatHanh;
    }

    // Khôi phục lệnh cùng lịch sử phát hành và kết quả hoàn tất đã được lưu.
    public PhieuSuaChua(String ma, String maBG, String bienSo, String chinh, String hoTro,
            LocalDateTime han, String ghiChu, TrangThaiSuaChua trangThai,
            List<ChiTietSuaChua> hangMuc, PhatSinhSuaChua phatSinh, String nguoiPhatHanh,
            LocalDateTime lucPhatHanh, String[] ketQua, LocalDateTime lucHoanTat) {
        this(ma, maBG, bienSo, chinh, hoTro, han, ghiChu, trangThai, hangMuc, phatSinh);
        if (ketQua == null || ketQua.length != 3
                || (trangThai == TrangThaiSuaChua.HOAN_THANH) != (lucHoanTat != null)) {
            throw new IllegalArgumentException("Trạng thái và kết quả hoàn tất không hợp lệ.");
        }
        this.nguoiPhatHanh = nguoiPhatHanh;
        this.thoiDiemPhatHanh = lucPhatHanh;
        this.ketQuaHoanTat = ketQua.clone();
        this.thoiDiemHoanTat = lucHoanTat;
    }

    // Trả về nguoi phat hanh.
    public String getNguoiPhatHanh() {
        return nguoiPhatHanh;
    }

    // Trả về thoi diem phat hanh.
    public LocalDateTime getThoiDiemPhatHanh() {
        return thoiDiemPhatHanh;
    }

    // Trả về ma phieu.
    public String getMaPhieu() {
        return maPhieu;
    }

    // Trả về ma bao gia.
    public String getMaBaoGia() {
        return maBaoGia;
    }

    // Trả về bien so.
    public String getBienSo() {
        return bienSo;
    }

    // Trả về ky thuat chinh.
    public String getKyThuatChinh() {
        return kyThuatChinh;
    }

    // Trả về ky thuat phoi hop.
    public String getKyThuatPhoiHop() {
        return kyThuatPhoiHop;
    }

    // Trả về han hoan thanh.
    public LocalDateTime getHanHoanThanh() {
        return hanHoanThanh;
    }

    // Trả về ghi chu.
    public String getGhiChu() {
        return ghiChu;
    }

    // Trả về trang thai.
    public TrangThaiSuaChua getTrangThai() {
        return trangThai;
    }

    // Trả về danh sach hang muc.
    public List<ChiTietSuaChua> getDanhSachHangMuc() {
        return new ArrayList<ChiTietSuaChua>(danhSachHangMuc);
    }

    // Trả về phat sinh.
    public PhatSinhSuaChua getPhatSinh() {
        return phatSinh;
    }

    // Trả về ket qua hoan tat.
    public String[] getKetQuaHoanTat() {
        return ketQuaHoanTat.clone();
    }

    // Trả về thoi diem hoan tat.
    public LocalDateTime getThoiDiemHoanTat() {
        return thoiDiemHoanTat;
    }

    // Cập nhật thông tin phân công khi lệnh chưa bắt đầu thực hiện.
    // Thực hiện cap nhat phan cong cho thành phần hiện tại.
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
    // Thực hiện cap nhat hang muc cho thành phần hiện tại.
    public void capNhatHangMuc(int viTri, ChiTietSuaChua hangMuc) {
        if (trangThai == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành, không sửa tiến độ trực tiếp.");
        }
        danhSachHangMuc.set(viTri, hangMuc);
        trangThai = TrangThaiSuaChua.DANG_SUA;
    }

    // Ghi nhận một đề xuất phát sinh đang chờ phản hồi từ khách.
    // Thực hiện ghi nhan phat sinh cho thành phần hiện tại.
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
    // Thực hiện hoan tat cho thành phần hiện tại.
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

    public static final class ChiTietSuaChua {
        private final String tenHangMuc;
        private final String taiKhoanThucHien;
        private final String ketQua;
        private final TrangThaiSuaChua trangThai;

        private final boolean phatSinh;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public ChiTietSuaChua(String tenHangMuc, String taiKhoanThucHien, String ketQua, TrangThaiSuaChua trangThai,
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

        // Trả về ten hang muc.
        public String getTenHangMuc() {
            return tenHangMuc;
        }

        // Trả về tai khoan thuc hien.
        public String getTaiKhoanThucHien() {
            return taiKhoanThucHien;
        }

        // Trả về ket qua.
        public String getKetQua() {
            return ketQua;
        }

        // Trả về trang thai.
        public TrangThaiSuaChua getTrangThai() {
            return trangThai;
        }

        // Cho biết phat sinh.
        public boolean isPhatSinh() {
            return phatSinh;
        }
    }

    public static final class PhatSinhSuaChua {
        // Thứ tự: hư hỏng, nguyên nhân, đề xuất, thời gian thêm và ghi chú.
        private final String[] noiDung;

        private final TrangThaiPhatSinh trangThai;
        private final String nguoiXacNhan;
        private final LocalDateTime thoiDiemXacNhan;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public PhatSinhSuaChua(String[] noiDung, TrangThaiPhatSinh trangThai, String nguoiXacNhan,
                LocalDateTime thoiDiemXacNhan) {
            if (noiDung == null || noiDung.length != 5) {
                throw new IllegalArgumentException("Đề xuất phát sinh phải có đủ năm trường thông tin.");
            }
            for (String giaTri : noiDung) {
                if (giaTri == null) {
                    throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
                }

            }
            if (noiDung[0].isBlank() || noiDung[1].isBlank() || noiDung[2].isBlank()) {
                throw new IllegalArgumentException("Cần nhập hư hỏng, nguyên nhân và đề xuất.");
            }
            if (trangThai == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.trangThai = trangThai;
            if (nguoiXacNhan == null) {
                throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
            }
            this.nguoiXacNhan = nguoiXacNhan;
            if (trangThai != TrangThaiPhatSinh.CHO_DUYET && (nguoiXacNhan.isBlank() || thoiDiemXacNhan == null)) {
                throw new IllegalArgumentException("Phản hồi khách cần có người và thời điểm xác nhận.");
            }
            this.noiDung = noiDung.clone();
            this.thoiDiemXacNhan = thoiDiemXacNhan;
        }

        // Trả về noi dung.
        public String[] getNoiDung() {
            return noiDung.clone();
        }

        // Trả về trang thai.
        public TrangThaiPhatSinh getTrangThai() {
            return trangThai;
        }

        // Trả về nguoi xac nhan.
        public String getNguoiXacNhan() {
            return nguoiXacNhan;
        }

        // Trả về thoi diem xac nhan.
        public LocalDateTime getThoiDiemXacNhan() {
            return thoiDiemXacNhan;
        }
    }

    public enum TrangThaiPhatSinh {
        CHO_DUYET("Chờ khách duyệt"), DA_DUYET("Đã được khách đồng ý"), TU_CHOI("Khách từ chối");

        private final String tenHienThi;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        TrangThaiPhatSinh(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        // Trả về ten hien thi.
        public String getTenHienThi() {
            return tenHienThi;
        }
    }

    public enum TrangThaiSuaChua {
        CHO_SUA_CHUA("Chờ sửa chữa"), DANG_SUA("Đang sửa"), CHO_PHU_TUNG("Chờ phụ tùng"), HOAN_THANH("Hoàn thành");

        private final String tenHienThi;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        TrangThaiSuaChua(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        // Trả về ten hien thi.
        public String getTenHienThi() {
            return tenHienThi;
        }

        // {@inheritDoc}
        // Hiển thị nhãn trạng thái trong JComboBox.
        @Override
        public String toString() {
            return tenHienThi;
        }
    }
}
