package com.example.elixir.member

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup.MarginLayoutParams
import android.util.TypedValue
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.Color
import com.example.elixir.R

class ImageGridAdapter(private val items: List<Int>) : RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            // 마진을 포함한 레이아웃 파라미터 설정
            layoutParams = MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300 // 높이 조절
            ).apply {
                // dp를 픽셀로 변환
                val spacing = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    4f, // 8dp 간격
                    parent.context.resources.displayMetrics
                ).toInt()
                
                // 각 방향 마진 설정
                setMargins(spacing, spacing, spacing, spacing)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            
            // 둥근 모서리 설정
            val radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                parent.context.resources.displayMetrics
            )
            val shape = RoundRectShape(
                floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius),
                null,
                null
            )
            val shapeDrawable = ShapeDrawable(shape).apply {
                paint.color = Color.WHITE
            }
            
            // 배경과 이미지를 함께 표시하기 위한 LayerDrawable 생성
            val layers = arrayOf<Drawable>(
                shapeDrawable,
                context.getDrawable(R.drawable.bg_rect_filled_gray) ?: shapeDrawable
            )
            background = LayerDrawable(layers)
            
            // 클리핑 활성화
            clipToOutline = true
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(items[position])
    }

    override fun getItemCount() = items.size
}
