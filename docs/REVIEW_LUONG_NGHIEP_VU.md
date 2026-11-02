# Rà soát luồng Garage — 02/11/2026

Nhánh: `refactor/review-garage-workflow`. Giữ Entity → DAO → UI, Java 17 và dữ liệu trong bộ nhớ; chưa commit/push.

## Package theo thực thể

Source đã được chia nhóm trên main. Lần này kiểm tra lại khai báo package và các điểm gọi, không chuyển lớp sang nhóm theo vai trò.

| Package con trong `entity` và `dao` | Dữ liệu liên quan |
| --- | --- |
| `nhansu` | Nhân viên, tài khoản, vai trò, nhật ký tài khoản |
| `khachhang` | Khách hàng, xe |
| `tiepnhan` | Phiếu tiếp nhận, phân công kiểm tra |
| `kiemtra` | Báo cáo kiểm tra |
| `baogia` | Báo giá, chi tiết, quyết định khách |
| `suachua` | Lệnh, công việc, phát sinh sửa chữa |
| `dichvu` | Danh mục dịch vụ và giá |
| `phutung` | Phụ tùng, nhập kho, thực dùng, biến động tồn |
| `hoadon` | Phiếu chờ thu, hóa đơn, hình thức thanh toán, đối chiếu |
| `bangiao` | Bàn giao, căn cứ thanh toán |
| `baocao` | Số liệu tổng hợp, báo cáo đã lưu |

Ví dụ: `entity.khachhang.Xe` và `dao.khachhang.XeDAO`. `dao.chung.DuLieuGarageDAO` nối các nguồn; không đặt DAO vào package Entity. Screen/dialog vẫn chia theo vai trò để tìm giao diện dễ hơn.

## Lỗi đã sửa

1. **Báo giá sửa chữa lẫn giữa các nguồn dữ liệu:** danh sách `static` bị bổ sung khi phát hành lệnh. Chuyển thành danh sách riêng của từng DAO; dữ liệu xem thử chỉ được nạp vào constructor xem thử.
2. **Kỹ thuật bị lỗi khi xem thực dùng:** quá trình lọc gọi tìm lệnh ngoài phân công và ném lỗi. Nay lấy danh sách lệnh được xem trước rồi lọc bản ghi thuộc danh sách đó.
3. **Phiên hết hiệu lực vẫn đọc phiếu tiếp nhận/phân công:** bổ sung kiểm tra phiên ở hai cửa vào DAO.
4. **Gọi DAO trực tiếp có thể hoàn tất mà chưa xuất phụ tùng:** DAO sửa chữa kiểm tra chứng từ thực dùng của từng dòng phụ tùng trước khi hoàn tất. Giữ kiểm tra trên màn Kỹ thuật để báo lý do sớm.
5. **API thanh toán chung bỏ qua mã chuyển khoản:** mọi đường ghi chuyển khoản đều phải có mã tham chiếu. Lỗi không tạo hóa đơn hoặc bỏ phiếu khỏi danh sách chờ.
6. **Callback xem chi tiết phiếu chờ không được gọi:** dùng đúng callback và truyền mã dòng đã chọn; khi mở từ cửa sổ chính vẫn đi qua bước đối chiếu.
7. **Cảnh báo Eclipse:** bỏ import, biến và hàm không dùng; dùng lại hàm làm mới dịch vụ. Không tắt cảnh báo của trình biên dịch.

## Luồng đã kiểm tra

| Bước | Điều kiện và kết quả |
| --- | --- |
| Tiếp nhận | Xe thuộc khách được chọn; đối chiếu hồ sơ; khách xác nhận; mã thao tác chặn ghi lặp |
| Phân công/kiểm tra | Kỹ thuật viên có liên kết hợp lệ; chỉ đọc phiếu được giao; báo cáo hoàn tất mới dùng lập báo giá |
| Báo giá | Dịch vụ/phụ tùng liên kết danh mục; bản gửi giữ nội dung; khách đồng ý đúng phiên bản mới phát hành lệnh |
| Phát hành | Phân công đủ từng dòng; không tạo lệnh thứ hai cho nhóm báo giá; hai nguồn độc lập không lẫn chứng từ |
| Sửa chữa/kho | Kiểm tra người thực hiện; không xuất vượt dòng duyệt/tồn; lỗi không trừ kho; lít tối đa 3 số lẻ |
| Hoàn tất | Đủ công việc, kết quả và kiểm tra cuối; có thực dùng cho các dòng phụ tùng; còn phát sinh chờ duyệt thì chặn |
| Đối chiếu/thu tiền | Cùng lệnh và báo giá; đủ tiền; đúng quyền/phiên; không thu hai lần; chuyển khoản có mã giao dịch |
| Sau thu | Rời danh sách chờ; mở đúng hóa đơn; tiền thừa không cộng doanh thu; xem/in giữ đủ chi tiết |
| Bàn giao | Tiếp nhận đọc căn cứ từ chính hóa đơn đã thu; không giao khi chưa đủ điều kiện; không giao lặp |

Tiền thu hiện theo **báo giá đã duyệt liên kết với lệnh**, không tự đổi sang tiền tính theo lượng xuất kho. Tồn kho ghi theo thực dùng. Lệnh hoàn thành vẫn giữ trạng thái kỹ thuật “Hoàn thành”; trạng thái đã thu được xác định bằng hóa đơn liên kết, không tạo thêm cờ thanh toán riêng.

## Chưa hoàn thiện — giữ lại theo yêu cầu

- **Duyệt/từ chối phát sinh:** Kỹ thuật gửi được đề xuất nhưng Tiếp nhận chưa có bước ghi nhận quyết định khách và cập nhật lệnh/chi phí. Lệnh có phát sinh chờ duyệt bị chặn hoàn tất. Người dùng đã chọn ghi rõ phần thiếu và làm sau; không tự duyệt hoặc bỏ chặn.
- Chưa kết nối database/ngân hàng. Chuyển khoản là Thu ngân xác nhận thủ công, không có xác minh tự động từ ngân hàng. Dữ liệu thay đổi mất khi đóng ứng dụng.
- Chưa có quy tắc thuế cụ thể để triển khai phần “thuế nếu có” trong REPORT; hiện tổng thu bằng tổng các dòng báo giá.

## Kết quả kiểm tra

- 139 file source: Java 17 `-Xlint:all` và Eclipse compiler đều **0 lỗi, 0 cảnh báo**.
- 13 bộ kiểm tra DAO đạt; có kiểm tra hồi quy nhiều nguồn dữ liệu, nhiều kỹ thuật viên, phiên hết hiệu lực, hoàn tất thiếu thực dùng và chuyển khoản thiếu mã.
- Luồng dùng chung chuyển khoản → báo cáo → bàn giao → đọc lại chi tiết hóa đơn: **32 kiểm tra đạt**.
- Kiểm tra Swing thanh toán: **24 kiểm tra đạt**; điều hướng/chi tiết/in hóa đơn: **20 kiểm tra đạt**. Kiểm tra in dùng bản vẽ `Printable`, chưa in máy thật.
- Điều hướng bốn vai trò ở 1920×1234, 1366×768 và 960×600: **190 kiểm tra đạt**.
- Công cụ thao tác cửa sổ Eclipse không khởi chạy được trong phiên này. Nếu IDE còn dấu cũ: chọn project → F5 → Project → Clean; xác nhận JRE System Library là JavaSE-17.

Test hồi quy mới nằm trong `test/dao/chung/KiemTraLuongGarageTest.java`; kết quả biên dịch trong `output/workflow-review`, chương trình kiểm tra Swing và ảnh trong `output/resolve-manager-main`. `test` và `output` đang bị Git ignore. Không tự đổi quy tắc ignore hoặc force-add file.
