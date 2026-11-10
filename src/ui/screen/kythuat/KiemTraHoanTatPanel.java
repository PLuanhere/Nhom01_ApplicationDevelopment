package ui.screen.kythuat;

import entity.PhieuSuaChua.ChiTietSuaChua;
import entity.PhieuSuaChua;
import entity.PhieuSuaChua.TrangThaiSuaChua;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;


import static ui.screen.kythuat.GiaoDienKyThuat.*;

// Hiển thị kiểm tra cuối và xử lý hoàn tất lệnh sửa chữa.
public final class KiemTraHoanTatPanel extends TrangKyThuatPanel {

    private static final long serialVersionUID = 1L;

    public KiemTraHoanTatPanel(KhongGianKyThuatPanel khongGian) {
        super(khongGian);
    }

    // Hiển thị các hạng mục và điều kiện kiểm tra trước khi hoàn tất lệnh.
    public void hienHoanTat() {
        PhieuSuaChua lenh = khongGian.dao.docLenh(khongGian.maLenh);

        JPanel pnlTrang = taoTrang(
                "Kiểm tra hoàn tất sửa chữa",
                "Rà soát hạng mục, kết quả và phụ tùng rồi báo hoàn thành.",
                "Danh sách / quay lại",
                "PHAN_CONG");

        themThanhPhan(pnlTrang, taoThanhBuoc(
                "Hạng mục", "Kiểm tra cuối", "Báo hoàn tất"));

        JPanel pnlCot = taoCot();
        int daXong = 0;

        for (ChiTietSuaChua hangMuc : lenh.getDanhSachHangMuc()) {
            if (hangMuc.getTrangThai() == TrangThaiSuaChua.HOAN_THANH) {
                daXong++;
            }
        }

        themThanhPhan(pnlCot, taoHang(
                taoGiaTri("Biển số / xe", lenh.getBienSo()),
                taoGiaTri("Kỹ thuật viên phụ trách", lenh.getKyThuatChinh()),
                taoGiaTri(
                        "Tiến độ",
                        daXong + "/" + lenh.getDanhSachHangMuc().size()
                                + " hạng mục hoàn thành")));

        khongGian.modelNhap = new DefaultTableModel(
                new String[] {
                        "Hạng mục", "Kết quả", "Phụ tùng thực tế", "Kiểm tra cuối · Đạt"
                },
                0) {

            private static final long serialVersionUID = 1L;

            // Khai báo cột kiểm tra kiểu Boolean để hiển thị checkbox.
            @Override
            public Class<?> getColumnClass(int cot) {
                return cot == 3 ? Boolean.class : Object.class;
            }

            // Chỉ cho đánh dấu đạt khi hạng mục đã hoàn thành và lệnh chưa khóa.
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return cot == 3
                        && lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH
                        && lenh.getDanhSachHangMuc().get(dong).getTrangThai()
                                == TrangThaiSuaChua.HOAN_THANH;
            }
        };

        for (int viTri = 0; viTri < lenh.getDanhSachHangMuc().size(); viTri++) {
            ChiTietSuaChua hangMuc = lenh.getDanhSachHangMuc().get(viTri);

            khongGian.modelNhap.addRow(new Object[] {
                    hangMuc.getTenHangMuc(),
                    hangMuc.getKetQua(),
                    khongGian.docThucDungAnToan(viTri),
                    lenh.getTrangThai() == TrangThaiSuaChua.HOAN_THANH
            });
        }

        khongGian.tblNhap = taoBang(khongGian.modelNhap);

        javax.swing.JCheckBox chkDat = new javax.swing.JCheckBox();
        PhuTungUi.dinhDangKiemTra(chkDat);

        khongGian.tblNhap.setDefaultEditor(
                Boolean.class, new javax.swing.DefaultCellEditor(chkDat));

        khongGian.tblNhap.setDefaultRenderer(
                Boolean.class, new javax.swing.table.TableCellRenderer() {
                    // Hiển thị checkbox theo định dạng chung của giao diện kỹ thuật.
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable bang, Object giaTri, boolean chon,
                            boolean tieuDiem, int dong, int cot) {

                        javax.swing.JCheckBox chkVe = new javax.swing.JCheckBox();
                        PhuTungUi.dinhDangKiemTra(chkVe);
                        chkVe.setSelected(Boolean.TRUE.equals(giaTri));
                        chkVe.setHorizontalAlignment(SwingConstants.LEFT);

                        return chkVe;
                    }
                });

        themThanhPhan(pnlCot, bocBang(khongGian.tblNhap));

        String lyDo = khongGian.dao.docLyDoChuaHoanTat(khongGian.maLenh);

        themThanhPhan(pnlCot, taoThongBao(
                lyDo.isEmpty()
                        ? "Đối chiếu từng hạng mục và đánh dấu Đạt trước khi báo hoàn tất."
                        : lyDo,
                !lyDo.isEmpty()));

        khongGian.txtKetLuan = khongGian.taoONhap(lenh.getKetQuaHoanTat()[0]);
        khongGian.txtKetLuan.setEditable(
                lenh.getTrangThai() != TrangThaiSuaChua.HOAN_THANH);

        themThanhPhan(pnlCot, taoHang(
                taoTruong("Kết quả kiểm tra cuối *", khongGian.txtKetLuan),
                taoTruong(
                        "Phát sinh chưa duyệt? · chỉ đọc",
                        khongGian.taoONhap(
                                lenh.getPhatSinh() != null
                                        && lenh.getPhatSinh().getTrangThai()
                                                == entity.PhieuSuaChua.TrangThaiPhatSinh.CHO_DUYET
                                        ? "Có → buộc xử lý" : "Không có",
                                true))));

        khongGian.btnHoanTat = taoNut(
                "Hoàn tất khi đủ điều kiện", "HOAN_TAT_LENH", true, khongGian);

        khongGian.btnHoanTat.setEnabled(false);

        khongGian.modelNhap.addTableModelListener(
                new javax.swing.event.TableModelListener() {
                    // Kiểm tra điều kiện hoàn tất khi người dùng đánh dấu xác nhận.
                    @Override
                    public void tableChanged(javax.swing.event.TableModelEvent suKien) {
                        capNhatHoanTat();
                    }
                });

        pnlCot.add(taoHangNut(khongGian.btnHoanTat));

        themThanhPhan(pnlTrang, taoThe(
                "Kiểm tra hoàn tất " + khongGian.maLenh,
                "Chỉ xác nhận hoàn thành khi toàn bộ hạng mục hợp lệ đã hoàn tất "
                        + "và không còn phát sinh chưa duyệt.",
                pnlCot));

        hienTrang(pnlTrang);
    }

    // Bật nút hoàn tất khi có kết luận và tất cả hạng mục đều được đánh dấu đạt.
    void capNhatHoanTat() {
        if (!"HOAN_TAT".equals(khongGian.trang)
                || khongGian.btnHoanTat == null
                || khongGian.modelNhap == null) {
            return;
        }

        boolean duDieuKien = khongGian.dao.docLyDoChuaHoanTat(khongGian.maLenh).isEmpty()
                && !khongGian.txtKetLuan.getText().isBlank();

        for (int dong = 0; dong < khongGian.modelNhap.getRowCount(); dong++) {
            duDieuKien = duDieuKien
                    && Boolean.TRUE.equals(khongGian.modelNhap.getValueAt(dong, 3));
        }

        khongGian.btnHoanTat.setEnabled(duDieuKien);
    }

    // Xác nhận lần cuối, lưu hoàn tất rồi quay lại danh sách công việc.
    void hoanTatLenh() {
        if (khongGian.tblNhap.isEditing()) {
            khongGian.tblNhap.getCellEditor().stopCellEditing();
        }

        boolean[] daDat = new boolean[khongGian.modelNhap.getRowCount()];

        for (int dong = 0; dong < daDat.length; dong++) {
            daDat[dong] = Boolean.TRUE.equals(
                    khongGian.modelNhap.getValueAt(dong, 3));
        }

        if (khongGian.xacNhan(
                "Xác nhận hoàn tất sửa chữa",
                "Đối chiếu kết quả trước khi chuyển thu ngân.",
                new String[][] {
                        {"Lệnh sửa chữa", khongGian.maLenh},
                        {"Kết luận", khongGian.txtKetLuan.getText()}
                },
                "Chỉ báo hoàn thành; chưa thanh toán hoặc bàn giao xe.",
                "Hoàn tất sửa chữa",
                false)) {

            khongGian.dao.hoanTat(
                    khongGian.maLenh, daDat, khongGian.txtKetLuan.getText());

            khongGian.anhChup = khongGian.docNoiDung();
            khongGian.chuyen("PHAN_CONG");
        }
    }
}