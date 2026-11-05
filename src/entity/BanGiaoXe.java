package entity;

import entity.BaoGia;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Chứng từ bất biến, giữ căn cứ và kết quả sửa chữa tại lúc giao; không có API sửa/xóa. */
public final class BanGiaoXe {
    private final String maBanGiao, nguoiGiao, nguoiNhan, tinhTrang, taiSanDaTra, ghiChu, hinhThucXacNhan;
    private final LocalDateTime thoiDiemGiao, thoiDiemGhiNhan;
    private final PhieuSuaChua lenh;
    private final BaoGia baoGia;
    private final CanCuBanGiao canCu;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BanGiaoXe(String ma, PhieuSuaChua lenh, BaoGia baoGia, CanCuBanGiao canCu, TaiKhoan giao, String nhan,
            LocalDateTime lucGiao, String tinhTrang, String taiSan, String ghiChu, String hinhThuc,
            boolean khachXacNhan, LocalDateTime lucLuu) {
        if (giao == null || giao.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ tiếp nhận xác nhận giao xe.");
        }
        if (!khachXacNhan) {
            throw new IllegalArgumentException("Cần khách/người nhận xác nhận đã nhận xe.");
        }
        if (lenh == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.lenh = new PhieuSuaChua(lenh);
        if (baoGia == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.baoGia = baoGia;
        if (canCu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.canCu = canCu;
        if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || lenh.getThoiDiemHoanTat() == null
                || !lenh.getMaBaoGia().equals(baoGia.getMaBaoGia()) || baoGia.getBaoCao() == null
                || !canCu.getMaLenh().equals(lenh.getMaPhieu()) || !canCu.duDieuKien()) {
            throw new IllegalArgumentException("Chưa đủ căn cứ bàn giao.");
        }
        if (lucGiao == null || lucLuu == null || lucGiao.isAfter(lucLuu) || canCu.getThoiDiem().isAfter(lucLuu)
                || lucGiao.isBefore(lenh.getThoiDiemHoanTat().truncatedTo(ChronoUnit.MINUTES))
                || lucGiao.isBefore(canCu.getThoiDiem().truncatedTo(ChronoUnit.MINUTES))) {
            throw new IllegalArgumentException("Giờ giao phải từ lúc hoàn tất sửa chữa/xác nhận căn cứ đến hiện tại.");
        }
        maBanGiao = batBuoc(ma, "Mã bàn giao");
        nguoiGiao = giao.getTenDangNhap();
        nguoiNhan = batBuoc(nhan, "Người nhận xe");
        this.tinhTrang = batBuoc(tinhTrang, "Tình trạng khi bàn giao");
        hinhThucXacNhan = batBuoc(hinhThuc, "Hình thức xác nhận của khách");
        if (!hinhThucXacNhan.equals("Ký phiếu bàn giao") && !hinhThucXacNhan.equals("Xác nhận trực tiếp")) {
            throw new IllegalArgumentException("Cần chọn hình thức khách xác nhận hợp lệ.");
        }
        taiSanDaTra = taiSan == null ? "" : taiSan.trim();
        this.ghiChu = ghiChu == null ? "" : ghiChu.trim();
        if (!baoGia.getBaoCao().getPhieu().getTaiSan().isBlank() && taiSanDaTra.isBlank()) {
            throw new IllegalArgumentException("Cần ghi rõ tài sản/phụ kiện đã đối chiếu và trả lại.");
        }
        thoiDiemGiao = lucGiao;
        thoiDiemGhiNhan = lucLuu;
    }

    /** Trả về ma ban giao. */
    public String getMaBanGiao() {
        return maBanGiao;
    }

    /** Trả về ma lenh. */
    public String getMaLenh() {
        return lenh.getMaPhieu();
    }

    /** Trả về ma xe. */
    public String getMaXe() {
        return baoGia.getBaoCao().getPhieu().getMaXe();
    }

    /** Trả về bien so. */
    public String getBienSo() {
        return lenh.getBienSo();
    }

    /** Trả về ten khach. */
    public String getTenKhach() {
        return baoGia.getBaoCao().getPhieu().getTenKhach();
    }

    /** Trả về nguoi giao. */
    public String getNguoiGiao() {
        return nguoiGiao;
    }

    /** Trả về nguoi nhan. */
    public String getNguoiNhan() {
        return nguoiNhan;
    }

    /** Trả về tinh trang. */
    public String getTinhTrang() {
        return tinhTrang;
    }

    /** Trả về tai san da tra. */
    public String getTaiSanDaTra() {
        return taiSanDaTra;
    }

    /** Trả về ghi chu. */
    public String getGhiChu() {
        return ghiChu;
    }

    /** Trả về hinh thuc xac nhan. */
    public String getHinhThucXacNhan() {
        return hinhThucXacNhan;
    }

    /** Trả về thoi diem giao. */
    public LocalDateTime getThoiDiemGiao() {
        return thoiDiemGiao;
    }

    /** Trả về thoi diem ghi nhan. */
    public LocalDateTime getThoiDiemGhiNhan() {
        return thoiDiemGhiNhan;
    }

    /** Trả về lenh. */
    public PhieuSuaChua getLenh() {
        return new PhieuSuaChua(lenh);
    }

    /** Trả về bao gia. */
    public BaoGia getBaoGia() {
        return baoGia;
    }

    /** Trả về can cu. */
    public CanCuBanGiao getCanCu() {
        return canCu;
    }

    /** Thực hiện bat buoc cho thành phần hiện tại. */
    private static String batBuoc(String s, String ten) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(ten + " không được để trống.");
        }
        return s.trim();
    }

    public static final class CanCuBanGiao {
        private final String maLenh, maChungTu, lyDo;
        private final long phaiThu, daThu;
        private final TaiKhoan nguoiXacNhan;
        private final LocalDateTime thoiDiem;
        private final boolean ngoaiLe;
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        public CanCuBanGiao(String maLenh, String maChungTu, long phaiThu, long daThu, TaiKhoan nguoiXacNhan,
                LocalDateTime thoiDiem, boolean ngoaiLe, String lyDo) {
            if (maLenh == null || maLenh.isBlank() || maChungTu == null || maChungTu.isBlank() || (phaiThu < 0)
                    || (daThu < 0) || (daThu > phaiThu) || thoiDiem == null) {
                throw new IllegalArgumentException("Căn cứ thanh toán/phê duyệt không hợp lệ.");
            }
            VaiTro vaiTro = ngoaiLe ? VaiTro.QUAN_LY : VaiTro.THU_NGAN;
            if (nguoiXacNhan == null || nguoiXacNhan.getVaiTro() != vaiTro) {
                throw new SecurityException("Sai vai trò xác nhận căn cứ bàn giao.");
            }
            if (ngoaiLe && (lyDo == null || lyDo.isBlank())) {
                throw new IllegalArgumentException("Phê duyệt ngoại lệ phải có lý do.");
            }
            this.maLenh = maLenh.trim();
            this.maChungTu = maChungTu.trim();
            this.phaiThu = phaiThu;
            this.daThu = daThu;
            this.nguoiXacNhan = nguoiXacNhan;
            this.thoiDiem = thoiDiem;
            this.ngoaiLe = ngoaiLe;
            this.lyDo = lyDo == null ? "" : lyDo.trim();
        }
    
        /** Trả về ma lenh. */
        public String getMaLenh() {
            return maLenh;
        }
    
        /** Trả về ma chung tu. */
        public String getMaChungTu() {
            return maChungTu;
        }
    
        /** Trả về phai thu. */
        public long getPhaiThu() {
            return phaiThu;
        }
    
        /** Trả về da thu. */
        public long getDaThu() {
            return daThu;
        }
    
        /** Trả về nguoi xac nhan. */
        public TaiKhoan getNguoiXacNhan() {
            return nguoiXacNhan;
        }
    
        /** Trả về thoi diem. */
        public LocalDateTime getThoiDiem() {
            return thoiDiem;
        }
    
        /** Cho biết ngoai le. */
        public boolean isNgoaiLe() {
            return ngoaiLe;
        }
    
        /** Trả về ly do. */
        public String getLyDo() {
            return lyDo;
        }
    
        /** Thực hiện du dieu kien cho thành phần hiện tại. */
        public boolean duDieuKien() {
            return ngoaiLe || (daThu == phaiThu);
        }
    
        /** So sánh cả nội dung chứng từ để buộc đối chiếu lại khi nguồn tài chính thay đổi. */
        public boolean cungNoiDung(CanCuBanGiao b) {
            return b != null && maLenh.equals(b.maLenh) && maChungTu.equals(b.maChungTu) && (phaiThu == b.phaiThu)
                    && (daThu == b.daThu) && nguoiXacNhan.getTenDangNhap().equals(b.nguoiXacNhan.getTenDangNhap())
                    && nguoiXacNhan.getVaiTro() == b.nguoiXacNhan.getVaiTro() && thoiDiem.equals(b.thoiDiem)
                    && ngoaiLe == b.ngoaiLe && lyDo.equals(b.lyDo);
        }
    }

    public enum TrangThaiBanGiao {
        CHO_CAN_CU("Chờ xác nhận thanh toán"), CHO_GIAO("Chờ bàn giao"), DA_GIAO("Đã bàn giao");
    
        private final String tenHienThi;
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        TrangThaiBanGiao(String tenHienThi) {
            this.tenHienThi = tenHienThi;
        }
    
        /** Trả về ten hien thi. */
        public String getTenHienThi() {
            return tenHienThi;
        }
    }
}
