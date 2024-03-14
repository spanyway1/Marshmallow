package com.marshmallow

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatActivity
import com.marshmallow.databinding.ActivityEasyviewBinding
import java.util.Locale

class EasyViewActivity : AppCompatActivity() , TextToSpeech.OnInitListener{
    private var mBinding: ActivityEasyviewBinding? = null
    private val binding get() = mBinding!!

    var db: LikeDatabase? = null
    var save_path:String = ""
    var org_path:String = ""
    var textTitle:String = ""
    var textTS = ""
    lateinit var dialog :ShowingLoadApp
    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initial()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.KOREAN)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                // Handle language not supported or missing data
            } else {
                speakText(textTS)
            }
        } else {
            // TTS initialization failed
            Toast.makeText(this@EasyViewActivity, "TTS 생성 실패", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
        super.onDestroy()
    }

    fun speakText(text: String){
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Utterance 시작 시 호출

            }

            override fun onDone(utteranceId: String?) {
                // Utterance 완료 시 호출
                runOnUiThread {

                }
            }

            override fun onError(utteranceId: String?) {
                // Utterance 오류 시 호출
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                runOnUiThread {
                    Log.d("시작", start.toString() + ", " + end.toString())
                    binding.OutputView.evaluateJavascript("highlightWordRange($start, $end)", null)
                }
            }
        })
        textToSpeech?.speak(textTS!!,TextToSpeech.QUEUE_FLUSH, null, "id1 ")
    }

    fun initial(){
        mBinding = ActivityEasyviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = LikeDatabase.getInstance(this@EasyViewActivity)

        textToSpeech = TextToSpeech(this, this)

        binding.btnOrg.setOnClickListener{
            if(binding.btnOrg.text == "어려운 말 보기"){
                binding.OutputView.loadUrl(org_path)
                binding.btnOrg.text = "쉬운 말 보기"
            }else{
                binding.OutputView.loadUrl(save_path)
                binding.btnOrg.text = "어려운 말 보기"
            }
        }
        dialog = ShowingLoadApp(this@EasyViewActivity)
        loadEasyContext(intent.getParcelableExtra<LikeEntity?>("Like")!!)
    }
    inner class WebAppInterface {

        @JavascriptInterface
        fun highlightWordRange(start: Int, end: Int) {
            // JavaScript에서 호출되는 함수로, WebView 상에서 특정 범위의 단어를 강조할 수 있습니다.
            val jsScript = "highlightWordRange($start, $end)"
            binding.OutputView.evaluateJavascript(jsScript, null)
        }
    }

    fun initWebView(){
        binding.OutputView.settings.javaScriptEnabled = true
        binding.OutputView.settings.allowFileAccess = true
        binding.OutputView.settings.allowContentAccess =true

        binding.OutputView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }

        binding.OutputView.webChromeClient = object : WebChromeClient(){
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                supportActionBar?.title = title
            }
        }
    }

    fun loadEasyContext(like:LikeEntity){
        initWebView()
        dialog.progressON("쉬운 말을 불러오고 있어요")
        save_path = like.save_path
        org_path = like.org_path
        textTitle = like.txt_title
        textTS = like.speak_str
        binding.OutputView.loadUrl(save_path!!)
        speakText(textTS)
        dialog.progressOFF()
    }
}