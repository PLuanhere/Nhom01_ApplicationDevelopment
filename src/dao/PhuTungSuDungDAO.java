package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import entity.PhuTung;
import entity.PhuTungSuDung;
import entity.TaiKhoan;
import entity.TrangThaiSuDung;
import entity.VaiTro;

// Quản lý phụ tùng và tồn kho mẫu trong bộ nhớ của phiên sử dụng.
public final class PhuTungSuDungDAO {
    private final Map<String, PhuTung> danhMuc = new LinkedHashMap<String, PhuTung>();
    private final Map<String, BigDecimal> tonKho = new LinkedHashMap<String, BigDecimal>();
    private final Map<String, PhuTungSuDung> hoSo = new LinkedHashMap<String, PhuTungSuDung>();
    private final Map<String, Map<String, BigDecimal>> phamViDuyet =
            new LinkedHashMap<String, Map<String, BigDecimal>>();
    private final Map<String, String> phanCong = new LinkedHashMap<String, String>();
    private final List<String> lenhChoPhuTung = new ArrayList<String>();
    private final Map<String, String> luotDaGhi = new LinkedHashMap<String, String>();
    private int soTiepTheo = 113;

    private PhieuSuaChuaDAO nguonLenh;

    public PhuTungSuDungDAO() { this(true); }

    /** Giữ danh mục/tồn đầu kỳ, nhưng không nạp lệnh và chứng từ mẫu khi dùng luồng chung. */
    public PhuTungSuDungDAO(boolean coHoSoMau) {
        themDanhMuc("PT001", "Dầu 5W-30", "lít", "48", "12");
        themDanhMuc("PT005", "Lọc dầu động cơ", "cái", "6", "12");
        themDanhMuc("PT004", "Rotuyn lái", "cái", "5", "2");
        if (!coHoSoMau) { soTiepTheo = 1; return; }
        String[] maLenh = {"SC-2026-0111", "SC-2026-0113", "SC-2026-0114", "SC-2026-0108"};
        for (String ma : maLenh) {
            Map<String, BigDecimal> chiTiet = new LinkedHashMap<String, BigDecimal>();
            chiTiet.put("PT001", new BigDecimal("4"));
            chiTiet.put("PT005", BigDecimal.ONE);
            if ("SC-2026-0114".equals(ma)) {
                chiTiet.put("PT004", BigDecimal.ONE);
            }
            phamViDuyet.put(ma, chiTiet);
            phanCong.put(ma, "kythuat");
        }
        hoSo.put("SD-2026-0112", taoBanGhi("SD-2026-0112", "SC-2026-0111", "PT001",
                new BigDecimal("2"), "Chưa xác nhận xuất kho", TrangThaiSuDung.NHAP));
        hoSo.put("SD-2026-0108", taoBanGhi("SD-2026-0108", "SC-2026-0108", "PT001",
                new BigDecimal("4"), "Đã ghi nhận thực dùng", TrangThaiSuDung.DA_GHI_NHAN));
    }

    void noiNguonLenh(PhieuSuaChuaDAO nguon) {
        if (nguonLenh != null) throw new IllegalStateException("Nguồn lệnh đã được kết nối.");
        nguonLenh = Objects.requireNonNull(nguon);
    }

    public List<PhuTung> docDanhMuc() { return new ArrayList<>(danhMuc.values()); }

    /** Khóa đúng dòng báo giá bằng vị trí, không suy ra phụ tùng từ tên hạng mục. */
    synchronized PhuTungSuDung ghiTheoHangMuc(TaiKhoan tk, String maLenh, int viTri, String maPT, BigDecimal so, String luot) {
        kiemTraLenh(tk, maLenh);
        entity.PhieuSuaChua lenh = nguonLenh.timTheoMa(tk, maLenh).orElseThrow();
        List<entity.ChiTietBaoGia> chiTiet = nguonLenh.layBaoGia(tk, maLenh).getChiTiet();
        if (lenh.getTrangThai() == entity.TrangThaiSuaChua.HOAN_THANH || viTri < 0 || viTri >= chiTiet.size()) throw new IllegalStateException("Lệnh/hạng mục không thể ghi nhận thêm phụ tùng.");
        entity.HangMucSuaChua hm = lenh.getDanhSachHangMuc().get(viTri);
        if (!hm.getTaiKhoanThucHien().equals(tk.getTenDangNhap()) && !lenh.getKyThuatChinh().equals(tk.getTenDangNhap())) throw new SecurityException("Không được ghi phụ tùng cho hạng mục này.");
        entity.ChiTietBaoGia ct = chiTiet.get(viTri);
        if (!"Phụ tùng".equals(ct.getLoai()) || !ct.getMaPhuTung().equals(maPT)) throw new IllegalArgumentException("Phụ tùng không thuộc dòng đã duyệt.");
        docPhuTung(maPT).kiemTraSoLuong(so);
        String khoa = maLenh + "#" + viTri;
        BigDecimal daDung = BigDecimal.ZERO;
        for (PhuTungSuDung dong : hoSo.values()) if (dong.getHangMuc().equals(khoa) && dong.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) daDung = daDung.add(dong.getSoLuong());
        if (daDung.add(so).compareTo(ct.getSoLuong()) > 0) throw new IllegalArgumentException("Lượng thực dùng vượt dòng báo giá đã duyệt.");
        return ghiNoiBo(tk, maLenh, khoa, maPT, so, docPhamViDuyet(tk, maLenh).get(maPT), luot);
    }

    // Ghi lượng thực dùng sau khi DAO kỹ thuật đã kiểm tra lệnh và hạng mục hiện tại.
    synchronized PhuTungSuDung ghiTheoHangMuc(TaiKhoan taiKhoan, String maLenh, String hangMuc,
            String maPhuTung, BigDecimal soLuong, BigDecimal gioiHan, String maLuot) {
        if (nguonLenh != null) throw new IllegalStateException("Cần chọn đúng dòng báo giá khi ghi nhận phụ tùng.");
        return ghiNoiBo(taiKhoan, maLenh, hangMuc, maPhuTung, soLuong, gioiHan, maLuot);
    }

    private PhuTungSuDung ghiNoiBo(TaiKhoan taiKhoan, String maLenh, String hangMuc,
            String maPhuTung, BigDecimal soLuong, BigDecimal gioiHan, String maLuot) {
        kiemTraTaiKhoan(taiKhoan);
        if (maLuot == null || maLuot.isBlank() || luotDaGhi.containsKey(maLuot)) {
            throw new IllegalStateException("Lượt sử dụng đã được ghi nhận hoặc không hợp lệ.");
        }
        PhuTung phuTung = docPhuTung(maPhuTung);
        phuTung.kiemTraSoLuong(soLuong);
        BigDecimal daDung = BigDecimal.ZERO;
        for (PhuTungSuDung dong : hoSo.values()) {
            if (maLenh.equals(dong.getMaLenhSuaChua()) && maPhuTung.equals(dong.getPhuTung().getMaPhuTung())
                    && dong.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                daDung = daDung.add(dong.getSoLuong());
            }
        }
        if (soLuong.compareTo(docTon(maPhuTung)) > 0) {
            throw new IllegalArgumentException("Số lượng thực dùng vượt tồn khả dụng.");
        }
        if (gioiHan == null || daDung.add(soLuong).compareTo(gioiHan) > 0) {
            throw new IllegalArgumentException("Tổng lượng dùng vượt phạm vi báo giá đã được duyệt.");
        }
        String ma = taoMaMoi();
        PhuTungSuDung dong = new PhuTungSuDung(ma, maLenh, phuTung, soLuong,
                taiKhoan.getTenDangNhap(), "Ghi nhận thực dùng", TrangThaiSuDung.DA_GHI_NHAN, hangMuc);
        hoSo.put(ma, dong);
        tonKho.put(maPhuTung, tonKho.get(maPhuTung).subtract(soLuong));
        phanCong.put(maLenh, taiKhoan.getTenDangNhap());
        luotDaGhi.put(maLuot, ma);
        return dong;
    }

    // Lọc hồ sơ theo từ khóa, trạng thái và người được phân công.
    public synchronized List<PhuTungSuDung> docDanhSach(TaiKhoan taiKhoan, String tuKhoa,
            TrangThaiSuDung trangThai) {
        kiemTraTaiKhoan(taiKhoan);
        List<PhuTungSuDung> ketQua = new ArrayList<PhuTungSuDung>();
        String khoa = Objects.requireNonNull(tuKhoa).trim().toLowerCase(Locale.ROOT);
        for (PhuTungSuDung banGhi : hoSo.values()) {
            String noiDung = banGhi.getMaGhiNhan() + " " + banGhi.getMaLenhSuaChua() + " " + banGhi.getPhuTung();
            boolean duocDoc = nguonLenh == null ? taiKhoan.getTenDangNhap().equals(phanCong.get(banGhi.getMaLenhSuaChua()))
                    : nguonLenh.timTheoMa(taiKhoan, banGhi.getMaLenhSuaChua()).isPresent();
            if (duocDoc && noiDung.toLowerCase(Locale.ROOT).contains(khoa)
                    && (trangThai == null || trangThai == banGhi.getTrangThai())) {
                ketQua.add(banGhi);
            }
        }
        return ketQua;
    }

    /** Tiếp nhận tra cứu thực dùng của lệnh để xem lịch sử; không có quyền xác nhận xuất kho. */
    public synchronized List<PhuTungSuDung> layThucDungChoTiepNhan(TaiKhoan tk, String maLenh) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) throw new SecurityException("Chỉ tiếp nhận được tra lịch sử tại quầy.");
        if (nguonLenh == null || nguonLenh.timTheoMa(tk, maLenh).isEmpty()) throw new IllegalArgumentException("Lệnh sửa chữa không tồn tại.");
        List<PhuTungSuDung> ketQua = new ArrayList<>();
        for (PhuTungSuDung dong : hoSo.values()) if (dong.getMaLenhSuaChua().equals(maLenh) && dong.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) ketQua.add(dong);
        return ketQua;
    }

    // Đọc các lệnh được phân công và có phạm vi phụ tùng đã duyệt.
    public synchronized List<String> docLenhDaDuyet(TaiKhoan taiKhoan) {
        kiemTraTaiKhoan(taiKhoan);
        List<String> ketQua = new ArrayList<String>();
        if (nguonLenh != null) {
            for (entity.PhieuSuaChua lenh : nguonLenh.timKiem(taiKhoan, "", "Tất cả")) ketQua.add(lenh.getMaPhieu());
            return ketQua;
        }
        for (String maLenh : phamViDuyet.keySet()) {
            if (taiKhoan.getTenDangNhap().equals(phanCong.get(maLenh))) {
                ketQua.add(maLenh);
            }
        }
        return ketQua;
    }

    // Trả bản sao phạm vi duyệt để màn hình không sửa trực tiếp dữ liệu nội bộ.
    public synchronized Map<String, BigDecimal> docPhamViDuyet(TaiKhoan taiKhoan, String maLenh) {
        kiemTraLenh(taiKhoan, maLenh);
        if (nguonLenh != null) {
            Map<String, BigDecimal> ketQua = new LinkedHashMap<>();
            for (entity.ChiTietBaoGia ct : nguonLenh.layBaoGia(taiKhoan, maLenh).getChiTiet()) {
                if (!ct.getMaPhuTung().isEmpty()) ketQua.merge(ct.getMaPhuTung(), ct.getSoLuong(), BigDecimal::add);
            }
            return ketQua;
        }
        return new LinkedHashMap<String, BigDecimal>(phamViDuyet.get(maLenh));
    }

    // Tìm phụ tùng trong danh mục và báo lỗi nếu mã không tồn tại.
    public synchronized PhuTung docPhuTung(String maPhuTung) {
        PhuTung phuTung = danhMuc.get(maPhuTung);
        if (phuTung == null) {
            throw new IllegalArgumentException("Phụ tùng không tồn tại.");
        }
        return phuTung;
    }

    // Đọc tồn hiện tại từ nguồn dữ liệu dùng chung của phiên.
    public synchronized BigDecimal docTon(String maPhuTung) {
        docPhuTung(maPhuTung);
        return tonKho.get(maPhuTung);
    }

    // Tính lượng còn được dùng sau các lần đã ghi nhận theo lệnh.
    public synchronized BigDecimal docLuongConLai(TaiKhoan taiKhoan, String maLenh, String maPhuTung) {
        kiemTraLenh(taiKhoan, maLenh);
        BigDecimal conLai = docPhamViDuyet(taiKhoan, maLenh).get(maPhuTung);
        if (conLai == null) {
            throw new IllegalArgumentException("Phụ tùng chưa được khách duyệt.");
        }
        for (PhuTungSuDung banGhi : hoSo.values()) {
            if (maLenh.equals(banGhi.getMaLenhSuaChua()) && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())
                    && banGhi.getTrangThai() == TrangThaiSuDung.DA_GHI_NHAN) {
                conLai = conLai.subtract(banGhi.getSoLuong());
            }
        }
        return conLai;
    }

    // Lưu nháp sau khi kiểm tra phân công, phạm vi duyệt và phiên bản đang sửa.
    public synchronized PhuTungSuDung luuNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu, String maLenh,
            String maPhuTung, BigDecimal soLuong, String ghiChu) {
        if (nguonLenh != null) throw new IllegalStateException("Ghi thực dùng theo từng hạng mục trong không gian kỹ thuật.");
        kiemTraLenh(taiKhoan, maLenh);
        if (banCu != null) {
            kiemTraNhap(taiKhoan, banCu);
        }
        PhuTung phuTung = docPhuTung(maPhuTung);
        phuTung.kiemTraSoLuong(soLuong);
        kiemTraPhamVi(taiKhoan, maLenh, maPhuTung, soLuong);
        for (PhuTungSuDung banGhi : hoSo.values()) {
            if (banGhi != banCu && banGhi.getTrangThai() == TrangThaiSuDung.NHAP
                    && maLenh.equals(banGhi.getMaLenhSuaChua())
                    && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())) {
                throw new IllegalArgumentException("Lệnh đã có nháp cho phụ tùng này. Hãy sửa nháp hiện có.");
            }
        }
        String ma = banCu == null ? taoMaMoi() : banCu.getMaGhiNhan();
        PhuTungSuDung ketQua = taoBanGhi(ma, maLenh, maPhuTung, soLuong, ghiChu, TrangThaiSuDung.NHAP);
        hoSo.put(ma, ketQua);
        return ketQua;
    }

    // Xóa bản nháp chưa xuất kho và giữ nguyên tồn kho hiện tại.
    public synchronized void xoaNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        kiemTraNhap(taiKhoan, banCu);
        hoSo.remove(banCu.getMaGhiNhan());
    }

    // Kiểm tra toàn bộ nhóm trước khi ghi để lỗi một dòng không làm trừ kho các dòng khác.
    public synchronized PhuTungSuDung xacNhanThucDung(TaiKhoan taiKhoan, PhuTungSuDung banCu,
            Map<String, BigDecimal> luongDung, String ghiChu) {
        kiemTraNhap(taiKhoan, banCu);
        String maLenh = banCu.getMaLenhSuaChua();
        Map<String, PhuTungSuDung> hoSoMoi = new LinkedHashMap<String, PhuTungSuDung>(hoSo);
        Map<String, BigDecimal> tonMoi = new LinkedHashMap<String, BigDecimal>(tonKho);
        BigDecimal luongGoc = luongDung.get(banCu.getPhuTung().getMaPhuTung());
        if (luongGoc == null || luongGoc.signum() <= 0) {
            throw new IllegalArgumentException("Phụ tùng của bản nháp đang xác nhận phải có lượng dùng lớn hơn 0.");
        }
        for (Map.Entry<String, BigDecimal> dong : luongDung.entrySet()) {
            PhuTung phuTung = docPhuTung(dong.getKey());
            BigDecimal soLuong = Objects.requireNonNull(dong.getValue(), "Thiếu số lượng.");
            if (soLuong.signum() == 0) {
                continue;
            }
            phuTung.kiemTraSoLuong(soLuong);
            if (soLuong.compareTo(tonKho.get(dong.getKey())) > 0) {
                throw new IllegalArgumentException(phuTung.getMaPhuTung() + " còn "
                        + tonKho.get(dong.getKey()).toPlainString() + " " + phuTung.getDonViTinh()
                        + " nhưng nhập " + soLuong.toPlainString() + " " + phuTung.getDonViTinh() + ".");
            }
            kiemTraPhamVi(taiKhoan, maLenh, dong.getKey(), soLuong);
        }
        for (Map.Entry<String, BigDecimal> dong : luongDung.entrySet()) {
            if (dong.getValue().signum() == 0) {
                continue;
            }
            PhuTungSuDung nhap = null;
            for (PhuTungSuDung banGhi : hoSo.values()) {
                if (banGhi.getTrangThai() == TrangThaiSuDung.NHAP && maLenh.equals(banGhi.getMaLenhSuaChua())
                        && dong.getKey().equals(banGhi.getPhuTung().getMaPhuTung())) {
                    nhap = banGhi;
                }
            }
            String ma = nhap == null ? taoMaMoi() : nhap.getMaGhiNhan();
            hoSoMoi.put(ma, taoBanGhi(ma, maLenh, dong.getKey(), dong.getValue(), ghiChu,
                    TrangThaiSuDung.DA_GHI_NHAN));
            tonMoi.put(dong.getKey(), tonKho.get(dong.getKey()).subtract(dong.getValue()));
        }
        hoSo.clear();
        hoSo.putAll(hoSoMoi);
        tonKho.clear();
        tonKho.putAll(tonMoi);
        lenhChoPhuTung.remove(maLenh);
        return hoSo.get(banCu.getMaGhiNhan());
    }

    // Lưu trạng thái chờ phụ tùng mà không xuất kho hoặc xóa bản nháp.
    public synchronized void chuyenChoPhuTung(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        kiemTraNhap(taiKhoan, banCu);
        if (!lenhChoPhuTung.contains(banCu.getMaLenhSuaChua())) {
            lenhChoPhuTung.add(banCu.getMaLenhSuaChua());
        }
    }

    // Thêm phụ tùng và số lượng tồn đầu phiên vào danh mục mẫu.
    private void themDanhMuc(String ma, String ten, String donVi, String ton, String toiThieu) {
        danhMuc.put(ma, new PhuTung(ma, ten, donVi, new BigDecimal(toiThieu)));
        tonKho.put(ma, new BigDecimal(ton));
    }

    // Tạo bản ghi dùng cho dữ liệu và chức năng phụ tùng hiện có.
    private PhuTungSuDung taoBanGhi(String ma, String maLenh, String maPhuTung, BigDecimal soLuong,
            String ghiChu, TrangThaiSuDung trangThai) {
        return new PhuTungSuDung(ma, maLenh, docPhuTung(maPhuTung), soLuong,
                "Chung Ngô Minh Hoàng", ghiChu, trangThai);
    }

    // Sinh mã ghi nhận không trùng trong phiên DAO hiện tại.
    private String taoMaMoi() {
        String ma;
        do {
            ma = String.format(Locale.ROOT, "SD-2026-%04d", soTiepTheo++);
        } while (hoSo.containsKey(ma));
        return ma;
    }

    // Kiểm tra tài khoản có vai trò kỹ thuật trước khi thao tác dữ liệu.
    private void kiemTraTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chức năng dành cho nhân viên kỹ thuật đã đăng nhập.");
        }
    }

    // Kiểm tra lệnh tồn tại và được phân công cho tài khoản hiện tại.
    private void kiemTraLenh(TaiKhoan taiKhoan, String maLenh) {
        kiemTraTaiKhoan(taiKhoan);
        if (nguonLenh != null) {
            if (nguonLenh.timTheoMa(taiKhoan, maLenh).isEmpty()) throw new SecurityException("Lệnh không tồn tại hoặc không thuộc phân công.");
            return;
        }
        if (!phamViDuyet.containsKey(maLenh)) {
            throw new IllegalArgumentException("Lệnh không tồn tại hoặc chưa được khách duyệt.");
        }
        if (!taiKhoan.getTenDangNhap().equals(phanCong.get(maLenh))) {
            throw new SecurityException("Lệnh không được phân công cho tài khoản hiện tại.");
        }
    }

    // Chặn thao tác lên hồ sơ đã ghi nhận hoặc bản nháp đã thay đổi.
    private void kiemTraNhap(TaiKhoan taiKhoan, PhuTungSuDung banCu) {
        Objects.requireNonNull(banCu, "Chưa chọn hồ sơ.");
        kiemTraLenh(taiKhoan, banCu.getMaLenhSuaChua());
        if (hoSo.get(banCu.getMaGhiNhan()) != banCu || banCu.getTrangThai() != TrangThaiSuDung.NHAP) {
            throw new IllegalStateException("Hồ sơ đã ghi nhận hoặc đã thay đổi. Hãy tải lại danh sách.");
        }
    }

    // Kiểm tra lượng sử dụng tích lũy không vượt số lượng khách đã duyệt.
    private void kiemTraPhamVi(TaiKhoan taiKhoan, String maLenh, String maPhuTung, BigDecimal soLuong) {
        if (soLuong.compareTo(docLuongConLai(taiKhoan, maLenh, maPhuTung)) > 0) {
            throw new IllegalArgumentException("Số lượng " + maPhuTung + " vượt lượng còn được khách duyệt.");
        }
    }
}