# LinguaVideo — Android starter project

Bản khung Android Studio cho ý tưởng mở website/video trong app và lưu từ/câu.

## Có sẵn
- Trình duyệt WebView bên trong ứng dụng, nhập link website/video.
- Khu lưu các từ/câu được bôi đen khi website cho phép chọn văn bản.
- Nút Back của Android quay lại trang trước.

## Giới hạn
- Đây là **mã nguồn mẫu**, chưa phải APK cài đặt sẵn.
- Không đảm bảo mọi website/video hoạt động trong WebView. Một số trang chặn trình duyệt tích hợp, cần đăng nhập hoặc dùng DRM.
- Bản mẫu chưa tự lấy phụ đề, chưa dịch tự động, chưa có đồng bộ từ vựng.
- Chọn văn bản phụ thuộc vào website. Một số trình phát không cho bôi đen phụ đề.
- Không vượt DRM, paywall hoặc cơ chế bảo vệ của website.

## Mở dự án
1. Cài Android Studio.
2. Chọn **Open** và chọn thư mục `LinguaVideo`.
3. Chờ Gradle đồng bộ, sau đó chạy trên máy Android hoặc emulator.
4. Nếu IDE yêu cầu Android SDK 35, cài SDK theo hướng dẫn.

## Hướng phát triển
1. Import phụ đề `.srt`/`.vtt` hoặc dán transcript.
2. Chạm từ trong phụ đề → nghĩa tiếng Việt + phát âm.
3. Nhấn câu để tua/lặp video nếu website/player cho phép.
4. Flashcard và ôn tập lặp lại ngắt quãng.
5. Lưu từ vựng cục bộ trên máy.


## AI tutor integration scaffold
The app now includes a question box and a configurable HTTPS backend endpoint. It sends JSON:
`{"question":"...","task":"Explain English vocabulary and context in Vietnamese","video_url":"..."}`.
The backend should return JSON such as `{"answer":"..."}` and call the OpenAI API server-side.
**Do not embed an OpenAI API key in the Android app/APK.** Mobile APKs can be reverse-engineered.
An OpenAI API key and API billing are separate from a ChatGPT subscription. The backend, authentication,
rate limiting, and privacy controls still need to be implemented before public use.
