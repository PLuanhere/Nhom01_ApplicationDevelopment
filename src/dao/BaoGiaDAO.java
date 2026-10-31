package dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import entity.BaoCaoKiemTra;
import entity.BaoGia;
import entity.ChiTietBaoGia;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiBaoGia;
import entity.TrangThaiKiemTra;
import entity.VaiTro;
import entity.Xe;

/** Kho báo giá trong phiên; kiểm tra quyền, nguồn kiểm tra và từng chuyển trạng thái. */
public final class BaoGiaDAO {
    private final PhieuTiepNhanDAO phieuDAO;
    /** Đọc nguồn hiện tại khi lập/sửa/gửi, không tin báo cáo do UI tự chuyển vào hàm lưu. */
    private final Supplier<List<BaoCaoKiemTra>> nguonBaoCao;
    private final Map<String, BaoGia> danhSach = new LinkedHashMap<>();
    private final Map<String, String> maThaoTac = new LinkedHashMap<>();
    private int soThuTu = 1;
    private PhuTungSuDungDAO danhMucPhuTung;

    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, Supplier<List<BaoCaoKiemTra>> nguonBaoCao) {
        this.phieuDAO = Objects.requireNonNull(phieuDAO);
        this.nguonBaoCao = Objects.requireNonNull(nguonBaoCao);
    }

    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, Supplier<List<BaoCaoKiemTra>> nguonBaoCao, PhuTungSuDungDAO danhMuc) {
        this(phieuDAO, nguonBaoCao);
        danhMucPhuTung = Objects.requireNonNull(danhMuc);
    }

    private void kiemTraDanhMuc(List<ChiTietBaoGia> chiTiet) {
        if (danhMucPhuTung == null || chiTiet == null) return;
        for (ChiTietBaoGia ct : chiTiet) {
            if (ct == null) continue;
            if ("Phụ tùng".equals(ct.getLoai())) {
                entity.PhuTung pt = danhMucPhuTung.docPhuTung(ct.getMaPhuTung());
                if (!pt.getTenPhuTung().equals(ct.getTenHangMuc()) || !pt.getDonViTinh().equals(ct.getDonVi())) {
                    throw new IllegalArgumentException("Tên/đơn vị phụ tùng không khớp danh mục.");
                }
                pt.kiemTraSoLuong(ct.getSoLuong());
            } else if (!"Dịch vụ".equals(ct.getLoai()) || !ct.getMaPhuTung().isEmpty()) {
                throw new IllegalArgumentException("Loại hạng mục không hợp lệ hoặc dịch vụ chứa mã phụ tùng.");
            }
        }
    }

    /** Chỉ trả báo cáo hoàn tất của đúng phiếu đã được kho tiếp nhận xác nhận. */
    public synchronized List<BaoCaoKiemTra> layCanCu(TaiKhoan tk) {
        quyen(tk);
        List<BaoCaoKiemTra> ketQua = new ArrayList<>();
        for (BaoCaoKiemTra bc : nguonBaoCao.get()) {
            if (bc.getTrangThai() != TrangThaiKiemTra.HOAN_TAT) continue;
            try {
                PhieuTiepNhan phieu = phieuDAO.timTheoMa(tk, bc.getPhieu().getMaPhieu());
                if (phieu == bc.getPhieu() && phieu.isDaXacNhan()) ketQua.add(bc);
            } catch (IllegalArgumentException e) {
                // Bỏ qua chứng từ cũ không thuộc kho tiếp nhận hiện tại.
            }
        }
        return ketQua;
    }

    public synchronized BaoGia taoNhap(TaiKhoan tk, String token, String maBaoCao,
            List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraMaThaoTac(token);
        kiemTraDanhMuc(chiTiet);
        BaoCaoKiemTra bc = canCu(tk, maBaoCao);
        String daTao = maThaoTac.get(token);
        if (daTao != null) return kiemTraLap(tk, daTao, bc, chiTiet, hen, null);
        for (BaoGia bg : danhSach.values()) if (bg.getBaoCao().getMaBaoCao().equals(maBaoCao)) {
            throw new IllegalStateException("Báo cáo đã có báo giá. Hãy mở báo giá hiện có hoặc tạo bản điều chỉnh.");
        }
        String ma = String.format(Locale.ROOT, "BG-%04d-%04d", LocalDateTime.now().getYear(), soThuTu);
        BaoGia moi = new BaoGia(ma, ma, 1, bc, chiTiet, hen, tk.getTenDangNhap(), LocalDateTime.now());
        danhSach.put(ma, moi);
        maThaoTac.put(token, ma);
        soThuTu++;
        return moi;
    }

    /** Chỉ cập nhật bản nháp đang đọc, chống ghi đè một đối tượng đã được màn khác thay đổi. */
    public synchronized BaoGia suaNhap(TaiKhoan tk, BaoGia daDoc, List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraDanhMuc(chiTiet);
        BaoGia cu = banHienTai(daDoc);
        moiNhat(cu);
        if (cu.getTrangThai() != TrangThaiBaoGia.NHAP) throw new IllegalStateException("Bản đã gửi/duyệt chỉ đọc; cần tạo phiên bản mới.");
        BaoGia moi = new BaoGia(cu.getMaBaoGia(), cu.getMaBaoGiaGoc(), cu.getPhienBan(), canCu(tk, cu.getBaoCao().getMaBaoCao()),
                chiTiet, hen, cu.getNguoiLap(), cu.getNgayLap());
        danhSach.put(moi.getMaBaoGia(), moi);
        return moi;
    }

    public synchronized BaoGia taoDieuChinh(TaiKhoan tk, String token, BaoGia coSo, List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraMaThaoTac(token);
        kiemTraDanhMuc(chiTiet);
        Objects.requireNonNull(coSo, "Cần bản cơ sở.");
        BaoGia cu = timTheoMa(tk, coSo.getMaBaoGia());
        BaoCaoKiemTra bc = canCu(tk, cu.getBaoCao().getMaBaoCao());
        if (maThaoTac.containsKey(token)) return kiemTraLap(tk, maThaoTac.get(token), bc, chiTiet, hen, cu);
        cu = banHienTai(coSo);
        moiNhat(cu);
        if (cu.getTrangThai() == TrangThaiBaoGia.NHAP) throw new IllegalStateException("Bản hiện tại còn nháp; sửa bản nháp trước.");
        int phienBan = cu.getPhienBan() + 1;
        String ma = cu.getMaBaoGiaGoc() + "-V" + phienBan;
        BaoGia moi = new BaoGia(ma, cu.getMaBaoGiaGoc(), phienBan, bc, chiTiet, hen, tk.getTenDangNhap(), LocalDateTime.now());
        danhSach.put(ma, moi);
        maThaoTac.put(token, ma);
        return moi;
    }

    /** Gửi ở đây là ghi nhận đã trình khách, không tự gửi email/tin nhắn. */
    public synchronized BaoGia guiKhach(TaiKhoan tk, BaoGia daDoc) {
        quyen(tk);
        Objects.requireNonNull(daDoc);
        BaoGia cu = timTheoMa(tk, daDoc.getMaBaoGia());
        moiNhat(cu);
        if (cu.getTrangThai() == TrangThaiBaoGia.CHO_XAC_NHAN && daDoc.getTrangThai() == TrangThaiBaoGia.NHAP
                && cu.getNguoiGui().equals(tk.getTenDangNhap()) && cungNoiDung(cu, daDoc)) return cu;
        cu = banHienTai(daDoc);
        canCu(tk, cu.getBaoCao().getMaBaoCao());
        BaoGia moi = cu.guiKhach(tk.getTenDangNhap(), LocalDateTime.now());
        danhSach.put(moi.getMaBaoGia(), moi);
        return moi;
    }

    public synchronized BaoGia ghiNhanPhanHoi(TaiKhoan tk, BaoGia daDoc, TrangThaiBaoGia quyetDinh,
            LocalDateTime lucPhanHoi, String kenh, String ghiChu) {
        quyen(tk);
        Objects.requireNonNull(daDoc);
        BaoGia cu = timTheoMa(tk, daDoc.getMaBaoGia());
        moiNhat(cu);
        if (cu.getTrangThai() == quyetDinh && cu.getNguoiGhiNhan().equals(tk.getTenDangNhap())
                && Objects.equals(cu.getThoiDiemPhanHoi(), lucPhanHoi) && cu.getKenhPhanHoi().equals(kenh == null ? "" : kenh.strip())
                && cu.getGhiChuPhanHoi().equals(ghiChu == null ? "" : ghiChu.strip())) return cu;
        cu = banHienTai(daDoc);
        BaoGia moi = cu.ghiNhanPhanHoi(quyetDinh, tk.getTenDangNhap(), lucPhanHoi, LocalDateTime.now(), kenh, ghiChu);
        danhSach.put(moi.getMaBaoGia(), moi);
        return moi;
    }

    public synchronized BaoGia timTheoMa(TaiKhoan tk, String ma) {
        quyen(tk);
        BaoGia bg = danhSach.get(ma);
        if (bg == null) throw new IllegalArgumentException("Báo giá không tồn tại.");
        return bg;
    }

    public synchronized List<BaoGia> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiBaoGia trangThai) {
        quyen(tk);
        String tu = Objects.requireNonNull(tuKhoa).strip().toLowerCase(Locale.ROOT);
        String bien = tu.toUpperCase(Locale.ROOT).replaceAll("[\\s.\\-]", "");
        List<BaoGia> ketQua = new ArrayList<>();
        for (BaoGia bg : danhSach.values()) {
            String chu = bg.getMaBaoGia() + " " + bg.getBaoCao().getPhieu().getTenKhach() + " " + bg.getBienSo();
            if ((trangThai == null || bg.getTrangThai() == trangThai) && (chu.toLowerCase(Locale.ROOT).contains(tu)
                    || (!bien.isEmpty() && Xe.chuanHoaBienSo(bg.getBienSo()).contains(bien)))) ketQua.add(bg);
        }
        return ketQua;
    }

    /** Bản duyệt cao nhất của mỗi nhóm; phát sinh chờ duyệt không mở quyền cho dòng mới. */
    public synchronized List<BaoGia> layDaDuyet(TaiKhoan tk) {
        quyen(tk);
        Map<String, BaoGia> duyet = new LinkedHashMap<>();
        for (BaoGia bg : danhSach.values()) if (bg.isDaDongY()) {
            BaoGia cu = duyet.get(bg.getMaBaoGiaGoc());
            if (cu == null || cu.getPhienBan() < bg.getPhienBan()) duyet.put(bg.getMaBaoGiaGoc(), bg);
        }
        return new ArrayList<>(duyet.values());
    }

    private BaoGia kiemTraLap(TaiKhoan tk, String ma, BaoCaoKiemTra bc, List<ChiTietBaoGia> chiTiet, LocalDateTime hen, BaoGia coSo) {
        BaoGia cu = timTheoMa(tk, ma);
        if ((coSo == null && cu.getPhienBan() != 1) || (coSo != null
                && (!cu.getMaBaoGiaGoc().equals(coSo.getMaBaoGiaGoc()) || cu.getPhienBan() != coSo.getPhienBan() + 1))) {
            throw new IllegalArgumentException("Mã thao tác thuộc bản báo giá khác.");
        }
        BaoGia noiDung = new BaoGia(cu.getMaBaoGia(), cu.getMaBaoGiaGoc(), cu.getPhienBan(), bc, chiTiet, hen, tk.getTenDangNhap(), LocalDateTime.now());
        if (!cungNoiDung(cu, noiDung) || !cu.getNguoiLap().equals(tk.getTenDangNhap())) throw new IllegalArgumentException("Mã thao tác đã lưu nội dung khác.");
        return cu;
    }

    private boolean cungNoiDung(BaoGia a, BaoGia b) {
        if (!a.getBaoCao().getMaBaoCao().equals(b.getBaoCao().getMaBaoCao()) || !a.getNgayHen().equals(b.getNgayHen())) return false;
        List<ChiTietBaoGia> x = a.getChiTiet(), y = b.getChiTiet();
        if (x.size() != y.size()) return false;
        for (int i = 0; i < x.size(); i++) {
            ChiTietBaoGia m = x.get(i), n = y.get(i);
            if (!m.getMaPhuTung().equals(n.getMaPhuTung()) || !m.getTenHangMuc().equals(n.getTenHangMuc()) || !m.getLoai().equals(n.getLoai()) || !m.getDonVi().equals(n.getDonVi())
                    || m.getSoLuong().compareTo(n.getSoLuong()) != 0 || m.getDonGia().compareTo(n.getDonGia()) != 0) return false;
        }
        return true;
    }

    private BaoCaoKiemTra canCu(TaiKhoan tk, String ma) {
        for (BaoCaoKiemTra bc : layCanCu(tk)) if (bc.getMaBaoCao().equals(ma)) return bc;
        throw new IllegalArgumentException("Báo cáo chưa hoàn tất hoặc chưa liên kết phiếu tiếp nhận trong phiên.");
    }
    private BaoGia banHienTai(BaoGia daDoc) {
        if (daDoc == null || danhSach.get(daDoc.getMaBaoGia()) != daDoc) {
            throw new IllegalStateException("Báo giá đã thay đổi. Hãy tải lại.");
        }
        return daDoc;
    }

    private void moiNhat(BaoGia bg) {
        for (BaoGia khac : danhSach.values()) {
            if (khac.getMaBaoGiaGoc().equals(bg.getMaBaoGiaGoc()) && khac.getPhienBan() > bg.getPhienBan()) {
                throw new IllegalStateException("Đã có phiên bản mới hơn. Hãy mở đúng phiên bản hiện hành.");
            }
        }
    }

    private void kiemTraMaThaoTac(String token) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Thiếu mã thao tác.");
    }

    private void quyen(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ tiếp nhận được quản lý báo giá.");
        }
    }
}
