package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Dữ liệu đối chiếu chi phí trước khi thu tiền.
 *
 * <p>Trả danh sách hạng mục chi phí hợp lệ của một lệnh sửa chữa đã hoàn tất,
 * tính tổng thực tế và kiểm tra điều kiện được phép đối chiếu.</p>
 *
 * <p>Hiện dùng in-memory; khi kết nối SQL Server thay phần khởi tạo bằng
 * truy vấn JDBC, hợp đồng phương thức giữ nguyên.</p>
 *
 * <p>Không phụ thuộc Swing, không gọi JOptionPane.</p>
 */
public final class DoiChieuChiPhiDAO {

    /** Một dòng chi phí hợp lệ trong lệnh sửa chữa. */
    public static final class DongChiPhi {
        private final String hangMuc;
        private final String slDvt;
        private final BigDecimal donGia;
        private final BigDecimal thanhTien;

        public DongChiPhi(String hangMuc, String slDvt,
                BigDecimal donGia, BigDecimal thanhTien) {
            this.hangMuc   = Objects.requireNonNull(hangMuc,   "Tên hạng mục không được null.");
            this.slDvt     = Objects.requireNonNull(slDvt,     "SL/ĐVT không được null.");
            this.donGia    = Objects.requireNonNull(donGia,    "Đơn giá không được null.");
            this.thanhTien = Objects.requireNonNull(thanhTien, "Thành tiền không được null.");
            if (donGia.signum() < 0 || thanhTien.signum() < 0) {
                throw new IllegalArgumentException("Đơn giá và thành tiền không được âm.");
            }
        }

        public String    getHangMuc()    { return hangMuc; }
        public String    getSlDvt()      { return slDvt; }
        public BigDecimal getDonGia()    { return donGia; }
        public BigDecimal getThanhTien() { return thanhTien; }
    }

    /** Kết quả đối chiếu trả về cho một phiếu. */
    public static final class KetQuaDoiChieu {
        private final String maPhieu;
        private final String tenKhach;
        private final String bienSo;
        private final String trangThaiThanhToan;
        private final List<DongChiPhi> danhSachChiPhi;
        private final BigDecimal tongThucTe;

        /** Tạo kết quả đối chiếu và tự tính tổng từ danh sách chi phí. */
        public KetQuaDoiChieu(String maPhieu, String tenKhach, String bienSo,
                String trangThaiThanhToan, List<DongChiPhi> danhSachChiPhi) {
            this.maPhieu            = Objects.requireNonNull(maPhieu);
            this.tenKhach           = Objects.requireNonNull(tenKhach);
            this.bienSo             = Objects.requireNonNull(bienSo);
            this.trangThaiThanhToan = Objects.requireNonNull(trangThaiThanhToan);
            this.danhSachChiPhi     = new ArrayList<>(Objects.requireNonNull(danhSachChiPhi));
            BigDecimal tong = BigDecimal.ZERO;
            for (DongChiPhi d : this.danhSachChiPhi) { tong = tong.add(d.getThanhTien()); }
            this.tongThucTe = tong;
        }

        public String           getMaPhieu()            { return maPhieu; }
        public String           getTenKhach()           { return tenKhach; }
        public String           getBienSo()             { return bienSo; }
        public String           getTrangThaiThanhToan() { return trangThaiThanhToan; }
        public List<DongChiPhi> getDanhSachChiPhi()     { return new ArrayList<>(danhSachChiPhi); }
        public BigDecimal       getTongThucTe()         { return tongThucTe; }
        public String           getNhanPhieu()          { return maPhieu + " · Hoàn tất"; }
        public String           getNhanKhach()          { return tenKhach + " · " + bienSo; }
    }

    private final Map<String, KetQuaDoiChieu> duLieuMock = new LinkedHashMap<>();

    /** Tạo DAO với dữ liệu mẫu khớp màn hình UI đề bài. */
    public DoiChieuChiPhiDAO() { khoiTao(); }

    private void khoiTao() {
        List<DongChiPhi> ct0108 = new ArrayList<>();
        ct0108.add(new DongChiPhi("Bảo dưỡng định kỳ", "1 công", new BigDecimal("300000"), new BigDecimal("300000")));
        ct0108.add(new DongChiPhi("Vệ sinh phanh",      "1 công", new BigDecimal("100000"), new BigDecimal("100000")));
        ct0108.add(new DongChiPhi("Dầu động cơ 5W-30", "4 lít",  new BigDecimal("180000"), new BigDecimal("720000")));
        ct0108.add(new DongChiPhi("Lọc dầu",            "1 cái",  new BigDecimal("150000"), new BigDecimal("150000")));
        duLieuMock.put("SC-2026-0108", new KetQuaDoiChieu("SC-2026-0108", "Nguyễn Văn An", "51H-246.80", "Chưa phát hành hóa đơn", ct0108));

        List<DongChiPhi> ct0111 = new ArrayList<>();
        ct0111.add(new DongChiPhi("Kiểm tra điều hòa", "1 công", new BigDecimal("300000"), new BigDecimal("300000")));
        ct0111.add(new DongChiPhi("Nạp gas R134a",      "1 kg",   new BigDecimal("500000"), new BigDecimal("500000")));
        duLieuMock.put("SC-2026-0111", new KetQuaDoiChieu("SC-2026-0111", "Trần Thị Bình", "51H-135.79", "Chưa phát hành hóa đơn", ct0111));
    }

    /**
     * Lấy kết quả đối chiếu chi phí cho một phiếu.
     * @param maPhieu mã phiếu sửa chữa, không được null
     * @return kết quả đối chiếu
     * @throws NullPointerException     nếu maPhieu là null
     * @throws IllegalArgumentException nếu phiếu không tồn tại hoặc chưa đủ điều kiện
     */
    public KetQuaDoiChieu layKetQuaDoiChieu(String maPhieu) {
        Objects.requireNonNull(maPhieu, "Mã phiếu không được null.");
        KetQuaDoiChieu kq = duLieuMock.get(maPhieu.trim());
        if (kq == null) {
            throw new IllegalArgumentException(
                    "Phiếu không tồn tại hoặc chưa đủ điều kiện đối chiếu: " + maPhieu.trim());
        }
        return kq;
    }

    /**
     * Tìm kết quả đối chiếu; trả Optional thay vì ném ngoại lệ.
     * @param maPhieu mã phiếu, không được null
     * @return Optional chứa kết quả hoặc rỗng
     * @throws NullPointerException nếu maPhieu là null
     */
    public Optional<KetQuaDoiChieu> timKetQua(String maPhieu) {
        Objects.requireNonNull(maPhieu, "Mã phiếu không được null.");
        return Optional.ofNullable(duLieuMock.get(maPhieu.trim()));
    }

    /**
     * Kiểm tra phiếu có đủ điều kiện đối chiếu không.
     * @param maPhieu mã phiếu, không được null
     * @return true nếu được phép đối chiếu
     * @throws NullPointerException nếu maPhieu là null
     */
    public boolean duDieuKienDoiChieu(String maPhieu) {
        Objects.requireNonNull(maPhieu, "Mã phiếu không được null.");
        return duLieuMock.containsKey(maPhieu.trim());
    }

    /**
     * Lấy danh sách mã phiếu hiện có — dùng cho kiểm thử.
     * @return danh sách mã phiếu (bản sao)
     */
    public List<String> layDanhSachMaPhieu() {
        return new ArrayList<>(duLieuMock.keySet());
    }
}
