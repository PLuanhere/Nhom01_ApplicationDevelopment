package dao;

import dao.DichVuDAO;
import dao.BaoCaoKiemTraDAO;
import dao.PhuTungSuDungDAO;
import dao.PhieuTiepNhanDAO;
import entity.BaoGia;
import entity.BaoGia.ChiTietBaoGia;
import entity.BaoGia.TrangThaiBaoGia;
import entity.DichVu;
import entity.Xe;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhuTung;
import entity.PhieuTiepNhan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Kho báo giá trong phiên; kiểm tra quyền, nguồn kiểm tra và từng chuyển trạng thái. */
public final class BaoGiaDAO {
    private DichVuDAO dichVuDAO = new DichVuDAO();
    private final PhieuTiepNhanDAO phieuDAO;
    /** Đọc nguồn hiện tại khi lập/sửa/gửi, không tin báo cáo do UI tự chuyển vào hàm lưu. */
    private final BaoCaoKiemTraDAO nguonBaoCao;
    private final List<BaoGia> danhSach = new ArrayList<>();
    private final List<String[]> maThaoTac = new ArrayList<>();
    private int soThuTu = 1;
    private PhuTungSuDungDAO danhMucPhuTung;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, BaoCaoKiemTraDAO nguonBaoCao) {
        if (phieuDAO == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phieuDAO = phieuDAO;
        if (nguonBaoCao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nguonBaoCao = nguonBaoCao;
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, BaoCaoKiemTraDAO nguonBaoCao, PhuTungSuDungDAO danhMuc) {
        this(phieuDAO, nguonBaoCao);
        if (danhMuc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        danhMucPhuTung = danhMuc;
    }

    /** Kiểm tra danh muc trước khi xử lý. */
    private void kiemTraDanhMuc(TaiKhoan tk, List<ChiTietBaoGia> chiTiet) {
        if (chiTiet == null) {
            throw new IllegalArgumentException("Cần nhập hạng mục báo giá.");
        }
        List<DichVu> dichVuDangApDung = dichVuDAO.layChoBaoGia(tk);
        for (ChiTietBaoGia dong : chiTiet) {
            if (dong == null) {
                throw new IllegalArgumentException("Hạng mục không được rỗng.");
            }
            if (dong.getLoai().equals("Dịch vụ")) {
                boolean timThay = false;
                for (DichVu dichVu : dichVuDangApDung) {
                    if (dichVu.getMaDichVu().equals(dong.getMaDichVu())) {
                        timThay = true;
                        break;
                    }
                }
                if (!timThay || !dong.getMaPhuTung().isEmpty()) {
                    throw new IllegalArgumentException("Cần chọn dịch vụ đang áp dụng trong danh mục.");
                }
            } else if (dong.getLoai().equals("Phụ tùng")) {
                if (danhMucPhuTung == null || !dong.getMaDichVu().isEmpty()) {
                    throw new IllegalArgumentException("Thiếu nguồn hoặc mã phụ tùng không hợp lệ.");
                }
                PhuTung pt = danhMucPhuTung.docPhuTung(dong.getMaPhuTung());
                if (!pt.isHoatDong()) {
                    throw new IllegalArgumentException("Phụ tùng đã ngừng hoạt động, không lập báo giá mới.");
                }
                if (!pt.getTenPhuTung().equals(dong.getTenHangMuc()) || !pt.getDonViTinh().equals(dong.getDonVi())) {
                    throw new IllegalArgumentException("Tên/đơn vị phụ tùng không khớp danh mục.");
                }
                pt.kiemTraSoLuong(dong.getSoLuong());
            } else {
                throw new IllegalArgumentException("Loại hạng mục không hợp lệ.");
            }
        }
    }

    /** Chỉ trả báo cáo hoàn tất của đúng phiếu đã được kho tiếp nhận xác nhận. */
    public List<BaoCaoKiemTra> layCanCu(TaiKhoan tk) {
        quyen(tk);
        List<BaoCaoKiemTra> ketQua = new ArrayList<>();
        for (BaoCaoKiemTra bc : nguonBaoCao.layHoanTatNoiBo()) {
            if (bc.getTrangThai() != TrangThaiKiemTra.HOAN_TAT) {
                continue;
            }
            try {
                PhieuTiepNhan phieu = phieuDAO.timTheoMa(tk, bc.getPhieu().getMaPhieu());
                if (phieu == bc.getPhieu() && phieu.isDaXacNhan()) {
                    ketQua.add(bc);
                }
            } catch (IllegalArgumentException e) {
                // Bỏ qua chứng từ cũ không thuộc kho tiếp nhận hiện tại.
            }
        }
        return ketQua;
    }

    /** Tạo nhap cho giao diện. */
    public BaoGia taoNhap(TaiKhoan tk, String token, String maBaoCao, List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraMaThaoTac(token);
        kiemTraDanhMuc(tk, chiTiet);
        BaoCaoKiemTra bc = canCu(tk, maBaoCao);
        String daTao = timMaTheoLuot(token);
        if (daTao != null) {
            return kiemTraLap(tk, daTao, bc, chiTiet, hen, null);
        }
        for (BaoGia bg : danhSach) {
            if (bg.getBaoCao().getMaBaoCao().equals(maBaoCao)) {
                throw new IllegalStateException(
                        "Báo cáo đã có báo giá. Hãy mở báo giá hiện có hoặc tạo bản điều chỉnh.");
            }
        }
        String ma = String.format(Locale.ROOT, "BG-%04d-%04d", LocalDateTime.now().getYear(), soThuTu);
        BaoGia moi = new BaoGia(ma, ma, 1, bc, chiTiet, hen, tk.getTenDangNhap(), LocalDateTime.now());
        luuBaoGia(moi);
        maThaoTac.add(new String[] { token, ma });
        soThuTu++;
        return moi;
    }

    /** Chỉ cập nhật bản nháp đang đọc, chống ghi đè một đối tượng đã được màn khác thay đổi. */
    public BaoGia suaNhap(TaiKhoan tk, BaoGia daDoc, List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraDanhMuc(tk, chiTiet);
        BaoGia cu = banHienTai(daDoc);
        moiNhat(cu);
        if (cu.getTrangThai() != TrangThaiBaoGia.NHAP) {
            throw new IllegalStateException("Bản đã gửi/duyệt chỉ đọc; cần tạo phiên bản mới.");
        }
        BaoGia moi = new BaoGia(cu.getMaBaoGia(), cu.getMaBaoGiaGoc(), cu.getPhienBan(),
                canCu(tk, cu.getBaoCao().getMaBaoCao()), chiTiet, hen, cu.getNguoiLap(), cu.getNgayLap());
        luuBaoGia(moi);
        return moi;
    }

    /** Tạo dieu chinh cho giao diện. */
    public BaoGia taoDieuChinh(TaiKhoan tk, String token, BaoGia coSo, List<ChiTietBaoGia> chiTiet, LocalDateTime hen) {
        quyen(tk);
        kiemTraMaThaoTac(token);
        kiemTraDanhMuc(tk, chiTiet);
        if (coSo == null) {
            throw new NullPointerException("Cần bản cơ sở.");
        }

        BaoGia cu = timTheoMa(tk, coSo.getMaBaoGia());
        BaoCaoKiemTra bc = canCu(tk, cu.getBaoCao().getMaBaoCao());
        if ((timMaTheoLuot(token) != null)) {
            return kiemTraLap(tk, timMaTheoLuot(token), bc, chiTiet, hen, cu);
        }
        cu = banHienTai(coSo);
        moiNhat(cu);
        if (cu.getTrangThai() == TrangThaiBaoGia.NHAP) {
            throw new IllegalStateException("Bản hiện tại còn nháp; sửa bản nháp trước.");
        }
        int phienBan = cu.getPhienBan() + 1;
        String ma = cu.getMaBaoGiaGoc() + "-V" + phienBan;
        BaoGia moi = new BaoGia(ma, cu.getMaBaoGiaGoc(), phienBan, bc, chiTiet, hen, tk.getTenDangNhap(),
                LocalDateTime.now());
        luuBaoGia(moi);
        maThaoTac.add(new String[] { token, ma });
        return moi;
    }

    /** Gửi ở đây là ghi nhận đã trình khách, không tự gửi email/tin nhắn. */
    public BaoGia guiKhach(TaiKhoan tk, BaoGia daDoc) {
        quyen(tk);
        if (daDoc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }

        BaoGia cu = timTheoMa(tk, daDoc.getMaBaoGia());
        moiNhat(cu);
        if (cu.getTrangThai() == TrangThaiBaoGia.CHO_XAC_NHAN && daDoc.getTrangThai() == TrangThaiBaoGia.NHAP
                && cu.getNguoiGui().equals(tk.getTenDangNhap()) && cungNoiDung(cu, daDoc)) {
            return cu;
        }
        cu = banHienTai(daDoc);
        canCu(tk, cu.getBaoCao().getMaBaoCao());
        kiemTraDanhMuc(tk, cu.getChiTiet());
        BaoGia moi = cu.guiKhach(tk.getTenDangNhap(), LocalDateTime.now());
        luuBaoGia(moi);
        return moi;
    }

    /** Thực hiện ghi nhan phan hoi cho thành phần hiện tại. */
    public BaoGia ghiNhanPhanHoi(TaiKhoan tk, BaoGia daDoc, TrangThaiBaoGia quyetDinh, LocalDateTime lucPhanHoi,
            String kenh, String ghiChu) {
        quyen(tk);
        if (daDoc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }

        BaoGia cu = timTheoMa(tk, daDoc.getMaBaoGia());
        moiNhat(cu);
        if (cu.getTrangThai() == quyetDinh && cu.getNguoiGhiNhan().equals(tk.getTenDangNhap())
                && (cu.getThoiDiemPhanHoi() == null ? lucPhanHoi == null : cu.getThoiDiemPhanHoi().equals(lucPhanHoi))
                && cu.getKenhPhanHoi().equals(kenh == null ? "" : kenh.trim())
                && cu.getGhiChuPhanHoi().equals(ghiChu == null ? "" : ghiChu.trim())) {
            return cu;
        }
        cu = banHienTai(daDoc);
        BaoGia moi = cu.ghiNhanPhanHoi(quyetDinh, tk.getTenDangNhap(), lucPhanHoi, LocalDateTime.now(), kenh, ghiChu);
        luuBaoGia(moi);
        return moi;
    }

    /** Thực hiện tim theo ma cho thành phần hiện tại. */
    public BaoGia timTheoMa(TaiKhoan tk, String ma) {
        quyen(tk);
        BaoGia bg = timNoiBo(ma);
        if (bg == null) {
            throw new IllegalArgumentException("Báo giá không tồn tại.");
        }
        return bg;
    }

    /** Thực hiện tim kiem cho thành phần hiện tại. */
    public List<BaoGia> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiBaoGia trangThai) {
        quyen(tk);
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String tu = tuKhoa.trim().toLowerCase();
        String bien = Xe.khoaBienSo(tu);
        List<BaoGia> ketQua = new ArrayList<>();
        for (BaoGia bg : danhSach) {
            String chu = bg.getMaBaoGia() + " " + bg.getBaoCao().getPhieu().getTenKhach() + " " + bg.getBienSo();
            if ((trangThai == null || bg.getTrangThai() == trangThai) && (chu.toLowerCase().contains(tu)
                    || (!bien.isEmpty() && Xe.chuanHoaBienSo(bg.getBienSo()).contains(bien)))) {
                ketQua.add(bg);
            }
        }
        return ketQua;
    }

    /** Bản duyệt cao nhất của mỗi nhóm; phát sinh chờ duyệt không mở quyền cho dòng mới. */
    public List<BaoGia> layDaDuyet(TaiKhoan tk) {
        quyen(tk);
        List<BaoGia> ketQua = new ArrayList<>();
        for (BaoGia bg : danhSach) {
            if (!bg.isDaDongY()) {
                continue;
            }
            int viTri = -1;
            for (int i = 0; i < ketQua.size(); i++) {
                if (ketQua.get(i).getMaBaoGiaGoc().equals(bg.getMaBaoGiaGoc())) {
                    viTri = i;
                    break;
                }
            }
            if (viTri < 0) {
                ketQua.add(bg);
            } else if (ketQua.get(viTri).getPhienBan() < bg.getPhienBan()) {
                ketQua.set(viTri, bg);
            }
        }
        return ketQua;
    }

    /** Kiểm tra lap trước khi xử lý. */
    private BaoGia kiemTraLap(TaiKhoan tk, String ma, BaoCaoKiemTra bc, List<ChiTietBaoGia> chiTiet, LocalDateTime hen,
            BaoGia coSo) {
        BaoGia cu = timTheoMa(tk, ma);
        if ((coSo == null && cu.getPhienBan() != 1)
                || (coSo != null && (!cu.getMaBaoGiaGoc().equals(coSo.getMaBaoGiaGoc())
                        || cu.getPhienBan() != coSo.getPhienBan() + 1))) {
            throw new IllegalArgumentException("Mã thao tác thuộc bản báo giá khác.");
        }
        BaoGia noiDung = new BaoGia(cu.getMaBaoGia(), cu.getMaBaoGiaGoc(), cu.getPhienBan(), bc, chiTiet, hen,
                tk.getTenDangNhap(), LocalDateTime.now());
        if (!cungNoiDung(cu, noiDung) || !cu.getNguoiLap().equals(tk.getTenDangNhap())) {
            throw new IllegalArgumentException("Mã thao tác đã lưu nội dung khác.");
        }
        return cu;
    }

    /** Thực hiện cung noi dung cho thành phần hiện tại. */
    private boolean cungNoiDung(BaoGia a, BaoGia b) {
        if (!a.getBaoCao().getMaBaoCao().equals(b.getBaoCao().getMaBaoCao())
                || !a.getNgayHen().equals(b.getNgayHen())) {
            return false;
        }
        List<ChiTietBaoGia> x = a.getChiTiet(), y = b.getChiTiet();
        if (x.size() != y.size()) {
            return false;
        }
        for (int i = 0; i < x.size(); i++) {
            ChiTietBaoGia m = x.get(i), n = y.get(i);
            if (!m.getMaPhuTung().equals(n.getMaPhuTung()) || !m.getTenHangMuc().equals(n.getTenHangMuc())
                    || !m.getLoai().equals(n.getLoai()) || !m.getDonVi().equals(n.getDonVi())
                    || (m.getSoLuong() != n.getSoLuong()) || (m.getDonGia() != n.getDonGia())) {
                return false;
            }
        }
        return true;
    }

    /** Thực hiện can cu cho thành phần hiện tại. */
    private BaoCaoKiemTra canCu(TaiKhoan tk, String ma) {
        for (BaoCaoKiemTra bc : layCanCu(tk)) {
            if (bc.getMaBaoCao().equals(ma)) {
                return bc;
            }
        }
        throw new IllegalArgumentException("Báo cáo chưa hoàn tất hoặc chưa liên kết phiếu tiếp nhận trong phiên.");
    }

    /** Thực hiện ban hien tai cho thành phần hiện tại. */
    private BaoGia banHienTai(BaoGia daDoc) {
        if (daDoc == null || timNoiBo(daDoc.getMaBaoGia()) != daDoc) {
            throw new IllegalStateException("Báo giá đã thay đổi. Hãy tải lại.");
        }
        return daDoc;
    }

    /** Mở i nhat khi người dùng yêu cầu. */
    private void moiNhat(BaoGia bg) {
        for (BaoGia khac : danhSach) {
            if (khac.getMaBaoGiaGoc().equals(bg.getMaBaoGiaGoc()) && khac.getPhienBan() > bg.getPhienBan()) {
                throw new IllegalStateException("Đã có phiên bản mới hơn. Hãy mở đúng phiên bản hiện hành.");
            }
        }
    }

    /** Kiểm tra ma thao tac trước khi xử lý. */
    private void kiemTraMaThaoTac(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thao tác.");
        }
    }

    /** Thực hiện quyen cho thành phần hiện tại. */
    private void quyen(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chỉ tiếp nhận được quản lý báo giá.");
        }
    }

    /** Đọc nội bộ báo giá theo mã để các DAO dùng cùng chứng từ. */
    public BaoGia timNoiBo(String ma) {
        for (BaoGia bg : danhSach) {
            if (bg.getMaBaoGia().equals(ma)) {
                return bg;
            }
        }
        return null;
    }

    /** Thêm hoặc thay thế đúng phiên bản báo giá, giữ thứ tự danh sách. */
    private void luuBaoGia(BaoGia moi) {
        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getMaBaoGia().equals(moi.getMaBaoGia())) {
                danhSach.set(i, moi);
                return;
            }
        }
        danhSach.add(moi);
    }

    /** Tìm chứng từ đã lưu theo mã thao tác để tránh ghi lặp. */
    private String timMaTheoLuot(String maThaoTac) {
        for (String[] dong : this.maThaoTac) {
            if (dong[0].equals(maThaoTac)) {
                return dong[1];
            }
        }
        return null;
    }

    /** Nhận danh mục dịch vụ dùng chung với màn Quản lý. */
    public BaoGiaDAO(PhieuTiepNhanDAO phieuDAO, BaoCaoKiemTraDAO baoCaoDAO, PhuTungSuDungDAO phuTungDAO,
            DichVuDAO dichVuDAO) {
        this(phieuDAO, baoCaoDAO, phuTungDAO);
        if (dichVuDAO == null) {
            throw new IllegalArgumentException("Thiếu nguồn dịch vụ.");
        }
        this.dichVuDAO = dichVuDAO;
    }

    /** Đọc danh mục hiện tại để hộp thoại lưu đúng mã dịch vụ. */
    public List<DichVu> layDichVu(TaiKhoan tk) {
        quyen(tk);
        return dichVuDAO.layChoBaoGia(tk);
    }

    /** Kiểm tra mọi phiên bản báo giá đã lưu theo mã dịch vụ. */
    public boolean daSuDungDichVu(String ma) {
        for (BaoGia bg : danhSach) {
            for (ChiTietBaoGia dong : bg.getChiTiet()) {
                if (dong.getMaDichVu().equals(ma)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Đọc bản sao danh sách nội bộ cho DAO báo cáo, không mở quyền ghi. */
    public List<BaoGia> layBaoGiaBaoCao() {
        return new ArrayList<>(danhSach);
    }
}
