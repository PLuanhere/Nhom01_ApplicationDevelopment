package entity.bangiao;

import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;

import java.time.LocalDateTime;

/** Bản đọc từ nguồn thu ngân/phê duyệt quản lý; UI tiếp nhận không được ghi nguồn này. */
public final class CanCuBanGiao {
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
