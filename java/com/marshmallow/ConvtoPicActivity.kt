package com.marshmallow

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import com.marshmallow.databinding.ActivityConvtoPicBinding
import android.net.Uri
import android.util.Log
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.FileUtils
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.WindowManager
import java.io.InputStream

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Math.abs
import java.util.UUID

class ConvtoPicActivity : AppCompatActivity() {
    private var mBinding: ActivityConvtoPicBinding? = null
    private val binding get() = mBinding!!
    var recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = 1.0f
    lateinit  var dialog : ShowingLoadApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityConvtoPicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mScaleGestureDetector = ScaleGestureDetector(this@ConvtoPicActivity, ScaleListener())
        dialog = ShowingLoadApp(this@ConvtoPicActivity)
        setManualImg(intent.getParcelableExtra<Uri?>("resURI"))
    }
    fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        var outputStream: OutputStream? = null

        try {
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    fun setManualImg(resultUri : Uri?){
        binding.InputImage.setImageURI(resultUri)
        var image = InputImage.fromFilePath(this@ConvtoPicActivity, resultUri!!)
        dialog.progressON("글자를 찾고 있어요")
        try{
            recognizer.process(image)
                .addOnSuccessListener {
                    var blocks = it.textBlocks!!
                    var rects = ArrayList<RectF>()
                    val width =  binding.InputImage.width
                    val height =  binding.InputImage.height
                    val resolution = getImageResolutionFromUri(resultUri!!)
                    var wordsList = ArrayList<Pair<String, RectF>>()
                    var ratio:Float = 1.0F
                    var gap :Float = 0.0F
                    for(i in 0 until blocks.size){
                        var vRect = RectF( blocks[i].boundingBox!!)
                        if(resolution!!.second.toFloat() < resolution!!.first.toFloat())//가로가 더 긴 경우 -> 가로 기준
                        {
                            Log.d("OCR 인식 위치", "width 기준")
                            ratio = width.toFloat() / resolution!!.first.toFloat()
                            gap =(height.toFloat() - (resolution!!.second.toFloat() * ratio)) / 2
                            if(width.toFloat() > resolution!!.first.toFloat())
                            {
                                ratio = resolution!!.first.toFloat() / width.toFloat()
                                gap =abs((height.toFloat() - (resolution!!.second.toFloat() * ratio))) / 2
                            }
                            Log.d("OCR 인식 위치", "비율 " + ratio.toString());
                            vRect.top = gap + ( blocks[i].boundingBox!!.top.toFloat() * ratio)
                            vRect.left = ( blocks[i].boundingBox!!.left.toFloat() * ratio)
                            vRect.bottom =   gap + (blocks[i].boundingBox!!.bottom.toFloat() * ratio)
                            vRect.right = ( blocks[i].boundingBox!!.right.toFloat() * ratio)
                        }else{ //세로가 더 긴 경우 -> 세로 기준
                            Log.d("OCR 인식 위치", "height 기준")
                            if(resolution.second > height.toFloat())
                            {
                                ratio = height.toFloat() / resolution!!.second.toFloat()
                                gap =(width.toFloat() - (resolution!!.first.toFloat() * ratio)) / 2
                                if(height.toFloat()  > resolution!!.second.toFloat() )
                                {
                                    ratio =  resolution!!.second.toFloat()  / height.toFloat()
                                    gap =abs((width.toFloat() - (resolution!!.first.toFloat() * ratio)) )/ 2
                                }
                            }else{
                                ratio =  1.0F
                                gap =abs((width.toFloat() - (resolution!!.first.toFloat() * ratio)) )/ 2
                            }
                            Log.d("OCR 인식 위치", "비율 " + ratio.toString());
                            vRect.top =( blocks[i].boundingBox!!.top.toFloat() * ratio)
                            vRect.left =  gap + ( blocks[i].boundingBox!!.left.toFloat() * ratio)
                            vRect.bottom =  (blocks[i].boundingBox!!.bottom.toFloat() * ratio)
                            vRect.right =  gap + ( blocks[i].boundingBox!!.right.toFloat() * ratio)
                        }
                        rects.add(vRect)
                        wordsList.add(Pair<String, RectF>(blocks[i].text, vRect))
                        Log.d("OCR 인식 위치", i.toString() +" 번째 원본" +  blocks[i].boundingBox.toString())
                        Log.d("OCR 인식 위치", i.toString() +" 번째 수정" +  vRect.toString())
                        Log.d("OCR 인식 위치", "갭"+  gap.toString())
                    }
                    dialog.progressOFF()
                    var timebasedUUID =  UUID.randomUUID()

                    val inputStream = contentResolver.openInputStream(resultUri)
                    val file = File(cacheDir, timebasedUUID.toString() + ".jpg")

                    // 이미지 파일을 임시 파일에 복사
                    copyInputStreamToFile(inputStream!!, file)

                    // 이미지 파일을 서버로 업로드
                    dialog.progressON("이미지를 보내고 있어요")

                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                    try{
                        thisRetrofit.server.postManual(body).enqueue((object: Callback<FluppyResult> {
                            override fun onFailure(call: Call<FluppyResult>, t:Throwable){
                                Log.d("레트로핏", t.message.toString())
                                dialog.progressOFF()
                            }
                            override fun onResponse(call: Call<FluppyResult>, response: Response<FluppyResult>) {
                                dialog.progressOFF()
                            }
                        }))
                    }catch(e: Exception){
                        Log.d("레트로핏",  e.message.toString())
                        dialog.progressOFF()
                    }

                    binding.InputImage.dialogLoading = ShowingLoadApp(this@ConvtoPicActivity)
                    binding.InputImage.dialogImages = DialogImages(this@ConvtoPicActivity)
                    binding.InputImage.setWordList(wordsList, binding.InputImage.top)
                    binding.InputImage.drawRectangles(rects)
                }
                .addOnFailureListener{
                    dialog.progressOFF()
                }
        }catch(e:IOException){
            e.printStackTrace()
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetector!!.onTouchEvent(event!!)
        return super.onTouchEvent(event)
    }

    inner class ScaleListener:ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScale(scaleGestureDetector: ScaleGestureDetector):Boolean{
            scaleFactor *= scaleGestureDetector.scaleFactor
            scaleFactor= Math.max(0.5f, Math.min(scaleFactor, 2.0f))

            binding.InputImage.scaleX = scaleFactor
            binding.InputImage.scaleY = scaleFactor
            return true
        }
    }

    fun getImageResolutionFromUri(imageUri: Uri): Pair<Int, Int>? {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                val width = options.outWidth
                val height = options.outHeight

                return Pair(width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}