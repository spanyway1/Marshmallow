package com.marshmallow;

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import java.net.URLEncoder
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class DrawableImageView(context: Context, attributeSet: AttributeSet): androidx.appcompat.widget.AppCompatImageView(context, attributeSet){
    private var callDraw = false
    lateinit var rect: ArrayList<RectF>
    var wordsList = ArrayList<Pair<String, RectF>>()
    var viewTop = 0
    lateinit var dialogLoading :ShowingLoadApp
    lateinit var dialogImages :DialogImages

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (callDraw) {
            callDraw = true
            for(i in 0 until rect.size){
                Log.d("Canvas", rect[i].left.toFloat().toString() + ","+
                        rect[i].top.toFloat().toString() + ","+
                    rect[i].right.toFloat().toString() + "," +
                        rect[i].bottom.toFloat().toString())
                var fillPaint = Paint()
                fillPaint.style = Paint.Style.FILL
                if(i % 2 == 0)
                {
                    fillPaint.color = Color.BLUE
                }else{
                    fillPaint.color = Color.GREEN
                }
                fillPaint.alpha = 60
                var strokePaint = Paint()
                strokePaint.style = Paint.Style.STROKE
                strokePaint.color = Color.WHITE
                strokePaint.alpha = 128
                strokePaint.strokeWidth = 5f

                canvas?.drawRect(rect[i].left, rect[i].top, rect[i].right, rect[i].bottom, fillPaint)
                canvas?.drawRect(rect[i].left, rect[i].top, rect[i].right, rect[i].bottom, strokePaint)
            }
        }
    }
    fun setWordList(words : ArrayList<Pair<String, RectF>>, vTop : Int){
        wordsList.clear()
        wordsList.addAll(words)
        viewTop = vTop
    }

    fun drawRectangles(pRect:ArrayList<RectF>) {
        callDraw = true
        rect = pRect
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val current = PointF(event!!.x, event!!.y)
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                for(i in 0 until wordsList.size){
                    val name = wordsList[i].first
                    val rect = wordsList[i].second
                    if(rect.left <= current.x && current.x <= rect.right){
                        if(rect.top <= current.y && current.y <= rect.bottom){
                            Toast.makeText(this@DrawableImageView.context, name, Toast.LENGTH_SHORT).show()
                            val keyWord = URLEncoder.encode(name, "UTF-8")
                            dialogLoading.progressON("이미지를 불러오고 있어요")
                            try{
                                var timebasedUUID =UUID.randomUUID()
                                thisRetrofit.server.getImageUrls(timebasedUUID.toString(),keyWord).enqueue((object: Callback<FluppyResult> {
                                    override fun onFailure(call: Call<FluppyResult>, t:Throwable){
                                        Log.d("레트로핏", t.message.toString())
                                    }
                                    override fun onResponse(call: Call<FluppyResult>, response: Response<FluppyResult>) {
                                        dialogLoading.progressOFF()

                                        var data = response.body()?.mData
                                        when(data) {
                                            "err"->{

                                            }
                                            ""->{

                                            }
                                            else->{
                                                var temp = ArrayList<String>()
                                                temp.addAll(data!!.split("\n"))
                                                Log.d("이미지 크롤링", data!! )
                                                dialogImages.imgTopic = name
                                                dialogImages.setImages(temp)
                                                dialogImages.show()
                                            }
                                        }
                                    }
                                }))
                            }catch(e: Exception){
                                Log.d("레트로핏",  e.message.toString())
                            }
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
