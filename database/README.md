# Cơ sở dữ liệu Garage Đức Dũng

Script chính là [QuanLiGarage.sql](QuanLiGarage.sql). Bản ngày 10/11/2026 bổ sung schema và API nghiệp vụ, giữ các bảng lõi và dữ liệu hiện có. File dùng cho SQL Server 2016 SP1 trở lên, compatibility level từ 130.

## Chạy script

1. Backup database đang dùng.
2. Chạy **toàn bộ file** trong một kết nối; các phần dùng chung bảng tạm cấu hình. Với database mới, thay `@TaoDuLieuMau=0` nếu cần database trống cho triển khai. Mặc định chỉ gieo demo khi bảng nhân viên chưa tồn tại.
3. Nên dùng `sqlcmd -b` để dừng khi có lỗi. Ví dụ dưới đây dùng Windows Authentication và chấp nhận chứng chỉ của SQL Server cục bộ; thay server bằng instance/cổng thực tế trên máy:

   ```powershell
   sqlcmd -S ".\SQLEXPRESS" -E -C -b -I -i "database\QuanLiGarage.sql"
   ```

4. Xem kết quả `vw_KiemTraNghiepVu` cuối file. Mỗi dòng là dữ liệu cần đối chiếu, không phải lệnh tự sửa dữ liệu vận hành.

Script không xóa/tạo lại database đang tồn tại, không reset sequence, không gieo lại demo trên database đã có bảng nhân viên. Các bảng/cột/index mới được tạo có điều kiện; view/procedure/trigger được cập nhật. DDL và từng nhóm chuyển đổi có transaction; toàn file gồm nhiều batch, nên không phải một transaction duy nhất cho toàn bộ nâng cấp. Nếu gặp lỗi phải dừng, kiểm tra nguyên nhân và backup trước khi chạy tiếp.

Cách dùng `-b` được mô tả trong [tài liệu sqlcmd của Microsoft](https://learn.microsoft.com/en-us/sql/tools/sqlcmd/sqlcmd-utility).

## Phạm vi đã bổ sung ở SQL

- Các bảng mà DAO main còn thiếu: chi tiết kiểm tra; đề xuất/chi tiết/phản hồi phát sinh; liên kết phát sinh với công việc; phiếu/chi tiết hoàn và điều chỉnh kho.
- Lượng thực dùng bằng xuất đã ghi nhận trừ hoàn đã ghi nhận; giữ nguyên chứng từ xuất gốc.
- Chi phí gồm công việc hoàn tất, phụ tùng dùng ròng và phụ lục được khách đồng ý.
- Snapshot đối chiếu, thuế/giảm giá có lý do, dòng hóa đơn và liên kết về công việc.
- Giao dịch thu nhiều lần, chờ xác minh, xác minh thủ công có căn cứ; điều chỉnh giảm/tăng sau hóa đơn và hoàn tiền đã duyệt.
- Phê duyệt bàn giao khi còn nợ, thời hạn và số nợ phải khớp hiện tại; mỗi lệnh chỉ bàn giao một lần.
- Cấp mã bằng sequence bền vững; phiếu nhập SQL với header/detail/tồn/nhật ký cùng transaction; lưu snapshot báo cáo.
- Danh mục đơn vị có quy tắc số lẻ; thông tin garage, chính sách giá vốn chưa chốt, thời gian nghiệp vụ Việt Nam.
- Salt/hash PBKDF2, đánh dấu tài khoản cần đổi mật khẩu; không ghi mật khẩu rõ mới.
- Trigger chặn sửa báo giá đã gửi, hóa đơn đã chốt, xuất/hoàn đã xác nhận và sửa/xóa nhật ký.

Các giá/địa chỉ/tài khoản ngân hàng/thuế chưa được gara xác nhận không được tự điền làm dữ liệu thật. Thông tin garage mặc định chưa sử dụng; phương pháp giá vốn là `CHUA_CHOT`. `GiaNhap` chưa được tự diễn giải thành giá vốn kế toán. Dữ liệu mẫu là minh họa.

## Nguồn đọc và thứ tự nghiệp vụ

| Bước | Nguồn SQL / procedure | Cách sử dụng |
|---|---|---|
| Kiểm tra | BaoCaoKiemTra + ChiTietKiemTra | DAO mới lưu header/detail cùng transaction. Một phiếu hiện giữ một báo cáo chính để tương thích; LichSuBaoCaoKiemTra lưu ảnh nội dung detail trước chỉnh sửa. Đây chưa phải mô hình nhiều lần tái khám độc lập. |
| Báo giá | BaoGia + ChiTietBaoGia; usp_SuaBaoGiaNhap | Đọc PhienBanKhoa (rowversion), truyền lại khi sửa nháp. Bản gửi/duyệt không được sửa dòng giá. Điều chỉnh tạo phiên bản mới như DAO hiện có. |
| Phát sinh | DeXuatPhatSinh, ChiTietDeXuatPhatSinh, PhanHoiPhatSinh, ChiTietSuaChuaPhatSinh | Khớp PhatSinhDAO main: lưu giá khi chờ; ghi phản hồi; đồng ý mới thêm công việc/link; từ chối không tạo việc. Không sửa phụ lục sau phản hồi. |
| Nhập kho | usp_NhapKho | Truyền người, nguồn, chứng từ, JSON dòng hàng và mã thao tác. Không chỉ tăng tồn rồi giữ phiếu trong RAM. |
| Hoàn kho | usp_HoanPhuTung hoặc HoanPhuTungDAO | Giới hạn theo lần xuất, tạo chứng từ bù. Cho hoàn khi lệnh vừa hoàn thành nhưng phải trước khi Thu ngân xác nhận đối chiếu; sau đối chiếu/hóa đơn phải dùng quy trình hủy hoặc điều chỉnh có thẩm quyền. |
| Đối chiếu | vw_HangMucDuocDuyet → vw_PhuTungThucDung → vw_ChiPhiThucTe; usp_XacNhanDoiChieu | Hiển thị lượng duyệt/dùng ròng/chênh lệch; xác nhận thuế/giảm giá có căn cứ. Không cộng thêm một lần nữa các dòng phát sinh đã nằm trong nguồn. |
| Ghi tiền | usp_GhiNhanThanhToan | Số tiền là tiền thực thu của giao dịch; tiền mặt khách đưa và tiền thối là thông tin riêng, không cộng vào doanh thu. Trả mã giao dịch nội bộ. |
| Xác minh | usp_XacMinhThanhToan | Thu ngân xác minh sao kê/tiền mặt, truyền căn cứ. Mã tham chiếu không chứng minh tiền đã về. SQL không kết nối ngân hàng và không tạo QR. |
| Hóa đơn | usp_LapHoaDonDaThu | Cần lệnh hoàn tất, đối chiếu đã xác nhận và tổng tiền xác minh đúng phải thu. Tạo snapshot từ chi phí thực tế; gắn giao dịch rồi cập nhật cờ đã thu cùng transaction. |
| Điều chỉnh sau thu | usp_DuyetDieuChinhHoaDon → usp_GhiNhanHoanTien | Quản lý duyệt số tiền điều chỉnh có dấu; thu ngân ghi khoản hoàn thực tế với căn cứ. Không sửa hóa đơn gốc. Số hoàn không vượt khoản giảm được duyệt. |
| Bàn giao | vw_NghiaVuThanhToan; vw_CanCuBanGiao / vw_CanCuBanGiaoMoRong; usp_PheDuyetBanGiaoNgoaiLe → usp_BanGiaoXe | Tiếp nhận bàn giao. Ngoại lệ do quản lý phê duyệt, còn hạn và khớp nghĩa vụ hiện hành kể cả điều chỉnh sau hóa đơn; truyền giờ giao thực tế. |
| Báo cáo | usp_LuuBaoCao; vw_DoanhThuDaThu, vw_TonKho | Lưu snapshot có chỉ số/cột/ô. Phân biệt tiền thu, điều chỉnh chứng từ, số tiền đã hoàn và lợi nhuận. |
| Chẩn đoán dữ liệu | vw_KiemTraNghiepVu | Kiểm tra dữ liệu legacy thiếu chứng từ/cờ/hash; không ghi giả chứng từ để làm hết cảnh báo. |

`usp_CapMaChungTu` trả mã qua tham số OUTPUT; các procedure tạo chứng từ trả một result set chứa mã. Mã thao tác phải được giữ nguyên khi retry cùng nội dung. Sequence có thể có khoảng trống sau rollback; không tái sử dụng mã.

Ví dụ JSON nhập kho:

```json
[
  {"MaPhuTung":"PT001","SoLuong":4.5,"DonGia":100000},
  {"MaPhuTung":"PT002","SoLuong":2,"DonGia":70000}
]
```

JSON sửa báo giá là mảng các dòng `MaDichVu/MaPhuTung, TenHangMuc, Loai, DonVi, SoLuong, DonGia`; mã không áp dụng phải là `null`. JSON lưu báo cáo: chỉ số `[{"TenChiSo":"Số hóa đơn","GiaTri":1}]`, cột `["Mã","Số tiền"]`, dòng `[["HD-...","920000"]]`.

## Trạng thái tích hợp Java

SQL không thể tự biến các ArrayList trong Java thành dữ liệu bền vững hoặc thay thế câu SELECT hardcode.

- **Đã nối trong working tree:** HoaDonDAO/DoiChieuChiPhiDAO/UI thu ngân dùng nguồn thực tế và API đối chiếu/thu/xác minh/lập hóa đơn; BanGiaoXeDAO gọi procedure; BaoGiaDAO dùng rowversion/procedure; TaiKhoanDAO xác minh và đặt lại PBKDF2; UI in hóa đơn đọc ThongTinGarage.
- **TaiKhoan còn thiếu:** UI bắt đổi mật khẩu khi `BuocDoiMatKhau=1` và quy trình tự đổi mật khẩu của chính nhân viên.
- **Tài khoản legacy:** script giữ dữ liệu cũ để không đoán mật khẩu, nhưng báo tài khoản chưa hash. Quản lý đặt lại mật khẩu trước khi dùng cơ chế mới; script không tuyên bố đã chuyển đổi được mật khẩu bất kỳ bằng T-SQL.
- **NhanVienDAO/DichVuDAO:** CRUD vào bảng SQL, dùng cùng nguồn cho tài khoản, phân công, báo giá gốc và phụ lục. Trigger chỉ yêu cầu bộ phận/vai trò khớp khi tài khoản hoạt động; nếu chuyển bộ phận, khóa tài khoản, đổi bộ phận/vai trò trong transaction rồi mở lại.
- **PhieuNhapKhoDAO/BaoCaoDAO:** dùng API SQL, đọc lại chứng từ từ DB; bỏ counter và danh sách RAM làm nguồn dữ liệu chính.
- **Báo cáo tiền theo hình thức:** nếu một hóa đơn được thu qua nhiều giao dịch/hình thức, đọc chi tiết GiaoDichThanhToan. Hình thức trên header HoaDon chỉ giữ tương thích và không đại diện cho cách chia tổng tiền của nhiều lần thu.
- **UI giá vốn/garage/đơn vị/thời gian:** đọc cấu hình SQL; mở rộng Entity/combobox tương ứng. Java vẫn phải thống nhất timezone cho các thời điểm nó tự ghi.
- SQL kiểm tra vai trò theo tham số nhân viên để giữ toàn vẹn, nhưng ứng dụng phải xác thực phiên thật và hạn chế quyền tài khoản JDBC; tham số nhân viên không phải token xác thực.

Không có trigger tự cộng/trừ kho trên bảng nhập/xuất, vì DAO main đang tự làm phần đó trong transaction. Thêm trigger cộng kho nữa sẽ gây trừ/cộng hai lần.

## Dữ liệu mẫu và nâng cấp

- Giữ các hồ sơ mẫu cũ; bổ sung chi tiết công việc, phân công, xuất thực dùng và biến động cho ba lệnh chờ thu.
- Sửa tham chiếu lần xuất mẫu từ mã lệnh thành mã ghi nhận sử dụng; đồng bộ cờ/thời điểm thanh toán của hóa đơn đã thu.
- Các lượng tồn mẫu sau bổ sung được trừ đúng theo xuất. Đơn vị công thay lốp và mã phụ tùng phanh được thống nhất trong dữ liệu mới.
- Dữ liệu báo cáo kiểm tra cũ giữ nguyên ở header, có bản chuyển detail được đánh dấu nguồn legacy.
- Phát sinh cũ đã duyệt nhưng thiếu chi tiết giá được đưa về hàng chờ đối chiếu trong bảng mới; không tự suy ra khách đồng ý giá nào. Bản cũ vẫn được giữ để kiểm tra.
- Các chứng từ tiền cũ chỉ được chuyển thành giao dịch thu khi có hóa đơn legacy (hợp đồng cũ chỉ tạo hóa đơn sau khi thu đủ). Không suy ra tiền thực nhận từ báo giá chưa có hóa đơn.
- Các snapshot hóa đơn cũ không được tự sửa giá/khối lượng cho giống giá danh mục hiện tại.

## Kiểm tra trước triển khai

Đã kiểm tra cú pháp bản hiện tại bằng Microsoft ScriptDom và biên dịch tĩnh Java. Bản SQL trước các sửa cuối đã từng được chạy trong database kiểm thử riêng; **các thay đổi cuối về thu vượt/hoàn tiền/nghĩa vụ sau hóa đơn/giờ bàn giao chưa được integration-test lại** vì dịch vụ `MSSQL$SQLEXPRESS` hiện dừng và tài khoản chạy Codex không có quyền khởi động dịch vụ.

Các ca đã từng chạy ở bản nền: dựng mới; chạy lại không nhân đôi; nâng cấp schema cũ; nhập/hoàn kho; thu nhiều lần; phát sinh; hóa đơn; bàn giao; ngoại lệ; điều chỉnh/hoàn tiền; snapshot; báo cáo; chặn sửa báo giá đã gửi. Phải chạy lại toàn bộ sau các sửa cuối trước khi triển khai.

Database legacy có ba lệnh gán hoàn tất nhưng thiếu chi tiết/thực dùng: migration giữ dữ liệu, báo lỗi đối chiếu và không đưa các lệnh đó ra thu với giá 0. Cần đối chiếu chứng từ thực tế để bổ sung; không tự bịa hạng mục hoặc trừ kho khi nâng cấp.

Kết quả kiểm thử SQL không thay thế kiểm thử Java/UI. Khi chạy câu SQL trực tiếp từ kết nối mới, bật QUOTED_IDENTIFIER/ANSI_NULLS/ANSI_WARNINGS/ARITHABORT/CONCAT_NULL_YIELDS_NULL và tắt NUMERIC_ROUNDABORT như phần đầu script để phù hợp filtered index.

Các ca cần chạy: khách đồng ý/từ chối nhiều phát sinh; hóa đơn có công/phụ tùng phát sinh; xuất-hoàn-xuất lại; chặn hoàn vượt; sửa nháp đồng thời với gửi báo giá; thu đủ qua nhiều giao dịch; giao dịch chưa xác minh; thu/hoàn/bàn giao gửi lặp; bàn giao còn nợ theo phê duyệt hết hạn; đóng/mở app vẫn giữ chứng từ; đọc/in lại hóa đơn cũ sau đổi giá; đặt lại mật khẩu và thu hồi phiên.

Phạm vi chứng từ hiện là quản lý nội bộ gara; cấu hình số tiền thuế không tự biến chứng từ thành hóa đơn điện tử theo quy định pháp luật. Chính sách giá vốn, tái kiểm tra nhiều phiên, xử lý hủy lượt sửa có đặt cọc và hàng bảo hành cần đặc tả riêng trước khi mở rộng quy trình đó.
