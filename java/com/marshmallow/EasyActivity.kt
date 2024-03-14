package com.marshmallow

import android.annotation.SuppressLint
import android.content.ContentValues
import java.util.UUID
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.marshmallow.databinding.ActivityEasyBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.Locale


class EasyActivity : AppCompatActivity() , TextToSpeech.OnInitListener{
    private var mBinding: ActivityEasyBinding? = null
    private val binding get() = mBinding!!

    val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    var db: LikeDatabase? = null
    var InputText:String = ""
    var save_path:String = ""
    var org_path:String = ""
    var textTitle:String = ""
    var textTS = ""
    lateinit var dialog :ShowingLoadApp
    private var textToSpeech: TextToSpeech? = null
    val timeBasedUUID: UUID = UUID.randomUUID()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileUtil = FileUtil(this@EasyActivity)
        fileUtil.createDirectory(Environment.DIRECTORY_DOWNLOADS +"/말쉬운말로")
        fileUtil.createDirectory(Environment.DIRECTORY_DOWNLOADS +"/말쉬운말로/" + timeBasedUUID)
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

            }
        } else {
            // TTS initialization failed
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
        super.onDestroy()
    }

    fun initial(){
        mBinding = ActivityEasyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = LikeDatabase.getInstance(this@EasyActivity)

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
        binding.btnAddLike.setOnClickListener{
            var temp: LikeEntity = LikeEntity(0,false,
                save_path,
                org_path,
                textTS,
                textTitle,
                "",
                "")
            binding.btnAddLike.isEnabled = false
            Toast.makeText(this@EasyActivity, "저장이 완료되었습니다!", Toast.LENGTH_LONG).show()
            CoroutineScope(Dispatchers.IO).async {
                db!!.likeDao().insertLike(temp)
            }
        }
        dialog = ShowingLoadApp(this@EasyActivity)
        getEasyContext(intent.getParcelableExtra<Uri?>("resURI"))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveHTML(filen:String, text:String) : String? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filen)
            put(MediaStore.Images.Media.MIME_TYPE, "text/html")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/말쉬운말로/" + timeBasedUUID)
        }

        val resolver = contentResolver
        var outputStream: OutputStream? = null
        var filePath: String? = ""

        try {
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                outputStream?.write(text.toByteArray())
            }

            // 안드로이드 10 이상일 경우
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.query(uri!!, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                        filePath = cursor.getString(columnIndex)
                    }
                }
            } else {
                // 안드로이드 10 미만일 경우
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(uri!!, projection, null, null, null)

                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        filePath = it.getString(columnIndex)
                    }
                }
            }
        } catch (e: Exception) {
            println("파일 저장 중 오류 발생: $e")
            return ""
        } finally {
            outputStream?.close()
        }
        return filePath
    }

    @SuppressLint("SetJavaScriptEnabled")
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

        binding.OutputView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                supportActionBar?.title = title
            }
        }
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
    fun getEasyContext(resultUri : Uri?){
        binding.InputImage.setImageURI(resultUri)
        var image = InputImage.fromFilePath(this@EasyActivity, resultUri!!)
        initWebView()
        dialog.progressON("쉬운 말을 받고 있어요")
        try{
            recognizer.process(image)
                .addOnSuccessListener {
                    InputText = (it.text)
                    runBlocking {
                        launch{
                            delay(1000)
                            thisRetrofit.server.getEasy(timeBasedUUID.toString(), it.text).enqueue((object: Callback<EasyResult> {
                                override  fun onFailure(call: Call<EasyResult>, t:Throwable){
                                    Log.d("레트로핏", t.message.toString())
                                    Toast.makeText(this@EasyActivity, "응답 실패", Toast.LENGTH_SHORT)
                                    dialog.progressOFF()
                                    Toast.makeText(this@EasyActivity, "LLM 처리 오류", Toast.LENGTH_LONG).show()
                                }

                                @RequiresApi(Build.VERSION_CODES.Q)
                                override fun onResponse(
                                    call: Call<EasyResult>,
                                    response: Response<EasyResult>
                                ) {
                                    try{
                                        if(response.isSuccessful){
                                            if(response.body()?.mData1!! =="ERR")
                                            {
                                                Toast.makeText(this@EasyActivity, "오류", Toast.LENGTH_SHORT).show()
                                            }else
                                            {
                                                save_path = saveHTML("result.html",response.body()?.mData1!!)!!
                                                org_path = saveHTML("raw_result.html",it.text!!)!!
                                                textTitle = response.body()?.mData3!!
                                                textTS = response.body()?.mData2!!
                                                binding.OutputView.loadUrl(save_path!!)
                                                speakText(textTS)
                                            }
                                            dialog.progressOFF()
                                        }
                                    }catch (e2 : IOException){
                                        e2.printStackTrace()
                                    }

                                }
                            }))
                        }
                    }

                }
                .addOnFailureListener{
                    InputText=it.message.toString()
                    dialog.progressOFF()
                }
        }catch(e:IOException){
            e.printStackTrace()
        }
    }
}