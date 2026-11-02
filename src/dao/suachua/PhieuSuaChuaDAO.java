package dao.suachua;

import dao.baogia.BaoGiaDAO;
import dao.nhansu.NhanVienDAO;
import dao.nhansu.TaiKhoanDAO;
import entity.baogia.BaoGia;
import entity.baogia.ChiTietBaoGia;
import entity.khachhang.Xe;
import entity.nhansu.KyThuatVienPhanCong;
import entity.nhansu.TaiKhoan;
import entity.nhansu.VaiTro;
import entity.suachua.HangMucSuaChua;
import entity.suachua.PhatSinhSuaChua;
import entity.suachua.PhieuSuaChua;
import entity.suachua.TrangThaiPhatSinh;
import entity.suachua.TrangThaiSuaChua;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Nghiệp vụ sửa chữa trên dữ liệu mẫu trong bộ nhớ
public final class PhieuSuaChuaDAO {
    private final List<PhieuSuaChua> danhSachPhieu = new ArrayList<PhieuSuaChua>();
    // Báo giá dùng chung để kiểm tra tham chiếu dịch vụ, kể cả khi chưa mở màn sửa chữa.
    private static final List<BaoGia> danhSachBaoGia = taoDanhSachBaoGia();
    private int soTiepTheo = 113;

    private BaoGiaDAO nguonBaoGia;
    private NhanVienDAO nguonKyThuat;
    private final List<String[]> luotPhatHanh = new ArrayList<>();

    /** Kho tiếp nhận mới nhận nguồn có kiểm soát, không trộn lệnh/báo giá mẫu của kỹ thuật. */
    public PhieuSuaChuaDAO(BaoGiaDAO baoGiaDAO, NhanVienDAO nguonKyThuat) {
        if (baoGiaDAO == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        nguonBaoGia = baoGiaDAO;
        if (nguonKyThuat == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nguonKyThuat = nguonKyThuat;
        soTiepTheo = 1;
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuSuaChuaDAO() {
        danhSachPhieu.add(new PhieuSuaChua("SC-2026-0112", "BG-2026-0111", "51K-001.23", "", "",
                LocalDateTime.of(2026, 10, 6, 11, 0), "Kiểm tra điều hòa và hệ thống làm lạnh.",
                TrangThaiSuaChua.CHO_SUA_CHUA, taoHangMuc("Điều hòa", false), null));
        danhSachPhieu.add(new PhieuSuaChua("SC-2026-0108", "BG-2026-0108", "51H-246.80", "kythuat", "",
                LocalDateTime.of(2026, 10, 5, 17, 0), "Bảo dưỡng, vệ sinh phanh.", TrangThaiSuaChua.DANG_SUA,
                taoHangMuc("Bảo dưỡng", true), null));
        // Các lệnh riêng giúp kiểm tra phản hồi khách mà không tự duyệt đề xuất vừa gửi.
        for (int soThuTu = 109; soThuTu <= 111; soThuTu++) {
            TrangThaiPhatSinh trangThai = soThuTu == 109 ? TrangThaiPhatSinh.DA_DUYET
                    : soThuTu == 110 ? TrangThaiPhatSinh.TU_CHOI : TrangThaiPhatSinh.CHO_DUYET;
            String maBaoGia = "BG-MAU-" + soThuTu;
            List<HangMucSuaChua> hangMuc = taoHangMuc("Bảo dưỡng", true);
            if (trangThai == TrangThaiPhatSinh.DA_DUYET) {
                hangMuc.add(new HangMucSuaChua("Thay rotuyn lái", "kythuat", "", TrangThaiSuaChua.CHO_SUA_CHUA, true));
            }
            PhatSinhSuaChua phatSinh = new PhatSinhSuaChua(
                    new String[] { "Rotuyn lái bị rơ", "Phát hiện khi kiểm tra bổ sung gầm xe.",
                            "Thay 1 rotuyn lái, tiền công thay.", "2 giờ", "Chỉ thực hiện phần đã được khách duyệt." },
                    trangThai, trangThai == TrangThaiPhatSinh.CHO_DUYET ? "" : "Nguyễn Văn An",
                    trangThai == TrangThaiPhatSinh.CHO_DUYET ? null : LocalDateTime.of(2026, 10, 5, 14, 0));
            danhSachPhieu.add(new PhieuSuaChua("SC-2026-0" + soThuTu, maBaoGia, "51H-246.80", "kythuat", "",
                    LocalDateTime.of(2026, 10, 5, 17, 0), "Bảo dưỡng, vệ sinh phanh.", TrangThaiSuaChua.DANG_SUA,
                    hangMuc, phatSinh));
        }
    }

    // Lấy báo giá của chính lệnh đang chọn để đối chiếu hạng mục, số lượng và giá tiền.
    /** Đọc bao gia từ dữ liệu hiện tại. */
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

    // Trả danh sách báo giá hợp lệ để tiếp nhận chọn khi tạo lệnh.
    /** Đọc bao gia da duyet từ dữ liệu hiện tại. */
    public List<BaoGia> layBaoGiaDaDuyet(TaiKhoan taiKhoan) {
        kiemTraTiepNhan(taiKhoan);
        if (nguonBaoGia != null) {
            return nguonBaoGia.layDaDuyet(taiKhoan);
        }
        List<BaoGia> ketQua = new ArrayList<BaoGia>();
        for (BaoGia baoGia : danhSachBaoGia) {
            if (baoGia.isDaDongY()) {
                ketQua.add(baoGia);
            }
        }
        return ketQua;
    }

    // Lọc lệnh theo quyền, phân công và tiêu chí rồi trả bản sao cho giao diện.
    /** Thực hiện tim kiem cho thành phần hiện tại. */
    public List<PhieuSuaChua> timKiem(TaiKhoan taiKhoan, String tuKhoa, String trangThai) {
        kiemTraVaiTro(taiKhoan);
        String tuKhoaChuanHoa = tuKhoa.trim().toLowerCase();
        List<PhieuSuaChua> ketQua = new ArrayList<PhieuSuaChua>();
        for (PhieuSuaChua phieu : danhSachPhieu) {
            String noiDung = phieu.getMaPhieu() + " " + phieu.getMaBaoGia() + " " + phieu.getBienSo();
            String bienTim = Xe.khoaBienSo(tuKhoaChuanHoa);
            if (duocXem(taiKhoan, phieu)
                    && (noiDung.toLowerCase().contains(tuKhoaChuanHoa)
                            || (!bienTim.isEmpty() && Xe.chuanHoaBienSo(phieu.getBienSo()).contains(bienTim)))
                    && ("Tất cả".equals(trangThai) || phieu.getTrangThai().getTenHienThi().equals(trangThai))) {
                ketQua.add(new PhieuSuaChua(phieu));
            }
        }
        return ketQua;
    }

    // Tìm lệnh theo mã trong đúng phạm vi của tài khoản đang đăng nhập.
    /** Thực hiện tim theo ma cho thành phần hiện tại. */
    public PhieuSuaChua timTheoMa(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraVaiTro(taiKhoan);
        for (PhieuSuaChua phieu : danhSachPhieu) {
            if (phieu.getMaPhieu().equals(maPhieu) && duocXem(taiKhoan, phieu)) {
                return new PhieuSuaChua(phieu);
            }
        }
        return null;
    }

    // Thêm hoặc sửa lệnh chờ, kiểm tra báo giá và chống tạo nhiều lệnh cho cùng báo giá.
    /** Kiểm tra và lưu . */
    public PhieuSuaChua luu(TaiKhoan taiKhoan, String maPhieu, String maBaoGia, String kyThuatChinh,
            String kyThuatPhoiHop, LocalDateTime hanHoanThanh, String ghiChu) {
        kiemTraTiepNhan(taiKhoan);
        if (nguonBaoGia != null) {
            throw new IllegalStateException("Dùng phát hành lệnh với phân công từng hạng mục.");
        }
        if (!"kythuat".equals(kyThuatChinh) && !"kythuatvu".equals(kyThuatChinh)) {
            throw new IllegalArgumentException("Cần chọn kỹ thuật viên chính hợp lệ.");
        }
        if ((!kyThuatPhoiHop.isEmpty() && !"kythuat".equals(kyThuatPhoiHop) && !"kythuatvu".equals(kyThuatPhoiHop))
                || kyThuatChinh.equals(kyThuatPhoiHop)) {
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
        List<HangMucSuaChua> hangMuc = phieuCu == null ? taoHangMuc("Điều hòa", false) : phieuCu.getDanhSachHangMuc();
        List<HangMucSuaChua> hangMucPhanCong = new ArrayList<HangMucSuaChua>();
        for (int viTri = 0; viTri < hangMuc.size(); viTri++) {
            HangMucSuaChua congViec = hangMuc.get(viTri);
            String nguoiThucHien = viTri > 0 && !kyThuatPhoiHop.isEmpty() ? kyThuatPhoiHop : kyThuatChinh;
            hangMucPhanCong.add(new HangMucSuaChua(congViec.getTenHangMuc(), nguoiThucHien, congViec.getKetQua(),
                    congViec.getTrangThai(), congViec.isPhatSinh()));
        }
        PhieuSuaChua banSua = new PhieuSuaChua(maMoi, maBaoGia, baoGiaChon.getBienSo(), kyThuatChinh, kyThuatPhoiHop,
                hanHoanThanh, ghiChu.trim(), TrangThaiSuaChua.CHO_SUA_CHUA, hangMucPhanCong, null);
        if (phieuCu == null) {
            danhSachPhieu.add(banSua);
            soTiepTheo++;
            return new PhieuSuaChua(banSua);
        }
        phieuCu.capNhatPhanCong(banSua);
        return new PhieuSuaChua(phieuCu);
    }

    /** Chỉ chọn một báo giá hiệu lực cho nhóm chưa có lệnh; điều chỉnh lệnh có sẵn là thao tác riêng. */
    public List<BaoGia> layBaoGiaCoThePhatHanh(TaiKhoan taiKhoan) {
        List<BaoGia> ketQua = new ArrayList<>();
        for (BaoGia bg : layBaoGiaDaDuyet(taiKhoan)) {
            boolean daCoLenh = false;
            for (PhieuSuaChua lenh : danhSachPhieu) {
                for (BaoGia daLap : danhSachBaoGia) {
                    if (lenh.getMaBaoGia().equals(daLap.getMaBaoGia())
                            && daLap.getMaBaoGiaGoc().equals(bg.getMaBaoGiaGoc())) {
                        daCoLenh = true;
                    }
                }
            }
            if (!daCoLenh) {
                ketQua.add(bg);
            }
        }
        return ketQua;
    }

    /** Nguồn phải liên kết bằng mã hồ sơ/tài khoản; không dùng tên để đoán tài khoản kỹ thuật. */
    public List<KyThuatVienPhanCong> layKyThuatPhanCong(TaiKhoan taiKhoan) {
        kiemTraTiepNhan(taiKhoan);
        if (nguonKyThuat == null) {
            return new ArrayList<>();
        }
        List<KyThuatVienPhanCong> ketQua = new ArrayList<>();
        List<String> taiKhoanDaCo = new ArrayList<>(), nhanVienDaCo = new ArrayList<>();
        for (KyThuatVienPhanCong kt : nguonKyThuat.layLienKetKyThuat(new TaiKhoanDAO())) {
            if (kt == null || !kt.duocPhanCong()) {
                continue;
            }
            String ten = kt.getTaiKhoan().getTenDangNhap(), ma = kt.getNhanVien().getMaNhanVien();
            if (taiKhoanDaCo.contains(ten) || nhanVienDaCo.contains(ma)) {
                throw new IllegalStateException("Nguồn phân công có liên kết nhân viên/tài khoản bị trùng.");
            }
            taiKhoanDaCo.add(ten);
            nhanVienDaCo.add(ma);
            ketQua.add(kt);
        }
        return ketQua;
    }

    /** Phát hành một lần; tên và thứ tự công việc luôn lấy từ chính phiên bản khách duyệt. */
    public PhieuSuaChua phatHanh(TaiKhoan taiKhoan, String maLuot, BaoGia baoGiaDaDoc, String chinh, String hoTro,
            List<String> phuTrach, LocalDateTime han, String ghiChu) {
        kiemTraTiepNhan(taiKhoan);
        if (nguonBaoGia == null) {
            throw new IllegalStateException("Chưa cấp nguồn báo giá cho chức năng phát hành.");
        }
        if (maLuot == null || maLuot.isBlank() || baoGiaDaDoc == null) {
            throw new IllegalArgumentException("Thiếu mã thao tác hoặc báo giá.");
        }
        String noiDung = ghiChu == null ? "" : ghiChu.trim();
        String daLuu = timLenhDaPhatHanh(maLuot);
        if (daLuu != null) {
            PhieuSuaChua cu = layPhieuNoiBo(taiKhoan, daLuu);
            if (!cungPhatHanh(cu, taiKhoan, baoGiaDaDoc, chinh, hoTro, phuTrach, han, noiDung)) {
                throw new IllegalArgumentException("Mã thao tác đã phát hành một nội dung khác.");
            }
            return new PhieuSuaChua(cu);
        }
        BaoGia baoGia = nguonBaoGia.timTheoMa(taiKhoan, baoGiaDaDoc.getMaBaoGia());
        if (baoGia != baoGiaDaDoc || !baoGia.isDaDongY() || baoGia.getThoiDiemPhanHoi() == null
                || !nguonBaoGia.layDaDuyet(taiKhoan).contains(baoGia)) {
            throw new IllegalStateException("Cần đúng phiên bản báo giá đã được khách đồng ý và còn hiệu lực.");
        }
        for (BaoGia cu : danhSachBaoGia) {
            if (cu.getMaBaoGiaGoc().equals(baoGia.getMaBaoGiaGoc())) {
                throw new IllegalStateException(
                        "Nhóm báo giá đã có lệnh. Không phát hành lệnh thứ hai từ bản điều chỉnh.");
            }
        }
        LocalDateTime lucLap = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (han == null || han.isBefore(lucLap) || han.isBefore(baoGia.getThoiDiemPhanHoi())) {
            throw new IllegalArgumentException("Hạn dự kiến phải sau thời điểm phát hành và phản hồi đồng ý.");
        }
        List<KyThuatVienPhanCong> nhanSu = layKyThuatPhanCong(taiKhoan);
        kiemTraNguoiPhanCong(nhanSu, chinh);
        if (hoTro == null || chinh.equals(hoTro)) {
            throw new IllegalArgumentException("Người hỗ trợ không được trùng kỹ thuật viên chính.");
        }
        if (!hoTro.isEmpty()) {
            kiemTraNguoiPhanCong(nhanSu, hoTro);
        }
        List<ChiTietBaoGia> chiTiet = baoGia.getChiTiet();
        if (phuTrach == null || phuTrach.size() != chiTiet.size()) {
            throw new IllegalArgumentException("Cần phân công đủ từng dòng báo giá đã duyệt.");
        }
        List<HangMucSuaChua> hangMuc = new ArrayList<>();
        for (int i = 0; i < chiTiet.size(); i++) {
            String nguoi = phuTrach.get(i);
            if (nguoi == null || nguoi.isBlank() || (!nguoi.equals(chinh) && !nguoi.equals(hoTro))) {
                throw new IllegalArgumentException(
                        "Hạng mục " + (i + 1) + " phải giao cho người chính hoặc hỗ trợ đã chọn.");
            }
            hangMuc.add(new HangMucSuaChua(chiTiet.get(i).getTenHangMuc(), nguoi, "", TrangThaiSuaChua.CHO_SUA_CHUA,
                    false));
        }
        String ma = String.format(Locale.ROOT, "SC-%04d-%04d", lucLap.getYear(), soTiepTheo);
        PhieuSuaChua moi = new PhieuSuaChua(ma, baoGia.getMaBaoGia(), baoGia.getBienSo(), chinh, hoTro, han, noiDung,
                hangMuc, taiKhoan.getTenDangNhap(), lucLap);
        danhSachPhieu.add(moi);
        danhSachBaoGia.add(baoGia);
        luotPhatHanh.add(new String[] { maLuot, ma });
        soTiepTheo++;
        return new PhieuSuaChua(moi);
    }

    /** Kiểm tra nguoi phan cong trước khi xử lý. */
    private void kiemTraNguoiPhanCong(List<KyThuatVienPhanCong> nhanSu, String tenDangNhap) {
        for (KyThuatVienPhanCong kt : nhanSu) {
            if (kt.getTaiKhoan().getTenDangNhap().equals(tenDangNhap)) {
                return;
            }
        }
        throw new IllegalArgumentException(
                "Kỹ thuật viên không tồn tại, đã nghỉ hoặc chưa có tài khoản kỹ thuật hợp lệ.");
    }

    /** Thực hiện cung phat hanh cho thành phần hiện tại. */
    private boolean cungPhatHanh(PhieuSuaChua cu, TaiKhoan tk, BaoGia bg, String chinh, String hoTro,
            List<String> phuTrach, LocalDateTime han, String ghiChu) {
        if (!cu.getNguoiPhatHanh().equals(tk.getTenDangNhap()) || !cu.getMaBaoGia().equals(bg.getMaBaoGia())
                || !cu.getKyThuatChinh().equals(chinh) || !cu.getKyThuatPhoiHop().equals(hoTro)
                || !cu.getHanHoanThanh().equals(han) || !cu.getGhiChu().equals(ghiChu) || phuTrach == null
                || phuTrach.size() != cu.getDanhSachHangMuc().size()) {
            return false;
        }
        List<HangMucSuaChua> hangMuc = cu.getDanhSachHangMuc();
        for (int i = 0; i < hangMuc.size(); i++) {
            if (!hangMuc.get(i).getTaiKhoanThucHien().equals(phuTrach.get(i))) {
                return false;
            }
        }
        return true;
    }

    // Chỉ xóa lệnh chưa bắt đầu và chưa có kết quả hoặc phát sinh liên quan.
    /** Thực hiện xoa cho thành phần hiện tại. */
    public void xoa(TaiKhoan taiKhoan, String maPhieu) {
        kiemTraTiepNhan(taiKhoan);
        if (nguonBaoGia != null) {
            throw new IllegalStateException("Lệnh đã phát hành cần giữ lịch sử, không xóa trực tiếp.");
        }
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

    // Cập nhật đúng công việc được giao và chặn thực hiện phát sinh chưa được khách duyệt.
    /** Thực hiện cap nhat tien do cho thành phần hiện tại. */
    public void capNhatTienDo(TaiKhoan taiKhoan, String maPhieu, int viTri, TrangThaiSuaChua trangThai, String ketQua) {
        PhieuSuaChua phieu = layPhieuKyThuat(taiKhoan, maPhieu);
        HangMucSuaChua hangMuc = phieu.getDanhSachHangMuc().get(viTri);
        // Người chính điều phối tiến độ lệnh; người phối hợp chỉ cập nhật công việc được giao.
        if (!hangMuc.getTaiKhoanThucHien().equals(taiKhoan.getTenDangNhap())
                && !phieu.getKyThuatChinh().equals(taiKhoan.getTenDangNhap())) {
            throw new SecurityException("Bạn không được cập nhật công việc này.");
        }
        if (hangMuc.isPhatSinh()
                && (phieu.getPhatSinh() == null || phieu.getPhatSinh().getTrangThai() != TrangThaiPhatSinh.DA_DUYET)) {
            throw new IllegalStateException("Phát sinh chưa được duyệt, chỉ tiếp tục các công việc đã được duyệt.");
        }
        if (ketQua.isBlank()) {
            throw new IllegalArgumentException("Kết quả thực hiện là bắt buộc.");
        }
        phieu.capNhatHangMuc(viTri, new HangMucSuaChua(hangMuc.getTenHangMuc(), hangMuc.getTaiKhoanThucHien(),
                ketQua.trim(), trangThai, hangMuc.isPhatSinh()));
    }

    // Gửi đề xuất sang trạng thái chờ duyệt, không tự tạo xác nhận của khách.
    /** Thực hiện gui phat sinh cho thành phần hiện tại. */
    public void guiPhatSinh(TaiKhoan taiKhoan, String maPhieu, String[] noiDung) {
        PhieuSuaChua phieu = layPhieuKyThuat(taiKhoan, maPhieu);
        phieu.ghiNhanPhatSinh(new PhatSinhSuaChua(noiDung, TrangThaiPhatSinh.CHO_DUYET, "", null));
    }

    // Hoàn tất sau khi tất cả công việc hợp lệ đã xong và không còn phát sinh chờ xử lý.
    /** Thực hiện hoan tat cho thành phần hiện tại. */
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

    // Kiểm tra vai trò kỹ thuật, phân công và trạng thái trước mọi thao tác tiến độ.
    /** Đọc phieu ky thuat từ dữ liệu hiện tại. */
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

    // Tìm bản nội bộ theo mã và từ chối truy cập ngoài phân công.
    /** Đọc phieu noi bo từ dữ liệu hiện tại. */
    private PhieuSuaChua layPhieuNoiBo(TaiKhoan taiKhoan, String maPhieu) {
        for (PhieuSuaChua phieu : danhSachPhieu) {
            if (phieu.getMaPhieu().equals(maPhieu) && duocXem(taiKhoan, phieu)) {
                return phieu;
            }
        }
        throw new IllegalArgumentException("Lệnh không còn tồn tại hoặc không thuộc phân công.");
    }

    // Giới hạn kỹ thuật vào các lệnh được giao chính hoặc phối hợp.
    /** Thực hiện duoc xem cho thành phần hiện tại. */
    private boolean duocXem(TaiKhoan taiKhoan, PhieuSuaChua phieu) {
        return taiKhoan.getVaiTro() == VaiTro.NHAN_VIEN_TIEP_NHAN
                || taiKhoan.getTenDangNhap().equals(phieu.getKyThuatChinh())
                || taiKhoan.getTenDangNhap().equals(phieu.getKyThuatPhoiHop());
    }

    // Chặn các vai trò không thuộc phân hệ sửa chữa.
    /** Kiểm tra vai tro trước khi xử lý. */
    private void kiemTraVaiTro(TaiKhoan taiKhoan) {
        if (taiKhoan != null) {
            taiKhoan.kiemTraPhien();
        }
        if (taiKhoan == null || (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN
                && taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_KY_THUAT)) {
            throw new SecurityException("Chức năng dành cho tiếp nhận và kỹ thuật.");
        }
    }

    // Chỉ cho tiếp nhận tạo, sửa phân công hoặc xóa lệnh chưa bắt đầu.
    /** Kiểm tra tiep nhan trước khi xử lý. */
    private void kiemTraTiepNhan(TaiKhoan taiKhoan) {
        kiemTraVaiTro(taiKhoan);
        if (taiKhoan.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Thao tác này dành cho nhân viên tiếp nhận.");
        }
    }

    // Tạo công việc ban đầu tương ứng với báo giá mẫu trong ZIP.
    /** Tạo hang muc cho giao diện. */
    private List<HangMucSuaChua> taoHangMuc(String nhom, boolean daBatDau) {
        List<HangMucSuaChua> ketQua = new ArrayList<HangMucSuaChua>();
        ketQua.add(new HangMucSuaChua("Điều hòa".equals(nhom) ? "Kiểm tra điều hòa" : "Bảo dưỡng dầu / lọc", "kythuat",
                daBatDau ? "Đã thay dầu, kiểm tra rò rỉ" : "",
                daBatDau ? TrangThaiSuaChua.HOAN_THANH : TrangThaiSuaChua.CHO_SUA_CHUA, false));
        if (!"Điều hòa".equals(nhom)) {
            ketQua.add(new HangMucSuaChua("Vệ sinh phanh trước", "kythuatvu", "Đang vệ sinh và kiểm tra",
                    TrangThaiSuaChua.DANG_SUA, false));
        }
        return ketQua;
    }

    /** Kiểm tra tham chiếu bằng mã, không phụ thuộc tên dịch vụ hoặc trạng thái duyệt báo giá. */
    public static boolean daSuDungDichVu(TaiKhoan taiKhoan, String maDichVu) {
        taiKhoan.kiemTraPhien();
        if (taiKhoan.getVaiTro() != VaiTro.QUAN_LY) {
            throw new SecurityException("Chỉ Quản lý được kiểm tra xóa dịch vụ.");
        }
        for (BaoGia baoGia : danhSachBaoGia) {
            for (ChiTietBaoGia dong : baoGia.getChiTiet()) {
                if (dong.getMaDichVu().equals(maDichVu)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Tạo danh sach bao gia cho giao diện. */
    private static List<BaoGia> taoDanhSachBaoGia() {
        List<BaoGia> ketQua = new ArrayList<>();
        ketQua.add(new BaoGia("BG-2026-0111", "51K-001.23", true, taoChiTietBaoGia(false)));
        ketQua.add(new BaoGia("BG-2026-0108", "51H-246.80", true, taoChiTietBaoGia(true)));
        ketQua.add(new BaoGia("BG-2026-0113", "51H-135.79", true, taoChiTietBaoGia(false)));
        ketQua.add(new BaoGia("BG-2026-0114", "51F-112.34", false, taoChiTietBaoGia(false)));
        for (int so = 109; so <= 111; so++) {
            ketQua.add(new BaoGia("BG-MAU-" + so, "51H-246.80", true, taoChiTietBaoGia(true)));
        }
        return new ArrayList<>(ketQua);
    }

    // Tạo chi tiết báo giá mẫu và truyền đúng tham số của constructor.
    /** Tạo chi tiet bao gia cho giao diện. */
    private static List<ChiTietBaoGia> taoChiTietBaoGia(boolean baoDuong) {
        List<ChiTietBaoGia> ketQua = new ArrayList<ChiTietBaoGia>();
        if (baoDuong) {
            ketQua.add(new ChiTietBaoGia("DV001", "Bảo dưỡng định kỳ", "Dịch vụ / tiền công", "", 1, 300000L, ""));
            ketQua.add(new ChiTietBaoGia("DV002", "Vệ sinh phanh trước", "Tiền công", "", 1, 100000L, ""));
            ketQua.add(new ChiTietBaoGia("Dầu động cơ 5W-30", "Phụ tùng", "lít", 4, 180000L));
            ketQua.add(new ChiTietBaoGia("Lọc dầu động cơ", "Phụ tùng", "cái", 1, 150000L));
        } else {
            ketQua.add(new ChiTietBaoGia("DV003", "Kiểm tra điều hòa", "Dịch vụ / tiền công", "", 1, 300000L, ""));
        }
        return ketQua;
    }

    /** Đọc bản sao các lệnh hoàn thành cho nguồn thanh toán dùng chung. */
    public List<PhieuSuaChua> layLenhHoanThanhNoiBo() {
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : danhSachPhieu) {
            if (lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH && lenh.getThoiDiemHoanTat() != null) {
                ketQua.add(new PhieuSuaChua(lenh));
            }
        }
        return ketQua;
    }

    /** Đọc báo giá liên kết nội bộ, không mở quyền sửa chữa cho Thu ngân. */
    public BaoGia docBaoGiaNoiBo(String ma) {
        if (nguonBaoGia != null) {
            return nguonBaoGia.timNoiBo(ma);
        }
        for (BaoGia baoGia : danhSachBaoGia) {
            if (baoGia.getMaBaoGia().equals(ma)) {
                return baoGia;
            }
        }
        return null;
    }

    /** Tìm chứng từ đã lưu theo mã thao tác để tránh ghi lặp. */
    private String timLenhDaPhatHanh(String maThaoTac) {
        for (String[] dong : luotPhatHanh) {
            if (dong[0].equals(maThaoTac)) {
                return dong[1];
            }
        }
        return null;
    }

    /** Đọc bản sao các lệnh để tổng hợp, không cho báo cáo sửa lệnh. */
    public List<PhieuSuaChua> layLenhBaoCao() {
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : danhSachPhieu) {
            ketQua.add(new PhieuSuaChua(lenh));
        }
        return ketQua;
    }
}
