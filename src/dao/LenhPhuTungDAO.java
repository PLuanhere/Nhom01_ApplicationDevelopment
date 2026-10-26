package dao;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import entity.LenhPhuTung;
import entity.PhuTung;
import entity.PhuTungSuDung;
import entity.TaiKhoan;
import entity.TrangThaiSuDung;


public final class LenhPhuTungDAO {
    private final PhuTungSuDungDAO phuTungDAO;
    private final Map<String, LenhPhuTung> cacLenh = new LinkedHashMap<String, LenhPhuTung>();

    public LenhPhuTungDAO(PhuTungSuDungDAO phuTungDAO, TaiKhoan taiKhoan) {
        this.phuTungDAO = phuTungDAO;
        for (String maLenh : phuTungDAO.docLenhDaDuyet(taiKhoan)) {
            boolean baoGiaV2 = phuTungDAO.docPhamViDuyet(taiKhoan, maLenh).containsKey("PT004");
            cacLenh.put(maLenh, new LenhPhuTung(maLenh, "51H-246.80", "Chung Ngô Minh Hoàng", baoGiaV2));
        }
    }

    /**
     * Đọc thông tin lệnh được phân công.
     * @param taiKhoan tài khoản đã đăng nhập
     * @param maLenh mã lệnh cần đọc
     * @return bản sao thông tin lệnh
     */
    // Kiểm tra quyền bằng DAO hiện có rồi trả bản sao lệnh cho giao diện.
    public synchronized LenhPhuTung docLenh(TaiKhoan taiKhoan, String maLenh) {
        phuTungDAO.docPhamViDuyet(taiKhoan, maLenh);
        LenhPhuTung lenh = cacLenh.get(maLenh);
        if (lenh == null) {
            throw new IllegalArgumentException("Không tìm thấy lệnh sửa chữa.");
        }
        return new LenhPhuTung(lenh);
    }

    /**
     * Cập nhật kết quả công việc của lệnh mẫu.
     * @param taiKhoan tài khoản kỹ thuật
     * @param maLenh mã lệnh
     * @param trangThai trạng thái công việc
     * @param ketQua kết quả thực hiện
     */
    // Lưu tiến độ đã nhập và đồng bộ trạng thái chờ phụ tùng của lệnh.
    public synchronized void capNhatTienDo(TaiKhoan taiKhoan, String maLenh, String trangThai, String ketQua) {
        kiemTraDangSua(taiKhoan, maLenh);
        if (!"Đang sửa chữa".equals(trangThai) && !"Chờ phụ tùng".equals(trangThai)
                && !"Hoàn tất".equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái công việc không hợp lệ.");
        }
        if (ketQua == null || ketQua.trim().isEmpty()) {
            throw new IllegalArgumentException("Hãy nhập kết quả thực hiện.");
        }
        LenhPhuTung lenh = cacLenh.get(maLenh);
        lenh.setTrangThaiCongViec(trangThai);
        lenh.setKetQuaCongViec(ketQua.trim());
        lenh.setTrangThai("Chờ phụ tùng".equals(trangThai) ? "Chờ phụ tùng" : "Đang sửa chữa");
    }

    /**
     * Lưu đề xuất phát sinh trong phiên mẫu.
     * @param taiKhoan tài khoản kỹ thuật
     * @param maLenh mã lệnh
     * @param noiDung lần lượt là hư hỏng, nguyên nhân, đề xuất, thời gian và ghi chú
     */
    // Ghi nhận phát sinh đang chờ, không tự duyệt hoặc thêm phụ tùng vào phạm vi được phép dùng.
    public synchronized void luuPhatSinh(TaiKhoan taiKhoan, String maLenh, String[] noiDung) {
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
        cacLenh.get(maLenh).setPhatSinh(duLieu);
    }

    /**
     * Chuyển lệnh sang chờ phụ tùng.
     * @param taiKhoan tài khoản kỹ thuật
     * @param maLenh mã lệnh
     * @param banNhap bản nháp hiện tại, có thể null khi đi vào từ màn lệnh
     */
    // Đánh dấu lệnh chờ phụ tùng và giữ nguyên toàn bộ lượng tồn.
    public synchronized void chuyenChoPhuTung(TaiKhoan taiKhoan, String maLenh, PhuTungSuDung banNhap) {
        kiemTraDangSua(taiKhoan, maLenh);
        if (banNhap != null) {
            if (!maLenh.equals(banNhap.getMaLenhSuaChua())) {
                throw new IllegalArgumentException("Bản nháp không thuộc lệnh đang mở.");
            }
            phuTungDAO.chuyenChoPhuTung(taiKhoan, banNhap);
        }
        cacLenh.get(maLenh).setTrangThai("Chờ phụ tùng");
        cacLenh.get(maLenh).setTrangThaiCongViec("Chờ phụ tùng");
    }

    /**
     * Xác nhận lượng thực dùng bằng DAO phụ tùng hiện có.
     * @param taiKhoan tài khoản kỹ thuật
     * @param maLenh mã lệnh
     * @param banNhap nháp được chọn, hoặc null khi mở trực tiếp từ lệnh
     * @param luongDung lượng thực tế theo mã phụ tùng
     * @param ghiChu ghi chú ghi nhận
     * @return bản ghi đã xác nhận
     */
    // Kiểm tra cả nhóm trước khi tạo nháp bổ sung và xác nhận trừ kho đúng một lần.
    public synchronized PhuTungSuDung xacNhanThucDung(TaiKhoan taiKhoan, String maLenh,
            PhuTungSuDung banNhap, Map<String, BigDecimal> luongDung, String ghiChu) {
        kiemTraDangSua(taiKhoan, maLenh);
        synchronized (phuTungDAO) {
            String maDauTien = null;
            for (Map.Entry<String, BigDecimal> dong : luongDung.entrySet()) {
                BigDecimal soLuong = dong.getValue();
                if (soLuong == null || soLuong.signum() < 0) {
                    throw new IllegalArgumentException("Số lượng không được âm hoặc để trống.");
                }
                if (soLuong.signum() == 0) {
                    continue;
                }
                PhuTung phuTung = phuTungDAO.docPhuTung(dong.getKey());
                phuTung.kiemTraSoLuong(soLuong);
                if (soLuong.compareTo(phuTungDAO.docTon(dong.getKey())) > 0) {
                    throw new IllegalArgumentException("Số lượng " + dong.getKey() + " vượt tồn kho.");
                }
                if (soLuong.compareTo(phuTungDAO.docLuongConLai(taiKhoan, maLenh, dong.getKey())) > 0) {
                    throw new IllegalArgumentException("Số lượng " + dong.getKey() + " vượt phạm vi còn được duyệt.");
                }
                if (maDauTien == null) {
                    maDauTien = dong.getKey();
                }
            }
            if (maDauTien == null) {
                throw new IllegalArgumentException("Hãy nhập ít nhất một phụ tùng có lượng thực dùng lớn hơn 0.");
            }
            if (banNhap == null) {
                for (PhuTungSuDung nhap : phuTungDAO.docDanhSach(taiKhoan, maLenh, TrangThaiSuDung.NHAP)) {
                    BigDecimal soLuong = luongDung.get(nhap.getPhuTung().getMaPhuTung());
                    if (maLenh.equals(nhap.getMaLenhSuaChua()) && soLuong != null && soLuong.signum() > 0) {
                        banNhap = nhap;
                        break;
                    }
                }
                if (banNhap == null) {
                    banNhap = phuTungDAO.luuNhap(taiKhoan, null, maLenh, maDauTien,
                            luongDung.get(maDauTien), ghiChu);
                }
            }
            if (!maLenh.equals(banNhap.getMaLenhSuaChua())) {
                throw new IllegalArgumentException("Bản nháp không thuộc lệnh đang xác nhận.");
            }
            PhuTungSuDung ketQua = phuTungDAO.xacNhanThucDung(taiKhoan, banNhap, luongDung, ghiChu);
            LenhPhuTung lenh = cacLenh.get(maLenh);
            lenh.setTrangThai("Đang sửa chữa");
            if ("Chờ phụ tùng".equals(lenh.getTrangThaiCongViec())) {
                lenh.setTrangThaiCongViec("Đang sửa chữa");
            }
            return ketQua;
        }
    }

    /**
     * Hoàn tất lệnh sau khi đối chiếu công việc và lượng thực dùng.
     * @param taiKhoan tài khoản kỹ thuật
     * @param maLenh mã lệnh
     * @param daKiemTra bốn xác nhận kiểm tra cuối
     * @param noiDung kết luận, bảo hành và khuyến nghị
     */
    // Chỉ chuyển lệnh sang hoàn thành khi đủ kiểm tra, có kết quả và đã ghi nhận phụ tùng.
    public synchronized void hoanTatLenh(TaiKhoan taiKhoan, String maLenh,
            boolean[] daKiemTra, String[] noiDung) {
        kiemTraDangSua(taiKhoan, maLenh);
        LenhPhuTung lenh = cacLenh.get(maLenh);
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
        for (String maPhuTung : phuTungDAO.docPhamViDuyet(taiKhoan, maLenh).keySet()) {
            boolean daGhiNhan = false;
            for (PhuTungSuDung banGhi : daDung) {
                if (maLenh.equals(banGhi.getMaLenhSuaChua())
                        && maPhuTung.equals(banGhi.getPhuTung().getMaPhuTung())) {
                    daGhiNhan = true;
                    break;
                }
            }
            if (!daGhiNhan) {
                throw new IllegalStateException("Chưa ghi nhận thực dùng cho " + maPhuTung + ".");
            }
        }
        lenh.setKetQuaHoanTat(new String[] {noiDung[0].trim(), noiDung[1].trim(), noiDung[2].trim()});
        lenh.setTrangThai("Hoàn thành");
    }

    // Chặn cập nhật lệnh đã hoàn thành và kiểm tra quyền của tài khoản hiện tại.
    private void kiemTraDangSua(TaiKhoan taiKhoan, String maLenh) {
        LenhPhuTung lenh = docLenh(taiKhoan, maLenh);
        if ("Hoàn thành".equals(lenh.getTrangThai())) {
            throw new IllegalStateException("Lệnh đã hoàn thành; không được ghi nhận hoặc sửa tiếp.");
        }
    }
}