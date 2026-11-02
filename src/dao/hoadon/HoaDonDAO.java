package dao.hoadon;

import dao.suachua.PhieuSuaChuaDAO;
import entity.bangiao.CanCuBanGiao;
import entity.baogia.BaoGia;
import entity.baogia.ChiTietBaoGia;
import entity.hoadon.HinhThucThanhToan;
import entity.hoadon.HoaDon;
import entity.hoadon.PhieuChoThu;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import entity.suachua.PhieuSuaChua;

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
        danhSachHoaDon.add(new HoaDon("HD-2026-0108", "SC-2026-0108", "Nguyễn Văn An", 1270000,
                HinhThucThanhToan.CHUYEN_KHOAN, LocalDateTime.of(2026, 10, 5, 14, 30), "thungan"));
        danhSachHoaDon.add(new HoaDon("HD-2026-0104", "SC-2026-0104", "Lê Văn Cường", 1000000,
                HinhThucThanhToan.TIEN_MAT, LocalDateTime.of(2026, 10, 4, 10, 15), "thungan"));
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
            String noiDung = hoaDon.getMaHoaDon() + " " + hoaDon.getMaPhieuSuaChua() + " " + hoaDon.getTenKhachHang();
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
        kiemTraThuNgan(taiKhoan);
        if (hinhThuc == null) {
            throw new IllegalArgumentException("Cần chọn hình thức thanh toán.");
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
        HoaDon hoaDon = new HoaDon(maHoaDon, phieu.getMaPhieu(), phieu.getTenKhach(), phieu.getTongTien(), hinhThuc,
                thoiDiem, taiKhoan.getTenDangNhap());
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
}
