package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bản đọc từ nguồn thu ngân/phê duyệt quản lý; UI tiếp nhận không được ghi nguồn này. */
public final class CanCuBanGiao {
    private final String maLenh, maChungTu, lyDo;
    private final BigDecimal phaiThu, daThu;
    private final TaiKhoan nguoiXacNhan;
    private final LocalDateTime thoiDiem;
    private final boolean ngoaiLe;

    public CanCuBanGiao(String maLenh, String maChungTu, BigDecimal phaiThu, BigDecimal daThu,
            TaiKhoan nguoiXacNhan, LocalDateTime thoiDiem, boolean ngoaiLe, String lyDo) {
        if (maLenh == null || maLenh.isBlank() || maChungTu == null || maChungTu.isBlank()
                || phaiThu == null || daThu == null || phaiThu.signum() < 0 || daThu.signum() < 0
                || daThu.compareTo(phaiThu) > 0 || thoiDiem == null) {
            throw new IllegalArgumentException("Căn cứ thanh toán/phê duyệt không hợp lệ.");
        }
        VaiTro vaiTro = ngoaiLe ? VaiTro.QUAN_LY : VaiTro.THU_NGAN;
        if (nguoiXacNhan == null || nguoiXacNhan.getVaiTro() != vaiTro) throw new SecurityException("Sai vai trò xác nhận căn cứ bàn giao.");
        if (ngoaiLe && (lyDo == null || lyDo.isBlank())) throw new IllegalArgumentException("Phê duyệt ngoại lệ phải có lý do.");
        this.maLenh = maLenh.strip(); this.maChungTu = maChungTu.strip();
        this.phaiThu = phaiThu; this.daThu = daThu; this.nguoiXacNhan = nguoiXacNhan;
        this.thoiDiem = thoiDiem; this.ngoaiLe = ngoaiLe; this.lyDo = lyDo == null ? "" : lyDo.strip();
    }

    public String getMaLenh() { return maLenh; }
    public String getMaChungTu() { return maChungTu; }
    public BigDecimal getPhaiThu() { return phaiThu; }
    public BigDecimal getDaThu() { return daThu; }
    public TaiKhoan getNguoiXacNhan() { return nguoiXacNhan; }
    public LocalDateTime getThoiDiem() { return thoiDiem; }
    public boolean isNgoaiLe() { return ngoaiLe; }
    public String getLyDo() { return lyDo; }
    public boolean duDieuKien() { return ngoaiLe || daThu.compareTo(phaiThu) == 0; }

    /** So sánh cả nội dung chứng từ để buộc đối chiếu lại khi nguồn tài chính thay đổi. */
    public boolean cungNoiDung(CanCuBanGiao b) {
        return b != null && maLenh.equals(b.maLenh) && maChungTu.equals(b.maChungTu)
                && phaiThu.compareTo(b.phaiThu) == 0 && daThu.compareTo(b.daThu) == 0
                && nguoiXacNhan.getTenDangNhap().equals(b.nguoiXacNhan.getTenDangNhap())
                && nguoiXacNhan.getVaiTro() == b.nguoiXacNhan.getVaiTro()
                && thoiDiem.equals(b.thoiDiem) && ngoaiLe == b.ngoaiLe && lyDo.equals(b.lyDo);
    }
}
