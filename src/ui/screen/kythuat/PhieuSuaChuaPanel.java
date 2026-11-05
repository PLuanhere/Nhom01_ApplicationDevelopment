package ui.screen.kythuat;

import dao.BaoCaoKiemTraDAO;
import dao.PhuTungSuDungDAO;
import dao.KyThuatDAO;
import dao.PhieuSuaChuaDAO;
import entity.TaiKhoan;
import entity.PhieuSuaChua;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import static ui.component.GiaoDienKyThuat.bocBang;
import static ui.component.GiaoDienKyThuat.dinhDangCombo;
import static ui.component.GiaoDienKyThuat.taoBang;
import static ui.component.GiaoDienKyThuat.taoMatPhang;
import static ui.component.GiaoDienKyThuat.taoModel;
import static ui.component.GiaoDienKyThuat.taoNhan;
import static ui.component.GiaoDienKyThuat.taoNut;
import static ui.component.GiaoDienKyThuat.taoThe;
import static ui.component.GiaoDienKyThuat.themThanhPhan;

// Hiển thị danh sách công việc được phân công cho kỹ thuật viên.
public final class PhieuSuaChuaPanel extends TrangKyThuatPanel {
    private static final long serialVersionUID = 1L;

    PhieuSuaChuaPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuSuaChuaPanel(JFrame winChu, TaiKhoan taiKhoan) {
        this(winChu, taiKhoan, new PhieuSuaChuaDAO());
    }

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public PhieuSuaChuaPanel(JFrame winChu, TaiKhoan taiKhoan, PhieuSuaChuaDAO nguonChung) {
        this(new KhongGianKyThuatPanel(winChu, taiKhoan, new ActionListener() {
            // Màn xem riêng không điều hướng đăng xuất.
            @Override
            public void actionPerformed(ActionEvent event) {
            }
        }, new KyThuatDAO(taiKhoan, nguonChung, new BaoCaoKiemTraDAO(), new PhuTungSuDungDAO())));
        actionPerformed(new ActionEvent(this, 0, "DANH_SACH"));
    }

    // Mở danh sách công việc khi nhận yêu cầu từ MainFrame.
    public void actionPerformed(ActionEvent suKien) {
        if ("DANH_SACH".equals(suKien.getActionCommand())) {
            khongGian.chuyen("PHAN_CONG");
            removeAll();
            add(khongGian.pnlNoiDung, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    // Hiển thị danh sách lệnh và các tiêu chí tìm kiếm.
    void hienDanhSachLenh(String tuKhoa) {
        khongGian.batDauTrang("PHAN_CONG");
        JPanel pnlTrang = taoTrang("Công việc được phân công",
                "Chỉ hiển thị xe/phiếu trong phạm vi phân công.", null, null);
        JTextField txtTim = khongGian.taoONhap(tuKhoa, false);
        txtTim.putClientProperty("goiY", "Tìm mã lệnh hoặc biển số...");
        txtTim.setPreferredSize(new Dimension(380, 36));
        JComboBox<String> cmbLoc = new JComboBox<String>(new String[] {
                "Tất cả", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"});
        dinhDangCombo(cmbLoc);
        cmbLoc.setPreferredSize(new Dimension(190, 36));
        DefaultTableModel model = taoModel("Mã lệnh", "Biển số", "Nội dung", "Hạn", "Trạng thái", "Thao tác");
        JTable tblBang = taoBang(model);
        List<PhieuSuaChua> cacDong = new ArrayList<PhieuSuaChua>();
        JLabel lblSo = taoNhan("", false);
        ActionListener loc = new ActionListener() {
            // Nạp danh sách theo từ khóa và trạng thái đang chọn.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                cacDong.clear();
                cacDong.addAll(khongGian.dao.docLenh(txtTim.getText().trim(), (String) cmbLoc.getSelectedItem()));
                model.setRowCount(0);
                for (PhieuSuaChua lenh : cacDong) {
                    model.addRow(new Object[] {lenh.getMaPhieu(), lenh.getBienSo(), lenh.getGhiChu(),
                            lenh.getHanHoanThanh().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                            khongGian.dao.docTrangThai(lenh), "Xem chi tiết"});
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
            // Mở lệnh khi bấm thao tác hoặc nhấp đôi vào dòng dữ liệu.
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());
                if (dong >= 0 && (tblBang.columnAtPoint(suKien.getPoint()) == 5 || suKien.getClickCount() == 2)) {
                    khongGian.maLenh = cacDong.get(tblBang.convertRowIndexToModel(dong)).getMaPhieu();
                    khongGian.viTriHangMuc = 0;
                    khongGian.chuyen("TIEN_DO");
                }
            }
        });
        JPanel pnlBang = new JPanel(new BorderLayout(0, 12));
        pnlBang.setOpaque(false);
        pnlBang.add(bocBang(tblBang));
        pnlBang.add(lblSo, BorderLayout.SOUTH);
        loc.actionPerformed(new ActionEvent(this, 0, "LOC"));
        themThanhPhan(pnlTrang, taoThe("Danh sách công việc được phân công",
                "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.", pnlBang));
        hienTrang(pnlTrang);
    }
}
