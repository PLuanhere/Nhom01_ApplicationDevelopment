package dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import entity.BaoGia;
import entity.ChiTietBaoGia;
import entity.HangMucSuaChua;
import entity.PhatSinhSuaChua;
import entity.PhieuSuaChua;
import entity.TaiKhoan;
import entity.TrangThaiPhatSinh;
import entity.TrangThaiSuaChua;
import entity.VaiTro;

// Nghiệp vụ sửa chữa trên dữ liệu mẫu trong bộ nhớ
public final class PhieuSuaChuaDAO {
    private final List<PhieuSuaChua> danhSachPhieu = new ArrayList<PhieuSuaChua>();
    private final List<BaoGia> danhSachBaoGia = new ArrayList<BaoGia>();
    private int soTiepTheo = 113;

    public PhieuSuaChuaDAO() {
        danhSachBaoGia.add(new BaoGia("BG-2026-0111", "51K-001.23", true, taoChiTietBaoGia(false)));
        danhSachBaoGia.add(new BaoGia("BG-2026-0108", "51H-246.80", true, taoChiTietBaoGia(true)));
        danhSachBaoGia.add(new BaoGia("BG-2026-0113", "51H-135.79", true, taoChiTietBaoGia(false)));
        danhSachBaoGia.add(new BaoGia("BG-2026-0114", "51F-112.34", false, taoChiTietBaoGia(false)));
        danhSachPhieu.add(new PhieuSuaChua("SC-2026-0112", "BG-2026-0111", "51K-001.23", "", "",
                LocalDateTime.of(2026, 10, 6, 11, 0), "Kiểm tra điều hòa và hệ thống làm lạnh.",
                TrangThaiSuaChua.CHO_SUA_CHUA, taoHangMuc("Điều hòa", false), null));
        danhSachPhieu.add(new PhieuSuaChua("SC-2026-0108", "BG-2026-0108", "51H-246.80", "kythuat", "",
                LocalDateTime.of(2026, 10, 5, 17, 0), "Bảo dưỡng, vệ sinh phanh.",
                TrangThaiSuaChua.DANG_SUA, taoHangMuc("Bảo dưỡng", true), null));
        // Các lệnh riêng giúp kiểm tra phản hồi khách mà không tự duyệt đề xuất vừa gửi.
        for (int soThuTu = 109; soThuTu <= 111; soThuTu++) {
            TrangThaiPhatSinh trangThai = soThuTu == 109 ? TrangThaiPhatSinh.DA_DUYET
                    : soThuTu == 110 ? TrangThaiPhatSinh.TU_CHOI : TrangThaiPhatSinh.CHO_DUYET;
            String maBaoGia = "BG-MAU-" + soThuTu;
            danhSachBaoGia.add(new BaoGia(maBaoGia, "51H-246.80", true, taoChiTietBaoGia(true)));
            List<HangMucSuaChua> hangMuc = taoHangMuc("Bảo dưỡng", true);
            if (trangThai == TrangThaiPhatSinh.DA_DUYET) {
                hangMuc.add(new HangMucSuaChua("Thay rotuyn lái", "kythuat", "",
                        TrangThaiSuaChua.CHO_SUA_CHUA, true));
            }
            PhatSinhSuaChua phatSinh = new PhatSinhSuaChua(new String[]{"Rotuyn lái bị rơ",
                "Phát hiện khi kiểm tra bổ sung gầm xe.", "Thay 1 rotuyn lái, tiền công thay.", "2 giờ",
                "Chỉ thực hiện phần đã được khách duyệt."}, trangThai,
                trangThai == TrangThaiPhatSinh.CHO_DUYET ? "" : "Nguyễn Văn An",
                trangThai == TrangThaiPhatSinh.CHO_DUYET ? null : LocalDateTime.of(2026, 10, 5, 14, 0));
            danhSachPhieu.add(new PhieuSuaChua("SC-2026-0" + soThuTu, maBaoGia, "51H-246.80", "kythuat", "",
                    LocalDateTime.of(2026, 10, 5, 17, 0), "Bảo dưỡng, vệ sinh phanh.",
                    TrangThaiSuaChua.DANG_SUA, hangMuc, phatSinh));
        }
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu mã lệnh
     * @return báo giá liên kết đã kiểm tra quyền
     */
    // Lấy báo giá của chính lệnh đang chọn để đối chiếu hạng mục, số lượng và giá tiền.
    public BaoGia layBaoGia(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraVaiTro(taiKhoan);
        PhieuSuaChua phieu = layPhieuNoiBo(taiKhoan, maPhieu);
        for (BaoGia baoGia : danhSachBaoGia) {
            if (baoGia.getMaBaoGia().equals(phieu.getMaBaoGia())) {
                return baoGia;
            }
        }
        throw new IllegalStateException("Không tìm thấy báo giá liên kết.");
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @return báo giá khách đã đồng ý
     */
    // Trả danh sách báo giá hợp lệ để tiếp nhận chọn khi tạo lệnh.
    public List<BaoGia> layBaoGiaDaDuyet(TaiKhoan taiKhoan) {
        kiemTraTiepNhan(taiKhoan);
        List<BaoGia> ketQua = new ArrayList<BaoGia>();
        for (BaoGia baoGia : danhSachBaoGia) {
            if (baoGia.isDaDongY()) {
                ketQua.add(baoGia);
            }
        }
        return ketQua;
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param tuKhoa từ khóa
     * @param trangThai bộ lọc
     * @return các lệnh phù hợp
     */
    // Lọc lệnh theo quyền, phân công và tiêu chí rồi trả bản sao cho giao diện.
    public List<PhieuSuaChua> timKiem(TaiKhoan taiKhoan, String tuKhoa, String trangThai) {
        kiemTraVaiTro(taiKhoan);
        String tuKhoaChuanHoa = tuKhoa.trim().toLowerCase(Locale.ROOT);
        List<PhieuSuaChua> ketQua = new ArrayList<PhieuSuaChua>();
        for (PhieuSuaChua phieu : danhSachPhieu) {
            String noiDung = phieu.getMaPhieu() + " " + phieu.getMaBaoGia() + " " + phieu.getBienSo();
            if (duocXem(taiKhoan, phieu) && noiDung.toLowerCase(Locale.ROOT).contains(tuKhoaChuanHoa)
                    && ("Tất cả".equals(trangThai) || phieu.getTrangThai().getTenHienThi().equals(trangThai))) {
                ketQua.add(new PhieuSuaChua(phieu));
            }
        }
        return ketQua;
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu mã lệnh
     * @return bản sao hoặc Optional rỗng
     */
    // Tìm lệnh theo mã trong đúng phạm vi của tài khoản đang đăng nhập.
    public Optional<PhieuSuaChua> timTheoMa(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraVaiTro(taiKhoan);
        for (PhieuSuaChua phieu : danhSachPhieu) {
            if (phieu.getMaPhieu().equals(maPhieu) && duocXem(taiKhoan, phieu)) {
                return Optional.of(new PhieuSuaChua(phieu));
            }
        }
        return Optional.empty();
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu rỗng khi thêm
     * @param maBaoGia báo giá đã duyệt
     * @param kyThuatChinh tài khoản chính
     * @param kyThuatPhoiHop tài khoản phối hợp
     * @param hanHoanThanh hạn dự kiến
     * @param ghiChu nội dung
     * @return lệnh đã lưu
     */
    // Thêm hoặc sửa lệnh chờ, kiểm tra báo giá và chống tạo nhiều lệnh cho cùng báo giá.
    public PhieuSuaChua luu(TaiKhoan taiKhoan, String maPhieu, String maBaoGia, String kyThuatChinh,
            String kyThuatPhoiHop, LocalDateTime hanHoanThanh, String ghiChu) {
        kiemTraTiepNhan(taiKhoan);
        if (!"kythuat".equals(kyThuatChinh) && !"kythuatvu".equals(kyThuatChinh)) {
            throw new IllegalArgumentException("Cần chọn kỹ thuật viên chính hợp lệ.");
        }
        if ((!kyThuatPhoiHop.isEmpty() && !"kythuat".equals(kyThuatPhoiHop)
                && !"kythuatvu".equals(kyThuatPhoiHop)) || kyThuatChinh.equals(kyThuatPhoiHop)) {
            throw new IllegalArgumentException("Kỹ thuật viên phối hợp không hợp lệ hoặc trùng người chính.");
        }
        BaoGia baoGiaChon = null;
        for (BaoGia baoGia : danhSachBaoGia) {
            if (baoGia.getMaBaoGia().equals(maBaoGia) && baoGia.isDaDongY()) {
                baoGiaChon = baoGia;
            }
        }
        if (baoGiaChon == null || hanHoanThanh == null || ghiChu.isBlank()) {
            throw new IllegalArgumentException("Cần báo giá đã được khách đồng ý, hạn và nội dung phân công.");
        }
        for (PhieuSuaChua phieu : danhSachPhieu) {
            if (phieu.getMaBaoGia().equals(maBaoGia) && !phieu.getMaPhieu().equals(maPhieu)) {
                throw new IllegalStateException("Báo giá đã có lệnh sửa chữa. Hãy mở lệnh hiện có.");
            }
        }
        PhieuSuaChua phieuCu = maPhieu.isEmpty() ? null : layPhieuNoiBo(taiKhoan, maPhieu);
        if (phieuCu != null && !phieuCu.getMaBaoGia().equals(maBaoGia)) {
            throw new IllegalStateException("Không đổi báo giá của lệnh đã tạo.");
        }
        String maMoi = phieuCu == null ? String.format("SC-2026-%04d", soTiepTheo) : maPhieu;
        List<HangMucSuaChua> hangMuc = phieuCu == null ? taoHangMuc("Điều hòa", false)
                : phieuCu.getDanhSachHangMuc();
        List<HangMucSuaChua> hangMucPhanCong = new ArrayList<HangMucSuaChua>();
        for (int viTri = 0; viTri < hangMuc.size(); viTri++) {
            HangMucSuaChua congViec = hangMuc.get(viTri);
            String nguoiThucHien = viTri > 0 && !kyThuatPhoiHop.isEmpty() ? kyThuatPhoiHop : kyThuatChinh;
            hangMucPhanCong.add(new HangMucSuaChua(congViec.getTenHangMuc(), nguoiThucHien,
                    congViec.getKetQua(), congViec.getTrangThai(), congViec.isPhatSinh()));
        }
        PhieuSuaChua banSua = new PhieuSuaChua(maMoi, maBaoGia, baoGiaChon.getBienSo(), kyThuatChinh,
                kyThuatPhoiHop, hanHoanThanh, ghiChu.trim(),
                TrangThaiSuaChua.CHO_SUA_CHUA, hangMucPhanCong, null);
        if (phieuCu == null) {
            danhSachPhieu.add(banSua);
            soTiepTheo++;
            return new PhieuSuaChua(banSua);
        }
        phieuCu.capNhatPhanCong(banSua);
        return new PhieuSuaChua(phieuCu);
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu lệnh cần xóa
     */
    // Chỉ xóa lệnh chưa bắt đầu và chưa có kết quả hoặc phát sinh liên quan.
    public void xoa(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraTiepNhan(taiKhoan);
        PhieuSuaChua phieu = layPhieuNoiBo(taiKhoan, maPhieu);
        if (phieu.getTrangThai() != TrangThaiSuaChua.CHO_SUA_CHUA || phieu.getPhatSinh() != null) {
            throw new IllegalStateException("Lệnh đã bắt đầu hoặc có dữ liệu liên quan, không được xóa.");
        }
        for (HangMucSuaChua hangMuc : phieu.getDanhSachHangMuc()) {
            if (!hangMuc.getKetQua().isBlank()) {
                throw new IllegalStateException("Lệnh đã có kết quả, không được xóa.");
            }
        }
        danhSachPhieu.remove(phieu);
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu lệnh
     * @param viTri công việc
     * @param trangThai tiến độ
     * @param ketQua nội dung thực hiện
     */
    // Cập nhật đúng công việc được giao và chặn thực hiện phát sinh chưa được khách duyệt.
    public void capNhatTienDo(TaiKhoan taiKhoan, String maPhieu, int viTri,
            TrangThaiSuaChua trangThai, String ketQua) {
        PhieuSuaChua phieu = layPhieuKyThuat(taiKhoan, maPhieu);
        HangMucSuaChua hangMuc = phieu.getDanhSachHangMuc().get(viTri);
        // Người chính điều phối tiến độ lệnh; người phối hợp chỉ cập nhật công việc được giao.
        if (!hangMuc.getTaiKhoanThucHien().equals(taiKhoan.getTenDangNhap())
                && !phieu.getKyThuatChinh().equals(taiKhoan.getTenDangNhap())) {
            throw new SecurityException("Bạn không được cập nhật công việc này.");
        }
        if (hangMuc.isPhatSinh() && (phieu.getPhatSinh() == null
                || phieu.getPhatSinh().getTrangThai() != TrangThaiPhatSinh.DA_DUYET)) {
            throw new IllegalStateException("Phát sinh chưa được duyệt, chỉ tiếp tục các công việc đã được duyệt.");
        }
        if (ketQua.isBlank()) {
            throw new IllegalArgumentException("Kết quả thực hiện là bắt buộc.");
        }
        phieu.capNhatHangMuc(viTri, new HangMucSuaChua(hangMuc.getTenHangMuc(),
                hangMuc.getTaiKhoanThucHien(), ketQua.trim(), trangThai, hangMuc.isPhatSinh()));
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu lệnh
     * @param noiDung năm trường đề xuất
     */
    // Gửi đề xuất sang trạng thái chờ duyệt, không tự tạo xác nhận của khách.
    public void guiPhatSinh(TaiKhoan taiKhoan, String maPhieu, String[] noiDung) {
        PhieuSuaChua phieu = layPhieuKyThuat(taiKhoan, maPhieu);
        phieu.ghiNhanPhatSinh(new PhatSinhSuaChua(noiDung, TrangThaiPhatSinh.CHO_DUYET, "", null));
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu lệnh
     * @param ketQua kết luận, bảo hành, khuyến nghị
     * @param daDoiChieu bốn xác nhận kiểm tra cuối
     */
    // Hoàn tất sau khi tất cả công việc hợp lệ đã xong và không còn phát sinh chờ xử lý.
    public void hoanTat(TaiKhoan taiKhoan, String maPhieu, String[] ketQua, boolean[] daDoiChieu) {
        PhieuSuaChua phieu = layPhieuKyThuat(taiKhoan, maPhieu);
        if (!phieu.getKyThuatChinh().equals(taiKhoan.getTenDangNhap())) {
            throw new SecurityException("Chỉ kỹ thuật viên chính được xác nhận hoàn thành lệnh.");
        }
        if (daDoiChieu.length != 4 || ketQua.length != 3) {
            throw new IllegalArgumentException("Thông tin kiểm tra cuối chưa đủ.");
        }
        for (boolean daChon : daDoiChieu) {
            if (!daChon) {
                throw new IllegalArgumentException("Cần đối chiếu và xác nhận đủ bốn điều kiện kiểm tra cuối.");
            }
        }
        if (phieu.getPhatSinh() != null && phieu.getPhatSinh().getTrangThai() == TrangThaiPhatSinh.CHO_DUYET) {
            throw new IllegalStateException("Phát sinh chưa được duyệt. Tiếp nhận cần ghi nhận phản hồi khách.");
        }
        for (HangMucSuaChua hangMuc : phieu.getDanhSachHangMuc()) {
            if (hangMuc.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || hangMuc.getKetQua().isBlank()) {
                throw new IllegalStateException("Còn công việc chưa hoàn thành: " + hangMuc.getTenHangMuc());
            }
        }
        phieu.hoanTat(ketQua, LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu mã lệnh
     * @return lệnh kỹ thuật được phép cập nhật
     */
    // Kiểm tra vai trò kỹ thuật, phân công và trạng thái trước mọi thao tác tiến độ.
    private PhieuSuaChua layPhieuKyThuat(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraVaiTro(taiKhoan);
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Thao tác này dành cho nhân viên kỹ thuật.");
        }
        PhieuSuaChua phieu = layPhieuNoiBo(taiKhoan, maPhieu);
        if (phieu.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
            throw new IllegalStateException("Lệnh đã hoàn thành, không cập nhật trực tiếp.");
        }
        return phieu;
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param maPhieu mã lệnh
     * @return dữ liệu nội bộ đã kiểm tra quyền
     */
    // Tìm bản nội bộ theo mã và từ chối truy cập ngoài phân công.
    private PhieuSuaChua layPhieuNoiBo(TaiKhoan taiKhoan, String maPhieu) {
        for (PhieuSuaChua phieu : danhSachPhieu) {
            if (phieu.getMaPhieu().equals(maPhieu) && duocXem(taiKhoan, phieu)) {
                return phieu;
            }
        }
        throw new IllegalArgumentException("Lệnh không còn tồn tại hoặc không thuộc phân công.");
    }

    /**
     * @param taiKhoan phiên đăng nhập
     * @param phieu lệnh cần xem
     * @return có quyền xem
     */
    // Giới hạn kỹ thuật vào các lệnh được giao chính hoặc phối hợp.
    private boolean duocXem(TaiKhoan taiKhoan, PhieuSuaChua phieu) {
        return taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN
                || taiKhoan.getTenDangNhap().equals(phieu.getKyThuatChinh())
                || taiKhoan.getTenDangNhap().equals(phieu.getKyThuatPhoiHop());
    }

    /** @param taiKhoan phiên đăng nhập */
    // Chặn các vai trò không thuộc phân hệ sửa chữa.
    private void kiemTraVaiTro(TaiKhoan taiKhoan) {
        if (taiKhoan == null || (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN
                && taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT)) {
            throw new SecurityException("Chức năng dành cho tiếp nhận và kỹ thuật.");
        }
    }

    /** @param taiKhoan phiên đăng nhập */
    // Chỉ cho tiếp nhận tạo, sửa phân công hoặc xóa lệnh chưa bắt đầu.
    private void kiemTraTiepNhan(TaiKhoan taiKhoan) {
        kiemTraVaiTro(taiKhoan);
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Thao tác này dành cho nhân viên tiếp nhận.");
        }
    }

    /**
     * @param nhom nhóm công việc mẫu
     * @param daBatDau có kết quả đã lưu
     * @return các công việc mẫu
     */
    // Tạo công việc ban đầu tương ứng với báo giá mẫu trong ZIP.
    private List<HangMucSuaChua> taoHangMuc(String nhom, boolean daBatDau) {
        List<HangMucSuaChua> ketQua = new ArrayList<HangMucSuaChua>();
        ketQua.add(new HangMucSuaChua("Điều hòa".equals(nhom) ? "Kiểm tra điều hòa" : "Bảo dưỡng dầu / lọc",
                "kythuat", daBatDau ? "Đã thay dầu, kiểm tra rò rỉ" : "",
                daBatDau ? TrangThaiSuaChua.HOAN_THANH : TrangThaiSuaChua.CHO_SUA_CHUA, false));
        if (!"Điều hòa".equals(nhom)) {
            ketQua.add(new HangMucSuaChua("Vệ sinh phanh trước", "kythuatvu", "Đang vệ sinh và kiểm tra",
                    TrangThaiSuaChua.DANG_SUA, false));
        }
        return ketQua;
    }

    /**
     * @param baoDuong nhóm bảo dưỡng hoặc điều hòa
     * @return các dòng báo giá mẫu
     */
    // Tạo chi tiết báo giá mẫu để kiểm tra bố cục và điều kiện phân công.
    private List<ChiTietBaoGia> taoChiTietBaoGia(boolean baoDuong) {
        List<ChiTietBaoGia> ketQua = new ArrayList<ChiTietBaoGia>();
        if (baoDuong) {
            ketQua.add(new ChiTietBaoGia("Bảo dưỡng định kỳ", "Dịch vụ / tiền công", "", "1", "300000"));
            ketQua.add(new ChiTietBaoGia("Vệ sinh phanh trước", "Tiền công", "", "1", "100000"));
            ketQua.add(new ChiTietBaoGia("Dầu động cơ 5W-30", "Phụ tùng", "lít", "4", "180000"));
            ketQua.add(new ChiTietBaoGia("Lọc dầu động cơ", "Phụ tùng", "cái", "1", "150000"));
        } else {
            ketQua.add(new ChiTietBaoGia("Kiểm tra điều hòa", "Dịch vụ / tiền công", "", "1", "300000"));
        }
        return ketQua;
    }
}