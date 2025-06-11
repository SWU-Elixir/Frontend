package com.example.elixir.calendar.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.result.ActivityResultLauncher
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.databinding.ItemMealListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class MealListAdapter(
    private val context: Context,
    private var data: MutableList<DietLogData>,
    private val listener: OnMealClickListener,
    private var ingredientMap: Map<Int, IngredientData> = emptyMap()
) : BaseAdapter() {
    override fun getCount(): Int = data.size
    override fun getItem(position: Int): DietLogData = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    // MealDetailFragment 띄우기 (이 변수가 현재 코드에서 사용되지 않는다면 제거를 고려해 보세요)
    private lateinit var mealDetailLauncher: ActivityResultLauncher<Intent>

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemMealListBinding
        val view: View

        // 뷰 재사용 로직
        if (convertView == null) {
            binding = ItemMealListBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemMealListBinding
            view = convertView
        }

        val item = getItem(position)

        // 이미지 처리 분기
        val dietImg = item.dietImg
        when {
            // "android.resource://"로 시작하는 경우 (앱 내장 기본 이미지)
            dietImg.startsWith("android.resource://") -> {
                try {
                    val resId = dietImg.substringAfterLast("/").toIntOrNull()
                    if (resId != null) {
                        binding.dietPicture.setImageResource(resId)
                    } else {
                        binding.dietPicture.setImageResource(R.drawable.img_blank)
                    }
                } catch (e: Exception) {
                    Log.e("MealListAdapter", "리소스 이미지 로드 실패: ${e.message}")
                    binding.dietPicture.setImageResource(R.drawable.img_blank)
                }
            }
            // "http://" 또는 "https://"로 시작하는 경우 (서버에서 가져온 이미지)
            dietImg.startsWith("http://") || dietImg.startsWith("https://") -> {
                Glide.with(context)
                    .load(dietImg)
                    .placeholder(R.drawable.img_blank)
                    .error(R.drawable.img_blank)
                    .into(binding.dietPicture)
            }
            // "content://" 또는 "file://"로 시작하는 경우 (로컬 파일)
            dietImg.startsWith("content://") || dietImg.startsWith("file://") -> {
                try {
                    val uri = Uri.parse(dietImg)
                    Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.img_blank)
                        .error(R.drawable.img_blank)
                        .into(binding.dietPicture)
                } catch (e: Exception) {
                    Log.e("MealListAdapter", "로컬 이미지 로드 실패: ${e.message}")
                    binding.dietPicture.setImageResource(R.drawable.img_blank)
                }
            }
            else -> {
                // 그 외의 경우 (예: 이미지 경로가 유효하지 않거나 비어있는 경우)
                binding.dietPicture.setImageResource(R.drawable.img_blank)
                Log.w("MealListAdapter", "알 수 없는 이미지 형식 또는 경로: $dietImg. 기본 이미지로 대체합니다.")
            }
        }

        binding.dietNameText.text = item.dietTitle

        // 식단 점수에 따른 아이콘 설정 (1~5점)
        val iconRes = when (item.score) {
            1 -> R.drawable.ic_meal_number1
            2 -> R.drawable.ic_meal_number2
            3 -> R.drawable.ic_meal_number3
            4 -> R.drawable.ic_meal_number4
            5 -> R.drawable.ic_meal_number5
            else -> R.drawable.ic_meal_number1 // 기본 아이콘
        }
        binding.dietScore.setImageResource(iconRes)

        // 재료 목록을 FlexboxLayoutManager를 사용하여 표시
        binding.dietIngredientList.layoutManager = FlexboxLayoutManager(context)
        binding.dietIngredientList.adapter = MealListIngredientAdapter(item.ingredientTags, ingredientMap)

        // Flexbox 레이아웃 매니저 설정 (이미 위에서 binding.dietIngredientList.layoutManager로 설정했으므로 중복 제거)
        // val layoutManager = FlexboxLayoutManager(context)
        // layoutManager.flexDirection = FlexDirection.COLUMN
        // layoutManager.justifyContent = JustifyContent.FLEX_END

        view.setOnClickListener {
            listener.onMealClick(item)
        }

        // 식사 시간 표시
        binding.dietTimesText.text = item.dietCategory

        return view
    }

    //어댑터의 데이터를 새로운 데이터로 업데이트
    fun updateData(newData: List<DietLogData>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    // 식재료 세팅
    fun setIngredientMap(map: Map<Int, IngredientData>) {
        this.ingredientMap = map
        notifyDataSetChanged()
    }
}