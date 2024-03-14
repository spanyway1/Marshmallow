package com.marshmallow

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageActivity
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.options

import com.marshmallow.databinding.FrameTakepictureBinding
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FragmentTakePicture : Fragment() {
    lateinit private var binding: FrameTakepictureBinding
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var parentContext : MainActivity
    private lateinit var resUri :Uri

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            parentContext.discriminate(result.uriContent!!)
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    private fun startCrop(m_Uri:Uri) {
        // Start cropping activity for pre-acquired image saved on the device and customize settings.
        cropImage.launch(
            options(uri = m_Uri) {
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
            }
        )
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentContext = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FrameTakepictureBinding.inflate(layoutInflater)
        startCamera()
        binding.btnCapture.setOnClickListener{
            takePhoto()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //세로
            Toast.makeText(parentContext, "화면 세로 입니다.", Toast.LENGTH_SHORT).show()
        }else{
            //가로
            Toast.makeText(parentContext, "화면 세로 입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto(){
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            newJpgFileName())

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(parentContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d("CameraX-Debug", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d("CameraX-Debug", msg)
                    resUri = Uri.fromFile(photoFile)

                    // 크롭 액티비티를 시작하기 위한 런처 생성
                    startCrop(resUri)
                }
            })
    }

    fun getImageUri() : Uri?{
        return resUri
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(parentContext)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
                .also{
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                //Unbind use cases before revinding
                cameraProvider.unbindAll()

                //Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }catch (exc:Exception) {
                Log.d("CameraX-D", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(parentContext))
    }

    private fun newJpgFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    private fun getOutputDirectory():File{
        val mediaDir = parentContext.externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else parentContext.filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}