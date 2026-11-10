package ui.screen.kythuat;

import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.PhieuTiepNhan.TrangThaiKiemTra;
import entity.ChiTietKiemTra;
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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import static ui.screen.kythuat.GiaoDienKyThuat.*;

// Hiển thị danh sách, lập báo cáo và đối chiếu kết quả kiểm tra.
public final class BaoCaoKiemTraPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public BaoCaoKiemTraPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Hiển thị danh sách báo cáo và bộ lọc tìm kiếm.
    public void hienDanhSachBaoCao() {
        JPanel pnlTrang = taoTrang(
                "Báo cáo kiểm tra",
                "Kiểm tra xe có phiếu tiếp nhận hợp lệ và ghi kết quả chẩn đoán.",
                "+ Lập báo cáo kiểm tra",
                "TAO_BAO_CAO");

        JTextField txtTim = khongGian.taoONhap("", false);
        txtTim.putClientProperty("goiY", "Tìm mã báo cáo, phiếu, biển số...");
        txtTim.setPreferredSize(new Dimension(380, 36));

        JComboBox<String> cmbLoc = new JComboBox<String>(
                new String[] {"Tất cả", "Nháp", "Hoàn tất"});

        dinhDangCombo(cmbLoc);
        cmbLoc.setPreferredSize(new Dimension(190, 36));

        DefaultTableModel model = taoModel(
                "Mã báo cáo",
                "Phiếu tiếp nhận",
                "Biển số",
                "Người kiểm tra",
                "Trạng thái",
                "Thao tác");

        JTable tblBang = taoBang(model);
        List<BaoCaoKiemTra> cacDong = new ArrayList<BaoCaoKiemTra>();
        JLabel lblSo = taoNhan("", false);

        ActionListener loc = new ActionListener() {
            // Đọc báo cáo phù hợp với từ khóa và trạng thái đang chọn.
            @Override
            public void actionPerformed(ActionEvent suKien) {
                cacDong.clear();
                cacDong.addAll(khongGian.dao.docBaoCao(
                        txtTim.getText().trim(), (String) cmbLoc.getSelectedItem()));

                model.setRowCount(0);

                for (BaoCaoKiemTra dong : cacDong) {
                    model.addRow(new Object[] {
                            dong.getMaBaoCao(),
                            dong.getPhieu().getMaPhieu(),
                            dong.getPhieu().getBienSo(),
                            khongGian.taiKhoan.getTenDangNhap(),
                            dong.getTrangThai() == TrangThaiKiemTra.HOAN_TAT
                                    ? "Đã hoàn tất" : "Chưa xác nhận",
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
            // Mở trang đối chiếu của báo cáo được chọn trong bảng.
            @Override
            public void mouseClicked(MouseEvent suKien) {
                int dong = tblBang.rowAtPoint(suKien.getPoint());

                if (dong >= 0
                        && (tblBang.columnAtPoint(suKien.getPoint()) == 5
                                || suKien.getClickCount() == 2)) {

                    int viTri = tblBang.convertRowIndexToModel(dong);
                    khongGian.baoCao = cacDong.get(viTri);
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

        themThanhPhan(pnlTrang, taoThe(
                "Danh sách báo cáo kiểm tra",
                "Chọn một dòng để xem, lọc hoặc mở tác vụ liên quan.",
                pnlBang));

        hienTrang(pnlTrang);
    }

    // Cho chọn phiếu tiếp nhận đã xác nhận và chưa có báo cáo hoàn tất.
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
            khongGian.baoLoi(
                    "Không có phiếu phù hợp",
                    "Không còn phiếu hợp lệ đang chờ kiểm tra trong phạm vi phân công.");
            return;
        }

        JPopupMenu mnuPhieu = new JPopupMenu();

        for (PhieuTiepNhan dong : cacPhieu) {
            JMenuItem mniPhieu = new JMenuItem(
                    dong.getMaPhieu() + " · " + dong.getBienSo());

            mniPhieu.setFont(CHU_THUONG);
            mniPhieu.addActionListener(new ActionListener() {
                // Mở biểu mẫu kiểm tra cho phiếu tiếp nhận được chọn.
                @Override
                public void actionPerformed(ActionEvent suKien) {
                    moPhieu(dong);
                }
            });

            mnuPhieu.add(mniPhieu);
        }

        // Neo vào khung chung để đường tắt từ Trang chủ vẫn mở được danh sách phiếu.
        mnuPhieu.show(khongGian.winChu.getRootPane(), 260, 140);
    }

    // Nạp phiếu tiếp nhận và báo cáo nháp vào biểu mẫu kiểm tra.
    void moPhieu(PhieuTiepNhan dong) {
        if (!khongGian.duocRoiTrang()) {
            return;
        }

        khongGian.phieu = dong;
        khongGian.baoCao = khongGian.dao.docBaoCao(dong);
        khongGian.batDauTrang("KIEM_TRA");
        hienLapBaoCao();
    }

    // Hiển thị biểu mẫu nhập các hạng mục kiểm tra linh hoạt.
    void hienLapBaoCao() {
        JPanel pnlTrang = taoTrang(
                "Lập báo cáo kiểm tra xe",
                "Kết quả phải đối chiếu với tình trạng ban đầu và yêu cầu khách hàng.",
                "Danh sách / quay lại",
                "KIEM_TRA");

        themThanhPhan(pnlTrang, taoThanhBuoc(
                "Nhận phiếu", "Ghi kết quả", "Đối chiếu & lưu"));

        JPanel pnlCot = taoCot();

        themThanhPhan(pnlCot, taoHang(
                taoGiaTri(
                        "Phiếu tiếp nhận / xe",
                        khongGian.phieu.getMaPhieu() + " · " + khongGian.phieu.getBienSo()),
                taoGiaTri(
                        "Người kiểm tra",
                        khongGian.taiKhoan.getTenDangNhap())));

        themThanhPhan(pnlCot, taoThongBao(
                "Thông tin gốc từ tiếp nhận: "
                        + khongGian.phieu.getYeuCau() + " · "
                        + khongGian.phieu.getTinhTrang(),
                false));

        khongGian.modelNhap = new DefaultTableModel(
                new String[] {"Khu vực", "Hạng mục", "Kết quả kiểm tra", "Mức độ", "Đề xuất"},
                0) {

            private static final long serialVersionUID = 1L;

            // Cho nhập nội dung của từng ô trên bảng hạng mục.
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return true;
            }
        };

        String[] noiDung = khongGian.baoCao == null
                ? new String[] {"", "", "", "", "", "", ""}
                : khongGian.baoCao.getNoiDung();

        if (khongGian.baoCao != null) {
            for (ChiTietKiemTra dong : khongGian.baoCao.getChiTiet()) {
                khongGian.modelNhap.addRow(new Object[] {
                        dong.getTenKhuVuc(),
                        dong.getTenHangMuc(),
                        dong.getKetQua(),
                        dong.getMucDo(),
                        dong.getDeXuat()
                });
            }
        }

        if (khongGian.modelNhap.getRowCount() == 0) {
            khongGian.modelNhap.addRow(
                    new Object[] {"", "", "", "Chưa đánh giá", ""});
        }

        khongGian.tblNhap = taoBang(khongGian.modelNhap);
        dinhDangBangNhap();

        JComboBox<String> cmbMucDo = new JComboBox<String>(new String[] {
                "Chưa đánh giá", "Đang kiểm tra", "Bình thường", "Cần theo dõi", "Cần xử lý"
        });

        cmbMucDo.setEditable(true);
        dinhDangCombo(cmbMucDo);

        khongGian.tblNhap.getColumnModel().getColumn(3).setCellEditor(
                new javax.swing.DefaultCellEditor(cmbMucDo));

        JPanel pnlNutDong = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlNutDong.setOpaque(false);

        pnlNutDong.add(taoNut(
                "Thêm hạng mục", "", false, new ActionListener() {
                    // Thêm dòng sau khi kết thúc chỉnh sửa ô hiện tại.
                    @Override
                    public void actionPerformed(ActionEvent suKien) {
                        themDongKiemTra();
                    }
                }));

        pnlNutDong.add(taoNut(
                "Xóa dòng đã chọn", "", false, new ActionListener() {
                    // Xóa dòng đang chọn khỏi bản nháp trên biểu mẫu.
                    @Override
                    public void actionPerformed(ActionEvent suKien) {
                        xoaDongKiemTra();
                    }
                }));

        themThanhPhan(pnlCot, pnlNutDong);

        themThanhPhan(pnlCot, taoThongBao(
                "Bấm vào ô có viền để nhập. Mức độ có danh sách chọn; "
                        + "đề xuất có thể để trống nếu không cần sửa.",
                false));

        themThanhPhan(pnlCot, bocBang(khongGian.tblNhap));

        khongGian.txtLoi = khongGian.taoONhap(noiDung[1]);
        khongGian.txtDeXuat = khongGian.taoONhap(noiDung[2]);

        themThanhPhan(pnlCot, taoHang(
                taoTruong("Kết luận sơ bộ *", khongGian.txtLoi),
                taoTruong("Đề xuất chung (nếu có)", khongGian.txtDeXuat)));

        pnlCot.add(taoHangNut(
                taoNut("Đối chiếu & lưu báo cáo", "DOI_CHIEU", true, khongGian)));

        themThanhPhan(pnlTrang, taoThe(
                "Lập báo cáo kiểm tra "
                        + (khongGian.baoCao == null
                                ? "mới" : khongGian.baoCao.getMaBaoCao()),
                "Tự lấy thông tin tiếp nhận, chỉ nhập kết quả chẩn đoán và hạng mục đề xuất.",
                pnlCot));

        hienTrang(pnlTrang);
    }

    // Làm rõ ô nhập bằng viền, gợi ý và thao tác chỉnh sửa một lần bấm.
    private void dinhDangBangNhap() {
        javax.swing.DefaultCellEditor boSua = (javax.swing.DefaultCellEditor)
                khongGian.tblNhap.getDefaultEditor(Object.class);

        boSua.setClickCountToStart(1);

        JTextField txtSua = (JTextField) boSua.getComponent();

        txtSua.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x2264A6)),
                        BorderFactory.createEmptyBorder(0, 9, 0, 9))));

        khongGian.tblNhap.putClientProperty(
                "terminateEditOnFocusLost", Boolean.TRUE);

        khongGian.tblNhap.setDefaultRenderer(
                Object.class, new javax.swing.table.DefaultTableCellRenderer() {

                    private static final long serialVersionUID = 1L;

                    private final String[] goiY = {
                            "Nhập khu vực...",
                            "Nhập tên hạng mục...",
                            "Nhập kết quả kiểm tra...",
                            "Chọn mức độ",
                            "Đề xuất (nếu có)..."
                    };

                    private final javax.swing.Icon muiTen = new javax.swing.Icon() {
                        // Vẽ mũi tên bằng hình học để không phụ thuộc ký tự của font.
                        @Override
                        public void paintIcon(
                                java.awt.Component thanhPhan,
                                java.awt.Graphics ve,
                                int x,
                                int y) {

                            ve.setColor(new Color(0x647489));
                            ve.fillPolygon(
                                    new int[] {x, x + 8, x + 4},
                                    new int[] {y, y, y + 5},
                                    3);
                        }

                        // Trả chiều rộng của biểu tượng mũi tên.
                        @Override
                        public int getIconWidth() {
                            return 8;
                        }

                        // Trả chiều cao của biểu tượng mũi tên.
                        @Override
                        public int getIconHeight() {
                            return 5;
                        }
                    };

                    // Vẽ chữ gợi ý ở ô trống mà không đưa chữ gợi ý vào dữ liệu.
                    @Override
                    public java.awt.Component getTableCellRendererComponent(
                            JTable bang, Object giaTri, boolean chon,
                            boolean tieuDiem, int dong, int cot) {

                        super.getTableCellRendererComponent(
                                bang, giaTri, chon, tieuDiem, dong, cot);

                        boolean trong = giaTri == null || giaTri.toString().isBlank();

                        setText(trong ? goiY[cot] : giaTri.toString());
                        setIcon(cot == 3 ? muiTen : null);
                        setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
                        setIconTextGap(8);
                        setFont(CHU_THUONG);
                        setForeground(
                                trong ? new Color(0x647489) : new Color(0x202D3D));
                        setBackground(
                                chon ? new Color(0xEEF4FA) : Color.WHITE);

                        setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createLineBorder(
                                                tieuDiem
                                                        ? new Color(0x2264A6)
                                                        : new Color(0xCBD5E1)),
                                        BorderFactory.createEmptyBorder(0, 9, 0, 9))));

                        setToolTipText(
                                cot == 3
                                        ? "Bấm để chọn mức độ"
                                        : "Bấm để nhập: " + bang.getColumnName(cot));

                        return this;
                    }
                });

        khongGian.modelNhap.addTableModelListener(
                new javax.swing.event.TableModelListener() {
                    // Cập nhật bố cục sau khi số dòng hoặc nội dung bảng thay đổi.
                    @Override
                    public void tableChanged(javax.swing.event.TableModelEvent suKien) {
                        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            // Đợi JTable nhận dữ liệu rồi cập nhật các vùng chứa.
                            @Override
                            public void run() {
                                capNhatBoCucBangNhap();
                            }
                        });
                    }
                });
    }

    // Cập nhật kích thước các vùng chứa để nhìn thấy đủ dòng mới thêm.
    private void capNhatBoCucBangNhap() {
        java.awt.Container cha = khongGian.tblNhap.getParent();

        while (cha != null) {
            cha.invalidate();

            if (cha instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) cha).revalidate();
            }

            cha = cha.getParent();
        }

        khongGian.tblNhap.repaint();
    }

    // Thêm hạng mục mới trên biểu mẫu, chưa ghi SQL cho tới khi lưu báo cáo.
    private void themDongKiemTra() {
        if (khongGian.tblNhap.isEditing()
                && !khongGian.tblNhap.getCellEditor().stopCellEditing()) {
            return;
        }

        khongGian.modelNhap.addRow(
                new Object[] {"", "", "", "Chưa đánh giá", ""});

        int dong = khongGian.modelNhap.getRowCount() - 1;
        khongGian.tblNhap.setRowSelectionInterval(dong, dong);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            // Chuyển con trỏ vào ô đầu của dòng mới sau khi cập nhật bố cục.
            @Override
            public void run() {
                capNhatBoCucBangNhap();

                khongGian.tblNhap.scrollRectToVisible(
                        khongGian.tblNhap.getCellRect(dong, 0, true));

                if (khongGian.tblNhap.editCellAt(dong, 0)) {
                    khongGian.tblNhap.getEditorComponent().requestFocusInWindow();
                }
            }
        });
    }

    // Xóa dòng đang chọn; SQL chỉ thay đổi khi người dùng lưu lại báo cáo.
    private void xoaDongKiemTra() {
        if (khongGian.tblNhap.isEditing()
                && !khongGian.tblNhap.getCellEditor().stopCellEditing()) {
            return;
        }

        int dong = khongGian.tblNhap.getSelectedRow();

        if (dong >= 0) {
            khongGian.modelNhap.removeRow(
                    khongGian.tblNhap.convertRowIndexToModel(dong));
        }
    }

    // Đọc giá trị ô và đổi ô trống thành chuỗi rỗng.
    private String docO(int dong, int cot) {
        Object giaTri = khongGian.modelNhap.getValueAt(dong, cot);
        return giaTri == null ? "" : giaTri.toString().trim();
    }

    // Lưu bản nháp từ biểu mẫu rồi mở trang đối chiếu báo cáo.
    void luuBanNhap() {
        if (khongGian.tblNhap.isEditing()
                && !khongGian.tblNhap.getCellEditor().stopCellEditing()) {
            return;
        }

        String[] noiDung = {
                khongGian.txtLoi.getText(),
                khongGian.txtLoi.getText(),
                khongGian.txtDeXuat.getText(),
                "",
                "",
                "",
                ""
        };

        List<ChiTietKiemTra> chiTiet = new ArrayList<ChiTietKiemTra>();

        for (int viTri = 0; viTri < khongGian.modelNhap.getRowCount(); viTri++) {
            String ten = docO(viTri, 0);
            String hangMuc = docO(viTri, 1);
            String ketQua = docO(viTri, 2);
            String mucDo = docO(viTri, 3);
            String deXuat = docO(viTri, 4);

            if (!ten.isBlank() || !hangMuc.isBlank()
                    || !ketQua.isBlank() || !deXuat.isBlank()) {

                chiTiet.add(new ChiTietKiemTra(
                        ten, hangMuc, ketQua, mucDo, deXuat));
            }
        }

        BaoCaoKiemTra nhap = new BaoCaoKiemTra(
                khongGian.baoCao == null ? "" : khongGian.baoCao.getMaBaoCao(),
                khongGian.phieu,
                khongGian.baoCao == null
                        ? LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                        : khongGian.baoCao.getNgayKiemTra(),
                "Tổng hợp",
                noiDung,
                TrangThaiKiemTra.NHAP,
                null,
                chiTiet);

        khongGian.baoCao = khongGian.dao.luuBaoCao(nhap, false);
        khongGian.batDauTrang("KIEM_TRA");
        hienDoiChieu();
    }

    // Hiển thị nội dung đối chiếu và các thao tác của báo cáo hiện tại.
    void hienDoiChieu() {
        JPanel pnlTrang = taoTrang(
                "Đối chiếu báo cáo kiểm tra",
                "Kiểm tra đầy đủ kết quả trước khi chuyển cho tiếp nhận lập báo giá.",
                "Danh sách / quay lại",
                "KIEM_TRA");

        JPanel pnlTrai = taoCot();

        themThanhPhan(pnlTrai, taoHang(
                taoGiaTri("Phiếu tiếp nhận", khongGian.phieu.getMaPhieu()),
                taoGiaTri("Biển số", khongGian.phieu.getBienSo()),
                taoGiaTri("Người kiểm tra", khongGian.taiKhoan.getTenDangNhap())));

        DefaultTableModel model = taoModel(
                "Khu vực",
                "Hạng mục",
                "Kết quả",
                "Mức độ",
                "Đề xuất",
                "Người ghi",
                "Thời điểm ghi");

        for (ChiTietKiemTra dong : khongGian.baoCao.getChiTiet()) {
            model.addRow(new Object[] {
                    dong.getTenKhuVuc(),
                    dong.getTenHangMuc(),
                    dong.getKetQua(),
                    dong.getMucDo(),
                    dong.getDeXuat(),
                    dong.getNguoiGhi(),
                    dong.getThoiDiemGhi() == null
                            ? ""
                            : dong.getThoiDiemGhi().format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy HH:mm"))
            });
        }

        themThanhPhan(pnlTrang, taoThe(
                "Các hạng mục đã kiểm tra",
                "Kết quả, đề xuất và người ghi từng dòng.",
                bocBang(taoBang(model))));

        pnlTrai.add(taoThongBao(
                "Đề xuất là cơ sở để tiếp nhận lập báo giá; "
                        + "chưa cho phép sửa khi khách chưa duyệt.",
                true));

        JPanel pnlPhai = taoCot();

        themThanhPhan(pnlPhai, taoTruong(
                "Tình trạng khi nhận · chỉ đọc",
                khongGian.taoONhap(khongGian.phieu.getTinhTrang(), true)));

        boolean daXong = khongGian.baoCao.getTrangThai() == TrangThaiKiemTra.HOAN_TAT;

        khongGian.txtKetLuan = daXong
                ? khongGian.taoONhap(khongGian.baoCao.getNoiDung()[0], true)
                : khongGian.taoONhap(khongGian.baoCao.getNoiDung()[0]);

        themThanhPhan(pnlPhai, taoTruong(
                "Kết luận kiểm tra", khongGian.txtKetLuan));

        themThanhPhan(pnlPhai, taoTruong(
                "Trạng thái báo cáo · chỉ đọc",
                khongGian.taoONhap(
                        daXong ? "Đã hoàn tất" : "Chưa xác nhận",
                        true)));

        themThanhPhan(pnlPhai, taoThongBao(
                "Chỉ hoàn tất khi nội dung kiểm tra đúng. "
                        + "Chuyển kết quả cho Tiếp nhận lập báo giá.",
                true));

        JButton btnXong = taoNut(
                "Hoàn tất báo cáo", "HOAN_TAT_BAO_CAO", true, khongGian);

        btnXong.setEnabled(!daXong);
        pnlPhai.add(btnXong);

        if (!daXong) {
            pnlPhai.add(Box.createVerticalStrut(10));
            pnlPhai.add(taoNut(
                    "Sửa bản nháp", "SUA_BAO_CAO", false, khongGian));

            pnlPhai.add(Box.createVerticalStrut(10));

            JButton btnXoa = taoNut(
                    "Xóa bản nháp", "XOA_BAO_CAO", false, khongGian);

            btnXoa.setForeground(new Color(0xB3261E));
            btnXoa.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xB3261E)),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));

            pnlPhai.add(btnXoa);
        }

        themThanhPhan(pnlTrang, taoHang(
                taoThe(
                        "Đối chiếu báo cáo " + khongGian.baoCao.getMaBaoCao(),
                        "Thông tin chẩn đoán phục vụ lập báo giá; không thay bằng bảng giá.",
                        pnlTrai),
                taoThe(
                        "Kiểm tra trước khi gửi",
                        "Đối chiếu kết quả với tình trạng ban đầu.",
                        pnlPhai)));

        hienTrang(pnlTrang);
    }

    // Xác nhận kết quả kiểm tra và lưu báo cáo ở trạng thái hoàn tất.
    void hoanTatBaoCao() {
        if (!khongGian.xacNhan(
                "Đối chiếu báo cáo kiểm tra",
                "Xác nhận kết quả kiểm tra trước khi gửi tiếp nhận.",
                new String[][] {
                        {"Báo cáo", khongGian.baoCao.getMaBaoCao()},
                        {"Phiếu tiếp nhận", khongGian.phieu.getMaPhieu()},
                        {"Biển số", khongGian.phieu.getBienSo()},
                        {"Kết luận", khongGian.txtKetLuan.getText()}
                },
                "Chỉ hoàn tất khi kết quả kiểm tra đã được đối chiếu.",
                "Hoàn tất báo cáo",
                false)) {
            return;
        }

        String[] noiDung = khongGian.baoCao.getNoiDung();
        noiDung[0] = khongGian.txtKetLuan.getText().trim();

        khongGian.baoCao = khongGian.dao.luuBaoCao(
                new BaoCaoKiemTra(
                        khongGian.baoCao.getMaBaoCao(),
                        khongGian.phieu,
                        khongGian.baoCao.getNgayKiemTra(),
                        khongGian.baoCao.getBoPhan(),
                        noiDung,
                        TrangThaiKiemTra.NHAP,
                        null,
                        khongGian.baoCao.getChiTiet()),
                true);

        khongGian.batDauTrang("KIEM_TRA");
        hienDoiChieu();
    }
}