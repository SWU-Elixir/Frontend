package com.example.elixir

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.elixir.databinding.FragmentDietLogBinding
import com.example.elixir.signup.OnProfileCompletedListener
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class DietLogFragment : Fragment() {
    private lateinit var dietLogBinding: FragmentDietLogBinding
    private lateinit var time: LocalDateTime

    private var dietImg: String = ""
    private var dietTitle: String = ""
    private var dietCategory: String = ""
    private var ingredientTags = mutableListOf<String>()
    private var score: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dietLogBinding = FragmentDietLogBinding.inflate(inflater, container, false)
        return dietLogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이미지 선택
        setImg()

        // 식단명 입력
        dietLogBinding.enterDietTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                dietTitle = s.toString().trim()
                checkAllValid()
            }
        })

        // 라디오 버튼 : 식단 유형 선택
        dietLogBinding.selectDiet.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.btn_breakfast -> dietCategory = R.string.breakfast.toString()
                R.id.btn_lunch -> dietCategory = R.string.lunch.toString()
                R.id.btn_dinner -> dietCategory = R.string.dinner.toString()
                R.id.btn_snack -> dietCategory = R.string.snack.toString()
            }
            checkAllValid()
        }

        //
        with(dietLogBinding) {
            // 일반 칩 리스트
            val chipList = listOf(
                ingredientChallenge, ingredientBrownRice, ingredientBean, ingredientGrain,
                ingredientGreenLeafy, ingredientBerry, ingredientNuts, ingredientOliveOil,
                ingredientFish, ingredientPoultry)

            // 해당 없음 칩
            val chipNone = findIngredient

            // 일반 칩 선택 시
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    // 일반 칩을 선택했다면 해당 없음 칩을 리스트에서 제거
                    if (isChecked) {
                        chipNone.isChecked = false
                        ingredientTags.remove(chipNone.text.toString())

                        // 중복 저장 방지
                        if (!ingredientTags.contains(chip.text.toString()))
                            ingredientTags.add(chip.text.toString())
                    }
                    // 두번 클릭 시 리스트에서 제거
                    else ingredientTags.remove(chip.text.toString())

                    // 상태 갱신
                    checkAllValid()
                }
            }

            // 특별히 없음 선택 시
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                // 일반 칩 모두 선택 해제, 리스트에서 제거하고 특별히 없음을 저장
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    ingredientTags.clear()
                    ingredientTags.add(chipNone.text.toString())
                }
                // 두 번 클릭 시 리스트에서 제거
                else ingredientTags.remove(chipNone.text.toString())

                // 상태 갱신
                checkAllValid()
            }
        }

        // 라디오 버튼 : 점수
        dietLogBinding.selectScore.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.btn_1 -> score = 1
                R.id.btn_2 -> score = 2
                R.id.btn_3 -> score = 3
                R.id.btn_4 -> score = 4
                R.id.btn_5 -> score = 5
            }
            checkAllValid()
        }
    }

    // 이미지 설정
    private fun setImg() {
        // 이미지 피커 선언 (PickVisualMedia)
        val imgSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                dietLogBinding.dietImg.setImageURI(uri)
                dietImg = uri.toString()
                // 갤러리에서 선택한 이미지의 생성 시간을 불러옴
                val createdTime = getImageCreatedTime(requireContext(), uri)
                displayImageTime(createdTime)
            }
        }

        // 프로필 이미지를 눌렀을 때,
        dietLogBinding.dietImg.setOnClickListener {
            // 커스텀 다이얼로그 띄우기
            SelectImgDialog(requireContext(),
                {
                    // 기본 이미지 선택 시 현재 시간 표시
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
                    dietLogBinding.dietImg.setImageURI(uri)
                    dietImg = uri.toString()
                    val currentTime = System.currentTimeMillis()
                    displayImageTime(currentTime)
                },
                {
                    // 갤러리에서 이미지 선택
                    imgSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ).show()
        }

        // 작성 버튼을 눌렀을 때
        dietLogBinding.btnWriteDietLog.setOnClickListener {
            DietLogData(dietImg, time, dietTitle, ingredientTags, score)
        }
    }

    // 시간 표시 함수
    private fun displayImageTime(timeInMillis: Long) {
        val date = Date(timeInMillis)
        val format = SimpleDateFormat("a hh:mm", Locale.getDefault())

        // DateFormatSymbols 사용하여 AM/PM 소문자 설정
        val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
        symbols.amPmStrings = arrayOf("AM", "PM")
        format.dateFormatSymbols = symbols

        val formattedTime = format.format(date)
        dietLogBinding.time12h.text = formattedTime // 시간 텍스트 뷰에 표시
    }

    // 이미지 생성 시간 가져오기
    private fun getImageCreatedTime(context: Context, imageUri: Uri): Long {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )
        context.contentResolver.query(
            imageUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                val dateTaken = if (dateTakenIndex != -1) cursor.getLong(dateTakenIndex) else null
                val dateAdded = if (dateAddedIndex != -1) cursor.getLong(dateAddedIndex) else null

                val time = dateTaken ?: dateAdded
                return time ?: System.currentTimeMillis() // 시간이 없으면 현재 시간 반환
            }
        }
        // 시간 정보를 찾을 수 없으면 현재 시간 반환
        return System.currentTimeMillis()
    }


    // 모든 변수에 유효한 값이 들어왔는지 확인
    fun checkAllValid() {
        // 다 유효한 상태일 떄만 버튼 활성화
        val allValid = dietImg.isNotBlank() && dietTitle.isNotBlank() && ingredientTags.isNotEmpty() && score != 0
        dietLogBinding.btnWriteDietLog.isEnabled = allValid

        // 버튼 색상 변경 (활성화: 주황, 비활성화: 회색)
        dietLogBinding.btnWriteDietLog.setBackgroundTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                if (allValid) R.color.elixir_orange
                else R.color.elixir_gray
            )
        )
    }
}