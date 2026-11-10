package ui.screen.tiepnhan;

import dao.BaoGiaDAO;
import dao.DuLieuGarageDAO;
import dao.PhuTungSuDungDAO;
import entity.BaoGia;
import entity.BaoGia.ChiTietBaoGia;
import entity.BaoGia.TrangThaiBaoGia;
import entity.PhieuTiepNhan.BaoCaoKiemTra;
import entity.TaiKhoan;
import entity.TaiKhoan.VaiTro;
import entity.PhieuTiepNhan;
import ui.component.KiemTraRoiTrang;
import ui.component.ScrollablePanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import ui.component.UiTheme;
import ui.dialog.tiepnhan.HangMucBaoGiaDialog;

/** Quản lý báo giá trong phiên; nội dung và chuyển trạng thái được kiểm tra tại DAO. */
public final class BaoGiaPanel extends JPanel implements KiemTraRoiTrang {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean choPhepRoiTrang() {
        return choPhepRoiManHinh();
    }
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private final TaiKhoan phien;
    private final CardLayout boCuc = new CardLayout();
    private final JPanel pnlThe = nen(boCuc);
    private final List<BaoCaoKiemTra> baoCaoHoanTat = new ArrayList<>();
    private final BaoGiaDAO dao;
    /** Đọc lại khi mở chọn mặt hàng để nhận danh mục và trạng thái mới nhất. */
    private final PhuTungSuDungDAO danhMuc;
    /** Bản nhập riêng, không sửa chi tiết của phiên bản đã gửi khách. */
    private final List<ChiTietBaoGia> banNhap = new ArrayList<>();
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] { "Tất cả trạng thái", "Nháp",
            "Chờ khách xác nhận", "Đã đồng ý", "Yêu cầu điều chỉnh", "Từ chối" });
    private final DefaultTableModel modelDanhSach = model("Mã báo giá", "Phiếu tiếp nhận", "Biển số", "Tổng dự kiến",
            "Phiên bản", "Trạng thái");
    private final JTable tblDanhSach = bang(modelDanhSach);
    private final JLabel lblDanhSach = nhan(" ", false);
    private final JComboBox<String> cboBaoCao = new JComboBox<>();
    private final JTextArea txtDeXuat = new JTextArea(3, 20);
    private final JLabel lblCanCu = nhan("Chọn kết quả kiểm tra hoàn tất.", false);
    private final JLabel lblPhienBan = nhan("Bản nháp · chưa lưu", true);
    private final JTextField txtHen = new JTextField();
    private final DefaultTableModel modelChiTiet = model("Hạng mục", "Loại", "SL", "ĐVT", "Đơn giá", "Thành tiền");
    private final JTable tblChiTiet = bang(modelChiTiet);
    private final JLabel lblTong = nhan("0 đ", true);
    private final JLabel lblLoi = nhan(" ", false);
    private final DefaultTableModel modelQuyetDinh = model("Hạng mục", "SL / ĐVT", "Thành tiền");
    private final JLabel lblBaoGiaGui = nhan(" ", true);
    private final JLabel lblTongQuyetDinh = nhan(" ", true);
    private final JRadioButton radDongY = new JRadioButton("Đồng ý báo giá");
    private final JRadioButton radDieuChinh = new JRadioButton("Yêu cầu điều chỉnh");
    private final JRadioButton radTuChoi = new JRadioButton("Từ chối báo giá");
    private final ButtonGroup nhomQuyetDinh = new ButtonGroup();
    private final JTextField txtPhanHoiLuc = new JTextField();
    private final JTextField txtKenh = new JTextField();
    private final JTextArea txtGhiChu = new JTextArea(3, 20);
    private final JButton btnGhiNhan = UiTheme.createAppButton("Ghi nhận quyết định", true);
    private final JLabel lblPhanHoi = nhan("Chưa chọn phản hồi · chưa lưu nghiệp vụ.", false);
    private BaoGia dangXem, coSoDieuChinh;
    private String maThaoTac;
    private boolean dangNap, daThayDoi;

    /** Khởi tạo dữ liệu và thành phần cần dùng của lớp. */
    public BaoGiaPanel(BaoGiaDAO dao, TaiKhoan phien) {
        this(dao, phien, new PhuTungSuDungDAO(false));
    }

    /** Nhận nguồn danh mục đang dùng chung với Quản lý và Kỹ thuật. */
    public BaoGiaPanel(BaoGiaDAO dao, TaiKhoan phien, PhuTungSuDungDAO danhMuc) {
        super(new BorderLayout());
        if (dao == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.dao = dao;
        if (danhMuc == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.danhMuc = danhMuc;
        if (phien == null) {
            throw new NullPointerException("Thiếu dữ liệu bắt buộc.");
        }
        this.phien = phien;
        kiemTraPhien();
        setOpaque(false);
        pnlThe.add(taoDanhSach(), "DANH_SACH");
        pnlThe.add(taoForm(), "FORM");
        pnlThe.add(taoQuyetDinh(), "QUYET_DINH");
        add(pnlThe);
        cboBaoCao.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dangNap) {
                    capNhatCanCu();
                    daThayDoi = true;
                }
            }
        });
        theoDoi(txtHen);
        theoDoi(txtPhanHoiLuc);
        theoDoi(txtKenh);
        theoDoi(txtGhiChu);
        locDanhSach();
    }

    /** Thực hiện cho phep roi man hinh cho thành phần hiện tại. */
    public boolean choPhepRoiManHinh() {
        return !daThayDoi || JOptionPane.showConfirmDialog(this, "Bản nhập báo giá/phản hồi chưa lưu. Bỏ thay đổi?",
                "Dữ liệu chưa lưu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Mở danh sach khi người dùng yêu cầu. */
    public void moDanhSach() {
        daThayDoi = false;
        boCuc.show(pnlThe, "DANH_SACH");
        locDanhSach();
    }

    /** Tạo danh sach cho giao diện. */
    private JPanel taoDanhSach() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        JPanel dau = nen(new BorderLayout(0, 16));
        dau.add(tieuDe("Báo giá", "Báo giá dựa trên kết quả kiểm tra hoàn tất.",
                nut("+ Lập báo giá", true, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        moForm(null);
                    }
                })), BorderLayout.NORTH);
        JPanel loc = khung(new BorderLayout(8, 0));
        txtTim.setBorder(BorderFactory.createTitledBorder("Mã báo giá / khách / biển số"));
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
                BaoGiaPanel.this.locDanhSach();
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
        ds.add(nhan("Danh sách báo giá · lưu trong phiên", true), BorderLayout.NORTH);
        ds.add(cuonBang(tblDanhSach));
        JPanel tacVu = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tacVu.add(nut("Xem chi tiết", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moDaChon(false);
            }
        }));
        tacVu.add(nut("Điều chỉnh / phát sinh", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                moDaChon(true);
            }
        }));
        tacVu.add(nut("Phản hồi khách", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                BaoGiaPanel.this.phanHoiKhach();
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
        pnl.add(tieuDe("Lập / điều chỉnh báo giá", "1. Kết quả kiểm tra → 2. Hạng mục & chi phí → 3. Khách phản hồi",
                nut("Danh sách / quay lại", false, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {

                        if (choPhepRoiManHinh()) {
                            moDanhSach();
                        }

                    }
                })), BorderLayout.NORTH);
        JPanel khung = khung(new BorderLayout(0, 16));
        JPanel dau = nen(new GridLayout(0, 1, 0, 8));
        dau.add(nhan("Báo cáo kiểm tra hoàn tất", true));
        for (BaoCaoKiemTra bc : baoCaoHoanTat) {
            cboBaoCao.addItem(bc.getMaBaoCao() + " · " + bc.getPhieu().getBienSo());
        }
        dau.add(cboBaoCao);
        dau.add(lblCanCu);
        dau.add(lblPhienBan);
        txtDeXuat.setEditable(false);
        txtDeXuat.setLineWrap(true);
        txtDeXuat.setWrapStyleWord(true);
        txtDeXuat.setFont(UiTheme.APP_FONT);
        dau.add(new JScrollPane(txtDeXuat));
        dau.add(truong("Ngày hẹn dự kiến * · dd/MM/yyyy HH:mm", txtHen));
        epCot(dau);
        khung.add(dau, BorderLayout.NORTH);
        JPanel chiTiet = nen(new BorderLayout(0, 10));
        JScrollPane bang = cuonBang(tblChiTiet);
        bang.setPreferredSize(new Dimension(0, 250));
        chiTiet.add(bang);
        JPanel tacVu = nen(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tacVu.add(nut("Thêm dòng", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                suaDong(false);
            }
        }));
        tacVu.add(nut("Sửa dòng", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                suaDong(true);
            }
        }));
        tacVu.add(nut("Xóa dòng", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                BaoGiaPanel.this.xoaDong();
            }
        }));
        chiTiet.add(tacVu, BorderLayout.SOUTH);
        khung.add(chiTiet);
        JPanel chan = nen(new GridLayout(0, 1, 0, 10));
        chan.add(lblTong);
        lblLoi.setForeground(UiTheme.ERROR);
        chan.add(lblLoi);
        chan.add(nhan("Bản đã gửi chỉ đọc. Điều chỉnh tạo phiên bản mới và cần khách duyệt lại.", false));
        JPanel nutLuu = nen(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutLuu.add(nut("Lưu nháp", false, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                luuBanNhap();
            }
        }));
        nutLuu.add(nut("Ghi nhận gửi khách", true, new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent event) {
                BaoGiaPanel.this.guiKhach();
            }
        }));
        chan.add(nutLuu);
        khung.add(chan, BorderLayout.SOUTH);
        pnl.add(cuonNoiDung(khung));
        return pnl;
    }

    /** Tạo quyet dinh cho giao diện. */
    private JPanel taoQuyetDinh() {
        JPanel pnl = nen(new BorderLayout(0, 16));
        pnl.add(tieuDe("Ghi nhận quyết định khách hàng", "Đối chiếu đúng báo giá và phiên bản đã gửi khách.",
                nut("Quay lại", false, new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        BaoGiaPanel.this.quayLaiForm();
                    }
                })), BorderLayout.NORTH);
        JPanel hang = nen(new GridLayout(1, 2, 16, 0));
        JPanel trai = khung(new BorderLayout(0, 12));
        trai.add(lblBaoGiaGui, BorderLayout.NORTH);
        trai.add(cuonBang(bang(modelQuyetDinh)));
        trai.add(lblTongQuyetDinh, BorderLayout.SOUTH);
        hang.add(trai);
        JPanel phai = khung(new BorderLayout(0, 14));
        JPanel truong = nen(new GridLayout(0, 1, 0, 10));
        truong.add(nhan("Kết quả phản hồi khách · không mặc định đồng ý", true));
        for (JRadioButton rad : new JRadioButton[] { radDongY, radDieuChinh, radTuChoi }) {
            rad.setFont(UiTheme.APP_FONT);
            rad.setOpaque(false);
            nhomQuyetDinh.add(rad);
            rad.addActionListener(new ActionListener() {
                /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                @Override
                public void actionPerformed(ActionEvent e) {
                    btnGhiNhan.setEnabled(true);
                    daThayDoi = true;
                    lblPhanHoi.setText("Phản hồi chưa lưu nghiệp vụ.");
                }
            });
            truong.add(rad);
        }
        truong.add(truong("Thời điểm phản hồi * · dd/MM/yyyy HH:mm", txtPhanHoiLuc));
        truong.add(truong("Kênh phản hồi *", txtKenh));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        truong.add(truong("Ghi chú phản hồi", txtGhiChu));
        epCot(truong);
        phai.add(truong, BorderLayout.NORTH);
        JPanel chan = nen(new GridLayout(0, 1, 0, 10));
        chan.add(lblPhanHoi);
        chan.add(nhan("Từ chối/điều chỉnh: không tạo lệnh từ bản hiện tại.", false));
        btnGhiNhan.setEnabled(false);
        btnGhiNhan.addActionListener(new ActionListener() {
            /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
            @Override
            public void actionPerformed(ActionEvent e) {
                kiemTraPhanHoi();
            }
        });
        chan.add(btnGhiNhan);
        phai.add(chan, BorderLayout.SOUTH);
        hang.add(phai);
        hang.setPreferredSize(new Dimension(600, 680));
        JScrollPane cuon = cuonNoiDung(hang);
        cuon.getViewport().addComponentListener(new ComponentAdapter() {
            /** Thực hiện component resized cho thành phần hiện tại. */
            @Override
            public void componentResized(ComponentEvent e) {
                boolean hep = cuon.getViewport().getWidth() < 850;
                GridLayout layout = (GridLayout) hang.getLayout();
                layout.setRows(hep ? 2 : 1);
                layout.setColumns(hep ? 1 : 2);
                layout.setVgap(16);
                hang.setPreferredSize(new Dimension(600, hep ? 1300 : 680));
                hang.revalidate();
            }
        });
        pnl.add(cuon);
        return pnl;
    }

    /** Mở form khi người dùng yêu cầu. */
    private void moForm(BaoGia baoGia) {
        kiemTraPhien();
        dangNap = true;
        dangXem = baoGia;
        coSoDieuChinh = null;
        maThaoTac = DuLieuGarageDAO.taoMaThaoTac();
        banNhap.clear();
        if (baoGia != null) {
            banNhap.addAll(baoGia.getChiTiet());
        }
        baoCaoHoanTat.clear();
        baoCaoHoanTat.addAll(dao.layCanCu(phien));
        // Cho phép xem lịch sử ngay cả khi nguồn kiểm tra không còn có trong danh sách hiện tại.
        if (baoGia != null && !baoCaoHoanTat.contains(baoGia.getBaoCao())) {
            baoCaoHoanTat.add(baoGia.getBaoCao());
        }
        cboBaoCao.removeAllItems();
        for (BaoCaoKiemTra bc : baoCaoHoanTat) {
            cboBaoCao.addItem(bc.getMaBaoCao() + " · " + bc.getPhieu().getBienSo());
        }
        if (baoGia != null) {
            cboBaoCao.setSelectedIndex(baoCaoHoanTat.indexOf(baoGia.getBaoCao()));
        }
        txtHen.setText((baoGia == null ? LocalDateTime.now().plusDays(1) : baoGia.getNgayHen()).format(NGAY));
        lblPhienBan.setText(baoGia == null ? "Chưa cấp mã · bản nháp chưa lưu" : moTa(baoGia));
        cboBaoCao.setEnabled(baoGia == null);
        txtHen.setEditable(duocSua());
        lblLoi.setText(baoGia == null || duocSua() ? " "
                : "Bản đã gửi chỉ đọc. Phản hồi đã lưu: " + baoGia.getKenhPhanHoi() + " · "
                        + baoGia.getGhiChuPhanHoi());
        capNhatCanCu();
        capNhatChiTiet();
        daThayDoi = false;
        dangNap = false;
        boCuc.show(pnlThe, "FORM");
    }

    /** Mở da chon khi người dùng yêu cầu. */
    private void moDaChon(boolean dieuChinh) {
        BaoGia bg = baoGiaDangChon();
        if (bg == null) {
            return;
        }
        if (dieuChinh && bg.getTrangThai() == TrangThaiBaoGia.NHAP) {
            lblDanhSach.setText("Hãy sửa bản nháp hiện có.");
            return;
        }
        moForm(bg);
        if (dieuChinh) {
            coSoDieuChinh = bg;
            dangXem = null;
            txtHen.setEditable(true);
            lblPhienBan.setText(bg.getMaBaoGiaGoc() + " · V" + (bg.getPhienBan() + 1) + " · điều chỉnh chưa lưu");
            lblLoi.setText(" ");
            daThayDoi = true;
        }
    }

    /** Thực hiện bao gia dang chon cho thành phần hiện tại. */
    private BaoGia baoGiaDangChon() {
        kiemTraPhien();
        int row = tblDanhSach.getSelectedRow();
        if (row < 0) {
            lblDanhSach.setText("Chọn báo giá trước.");
            return null;
        }
        String ma = (String) modelDanhSach.getValueAt(tblDanhSach.convertRowIndexToModel(row), 0);
        return dao.timTheoMa(phien, ma);
    }

    /** Thực hiện duoc sua cho thành phần hiện tại. */
    private boolean duocSua() {
        return dangXem == null || dangXem.getTrangThai() == TrangThaiBaoGia.NHAP;
    }

    /** Mở ta khi người dùng yêu cầu. */
    private static String moTa(BaoGia bg) {
        return bg.getMaBaoGia() + " · V" + bg.getPhienBan() + " · " + bg.getTrangThai().getTenHienThi();
    }

    /** Thực hiện sua dong cho thành phần hiện tại. */
    private void suaDong(boolean sua) {
        kiemTraPhien();
        if (!duocSua()) {
            lblLoi.setText("Bản gốc chỉ đọc. Dùng Điều chỉnh / phát sinh từ danh sách để tạo bản nhập riêng.");
            return;
        }
        int row = tblChiTiet.getSelectedRow();
        if (sua && row < 0) {
            lblLoi.setText("Chọn dòng để sửa.");
            return;
        }
        int index = sua ? tblChiTiet.convertRowIndexToModel(row) : -1;
        new HangMucBaoGiaDialog(SwingUtilities.getWindowAncestor(this), sua ? banNhap.get(index) : null,
                new ActionListener() {
                    /** Xử lý thao tác được gắn với nút hoặc lựa chọn này. */
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        ChiTietBaoGia dong = (ChiTietBaoGia) event.getSource();

                        kiemTraPhien();
                        if (index < 0) {
                            banNhap.add(dong);
                        } else {
                            banNhap.set(index, dong);
                        }
                        daThayDoi = true;
                        capNhatChiTiet();
                        lblLoi.setText(" ");

                    }
                }, danhMuc.docDanhMucDangHoatDong(), dao.layDichVu(phien)).setVisible(true);
    }

    /** Thực hiện xoa dong cho thành phần hiện tại. */
    private void xoaDong() {
        kiemTraPhien();
        if (!duocSua()) {
            lblLoi.setText("Bản gốc chỉ đọc; cần tạo bản điều chỉnh riêng.");
            return;
        }
        int row = tblChiTiet.getSelectedRow();
        if (row < 0) {
            lblLoi.setText("Chọn dòng để xóa.");
            return;
        }
        int index = tblChiTiet.convertRowIndexToModel(row);
        if (JOptionPane.showConfirmDialog(this, "Xóa dòng khỏi bản nhập?", "Xóa hạng mục",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            banNhap.remove(index);
            daThayDoi = true;
            capNhatChiTiet();
        }
    }

    /** Thực hiện cap nhat can cu cho thành phần hiện tại. */
    private void capNhatCanCu() {
        int index = cboBaoCao.getSelectedIndex();
        if (index < 0) {
            lblCanCu.setText("Không có báo cáo hoàn tất; chưa thể lập báo giá.");
            txtDeXuat.setText("");
            return;
        }
        BaoCaoKiemTra bc = baoCaoHoanTat.get(index);
        PhieuTiepNhan phieu = bc.getPhieu();
        lblCanCu.setText(phieu.getMaPhieu() + " · " + phieu.getTenKhach() + " · " + phieu.getBienSo());
        txtDeXuat.setText("Kết quả: " + bc.getNoiDung()[0] + "\nĐề xuất: " + bc.getNoiDung()[2]);
        // Hiển thị thêm các hạng mục kiểm tra linh hoạt để Tiếp nhận có đủ căn cứ lập báo giá.
        StringBuilder chiTietKiemTra = new StringBuilder();
        for (entity.ChiTietKiemTra dong : bc.getChiTiet()) {
            chiTietKiemTra.append("\n\nKhu vực: ").append(dong.getTenKhuVuc());
            chiTietKiemTra.append(" · Hạng mục: ").append(dong.getTenHangMuc());
            chiTietKiemTra.append("\nKết quả: ").append(dong.getKetQua());
            chiTietKiemTra.append("\nMức độ: ").append(dong.getMucDo());
            chiTietKiemTra.append("\nĐề xuất: ").append(dong.getDeXuat());
        }
        txtDeXuat.append(chiTietKiemTra.toString());
        if (dangXem != null) {
            String lichSu = "\nNgười lập: " + dangXem.getNguoiLap() + " · " + dangXem.getNgayLap().format(NGAY);
            if (dangXem.getNgayGui() != null) {
                lichSu += "\nGửi khách: " + dangXem.getNguoiGui() + " · " + dangXem.getNgayGui().format(NGAY);
            }
            if (dangXem.getThoiDiemPhanHoi() != null) {
                lichSu += "\nKhách phản hồi: " + dangXem.getThoiDiemPhanHoi().format(NGAY) + " · "
                        + dangXem.getKenhPhanHoi() + "\nGhi nhận: " + dangXem.getNguoiGhiNhan() + " · "
                        + dangXem.getThoiDiemGhiNhan().format(NGAY) + "\nGhi chú: " + dangXem.getGhiChuPhanHoi();
            }
            txtDeXuat.append(lichSu);
        }
        txtDeXuat.setCaretPosition(0);
    }

    /** Thực hiện cap nhat chi tiet cho thành phần hiện tại. */
    private void capNhatChiTiet() {
        modelChiTiet.setRowCount(0);
        for (ChiTietBaoGia dong : banNhap) {
            modelChiTiet.addRow(new Object[] { dong.getTenHangMuc(), dong.getLoai(), dong.getSoLuong(), dong.getDonVi(),
                    dong.getDonGia(), dong.getThanhTien() });
        }
        lblTong.setText("Tổng dự kiến: " + tien(tong(banNhap)) + " đ · tự tính từ chi tiết");
    }

    /** Kiểm tra và lưu ban nhap. */
    private boolean luuBanNhap() {
        kiemTraPhien();
        if (!duocSua()) {
            lblLoi.setText("Bản đã gửi chỉ đọc; hãy tạo bản điều chỉnh.");
            return false;
        }
        if (cboBaoCao.getSelectedIndex() < 0) {
            lblLoi.setText("Cần báo cáo kiểm tra hoàn tất liên kết đúng phiếu tiếp nhận.");
            return false;
        }
        try {
            LocalDateTime hen = LocalDateTime.parse(txtHen.getText().trim(), NGAY);
            if (dangXem != null) {
                dangXem = dao.suaNhap(phien, dangXem, banNhap, hen);
            } else if (coSoDieuChinh != null) {
                dangXem = dao.taoDieuChinh(phien, maThaoTac, coSoDieuChinh, banNhap, hen);
            } else {
                dangXem = dao.taoNhap(phien, maThaoTac, baoCaoHoanTat.get(cboBaoCao.getSelectedIndex()).getMaBaoCao(),
                        banNhap, hen);
            }
            coSoDieuChinh = null;
            cboBaoCao.setEnabled(false);
            lblPhienBan.setText(moTa(dangXem));
            lblLoi.setText("Đã lưu nháp trong phiên.");
            daThayDoi = false;
            return true;
        } catch (DateTimeParseException e) {
            lblLoi.setText("Ngày hẹn không hợp lệ; dùng dd/MM/yyyy HH:mm.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblLoi.setText(e.getMessage());
        }
        return false;
    }

    /** Thực hiện gui khach cho thành phần hiện tại. */
    private void guiKhach() {
        kiemTraPhien();
        if (dangXem != null && dangXem.getTrangThai() != TrangThaiBaoGia.NHAP) {
            if (dangXem.getTrangThai() == TrangThaiBaoGia.CHO_XAC_NHAN) {
                moPhanHoi();
            } else {
                lblLoi.setText("Bản này đã có phản hồi; cần tạo phiên bản điều chỉnh.");
            }

            return;
        }
        if (!luuBanNhap()) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Ghi nhận đã gửi " + moTa(dangXem) + " cho khách? Nội dung sẽ chỉ đọc.",
                "Ghi nhận gửi báo giá", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dangXem = dao.guiKhach(phien, dangXem);
            txtHen.setEditable(false);
            lblPhienBan.setText(moTa(dangXem));
            moPhanHoi();
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblLoi.setText(e.getMessage());
        }
    }

    /** Thực hiện phan hoi khach cho thành phần hiện tại. */
    private void phanHoiKhach() {
        BaoGia bg = baoGiaDangChon();
        if (bg == null) {
            return;
        }
        if (bg.getTrangThai() != TrangThaiBaoGia.CHO_XAC_NHAN) {
            lblDanhSach.setText("Chỉ ghi nhận phản hồi cho bản đang chờ khách xác nhận; xem chi tiết để đọc lịch sử.");
            return;
        }
        moForm(bg);
        moPhanHoi();
    }

    /** Mở phan hoi khi người dùng yêu cầu. */
    private void moPhanHoi() {
        dangNap = true;
        modelQuyetDinh.setRowCount(0);
        for (ChiTietBaoGia item : dangXem.getChiTiet()) {
            modelQuyetDinh.addRow(new Object[] { item.getTenHangMuc(),
                    String.valueOf(item.getSoLuong()) + " " + item.getDonVi(), item.getThanhTien() });
        }
        lblBaoGiaGui.setText(moTa(dangXem));
        lblTongQuyetDinh.setText(
                "Tổng dự kiến: " + tien(dangXem.getTongTien()) + " đ · Hẹn " + dangXem.getNgayHen().format(NGAY));
        nhomQuyetDinh.clearSelection();
        btnGhiNhan.setEnabled(false);
        txtPhanHoiLuc.setText(LocalDateTime.now().format(NGAY));
        txtKenh.setText("");
        txtGhiChu.setText("");
        lblPhanHoi.setText("Chưa chọn phản hồi · chưa lưu nghiệp vụ.");
        dangNap = false;
        boCuc.show(pnlThe, "QUYET_DINH");
    }

    /** Thực hiện quay lai form cho thành phần hiện tại. */
    private void quayLaiForm() {
        if (!choPhepRoiManHinh()) {
            return;
        }
        moForm(dangXem);
    }

    /** Kiểm tra phan hoi trước khi xử lý. */
    private void kiemTraPhanHoi() {
        kiemTraPhien();
        if (nhomQuyetDinh.getSelection() == null) {
            lblPhanHoi.setText("Cần chọn phản hồi khách.");
            return;
        }
        TrangThaiBaoGia quyetDinh = radDongY.isSelected() ? TrangThaiBaoGia.DA_DONG_Y
                : radDieuChinh.isSelected() ? TrangThaiBaoGia.YEU_CAU_DIEU_CHINH : TrangThaiBaoGia.TU_CHOI;
        try {
            LocalDateTime luc = LocalDateTime.parse(txtPhanHoiLuc.getText().trim(), NGAY);
            dangXem = dao.ghiNhanPhanHoi(phien, dangXem, quyetDinh, luc, txtKenh.getText(), txtGhiChu.getText());
            moDanhSach();
            lblDanhSach.setText("Đã lưu phản hồi · " + moTa(dangXem));
        } catch (DateTimeParseException e) {
            lblPhanHoi.setText("Thời điểm phản hồi không hợp lệ.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            lblPhanHoi.setText(e.getMessage());
        }
    }

    /** Thực hiện loc danh sach cho thành phần hiện tại. */
    private void locDanhSach() {
        kiemTraPhien();
        modelDanhSach.setRowCount(0);
        TrangThaiBaoGia trangThai = cboTrangThai.getSelectedIndex() <= 0 ? null
                : TrangThaiBaoGia.values()[cboTrangThai.getSelectedIndex() - 1];
        for (BaoGia bg : dao.timKiem(phien, txtTim.getText(), trangThai)) {
            modelDanhSach.addRow(new Object[] { bg.getMaBaoGia(), bg.getBaoCao().getPhieu().getMaPhieu(),
                    bg.getBienSo(), bg.getTongTien(), "V" + bg.getPhienBan(), bg.getTrangThai().getTenHienThi() });
        }
        lblDanhSach.setText(modelDanhSach.getRowCount() + " báo giá · dữ liệu chỉ giữ trong phiên sử dụng.");
    }

    /** Thực hiện theo doi cho thành phần hiện tại. */
    private void theoDoi(JTextComponent component) {
        component.getDocument().addDocumentListener(new DocumentListener() {
            /** Thực hiện doi cho thành phần hiện tại. */
            private void doi() {
                if (!dangNap) {
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

    /** Kiểm tra phien trước khi xử lý. */
    private void kiemTraPhien() {
        if (phien == null || phien.getVaiTro() != VaiTro.NHAN_VIEN_TIEP_NHAN) {
            throw new SecurityException("Chức năng dành cho tiếp nhận.");
        }
    }

    /** Thực hiện tong cho thành phần hiện tại. */
    private static long tong(List<ChiTietBaoGia> dong) {
        long tong = 0;
        for (ChiTietBaoGia ct : dong) {
            long tien = ct.getThanhTien();
            if (Long.MAX_VALUE - tong < tien) {
                throw new IllegalArgumentException("Tổng báo giá vượt giới hạn.");
            }
            tong += tien;
        }
        return tong;
    }

    /** Thực hiện tien cho thành phần hiện tại. */
    private static String tien(long giaTri) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(giaTri);
    }

    /** Giữ nhịp form gọn: nhãn/radio không bị GridLayout kéo cao theo textarea. */
    private static void epCot(JPanel pnl) {
        Component[] cacThanhPhan = pnl.getComponents();
        pnl.removeAll();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        for (Component component : cacThanhPhan) {
            JComponent c = (JComponent) component;
            int cao = c instanceof JComboBox ? 36 : c.getPreferredSize().height;
            if (c instanceof JScrollPane) {
                cao = 65;
            }
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, cao));
            c.setPreferredSize(new Dimension(0, cao));
            pnl.add(c);
            pnl.add(Box.createVerticalStrut(8));
        }
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
    private static JLabel nhan(String chu, boolean dam) {
        JLabel lbl = new JLabel(chu);
        lbl.setFont(dam ? UiTheme.APP_BOLD_FONT : UiTheme.APP_FONT);
        lbl.setForeground(dam ? UiTheme.APP_TEXT : UiTheme.APP_MUTED);
        return lbl;
    }

    /** Đồng bộ chiều cao điều khiển lọc với ô tìm kiếm có nhãn viền. */
    private static <T extends Component> T caoBangOtim(T control) {
        Dimension kichThuoc = control.getPreferredSize();
        control.setPreferredSize(new Dimension(kichThuoc.width, 36));
        return control;
    }

    /** Thực hiện nut cho thành phần hiện tại. */
    private static JButton nut(String ten, boolean chinh, ActionListener action) {
        JButton btn = UiTheme.createAppButton(ten, chinh);
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

    /** Thực hiện truong cho thành phần hiện tại. */
    private static JPanel truong(String ten, JComponent component) {
        JPanel pnl = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(ten, true);
        lbl.setLabelFor(component);
        pnl.add(lbl, BorderLayout.NORTH);
        component.setFont(UiTheme.APP_FONT);
        component.setPreferredSize(new Dimension(0, 40));
        pnl.add(component instanceof JTextArea ? new JScrollPane(component) : component);
        return pnl;
    }

    /** Thực hiện tieu de cho thành phần hiện tại. */
    private static JPanel tieuDe(String ten, String moTa, JButton thaoTac) {
        JPanel pnl = nen(new BorderLayout(8, 10));
        pnl.add(nhan("Tiếp nhận / Nghiệp vụ / " + ten, false), BorderLayout.NORTH);
        JPanel chu = nen(new BorderLayout(0, 6));
        JLabel lbl = nhan(ten, true);
        lbl.setFont(UiTheme.APP_BOLD_FONT.deriveFont(24f));
        chu.add(lbl, BorderLayout.NORTH);
        chu.add(nhan(moTa, false));
        pnl.add(chu);
        JPanel nut = nen(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        nut.add(thaoTac);
        pnl.add(nut, BorderLayout.EAST);
        return pnl;
    }

    /** Mở del khi người dùng yêu cầu. */
    private static DefaultTableModel model(String... cot) {
        return new DefaultTableModel(cot, 0) {
            private static final long serialVersionUID = 1L;

            /** Cho biết cell editable. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            /** Trả về column class. */
            @Override
            public Class<?> getColumnClass(int col) {
                return getRowCount() > 0 && getValueAt(0, col) instanceof Number ? getValueAt(0, col).getClass()
                        : String.class;
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
        tbl.setAutoCreateRowSorter(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getTableHeader().setFont(UiTheme.APP_BOLD_FONT);
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);
        tbl.setDefaultRenderer(Long.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            /** Kiểm tra và cập nhật value. */
            @Override
            public void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(value instanceof Number ? tien(((Number) value).longValue()) : "");
            }
        });
        return tbl;
    }

    /** Thực hiện cuon bang cho thành phần hiện tại. */
    private static JScrollPane cuonBang(JTable tbl) {
        JScrollPane cuon = new JScrollPane(tbl);
        cuon.getViewport().setBackground(Color.WHITE);
        return cuon;
    }

    /** Thực hiện cuon noi dung cho thành phần hiện tại. */
    private static JScrollPane cuonNoiDung(JPanel pnl) {
        JPanel neo = new ScrollablePanel();
        neo.add(pnl, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(neo);
        cuon.setBorder(null);
        cuon.getViewport().setBackground(UiTheme.APP_BACKGROUND);
        cuon.getVerticalScrollBar().setUnitIncrement(20);
        return cuon;
    }
}
