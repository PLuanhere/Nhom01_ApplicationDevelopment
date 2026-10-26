package ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import dao.PhuTungSuDungDAO;
import entity.PhuTung;
import entity.PhuTungSuDung;
import entity.TaiKhoan;
import ui.component.PhuTungUi;
import ui.component.UiTheme;

// Biểu mẫu thêm, sửa, xem và chi tiết liên quan; lưu qua DAO, không tự cập nhật tồn.
public final class PhuTungSuDungDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final PhuTungSuDungDAO dao;
    private final TaiKhoan taiKhoan;
    private final PhuTungSuDung banCu;
    private final boolean chiDoc;
    private final boolean lienQuan;
    private final JComboBox<String> cmbLenh = new JComboBox<String>();
    private final JComboBox<PhuTung> cmbPhuTung = new JComboBox<PhuTung>();
    private final JTextField txtSoLuong = new JTextField("2");
    private final JTextArea txaGhiChu = new JTextArea(3, 24);
    private final JLabel lblTon = PhuTungUi.taoNhan("", false);
    private final JLabel lblLoi = PhuTungUi.taoNhan(" ", false);
    private String noiDungBanDau;
    private boolean daLuu;
    private boolean yeuCauXoa;

    /**
     * Tạo biểu mẫu đúng chế độ, không có vùng cuộn bao toàn bộ hộp thoại.
     * @param winChu cửa sổ chính
     * @param dao DAO mẫu của phiên hiện tại
     * @param taiKhoan danh tính đã đăng nhập
     * @param banCu null khi thêm, bản hiện tại khi sửa hoặc xem
     * @param chiDoc true khi xem chi tiết
     * @param lienQuan true cho bố cục chi tiết dữ liệu liên quan
     */
    public PhuTungSuDungDialog(Window winChu, PhuTungSuDungDAO dao, TaiKhoan taiKhoan,
            PhuTungSuDung banCu, boolean chiDoc, boolean lienQuan) {
        super(winChu, lienQuan ? "Chi tiết " + banCu.getMaGhiNhan()
                : chiDoc ? "Xem phụ tùng sử dụng" : banCu == null ? "Thêm phụ tùng sử dụng" : "Sửa phụ tùng sử dụng",
                ModalityType.APPLICATION_MODAL);
        this.dao = dao;
        this.taiKhoan = taiKhoan;
        this.banCu = banCu;
        this.chiDoc = chiDoc;
        this.lienQuan = lienQuan;
        PhuTungUi.dinhDangCombo(cmbLenh);
        PhuTungUi.dinhDangCombo(cmbPhuTung);
        setUndecorated(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        add(PhuTungUi.taoTieuDe(this), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoNutCuoi(), BorderLayout.SOUTH);
        noiDungBanDau = docNoiDung();
        addWindowListener(new WindowAdapter() {
            // Kiểm tra dữ liệu chưa lưu khi bấm dấu đóng trên thanh tiêu đề.
            @Override
            public void windowClosing(WindowEvent suKien) {
                dongBieuMau();
            }
        });
        PhuTungUi.dinhDangCay(getContentPane());
        pack();
        setSize(760, Math.max(getHeight(), lienQuan ? 324 : chiDoc ? 376 : 458));
        setLocationRelativeTo(winChu);
    }

    public boolean isDaLuu() {
        return daLuu;
    }

    public boolean isYeuCauXoa() {
        return yeuCauXoa;
    }

    /** {@inheritDoc} */
    // Phân biệt đổi lựa chọn, lưu, đóng và yêu cầu xóa từ chi tiết liên quan.
    @Override
    public void actionPerformed(ActionEvent suKien) {
        String lenh = suKien.getActionCommand();
        if ("DOI_LENH".equals(lenh)) {
            napPhuTung();
        } else if ("DOI_PHU_TUNG".equals(lenh)) {
            capNhatTon();
        } else if ("LUU".equals(lenh)) {
            luuDuLieu();
        } else if ("XOA".equals(lenh)) {
            yeuCauXoa = true;
            dispose();
        } else {
            dongBieuMau();
        }
    }

    // Tạo bố cục hai cột, dùng nhãn giá trị cho các chế độ chỉ đọc.
    private JPanel taoNoiDung() {
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 12));
        pnlNoiDung.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 16));
        pnlNoiDung.add(PhuTungUi.taoNhan(chiDoc ? "Thông tin hồ sơ · Chỉ đọc"
                : banCu == null ? "Nhập hồ sơ mới · Các trường * là bắt buộc"
                : "Chỉnh sửa hồ sơ · Các trường * là bắt buộc", true), BorderLayout.NORTH);
        JPanel pnlTruong = new JPanel(new GridBagLayout());
        pnlTruong.setOpaque(false);
        if (chiDoc) {
            themTruong(pnlTruong, 0, 0, "Số ghi nhận", PhuTungUi.taoNhan(banCu.getMaGhiNhan(), false));
            themTruong(pnlTruong, 0, 1, "Lệnh sửa chữa", PhuTungUi.taoNhan(banCu.getMaLenhSuaChua(), false));
            themTruong(pnlTruong, 1, 0, "Phụ tùng", PhuTungUi.taoNhan(banCu.getPhuTung().toString(), false));
            themTruong(pnlTruong, 1, 1, "Số lượng", PhuTungUi.taoNhan(
                    PhuTungUi.dinhDangLuong(banCu.getSoLuong()) + " " + banCu.getPhuTung().getDonViTinh(), false));
            themTruong(pnlTruong, 2, 0, lienQuan ? "Trạng thái" : "Tồn hiện tại", PhuTungUi.taoNhan(lienQuan
                    ? banCu.getTrangThai().getTenHienThi() : PhuTungUi.dinhDangLuong(
                            dao.docTon(banCu.getPhuTung().getMaPhuTung()))
                            + " " + banCu.getPhuTung().getDonViTinh(), false));
            if (!lienQuan) {
                themTruong(pnlTruong, 2, 1, "Người thực hiện", PhuTungUi.taoNhan(banCu.getNguoiThucHien(), false));
                themTruong(pnlTruong, 3, 0, "Ghi chú", PhuTungUi.taoMoTa(banCu.getGhiChu()));
            }
        } else {
            napBieuMau();
            themTruong(pnlTruong, 0, 0, "Số ghi nhận", PhuTungUi.taoNhan(
                    banCu == null ? "Tự sinh khi lưu" : banCu.getMaGhiNhan(), false));
            themTruong(pnlTruong, 0, 1, "Lệnh sửa chữa *", cmbLenh);
            themTruong(pnlTruong, 1, 0, "Phụ tùng đã duyệt *", cmbPhuTung);
            themTruong(pnlTruong, 1, 1, "Số lượng thực dùng *", txtSoLuong);
            themTruong(pnlTruong, 2, 0, "Tồn hiện tại", lblTon);
            themTruong(pnlTruong, 2, 1, "Người thực hiện", PhuTungUi.taoNhan("Chung Ngô Minh Hoàng", false));
            txaGhiChu.setLineWrap(true);
            txaGhiChu.setWrapStyleWord(true);
            txaGhiChu.setFont(UiTheme.SHELL_FONT);
            JScrollPane scrGhiChu = new JScrollPane(txaGhiChu);
            scrGhiChu.setPreferredSize(new Dimension(0, 72));
            scrGhiChu.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
            themTruong(pnlTruong, 3, 0, "Ghi chú", scrGhiChu);
        }
        JPanel pnlCanDinh = new JPanel(new BorderLayout());
        pnlCanDinh.setOpaque(false);
        pnlCanDinh.add(pnlTruong, BorderLayout.NORTH);
        pnlNoiDung.add(pnlCanDinh, BorderLayout.CENTER);
        JPanel pnlHuongDan = new JPanel(new BorderLayout(0, 4));
        pnlHuongDan.setOpaque(false);
        pnlHuongDan.add(PhuTungUi.taoMoTa(PhuTungUi.QUY_TAC), BorderLayout.CENTER);
        lblLoi.setForeground(PhuTungUi.MAU_NGUY_HIEM);
        lblLoi.setFont(UiTheme.CAPTION_FONT);
        pnlHuongDan.add(lblLoi, BorderLayout.SOUTH);
        pnlNoiDung.add(pnlHuongDan, BorderLayout.SOUTH);
        return pnlNoiDung;
    }

    // Xếp trường theo hai cột và định dạng giá trị chỉ đọc thành ô nền xám.
    private void themTruong(JPanel pnlTruong, int hang, int cot, String nhan, JComponent thanhPhan) {
        if (thanhPhan instanceof JLabel && !lienQuan) {
            JLabel lblGiaTri = (JLabel) thanhPhan;
            lblGiaTri.setOpaque(true);
            lblGiaTri.setBackground(UiTheme.WINDOW);
            lblGiaTri.setForeground(UiTheme.TEXT);
            lblGiaTri.setFont(UiTheme.SHELL_FONT.deriveFont(13.0f));
            lblGiaTri.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UiTheme.BORDER),
                    BorderFactory.createEmptyBorder(3, 7, 3, 7)));
            lblGiaTri.setPreferredSize(new Dimension(0, 30));
            lblGiaTri.setMinimumSize(new Dimension(0, 30));
        }
        if (thanhPhan instanceof JScrollPane) {
            PhuTungUi.dinhDangVungCuon((JScrollPane) thanhPhan);
        }
        GridBagConstraints rangBuoc = new GridBagConstraints();
        rangBuoc.gridx = cot;
        rangBuoc.gridy = hang;
        rangBuoc.weightx = 1;
        rangBuoc.fill = GridBagConstraints.HORIZONTAL;
        rangBuoc.anchor = GridBagConstraints.NORTH;
        rangBuoc.insets = new Insets(0, cot == 0 ? 0 : 6, 12, cot == 0 ? 6 : 0);
        JPanel pnlMotTruong = PhuTungUi.taoTruong(nhan, thanhPhan);
        int chieuCao = pnlMotTruong.getPreferredSize().height;
        pnlMotTruong.setPreferredSize(new Dimension(358, chieuCao));
        pnlMotTruong.setMinimumSize(new Dimension(0, chieuCao));
        pnlTruong.add(pnlMotTruong, rangBuoc);
    }

    // Nạp lệnh và phụ tùng trước khi đăng ký sự kiện để tránh phát sinh cập nhật giữa chừng.
    private void napBieuMau() {
        for (String maLenh : dao.docLenhDaDuyet(taiKhoan)) {
            cmbLenh.addItem(maLenh);
        }
        cmbLenh.setSelectedItem(banCu == null ? "SC-2026-0113" : banCu.getMaLenhSuaChua());
        napPhuTung();
        if (banCu != null) {
            cmbPhuTung.setSelectedItem(banCu.getPhuTung());
            txtSoLuong.setText(PhuTungUi.dinhDangLuong(banCu.getSoLuong()));
            txaGhiChu.setText(banCu.getGhiChu());
        }
        capNhatTon();
        cmbLenh.setActionCommand("DOI_LENH");
        cmbPhuTung.setActionCommand("DOI_PHU_TUNG");
        cmbLenh.addActionListener(this);
        cmbPhuTung.addActionListener(this);
    }

    // Chỉ đưa các phụ tùng trong phạm vi khách duyệt vào danh sách chọn.
    private void napPhuTung() {
        cmbPhuTung.removeAllItems();
        String maLenh = (String) cmbLenh.getSelectedItem();
        if (maLenh != null) {
            Map<String, BigDecimal> phamVi = dao.docPhamViDuyet(taiKhoan, maLenh);
            for (String maPhuTung : phamVi.keySet()) {
                cmbPhuTung.addItem(dao.docPhuTung(maPhuTung));
            }
        }
        capNhatTon();
    }

    // Đọc lại tồn của phụ tùng đang chọn từ cùng DAO với màn xác nhận.
    private void capNhatTon() {
        PhuTung phuTung = (PhuTung) cmbPhuTung.getSelectedItem();
        lblTon.setText(phuTung == null ? "—" : PhuTungUi.dinhDangLuong(dao.docTon(phuTung.getMaPhuTung()))
                + " " + phuTung.getDonViTinh());
    }

    // Tạo các nút tương ứng chế độ thêm, sửa, xem hoặc dữ liệu liên quan.
    private JPanel taoNutCuoi() {
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        themNut(pnlNut, chiDoc ? "Đóng" : "Hủy", "DONG", false, false);
        if (!chiDoc) {
            themNut(pnlNut, "Lưu", "LUU", true, false);
        } else if (lienQuan) {
            themNut(pnlNut, "Xóa", "XOA", false, true);
        }
        return pnlNut;
    }

    // Gắn lệnh xử lý và kiểu hiển thị cho một nút của hộp thoại.
    private void themNut(JPanel pnlNut, String nhan, String lenh, boolean chinh, boolean nguyHiem) {
        JButton btnNut = PhuTungUi.taoNut(nhan, chinh, nguyHiem);
        btnNut.setActionCommand(lenh);
        btnNut.addActionListener(this);
        pnlNut.add(btnNut);
    }

    // Lưu qua DAO và giữ nguyên biểu mẫu nếu kiểm tra thất bại.
    private void luuDuLieu() {
        try {
            PhuTung phuTung = (PhuTung) cmbPhuTung.getSelectedItem();
            if (phuTung == null) {
                throw new IllegalArgumentException("Chưa chọn phụ tùng đã duyệt.");
            }
            dao.luuNhap(taiKhoan, banCu, (String) cmbLenh.getSelectedItem(), phuTung.getMaPhuTung(),
                    PhuTungUi.docSoLuong(txtSoLuong), txaGhiChu.getText().trim());
            daLuu = true;
            dispose();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException loi) {
            lblLoi.setText(loi.getMessage());
            lblLoi.setToolTipText(loi.getMessage());
            txtSoLuong.requestFocusInWindow();
        }
    }

    // Chụp nội dung các trường để chỉ hỏi bỏ thay đổi khi đã chỉnh sửa.
    private String docNoiDung() {
        return cmbLenh.getSelectedItem() + "\n" + cmbPhuTung.getSelectedItem() + "\n"
                + txtSoLuong.getText() + "\n" + txaGhiChu.getText();
    }

    // Yêu cầu xác nhận bỏ thay đổi rồi đóng mà không sửa dữ liệu DAO.
    private void dongBieuMau() {
        if (chiDoc || noiDungBanDau.equals(docNoiDung()) || PhuTungUi.hienHopThoai(this,
                "Bỏ thay đổi chưa lưu?", "Thông tin đang nhập chưa được lưu.\n\n"
                        + "Nếu bỏ thay đổi, danh sách giữ nguyên dữ liệu trước khi mở biểu mẫu.",
                "Bỏ thay đổi", true, true)) {
            dispose();
        }
    }
}