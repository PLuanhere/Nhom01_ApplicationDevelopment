package dao;

import dao.HoaDonDAO;
import dao.PhieuSuaChuaDAO;
import entity.BanGiaoXe;
import entity.BanGiaoXe.CanCuBanGiao;
import entity.BanGiaoXe.TrangThaiBanGiao;
import entity.BaoGia;
import entity.Xe;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Kho bàn giao trong bộ nhớ; nguồn thu tiền/phê duyệt chỉ đọc, luôn kiểm tra lại khi xác nhận. */
public final class BanGiaoXeDAO {
    private final PhieuSuaChuaDAO suaChua;
    private final HoaDonDAO nguonCanCu;
    private final List<BanGiaoXe> theoLenh = new ArrayList<>();
    private final List<String[]> theoThaoTac = new ArrayList<>();
    private int soThuTu = 1;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BanGiaoXeDAO(PhieuSuaChuaDAO suaChua, HoaDonDAO nguonCanCu) {
        if (suaChua == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.suaChua = suaChua;
        if (nguonCanCu == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.nguonCanCu = nguonCanCu;
    }

    /** Đọc lenh từ dữ liệu hiện tại. */
    public PhieuSuaChua docLenh(TaiKhoan tk, String ma) {
        quyen(tk);
        PhieuSuaChua ketQuaTraCuu1 = suaChua.timTheoMa(tk, ma);
        if (ketQuaTraCuu1 == null) {
            throw new IllegalArgumentException("Lệnh sửa chữa không tồn tại.");
        }
        return ketQuaTraCuu1;
    }

    /** Đọc bao gia từ dữ liệu hiện tại. */
    public BaoGia docBaoGia(TaiKhoan tk, String ma) {
        docLenh(tk, ma);
        return suaChua.layBaoGia(tk, ma);
    }

    /** Đọc can cu từ dữ liệu hiện tại. */
    public CanCuBanGiao docCanCu(TaiKhoan tk, String ma) {
        docLenh(tk, ma);
        return nguonCanCu.docCanCuBanGiao(ma);
    }

    /** Thực hiện tim theo lenh cho thành phần hiện tại. */
    public BanGiaoXe timTheoLenh(TaiKhoan tk, String ma) {
        quyen(tk);
        return timGiaoNoiBo(ma);
    }

    /** Thực hiện ly do chua du cho thành phần hiện tại. */
    public String lyDoChuaDu(TaiKhoan tk, String ma) {
        PhieuSuaChua lenh = docLenh(tk, ma);
        return lyDo(lenh, docBaoGia(tk, ma), docCanCu(tk, ma));
    }

    /** Thực hiện ly do cho thành phần hiện tại. */
    private String lyDo(PhieuSuaChua lenh, BaoGia bg, CanCuBanGiao canCu) {
        if (lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || lenh.getThoiDiemHoanTat() == null) {
            return "Xe chưa hoàn thành sửa chữa.";
        }
        for (ChiTietSuaChua hm : lenh.getDanhSachHangMuc()) {
            if (hm.getTrangThai() != TrangThaiSuaChua.HOAN_THANH || hm.getKetQua().isBlank()) {
                return "Còn hạng mục chưa hoàn thành.";
            }
        }
        if (!bg.isDaDongY()) {
            return "Thiếu báo giá được khách đồng ý của lệnh.";
        }
        if (bg.getBaoCao() == null) {
            return "Thiếu liên kết phiếu tiếp nhận của lệnh.";
        }
        if (canCu == null) {
            return "Chưa có xác nhận thanh toán/phê duyệt từ nguồn phụ trách.";
        }
        if (!canCu.getMaLenh().equals(lenh.getMaPhieu())) {
            return "Căn cứ thanh toán/phê duyệt không thuộc lệnh đang giao.";
        }
        if (canCu.getThoiDiem().isAfter(LocalDateTime.now())) {
            return "Thời điểm xác nhận căn cứ không hợp lệ.";
        }
        if (!canCu.duDieuKien()) {
            return "Khách chưa thanh toán đủ và chưa có phê duyệt ngoại lệ.";
        }
        return "";
    }

    /** Thực hiện trang thai cho thành phần hiện tại. */
    public TrangThaiBanGiao trangThai(TaiKhoan tk, String ma) {
        quyen(tk);
        if ((timGiaoNoiBo(ma) != null)) {
            return TrangThaiBanGiao.DA_GIAO;
        }
        return lyDoChuaDu(tk, ma).isEmpty() ? TrangThaiBanGiao.CHO_GIAO : TrangThaiBanGiao.CHO_CAN_CU;
    }

    /** Chỉ ghi một lần cho lệnh; lặp đúng cùng thao tác trả chứng từ cũ và giữ thời điểm cũ. */
    public BanGiaoXe xacNhan(TaiKhoan tk, String token, String maLenh, CanCuBanGiao daDoc, String nguoiNhan,
            LocalDateTime lucGiao, String tinhTrang, String taiSan, String ghiChu, String hinhThuc,
            boolean khachXacNhan) {
        quyen(tk);
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thao tác bàn giao.");
        }
        String daLuu = timLenhTheoLuot(token);
        if (daLuu != null) {
            BanGiaoXe cu = timGiaoNoiBo(daLuu);
            if (!cungThaoTac(cu, tk, maLenh, daDoc, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu, hinhThuc,
                    khachXacNhan)) {
                throw new IllegalArgumentException("Mã thao tác đã lưu một nội dung bàn giao khác.");
            }
            return cu;
        }
        if ((timGiaoNoiBo(maLenh) != null)) {
            throw new IllegalStateException("Lệnh đã bàn giao; mở lịch sử để xem, không ghi lần nữa.");
        }
        PhieuSuaChua lenh = docLenh(tk, maLenh);
        BaoGia bg = docBaoGia(tk, maLenh);
        CanCuBanGiao hienTai = docCanCu(tk, maLenh);
        String lyDo = lyDo(lenh, bg, hienTai);
        if (!lyDo.isEmpty()) {
            throw new IllegalStateException(lyDo);
        }
        if (!hienTai.cungNoiDung(daDoc)) {
            throw new IllegalStateException("Thanh toán/phê duyệt đã thay đổi. Mở lại lệnh để đối chiếu.");
        }
        LocalDateTime lucLuu = LocalDateTime.now();
        String ma = String.format(Locale.ROOT, "GX-%04d-%04d", lucGiao == null ? lucLuu.getYear() : lucGiao.getYear(),
                soThuTu);
        BanGiaoXe moi = new BanGiaoXe(ma, lenh, bg, hienTai, tk, nguoiNhan, lucGiao, tinhTrang, taiSan, ghiChu,
                hinhThuc, khachXacNhan, lucLuu);
        theoLenh.add(moi);
        theoThaoTac.add(new String[] { token, maLenh });
        soThuTu++;
        return moi;
    }

    /** Với xe chưa giao, bộ lọc ngày dùng ngày hoàn tất sửa chữa; xe đã giao dùng ngày giao thực tế. */
    public List<PhieuSuaChua> timKiem(TaiKhoan tk, String tuKhoa, TrangThaiBanGiao trangThai, LocalDate tu,
            LocalDate den) {
        quyen(tk);
        if (tu != null && den != null && tu.isAfter(den)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
        }
        if (tuKhoa == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        String khoa = tuKhoa.trim().toLowerCase();
        String bien = Xe.khoaBienSo(khoa);
        List<PhieuSuaChua> ketQua = new ArrayList<>();
        for (PhieuSuaChua lenh : suaChua.timKiem(tk, "", "Hoàn thành")) {
            BanGiaoXe giao = timGiaoNoiBo(lenh.getMaPhieu());
            LocalDate ngay = giao == null
                    ? lenh.getThoiDiemHoanTat() == null ? null : lenh.getThoiDiemHoanTat().toLocalDate()
                    : giao.getThoiDiemGiao().toLocalDate();
            if (ngay == null || (tu != null && ngay.isBefore(tu)) || (den != null && ngay.isAfter(den))) {
                continue;
            }
            BaoGia bg = docBaoGia(tk, lenh.getMaPhieu());
            String chu = lenh.getMaPhieu() + " " + lenh.getBienSo() + " "
                    + (bg.getBaoCao() == null ? "" : bg.getBaoCao().getPhieu().getTenKhach());
            if (giao != null) {
                chu += " " + giao.getMaBanGiao() + " " + giao.getNguoiNhan() + " " + giao.getNguoiGiao();
            }
            if (trangThai != null && trangThai(tk, lenh.getMaPhieu()) != trangThai) {
                continue;
            }
            if (chu.toLowerCase().contains(khoa)
                    || (!bien.isEmpty() && Xe.chuanHoaBienSo(lenh.getBienSo()).contains(bien))) {
                ketQua.add(lenh);
            }
        }
        return ketQua;
    }

    /** Thực hiện lich su theo xe cho thành phần hiện tại. */
    public List<BanGiaoXe> lichSuTheoXe(TaiKhoan tk, String maXe) {
        quyen(tk);
        List<BanGiaoXe> ds = new ArrayList<>();
        for (BanGiaoXe g : theoLenh) {
            if (g.getMaXe().equals(maXe)) {
                ds.add(g);
            }
        }
        return ds;
    }

    /** Thực hiện cung thao tac cho thành phần hiện tại. */
    private boolean cungThaoTac(BanGiaoXe g, TaiKhoan tk, String ma, CanCuBanGiao canCu, String nhan, LocalDateTime luc,
            String tinhTrang, String taiSan, String ghiChu, String hinhThuc, boolean xacNhan) {
        return g != null && xacNhan && g.getMaLenh().equals(ma) && g.getNguoiGiao().equals(tk.getTenDangNhap())
                && g.getCanCu().cungNoiDung(canCu) && g.getNguoiNhan().equals(chu(nhan))
                && g.getThoiDiemGiao().equals(luc) && g.getTinhTrang().equals(chu(tinhTrang))
                && g.getTaiSanDaTra().equals(chu(taiSan)) && g.getGhiChu().equals(chu(ghiChu))
                && g.getHinhThucXacNhan().equals(chu(hinhThuc));
    }

    /** Thực hiện chu cho thành phần hiện tại. */
    private String chu(String s) {
        return s == null ? "" : s.trim();
    }

    /** Thực hiện quyen cho thành phần hiện tại. */
    private void quyen(TaiKhoan tk) {
        if (tk == null || tk.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chức năng bàn giao dành cho tiếp nhận.");
        }
    }

    /** Tìm bản bàn giao đã lưu của một lệnh. */
    public BanGiaoXe timGiaoNoiBo(String maLenh) {
        for (BanGiaoXe giao : theoLenh) {
            if (giao.getMaLenh().equals(maLenh)) {
                return giao;
            }
        }
        return null;
    }

    /** Tìm chứng từ đã lưu theo mã thao tác để tránh ghi lặp. */
    private String timLenhTheoLuot(String maThaoTac) {
        for (String[] dong : theoThaoTac) {
            if (dong[0].equals(maThaoTac)) {
                return dong[1];
            }
        }
        return null;
    }
}
