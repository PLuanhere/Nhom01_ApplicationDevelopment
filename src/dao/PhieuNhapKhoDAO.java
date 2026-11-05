package dao;

import entity.BaoGia.ChiTietBaoGia;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import dao.PhieuNhapKhoDAO.ChiTietNhapKho;
import dao.PhieuNhapKhoDAO.PhieuNhapKho;
import entity.PhuTung;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Lưu phiếu nhập nhiều mặt hàng và cập nhật tồn sau khi kiểm tra đủ dữ liệu. */
public final class PhieuNhapKhoDAO {
    private final PhuTungDAO kho;
    private final List<PhieuNhapKho> danhSach = new ArrayList<>();
    private int soTiepTheo = 1;

    /** Gắn nguồn phiếu nhập với đúng DAO đang quản lý kho. */
    PhieuNhapKhoDAO(PhuTungDAO kho) {
        this.kho = kho;
    }

    /** Sinh mã phiếu nhập kho dự kiến để hiển thị, chưa lưu hoặc chiếm mã. */
    public String maTiepTheo(TaiKhoan tk) {
        kiemTraQuyen(tk);
        return "NK-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", soTiepTheo);
    }

    /** Kiểm tra phiếu nhập, gộp lượng cùng mã và lưu phiếu cùng biến động tồn. */
    public PhieuNhapKho ghiNhan(TaiKhoan tk, String ma, String nguon, String chungTu, List<ChiTietNhapKho> chiTiet) {
        kiemTraQuyen(tk);
        if (!maTiepTheo(tk).equals(ma)) {
            throw new IllegalArgumentException("Mã lần nhập đã thay đổi. Kiểm tra mã mới rồi ghi nhận lại.");
        }
        PhieuNhapKho phieu = new PhieuNhapKho(ma, LocalDateTime.now(), nguon, chungTu, tk.getTenDangNhap(), chiTiet);
        String[] cacMa = new String[chiTiet.size()];
        double[] cacLuong = new double[chiTiet.size()];
        int soMatHang = 0;
        for (ChiTietNhapKho dong : phieu.getChiTiet()) {
            PhuTung pt = kho.timNoiBo(dong.getPhuTung().getMaPhuTung());
            if (!kho.cungThongTin(pt, dong.getPhuTung())) {
                throw new IllegalArgumentException("Danh mục đã thay đổi. Mở sửa dòng hàng rồi kiểm tra lại.");
            }
            if (!pt.isHoatDong()) {
                throw new IllegalArgumentException("Phụ tùng đã ngừng hoạt động: " + pt.getMaPhuTung());
            }
            int viTri = -1;
            for (int i = 0; i < soMatHang; i++) {
                if (cacMa[i].equals(pt.getMaPhuTung())) {
                    viTri = i;
                    break;
                }
            }
            if (viTri == -1) {
                cacMa[soMatHang] = pt.getMaPhuTung();
                cacLuong[soMatHang] = dong.getSoLuong();
                soMatHang++;
            } else {
                double tongLuong = cacLuong[viTri] + dong.getSoLuong();
                if (!(tongLuong < Long.MAX_VALUE / 1000.0)) {
                    throw new IllegalArgumentException("Tổng lượng nhập vượt giới hạn cho phép.");
                }
                cacLuong[viTri] = Math.round(tongLuong * 1000) / 1000.0;
            }
        }
        String[] maPhuTung = new String[soMatHang];
        double[] luongNhap = new double[soMatHang];
        for (int i = 0; i < soMatHang; i++) {
            maPhuTung[i] = cacMa[i];
            luongNhap[i] = cacLuong[i];
        }
        kho.ghiBienDong(maPhuTung, luongNhap, true, ma, tk, phieu.getNguonNhap(), phieu.getNgayNhap());
        danhSach.add(phieu);
        soTiepTheo++;
        return phieu;
    }

    /** Đọc danh sách phiếu nhập kho sau khi kiểm tra quyền truy cập. */
    public List<PhieuNhapKho> layDanhSach(TaiKhoan tk) {
        kiemTraQuyen(tk);
        return new ArrayList<>(danhSach);
    }

    /** Tìm phiếu nhập kho theo mã; báo lỗi nếu không tồn tại. */
    public PhieuNhapKho timTheoMa(TaiKhoan tk, String ma) {
        kiemTraQuyen(tk);
        for (PhieuNhapKho phieu : danhSach) {
            if (phieu.getMaPhieu().equals(ma)) {
                return phieu;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy lần nhập kho.");
    }

    /** Kiểm tra tài khoản, phiên đăng nhập và quyền Quản lý trước thao tác DAO. */
    private void kiemTraQuyen(TaiKhoan tk) {
        if (tk == null) {
            throw new SecurityException("Cần đăng nhập.");
        }
        tk.kiemTraPhien();
        if (tk.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được ghi nhận nhập kho.");
        }
    }

    public static final class ChiTietNhapKho {
        private final PhuTung phuTung;
        private final double soLuong;
        private final long donGia;
    
        /** Tạo dòng nhập, kiểm tra số lượng, đơn giá và giới hạn thành tiền. */
        public ChiTietNhapKho(PhuTung phuTung, double soLuong, long donGia) {
            if (phuTung == null) {
                throw new IllegalArgumentException("Cần chọn phụ tùng.");
            }
            this.phuTung = new PhuTung(phuTung);
            phuTung.kiemTraSoLuong(soLuong);
            this.soLuong = soLuong;
            this.donGia = donGia;
            if (donGia < 0) {
                throw new IllegalArgumentException("Đơn giá nhập không được âm.");
            }
            getThanhTien();
        }
    
        /** Trả về thông tin phụ tùng được lưu trên dòng dữ liệu. */
        public PhuTung getPhuTung() {
            return phuTung;
        }
    
        /** Trả về số lượng nhập. */
        public double getSoLuong() {
            return soLuong;
        }
    
        /** Trả về đơn giá đã lưu. */
        public long getDonGia() {
            return donGia;
        }
    
        /** Tính tiền dòng nhập và làm tròn đến đồng gần nhất, đồng thời kiểm tra tràn số. */
        public long getThanhTien() {
            return ChiTietBaoGia.tinhThanhTien(soLuong, donGia);
        }
    }

    public static final class PhieuNhapKho {
        private final String maPhieu;
        private final LocalDateTime ngayNhap;
        private final String nguonNhap;
        private final String chungTuNguon;
        private final String nguoiNhap;
        private final List<ChiTietNhapKho> chiTiet;
    
        /** Tạo phiếu nhập, sao chép các dòng và kiểm tra tổng tiền. */
        public PhieuNhapKho(String maPhieu, LocalDateTime ngayNhap, String nguonNhap, String chungTuNguon, String nguoiNhap,
                List<ChiTietNhapKho> chiTiet) {
            if (ngayNhap == null) {
                throw new IllegalArgumentException("Thiếu ngayNhap.");
            }
            if (chungTuNguon == null) {
                throw new IllegalArgumentException("Thiếu chungTuNguon.");
            }
            this.maPhieu = batBuoc(maPhieu, "Mã phiếu");
            this.ngayNhap = ngayNhap;
            this.nguonNhap = batBuoc(nguonNhap, "Nhà cung cấp / nguồn nhập");
            this.chungTuNguon = chungTuNguon.trim();
            this.nguoiNhap = batBuoc(nguoiNhap, "Người nhập");
            if (chiTiet == null) {
                throw new IllegalArgumentException("Cần ít nhất một mặt hàng.");
            }
            this.chiTiet = new ArrayList<>();
            for (ChiTietNhapKho dong : chiTiet) {
                if (dong == null) {
                    throw new IllegalArgumentException("Dòng nhập không được null.");
                }
                this.chiTiet.add(dong);
            }
            if (this.chiTiet.isEmpty()) {
                throw new IllegalArgumentException("Phiếu nhập cần ít nhất một mặt hàng.");
            }
            getTongTien();
        }
    
        /** Kiểm tra trường bắt buộc không rỗng và trả về nội dung đã bỏ khoảng trắng. */
        private static String batBuoc(String chu, String ten) {
            if (chu == null || chu.isBlank()) {
                throw new IllegalArgumentException(ten + " không được rỗng.");
            }
            return chu.trim();
        }
    
        /** Trả về mã phiếu nhập. */
        public String getMaPhieu() {
            return maPhieu;
        }
    
        /** Trả về thời điểm nhập kho. */
        public LocalDateTime getNgayNhap() {
            return ngayNhap;
        }
    
        /** Trả về nguồn hàng hoặc diễn giải nhập kho. */
        public String getNguonNhap() {
            return nguonNhap;
        }
    
        /** Trả về mã hóa đơn hoặc biên nhận nguồn. */
        public String getChungTuNguon() {
            return chungTuNguon;
        }
    
        /** Trả về tên tài khoản nhập kho. */
        public String getNguoiNhap() {
            return nguoiNhap;
        }
    
        /** Trả về bản sao danh sách mặt hàng trên phiếu nhập. */
        public List<ChiTietNhapKho> getChiTiet() {
            return new ArrayList<>(chiTiet);
        }
    
        /** Cộng thành tiền các dòng nhập đã làm tròn và kiểm tra tràn tổng. */
        public long getTongTien() {
            long tong = 0;
            for (ChiTietNhapKho dong : chiTiet) {
                if (Long.MAX_VALUE - tong < dong.getThanhTien()) {
                    throw new IllegalArgumentException("Tổng tiền vượt giới hạn cho phép.");
                }
                tong = tong + dong.getThanhTien();
            }
            return tong;
        }
    }
}
