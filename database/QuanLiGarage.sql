-- GARAGE ĐỨC DŨNG — SQL SERVER 2016 SP1+ (compatibility level >= 130).
-- Bản hoàn chỉnh 2026-11-10: schema lõi + migration bổ sung + dữ liệu demo + API nghiệp vụ.
-- Chạy trọn file bằng SSMS hoặc sqlcmd -b. Không DROP/TRUNCATE database/bảng/dữ liệu.
-- Chạy lại: không gieo lại demo, không reset sequence, không ghi đè chứng từ.
-- Database có dữ liệu: backup trước, đối chiếu cảnh báo cuối file; script không tự bịa dữ liệu lịch sử.
-- DAO/UI phải dùng nguồn chi phí thực tế và các procedure nghiệp vụ; xem database/README.md.
-- Cờ DaThanhToan trên lệnh tách khỏi trạng thái sửa chữa HOAN_THANH.
-- Mật khẩu demo dùng PBKDF2 có salt; ứng dụng phải xác minh qua TaiKhoanThongTinBaoMat.
-- SQL kiểm tra vai trò theo mã nhân viên, nhưng không thay thế xác thực/kiểm tra phiên của ứng dụng.

USE master;
DROP DATABASE QuanLiGarage
GO
IF DB_ID(N'QuanLiGarage') IS NULL
    EXEC(N'CREATE DATABASE QuanLiGarage');
GO
USE QuanLiGarage;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
IF (SELECT compatibility_level FROM sys.databases WHERE database_id=DB_ID())<130
    THROW 51000,N'Cần compatibility level từ 130 để dùng OPENJSON; quản trị viên cần xác nhận cấu hình.',1;
-- Đổi @TaoDuLieuMau=0 khi dựng database trống cho triển khai.
DECLARE @TaoDuLieuMau BIT=1;
IF OBJECT_ID('tempdb..#CauHinhCaiDat') IS NOT NULL DROP TABLE #CauHinhCaiDat;
CREATE TABLE #CauHinhCaiDat(GieoMau BIT NOT NULL);
INSERT #CauHinhCaiDat VALUES(CASE WHEN OBJECT_ID('dbo.NhanVien','U') IS NULL AND @TaoDuLieuMau=1 THEN 1 ELSE 0 END);
GO
-- PHẦN 1: GIỮ CÁC BẢNG LÕI, TẠO CÒN THIẾU VÀ CÁC BẢNG MỞ RỘNG.
BEGIN TRY
    BEGIN TRANSACTION;
    IF OBJECT_ID(N'dbo.SoMaThaoTac', N'SO') IS NULL
    CREATE SEQUENCE dbo.SoMaThaoTac
        AS BIGINT
        START WITH 1
        INCREMENT BY 1;

    -- 1. Nhân viên, tài khoản
    IF OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.TaiKhoan', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.NhatKyTaiKhoan', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.KhachHang', N'U') IS NULL
    CREATE TABLE dbo.KhachHang (
        MaKH VARCHAR(30) PRIMARY KEY,
        HoTen NVARCHAR(150) NOT NULL,
        SoDienThoai VARCHAR(20) NOT NULL,
        DiaChi NVARCHAR(500) NOT NULL DEFAULT N'',
        Email NVARCHAR(254) NOT NULL DEFAULT N'',
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        CHECK (LEN(LTRIM(RTRIM(HoTen))) > 0)
    );

    IF OBJECT_ID(N'dbo.Xe', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.DichVu', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.PhuTung', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.PhieuTiepNhan', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.BaoCaoKiemTra', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.NhomBaoGia', N'U') IS NULL
    CREATE TABLE dbo.NhomBaoGia (
        MaBaoGiaGoc VARCHAR(40) PRIMARY KEY,
        MaBaoCao VARCHAR(40) NOT NULL UNIQUE,
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCaoKiemTra(MaBaoCao)
    );

    IF OBJECT_ID(N'dbo.BaoGia', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.ChiTietBaoGia', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.PhieuSuaChua', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.PhanCong', N'U') IS NULL
    CREATE TABLE dbo.PhanCong (
        MaPC VARCHAR(40) PRIMARY KEY,
        MaPhieuSC VARCHAR(40) NOT NULL,
        MaNV VARCHAR(20) NOT NULL,
        NgayPhanCong DATETIME2(3) NOT NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        FOREIGN KEY (MaPhieuSC) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (MaNV) REFERENCES dbo.NhanVien(MaNhanVien)
    );

    IF OBJECT_ID(N'dbo.ChiTietSuaChua', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.PhatSinhSuaChua', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.PhieuNhapKho', N'U') IS NULL
    CREATE TABLE dbo.PhieuNhapKho (
        MaPhieu VARCHAR(40) PRIMARY KEY,
        NgayNhap DATETIME2(3) NOT NULL,
        NguonNhap NVARCHAR(1000) NOT NULL,
        ChungTuNguon NVARCHAR(100) NOT NULL DEFAULT N'',
        NguoiNhap VARCHAR(20) NOT NULL,
        FOREIGN KEY (NguoiNhap) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (LEN(LTRIM(RTRIM(NguonNhap))) > 0)
    );

    IF OBJECT_ID(N'dbo.ChiTietNhapKho', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.PhuTungSuDung', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.BienDongKho', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.ChiTietHoaDon', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.BanGiaoXe', N'U') IS NULL
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
    IF OBJECT_ID(N'dbo.BaoCao', N'U') IS NULL
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

    IF OBJECT_ID(N'dbo.ChiSoBaoCao', N'U') IS NULL
    CREATE TABLE dbo.ChiSoBaoCao (
        MaBaoCao VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenChiSo NVARCHAR(200) NOT NULL,
        GiaTri BIGINT NOT NULL,
        PRIMARY KEY (MaBaoCao, ViTri),
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCao(MaBaoCao),
        CHECK (ViTri >= 0)
    );

    IF OBJECT_ID(N'dbo.CotBaoCao', N'U') IS NULL
    CREATE TABLE dbo.CotBaoCao (
        MaBaoCao VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenCot NVARCHAR(200) NOT NULL,
        PRIMARY KEY (MaBaoCao, ViTri),
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCao(MaBaoCao),
        CHECK (ViTri >= 0)
    );

    IF OBJECT_ID(N'dbo.OBaoCao', N'U') IS NULL
    CREATE TABLE dbo.OBaoCao (
        MaBaoCao VARCHAR(40) NOT NULL,
        Dong INT NOT NULL,
        Cot INT NOT NULL,
        GiaTri NVARCHAR(MAX) NOT NULL,
        PRIMARY KEY (MaBaoCao, Dong, Cot),
        FOREIGN KEY (MaBaoCao, Cot) REFERENCES dbo.CotBaoCao(MaBaoCao, ViTri),
        CHECK (Dong >= 0 AND Cot >= 0)
    );


    IF OBJECT_ID(N'dbo.SoMaChungTu',N'SO') IS NULL
    CREATE SEQUENCE dbo.SoMaChungTu AS BIGINT START WITH 100000 INCREMENT BY 1;
    -- 10. Schema mở rộng để lưu dữ liệu nghiệp vụ có lịch sử.
    -- Các bảng lõi bên trên được giữ lại để tương thích dữ liệu và DAO hiện có.

    -- Thông tin xác thực mới. DAO đăng nhập cần chuyển sang xác minh PBKDF2;
    -- MatKhau legacy giữ nguyên khi nâng cấp; tài khoản mới chỉ dùng marker ngẫu nhiên.
    -- Giá trị hash/salt thực nằm trong TaiKhoanThongTinBaoMat; DAO mới không được fallback plaintext.
    IF OBJECT_ID(N'dbo.TaiKhoanThongTinBaoMat', N'U') IS NULL
    CREATE TABLE dbo.TaiKhoanThongTinBaoMat (
        MaNhanVien VARCHAR(20) PRIMARY KEY,
        ThuatToan VARCHAR(40) NOT NULL,
        Muoi VARBINARY(32) NOT NULL,
        GiaTriBam VARBINARY(64) NOT NULL,
        SoVong INT NOT NULL,
        NgayCapNhat DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        FOREIGN KEY (MaNhanVien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (ThuatToan IN ('PBKDF2-HMAC-SHA256')),
        CHECK (DATALENGTH(Muoi) BETWEEN 16 AND 32),
        CHECK (DATALENGTH(GiaTriBam) BETWEEN 32 AND 64),
        CHECK (SoVong >= 600000)
    );

    -- Chi tiết kiểm tra linh hoạt. Các cột NoiDung0..6/DeXuat0..2 vẫn được giữ
    -- để đọc dữ liệu cũ trong giai đoạn chuyển DAO/UI.
    IF OBJECT_ID(N'dbo.ChiTietKiemTra', N'U') IS NULL
    CREATE TABLE dbo.ChiTietKiemTra (
        MaBaoCao VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        TenKhuVuc NVARCHAR(150) NOT NULL,
        TenHangMuc NVARCHAR(200) NOT NULL,
        KetQua NVARCHAR(2000) NOT NULL,
        MucDo NVARCHAR(50) NOT NULL DEFAULT N'Chưa đánh giá',
        DeXuat NVARCHAR(2000) NOT NULL DEFAULT N'',
        NguoiGhi VARCHAR(20) NULL,
        ThoiDiemGhi DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        PRIMARY KEY (MaBaoCao, ViTri),
        FOREIGN KEY (MaBaoCao) REFERENCES dbo.BaoCaoKiemTra(MaBaoCao),
        FOREIGN KEY (NguoiGhi) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (ViTri >= 0),
        CHECK (LEN(LTRIM(RTRIM(TenKhuVuc))) > 0),
        CHECK (LEN(LTRIM(RTRIM(TenHangMuc))) > 0)
    );

    -- Một lệnh có thể có nhiều đề xuất phát sinh độc lập.
    IF OBJECT_ID(N'dbo.DeXuatPhatSinh', N'U') IS NULL
    CREATE TABLE dbo.DeXuatPhatSinh (
        MaPhatSinh BIGINT IDENTITY(1,1) PRIMARY KEY,
        MaPhieu VARCHAR(40) NOT NULL,
        SoThuTu INT NOT NULL,
        HuHong NVARCHAR(2000) NOT NULL,
        NguyenNhan NVARCHAR(2000) NOT NULL,
        DeXuat NVARCHAR(2000) NOT NULL,
        ThoiGianDuKienPhut INT NULL,
        MaBaoGiaDieuChinh VARCHAR(50) NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(24) NOT NULL DEFAULT 'CHO_KHACH',
        NguoiBao VARCHAR(20) NOT NULL,
        ThoiDiemBao DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        FOREIGN KEY (MaPhieu) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (MaBaoGiaDieuChinh) REFERENCES dbo.BaoGia(MaBaoGia),
        FOREIGN KEY (NguoiBao) REFERENCES dbo.TaiKhoan(MaNhanVien),
        UNIQUE (MaPhieu, SoThuTu),
        UNIQUE (MaPhatSinh, MaPhieu),
        CHECK (SoThuTu > 0),
        CHECK (ThoiGianDuKienPhut IS NULL OR ThoiGianDuKienPhut >= 0),
        CHECK (TrangThai IN ('CHO_KHACH', 'DA_DONG_Y', 'TU_CHOI', 'DA_HUY'))
    );

    IF OBJECT_ID(N'dbo.ChiTietDeXuatPhatSinh', N'U') IS NULL
    CREATE TABLE dbo.ChiTietDeXuatPhatSinh (
        MaPhatSinh BIGINT NOT NULL,
        ViTri INT NOT NULL,
        MaDichVu VARCHAR(30) NULL,
        MaPhuTung VARCHAR(30) NULL,
        TenHangMuc NVARCHAR(200) NOT NULL,
        Loai NVARCHAR(20) NOT NULL,
        DonVi NVARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        DonGia BIGINT NOT NULL,
        ThanhTien AS CONVERT(BIGINT, ROUND(CONVERT(DECIMAL(38,3), DonGia) * SoLuong, 0)),
        PRIMARY KEY (MaPhatSinh, ViTri),
        FOREIGN KEY (MaPhatSinh) REFERENCES dbo.DeXuatPhatSinh(MaPhatSinh),
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
        CHECK (DonVi NOT IN (N'cái', N'bộ', N'chai') OR SoLuong % 1 = 0)
    );

    -- Liên kết từng hạng mục phát sinh đã được đồng ý với hạng mục thực thi.
    IF OBJECT_ID(N'dbo.ChiTietSuaChuaPhatSinh', N'U') IS NULL
    CREATE TABLE dbo.ChiTietSuaChuaPhatSinh (
        MaPhieu VARCHAR(40) NOT NULL,
        ViTriSuaChua INT NOT NULL,
        MaPhatSinh BIGINT NOT NULL,
        ViTriDeXuat INT NOT NULL,
        PRIMARY KEY (MaPhieu, ViTriSuaChua),
        FOREIGN KEY (MaPhieu, ViTriSuaChua) REFERENCES dbo.ChiTietSuaChua(MaPhieu, ViTri),
        FOREIGN KEY (MaPhatSinh, ViTriDeXuat) REFERENCES dbo.ChiTietDeXuatPhatSinh(MaPhatSinh, ViTri),
        FOREIGN KEY (MaPhatSinh, MaPhieu) REFERENCES dbo.DeXuatPhatSinh(MaPhatSinh, MaPhieu)
    );

    -- Lưu mọi lần khách phản hồi để không ghi đè lịch sử đồng ý/từ chối.
    IF OBJECT_ID(N'dbo.PhanHoiPhatSinh', N'U') IS NULL
    CREATE TABLE dbo.PhanHoiPhatSinh (
        MaPhanHoi BIGINT IDENTITY(1,1) PRIMARY KEY,
        MaPhatSinh BIGINT NOT NULL,
        QuyetDinh VARCHAR(16) NOT NULL,
        TenNguoiXacNhanKhach NVARCHAR(150) NOT NULL,
        KenhPhanHoi NVARCHAR(100) NOT NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        NguoiGhiNhan VARCHAR(20) NOT NULL,
        ThoiDiemKhachPhanHoi DATETIME2(3) NOT NULL,
        ThoiDiemGhiNhan DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        FOREIGN KEY (MaPhatSinh) REFERENCES dbo.DeXuatPhatSinh(MaPhatSinh),
        FOREIGN KEY (NguoiGhiNhan) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (QuyetDinh IN ('DA_DONG_Y', 'TU_CHOI'))
    );

    -- Snapshot đối chiếu phải thu trước khi thu tiền; chưa thay thế luồng DAO cũ.
    IF OBJECT_ID(N'dbo.DoiChieuChiPhi', N'U') IS NULL
    CREATE TABLE dbo.DoiChieuChiPhi (
        MaPhieu VARCHAR(40) PRIMARY KEY,
        MaBaoGia VARCHAR(50) NOT NULL,
        TrangThai VARCHAR(20) NOT NULL DEFAULT 'NHAP',
        TongTienCong BIGINT NOT NULL DEFAULT 0,
        TongTienPhuTung BIGINT NOT NULL DEFAULT 0,
        TongTienPhatSinh BIGINT NOT NULL DEFAULT 0,
        TienGiam BIGINT NOT NULL DEFAULT 0,
        TienThue BIGINT NOT NULL DEFAULT 0,
        TongPhaiThu BIGINT NOT NULL DEFAULT 0,
        NguoiLap VARCHAR(20) NOT NULL,
        ThoiDiemLap DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        NguoiXacNhan VARCHAR(20) NULL,
        ThoiDiemXacNhan DATETIME2(3) NULL,
        FOREIGN KEY (MaPhieu) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (MaBaoGia) REFERENCES dbo.BaoGia(MaBaoGia),
        FOREIGN KEY (NguoiLap) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiXacNhan) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (TrangThai IN ('NHAP', 'DA_XAC_NHAN', 'HUY')),
        CHECK (TongTienCong >= 0 AND TongTienPhuTung >= 0 AND TongTienPhatSinh >= 0),
        CHECK (TienGiam >= 0 AND TienThue >= 0 AND TongPhaiThu >= 0),
        CHECK (
            CONVERT(DECIMAL(38,0), TongPhaiThu) =
            CONVERT(DECIMAL(38,0), TongTienCong) + TongTienPhuTung + TongTienPhatSinh + TienThue - TienGiam
        ),
        CHECK (
            (TrangThai = 'DA_XAC_NHAN' AND NguoiXacNhan IS NOT NULL AND ThoiDiemXacNhan IS NOT NULL)
            OR TrangThai <> 'DA_XAC_NHAN'
        )
    );

    IF OBJECT_ID(N'dbo.ChiTietDoiChieuChiPhi', N'U') IS NULL
    CREATE TABLE dbo.ChiTietDoiChieuChiPhi (
        MaPhieu VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        LoaiChiPhi VARCHAR(20) NOT NULL,
        TenHangMuc NVARCHAR(300) NOT NULL,
        DonVi NVARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        DonGia BIGINT NOT NULL,
        ThanhTien AS CONVERT(BIGINT, ROUND(CONVERT(DECIMAL(38,3), DonGia) * SoLuong, 0)),
        MaBaoGia VARCHAR(50) NULL,
        ViTriBaoGia INT NULL,
        MaGhiNhan VARCHAR(40) NULL,
        MaPhatSinh BIGINT NULL,
        PRIMARY KEY (MaPhieu, ViTri),
        FOREIGN KEY (MaPhieu) REFERENCES dbo.DoiChieuChiPhi(MaPhieu),
        FOREIGN KEY (MaBaoGia, ViTriBaoGia) REFERENCES dbo.ChiTietBaoGia(MaBaoGia, ViTri),
        FOREIGN KEY (MaGhiNhan) REFERENCES dbo.PhuTungSuDung(MaGhiNhan),
        FOREIGN KEY (MaPhatSinh) REFERENCES dbo.DeXuatPhatSinh(MaPhatSinh),
        CHECK (ViTri >= 0),
        CHECK (LoaiChiPhi IN ('CONG', 'PHU_TUNG', 'PHAT_SINH', 'DIEU_CHINH')),
        CHECK (SoLuong > 0 AND DonGia >= 0),
        CHECK (DonVi NOT IN (N'cái', N'bộ', N'chai') OR SoLuong % 1 = 0)
    );

    -- Giao dịch thu/hoàn tiền có trạng thái đối soát riêng, không sửa hóa đơn gốc.
    IF OBJECT_ID(N'dbo.GiaoDichThanhToan', N'U') IS NULL
    CREATE TABLE dbo.GiaoDichThanhToan (
        MaGiaoDichNoiBo BIGINT IDENTITY(1,1) PRIMARY KEY,
        MaHoaDon VARCHAR(40) NULL,
        MaLenh VARCHAR(40) NOT NULL,
        MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
        ChungCuXacMinh NVARCHAR(1000) NULL,
        LoaiGiaoDich VARCHAR(16) NOT NULL DEFAULT 'THU',
        HinhThuc VARCHAR(20) NOT NULL,
        SoTien BIGINT NOT NULL,
        MaThamChieu NVARCHAR(100) NOT NULL DEFAULT N'',
        TrangThai VARCHAR(20) NOT NULL DEFAULT 'CHO_XAC_MINH',
        NguoiThucHien VARCHAR(20) NOT NULL,
        ThoiDiemTao DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        NguoiXacMinh VARCHAR(20) NULL,
        ThoiDiemXacMinh DATETIME2(3) NULL,
        GhiChu NVARCHAR(2000) NOT NULL DEFAULT N'',
        FOREIGN KEY (MaHoaDon) REFERENCES dbo.HoaDon(MaHoaDon),
        FOREIGN KEY (MaLenh) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (NguoiThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiXacMinh) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (LoaiGiaoDich IN ('THU', 'HOAN_TIEN')),
        CHECK (HinhThuc IN ('TIEN_MAT', 'CHUYEN_KHOAN')),
        CHECK (SoTien > 0),
        CHECK (TrangThai IN ('CHO_XAC_MINH', 'DA_XAC_MINH', 'TU_CHOI', 'DA_HOAN')),
        CHECK (LoaiGiaoDich <> 'HOAN_TIEN' OR MaHoaDon IS NOT NULL),
        CHECK (TrangThai <> 'DA_HOAN' OR LoaiGiaoDich = 'HOAN_TIEN'),
        CHECK (
            (TrangThai IN ('DA_XAC_MINH', 'DA_HOAN') AND NguoiXacMinh IS NOT NULL AND ThoiDiemXacMinh IS NOT NULL)
            OR TrangThai IN ('CHO_XAC_MINH', 'TU_CHOI')
        ),
        CHECK (HinhThuc <> 'CHUYEN_KHOAN' OR LEN(LTRIM(RTRIM(MaThamChieu))) > 0)
    );

    -- Phê duyệt bàn giao ngoại lệ; cần DAO/UI kiểm tra quyền trước khi sử dụng.
    IF OBJECT_ID(N'dbo.PheDuyetBanGiaoNgoaiLe', N'U') IS NULL
    CREATE TABLE dbo.PheDuyetBanGiaoNgoaiLe (
        MaPheDuyet BIGINT IDENTITY(1,1) PRIMARY KEY,
        MaLenh VARCHAR(40) NOT NULL,
        SoTienConNo BIGINT NOT NULL,
        LyDo NVARCHAR(2000) NOT NULL,
        TrangThai VARCHAR(16) NOT NULL DEFAULT 'CHO_DUYET',
        NguoiDeNghi VARCHAR(20) NOT NULL,
        ThoiDiemDeNghi DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        NguoiPheDuyet VARCHAR(20) NULL,
        ThoiDiemPheDuyet DATETIME2(3) NULL,
        ThoiHan DATETIME2(3) NULL,
        FOREIGN KEY (MaLenh) REFERENCES dbo.PhieuSuaChua(MaPhieu),
        FOREIGN KEY (NguoiDeNghi) REFERENCES dbo.TaiKhoan(MaNhanVien),
        FOREIGN KEY (NguoiPheDuyet) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (SoTienConNo > 0),
        CHECK (TrangThai IN ('CHO_DUYET', 'DA_DUYET', 'TU_CHOI', 'HET_HAN')),
        CHECK (
            (TrangThai IN ('DA_DUYET', 'TU_CHOI', 'HET_HAN') AND NguoiPheDuyet IS NOT NULL AND ThoiDiemPheDuyet IS NOT NULL)
            OR TrangThai = 'CHO_DUYET'
        )
    );



    -- Nhật ký điều chỉnh/hoàn kho tham chiếu tới biến động ban đầu.
    IF OBJECT_ID(N'dbo.PhieuDieuChinhKho', N'U') IS NULL
    CREATE TABLE dbo.PhieuDieuChinhKho (
        MaPhieuDieuChinh VARCHAR(40) PRIMARY KEY,
        ThoiDiem DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        LyDo NVARCHAR(2000) NOT NULL,
        NguoiThucHien VARCHAR(20) NOT NULL,
        TrangThai VARCHAR(16) NOT NULL DEFAULT 'NHAP',
        FOREIGN KEY (NguoiThucHien) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (LEN(LTRIM(RTRIM(LyDo))) > 0),
        CHECK (TrangThai IN ('NHAP', 'DA_GHI_NHAN', 'HUY'))
    );

    IF OBJECT_ID(N'dbo.ChiTietDieuChinhKho', N'U') IS NULL
    CREATE TABLE dbo.ChiTietDieuChinhKho (
        MaPhieuDieuChinh VARCHAR(40) NOT NULL,
        ViTri INT NOT NULL,
        MaPhuTung VARCHAR(30) NOT NULL,
        SoLuong DECIMAL(18,3) NOT NULL,
        Chieu VARCHAR(12) NOT NULL,
        MaBienDongGoc BIGINT NULL,
        PRIMARY KEY (MaPhieuDieuChinh, ViTri),
        FOREIGN KEY (MaPhieuDieuChinh) REFERENCES dbo.PhieuDieuChinhKho(MaPhieuDieuChinh),
        FOREIGN KEY (MaPhuTung) REFERENCES dbo.PhuTung(MaPhuTung),
        FOREIGN KEY (MaBienDongGoc) REFERENCES dbo.BienDongKho(MaBienDong),
        CHECK (ViTri >= 0 AND SoLuong > 0),
        CHECK (Chieu IN ('NHAP', 'XUAT'))
    );

    -- Cấu hình thông tin đơn vị in hóa đơn, không điền địa chỉ/số điện thoại giả.
    IF OBJECT_ID(N'dbo.ThongTinGarage', N'U') IS NULL
    CREATE TABLE dbo.ThongTinGarage (
        MaCauHinh TINYINT PRIMARY KEY,
        TenGarage NVARCHAR(200) NOT NULL DEFAULT N'',
        DiaChi NVARCHAR(500) NOT NULL DEFAULT N'',
        SoDienThoai VARCHAR(30) NOT NULL DEFAULT '',
        MaSoThue VARCHAR(30) NOT NULL DEFAULT '',
        MuiGio VARCHAR(64) NOT NULL DEFAULT 'SE Asia Standard Time',
        DangSuDung BIT NOT NULL DEFAULT 0,
        CHECK (MaCauHinh = 1)
    );

    -- Không mặc định một phương pháp giá vốn khi gara chưa xác nhận chính sách.
    IF OBJECT_ID(N'dbo.CauHinhGiaVon', N'U') IS NULL
    CREATE TABLE dbo.CauHinhGiaVon (
        MaCauHinh TINYINT PRIMARY KEY,
        PhuongPhap VARCHAR(20) NOT NULL DEFAULT 'CHUA_CHOT',
        ThoiDiemCapNhat DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
        NguoiCapNhat VARCHAR(20) NULL,
        FOREIGN KEY (NguoiCapNhat) REFERENCES dbo.TaiKhoan(MaNhanVien),
        CHECK (MaCauHinh = 1),
        CHECK (PhuongPhap IN ('CHUA_CHOT', 'GIA_CHUAN', 'BINH_QUAN', 'FIFO'))
    );

    IF NOT EXISTS (SELECT 1 FROM dbo.CauHinhGiaVon WHERE MaCauHinh=1)
    INSERT dbo.CauHinhGiaVon (MaCauHinh, PhuongPhap) VALUES (1, 'CHUA_CHOT');
    IF NOT EXISTS (SELECT 1 FROM dbo.ThongTinGarage WHERE MaCauHinh=1)
    INSERT dbo.ThongTinGarage (MaCauHinh, DangSuDung) VALUES (1, 0);


    COMMIT;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK;
    THROW;
END CATCH;
GO
-- NÂNG CẤP TƯƠNG THÍCH: bổ sung cột còn thiếu, không xóa dữ liệu.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF COL_LENGTH('dbo.PhieuSuaChua','DaThanhToan') IS NULL
    ALTER TABLE dbo.PhieuSuaChua ADD DaThanhToan BIT NOT NULL CONSTRAINT DF_SC_DaThanhToan DEFAULT 0;
IF COL_LENGTH('dbo.PhieuSuaChua','ThoiDiemThanhToan') IS NULL
    ALTER TABLE dbo.PhieuSuaChua ADD ThoiDiemThanhToan DATETIME2(3) NULL;
IF COL_LENGTH('dbo.PhuTungSuDung','DonGiaVonTaiThoiDiem') IS NULL
    ALTER TABLE dbo.PhuTungSuDung ADD DonGiaVonTaiThoiDiem BIGINT NULL;
IF COL_LENGTH('dbo.HoaDon','TienTruocThue') IS NULL
    ALTER TABLE dbo.HoaDon ADD TienTruocThue BIGINT NULL;
IF COL_LENGTH('dbo.HoaDon','TienThue') IS NULL
    ALTER TABLE dbo.HoaDon ADD TienThue BIGINT NOT NULL CONSTRAINT DF_HD_Thue DEFAULT 0;
IF COL_LENGTH('dbo.HoaDon','TienGiam') IS NULL
    ALTER TABLE dbo.HoaDon ADD TienGiam BIGINT NOT NULL CONSTRAINT DF_HD_Giam DEFAULT 0;
IF COL_LENGTH('dbo.HoaDon','LyDoDieuChinh') IS NULL
    ALTER TABLE dbo.HoaDon ADD LyDoDieuChinh NVARCHAR(2000) NOT NULL CONSTRAINT DF_HD_LyDo DEFAULT N'';
IF COL_LENGTH('dbo.HoaDon','MaThaoTac') IS NULL
    ALTER TABLE dbo.HoaDon ADD MaThaoTac VARCHAR(100) NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon','MaPhatSinh') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon ADD MaPhatSinh BIGINT NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon','ViTriSuaChua') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon ADD ViTriSuaChua INT NULL;
IF COL_LENGTH('dbo.BanGiaoXe','MaPheDuyetNgoaiLe') IS NULL
    ALTER TABLE dbo.BanGiaoXe ADD MaPheDuyetNgoaiLe BIGINT NULL;
ALTER TABLE dbo.BanGiaoXe ALTER COLUMN MaHoaDon VARCHAR(40) NULL;
COMMIT;
GO

IF OBJECT_ID(N'dbo.DonViTinh',N'U') IS NULL
CREATE TABLE dbo.DonViTinh (
    DonVi NVARCHAR(10) PRIMARY KEY,
    ChoPhepSoLe BIT NOT NULL,
    HoatDong BIT NOT NULL DEFAULT 1
);
INSERT dbo.DonViTinh(DonVi,ChoPhepSoLe)
SELECT x.DonVi,x.SoLe FROM (VALUES(N'cái',0),(N'bộ',0),(N'lít',1),(N'm',1),(N'kg',1),(N'chai',0),(N'ml',1)) x(DonVi,SoLe)
WHERE NOT EXISTS(SELECT 1 FROM dbo.DonViTinh d WHERE d.DonVi=x.DonVi);
-- Giữ nguyên đơn vị đã tồn tại khi nâng cấp; không đổi tên/lượng trong dữ liệu cũ.
INSERT dbo.DonViTinh(DonVi,ChoPhepSoLe)
SELECT DISTINCT p.DonViTinh,CASE WHEN p.DonViTinh IN(N'cái',N'bộ',N'chai') THEN 0 ELSE 1 END
FROM dbo.PhuTung p WHERE NOT EXISTS(SELECT 1 FROM dbo.DonViTinh d WHERE d.DonVi=p.DonViTinh);

-- Chỉ thay các CHECK legacy giới hạn danh mục/độ lẻ. Thay bằng FK + trigger theo danh mục.
DECLARE @Lenh NVARCHAR(MAX)=N'';
SELECT @Lenh=@Lenh+N'ALTER TABLE dbo.PhuTung DROP CONSTRAINT '+QUOTENAME(name)+N';'
FROM sys.check_constraints
WHERE parent_object_id=OBJECT_ID(N'dbo.PhuTung') AND definition LIKE '%DonViTinh%';
EXEC sys.sp_executesql @Lenh;
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_PhuTung_DonViTinh')
    ALTER TABLE dbo.PhuTung WITH CHECK ADD CONSTRAINT FK_PhuTung_DonViTinh
        FOREIGN KEY(DonViTinh) REFERENCES dbo.DonViTinh(DonVi);
GO

-- Schema bổ sung cho lịch sử, đối soát và chứng từ điều chỉnh.
IF OBJECT_ID(N'dbo.NhatKyNghiepVu',N'U') IS NULL
CREATE TABLE dbo.NhatKyNghiepVu (
    MaNhatKy BIGINT IDENTITY PRIMARY KEY,
    Bang NVARCHAR(128) NOT NULL,
    KhoaChungTu NVARCHAR(100) NOT NULL,
    HanhDong VARCHAR(20) NOT NULL,
    NguoiThucHien VARCHAR(20) NULL,
    ThoiDiem DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
    NoiDung NVARCHAR(MAX) NOT NULL
);
IF OBJECT_ID(N'dbo.LichSuBaoCaoKiemTra',N'U') IS NULL
CREATE TABLE dbo.LichSuBaoCaoKiemTra (
    MaLichSu BIGINT IDENTITY PRIMARY KEY,
    MaBaoCao VARCHAR(40) NOT NULL,
    ThoiDiem DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
    -- Giữ mã tham chiếu logic để audit vẫn còn khi DAO xóa báo cáo NHAP.
    NoiDungTruoc NVARCHAR(MAX) NOT NULL
);
IF OBJECT_ID(N'dbo.DieuChinhHoaDon',N'U') IS NULL
CREATE TABLE dbo.DieuChinhHoaDon (
    MaDieuChinh BIGINT IDENTITY PRIMARY KEY,
    MaHoaDon VARCHAR(40) NOT NULL,
    MaThaoTac VARCHAR(100) NOT NULL UNIQUE,
    LyDo NVARCHAR(2000) NOT NULL,
    SoTienDieuChinh BIGINT NOT NULL,
    NguoiLap VARCHAR(20) NOT NULL,
    ThoiDiem DATETIME2(3) NOT NULL DEFAULT SYSDATETIME(),
    NguoiDuyet VARCHAR(20) NULL,
    ThoiDiemDuyet DATETIME2(3) NULL,
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'NHAP',
    FOREIGN KEY(MaHoaDon) REFERENCES dbo.HoaDon(MaHoaDon),
    FOREIGN KEY(NguoiLap) REFERENCES dbo.TaiKhoan(MaNhanVien),
    FOREIGN KEY(NguoiDuyet) REFERENCES dbo.TaiKhoan(MaNhanVien),
    CHECK(SoTienDieuChinh<>0 AND LEN(LTRIM(RTRIM(LyDo)))>0),
    CHECK(TrangThai IN('NHAP','DA_DUYET','TU_CHOI')),
    CHECK(TrangThai='NHAP' OR (NguoiDuyet IS NOT NULL AND ThoiDiemDuyet IS NOT NULL))
);
GO
-- Cột bổ sung cho các database đã từng chạy bản schema cũ.
IF COL_LENGTH('dbo.GiaoDichThanhToan','MaLenh') IS NULL
    ALTER TABLE dbo.GiaoDichThanhToan ADD MaLenh VARCHAR(40) NULL;
IF COL_LENGTH('dbo.GiaoDichThanhToan','MaThaoTac') IS NULL
    ALTER TABLE dbo.GiaoDichThanhToan ADD MaThaoTac VARCHAR(100) NULL;
IF COL_LENGTH('dbo.GiaoDichThanhToan','ChungCuXacMinh') IS NULL
    ALTER TABLE dbo.GiaoDichThanhToan ADD ChungCuXacMinh NVARCHAR(1000) NULL;
IF COL_LENGTH('dbo.GiaoDichThanhToan','MaDieuChinh') IS NULL
    ALTER TABLE dbo.GiaoDichThanhToan ADD MaDieuChinh BIGINT NULL;
IF COL_LENGTH('dbo.DoiChieuChiPhi','LyDoDieuChinh') IS NULL
    ALTER TABLE dbo.DoiChieuChiPhi ADD LyDoDieuChinh NVARCHAR(2000) NOT NULL DEFAULT N'';
IF COL_LENGTH('dbo.PhieuNhapKho','MaThaoTac') IS NULL
    ALTER TABLE dbo.PhieuNhapKho ADD MaThaoTac VARCHAR(100) NULL;
IF COL_LENGTH('dbo.BaoCao','MaThaoTac') IS NULL
    ALTER TABLE dbo.BaoCao ADD MaThaoTac VARCHAR(100) NULL;
IF COL_LENGTH('dbo.BaoCao','JsonNguon') IS NULL
    ALTER TABLE dbo.BaoCao ADD JsonNguon NVARCHAR(MAX) NULL;
IF COL_LENGTH('dbo.PhieuDieuChinhKho','MaThaoTac') IS NULL
    ALTER TABLE dbo.PhieuDieuChinhKho ADD MaThaoTac VARCHAR(100) NULL;
IF COL_LENGTH('dbo.TaiKhoan','BuocDoiMatKhau') IS NULL
    ALTER TABLE dbo.TaiKhoan ADD BuocDoiMatKhau BIT NOT NULL DEFAULT 1;
IF COL_LENGTH('dbo.BaoGia','PhienBanKhoa') IS NULL
    ALTER TABLE dbo.BaoGia ADD PhienBanKhoa ROWVERSION;
GO
UPDATE g SET MaLenh=h.MaPhieuSuaChua
FROM dbo.GiaoDichThanhToan g JOIN dbo.HoaDon h ON h.MaHoaDon=g.MaHoaDon WHERE g.MaLenh IS NULL;
UPDATE dbo.GiaoDichThanhToan SET MaThaoTac=CONCAT('MIGRATION-GD-',MaGiaoDichNoiBo) WHERE MaThaoTac IS NULL;
IF EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan WHERE MaLenh IS NULL)
    THROW 51010,N'Có giao dịch cũ chưa xác định được lệnh; cần đối chiếu trước khi nâng cấp.',1;
ALTER TABLE dbo.GiaoDichThanhToan ALTER COLUMN MaLenh VARCHAR(40) NOT NULL;
ALTER TABLE dbo.GiaoDichThanhToan ALTER COLUMN MaThaoTac VARCHAR(100) NOT NULL;
IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.GiaoDichThanhToan')
    AND name='MaHoaDon' AND is_nullable=0)
    ALTER TABLE dbo.GiaoDichThanhToan ALTER COLUMN MaHoaDon VARCHAR(40) NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.GiaoDichThanhToan') AND name='UX_ThanhToan_ThaoTac')
    CREATE UNIQUE INDEX UX_ThanhToan_ThaoTac ON dbo.GiaoDichThanhToan(MaThaoTac);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.PhieuNhapKho') AND name='UX_NhapKho_ThaoTac')
    CREATE UNIQUE INDEX UX_NhapKho_ThaoTac ON dbo.PhieuNhapKho(MaThaoTac) WHERE MaThaoTac IS NOT NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.PhieuDieuChinhKho') AND name='UX_DieuChinhKho_ThaoTac')
    CREATE UNIQUE INDEX UX_DieuChinhKho_ThaoTac ON dbo.PhieuDieuChinhKho(MaThaoTac) WHERE MaThaoTac IS NOT NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.BaoCao') AND name='UX_BaoCao_ThaoTac')
    CREATE UNIQUE INDEX UX_BaoCao_ThaoTac ON dbo.BaoCao(MaThaoTac) WHERE MaThaoTac IS NOT NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.PhanHoiPhatSinh') AND name='UX_PhanHoi_QuyetDinh')
    CREATE UNIQUE INDEX UX_PhanHoi_QuyetDinh ON dbo.PhanHoiPhatSinh(MaPhatSinh);
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_GiaoDich_DieuChinh')
    ALTER TABLE dbo.GiaoDichThanhToan WITH CHECK ADD CONSTRAINT FK_GiaoDich_DieuChinh
        FOREIGN KEY(MaDieuChinh) REFERENCES dbo.DieuChinhHoaDon(MaDieuChinh);
IF NOT EXISTS(SELECT 1 FROM sys.key_constraints WHERE name='UQ_PheDuyet_Lenh')
    ALTER TABLE dbo.PheDuyetBanGiaoNgoaiLe ADD CONSTRAINT UQ_PheDuyet_Lenh UNIQUE(MaPheDuyet,MaLenh);
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_BanGiao_PheDuyetDungLenh')
    ALTER TABLE dbo.BanGiaoXe WITH CHECK ADD CONSTRAINT FK_BanGiao_PheDuyetDungLenh
        FOREIGN KEY(MaPheDuyetNgoaiLe,MaLenh) REFERENCES dbo.PheDuyetBanGiaoNgoaiLe(MaPheDuyet,MaLenh);
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name='CK_BanGiao_CanCu')
    ALTER TABLE dbo.BanGiaoXe WITH CHECK ADD CONSTRAINT CK_BanGiao_CanCu
        CHECK(MaHoaDon IS NOT NULL OR MaPheDuyetNgoaiLe IS NOT NULL);
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name='FK_ChiTietHoaDon_PhatSinh')
    ALTER TABLE dbo.ChiTietHoaDon WITH CHECK ADD CONSTRAINT FK_ChiTietHoaDon_PhatSinh
        FOREIGN KEY(MaPhatSinh) REFERENCES dbo.DeXuatPhatSinh(MaPhatSinh);
GO
-- Tạo index có điều kiện để chạy lại không trùng.
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.HoaDon') AND name='UX_HoaDon_MaThaoTac')
    CREATE UNIQUE INDEX UX_HoaDon_MaThaoTac ON dbo.HoaDon(MaThaoTac) WHERE MaThaoTac IS NOT NULL;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.GiaoDichThanhToan') AND name='UX_GiaoDich_ThamChieu')
    CREATE UNIQUE INDEX UX_GiaoDich_ThamChieu ON dbo.GiaoDichThanhToan(MaThamChieu)
    WHERE HinhThuc='CHUYEN_KHOAN' AND LoaiGiaoDich='THU';
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.PhuTungSuDung') AND name='IX_SuDung_LenhHangMuc')
    CREATE INDEX IX_SuDung_LenhHangMuc ON dbo.PhuTungSuDung(MaLenhSuaChua,ViTriHangMuc,MaPhuTung);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.BienDongKho') AND name='IX_BienDong_ChungTu')
    CREATE INDEX IX_BienDong_ChungTu ON dbo.BienDongKho(ChungTu,MaPhuTung,Loai);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.DeXuatPhatSinh') AND name='IX_PhatSinh_Lenh')
    CREATE INDEX IX_PhatSinh_Lenh ON dbo.DeXuatPhatSinh(MaPhieu,TrangThai);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.GiaoDichThanhToan') AND name='IX_ThanhToan_Lenh')
    CREATE INDEX IX_ThanhToan_Lenh ON dbo.GiaoDichThanhToan(MaLenh,TrangThai);
GO

-- PHẦN 2: NGUỒN ĐỌC NGHIỆP VỤ THỐNG NHẤT.
    -- Chỉ mục tìm kiếm
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.KhachHang') AND name='IX_KhachHang_SoDienThoai')
    CREATE INDEX IX_KhachHang_SoDienThoai ON dbo.KhachHang(SoDienThoai);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.Xe') AND name='IX_Xe_MaKH')
    CREATE INDEX IX_Xe_MaKH ON dbo.Xe(MaKH);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PhieuTiepNhan') AND name='IX_PhieuTiepNhan_MaXe')
    CREATE INDEX IX_PhieuTiepNhan_MaXe ON dbo.PhieuTiepNhan(MaXe, ThoiGianNhan);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PhieuSuaChua') AND name='IX_PhieuSuaChua_KyThuatChinh')
    CREATE INDEX IX_PhieuSuaChua_KyThuatChinh ON dbo.PhieuSuaChua(KyThuatChinh, TrangThai);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PhieuSuaChua') AND name='IX_PhieuSuaChua_KyThuatPhoiHop')
    CREATE INDEX IX_PhieuSuaChua_KyThuatPhoiHop ON dbo.PhieuSuaChua(KyThuatPhoiHop, TrangThai);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.BienDongKho') AND name='IX_BienDongKho_MaPhuTung')
    CREATE INDEX IX_BienDongKho_MaPhuTung ON dbo.BienDongKho(MaPhuTung, ThoiDiem);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.HoaDon') AND name='IX_HoaDon_ThoiDiem')
    CREATE INDEX IX_HoaDon_ThoiDiem ON dbo.HoaDon(ThoiDiemThanhToan);


GO

-- Nguồn thời gian dùng thống nhất cho các procedure mới.
-- DEFAULT tham chiếu hàm tạo dependency; chỉ tạo hàm ổn định này nếu chưa có.
-- Không ALTER hàm trên mỗi lần chạy lại khi các DEFAULT đã tham chiếu nó.
IF OBJECT_ID(N'dbo.fn_ThoiDiemGarage',N'FN') IS NULL
    EXEC(N'CREATE FUNCTION dbo.fn_ThoiDiemGarage()
        RETURNS DATETIME2(3) AS BEGIN
            RETURN CONVERT(DATETIME2(3),SYSDATETIMEOFFSET() AT TIME ZONE ''SE Asia Standard Time'');
        END;');
GO
-- Tổng đã hoàn cho từng lần xuất. Giữ lần xuất gốc, chỉ trừ phiếu hoàn đã xác nhận.
-- Đổi default thời gian của các bảng thuộc project sang giờ nghiệp vụ Việt Nam.
-- Không viết lại timestamp đã có trong lịch sử.
DECLARE @MacDinh NVARCHAR(MAX)=N'';
SELECT @MacDinh=@MacDinh+N'ALTER TABLE dbo.'+QUOTENAME(t.name)+N' DROP CONSTRAINT '+QUOTENAME(d.name)+N';'
    +N'ALTER TABLE dbo.'+QUOTENAME(t.name)+N' ADD DEFAULT (dbo.fn_ThoiDiemGarage()) FOR '+QUOTENAME(c.name)+N';'
FROM sys.default_constraints d JOIN sys.tables t ON t.object_id=d.parent_object_id
JOIN sys.columns c ON c.object_id=t.object_id AND c.column_id=d.parent_column_id
WHERE SCHEMA_NAME(t.schema_id)='dbo' AND d.definition LIKE '%sysdatetime()%'
  AND t.name IN('TaiKhoan','NhatKyTaiKhoan','TaiKhoanThongTinBaoMat','ChiTietKiemTra','DeXuatPhatSinh',
    'PhanHoiPhatSinh','DoiChieuChiPhi','GiaoDichThanhToan','PheDuyetBanGiaoNgoaiLe','PhieuDieuChinhKho',
    'CauHinhGiaVon','NhatKyNghiepVu','LichSuBaoCaoKiemTra','DieuChinhHoaDon');
EXEC sys.sp_executesql @MacDinh;
GO
CREATE OR ALTER VIEW dbo.vw_PhuTungThucDung
AS
WITH Hoan AS (
    SELECT b.ChungTu AS MaGhiNhan,d.MaPhuTung,SUM(d.SoLuong) AS DaHoan
    FROM dbo.ChiTietDieuChinhKho d
    JOIN dbo.PhieuDieuChinhKho h ON h.MaPhieuDieuChinh=d.MaPhieuDieuChinh
    JOIN dbo.BienDongKho b ON b.MaBienDong=d.MaBienDongGoc AND b.MaPhuTung=d.MaPhuTung
    WHERE h.TrangThai='DA_GHI_NHAN' AND d.Chieu='NHAP' AND b.Loai=N'Xuất'
    GROUP BY b.ChungTu,d.MaPhuTung
)
SELECT s.MaGhiNhan,s.MaLenhSuaChua,s.ViTriHangMuc,s.MaPhuTung,
       s.SoLuong AS DaXuat,COALESCE(h.DaHoan,0) AS DaHoan,
       s.SoLuong-COALESCE(h.DaHoan,0) AS ThucDung,s.NguoiThucHien
FROM dbo.PhuTungSuDung s
LEFT JOIN Hoan h ON h.MaGhiNhan=s.MaGhiNhan AND h.MaPhuTung=s.MaPhuTung
WHERE s.TrangThai='DA_GHI_NHAN';
GO

-- Căn cứ giá được khách duyệt, gồm báo giá gốc và phụ lục phát sinh.
-- Phụ lục không sửa báo giá gốc; liên kết chính xác tới hạng mục lệnh.
CREATE OR ALTER VIEW dbo.vw_HangMucDuocDuyet
AS
SELECT sc.MaPhieu,ct.ViTri AS ViTriSuaChua,ct.MaBaoGia,ct.ViTri AS ViTriBaoGia,
       CONVERT(BIGINT,NULL) AS MaPhatSinh,CONVERT(INT,NULL) AS ViTriDeXuat,
       ct.MaDichVu,ct.MaPhuTung,ct.TenHangMuc,ct.Loai,ct.DonVi,
       ct.SoLuong AS SoLuongDuocDuyet,ct.DonGia
FROM dbo.PhieuSuaChua sc
JOIN dbo.BaoGia bg ON bg.MaBaoGia=sc.MaBaoGia AND bg.TrangThai='DA_DONG_Y'
JOIN dbo.ChiTietBaoGia ct ON ct.MaBaoGia=bg.MaBaoGia
UNION ALL
SELECT l.MaPhieu,l.ViTriSuaChua,CONVERT(VARCHAR(50),NULL),CONVERT(INT,NULL),
       d.MaPhatSinh,ct.ViTri,ct.MaDichVu,ct.MaPhuTung,ct.TenHangMuc,ct.Loai,ct.DonVi,ct.SoLuong,ct.DonGia
FROM dbo.ChiTietSuaChuaPhatSinh l
JOIN dbo.DeXuatPhatSinh d ON d.MaPhatSinh=l.MaPhatSinh AND d.MaPhieu=l.MaPhieu AND d.TrangThai='DA_DONG_Y'
JOIN dbo.ChiTietDeXuatPhatSinh ct ON ct.MaPhatSinh=l.MaPhatSinh AND ct.ViTri=l.ViTriDeXuat;
GO

CREATE OR ALTER VIEW dbo.vw_ChiPhiThucTe
AS
WITH SuDung AS (
    SELECT MaLenhSuaChua,ViTriHangMuc,MaPhuTung,SUM(ThucDung) AS SoLuong
    FROM dbo.vw_PhuTungThucDung
    GROUP BY MaLenhSuaChua,ViTriHangMuc,MaPhuTung
)
SELECT h.*,ct.TrangThai AS TrangThaiCongViec,ct.KetQua,
       CONVERT(DECIMAL(18,3),CASE WHEN h.Loai=N'Phụ tùng' THEN COALESCE(s.SoLuong,0)
                    WHEN ct.TrangThai='HOAN_THANH' THEN h.SoLuongDuocDuyet ELSE 0 END) AS SoLuongTinhTien,
       CONVERT(BIGINT,ROUND(CONVERT(DECIMAL(38,3),h.DonGia)*
                CASE WHEN h.Loai=N'Phụ tùng' THEN COALESCE(s.SoLuong,0)
                     WHEN ct.TrangThai='HOAN_THANH' THEN h.SoLuongDuocDuyet ELSE 0 END,0)) AS ThanhTien
FROM dbo.vw_HangMucDuocDuyet h
JOIN dbo.ChiTietSuaChua ct ON ct.MaPhieu=h.MaPhieu AND ct.ViTri=h.ViTriSuaChua
LEFT JOIN SuDung s ON s.MaLenhSuaChua=h.MaPhieu AND s.ViTriHangMuc=h.ViTriSuaChua AND s.MaPhuTung=h.MaPhuTung;
GO

-- Thuế/giảm giá mặc định bằng 0; chỉ áp dụng khoản đã được đối chiếu xác nhận.
CREATE OR ALTER VIEW dbo.vw_TongPhaiThu
AS
WITH Tong AS (
    SELECT MaPhieu,SUM(CONVERT(DECIMAL(38,0),ThanhTien)) AS TienHangCong
    FROM dbo.vw_ChiPhiThucTe GROUP BY MaPhieu
)
SELECT sc.MaPhieu,
       CONVERT(BIGINT,COALESCE(t.TienHangCong,0)) AS TienHangCong,
       COALESCE(dc.TienGiam,0) AS TienGiam,COALESCE(dc.TienThue,0) AS TienThue,
       CONVERT(BIGINT,COALESCE(t.TienHangCong,0)-COALESCE(dc.TienGiam,0)+COALESCE(dc.TienThue,0)) AS TongPhaiThu
FROM dbo.PhieuSuaChua sc
JOIN Tong t ON t.MaPhieu=sc.MaPhieu
LEFT JOIN dbo.DoiChieuChiPhi dc ON dc.MaPhieu=sc.MaPhieu AND dc.TrangThai='DA_XAC_NHAN'
WHERE NOT EXISTS(SELECT 1 FROM dbo.ChiTietBaoGia b WHERE b.MaBaoGia=sc.MaBaoGia
    AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=sc.MaPhieu AND c.ViTri=b.ViTri))
  AND NOT EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh d JOIN dbo.ChiTietDeXuatPhatSinh c
    ON c.MaPhatSinh=d.MaPhatSinh WHERE d.MaPhieu=sc.MaPhieu AND d.TrangThai='DA_DONG_Y'
    AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietSuaChuaPhatSinh l
        WHERE l.MaPhieu=sc.MaPhieu AND l.MaPhatSinh=d.MaPhatSinh AND l.ViTriDeXuat=c.ViTri));
GO

CREATE OR ALTER VIEW dbo.vw_SoTienDaXacMinh
AS
SELECT MaLenh,SUM(CONVERT(DECIMAL(38,0),CASE
         WHEN LoaiGiaoDich='THU' AND TrangThai='DA_XAC_MINH' THEN SoTien
         WHEN LoaiGiaoDich='HOAN_TIEN' AND TrangThai='DA_HOAN' THEN -SoTien ELSE 0 END)) AS DaThu
FROM dbo.GiaoDichThanhToan GROUP BY MaLenh;
GO

-- Nghĩa vụ hiện hành: trước hóa đơn lấy đối chiếu/thực tế; sau hóa đơn lấy snapshot cộng điều chỉnh được duyệt.
CREATE OR ALTER VIEW dbo.vw_NghiaVuThanhToan
AS
SELECT sc.MaPhieu,
       CONVERT(BIGINT,CASE WHEN hd.MaHoaDon IS NULL THEN CONVERT(DECIMAL(38,0),t.TongPhaiThu)
            ELSE CONVERT(DECIMAL(38,0),hd.TongTien)+a.DieuChinh END) AS TongPhaiThu
FROM dbo.PhieuSuaChua sc
JOIN dbo.vw_TongPhaiThu t ON t.MaPhieu=sc.MaPhieu
LEFT JOIN dbo.HoaDon hd ON hd.MaPhieuSuaChua=sc.MaPhieu
OUTER APPLY(SELECT COALESCE(SUM(CONVERT(DECIMAL(38,0),d.SoTienDieuChinh)),0) AS DieuChinh
            FROM dbo.DieuChinhHoaDon d WHERE d.MaHoaDon=hd.MaHoaDon AND d.TrangThai='DA_DUYET') a;
GO

CREATE OR ALTER VIEW dbo.vw_PhieuChoThu
AS
SELECT sc.MaPhieu AS MaLenh,tn.TenKhach AS TenKhachHang,sc.BienSo,t.TongPhaiThu AS SoTienPhaiThu
FROM dbo.PhieuSuaChua sc
JOIN dbo.BaoGia bg ON bg.MaBaoGia=sc.MaBaoGia
JOIN dbo.NhomBaoGia n ON n.MaBaoGiaGoc=bg.MaBaoGiaGoc
JOIN dbo.BaoCaoKiemTra kt ON kt.MaBaoCao=n.MaBaoCao
JOIN dbo.PhieuTiepNhan tn ON tn.MaPhieu=kt.MaPhieuTiepNhan
JOIN dbo.vw_TongPhaiThu t ON t.MaPhieu=sc.MaPhieu
WHERE sc.TrangThai='HOAN_THANH' AND sc.DaThanhToan=0
  AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon hd WHERE hd.MaPhieuSuaChua=sc.MaPhieu)
  AND NOT EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh d WHERE d.MaPhieu=sc.MaPhieu AND d.TrangThai='CHO_KHACH');
GO

-- View tương thích: hóa đơn cũ là chứng từ đã thu đủ một lần.
-- Có dữ liệu giao dịch mới thì dùng số thực nhận, không mặc định bằng tổng hóa đơn.
CREATE OR ALTER VIEW dbo.vw_CanCuBanGiao
AS
SELECT hd.MaPhieuSuaChua AS MaLenh,hd.MaHoaDon AS MaChungTu,
       CONVERT(BIGINT,CONVERT(DECIMAL(38,0),hd.TongTien)+a.DieuChinh) AS PhaiThu,
       CONVERT(BIGINT,CASE WHEN t.MaLenh IS NULL THEN hd.TongTien ELSE t.DaThu END) AS DaThu,
       COALESCE(g.NguoiXacMinh,hd.NguoiThucHien) AS NguoiXacNhan,
       COALESCE(g.ThoiDiemXacMinh,hd.ThoiDiemThanhToan) AS ThoiDiem,
       CONVERT(BIT,0) AS NgoaiLe,CONVERT(NVARCHAR(2000),N'') AS LyDo
FROM dbo.HoaDon hd
JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu=hd.MaPhieuSuaChua AND sc.DaThanhToan=1
LEFT JOIN dbo.vw_SoTienDaXacMinh t ON t.MaLenh=sc.MaPhieu
CROSS APPLY(SELECT COALESCE(SUM(CONVERT(DECIMAL(38,0),SoTienDieuChinh)),0) AS DieuChinh
            FROM dbo.DieuChinhHoaDon d WHERE d.MaHoaDon=hd.MaHoaDon AND d.TrangThai='DA_DUYET') a
OUTER APPLY(SELECT TOP(1) NguoiXacMinh,ThoiDiemXacMinh FROM dbo.GiaoDichThanhToan x
            WHERE x.MaLenh=sc.MaPhieu AND x.TrangThai IN('DA_XAC_MINH','DA_HOAN')
            ORDER BY x.ThoiDiemXacMinh DESC,x.MaGiaoDichNoiBo DESC) g
WHERE NOT EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan g WHERE g.MaLenh=sc.MaPhieu)
   OR t.DaThu>=CONVERT(DECIMAL(38,0),hd.TongTien)+a.DieuChinh;
GO

CREATE OR ALTER VIEW dbo.vw_CanCuBanGiaoMoRong
AS
SELECT * FROM dbo.vw_CanCuBanGiao
UNION ALL
SELECT p.MaLenh,CONVERT(VARCHAR(40),CONCAT('NGOAI_LE-',p.MaPheDuyet)),
       t.TongPhaiThu,CONVERT(BIGINT,COALESCE(s.DaThu,0)),p.NguoiPheDuyet,p.ThoiDiemPheDuyet,
       CONVERT(BIT,1),p.LyDo
FROM dbo.PheDuyetBanGiaoNgoaiLe p
JOIN dbo.vw_NghiaVuThanhToan t ON t.MaPhieu=p.MaLenh
JOIN dbo.TaiKhoan tk ON tk.MaNhanVien=p.NguoiPheDuyet AND tk.VaiTro='QUAN_LY'
LEFT JOIN dbo.vw_SoTienDaXacMinh s ON s.MaLenh=p.MaLenh
WHERE p.TrangThai='DA_DUYET' AND (p.ThoiHan IS NULL OR p.ThoiHan>=dbo.fn_ThoiDiemGarage())
  AND p.SoTienConNo=t.TongPhaiThu-COALESCE(s.DaThu,0)
  AND NOT EXISTS(SELECT 1 FROM dbo.vw_CanCuBanGiao c WHERE c.MaLenh=p.MaLenh)
  AND NOT EXISTS(SELECT 1 FROM dbo.PheDuyetBanGiaoNgoaiLe m
                 WHERE m.MaLenh=p.MaLenh AND m.TrangThai='DA_DUYET' AND m.MaPheDuyet>p.MaPheDuyet);
GO
CREATE OR ALTER VIEW dbo.vw_TonKho
AS
SELECT MaPhuTung,TenPhuTung,DonViTinh,SoLuongTon,MucToiThieu,
       CASE WHEN SoLuongTon<=MucToiThieu THEN 1 ELSE 0 END AS CanhBao
FROM dbo.PhuTung;
GO
CREATE OR ALTER VIEW dbo.vw_DoanhThuDaThu
AS
SELECT hd.MaHoaDon,hd.MaPhieuSuaChua,hd.ThoiDiemThanhToan,hd.TongTien,
       CONVERT(BIGINT,COALESCE((SELECT SUM(CONVERT(DECIMAL(38,0),SoTienDieuChinh))
          FROM dbo.DieuChinhHoaDon d WHERE d.MaHoaDon=hd.MaHoaDon AND d.TrangThai='DA_DUYET'),0)) AS DieuChinhSauHoaDon,
       CONVERT(BIGINT,COALESCE(s.DaThu,hd.TongTien)) AS TienThucNhanConLai
FROM dbo.HoaDon hd LEFT JOIN dbo.vw_SoTienDaXacMinh s ON s.MaLenh=hd.MaPhieuSuaChua;
GO

-- PHẦN 3: API GHI DỮ LIỆU CHO DAO MỚI.
-- Các procedure dưới đây là API SQL cho DAO mới.
-- Vai trò/phiên đăng nhập vẫn phải được xác minh ở tầng ứng dụng; tham số mã NV không thay thế xác thực.

CREATE OR ALTER PROCEDURE dbo.usp_CapMaChungTu @TienTo VARCHAR(8),@Ma VARCHAR(40) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    IF @TienTo NOT IN('HD','GX','NK','BC','DC') THROW 51100,N'Tiền tố chứng từ không hợp lệ.',1;
    DECLARE @So BIGINT=NEXT VALUE FOR dbo.SoMaChungTu;
    SET @Ma=CONCAT(@TienTo,'-',YEAR(dbo.fn_ThoiDiemGarage()),'-',@So);
END;
GO

-- Header/detail nhập và tồn/audit trong cùng transaction. Không có trigger tự cộng tồn
-- để không cộng lần hai khi DAO cũ đang tự cập nhật tồn.
CREATE OR ALTER PROCEDURE dbo.usp_NhapKho
    @NguoiNhap VARCHAR(20),@NguonNhap NVARCHAR(1000),@ChungTuNguon NVARCHAR(100),
    @ChiTiet NVARCHAR(MAX),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiNhap AND VaiTro='QUAN_LY' AND HoatDong=1)
        THROW 51101,N'Chỉ quản lý đang hoạt động được nhập kho.',1;
    IF NULLIF(LTRIM(RTRIM(@NguonNhap)),N'') IS NULL OR NULLIF(@MaThaoTac,'') IS NULL OR ISJSON(@ChiTiet)<>1
        THROW 51102,N'Nguồn nhập, mã thao tác và JSON chi tiết là bắt buộc.',1;
    DECLARE @Dong TABLE(ViTri INT PRIMARY KEY,MaPhuTung VARCHAR(30),SoLuong DECIMAL(18,3),DonGia BIGINT);
    INSERT @Dong SELECT CONVERT(INT,j.[key]),d.MaPhuTung,d.SoLuong,d.DonGia
    FROM OPENJSON(@ChiTiet) j CROSS APPLY OPENJSON(j.value) WITH(
        MaPhuTung VARCHAR(30),SoLuong DECIMAL(18,3),DonGia BIGINT) d;
    IF NOT EXISTS(SELECT 1 FROM @Dong) OR EXISTS(
        SELECT 1 FROM @Dong d LEFT JOIN dbo.PhuTung p ON p.MaPhuTung=d.MaPhuTung
        LEFT JOIN dbo.DonViTinh u ON u.DonVi=p.DonViTinh
        WHERE p.MaPhuTung IS NULL OR p.HoatDong=0 OR d.SoLuong IS NULL OR d.SoLuong<=0
          OR d.DonGia IS NULL OR d.DonGia<0 OR (u.ChoPhepSoLe=0 AND d.SoLuong%1<>0))
        THROW 51103,N'Dòng nhập không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Ma VARCHAR(40);
        SELECT @Ma=MaPhieu FROM dbo.PhieuNhapKho WITH(UPDLOCK,HOLDLOCK) WHERE MaThaoTac=@MaThaoTac;
        IF @Ma IS NOT NULL
        BEGIN
            IF EXISTS(SELECT 1 FROM dbo.PhieuNhapKho WHERE MaPhieu=@Ma AND
                (NguoiNhap<>@NguoiNhap OR NguonNhap<>@NguonNhap OR ChungTuNguon<>COALESCE(@ChungTuNguon,N'')))
               OR EXISTS(SELECT ViTri,MaPhuTung,SoLuong,DonGia FROM @Dong
                         EXCEPT SELECT ViTri,MaPhuTung,SoLuong,DonGia FROM dbo.ChiTietNhapKho WHERE MaPhieu=@Ma)
               OR EXISTS(SELECT ViTri,MaPhuTung,SoLuong,DonGia FROM dbo.ChiTietNhapKho WHERE MaPhieu=@Ma
                         EXCEPT SELECT ViTri,MaPhuTung,SoLuong,DonGia FROM @Dong)
                THROW 51104,N'Mã thao tác đã được dùng cho nội dung nhập khác.',1;
            COMMIT; SELECT @Ma AS MaPhieu; RETURN;
        END;
        EXEC dbo.usp_CapMaChungTu 'NK',@Ma OUTPUT;
        DECLARE @Luc DATETIME2(3)=dbo.fn_ThoiDiemGarage();
        INSERT dbo.PhieuNhapKho(MaPhieu,NgayNhap,NguonNhap,ChungTuNguon,NguoiNhap,MaThaoTac)
        VALUES(@Ma,@Luc,@NguonNhap,COALESCE(@ChungTuNguon,N''),@NguoiNhap,@MaThaoTac);
        INSERT dbo.ChiTietNhapKho(MaPhieu,ViTri,MaPhuTung,SoLuong,DonGia)
        SELECT @Ma,ViTri,MaPhuTung,SoLuong,DonGia FROM @Dong;
        DECLARE @BienDong TABLE(MaPhuTung VARCHAR(30),TonTruoc DECIMAL(18,3),TonSau DECIMAL(18,3));
        UPDATE p WITH(UPDLOCK,HOLDLOCK) SET SoLuongTon=p.SoLuongTon+d.So
        OUTPUT inserted.MaPhuTung,deleted.SoLuongTon,inserted.SoLuongTon INTO @BienDong
        FROM dbo.PhuTung p JOIN(SELECT MaPhuTung,SUM(SoLuong) AS So FROM @Dong GROUP BY MaPhuTung) d
            ON d.MaPhuTung=p.MaPhuTung;
        INSERT dbo.BienDongKho(MaPhuTung,ThoiDiem,Loai,TonTruoc,TonSau,ChungTu,NguoiThucHien,LyDo)
        SELECT MaPhuTung,@Luc,N'Nhập',TonTruoc,TonSau,@Ma,@NguoiNhap,@NguonNhap FROM @BienDong;
        COMMIT; SELECT @Ma AS MaPhieu;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_SuaBaoGiaNhap
    @MaBaoGia VARCHAR(50),@NguoiSua VARCHAR(20),@BanDaDoc VARBINARY(8),
    @NgayHen DATETIME2(3),@ChiTiet NVARCHAR(MAX)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiSua AND VaiTro='NHAN_VIEN_TIEP_NHAN' AND HoatDong=1)
        OR @BanDaDoc IS NULL OR @NgayHen IS NULL OR @NgayHen<dbo.fn_ThoiDiemGarage() OR ISJSON(@ChiTiet)<>1
        THROW 51105,N'Quyền, phiên bản đã đọc, ngày hẹn hoặc chi tiết không hợp lệ.',1;
    DECLARE @Dong TABLE(ViTri INT PRIMARY KEY,MaDichVu VARCHAR(30),MaPhuTung VARCHAR(30),
        TenHangMuc NVARCHAR(200),Loai NVARCHAR(20),DonVi NVARCHAR(30),SoLuong DECIMAL(18,3),DonGia BIGINT);
    INSERT @Dong SELECT CONVERT(INT,j.[key]),x.MaDichVu,x.MaPhuTung,x.TenHangMuc,x.Loai,x.DonVi,x.SoLuong,x.DonGia
    FROM OPENJSON(@ChiTiet) j CROSS APPLY OPENJSON(j.value) WITH(
        MaDichVu VARCHAR(30),MaPhuTung VARCHAR(30),TenHangMuc NVARCHAR(200),Loai NVARCHAR(20),
        DonVi NVARCHAR(30),SoLuong DECIMAL(18,3),DonGia BIGINT) x;
    IF NOT EXISTS(SELECT 1 FROM @Dong) OR EXISTS(
        SELECT 1 FROM @Dong d LEFT JOIN dbo.DichVu v ON v.MaDichVu=d.MaDichVu
        LEFT JOIN dbo.PhuTung p ON p.MaPhuTung=d.MaPhuTung LEFT JOIN dbo.DonViTinh u ON u.DonVi=p.DonViTinh
        WHERE d.Loai IS NULL OR d.Loai NOT IN(N'Dịch vụ',N'Phụ tùng') OR d.SoLuong IS NULL OR d.SoLuong<=0
           OR d.DonGia IS NULL OR d.DonGia<0 OR NULLIF(d.TenHangMuc,N'') IS NULL OR NULLIF(d.DonVi,N'') IS NULL
           OR (d.Loai=N'Dịch vụ' AND (v.MaDichVu IS NULL OR v.DangApDung=0 OR d.MaPhuTung IS NOT NULL
                OR d.TenHangMuc<>v.TenDichVu OR d.DonVi<>v.DonViTinh))
           OR (d.Loai=N'Phụ tùng' AND (p.MaPhuTung IS NULL OR p.HoatDong=0 OR d.MaDichVu IS NOT NULL
                OR d.TenHangMuc<>p.TenPhuTung OR d.DonVi<>p.DonViTinh OR (u.ChoPhepSoLe=0 AND d.SoLuong%1<>0))))
        THROW 51106,N'Dòng giá không khớp danh mục hoặc số lượng không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        IF NOT EXISTS(SELECT 1 FROM dbo.BaoGia WITH(UPDLOCK,HOLDLOCK)
            WHERE MaBaoGia=@MaBaoGia AND TrangThai='NHAP' AND PhienBanKhoa=@BanDaDoc)
            THROW 51107,N'Báo giá đã thay đổi/đã gửi; tải lại bản mới.',1;
        UPDATE dbo.BaoGia SET NgayHen=@NgayHen WHERE MaBaoGia=@MaBaoGia;
        DELETE dbo.ChiTietBaoGia WHERE MaBaoGia=@MaBaoGia;
        INSERT dbo.ChiTietBaoGia(MaBaoGia,ViTri,MaDichVu,MaPhuTung,TenHangMuc,Loai,DonVi,SoLuong,DonGia)
        SELECT @MaBaoGia,ViTri,MaDichVu,MaPhuTung,TenHangMuc,Loai,DonVi,SoLuong,DonGia FROM @Dong;
        INSERT dbo.NhatKyNghiepVu(Bang,KhoaChungTu,HanhDong,NguoiThucHien,ThoiDiem,NoiDung)
        VALUES(N'BaoGia',@MaBaoGia,'SUA_NHAP',@NguoiSua,dbo.fn_ThoiDiemGarage(),@ChiTiet);
        COMMIT; SELECT MaBaoGia,PhienBanKhoa FROM dbo.BaoGia WHERE MaBaoGia=@MaBaoGia;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO


CREATE OR ALTER PROCEDURE dbo.usp_XacNhanDoiChieu
    @MaLenh VARCHAR(40),@NguoiXacNhan VARCHAR(20),
    @TienGiam BIGINT=0,@TienThue BIGINT=0,@LyDo NVARCHAR(2000)=N''
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiXacNhan AND VaiTro='THU_NGAN' AND HoatDong=1)
        THROW 51110,N'Chỉ thu ngân đang hoạt động được đối chiếu.',1;
    IF @TienGiam IS NULL OR @TienThue IS NULL OR @TienGiam<0 OR @TienThue<0
        OR ((@TienGiam>0 OR @TienThue>0) AND NULLIF(LTRIM(RTRIM(@LyDo)),N'') IS NULL)
        THROW 51111,N'Thuế/giảm giá không hợp lệ hoặc thiếu lý do/căn cứ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Bg VARCHAR(50);
        SELECT @Bg=MaBaoGia FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK)
        WHERE MaPhieu=@MaLenh AND TrangThai='HOAN_THANH' AND DaThanhToan=0;
        IF @Bg IS NULL OR EXISTS(SELECT 1 FROM dbo.HoaDon WHERE MaPhieuSuaChua=@MaLenh)
            THROW 51112,N'Lệnh chưa hoàn thành hoặc đã có hóa đơn.',1;
        IF EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh WHERE MaPhieu=@MaLenh AND TrangThai='CHO_KHACH')
           OR EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE MaPhieu=@MaLenh AND
                (TrangThai<>'HOAN_THANH' OR NULLIF(LTRIM(RTRIM(KetQua)),N'') IS NULL))
           OR EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=@MaLenh
                AND NOT EXISTS(SELECT 1 FROM dbo.vw_HangMucDuocDuyet h WHERE h.MaPhieu=c.MaPhieu AND h.ViTriSuaChua=c.ViTri))
           OR NOT EXISTS(SELECT 1 FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=@MaLenh)
           OR NOT EXISTS(SELECT 1 FROM dbo.vw_TongPhaiThu WHERE MaPhieu=@MaLenh)
           OR EXISTS(SELECT 1 FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=@MaLenh
                AND (SoLuongTinhTien<0 OR SoLuongTinhTien>SoLuongDuocDuyet))
            THROW 51113,N'Công việc, căn cứ duyệt hoặc số lượng chưa đủ để đối chiếu.',1;
        DECLARE @Cong BIGINT,@PhuTung BIGINT,@PhatSinh BIGINT,@Tong BIGINT;
        SELECT @Cong=COALESCE(SUM(CASE WHEN MaPhatSinh IS NULL AND Loai=N'Dịch vụ' THEN ThanhTien ELSE 0 END),0),
               @PhuTung=COALESCE(SUM(CASE WHEN MaPhatSinh IS NULL AND Loai=N'Phụ tùng' THEN ThanhTien ELSE 0 END),0),
               @PhatSinh=COALESCE(SUM(CASE WHEN MaPhatSinh IS NOT NULL THEN ThanhTien ELSE 0 END),0)
        FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=@MaLenh;
        SET @Tong=CONVERT(DECIMAL(38,0),@Cong)+@PhuTung+@PhatSinh-@TienGiam+@TienThue;
        IF @Tong<0 THROW 51114,N'Giảm giá vượt tiền hàng và công.',1;
        DELETE dbo.ChiTietDoiChieuChiPhi WHERE MaPhieu=@MaLenh;
        DELETE dbo.DoiChieuChiPhi WHERE MaPhieu=@MaLenh;
        INSERT dbo.DoiChieuChiPhi(MaPhieu,MaBaoGia,TrangThai,TongTienCong,TongTienPhuTung,TongTienPhatSinh,
            TienGiam,TienThue,TongPhaiThu,NguoiLap,ThoiDiemLap,NguoiXacNhan,ThoiDiemXacNhan,LyDoDieuChinh)
        VALUES(@MaLenh,@Bg,'DA_XAC_NHAN',@Cong,@PhuTung,@PhatSinh,@TienGiam,@TienThue,@Tong,
            @NguoiXacNhan,dbo.fn_ThoiDiemGarage(),@NguoiXacNhan,dbo.fn_ThoiDiemGarage(),@LyDo);
        INSERT dbo.ChiTietDoiChieuChiPhi(MaPhieu,ViTri,LoaiChiPhi,TenHangMuc,DonVi,SoLuong,DonGia,
            MaBaoGia,ViTriBaoGia,MaPhatSinh)
        SELECT MaPhieu,ViTriSuaChua,CASE WHEN MaPhatSinh IS NOT NULL THEN 'PHAT_SINH'
            WHEN Loai=N'Phụ tùng' THEN 'PHU_TUNG' ELSE 'CONG' END,TenHangMuc,DonVi,SoLuongTinhTien,
            DonGia,MaBaoGia,ViTriBaoGia,MaPhatSinh
        FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=@MaLenh AND SoLuongTinhTien>0;
        COMMIT; SELECT * FROM dbo.DoiChieuChiPhi WHERE MaPhieu=@MaLenh;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

-- Thu nhiều lần/đặt cọc: ghi chờ xác minh, không tự đánh dấu đã nhận tiền.
CREATE OR ALTER PROCEDURE dbo.usp_GhiNhanThanhToan
    @MaLenh VARCHAR(40),@NguoiThu VARCHAR(20),@HinhThuc VARCHAR(20),@SoTien BIGINT,
    @MaThamChieu NVARCHAR(100),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiThu AND VaiTro='THU_NGAN' AND HoatDong=1)
        THROW 51120,N'Chỉ thu ngân đang hoạt động được ghi nhận thu.',1;
    IF @SoTien IS NULL OR @SoTien<=0 OR @HinhThuc NOT IN('TIEN_MAT','CHUYEN_KHOAN')
        OR @HinhThuc IS NULL OR NULLIF(@MaThaoTac,'') IS NULL
        OR (@HinhThuc='CHUYEN_KHOAN' AND NULLIF(LTRIM(RTRIM(@MaThamChieu)),N'') IS NULL)
        THROW 51121,N'Dữ liệu thanh toán không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        IF NOT EXISTS(SELECT 1 FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=@MaLenh)
            THROW 51122,N'Lệnh không tồn tại.',1;
        DECLARE @Id BIGINT,@HoaDonCu VARCHAR(40),@PhaiThu DECIMAL(38,0),
            @DaXacMinh DECIMAL(38,0),@DangCho DECIMAL(38,0);
        SELECT @HoaDonCu=MaHoaDon FROM dbo.HoaDon WHERE MaPhieuSuaChua=@MaLenh;
        SELECT @Id=MaGiaoDichNoiBo FROM dbo.GiaoDichThanhToan WITH(UPDLOCK,HOLDLOCK) WHERE MaThaoTac=@MaThaoTac;
        IF @Id IS NOT NULL
        BEGIN
            IF EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan WHERE MaGiaoDichNoiBo=@Id AND
                (MaLenh<>@MaLenh OR SoTien<>@SoTien OR HinhThuc<>@HinhThuc
                 OR NguoiThucHien<>@NguoiThu OR MaThamChieu<>COALESCE(@MaThamChieu,N'')))
                THROW 51123,N'Mã thao tác đã được dùng cho giao dịch khác.',1;
            COMMIT; SELECT @Id AS MaGiaoDichNoiBo; RETURN;
        END;
        IF @HoaDonCu IS NULL
            SELECT @PhaiThu=TongPhaiThu FROM dbo.DoiChieuChiPhi
            WHERE MaPhieu=@MaLenh AND TrangThai='DA_XAC_NHAN';
        ELSE
            SELECT @PhaiThu=CONVERT(DECIMAL(38,0),h.TongTien)+COALESCE(
                (SELECT SUM(CONVERT(DECIMAL(38,0),d.SoTienDieuChinh)) FROM dbo.DieuChinhHoaDon d
                 WHERE d.MaHoaDon=h.MaHoaDon AND d.TrangThai='DA_DUYET'),0)
            FROM dbo.HoaDon h WHERE h.MaHoaDon=@HoaDonCu;
        IF @PhaiThu IS NULL THROW 51127,N'Cần xác nhận đối chiếu trước khi ghi nhận tiền.',1;
        SELECT @DaXacMinh=COALESCE(DaThu,0) FROM dbo.vw_SoTienDaXacMinh WHERE MaLenh=@MaLenh;
        SET @DaXacMinh=COALESCE(@DaXacMinh,0);
        SELECT @DangCho=COALESCE(SUM(CONVERT(DECIMAL(38,0),SoTien)),0)
        FROM dbo.GiaoDichThanhToan WHERE MaLenh=@MaLenh AND LoaiGiaoDich='THU' AND TrangThai='CHO_XAC_MINH';
        IF @SoTien>@PhaiThu-@DaXacMinh-@DangCho
            THROW 51126,N'Số tiền ghi nhận vượt số còn phải thu hoặc đã có khoản chờ xác minh.',1;
        INSERT dbo.GiaoDichThanhToan(MaLenh,MaHoaDon,LoaiGiaoDich,HinhThuc,SoTien,MaThamChieu,
            TrangThai,NguoiThucHien,ThoiDiemTao,MaThaoTac)
        VALUES(@MaLenh,@HoaDonCu,'THU',@HinhThuc,@SoTien,COALESCE(@MaThamChieu,N''),'CHO_XAC_MINH',
               @NguoiThu,dbo.fn_ThoiDiemGarage(),@MaThaoTac);
        SET @Id=SCOPE_IDENTITY();
        COMMIT; SELECT @Id AS MaGiaoDichNoiBo;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_XacMinhThanhToan
    @MaGiaoDichNoiBo BIGINT,@NguoiXacMinh VARCHAR(20),@ChungCu NVARCHAR(1000),@ThanhCong BIT
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiXacMinh AND VaiTro='THU_NGAN' AND HoatDong=1)
       OR NULLIF(LTRIM(RTRIM(@ChungCu)),N'') IS NULL OR @ThanhCong IS NULL
        THROW 51124,N'Cần thu ngân đang hoạt động và căn cứ xác minh.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @MaLenh VARCHAR(40),@Khoa VARCHAR(40),@SoTien BIGINT,@PhaiThu DECIMAL(38,0),
            @DaXacMinh DECIMAL(38,0),@MaHoaDon VARCHAR(40);
        SELECT @MaLenh=MaLenh,@SoTien=SoTien,@MaHoaDon=MaHoaDon
        FROM dbo.GiaoDichThanhToan WITH(UPDLOCK,HOLDLOCK)
        WHERE MaGiaoDichNoiBo=@MaGiaoDichNoiBo AND TrangThai='CHO_XAC_MINH' AND LoaiGiaoDich='THU';
        SELECT @Khoa=MaPhieu FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=@MaLenh;
        IF @MaLenh IS NULL THROW 51125,N'Giao dịch không tồn tại hoặc đã xử lý.',1;
        IF @ThanhCong=1
        BEGIN
            IF @MaHoaDon IS NULL
                SELECT @PhaiThu=TongPhaiThu FROM dbo.DoiChieuChiPhi
                WHERE MaPhieu=@MaLenh AND TrangThai='DA_XAC_NHAN';
            ELSE
                SELECT @PhaiThu=CONVERT(DECIMAL(38,0),h.TongTien)+COALESCE(
                    (SELECT SUM(CONVERT(DECIMAL(38,0),d.SoTienDieuChinh)) FROM dbo.DieuChinhHoaDon d
                     WHERE d.MaHoaDon=h.MaHoaDon AND d.TrangThai='DA_DUYET'),0)
                FROM dbo.HoaDon h WHERE h.MaHoaDon=@MaHoaDon;
            SELECT @DaXacMinh=COALESCE(DaThu,0) FROM dbo.vw_SoTienDaXacMinh WHERE MaLenh=@MaLenh;
            SET @DaXacMinh=COALESCE(@DaXacMinh,0);
            IF @PhaiThu IS NULL OR @DaXacMinh+@SoTien>@PhaiThu
                THROW 51128,N'Xác minh làm tổng thu vượt số phải thu hiện tại.',1;
        END;
        UPDATE dbo.GiaoDichThanhToan SET TrangThai=CASE WHEN @ThanhCong=1 THEN 'DA_XAC_MINH' ELSE 'TU_CHOI' END,
            NguoiXacMinh=@NguoiXacMinh,ThoiDiemXacMinh=dbo.fn_ThoiDiemGarage(),ChungCuXacMinh=@ChungCu
        WHERE MaGiaoDichNoiBo=@MaGiaoDichNoiBo AND TrangThai='CHO_XAC_MINH' AND LoaiGiaoDich='THU';
        IF @@ROWCOUNT<>1 THROW 51125,N'Giao dịch không tồn tại hoặc đã xử lý.',1;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_LapHoaDonDaThu
    @MaLenh VARCHAR(40),@NguoiThu VARCHAR(20),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiThu AND VaiTro='THU_NGAN' AND HoatDong=1)
       OR NULLIF(@MaThaoTac,'') IS NULL THROW 51130,N'Thu ngân hoặc mã thao tác không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Bg VARCHAR(50),@MaHd VARCHAR(40),@Tong BIGINT,@Giam BIGINT,@Thue BIGINT,@DaThu DECIMAL(38,0);
        SELECT @Bg=MaBaoGia FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=@MaLenh AND TrangThai='HOAN_THANH';
        SELECT @MaHd=MaHoaDon FROM dbo.HoaDon WHERE MaThaoTac=@MaThaoTac
            AND MaPhieuSuaChua=@MaLenh AND NguoiThucHien=@NguoiThu;
        IF @MaHd IS NOT NULL BEGIN COMMIT; SELECT @MaHd AS MaHoaDon; RETURN; END;
        IF @Bg IS NULL OR EXISTS(SELECT 1 FROM dbo.HoaDon WHERE MaPhieuSuaChua=@MaLenh)
            THROW 51131,N'Lệnh chưa hoàn thành hoặc đã có hóa đơn.',1;
        SELECT @Tong=TongPhaiThu,@Giam=TienGiam,@Thue=TienThue
        FROM dbo.DoiChieuChiPhi WHERE MaPhieu=@MaLenh AND TrangThai='DA_XAC_NHAN';
        IF @Tong IS NULL THROW 51132,N'Phải xác nhận đối chiếu chi phí trước khi thu.',1;
        SELECT @DaThu=DaThu FROM dbo.vw_SoTienDaXacMinh WHERE MaLenh=@MaLenh;
        IF COALESCE(@DaThu,0)<>@Tong
            THROW 51133,N'Tiền đã xác minh phải bằng phải thu; xử lý khoản thiếu/thừa trước khi chốt.',1;
        EXEC dbo.usp_CapMaChungTu 'HD',@MaHd OUTPUT;
        DECLARE @HT VARCHAR(20),@ThamChieu NVARCHAR(100),@Luc DATETIME2(3)=dbo.fn_ThoiDiemGarage();
        SELECT TOP(1) @HT=HinhThuc,@ThamChieu=MaThamChieu
        FROM dbo.GiaoDichThanhToan WHERE MaLenh=@MaLenh AND TrangThai='DA_XAC_MINH'
        ORDER BY MaGiaoDichNoiBo DESC;
        INSERT dbo.HoaDon(MaHoaDon,MaPhieuSuaChua,TenKhachHang,BienSo,TongTien,HinhThucThanhToan,
            ThoiDiemThanhToan,NguoiThucHien,MaGiaoDich,TienTruocThue,TienGiam,TienThue,LyDoDieuChinh,MaThaoTac)
        SELECT @MaHd,@MaLenh,tn.TenKhach,sc.BienSo,@Tong,COALESCE(@HT,'TIEN_MAT'),@Luc,@NguoiThu,
            COALESCE(@ThamChieu,N''),@Tong+@Giam-@Thue,@Giam,@Thue,dc.LyDoDieuChinh,@MaThaoTac
        FROM dbo.PhieuSuaChua sc JOIN dbo.BaoGia bg ON bg.MaBaoGia=sc.MaBaoGia
        JOIN dbo.NhomBaoGia n ON n.MaBaoGiaGoc=bg.MaBaoGiaGoc
        JOIN dbo.BaoCaoKiemTra kt ON kt.MaBaoCao=n.MaBaoCao
        JOIN dbo.PhieuTiepNhan tn ON tn.MaPhieu=kt.MaPhieuTiepNhan
        JOIN dbo.DoiChieuChiPhi dc ON dc.MaPhieu=sc.MaPhieu WHERE sc.MaPhieu=@MaLenh;
        INSERT dbo.ChiTietHoaDon(MaHoaDon,ViTri,TenHangMuc,Loai,DonVi,SoLuong,DonGia,ThanhTien,MaPhatSinh,ViTriSuaChua)
        SELECT @MaHd,ViTriSuaChua,TenHangMuc,Loai,DonVi,SoLuongTinhTien,DonGia,ThanhTien,MaPhatSinh,ViTriSuaChua
        FROM dbo.vw_ChiPhiThucTe WHERE MaPhieu=@MaLenh AND SoLuongTinhTien>0;
        UPDATE dbo.GiaoDichThanhToan SET MaHoaDon=@MaHd WHERE MaLenh=@MaLenh
            AND TrangThai='DA_XAC_MINH' AND LoaiGiaoDich='THU';
        UPDATE dbo.PhieuSuaChua SET DaThanhToan=1,ThoiDiemThanhToan=@Luc WHERE MaPhieu=@MaLenh AND DaThanhToan=0;
        IF @@ROWCOUNT<>1 THROW 51134,N'Lệnh đã chốt thu ở phiên khác.',1;
        COMMIT; SELECT @MaHd AS MaHoaDon;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
CREATE OR ALTER PROCEDURE dbo.usp_DatMatKhauBam
    @MaNhanVien VARCHAR(20),@NguoiThucHien VARCHAR(20),@Muoi VARBINARY(32),
    @GiaTriBam VARBINARY(64),@SoVong INT=600000
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiThucHien
        AND VaiTro='QUAN_LY' AND HoatDong=1)
        THROW 51140,N'Chỉ quản lý được ghi nhận đặt lại mật khẩu.',1;
    IF @Muoi IS NULL OR @GiaTriBam IS NULL OR DATALENGTH(@Muoi)<16
       OR DATALENGTH(@GiaTriBam)<>32 OR @SoVong<600000
        THROW 51141,N'Hash PBKDF2-HMAC-SHA256 cần salt ít nhất 16 byte, hash 32 byte và đủ số vòng.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        UPDATE dbo.TaiKhoan SET MatKhau=CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())),
            PhienBan=PhienBan+1,BuocDoiMatKhau=1 WHERE MaNhanVien=@MaNhanVien;
        IF @@ROWCOUNT<>1 THROW 51142,N'Tài khoản không tồn tại.',1;
        UPDATE dbo.TaiKhoanThongTinBaoMat SET ThuatToan='PBKDF2-HMAC-SHA256',Muoi=@Muoi,
            GiaTriBam=@GiaTriBam,SoVong=@SoVong,NgayCapNhat=dbo.fn_ThoiDiemGarage()
        WHERE MaNhanVien=@MaNhanVien;
        IF @@ROWCOUNT=0 INSERT dbo.TaiKhoanThongTinBaoMat(MaNhanVien,ThuatToan,Muoi,GiaTriBam,SoVong)
            VALUES(@MaNhanVien,'PBKDF2-HMAC-SHA256',@Muoi,@GiaTriBam,@SoVong);
        INSERT dbo.NhatKyTaiKhoan(NguoiThucHien,MaTaiKhoan,HanhDong,LyDo)
            VALUES(@NguoiThucHien,@MaNhanVien,N'Đặt lại mật khẩu PBKDF2',N'Đặt lại bằng hash có salt; không lưu mật khẩu rõ.');
        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_PheDuyetBanGiaoNgoaiLe
    @MaLenh VARCHAR(40),@NguoiQuanLy VARCHAR(20),@LyDo NVARCHAR(2000),@ThoiHan DATETIME2(3)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiQuanLy AND VaiTro='QUAN_LY' AND HoatDong=1)
        OR NULLIF(LTRIM(RTRIM(@LyDo)),N'') IS NULL OR @ThoiHan IS NULL OR @ThoiHan<=dbo.fn_ThoiDiemGarage()
        THROW 51150,N'Quản lý, lý do hoặc thời hạn không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Lenh VARCHAR(40),@No BIGINT;
        SELECT @Lenh=MaPhieu FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK)
            WHERE MaPhieu=@MaLenh AND TrangThai='HOAN_THANH';
        IF @Lenh IS NULL OR EXISTS(SELECT 1 FROM dbo.BanGiaoXe WHERE MaLenh=@MaLenh)
            THROW 51151,N'Lệnh chưa hoàn tất hoặc đã bàn giao.',1;
        IF NOT EXISTS(SELECT 1 FROM dbo.DoiChieuChiPhi WHERE MaPhieu=@MaLenh AND TrangThai='DA_XAC_NHAN')
            THROW 51152,N'Cần xác nhận số phải thu trước khi duyệt công nợ.',1;
        SELECT @No=t.TongPhaiThu-COALESCE(s.DaThu,0) FROM dbo.vw_NghiaVuThanhToan t
            LEFT JOIN dbo.vw_SoTienDaXacMinh s ON s.MaLenh=t.MaPhieu WHERE t.MaPhieu=@MaLenh;
        IF @No IS NULL OR @No<=0 THROW 51153,N'Không có số dư cần phê duyệt.',1;
        INSERT dbo.PheDuyetBanGiaoNgoaiLe(MaLenh,SoTienConNo,LyDo,TrangThai,NguoiDeNghi,
            NguoiPheDuyet,ThoiDiemPheDuyet,ThoiHan)
        VALUES(@MaLenh,@No,@LyDo,'DA_DUYET',@NguoiQuanLy,@NguoiQuanLy,dbo.fn_ThoiDiemGarage(),@ThoiHan);
        DECLARE @Ma BIGINT=SCOPE_IDENTITY();
        COMMIT; SELECT @Ma AS MaPheDuyet;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_BanGiaoXe
    @MaLenh VARCHAR(40),@NguoiGiao VARCHAR(20),@NguoiNhan NVARCHAR(150),
    @ThoiDiemGiao DATETIME2(3),
    @TinhTrang NVARCHAR(2000),@TaiSanDaTra NVARCHAR(2000),@HinhThucXacNhan NVARCHAR(200),
    @GhiChu NVARCHAR(2000),@MaThaoTac VARCHAR(100),@MaPheDuyetNgoaiLe BIGINT=NULL
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiGiao
        AND VaiTro='NHAN_VIEN_TIEP_NHAN' AND HoatDong=1)
        OR NULLIF(@MaThaoTac,'') IS NULL OR @ThoiDiemGiao IS NULL OR NULLIF(LTRIM(RTRIM(@NguoiNhan)),N'') IS NULL
        OR NULLIF(LTRIM(RTRIM(@HinhThucXacNhan)),N'') IS NULL OR NULLIF(LTRIM(RTRIM(@TinhTrang)),N'') IS NULL
        THROW 51160,N'Thiếu người nhận/xác nhận/tình trạng hoặc sai quyền bàn giao.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Ma VARCHAR(40),@Hd VARCHAR(40),@Luc DATETIME2(3)=dbo.fn_ThoiDiemGarage(),@Khoa VARCHAR(40),
            @LucHoanTat DATETIME2(3),@LucCanCu DATETIME2(3);
        SELECT @Khoa=MaPhieu,@LucHoanTat=ThoiDiemHoanTat FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK)
        WHERE MaPhieu=@MaLenh AND TrangThai='HOAN_THANH';
        IF @Khoa IS NULL THROW 51161,N'Lệnh chưa hoàn tất.',1;
        IF @ThoiDiemGiao>@Luc OR @ThoiDiemGiao<@LucHoanTat
            THROW 51166,N'Thời điểm giao phải từ lúc hoàn tất sửa chữa đến hiện tại.',1;
        SELECT @Ma=MaBanGiao FROM dbo.BanGiaoXe WHERE MaThaoTac=@MaThaoTac;
        IF @Ma IS NOT NULL
        BEGIN
            IF EXISTS(SELECT 1 FROM dbo.BanGiaoXe WHERE MaBanGiao=@Ma AND
                (MaLenh<>@MaLenh OR NguoiGiao<>@NguoiGiao OR NguoiNhan<>@NguoiNhan OR ThoiDiemGiao<>@ThoiDiemGiao
                 OR TinhTrang<>@TinhTrang
                 OR TaiSanDaTra<>COALESCE(@TaiSanDaTra,N'') OR HinhThucXacNhan<>@HinhThucXacNhan
                 OR GhiChu<>COALESCE(@GhiChu,N'') OR COALESCE(MaPheDuyetNgoaiLe,-1)<>COALESCE(@MaPheDuyetNgoaiLe,-1)))
                THROW 51162,N'Mã thao tác đã dùng cho bàn giao khác.',1;
            COMMIT; SELECT @Ma AS MaBanGiao; RETURN;
        END;
        IF EXISTS(SELECT 1 FROM dbo.BanGiaoXe WHERE MaLenh=@MaLenh)
            THROW 51163,N'Lệnh đã bàn giao.',1;
        IF @MaPheDuyetNgoaiLe IS NULL
        BEGIN
            SELECT @Hd=MaChungTu,@LucCanCu=ThoiDiem FROM dbo.vw_CanCuBanGiao WHERE MaLenh=@MaLenh;
            IF @Hd IS NULL THROW 51164,N'Chưa đủ tiền/chứng từ thanh toán.',1;
        END
        ELSE
        BEGIN
            SELECT @LucCanCu=ThoiDiem FROM dbo.vw_CanCuBanGiaoMoRong WHERE MaLenh=@MaLenh
                AND MaChungTu=CONVERT(VARCHAR(40),CONCAT('NGOAI_LE-',@MaPheDuyetNgoaiLe)) AND NgoaiLe=1;
            IF @LucCanCu IS NULL
                THROW 51165,N'Phê duyệt ngoại lệ không hợp lệ/còn hiệu lực hoặc số nợ đã đổi.',1;
        END;
        IF @ThoiDiemGiao<@LucCanCu THROW 51167,N'Thời điểm giao không được trước thời điểm xác nhận căn cứ.',1;
        EXEC dbo.usp_CapMaChungTu 'GX',@Ma OUTPUT;
        INSERT dbo.BanGiaoXe(MaBanGiao,MaLenh,MaHoaDon,NguoiGiao,NguoiNhan,ThoiDiemGiao,ThoiDiemGhiNhan,
            TinhTrang,TaiSanDaTra,GhiChu,HinhThucXacNhan,MaThaoTac,MaPheDuyetNgoaiLe)
        VALUES(@Ma,@MaLenh,@Hd,@NguoiGiao,@NguoiNhan,@ThoiDiemGiao,@Luc,@TinhTrang,COALESCE(@TaiSanDaTra,N''),
            COALESCE(@GhiChu,N''),@HinhThucXacNhan,@MaThaoTac,@MaPheDuyetNgoaiLe);
        COMMIT; SELECT @Ma AS MaBanGiao;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
-- Lưu bản chụp báo cáo có cấu trúc: JSON chỉ số [{TenChiSo,GiaTri}],
-- cột ["Mã","Tên",...], dòng [["...","..."],...].
CREATE OR ALTER PROCEDURE dbo.usp_LuuBaoCao
    @NguoiLap VARCHAR(20),@Loai NVARCHAR(100),@TuNgay DATE,@DenNgay DATE,
    @NoiDung NVARCHAR(MAX),@ChiSo NVARCHAR(MAX),@Cot NVARCHAR(MAX),@Dong NVARCHAR(MAX),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@NguoiLap AND VaiTro='QUAN_LY' AND HoatDong=1)
       OR @TuNgay IS NULL OR @DenNgay IS NULL OR @TuNgay>@DenNgay OR NULLIF(@MaThaoTac,'') IS NULL
       OR ISJSON(@ChiSo)<>1 OR ISJSON(@Cot)<>1 OR ISJSON(@Dong)<>1
        THROW 51170,N'Quyền/ngày/JSON báo cáo không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Ma VARCHAR(40);
        SELECT @Ma=MaBaoCao FROM dbo.BaoCao WITH(UPDLOCK,HOLDLOCK) WHERE MaThaoTac=@MaThaoTac;
        IF @Ma IS NOT NULL
        BEGIN
            IF EXISTS(SELECT 1 FROM dbo.BaoCao WHERE MaBaoCao=@Ma AND
                (NguoiLap<>@NguoiLap OR LoaiBaoCao<>@Loai OR TuNgay<>@TuNgay OR DenNgay<>@DenNgay
                 OR NoiDung<>COALESCE(@NoiDung,N'') OR JsonNguon<>
                    (SELECT @ChiSo AS ChiSo,@Cot AS Cot,@Dong AS Dong FOR JSON PATH,WITHOUT_ARRAY_WRAPPER)))
                THROW 51171,N'Mã thao tác đã lưu một báo cáo khác.',1;
            COMMIT; SELECT @Ma AS MaBaoCao; RETURN;
        END;
        EXEC dbo.usp_CapMaChungTu 'BC',@Ma OUTPUT;
        INSERT dbo.BaoCao(MaBaoCao,LoaiBaoCao,TuNgay,DenNgay,NoiDung,NgayLap,NguoiLap,MaThaoTac,JsonNguon)
        VALUES(@Ma,@Loai,@TuNgay,@DenNgay,COALESCE(@NoiDung,N''),dbo.fn_ThoiDiemGarage(),@NguoiLap,@MaThaoTac,
            (SELECT @ChiSo AS ChiSo,@Cot AS Cot,@Dong AS Dong FOR JSON PATH,WITHOUT_ARRAY_WRAPPER));
        INSERT dbo.ChiSoBaoCao(MaBaoCao,ViTri,TenChiSo,GiaTri)
        SELECT @Ma,CONVERT(INT,j.[key]),x.TenChiSo,x.GiaTri
        FROM OPENJSON(@ChiSo) j CROSS APPLY OPENJSON(j.value) WITH(TenChiSo NVARCHAR(200),GiaTri BIGINT) x;
        INSERT dbo.CotBaoCao(MaBaoCao,ViTri,TenCot) SELECT @Ma,CONVERT(INT,[key]),value FROM OPENJSON(@Cot);
        -- Tách bung JSON khỏi bước ghi bảng có khóa. Bước ghi dùng số ô thực tế
        -- thay cho ước lượng nhân lên qua hai OPENJSON với giá trị NVARCHAR(MAX).
        CREATE TABLE #OBaoCaoNhap (
            Dong INT NOT NULL,
            Cot INT NOT NULL,
            GiaTri NVARCHAR(MAX) NOT NULL
        );
        INSERT #OBaoCaoNhap(Dong,Cot,GiaTri)
        SELECT CONVERT(INT,d.[key]),CONVERT(INT,c.[key]),COALESCE(c.value,N'')
        FROM OPENJSON(@Dong) d CROSS APPLY OPENJSON(d.value) c;
        INSERT dbo.OBaoCao(MaBaoCao,Dong,Cot,GiaTri)
        SELECT @Ma,Dong,Cot,GiaTri
        FROM #OBaoCaoNhap
        OPTION (RECOMPILE);
        COMMIT; SELECT @Ma AS MaBaoCao;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_DuyetDieuChinhHoaDon
    @MaHoaDon VARCHAR(40),@QuanLy VARCHAR(20),@SoTienDieuChinh BIGINT,
    @LyDo NVARCHAR(2000),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@QuanLy AND VaiTro='QUAN_LY' AND HoatDong=1)
       OR @SoTienDieuChinh IS NULL OR @SoTienDieuChinh=0 OR NULLIF(LTRIM(RTRIM(@LyDo)),N'') IS NULL
       OR NULLIF(@MaThaoTac,'') IS NULL
        THROW 51180,N'Quản lý, số tiền hoặc lý do điều chỉnh không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @Id BIGINT,@MaLenh VARCHAR(40),@Tong DECIMAL(38,0),@Khoa VARCHAR(40);
        SELECT @MaLenh=MaPhieuSuaChua FROM dbo.HoaDon WHERE MaHoaDon=@MaHoaDon;
        SELECT @Khoa=MaPhieu FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=@MaLenh;
        SELECT @Id=MaDieuChinh FROM dbo.DieuChinhHoaDon WHERE MaThaoTac=@MaThaoTac;
        IF @Id IS NOT NULL
        BEGIN
            IF EXISTS(SELECT 1 FROM dbo.DieuChinhHoaDon WHERE MaDieuChinh=@Id AND
                (MaHoaDon<>@MaHoaDon OR SoTienDieuChinh<>@SoTienDieuChinh OR LyDo<>@LyDo OR NguoiLap<>@QuanLy))
                THROW 51181,N'Mã thao tác đã dùng cho điều chỉnh khác.',1;
            COMMIT; SELECT @Id AS MaDieuChinh; RETURN;
        END;
        SELECT @Tong=CONVERT(DECIMAL(38,0),TongTien)+COALESCE(
            (SELECT SUM(CONVERT(DECIMAL(38,0),SoTienDieuChinh)) FROM dbo.DieuChinhHoaDon d
             WHERE d.MaHoaDon=h.MaHoaDon AND d.TrangThai='DA_DUYET'),0)
        FROM dbo.HoaDon h WHERE MaHoaDon=@MaHoaDon;
        IF @Tong IS NULL OR @Tong+@SoTienDieuChinh<0 THROW 51182,N'Điều chỉnh làm tổng hóa đơn âm hoặc hóa đơn không tồn tại.',1;
        INSERT dbo.DieuChinhHoaDon(MaHoaDon,MaThaoTac,LyDo,SoTienDieuChinh,NguoiLap,NguoiDuyet,ThoiDiemDuyet,TrangThai)
        VALUES(@MaHoaDon,@MaThaoTac,@LyDo,@SoTienDieuChinh,@QuanLy,@QuanLy,dbo.fn_ThoiDiemGarage(),'DA_DUYET');
        SET @Id=SCOPE_IDENTITY();
        COMMIT; SELECT @Id AS MaDieuChinh;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE OR ALTER PROCEDURE dbo.usp_GhiNhanHoanTien
    @MaDieuChinh BIGINT,@ThuNgan VARCHAR(20),@SoTien BIGINT,@HinhThuc VARCHAR(20),
    @MaThamChieu NVARCHAR(100),@ChungCu NVARCHAR(1000),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE MaNhanVien=@ThuNgan AND VaiTro='THU_NGAN' AND HoatDong=1)
       OR @SoTien IS NULL OR @SoTien<=0 OR @HinhThuc IS NULL OR @HinhThuc NOT IN('TIEN_MAT','CHUYEN_KHOAN')
       OR NULLIF(LTRIM(RTRIM(@ChungCu)),N'') IS NULL OR NULLIF(@MaThaoTac,'') IS NULL
       OR (@HinhThuc='CHUYEN_KHOAN' AND NULLIF(LTRIM(RTRIM(@MaThamChieu)),N'') IS NULL)
        THROW 51183,N'Quyền/số tiền/căn cứ hoàn tiền không hợp lệ.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @MaLenh VARCHAR(40),@MaHd VARCHAR(40),@ToiDa BIGINT,@DaHoan DECIMAL(38,0),@Id BIGINT,
            @Khoa VARCHAR(40),@PhaiThu DECIMAL(38,0),@DaThu DECIMAL(38,0);
        SELECT @MaHd=d.MaHoaDon,@MaLenh=h.MaPhieuSuaChua,@ToiDa=-d.SoTienDieuChinh
        FROM dbo.DieuChinhHoaDon d JOIN dbo.HoaDon h ON h.MaHoaDon=d.MaHoaDon
        WHERE d.MaDieuChinh=@MaDieuChinh AND d.TrangThai='DA_DUYET' AND d.SoTienDieuChinh<0;
        SELECT @Khoa=MaPhieu FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=@MaLenh;
        IF @MaHd IS NULL THROW 51184,N'Không có điều chỉnh giảm được duyệt.',1;
        SELECT @Id=MaGiaoDichNoiBo FROM dbo.GiaoDichThanhToan WHERE MaThaoTac=@MaThaoTac;
        IF @Id IS NOT NULL
        BEGIN
            IF EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan WHERE MaGiaoDichNoiBo=@Id AND
                (MaDieuChinh<>@MaDieuChinh OR SoTien<>@SoTien OR NguoiThucHien<>@ThuNgan
                 OR HinhThuc<>@HinhThuc OR MaThamChieu<>COALESCE(@MaThamChieu,N'') OR ChungCuXacMinh<>@ChungCu))
                THROW 51185,N'Mã thao tác đã dùng cho lần hoàn khác.',1;
            COMMIT; SELECT @Id AS MaGiaoDichNoiBo; RETURN;
        END;
        SELECT @DaHoan=COALESCE(SUM(CONVERT(DECIMAL(38,0),SoTien)),0) FROM dbo.GiaoDichThanhToan
        WHERE MaDieuChinh=@MaDieuChinh AND LoaiGiaoDich='HOAN_TIEN' AND TrangThai='DA_HOAN';
        IF @DaHoan+@SoTien>@ToiDa THROW 51186,N'Hoàn vượt khoản giảm được duyệt.',1;
        SELECT @PhaiThu=CONVERT(DECIMAL(38,0),h.TongTien)+COALESCE(
            (SELECT SUM(CONVERT(DECIMAL(38,0),d.SoTienDieuChinh)) FROM dbo.DieuChinhHoaDon d
             WHERE d.MaHoaDon=h.MaHoaDon AND d.TrangThai='DA_DUYET'),0)
        FROM dbo.HoaDon h WHERE h.MaHoaDon=@MaHd;
        SELECT @DaThu=DaThu FROM dbo.vw_SoTienDaXacMinh WHERE MaLenh=@MaLenh;
        IF COALESCE(@DaThu,0)-@SoTien<@PhaiThu
            THROW 51187,N'Khoản hoàn làm tiền thực nhận thấp hơn nghĩa vụ sau các điều chỉnh hiện tại.',1;
        INSERT dbo.GiaoDichThanhToan(MaLenh,MaHoaDon,MaDieuChinh,MaThaoTac,LoaiGiaoDich,HinhThuc,SoTien,
            MaThamChieu,TrangThai,NguoiThucHien,NguoiXacMinh,ThoiDiemXacMinh,ChungCuXacMinh)
        VALUES(@MaLenh,@MaHd,@MaDieuChinh,@MaThaoTac,'HOAN_TIEN',@HinhThuc,@SoTien,COALESCE(@MaThamChieu,N''),
            'DA_HOAN',@ThuNgan,@ThuNgan,dbo.fn_ThoiDiemGarage(),@ChungCu);
        SET @Id=SCOPE_IDENTITY();
        COMMIT; SELECT @Id AS MaGiaoDichNoiBo;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO
CREATE OR ALTER PROCEDURE dbo.usp_HoanPhuTung
    @MaGhiNhan VARCHAR(40),@SoLuong DECIMAL(18,3),@NguoiThucHien VARCHAR(20),
    @LyDo NVARCHAR(2000),@MaThaoTac VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    IF @SoLuong IS NULL OR @SoLuong<=0 OR NULLIF(LTRIM(RTRIM(@LyDo)),N'') IS NULL
        OR NULLIF(@MaThaoTac,'') IS NULL THROW 51190,N'Thiếu số lượng, lý do hoặc mã thao tác hoàn.',1;
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @MaLenh VARCHAR(40),@MaPT VARCHAR(30),@ViTri INT,@Khoa VARCHAR(40),
            @Goc BIGINT,@Ton DECIMAL(18,3),@DaXuat DECIMAL(18,3),@Con DECIMAL(18,3),@Ma VARCHAR(40);
        SELECT @MaLenh=MaLenhSuaChua FROM dbo.PhuTungSuDung WHERE MaGhiNhan=@MaGhiNhan AND TrangThai='DA_GHI_NHAN';
        SELECT @Khoa=MaPhieu FROM dbo.PhieuSuaChua WITH(UPDLOCK,HOLDLOCK) WHERE MaPhieu=@MaLenh AND DaThanhToan=0;
        IF @Khoa IS NULL OR EXISTS(SELECT 1 FROM dbo.HoaDon WHERE MaPhieuSuaChua=@MaLenh)
            THROW 51191,N'Lần xuất không hợp lệ hoặc đã có hóa đơn; cần luồng điều chỉnh sau thu.',1;
        IF EXISTS(SELECT 1 FROM dbo.DoiChieuChiPhi WITH(UPDLOCK,HOLDLOCK)
                  WHERE MaPhieu=@MaLenh AND TrangThai='DA_XAC_NHAN')
            THROW 51198,N'Chi phí đã được xác nhận; phải hủy/điều chỉnh đối chiếu trước khi hoàn phụ tùng.',1;
        SELECT @MaPT=MaPhuTung,@ViTri=ViTriHangMuc,@DaXuat=SoLuong FROM dbo.PhuTungSuDung
            WITH(UPDLOCK,HOLDLOCK) WHERE MaGhiNhan=@MaGhiNhan;
        IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan t WHERE t.MaNhanVien=@NguoiThucHien AND t.HoatDong=1
            AND (t.VaiTro='QUAN_LY' OR (t.VaiTro='NHAN_VIEN_KY_THUAT' AND EXISTS(
                SELECT 1 FROM dbo.PhieuSuaChua p JOIN dbo.ChiTietSuaChua c ON c.MaPhieu=p.MaPhieu
                WHERE p.MaPhieu=@MaLenh AND c.ViTri=@ViTri
                  AND (@NguoiThucHien=p.KyThuatChinh OR @NguoiThucHien=c.TaiKhoanThucHien)))))
            THROW 51192,N'Không có quyền hoàn phụ tùng của hạng mục.',1;
        IF EXISTS(SELECT 1 FROM dbo.PhuTung p JOIN dbo.DonViTinh u ON u.DonVi=p.DonViTinh
            WHERE p.MaPhuTung=@MaPT AND u.ChoPhepSoLe=0 AND @SoLuong%1<>0)
            THROW 51193,N'Đơn vị đếm không cho phép số lẻ.',1;
        SELECT @Ma=MaPhieuDieuChinh FROM dbo.PhieuDieuChinhKho WHERE MaThaoTac=@MaThaoTac;
        IF @Ma IS NOT NULL
        BEGIN
            IF NOT EXISTS(SELECT 1 FROM dbo.PhieuDieuChinhKho h JOIN dbo.ChiTietDieuChinhKho d
                ON d.MaPhieuDieuChinh=h.MaPhieuDieuChinh JOIN dbo.BienDongKho b ON b.MaBienDong=d.MaBienDongGoc
                WHERE h.MaPhieuDieuChinh=@Ma AND h.NguoiThucHien=@NguoiThucHien AND h.LyDo=@LyDo
                  AND d.SoLuong=@SoLuong AND d.MaPhuTung=@MaPT AND b.ChungTu=@MaGhiNhan)
                THROW 51194,N'Mã thao tác đã dùng cho nội dung hoàn khác.',1;
            COMMIT; SELECT @Ma AS MaPhieuDieuChinh; RETURN;
        END;
        SELECT @Ton=SoLuongTon FROM dbo.PhuTung WITH(UPDLOCK,HOLDLOCK) WHERE MaPhuTung=@MaPT;
        IF (SELECT COUNT(*) FROM dbo.BienDongKho WHERE ChungTu=@MaGhiNhan AND MaPhuTung=@MaPT AND Loai=N'Xuất')<>1
            THROW 51195,N'Không tìm được duy nhất biến động xuất gốc; cần đối chiếu kho.',1;
        SELECT @Goc=MaBienDong FROM dbo.BienDongKho WHERE ChungTu=@MaGhiNhan AND MaPhuTung=@MaPT AND Loai=N'Xuất'
            AND TonTruoc-TonSau=@DaXuat;
        IF @Goc IS NULL THROW 51196,N'Lượng xuất và nhật ký kho không khớp.',1;
        SELECT @Con=ThucDung FROM dbo.vw_PhuTungThucDung WHERE MaGhiNhan=@MaGhiNhan;
        IF @SoLuong>@Con THROW 51197,N'Hoàn vượt lượng còn lại.',1;
        EXEC dbo.usp_CapMaChungTu 'DC',@Ma OUTPUT;
        DECLARE @Luc DATETIME2(3)=dbo.fn_ThoiDiemGarage();
        INSERT dbo.PhieuDieuChinhKho(MaPhieuDieuChinh,ThoiDiem,LyDo,NguoiThucHien,TrangThai,MaThaoTac)
        VALUES(@Ma,@Luc,@LyDo,@NguoiThucHien,'DA_GHI_NHAN',@MaThaoTac);
        INSERT dbo.ChiTietDieuChinhKho(MaPhieuDieuChinh,ViTri,MaPhuTung,SoLuong,Chieu,MaBienDongGoc)
        VALUES(@Ma,0,@MaPT,@SoLuong,'NHAP',@Goc);
        UPDATE dbo.PhuTung SET SoLuongTon=SoLuongTon+@SoLuong WHERE MaPhuTung=@MaPT;
        INSERT dbo.BienDongKho(MaPhuTung,ThoiDiem,Loai,TonTruoc,TonSau,ChungTu,NguoiThucHien,LyDo)
        VALUES(@MaPT,@Luc,N'Hoàn kho',@Ton,@Ton+@SoLuong,@Ma,@NguoiThucHien,@LyDo);
        COMMIT; SELECT @Ma AS MaPhieuDieuChinh;
    END TRY
    BEGIN CATCH
        IF XACT_STATE()<>0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

-- PHẦN 4: GIEO DEMO TRONG MỘT TRANSACTION, CHỈ KHI DỰNG MỚI.
-- Mật khẩu demo Garage@2026!; salt khác nhau, PBKDF2-HMAC-SHA256 600.000 vòng.
IF EXISTS(SELECT 1 FROM #CauHinhCaiDat WHERE GieoMau=1)
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

    -- 2. Tài khoản. Tài khoản demo dùng PBKDF2 ở cuối khối seed
    INSERT dbo.TaiKhoan
        (MaNhanVien, MatKhau, VaiTro, HoatDong, NgayTao)
    VALUES
        ('NV001', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'QUAN_LY', 1, SYSDATETIME()),
        ('NV003', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'NHAN_VIEN_TIEP_NHAN', 1, SYSDATETIME()),
        ('NV005', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'NHAN_VIEN_KY_THUAT', 1, SYSDATETIME()),
        ('NV007', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'THU_NGAN', 1, SYSDATETIME());

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
        ('PT001', DATEADD(MINUTE, 160, @LucTiepNhan), N'Xuất', 20, 16, N'SD-2026-0001', 'NV005', N'Dùng cho xe 51A-12345'),
        ('PT002', DATEADD(MINUTE, 161, @LucTiepNhan), N'Xuất', 10, 9, N'SD-2026-0002', 'NV005', N'Dùng cho xe 51A-12345');

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


-- Thêm nhân viên
INSERT dbo.NhanVien
    (MaNhanVien, HoTen, NgaySinh, SoDienThoai, GioiTinh, DiaChi, SoNamKinhNghiem, BoPhan, TrangThai)
VALUES
    ('NV008', N'Lê Thị Thu Hà', '1997-05-12', '0908765432', N'Nữ', N'Q10, TP.HCM', 2, 'THU_NGAN', 'DANG_LAM'),
    ('NV009', N'Phạm Văn Tùng', '1999-09-25', '0912345678', N'Nam', N'Q5, TP.HCM', 1, 'TIEP_NHAN', 'DANG_LAM'),
    ('NV010', N'Trần Văn Khoa', '1995-03-18', '0909876543', N'Nam', N'Bình Tân, TP.HCM', 5, 'KY_THUAT', 'DANG_LAM');

INSERT dbo.TaiKhoan (MaNhanVien, MatKhau, VaiTro, HoatDong, NgayTao)
VALUES
    ('NV008', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'THU_NGAN', 1, SYSDATETIME()),
    ('NV009', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'NHAN_VIEN_TIEP_NHAN', 1, SYSDATETIME()),
    ('NV010', CONCAT(N'!PBKDF2!',CONVERT(NVARCHAR(36),NEWID())), 'NHAN_VIEN_KY_THUAT', 1, SYSDATETIME());

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
INSERT dbo.DichVu VALUES('DV005', N'Thay lốp', N'Lốp', N'cái', 50000, N'Thay lốp', N'30p', N'Tất cả', 1);
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
    ('PT008', N'Má phanh trước Mazda CX-5', N'Phanh', N'bộ', 600000, 900000, 2, 6, 1),
    ('PT009', N'Dầu phanh DOT4', N'Dầu', N'lít', 80000, 120000, 5, 20, 1),
    ('PT010', N'Lọc gió', N'Lọc', N'cái', 100000, 180000, 5, 12, 1);

PRINT N'Đã thêm nhân viên, khách hàng, xe, dịch vụ, phụ tùng';
DECLARE @D DATE=CONVERT(DATE,DATEADD(DAY,-1,dbo.fn_ThoiDiemGarage()));
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
('BG-2026-0002',0,'DV004',NULL,N'Thay má phanh',N'Dịch vụ',N'Công',1,300000),('BG-2026-0002',1,NULL,'PT008',N'Má phanh trước Mazda CX-5',N'Phụ tùng',N'bộ',1,900000),('BG-2026-0002',2,NULL,'PT009',N'Dầu phanh DOT4',N'Phụ tùng',N'lít',1,120000),
('BG-2026-0003',0,'DV005',NULL,N'Thay lốp',N'Dịch vụ',N'cái',2,50000),('BG-2026-0003',1,NULL,'PT004',N'Lốp Michelin',N'Phụ tùng',N'cái',2,1500000),
('BG-2026-0004',0,'DV006',NULL,N'Bảo dưỡng định kỳ',N'Dịch vụ',N'Gói',1,500000),('BG-2026-0004',1,NULL,'PT001',N'Dầu động cơ 5W-30',N'Phụ tùng',N'lít',4,150000),('BG-2026-0004',2,NULL,'PT010',N'Lọc gió',N'Phụ tùng',N'cái',1,180000);
INSERT dbo.PhieuSuaChua(MaPhieu,MaBaoGia,MaBaoGiaGoc,BienSo,KyThuatChinh,KyThuatPhoiHop,HanHoanThanh,GhiChu,TrangThai,NguoiPhatHanh,ThoiDiemPhatHanh,ThoiDiemHoanTat,KetQua0,KetQua1,KetQua2,MaThaoTac) VALUES
('SC-2026-0002','BG-2026-0002','BG-2026-0002','51C-11111','NV010',NULL,DATEADD(HOUR,4,@T1),N'Theo báo giá','HOAN_THANH','NV009',DATEADD(MINUTE,50,@T1),DATEADD(MINUTE,120,@T1),N'Phanh hoạt động tốt',N'Đã đối chiếu',N'Chờ thu tiền','MAU-SC-2026-0002'),
('SC-2026-0003','BG-2026-0003','BG-2026-0003','51D-22222','NV010',NULL,DATEADD(HOUR,4,@T2),N'Theo báo giá','HOAN_THANH','NV009',DATEADD(MINUTE,50,@T2),DATEADD(MINUTE,120,@T2),N'Xe vận hành tốt',N'Đã cân bằng bánh',N'Chờ thu tiền','MAU-SC-2026-0003'),
('SC-2026-0004','BG-2026-0004','BG-2026-0004','51E-33333','NV010',NULL,DATEADD(HOUR,4,@T3),N'Theo báo giá','HOAN_THANH','NV009',DATEADD(MINUTE,50,@T3),DATEADD(MINUTE,120,@T3),N'Đã bảo dưỡng',N'Đã kiểm tra',N'Chờ thu tiền','MAU-SC-2026-0004');

    DECLARE @Luc DATETIME2(3)=DATEADD(HOUR,6,CONVERT(DATETIME2(3),CONVERT(DATE,DATEADD(DAY,-2,dbo.fn_ThoiDiemGarage()))));
    INSERT dbo.PhieuNhapKho(MaPhieu,NgayNhap,NguonNhap,ChungTuNguon,NguoiNhap,MaThaoTac)
    VALUES('NK-2026-0002',@Luc,N'Nhập tồn đầu kỳ cho danh mục bổ sung',N'MAU-TON-DAU-KY','NV001','MAU-NK-2026-0002');
    INSERT dbo.ChiTietNhapKho(MaPhieu,ViTri,MaPhuTung,SoLuong,DonGia)
    SELECT 'NK-2026-0002',CONVERT(INT,ROW_NUMBER() OVER(ORDER BY MaPhuTung))-1,MaPhuTung,SoLuongTon,GiaNhap
    FROM dbo.PhuTung WHERE MaPhuTung BETWEEN 'PT004' AND 'PT010';
    INSERT dbo.BienDongKho(MaPhuTung,ThoiDiem,Loai,TonTruoc,TonSau,ChungTu,NguoiThucHien,LyDo)
    SELECT MaPhuTung,@Luc,N'Nhập',0,SoLuongTon,'NK-2026-0002','NV001',N'Tồn đầu kỳ dữ liệu mẫu'
    FROM dbo.PhuTung WHERE MaPhuTung BETWEEN 'PT004' AND 'PT010';

    INSERT dbo.PhanCong(MaPC,MaPhieuSC,MaNV,NgayPhanCong,GhiChu)
    SELECT CONCAT('PC-MAU-',MaPhieu),MaPhieu,KyThuatChinh,ThoiDiemPhatHanh,N'Phân công dữ liệu mẫu'
    FROM dbo.PhieuSuaChua WHERE MaPhieu IN('SC-2026-0002','SC-2026-0003','SC-2026-0004');
    INSERT dbo.ChiTietSuaChua(MaPhieu,ViTri,TenHangMuc,TaiKhoanThucHien,KetQua,TrangThai,PhatSinh)
    SELECT sc.MaPhieu,c.ViTri,c.TenHangMuc,sc.KyThuatChinh,N'Đã thực hiện theo báo giá mẫu','HOAN_THANH',0
    FROM dbo.PhieuSuaChua sc JOIN dbo.ChiTietBaoGia c ON c.MaBaoGia=sc.MaBaoGia
    WHERE sc.MaPhieu IN('SC-2026-0002','SC-2026-0003','SC-2026-0004');
    INSERT dbo.PhuTungSuDung(MaGhiNhan,MaLenhSuaChua,ViTriHangMuc,MaPhuTung,SoLuong,NguoiThucHien,GhiChu,TrangThai,MaThaoTac)
    SELECT CONCAT('SD-MAU-',sc.MaPhieu,'-',c.ViTri),sc.MaPhieu,c.ViTri,c.MaPhuTung,c.SoLuong,sc.KyThuatChinh,
        N'Xuất thực dùng cho dữ liệu mẫu','DA_GHI_NHAN',CONCAT('MAU-SD-',sc.MaPhieu,'-',c.ViTri)
    FROM dbo.PhieuSuaChua sc JOIN dbo.ChiTietBaoGia c ON c.MaBaoGia=sc.MaBaoGia
    WHERE sc.MaPhieu IN('SC-2026-0002','SC-2026-0003','SC-2026-0004') AND c.MaPhuTung IS NOT NULL;

    INSERT dbo.BienDongKho(MaPhuTung,ThoiDiem,Loai,TonTruoc,TonSau,ChungTu,NguoiThucHien,LyDo)
    SELECT s.MaPhuTung,sc.ThoiDiemHoanTat,N'Xuất',
        p.SoLuongTon-COALESCE(SUM(s.SoLuong) OVER(PARTITION BY s.MaPhuTung ORDER BY s.MaGhiNhan
                   ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING),0),
        p.SoLuongTon-SUM(s.SoLuong) OVER(PARTITION BY s.MaPhuTung ORDER BY s.MaGhiNhan
                   ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW),
        s.MaGhiNhan,s.NguoiThucHien,N'Xuất theo dữ liệu mẫu'
    FROM dbo.PhuTungSuDung s JOIN dbo.PhuTung p ON p.MaPhuTung=s.MaPhuTung
    JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu=s.MaLenhSuaChua WHERE s.MaGhiNhan LIKE 'SD-MAU-%';
    UPDATE p SET SoLuongTon=p.SoLuongTon-x.So
    FROM dbo.PhuTung p JOIN(SELECT MaPhuTung,SUM(SoLuong) AS So FROM dbo.PhuTungSuDung
         WHERE MaGhiNhan LIKE 'SD-MAU-%' GROUP BY MaPhuTung) x ON x.MaPhuTung=p.MaPhuTung;

    INSERT dbo.TaiKhoanThongTinBaoMat(MaNhanVien,ThuatToan,Muoi,GiaTriBam,SoVong)
    VALUES
('NV001','PBKDF2-HMAC-SHA256',0x2F3EF18BA29123CD3180B9D4751590BA,0x96F73562B42114AC8FF57F3E490508B1A874479973132D3D9A635CF38F580530,600000),
('NV003','PBKDF2-HMAC-SHA256',0xC97EE7928E0BE0C763E34906E35FEE3E,0xE4200DF21FFB02E518D5429BB770B6F49559682FBCA2DC24F8135FF707F403B9,600000),
('NV005','PBKDF2-HMAC-SHA256',0x00D0E9783A075782B94518DB3C7F84BE,0xF4C6CF61235B6FB19F3C53F01725614A398D2C8ABFD2A5F68A61A383C7340FF4,600000),
('NV007','PBKDF2-HMAC-SHA256',0x5DCE34C079A4BE778E6E80FCA518ADD8,0x7F6CBD9D9DF14400C468CDDBF30B2C7582A4366E9A02E10EE81AACBDA2454D53,600000),
('NV008','PBKDF2-HMAC-SHA256',0xFB79641335AD4274024E64C2E2374623,0x90F92DE888DDB4C041B9F5FD3A85552E7053F2453F489EE3B31C20548C13DAE3,600000),
('NV009','PBKDF2-HMAC-SHA256',0x08396C539C412BC01110ACFD345B7048,0x46BEE883E35C7AEEE0890EBA20A80579B721193813DBFA7F6E3F00D7272C1BA6,600000),
('NV010','PBKDF2-HMAC-SHA256',0x5D30E47DAB16B598DDCF6FAB16B561CB,0x2F223CFD4ADEF5CD5625C60715A03BDB6BE464313B19FBEC43616476C4EFD029,600000);

    COMMIT;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK;
    THROW;
END CATCH;
GO

-- Chuyển dữ liệu legacy có căn cứ; không suy đoán lịch sử người thực hiện.
BEGIN TRANSACTION;
INSERT dbo.ChiTietKiemTra(MaBaoCao,ViTri,TenKhuVuc,TenHangMuc,KetQua,MucDo,DeXuat,NguoiGhi,ThoiDiemGhi)
SELECT b.MaBaoCao,x.ViTri,N'Dữ liệu chuyển từ báo cáo cũ',x.Ten,x.KetQua,N'Chưa đánh giá',x.DeXuat,
       NULL,COALESCE(b.ThoiDiemHoanTat,CONVERT(DATETIME2(3),b.NgayKiemTra))
FROM dbo.BaoCaoKiemTra b CROSS APPLY(VALUES
    (0,N'Nội dung cũ 0',b.NoiDung0,N''),(1,N'Nội dung cũ 1',b.NoiDung1,N''),
    (2,N'Nội dung cũ 2',b.NoiDung2,N''),(3,N'Nội dung cũ 3',b.NoiDung3,b.DeXuat0),
    (4,N'Nội dung cũ 4',b.NoiDung4,b.DeXuat1),(5,N'Nội dung cũ 5',b.NoiDung5,b.DeXuat2),
    (6,N'Nội dung cũ 6',b.NoiDung6,N'')) x(ViTri,Ten,KetQua,DeXuat)
WHERE (NULLIF(x.KetQua,N'') IS NOT NULL OR NULLIF(x.DeXuat,N'') IS NOT NULL)
  AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietKiemTra c WHERE c.MaBaoCao=b.MaBaoCao);

-- TU_CHOI có thể chuyển nguyên trạng. DA_DUYET thiếu chi tiết giá phải đối chiếu thủ công:
-- không tự tạo dòng giá hay quyết định khách không có trong dữ liệu gốc.
INSERT dbo.DeXuatPhatSinh(MaPhieu,SoThuTu,HuHong,NguyenNhan,DeXuat,GhiChu,TrangThai,NguoiBao,ThoiDiemBao)
SELECT p.MaPhieu,1,p.HuHong,p.NguyenNhan,p.DeXuat,
       CONCAT(N'Chuyển dữ liệu cũ; người báo thực tế chưa lưu. ',p.GhiChu),
       CASE WHEN p.TrangThai='TU_CHOI' THEN 'TU_CHOI' ELSE 'CHO_KHACH' END,
       sc.KyThuatChinh,COALESCE(p.ThoiDiemXacNhan,sc.ThoiDiemPhatHanh)
FROM dbo.PhatSinhSuaChua p JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu=p.MaPhieu
WHERE NOT EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh d WHERE d.MaPhieu=p.MaPhieu);

-- Cờ được đối chiếu theo hóa đơn đã thu legacy, không phụ thuộc dữ liệu mô phỏng trên UI.
UPDATE sc SET DaThanhToan=1,ThoiDiemThanhToan=hd.ThoiDiemThanhToan
FROM dbo.PhieuSuaChua sc JOIN dbo.HoaDon hd ON hd.MaPhieuSuaChua=sc.MaPhieu
WHERE sc.DaThanhToan=0 AND NOT EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan g WHERE g.MaLenh=sc.MaPhieu);

UPDATE dbo.HoaDon SET TienTruocThue=CONVERT(DECIMAL(38,0),TongTien)+TienGiam-TienThue
WHERE TienTruocThue IS NULL;

-- Theo hợp đồng DAO legacy, hóa đơn chỉ được tạo khi đã thu đủ. Chuyển đúng
-- bằng chứng đó thành một giao dịch thu để số dư không âm khi phát sinh hoàn tiền.
INSERT dbo.GiaoDichThanhToan(MaLenh,MaHoaDon,MaThaoTac,LoaiGiaoDich,HinhThuc,SoTien,MaThamChieu,
    TrangThai,NguoiThucHien,ThoiDiemTao,NguoiXacMinh,ThoiDiemXacMinh,ChungCuXacMinh)
SELECT h.MaPhieuSuaChua,h.MaHoaDon,CONCAT('MIGRATION-THU-',h.MaHoaDon),'THU',h.HinhThucThanhToan,
    h.TongTien,h.MaGiaoDich,'DA_XAC_MINH',h.NguoiThucHien,h.ThoiDiemThanhToan,
    h.NguoiThucHien,h.ThoiDiemThanhToan,N'Chuyển từ hóa đơn legacy chỉ được lập khi thu đủ.'
FROM dbo.HoaDon h WHERE h.TongTien>0
  AND NOT EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan g WHERE g.MaHoaDon=h.MaHoaDon AND g.LoaiGiaoDich='THU');
COMMIT;
GO

-- PHẦN 5: RÀNG BUỘC ĐƯỜNG GHI JDBC VÀ BẢO TOÀN CHỨNG TỪ.
-- Ràng buộc cho các đường ghi JDBC trực tiếp. Tạo sau dữ liệu mẫu/migration.
CREATE OR ALTER TRIGGER dbo.trg_PhuTung_DonVi ON dbo.PhuTung
AFTER INSERT,UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.DonViTinh u ON u.DonVi=i.DonViTinh
        WHERE u.ChoPhepSoLe=0 AND (i.SoLuongTon%1<>0 OR i.MucToiThieu%1<>0))
        THROW 51201,N'Đơn vị đếm không cho phép tồn hoặc mức tối thiểu có số lẻ.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_TaiKhoan_VaiTro ON dbo.TaiKhoan
AFTER INSERT,UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.NhanVien n ON n.MaNhanVien=i.MaNhanVien
        WHERE (i.HoatDong=1 AND i.VaiTro<>CASE n.BoPhan WHEN 'TIEP_NHAN' THEN 'NHAN_VIEN_TIEP_NHAN'
              WHEN 'KY_THUAT' THEN 'NHAN_VIEN_KY_THUAT' ELSE n.BoPhan END)
          OR (i.HoatDong=1 AND n.TrangThai<>'DANG_LAM'))
        THROW 51202,N'Vai trò phải khớp bộ phận; nhân viên đã nghỉ không được có tài khoản hoạt động.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_NhanVien_BoPhan ON dbo.NhanVien
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.TaiKhoan t ON t.MaNhanVien=i.MaNhanVien
        WHERE (t.HoatDong=1 AND t.VaiTro<>CASE i.BoPhan WHEN 'TIEP_NHAN' THEN 'NHAN_VIEN_TIEP_NHAN'
              WHEN 'KY_THUAT' THEN 'NHAN_VIEN_KY_THUAT' ELSE i.BoPhan END)
          OR (i.TrangThai='DA_NGHI' AND t.HoatDong=1))
        THROW 51203,N'Cần khóa/chuyển vai trò tài khoản trước khi thay đổi bộ phận hoặc nghỉ việc.',1;
END;
GO
-- Khóa bản báo giá được tham chiếu ngay trong thao tác detail; ngăn cửa sổ cũ sửa bản đã gửi.
CREATE OR ALTER TRIGGER dbo.trg_ChiTietBaoGia_KhoaDaGui ON dbo.ChiTietBaoGia
AFTER INSERT,UPDATE,DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM dbo.BaoGia b WITH(UPDLOCK,HOLDLOCK)
        JOIN(SELECT MaBaoGia FROM inserted UNION SELECT MaBaoGia FROM deleted) d ON d.MaBaoGia=b.MaBaoGia
        WHERE b.TrangThai<>'NHAP')
        THROW 51204,N'Không được sửa chi tiết báo giá đã gửi; tạo phiên bản mới.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhatSinh_GiaDaChot ON dbo.ChiTietDeXuatPhatSinh
AFTER INSERT,UPDATE,DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh p WITH(UPDLOCK,HOLDLOCK)
        JOIN(SELECT MaPhatSinh FROM inserted UNION SELECT MaPhatSinh FROM deleted) d ON d.MaPhatSinh=p.MaPhatSinh
        WHERE p.TrangThai<>'CHO_KHACH')
        THROW 51205,N'Phụ lục đã có phản hồi không được thay đổi nội dung giá.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhanHoiPhatSinh_HopLe ON dbo.PhanHoiPhatSinh
AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.DeXuatPhatSinh d ON d.MaPhatSinh=i.MaPhatSinh
        JOIN dbo.TaiKhoan t ON t.MaNhanVien=i.NguoiGhiNhan
        WHERE t.VaiTro<>'NHAN_VIEN_TIEP_NHAN' OR t.HoatDong=0
          OR i.ThoiDiemKhachPhanHoi<d.ThoiDiemBao OR i.ThoiDiemKhachPhanHoi>i.ThoiDiemGhiNhan
          OR (i.QuyetDinh='DA_DONG_Y' AND NOT EXISTS(
                SELECT 1 FROM dbo.ChiTietDeXuatPhatSinh c WHERE c.MaPhatSinh=i.MaPhatSinh)))
        THROW 51206,N'Phản hồi sai vai trò/thời điểm hoặc chưa có phụ lục giá.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhanHoiPhatSinh_BatBien ON dbo.PhanHoiPhatSinh
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted) THROW 51207,N'Không sửa/xóa phản hồi đã ghi; tạo phát sinh mới để điều chỉnh.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_DeXuatPhatSinh_QuyetDinh ON dbo.DeXuatPhatSinh
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.MaPhatSinh=i.MaPhatSinh
        WHERE (d.TrangThai<>'CHO_KHACH' AND (i.TrangThai<>d.TrangThai OR i.HuHong<>d.HuHong
              OR i.NguyenNhan<>d.NguyenNhan OR i.DeXuat<>d.DeXuat
              OR COALESCE(i.ThoiGianDuKienPhut,-1)<>COALESCE(d.ThoiGianDuKienPhut,-1)))
           OR (i.TrangThai IN('DA_DONG_Y','TU_CHOI') AND NOT EXISTS(
                SELECT 1 FROM dbo.PhanHoiPhatSinh p WHERE p.MaPhatSinh=i.MaPhatSinh AND p.QuyetDinh=i.TrangThai)))
        THROW 51208,N'Quyết định phải có phản hồi tương ứng và không được ghi đè.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_DieuChinhKho_KiemTra ON dbo.ChiTietDieuChinhKho
AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Khoa BIGINT;
    SELECT @Khoa=MAX(b.MaBienDong) FROM dbo.BienDongKho b WITH(UPDLOCK,HOLDLOCK)
        JOIN inserted i ON i.MaBienDongGoc=b.MaBienDong;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.BienDongKho b ON b.MaBienDong=i.MaBienDongGoc
              WHERE i.MaPhuTung<>b.MaPhuTung OR b.Loai<>N'Xuất' OR i.Chieu<>'NHAP')
        THROW 51209,N'Hoàn kho phải tham chiếu đúng mặt hàng và biến động xuất gốc.',1;
    IF EXISTS(SELECT 1 FROM dbo.ChiTietDieuChinhKho d JOIN dbo.PhieuDieuChinhKho h
        ON h.MaPhieuDieuChinh=d.MaPhieuDieuChinh
        JOIN dbo.BienDongKho b ON b.MaBienDong=d.MaBienDongGoc
        WHERE h.TrangThai='DA_GHI_NHAN' AND d.MaBienDongGoc IN(SELECT MaBienDongGoc FROM inserted)
        GROUP BY b.MaBienDong,b.TonTruoc,b.TonSau HAVING SUM(d.SoLuong)>b.TonTruoc-b.TonSau)
        THROW 51210,N'Tổng hoàn vượt lượng xuất gốc.',1;
END;
GO
-- Chứng từ hoàn đã ghi không được sửa/xóa; phiếu NHAP vẫn có thể thay đổi.
CREATE OR ALTER TRIGGER dbo.trg_DieuChinhKho_BatBien ON dbo.ChiTietDieuChinhKho
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted d JOIN dbo.PhieuDieuChinhKho h ON h.MaPhieuDieuChinh=d.MaPhieuDieuChinh
        WHERE h.TrangThai='DA_GHI_NHAN')
        THROW 51211,N'Không sửa/xóa dòng hoàn đã ghi nhận.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhieuDieuChinhKho_BatBien ON dbo.PhieuDieuChinhKho
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted WHERE TrangThai='DA_GHI_NHAN')
        THROW 51212,N'Không sửa/xóa phiếu điều chỉnh đã ghi nhận.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhuTungSuDung_BatBien ON dbo.PhuTungSuDung
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted WHERE TrangThai='DA_GHI_NHAN')
        THROW 51213,N'Không sửa/xóa lần xuất đã ghi nhận; dùng chứng từ hoàn.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_BienDongKho_BatBien ON dbo.BienDongKho
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted) THROW 51214,N'Không sửa/xóa nhật ký biến động; ghi chứng từ bù trừ.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_BaoCaoKiemTra_LichSu ON dbo.ChiTietKiemTra
AFTER UPDATE,DELETE AS
BEGIN
    SET NOCOUNT ON;
    INSERT dbo.LichSuBaoCaoKiemTra(MaBaoCao,ThoiDiem,NoiDungTruoc)
    SELECT x.MaBaoCao,dbo.fn_ThoiDiemGarage(),
        (SELECT d.* FROM deleted d WHERE d.MaBaoCao=x.MaBaoCao FOR JSON PATH)
    FROM(SELECT DISTINCT MaBaoCao FROM deleted) x;
END;
GO
-- Chặn chốt thu với hóa đơn thiếu phụ lục, sai lượng hoàn hoặc sai tổng.
-- Không tự sửa số tiền âm thầm của DAO: nếu DAO dùng báo giá cũ, transaction bị từ chối.
CREATE OR ALTER TRIGGER dbo.trg_PhieuSuaChua_ChotThanhToan ON dbo.PhieuSuaChua
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(DaThanhToan) RETURN;
    IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.MaPhieu=i.MaPhieu
        WHERE d.DaThanhToan=1 AND i.DaThanhToan=0)
        THROW 51220,N'Không bỏ cờ đã thu bằng sửa trực tiếp; dùng chứng từ điều chỉnh.',1;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.HoaDon h ON h.MaPhieuSuaChua=i.MaPhieu
        LEFT JOIN dbo.vw_SoTienDaXacMinh s ON s.MaLenh=i.MaPhieu
        WHERE i.DaThanhToan=1 AND COALESCE(s.DaThu,0)<>h.TongTien)
        THROW 51227,N'Phải có giao dịch xác minh đủ tiền trước khi chốt hóa đơn.',1;
    IF EXISTS(SELECT 1 FROM inserted i
        WHERE i.DaThanhToan=1 AND
          (i.TrangThai<>'HOAN_THANH' OR NOT EXISTS(
            SELECT 1 FROM dbo.HoaDon h JOIN dbo.vw_TongPhaiThu t ON t.MaPhieu=h.MaPhieuSuaChua
            WHERE h.MaPhieuSuaChua=i.MaPhieu AND h.TongTien=t.TongPhaiThu)))
        THROW 51221,N'Hóa đơn chưa đúng tổng thực tế hoặc lệnh chưa hoàn tất.',1;
    IF EXISTS(
        SELECT i.MaPhieu,c.ViTriSuaChua,c.TenHangMuc,c.Loai,c.DonVi,c.SoLuongTinhTien,c.DonGia,c.ThanhTien
        FROM inserted i JOIN dbo.vw_ChiPhiThucTe c ON c.MaPhieu=i.MaPhieu
        WHERE i.DaThanhToan=1 AND c.SoLuongTinhTien>0
        EXCEPT
        SELECT i.MaPhieu,c.ViTri,c.TenHangMuc,c.Loai,c.DonVi,c.SoLuong,c.DonGia,c.ThanhTien
        FROM inserted i JOIN dbo.HoaDon h ON h.MaPhieuSuaChua=i.MaPhieu
        JOIN dbo.ChiTietHoaDon c ON c.MaHoaDon=h.MaHoaDon WHERE i.DaThanhToan=1)
        OR EXISTS(
        SELECT i.MaPhieu,c.ViTri,c.TenHangMuc,c.Loai,c.DonVi,c.SoLuong,c.DonGia,c.ThanhTien
        FROM inserted i JOIN dbo.HoaDon h ON h.MaPhieuSuaChua=i.MaPhieu
        JOIN dbo.ChiTietHoaDon c ON c.MaHoaDon=h.MaHoaDon WHERE i.DaThanhToan=1
        EXCEPT
        SELECT i.MaPhieu,c.ViTriSuaChua,c.TenHangMuc,c.Loai,c.DonVi,c.SoLuongTinhTien,c.DonGia,c.ThanhTien
        FROM inserted i JOIN dbo.vw_ChiPhiThucTe c ON c.MaPhieu=i.MaPhieu
        WHERE i.DaThanhToan=1 AND c.SoLuongTinhTien>0)
        THROW 51222,N'Chi tiết hóa đơn không khớp công việc/phát sinh/thực dùng sau hoàn.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_HoaDon_BatBien ON dbo.HoaDon
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted d JOIN dbo.PhieuSuaChua p ON p.MaPhieu=d.MaPhieuSuaChua WHERE p.DaThanhToan=1)
        THROW 51223,N'Hóa đơn đã thu là snapshot bất biến; lập chứng từ điều chỉnh.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_ChiTietHoaDon_BatBien ON dbo.ChiTietHoaDon
AFTER INSERT,UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM dbo.HoaDon h JOIN dbo.PhieuSuaChua p ON p.MaPhieu=h.MaPhieuSuaChua
        JOIN(SELECT MaHoaDon FROM inserted UNION SELECT MaHoaDon FROM deleted) x ON x.MaHoaDon=h.MaHoaDon
        WHERE p.DaThanhToan=1)
        THROW 51224,N'Không thay đổi dòng hóa đơn sau khi chốt thu.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_BanGiaoXe_DieuKien ON dbo.BanGiaoXe
AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuSuaChua p ON p.MaPhieu=i.MaLenh
        WHERE p.TrangThai<>'HOAN_THANH' OR
        (i.MaPheDuyetNgoaiLe IS NULL AND NOT EXISTS(
           SELECT 1 FROM dbo.vw_CanCuBanGiao c WHERE c.MaLenh=i.MaLenh AND c.MaChungTu=i.MaHoaDon))
        OR (i.MaPheDuyetNgoaiLe IS NOT NULL AND NOT EXISTS(
           SELECT 1 FROM dbo.vw_CanCuBanGiaoMoRong c WHERE c.MaLenh=i.MaLenh
              AND c.MaChungTu=CONVERT(VARCHAR(40),CONCAT('NGOAI_LE-',i.MaPheDuyetNgoaiLe)) AND c.NgoaiLe=1)))
        THROW 51225,N'Chưa đủ điều kiện thanh toán/phê duyệt để bàn giao.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_BanGiaoXe_BatBien ON dbo.BanGiaoXe
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted) THROW 51226,N'Không sửa/xóa biên bản bàn giao đã ghi.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_TaiKhoan_MatKhauMoi ON dbo.TaiKhoan
AFTER INSERT,UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i LEFT JOIN deleted d ON d.MaNhanVien=i.MaNhanVien
        WHERE (d.MaNhanVien IS NULL OR i.MatKhau<>d.MatKhau)
          AND (i.MatKhau NOT LIKE N'!PBKDF2!%' OR LEN(i.MatKhau)<40))
        THROW 51230,N'Không ghi mật khẩu rõ. Dùng hash có salt và usp_DatMatKhauBam.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_GiaoDichThanhToan_BaoVe ON dbo.GiaoDichThanhToan
AFTER INSERT,UPDATE,DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM deleted d LEFT JOIN inserted i ON i.MaGiaoDichNoiBo=d.MaGiaoDichNoiBo
        WHERE d.TrangThai IN('DA_XAC_MINH','DA_HOAN') AND
        (i.MaGiaoDichNoiBo IS NULL OR i.SoTien<>d.SoTien OR i.TrangThai<>d.TrangThai OR i.MaLenh<>d.MaLenh
         OR i.HinhThuc<>d.HinhThuc OR i.MaThamChieu<>d.MaThamChieu OR i.LoaiGiaoDich<>d.LoaiGiaoDich
         OR i.MaThaoTac<>d.MaThaoTac OR i.NguoiThucHien<>d.NguoiThucHien
         OR i.ThoiDiemTao<>d.ThoiDiemTao OR COALESCE(i.MaDieuChinh,-1)<>COALESCE(d.MaDieuChinh,-1)
         OR COALESCE(i.NguoiXacMinh,'')<>COALESCE(d.NguoiXacMinh,'')
         OR COALESCE(i.ChungCuXacMinh,N'')<>COALESCE(d.ChungCuXacMinh,N'')
         OR COALESCE(i.ThoiDiemXacMinh,'19000101')<>COALESCE(d.ThoiDiemXacMinh,'19000101')
         OR (d.MaHoaDon IS NOT NULL AND COALESCE(i.MaHoaDon,'')<>d.MaHoaDon)))
        THROW 51231,N'Không sửa số tiền/trạng thái hoặc xóa giao dịch đã xác minh.',1;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.HoaDon h ON h.MaHoaDon=i.MaHoaDon
        WHERE i.MaLenh<>h.MaPhieuSuaChua)
        THROW 51232,N'Giao dịch và hóa đơn phải thuộc cùng lệnh.',1;
    IF EXISTS(SELECT 1 FROM inserted WHERE TrangThai IN('DA_XAC_MINH','DA_HOAN')
        AND NULLIF(LTRIM(RTRIM(ChungCuXacMinh)),N'') IS NULL)
        THROW 51233,N'Giao dịch xác minh cần căn cứ thu/chi.',1;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.vw_SoTienDaXacMinh s ON s.MaLenh=i.MaLenh
        WHERE i.LoaiGiaoDich='THU' AND i.TrangThai='DA_XAC_MINH'
          AND s.DaThu>COALESCE(
            (SELECT TOP(1) CONVERT(DECIMAL(38,0),h.TongTien)+COALESCE(
                (SELECT SUM(CONVERT(DECIMAL(38,0),d.SoTienDieuChinh)) FROM dbo.DieuChinhHoaDon d
                 WHERE d.MaHoaDon=h.MaHoaDon AND d.TrangThai='DA_DUYET'),0)
             FROM dbo.HoaDon h WHERE h.MaPhieuSuaChua=i.MaLenh),
            (SELECT CONVERT(DECIMAL(38,0),dc.TongPhaiThu) FROM dbo.DoiChieuChiPhi dc
             WHERE dc.MaPhieu=i.MaLenh AND dc.TrangThai='DA_XAC_NHAN')))
        THROW 51237,N'Tổng tiền xác minh vượt nghĩa vụ phải thu hiện tại.',1;
    IF EXISTS(SELECT 1 FROM inserted i LEFT JOIN dbo.DieuChinhHoaDon d ON d.MaDieuChinh=i.MaDieuChinh
        WHERE i.LoaiGiaoDich='HOAN_TIEN' AND i.TrangThai='DA_HOAN'
          AND (d.MaDieuChinh IS NULL OR d.MaHoaDon<>i.MaHoaDon OR d.TrangThai<>'DA_DUYET' OR d.SoTienDieuChinh>=0))
        THROW 51235,N'Hoàn tiền phải có đúng điều chỉnh giảm đã được duyệt.',1;
    IF EXISTS(SELECT 1 FROM dbo.GiaoDichThanhToan g JOIN dbo.DieuChinhHoaDon d ON d.MaDieuChinh=g.MaDieuChinh
        WHERE g.TrangThai='DA_HOAN' AND g.LoaiGiaoDich='HOAN_TIEN'
          AND g.MaDieuChinh IN(SELECT MaDieuChinh FROM inserted)
        GROUP BY g.MaDieuChinh,d.SoTienDieuChinh HAVING SUM(CONVERT(DECIMAL(38,0),g.SoTien))>-d.SoTienDieuChinh)
        THROW 51236,N'Tổng hoàn vượt điều chỉnh giảm được duyệt.',1;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.vw_SoTienDaXacMinh s ON s.MaLenh=i.MaLenh
        WHERE i.LoaiGiaoDich='HOAN_TIEN' AND i.TrangThai='DA_HOAN'
          AND s.DaThu<(SELECT CONVERT(DECIMAL(38,0),h.TongTien)+COALESCE(
                (SELECT SUM(CONVERT(DECIMAL(38,0),d.SoTienDieuChinh)) FROM dbo.DieuChinhHoaDon d
                 WHERE d.MaHoaDon=h.MaHoaDon AND d.TrangThai='DA_DUYET'),0)
             FROM dbo.HoaDon h WHERE h.MaPhieuSuaChua=i.MaLenh))
        THROW 51238,N'Hoàn tiền làm số thực nhận thấp hơn nghĩa vụ sau điều chỉnh.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_DieuChinhHoaDon_BatBien ON dbo.DieuChinhHoaDon
AFTER UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM deleted WHERE TrangThai='DA_DUYET')
        THROW 51234,N'Không sửa/xóa điều chỉnh đã duyệt.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_XuatPhuTung_DungPhamVi ON dbo.PhuTungSuDung
AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuSuaChua p ON p.MaPhieu=i.MaLenhSuaChua
        WHERE i.TrangThai='DA_GHI_NHAN' AND
        (p.TrangThai='HOAN_THANH' OR p.DaThanhToan=1 OR NOT EXISTS(
            SELECT 1 FROM dbo.vw_HangMucDuocDuyet h WHERE h.MaPhieu=i.MaLenhSuaChua
                AND h.ViTriSuaChua=i.ViTriHangMuc AND h.MaPhuTung=i.MaPhuTung)))
        THROW 51240,N'Không được xuất ngoài phạm vi duyệt hoặc sau hoàn tất.',1;
    IF EXISTS(SELECT 1 FROM dbo.vw_PhuTungThucDung s JOIN dbo.vw_HangMucDuocDuyet h
        ON h.MaPhieu=s.MaLenhSuaChua AND h.ViTriSuaChua=s.ViTriHangMuc AND h.MaPhuTung=s.MaPhuTung
        WHERE EXISTS(SELECT 1 FROM inserted i WHERE i.MaLenhSuaChua=s.MaLenhSuaChua AND i.ViTriHangMuc=s.ViTriHangMuc)
        GROUP BY s.MaLenhSuaChua,s.ViTriHangMuc,s.MaPhuTung,h.SoLuongDuocDuyet
        HAVING SUM(s.ThucDung)>h.SoLuongDuocDuyet OR SUM(s.ThucDung)<0)
        THROW 51241,N'Lượng thực dùng vượt phần đã được khách duyệt.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_CongViec_KhoaDaThu ON dbo.ChiTietSuaChua
AFTER INSERT,UPDATE,DELETE AS
BEGIN
    IF EXISTS(SELECT 1 FROM dbo.PhieuSuaChua p
        JOIN(SELECT MaPhieu FROM inserted UNION SELECT MaPhieu FROM deleted) x ON x.MaPhieu=p.MaPhieu
        WHERE p.DaThanhToan=1)
        THROW 51242,N'Không thay đổi công việc của lệnh đã thu; lập hồ sơ điều chỉnh.',1;
END;
GO

CREATE OR ALTER TRIGGER dbo.trg_HoaDon_DungLenh ON dbo.HoaDon
AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuSuaChua sc ON sc.MaPhieu=i.MaPhieuSuaChua
        JOIN dbo.BaoGia bg ON bg.MaBaoGia=sc.MaBaoGia
        JOIN dbo.NhomBaoGia n ON n.MaBaoGiaGoc=bg.MaBaoGiaGoc
        JOIN dbo.BaoCaoKiemTra kt ON kt.MaBaoCao=n.MaBaoCao
        JOIN dbo.PhieuTiepNhan tn ON tn.MaPhieu=kt.MaPhieuTiepNhan
        WHERE sc.TrangThai<>'HOAN_THANH' OR bg.TrangThai<>'DA_DONG_Y'
          OR i.TenKhachHang<>tn.TenKhach OR i.BienSo<>sc.BienSo
          OR EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh d WHERE d.MaPhieu=sc.MaPhieu AND d.TrangThai='CHO_KHACH')
          OR NOT EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=sc.MaPhieu))
        THROW 51250,N'Hóa đơn sai khách/xe hoặc lệnh chưa đủ điều kiện hoàn tất.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhieuSuaChua_HoanTat ON dbo.PhieuSuaChua
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(TrangThai) RETURN;
    IF EXISTS(SELECT 1 FROM inserted i JOIN deleted d ON d.MaPhieu=i.MaPhieu
        WHERE i.TrangThai='HOAN_THANH' AND d.TrangThai<>'HOAN_THANH'
          AND (NOT EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=i.MaPhieu)
            OR EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=i.MaPhieu
                       AND (c.TrangThai<>'HOAN_THANH' OR NULLIF(LTRIM(RTRIM(c.KetQua)),N'') IS NULL))
            OR EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh p WHERE p.MaPhieu=i.MaPhieu AND p.TrangThai='CHO_KHACH')
            OR EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=i.MaPhieu AND NOT EXISTS(
                SELECT 1 FROM dbo.vw_HangMucDuocDuyet h WHERE h.MaPhieu=c.MaPhieu AND h.ViTriSuaChua=c.ViTri))))
        THROW 51251,N'Chưa hoàn thành công việc hoặc còn phát sinh/căn cứ chưa duyệt.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhatSinh_ChiGuiChoKhach ON dbo.DeXuatPhatSinh
AFTER INSERT AS
BEGIN
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuSuaChua p ON p.MaPhieu=i.MaPhieu
        JOIN dbo.TaiKhoan t ON t.MaNhanVien=i.NguoiBao
        WHERE i.TrangThai<>'CHO_KHACH' OR p.TrangThai='HOAN_THANH' OR p.DaThanhToan=1
          OR t.HoatDong=0 OR t.VaiTro<>'NHAN_VIEN_KY_THUAT'
          OR (i.NguoiBao<>p.KyThuatChinh AND COALESCE(p.KyThuatPhoiHop,'')<>i.NguoiBao))
        THROW 51252,N'Phát sinh mới phải chờ khách và do kỹ thuật được phân công báo trước khi hoàn tất.',1;
END;
GO

-- PHẦN 6: ĐỐI CHIẾU SAU CÀI ĐẶT/NÂNG CẤP, KHÔNG SỬA ÂM THẦM DỮ LIỆU VẬN HÀNH.
CREATE OR ALTER VIEW dbo.vw_KiemTraNghiepVu
AS
SELECT 'HD_TONG_CHI_TIET' AS MaLoi,CONVERT(VARCHAR(100),h.MaHoaDon) AS MaChungTu,
       CONVERT(NVARCHAR(1000),N'Tổng hóa đơn khác tổng chi tiết + thuế - giảm giá.') AS MoTa
FROM dbo.HoaDon h LEFT JOIN dbo.ChiTietHoaDon c ON c.MaHoaDon=h.MaHoaDon
GROUP BY h.MaHoaDon,h.TongTien,h.TienThue,h.TienGiam
HAVING CONVERT(DECIMAL(38,0),h.TongTien)<>COALESCE(SUM(CONVERT(DECIMAL(38,0),c.ThanhTien)),0)+h.TienThue-h.TienGiam
UNION ALL
SELECT 'HD_CO_THANH_TOAN',h.MaHoaDon,N'Hóa đơn thu đủ legacy nhưng cờ thanh toán trên lệnh chưa đồng bộ.'
FROM dbo.HoaDon h JOIN dbo.PhieuSuaChua p ON p.MaPhieu=h.MaPhieuSuaChua WHERE p.DaThanhToan=0
UNION ALL
SELECT 'SC_THIEU_HANG_MUC',p.MaPhieu,N'Lệnh hoàn thành chưa có chi tiết sửa chữa.'
FROM dbo.PhieuSuaChua p WHERE p.TrangThai='HOAN_THANH'
AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietSuaChua c WHERE c.MaPhieu=p.MaPhieu)
UNION ALL
SELECT 'SC_CON_CHO_KHACH',p.MaPhieu,N'Lệnh hoàn thành còn phát sinh chưa có quyết định (có thể là dữ liệu legacy cần đối chiếu).'
FROM dbo.PhieuSuaChua p WHERE p.TrangThai='HOAN_THANH'
AND EXISTS(SELECT 1 FROM dbo.DeXuatPhatSinh d WHERE d.MaPhieu=p.MaPhieu AND d.TrangThai='CHO_KHACH')
UNION ALL
SELECT 'HOAN_VUOT_XUAT',s.MaGhiNhan,N'Lượng đã hoàn vượt lượng xuất.'
FROM dbo.vw_PhuTungThucDung s WHERE s.ThucDung<0
UNION ALL
SELECT 'XUAT_THIEU_NHAT_KY',s.MaGhiNhan,N'Lần xuất chưa có duy nhất biến động gốc đúng mã và lượng.'
FROM dbo.PhuTungSuDung s WHERE s.TrangThai='DA_GHI_NHAN'
AND (SELECT COUNT(*) FROM dbo.BienDongKho b WHERE b.ChungTu=s.MaGhiNhan
     AND b.MaPhuTung=s.MaPhuTung AND b.Loai=N'Xuất' AND b.TonTruoc-b.TonSau=s.SoLuong)<>1
UNION ALL
SELECT 'TAI_KHOAN_CHUA_HASH',t.MaNhanVien,N'Tài khoản legacy chưa có hash/salt; cần đặt lại trước khi dùng DAO mới.'
FROM dbo.TaiKhoan t WHERE NOT EXISTS(SELECT 1 FROM dbo.TaiKhoanThongTinBaoMat b WHERE b.MaNhanVien=t.MaNhanVien)
UNION ALL
SELECT 'DON_VI_SO_LE',p.MaPhuTung,N'Tồn/số tối thiểu có số lẻ không phù hợp đơn vị.'
FROM dbo.PhuTung p JOIN dbo.DonViTinh u ON u.DonVi=p.DonViTinh
WHERE u.ChoPhepSoLe=0 AND (p.SoLuongTon%1<>0 OR p.MucToiThieu%1<>0);
GO
IF OBJECT_ID(N'dbo.LichSuNangCap',N'U') IS NULL
CREATE TABLE dbo.LichSuNangCap (
    PhienBan VARCHAR(50) PRIMARY KEY,
    ThoiDiem DATETIME2(3) NOT NULL,
    MoTa NVARCHAR(2000) NOT NULL
);
IF NOT EXISTS(SELECT 1 FROM dbo.LichSuNangCap WHERE PhienBan='2026-11-10-nghiep-vu-v3')
INSERT dbo.LichSuNangCap VALUES('2026-11-10-nghiep-vu-v3',dbo.fn_ThoiDiemGarage(),
    N'Schema linh hoạt, phát sinh, hoàn kho, chi phí thực tế, thanh toán xác minh, bàn giao và bảo mật.');

SELECT * FROM dbo.vw_KiemTraNghiepVu ORDER BY MaLoi,MaChungTu;
SELECT * FROM dbo.vw_PhieuChoThu ORDER BY MaLenh;
SELECT MaPhuTung,TenPhuTung,SoLuongTon FROM dbo.vw_TonKho ORDER BY MaPhuTung;
PRINT N'Đã cập nhật schema. Hãy đọc database/README.md và nối DAO với API SQL mới.';
GO
