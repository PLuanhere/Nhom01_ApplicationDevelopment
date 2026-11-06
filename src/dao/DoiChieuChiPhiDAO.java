package dao;

import java.util.ArrayList;
import java.util.List;

import entity.BaoGia.ChiTietBaoGia;
import dao.HoaDonDAO.PhieuChoThu;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.HoaDon;
import entity.ChiTietHoaDon;

/** Đọc chi phí của đúng phiếu chờ thu từ nguồn hóa đơn dùng chung. */
public final class DoiChieuChiPhiDAO {
    private final HoaDonDAO hoaDonDAO;
    private final TaiKhoan taiKhoan;

    /** Mở dữ liệu xem thử độc lập. */
    public DoiChieuChiPhiDAO() {
        this(new HoaDonDAO(), new TaiKhoan("NV007", VaiTro.THU_NGAN));
    }

    /** Dùng cùng nguồn và phiên Thu ngân với màn thanh toán. */
    public DoiChieuChiPhiDAO(HoaDonDAO hoaDonDAO, TaiKhoan taiKhoan) {
        if (hoaDonDAO == null || taiKhoan == null) {
            throw new IllegalArgumentException("Thiếu nguồn đối chiếu.");
        }
        this.hoaDonDAO = hoaDonDAO;
        this.taiKhoan = taiKhoan;
    }

    /** Đọc lại phiếu và chi phí, từ chối phiếu đã thu hoặc chưa hoàn thành. */
    public KetQuaDoiChieu layKetQuaDoiChieu(String maPhieu) {
        if (maPhieu == null) {
            throw new IllegalArgumentException("Cần chọn phiếu.");
        }
        PhieuChoThu phieu = hoaDonDAO.timPhieuTheoMa(taiKhoan, maPhieu.trim());
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu không còn đủ điều kiện đối chiếu.");
        }
        List<DongChiPhi> dong = new ArrayList<>();
        for (ChiTietBaoGia ct : hoaDonDAO.layChiTietDoiChieu(taiKhoan, phieu.getMaPhieu())) {
            dong.add(new DongChiPhi(ct.getTenHangMuc(), ct.getSoLuong() + " " + ct.getDonVi(), ct.getDonGia(),
                    ct.getThanhTien()));
        }
        KetQuaDoiChieu kq = new KetQuaDoiChieu(phieu.getMaPhieu(), phieu.getTenKhach(), phieu.getBienSo(),
                "Chưa phát hành hóa đơn", dong);
        if (kq.getTongThucTe() != phieu.getTongTien()) {
            throw new IllegalStateException("Chi phí không khớp tổng phải thu. Hãy kiểm tra lại chứng từ.");
        }
        return kq;
    }

    /** Tra cứu phiếu đủ điều kiện hoặc trả null khi không có. */
    public KetQuaDoiChieu timKetQua(String maPhieu) {
        if (!duDieuKienDoiChieu(maPhieu)) {
            return null;
        }
        return layKetQuaDoiChieu(maPhieu);
    }

    /** Kiểm tra điều kiện từ nguồn hiện tại, không giữ trạng thái riêng. */
    public boolean duDieuKienDoiChieu(String maPhieu) {
        if (maPhieu == null) {
            throw new IllegalArgumentException("Cần chọn phiếu.");
        }
        return hoaDonDAO.timPhieuTheoMa(taiKhoan, maPhieu.trim()) != null;
    }

    /** Lấy mã các phiếu đang chờ để người dùng lựa chọn. */
    public List<String> layDanhSachMaPhieu() {
        List<String> ketQua = new ArrayList<>();
        for (PhieuChoThu p : hoaDonDAO.layPhieuChoThu(taiKhoan)) {
            ketQua.add(p.getMaPhieu());
        }
        return ketQua;
    }

    /** Một dòng chi phí chỉ đọc; tiền dùng long nguyên đồng. */
    public static final class DongChiPhi {
        private final String hangMuc;
        private final String slDvt;
        private final long donGia;
        private final long thanhTien;

        /** Kiểm tra dữ liệu dòng trước khi đưa lên bảng. */
        public DongChiPhi(String hangMuc, String slDvt, long donGia, long thanhTien) {
            if (hangMuc == null || slDvt == null || donGia < 0 || thanhTien < 0) {
                throw new IllegalArgumentException("Chi phí không hợp lệ.");
            }
            this.hangMuc = hangMuc;
            this.slDvt = slDvt;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
        }

        /** Trả tên hạng mục. */
        public String getHangMuc() {
            return hangMuc;
        }

        /** Trả số lượng và đơn vị đã lưu trên chứng từ. */
        public String getSlDvt() {
            return slDvt;
        }

        /** Trả đơn giá nguyên đồng. */
        public long getDonGia() {
            return donGia;
        }

        /** Trả thành tiền đã làm tròn của dòng. */
        public long getThanhTien() {
            return thanhTien;
        }
    }

    /** Kết quả đối chiếu chỉ đọc của một phiếu cụ thể. */
    public static final class KetQuaDoiChieu {
        private final String maPhieu, tenKhach, bienSo, trangThaiThanhToan;
        private final List<DongChiPhi> danhSachChiPhi;
        private final long tongThucTe;

        /** Tính tổng từ từng dòng và kiểm tra tràn tiền. */
        public KetQuaDoiChieu(String ma, String ten, String bien, String trangThai, List<DongChiPhi> dong) {
            if (ma == null || ten == null || bien == null || trangThai == null || dong == null) {
                throw new IllegalArgumentException("Thiếu dữ liệu đối chiếu.");
            }
            maPhieu = ma;
            tenKhach = ten;
            bienSo = bien;
            trangThaiThanhToan = trangThai;
            danhSachChiPhi = new ArrayList<>(dong);
            List<ChiTietHoaDon> chiTiet = new ArrayList<>();
            int viTri = 0;
            for (DongChiPhi ct : dong) {
                if (ct == null) {
                    throw new IllegalArgumentException("Chi phí không được null.");
                }
                chiTiet.add(new ChiTietHoaDon("DOI-CHIEU", viTri++, ct.getHangMuc(), "Đối chiếu", "dòng", 1,
                        ct.getThanhTien()));
            }
            tongThucTe = HoaDon.tinhTongChiTiet(chiTiet);
        }

        /** Trả mã phiếu đang đối chiếu. */
        public String getMaPhieu() {
            return maPhieu;
        }

        /** Trả khách hàng của phiếu. */
        public String getTenKhach() {
            return tenKhach;
        }

        /** Trả biển số đã lưu trên phiếu. */
        public String getBienSo() {
            return bienSo;
        }

        /** Trả trạng thái phát hành hóa đơn. */
        public String getTrangThaiThanhToan() {
            return trangThaiThanhToan;
        }

        /** Trả bản sao danh sách dòng chi phí. */
        public List<DongChiPhi> getDanhSachChiPhi() {
            return new ArrayList<>(danhSachChiPhi);
        }

        /** Trả tổng các dòng hợp lệ. */
        public long getTongThucTe() {
            return tongThucTe;
        }

        /** Trả nhãn phiếu cho màn đối chiếu. */
        public String getNhanPhieu() {
            return maPhieu + " · Hoàn tất";
        }

        /** Trả nhãn khách và xe. */
        public String getNhanKhach() {
            return tenKhach + " · " + bienSo;
        }
    }
}
