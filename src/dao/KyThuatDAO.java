package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import entity.BaoCaoKiemTra;
import entity.BaoGia;
import entity.ChiTietBaoGia;
import entity.HangMucSuaChua;
import entity.PhuTung;
import entity.PhuTungSuDung;
import entity.PhieuSuaChua;
import entity.PhieuTiepNhan;
import entity.TaiKhoan;
import entity.TrangThaiPhatSinh;
import entity.TrangThaiSuaChua;
import entity.TrangThaiSuDung;
import entity.VaiTro;

// Giữ các DAO dùng chung trong một phiên đăng nhập kỹ thuật.
public final class KyThuatDAO {
    private final TaiKhoan taiKhoan;
    private final PhieuSuaChuaDAO suaChuaDAO = new PhieuSuaChuaDAO();
    private final BaoCaoKiemTraDAO kiemTraDAO = new BaoCaoKiemTraDAO();
    private final PhuTungSuDungDAO phuTungDAO = new PhuTungSuDungDAO();

    public KyThuatDAO(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chức năng dành cho kỹ thuật viên đã đăng nhập.");
        }
        this.taiKhoan = taiKhoan;
    }

    // Lọc công việc theo phân công, từ khóa và trạng thái thực tế.
    public List<PhieuSuaChua> docLenh(String tuKhoa, String trangThai) {
        List<PhieuSuaChua> ketQua = new ArrayList<PhieuSuaChua>();
        for (PhieuSuaChua lenh : suaChuaDAO.timKiem(taiKhoan, tuKhoa, "Tất cả")) {
            if ("Tất cả".equals(trangThai) || docTrangThai(lenh).equals(trangThai)) {
                ketQua.add(lenh);
            }
        }
        return ketQua;
    }

    // Đọc lại lệnh để các màn hình luôn nhận bản dữ liệu sau cập nhật.
    public PhieuSuaChua docLenh(String maLenh) {
        Optional<PhieuSuaChua> ketQua = suaChuaDAO.timTheoMa(taiKhoan, maLenh);
        if (!ketQua.isPresent()) {
            throw new IllegalArgumentException("Lệnh không thuộc phân công hoặc không tồn tại.");
        }
        return ketQua.get();
    }

    // Xác định trạng thái hiển thị từ lệnh và các hạng mục sửa chữa.
    public String docTrangThai(PhieuSuaChua lenh) {
        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            return "Hoàn thành";
        }
        for (HangMucSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            if (hangMuc.getTrangThai() == TrangThaiSuaChua.CHO_PHU_TUNG) {
                return "Chờ phụ tùng";
            }
        }
        return lenh.getTrangThai().getTenHienThi();
    }

    // Đọc phiếu tiếp nhận thuộc phạm vi của tài khoản đang đăng nhập.
    public List<PhieuTiepNhan> docPhieu() {
        return kiemTraDAO.layPhieu(taiKhoan);
    }

    // Tìm báo cáo theo bộ lọc và điều kiện phân quyền hiện có.
    public List<BaoCaoKiemTra> docBaoCao(String tuKhoa, String trangThai) {
        return kiemTraDAO.timKiem(taiKhoan, tuKhoa, trangThai);
    }

    // Đọc báo cáo đã có của phiếu tiếp nhận được chọn.
    public BaoCaoKiemTra docBaoCao(PhieuTiepNhan phieu) {
        return kiemTraDAO.timTheoPhieu(taiKhoan, phieu).orElse(null);
    }

    // Lưu báo cáo và kiểm tra đủ ba khu vực trước khi hoàn tất.
    public BaoCaoKiemTra luuBaoCao(BaoCaoKiemTra baoCao, boolean hoanTat) {
        if (hoanTat) {
            for (int viTri = 0; viTri < 3; viTri++) {
                if (baoCao.getNoiDung()[viTri + 3].isBlank()
                        || baoCao.getDeXuatTheoVung()[viTri].isBlank()) {
                    throw new IllegalArgumentException("Cần đủ kết quả và đề xuất của ba khu vực kiểm tra.");
                }
            }
        }
        return kiemTraDAO.luu(taiKhoan, baoCao, hoanTat);
    }

    // Xóa báo cáo nháp theo điều kiện bảo vệ dữ liệu của DAO kiểm tra.
    public void xoaBaoCao(BaoCaoKiemTra baoCao) {
        kiemTraDAO.xoa(taiKhoan, baoCao);
    }

    // Cập nhật tiến độ của đúng hạng mục được chọn.
    public void luuTienDo(String maLenh, int viTri, TrangThaiSuaChua trangThai, String ketQua) {
        suaChuaDAO.capNhatTienDo(taiKhoan, maLenh, viTri, trangThai, ketQua);
    }

    // Gửi đề xuất phát sinh và giữ trạng thái chờ lập báo giá bổ sung.
    public void guiPhatSinh(String maLenh, String hangMuc, String phuTung, String moTa) {
        if (hangMuc.isBlank() || moTa.isBlank()) {
            throw new IllegalArgumentException("Hạng mục phát sinh và mô tả là bắt buộc.");
        }
        suaChuaDAO.guiPhatSinh(taiKhoan, maLenh,
                new String[] {hangMuc.trim(), moTa.trim(), phuTung.trim(), "", "Chờ lập báo giá bổ sung"});
    }

    // Đọc hạng mục hợp lệ và chặn phát sinh chưa được khách duyệt.
    public HangMucSuaChua docHangMuc(String maLenh, int viTri) {
        PhieuSuaChua lenh = docLenh(maLenh);
        if (viTri < 0 || viTri >= lenh.getDanhSachHangMuc().size()) {
            throw new IllegalArgumentException("Chưa chọn hạng mục trong lệnh.");
        }
        HangMucSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);
        if (hangMuc.isPhatSinh() && (lenh.getPhatSinh() == null
                || lenh.getPhatSinh().getTrangThai() != TrangThaiPhatSinh.DA_DUYET)) {
            throw new IllegalStateException("Hạng mục phát sinh chưa được khách duyệt.");
        }
        return hangMuc;
    }

    // Đọc phụ tùng phù hợp với hạng mục và báo giá của lệnh hiện tại.
    public List<PhuTung> docPhuTung(String maLenh, int viTri) {
        List<PhuTung> ketQua = new ArrayList<PhuTung>();
        for (String ma : new String[] {"PT001", "PT005", "PT004"}) {
            if (docGioiHan(maLenh, viTri, ma).signum() > 0) {
                ketQua.add(phuTungDAO.docPhuTung(ma));
            }
        }
        return ketQua;
    }

    // Đối chiếu phụ tùng mẫu với hạng mục và số lượng trong báo giá đã duyệt.
    private BigDecimal docGioiHan(String maLenh, int viTri, String maPhuTung) {
        HangMucSuaChua hangMuc = docHangMuc(maLenh, viTri);
        String ten = hangMuc.getTenHangMuc().toLowerCase(Locale.ROOT);
        if ("PT004".equals(maPhuTung)) {
            return hangMuc.isPhatSinh() && ten.contains("rotuyn") ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        if (!(ten.contains("dầu") || ten.contains("lọc") || ten.contains("bảo dưỡng"))) {
            return BigDecimal.ZERO;
        }
        BaoGia baoGia = suaChuaDAO.layBaoGia(taiKhoan, maLenh);
        if (!baoGia.isDaDongY()) {
            return BigDecimal.ZERO;
        }
        for (ChiTietBaoGia dong : baoGia.getChiTiet()) {
            String tenDong = dong.getTenHangMuc().toLowerCase(Locale.ROOT);
            if ("Phụ tùng".equals(dong.getLoai())
                    && (("PT001".equals(maPhuTung) && tenDong.contains("dầu") && !tenDong.contains("lọc"))
                    || ("PT005".equals(maPhuTung) && tenDong.contains("lọc")))) {
                return dong.getSoLuong();
            }
        }
        return BigDecimal.ZERO;
    }

    // Đọc tồn kho từ cùng DAO phụ tùng trong phiên.
    public BigDecimal docTon(String maPhuTung) {
        return phuTungDAO.docTon(maPhuTung);
    }

    // Kiểm tra lệnh rồi ghi nhận phụ tùng và trừ kho một lần theo mã lượt.
    public synchronized PhuTungSuDung ghiNhan(String maLenh, int viTri, String maPhuTung,
            BigDecimal soLuong, String maLuot) {
        PhieuSuaChua lenh = docLenh(maLenh);
        HangMucSuaChua hangMuc = docHangMuc(maLenh, viTri);
        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành; không ghi nhận thêm phụ tùng.");
        }
        return phuTungDAO.ghiTheoHangMuc(taiKhoan, maLenh, hangMuc.getTenHangMuc(), maPhuTung,
                soLuong, docGioiHan(maLenh, viTri, maPhuTung), maLuot);
    }

    // Tổng hợp phụ tùng thực dùng của hạng mục theo các chứng từ đã ghi nhận.
    public String docThucDung(String maLenh, int viTri) {
        HangMucSuaChua hangMuc = docHangMuc(maLenh, viTri);
        StringBuilder ketQua = new StringBuilder();
        for (PhuTungSuDung dong : phuTungDAO.docDanhSach(taiKhoan, maLenh, TrangThaiSuDung.DA_GHI_NHAN)) {
            if (maLenh.equals(dong.getMaLenhSuaChua()) && (hangMuc.getTenHangMuc().equals(dong.getHangMuc())
                    || (dong.getHangMuc().isEmpty()
                    && docGioiHan(maLenh, viTri, dong.getPhuTung().getMaPhuTung()).signum() > 0))) {
                if (ketQua.length() > 0) {
                    ketQua.append("; ");
                }
                ketQua.append(dong.getSoLuong().stripTrailingZeros().toPlainString()).append(" ")
                        .append(dong.getPhuTung().getDonViTinh()).append(" ").append(dong.getPhuTung().getTenPhuTung());
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
        for (HangMucSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
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
        String hangMuc = docHangMuc(maLenh, viTri).getTenHangMuc();
        for (PhuTungSuDung dong : phuTungDAO.docDanhSach(taiKhoan, maLenh, TrangThaiSuDung.DA_GHI_NHAN)) {
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
        suaChuaDAO.hoanTat(taiKhoan, maLenh, new String[] {ketLuan.trim(), "", ""},
                new boolean[] {true, true, true, true});
    }
}