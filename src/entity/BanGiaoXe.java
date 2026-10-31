package entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Chứng từ bất biến, giữ căn cứ và kết quả sửa chữa tại lúc giao; không có API sửa/xóa. */
public final class BanGiaoXe {
    private final String maBanGiao, nguoiGiao, nguoiNhan, tinhTrang, taiSanDaTra, ghiChu, hinhThucXacNhan;
    private final LocalDateTime thoiDiemGiao, thoiDiemGhiNhan;
    private final PhieuSuaChua lenh;
    private final BaoGia baoGia;
    private final CanCuBanGiao canCu;

    public BanGiaoXe(String ma, PhieuSuaChua lenh, BaoGia baoGia, CanCuBanGiao canCu, TaiKhoan giao,
            String nhan, LocalDateTime lucGiao, String tinhTrang, String taiSan, String ghiChu,
            String hinhThuc, boolean khachXacNhan, LocalDateTime lucLuu) {
        if (giao == null || giao.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chỉ tiếp nhận xác nhận giao xe.");
        if (!khachXacNhan) throw new IllegalArgumentException("Cần khách/người nhận xác nhận đã nhận xe.");
        this.lenh = new PhieuSuaChua(Objects.requireNonNull(lenh));
        this.baoGia = Objects.requireNonNull(baoGia); this.canCu = Objects.requireNonNull(canCu);
        if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || lenh.getThoiDiemHoanTat() == null
                || !lenh.getMaBaoGia().equals(baoGia.getMaBaoGia()) || baoGia.getBaoCao() == null
                || !canCu.getMaLenh().equals(lenh.getMaPhieu()) || !canCu.duDieuKien()) throw new IllegalArgumentException("Chưa đủ căn cứ bàn giao.");
        if (lucGiao == null || lucLuu == null || lucGiao.isAfter(lucLuu) || canCu.getThoiDiem().isAfter(lucLuu)
                || lucGiao.isBefore(lenh.getThoiDiemHoanTat().truncatedTo(ChronoUnit.MINUTES))
                || lucGiao.isBefore(canCu.getThoiDiem().truncatedTo(ChronoUnit.MINUTES))) {
            throw new IllegalArgumentException("Giờ giao phải từ lúc hoàn tất sửa chữa/xác nhận căn cứ đến hiện tại.");
        }
        maBanGiao = batBuoc(ma, "Mã bàn giao"); nguoiGiao = giao.getTenDangNhap();
        nguoiNhan = batBuoc(nhan, "Người nhận xe"); this.tinhTrang = batBuoc(tinhTrang, "Tình trạng khi bàn giao");
        hinhThucXacNhan = batBuoc(hinhThuc, "Hình thức xác nhận của khách");
        if (!hinhThucXacNhan.equals("Ký phiếu bàn giao") && !hinhThucXacNhan.equals("Xác nhận trực tiếp")) throw new IllegalArgumentException("Cần chọn hình thức khách xác nhận hợp lệ.");
        taiSanDaTra = taiSan == null ? "" : taiSan.strip(); this.ghiChu = ghiChu == null ? "" : ghiChu.strip();
        if (!baoGia.getBaoCao().getPhieu().getTaiSan().isBlank() && taiSanDaTra.isBlank()) throw new IllegalArgumentException("Cần ghi rõ tài sản/phụ kiện đã đối chiếu và trả lại.");
        thoiDiemGiao = lucGiao; thoiDiemGhiNhan = lucLuu;
    }

    public String getMaBanGiao() { return maBanGiao; }
    public String getMaLenh() { return lenh.getMaPhieu(); }
    public String getMaXe() { return baoGia.getBaoCao().getPhieu().getMaXe(); }
    public String getBienSo() { return lenh.getBienSo(); }
    public String getTenKhach() { return baoGia.getBaoCao().getPhieu().getTenKhach(); }
    public String getNguoiGiao() { return nguoiGiao; }
    public String getNguoiNhan() { return nguoiNhan; }
    public String getTinhTrang() { return tinhTrang; }
    public String getTaiSanDaTra() { return taiSanDaTra; }
    public String getGhiChu() { return ghiChu; }
    public String getHinhThucXacNhan() { return hinhThucXacNhan; }
    public LocalDateTime getThoiDiemGiao() { return thoiDiemGiao; }
    public LocalDateTime getThoiDiemGhiNhan() { return thoiDiemGhiNhan; }
    public PhieuSuaChua getLenh() { return new PhieuSuaChua(lenh); }
    public BaoGia getBaoGia() { return baoGia; }
    public CanCuBanGiao getCanCu() { return canCu; }
    private static String batBuoc(String s, String ten) { if (s == null || s.isBlank()) throw new IllegalArgumentException(ten + " không được để trống."); return s.strip(); }
}
