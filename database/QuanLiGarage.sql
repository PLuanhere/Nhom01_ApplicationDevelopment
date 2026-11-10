-- Garage Đức Dũng - SQL Server
-- Chạy toàn bộ file này trong SQL Server Management Studio.
-- Cảnh báo: database QuanLiGarage cũ sẽ bị xóa cùng toàn bộ dữ liệu.

USE master;
GO

IF DB_ID(N'QuanLiGarage') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLiGarage SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLiGarage;
END;
GO

CREATE DATABASE QuanLiGarage;
GO

USE QuanLiGarage;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    CREATE SEQUENCE dbo.SoMaThaoTac
        AS BIGINT
        START WITH 1
        INCREMENT BY 1;

    -- 1. Nhân viên, tài khoản
    CREATE TABLE dbo.NhanVien (
        MaNhanVien VARCHAR(20) PRIMARY KEY,
        HoTen NVARCHAR(150) NOT NULL,
        NgaySinh DATE NOT NULL,
        SoDienThoai VARCHAR(20) NOT NULL,
        GioiTinh NVARCHAR(10) NOT NULL,
        DiaChi NVARCHAR(500) NOT NULL,
        SoNamKinhNghiem INT NOT NULL DEFAULT 0,
        BoPhan VARCHAR(30) NOT NULL,
        TrangThai VARCHAR(20) NOT NULL,
        CHECK (LEN(LTRIM(RTRIM(HoTen))) > 0),
        CHECK (GioiTinh IN (N'Nam', N'Nữ', N'Khác')),
        CHECK (SoNamKinhNghiem >= 0),
        CHECK (BoPhan IN ('QUAN_LY', 'TIEP_NHAN', 'KY_THUAT', 'THU_NGAN')),
        CHECK (TrangThai IN ('DANG_LAM', 'DA_NGHI'))
    );

    CREATE TABLE dbo.TaiKhoan (
        MaNhanVien VARCHAR(20) PRIMARY KEY,
        MatKhau NVARCHAR(255) NOT NULL,
        VaiTro VARCHAR(30) NOT NULL,
        HoatDong BIT NOT NULL DEFAULT 1,
        NgayTao DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        PhienBan INT NOT NULL DEFAULT 1,
        FOREIGN KEY (MaNhanVien) REFERENCES dbo.NhanVien(MaNhanVien),
        CHECK (LEN(MatKhau) > 0),
        CHECK (VaiTro IN ('QUAN_LY', 'NHAN_VIEN_TIEP_NHAN', 'NHAN_VIEN_KY_THUAT', 'THU_NGAN'))
    );

    CREATE TABLE dbo.NhatKyTaiKhoan (
        MaNhatKy BIGINT IDENTITY(1,1) PRIMARY KEY,
        ThoiDiem DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        NguoiThucHien VARCHAR(20) NOT NULL,
        MaTaiKhoan VARCHAR(20) NOT NULL,
        HanhDong NVARCHAR(100) NOT NULL,
        LyDo NVARCHAR(1000) NOT NULL,
        FOREIGN KEY (NguoiThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (MaTaiKhoan) REFERENCES dbo.TaiKhoan(MaNhanVien)
    );

    -- 2. Khách hàng, xe
    CREATE TABLE dbo.KhachHang (
        MaKH VARCHAR(30) PRIMARY KEY,
        HoTen NVARCHAR(150) NOT NULL,
        SoDienThoai VARCHAR(20) NOT NULL,
        DiaChi NVARCHAR(500) NOT NULL DEFAULT N'',
        Email NVARCHAR(254) NOT NULL DEFAULT N'',
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        CHECK (LEN(LTRIM(RTRIM(HoTen))) > 0)
    );

    CREATE TABLE dbo.Xe (
        MaXe VARCHAR(30) PRIMARY KEY,
        BienSo VARCHAR(20) NOT NULL UNIQUE,
        MaKH VARCHAR(30) NOT NULL,
        HangXe NVARCHAR(80) NOT NULL,
        Model NVARCHAR(100) NOT NULL,
        NamSX INT NOT NULL,
        MauXe NVARCHAR(50) NOT NULL DEFAULT N'',
        SoKhung VARCHAR(100) NOT NULL DEFAULT '',
        SoMay VARCHAR(100) NOT NULL DEFAULT '',
        FOREIGN KEY (MaKH) REFERENCES dbo.KhachHang(MaKH),
        CHECK (NamSX BETWEEN 1886 AND 9999)
    );

    -- 3. Dịch vụ, phụ tùng
    CREATE TABLE dbo.DichVu (
        MaDichVu VARCHAR(30) PRIMARY KEY,
        TenDichVu NVARCHAR(200) NOT NULL,
        NhomDichVu NVARCHAR(100) NOT NULL DEFAULT N'',
        DonViTinh NVARCHAR(30) NOT NULL,
        DonGia BIGINT NOT NULL,
        MoTa NVARCHAR(2000) NOT NULL DEFAULT N'',
        ThoiGianDuKien NVARCHAR(200) NOT NULL DEFAULT N'',
        DieuKienApDung NVARCHAR(2000) NOT NULL DEFAULT N'',
        DangApDung BIT NOT NULL DEFAULT 1,
        CHECK (DonGia >= 0)
    );

    CREATE TABLE dbo.PhuTung (
        MaPhuTung VARCHAR(30) PRIMARY KEY,
        TenPhuTung NVARCHAR(200) NOT NULL,
        LoaiPhuTung NVARCHAR(100) NOT NULL DEFAULT N'',
        DonViTinh NVARCHAR(10) NOT NULL,
        GiaNhap BIGINT NOT NULL DEFAULT 0,
        GiaBan BIGINT NOT NULL DEFAULT 0,
        MucToiThieu DECIMAL(18,3) NOT NULL DEFAULT 0,
        SoLuongTon DECIMAL(18,3) NOT NULL DEFAULT 0,
        HoatDong BIT NOT NULL DEFAULT 1,
        CHECK (DonViTinh IN (N'cái', N'bộ', N'lít')),
        CHECK (GiaNhap >= 0 AND GiaBan >= 0),
        CHECK (MucToiThieu >= 0 AND SoLuongTon >= 0),
        CHECK (DonViTinh = N'lít' OR (MucToiThieu % 1 = 0 AND SoLuongTon % 1 = 0))
    );

    -- 4. Tiếp nhận, kiểm tra
    CREATE TABLE dbo.PhieuTiepNhan (
        MaPhieu VARCHAR(40) PRIMARY KEY,
        MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
        MaXe VARCHAR(30) NOT NULL,
        MaKH VARCHAR(30) NOT NULL,
        BienSo VARCHAR(20) NOT NULL,
        TenKhach NVARCHAR(150) NOT NULL,
        SoDienThoai VARCHAR(20) NOT NULL,
        TenXe NVARCHAR(200) NOT NULL,
        SoKm BIGINT NOT NULL,
        ThoiGianNhan DATETIME2(3) NOT NULL,
        TinhTrang NVARCHAR(2000) NOT NULL,
        TaiSan NVARCHAR(2000) NOT NULL DEFAULT N'',
        YeuCau NVARCHAR(2000) NOT NULL,
        NguoiTiepNhan VARCHAR(20) NOT NULL,
        DaXacNhan BIT NOT NULL DEFAULT 1,
        ThoiGianXacNhan DATETIME2(3) NOT NULL,
        TaiKhoanKyThuat VARCHAR(20) NULL,
        NguoiPhanCong VARCHAR(20) NULL,
        ThoiDiemPhanCong DATETIME2(3) NULL,
        FOREIGN KEY (MaXe) REFERENCES dbo.Xe(MaXe),
        FOREIGN KEY (MaKH) REFERENCES dbo.KhachHang(MaKH),
        FOREIGN KEY (NguoiTiepNhan) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (TaiKhoanKyThuat) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiPhanCong) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (SoKm >= 0),
        CHECK (DaXacNhan = 1),
        CHECK (
            (TaiKhoanKyThuat IS NULL AND NguoiPhanCong IS NULL AND ThoiDiemPhanCong IS NULL)
            OR
            (TaiKhoanKyThuat IS NOT NULL AND NguoiPhanCong IS NOT NULL AND ThoiDiemPhanCong IS NOT NULL)
        )
    );

    CREATE TABLE dbo.BaoCaoKiemTra (
        MaBaoCao VARCHAR(40) PRIMARY KEY,
        MaPhieuTiepNhan VARCHAR(40) NOT NULL UNIQUE,
        NgayKiemTra DATE NOT NULL,
        BoPhan NVARCHAR(100) NOT NULL,
        NoiDung0 NVARCHAR(2000) NOT NULL DEFAULT N'',
        NoiDung1 NVARCHAR(2000) NOT NULL DEFAULT N'',
        NoiDung2 NVARCHAR(2000) NOT NULL DEFAULT N'',
        NoiDung3 NVARCHAR(2000) NOT NULL DEFAULT N'',
        NoiDung4 NVARCHAR(2000) NOT NULL DEFAULT N'',
        NoiDung5 NVARCHAR(2000) NOT NULL DEFAULT N'',
        NoiDung6 NVARCHAR(2000) NOT NULL DEFAULT N'',
        DeXuat0 NVARCHAR(2000) NOT NULL DEFAULT N'',
        DeXuat1 NVARCHAR(2000) NOT NULL DEFAULT N'',
        DeXuat2 NVARCHAR(2000) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(20) NOT NULL,
        ThoiDiemHoanTat DATETIME2(3) NULL,
        FOREIGN KEY (MaPhieuTiepNhan) REFERENCES dbo.PhieuTiepNhan(MaPhieu),
        CHECK (TrangThai IN ('NHAP', 'HOAN_TAT')),
        CHECK (
            (TrangThai = 'NHAP' AND ThoiDiemHoanTat IS NULL)
            OR
            (TrangThai = 'HOAN_TAT' AND ThoiDiemHoanTat IS NOT NULL)
        )
    );

    -- 5. Báo giá
    CREATE TABLE dbo.NhomBaoGia (
        MaBaoGiaGoc VARCHAR(40) PRIMARY KEY,
        MaBaoCao VARCHAR(40) NOT NULL UNIQUE,
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCaoKiemTra(MaBaoCao)
    );

    CREATE TABLE dbo.BaoGia (
        MaBaoGia VARCHAR(50) PRIMARY KEY,
        MaBaoGiaGoc VARCHAR(40) NOT NULL,
        PhienBan INT NOT NULL,
        BienSo VARCHAR(20) NOT NULL,
        NgayHen DATETIME2(3) NOT NULL,
        NgayLap DATETIME2(3) NOT NULL,
        NguoiLap VARCHAR(20) NOT NULL,
        TrangThai VARCHAR(30) NOT NULL,
        NgayGui DATETIME2(3) NULL,
        NguoiGui VARCHAR(20) NULL,
        ThoiDiemPhanHoi DATETIME2(3) NULL,
        ThoiDiemGhiNhan DATETIME2(3) NULL,
        NguoiGhiNhan VARCHAR(20) NULL,
        KenhPhanHoi NVARCHAR(100) NOT NULL DEFAULT N'',
        GhiChuPhanHoi NVARCHAR(2000) NOT NULL DEFAULT N'',
        MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
        FOREIGN KEY (MaBaoGiaGoc) REFERENCES dbo.NhomBaoGia(MaBaoGiaGoc),
        FOREIGN KEY (NguoiLap) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiGui) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiGhiNhan) REFERENCES dbo.TaiKhoan(MaNhanVien),
        UNIQUE (MaBaoGiaGoc, PhienBan),
        UNIQUE (MaBaoGia, MaBaoGiaGoc),
        CHECK (PhienBan > 0),
        CHECK (TrangThai IN ('NHAP', 'CHO_XAC_NHAN', 'DA_DONG_Y', 'YEU_CAU_DIEU_CHINH', 'TU_CHOI')),
        CHECK (TrangThai = 'NHAP' OR (NgayGui IS NOT NULL AND NguoiGui IS NOT NULL)),
        CHECK (
            TrangThai IN ('NHAP', 'CHO_XAC_NHAN')
            OR (ThoiDiemPhanHoi IS NOT NULL AND ThoiDiemGhiNhan IS NOT NULL AND NguoiGhiNhan IS NOT NULL)
        )
    );

    CREATE TABLE dbo.ChiTietBaoGia (
        MaBaoGia VARCHAR(50) NOT NULL,
        ViTri INT NOT NULL,
        MaDichVu VARCHAR(30) NULL,
        MaPhuTung VARCHAR(30) NULL,
        TenHangMuc NVARCHAR(200) NOT NULL,
        Loai NVARCHAR(20) NOT NULL,
        DonVi NVARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        DonGia BIGINT NOT NULL,
        ThanhTien AS CONVERT(BIGINT, ROUND(CONVERT(DECIMAL(38,3), DonGia) * SoLuong, 0)),
        PRIMARY KEY (MaBaoGia, ViTri),
        FOREIGN KEY (MaBaoGia) REFERENCES dbo.BaoGia(MaBaoGia),
        FOREIGN KEY (MaDichVu) REFERENCES dbo.DichVu(MaDichVu),
        FOREIGN KEY (MaPhuTung) REFERENCES dbo.PhuTung(MaPhuTung),
        CHECK (ViTri >= 0),
        CHECK (Loai IN (N'Dịch vụ', N'Phụ tùng')),
        CHECK (SoLuong > 0 AND DonGia >= 0),
        CHECK (
            (Loai = N'Dịch vụ' AND MaDichVu IS NOT NULL AND MaPhuTung IS NULL)
            OR
            (Loai = N'Phụ tùng' AND MaDichVu IS NULL AND MaPhuTung IS NOT NULL)
        ),
        CHECK (DonVi NOT IN (N'cái', N'bộ') OR SoLuong % 1 = 0)
    );

    -- 6. Sửa chữa
    CREATE TABLE dbo.PhieuSuaChua (
        MaPhieu VARCHAR(40) PRIMARY KEY,
        MaBaoGia VARCHAR(50) NOT NULL,
        MaBaoGiaGoc VARCHAR(40) NOT NULL UNIQUE,
        BienSo VARCHAR(20) NOT NULL,
        KyThuatChinh VARCHAR(20) NOT NULL,
        KyThuatPhoiHop VARCHAR(20) NULL,
        HanHoanThanh DATETIME2(3) NOT NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(30) NOT NULL,
        NguoiPhatHanh VARCHAR(20) NOT NULL,
        ThoiDiemPhatHanh DATETIME2(3) NOT NULL,
        ThoiDiemHoanTat DATETIME2(3) NULL,
        DaThanhToan BIT NOT NULL DEFAULT 0,
        ThoiDiemThanhToan DATETIME2(3) NULL,
        KetQua0 NVARCHAR(2000) NOT NULL DEFAULT N'',
        KetQua1 NVARCHAR(2000) NOT NULL DEFAULT N'',
        KetQua2 NVARCHAR(2000) NOT NULL DEFAULT N'',
        MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
        FOREIGN KEY (MaBaoGia, MaBaoGiaGoc) REFERENCES dbo.BaoGia(MaBaoGia, MaBaoGiaGoc),
        FOREIGN KEY (KyThuatChinh) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (KyThuatPhoiHop) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiPhatHanh) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (TrangThai IN ('CHO_SUA_CHUA', 'DANG_SUA', 'CHO_PHU_TUNG', 'HOAN_THANH')),
        CHECK (KyThuatPhoiHop IS NULL OR KyThuatPhoiHop <> KyThuatChinh),
        CHECK (
            (TrangThai = 'HOAN_THANH' AND ThoiDiemHoanTat IS NOT NULL)
            OR
            (TrangThai <> 'HOAN_THANH' AND ThoiDiemHoanTat IS NULL)
        ),
        CHECK ((DaThanhToan = 1 AND ThoiDiemThanhToan IS NOT NULL)
            OR (DaThanhToan = 0 AND ThoiDiemThanhToan IS NULL))
    );

    CREATE TABLE dbo.PhanCong (
        MaPC VARCHAR(40) PRIMARY KEY,
        MaPhieuSC VARCHAR(40) NOT NULL,
        MaNV VARCHAR(20) NOT NULL,
        NgayPhanCong DATETIME2(3) NOT NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        FOREIGN KEY (MaPhieuSC) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (MaNV) REFERENCES dbo.NhanVien(MaNhanVien)
    );

    CREATE TABLE dbo.ChiTietSuaChua (
        MaPhieu VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenHangMuc NVARCHAR(200) NOT NULL,
        TaiKhoanThucHien VARCHAR(20) NOT NULL,
        KetQua NVARCHAR(2000) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(30) NOT NULL,
        PhatSinh BIT NOT NULL DEFAULT 0,
        PRIMARY KEY (MaPhieu, ViTri),
        FOREIGN KEY (MaPhieu) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (TaiKhoanThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (ViTri >= 0),
        CHECK (TrangThai IN ('CHO_SUA_CHUA', 'DANG_SUA', 'CHO_PHU_TUNG', 'HOAN_THANH'))
    );

    CREATE TABLE dbo.PhatSinhSuaChua (
        MaPhieu VARCHAR(40) PRIMARY KEY,
        HuHong NVARCHAR(2000) NOT NULL,
        NguyenNhan NVARCHAR(2000) NOT NULL,
        DeXuat NVARCHAR(2000) NOT NULL,
        ThoiGianThem NVARCHAR(200) NOT NULL DEFAULT N'',
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(20) NOT NULL,
        NguoiXacNhan NVARCHAR(150) NULL,
        ThoiDiemXacNhan DATETIME2(3) NULL,
        FOREIGN KEY (MaPhieu) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        CHECK (TrangThai IN ('CHO_DUYET', 'DA_DUYET', 'TU_CHOI')),
        CHECK (TrangThai = 'CHO_DUYET' OR (NguoiXacNhan IS NOT NULL AND ThoiDiemXacNhan IS NOT NULL))
    );

    -- 7. Nhập kho, sử dụng phụ tùng
    CREATE TABLE dbo.PhieuNhapKho (
        MaPhieu VARCHAR(40) PRIMARY KEY,
        NgayNhap DATETIME2(3) NOT NULL,
        NguonNhap NVARCHAR(1000) NOT NULL,
        ChungTuNguon NVARCHAR(100) NOT NULL DEFAULT N'',
        NguoiNhap VARCHAR(20) NOT NULL,
        FOREIGN KEY (NguoiNhap) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (LEN(LTRIM(RTRIM(NguonNhap))) > 0)
    );

    CREATE TABLE dbo.ChiTietNhapKho (
        MaPhieu VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        MaPhuTung VARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        DonGia BIGINT NOT NULL,
        PRIMARY KEY (MaPhieu, ViTri),
        FOREIGN KEY (MaPhieu) REFERENCES dbo.PhieuNhapKho(MaPhieu),
        FOREIGN KEY (MaPhuTung) REFERENCES dbo.PhuTung(MaPhuTung),
        CHECK (ViTri >= 0),
        CHECK (SoLuong > 0 AND DonGia >= 0)
    );

    CREATE TABLE dbo.PhuTungSuDung (
        MaGhiNhan VARCHAR(40) PRIMARY KEY,
        MaLenhSuaChua VARCHAR(40) NOT NULL,
        ViTriHangMuc INT NOT NULL,
        MaPhuTung VARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        NguoiThucHien VARCHAR(20) NOT NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(20) NOT NULL,
        MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
        FOREIGN KEY (MaLenhSuaChua, ViTriHangMuc) REFERENCES dbo.ChiTietSuaChua(MaPhieu, ViTri),
        FOREIGN KEY (MaPhuTung) REFERENCES dbo.PhuTung(MaPhuTung),
        FOREIGN KEY (NguoiThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (SoLuong > 0),
        CHECK (TrangThai IN ('NHAP', 'DA_GHI_NHAN'))
    );

    CREATE TABLE dbo.BienDongKho (
        MaBienDong BIGINT IDENTITY(1,1) PRIMARY KEY,
        MaPhuTung VARCHAR(30) NOT NULL,
        ThoiDiem DATETIME2(3) NOT NULL,
        Loai NVARCHAR(30) NOT NULL,
        TonTruoc DECIMAL(18,3) NOT NULL,
        TonSau DECIMAL(18,3) NOT NULL,
        ChungTu NVARCHAR(100) NOT NULL,
        NguoiThucHien VARCHAR(20) NOT NULL,
        LyDo NVARCHAR(2000) NOT NULL,
        FOREIGN KEY (MaPhuTung) REFERENCES dbo.PhuTung(MaPhuTung),
        FOREIGN KEY (NguoiThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (TonTruoc >= 0 AND TonSau >= 0)
    );

    -- 8. Hóa đơn, bàn giao
    CREATE TABLE dbo.HoaDon (
        MaHoaDon VARCHAR(40) PRIMARY KEY,
        MaPhieuSuaChua VARCHAR(40) NOT NULL UNIQUE,
        TenKhachHang NVARCHAR(150) NOT NULL,
        BienSo VARCHAR(20) NOT NULL,
        TongTien BIGINT NOT NULL,
        HinhThucThanhToan VARCHAR(20) NOT NULL,
        ThoiDiemThanhToan DATETIME2(3) NOT NULL,
        NguoiThucHien VARCHAR(20) NOT NULL,
        MaGiaoDich NVARCHAR(100) NOT NULL DEFAULT N'',
        FOREIGN KEY (MaPhieuSuaChua) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        UNIQUE (MaHoaDon, MaPhieuSuaChua),
        FOREIGN KEY (NguoiThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (TongTien >= 0),
        CHECK (HinhThucThanhToan IN ('TIEN_MAT', 'CHUYEN_KHOAN')),
        CHECK (HinhThucThanhToan <> 'CHUYEN_KHOAN' OR LEN(LTRIM(RTRIM(MaGiaoDich))) > 0)
    );

    CREATE TABLE dbo.ChiTietHoaDon (
        MaHoaDon VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenHangMuc NVARCHAR(300) NOT NULL,
        Loai NVARCHAR(50) NOT NULL,
        DonVi NVARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        DonGia BIGINT NOT NULL,
        ThanhTien BIGINT NOT NULL,
        PRIMARY KEY (MaHoaDon, ViTri),
        FOREIGN KEY (MaHoaDon) REFERENCES dbo.HoaDon(MaHoaDon),
        CHECK (ViTri >= 0),
        CHECK (SoLuong > 0),
        CHECK (DonGia >= 0 AND ThanhTien >= 0)
    );

    CREATE TABLE dbo.BanGiaoXe (
        MaBanGiao VARCHAR(40) PRIMARY KEY,
        MaLenh VARCHAR(40) NOT NULL UNIQUE,
        MaHoaDon VARCHAR(40) NOT NULL,
        NguoiGiao VARCHAR(20) NOT NULL,
        NguoiNhan NVARCHAR(150) NOT NULL,
        ThoiDiemGiao DATETIME2(3) NOT NULL,
        ThoiDiemGhiNhan DATETIME2(3) NOT NULL,
        TinhTrang NVARCHAR(2000) NOT NULL,
        TaiSanDaTra NVARCHAR(2000) NOT NULL DEFAULT N'',
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        HinhThucXacNhan NVARCHAR(200) NOT NULL,
        MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
        FOREIGN KEY (MaHoaDon, MaLenh) REFERENCES dbo.HoaDon(MaHoaDon, MaPhieuSuaChua),
        FOREIGN KEY (NguoiGiao) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (ThoiDiemGiao <= ThoiDiemGhiNhan)
    );

    -- 9. Báo cáo đã lưu
    CREATE TABLE dbo.BaoCao (
        MaBaoCao VARCHAR(40) PRIMARY KEY,
        LoaiBaoCao NVARCHAR(100) NOT NULL,
        TuNgay DATE NOT NULL,
        DenNgay DATE NOT NULL,
        NoiDung NVARCHAR(MAX) NOT NULL DEFAULT N'',
        NgayLap DATETIME2(3) NOT NULL,
        NguoiLap VARCHAR(20) NOT NULL,
        FOREIGN KEY (NguoiLap) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (TuNgay <= DenNgay)
    );

    CREATE TABLE dbo.ChiSoBaoCao (
        MaBaoCao VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenChiSo NVARCHAR(200) NOT NULL,
        GiaTri BIGINT NOT NULL,
        PRIMARY KEY (MaBaoCao, ViTri),
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCao(MaBaoCao),
        CHECK (ViTri >= 0)
    );

    CREATE TABLE dbo.CotBaoCao (
        MaBaoCao VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenCot NVARCHAR(200) NOT NULL,
        PRIMARY KEY (MaBaoCao, ViTri),
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCao(MaBaoCao),
        CHECK (ViTri >= 0)
    );

    CREATE TABLE dbo.OBaoCao (
        MaBaoCao VARCHAR(40) NOT NULL,
        Dong INT NOT NULL,
        Cot INT NOT NULL,
        GiaTri NVARCHAR(MAX) NOT NULL,
        PRIMARY KEY (MaBaoCao, Dong, Cot),
        FOREIGN KEY (MaBaoCao, Cot) REFERENCES dbo.CotBaoCao(MaBaoCao, ViTri),
        CHECK (Dong >= 0 AND Cot >= 0)
    );

    -- Chỉ mục tìm kiếm
    CREATE INDEX IX_KhachHang_SoDienThoai ON dbo.KhachHang(SoDienThoai);
    CREATE INDEX IX_Xe_MaKH ON dbo.Xe(MaKH);
    CREATE INDEX IX_PhieuTiepNhan_MaXe ON dbo.PhieuTiepNhan(MaXe, ThoiGianNhan);
    CREATE INDEX IX_PhieuSuaChua_KyThuatChinh ON dbo.PhieuSuaChua(KyThuatChinh, TrangThai);
    CREATE INDEX IX_PhieuSuaChua_KyThuatPhoiHop ON dbo.PhieuSuaChua(KyThuatPhoiHop, TrangThai);
    CREATE INDEX IX_BienDongKho_MaPhuTung ON dbo.BienDongKho(MaPhuTung, ThoiDiem);
    CREATE INDEX IX_HoaDon_ThoiDiem ON dbo.HoaDon(ThoiDiemThanhToan);

    COMMIT TRANSACTION;
    PRINT N'Đã tạo database QuanLiGarage và 27 bảng.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT COUNT(*) AS SoBang
FROM sys.tables
WHERE is_ms_shipped = 0;
GO

-- PHẦN 2: TẠO VIEW TRA CỨU
USE QuanLiGarage;
GO

-- Các lệnh đã hoàn thành nhưng chưa thu tiền.
CREATE OR ALTER VIEW dbo.vw_PhieuChoThu
AS
SELECT
    sc.MaPhieu AS MaLenh,
    tn.TenKhach AS TenKhachHang,
    sc.BienSo,
    SUM(ct.ThanhTien) AS SoTienPhaiThu
FROM dbo.PhieuSuaChua sc
JOIN dbo.BaoGia bg ON bg.MaBaoGia = sc.MaBaoGia
JOIN dbo.NhomBaoGia nbg ON nbg.MaBaoGiaGoc = bg.MaBaoGiaGoc
JOIN dbo.BaoCaoKiemTra kt ON kt.MaBaoCao = nbg.MaBaoCao
JOIN dbo.PhieuTiepNhan tn ON tn.MaPhieu = kt.MaPhieuTiepNhan
JOIN dbo.ChiTietBaoGia ct ON ct.MaBaoGia = bg.MaBaoGia
WHERE sc.TrangThai = 'HOAN_THANH'
  AND sc.DaThanhToan = 0
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.HoaDon hd
      WHERE hd.MaPhieuSuaChua = sc.MaPhieu
  )
GROUP BY sc.MaPhieu, tn.TenKhach, sc.BienSo;
GO

-- Căn cứ thanh toán dùng trước khi bàn giao xe.
CREATE OR ALTER VIEW dbo.vw_CanCuBanGiao
AS
SELECT
    hd.MaPhieuSuaChua AS MaLenh,
    hd.MaHoaDon AS MaChungTu,
    hd.TongTien AS PhaiThu,
    hd.TongTien AS DaThu,
    hd.NguoiThucHien AS NguoiXacNhan,
    hd.ThoiDiemThanhToan AS ThoiDiem,
    CONVERT(BIT, 0) AS NgoaiLe,
    CONVERT(NVARCHAR(500), N'') AS LyDo
FROM dbo.HoaDon hd
JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu = hd.MaPhieuSuaChua
WHERE sc.DaThanhToan = 1;
GO

-- Tồn kho và trạng thái cảnh báo.
CREATE OR ALTER VIEW dbo.vw_TonKho
AS
SELECT
    MaPhuTung,
    TenPhuTung,
    DonViTinh,
    SoLuongTon,
    MucToiThieu,
    CASE WHEN SoLuongTon <= MucToiThieu THEN 1 ELSE 0 END AS CanhBao
FROM dbo.PhuTung;
GO

PRINT N'Đã tạo 3 view tra cứu.';
GO

-- PHẦN 3: TẠO DỮ LIỆU MẪU
USE QuanLiGarage;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @NgayBatDau DATE = DATEADD(DAY, -1, CONVERT(DATE, SYSDATETIME()));
    DECLARE @BatDau DATETIME2(3) = DATEADD(HOUR, 8, CONVERT(DATETIME2(3), @NgayBatDau));
    DECLARE @LucNhapKho DATETIME2(3) = DATEADD(HOUR, -2, @BatDau);
    DECLARE @LucTiepNhan DATETIME2(3) = @BatDau;

    -- 1. Nhân viên
    INSERT dbo.NhanVien
        (MaNhanVien, HoTen, NgaySinh, SoDienThoai, GioiTinh, DiaChi,
         SoNamKinhNghiem, BoPhan, TrangThai)
    VALUES
        ('NV001', N'Trần Minh Hùng', '1985-03-15', '0909345211', N'Nam',
         N'Gò Vấp, TP. Hồ Chí Minh', 12, 'QUAN_LY', 'DANG_LAM'),
        ('NV003', N'Ngô Minh Hoàng', '1994-07-20', '0901234567', N'Nam',
         N'Bình Thạnh, TP. Hồ Chí Minh', 5, 'TIEP_NHAN', 'DANG_LAM'),
        ('NV005', N'Lê Minh Tâm', '1996-11-08', '0912888456', N'Nam',
         N'Tân Bình, TP. Hồ Chí Minh', 4, 'KY_THUAT', 'DANG_LAM'),
        ('NV007', N'Nguyễn Minh Vũ', '1998-01-10', '0908678111', N'Nam',
         N'Phú Nhuận, TP. Hồ Chí Minh', 3, 'THU_NGAN', 'DANG_LAM');

    -- 2. Tài khoản. Mật khẩu chung dùng để thử: 123
    INSERT dbo.TaiKhoan
        (MaNhanVien, MatKhau, VaiTro, HoatDong, NgayTao)
    VALUES
        ('NV001', N'123', 'QUAN_LY', 1, SYSDATETIME()),
        ('NV003', N'123', 'NHAN_VIEN_TIEP_NHAN', 1, SYSDATETIME()),
        ('NV005', N'123', 'NHAN_VIEN_KY_THUAT', 1, SYSDATETIME()),
        ('NV007', N'123', 'THU_NGAN', 1, SYSDATETIME());

    -- 3. Nhật ký tài khoản
    INSERT dbo.NhatKyTaiKhoan
        (ThoiDiem, NguoiThucHien, MaTaiKhoan, HanhDong, LyDo)
    VALUES
        (SYSDATETIME(), 'NV001', 'NV001', N'Khởi tạo dữ liệu mẫu', N'Tạo bốn tài khoản ban đầu.');

    -- 4. Khách hàng
    INSERT dbo.KhachHang
        (MaKH, HoTen, SoDienThoai, DiaChi, Email, GhiChu)
    VALUES
        ('KH001', N'Nguyễn Văn An', '0901000001', N'Quận 1, TP. Hồ Chí Minh', N'an@example.com', N'Khách hàng mẫu'),
        ('KH002', N'Trần Thị Bình', '0901000002', N'Quận 3, TP. Hồ Chí Minh', N'binh@example.com', N'Khách hàng mẫu');

    -- 5. Xe
    INSERT dbo.Xe
        (MaXe, BienSo, MaKH, HangXe, Model, NamSX, MauXe, SoKhung, SoMay)
    VALUES
        ('XE001', '51A-12345', 'KH001', N'Toyota', N'Vios', 2021, N'Trắng', 'RL4A001', 'MAY001'),
        ('XE002', '51B-67890', 'KH002', N'Honda', N'City', 2022, N'Đen', 'RL4A002', 'MAY002');

    -- 6. Dịch vụ
    INSERT dbo.DichVu
        (MaDichVu, TenDichVu, NhomDichVu, DonViTinh, DonGia, MoTa,
         ThoiGianDuKien, DieuKienApDung, DangApDung)
    VALUES
        ('DV001', N'Thay dầu động cơ', N'Bảo dưỡng', N'Công', 200000,
         N'Thay dầu và kiểm tra rò rỉ.', N'45 phút', N'Xe du lịch', 1),
        ('DV002', N'Vệ sinh phanh', N'Phanh', N'Công', 150000,
         N'Vệ sinh và kiểm tra hệ thống phanh.', N'60 phút', N'Xe du lịch', 1),
        ('DV003', N'Kiểm tra điều hòa', N'Điện lạnh', N'Công', 180000,
         N'Kiểm tra khả năng làm lạnh.', N'60 phút', N'Xe du lịch', 1);

    -- 7. Phụ tùng. Số tồn bên dưới khớp với lịch sử nhập và xuất mẫu.
    INSERT dbo.PhuTung
        (MaPhuTung, TenPhuTung, LoaiPhuTung, DonViTinh, GiaNhap,
         GiaBan, MucToiThieu, SoLuongTon, HoatDong)
    VALUES
        ('PT001', N'Dầu động cơ 5W-30', N'Dầu nhớt', N'lít', 100000, 150000, 10, 16, 1),
        ('PT002', N'Lọc dầu động cơ', N'Lọc dầu', N'cái', 70000, 120000, 3, 9, 1),
        ('PT003', N'Bộ má phanh trước', N'Phanh', N'bộ', 550000, 800000, 2, 5, 1);

    -- 8. Phiếu tiếp nhận
    INSERT dbo.PhieuTiepNhan
        (MaPhieu, MaThaoTac, MaXe, MaKH, BienSo, TenKhach, SoDienThoai,
         TenXe, SoKm, ThoiGianNhan, TinhTrang, TaiSan, YeuCau,
         NguoiTiepNhan, DaXacNhan, ThoiGianXacNhan,
         TaiKhoanKyThuat, NguoiPhanCong, ThoiDiemPhanCong)
    VALUES
        ('TN-2026-0001', 'MAU-TN-2026-0001', 'XE001', 'KH001', '51A-12345', N'Nguyễn Văn An', '0901000001',
         N'Toyota Vios', 42000, @LucTiepNhan, N'Đến kỳ thay dầu', N'01 chìa khóa',
         N'Thay dầu và lọc dầu', 'NV003', 1, DATEADD(MINUTE, 5, @LucTiepNhan),
         'NV005', 'NV003', DATEADD(MINUTE, 10, @LucTiepNhan)),
        ('TN-2026-0002', 'MAU-TN-2026-0002', 'XE002', 'KH002', '51B-67890', N'Trần Thị Bình', '0901000002',
         N'Honda City', 28000, DATEADD(HOUR, 1, @LucTiepNhan), N'Điều hòa làm lạnh yếu', N'01 chìa khóa',
         N'Kiểm tra điều hòa', 'NV003', 1, DATEADD(MINUTE, 65, @LucTiepNhan),
         'NV005', 'NV003', DATEADD(MINUTE, 70, @LucTiepNhan));

    -- 9. Báo cáo kiểm tra: một phiếu hoàn tất và một phiếu đang nhập.
    INSERT dbo.BaoCaoKiemTra
        (MaBaoCao, MaPhieuTiepNhan, NgayKiemTra, BoPhan,
         NoiDung0, NoiDung1, NoiDung2, NoiDung3, NoiDung4, NoiDung5, NoiDung6,
         DeXuat0, DeXuat1, DeXuat2, TrangThai, ThoiDiemHoanTat)
    VALUES
        ('KT-2026-0001', 'TN-2026-0001', @NgayBatDau, N'Tổng hợp',
         N'Dầu động cơ đã cũ', N'Lọc dầu cần thay', N'Phanh hoạt động bình thường',
         N'Lốp đủ áp suất', N'Điện hoạt động bình thường', N'Không có mã lỗi', N'Xe đủ điều kiện sửa chữa',
         N'Thay dầu động cơ', N'Thay lọc dầu', N'Kiểm tra lại sau sửa chữa',
         'HOAN_TAT', DATEADD(MINUTE, 40, @LucTiepNhan)),
        ('KT-2026-0002', 'TN-2026-0002', @NgayBatDau, N'Điều hòa',
         N'Đang kiểm tra điều hòa', N'', N'', N'', N'', N'', N'',
         N'', N'', N'', 'NHAP', NULL);

    -- 10. Nhóm báo giá
    INSERT dbo.NhomBaoGia (MaBaoGiaGoc, MaBaoCao)
    VALUES ('BG-2026-0001', 'KT-2026-0001');

    -- 11. Báo giá đã được khách đồng ý
    INSERT dbo.BaoGia
        (MaBaoGia, MaBaoGiaGoc, PhienBan, BienSo, NgayHen, NgayLap,
         NguoiLap, TrangThai, NgayGui, NguoiGui,
         ThoiDiemPhanHoi, ThoiDiemGhiNhan, NguoiGhiNhan,
         KenhPhanHoi, GhiChuPhanHoi, MaThaoTac)
    VALUES
        ('BG-2026-0001', 'BG-2026-0001', 1, '51A-12345', DATEADD(DAY, 1, @LucTiepNhan),
         DATEADD(MINUTE, 45, @LucTiepNhan), 'NV003', 'DA_DONG_Y',
         DATEADD(MINUTE, 50, @LucTiepNhan), 'NV003',
         DATEADD(MINUTE, 60, @LucTiepNhan), DATEADD(MINUTE, 61, @LucTiepNhan), 'NV003',
         N'Trực tiếp', N'Khách đồng ý toàn bộ báo giá.', 'MAU-BG-2026-0001');

    -- 12. Chi tiết báo giá. Tổng cộng: 920.000 đồng.
    INSERT dbo.ChiTietBaoGia
        (MaBaoGia, ViTri, MaDichVu, MaPhuTung, TenHangMuc, Loai, DonVi, SoLuong, DonGia)
    VALUES
        ('BG-2026-0001', 0, 'DV001', NULL, N'Thay dầu động cơ', N'Dịch vụ', N'Công', 1, 200000),
        ('BG-2026-0001', 1, NULL, 'PT001', N'Dầu động cơ 5W-30', N'Phụ tùng', N'lít', 4, 150000),
        ('BG-2026-0001', 2, NULL, 'PT002', N'Lọc dầu động cơ', N'Phụ tùng', N'cái', 1, 120000);

    -- 13. Phiếu sửa chữa
    INSERT dbo.PhieuSuaChua
        (MaPhieu, MaBaoGia, MaBaoGiaGoc, BienSo, KyThuatChinh, KyThuatPhoiHop,
         HanHoanThanh, GhiChu, TrangThai, NguoiPhatHanh,
         ThoiDiemPhatHanh, ThoiDiemHoanTat, KetQua0, KetQua1, KetQua2, MaThaoTac)
    VALUES
        ('SC-2026-0001', 'BG-2026-0001', 'BG-2026-0001', '51A-12345', 'NV005', NULL,
         DATEADD(DAY, 1, @LucTiepNhan), N'Thay đúng loại dầu trong báo giá.', 'HOAN_THANH', 'NV003',
         DATEADD(MINUTE, 70, @LucTiepNhan), DATEADD(MINUTE, 180, @LucTiepNhan),
         N'Xe vận hành bình thường', N'Đã đối chiếu đủ phụ tùng', N'Đủ điều kiện thu tiền', 'MAU-SC-2026-0001');

    -- 14. Phân công kỹ thuật viên
    INSERT dbo.PhanCong
        (MaPC, MaPhieuSC, MaNV, NgayPhanCong, GhiChu)
    VALUES
        ('PC-2026-0001', 'SC-2026-0001', 'NV005', DATEADD(MINUTE, 70, @LucTiepNhan),
         N'Thực hiện các hạng mục theo báo giá đã duyệt.');

    -- 15. Chi tiết sửa chữa
    INSERT dbo.ChiTietSuaChua
        (MaPhieu, ViTri, TenHangMuc, TaiKhoanThucHien, KetQua, TrangThai, PhatSinh)
    VALUES
        ('SC-2026-0001', 0, N'Thay dầu động cơ', 'NV005', N'Đã thực hiện', 'HOAN_THANH', 0),
        ('SC-2026-0001', 1, N'Dầu động cơ 5W-30', 'NV005', N'Đã sử dụng 4 lít', 'HOAN_THANH', 0),
        ('SC-2026-0001', 2, N'Lọc dầu động cơ', 'NV005', N'Đã thay 1 cái', 'HOAN_THANH', 0);

    -- 16. Phát sinh sửa chữa đã bị khách từ chối nên không làm tăng chi phí.
    INSERT dbo.PhatSinhSuaChua
        (MaPhieu, HuHong, NguyenNhan, DeXuat, ThoiGianThem, GhiChu,
         TrangThai, NguoiXacNhan, ThoiDiemXacNhan)
    VALUES
        ('SC-2026-0001', N'Lốp dự phòng hơi non', N'Để lâu không sử dụng', N'Bơm và kiểm tra lốp dự phòng',
         N'15 phút', N'Khách chưa cần thực hiện.', 'TU_CHOI', N'Nguyễn Văn An',
         DATEADD(MINUTE, 90, @LucTiepNhan));

    -- 16. Phiếu nhập kho
    INSERT dbo.PhieuNhapKho
        (MaPhieu, NgayNhap, NguonNhap, ChungTuNguon, NguoiNhap)
    VALUES
        ('NK-2026-0001', @LucNhapKho, N'Nhà cung cấp An Phát', N'CT-NK-2026-0001', 'NV001');

    -- 17. Chi tiết nhập kho
    INSERT dbo.ChiTietNhapKho
        (MaPhieu, ViTri, MaPhuTung, SoLuong, DonGia)
    VALUES
        ('NK-2026-0001', 0, 'PT001', 20, 100000),
        ('NK-2026-0001', 1, 'PT002', 10, 70000),
        ('NK-2026-0001', 2, 'PT003', 5, 550000);

    -- 18. Phụ tùng thực dùng
    INSERT dbo.PhuTungSuDung
        (MaGhiNhan, MaLenhSuaChua, ViTriHangMuc, MaPhuTung,
         SoLuong, NguoiThucHien, GhiChu, TrangThai, MaThaoTac)
    VALUES
        ('SD-2026-0001', 'SC-2026-0001', 1, 'PT001', 4, 'NV005', N'Dùng theo báo giá', 'DA_GHI_NHAN', 'MAU-SD-2026-0001'),
        ('SD-2026-0002', 'SC-2026-0001', 2, 'PT002', 1, 'NV005', N'Dùng theo báo giá', 'DA_GHI_NHAN', 'MAU-SD-2026-0002');

    -- 19. Biến động kho
    INSERT dbo.BienDongKho
        (MaPhuTung, ThoiDiem, Loai, TonTruoc, TonSau, ChungTu, NguoiThucHien, LyDo)
    VALUES
        ('PT001', @LucNhapKho, N'Nhập', 0, 20, N'NK-2026-0001', 'NV001', N'Nhập dầu động cơ'),
        ('PT002', @LucNhapKho, N'Nhập', 0, 10, N'NK-2026-0001', 'NV001', N'Nhập lọc dầu'),
        ('PT003', @LucNhapKho, N'Nhập', 0, 5, N'NK-2026-0001', 'NV001', N'Nhập má phanh'),
        ('PT001', DATEADD(MINUTE, 160, @LucTiepNhan), N'Xuất', 20, 16, N'SC-2026-0001', 'NV005', N'Dùng cho xe 51A-12345'),
        ('PT002', DATEADD(MINUTE, 161, @LucTiepNhan), N'Xuất', 10, 9, N'SC-2026-0001', 'NV005', N'Dùng cho xe 51A-12345');

    -- 20. Hóa đơn
    INSERT dbo.HoaDon
        (MaHoaDon, MaPhieuSuaChua, TenKhachHang, BienSo, TongTien,
         HinhThucThanhToan, ThoiDiemThanhToan, NguoiThucHien, MaGiaoDich)
    VALUES
        ('HD-2026-0001', 'SC-2026-0001', N'Nguyễn Văn An', '51A-12345', 920000,
         'TIEN_MAT', DATEADD(MINUTE, 190, @LucTiepNhan), 'NV007', N'');

    -- 21. Chi tiết hóa đơn được chụp từ báo giá tại lúc thu tiền.
    INSERT dbo.ChiTietHoaDon
        (MaHoaDon, ViTri, TenHangMuc, Loai, DonVi, SoLuong, DonGia, ThanhTien)
    SELECT
        'HD-2026-0001', ViTri, TenHangMuc, Loai, DonVi, SoLuong, DonGia, ThanhTien
    FROM dbo.ChiTietBaoGia
    WHERE MaBaoGia = 'BG-2026-0001';

    -- 22. Bàn giao xe do Tiếp nhận thực hiện sau khi đã thanh toán.
    INSERT dbo.BanGiaoXe
        (MaBanGiao, MaLenh, MaHoaDon, NguoiGiao, NguoiNhan,
         ThoiDiemGiao, ThoiDiemGhiNhan, TinhTrang, TaiSanDaTra,
         GhiChu, HinhThucXacNhan, MaThaoTac)
    VALUES
        ('GX-2026-0001', 'SC-2026-0001', 'HD-2026-0001', 'NV003', N'Nguyễn Văn An',
         DATEADD(MINUTE, 200, @LucTiepNhan), DATEADD(MINUTE, 201, @LucTiepNhan),
         N'Xe vận hành bình thường', N'Đã trả 01 chìa khóa',
         N'Khách đã kiểm tra và nhận xe.', N'Ký phiếu bàn giao', 'MAU-GX-2026-0001');

    -- 23. Báo cáo đã lưu
    INSERT dbo.BaoCao
        (MaBaoCao, LoaiBaoCao, TuNgay, DenNgay, NoiDung, NgayLap, NguoiLap)
    VALUES
        ('BC-2026-0001', N'Doanh thu', @NgayBatDau, @NgayBatDau, N'Tổng doanh thu trong ngày: 920.000 đồng.',
         DATEADD(MINUTE, 210, @LucTiepNhan), 'NV001');

    -- 24. Chỉ số báo cáo
    INSERT dbo.ChiSoBaoCao
        (MaBaoCao, ViTri, TenChiSo, GiaTri)
    VALUES
        ('BC-2026-0001', 0, N'Doanh thu đã thu', 920000),
        ('BC-2026-0001', 1, N'Số hóa đơn', 1),
        ('BC-2026-0001', 2, N'Tiền mặt', 920000),
        ('BC-2026-0001', 3, N'Chuyển khoản', 0);

    -- 25. Cột báo cáo
    INSERT dbo.CotBaoCao
        (MaBaoCao, ViTri, TenCot)
    VALUES
        ('BC-2026-0001', 0, N'Mã hóa đơn'),
        ('BC-2026-0001', 1, N'Ngày thanh toán'),
        ('BC-2026-0001', 2, N'Hình thức'),
        ('BC-2026-0001', 3, N'Số tiền');

    -- 26. Ô dữ liệu báo cáo
    INSERT dbo.OBaoCao
        (MaBaoCao, Dong, Cot, GiaTri)
    VALUES
        ('BC-2026-0001', 0, 0, N'HD-2026-0001'),
        ('BC-2026-0001', 0, 1, CONVERT(NVARCHAR(19), DATEADD(MINUTE, 190, @LucTiepNhan), 120)),
        ('BC-2026-0001', 0, 2, N'Tiền mặt'),
        ('BC-2026-0001', 0, 3, N'920000');

    COMMIT TRANSACTION;
    PRINT N'Đã thêm dữ liệu mẫu cho đủ 27 bảng.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT MaNhanVien, VaiTro, HoatDong
FROM dbo.TaiKhoan
ORDER BY MaNhanVien;

SELECT MaHoaDon, MaPhieuSuaChua, TongTien, HinhThucThanhToan
FROM dbo.HoaDon;

SELECT MaPhuTung, TenPhuTung, SoLuongTon, MucToiThieu
FROM dbo.PhuTung
ORDER BY MaPhuTung;
GO







-- ================================================================
-- BỔ SUNG DỮ LIỆU THU NGÂN - 3 PHIẾU CHỜ THU
-- ================================================================

USE QuanLiGarage;
GO

-- Thêm nhân viên
INSERT dbo.NhanVien
    (MaNhanVien, HoTen, NgaySinh, SoDienThoai, GioiTinh, DiaChi, SoNamKinhNghiem, BoPhan, TrangThai)
VALUES
    ('NV008', N'Lê Thị Thu Hà', '1997-05-12', '0908765432', N'Nữ', N'Q10, TP.HCM', 2, 'THU_NGAN', 'DANG_LAM'),
    ('NV009', N'Phạm Văn Tùng', '1999-09-25', '0912345678', N'Nam', N'Q5, TP.HCM', 1, 'TIEP_NHAN', 'DANG_LAM'),
    ('NV010', N'Trần Văn Khoa', '1995-03-18', '0909876543', N'Nam', N'Bình Tân, TP.HCM', 5, 'KY_THUAT', 'DANG_LAM');

INSERT dbo.TaiKhoan (MaNhanVien, MatKhau, VaiTro, HoatDong, NgayTao)
VALUES
    ('NV008', N'123', 'THU_NGAN', 1, SYSDATETIME()),
    ('NV009', N'123', 'NHAN_VIEN_TIEP_NHAN', 1, SYSDATETIME()),
    ('NV010', N'123', 'NHAN_VIEN_KY_THUAT', 1, SYSDATETIME());

-- Thêm khách hàng
INSERT dbo.KhachHang VALUES('KH003', N'Lê Văn Cường', '0903111222', N'Q7, TP.HCM', N'cuong.le@email.com', N'Khách quen');
INSERT dbo.KhachHang VALUES('KH004', N'Phạm Thị Dung', '0904222333', N'Q2, TP.HCM', N'dung.pham@email.com', N'');
INSERT dbo.KhachHang VALUES('KH005', N'Ngô Minh Tuấn', '0905333444', N'Gò Vấp', N'tuan.ngo@email.com', N'VIP');
INSERT dbo.KhachHang VALUES('KH006', N'Hoàng Thị Mai', '0906444555', N'Tân Phú', N'mai@email.com', N'');
INSERT dbo.KhachHang VALUES('KH007', N'Võ Văn Nam', '0907555666', N'Q12', N'nam@email.com', N'');

-- Thêm xe
INSERT dbo.Xe VALUES('XE003', '51C-11111', 'KH003', N'Mazda', N'CX-5', 2020, N'Đỏ', 'MZ001', 'MAY003');
INSERT dbo.Xe VALUES('XE004', '51D-22222', 'KH004', N'Hyundai', N'Accent', 2021, N'Bạc', 'HY001', 'MAY004');
INSERT dbo.Xe VALUES('XE005', '51E-33333', 'KH005', N'KIA', N'Seltos', 2022, N'Trắng', 'KI001', 'MAY005');
INSERT dbo.Xe VALUES('XE006', '51F-44444', 'KH006', N'Honda', N'CR-V', 2019, N'Đen', 'HD001', 'MAY006');
INSERT dbo.Xe VALUES('XE007', '51G-55555', 'KH007', N'Toyota', N'Camry', 2023, N'Xanh', 'TY002', 'MAY007');

-- Thêm dịch vụ
INSERT dbo.DichVu VALUES('DV004', N'Thay má phanh', N'Phanh', N'Công', 300000, N'Thay má phanh', N'90p', N'Xe du lịch', 1);
INSERT dbo.DichVu VALUES('DV005', N'Thay lốp', N'Lốp', N'Cái', 50000, N'Thay lốp', N'30p', N'Tất cả', 1);
INSERT dbo.DichVu VALUES('DV006', N'Bảo dưỡng', N'BD', N'Gói', 500000, N'BD định kỳ', N'120p', N'Xe du lịch', 1);
INSERT dbo.DichVu VALUES('DV007', N'Thay ắc quy', N'Điện', N'Công', 100000, N'Thay ắc quy', N'20p', N'Tất cả', 1);

-- Thêm phụ tùng
INSERT dbo.PhuTung
    (MaPhuTung, TenPhuTung, LoaiPhuTung, DonViTinh, GiaNhap, GiaBan, MucToiThieu, SoLuongTon, HoatDong)
VALUES
    ('PT004', N'Lốp Michelin', N'Lốp', N'cái', 1200000, 1500000, 4, 12, 1),
    ('PT005', N'Ắc quy 12V 60Ah', N'Điện', N'cái', 1000000, 1300000, 2, 8, 1),
    ('PT006', N'Gạt mưa', N'Phụ kiện', N'bộ', 150000, 250000, 4, 10, 1),
    ('PT007', N'Đèn LED H4', N'Đèn', N'cái', 200000, 350000, 6, 15, 1),
    ('PT008', N'Má phanh Toyota', N'Phanh', N'bộ', 600000, 900000, 2, 6, 1),
    ('PT009', N'Dầu phanh DOT4', N'Dầu', N'lít', 80000, 120000, 5, 20, 1),
    ('PT010', N'Lọc gió', N'Lọc', N'cái', 100000, 180000, 5, 12, 1);

PRINT N'Đã thêm nhân viên, khách hàng, xe, dịch vụ, phụ tùng';
GO

-- 3 PHIẾU HOÀN THÀNH CHƯA CÓ HÓA ĐƠN: HIỂN THỊ Ở TRUNG TÂM THANH TOÁN
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRY
BEGIN TRANSACTION;
DECLARE @D DATE=CONVERT(DATE,SYSDATETIME());
DECLARE @T1 DATETIME2(3)=DATEADD(HOUR,8,CONVERT(DATETIME2(3),@D));
DECLARE @T2 DATETIME2(3)=DATEADD(HOUR,10,CONVERT(DATETIME2(3),@D));
DECLARE @T3 DATETIME2(3)=DATEADD(HOUR,13,CONVERT(DATETIME2(3),@D));
INSERT dbo.PhieuTiepNhan(MaPhieu,MaThaoTac,MaXe,MaKH,BienSo,TenKhach,SoDienThoai,TenXe,SoKm,ThoiGianNhan,TinhTrang,TaiSan,YeuCau,NguoiTiepNhan,DaXacNhan,ThoiGianXacNhan,TaiKhoanKyThuat,NguoiPhanCong,ThoiDiemPhanCong) VALUES
('TN-2026-0003','MAU-TN-2026-0003','XE003','KH003','51C-11111',N'Lê Văn Cường','0903111222',N'Mazda CX-5',35000,@T1,N'Phanh kêu',N'01 chìa khóa',N'Thay má phanh','NV009',1,DATEADD(MINUTE,5,@T1),'NV010','NV009',DATEADD(MINUTE,10,@T1)),
('TN-2026-0004','MAU-TN-2026-0004','XE004','KH004','51D-22222',N'Phạm Thị Dung','0904222333',N'Hyundai Accent',48000,@T2,N'Lốp trước mòn',N'01 chìa khóa',N'Thay hai lốp trước','NV009',1,DATEADD(MINUTE,5,@T2),'NV010','NV009',DATEADD(MINUTE,10,@T2)),
('TN-2026-0005','MAU-TN-2026-0005','XE005','KH005','51E-33333',N'Ngô Minh Tuấn','0905333444',N'KIA Seltos',15000,@T3,N'Đến kỳ bảo dưỡng',N'01 chìa khóa',N'Bảo dưỡng định kỳ','NV009',1,DATEADD(MINUTE,5,@T3),'NV010','NV009',DATEADD(MINUTE,10,@T3));
INSERT dbo.BaoCaoKiemTra(MaBaoCao,MaPhieuTiepNhan,NgayKiemTra,BoPhan,NoiDung0,NoiDung1,NoiDung2,NoiDung3,NoiDung4,NoiDung5,NoiDung6,DeXuat0,DeXuat1,DeXuat2,TrangThai,ThoiDiemHoanTat) VALUES
('KT-2026-0003','TN-2026-0003',@D,N'Phanh',N'Má phanh mòn',N'Dầu phanh thấp',N'',N'',N'',N'',N'',N'Thay má phanh',N'Bổ sung dầu',N'','HOAN_TAT',DATEADD(MINUTE,30,@T1)),
('KT-2026-0004','TN-2026-0004',@D,N'Lốp',N'Hai lốp trước mòn',N'',N'',N'',N'',N'',N'',N'Thay hai lốp',N'',N'','HOAN_TAT',DATEADD(MINUTE,30,@T2)),
('KT-2026-0005','TN-2026-0005',@D,N'Tổng hợp',N'Dầu cần thay',N'Lọc gió bẩn',N'',N'',N'',N'',N'',N'Bảo dưỡng',N'Thay lọc gió',N'','HOAN_TAT',DATEADD(MINUTE,30,@T3));
INSERT dbo.NhomBaoGia VALUES('BG-2026-0002','KT-2026-0003'),('BG-2026-0003','KT-2026-0004'),('BG-2026-0004','KT-2026-0005');
INSERT dbo.BaoGia(MaBaoGia,MaBaoGiaGoc,PhienBan,BienSo,NgayHen,NgayLap,NguoiLap,TrangThai,NgayGui,NguoiGui,ThoiDiemPhanHoi,ThoiDiemGhiNhan,NguoiGhiNhan,KenhPhanHoi,GhiChuPhanHoi,MaThaoTac) VALUES
('BG-2026-0002','BG-2026-0002',1,'51C-11111',DATEADD(HOUR,4,@T1),DATEADD(MINUTE,35,@T1),'NV009','DA_DONG_Y',DATEADD(MINUTE,40,@T1),'NV009',DATEADD(MINUTE,45,@T1),DATEADD(MINUTE,46,@T1),'NV009',N'Trực tiếp',N'Đồng ý','MAU-BG-2026-0002'),
('BG-2026-0003','BG-2026-0003',1,'51D-22222',DATEADD(HOUR,4,@T2),DATEADD(MINUTE,35,@T2),'NV009','DA_DONG_Y',DATEADD(MINUTE,40,@T2),'NV009',DATEADD(MINUTE,45,@T2),DATEADD(MINUTE,46,@T2),'NV009',N'Điện thoại',N'Đồng ý','MAU-BG-2026-0003'),
('BG-2026-0004','BG-2026-0004',1,'51E-33333',DATEADD(HOUR,4,@T3),DATEADD(MINUTE,35,@T3),'NV009','DA_DONG_Y',DATEADD(MINUTE,40,@T3),'NV009',DATEADD(MINUTE,45,@T3),DATEADD(MINUTE,46,@T3),'NV009',N'Trực tiếp',N'Đồng ý','MAU-BG-2026-0004');
INSERT dbo.ChiTietBaoGia(MaBaoGia,ViTri,MaDichVu,MaPhuTung,TenHangMuc,Loai,DonVi,SoLuong,DonGia) VALUES
('BG-2026-0002',0,'DV004',NULL,N'Thay má phanh',N'Dịch vụ',N'Công',1,300000),('BG-2026-0002',1,NULL,'PT008',N'Má phanh Toyota',N'Phụ tùng',N'bộ',1,900000),('BG-2026-0002',2,NULL,'PT009',N'Dầu phanh DOT4',N'Phụ tùng',N'lít',1,120000),
('BG-2026-0003',0,'DV005',NULL,N'Thay lốp',N'Dịch vụ',N'Công',2,50000),('BG-2026-0003',1,NULL,'PT004',N'Lốp Michelin',N'Phụ tùng',N'cái',2,1500000),
('BG-2026-0004',0,'DV006',NULL,N'Bảo dưỡng định kỳ',N'Dịch vụ',N'Gói',1,500000),('BG-2026-0004',1,NULL,'PT001',N'Dầu động cơ 5W-30',N'Phụ tùng',N'lít',4,150000),('BG-2026-0004',2,NULL,'PT010',N'Lọc gió',N'Phụ tùng',N'cái',1,180000);
INSERT dbo.PhieuSuaChua(MaPhieu,MaBaoGia,MaBaoGiaGoc,BienSo,KyThuatChinh,KyThuatPhoiHop,HanHoanThanh,GhiChu,TrangThai,NguoiPhatHanh,ThoiDiemPhatHanh,ThoiDiemHoanTat,KetQua0,KetQua1,KetQua2,MaThaoTac) VALUES
('SC-2026-0002','BG-2026-0002','BG-2026-0002','51C-11111','NV010',NULL,DATEADD(HOUR,4,@T1),N'Theo báo giá','HOAN_THANH','NV009',DATEADD(MINUTE,50,@T1),DATEADD(MINUTE,120,@T1),N'Phanh hoạt động tốt',N'Đã đối chiếu',N'Chờ thu tiền','MAU-SC-2026-0002'),
('SC-2026-0003','BG-2026-0003','BG-2026-0003','51D-22222','NV010',NULL,DATEADD(HOUR,4,@T2),N'Theo báo giá','HOAN_THANH','NV009',DATEADD(MINUTE,50,@T2),DATEADD(MINUTE,120,@T2),N'Xe vận hành tốt',N'Đã cân bằng bánh',N'Chờ thu tiền','MAU-SC-2026-0003'),
('SC-2026-0004','BG-2026-0004','BG-2026-0004','51E-33333','NV010',NULL,DATEADD(HOUR,4,@T3),N'Theo báo giá','HOAN_THANH','NV009',DATEADD(MINUTE,50,@T3),DATEADD(MINUTE,120,@T3),N'Đã bảo dưỡng',N'Đã kiểm tra',N'Chờ thu tiền','MAU-SC-2026-0004');
COMMIT TRANSACTION;
END TRY
BEGIN CATCH
IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
THROW;
END CATCH;
GO
PRINT N'=== PHIẾU CHỜ THANH TOÁN ===';
SELECT * FROM dbo.vw_PhieuChoThu ORDER BY MaLenh;
PRINT N'=== HÓA ĐƠN ĐÃ THANH TOÁN ===';
SELECT MaHoaDon, MaPhieuSuaChua, TenKhachHang, TongTien, HinhThucThanhToan
FROM dbo.HoaDon ORDER BY MaHoaDon;

PRINT N'=== KIỂM TRA TỔNG HÓA ĐƠN / CHI TIẾT ===';
IF EXISTS (
    SELECT 1
    FROM dbo.HoaDon hd
    LEFT JOIN dbo.ChiTietHoaDon ct ON ct.MaHoaDon = hd.MaHoaDon
    GROUP BY hd.MaHoaDon, hd.TongTien
    HAVING hd.TongTien <> COALESCE(SUM(ct.ThanhTien), 0)
)
    THROW 51000, N'Tổng hóa đơn không khớp tổng chi tiết hóa đơn.', 1;

SELECT hd.MaHoaDon, hd.TongTien, SUM(ct.ThanhTien) AS TongChiTiet
FROM dbo.HoaDon hd
JOIN dbo.ChiTietHoaDon ct ON ct.MaHoaDon = hd.MaHoaDon
GROUP BY hd.MaHoaDon, hd.TongTien
ORDER BY hd.MaHoaDon;
GO
.MaHoaDon, hd.TongTien
ORDER BY hd.MaHoaDon;
GO
