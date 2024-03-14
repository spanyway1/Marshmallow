package com.marshmallow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.marshmallow.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    val PERMISSIONS_REQUEST = 100
    val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    var db: LikeDatabase? = null
    lateinit var dialog :ShowingLoadApp
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if(!hasFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.systemBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        else {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions(PERMISSIONS, PERMISSIONS_REQUEST)
        db = LikeDatabase.getInstance(this)

        supportFragmentManager.beginTransaction()
            .replace(binding.photoView.id, FragmentTakePicture())
            .commit()

        binding.btnLikes.setOnClickListener {
            var activityWord = Intent(this@MainActivity, WordsActivity::class.java)
            startActivity(activityWord)
        }
        binding.btnEasyManual.setOnClickListener{
            var activityEasier = Intent(this@MainActivity, EasierManualActivity::class.java)
            startActivity(activityEasier)
        }
        binding.btnMap.setOnClickListener {
            var activityMaps = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(activityMaps)
        }

        dialog = ShowingLoadApp(this@MainActivity)
    }

    fun showYesNODialog(title:String, message:String, pM:String, nM:String, yF:()->Unit, nF:()->Unit){
        val builder = AlertDialog.Builder(this)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(pM, { _, _ ->
            run {
                yF.invoke()
            }
        })
        builder.setNegativeButton(nM, { _, _ ->
            run {
                nF.invoke()
            }
        })
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }
    fun discriminate(resultUri: Uri){
        binding.InputImage.setImageURI(resultUri)
        var image = InputImage.fromFilePath(this, resultUri!!)

        dialog.progressON("글의 종류를 생각하고 있어요")
        try{
            recognizer.process(image)
                .addOnSuccessListener {
                    var timebasedUUID = UUID.randomUUID()
                    thisRetrofit.server.getDiscrimination(timebasedUUID.toString(), it.text).enqueue((object: Callback<FluppyResult> {
                        override  fun onFailure(call: Call<FluppyResult>, t:Throwable){                            Log.d("레트로핏", t.message.toString())
                            dialog.progressOFF()
                        }
                        override fun onResponse(
                            call: Call<FluppyResult>,
                            response: Response<FluppyResult>
                        ) {
                            try{
                                if(response.isSuccessful){
                                    if(response.body()?.mData == "Y")//어려운 글인가
                                    {
                                        showYesNODialog("어려운 글인가요?", "어려운 글이 맞으면 '어려운 글'을, 메뉴판이면 '메뉴판'을 눌러주세요", "어려운 글", "메뉴판",
                                            {val actEasy = Intent(this@MainActivity, EasyActivity::class.java)
                                                actEasy.putExtra("resURI", resultUri!!)
                                                dialog.progressOFF()
                                                startActivity(actEasy)},
                                            {val actConvert = Intent(this@MainActivity, ConvtoPicActivity::class.java)
                                                actConvert.putExtra("resURI", resultUri!!)
                                                dialog.progressOFF()
                                                startActivity(actConvert)})
                                    }else{
                                        showYesNODialog("메뉴판이 맞나요?", "메뉴판이면 '메뉴판'을, 어려운 글이 맞으면 '어려운 글'을 눌러주세요", "메뉴판", "어려운 글",
                                            {val actConvert = Intent(this@MainActivity, ConvtoPicActivity::class.java)
                                                actConvert.putExtra("resURI", resultUri!!)
                                                startActivity(actConvert)},
                                            {val actEasy = Intent(this@MainActivity, EasyActivity::class.java)
                                                actEasy.putExtra("resURI", resultUri!!)
                                                startActivity(actEasy)})
                                    }
                                    dialog.progressOFF()
                                }
                            }catch (e2 : IOException){
                                e2.printStackTrace();
                            }

                        }
                    }))
                }
                .addOnFailureListener{
                    dialog.progressOFF()

                }
        }catch(e:IOException){
            e.printStackTrace()
        }

    }

    private fun checkPermissions(permissions: Array<String>, permissionsRequest: Int): Boolean {
        val requestList : MutableList<String> = mutableListOf()
        for(permission in permissions){
            val result = ContextCompat.checkSelfPermission(this, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                requestList.add(permission)
            }
        }
        if(requestList.isNotEmpty()){
            ActivityCompat.requestPermissions(this, requestList.toTypedArray(), PERMISSIONS_REQUEST)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSIONS_REQUEST)
        {
            for(result in grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "권한 승인 부탁드립니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

}