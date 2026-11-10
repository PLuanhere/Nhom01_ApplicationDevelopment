package ui.screen.tiepnhan;

import dao.DuLieuGarageDAO;
import dao.PhieuSuaChuaDAO;
import entity.BaoGia;
import entity.BaoGia.ChiTietBaoGia;
import dao.NhanVienDAO.KyThuatVienPhanCong;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuTiepNhan;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import ui.component.UiTheme;

/** Màn tiếp nhận dùng kho lệnh hiện có; không chứa thao tác cập nhật tiến độ của kỹ thuật. */
public final class LenhSuaChuaTiepNhanPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private final PhieuSuaChuaDAO dao;
    private final TaiKhoan phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = nen(boCuc);
    private JScrollPane cuonForm;
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(
            new String[] { "Tất cả", "Chờ sửa chữa", "Đang sửa", "Chờ phụ tùng", "Hoàn thành" });
    private final DefaultTableModel modelDanhSach = chiDoc("Mã lệnh", "Biển số", "Kỹ thuật viên", "Hạn dự kiến",
            "Trạng thái");
    private final JTable tblDanhSach = bang(modelDanhSach);
    private final JLabel lblDanhSach = nhan(" ", false);
    private final JComboBox<BaoGia> cboBaoGia = new JComboBox<>();
    private final JTextArea txtCanCu = vanBan(3, false);
    private final JComboBox<Object> cboChinh = new JComboBox<>();
    private final JComboBox<Object> cboHoTro = new JComboBox<>();
    private final JComboBox<KyThuatVienPhanCong> cboPhuTrach = new JComboBox<>();
    private final JTextField txtMa = new JTextField();
    private final JTextField txtHan = new JTextField();
    private final JTextArea txtGhiChu = vanBan(2, true);
    private final JLabel lblLoi = nhan(" ", false);
    private final JButton btnPhatHanh = nut("Phát hành lệnh & phân công", true, new ActionListener() {
        /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
        @Override
        public void actionPerformed(ActionEvent event) {
            LenhSuaChuaTiepNhanPanel.this.phatHanh();
        }
    });
    private final DefaultTableModel modelHangMuc = new DefaultTableModel(
            new String[] { "Hạng mục được duyệt", "Số lượng", "Kỹ thuật viên phụ trách", "Tình trạng" }, 0) {
        private static final long serialVersionUID = 1L;

        /** Cho biết cell editable. */
        @Override
        public boolean isCellEditable(int row, int column) {
            return !chiDoc && column == 2;
        }
    };
    private final JTable tblHangMuc = bang(modelHangMuc);
    private BaoGia baoGiaChon;
    private boolean chiDoc, dangNap, daThayDoi;
    private String maLuot;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public LenhSuaChuaTiepNhanPanel(PhieuSuaChuaDAO dao, TaiKhoan phien) {
        super(new BorderLayout());
        if (dao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.dao = dao;
        if (phien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phien = phien;
        kiemTraPhien();
        setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        add(pnlThe);
        cboBaoGia.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Trả về list cell renderer component. */
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setText(value instanceof BaoGia
                        ? ((BaoGia) value).getMaBaoGia() + " · V" + ((BaoGia) value).getPhienBan() + " · Đã đồng ý"
                        : "Chưa có báo giá đủ điều kiện");
                return this;
            }
        });
        for (JComboBox<?> cbo : new JComboBox<?>[] { cboChinh, cboHoTro, cboPhuTrach }) {
            cbo.setRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                /** Trả về list cell renderer component. */
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                        boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setText(value == null ? "Chưa phân công" : value.toString());
                    return this;
                }
            });
        }
        cboBaoGia.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dangNap) {
                    doiBaoGia();
                }
            }
        });
        cboChinh.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dangNap) {
                    doiNhanSu();
                }
            }
        });
        cboHoTro.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dangNap) {
                    doiNhanSu();
                }
            }
        });
        tblHangMuc.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(cboPhuTrach));
        DefaultTableCellRenderer soLuong = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Trả về table cell renderer component. */
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus,
                    int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        };
        tblHangMuc.getColumnModel().getColumn(1).setCellRenderer(soLuong);
        int[] rongCot = { 360, 150, 330, 200 };
        for (int i = 0; i < rongCot.length; i++) {
            tblHangMuc.getColumnModel().getColumn(i).setPreferredWidth(rongCot[i]);
        }
        modelHangMuc.addTableModelListener(new TableModelListener() {
            /** Thực hiện table changed cho thành phần hiện tại. */
            @Override
            public void tableChanged(TableModelEvent e) {
                if (!dangNap) {
                    daThayDoi = true;
                }
            }
        });
        theoDoi(txtHan);
        theoDoi(txtGhiChu);
        moDanhSach();
    }

    /** Thực hiện cho phep roi man hinh cho thành phần hiện tại. */
    public boolean choPhepRoiManHinh() {
        ketThucSuaO();
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Phân công chưa phát hành. Bỏ thay đổi?",
                "Dữ liệu chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Mở danh sach khi người dùng yêu cầu. */
    public void moDanhSach() {
        kiemTraPhien();
        daThayDoi = false;
        locDanhSach();
        boCuc.show(pnlThe, "DANH_SACH");
    }

    /** Tạo danh sach cho giao diện. */
    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16)), dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Lệnh sửa chữa", "Tạo lệnh sau khi khách đã đồng ý các hạng mục.",
                nut("+ Tạo lệnh từ báo giá", true, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        moForm(null);
                    }
                })), BorderLayout.NORTH);
        JPanel loc = khung(new BorderLayout(8, 0));
        txtTim.setBorder(BorderFactory.createTitledBorder("Tìm mã lệnh, báo giá, biển số"));
        txtTim.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                locDanhSach();
            }
        });
        loc.add(txtTim);
        JPanel nut = nen(new BorderLayout());
        JPanel thanhNut = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cboTrangThai.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                locDanhSach();
            }
        });
        thanhNut.add(caoBangOtim(cboTrangThai));
        thanhNut.add(caoBangOtim(nut("Tìm", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                LenhSuaChuaTiepNhanPanel.this.locDanhSach();
            }
        })));
        thanhNut.add(caoBangOtim(nut("Làm mới", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {

                txtTim.setText("");
                cboTrangThai.setSelectedIndex(0);
                locDanhSach();

            }
        })));
        nut.add(thanhNut, BorderLayout.SOUTH);
        loc.add(nut, BorderLayout.EAST);
        dau.add(loc);
        pnl.add(dau, BorderLayout.NORTH);
        JPanel ds = khung(new BorderLayout(0, 12));
        ds.add(nhan("Danh sách lệnh sửa chữa", true), BorderLayout.NORTH);
        ds.add(cuonBang(tblDanhSach));
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tacVu.add(nut("Xem chi tiết", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                LenhSuaChuaTiepNhanPanel.this.xemChiTiet();
            }
        }));
        tacVu.add(nut("Phát sinh / phản hồi khách", false, new ActionListener() {
            // Mở phụ lục của lệnh được chọn, không cho tiếp nhận cập nhật tiến độ kỹ thuật.
            @Override
            public void actionPerformed(ActionEvent e) {
                int dong = tblDanhSach.getSelectedRow();
                if (dong < 0) {
                    lblDanhSach.setText("Chọn một lệnh để xem phát sinh.");
                    return;
                }
                String ma = modelDanhSach.getValueAt(tblDanhSach.convertRowIndexToModel(dong), 0).toString();
                try {
                    new ui.dialog.tiepnhan.PhatSinhSuaChuaDialog(SwingUtilities.getWindowAncestor(
                            LenhSuaChuaTiepNhanPanel.this), phien, ma).setVisible(true);
                    locDanhSach();
                } catch (RuntimeException ex) {
                    lblDanhSach.setText(ex.getMessage());
                }
            }
        }));
        ds.add(tacVu, BorderLayout.SOUTH);
        pnl.add(ds);
        pnl.add(lblDanhSach, BorderLayout.SOUTH);
        return pnl;
    }

    /** Tạo form cho giao diện. */
    private JPanel taoForm() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Tạo lệnh & phân công kỹ thuật", "1. Báo giá hợp lệ → 2. Phân công → 3. Phát hành lệnh",
                nut("Danh sách / quay lại", false, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {

                        if (choPhepRoiManHinh()) {
                            moDanhSach();
                        }

                    }
                })), BorderLayout.NORTH);
        JPanel form = khung(new BorderLayout(0, 14));
        JPanel dau = cot();
        them(dau, nhan("Tạo lệnh sửa chữa từ báo giá đã được duyệt", true), 24);
        them(dau, nhan("Phân công theo hạng mục khách xác nhận; không tự thêm dòng ngoài báo giá.", false), 24);
        them(dau, truong("Báo giá được duyệt *", cboBaoGia), 68);
        them(dau, new JScrollPane(txtCanCu), 85);
        form.add(dau, BorderLayout.NORTH);
        JPanel giua = nen(new BorderLayout(0, 10));
        giua.add(nhan("Danh sách hạng mục được phép sửa chữa", true), BorderLayout.NORTH);
        JScrollPane bang = cuonBang(tblHangMuc);
        bang.setPreferredSize(new Dimension(0, 220));
        giua.add(bang);
        form.add(giua);
        JPanel cuoi = cot();
        them(cuoi, hang(truong("Kỹ thuật viên chính *", cboChinh), truong("Người hỗ trợ (nếu có)", cboHoTro)), 68);
        txtMa.setEditable(false);
        them(cuoi, hang(truong("Mã lệnh · tự sinh", txtMa), truong("Hạn dự kiến * · dd/MM/yyyy HH:mm", txtHan)), 68);
        them(cuoi, truong("Ghi chú phân công", txtGhiChu), 95);
        JTextArea quyTac = vanBan(2, false);
        quyTac.setText(
                "Phát sinh cần báo giá bổ sung và khách đồng ý trước khi thực hiện. Tiếp tục phần đã được duyệt.");
        quyTac.setBackground(new Color(0xFFF7E5));
        quyTac.setForeground(new Color(0x91631A));
        quyTac.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        them(cuoi, quyTac, 58);
        form.add(cuoi, BorderLayout.SOUTH);
        cuonForm = cuonNoiDung(form);
        pnl.add(cuonForm);
        JPanel chan = nen(new BorderLayout(0, 8));
        chan.add(lblLoi, BorderLayout.NORTH);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tacVu.add(btnPhatHanh);
        chan.add(tacVu);
        pnl.add(chan, BorderLayout.SOUTH);
        return pnl;
    }

    /** Mở form khi người dùng yêu cầu. */
    private void moForm(PhieuSuaChua phieu) {
        kiemTraPhien();
        dangNap = true;
        chiDoc = phieu != null;
        maLuot = DuLieuGarageDAO.taoMaThaoTac();
        modelHangMuc.setRowCount(0);
        cboBaoGia.removeAllItems();
        if (chiDoc) {
            cboBaoGia.addItem(dao.layBaoGia(phien, phieu.getMaPhieu()));
        } else {
            for (BaoGia bg : dao.layBaoGiaCoThePhatHanh(phien)) {
                cboBaoGia.addItem(bg);
            }
        }
        cboBaoGia.setEnabled(!chiDoc);
        baoGiaChon = (BaoGia) cboBaoGia.getSelectedItem();
        cboChinh.removeAllItems();
        cboHoTro.removeAllItems();
        cboChinh.addItem(null);
        cboHoTro.addItem(null);
        for (KyThuatVienPhanCong kt : dao.layKyThuatPhanCong(phien)) {
            cboChinh.addItem(kt);
            cboHoTro.addItem(kt);
        }
        cboChinh.setEnabled(!chiDoc);
        cboHoTro.setEnabled(!chiDoc);
        txtHan.setEditable(!chiDoc);
        txtGhiChu.setEditable(!chiDoc);
        txtMa.setText(chiDoc ? phieu.getMaPhieu() : "Tự sinh khi phát hành");
        txtHan.setText((chiDoc ? phieu.getHanHoanThanh()
                : baoGiaChon == null ? LocalDateTime.now().plusDays(1) : baoGiaChon.getNgayHen()).format(NGAY));
        txtGhiChu.setText(chiDoc ? phieu.getGhiChu() : "");
        capNhatCanCu();
        if (baoGiaChon != null) {
            List<ChiTietBaoGia> chiTiet = baoGiaChon.getChiTiet();
            for (int i = 0; i < chiTiet.size(); i++) {
                ChiTietBaoGia ct = chiTiet.get(i);
                Object nguoi = null;
                String trangThai = "Chưa bắt đầu";
                if (chiDoc) {
                    ChiTietSuaChua hm = phieu.getDanhSachHangMuc().get(i);
                    nguoi = tenKyThuat(hm.getTaiKhoanThucHien());
                    trangThai = hm.getTrangThai().getTenHienThi();
                }
                modelHangMuc.addRow(new Object[] { ct.getTenHangMuc(),
                        String.valueOf(ct.getSoLuong()) + " " + ct.getDonVi(), nguoi, trangThai });
            }
        }
        if (chiDoc) {
            chonNguoiLichSu(cboChinh, phieu.getKyThuatChinh());
            chonNguoiLichSu(cboHoTro, phieu.getKyThuatPhoiHop());
            txtCanCu.append(
                    "\nPhát hành: " + phieu.getNguoiPhatHanh() + " · " + phieu.getThoiDiemPhatHanh().format(NGAY));
        }
        btnPhatHanh.setVisible(!chiDoc);
        lblLoi.setForeground(UiTheme.APP_MUTED);
        lblLoi.setText(chiDoc ? "Lệnh đã phát hành · chỉ đọc"
                : baoGiaChon == null ? "Chưa có báo giá đủ điều kiện phát hành."
                        : "Chọn người chính/hỗ trợ rồi phân công từng dòng.");
        dangNap = false;
        daThayDoi = false;
        capNhatEditor();
        boCuc.show(pnlThe, "FORM");
        SwingUtilities.invokeLater(new Runnable() {
            /** Khởi tạo hoặc cập nhật giao diện trên luồng Swing. */
            @Override
            public void run() {
                cuonForm.getViewport().setViewPosition(new Point(0, 0));
            }
        });
    }

    /** Thực hiện cap nhat can cu cho thành phần hiện tại. */
    private void capNhatCanCu() {
        if (baoGiaChon == null) {
            txtCanCu.setText("Chưa có báo giá đã đồng ý và chưa lập lệnh.");
            return;
        }
        PhieuTiepNhan phieu = baoGiaChon.getBaoCao().getPhieu();
        txtCanCu.setText("Phiếu tiếp nhận: " + phieu.getMaPhieu() + "\nBáo giá: " + baoGiaChon.getMaBaoGia() + " · V"
                + baoGiaChon.getPhienBan() + " · Đã đồng ý\nKhách / xe: " + phieu.getTenKhach() + " · "
                + baoGiaChon.getBienSo());
        txtCanCu.setCaretPosition(0);
    }

    /** Thực hiện doi bao gia cho thành phần hiện tại. */
    private void doiBaoGia() {
        BaoGia moi = (BaoGia) cboBaoGia.getSelectedItem();
        if (moi == baoGiaChon) {
            return;
        }
        ketThucSuaO();
        if (daThayDoi && JOptionPane.showConfirmDialog(this, "Đổi báo giá sẽ bỏ phân công đang nhập. Tiếp tục?",
                "Đổi báo giá", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            dangNap = true;
            cboBaoGia.setSelectedItem(baoGiaChon);
            dangNap = false;
            return;
        }
        dangNap = true;
        baoGiaChon = moi;
        modelHangMuc.setRowCount(0);
        capNhatCanCu();
        if (moi != null) {
            for (ChiTietBaoGia ct : moi.getChiTiet()) {
                modelHangMuc.addRow(new Object[] { ct.getTenHangMuc(),
                        String.valueOf(ct.getSoLuong()) + " " + ct.getDonVi(), null, "Chưa bắt đầu" });
            }
            txtHan.setText(moi.getNgayHen().format(NGAY));
        }
        dangNap = false;
        daThayDoi = true;
    }

    /** Thực hiện doi nhan su cho thành phần hiện tại. */
    private void doiNhanSu() {
        ketThucSuaO();
        capNhatEditor();
        daThayDoi = true;
        for (int i = 0; i < modelHangMuc.getRowCount(); i++) {
            Object value = modelHangMuc.getValueAt(i, 2);
            if (value != cboChinh.getSelectedItem() && value != cboHoTro.getSelectedItem()) {
                modelHangMuc.setValueAt(null, i, 2);
            }
        }
    }

    /** Thực hiện cap nhat editor cho thành phần hiện tại. */
    private void capNhatEditor() {
        if (chiDoc) {
            return;
        }
        cboPhuTrach.removeAllItems();
        cboPhuTrach.addItem(null);
        if (cboChinh.getSelectedItem() != null) {
            cboPhuTrach.addItem((KyThuatVienPhanCong) cboChinh.getSelectedItem());
        }
        if (cboHoTro.getSelectedItem() != null && cboHoTro.getSelectedItem() != cboChinh.getSelectedItem()) {
            cboPhuTrach.addItem((KyThuatVienPhanCong) cboHoTro.getSelectedItem());
        }
    }

    /** Thực hiện phat hanh cho thành phần hiện tại. */
    private void phatHanh() {
        kiemTraPhien();
        if (chiDoc) {
            return;
        }
        ketThucSuaO();
        btnPhatHanh.setEnabled(false);
        try {
            LocalDateTime han = LocalDateTime.parse(txtHan.getText().trim(), NGAY);
            List<String> phuTrach = new ArrayList<>();
            for (int i = 0; i < modelHangMuc.getRowCount(); i++) {
                phuTrach.add(taiKhoan(modelHangMuc.getValueAt(i, 2)));
            }
            PhieuSuaChua moi = dao.phatHanh(phien, maLuot, baoGiaChon, taiKhoan(cboChinh.getSelectedItem()),
                    taiKhoan(cboHoTro.getSelectedItem()), phuTrach, han, txtGhiChu.getText());
            moDanhSach();
            lblDanhSach.setText("Đã phát hành " + moi.getMaPhieu() + " · " + moi.getMaBaoGia());
        } catch (DateTimeParseException e) {
            lblLoi.setForeground(UiTheme.ERROR);
            lblLoi.setText("Hạn dự kiến không hợp lệ; dùng dd/MM/yyyy HH:mm.");
            txtHan.requestFocusInWindow();
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblLoi.setForeground(UiTheme.ERROR);
            lblLoi.setText(e.getMessage());
            lblLoi.setToolTipText(e.getMessage());
        } finally {
            btnPhatHanh.setEnabled(true);
        }
    }

    /** Thực hiện xem chi tiet cho thành phần hiện tại. */
    private void xemChiTiet() {
        kiemTraPhien();
        int row = tblDanhSach.getSelectedRow();
        if (row < 0) {
            lblDanhSach.setText("Chọn lệnh để xem chi tiết.");
            return;
        }
        String ma = (String) modelDanhSach.getValueAt(tblDanhSach.convertRowIndexToModel(row), 0);
        PhieuSuaChua ketQuaTraCuu1 = dao.timTheoMa(phien, ma);
        if (ketQuaTraCuu1 == null) {
            throw new IllegalArgumentException("Lệnh không còn tồn tại.");
        }
        PhieuSuaChua phieu = ketQuaTraCuu1;
        moForm(phieu);
    }

    /** Thực hiện loc danh sach cho thành phần hiện tại. */
    private void locDanhSach() {
        kiemTraPhien();
        modelDanhSach.setRowCount(0);
        for (PhieuSuaChua p : dao.timKiem(phien, txtTim.getText(), (String) cboTrangThai.getSelectedItem())) {
            modelDanhSach.addRow(new Object[] { p.getMaPhieu(), p.getBienSo(), tenKyThuat(p.getKyThuatChinh()),
                    p.getHanHoanThanh().format(NGAY), p.getTrangThai().getTenHienThi() });
        }
        lblDanhSach.setText(modelDanhSach.getRowCount() + " lệnh sửa chữa.");
    }

    /** Thực hiện ten ky thuat cho thành phần hiện tại. */
    private String tenKyThuat(String taiKhoan) {
        for (KyThuatVienPhanCong kt : dao.layKyThuatPhanCong(phien)) {
            if (kt.getTaiKhoan().getTenDangNhap().equals(taiKhoan)) {
                return kt.toString();
            }
        }
        return taiKhoan;
    }

    /** Thực hiện chon nguoi lich su cho thành phần hiện tại. */
    private void chonNguoiLichSu(JComboBox<Object> cbo, String ten) {
        for (int i = 0; i < cbo.getItemCount(); i++) {
            if (cbo.getItemAt(i) != null && taiKhoan(cbo.getItemAt(i)).equals(ten)) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
        if (!ten.isEmpty()) {
            cbo.addItem(ten + " · không còn trong nguồn hoạt động");
            cbo.setSelectedIndex(cbo.getItemCount() - 1);
        }
    }

    /** Đọc lại dữ liệu và cập nhật phần hiển thị. */
    private static String taiKhoan(Object nguoi) {
        return nguoi instanceof KyThuatVienPhanCong ? ((KyThuatVienPhanCong) nguoi).getTaiKhoan().getTenDangNhap() : "";
    }

    /** Thực hiện ket thuc sua o cho thành phần hiện tại. */
    private void ketThucSuaO() {
        if (tblHangMuc.isEditing()) {
            tblHangMuc.getCellEditor().stopCellEditing();
        }
    }

    /** Kiểm tra phien trước khi xử lý. */
    private void kiemTraPhien() {
        if (phien == null || phien.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chức năng dành cho tiếp nhận.");
        }
    }

    /** Thực hiện theo doi cho thành phần hiện tại. */
    private void theoDoi(JTextComponent txt) {
        txt.getDocument().addDocumentListener(new DocumentListener() {
            /** Thực hiện doi cho thành phần hiện tại. */
            private void doi() {
                if (!dangNap && !chiDoc) {
                    daThayDoi = true;
                }
            }

            /** Thực hiện insert update cho thành phần hiện tại. */
            @Override
            public void insertUpdate(DocumentEvent e) {
                doi();
            }

            /** Thực hiện remove update cho thành phần hiện tại. */
            @Override
            public void removeUpdate(DocumentEvent e) {
                doi();
            }

            /** Thực hiện changed update cho thành phần hiện tại. */
            @Override
            public void changedUpdate(DocumentEvent e) {
                doi();
            }
        });
    }

    /** Thực hiện nen cho thành phần hiện tại. */
    private static JPanel nen(LayoutManager layout) {
        JPanel pnl = new JPanel(layout);
        pnl.setOpaque(false);
        return pnl;
    }

    /** Thực hiện khung cho thành phần hiện tại. */
    private static JPanel khung(LayoutManager layout) {
        JPanel pnl = UiTheme.roundedPanel(layout, Color.WHITE, 8);
        pnl.setBorder(UiTheme.roundedBorder(UiTheme.APP_BORDER, 8, 16, 16));
        return pnl;
    }

    /** Thực hiện nhan cho thành phần hiện tại. */
    private static JLabel nhan(String text, boolean dam) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED);
        return lbl;
    }

    /** Thực hiện van ban cho thành phần hiện tại. */
    private static JTextArea vanBan(int rows, boolean editable) {
        JTextArea txt = new JTextArea(rows, 20);
        txt.setEditable(editable);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(UiTheme.APP_FONT);
        return txt;
    }

    /** Đồng bộ chiều cao điều khiển lọc với ô tìm kiếm có nhãn viền. */
    private static <T extends Component> T caoBangOtim(T control) {
        Dimension kichThuoc = control.getPreferredSize();
        control.setPreferredSize(new Dimension(kichThuoc.width, 36));
        return control;
    }

    /** Thực hiện nut cho thành phần hiện tại. */
    private static JButton nut(String text, boolean chinh, ActionListener action) {
        JButton btn = UiTheme.createAppButton(text, chinh);
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 36));
        btn.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, "thaoTac"));
            }
        });
        return btn;
    }

    /** Thực hiện cot cho thành phần hiện tại. */
    private static JPanel cot() {
        JPanel pnl = nen(null);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        return pnl;
    }

    /** Thực hiện them cho thành phần hiện tại. */
    private static void them(JPanel cot, JComponent component, int cao) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
        component.setPreferredSize(new Dimension(0, cao));
        cot.add(component);
        cot.add(Box.createVerticalStrut(10));
    }

    /** Thực hiện hang cho thành phần hiện tại. */
    private static JPanel hang(JComponent trai, JComponent phai) {
        JPanel pnl = nen(new GridLayout(1, 2, 14, 0));
        pnl.add(trai);
        pnl.add(phai);
        return pnl;
    }

    /** Thực hiện truong cho thành phần hiện tại. */
    private static JPanel truong(String text, JComponent c) {
        JPanel pnl = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(text, true);
        lbl.setLabelFor(c);
        pnl.add(lbl, BorderLayout.NORTH);
        c.setFont(UiTheme.APP_FONT);
        pnl.add(c instanceof JTextArea ? new JScrollPane(c) : c);
        return pnl;
    }

    /** Thực hiện tieu de cho thành phần hiện tại. */
    private static JPanel tieuDe(String text, String note, JButton btn) {
        JPanel pnl = nen(new BorderLayout(8, 10));
        pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + text, false), BorderLayout.NORTH);
        JPanel title = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(text, true);
        lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f));
        title.add(lbl, BorderLayout.NORTH);
        title.add(nhan(note, false));
        pnl.add(title);
        JPanel action = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        action.add(btn);
        pnl.add(action, BorderLayout.EAST);
        return pnl;
    }

    /** Thực hiện chi doc cho thành phần hiện tại. */
    private static DefaultTableModel chiDoc(String... columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Thực hiện bang cho thành phần hiện tại. */
    private static JTable bang(DefaultTableModel model) {
        JTable tbl = new JTable(model);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);
        tbl.setFont(UiTheme.APP_FONT);
        tbl.setRowHeight(44);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setAutoCreateRowSorter(true);
        tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);
        return tbl;
    }

    /** Thực hiện cuon bang cho thành phần hiện tại. */
    private static JScrollPane cuonBang(JTable tbl) {
        JScrollPane cuon = new JScrollPane(tbl);
        cuon.getViewport().setBackground(Color.WHITE);
        return cuon;
    }

    private static final class NoiDungCuon extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
        NoiDungCuon() {
            super(new BorderLayout());
            setOpaque(false);
        }

        /** Trả về preferred scrollable viewport size. */
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        /** Trả về scrollable unit increment. */
        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 20;
        }

        /** Trả về scrollable block increment. */
        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(20, r.height - 20);
        }

        /** Trả về scrollable tracks viewport width. */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        /** Trả về scrollable tracks viewport height. */
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Thực hiện cuon noi dung cho thành phần hiện tại. */
    private static JScrollPane cuonNoiDung(JPanel pnl) {
        JPanel neo = new NoiDungCuon();
        neo.add(pnl, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(neo);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        return cuon;
    }
}
