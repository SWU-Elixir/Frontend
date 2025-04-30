package com.example.elixir

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.elixir.calendar.MealPlanData
import com.example.elixir.databinding.FragmentDietLogBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.math.BigInteger
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DietLogFragment : Fragment() {
    private lateinit var dietLogBinding: FragmentDietLogBinding
    private val dietLogViewModel: DietLogViewModel by activityViewModels()

    // 날짜 & 시간
    private lateinit var formattedDate: String                          // 문자형 날짜(yyyy년 m월 d일)
    private var selectedTimeMillis: Long = System.currentTimeMillis()   // 선택된 시간
    private var selectedHour: Int = -1
    private var selectedMin: Int = -1
    private var selectedTime = Calendar.getInstance()

    // 이미지
    private lateinit var imgUri: Uri

    // 정보
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
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // -------------------------------------------- 초기화 -----------------------------------------------//
        // 이미지뷰: 기본 이미지로 설정
        imgUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
        dietLogBinding.dietImg.setImageURI(imgUri)
        dietImg = imgUri.toString()

        // 시간: 현재 시간으로 설정
        displayImageTime(selectedTimeMillis)

        // 날짜
        // 데이터 받아오기
        val year = activity?.intent?.getIntExtra("year", -1) ?: -1
        val month = activity?.intent?.getIntExtra("month", -1) ?: -1
        val day = activity?.intent?.getIntExtra("day", -1) ?: -1

        // 날짜에 유효한 값이 들어왔을 때만 텍스트뷰에 띄워주기
        if (year != -1 && month != -1 && day != -1) {
            formattedDate = "%d년 %d월 %d일".format(year, month, day)
            dietLogBinding.timestampDate.text = formattedDate
        }

        // 현재 시간으로 설정 체크
        dietLogBinding.setNowCb.isChecked = true

        // 시간 설정
        selectedTime = Calendar.getInstance().apply {
            timeInMillis = selectedTimeMillis
        }

        selectedHour = selectedTime.get(Calendar.HOUR)
        selectedMin = selectedTime.get(Calendar.MINUTE)

        // -------------------------------------------- 리스너 -----------------------------------------------//
        // 현재 시간으로 설정: 체크하면 현재 시간으로 설정되도록
        dietLogBinding.setNowCb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedTimeMillis = System.currentTimeMillis()
                // 시간 재설정
                selectedTime = Calendar.getInstance().apply {
                    timeInMillis = selectedTimeMillis
                }

                selectedHour = selectedTime.get(Calendar.HOUR)
                selectedMin = selectedTime.get(Calendar.MINUTE)

                displayImageTime(selectedTimeMillis)
            }
            checkAllValid()
        }

        // 타임피커: 다이얼로그 띄워주기(material3 제공)
        dietLogBinding.timePicker.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour)
                .setMinute(selectedMin)
                .setTheme(R.style.AppTheme_TimePicker)
                .build()

            picker.show(parentFragmentManager, "TIME_PICKER")

            // 확인 버튼을 누르면 저장
            picker.addOnPositiveButtonClickListener {
                selectedHour = picker.hour
                selectedMin = picker.minute

                // Calendar를 이용해서 millis로 변환
                selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMin)
                selectedTime.set(Calendar.SECOND, 0)
                selectedTime.set(Calendar.MILLISECOND, 0)

                selectedTimeMillis = selectedTime.timeInMillis
                displayImageTime(selectedTimeMillis)

                // 현재 시간과 다르면 체크박스 해제
                val nowCalendar = Calendar.getInstance()
                if (selectedTime.get(Calendar.HOUR_OF_DAY) != nowCalendar.get(Calendar.HOUR_OF_DAY) ||
                    selectedTime.get(Calendar.MINUTE) != nowCalendar.get(Calendar.MINUTE)
                ) {
                    dietLogBinding.setNowCb.isChecked = false
                }
            }
            checkAllValid()
        }

        // 식단명 입력
        dietLogBinding.enterDietTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                dietTitle = s.toString().trim()
                checkAllValid()
            }
        })

        // 식재료 태그 선택
        with(dietLogBinding) {
            // 일반 칩 리스트
            val chipList = listOf(
                ingredientBrownRice, ingredientBean, ingredientGrain,
                ingredientGreenLeafy, ingredientBerry, ingredientNuts, ingredientOliveOil,
                ingredientFish, ingredientPoultry)

            // 챌린지 칩
            val chipChallenge = ingredientChallenge

            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    val tagText = chip.text.toString()

                    if (isChecked) {
                        if (chip != chipChallenge) {
                            // 일반 칩이면 5개 제한 체크
                            val normalTagCount = ingredientTags.count { it != chipChallenge.text.toString() }
                            if (normalTagCount >= 5) {
                                chip.isChecked = false
                                Toast.makeText(requireContext(), "일반 재료는 최대 5개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                                return@setOnCheckedChangeListener
                            }

                            checkAllValid()
                        }

                        // 추가 (챌린지든 일반이든)
                        if (!ingredientTags.contains(tagText)) {
                            ingredientTags.add(tagText)

                            checkAllValid()
                        }
                    } else {
                        // 체크 해제: 무조건 제거
                        ingredientTags.remove(tagText)
                    }
                    checkAllValid()
                }
            }
        }

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

        // 라디오 버튼 : 점수
        dietLogBinding.selectScore.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btn_1 -> score = 1
                R.id.btn_2 -> score = 2
                R.id.btn_3 -> score = 3
                R.id.btn_4 -> score = 4
                R.id.btn_5 -> score = 5
            }
            checkAllValid()
        }

        // 작성 버튼
        dietLogBinding.btnWriteDietLog.setOnClickListener {
            // ToolbarActivity 속 onDietLogCompleted 호출
            val activity = requireActivity()
            if (activity is ToolbarActivity) {
                activity.onDietLogCompleted()
            }

            val selectedDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(selectedTimeMillis),
                ZoneId.systemDefault()
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H시 m분")
            val formattedString = selectedDateTime.format(formatter)

            val dietLogData = DietLogData(
                dietImg = dietImg,
                time = selectedDateTime,   // 변환한 시간 넣기
                dietTitle = dietTitle,
                dietCategory = dietCategory,
                ingredientTags = ingredientTags.toList(),
                score = score
            )
            dietLogViewModel.saveDietLog(dietLogData)

            val mealPlanData = MealPlanData(
                id = BigInteger.valueOf(System.currentTimeMillis()),
                memberId = BigInteger("1005"),
                name = dietTitle,
                imageUrl = dietImg,
                createdAt = formattedString,
                mealtimes = dietCategory,
                score = score,
                mealPlanIngredients = ingredientTags.toList()
            )

            val intent = Intent().apply {
                putExtra("mealData", mealPlanData)
            }

            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }

        setImg()
        checkAllValid()
    }

    // 시간 표시 함수
    private fun displayImageTime(timeInMillis: Long) {
        val date = Date(timeInMillis)
        val format = SimpleDateFormat("a hh:mm", Locale.getDefault())

        val symbols = DateFormatSymbols.getInstance(Locale.getDefault())
        symbols.amPmStrings = arrayOf("AM", "PM")
        format.dateFormatSymbols = symbols

        val formattedTime = format.format(date)
        dietLogBinding.time12h.text = formattedTime
        dietLogBinding.timestampTime.text = formattedTime
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

        // 프로필 이미지를 눌렀을 때, 선택 다이얼로그 띄우기
        dietLogBinding.dietImg.setOnClickListener {
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