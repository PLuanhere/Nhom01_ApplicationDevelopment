package dao;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import entity.NhanVien;
import entity.NhatKyTaiKhoan;
import entity.TaiKhoan;
import entity.VaiTro;

/** Quản trị và xác thực trên cùng nguồn dữ liệu trong bộ nhớ, chưa kết nối database. */
public final class TaiKhoanDAO {

    private static final int SO_VONG_BAM = 100_000;

    private static final List<DuLieuTaiKhoan> DANH_SACH = taoDuLieuBanDau();
    private static final List<NhatKyTaiKhoan> NHAT_KY = new ArrayList<>();
    private static final PropertyChangeSupport THAY_DOI =
            new PropertyChangeSupport(TaiKhoanDAO.class);

    /** Salt và hash chỉ nằm trong DAO, không trả sang UI hoặc nhật ký. */
    private static final class DuLieuTaiKhoan {

        private TaiKhoan thongTin;
        private byte[] salt;
        private byte[] hash;

        private DuLieuTaiKhoan(TaiKhoan thongTin, byte[] salt, byte[] hash) {
            this.thongTin = thongTin;
            this.salt = salt;
            this.hash = hash;
        }
    }

    /** DAO nhân sự đọc thông tin liên kết; không cung cấp salt hoặc hash mật khẩu. */
    List<TaiKhoan> layTaiKhoanNoiBo() {
        synchronized (DANH_SACH) {
            List<TaiKhoan> ketQua = new ArrayList<>();

            for (DuLieuTaiKhoan duLieu : DANH_SACH) {
                ketQua.add(duLieu.thongTin);
            }

            return List.copyOf(ketQua);
        }
    }

    /**
     * @param tenDangNhap tên đăng nhập, bỏ khoảng trắng hai đầu
     * @param matKhau mật khẩu nguyên bản; không sửa mảng đầu vào
     * @return danh tính nếu mật khẩu đúng và tài khoản đang hoạt động
     */
    public Optional<TaiKhoan> dangNhap(String tenDangNhap, char[] matKhau) {
        Objects.requireNonNull(tenDangNhap);
        Objects.requireNonNull(matKhau);

        synchronized (DANH_SACH) {
            for (DuLieuTaiKhoan duLieu : DANH_SACH) {
                TaiKhoan tk = duLieu.thongTin;

                if (tk.isHoatDong()
                        && tk.getTenDangNhap().equals(tenDangNhap.trim())
                        && matKhau.length > 0) {
                    byte[] hash = bamMatKhau(matKhau, duLieu.salt);

                    try {
                        return MessageDigest.isEqual(hash, duLieu.hash)
                                ? Optional.of(tk)
                                : Optional.empty();
                    } finally {
                        Arrays.fill(hash, (byte) 0);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * @param nguoiDung Quản lý đã đăng nhập
     * @return danh sách chỉ đọc, không có mật khẩu
     */
    public List<TaiKhoan> layDanhSach(TaiKhoan nguoiDung) {
        synchronized (DANH_SACH) {
            kiemTraQuyen(nguoiDung);
            List<TaiKhoan> ketQua = new ArrayList<>();

            for (DuLieuTaiKhoan duLieu : DANH_SACH) {
                ketQua.add(duLieu.thongTin);
            }

            return List.copyOf(ketQua);
        }
    }

    /**
     * @param nguoiDung Quản lý đã đăng nhập
     * @param tuKhoa mã TK, tên đăng nhập, mã/tên nhân viên
     * @param hoatDong null để lấy mọi trạng thái
     * @return danh sách phù hợp
     */
    public List<TaiKhoan> timKiem(TaiKhoan nguoiDung, String tuKhoa, Boolean hoatDong) {
        return timKiem(nguoiDung, tuKhoa, hoatDong, null);
    }

    /**
     * Kết hợp từ khóa, trạng thái và vai trò; không thay đổi thứ tự nguồn dữ liệu.
     *
     * @param nguoiDung Quản lý đã đăng nhập
     * @param tuKhoa mã TK, tên đăng nhập hoặc nhân viên
     * @param hoatDong null để lấy mọi trạng thái
     * @param vaiTro null để lấy mọi vai trò
     * @return các tài khoản khớp đồng thời các điều kiện
     */
    public List<TaiKhoan> timKiem(TaiKhoan nguoiDung, String tuKhoa,
            Boolean hoatDong, VaiTro vaiTro) {
        String chu = boDau(Objects.requireNonNull(tuKhoa).trim());
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

            if ((hoatDong == null || tk.isHoatDong() == hoatDong)
                    && (vaiTro == null || tk.getVaiTro() == vaiTro)
                    && boDau(noiDung).contains(chu)) {
                ketQua.add(tk);
            }
        }

        return ketQua;
    }

    /**
     * @param nguoiDung Quản lý
     * @param ma mã tài khoản
     * @return hồ sơ hiện tại
     */
    public TaiKhoan timTheoMa(TaiKhoan nguoiDung, String ma) {
        synchronized (DANH_SACH) {
            kiemTraQuyen(nguoiDung);
            return timDuLieu(ma).thongTin;
        }
    }

    /**
     * @param nguoiDung Quản lý
     * @return mã dự kiến, chưa chiếm mã hoặc lưu dữ liệu
     */
    public String maTiepTheo(TaiKhoan nguoiDung) {
        int lonNhat = 0;

        for (TaiKhoan tk : layDanhSach(nguoiDung)) {
            lonNhat = Math.max(
                    lonNhat,
                    Integer.parseInt(tk.getMaTaiKhoan().substring(2)));
        }

        return String.format(Locale.ROOT, "TK%03d", lonNhat + 1);
    }

    /**
     * @param nguoiDung Quản lý
     * @return nhân viên chưa được cấp tài khoản
     */
    public List<NhanVien> nhanVienChuaCoTaiKhoan(TaiKhoan nguoiDung) {
        List<NhanVien> ketQua =
                new ArrayList<>(new NhanVienDAO().layDanhSach(nguoiDung));

        for (TaiKhoan tk : layDanhSach(nguoiDung)) {
            ketQua.removeIf(nv -> nv.getMaNhanVien().equals(tk.getMaNhanVien()));
        }

        return ketQua;
    }

    /**
     * Kiểm tra nhân viên, tên duy nhất, mã và mật khẩu trước khi cấp tài khoản.
     *
     * @param nguoiDung Quản lý thực hiện
     * @param thongTin dữ liệu form; ngày tạo được xác định khi lưu
     * @param matKhau mật khẩu đã xác nhận trên UI
     * @return tài khoản vừa cấp
     */
    public TaiKhoan them(TaiKhoan nguoiDung, TaiKhoan thongTin, char[] matKhau) {
        synchronized (DANH_SACH) {
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

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            DANH_SACH.add(new DuLieuTaiKhoan(tk, salt, bamMatKhau(matKhau, salt)));

            ghiNhatKy(
                    nguoiDung,
                    tk,
                    "Cấp tài khoản · " + tk.getVaiTro().getTenHienThi(),
                    "Cấp cho nhân viên");

            return tk;
        }
    }

    /**
     * Đổi vai trò/trạng thái; bảo vệ tài khoản đang đăng nhập và Quản lý cuối cùng.
     *
     * @param nguoiDung Quản lý thực hiện
     * @param banDau hồ sơ đã mở, dùng phát hiện thay đổi đồng thời
     * @param vaiTro vai trò mới
     * @param hoatDong trạng thái mới
     * @param lyDo lý do bắt buộc để lưu nhật ký
     */
    public void capNhat(TaiKhoan nguoiDung, TaiKhoan banDau,
            VaiTro vaiTro, boolean hoatDong, String lyDo) {
        synchronized (DANH_SACH) {
            kiemTraQuyen(nguoiDung);
            Objects.requireNonNull(vaiTro);

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
    }

    /**
     * Đổi hash, giữ nguyên trạng thái và thu hồi mọi phiên đăng nhập cũ.
     *
     * @param nguoiDung Quản lý thực hiện
     * @param banDau tài khoản đích đang xem
     * @param matKhau mật khẩu mới
     * @param lyDo lý do bắt buộc, không chứa mật khẩu
     */
    public void datLaiMatKhau(TaiKhoan nguoiDung, TaiKhoan banDau,
            char[] matKhau, String lyDo) {
        synchronized (DANH_SACH) {
            kiemTraQuyen(nguoiDung);
            TaiKhoan.kiemTraMatKhauMoi(matKhau);

            String lyDoHopLe = kiemTraLyDo(lyDo);
            DuLieuTaiKhoan duLieu = kiemTraBanHienTai(banDau);

            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = bamMatKhau(matKhau, salt);

            TaiKhoan moi = new TaiKhoan(
                    banDau.getMaTaiKhoan(),
                    banDau.getMaNhanVien(),
                    banDau.getTenDangNhap(),
                    banDau.getVaiTro(),
                    banDau.isHoatDong(),
                    banDau.getNgayTao());

            Arrays.fill(duLieu.hash, (byte) 0);

            duLieu.salt = salt;
            duLieu.hash = hash;
            duLieu.thongTin = moi;

            ghiNhatKy(nguoiDung, moi, "Đặt lại mật khẩu", lyDoHopLe);
            thuHoiPhien(banDau, moi);
        }
    }

    /**
     * @param nguoiDung Quản lý
     * @return bản chụp nhật ký chỉ đọc
     */
    public List<NhatKyTaiKhoan> layNhatKy(TaiKhoan nguoiDung) {
        synchronized (DANH_SACH) {
            kiemTraQuyen(nguoiDung);
            return List.copyOf(NHAT_KY);
        }
    }

    /** @param listener khung chính theo dõi để đóng phiên bị thu hồi */
    public void themTheoDoi(PropertyChangeListener listener) {
        THAY_DOI.addPropertyChangeListener(listener);
    }

    /** @param listener khung đã đóng, cần gỡ tham chiếu */
    public void boTheoDoi(PropertyChangeListener listener) {
        THAY_DOI.removePropertyChangeListener(listener);
    }

    /**
     * @param tk tài khoản
     * @return tên trạng thái hiển thị
     */
    public static String tenTrangThai(TaiKhoan tk) {
        return tk.isHoatDong() ? "Đang hoạt động" : "Đã khóa";
    }

    /** Chỉ nhận danh tính Quản lý do đăng nhập trả về, còn hiệu lực trong nguồn hiện tại. */
    private static void kiemTraQuyen(TaiKhoan nguoiDung) {
        Objects.requireNonNull(nguoiDung).kiemTraPhien();

        if (nguoiDung.getVaiTro() != VaiTro.QUAN_LY
                || nguoiDung.getMaTaiKhoan().isEmpty()) {
            throw new SecurityException("Chỉ Quản lý được quản trị tài khoản.");
        }

        if (timDuLieu(nguoiDung.getMaTaiKhoan()).thongTin != nguoiDung) {
            throw new SecurityException("Cần tài khoản Quản lý đã đăng nhập.");
        }
    }

    /** Chỉ dùng nội bộ sau khi kiểm tra quyền; không trả dữ liệu bí mật sang UI. */
    private static DuLieuTaiKhoan timDuLieu(String ma) {
        for (DuLieuTaiKhoan duLieu : DANH_SACH) {
            if (duLieu.thongTin.getMaTaiKhoan().equals(ma)) {
                return duLieu;
            }
        }

        throw new IllegalArgumentException("Không tìm thấy tài khoản.");
    }

    /** Không ghi đè một hồ sơ đã được thay đổi sau khi form được mở. */
    private static DuLieuTaiKhoan kiemTraBanHienTai(TaiKhoan banDau) {
        DuLieuTaiKhoan duLieu =
                timDuLieu(Objects.requireNonNull(banDau).getMaTaiKhoan());

        if (duLieu.thongTin != banDau) {
            throw new IllegalArgumentException(
                    "Tài khoản đã thay đổi. Hãy quay lại danh sách và mở lại.");
        }

        return duLieu;
    }

    /** @return số tài khoản Quản lý còn quyền truy cập */
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

    /** @return lý do không rỗng, bỏ khoảng trắng hai đầu */
    private static String kiemTraLyDo(String lyDo) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do thay đổi.");
        }

        return lyDo.trim();
    }

    /** Ghi đúng người thực hiện, thời điểm và nội dung; không nhận mật khẩu. */
    private static void ghiNhatKy(TaiKhoan nguoiDung, TaiKhoan dich,
            String hanhDong, String lyDo) {
        NHAT_KY.add(new NhatKyTaiKhoan(
                LocalDateTime.now(),
                nguoiDung.getTenDangNhap(),
                dich.getMaTaiKhoan(),
                hanhDong,
                lyDo));
    }

    /** Chặn danh tính cũ tại DAO trước khi yêu cầu khung chính đóng phiên. */
    private static void thuHoiPhien(TaiKhoan cu, TaiKhoan moi) {
        cu.ketThucPhien();
        THAY_DOI.firePropertyChange("taiKhoan", cu, moi);
    }

    /** @return chữ thường không dấu để tìm kiếm */
    private static String boDau(String chu) {
        return Normalizer.normalize(
                chu.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
    }

    /** Băm bằng thư viện JDK và xóa bản sao mật khẩu trong tham số thuật toán. */
    private static byte[] bamMatKhau(char[] matKhau, byte[] salt) {
        PBEKeySpec thongSo = new PBEKeySpec(
                matKhau, salt, SO_VONG_BAM, 256);

        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(thongSo)
                    .getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(
                    "Không thể xử lý mật khẩu. Vui lòng thử lại.", ex);
        } finally {
            thongSo.clearPassword();
        }
    }

    /** Bốn tài khoản cũ giữ mật khẩu 123 và liên kết hồ sơ nhân viên tương ứng. */
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

            ketQua.add(new DuLieuTaiKhoan(
                    tk,
                    Base64.getDecoder().decode("WxicU03syFGfMza7C+tuKA=="),
                    Base64.getDecoder().decode(
                            "HDg+XvXmvAPJjeZgQLmrPozADDpa/2XOT/PsBNcN+vo=")));
        }

        return ketQua;
    }
}