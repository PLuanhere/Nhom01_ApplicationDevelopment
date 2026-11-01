# Ảnh giao diện Page 3

Ảnh và icon được lấy từ file Figma `XEDrqEs0dSe51rsn2PIQ6s`, Page 3. Ứng dụng đọc tài nguyên qua classpath, không tải ảnh từ Internet lúc chạy.

| Tệp | Frame/layer gốc | Kích thước sử dụng |
| --- | --- | --- |
| login-background.png | 2477:1048 — ảnh nền login | Cover, giữ tỷ lệ và cắt giữa |
| car-login.png | 2526:6793 | 16×16 |
| car.png | 2526:7259 | 25×25 |
| dashboard.png | 2526:7265 | 17×17 |
| users.png | 2526:7270 | 17×17 |
| shield.png | 2526:7274 | 17×17 |
| clipboard.png | 2526:7277 | 17×17 |
| package.png | 2526:7281 | 17×17 |
| truck.png | 2526:7285 | 17×17 |
| chart.png | 2526:7290 | 17×17 |
| search.png | 2526:7293 | 14×14 |

Icon là bản PNG do Figma xuất từ đúng layer vector để Swing dùng được bằng ImageIcon, không cần thư viện SVG.

## Icon tiếp nhận

`reception-home.png`, `reception-file.png`, `reception-wrench.png`, `reception-key.png` lấy từ asset SVG của frame Figma `2479:10018` (Page 3), chuyển PNG giữ kích thước gốc để Swing đọc bằng ImageIO. Các icon clipboard/users/car tái sử dụng tài nguyên chung.

`reception-car.png`: icon Phương tiện trong sidebar tiếp nhận, layer `2526:8327` (slot `2479:10045`), 17×17, màu #CBD9E6; xuất từ asset SVG Figma giữ kích thước gốc. `car.png` 25×25 tiếp tục dùng cho thương hiệu.
