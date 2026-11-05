package ui.screen.kythuat;

import dao.BaoCaoKiemTraDAO;
import dao.PhuTungSuDungDAO;
import dao.KyThuatDAO;
import dao.PhieuSuaChuaDAO;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.TaiKhoan;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import static ui.component.GiaoDienKyThuat.CHU_THUONG;
import static ui.component.GiaoDienKyThuat.bocBang;
import static ui.component.GiaoDienKyThuat.dinhDangCombo;
import static ui.component.GiaoDienKyThuat.taoBang;
import static ui.component.GiaoDienKyThuat.taoCot;
import static ui.component.GiaoDienKyThuat.taoHangNut;
import static ui.component.GiaoDienKyThuat.taoMatPhang;
import static ui.component.GiaoDienKyThuat.taoModel;
import static ui.component.GiaoDienKyThuat.taoNhan;
import static ui.component.GiaoDienKyThuat.taoNut;
import static ui.component.GiaoDienKyThuat.taoThe;
import static ui.component.GiaoDienKyThuat.taoThongBao;
import static ui.component.GiaoDienKyThuat.taoTruong;
import static ui.component.GiaoDienKyThuat.themThanhPhan;

// Hiển thị danh sách, lập báo cáo và đối chiếu kết quả kiểm tra.
public final class BaoCaoKiemTraPanel extends TrangKyThuatPanel {
    private static final long serialVersionUID = 1L;

    BaoCaoKiemTraPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BaoCaoKiemTraPanel(JFrame winChu, TaiKhoan taiKhoan) {
        this(winChu, taiKhoan, new BaoCaoKiemTraDAO());
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BaoCaoKiemTraPanel(JFrame winChu, TaiKhoan taiKhoan, BaoCaoKiemTraDAO nguonChung) {
        this(new KhongGianKyThuatPanel(winChu, taiKhoan, new ActionListener() {
            // Constructor xem riêng báo cáo không điều hướng đăng xuất.
            @Override
            public void actionPerformed(ActionEvent event) {
            }
        }, new KyThuatDAO(taiKhoan, new PhieuSuaChuaDAO(), nguonChung, new PhuTungSuDungDAO())));
        actionPerformed(new ActionEvent(this, 0, "DANH_SACH"));
    }

    // Mở danh sách báo cáo khi nhận yêu cầu từ MainFrame.
    public void actionPerformed(ActionEvent suKien) {
        if ("DANH_SACH".equals(suKien.getActionCommand())) {
            khongGian.chuyen("KIEM_TRA");
            removeAll();
            add(khongGian.pnlNoiDung, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    // Hiển thị danh sách báo cáo và bộ lọc tìm kiếm.
    void hienDanhSachBaoCao() {
        JPanel pnlTrang = taoTrang("Báo cáo kiểm tra",
                "Kiểm tra xe có phiếu tiếp nhận hợp lệ và ghi kết quả chẩn đoán.",
                "+ Lập báo cáo kiểm tra", "TAO_BAO_CAO");
        JTextField txtTim = khongGian.taoONhap("", false);
        txtTim.putClientProperty("goiY", "Tìm mã báo cáo, phiếu, biển số...");
        txtTim.setPreferredSize(new Dimension(380, 36));
        JComboBox<String> cmbLoc = new JComboBox<String>(new String[] {"Tất cả", "Nháp", "Hoàn tất"});
        dinhDangCombo(cmbLoc);
        cmbLoc.setPreferredSize(new Dimension(190, 36));
        DefaultTableModel model = taoModel("Mã báo cáo", "Phiếu tiếp nhận", "Biển số",
                "Người kiểm tra", "Trạng thái", "Thao tác");
        JTable tblBang = taoBang(model);
        List<BaoCaoKiemTra> cacDong = new ArrayList<BaoCaoKiemTra>();
        JLabel lblSo = taoNhan("", false);
        ActionListener loc = new ActionListener() {
            // Đọc báo cáo phù hợp với tiêu chí đang chọn.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                cacDong.clear();
                cacDong.addAll(khongGian.dao.docBaoCao(txtTim.getText().trim(), (String) cmbLoc.getSelectedItem()));
                model.setRowCount(0);
                for (BaoCaoKiemTra dong : cacDong) {
                    model.addRow(new Object[] {dong.getMaBaoCao(), dong.getPhieu().getMaPhieu(),
                            dong.getPhieu().getBienSo(), khongGian.taiKhoan.getTenDangNhap(),
                            dong.getTrangThai() == TrangThaiKiemTra.HOAN_TAT
                                    ? "Đã hoàn tất" : "Chưa xác nhận", "Xem chi tiết"});
                }
                lblSo.setText("Hiển thị " + cacDong.size() + " / " + cacDong.size() + " hồ sơ");
            }
        };
        txtTim.addActionListener(loc);
        cmbLoc.addActionListener(loc);
        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(txtTim);
        pnlLoc.add(cmbLoc);
        pnlLoc.add(taoNut("Làm mới", "LOC", false, loc));
        JPanel pnlThanh = taoMatPhang();
        pnlThanh.add(pnlLoc);
        themThanhPhan(pnlTrang, pnlThanh);
        tblBang.addMouseListener(new MouseAdapter() {
            // Mở trang đối chiếu của báo cáo được chọn.
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());
                if (dong >= 0 && (tblBang.columnAtPoint(suKien.getPoint()) == 5 || suKien.getClickCount() == 2)) {
                    khongGian.baoCao = cacDong.get(tblBang.convertRowIndexToModel(dong));
                    khongGian.phieu = khongGian.baoCao.getPhieu();
                    khongGian.batDauTrang("KIEM_TRA");
                    hienDoiChieu();
                }
            }
        });
        JPanel pnlBang = new JPanel(new BorderLayout(0, 10));
        pnlBang.setOpaque(false);
        pnlBang.add(bocBang(tblBang));
        pnlBang.add(lblSo, BorderLayout.SOUTH);
        loc.actionPerformed(new ActionEvent(this, 0, "LOC"));
        themThanhPhan(pnlTrang, taoThe("Danh sách báo cáo kiểm tra",
                "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.", pnlBang));
        hienTrang(pnlTrang);
    }

    // Cho chọn phiếu đã xác nhận và chưa có báo cáo hoàn tất.
    void chonPhieu() {
        if (!khongGian.duocRoiTrang()) {
            return;
        }
        List<PhieuTiepNhan> cacPhieu = new ArrayList<PhieuTiepNhan>();
        for (PhieuTiepNhan dong : khongGian.dao.docPhieu()) {
            if (dong.isDaXacNhan()) {
                BaoCaoKiemTra cu = khongGian.dao.docBaoCao(dong);
                if (cu == null || cu.getTrangThai() == TrangThaiKiemTra.NHAP) {
                    cacPhieu.add(dong);
                }
            }
        }
        if (cacPhieu.isEmpty()) {
            khongGian.baoLoi("Không có phiếu phù hợp",
                    "Không còn phiếu hợp lệ đang chờ kiểm tra trong phạm vi phân công.");
            return;

        }
        JPopupMenu mnuPhieu = new JPopupMenu();
        for (PhieuTiepNhan dong : cacPhieu) {
            JMenuItem mniPhieu = new JMenuItem(dong.getMaPhieu() + " · " + dong.getBienSo());
            mniPhieu.setFont(CHU_THUONG);
            mniPhieu.addActionListener(new ActionListener() {
                // Mở biểu mẫu kiểm tra cho phiếu được chọn.
                @Override
                public void actionPerformed(ActionEvent suKien) {
                    moPhieu(dong);
                }
            });
            mnuPhieu.add(mniPhieu);
        }
        mnuPhieu.show(khongGian.pnlNoiDung, 50, 110);
    }

    // Nạp phiếu tiếp nhận và báo cáo nháp vào biểu mẫu.
    void moPhieu(PhieuTiepNhan dong) {
        if (!khongGian.duocRoiTrang()) {
            return;
        }
        khongGian.phieu = dong;
        khongGian.baoCao = khongGian.dao.docBaoCao(dong);
        khongGian.batDauTrang("KIEM_TRA");
        hienLapBaoCao();
    }

    // Hiển thị biểu mẫu nhập kết quả và đề xuất theo khu vực.
    void hienLapBaoCao() {
        JPanel pnlTrang = taoTrang("Lập báo cáo kiểm tra xe",
                "Kết quả phải đối chiếu với tình trạng ban đầu và yêu cầu khách hàng.",
                "Danh sách / quay lại", "KIEM_TRA");
        themThanhPhan(pnlTrang, taoThanhBuoc("Nhận phiếu", "Ghi kết quả", "Đối chiếu & lưu"));
        JPanel pnlCot = taoCot();
        themThanhPhan(pnlCot, taoHang(
                taoGiaTri("Phiếu tiếp nhận / xe",
                        khongGian.phieu.getMaPhieu() + " · " + khongGian.phieu.getBienSo()),
                taoGiaTri("Người kiểm tra", khongGian.taiKhoan.getTenDangNhap())));
        themThanhPhan(pnlCot, taoThongBao("Thông tin gốc từ tiếp nhận: "
                + khongGian.phieu.getYeuCau() + " · " + khongGian.phieu.getTinhTrang(), false));
        khongGian.modelNhap = new DefaultTableModel(
                new String[] {"Khu vực", "Kết quả kiểm tra", "Kết luận / đề xuất"}, 0) {
            private static final long serialVersionUID = 1L;

            // Chỉ cho chỉnh sửa kết quả và đề xuất của khu vực.
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return cot > 0;
            }
        };
        String[] noiDung = khongGian.baoCao == null
                ? new String[] {"", "", "", "", "", "", ""} : khongGian.baoCao.getNoiDung();
        String[] deXuat = khongGian.baoCao == null
                ? new String[] {"", "", ""} : khongGian.baoCao.getDeXuatTheoVung();
        String[] khuVuc = {"Phanh", "Khoang máy / dầu", "Điện – điều hòa"};
        for (int viTri = 0; viTri < 3; viTri++) {
            khongGian.modelNhap.addRow(new Object[] {khuVuc[viTri], noiDung[viTri + 3], deXuat[viTri]});
        }
        khongGian.tblNhap = taoBang(khongGian.modelNhap);
        themThanhPhan(pnlCot, bocBang(khongGian.tblNhap));
        khongGian.txtLoi = khongGian.taoONhap(noiDung[1]);
        khongGian.txtDeXuat = khongGian.taoONhap(noiDung[2]);
        themThanhPhan(pnlCot, taoHang(taoTruong("Lỗi phát hiện *", khongGian.txtLoi),
                taoTruong("Hạng mục đề xuất *", khongGian.txtDeXuat)));
        pnlCot.add(taoHangNut(taoNut("Đối chiếu & lưu báo cáo", "DOI_CHIEU", true, khongGian)));
        themThanhPhan(pnlTrang, taoThe(
                "Lập báo cáo kiểm tra " + (khongGian.baoCao == null ? "mới" : khongGian.baoCao.getMaBaoCao()),
                "Tự lấy thông tin tiếp nhận, chỉ nhập kết quả chẩn đoán và hạng mục đề xuất.", pnlCot));
        hienTrang(pnlTrang);
    }

    // Lưu bản nháp từ biểu mẫu rồi chuyển sang trang đối chiếu.
    void luuBanNhap() {
        if (khongGian.tblNhap.isEditing() && !khongGian.tblNhap.getCellEditor().stopCellEditing()) {
            return;
        }
        String[] noiDung = {khongGian.txtLoi.getText(), khongGian.txtLoi.getText(),
                khongGian.txtDeXuat.getText(), "", "", "", ""};
        String[] deXuat = new String[3];
        for (int viTri = 0; viTri < 3; viTri++) {
            noiDung[viTri + 3] = String.valueOf(khongGian.modelNhap.getValueAt(viTri, 1)).trim();
            deXuat[viTri] = String.valueOf(khongGian.modelNhap.getValueAt(viTri, 2)).trim();
        }
        BaoCaoKiemTra nhap = new BaoCaoKiemTra(
                khongGian.baoCao == null ? "" : khongGian.baoCao.getMaBaoCao(), khongGian.phieu,
                khongGian.baoCao == null ? LocalDate.now() : khongGian.baoCao.getNgayKiemTra(),
                "Tổng hợp", noiDung, TrangThaiKiemTra.NHAP, null, deXuat);
        khongGian.baoCao = khongGian.dao.luuBaoCao(nhap, false);
        khongGian.batDauTrang("KIEM_TRA");
        hienDoiChieu();
    }

    // Hiển thị nội dung đối chiếu và các hành động của báo cáo.
    void hienDoiChieu() {
        JPanel pnlTrang = taoTrang("Đối chiếu báo cáo kiểm tra",
                "Kiểm tra đầy đủ kết quả trước khi chuyển cho tiếp nhận lập báo giá.",
                "Danh sách / quay lại", "KIEM_TRA");
        JPanel pnlTrai = taoCot();
        themThanhPhan(pnlTrai, taoHang(taoGiaTri("Phiếu tiếp nhận", khongGian.phieu.getMaPhieu()),
                taoGiaTri("Biển số", khongGian.phieu.getBienSo()),
                taoGiaTri("Người kiểm tra", khongGian.taiKhoan.getTenDangNhap())));
        DefaultTableModel model = taoModel("Khu vực", "Kết quả kiểm tra", "Đề xuất");
        String[] ten = {"Hệ thống phanh", "Khoang máy / dầu", "Điện / điều hòa"};
        for (int viTri = 0; viTri < 3; viTri++) {
            model.addRow(new Object[] {ten[viTri], khongGian.baoCao.getNoiDung()[viTri + 3],
                    khongGian.baoCao.getDeXuatTheoVung()[viTri]});
        }
        themThanhPhan(pnlTrai, bocBang(taoBang(model)));
        pnlTrai.add(taoThongBao(
                "Đề xuất là cơ sở để tiếp nhận lập báo giá; chưa cho phép sửa khi khách chưa duyệt.", true));
        JPanel pnlPhai = taoCot();
        themThanhPhan(pnlPhai, taoTruong("Tình trạng khi nhận · chỉ đọc",
                khongGian.taoONhap(khongGian.phieu.getTinhTrang(), true)));
        boolean daXong = khongGian.baoCao.getTrangThai() == TrangThaiKiemTra.HOAN_TAT;
        khongGian.txtKetLuan = daXong
                ? khongGian.taoONhap(khongGian.baoCao.getNoiDung()[0], true)
                : khongGian.taoONhap(khongGian.baoCao.getNoiDung()[0]);
        themThanhPhan(pnlPhai, taoTruong("Kết luận kiểm tra", khongGian.txtKetLuan));
        themThanhPhan(pnlPhai, taoTruong("Trạng thái báo cáo · chỉ đọc",
                khongGian.taoONhap(daXong ? "Đã hoàn tất" : "Chưa xác nhận", true)));
        themThanhPhan(pnlPhai, taoThongBao(
                "Chỉ hoàn tất khi nội dung kiểm tra đúng. Chuyển kết quả cho Tiếp nhận lập báo giá.", true));
        JButton btnXong = taoNut("Hoàn tất báo cáo", "HOAN_TAT_BAO_CAO", true, khongGian);
        btnXong.setEnabled(!daXong);
        pnlPhai.add(btnXong);
        if (!daXong) {
            pnlPhai.add(Box.createVerticalStrut(10));
            pnlPhai.add(taoNut("Sửa bản nháp", "SUA_BAO_CAO", false, khongGian));
            pnlPhai.add(Box.createVerticalStrut(10));
            JButton btnXoa = taoNut("Xóa bản nháp", "XOA_BAO_CAO", false, khongGian);
            btnXoa.setForeground(new Color(0xB3261E));
            btnXoa.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xB3261E)),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            pnlPhai.add(btnXoa);
        }
        themThanhPhan(pnlTrang, taoHang(taoThe("Đối chiếu báo cáo " + khongGian.baoCao.getMaBaoCao(),
                "Thông tin chẩn đoán phục vụ lập báo giá; không thay bằng bảng giá.", pnlTrai),
                taoThe("Kiểm tra trước khi gửi", "Đối chiếu kết quả với tình trạng ban đầu.", pnlPhai)));
        hienTrang(pnlTrang);
    }

    // Xác nhận kết quả và lưu báo cáo ở trạng thái hoàn tất.
    void hoanTatBaoCao() {
        if (!khongGian.xacNhan("Đối chiếu báo cáo kiểm tra",
                "Xác nhận kết quả kiểm tra trước khi gửi tiếp nhận.",
                new String[][] {{"Báo cáo", khongGian.baoCao.getMaBaoCao()},
                    {"Phiếu tiếp nhận", khongGian.phieu.getMaPhieu()},
                    {"Biển số", khongGian.phieu.getBienSo()},
                    {"Kết luận", khongGian.txtKetLuan.getText()}},
                "Chỉ hoàn tất khi kết quả kiểm tra đã được đối chiếu.", "Hoàn tất báo cáo", false)) {
            return;
        }
        String[] noiDung = khongGian.baoCao.getNoiDung();
        noiDung[0] = khongGian.txtKetLuan.getText().trim();
        khongGian.baoCao = khongGian.dao.luuBaoCao(new BaoCaoKiemTra(
                khongGian.baoCao.getMaBaoCao(), khongGian.phieu, khongGian.baoCao.getNgayKiemTra(),
                khongGian.baoCao.getBoPhan(), noiDung, TrangThaiKiemTra.NHAP,
                null, khongGian.baoCao.getDeXuatTheoVung()), true);
        khongGian.batDauTrang("KIEM_TRA");
        hienDoiChieu();
    }
}
