package dao;

import dao.KhachHangDAO;
import dao.XeDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.KhachHang;
import entity.Xe;
import dao.NhanVienDAO.KyThuatVienPhanCong;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuTiepNhan;
import entity.PhieuTiepNhan.TrangThaiTiepNhan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Kho phiếu trong phiên. */
public final class PhieuTiepNhanDAO {
    private final KhachHangDAO khachHangDAO;
    private final XeDAO xeDAO;
    /** Mỗi mã thao tác chỉ ghi được một nội dung xác nhận. */
    private final List<PhieuTiepNhan> danhSachPhieu = new ArrayList<>();
    private final List<String[]> theoThaoTac = new ArrayList<>();
    private int soThuTu = 1;
    private final List<String[]> phanCongKiemTra = new ArrayList<>();
    private final NhanVienDAO nguonKyThuat;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuTiepNhanDAO(KhachHangDAO khachHangDAO, XeDAO xeDAO) {
        this(khachHangDAO, xeDAO, new NhanVienDAO());
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuTiepNhanDAO(KhachHangDAO khachHangDAO, XeDAO xeDAO, NhanVienDAO nguonKyThuat) {
        if (khachHangDAO == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.khachHangDAO = khachHangDAO;
        if (xeDAO == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.xeDAO = xeDAO;
        if (nguonKyThuat == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nguonKyThuat = nguonKyThuat;
    }

    /** Ghi một phiếu sau mọi kiểm tra. */
    public PhieuTiepNhan luu(TaiKhoan tk, String maThaoTac, Xe xeDoiChieu, KhachHang khachDoiChieu, long km,
            LocalDateTime ngayNhan, String tinhTrang, String taiSan, String yeuCau, boolean khachXacNhan) {
        kiemTraQuyen(tk);
        if (maThaoTac == null || maThaoTac.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thao tác tiếp nhận.");
        }
        if (!khachXacNhan) {
            throw new IllegalArgumentException("Cần khách xác nhận nội dung trước khi lưu.");
        }
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
        PhieuTiepNhan cu = timTheoLuot(maThaoTac);
        String ma = cu == null ? String.format(Locale.ROOT, "TN-%04d-%04d", ngayNhan.getYear(), soThuTu)
                : cu.getMaPhieu();
        PhieuTiepNhan moi = new PhieuTiepNhan(ma, xe, kh, km, ngayNhan, tinhTrang, taiSan, yeuCau, tk.getTenDangNhap(),
                LocalDateTime.now());
        if (cu != null) {
            if (!cungNoiDung(cu, moi)) {
                throw new IllegalArgumentException("Mã thao tác đã được lưu với nội dung khác.");
            }
            return cu;
        }
        // Không thay đổi kho hay số thứ tự trước khi toàn bộ dữ liệu đã hợp lệ.
        danhSachPhieu.add(moi);
        theoThaoTac.add(new String[] { maThaoTac, moi.getMaPhieu() });
        soThuTu++;
        return moi;
    }

    /** Thực hiện tim kiem cho thành phần hiện tại. */
    public List<PhieuTiepNhan> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiTiepNhan trangThai) {
        kiemTraQuyen(tk);
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String tu = tuKhoa.trim().toLowerCase();
        String bien = Xe.khoaBienSo(tu);
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        for (PhieuTiepNhan phieu : danhSachPhieu) {
            String chu = phieu.getMaPhieu() + " " + phieu.getBienSo() + " " + phieu.getTenKhach() + " "
                    + phieu.getSoDienThoai();
            if ((trangThai == null || phieu.getTrangThai() == trangThai) && (chu.toLowerCase().contains(tu)
                    || (!bien.isEmpty() && Xe.chuanHoaBienSo(phieu.getBienSo()).contains(bien)))) {
                ketQua.add(phieu);
            }
        }
        return ketQua;
    }

    /** Thực hiện tim theo ma cho thành phần hiện tại. */
    public PhieuTiepNhan timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        for (PhieuTiepNhan phieu : danhSachPhieu) {
            if (phieu.getMaPhieu().equals(ma)) {
                return phieu;
            }
        }
        throw new IllegalArgumentException("Phiếu tiếp nhận không tồn tại.");
    }

    /** Thực hiện lich su theo xe cho thành phần hiện tại. */
    public List<PhieuTiepNhan> lichSuTheoXe(TaiKhoan tk, String maXe) {
        kiemTraQuyen(tk);
        xeDAO.timTheoMa(tk, maXe);
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        for (PhieuTiepNhan phieu : danhSachPhieu) {
            if (phieu.getMaXe().equals(maXe)) {
                ketQua.add(phieu);
            }
        }
        return ketQua;
    }

    /** Đọc ky thuat phan cong từ dữ liệu hiện tại. */
    public List<KyThuatVienPhanCong> layKyThuatPhanCong(TaiKhoan tk) {
        kiemTraQuyen(tk);
        List<KyThuatVienPhanCong> ketQua = new ArrayList<>();
        for (KyThuatVienPhanCong kt : nguonKyThuat.layLienKetKyThuat(new TaiKhoanDAO())) {
            if (kt.duocPhanCong()) {
                ketQua.add(kt);
            }
        }
        return ketQua;
    }

    /** Giao kiểm tra một lần, không đổi phiếu đã xác nhận để giữ nguyên snapshot và liên kết báo cáo. */
    public void giaoKiemTra(TaiKhoan tk, PhieuTiepNhan daDoc, String taiKhoanKyThuat) {
        kiemTraQuyen(tk);
        if (daDoc == null || timTheoMa(tk, daDoc.getMaPhieu()) != daDoc) {
            throw new IllegalArgumentException("Cần phiếu tiếp nhận đã lưu.");
        }
        boolean hopLe = false;
        for (KyThuatVienPhanCong kt : layKyThuatPhanCong(tk)) {
            if (kt.getTaiKhoan().getTenDangNhap().equals(taiKhoanKyThuat)) {
                hopLe = true;
            }
        }
        if (!hopLe) {
            throw new IllegalArgumentException(
                    "Kỹ thuật viên không còn hoạt động hoặc không có liên kết tài khoản hợp lệ.");
        }
        String cu = docPhanCong(daDoc.getMaPhieu())[1];
        if (cu != null) {
            if (!cu.equals(taiKhoanKyThuat)) {
                throw new IllegalStateException("Phiếu đã giao kiểm tra. Không tự đổi người kiểm tra.");
            }

            return;
        }
        String[] phanCong = { daDoc.getMaPhieu(), taiKhoanKyThuat, tk.getTenDangNhap(),
                LocalDateTime.now().toString() };
        boolean daCo = false;
        for (int i = 0; i < phanCongKiemTra.size(); i++) {
            if (phanCongKiemTra.get(i)[0].equals(daDoc.getMaPhieu())) {
                phanCongKiemTra.set(i, phanCong);
                daCo = true;
                break;
            }
        }
        if (!daCo) {
            phanCongKiemTra.add(phanCong);
        }
    }

    /** Đọc phan cong kiem tra từ dữ liệu hiện tại. */
    public String docPhanCongKiemTra(TaiKhoan tk, String ma) {
        timTheoMa(tk, ma);
        String nguoi = docPhanCong(ma)[1];
        return nguoi == null ? "Chưa phân công" : nguoi + " · " + docPhanCong(ma)[2] + " · " + docPhanCong(ma)[3];
    }

    /** Đọc phieu kiem tra từ dữ liệu hiện tại. */
    public List<PhieuTiepNhan> layPhieuKiemTra(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chỉ kỹ thuật được đọc phiếu kiểm tra đã giao.");
        }
        tk.kiemTraPhien();
        boolean dangLam = false;
        for (KyThuatVienPhanCong kt : nguonKyThuat.layLienKetKyThuat(new TaiKhoanDAO())) {
            if (kt.duocPhanCong() && kt.getTaiKhoan().getTenDangNhap().equals(tk.getTenDangNhap())) {
                dangLam = true;
            }
        }
        List<PhieuTiepNhan> ketQua = new ArrayList<>();
        if (dangLam) {
            for (PhieuTiepNhan p : danhSachPhieu) {
                if (tk.getTenDangNhap().equals(docPhanCong(p.getMaPhieu())[1])) {
                    ketQua.add(p);
                }
            }
        }
        return ketQua;
    }

    /** Thực hiện cung noi dung cho thành phần hiện tại. */
    private boolean cungNoiDung(PhieuTiepNhan a, PhieuTiepNhan b) {
        return a.getMaXe().equals(b.getMaXe()) && a.getMaKH().equals(b.getMaKH())
                && a.getTenKhach().equals(b.getTenKhach()) && a.getSoDienThoai().equals(b.getSoDienThoai())
                && a.getBienSo().equals(b.getBienSo()) && a.getTenXe().equals(b.getTenXe())
                && a.getSoKm() == b.getSoKm() && a.getThoiGianNhan().equals(b.getThoiGianNhan())
                && a.getTinhTrang().equals(b.getTinhTrang()) && a.getTaiSan().equals(b.getTaiSan())
                && a.getYeuCau().equals(b.getYeuCau()) && a.getNguoiTiepNhan().equals(b.getNguoiTiepNhan());
    }

    /** Kiểm tra quyen trước khi xử lý. */
    private void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null) {
            throw new NullPointerException("Cần phiên đăng nhập.");
        }
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ nhân viên tiếp nhận được truy cập kho phiếu.");
        }
    }

    /** Tìm phiếu của thao tác đã lưu; không tạo thêm phiếu khi bấm lặp. */
    private PhieuTiepNhan timTheoLuot(String maThaoTac) {
        for (String[] luot : theoThaoTac) {
            if (luot[0].equals(maThaoTac)) {
                for (PhieuTiepNhan phieu : danhSachPhieu) {
                    if (phieu.getMaPhieu().equals(luot[1])) {
                        return phieu;
                    }
                }
            }
        }
        return null;
    }

    /** Đọc thông tin phân công gồm mã phiếu, kỹ thuật viên, người giao và thời điểm. */
    private String[] docPhanCong(String maPhieu) {
        for (String[] dong : phanCongKiemTra) {
            if (dong[0].equals(maPhieu)) {
                return dong;
            }
        }
        return new String[] { maPhieu, null, null, null };
    }

    /** Đọc bản sao danh sách nội bộ cho DAO báo cáo, không mở quyền ghi. */
    public List<PhieuTiepNhan> layPhieuBaoCao() {
        return new ArrayList<>(danhSachPhieu);
    }
}
