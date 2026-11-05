package dao;

import java.util.Arrays;
import entity.TaiKhoan;
import dao.LenhPhuTungDAO.LenhPhuTung;
import dao.PhuTungSuDungDAO.LuongPhuTung;
import entity.PhuTung;
import dao.PhuTungSuDungDAO.PhuTungSuDung;
import dao.PhuTungSuDungDAO.TrangThaiSuDung;

import java.util.ArrayList;
import java.util.List;

public final class LenhPhuTungDAO {
    private final PhuTungSuDungDAO phuTungDAO;
    private final List<LenhPhuTung> cacLenh = new ArrayList<>();

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public LenhPhuTungDAO(PhuTungSuDungDAO phuTungDAO, TaiKhoan taiKhoan) {
        this.phuTungDAO = phuTungDAO;
        for (String maLenh : phuTungDAO.docLenhDaDuyet(taiKhoan)) {
            boolean baoGiaV2 = PhuTungSuDungDAO.layLuong(phuTungDAO.docPhamViDuyet(taiKhoan, maLenh), "PT004") > 0;
            cacLenh.add(new LenhPhuTung(maLenh, "51H-246.80", "Chung Ngô Minh Hoàng", baoGiaV2));
        }
    }

    /** Đọc thông tin lệnh được phân công. */
    // Kiểm tra quyền bằng DAO hiện có rồi trả bản sao lệnh cho giao diện.
    public LenhPhuTung docLenh(TaiKhoan taiKhoan, String maLenh) {
        phuTungDAO.docPhamViDuyet(taiKhoan, maLenh);
        LenhPhuTung lenh = timLenh(maLenh);
        if (lenh == null) {
            throw new IllegalArgumentException("Không tìm thấy lệnh sửa chữa.");
        }
        return new LenhPhuTung(lenh);
    }

    /** Cập nhật kết quả công việc của lệnh mẫu. */
    // Lưu tiến độ đã nhập và đồng bộ trạng thái chờ phụ tùng của lệnh.
    public void capNhatTienDo(TaiKhoan taiKhoan, String maLenh, String trangThai, String ketQua) {
        kiemTraDangSua(taiKhoan, maLenh);
        if (!"Đang sửa chữa".equals(trangThai) && !"Chờ phụ tùng".equals(trangThai) && !"Hoàn tất".equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái công việc không hợp lệ.");
        }
        if (ketQua == null || ketQua.trim().isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập kết quả thực hiện.");
        }
        LenhPhuTung lenh = timLenh(maLenh);
        lenh.setTrangThaiCongViec(trangThai);
        lenh.setKetQuaCongViec(ketQua.trim());
        lenh.setTrangThai("Chờ phụ tùng".equals(trangThai) ? "Chờ phụ tùng" : "Đang sửa chữa");
    }

    /** Lưu đề xuất phát sinh trong phiên mẫu. */
    // Ghi nhận phát sinh đang chờ, không tự duyệt hoặc thêm phụ tùng vào phạm vi được phép dùng.
    public void luuPhatSinh(TaiKhoan taiKhoan, String maLenh, String[] noiDung) {
        kiemTraDangSua(taiKhoan, maLenh);
        if (noiDung == null || noiDung.length != 5) {
            throw new IllegalArgumentException("Thông tin phát sinh không hợp lệ.");
        }
        String[] duLieu = new String[5];
        for (int viTri = 0; viTri < noiDung.length; viTri++) {
            duLieu[viTri] = noiDung[viTri] == null ? "" : noiDung[viTri].trim();
            if (viTri < 3 && duLieu[viTri].isEmpty()) {
                throw new IllegalArgumentException("Hãy nhập đủ ba trường phát sinh bắt buộc.");
            }
        }
        timLenh(maLenh).setPhatSinh(duLieu);
    }

    /** Chuyển lệnh sang chờ phụ tùng. */
    // Đánh dấu lệnh chờ phụ tùng và giữ nguyên toàn bộ lượng tồn.
    public void chuyenChoPhuTung(TaiKhoan taiKhoan, String maLenh, PhuTungSuDung banNhap) {
        kiemTraDangSua(taiKhoan, maLenh);
        if (banNhap != null) {
            if (!maLenh.equals(banNhap.getMaLenhSuaChua())) {
                throw new IllegalArgumentException("Bản nháp không thuộc lệnh đang mở.");
            }
            phuTungDAO.chuyenChoPhuTung(taiKhoan, banNhap);
        }
        timLenh(maLenh).setTrangThai("Chờ phụ tùng");
        timLenh(maLenh).setTrangThaiCongViec("Chờ phụ tùng");
    }

    /** Xác nhận lượng thực dùng bằng DAO phụ tùng hiện có. */
    // Kiểm tra cả nhóm trước khi tạo nháp bổ sung và xác nhận trừ kho đúng một lần.
    public PhuTungSuDung xacNhanThucDung(TaiKhoan taiKhoan, String maLenh, PhuTungSuDung banNhap,
            List<LuongPhuTung> luongDung, String ghiChu) {
        kiemTraDangSua(taiKhoan, maLenh);
        {
            String maDauTien = null;
            for (LuongPhuTung dongLuong : luongDung) {
                double soLuong = dongLuong.getSoLuong();
                if ((soLuong < 0)) {
                    throw new IllegalArgumentException("Số lượng không được âm hoặc để trống.");
                }
                if ((soLuong == 0)) {

                    continue;
                }
                PhuTung phuTung = phuTungDAO.docPhuTung(dongLuong.getMaPhuTung());
                phuTung.kiemTraSoLuong(soLuong);
                if ((soLuong > phuTungDAO.docTon(dongLuong.getMaPhuTung()))) {
                    throw new IllegalArgumentException("Số lượng " + dongLuong.getMaPhuTung() + " vượt tồn kho.");
                }
                if ((soLuong > phuTungDAO.docLuongConLai(taiKhoan, maLenh, dongLuong.getMaPhuTung()))) {
                    throw new IllegalArgumentException(
                            "Số lượng " + dongLuong.getMaPhuTung() + " vượt phạm vi còn được duyệt.");
                }
                if (maDauTien == null) {
                    maDauTien = dongLuong.getMaPhuTung();
                }
            }
            if (maDauTien == null) {
                throw new IllegalArgumentException("Hãy nhập ít nhất một phụ tùng có lượng thực dùng lớn hơn 0.");
            }
            if (banNhap == null) {
                for (PhuTungSuDung nhap : phuTungDAO.docDanhSach(taiKhoan, maLenh, TrangThaiSuDung.NHAP)) {
                    double soLuong = PhuTungSuDungDAO.layLuong(luongDung, nhap.getPhuTung().getMaPhuTung());
                    if (maLenh.equals(nhap.getMaLenhSuaChua()) && (soLuong > 0)) {
                        banNhap = nhap;
                        break;

                    }
                }
                if (banNhap == null) {
                    banNhap = phuTungDAO.luuNhap(taiKhoan, null, maLenh, maDauTien,
                            PhuTungSuDungDAO.layLuong(luongDung, maDauTien), ghiChu);
                }
            }
            if (!maLenh.equals(banNhap.getMaLenhSuaChua())) {
                throw new IllegalArgumentException("Bản nháp không thuộc lệnh đang xác nhận.");
            }
            PhuTungSuDung ketQua = phuTungDAO.xacNhanThucDung(taiKhoan, banNhap, luongDung, ghiChu);
            LenhPhuTung lenh = timLenh(maLenh);
            lenh.setTrangThai("Đang sửa chữa");
            if ("Chờ phụ tùng".equals(lenh.getTrangThaiCongViec())) {
                lenh.setTrangThaiCongViec("Đang sửa chữa");
            }
            return ketQua;
        }
    }

    /** Hoàn tất lệnh sau khi đối chiếu công việc và lượng thực dùng. */
    // Chỉ chuyển lệnh sang hoàn thành khi đủ kiểm tra, có kết quả và đã ghi nhận phụ tùng.
    public void hoanTatLenh(TaiKhoan taiKhoan, String maLenh, boolean[] daKiemTra, String[] noiDung) {
        kiemTraDangSua(taiKhoan, maLenh);
        LenhPhuTung lenh = timLenh(maLenh);
        if (daKiemTra == null || daKiemTra.length != 4) {
            throw new IllegalArgumentException("Thiếu nội dung kiểm tra cuối.");
        }
        for (boolean daDat : daKiemTra) {
            if (!daDat) {
                throw new IllegalArgumentException("Hãy kiểm tra và xác nhận đủ bốn nội dung.");
            }
        }
        if (!"Hoàn tất".equals(lenh.getTrangThaiCongViec())) {
            throw new IllegalStateException("Công việc chưa hoàn tất. Hãy quay lại lệnh và cập nhật kết quả.");
        }
        if (!lenh.getPhatSinh()[0].isEmpty()) {
            throw new IllegalStateException("Lệnh còn phát sinh đang chờ xử lý; chưa được xác nhận hoàn thành.");
        }
        if (noiDung == null || noiDung.length != 3 || noiDung[0] == null || noiDung[0].trim().isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập kết luận hoàn tất.");
        }
        List<PhuTungSuDung> daDung = phuTungDAO.docDanhSach(taiKhoan, maLenh, TrangThaiSuDung.DA_GHI_NHAN);
        for (LuongPhuTung dongPhamVi : phuTungDAO.docPhamViDuyet(taiKhoan, maLenh)) {
            String maPhuTung = dongPhamVi.getMaPhuTung();
            boolean daGhiNhan = false;
            for (PhuTungSuDung banGhi : daDung) {
                if (maLenh.equals(banGhi.getMaLenhSuaChua()) && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())) {
                    daGhiNhan = true;

                    break;
                }
            }
            if (!daGhiNhan) {
                throw new IllegalStateException("Chưa ghi nhận thực dùng cho " + maPhuTung + ".");
            }
        }
        lenh.setKetQuaHoanTat(new String[] { noiDung[0].trim(), noiDung[1].trim(), noiDung[2].trim() });
        lenh.setTrangThai("Hoàn thành");
    }

    // Chặn cập nhật lệnh đã hoàn thành và kiểm tra quyền của tài khoản hiện tại.
    /** Kiểm tra dang sua trước khi xử lý. */
    private void kiemTraDangSua(TaiKhoan taiKhoan, String maLenh) {
        LenhPhuTung lenh = docLenh(taiKhoan, maLenh);
        if ("Hoàn thành".equals(lenh.getTrangThai())) {
            throw new IllegalStateException("Lệnh đã hoàn thành; không được ghi nhận hoặc sửa tiếp.");
        }
    }

    /** Tìm hồ sơ lệnh phụ tùng theo mã bằng vòng lặp. */
    private LenhPhuTung timLenh(String maLenh) {
        for (LenhPhuTung lenh : cacLenh) {
            if (lenh.getMaLenh().equals(maLenh)) {
                return lenh;
            }
        }
        return null;
    }

    public static final class LenhPhuTung {
        private final String maLenh;
        private final String bienSo;
        private final String kyThuatVien;
        private final boolean baoGiaV2;
        private String trangThai;
        private String trangThaiCongViec;
        private String ketQuaCongViec;
        private String[] phatSinh;
        private String[] ketQuaHoanTat;
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        public LenhPhuTung(String maLenh, String bienSo, String kyThuatVien, boolean baoGiaV2) {
            this.maLenh = maLenh;
            this.bienSo = bienSo;
            this.kyThuatVien = kyThuatVien;
            this.baoGiaV2 = baoGiaV2;
            trangThai = "Đang sửa chữa";
            trangThaiCongViec = "Đang sửa chữa";
            ketQuaCongViec = "Đã thay dầu/lọc; đang kiểm tra phanh.";
            phatSinh = new String[] { "", "", "", "", "" };
            ketQuaHoanTat = new String[] { "", "", "" };
        }
    
        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        public LenhPhuTung(LenhPhuTung lenh) {
            maLenh = lenh.maLenh;
            bienSo = lenh.bienSo;
            kyThuatVien = lenh.kyThuatVien;
            baoGiaV2 = lenh.baoGiaV2;
            trangThai = lenh.trangThai;
            trangThaiCongViec = lenh.trangThaiCongViec;
            ketQuaCongViec = lenh.ketQuaCongViec;
            phatSinh = Arrays.copyOf(lenh.phatSinh, lenh.phatSinh.length);
            ketQuaHoanTat = Arrays.copyOf(lenh.ketQuaHoanTat, lenh.ketQuaHoanTat.length);
        }
    
        /** Trả về ma lenh. */
        public String getMaLenh() {
            return maLenh;
        }
    
        /** Trả về bien so. */
        public String getBienSo() {
            return bienSo;
        }
    
        /** Trả về ky thuat vien. */
        public String getKyThuatVien() {
            return kyThuatVien;
        }
    
        /** Cho biết bao gia v2. */
        public boolean isBaoGiaV2() {
            return baoGiaV2;
        }
    
        /** Trả về trang thai. */
        public String getTrangThai() {
            return trangThai;
        }
    
        /** Kiểm tra và cập nhật trang thai. */
        public void setTrangThai(String trangThai) {
            this.trangThai = trangThai;
        }
    
        /** Trả về trang thai cong viec. */
        public String getTrangThaiCongViec() {
            return trangThaiCongViec;
        }
    
        /** Kiểm tra và cập nhật trang thai cong viec. */
        public void setTrangThaiCongViec(String trangThaiCongViec) {
            this.trangThaiCongViec = trangThaiCongViec;
        }
    
        /** Trả về ket qua cong viec. */
        public String getKetQuaCongViec() {
            return ketQuaCongViec;
        }
    
        /** Kiểm tra và cập nhật ket qua cong viec. */
        public void setKetQuaCongViec(String ketQuaCongViec) {
            this.ketQuaCongViec = ketQuaCongViec;
        }
    
        /** Trả về phat sinh. */
        public String[] getPhatSinh() {
            return Arrays.copyOf(phatSinh, phatSinh.length);
        }
    
        /** Kiểm tra và cập nhật phat sinh. */
        public void setPhatSinh(String[] phatSinh) {
            this.phatSinh = Arrays.copyOf(phatSinh, phatSinh.length);
        }
    
        /** Trả về ket qua hoan tat. */
        public String[] getKetQuaHoanTat() {
            return Arrays.copyOf(ketQuaHoanTat, ketQuaHoanTat.length);
        }
    
        /** Kiểm tra và cập nhật ket qua hoan tat. */
        public void setKetQuaHoanTat(String[] ketQuaHoanTat) {
            this.ketQuaHoanTat = Arrays.copyOf(ketQuaHoanTat, ketQuaHoanTat.length);
        }
    }
}
