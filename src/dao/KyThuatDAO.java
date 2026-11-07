package dao;

import dao.BaoCaoKiemTraDAO;
import dao.PhuTungSuDungDAO;
import entity.BaoGia;
import entity.BaoGia.ChiTietBaoGia;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhuTung;
import dao.PhuTungSuDungDAO.PhuTungSuDung;
import dao.PhuTungSuDungDAO.TrangThaiSuDung;
import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiPhatSinh;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuTiepNhan;

import java.util.ArrayList;
import java.util.List;

// Điều phối các DAO kỹ thuật đã chuyển sang SQL Server.
public final class KyThuatDAO {
    private final TaiKhoan phien;
    private final String maPhien = java.util.UUID.randomUUID().toString();
    private final PhieuSuaChuaDAO suaChuaDAO;
    private final BaoCaoKiemTraDAO kiemTraDAO;
    private final PhuTungSuDungDAO phuTungDAO;
    // Khởi tạo các DAO kỹ thuật cùng đọc một database.
    public KyThuatDAO(TaiKhoan taiKhoan) {
        this(taiKhoan, new PhieuSuaChuaDAO(), new BaoCaoKiemTraDAO(), new PhuTungSuDungDAO());
    }

    // Nhận các DAO dùng chung do MainFrame truyền vào và kiểm tra phiên đăng nhập.
    public KyThuatDAO(TaiKhoan phien, PhieuSuaChuaDAO suaChua,
            BaoCaoKiemTraDAO kiemTra, PhuTungSuDungDAO phuTung) {
        this.phien = java.util.Objects.requireNonNull(phien, "Thiếu phiên đăng nhập.");
        suaChuaDAO = java.util.Objects.requireNonNull(suaChua, "Thiếu nguồn sửa chữa.");
        kiemTraDAO = java.util.Objects.requireNonNull(kiemTra, "Thiếu nguồn kiểm tra.");
        phuTungDAO = java.util.Objects.requireNonNull(phuTung, "Thiếu nguồn phụ tùng.");
        phien();
    }

    // Kiểm tra lại vai trò và hiệu lực phiên trước khi truy cập dữ liệu.
    private TaiKhoan phien() {
        TaiKhoan tk = phien;

        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Cần phiên kỹ thuật còn hiệu lực.");
        }

        tk.kiemTraPhien();
        return tk;
    }

    // Lọc công việc theo phân công, từ khóa và trạng thái thực tế.
    public List<PhieuSuaChua> docLenh(String tuKhoa, String trangThai) {
        List<PhieuSuaChua> ketQua = new ArrayList<PhieuSuaChua>();
        for (PhieuSuaChua lenh : suaChuaDAO.timKiem(phien(), tuKhoa, "Tất cả")) {
            if ("Tất cả".equals(trangThai) || docTrangThai(lenh).equals(trangThai)) {
                ketQua.add(lenh);
            }
        }
        return ketQua;
    }

    // Đọc lại lệnh để các màn hình luôn nhận bản dữ liệu sau cập nhật.
    public PhieuSuaChua docLenh(String maLenh) {
        PhieuSuaChua ketQua = suaChuaDAO.timTheoMa(phien(), maLenh);
        if (ketQua == null) {
            throw new IllegalArgumentException("Lệnh không thuộc phân công hoặc không tồn tại.");
        }
        return ketQua;
    }

    // Xác định trạng thái hiển thị từ lệnh và các hạng mục sửa chữa.
    public String docTrangThai(PhieuSuaChua lenh) {
        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            return "Hoàn thành";
        }
        for (ChiTietSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            if (hangMuc.getTrangThai() == TrangThaiSuaChua.CHO_PHU_TUNG) {
                return "Chờ phụ tùng";
            }
        }
        return lenh.getTrangThai().getTenHienThi();
    }

    // Đọc phiếu tiếp nhận thuộc phạm vi của tài khoản đang đăng nhập.
    public List<PhieuTiepNhan> docPhieu() {
        return kiemTraDAO.layPhieu(phien());
    }

    // Tìm báo cáo theo bộ lọc và điều kiện phân quyền hiện có.
    public List<BaoCaoKiemTra> docBaoCao(String tuKhoa, String trangThai) {
        return kiemTraDAO.timKiem(phien(), tuKhoa, trangThai);
    }

    // Đọc báo cáo đã có của phiếu tiếp nhận được chọn.
    public BaoCaoKiemTra docBaoCao(PhieuTiepNhan phieu) {
        return kiemTraDAO.timTheoPhieu(phien(), phieu);
    }

    // Lưu báo cáo và kiểm tra đủ ba khu vực trước khi hoàn tất.
    public BaoCaoKiemTra luuBaoCao(BaoCaoKiemTra baoCao, boolean hoanTat) {
        if (hoanTat) {
            for (int viTri = 0; viTri < 3; viTri++) {
                if (baoCao.getNoiDung()[viTri + 3].isBlank() || baoCao.getDeXuatTheoVung()[viTri].isBlank()) {
                    throw new IllegalArgumentException("Cần đủ kết quả và đề xuất của ba khu vực kiểm tra.");
                }
            }
        }
        return kiemTraDAO.luu(phien(), baoCao, hoanTat);
    }

    // Xóa báo cáo nháp theo điều kiện bảo vệ dữ liệu của DAO kiểm tra.
    public void xoaBaoCao(BaoCaoKiemTra baoCao) {
        kiemTraDAO.xoa(phien(), baoCao);
    }

    // Cập nhật tiến độ của đúng hạng mục được chọn.
    public void luuTienDo(String maLenh, int viTri, TrangThaiSuaChua trangThai, String ketQua) {
        suaChuaDAO.capNhatTienDo(phien(), maLenh, viTri, trangThai, ketQua);
    }

    // Gửi đề xuất phát sinh và giữ trạng thái chờ lập báo giá bổ sung.
    public void guiPhatSinh(String maLenh, String hangMuc, String phuTung, String moTa) {
        if (hangMuc.isBlank() || moTa.isBlank()) {
            throw new IllegalArgumentException("Hạng mục phát sinh và mô tả là bắt buộc.");
        }
        suaChuaDAO.guiPhatSinh(phien(), maLenh,
                new String[] { hangMuc.trim(), moTa.trim(), phuTung.trim(), "", "Chờ lập báo giá bổ sung" });
    }

    // Đọc hạng mục hợp lệ và chặn phát sinh chưa được khách duyệt.
    public ChiTietSuaChua docHangMuc(String maLenh, int viTri) {
        PhieuSuaChua lenh = docLenh(maLenh);
        if (viTri < 0 || viTri >= lenh.getDanhSachHangMuc().size()) {
            throw new IllegalArgumentException("Chưa chọn hạng mục trong lệnh.");
        }
        ChiTietSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);
        if (hangMuc.isPhatSinh()
                && (lenh.getPhatSinh() == null || lenh.getPhatSinh().getTrangThai() != TrangThaiPhatSinh.DA_DUYET)) {
            throw new IllegalStateException("Hạng mục phát sinh chưa được khách duyệt.");
        }
        return hangMuc;
    }

    // Đọc phụ tùng phù hợp với hạng mục và báo giá của lệnh hiện tại.
    public List<PhuTung> docPhuTung(String maLenh, int viTri) {
        List<PhuTung> ketQua = new ArrayList<PhuTung>();
        for (PhuTung pt : phuTungDAO.docDanhMuc()) {
            if ((docGioiHan(maLenh, viTri, pt.getMaPhuTung()) > 0)) {
                ketQua.add(pt);
            }
        }
        return ketQua;
    }

    // Đối chiếu mã phụ tùng và vị trí hạng mục với báo giá đã duyệt.
    private double docGioiHan(String maLenh, int viTri, String maPhuTung) {
        docHangMuc(maLenh, viTri);
        BaoGia bg = suaChuaDAO.layBaoGia(phien(), maLenh);
        if (!bg.isDaDongY() || viTri >= bg.getChiTiet().size()) {
            return 0;
        }
        ChiTietBaoGia ct = bg.getChiTiet().get(viTri);
        return ct.getMaPhuTung().equals(maPhuTung) ? ct.getSoLuong() : 0;
    }

    // Đọc tồn kho từ cùng DAO phụ tùng trong phiên.
    public double docTon(String maPhuTung) {
        phien();
        return phuTungDAO.docTon(maPhuTung);
    }

    // Kiểm tra lệnh rồi ghi nhận phụ tùng và trừ kho một lần theo mã lượt.
    public PhuTungSuDung ghiNhan(String maLenh, int viTri, String maPhuTung, double soLuong, String maLuot) {
        phien();
        if (maLuot == null || maLuot.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thao tác.");
        }
        return phuTungDAO.ghiTheoHangMuc(phien(), maLenh, viTri, maPhuTung, soLuong,
                maPhien + ":" + maLuot);
    }

    // Tổng hợp phụ tùng thực dùng của hạng mục theo các chứng từ đã ghi nhận.
    public String docThucDung(String maLenh, int viTri) {
        docHangMuc(maLenh, viTri);
        StringBuilder ketQua = new StringBuilder();
        for (PhuTungSuDung dong : phuTungDAO.docDanhSach(phien(), maLenh, TrangThaiSuDung.DA_GHI_NHAN)) {
            if (maLenh.equals(dong.getMaLenhSuaChua())
                    && ((maLenh + "#" + viTri).equals(dong.getHangMuc())
                            || (dong.getHangMuc().isEmpty()
                                    && (docGioiHan(maLenh, viTri, dong.getPhuTung().getMaPhuTung()) > 0)))) {
                if (ketQua.length() > 0) {
                    ketQua.append("; ");
                }
                ketQua.append(String.valueOf(dong.getSoLuong())).append(" ").append(dong.getPhuTung().getDonViTinh())
                        .append(" ").append(dong.getPhuTung().getTenPhuTung());
            }
        }
        return ketQua.length() == 0 ? "—" : ketQua.toString();
    }

    // Trả lý do chặn hoàn tất dựa trên hạng mục, phát sinh và phụ tùng thực dùng.
    public String docLyDoChuaHoanTat(String maLenh) {
        PhieuSuaChua lenh = docLenh(maLenh);
        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            return "Lệnh đã hoàn thành.";
        }
        if (lenh.getPhatSinh() != null && lenh.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.CHO_DUYET) {
            return "Còn phát sinh chưa được khách duyệt; giữ trạng thái chờ xử lý.";
        }
        int chuaXong = 0;
        for (ChiTietSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            if (hangMuc.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || hangMuc.getKetQua().isBlank()) {
                chuaXong++;
            }
        }
        if (chuaXong > 0) {
            return "Không thể xác nhận hoàn tất: còn " + chuaXong + " hạng mục chưa hoàn thành.";
        }
        for (int viTri = 0; viTri < lenh.getDanhSachHangMuc().size(); viTri++) {
            for (PhuTung phuTung : docPhuTung(maLenh, viTri)) {
                if (!daGhiNhan(maLenh, viTri, phuTung.getMaPhuTung())) {
                    return "Chưa ghi nhận thực dùng " + phuTung.getTenPhuTung() + " của hạng mục " + (viTri + 1) + ".";
                }
            }
        }
        return "";
    }

    // Kiểm tra chứng từ đã ghi nhận của đúng phụ tùng và hạng mục trong lệnh.
    private boolean daGhiNhan(String maLenh, int viTri, String maPhuTung) {
        String hangMuc = maLenh + "#" + viTri;
        for (PhuTungSuDung dong : phuTungDAO.docDanhSach(phien(), maLenh, TrangThaiSuDung.DA_GHI_NHAN)) {
            if (maLenh.equals(dong.getMaLenhSuaChua()) && maPhuTung.equals(dong.getPhuTung().getMaPhuTung())
                    && (dong.getHangMuc().isEmpty() || hangMuc.equals(dong.getHangMuc()))) {
                return true;
            }
        }
        return false;
    }

    // Hoàn tất lệnh sau khi mọi hạng mục đã đạt kiểm tra cuối và đủ điều kiện.
    public void hoanTat(String maLenh, boolean[] daDat, String ketLuan) {
        String lyDo = docLyDoChuaHoanTat(maLenh);
        if (!lyDo.isEmpty()) {
            throw new IllegalStateException(lyDo);
        }
        if (daDat.length != docLenh(maLenh).getDanhSachHangMuc().size() || ketLuan.isBlank()) {
            throw new IllegalArgumentException("Cần đủ kiểm tra cuối và kết luận hoàn tất.");
        }
        for (int viTri = 0; viTri < daDat.length; viTri++) {
            if (!daDat[viTri]) {
                throw new IllegalArgumentException("Chưa đánh dấu Đạt cho mọi hạng mục.");
            }
            if (!docPhuTung(maLenh, viTri).isEmpty() && "—".equals(docThucDung(maLenh, viTri))) {
                throw new IllegalStateException("Chưa ghi nhận phụ tùng của hạng mục " + (viTri + 1) + ".");
            }
        }
        suaChuaDAO.hoanTat(phien(), maLenh, new String[] { ketLuan.trim(), "", "" },
                new boolean[] { true, true, true, true });
    }
}
