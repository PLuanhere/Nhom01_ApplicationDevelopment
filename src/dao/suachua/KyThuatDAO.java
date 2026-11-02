package dao.suachua;

import dao.kiemtra.BaoCaoKiemTraDAO;
import dao.phutung.PhuTungSuDungDAO;
import entity.baogia.BaoGia;
import entity.baogia.ChiTietBaoGia;
import entity.kiemtra.BaoCaoKiemTra;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import entity.phutung.PhuTung;
import entity.phutung.PhuTungSuDung;
import entity.phutung.TrangThaiSuDung;
import entity.suachua.HangMucSuaChua;
import entity.suachua.PhieuSuaChua;
import entity.suachua.TrangThaiPhatSinh;
import entity.suachua.TrangThaiSuaChua;
import entity.tiepnhan.PhieuTiepNhan;

import java.util.ArrayList;
import java.util.List;

// Giữ các DAO dùng chung trong một phiên đăng nhập kỹ thuật.
public final class KyThuatDAO {
    private final TaiKhoan phien;
    private final boolean dungNguonChung;
    private final PhieuSuaChuaDAO suaChuaDAO;
    private final BaoCaoKiemTraDAO kiemTraDAO;
    private final PhuTungSuDungDAO phuTungDAO;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KyThuatDAO(TaiKhoan taiKhoan) {
        this(taiKhoan, new PhieuSuaChuaDAO(), new BaoCaoKiemTraDAO(), new PhuTungSuDungDAO(), false);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public KyThuatDAO(TaiKhoan phien, PhieuSuaChuaDAO suaChua, BaoCaoKiemTraDAO kiemTra, PhuTungSuDungDAO phuTung) {
        this(phien, suaChua, kiemTra, phuTung, true);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    private KyThuatDAO(TaiKhoan phien, PhieuSuaChuaDAO suaChua, BaoCaoKiemTraDAO kiemTra, PhuTungSuDungDAO phuTung,
            boolean dungNguonChung) {
        if (phien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phien = phien;
        phien();

        if (suaChua == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        suaChuaDAO = suaChua;
        if (kiemTra == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        kiemTraDAO = kiemTra;
        if (phuTung == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        phuTungDAO = phuTung;
        this.dungNguonChung = dungNguonChung;
    }

    // Kiểm tra lại vai trò và hiệu lực phiên trước khi truy cập dữ liệu.
    /** Thực hiện phien cho thành phần hiện tại. */
    private TaiKhoan phien() {
        TaiKhoan tk = phien;

        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Cần phiên kỹ thuật còn hiệu lực.");
        }

        tk.kiemTraPhien();
        return tk;
    }

    // Lọc công việc theo phân công, từ khóa và trạng thái thực tế.
    /** Đọc lenh từ dữ liệu hiện tại. */
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
    /** Đọc lenh từ dữ liệu hiện tại. */
    public PhieuSuaChua docLenh(String maLenh) {
        PhieuSuaChua ketQua = suaChuaDAO.timTheoMa(phien(), maLenh);
        if (!(ketQua != null)) {
            throw new IllegalArgumentException("Lệnh không thuộc phân công hoặc không tồn tại.");
        }
        return ketQua;
    }

    // Xác định trạng thái hiển thị từ lệnh và các hạng mục sửa chữa.
    /** Đọc trang thai từ dữ liệu hiện tại. */
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
    /** Đọc phieu từ dữ liệu hiện tại. */
    public List<PhieuTiepNhan> docPhieu() {
        return kiemTraDAO.layPhieu(phien());
    }

    // Tìm báo cáo theo bộ lọc và điều kiện phân quyền hiện có.
    /** Đọc bao cao từ dữ liệu hiện tại. */
    public List<BaoCaoKiemTra> docBaoCao(String tuKhoa, String trangThai) {
        return kiemTraDAO.timKiem(phien(), tuKhoa, trangThai);
    }

    // Đọc báo cáo đã có của phiếu tiếp nhận được chọn.
    /** Đọc bao cao từ dữ liệu hiện tại. */
    public BaoCaoKiemTra docBaoCao(PhieuTiepNhan phieu) {
        return kiemTraDAO.timTheoPhieu(phien(), phieu);
    }

    // Lưu báo cáo và kiểm tra đủ ba khu vực trước khi hoàn tất.
    /** Kiểm tra và lưu bao cao. */
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
    /** Thực hiện xoa bao cao cho thành phần hiện tại. */
    public void xoaBaoCao(BaoCaoKiemTra baoCao) {
        kiemTraDAO.xoa(phien(), baoCao);
    }

    // Cập nhật tiến độ của đúng hạng mục được chọn.
    /** Kiểm tra và lưu tien do. */
    public void luuTienDo(String maLenh, int viTri, TrangThaiSuaChua trangThai, String ketQua) {
        suaChuaDAO.capNhatTienDo(phien(), maLenh, viTri, trangThai, ketQua);
    }

    // Gửi đề xuất phát sinh và giữ trạng thái chờ lập báo giá bổ sung.
    /** Thực hiện gui phat sinh cho thành phần hiện tại. */
    public void guiPhatSinh(String maLenh, String hangMuc, String phuTung, String moTa) {
        if (hangMuc.isBlank() || moTa.isBlank()) {
            throw new IllegalArgumentException("Hạng mục phát sinh và mô tả là bắt buộc.");
        }
        suaChuaDAO.guiPhatSinh(phien(), maLenh,
                new String[] { hangMuc.trim(), moTa.trim(), phuTung.trim(), "", "Chờ lập báo giá bổ sung" });
    }

    // Đọc hạng mục hợp lệ và chặn phát sinh chưa được khách duyệt.
    /** Đọc hang muc từ dữ liệu hiện tại. */
    public HangMucSuaChua docHangMuc(String maLenh, int viTri) {
        PhieuSuaChua lenh = docLenh(maLenh);
        if (viTri < 0 || viTri >= lenh.getDanhSachHangMuc().size()) {
            throw new IllegalArgumentException("Chưa chọn hạng mục trong lệnh.");
        }
        HangMucSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);
        if (hangMuc.isPhatSinh()
                && (lenh.getPhatSinh() == null || lenh.getPhatSinh().getTrangThai() != TrangThaiPhatSinh.DA_DUYET)) {
            throw new IllegalStateException("Hạng mục phát sinh chưa được khách duyệt.");
        }
        return hangMuc;
    }

    // Đọc phụ tùng phù hợp với hạng mục và báo giá của lệnh hiện tại.
    /** Đọc phu tung từ dữ liệu hiện tại. */
    public List<PhuTung> docPhuTung(String maLenh, int viTri) {
        List<PhuTung> ketQua = new ArrayList<PhuTung>();
        for (PhuTung pt : phuTungDAO.docDanhMuc()) {
            if ((docGioiHan(maLenh, viTri, pt.getMaPhuTung()) > 0)) {
                ketQua.add(pt);
            }
        }
        return ketQua;
    }

    // Đối chiếu phụ tùng mẫu với hạng mục và số lượng trong báo giá đã duyệt.
    /** Đọc gioi han từ dữ liệu hiện tại. */
    private double docGioiHan(String maLenh, int viTri, String maPhuTung) {
        HangMucSuaChua hangMuc = docHangMuc(maLenh, viTri);
        if (dungNguonChung) {
            BaoGia bg = suaChuaDAO.layBaoGia(phien(), maLenh);
            if (viTri >= bg.getChiTiet().size()) {
                return 0.0;
            }
            ChiTietBaoGia ct = bg.getChiTiet().get(viTri);
            return ct.getMaPhuTung().equals(maPhuTung) ? ct.getSoLuong() : 0.0;
        }
        String ten = hangMuc.getTenHangMuc().toLowerCase();
        if ("PT004".equals(maPhuTung)) {
            return hangMuc.isPhatSinh() && ten.contains("rotuyn") ? 1.0 : 0.0;
        }
        if (!(ten.contains("dầu") || ten.contains("lọc") || ten.contains("bảo dưỡng"))) {
            return 0.0;
        }
        BaoGia baoGia = suaChuaDAO.layBaoGia(phien(), maLenh);
        if (!baoGia.isDaDongY()) {
            return 0.0;
        }
        for (ChiTietBaoGia dong : baoGia.getChiTiet()) {
            String tenDong = dong.getTenHangMuc().toLowerCase();
            if ("Phụ tùng".equals(dong.getLoai())
                    && (("PT001".equals(maPhuTung) && tenDong.contains("dầu") && !tenDong.contains("lọc"))
                            || ("PT005".equals(maPhuTung) && tenDong.contains("lọc")))) {
                return dong.getSoLuong();
            }
        }
        return 0.0;
    }

    // Đọc tồn kho từ cùng DAO phụ tùng trong phiên.
    /** Đọc ton từ dữ liệu hiện tại. */
    public double docTon(String maPhuTung) {
        phien();
        return phuTungDAO.docTon(maPhuTung);
    }

    // Kiểm tra lệnh rồi ghi nhận phụ tùng và trừ kho một lần theo mã lượt.
    /** Thực hiện ghi nhan cho thành phần hiện tại. */
    public PhuTungSuDung ghiNhan(String maLenh, int viTri, String maPhuTung, double soLuong, String maLuot) {
        PhieuSuaChua lenh = docLenh(maLenh);
        HangMucSuaChua hangMuc = docHangMuc(maLenh, viTri);
        if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành; không ghi nhận thêm phụ tùng.");
        }
        if (dungNguonChung) {
            return phuTungDAO.ghiTheoHangMuc(phien(), maLenh, viTri, maPhuTung, soLuong, maLuot);
        }
        return phuTungDAO.ghiTheoHangMuc(phien(), maLenh, hangMuc.getTenHangMuc(), maPhuTung, soLuong,
                docGioiHan(maLenh, viTri, maPhuTung), maLuot);
    }

    // Tổng hợp phụ tùng thực dùng của hạng mục theo các chứng từ đã ghi nhận.
    /** Đọc thuc dung từ dữ liệu hiện tại. */
    public String docThucDung(String maLenh, int viTri) {
        HangMucSuaChua hangMuc = docHangMuc(maLenh, viTri);
        StringBuilder ketQua = new StringBuilder();
        for (PhuTungSuDung dong : phuTungDAO.docDanhSach(phien(), maLenh, TrangThaiSuDung.DA_GHI_NHAN)) {
            if (maLenh.equals(dong.getMaLenhSuaChua())
                    && ((dungNguonChung ? (maLenh + "#" + viTri) : hangMuc.getTenHangMuc()).equals(dong.getHangMuc())
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
    /** Đọc ly do chua hoan tat từ dữ liệu hiện tại. */
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
    /** Thực hiện da ghi nhan cho thành phần hiện tại. */
    private boolean daGhiNhan(String maLenh, int viTri, String maPhuTung) {
        String hangMuc = dungNguonChung ? maLenh + "#" + viTri : docHangMuc(maLenh, viTri).getTenHangMuc();
        for (PhuTungSuDung dong : phuTungDAO.docDanhSach(phien(), maLenh, TrangThaiSuDung.DA_GHI_NHAN)) {
            if (maLenh.equals(dong.getMaLenhSuaChua()) && maPhuTung.equals(dong.getPhuTung().getMaPhuTung())
                    && (dong.getHangMuc().isEmpty() || hangMuc.equals(dong.getHangMuc()))) {
                return true;
            }
        }
        return false;
    }

    // Hoàn tất lệnh sau khi mọi hạng mục đã đạt kiểm tra cuối và đủ điều kiện.
    /** Thực hiện hoan tat cho thành phần hiện tại. */
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
