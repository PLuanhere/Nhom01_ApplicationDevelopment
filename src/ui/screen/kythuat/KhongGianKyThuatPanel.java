package ui.screen.kythuat;

import dao.KyThuatDAO;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.TaiKhoan;
import entity.PhuTung;
import entity.PhieuSuaChua.TrangThaiSuaChua;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import ui.MainFrame;
import ui.component.KyThuatNavigationPanel;
import ui.component.GiaoDienKyThuat;
import ui.component.PhuTungUi;
import ui.component.UiChung;
import ui.dialog.kythuat.DoiChieuKyThuatDialog;

// Giữ trạng thái biểu mẫu và xử lý thao tác; MainFrame quản lý khung và điều hướng.
public final class KhongGianKyThuatPanel implements ActionListener, DocumentListener {

    final MainFrame winChu;
    final TaiKhoan taiKhoan;
    final ActionListener dangXuat;
    final KyThuatDAO dao;

    final List<JTextField> truongDangNhap = new ArrayList<JTextField>();

    String trang = "TONG_QUAN";
    String maLenh;
    int viTriHangMuc;

    BaoCaoKiemTra baoCao;
    PhieuTiepNhan phieu;
    DefaultTableModel modelNhap;
    JTable tblNhap;

    JTextField txtLoi;
    JTextField txtDeXuat;
    JTextField txtKetLuan;
    JTextField txtKetQua;
    JTextField txtPhatSinh;
    JTextField txtPhuTungDuKien;
    JTextField txtMoTa;
    JTextField txtLuong;
    JTextField txtTon;
    JTextField txtTonSau;

    JComboBox<TrangThaiSuaChua> cmbTienDo;
    JComboBox<String> cmbHangMuc;
    JComboBox<PhuTung> cmbPhuTung;
    JButton btnHoanTat;

    String maLuot;
    String anhChup = "";
    boolean dangNap;

    // Các tham chiếu này dùng lại panel do MainFrame tạo, không tự tạo bộ panel khác.
    TrangChuKyThuatPanel pnlTongQuanCongViec;
    PhieuSuaChuaPanel pnlCongViecPhanCong;
    BaoCaoKiemTraPanel pnlKiemTraXe;
    TienDoSuaChuaPanel pnlTienDoSuaChua;
    BaoHuHongPhatSinhPanel pnlBaoHuHongPhatSinh;
    PhuTungSuDungPanel pnlGhiNhanPhuTung;
    KiemTraHoanTatPanel pnlKiemTraHoanTat;

    public KhongGianKyThuatPanel(
            MainFrame winChu, TaiKhoan taiKhoan, ActionListener dangXuat, KyThuatDAO daoChung) {

        if (winChu == null || taiKhoan == null || dangXuat == null || daoChung == null) {
            throw new IllegalArgumentException("Thiếu khung chính, tài khoản hoặc nguồn kỹ thuật.");
        }

        taiKhoan.kiemTraPhien();

        if (taiKhoan.getVaiTro() != TaiKhoan.VaiTro.NHAN_VIEN_KY_THUAT) {
            throw new SecurityException("Chỉ dành cho kỹ thuật viên.");
        }

        this.winChu = winChu;
        this.taiKhoan = taiKhoan;
        this.dangXuat = dangXuat;
        this.dao = daoChung;
    }

    // Nhận bộ panel dùng chung do MainFrame khởi tạo và giữ trong các field.
    public void ganCacTrang(
            TrangChuKyThuatPanel tongQuan,
            PhieuSuaChuaPanel phanCong,
            BaoCaoKiemTraPanel kiemTra,
            TienDoSuaChuaPanel tienDo,
            PhuTungSuDungPanel phuTung,
            BaoHuHongPhatSinhPanel phatSinh,
            KiemTraHoanTatPanel hoanTat) {

        if (tongQuan == null || phanCong == null || kiemTra == null || tienDo == null
                || phuTung == null || phatSinh == null || hoanTat == null) {
            throw new IllegalArgumentException("Cần đủ bảy trang kỹ thuật.");
        }

        pnlTongQuanCongViec = tongQuan;
        pnlCongViecPhanCong = phanCong;
        pnlKiemTraXe = kiemTra;
        pnlTienDoSuaChua = tienDo;
        pnlGhiNhanPhuTung = phuTung;
        pnlBaoHuHongPhatSinh = phatSinh;
        pnlKiemTraHoanTat = hoanTat;
    }

    public String getMaLenh() {
        return maLenh;
    }

    // Xử lý các tác vụ kỹ thuật và giữ biểu mẫu nếu thao tác dữ liệu thất bại.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        String lenh = suKien.getActionCommand();

        try {
            if (KyThuatNavigationPanel.coMuc(lenh)) {
                chuyen(lenh);
                return;
            }

            if ("DANG_XUAT".equals(lenh)) {
                yeuCauDangXuat();
            } else if ("THOAT".equals(lenh)) {
                yeuCauThoat();
            } else if ("TAO_BAO_CAO".equals(lenh)) {
                pnlKiemTraXe.chonPhieu();
            } else if ("HUONG_DAN".equals(lenh)) {
                baoLoi("Hướng dẫn sử dụng",
                        "Chọn công việc được phân công để mở lệnh. "
                                + "Chỉ cập nhật hạng mục đã được duyệt; "
                                + "ghi nhận phụ tùng thực dùng trước khi hoàn tất.");
            } else if ("SUA_BAO_CAO".equals(lenh) && choPhepRoiTrang()) {
                batDauTrang("KIEM_TRA");
                pnlKiemTraXe.hienLapBaoCao();
            } else if ("XOA_BAO_CAO".equals(lenh)) {
                if (xacNhan(
                        "Xác nhận xóa",
                        "Xóa báo cáo nháp " + baoCao.getMaBaoCao() + "?",
                        new String[0][0],
                        "Chỉ xóa bản nháp chưa hoàn tất.",
                        "Xóa",
                        true)) {

                    dao.xoaBaoCao(baoCao);
                    batDauTrang("KIEM_TRA");
                    pnlKiemTraXe.hienDanhSachBaoCao();
                }
            } else if ("DOI_CHIEU".equals(lenh)) {
                pnlKiemTraXe.luuBanNhap();
            } else if ("HOAN_TAT_BAO_CAO".equals(lenh)) {
                pnlKiemTraXe.hoanTatBaoCao();
            } else if ("LUU_TIEN_DO".equals(lenh)) {
                dao.luuTienDo(
                        maLenh, viTriHangMuc,
                        (TrangThaiSuaChua) cmbTienDo.getSelectedItem(),
                        txtKetQua.getText());

                anhChup = docNoiDung();
                chuyen("TIEN_DO");
            } else if ("GUI_PHAT_SINH".equals(lenh)) {
                dao.guiPhatSinh(
                        maLenh, txtPhatSinh.getText(),
                        txtPhuTungDuKien.getText(), txtMoTa.getText());

                anhChup = docNoiDung();
                chuyen("PHAT_SINH");
            } else if ("DOI_HANG_MUC".equals(lenh) && !dangNap) {
                pnlGhiNhanPhuTung.napPhuTung();
            } else if ("DOI_PHU_TUNG".equals(lenh) && !dangNap) {
                pnlGhiNhanPhuTung.capNhatTon();
            } else if ("GHI_PHU_TUNG".equals(lenh)) {
                pnlGhiNhanPhuTung.ghiPhuTung();
            } else if ("HOAN_TAT_LENH".equals(lenh)) {
                pnlKiemTraHoanTat.hoanTatLenh();
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            baoLoi("Không thể thực hiện", loi.getMessage());
        }
    }

    // Kiểm tra nội dung chưa lưu trước khi rời biểu mẫu kỹ thuật.
    boolean duocRoiTrang() {
        return choPhepRoiTrang();
    }

    // Cho MainFrame kiểm tra nội dung chưa lưu khi chuyển trang, đóng hoặc đăng xuất.
    public boolean choPhepRoiTrang() {
        if (tblNhap != null && tblNhap.isEditing()
                && !tblNhap.getCellEditor().stopCellEditing()) {
            return false;
        }

        if (anhChup.equals(docNoiDung())) {
            return true;
        }

        return xacNhan(
                "Bỏ thay đổi chưa lưu?",
                "Nội dung đang nhập chưa được lưu.",
                new String[0][0],
                "Nếu bỏ thay đổi, dữ liệu đã lưu vẫn được giữ nguyên.",
                "Bỏ thay đổi",
                true);
    }

    // Gửi yêu cầu điều hướng từ nút hoặc đường tắt về MainFrame.
    void chuyen(String tenTrang) {
        winChu.dieuHuongKyThuat(tenTrang);
    }

    // Xóa trạng thái nhập cũ và nhờ MainFrame đồng bộ mục đang chọn.
    public void batDauTrang(String tenTrang) {
        trang = tenTrang;
        truongDangNhap.clear();
        modelNhap = null;
        tblNhap = null;
        btnHoanTat = null;
        anhChup = "";

        winChu.capNhatMucKyThuat(tenTrang);
    }

    // Bọc nội dung trong vùng cuộn rồi nhờ MainFrame hiển thị panel được cấp.
    void hienTrang(JPanel pnlManHinh, JPanel pnlCot) {
        JScrollPane scrTrang = UiChung.taoVungCuon(pnlCot);
        PhuTungUi.dinhDangVungCuon(scrTrang);

        scrTrang.setBorder(BorderFactory.createEmptyBorder());
        scrTrang.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        pnlManHinh.removeAll();
        pnlManHinh.add(scrTrang, BorderLayout.CENTER);

        anhChup = docNoiDung();
        winChu.hienTrangKyThuat(pnlManHinh);
    }

    // Tạo ô nhập thông thường hoặc chỉ đọc bằng component chung.
    JTextField taoONhap(String noiDung, boolean chiDoc) {
        return GiaoDienKyThuat.taoONhap(noiDung, chiDoc);
    }

    // Tạo ô nhập được theo dõi để phát hiện thay đổi chưa lưu.
    JTextField taoONhap(String noiDung) {
        JTextField txtNhap = taoONhap(noiDung, false);
        truongDangNhap.add(txtNhap);
        txtNhap.getDocument().addDocumentListener(this);
        return txtNhap;
    }

    // Đọc phụ tùng thực dùng và hiển thị thông báo khi chưa được phép thực hiện.
    String docThucDungAnToan(int viTri) {
        try {
            return dao.docThucDung(maLenh, viTri);
        } catch (IllegalStateException loi) {
            return "Chưa được phép thực hiện";
        }
    }

    // Chụp nội dung biểu mẫu để phát hiện các thay đổi chưa được lưu.
    String docNoiDung() {
        StringBuilder noiDung = new StringBuilder();

        for (JTextField txtNhap : truongDangNhap) {
            noiDung.append(txtNhap.getText()).append('\n');
        }

        if ("TIEN_DO".equals(trang) && cmbTienDo != null) {
            noiDung.append(cmbTienDo.getSelectedItem());
        }

        if ("PHU_TUNG".equals(trang) && cmbHangMuc != null && cmbPhuTung != null) {
            noiDung.append(cmbHangMuc.getSelectedIndex());
            noiDung.append(cmbPhuTung.getSelectedItem());
        }

        if (modelNhap != null) {
            for (int dong = 0; dong < modelNhap.getRowCount(); dong++) {
                for (int cot = 0; cot < modelNhap.getColumnCount(); cot++) {
                    noiDung.append(modelNhap.getValueAt(dong, cot)).append('\n');
                }
            }
        }

        return noiDung.toString();
    }

    // Mở hộp thoại đối chiếu và trả về lựa chọn xác nhận.
    boolean xacNhan(
            String tieuDe, String moTa, String[][] thongTin,
            String canhBao, String nhanNut, boolean nguyHiem) {

        DoiChieuKyThuatDialog winDialog = new DoiChieuKyThuatDialog(
                winChu, tieuDe, moTa, thongTin, canhBao, nhanNut, nguyHiem);

        winDialog.setVisible(true);
        return winDialog.isDongY();
    }

    // Hiển thị thông báo bằng hộp thoại kỹ thuật đang có.
    void baoLoi(String tieuDe, String noiDung) {
        xacNhan(tieuDe, noiDung, new String[0][0], "", null, false);
    }

    // Tính lại dữ liệu phụ thuộc khi thêm ký tự vào biểu mẫu.
    @Override
    public void insertUpdate(DocumentEvent suKien) {
        pnlGhiNhanPhuTung.capNhatTon();
        pnlKiemTraHoanTat.capNhatHoanTat();
    }

    // Tính lại dữ liệu phụ thuộc khi xóa ký tự khỏi biểu mẫu.
    @Override
    public void removeUpdate(DocumentEvent suKien) {
        pnlGhiNhanPhuTung.capNhatTon();
        pnlKiemTraHoanTat.capNhatHoanTat();
    }

    // Tính lại dữ liệu phụ thuộc khi thuộc tính tài liệu thay đổi.
    @Override
    public void changedUpdate(DocumentEvent suKien) {
        pnlGhiNhanPhuTung.capNhatTon();
        pnlKiemTraHoanTat.capNhatHoanTat();
    }

    // Gửi yêu cầu đăng xuất qua luồng kiểm tra dữ liệu của MainFrame.
    private void yeuCauDangXuat() {
        dangXuat.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "dangXuat"));
    }

    // Gửi yêu cầu đóng cửa sổ để MainFrame kiểm tra nội dung chưa lưu.
    private void yeuCauThoat() {
        if (winChu != null) {
            winChu.dispatchEvent(new WindowEvent(winChu, WindowEvent.WINDOW_CLOSING));
        }
    }

    // Giữ ô tìm, bộ lọc và nút làm mới trên cùng một hàng co giãn.
    private JPanel taoThanhLoc(
            JTextField txtTim, JComboBox<?> cmbLoc, ActionListener loc) {

        JPanel pnl = new JPanel(new BorderLayout(8, 0));
        pnl.setOpaque(false);

        JPanel pnlPhai = new JPanel(new BorderLayout(8, 0));
        pnlPhai.setOpaque(false);
        pnlPhai.add(cmbLoc, BorderLayout.CENTER);
        pnlPhai.add(
                GiaoDienKyThuat.taoNut("Làm mới", "LOC", false, loc),
                BorderLayout.EAST);

        pnl.add(txtTim, BorderLayout.CENTER);
        pnl.add(pnlPhai, BorderLayout.EAST);

        return pnl;
    }
}