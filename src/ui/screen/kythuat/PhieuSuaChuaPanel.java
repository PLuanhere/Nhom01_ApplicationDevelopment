package ui.screen.kythuat;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import static ui.component.GiaoDienKyThuat.*;

// Hiển thị danh sách công việc được phân công cho kỹ thuật viên.
public final class PhieuSuaChuaPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public PhieuSuaChuaPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Hiển thị danh sách lệnh và các tiêu chí tìm kiếm.
    public void hienDanhSachLenh(String tuKhoa) {
        khongGian.batDauTrang("PHAN_CONG");

        JPanel pnlTrang = taoTrang(
                "Công việc được phân công",
                "Chỉ hiển thị xe/phiếu trong phạm vi phân công.",
                null,
                null);

        JTextField txtTim = khongGian.taoONhap(tuKhoa, false);
        txtTim.putClientProperty("goiY", "Tìm mã lệnh hoặc biển số...");
        txtTim.setPreferredSize(new Dimension(380, 36));

        JComboBox<String> cmbLoc = new JComboBox<String>(new String[] {
                "Tất cả", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành"
        });

        dinhDangCombo(cmbLoc);
        cmbLoc.setPreferredSize(new Dimension(190, 36));

        DefaultTableModel model = taoModel(
                "Mã lệnh", "Biển số", "Nội dung", "Hạn", "Trạng thái", "Thao tác");

        JTable tblBang = taoBang(model);
        List<PhieuSuaChua> cacDong = new ArrayList<PhieuSuaChua>();
        JLabel lblSo = taoNhan("", false);

        ActionListener loc = new ActionListener() {
            // Nạp danh sách theo từ khóa và trạng thái đang chọn.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                cacDong.clear();
                cacDong.addAll(khongGian.dao.docLenh(
                        txtTim.getText().trim(), (String) cmbLoc.getSelectedItem()));

                model.setRowCount(0);

                for (PhieuSuaChua lenh : cacDong) {
                    model.addRow(new Object[] {
                            lenh.getMaPhieu(),
                            lenh.getBienSo(),
                            lenh.getGhiChu(),
                            lenh.getHanHoanThanh().format(
                                    DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                            khongGian.dao.docTrangThai(lenh),
                            "Xem chi tiết"
                    });
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
            // Chọn lệnh và mở tiến độ khi bấm thao tác hoặc nhấp đôi vào dòng.
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());

                if (dong >= 0
                        && (tblBang.columnAtPoint(suKien.getPoint()) == 5
                                || suKien.getClickCount() == 2)) {

                    int viTri = tblBang.convertRowIndexToModel(dong);
                    khongGian.maLenh = cacDong.get(viTri).getMaPhieu();
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

        themThanhPhan(pnlTrang, taoThe(
                "Danh sách công việc được phân công",
                "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.",
                pnlBang));

        hienTrang(pnlTrang);
    }
}