package dao;

import java.time.LocalDateTime;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import entity.NhanVien;
import entity.NhatKyTaiKhoan;
import entity.TaiKhoan;
import entity.VaiTro;

/** Quản lý tài khoản thử nghiệm, xác thực, phân quyền và thu hồi phiên. */
public final class TaiKhoanDAO {

    private static final List<DuLieuTaiKhoan> DANH_SACH = taoDuLieuBanDau();
    private static final List<NhatKyTaiKhoan> NHAT_KY = new ArrayList<>();
    private static final List<ActionListener> THEO_DOI = new ArrayList<>();

    private static final class DuLieuTaiKhoan {
        private TaiKhoan thongTin;
        private String matKhau;

        /** Gắn thông tin tài khoản với mật khẩu thử nghiệm lưu trong DAO. */
        private DuLieuTaiKhoan(TaiKhoan thongTin, String matKhau) {
            this.thongTin = thongTin;
            this.matKhau = matKhau;
        }
    }

    /** Đọc thông tin tài khoản để liên kết hồ sơ nhân viên, không trả mật khẩu. */
    List<TaiKhoan> layTaiKhoanNoiBo() {
        List<TaiKhoan> ketQua = new ArrayList<>();

        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            ketQua.add(duLieu.thongTin);
        }

        return ketQua;
    }

    /** Kiểm tra tên đăng nhập và mật khẩu; trả tài khoản hợp lệ hoặc null. */
    public TaiKhoan dangNhap(String tenDangNhap, char[] matKhau) {
        if (tenDangNhap == null || matKhau == null) {
            throw new IllegalArgumentException("Cần nhập tên đăng nhập và mật khẩu.");
        }
        String matKhauNhap = new String(matKhau);
        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            TaiKhoan tk = duLieu.thongTin;
            if (tk.isHoatDong() && tk.getTenDangNhap().equals(tenDangNhap.trim())
                && duLieu.matKhau.equals(matKhauNhap)) { return tk; }
        }
        return null;
    }

    /** Đọc danh sách tài khoản sau khi kiểm tra quyền truy cập. */
    public List<TaiKhoan> layDanhSach(TaiKhoan nguoiDung) {
        kiemTraQuyen(nguoiDung);
        List<TaiKhoan> ketQua = new ArrayList<>();

        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            ketQua.add(duLieu.thongTin);
        }

        return ketQua;
    }

    /** Tìm và lọc tài khoản theo các điều kiện được truyền vào. */
    public List<TaiKhoan> timKiem(TaiKhoan nguoiDung, String tuKhoa, int hoatDong) {
        return timKiem(nguoiDung, tuKhoa, hoatDong, null);
    }

    /** Tìm và lọc tài khoản theo các điều kiện được truyền vào. */
    public List<TaiKhoan> timKiem(TaiKhoan nguoiDung, String tuKhoa,
        int hoatDong, VaiTro vaiTro) {
        String chu = boDau(tuKhoa == null ? "" : tuKhoa.trim());
        List<NhanVien> nhanVien = new NhanVienDAO().layDanhSach(nguoiDung);
        List<TaiKhoan> ketQua = new ArrayList<>();

        for (TaiKhoan tk : layDanhSach(nguoiDung)) {
            String ten = "";

            for (NhanVien nv : nhanVien) {
                if (nv.getMaNhanVien().equals(tk.getMaNhanVien())) {
                    ten = nv.getHoTen();
                    break;
                }
            }

            String noiDung = tk.getMaTaiKhoan() + " "
                + tk.getTenDangNhap() + " "
                + tk.getMaNhanVien() + " " + ten;

            if ((hoatDong == -1 || tk.isHoatDong() == (hoatDong == 1))
                && (vaiTro == null || tk.getVaiTro() == vaiTro)
                && boDau(noiDung).contains(chu)) {
                ketQua.add(tk);
            }
        }

        return ketQua;
    }

    /** Tìm tài khoản theo mã; báo lỗi nếu không tồn tại. */
    public TaiKhoan timTheoMa(TaiKhoan nguoiDung, String ma) {
        kiemTraQuyen(nguoiDung);
        return timDuLieu(ma).thongTin;
    }

    /** Sinh mã tài khoản dự kiến để hiển thị, chưa lưu hoặc chiếm mã. */
    public String maTiepTheo(TaiKhoan nguoiDung) {
        int lonNhat = 0;

        for (TaiKhoan tk : layDanhSach(nguoiDung)) {
            lonNhat = Math.max(
                lonNhat,
                Integer.parseInt(tk.getMaTaiKhoan().substring(2)));
        }

        return String.format("TK%03d", lonNhat + 1);
    }

    /** Lấy các nhân viên chưa được liên kết với tài khoản nào. */
    public List<NhanVien> nhanVienChuaCoTaiKhoan(TaiKhoan nguoiDung) {
        List<NhanVien> ketQua = new ArrayList<>();
        List<TaiKhoan> cacTaiKhoan = layDanhSach(nguoiDung);
        for (NhanVien nv : new NhanVienDAO().layDanhSach(nguoiDung)) {
            boolean daCo = false;
            for (TaiKhoan tk : cacTaiKhoan) {
                if (nv.getMaNhanVien().equals(tk.getMaNhanVien())) { daCo = true; break; }
            }
            if (!daCo) { ketQua.add(nv); }
        }
        return ketQua;
    }

    /** Kiểm tra quyền và dữ liệu trước khi thêm tài khoản vào nguồn. */
    public TaiKhoan them(TaiKhoan nguoiDung, TaiKhoan thongTin, char[] matKhau) {
        kiemTraQuyen(nguoiDung);
        TaiKhoan.kiemTraMatKhauMoi(matKhau);
        new NhanVienDAO().timTheoMa(nguoiDung, thongTin.getMaNhanVien());

        if (!maTiepTheo(nguoiDung).equals(thongTin.getMaTaiKhoan())) {
            throw new IllegalArgumentException(
                "Mã tài khoản đã thay đổi. Hãy mở lại form cấp tài khoản.");
        }

        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            if (duLieu.thongTin.getTenDangNhap().equals(thongTin.getTenDangNhap())) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
            }

            if (duLieu.thongTin.getMaNhanVien().equals(thongTin.getMaNhanVien())) {
                throw new IllegalArgumentException("Nhân viên này đã có tài khoản.");
            }
        }

        TaiKhoan tk = new TaiKhoan(
            thongTin.getMaTaiKhoan(),
            thongTin.getMaNhanVien(),
            thongTin.getTenDangNhap(),
            thongTin.getVaiTro(),
            thongTin.isHoatDong(),
            LocalDateTime.now());

        DANH_SACH.add(new DuLieuTaiKhoan(tk, new String(matKhau)));

        ghiNhatKy(
            nguoiDung,
            tk,
            "Cấp tài khoản · " + tk.getVaiTro().getTenHienThi(),
            "Cấp cho nhân viên");

        return tk;
    }

    /** Kiểm tra dữ liệu rồi cập nhật tài khoản trong nguồn. */
    public void capNhat(TaiKhoan nguoiDung, TaiKhoan banDau,
        VaiTro vaiTro, boolean hoatDong, String lyDo) {
        kiemTraQuyen(nguoiDung);
        if (vaiTro == null) { throw new IllegalArgumentException("Thiếu vaiTro."); }

        String lyDoHopLe = kiemTraLyDo(lyDo);
        DuLieuTaiKhoan duLieu = kiemTraBanHienTai(banDau);

        if (banDau.getVaiTro() == vaiTro && banDau.isHoatDong() == hoatDong) {
            throw new IllegalArgumentException("Vai trò và trạng thái chưa thay đổi.");
        }

        if (nguoiDung.getMaTaiKhoan().equals(banDau.getMaTaiKhoan())
            && (!hoatDong || vaiTro != VaiTro.QUAN_LY)) {
            throw new IllegalArgumentException(
                "Không được tự khóa hoặc tự hạ quyền tài khoản đang đăng nhập.");
        }

        if (banDau.isHoatDong()
            && banDau.getVaiTro() == VaiTro.QUAN_LY
            && (!hoatDong || vaiTro != VaiTro.QUAN_LY)
            && demQuanLyHoatDong() <= 1) {
            throw new IllegalArgumentException(
                "Phải giữ ít nhất một tài khoản Quản lý hoạt động.");
        }

        TaiKhoan moi = new TaiKhoan(
            banDau.getMaTaiKhoan(),
            banDau.getMaNhanVien(),
            banDau.getTenDangNhap(),
            vaiTro,
            hoatDong,
            banDau.getNgayTao());

        duLieu.thongTin = moi;

        ghiNhatKy(
            nguoiDung,
            moi,
            banDau.getVaiTro().getTenHienThi() + " → "
            + vaiTro.getTenHienThi() + "; "
            + tenTrangThai(banDau) + " → " + tenTrangThai(moi),
            lyDoHopLe);

        thuHoiPhien(banDau, moi);
    }

    /** Kiểm tra quyền, lưu mật khẩu mới, ghi nhật ký và thu hồi phiên cũ. */
    public void datLaiMatKhau(TaiKhoan nguoiDung, TaiKhoan banDau,
        char[] matKhau, String lyDo) {
        kiemTraQuyen(nguoiDung);
        TaiKhoan.kiemTraMatKhauMoi(matKhau);

        String lyDoHopLe = kiemTraLyDo(lyDo);
        DuLieuTaiKhoan duLieu = kiemTraBanHienTai(banDau);

        TaiKhoan moi = new TaiKhoan(
            banDau.getMaTaiKhoan(),
            banDau.getMaNhanVien(),
            banDau.getTenDangNhap(),
            banDau.getVaiTro(),
            banDau.isHoatDong(),
            banDau.getNgayTao());

        duLieu.matKhau = new String(matKhau);
        duLieu.thongTin = moi;

        ghiNhatKy(nguoiDung, moi, "Đặt lại mật khẩu", lyDoHopLe);
        thuHoiPhien(banDau, moi);
    }

    /** Đọc lịch sử thay đổi theo quyền truy cập và các điều kiện lọc. */
    public List<NhatKyTaiKhoan> layNhatKy(TaiKhoan nguoiDung) {
        kiemTraQuyen(nguoiDung);
        return new ArrayList<>(NHAT_KY);
    }

    /** Đăng ký khung ứng dụng cần nhận thông báo thu hồi phiên. */
    public void themTheoDoi(ActionListener listener) {
        THEO_DOI.add(listener);
    }

    /** Gỡ khung đã đóng khỏi danh sách nhận thông báo tài khoản. */
    public void boTheoDoi(ActionListener listener) {
        THEO_DOI.remove(listener);
    }

    /** Lấy nhãn hoạt động hoặc đã khóa của tài khoản. */
    public static String tenTrangThai(TaiKhoan tk) {
        return tk.isHoatDong() ? "Đang hoạt động" : "Đã khóa";
    }

    /** Kiểm tra tài khoản, phiên đăng nhập và quyền Quản lý trước thao tác DAO. */
    private static void kiemTraQuyen(TaiKhoan nguoiDung) {
        if (nguoiDung == null) { throw new SecurityException("Cần đăng nhập."); }
        nguoiDung.kiemTraPhien();

        if (nguoiDung.getVaiTro() != VaiTro.QUAN_LY
            || nguoiDung.getMaTaiKhoan().isEmpty()) {
            throw new SecurityException("Chỉ Quản lý được quản trị tài khoản.");
        }

        if (timDuLieu(nguoiDung.getMaTaiKhoan()).thongTin != nguoiDung) {
            throw new SecurityException("Cần tài khoản Quản lý đã đăng nhập.");
        }
    }

    /** Tìm dữ liệu nội bộ của tài khoản theo mã; báo lỗi nếu không có. */
    private static DuLieuTaiKhoan timDuLieu(String ma) {
        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            if (duLieu.thongTin.getMaTaiKhoan().equals(ma)) {
                return duLieu;
            }
        }

        throw new IllegalArgumentException("Không tìm thấy tài khoản.");
    }

    /** Kiểm tra tài khoản mở trên form vẫn là bản ghi hiện tại trước khi sửa. */
    private static DuLieuTaiKhoan kiemTraBanHienTai(TaiKhoan banDau) {
        if (banDau == null) { throw new IllegalArgumentException("Thiếu tài khoản ban đầu."); }
        DuLieuTaiKhoan duLieu = timDuLieu(banDau.getMaTaiKhoan());

        if (duLieu.thongTin != banDau) {
            throw new IllegalArgumentException(
                "Tài khoản đã thay đổi. Hãy quay lại danh sách và mở lại.");
        }

        return duLieu;
    }

    /** Đếm tài khoản Quản lý đang hoạt động để bảo vệ quyền quản trị cuối cùng. */
    private static int demQuanLyHoatDong() {
        int soLuong = 0;

        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            if (duLieu.thongTin.isHoatDong()
                && duLieu.thongTin.getVaiTro() == VaiTro.QUAN_LY) {
                soLuong++;
            }
        }

        return soLuong;
    }

    /** Kiểm tra lý do thay đổi không rỗng và bỏ khoảng trắng hai đầu. */
    private static String kiemTraLyDo(String lyDo) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do thay đổi.");
        }

        return lyDo.trim();
    }

    /** Lưu người thực hiện, thời điểm, tài khoản đích và nội dung thay đổi. */
    private static void ghiNhatKy(TaiKhoan nguoiDung, TaiKhoan dich,
        String hanhDong, String lyDo) {
        NHAT_KY.add(new NhatKyTaiKhoan(
            LocalDateTime.now(),
            nguoiDung.getTenDangNhap(),
            dich.getMaTaiKhoan(),
            hanhDong,
            lyDo));
    }

    /** Hủy hiệu lực phiên cũ và thông báo các khung đang theo dõi tài khoản. */
    private static void thuHoiPhien(TaiKhoan cu, TaiKhoan moi) {
        cu.ketThucPhien();
        List<ActionListener> cacKhung = new ArrayList<>(THEO_DOI);
        for (ActionListener khung : cacKhung) {
            khung.actionPerformed(new ActionEvent(moi, ActionEvent.ACTION_PERFORMED, "taiKhoan"));
        }
    }

    /** Đổi chữ tiếng Việt thành chữ thường không dấu để tìm và sắp xếp. */
    private static String boDau(String chu) {
        String thuong = chu.toLowerCase();
        String[] cacNhom = {"àáảãạăằắẳẵặâầấẩẫậ", "èéẻẽẹêềếểễệ", "ìíỉĩị",
            "òóỏõọôồốổỗộơờớởỡợ", "ùúủũụưừứửữự", "ỳýỷỹỵ", "đ"};
        String thayThe = "aeiouyd";
        String ketQua = "";
        for (int i = 0; i < thuong.length(); i++) {
            char c = thuong.charAt(i);
            if (c >= '\u0300' && c <= '\u036F') { continue; }
            for (int j = 0; j < cacNhom.length; j++) {
                if (cacNhom[j].indexOf(c) >= 0) { c = thayThe.charAt(j); break; }
            }
            ketQua = ketQua + c;
        }
        return ketQua;
    }

    /** Tạo danh sách tài khoản ban đầu cho lần chạy thử nghiệm. */
    private static List<DuLieuTaiKhoan> taoDuLieuBanDau() {
        List<DuLieuTaiKhoan> ketQua = new ArrayList<>();

        String[] ma = {"001", "003", "005", "007"};
        String[] ten = {"quanly", "tiepnhan", "kythuat", "thungan"};

        for (int i = 0; i < ma.length; i++) {
            TaiKhoan tk = new TaiKhoan(
                "TK" + ma[i],
                "NV" + ma[i],
                ten[i],
                VaiTro.values()[i],
                true,
                LocalDateTime.of(2026, 10, 5, 8, 0));

            ketQua.add(new DuLieuTaiKhoan(tk, "123"));
        }

        return ketQua;
    }

    /** Tìm và lọc tài khoản theo các điều kiện rồi sắp xếp danh sách kết quả. */
    public List<TaiKhoan> timKiem(TaiKhoan nguoiDung, String tuKhoa, int hoatDong, VaiTro vaiTro, int sapXep) {
        List<TaiKhoan> ketQua = timKiem(nguoiDung, tuKhoa, hoatDong, vaiTro);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                TaiKhoan a = ketQua.get(i);
                TaiKhoan b = ketQua.get(j);
                int soSanh;
                if (sapXep == 0) { soSanh = a.getMaTaiKhoan().compareToIgnoreCase(b.getMaTaiKhoan()); }
                else { soSanh = boDau(a.getTenDangNhap()).compareTo(boDau(b.getTenDangNhap())); }
                if (sapXep == 2) { soSanh = -soSanh; }
                if (soSanh > 0) { ketQua.set(i, b); ketQua.set(j, a); }
            }
        }
        return ketQua;
    }
}
