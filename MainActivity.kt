package com.example.linguavideo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var browser: WebView
    private lateinit var address: EditText
    private lateinit var status: TextView
    private lateinit var words: TextView
    private lateinit var chatInput: EditText
    private lateinit var chatOutput: TextView
    private lateinit var backendInput: EditText
    private val savedWords = linkedSetOf<String>()
    private var selectedText: String = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 6)
            setBackgroundColor(0xFFF5F7F5.toInt())
        }
        root.addView(TextView(this).apply {
            text = "LinguaVideo  •  Watch, Learn & Ask AI"
            textSize = 19f
            setTextColor(0xFF16372E.toInt())
            setPadding(2, 5, 2, 6)
        })

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        address = EditText(this).apply {
            hint = "Dán link video hoặc website…"
            textSize = 13f
            singleLine = true
            setText("https://www.youtube.com")
            imeOptions = EditorInfo.IME_ACTION_GO
        }
        row.addView(address, LinearLayout.LayoutParams(0, 46, 1f))
        row.addView(Button(this).apply {
            text = "Mở"
            setOnClickListener { openAddress() }
        })
        root.addView(row)

        status = TextView(this).apply {
            text = "Mở video, chọn từ/câu nếu trang cho phép, rồi hỏi AI bên dưới."
            textSize = 11f
            setTextColor(0xFF52645D.toInt())
            setPadding(2, 2, 2, 4)
        }
        root.addView(status)

        browser = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    status.text = "Trang: ${url ?: ""} • Nếu không phát được, website có thể chặn WebView."
                }
            }
        }
        root.addView(browser, LinearLayout.LayoutParams(-1, 0, 1f))

        root.addView(TextView(this).apply {
            text = "HỎI AI VỀ TỪ / CÂU ĐANG HỌC"
            textSize = 12f
            setTextColor(0xFF2D7D68.toInt())
            setPadding(2, 5, 2, 1)
        })

        chatInput = EditText(this).apply {
            hint = "Ví dụ: awkward — giải thích nghĩa và ngữ cảnh"
            textSize = 13f
            minLines = 2
            maxLines = 3
        }
        root.addView(chatInput, LinearLayout.LayoutParams(-1, 62))

        val chatButtons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        chatButtons.addView(Button(this).apply {
            text = "Giải thích"
            setOnClickListener { askAi() }
        }, LinearLayout.LayoutParams(0, 42, 1f))
        chatButtons.addView(Button(this).apply {
            text = "Lưu từ/câu"
            setOnClickListener {
                val value = chatInput.text.toString().trim()
                if (value.isNotBlank()) {
                    savedWords.add(value)
                    refreshWords()
                    status.text = "Đã lưu nội dung vào danh sách trong phiên hiện tại."
                } else status.text = "Nhập từ/câu cần lưu trước."
            }
        }, LinearLayout.LayoutParams(0, 42, 1f))
        root.addView(chatButtons)

        chatOutput = TextView(this).apply {
            text = "AI sẽ giải thích nghĩa tiếng Việt, ngữ cảnh, sắc thái, cấu trúc và ví dụ khi bạn cấu hình máy chủ AI."
            textSize = 13f
            setTextColor(0xFF20332C.toInt())
            setPadding(6, 4, 6, 4)
        }
        root.addView(ScrollView(this).apply { addView(chatOutput) },
            LinearLayout.LayoutParams(-1, 90))

        root.addView(TextView(this).apply {
            text = "API endpoint máy chủ của bạn (không nhập API key vào app)"
            textSize = 10f
            setTextColor(0xFF52645D.toInt())
        })
        backendInput = EditText(this).apply {
            hint = "https://your-server.example/api/tutor"
            textSize = 11f
            singleLine = true
            setText("")
        }
        root.addView(backendInput, LinearLayout.LayoutParams(-1, 42))

        root.addView(TextView(this).apply {
            text = "TỪ/CÂU ĐÃ LƯU"
            textSize = 11f
            setTextColor(0xFF2D7D68.toInt())
        })
        words = TextView(this).apply {
            text = "Chưa có từ/câu nào."
            textSize = 12f
            setTextColor(0xFF20332C.toInt())
        }
        root.addView(ScrollView(this).apply { addView(words) },
            LinearLayout.LayoutParams(-1, 55))

        setContentView(root)
        address.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) { openAddress(); true } else false
        }
        openAddress()
    }

    private fun openAddress() {
        var url = address.text.toString().trim()
        if (url.isBlank()) return
        if (!url.startsWith("https://") && !url.startsWith("http://")) url = "https://$url"
        address.setText(url)
        browser.loadUrl(url)
    }

    private fun askAi() {
        val endpoint = backendInput.text.toString().trim()
        val question = chatInput.text.toString().trim()
        if (question.isBlank()) {
            status.text = "Nhập từ/câu hoặc câu hỏi trước."
            return
        }
        if (endpoint.isBlank()) {
            chatOutput.text = "Chưa kết nối AI. Hãy nhập URL máy chủ backend của bạn. Không đặt OpenAI API key trực tiếp trong APK vì có thể bị trích xuất."
            return
        }
        chatOutput.text = "Đang hỏi AI…"
        thread {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 30000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
                val payload = JSONObject()
                    .put("question", question)
                    .put("task", "Explain English vocabulary and context in Vietnamese")
                    .put("video_url", address.text.toString())
                    .toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = stream.bufferedReader().use { it.readText() }
                val obj = JSONObject(response)
                val answer = obj.optString("answer", response)
                runOnUiThread { chatOutput.text = answer }
            } catch (e: Exception) {
                runOnUiThread {
                    chatOutput.text = "Không kết nối được máy chủ AI: ${e.message ?: "lỗi không xác định"}. Kiểm tra URL backend và kết nối mạng."
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun refreshWords() {
        words.text = if (savedWords.isEmpty()) "Chưa có từ/câu nào." else
            savedWords.mapIndexed { i, w -> "${i + 1}. $w" }.joinToString("\n")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::browser.isInitialized && browser.canGoBack()) browser.goBack()
        else super.onBackPressed()
    }
}
