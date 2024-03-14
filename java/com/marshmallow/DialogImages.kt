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

class DialogImages (context: Context) : Dialog(context) {
    var mBinding:DialogImagesBinding?= null
    var imgTopic:String = ""
    val binding get() = mBinding!!
    var imgList = ArrayList<String>()
    var index = 0
    var db:LikeDatabase? = null
    override fun onCreate(savedInstanstanceState : Bundle?) {
        super.onCreate(savedInstanstanceState)
        db = LikeDatabase.getInstance(context)
        binding.btnAddLike.setOnClickListener{
            addLike()
            binding.btnAddLike.isEnabled = false
        }
    }

    fun setImages(urlList: ArrayList<String>) {
        mBinding = DialogImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        imgList.clear()
        imgList.addAll(urlList)
        index = 0
        Toast.makeText(context, "사진을 터치하면 다음 사진이 나타나요!", Toast.LENGTH_LONG).show()
        binding.exImgView.setOnClickListener{
            index += 1
            if(index + 1 > imgList.size)
            {
                index = 0
            }
            binding.btnAddLike.isEnabled = false
            Log.d("다이얼로그 이미지", index.toString())
            Glide.with(context)
                .load(imgList[index])
                .override(Target.SIZE_ORIGINAL)
                .into(binding.exImgView)
            binding.btnAddLike.isEnabled = true
        }

        Glide.with(context)
            .load(imgList[index])
            .override(Target.SIZE_ORIGINAL)
            .into(binding.exImgView)

        binding.btnAddLike.isEnabled = true
    }

    fun addLike(){
        var temp = LikeEntity(0, true, "", "", "","",imgTopic, imgList[index].toString())
        CoroutineScope(Dispatchers.IO).async {
            db!!.likeDao().insertLike(temp)
        }
    }
}