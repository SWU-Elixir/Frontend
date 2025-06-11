package com.example.elixir.calendar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.elixir.ingredient.ui.IngredientSearchFragment
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.calendar.network.DietApi
import com.example.elixir.calendar.network.db.DietLogDao
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.calendar.viewmodel.MealViewModel
import com.example.elixir.calendar.viewmodel.MealViewModelFactory
import com.example.elixir.databinding.FragmentDietLogBinding
import com.example.elixir.dialog.SaveDialog
import com.example.elixir.ingredient.data.IngredientDao
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientApi
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.member.data.MemberDao
import com.example.elixir.member.network.MemberApi
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.network.AppDatabase
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class DietLogFragment : Fragment() {
    // 바인딩, 뷰모델 정의
    private lateinit var dietLogBinding: FragmentDietLogBinding

    // 날짜 & 시간
    private lateinit var formattedTime: String
    private var selectedHour: Int = -1
    private var selectedMin: Int = -1
    private lateinit var selectedTime: LocalTime

    // 정보
    private var dietImg: String = ""
    private var dietTitle: String = ""
    private var dietCategory: String = ""
    private var ingredientTags = mutableListOf<Int>()
    private var score: Int = 0
    private var isEditMode: Boolean = false

    private lateinit var dietRepository: DietLogRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var ingredientRepository: IngredientRepository

    private lateinit var dietDao: DietLogDao
    private lateinit var memberDao: MemberDao
    private lateinit var ingredientDao: IngredientDao

    private lateinit var dietApi: DietApi
    private lateinit var memberApi: MemberApi
    private lateinit var ingredientApi: IngredientApi
    private lateinit var mealData: DietLogData
    private var mealDataJson: String? = null
    private var dietId: Int = -1

    private val dietLogViewModel: MealViewModel by viewModels {
        MealViewModelFactory(dietRepository, memberRepository, ingredientRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dietLogBinding = FragmentDietLogBinding.inflate(inflater, container, false)
        return dietLogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // -------------------------------------------- 초기화 -----------------------------------------------//
        super.onViewCreated(view, savedInstanceState)
        mealDataJson = arguments?.getString("mealData")

        // 데이터베이스와 API 초기화
        dietDao = AppDatabase.getInstance(requireContext()).dietLogDao()
        dietApi = RetrofitClient.instanceDietApi
        dietRepository = DietLogRepository(dietDao, dietApi)

        memberDao = MemberDB.getInstance(requireContext()).memberDao()
        memberApi = RetrofitClient.instanceMemberApi
        memberRepository = MemberRepository(memberApi, memberDao)

        ingredientDao = IngredientDB.getInstance(requireContext()).ingredientDao()
        ingredientApi = RetrofitClient.instanceIngredientApi
        ingredientRepository = IngredientRepository(ingredientApi, ingredientDao)

        // 수정 모드일 때 초기화
        if (mealDataJson != null) {
            mealData = Gson().fromJson(mealDataJson, DietLogData::class.java)
            isEditMode = true
            dietLogBinding.enterDietTitle.setText(mealData.dietTitle)

            dietTitle = mealData.dietTitle
            dietImg = mealData.dietImg
            dietCategory = mealData.dietCategory
            score = mealData.score

            // 이미지 처리
            Glide.with(requireContext())
                .load(mealData.dietImg)
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(dietLogBinding.dietImg)

            // 시간 처리
            selectedTime = mealData.time.toLocalTime()
            selectedHour = selectedTime.hour
            selectedMin = selectedTime.minute
            dietLogBinding.setNowCb.isChecked = selectedTime == LocalTime.now()
            dietLogBinding.time12h.text = mealData.time.format(DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH))

            // 점수(1~5)에 따라 라디오버튼 체크
            when (score) {
                1 -> dietLogBinding.selectScore.check(dietLogBinding.btn1.id)
                2 -> dietLogBinding.selectScore.check(dietLogBinding.btn2.id)
                3 -> dietLogBinding.selectScore.check(dietLogBinding.btn3.id)
                4 -> dietLogBinding.selectScore.check(dietLogBinding.btn4.id)
                5 -> dietLogBinding.selectScore.check(dietLogBinding.btn5.id)
            }

            // 카테고리(아침, 점심, 저녁, 간식)에 따라 라디오버튼 체크
            when (dietCategory) {
                getString(R.string.breakfast) -> dietLogBinding.selectDiet.check(dietLogBinding.btnBreakfast.id)
                getString(R.string.lunch) -> dietLogBinding.selectDiet.check(dietLogBinding.btnLunch.id)
                getString(R.string.dinner) -> dietLogBinding.selectDiet.check(dietLogBinding.btnDinner.id)
                getString(R.string.snack) -> dietLogBinding.selectDiet.check(dietLogBinding.btnSnack.id)
            }

            // 식재료 태그
            ingredientTags = mealData.ingredientTags.toMutableList()

            dietLogViewModel.loadIngredients()

            dietLogViewModel.ingredientList.observe(viewLifecycleOwner) { ingredientList ->
                // ingredientMap 생성
                val ingredientMap = ingredientList.associateBy { it.id }

                // dietLogData?.ingredientTags는 태그로 보여줄 id 리스트라고 가정
                val ingredientTags = mealData.ingredientTags

                showInitialIngredientChips(ingredientTags = ingredientTags, ingredientMap = ingredientMap,
                    chipGroup = dietLogBinding.tagsIngredient, findIngredientChip = dietLogBinding.findIngredient)
            }

            checkAllValid()

        } else {
            // 기본 이미지로 설정
            val defaultUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
            dietLogBinding.dietImg.setImageURI(defaultUri)
            dietImg = defaultUri.toString()

            // 현재 시간으로 초기화
            selectedTime = LocalTime.now()
            dietLogBinding.setNowCb.isChecked = true

            selectedHour = selectedTime.hour
            selectedMin = selectedTime.minute

            formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
            dietLogBinding.time12h.text = formattedTime
        }

        // -------------------------------------------- 리스너 -----------------------------------------------//
        // 현재 시간으로 설정: 체크하면 현재 시간으로 설정되도록
        dietLogBinding.setNowCb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 시간 현재로 재설정
                val now = LocalTime.now()
                dietLogBinding.setNowCb.isChecked = selectedTime.hour == now.hour && selectedTime.minute == now.minute

                formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                dietLogBinding.time12h.text = formattedTime
            }
            checkAllValid()
        }

        // 타임피커: 다이얼로그 띄워주기(material3 제공)
        dietLogBinding.timePicker.setOnClickListener {
            // 현재 시간으로 설정 체크박스 해제
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour)
                .setMinute(selectedMin)
                .setTheme(R.style.AppTheme_TimePicker)
                .build()

            picker.show(parentFragmentManager, "TIME_PICKER")

            // 확인 버튼을 누르면 저장
            picker.addOnPositiveButtonClickListener {
                // 선택된 시간으로 설정
                selectedHour = picker.hour
                selectedMin = picker.minute
                selectedTime = LocalTime.of(selectedHour, selectedMin)
                dietLogBinding.setNowCb.isChecked = false

                // 선택된 시간 텍스트뷰에 띄워주기
                formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                dietLogBinding.time12h.text = formattedTime
            }
            checkAllValid()
        }

        // 식단명 입력, 변경 탐지 및 유효성 검사
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
                dietLogBinding.btnBreakfast.id -> dietCategory = getString(R.string.breakfast)
                dietLogBinding.btnLunch.id -> dietCategory = getString(R.string.lunch)
                dietLogBinding.btnDinner.id -> dietCategory = getString(R.string.dinner)
                dietLogBinding.btnSnack.id -> dietCategory = getString(R.string.snack)
            }
            checkAllValid()
        }

        // SearchFragment에서 전달된 결과 수신
        parentFragmentManager.setFragmentResultListener("ingredient_selection", viewLifecycleOwner) { _, bundle ->
            val ingredientId = bundle.getInt("ingredientId", -1)
            val ingredientName = bundle.getString("ingredientName") ?: return@setFragmentResultListener
            
            if (ingredientId == -1) return@setFragmentResultListener
            val findIngredientChip = dietLogBinding.findIngredient

            // 중복 방지
            if (ingredientTags.contains(ingredientId)) {
                Toast.makeText(requireContext(), "이미 추가된 재료입니다.", Toast.LENGTH_SHORT).show()
                return@setFragmentResultListener
            }

            // 일반 태그 개수 제한
            if (ingredientTags.size >= 5) {
                Toast.makeText(requireContext(), "일반 재료는 최대 5개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return@setFragmentResultListener
            }

            // 칩 생성 및 추가
            val chip = Chip(ContextThemeWrapper(requireContext(), R.style.ChipStyle_Short)).apply {
                text = ingredientName
                isClickable = true
                isCheckable = false
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.elixir_orange)
                )
                setTextColor(ContextCompat.getColor(context, R.color.white))
                
                // 칩 클릭 리스너로 변경
                setOnClickListener {
                    ingredientTags.remove(ingredientId)
                    dietLogBinding.tagsIngredient.removeView(this)
                    checkAllValid()
                }
            }

            // findIngredient Chip 앞에 삽입
            val index = dietLogBinding.tagsIngredient.indexOfChild(findIngredientChip)
            dietLogBinding.tagsIngredient.addView(chip, index)
            
            // 리스트에 추가 (ID 저장)
            ingredientTags.add(ingredientId)
            checkAllValid()
        }

        // 식재료 검색 버튼 클릭 리스너
        dietLogBinding.findIngredient.setOnClickListener {
            // 칩 상태 토글
            dietLogBinding.findIngredient.isChecked = !dietLogBinding.findIngredient.isChecked
            
            // IngredientSearchFragment로 이동
            val ingredientSearchFragment = IngredientSearchFragment()
            
            // Activity의 레이아웃을 사용하여 Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, ingredientSearchFragment)
                .addToBackStack(null)
                .commit()
        }

        // Fragment가 다시 보일 때 검색 칩 상태 초기화
        viewLifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                dietLogBinding.findIngredient.isChecked = false
            }
        })

        // 라디오 버튼: 점수
        dietLogBinding.selectScore.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                dietLogBinding.btn1.id -> score = 1
                dietLogBinding.btn2.id -> score = 2
                dietLogBinding.btn3.id -> score = 3
                dietLogBinding.btn4.id -> score = 4
                dietLogBinding.btn5.id -> score = 5
            }
            checkAllValid()
        }

        // 업로드 관찰
        dietLogViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            Log.d("DietLogFragment", "uploadResult observed: $result")
            if (result.isSuccess) {
                if (::mealData.isInitialized) {
                    mealDataJson = Gson().toJson(mealData)
                    val intent = Intent().apply {
                        putExtra("mode", 0)
                        putExtra("dietLogData", mealDataJson)
                    }
                    Log.d("DietLogFragment", "Activity finish() 호출됨")
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
                }
            } else {
                Toast.makeText(requireContext(), "서버 업로드에 실패했습니다. 로컬에 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 작성 버튼
        dietLogBinding.btnWriteDietLog.setOnClickListener {
            SaveDialog(requireActivity()) {
                mealData = DietLogData(
                    id= 0, // ID는 Room DB에서 자동 생성되므로 0으로 설정
                    dietTitle = dietTitle,
                    dietCategory = dietCategory,
                    score = score,
                    ingredientTags = ingredientTags,
                    time = selectedTime.atDate(LocalDate.now()),
                    dietImg = dietImg
                )

                Log.d("DietLogFragment", "저장할 selectedTime: $selectedTime")
                Log.d("DietLogFragment", dietImg)

                // 업로드용 이미지 File 객체 생성
                // 이미지 경로에 따라 File 객체 생성
                val imageFile: File? = when {
                    dietImg.startsWith("http://") || dietImg.startsWith("https://") -> {
                        null
                    }
                    dietImg.startsWith("android.resource://") -> {
                        // 리소스 ID 추출
                        val resId = dietImg.substringAfterLast("/").toIntOrNull()
                        if (resId != null) copyResourceToFile(requireContext(), resId) else null
                    }
                    dietImg.startsWith("content://") -> {
                        // content URI를 내부 파일로 복사
                        val uri = Uri.parse(dietImg)
                        val copiedUri = copyUriToInternal(requireContext(), uri)
                        copiedUri?.let { File(it.path!!) }
                    }
                    dietImg.startsWith("file://") -> {
                        File(Uri.parse(dietImg).path!!)
                    }
                    else -> {
                        File(dietImg)
                    }
                }

                // 이미지 파일이 null인 경우는 서버 URL이거나 파일 생성이 필요 없는 경우임
                if (imageFile != null && !imageFile.exists()) {
                    Toast.makeText(requireContext(), "이미지 파일 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@SaveDialog
                }

                // 수정 모드
                if(isEditMode) {
                    try {
                        // Room DB 업데이트 + 서버 업로드 동시 실행
                        dietLogViewModel.updateDietLog(mealData, imageFile)
                        Toast.makeText(requireContext(), "식단 기록을 수정하였습니다.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // 로컬 DB에만 저장
                        dietLogViewModel.updateToLocalDB(mealData)
                        Toast.makeText(requireContext(), "식단 기록 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        return@SaveDialog
                    }

                } else {
                    try {
                        // Room 저장 + 서버 업로드 동시 실행
                        dietLogViewModel.saveAndUpload(mealData, imageFile!!)
                        Toast.makeText(requireContext(), "식단 기록을 저장하였습니다.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        // 로컬에만 저장
                        dietLogViewModel.saveToLocalDB(mealData)
                        Toast.makeText(requireContext(), "식단 기록을 저장하지 못했습니다.", Toast.LENGTH_SHORT).show()
                        return@SaveDialog
                    }
                }
            }.show()
        }

        setImg()
        checkAllValid()
    }

    // 이미지 설정
    private fun setImg() {
        val imgSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                // 1. 내부 캐시에 복사
                val copiedUri = copyUriToInternal(requireContext(), uri)
                if (copiedUri != null) {
                    dietLogBinding.dietImg.setImageURI(copiedUri)
                    dietImg = copiedUri.toString()
                }

                // 이미지 생성 시간
                val createdDateTime = getImageCreatedTime(requireContext(), uri)
                val formattedTime = DateTimeFormatter
                    .ofPattern("a h:mm", Locale.ENGLISH)
                    .format(createdDateTime)
                dietLogBinding.time12h.text = formattedTime

                if (createdDateTime != LocalDateTime.now())
                    dietLogBinding.setNowCb.isChecked = false
            }
        }

        dietLogBinding.dietImg.setOnClickListener {
            SelectImgDialog(requireContext(),
                {
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
                    dietLogBinding.dietImg.setImageURI(uri)
                    dietImg = uri.toString()
                },
                {
                    imgSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ).show()
        }
    }

    // 이미지 생성 시간 가져오기
    private fun getImageCreatedTime(context: Context, imageUri: Uri): LocalDateTime {
        // MediaStore에서 이미지의 생성 시간 가져오기
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
            // 커서가 비어있지 않으면 이미지 생성 시간 가져오기
            if (cursor.moveToFirst()) {
                val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)

                val dateTaken = if (dateTakenIndex != -1) cursor.getLong(dateTakenIndex) else null
                val dateAdded = if (dateAddedIndex != -1) cursor.getLong(dateAddedIndex) else null

                val timeMillis = dateTaken ?: dateAdded ?: System.currentTimeMillis()

                // 이미지 생성 시간을 LocalDateTime으로 변환
                return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timeMillis),
                    ZoneId.systemDefault()
                )
            }
        }
        // 이미지 생성 시간이 없으면 현재 시간으로 설정
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(System.currentTimeMillis()),
            ZoneId.systemDefault()
        )
    }

    // 모든 변수에 유효한 값이 들어왔는지 확인
    fun checkAllValid() {
        // 다 유효한 상태일 떄만 버튼 활성화
        val allValid = dietImg.isNotBlank() && dietTitle.isNotBlank() && ingredientTags.isNotEmpty() && score != 0
        dietLogBinding.btnWriteDietLog.isEnabled = allValid

        // 버튼 색상 변경 (활성화: 주황, 비활성화: 회색)
        dietLogBinding.btnWriteDietLog.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (allValid) R.color.elixir_orange
            else R.color.elixir_gray
        )
    }

    // Photo Picker URI를 내부 캐시에 복사
    private fun copyUriToInternal(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    private fun copyResourceToFile(context: Context, resId: Int): File? {
        return try {
            val inputStream = context.resources.openRawResource(resId)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    // 이미 선택된 식재료 태그를 미리 추가
    private fun showInitialIngredientChips(
        ingredientTags: List<Int>,
        ingredientMap: Map<Int, IngredientData>,
        chipGroup: ChipGroup,
        findIngredientChip: Chip
    ) {
        // 기존 Chip 모두 제거 (findIngredientChip만 남기기)
        chipGroup.removeAllViews()
        chipGroup.addView(findIngredientChip)

        ingredientTags.forEach { ingredientId ->
            val ingredientName = ingredientMap[ingredientId]?.name ?: "알 수 없음"
            val chip = Chip(ContextThemeWrapper(chipGroup.context, R.style.ChipStyle_Short)).apply {
                text = ingredientName
                isClickable = true
                isCheckable = false
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.elixir_orange)
                )
                setTextColor(ContextCompat.getColor(context, R.color.white))
                // 칩 클릭 리스너로 변경
                setOnClickListener {
                    (ingredientTags as? MutableList)?.remove(ingredientId)
                    chipGroup.removeView(this)
                    checkAllValid()
                }
            }
            // findIngredientChip 앞에 삽입
            val index = chipGroup.indexOfChild(findIngredientChip)
            chipGroup.addView(chip, index)
        }
    }
}