package dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import entity.KyThuatVienPhanCong;
import entity.KhachHang;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiTiepNhan;
import entity.VaiTro;
import entity.Xe;

/** Kho phiếu trong phiên. Không có API sửa/xóa phiếu đã xác nhận, chưa phải JDBC. */
public final class PhieuTiepNhanDAO {
    private final KhachHangDAO khachHangDAO;
    private final XeDAO xeDAO;
    /** Mỗi mã thao tác chỉ ghi được một nội dung xác nhận. */
    private final Map<String, PhieuTiepNhan> theoThaoTac = new LinkedHashMap<>();
    private int soThuTu = 1;
    private final Supplier<List<KyThuatVienPhanCong>> nguonKyThuat;
    private final Map<String, String> phanCongKiemTra = new LinkedHashMap<>();
    private final Map<String, String> nguoiGiaoKiemTra = new LinkedHashMap<>();
    private final Map<String, LocalDateTime> lucGiaoKiemTra = new LinkedHashMap<>();

    public PhieuTiepNhanDAO(KhachHangDAO khachHangDAO, XeDAO xeDAO) {
        this(khachHangDAO, xeDAO, List::of);
    }

    public PhieuTiepNhanDAO(KhachHangDAO khachHangDAO, XeDAO xeDAO,
            Supplier<List<KyThuatVienPhanCong>> nguonKyThuat) {
        this.khachHangDAO = Objects.requireNonNull(khachHangDAO);
        this.xeDAO = Objects.requireNonNull(xeDAO);
        this.nguonKyThuat = Objects.requireNonNull(nguonKyThuat);
    }

    /**
     * Ghi một phiếu sau mọi kiểm tra. Hai lần đến dùng hai mã thao tác khác nhau.
     * Dữ liệu khách/xe phải đúng nội dung vừa được đối chiếu, tránh lưu snapshot đã cũ.
     */
    public synchronized PhieuTiepNhan luu(TaiKhoan tk, String maThaoTac, Xe xeDoiChieu,
            KhachHang khachDoiChieu, long km, LocalDateTime ngayNhan, String tinhTrang,
            String taiSan, String yeuCau, boolean khachXacNhan) {
        kiemTraQuyen(tk);
        if (maThaoTac == null || maThaoTac.isBlank()) throw new IllegalArgumentException("Thiếu mã thao tác tiếp nhận.");
        if (!khachXacNhan) throw new IllegalArgumentException("Cần khách xác nhận nội dung trước khi lưu.");
        if (xeDoiChieu == null || khachDoiChieu == null || ngayNhan == null) {
            throw new IllegalArgumentException("Thiếu xe, khách hoặc thời gian nhận.");
        }
        Xe xe = xeDAO.timTheoMa(tk, xeDoiChieu.getMaXe());
        KhachHang kh = khachHangDAO.timTheoMa(tk, khachDoiChieu.getMaKH());
        if (!xe.getMaKH().equals(kh.getMaKH()) || !xe.getBienSo().equals(xeDoiChieu.getBienSo())
                || !xe.getHangXe().equals(xeDoiChieu.getHangXe()) || !xe.getModel().equals(xeDoiChieu.getModel())
                || !kh.getHoTen().equals(khachDoiChieu.getHoTen())
                || !kh.getSoDienThoai().equals(khachDoiChieu.getSoDienThoai())) {
            throw new IllegalArgumentException("Hồ sơ đã thay đổi hoặc không khớp. Hãy đối chiếu lại với khách.");
        }
        PhieuTiepNhan cu = theoThaoTac.get(maThaoTac);
        String ma = cu == null ? String.format(Locale.ROOT, "TN-%04d-%04d", ngayNhan.getYear(), soThuTu) : cu.getMaPhieu();
        PhieuTiepNhan moi = new PhieuTiepNhan(ma, xe, kh, km, ngayNhan, tinhTrang,
                taiSan, yeuCau, tk.getTenDangNhap(), LocalDateTime.now());
        if (cu != null) {
            if (!cungNoiDung(cu, moi)) throw new IllegalArgumentException("Mã thao tác đã được lưu với nội dung khác.");
            return cu;
        }
        // Không thay đổi kho hay số thứ tự trước khi toàn bộ dữ liệu đã hợp lệ.
        theoThaoTac.put(maThaoTac, moi);
        soThuTu++;
        return moi;
    }

    public synchronized List<PhieuTiepNhan> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiTiepNhan trangThai) {
        kiemTraQuyen(tk);
        String tu = Objects.requireNonNull(tuKhoa).strip().toLowerCase(Locale.ROOT);
        String bien = tu.toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        for (PhieuTiepNhan phieu : theoThaoTac.values()) {
            String chu = phieu.getMaPhieu() + " " + phieu.getBienSo() + " " + phieu.getTenKhach() + " " + phieu.getSoDienThoai();
            if ((trangThai == null || phieu.getTrangThai() == trangThai) && (chu.toLowerCase(Locale.ROOT).contains(tu)
                    || (!bien.isEmpty() && Xe.chuanHoaBienSo(phieu.getBienSo()).contains(bien)))) ketQua.add(phieu);
        }
        return ketQua;
    }

    public synchronized PhieuTiepNhan timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        for (PhieuTiepNhan phieu : theoThaoTac.values()) if (phieu.getMaPhieu().equals(ma)) return phieu;
        throw new IllegalArgumentException("Phiếu tiếp nhận không tồn tại.");
    }

    public synchronized List<PhieuTiepNhan> lichSuTheoXe(TaiKhoan tk, String maXe) {
        kiemTraQuyen(tk);
        xeDAO.timTheoMa(tk, maXe);
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        for (PhieuTiepNhan phieu : theoThaoTac.values()) if (phieu.getMaXe().equals(maXe)) ketQua.add(phieu);
        return ketQua;
    }

    public synchronized List<KyThuatVienPhanCong> layKyThuatPhanCong(TaiKhoan tk) {
        kiemTraQuyen(tk);
        List<KyThuatVienPhanCong> ketQua = new ArrayList<>();
        for (KyThuatVienPhanCong kt : nguonKyThuat.get()) if (kt.duocPhanCong()) ketQua.add(kt);
        return ketQua;
    }

    /** Giao kiểm tra một lần, không đổi phiếu đã xác nhận để giữ nguyên snapshot và liên kết báo cáo. */
    public synchronized void giaoKiemTra(TaiKhoan tk, PhieuTiepNhan daDoc, String taiKhoanKyThuat) {
        kiemTraQuyen(tk);
        if (daDoc == null || timTheoMa(tk, daDoc.getMaPhieu()) != daDoc) throw new IllegalArgumentException("Cần phiếu tiếp nhận đã lưu.");
        boolean hopLe = false;
        for (KyThuatVienPhanCong kt : layKyThuatPhanCong(tk)) if (kt.getTaiKhoan().getTenDangNhap().equals(taiKhoanKyThuat)) hopLe = true;
        if (!hopLe) throw new IllegalArgumentException("Kỹ thuật viên không còn hoạt động hoặc không có liên kết tài khoản hợp lệ.");
        String cu = phanCongKiemTra.get(daDoc.getMaPhieu());
        if (cu != null) {
            if (!cu.equals(taiKhoanKyThuat)) throw new IllegalStateException("Phiếu đã giao kiểm tra. Không tự đổi người kiểm tra.");
            return;
        }
        phanCongKiemTra.put(daDoc.getMaPhieu(), taiKhoanKyThuat);
        nguoiGiaoKiemTra.put(daDoc.getMaPhieu(), tk.getTenDangNhap());
        lucGiaoKiemTra.put(daDoc.getMaPhieu(), LocalDateTime.now());
    }

    public synchronized String docPhanCongKiemTra(TaiKhoan tk, String ma) {
        timTheoMa(tk, ma);
        String nguoi = phanCongKiemTra.get(ma);
        return nguoi == null ? "Chưa phân công" : nguoi + " · " + nguoiGiaoKiemTra.get(ma) + " · " + lucGiaoKiemTra.get(ma);
    }

    public synchronized List<PhieuTiepNhan> layPhieuKiemTra(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) throw new SecurityException("Chỉ kỹ thuật được đọc phiếu kiểm tra đã giao.");
        boolean dangLam = false;
        for (KyThuatVienPhanCong kt : nguonKyThuat.get()) if (kt.duocPhanCong() && kt.getTaiKhoan().getTenDangNhap().equals(tk.getTenDangNhap())) dangLam = true;
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        if (dangLam) for (PhieuTiepNhan p : theoThaoTac.values()) {
            if (tk.getTenDangNhap().equals(phanCongKiemTra.get(p.getMaPhieu()))) ketQua.add(p);
        }
        return ketQua;
    }

    private boolean cungNoiDung(PhieuTiepNhan a, PhieuTiepNhan b) {
        return a.getMaXe().equals(b.getMaXe()) && a.getMaKH().equals(b.getMaKH())
                && a.getTenKhach().equals(b.getTenKhach()) && a.getSoDienThoai().equals(b.getSoDienThoai())
                && a.getBienSo().equals(b.getBienSo()) && a.getTenXe().equals(b.getTenXe())
                && a.getSoKm() == b.getSoKm() && a.getThoiGianNhan().equals(b.getThoiGianNhan())
                && a.getTinhTrang().equals(b.getTinhTrang()) && a.getTaiSan().equals(b.getTaiSan())
                && a.getYeuCau().equals(b.getYeuCau()) && a.getNguoiTiepNhan().equals(b.getNguoiTiepNhan());
    }

    private void kiemTraQuyen(TaiKhoan tk) {
        if (Objects.requireNonNull(tk, "Cần phiên đăng nhập.").getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ nhân viên tiếp nhận được truy cập kho phiếu.");
        }
    }
}
