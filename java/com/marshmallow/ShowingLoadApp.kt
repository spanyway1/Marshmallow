package com.marshmallow

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import com.marshmallow.databinding.LoadingFrameBinding

class  ShowingLoadApp(context: Context) : Dialog(context) {
    var mBinding : LoadingFrameBinding? = null
    val binding get()= mBinding!!
    lateinit var frameAnimation :AnimationDrawable
    override fun onCreate(savedInstanstanceState : Bundle?) {
        super.onCreate(savedInstanstanceState)
        mBinding = LoadingFrameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun progressON(message: String?) {
        if (!this!!.isShowing) {
            progressSET(message)
        }
        binding.ivFrameLoading.setBackgroundResource(R.drawable.frame_loading)
        frameAnimation = binding.ivFrameLoading?.background as AnimationDrawable
        frameAnimation.start()
        binding.tvProgressMessage.text = message
    }

    fun progressSET(message: String?) {
        this.show()
        binding.tvProgressMessage.text = message
    }

    fun progressOFF() {
        if (this != null && this!!.isShowing) {
            this!!.dismiss()
        }
    }
}