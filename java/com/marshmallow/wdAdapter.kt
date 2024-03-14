package com.marshmallow

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class wdAdapter (val itemList:ArrayList<WordItem>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<wdAdapter.wdViewHolder>(){
    class wdViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        val tv_title = itemView.findViewById<TextView>(R.id.txtTitle)
        val tv_PicName = itemView.findViewById<TextView>(R.id.txtPicName)
        val pic_Layout = itemView.findViewById<LinearLayout>(R.id.pic_layout)
        val word_Layout = itemView.findViewById<LinearLayout>(R.id.word_layout)
        val btn_Pic = itemView.findViewById<Button>(R.id.btn_ShowPic)
        val btn_DPic = itemView.findViewById<Button>(R.id.btn_DeletePic)
        val btn_Easy = itemView.findViewById<Button>(R.id.btn_ShowEasy)
        val btn_DEasy = itemView.findViewById<Button>(R.id.btn_DeleteEasy)
    }
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(position:Int)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): wdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wdview_holder, parent, false)
        return wdViewHolder(view)
    }

    override fun onBindViewHolder(holder: wdAdapter.wdViewHolder, position: Int) {
        if (itemList[position].type == true)//메뉴판
        {
            holder.pic_Layout.visibility = View.VISIBLE
            holder.word_Layout.visibility = View.GONE
            holder.tv_PicName.text = itemList[position].picName
            holder.btn_Pic.setOnClickListener{
                itemClickListener.onItemClick(position)
            }
            holder.btn_DPic.setOnClickListener {
                itemClickListener.onDeleteClick(position)
            }
        }else{//쉬운말
            holder.word_Layout.visibility = View.VISIBLE
            holder.pic_Layout.visibility = View.GONE
            holder.tv_title.text = itemList[position].txt_title
            holder.btn_Easy.setOnClickListener{
                itemClickListener.onItemClick(position)
            }
            holder.btn_DEasy.setOnClickListener {
                itemClickListener.onDeleteClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}