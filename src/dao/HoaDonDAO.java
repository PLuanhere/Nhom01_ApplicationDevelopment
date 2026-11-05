package dao;

import dao.PhieuSuaChuaDAO;
import entity.BanGiaoXe.CanCuBanGiao;
import entity.BaoGia;
import entity.BaoGia.ChiTietBaoGia;
import entity.HoaDon.HinhThucThanhToan;
import entity.HoaDon;
import dao.HoaDonDAO.PhieuChoThu;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuSuaChua;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Nguồn hóa đơn duy nhất; đọc lệnh hoàn thành và cung cấp căn cứ Bàn giao. */
public final class HoaDonDAO {
    private final List<HoaDon> danhSachHoaDon = new ArrayList<>();
    private final List<PhieuChoThu> phieuTaoSan = new ArrayList<>();
    private final PhieuSuaChuaDAO suaChuaDAO;
    private int soTiepTheo = 1;

    /** Tạo dữ liệu để xem thử màn Thu ngân độc lập. */
    public HoaDonDAO() {
        suaChuaDAO = null;
        phieuTaoSan.add(new PhieuChoThu("SC-2026-0111", "51H-135.79", "Trần Thị Bình", 800000,
                LocalDateTime.of(2026, 10, 5, 11, 0)));
        phieuTaoSan.add(new PhieuChoThu("SC-2026-0124", "51H-246.80", "Nguyễn Văn An", 1270000,
                LocalDateTime.of(2026, 10, 5, 15, 30)));
        danhSachHoaDon.add(new HoaDon("HD-2026-0108", "SC-2026-0108", "Nguyễn Văn An", "51H-246.80", 1270000,
                HinhThucThanhToan.CHUYEN_KHOAN, LocalDateTime.of(2026, 10, 5, 14, 30), "thungan", ""));
        danhSachHoaDon.add(new HoaDon("HD-2026-0104", "SC-2026-0104", "Lê Văn Cường", "51G-789.12", 1000000,
                HinhThucThanhToan.TIEN_MAT, LocalDateTime.of(2026, 10, 4, 10, 15), "thungan", ""));
    }

    /** Nhận nguồn sửa chữa dùng chung, không trộn chứng từ tạo sẵn vào luồng này. */
    public HoaDonDAO(PhieuSuaChuaDAO suaChuaDAO) {
        if (suaChuaDAO == null) {
            throw new IllegalArgumentException("Thiếu nguồn sửa chữa.");
        }
        this.suaChuaDAO = suaChuaDAO;
    }

    /** Đọc lệnh hoàn thành chưa có hóa đơn và số tiền từ báo giá của lệnh. */
    public List<PhieuChoThu> layPhieuChoThu(TaiKhoan taiKhoan) {
        kiemTraThuNgan(taiKhoan);
        List<PhieuChoThu> ketQua = new ArrayList<>();
        if (suaChuaDAO == null) {
            for (PhieuChoThu phieu : phieuTaoSan) {
                if (timHoaDonTheoLenh(phieu.getMaPhieu()) == null) {
                    ketQua.add(phieu);
                }
            }
        } else {
            for (PhieuSuaChua lenh : suaChuaDAO.layLenhHoanThanhNoiBo()) {
                if (timHoaDonTheoLenh(lenh.getMaPhieu()) != null) {
                    continue;
                }
                BaoGia baoGia = suaChuaDAO.docBaoGiaNoiBo(lenh.getMaBaoGia());
                if (baoGia == null || !baoGia.isDaDongY() || baoGia.getBaoCao() == null) {
                    throw new IllegalStateException("Lệnh hoàn thành thiếu báo giá/khách hàng: " + lenh.getMaPhieu());
                }
                ketQua.add(new PhieuChoThu(lenh.getMaPhieu(), lenh.getBienSo(),
                        baoGia.getBaoCao().getPhieu().getTenKhach(), baoGia.getTongTien(), lenh.getThoiDiemHoanTat()));
            }
        }
        return ketQua;
    }

    /** Tìm phiếu còn chờ thu theo mã. */
    public PhieuChoThu timPhieuTheoMa(TaiKhoan taiKhoan, String maPhieu) {
        if (maPhieu == null) {
            throw new IllegalArgumentException("Cần nhập mã phiếu.");
        }
        for (PhieuChoThu phieu : layPhieuChoThu(taiKhoan)) {
            if (phieu.getMaPhieu().equals(maPhieu.trim())) {
                return phieu;
            }
        }
        return null;
    }

    /** Trả bản sao danh sách hóa đơn đã thanh toán. */
    public List<HoaDon> layHoaDonDaThu(TaiKhoan taiKhoan) {
        kiemTraThuNgan(taiKhoan);
        List<HoaDon> ketQua = new ArrayList<>(danhSachHoaDon);
        for (int i = 0; i < ketQua.size() - 1; i++) {
            for (int j = i + 1; j < ketQua.size(); j++) {
                if (ketQua.get(i).getThoiDiemThanhToan().isBefore(ketQua.get(j).getThoiDiemThanhToan())) {
                    HoaDon cu = ketQua.get(i);
                    ketQua.set(i, ketQua.get(j));
                    ketQua.set(j, cu);
                }
            }
        }
        return ketQua;
    }

    /** Tìm hóa đơn bằng mã, trả null khi không tồn tại. */
    public HoaDon timHoaDonTheoMa(TaiKhoan taiKhoan, String maHoaDon) {
        kiemTraThuNgan(taiKhoan);
        for (HoaDon hoaDon : danhSachHoaDon) {
            if (hoaDon.getMaHoaDon().equals(maHoaDon)) {
                return hoaDon;
            }
        }
        return null;
    }

    /** Tìm hóa đơn theo mã, khách hàng, mã lệnh và hình thức thanh toán. */
    public List<HoaDon> timKiem(TaiKhoan taiKhoan, String tuKhoa, HinhThucThanhToan hinhThuc) {
        kiemTraThuNgan(taiKhoan);
        String khoa = tuKhoa == null ? "" : tuKhoa.trim().toLowerCase();
        List<HoaDon> ketQua = new ArrayList<>();
        for (HoaDon hoaDon : layHoaDonDaThu(taiKhoan)) {
            String noiDung = hoaDon.getMaHoaDon() + " " + hoaDon.getMaPhieuSuaChua() + " " + hoaDon.getTenKhachHang()
                    + " " + hoaDon.getBienSo();
            if (noiDung.toLowerCase().contains(khoa)
                    && (hinhThuc == null || hoaDon.getHinhThucThanhToan() == hinhThuc)) {
                ketQua.add(hoaDon);
            }
        }
        return ketQua;
    }

    /** Kiểm tra số tiền và phiếu hiện tại rồi lập đúng một hóa đơn tiền mặt. */
    public String xacNhanThanhToanTienMat(TaiKhoan taiKhoan, String maPhieu, long soTienNhan) {
        return xacNhanThanhToan(taiKhoan, maPhieu, soTienNhan, HinhThucThanhToan.TIEN_MAT);
    }

    /** Ghi nhận tiền thực nhận đã đối chiếu; từ chối phiếu chưa hoàn thành hoặc đã thu. */
    public String xacNhanThanhToan(TaiKhoan taiKhoan, String maPhieu, long soTienNhan, HinhThucThanhToan hinhThuc) {
        return ghiThanhToan(taiKhoan, maPhieu, soTienNhan, hinhThuc, "");
    }

    /** Chuyển khoản phải có mã tham chiếu và tiền đã đối chiếu đủ trước khi lưu. */
    public String xacNhanThanhToanChuyenKhoan(TaiKhoan tk, String maPhieu, long tienNhan, String maGiaoDich) {
        kiemTraThuNgan(tk);
        if (maGiaoDich == null || maGiaoDich.isBlank()) {
            throw new IllegalArgumentException("Nhập mã giao dịch chuyển khoản.");
        }
        return ghiThanhToan(tk, maPhieu, tienNhan, HinhThucThanhToan.CHUYEN_KHOAN, maGiaoDich.trim());
    }

    /** Lưu một lần vào nguồn hóa đơn chung sau khi kiểm tra phiếu và số tiền. */
    private String ghiThanhToan(TaiKhoan taiKhoan, String maPhieu, long soTienNhan, HinhThucThanhToan hinhThuc,
            String maGiaoDich) {
        kiemTraThuNgan(taiKhoan);
        if (hinhThuc == null) {
            throw new IllegalArgumentException("Cần chọn hình thức thanh toán.");
        }
        if (hinhThuc == HinhThucThanhToan.CHUYEN_KHOAN && (maGiaoDich == null || maGiaoDich.isBlank())) {
            throw new IllegalArgumentException("Nhập mã giao dịch chuyển khoản.");
        }
        PhieuChoThu phieu = timPhieuTheoMa(taiKhoan, maPhieu);
        if (phieu == null) {
            throw new IllegalStateException("Phiếu không tồn tại, chưa hoàn thành hoặc đã thanh toán.");
        }
        if (soTienNhan < phieu.getTongTien()) {
            throw new IllegalArgumentException("Số tiền nhận chưa đủ.");
        }
        LocalDateTime thoiDiem = LocalDateTime.now();
        String maHoaDon;
        do {
            maHoaDon = String.format("HD-%04d-%04d", thoiDiem.getYear(), soTiepTheo++);
        } while (timHoaDonTheoMa(taiKhoan, maHoaDon) != null);
        HoaDon hoaDon = new HoaDon(maHoaDon, phieu.getMaPhieu(), phieu.getTenKhach(), phieu.getBienSo(),
                phieu.getTongTien(), hinhThuc, thoiDiem, taiKhoan.getTenDangNhap(), maGiaoDich);
        danhSachHoaDon.add(hoaDon);
        return maHoaDon;
    }

    /** Đếm phiếu chờ thu từ cùng nguồn hiển thị danh sách. */
    public int demPhieuChoThu(TaiKhoan taiKhoan) {
        return layPhieuChoThu(taiKhoan).size();
    }

    /** Đếm hóa đơn đã thanh toán trong lần chạy ứng dụng. */
    public int demHoaDonDaThu(TaiKhoan taiKhoan) {
        return layHoaDonDaThu(taiKhoan).size();
    }

    /** Tính tiền đã thu trong khoảng ngày bao gồm hai đầu và kiểm tra tổng vượt giới hạn. */
    public long tinhTongDaThu(TaiKhoan taiKhoan, LocalDate tuNgay, LocalDate denNgay, HinhThucThanhToan hinhThuc) {
        kiemTraThuNgan(taiKhoan);
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        }
        long tong = 0;
        for (HoaDon hoaDon : danhSachHoaDon) {
            LocalDate ngay = hoaDon.getThoiDiemThanhToan().toLocalDate();
            if ((tuNgay == null || !ngay.isBefore(tuNgay)) && (denNgay == null || !ngay.isAfter(denNgay))
                    && (hinhThuc == null || hoaDon.getHinhThucThanhToan() == hinhThuc)) {
                if (Long.MAX_VALUE - tong < hoaDon.getTongTien()) {
                    throw new IllegalStateException("Tổng tiền vượt giới hạn.");
                }
                tong += hoaDon.getTongTien();
            }
        }
        return tong;
    }

    /** Tính tổng đã thu theo hình thức từ cùng danh sách hóa đơn. */
    public long tinhTongTheoHinhThuc(TaiKhoan taiKhoan, String hinhThuc) {
        return tinhTongDaThu(taiKhoan, null, null, HinhThucThanhToan.tuTenHienThi(hinhThuc));
    }

    /** Cung cấp căn cứ chỉ đọc cho Bàn giao; Tiếp nhận không được gọi thao tác thu tiền. */
    public CanCuBanGiao docCanCuBanGiao(String maLenh) {
        HoaDon hoaDon = timHoaDonTheoLenh(maLenh);
        if (hoaDon == null) {
            return null;
        }
        TaiKhoan nguoiThu = new TaiKhoan(hoaDon.getNguoiThucHien(), VaiTro.THU_NGAN);
        return new CanCuBanGiao(maLenh, hoaDon.getMaHoaDon(), hoaDon.getTongTien(), hoaDon.getTongTien(), nguoiThu,
                hoaDon.getThoiDiemThanhToan(), false, "");
    }

    /** Tìm hóa đơn liên kết nội bộ theo mã lệnh. */
    private HoaDon timHoaDonTheoLenh(String maLenh) {
        for (HoaDon hoaDon : danhSachHoaDon) {
            if (hoaDon.getMaPhieuSuaChua().equals(maLenh)) {
                return hoaDon;
            }
        }
        return null;
    }

    /** Kiểm tra vai trò và phiên trước thao tác của Thu ngân. */
    private void kiemTraThuNgan(TaiKhoan taiKhoan) {
        if (taiKhoan == null || taiKhoan.getVaiTro() != VaiTro.THU_NGAN) {
            throw new SecurityException("Chỉ Thu ngân được thao tác hóa đơn và thanh toán.");
        }
        taiKhoan.kiemTraPhien();
    }

    /** Đọc các dòng đã duyệt để đối chiếu; không dùng một bộ chi phí tạo sẵn khác. */
    public List<ChiTietBaoGia> layChiTietDoiChieu(TaiKhoan tk, String maPhieu) {
        PhieuChoThu phieu = timPhieuTheoMa(tk, maPhieu);
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu không còn chờ thu.");
        }
        if (suaChuaDAO == null) {
            List<ChiTietBaoGia> dong = new ArrayList<>();
            dong.add(new ChiTietBaoGia("Chi phí sửa chữa", "Dịch vụ", "lần", 1, phieu.getTongTien()));
            return dong;
        }
        for (PhieuSuaChua lenh : suaChuaDAO.layLenhHoanThanhNoiBo()) {
            if (lenh.getMaPhieu().equals(maPhieu)) {
                return new ArrayList<>(suaChuaDAO.docBaoGiaNoiBo(lenh.getMaBaoGia()).getChiTiet());
            }
        }
        throw new IllegalArgumentException("Không tìm thấy lệnh đã hoàn thành.");
    }

    /** Đọc lại các dòng của chứng từ đã thu để xem/in; không mở lại phiếu chờ. */
    public List<ChiTietBaoGia> layChiTietHoaDon(TaiKhoan tk, String maHoaDon) {
        HoaDon hd = timHoaDonTheoMa(tk, maHoaDon);
        if (hd == null) {
            throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
        }
        if (suaChuaDAO == null) {
            List<ChiTietBaoGia> dong = new ArrayList<>();
            dong.add(new ChiTietBaoGia("Chi phí sửa chữa", "Dịch vụ", "lần", 1, hd.getTongTien()));
            return dong;
        }
        for (PhieuSuaChua lenh : suaChuaDAO.layLenhHoanThanhNoiBo()) {
            if (lenh.getMaPhieu().equals(hd.getMaPhieuSuaChua())) {
                List<ChiTietBaoGia> dong = suaChuaDAO.docBaoGiaNoiBo(lenh.getMaBaoGia()).getChiTiet();
                long tong = 0;
                for (ChiTietBaoGia ct : dong) {
                    if (Long.MAX_VALUE - tong < ct.getThanhTien()) {
                        throw new IllegalStateException("Tổng chi phí vượt giới hạn.");
                    }
                    tong += ct.getThanhTien();
                }
                if (tong != hd.getTongTien()) {
                    throw new IllegalStateException("Chi tiết không khớp hóa đơn đã lưu.");
                }
                return new ArrayList<>(dong);
            }
        }
        throw new IllegalArgumentException("Thiếu lệnh gốc của hóa đơn.");
    }

    /** Tách tiền dịch vụ và phụ tùng của báo giá đã duyệt cho màn đối chiếu. */
    public long[] docChiPhi(TaiKhoan taiKhoan, String maPhieu) {
        PhieuChoThu phieu = timPhieuTheoMa(taiKhoan, maPhieu);
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu không còn chờ thu.");
        }
        if (suaChuaDAO == null) {
            return new long[] { phieu.getTongTien(), 0 };
        }
        for (PhieuSuaChua lenh : suaChuaDAO.layLenhHoanThanhNoiBo()) {
            if (lenh.getMaPhieu().equals(maPhieu)) {
                long dichVu = 0, phuTung = 0;
                for (ChiTietBaoGia dong : suaChuaDAO.docBaoGiaNoiBo(lenh.getMaBaoGia()).getChiTiet()) {
                    if (dong.getLoai().equals("Phụ tùng")) {
                        phuTung += dong.getThanhTien();
                    } else {
                        dichVu += dong.getThanhTien();
                    }
                }
                return new long[] { dichVu, phuTung };
            }
        }
        throw new IllegalArgumentException("Không tìm thấy lệnh sửa chữa.");
    }

    /** Đọc bản sao danh sách nội bộ cho DAO báo cáo, không mở quyền ghi. */
    public List<HoaDon> layHoaDonBaoCao() {
        return new ArrayList<>(danhSachHoaDon);
    }

    public static final class PhieuChoThu {
        private final String maPhieu;
        private final String bienSo;
        private final String tenKhach;
        private final long tongTien;
        private final LocalDateTime thoiDiemHoanTat;
    
        /** Tạo thông tin phiếu chờ thu từ lệnh và báo giá đã duyệt. */
        public PhieuChoThu(String maPhieu, String bienSo, String tenKhach, long tongTien, LocalDateTime thoiDiemHoanTat) {
            if (maPhieu == null || maPhieu.trim().isEmpty() || bienSo == null || tenKhach == null || tongTien < 0
                    || thoiDiemHoanTat == null) {
                throw new IllegalArgumentException("Thông tin phiếu chờ thu không hợp lệ.");
            }
            this.maPhieu = maPhieu.trim();
            this.bienSo = bienSo;
            this.tenKhach = tenKhach;
            this.tongTien = tongTien;
            this.thoiDiemHoanTat = thoiDiemHoanTat;
        }
    
        /** Trả về mã lệnh sửa chữa. */
        public String getMaPhieu() {
            return maPhieu;
        }
    
        /** Trả về biển số xe. */
        public String getBienSo() {
            return bienSo;
        }
    
        /** Trả về tên khách hàng. */
        public String getTenKhach() {
            return tenKhach;
        }
    
        /** Trả về số tiền phải thu, nguyên đồng. */
        public long getTongTien() {
            return tongTien;
        }
    
        /** Trả về thời điểm lệnh hoàn thành. */
        public LocalDateTime getThoiDiemHoanTat() {
            return thoiDiemHoanTat;
        }
    }
}
