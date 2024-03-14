package com.marshmallow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.marshmallow.databinding.ActivityWordsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class WordsActivity : AppCompatActivity(), wdAdapter.OnItemClickListener {
    var mBinding : ActivityWordsBinding? = null
    val binding get() = mBinding!!
    val wordList :ArrayList<WordItem> = arrayListOf<WordItem>()
    lateinit var Adapter:wdAdapter
    var db:LikeDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.wordsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        Adapter= wdAdapter(wordList, this)
        binding.wordsList.adapter = Adapter
        binding.wordsList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        db = LikeDatabase.getInstance(this)
        CoroutineScope(Dispatchers.IO).async {
            val likes = db!!.likeDao().getLikes()
            if(likes.isNotEmpty())
            {
                for( i in likes){
                    var temp= WordItem(i.id,i.type, i.save_path, i.org_path, i.speak_str, i.txt_title, i.picName, i.picURL)
                    wordList.add(temp)
                }
            }
        }
        Adapter.notifyDataSetChanged()
    }

    fun callActivity(position:Int){
        val actEasy = Intent(this@WordsActivity, EasyViewActivity::class.java)
        var temp = LikeEntity(0,false,
            wordList[position].save_path,
            wordList[position].org_path,
            wordList[position].speak_str,
            wordList[position].txt_title,
            "",
            "")
        actEasy.putExtra("Like", temp)
        startActivity(actEasy)
    }

    override fun onDeleteClick(position: Int) {
        CoroutineScope(Dispatchers.IO).async {
            // 가져올 데이터의 기본키를 사용하여 LikeEntity 객체 생성
            val primaryKey = wordList[position].id  // 예시로 기본키가 id라고 가정

            // LikeEntity를 삭제
            db?.likeDao()?.deleteLikeById(primaryKey)

            // 삭제 후 데이터를 다시 로드하여 업데이트
            val likes = db?.likeDao()?.getLikes()
            wordList.clear() // 기존 데이터 삭제

            if (!likes.isNullOrEmpty()) {
                for (i in likes) {
                    val temp = WordItem(
                        i.id, i.type, i.save_path, i.org_path, i.speak_str,
                        i.txt_title, i.picName, i.picURL
                    )
                    wordList.add(temp)
                }
            }

            // UI 업데이트
            withContext(Dispatchers.Main) {
                Adapter = wdAdapter(wordList, this@WordsActivity)
                binding.wordsList.adapter = Adapter
                binding.wordsList.addItemDecoration(
                    DividerItemDecoration(
                        this@WordsActivity,
                        LinearLayoutManager.VERTICAL
                    )
                )
                Adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClick(position: Int) {
        if (wordList[position].type == true){//메뉴판
            var dialogImage = DialogImage(this@WordsActivity)
            dialogImage.setImage(wordList[position].picURL)
            dialogImage.binding.btnAddLike.setText(wordList[position].picName)
            dialogImage.show()
        }else{
            callActivity(position)
        }
    }
}