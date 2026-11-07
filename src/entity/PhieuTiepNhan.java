package entity;

import entity.KhachHang;
import entity.Xe;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Phiếu chỉ đọc sau xác nhận; giữ snapshot hồ sơ tại thời điểm tiếp nhận.
public final class PhieuTiepNhan {
    private final String maPhieu, bienSo, yeuCau, tinhTrang, taiKhoanKyThuat;
    private final boolean daXacNhan;
    private final String maXe, maKH, tenKhach, soDienThoai, tenXe, taiSan, nguoiTiepNhan;
    private final long soKm;
    private final LocalDateTime thoiGianNhan, thoiGianXacNhan;

    // Giữ nguyên hợp đồng constructor mà DAO kiểm tra của nhóm đang dùng.
    public PhieuTiepNhan(String maPhieu, String bienSo, String yeuCau, String tinhTrang, String taiKhoanKyThuat,
            boolean daXacNhan) {
        if (maPhieu == null || bienSo == null || taiKhoanKyThuat == null || yeuCau == null || tinhTrang == null
                || maPhieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Thông tin phiếu tiếp nhận không hợp lệ.");
        }
        this.maPhieu = maPhieu;
        this.bienSo = bienSo;
        this.yeuCau = yeuCau;
        this.tinhTrang = tinhTrang;
        this.taiKhoanKyThuat = taiKhoanKyThuat;
        this.daXacNhan = daXacNhan;
        maXe = "";
        maKH = "";
        tenKhach = "";
        soDienThoai = "";
        tenXe = "";
        taiSan = "";
        nguoiTiepNhan = "";
        soKm = 0;
        thoiGianNhan = null;
        thoiGianXacNhan = null;
    }

    // Phiếu mới đã được khách xác nhận; tồn tại/quan hệ/quyền được kiểm tra tại DAO.
    public PhieuTiepNhan(String maPhieu, Xe xe, KhachHang khach, long soKm, LocalDateTime thoiGianNhan,
            String tinhTrang, String taiSan, String yeuCau, String nguoiTiepNhan, LocalDateTime thoiGianXacNhan) {
        this.maPhieu = batBuoc(maPhieu, "Mã phiếu");
        if (xe == null) {
            throw new NullPointerException("Cần phương tiện.");
        }

        if (khach == null) {
            throw new NullPointerException("Cần khách hàng.");
        }

        if (!xe.getMaKH().equals(khach.getMaKH())) {
            throw new IllegalArgumentException("Xe không thuộc khách đã chọn.");
        }
        if (soKm < 0) {
            throw new IllegalArgumentException("Kilomet không được âm.");
        }
        this.soKm = soKm;
        if (thoiGianNhan == null) {
            throw new NullPointerException("Cần thời gian nhận.");
        }
        this.thoiGianNhan = thoiGianNhan;
        if (thoiGianXacNhan == null) {
            throw new NullPointerException("Cần thời điểm xác nhận.");
        }
        this.thoiGianXacNhan = thoiGianXacNhan;
        this.tinhTrang = batBuoc(tinhTrang, "Tình trạng ban đầu");
        this.yeuCau = batBuoc(yeuCau, "Yêu cầu khách hàng");
        this.taiSan = taiSan == null ? "" : taiSan.trim();
        this.nguoiTiepNhan = batBuoc(nguoiTiepNhan, "Người tiếp nhận");
        maXe = xe.getMaXe();
        maKH = khach.getMaKH();
        bienSo = xe.getBienSo();
        tenKhach = khach.getHoTen();
        soDienThoai = khach.getSoDienThoai();
        tenXe = xe.getHangXe() + " " + xe.getModel();
        taiKhoanKyThuat = "";
        daXacNhan = true;
    }

    // Khôi phục snapshot đã lưu trong database mà không lấy lại hồ sơ khách hoặc xe hiện tại.
    public PhieuTiepNhan(String maPhieu, String bienSo, String yeuCau, String tinhTrang,
            String taiKhoanKyThuat, boolean daXacNhan, String maXe, String maKH,
            String tenKhach, String soDienThoai, String tenXe, String taiSan,
            String nguoiTiepNhan, long soKm, LocalDateTime thoiGianNhan,
            LocalDateTime thoiGianXacNhan) {
        this.maPhieu = batBuoc(maPhieu, "Mã phiếu");
        this.bienSo = batBuoc(bienSo, "Biển số");
        this.yeuCau = batBuoc(yeuCau, "Yêu cầu");
        this.tinhTrang = batBuoc(tinhTrang, "Tình trạng");
        this.taiKhoanKyThuat = taiKhoanKyThuat;
        this.daXacNhan = daXacNhan;
        this.maXe = maXe;
        this.maKH = maKH;
        this.tenKhach = tenKhach;
        this.soDienThoai = soDienThoai;
        this.tenXe = tenXe;
        this.taiSan = taiSan;
        this.nguoiTiepNhan = nguoiTiepNhan;
        this.soKm = soKm;
        this.thoiGianNhan = thoiGianNhan;
        this.thoiGianXacNhan = thoiGianXacNhan;
    }

    // Trả về ma phieu.
    public String getMaPhieu() {
        return maPhieu;
    }

    // Trả về bien so.
    public String getBienSo() {
        return bienSo;
    }

    // Trả về yeu cau.
    public String getYeuCau() {
        return yeuCau;
    }

    // Trả về tinh trang.
    public String getTinhTrang() {
        return tinhTrang;
    }

    // Trả về tai khoan ky thuat.
    public String getTaiKhoanKyThuat() {
        return taiKhoanKyThuat;
    }

    // Cho biết da xac nhan.
    public boolean isDaXacNhan() {
        return daXacNhan;
    }

    // Trả về ma xe.
    public String getMaXe() {
        return maXe;
    }

    // Trả về ma kh.
    public String getMaKH() {
        return maKH;
    }

    // Trả về ten khach.
    public String getTenKhach() {
        return tenKhach;
    }

    // Trả về so dien thoai.
    public String getSoDienThoai() {
        return soDienThoai;
    }

    // Trả về ten xe.
    public String getTenXe() {
        return tenXe;
    }

    // Trả về tai san.
    public String getTaiSan() {
        return taiSan;
    }

    // Trả về nguoi tiep nhan.
    public String getNguoiTiepNhan() {
        return nguoiTiepNhan;
    }

    // Trả về so km.
    public long getSoKm() {
        return soKm;
    }

    // Trả về thoi gian nhan.
    public LocalDateTime getThoiGianNhan() {
        return thoiGianNhan;
    }

    // Trả về thoi gian xac nhan.
    public LocalDateTime getThoiGianXacNhan() {
        return thoiGianXacNhan;
    }

    // Trả về trang thai.
    public TrangThaiTiepNhan getTrangThai() {
        return daXacNhan ? TrangThaiTiepNhan.CHO_KIEM_TRA : TrangThaiTiepNhan.NHAP;
    }

    // Thực hiện to string cho thành phần hiện tại.
    @Override
    public String toString() {
        return maPhieu + (daXacNhan ? " · Đã xác nhận" : " · Nháp");
    }

    // Thực hiện bat buoc cho thành phần hiện tại.
    private static String batBuoc(String chu, String ten) {
        if (chu == null || chu.isBlank()) {
            throw new IllegalArgumentException(ten + " không được rỗng.");
        }
        return chu.trim();
    }

    public static final class BaoCaoKiemTra {
        private final String maBaoCao;
        private final PhieuTiepNhan phieu;
        private final LocalDate ngayKiemTra;
        private final String boPhan;
        // Thứ tự: kết quả, hư hỏng, đề xuất, phanh, dầu, điện, mã lỗi.
        private final String[] noiDung;
        private final String[] deXuatTheoVung;
        private final TrangThaiKiemTra trangThai;
        private final LocalDateTime thoiDiemHoanTat;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public BaoCaoKiemTra(String maBaoCao, PhieuTiepNhan phieu, LocalDate ngayKiemTra, String boPhan, String[] noiDung,
                TrangThaiKiemTra trangThai, LocalDateTime thoiDiemHoanTat) {
            this(maBaoCao, phieu, ngayKiemTra, boPhan, noiDung, trangThai, thoiDiemHoanTat, new String[] { "", "", "" });
        }

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        public BaoCaoKiemTra(String maBaoCao, PhieuTiepNhan phieu, LocalDate ngayKiemTra, String boPhan, String[] noiDung,
                TrangThaiKiemTra trangThai, LocalDateTime thoiDiemHoanTat, String[] deXuatTheoVung) {
            if (maBaoCao == null || phieu == null || ngayKiemTra == null || boPhan == null || noiDung == null
                    || noiDung.length != 7 || trangThai == null) {
                throw new IllegalArgumentException("Thông tin báo cáo không hợp lệ.");
            }
            for (String giaTri : noiDung) {
                if (giaTri == null) {
                    throw new IllegalArgumentException("Nội dung kiểm tra không được null.");
                }
            }
            if ((trangThai == TrangThaiKiemTra.HOAN_TAT) != (thoiDiemHoanTat != null)) {
                throw new IllegalArgumentException("Thời điểm hoàn tất không khớp trạng thái.");
            }
            if (deXuatTheoVung == null || deXuatTheoVung.length != 3) {
                throw new IllegalArgumentException("Cần ba đề xuất theo khu vực kiểm tra.");
            }
            for (String deXuat : deXuatTheoVung) {
                if (deXuat == null) {
                    throw new IllegalArgumentException("Đề xuất theo khu vực không được null.");
                }
            }
            this.maBaoCao = maBaoCao;
            this.phieu = phieu;
            this.ngayKiemTra = ngayKiemTra;
            this.boPhan = boPhan;
            this.noiDung = noiDung.clone();
            this.deXuatTheoVung = deXuatTheoVung.clone();
            this.trangThai = trangThai;
            this.thoiDiemHoanTat = thoiDiemHoanTat;
        }

        // Trả về ma bao cao.
        public String getMaBaoCao() {
            return maBaoCao;
        }

        // Trả về phieu.
        public PhieuTiepNhan getPhieu() {
            return phieu;
        }

        // Trả về ngay kiem tra.
        public LocalDate getNgayKiemTra() {
            return ngayKiemTra;
        }

        // Trả về bo phan.
        public String getBoPhan() {
            return boPhan;
        }

        // Trả về noi dung.
        public String[] getNoiDung() {
            return noiDung.clone();
        }

        // Trả về de xuat theo vung.
        public String[] getDeXuatTheoVung() {
            return deXuatTheoVung.clone();
        }

        // Trả về trang thai.
        public TrangThaiKiemTra getTrangThai() {
            return trangThai;
        }

        // Trả về thoi diem hoan tat.
        public LocalDateTime getThoiDiemHoanTat() {
            return thoiDiemHoanTat;
        }
    }

    public enum TrangThaiKiemTra {
        NHAP("Nháp"), HOAN_TAT("Hoàn tất");

        private final String tenHienThi;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        TrangThaiKiemTra(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        // Trả về ten hien thi.
        public String getTenHienThi() {
            return tenHienThi;
        }
    }

    public enum TrangThaiTiepNhan {
        NHAP("Nháp"), CHO_KIEM_TRA("Chờ kiểm tra");

        private final String tenHienThi;

        // Khởi tạo dữ liệu và thành phần cần dùng của lớp.
        TrangThaiTiepNhan(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }

        // Trả về ten hien thi.
        public String getTenHienThi() {
            return tenHienThi;
        }
    }
}
