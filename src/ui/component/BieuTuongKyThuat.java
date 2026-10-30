package ui.component;

import java.util.Base64;
import javax.swing.Icon;
import javax.swing.ImageIcon;

// Cung cấp các biểu tượng PNG xuất từ thiết kế Figma.
public final class BieuTuongKyThuat {
    private static final String[] DU_LIEU = {
        // Biểu tượng xe.
        "iVBORw0KGgoAAAANSUhEUgAAABkAAAAZCAYAAADE6YVjAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAT5JREFUeAHtVO1NwzAQfUEdIEyQMEG7Ae4GGSFsABvABjABbNAy"
                + "QbwBYQLSCcgG7rNyUdPKH3HVH/3RJz2dFD/H9+7OBm64NmQxgTHmnWEZkLRZlr3gXPAAZeZBhf5zFzmnlvhMPjj4JutV6CfBcjHD"
                + "jqEg12TnkOTkD9mzZPdIBQ9YmTQsk51wU8OgbJZkCz9s2axbTTdrJLgoJbu/mfpW9IVr3df4lUSNeRh1yrW4wHFGpQirybcaceQS"
                + "K+ptCxqWbudUUrA1l8HG6wTDfbAj+YphZHOhbfzWkdcThqb3E0cf5CdCMIdb/i9R+wZAGr4Zh0SiOtUtHHvHebeZ1+QjqY376bAO"
                + "Kom/ZCn7NSJOGnPBfvicYOKkx3zk4iqPKidOCiTAHC5wc7rmcvKN4cHbIQHUdzzgC+En6IYrwR6eIYFWa48X4AAAAABJRU5ErkJg"
                + "gg==",
        // Biểu tượng công việc kỹ thuật.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAVFJREFUeAHlkk9OwlAQxmfmPdiKW0MCnMBC2Nu9EOAEeAS8QY+A"
                + "J7CcQBNwLWvQAGsXhRvUNe+P86olKljEuGOatO9NZ37zfS8P4LjiYRJ5w+mLn1WDP/0YzVZdq22fSwrvGTtv1svVXbW0c/pzdGU1"
                + "hAbsQBnz0SjCVNmvIMZAYBHCVr3Skwj3bq2EGowmUWiIZnezqJAJSQqQSmhokBaTAU+uYYyEF24vlfT2KnGh+OlUK7GS4Fswpy6H"
                + "QNdJkxBRJoQnBgg2zttc7PYMWjLIUxra2uiAUePL2tnqS89nG7k19IGwZUn6jXpxMZxGARGVQKtXkxNdnhgDydb3wZgCnGciPAHK"
                + "+26SO8QECDQH14w4V6RunMUt9clL4y0RbANYUbNWXMCeSO3w4cle4w+AjZ3R0/KRP2ULsOSEdwhgo2QtbEcY0eblOd+R3iGAf4s3"
                + "KGySLXGxZ0cAAAAASUVORK5CYII=",
        // Biểu tượng công việc được phân công.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAPlJREFUeAHNkT1OAzEQhZ/H3kWiSo2E5JyA3x73CIXcIEfYGwAn"
                + "WTgBSCi00EMUbrDsDbaG9Qy2siny4yJKkbxiZI/0Pkufgb3N87TqYcOormjzP/R+MzSmxQfIlDcXxw+vn5Uj8jbdPny/Pj+qTTzn"
                + "DMdEpfHSkEIDpR+7FwqR7CQJIZYwn9T8Pp7UdxAZgXIX6dh5thIbhNrW4CeKZYWXwWW/ePuqbkX5NHRFrJqJDV5qNriPe4YeQMgl"
                + "IevEsijXaj8cnvUb7DxR7KZyTVe08P4UWn/PxYZ1MZ5UI2bYVJn0QbkoFlmJJbEi+ir8XxICJdMw6/0R+w9B+n5/wponxwAAAABJ"
                + "RU5ErkJggg==",
        // Biểu tượng kiểm tra.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAXBJREFUeAGllEFSwjAUhl8etWvWjiJduBYR9mXNdMAbyAngBuIN"
                + "vEkRrFvrVluLJ0jLwq3dS/NMqhlxJkgd/5m0L69/v7wkTRkY5Ce8br9DU7B1fTPvdY9Dk5+ZkvOILxngicEeep1GbyskSLIxFTRU"
                + "MQFzgbDndQ9D/fwu4sMC0GdAZa4gehh0namKsQTE/EIUcE2sFqoGBhUEeTnAlwcZG8+T1UDlLHURAprAZKlnh1eqv4jSCcB6ehtn"
                + "XEOIqI5Aaf/b4zLBWjK8QTCKRoQ1LuRsdSOGuaDayOS2TEmv48zkbQYVhfAHqa3/FySIs0u7YEllyCJOff+RtzYBcmEnAq2hyW9c"
                + "EyTMLaR7CerZNRwoAKHleu2Dl8qQfqcxCqIVWIiJBOS/AbZOR4OA4XQXYGslWvrj26WyErTwDUE0g+fXoyovlVtN5Og+00lrDUtZ"
                + "fiXI5+iUAtpuv72f/fgV+E/crURQh3EP0vNTpzyUH+eRn63RJiYvAAAAAElFTkSuQmCC",
        // Biểu tượng tiến độ.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAU5JREFUeAHdkjFSwlAQhndfHtaxdUZjCmtAoYcaM8oN5AQeAbiB"
                + "nkA8ASpah1pgwBMkzxOkT/LWfXHIQMaIhYXjFnmz79/9drNvAX7BsEyYzIIWCqiRBpvPiHSy8pon0x9BTDIg9llqsRgSYAhANks1"
                + "BApjinvdAkxsOi8L1QcUvgZ6Syy9f95wXK9x1PYaTp19F0FMJe75j7NgAGXtT+aKSgM2Cpm4sen4C0j4tFB33yU/z8Mgi31VN5P5"
                + "u7/1O59zEI6FlUEZQBMMUqJ74ycVzXHUWneTQYQUVR7aqnN6oLaSl+raVDcATXp40XSzIt26GwFpJVFWc4gxAhEVO0hT3ePXOTYF"
                + "1oC1mTv+2DmkU3duEyvtFiGCYMTHisAaFbVY4pV3djiEP2X5xo6XgS1T9PnC3plFEMWS2tmAobD2/PaXhLshyBDe5Af4n/YB5wSc"
                + "3zubVdgAAAAASUVORK5CYII=",
        // Biểu tượng phụ tùng.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAblJREFUeAGVVE1SwlAMTtIHLsWto0O5gT/DWlyrM3gC4QR6A+kN"
                + "9ATFE+Dws7ZrBMW9Y4sLt7J12r6Yh8JAh/KTzXvTJF++5MsrwArrdP2DVTGY5mg9+yVAvJWQEgJ7SNnK2dHucFEsJT80Xn273f2o"
                + "A9LT2M10ikCB1mHQ6Q/dzstXPpWJJOdUBDdS/VqSB8DoXBT3valfmCkAA57XrB1lbbkTZmOQVs8vy9WVuiO2VO38cO8BUqzZ/6yg"
                + "jmuIyCFz9bJY8P7aYZDhYY7lqqNwJw3AsBUAW9huM6CtSJ0kZoKetHAv1G7avcBv9v3KLIBhK+0O0LSL1p2Ue4NFgz0v5u8iBaIG"
                + "ecjkGrBW773c6gVmyA0g5YYWFy6O9x0p+j3JU0nKl4eFQI6qUHeysXXLkGkYlkTKXlviWTCyYmccRCp1R5aCbGJTEASdWzfJqCRH"
                + "LgniMfPOIlWS1hH/WCUAm0iWEuY31s5qutIMNXkrAXNYVUoFUUw+UcbW8U/BvCVJOGCROKLoXuY2mgOZAxNVNHCFgB81YBmZB4wo"
                + "yVC3MFNLDhmX9G2r2HJlnUv/izj3ljayxhr/k18V1MTNgtcGXQAAAABJRU5ErkJggg==",
        // Biểu tượng phát sinh.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAV1JREFUeAG9k0FOwlAQhv+Z18alrE2McAM0Yc/ekOgJxBPIDawn"
                + "sDdQT0AUWFu3AkEPYF65AezUtm98jwTSGCzFGP/dzHtv+s38U+A/1B/rzqY7XHQ4GOu2CF/fD3VQdI9+OuhOdNVLEUEwA/NBqkzt"
                + "9LA224rES9BhIkl9NCFmzik6W5EsKDLWpFSbkuQVCnVj22L268dHe9NSJH4iAUNiheTJME9AO48EeckkDVGmHUchrM6MqPN8XkSu"
                + "SOSkO9TNjUW8jLpCuG019qN8vtWoRZYm8khdFhZxltox1RX5gYvfYZ0BRZ/0MXdxosTSSXMdzUq9oY4fxtMbFKj3PA37o1ivJXEU"
                + "ztIlxSo/egvtnCrLOPVNIEAlv4CLIm6YxiBIRe7yFrrHGfm7+aJu4ezHQia+yBe3iDocfEMsknvsWl/S0Gqx7F7YOEZ5VQVUcb+D"
                + "B+cAsevzlzL4E30BpEipDG7rEjQAAAAASUVORK5CYII=",
        // Biểu tượng hoàn tất.
        "iVBORw0KGgoAAAANSUhEUgAAABEAAAARCAYAAAA7bUf6AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAAdFJREFUeAGtUzFy20AMBKBTaqnNJBFZpJbsEVNLdcKx9AKPX5D8"
                + "QNQPnBfEP9AknNRiLcsT8gUk84EwteiDcbSPpmnT48LX8EDs7S0WOIBXWNiVCC/TGRJMWMNAvgXrMva9j9GLSMxhQFxJaibJjAEz"
                + "AB5IaoLA2YEPZ8sWGTWD31f5CpC2Gjgpe3r4ZTpy/emHuT8dHUnsIlCk8M3252UaQJf8cJ9zJ6BxkcFtjOInSLJfV/mPZwl26aTC"
                + "7vLzcP93+6CcWx9o1MN+0EUQ7tJzINyYfdnXguOZVVORkKKxmBZ/Pn6bm3jzJx20SwCir5rUwsTLI7cA1rlCNa5JdKmHDFTYQ6qE"
                + "2HoT7tOFZpA9ffOP3yU1M2Iqva0uU3dK/l1rdmy+ZFgowq0QCAhPNev1iTf63lTHwENkKJpKEvnh2DKWn9y41Dw3BBJGJ54bNAkE"
                + "50i7x8KU1CS+50biSdTXtLLAiqjHMh98Bq3VP3BAMni+9z6qy6nkMa/lZjNI/+3NYmDWJjAmi0enzDSv7XkCEBDCBUi7bbfuShio"
                + "gxhsulR5dF/io7djei/yLszcIHMsCGOeI2/IEXgEjGtbRifJAzIlcyCvWJwroISkffhV1w3KHeW9AtH3WAAAAABJRU5ErkJggg==",
        // Biểu tượng tìm kiếm.
        "iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1B"
                + "AACxjwv8YQUAAAAOdEVYdFNvZnR3YXJlAEZpZ21hnrGWYwAAARNJREFUeAGdUsFNAzEQXPuOe/C6EvZ+CEIw3+iITClUQAmkhKMC"
                + "oILQQSwh5UskJL6YCsg7YJu5oJAoZytKVrJseTw7s+slOjBE7JJZl3S8YCoKa2dmvpNYndUXQYoGR4XVEhjrUeRu9PE6/dx8K/9V"
                + "TjQHSYZEeKYsq+zbS9XuFIINLjOs4CJqr1dPuF/fxrFBw71h0wWQjfvDL0oEcN7G/6wulrXMU0Q0yMJy2SUW1AKcqoNPtSIhbIe4"
                + "bHkIhpy/i0pmbkTeP21e5etTfkM/34bPr0py7t6+T2domIZSm0wDb7pWV3XkR9qTh4KcIEEAaYzvMXBzDcUHlKJW76OTE69zoKA6"
                + "JikvU9OUJqeGYJ/4BQHmX59l3kWUAAAAAElFTkSuQmCC"
    };
    private static final Icon[] CAC_ANH = new Icon[DU_LIEU.length];

    private BieuTuongKyThuat() {
    }

    // Giải mã biểu tượng đã nhúng và lưu lại để sử dụng nhiều lần.
    public static Icon lay(int viTri) {
        if (CAC_ANH[viTri] == null) {
            CAC_ANH[viTri] = new ImageIcon(Base64.getDecoder().decode(DU_LIEU[viTri]));
        }
        return CAC_ANH[viTri];
    }
}