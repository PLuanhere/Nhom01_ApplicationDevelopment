package ui.screen.kythuat;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ui.component.KiemTraRoiTrang;

import static ui.screen.kythuat.GiaoDienKyThuat.*;

// Chia sẻ bố cục nội dung và thanh bước giữa các màn hình kỹ thuật.
abstract class TrangKyThuatPanel extends JPanel implements KiemTraRoiTrang {

    private static final long serialVersionUID = 1L;
    protected final KhongGianKyThuatPanel khongGian;

    TrangKyThuatPanel(KhongGianKyThuatPanel khongGian) {
        super(new BorderLayout());
        this.khongGian = khongGian;
    }

    // Kiểm tra dữ liệu chưa lưu trước khi chuyển màn hình.
    @Override
    public boolean choPhepRoiTrang() {
        return khongGian == null || khongGian.choPhepRoiTrang();
    }

    // Gửi nội dung trang về vùng hiển thị chung của MainFrame.
    protected void hienTrang(JPanel pnlTrang) {
        khongGian.hienTrang(this, pnlTrang);
    }

    // Tạo breadcrumb, tiêu đề, mô tả và nút thao tác đầu trang.
    protected JPanel taoTrang(String tieuDe, String moTa, String nut, String lenh) {
        JPanel pnlCot = taoCot();
        pnlCot.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        String nhom = "TONG_QUAN".equals(khongGian.trang) ? "Hệ thống / " : "Nghiệp vụ / ";
        JLabel lblBreadcrumb = taoNhan("Kỹ thuật viên / " + nhom + tieuDe, false);
        lblBreadcrumb.setFont(CHU_NHO);
        lblBreadcrumb.setForeground(CHU_PHU);
        lblBreadcrumb.setPreferredSize(new Dimension(0, 18));
        themThanhPhan(pnlCot, lblBreadcrumb);

        JPanel pnlDau = new JPanel(new BorderLayout(12, 0));
        pnlDau.setOpaque(false);
        JPanel pnlChu = taoCot();

        JLabel lblTen = taoNhan(tieuDe, true);
        lblTen.setFont(TIEU_DE);
        lblTen.setPreferredSize(new Dimension(lblTen.getPreferredSize().width, 30));
        pnlChu.add(lblTen);
        pnlChu.add(Box.createVerticalStrut(5));

        JLabel lblMoTa = taoNhan(moTa, false);
        lblMoTa.setForeground(CHU_PHU);
        lblMoTa.setPreferredSize(new Dimension(lblMoTa.getPreferredSize().width, 20));
        pnlChu.add(lblMoTa);
        pnlDau.add(pnlChu, BorderLayout.CENTER);

        if (nut != null) {
            JPanel pnlNutDau = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnlNutDau.setOpaque(false);
            pnlNutDau.add(taoNut(nut, lenh, "TAO_BAO_CAO".equals(lenh), khongGian));
            pnlDau.add(pnlNutDau, BorderLayout.EAST);
        }

        themThanhPhan(pnlCot, pnlDau);
        return pnlCot;
    }

    // Chia các thành phần thành một hàng có chiều rộng bằng nhau.
    protected JPanel taoHang(JComponent... cacCot) {
        JPanel pnlHang = new JPanel(new GridLayout(1, cacCot.length, 14, 0));
        pnlHang.setOpaque(false);
        for (JComponent cot : cacCot) {
            pnlHang.add(cot);
        }
        return pnlHang;
    }

    // Chia nội dung thành hai cột theo tỷ lệ 59/41 và căn phía trên.
    protected JPanel taoHaiCot(JComponent trai, JComponent phai) {
        JPanel pnlHaiCot = new JPanel(new GridBagLayout());
        pnlHaiCot.setOpaque(false);
        JComponent[] cacCot = {trai, phai};

        for (int viTri = 0; viTri < cacCot.length; viTri++) {
            GridBagConstraints rangBuoc = new GridBagConstraints();
            rangBuoc.gridx = viTri;
            rangBuoc.weightx = viTri == 0 ? 0.59 : 0.41;
            rangBuoc.fill = GridBagConstraints.HORIZONTAL;
            rangBuoc.anchor = GridBagConstraints.NORTH;
            rangBuoc.insets = new Insets(0, viTri == 0 ? 0 : 7, 0, viTri == 0 ? 7 : 0);
            cacCot[viTri].setPreferredSize(new Dimension(0, cacCot[viTri].getPreferredSize().height));
            pnlHaiCot.add(cacCot[viTri], rangBuoc);
        }
        return pnlHaiCot;
    }

    // Tạo nhóm nhãn và giá trị chỉ đọc.
    protected JPanel taoGiaTri(String nhan, String giaTri) {
        return taoTruong(nhan, taoNhan(giaTri, true));
    }

    // Tạo thanh bước gồm số thứ tự, tên bước và đường nối.
    protected JPanel taoThanhBuoc(String... tenBuoc) {
        JPanel pnlBuoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlBuoc.setOpaque(false);
        pnlBuoc.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));

        int buocHienTai = "TIEN_DO".equals(khongGian.trang)
            || "KIEM_TRA".equals(khongGian.trang) ? 1 : 0;
        Color mauChuPhu = new Color(0x7789A3);
        Color mauDuong = new Color(0xDCE5EF);

        for (int viTri = 0; viTri < tenBuoc.length; viTri++) {
            final boolean dangChon = viTri == buocHienTai;
            JPanel pnlMuc = new JPanel(new BorderLayout(8, 0));
            pnlMuc.setOpaque(false);

            JLabel lblSo = new JLabel(String.valueOf(viTri + 1), SwingConstants.CENTER) {
                private static final long serialVersionUID = 1L;

                // Vẽ vòng tròn và màu nền của số thứ tự.
                @Override
                protected void paintComponent(Graphics doHoa) {
                    Graphics2D ve = (Graphics2D) doHoa.create();
                    ve.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    ve.setColor(dangChon ? new Color(0xEAF4FF) : NEN);
                    ve.fillOval(1, 1, getWidth() - 3, getHeight() - 3);
                    ve.setColor(dangChon ? new Color(0xA8CCEF) : mauDuong);
                    ve.setStroke(new BasicStroke(1.5f));
                    ve.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                    ve.dispose();
                    super.paintComponent(doHoa);
                }
            };
            lblSo.setOpaque(false);
            lblSo.setFont((dangChon ? CHU_DAM : CHU_THUONG).deriveFont(14.0f));
            lblSo.setForeground(dangChon ? XANH : mauChuPhu);
            lblSo.setPreferredSize(new Dimension(28, 28));

            JLabel lblTen = taoNhan(tenBuoc[viTri], dangChon);
            lblTen.setFont((dangChon ? CHU_DAM : CHU_THUONG).deriveFont(14.0f));
            lblTen.setForeground(dangChon ? XANH : mauChuPhu);
            pnlMuc.add(lblSo, BorderLayout.WEST);
            pnlMuc.add(lblTen, BorderLayout.CENTER);
            pnlBuoc.add(pnlMuc);

            if (viTri + 1 < tenBuoc.length) {
                JPanel pnlDuong = new JPanel() {
                    private static final long serialVersionUID = 1L;

                    // Vẽ đường nối ngang giữa hai bước.
                    @Override
                    protected void paintComponent(Graphics doHoa) {
                        super.paintComponent(doHoa);
                        doHoa.setColor(mauDuong);
                        doHoa.fillRect(0, getHeight() / 2, getWidth(), 1);
                    }
                };
                pnlDuong.setOpaque(false);
                pnlDuong.setPreferredSize(new Dimension(64, 28));
                pnlBuoc.add(pnlDuong);
            }
        }
        return pnlBuoc;
    }
}