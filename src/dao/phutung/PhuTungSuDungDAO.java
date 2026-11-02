package dao.phutung;

import dao.suachua.PhieuSuaChuaDAO;
import entity.baogia.ChiTietBaoGia;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import entity.phutung.LuongPhuTung;
import entity.phutung.PhuTung;
import entity.phutung.PhuTungSuDung;
import entity.phutung.TrangThaiSuDung;
import entity.suachua.HangMucSuaChua;
import entity.suachua.PhieuSuaChua;
import entity.suachua.TrangThaiSuaChua;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Quản lý phụ tùng và tồn kho mẫu trong bộ nhớ của phiên sử dụng.
public final class PhuTungSuDungDAO {
    /** Danh mục và tồn do DAO kho quản lý, dùng chung với Quản lý và báo giá. */
    private final PhuTungDAO kho;
    private final List<PhuTungSuDung> hoSo = new ArrayList<>();
    private final List<String> lenhChoPhuTung = new ArrayList<String>();
    private final List<String> luotDaGhi = new ArrayList<>();
    private int soTiepTheo = 113;

    private PhieuSuaChuaDAO nguonLenh;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhuTungSuDungDAO() {
        this(new PhuTungDAO(), true);
    }

    /** Giữ danh mục/tồn đầu kỳ, nhưng không nạp lệnh và chứng từ mẫu khi dùng luồng chung. */
    public PhuTungSuDungDAO(boolean coHoSoMau) {
        this(new PhuTungDAO(), coHoSoMau);
    }

    /** Nhận đúng nguồn kho đang dùng trong lần chạy ứng dụng. */
    public PhuTungSuDungDAO(PhuTungDAO kho, boolean coHoSoMau) {
        if (kho == null) {
            throw new IllegalArgumentException("Thiếu nguồn kho.");
        }
        this.kho = kho;
        if (!coHoSoMau) {
            soTiepTheo = 1;
            return;
        }
        hoSo.add(taoBanGhi("SD-2026-0112", "SC-2026-0111", "PT001", 2, "Chưa xác nhận xuất kho", TrangThaiSuDung.NHAP));
        hoSo.add(taoBanGhi("SD-2026-0108", "SC-2026-0108", "PT001", 4, "Đã ghi nhận thực dùng",
                TrangThaiSuDung.DA_GHI_NHAN));
    }

    /** Thực hiện noi nguon lenh cho thành phần hiện tại. */
    public void noiNguonLenh(PhieuSuaChuaDAO nguon) {
        if (nguonLenh != null) {
            throw new IllegalStateException("Nguồn lệnh đã được kết nối.");
        }
        if (nguon == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        nguonLenh = nguon;
    }

    /** Lệnh đã được duyệt vẫn có thể dùng phụ tùng sau khi danh mục ngừng hoạt động. */
    public List<PhuTung> docDanhMuc() {
        return kho.danhMucNoiBo();
    }

    /** Chỉ các phụ tùng đang hoạt động được chọn cho báo giá mới. */
    public List<PhuTung> docDanhMucDangHoatDong() {
        return kho.danhMucDangHoatDong();
    }

    /** Trả về kho dao. */
    public PhuTungDAO getKhoDAO() {
        return kho;
    }

    /** Khóa đúng dòng báo giá bằng vị trí, không suy ra phụ tùng từ tên hạng mục. */
    public PhuTungSuDung ghiTheoHangMuc(TaiKhoan tk, String maLenh, int viTri, String maPT, double so, String luot) {
        kiemTraLenh(tk, maLenh);
        PhieuSuaChua ketQuaTraCuu1 = nguonLenh.timTheoMa(tk, maLenh);
        if (ketQuaTraCuu1 == null) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu.");
        }
        PhieuSuaChua lenh = ketQuaTraCuu1;
        List<ChiTietBaoGia> chiTiet = nguonLenh.layBaoGia(tk, maLenh).getChiTiet();
        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH || viTri < 0 || viTri >= chiTiet.size()) {
            throw new IllegalStateException("Lệnh/hạng mục không thể ghi nhận thêm phụ tùng.");
        }
        HangMucSuaChua hm = lenh.getDanhSachHangMuc().get(viTri);
        if (!hm.getTaiKhoanThucHien().equals(tk.getTenDangNhap())
                && !lenh.getKyThuatChinh().equals(tk.getTenDangNhap())) {
            throw new SecurityException("Không được ghi phụ tùng cho hạng mục này.");
        }
        ChiTietBaoGia ct = chiTiet.get(viTri);
        if (!"Phụ tùng".equals(ct.getLoai()) || !ct.getMaPhuTung().equals(maPT)) {
            throw new IllegalArgumentException("Phụ tùng không thuộc dòng đã duyệt.");
        }
        docPhuTung(maPT).kiemTraSoLuong(so);
        String khoa = maLenh + "#" + viTri;
        double daDung = 0.0;
        for (PhuTungSuDung dong : hoSo) {
            if (dong.getHangMuc().equals(khoa) && dong.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                daDung = Math.round((daDung + dong.getSoLuong()) * 1000) / 1000.0;
            }
        }
        if (((Math.round((daDung + so) * 1000) / 1000.0) > ct.getSoLuong())) {
            throw new IllegalArgumentException("Lượng thực dùng vượt dòng báo giá đã duyệt.");
        }
        return ghiNoiBo(tk, maLenh, khoa, maPT, so, layLuong(docPhamViDuyet(tk, maLenh), maPT), luot);
    }

    // Ghi lượng thực dùng sau khi DAO kỹ thuật đã kiểm tra lệnh và hạng mục hiện tại.
    /** Thực hiện ghi theo hang muc cho thành phần hiện tại. */
    public PhuTungSuDung ghiTheoHangMuc(TaiKhoan taiKhoan, String maLenh, String hangMuc, String maPhuTung,
            double soLuong, double gioiHan, String maLuot) {
        if (nguonLenh != null) {
            throw new IllegalStateException("Cần chọn đúng dòng báo giá khi ghi nhận phụ tùng.");
        }
        return ghiNoiBo(taiKhoan, maLenh, hangMuc, maPhuTung, soLuong, gioiHan, maLuot);
    }

    /** Thực hiện ghi noi bo cho thành phần hiện tại. */
    private PhuTungSuDung ghiNoiBo(TaiKhoan taiKhoan, String maLenh, String hangMuc, String maPhuTung, double soLuong,
            double gioiHan, String maLuot) {

        kiemTraTaiKhoan(taiKhoan);
        if (maLuot == null || maLuot.isBlank() || luotDaGhi.contains(maLuot)) {
            throw new IllegalStateException("Lượt sử dụng đã được ghi nhận hoặc không hợp lệ.");
        }
        PhuTung phuTung = docPhuTung(maPhuTung);
        phuTung.kiemTraSoLuong(soLuong);
        double daDung = 0.0;
        for (PhuTungSuDung dong : hoSo) {
            if (maLenh.equals(dong.getMaLenhSuaChua()) && maPhuTung.equals(dong.getPhuTung().getMaPhuTung())
                    && dong.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                daDung = Math.round((daDung + dong.getSoLuong()) * 1000) / 1000.0;
            }
        }
        if ((soLuong > docTon(maPhuTung))) {
            throw new IllegalArgumentException("Số lượng thực dùng vượt tồn khả dụng.");
        }
        if (gioiHan <= 0 || ((Math.round((daDung + soLuong) * 1000) / 1000.0) > gioiHan)) {
            throw new IllegalArgumentException("Tổng lượng dùng vượt phạm vi báo giá đã được duyệt.");
        }
        String ma = taoMaMoi();
        PhuTungSuDung dong = new PhuTungSuDung(ma, maLenh, phuTung, soLuong, taiKhoan.getTenDangNhap(),
                "Ghi nhận thực dùng", TrangThaiSuDung.DA_GHI_NHAN, hangMuc);
        ghiBienDongKho(taoLuong(maPhuTung, soLuong), false, maLenh, taiKhoan, "Ghi nhận thực dùng · " + ma,
                LocalDateTime.now());
        luuHoSo(hoSo, dong);
        luotDaGhi.add(maLuot);
        return dong;

    }

    // Lọc hồ sơ theo từ khóa, trạng thái và người được phân công.
    /** Đọc danh sach từ dữ liệu hiện tại. */
    public List<PhuTungSuDung> docDanhSach(TaiKhoan taiKhoan, String tuKhoa, TrangThaiSuDung trangThai) {
        kiemTraTaiKhoan(taiKhoan);
        List<PhuTungSuDung> ketQua = new ArrayList<PhuTungSuDung>();
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String khoa = tuKhoa.trim().toLowerCase();
        for (PhuTungSuDung banGhi : hoSo) {
            String noiDung = banGhi.getMaGhiNhan() + " " + banGhi.getMaLenhSuaChua() + " " + banGhi.getPhuTung();
            boolean duocDoc = nguonLenh == null ? taiKhoan.getTenDangNhap().equals("kythuat")
                    : (nguonLenh.timTheoMa(taiKhoan, banGhi.getMaLenhSuaChua()) != null);
            if (duocDoc && noiDung.toLowerCase().contains(khoa)
                    && (trangThai == null || trangThai == banGhi.getTrangThai())) {
                ketQua.add(banGhi);
            }
        }
        return ketQua;
    }

    /** Tiếp nhận tra cứu thực dùng của lệnh để xem lịch sử; không có quyền xác nhận xuất kho. */
    public List<PhuTungSuDung> layThucDungChoTiepNhan(TaiKhoan tk, String maLenh) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ tiếp nhận được tra lịch sử tại quầy.");
        }
        if (nguonLenh == null || (nguonLenh.timTheoMa(tk, maLenh) == null)) {
            throw new IllegalArgumentException("Lệnh sửa chữa không tồn tại.");
        }
        List<PhuTungSuDung> ketQua = new ArrayList<>();
        for (PhuTungSuDung dong : hoSo) {
            if (dong.getMaLenhSuaChua().equals(maLenh) && dong.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                ketQua.add(dong);
            }
        }
        return ketQua;
    }

    // Đọc các lệnh được phân công và có phạm vi phụ tùng đã duyệt.
    /** Đọc lenh da duyet từ dữ liệu hiện tại. */
    public List<String> docLenhDaDuyet(TaiKhoan taiKhoan) {
        kiemTraTaiKhoan(taiKhoan);
        List<String> ketQua = new ArrayList<>();
        if (nguonLenh != null) {
            for (PhieuSuaChua lenh : nguonLenh.timKiem(taiKhoan, "", "Tất cả")) {
                ketQua.add(lenh.getMaPhieu());
            }
        } else if (taiKhoan.getTenDangNhap().equals("kythuat")) {
            String[] maMau = { "SC-2026-0111", "SC-2026-0113", "SC-2026-0114", "SC-2026-0108" };
            for (String ma : maMau) {
                ketQua.add(ma);
            }
        }
        return ketQua;
    }

    // Trả bản sao phạm vi duyệt để màn hình không sửa trực tiếp dữ liệu nội bộ.
    /** Đọc pham vi duyet từ dữ liệu hiện tại. */
    public List<LuongPhuTung> docPhamViDuyet(TaiKhoan taiKhoan, String maLenh) {
        kiemTraLenh(taiKhoan, maLenh);
        List<LuongPhuTung> ketQua = new ArrayList<>();
        if (nguonLenh != null) {
            for (ChiTietBaoGia dong : nguonLenh.layBaoGia(taiKhoan, maLenh).getChiTiet()) {
                if (!dong.getMaPhuTung().isEmpty()) {
                    themLuong(ketQua, dong.getMaPhuTung(), dong.getSoLuong());
                }
            }
        } else {
            ketQua.add(new LuongPhuTung("PT001", 4));
            ketQua.add(new LuongPhuTung("PT005", 1));
            if (maLenh.equals("SC-2026-0114")) {
                ketQua.add(new LuongPhuTung("PT004", 1));
            }
        }
        return ketQua;
    }

    // Tìm phụ tùng trong danh mục và báo lỗi nếu mã không tồn tại.
    /** Đọc phu tung từ dữ liệu hiện tại. */
    public PhuTung docPhuTung(String maPhuTung) {
        return new PhuTung(kho.timNoiBo(maPhuTung));
    }

    /** Tồn luôn đọc từ nguồn kho dùng chung. */
    public double docTon(String maPhuTung) {
        return kho.tonNoiBo(maPhuTung);
    }

    // Tính lượng còn được dùng sau các lần đã ghi nhận theo lệnh.
    /** Đọc luong con lai từ dữ liệu hiện tại. */
    public double docLuongConLai(TaiKhoan taiKhoan, String maLenh, String maPhuTung) {
        kiemTraLenh(taiKhoan, maLenh);
        double conLai = layLuong(docPhamViDuyet(taiKhoan, maLenh), maPhuTung);
        if (conLai <= 0) {
            throw new IllegalArgumentException("Phụ tùng chưa được khách duyệt.");
        }
        for (PhuTungSuDung banGhi : hoSo) {
            if (maLenh.equals(banGhi.getMaLenhSuaChua()) && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())
                    && banGhi.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                conLai = Math.round((conLai - banGhi.getSoLuong()) * 1000) / 1000.0;
            }
        }
        return conLai;
    }

    // Lưu nháp sau khi kiểm tra phân công, phạm vi duyệt và phiên bản đang sửa.
    /** Kiểm tra và lưu nhap. */
    public PhuTungSuDung luuNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu, String maLenh, String maPhuTung,
            double soLuong, String ghiChu) {
        if (nguonLenh != null) {
            throw new IllegalStateException("Ghi thực dùng theo từng hạng mục trong không gian kỹ thuật.");
        }
        kiemTraLenh(taiKhoan, maLenh);
        if (banCu != null) {
            kiemTraNhap(taiKhoan, banCu);
        }
        PhuTung phuTung = docPhuTung(maPhuTung);
        phuTung.kiemTraSoLuong(soLuong);
        kiemTraPhamVi(taiKhoan, maLenh, maPhuTung, soLuong);
        for (PhuTungSuDung banGhi : hoSo) {
            if (banGhi != banCu && banGhi.getTrangThai() == TrangThaiSuDung.NHAP
                    && maLenh.equals(banGhi.getMaLenhSuaChua())
                    && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())) {
                throw new IllegalArgumentException("Lệnh đã có nháp cho phụ tùng này. Hãy sửa nháp hiện có.");
            }
        }
        String ma = banCu == null ? taoMaMoi() : banCu.getMaGhiNhan();
        PhuTungSuDung ketQua = taoBanGhi(ma, maLenh, maPhuTung, soLuong, ghiChu, TrangThaiSuDung.NHAP);
        luuHoSo(hoSo, ketQua);
        return ketQua;
    }

    // Xóa bản nháp chưa xuất kho và giữ nguyên tồn kho hiện tại.
    /** Thực hiện xoa nhap cho thành phần hiện tại. */
    public void xoaNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        kiemTraNhap(taiKhoan, banCu);
        hoSo.remove(timHoSo(banCu.getMaGhiNhan()));
    }

    // Kiểm tra toàn bộ nhóm trước khi ghi để lỗi một dòng không làm trừ kho các dòng khác.
    /** Thực hiện xac nhan thuc dung cho thành phần hiện tại. */
    public PhuTungSuDung xacNhanThucDung(TaiKhoan taiKhoan, PhuTungSuDung banCu, List<LuongPhuTung> luongDung,
            String ghiChu) {

        kiemTraNhap(taiKhoan, banCu);
        String maLenh = banCu.getMaLenhSuaChua();
        List<PhuTungSuDung> hoSoMoi = new ArrayList<>(hoSo);
        double luongGoc = PhuTungSuDungDAO.layLuong(luongDung, banCu.getPhuTung().getMaPhuTung());
        if ((luongGoc <= 0)) {
            throw new IllegalArgumentException("Phụ tùng của bản nháp đang xác nhận phải có lượng dùng lớn hơn 0.");
        }
        for (LuongPhuTung dongLuong : luongDung) {
            PhuTung phuTung = docPhuTung(dongLuong.getMaPhuTung());
            double soLuong = dongLuong.getSoLuong();
            if ((soLuong == 0)) {

                continue;
            }
            phuTung.kiemTraSoLuong(soLuong);
            if ((soLuong > docTon(dongLuong.getMaPhuTung()))) {
                throw new IllegalArgumentException(phuTung.getMaPhuTung() + " còn "
                        + String.valueOf(docTon(dongLuong.getMaPhuTung())) + " " + phuTung.getDonViTinh()
                        + " nhưng nhập " + String.valueOf(soLuong) + " " + phuTung.getDonViTinh() + ".");
            }
            kiemTraPhamVi(taiKhoan, maLenh, dongLuong.getMaPhuTung(), soLuong);
        }
        for (LuongPhuTung dongLuong : luongDung) {
            if ((dongLuong.getSoLuong() == 0)) {

                continue;
            }
            PhuTungSuDung nhap = null;
            for (PhuTungSuDung banGhi : hoSo) {
                if (banGhi.getTrangThai() == TrangThaiSuDung.NHAP && maLenh.equals(banGhi.getMaLenhSuaChua())
                        && dongLuong.getMaPhuTung().equals(banGhi.getPhuTung().getMaPhuTung())) {
                    nhap = banGhi;
                }
            }
            String ma = nhap == null ? taoMaMoi() : nhap.getMaGhiNhan();
            luuHoSo(hoSoMoi, taoBanGhi(ma, maLenh, dongLuong.getMaPhuTung(), dongLuong.getSoLuong(), ghiChu,
                    TrangThaiSuDung.DA_GHI_NHAN));
        }
        List<LuongPhuTung> luongXuat = new ArrayList<>();
        for (LuongPhuTung dongLuong : luongDung) {
            if ((dongLuong.getSoLuong() > 0)) {
                themLuong(luongXuat, dongLuong.getMaPhuTung(), dongLuong.getSoLuong());
            }
        }
        ghiBienDongKho(luongXuat, false, maLenh, taiKhoan, ghiChu, LocalDateTime.now());
        hoSo.clear();
        hoSo.addAll(hoSoMoi);
        lenhChoPhuTung.remove(maLenh);
        return timHoSo(banCu.getMaGhiNhan());

    }

    // Lưu trạng thái chờ phụ tùng mà không xuất kho hoặc xóa bản nháp.
    /** Thực hiện chuyen cho phu tung cho thành phần hiện tại. */
    public void chuyenChoPhuTung(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        kiemTraNhap(taiKhoan, banCu);
        if (!lenhChoPhuTung.contains(banCu.getMaLenhSuaChua())) {
            lenhChoPhuTung.add(banCu.getMaLenhSuaChua());
        }
    }

    // Tạo bản ghi dùng cho dữ liệu và chức năng phụ tùng hiện có.
    /** Tạo ban ghi cho giao diện. */
    private PhuTungSuDung taoBanGhi(String ma, String maLenh, String maPhuTung, double soLuong, String ghiChu,
            TrangThaiSuDung trangThai) {
        return new PhuTungSuDung(ma, maLenh, docPhuTung(maPhuTung), soLuong, "Chung Ngô Minh Hoàng", ghiChu, trangThai);
    }

    // Sinh mã ghi nhận không trùng trong phiên DAO hiện tại.
    /** Tạo ma moi cho giao diện. */
    private String taoMaMoi() {
        String ma;
        do {
            ma = String.format(Locale.ROOT, "SD-2026-%04d", soTiepTheo++);
        } while ((timHoSo(ma) != null));
        return ma;
    }

    // Kiểm tra tài khoản có vai trò kỹ thuật trước khi thao tác dữ liệu.
    /** Kiểm tra tai khoan trước khi xử lý. */
    private void kiemTraTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan != null) {
            taiKhoan.kiemTraPhien();
        }
        if (taiKhoan == null || taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chức năng dành cho nhân viên kỹ thuật đã đăng nhập.");
        }
    }

    // Kiểm tra lệnh tồn tại và được phân công cho tài khoản hiện tại.
    /** Kiểm tra lenh trước khi xử lý. */
    private void kiemTraLenh(TaiKhoan taiKhoan, String maLenh) {
        kiemTraTaiKhoan(taiKhoan);
        if (nguonLenh != null) {
            if (nguonLenh.timTheoMa(taiKhoan, maLenh) == null) {
                throw new SecurityException("Lệnh không tồn tại hoặc không thuộc phân công.");
            }
            return;
        }
        if (!taiKhoan.getTenDangNhap().equals("kythuat")) {
            throw new SecurityException("Lệnh không thuộc phân công.");
        }
        String[] maMau = { "SC-2026-0111", "SC-2026-0113", "SC-2026-0114", "SC-2026-0108" };
        for (String ma : maMau) {
            if (ma.equals(maLenh)) {
                return;
            }
        }
        throw new IllegalArgumentException("Lệnh không tồn tại hoặc chưa được khách duyệt.");
    }

    // Chặn thao tác lên hồ sơ đã ghi nhận hoặc bản nháp đã thay đổi.
    /** Kiểm tra nhap trước khi xử lý. */
    private void kiemTraNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        if (banCu == null) {
            throw new NullPointerException("Chưa chọn hồ sơ.");
        }

        kiemTraLenh(taiKhoan, banCu.getMaLenhSuaChua());
        if (timHoSo(banCu.getMaGhiNhan()) != banCu || banCu.getTrangThai() != TrangThaiSuDung.NHAP) {
            throw new IllegalStateException("Hồ sơ đã ghi nhận hoặc đã thay đổi. Hãy tải lại danh sách.");
        }
    }

    // Kiểm tra lượng sử dụng tích lũy không vượt số lượng khách đã duyệt.
    /** Kiểm tra pham vi trước khi xử lý. */
    private void kiemTraPhamVi(TaiKhoan taiKhoan, String maLenh, String maPhuTung, double soLuong) {
        if ((soLuong > docLuongConLai(taiKhoan, maLenh, maPhuTung))) {
            throw new IllegalArgumentException("Số lượng " + maPhuTung + " vượt lượng còn được khách duyệt.");
        }
    }

    /** Chỗ nối với module Kỹ thuật hiện có; không đổi kiểu dữ liệu của module đó. */
    private void ghiBienDongKho(List<LuongPhuTung> luong, boolean nhap, String chungTu, TaiKhoan tk, String lyDo,
            LocalDateTime ngay) {
        String[] ma = new String[luong.size()];
        double[] soLuong = new double[luong.size()];
        int i = 0;
        for (LuongPhuTung dongLuong : luong) {
            ma[i] = dongLuong.getMaPhuTung();
            soLuong[i] = dongLuong.getSoLuong();
            i++;
        }
        kho.ghiBienDong(ma, soLuong, nhap, chungTu, tk, lyDo, ngay);
    }

    /** Tìm lượng theo mã bằng vòng lặp; mã chưa có trả về 0. */
    public static double layLuong(List<LuongPhuTung> danhSach, String ma) {
        for (LuongPhuTung dong : danhSach) {
            if (dong.getMaPhuTung().equals(ma)) {
                return dong.getSoLuong();
            }
        }
        return 0;
    }

    /** Cộng lượng của cùng mã và làm tròn về ba số lẻ. */
    public static void themLuong(List<LuongPhuTung> danhSach, String ma, double soLuong) {
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getMaPhuTung().equals(ma)) {
                double tong = Math.round((danhSach.get(i).getSoLuong() + soLuong) * 1000) / 1000.0;
                danhSach.set(i, new LuongPhuTung(ma, tong));
                return;
            }
        }
        danhSach.add(new LuongPhuTung(ma, soLuong));
    }

    /** Tạo danh sách chỉ có một phụ tùng thực dùng. */
    private static List<LuongPhuTung> taoLuong(String ma, double soLuong) {
        List<LuongPhuTung> ketQua = new ArrayList<>();
        ketQua.add(new LuongPhuTung(ma, soLuong));
        return ketQua;
    }

    /** Tìm hồ sơ thực dùng theo mã. */
    private PhuTungSuDung timHoSo(String ma) {
        for (PhuTungSuDung dong : hoSo) {
            if (dong.getMaGhiNhan().equals(ma)) {
                return dong;
            }
        }
        return null;
    }

    /** Thêm hoặc thay thế đúng hồ sơ trong danh sách. */
    private void luuHoSo(List<PhuTungSuDung> danhSach, PhuTungSuDung moi) {
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getMaGhiNhan().equals(moi.getMaGhiNhan())) {
                danhSach.set(i, moi);
                return;
            }
        }
        danhSach.add(moi);
    }
}
