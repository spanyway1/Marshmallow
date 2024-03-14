package com.marshmallow

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.marshmallow.databinding.DialogImagesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DialogImage (context: Context) : Dialog(context) {
    var mBinding:DialogImagesBinding?= null
    val binding get() = mBinding!!
    override fun onCreate(savedInstanstanceState : Bundle?) {
        super.onCreate(savedInstanstanceState)
    }
    fun setImage(url:String) {
        mBinding = DialogImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        Glide.with(context)
            .load(url)
            .override(Target.SIZE_ORIGINAL)
            .into(binding.exImgView)
    }
}