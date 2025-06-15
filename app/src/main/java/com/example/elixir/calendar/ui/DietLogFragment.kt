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
import androidx.lifecycle.lifecycleScope
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
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

class DietLogFragment : Fragment() {
    // 바인딩, 뷰모델 정의
    private lateinit var dietLogBinding: FragmentDietLogBinding

    // 날짜 & 시간
    private lateinit var formattedTime: String
    private var selectedHour: Int = -1
    private var selectedMin: Int = -1
    private lateinit var selectedTime: LocalTime

    // 정보
    private var dietImg: String = "" // 이미지 URI (http/s, file, android.resource)
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

    private var isUserSelectedImage: Boolean = false // 사용자가 갤러리에서 직접 선택했는지 추적


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

        // currentMemberId 초기화
        lifecycleScope.launch {
            dietRepository.setCurrentMemberIdFromDb(memberRepository)
        }

        // 수정 모드일 때 초기화
        if (mealDataJson != null) {
            mealData = Gson().fromJson(mealDataJson, DietLogData::class.java)
            isEditMode = true
            dietId = mealData.id // 수정 모드일 때 기존 ID 저장
            dietLogBinding.enterDietTitle.setText(mealData.dietTitle)

            dietTitle = mealData.dietTitle
            dietImg = mealData.dietImg // 기존 이미지 URI 설정
            dietCategory = mealData.dietCategory
            score = mealData.score

            isUserSelectedImage = false

            // 이미지 처리: Glide는 다양한 URI 형식을 자동으로 처리합니다.
            Glide.with(requireContext())
                .load(dietImg) // mealData.dietImg가 이미 적절한 URI 형태일 것이므로 그대로 사용
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(dietLogBinding.dietImg)

            // 시간 처리
            selectedTime = mealData.time.toLocalTime()
            selectedHour = selectedTime.hour
            selectedMin = selectedTime.minute
            dietLogBinding.setNowCb.isChecked = (selectedTime.hour == LocalTime.now().hour && selectedTime.minute == LocalTime.now().minute) // 현재 시간과 정확히 일치하는지 확인
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
            dietImg = defaultUri.toString() // dietImg 변수에도 저장

            // 현재 시간으로 초기화
            selectedTime = LocalTime.now()
            dietLogBinding.setNowCb.isChecked = true

            selectedHour = selectedTime.hour
            selectedMin = selectedTime.minute

            formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
            dietLogBinding.time12h.text = formattedTime

            isUserSelectedImage = false

            checkAllValid()
        }

        // -------------------------------------------- 리스너 -----------------------------------------------//
        // 현재 시간으로 설정: 체크하면 현재 시간으로 설정되도록
        dietLogBinding.setNowCb.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 시간 현재로 재설정
                val now = LocalTime.now()
                selectedTime = now // 현재 시간으로 업데이트
                dietLogBinding.setNowCb.isChecked = (selectedTime.hour == now.hour && selectedTime.minute == now.minute) // 정확한 비교

                formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                dietLogBinding.time12h.text = formattedTime
                selectedHour = now.hour
                selectedMin = now.minute
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
            val oldDietCategory = dietCategory
            when(checkedId) {
                dietLogBinding.btnBreakfast.id -> {
                    dietCategory = getString(R.string.breakfast)
                }
                dietLogBinding.btnLunch.id -> {
                    dietCategory = getString(R.string.lunch)
                }
                dietLogBinding.btnDinner.id -> {
                    dietCategory = getString(R.string.dinner)
                }
                dietLogBinding.btnSnack.id -> {
                    dietCategory = getString(R.string.snack)
                }
            }

            // 사용자가 갤러리에서 직접 선택하지 않은 경우에만 이미지 변경
            if (!isUserSelectedImage) {
                val defaultImageResId = when (dietCategory) {
                    getString(R.string.breakfast) -> R.drawable.ic_meal_morning
                    getString(R.string.lunch) -> R.drawable.ic_meal_lunch
                    getString(R.string.dinner) -> R.drawable.ic_meal_dinner
                    getString(R.string.snack) -> R.drawable.ic_meal_snack
                    else -> R.drawable.img_blank
                }

                // SVG를 파일로 변환
                val imageFile = copyResourceToFile(requireContext(), defaultImageResId)
                if (imageFile != null) {
                    val fileUri = Uri.fromFile(imageFile)
                    dietLogBinding.dietImg.setImageURI(fileUri)
                    dietImg = fileUri.toString()
                    Log.d("DietLogFragment", "Diet category changed, image updated: $dietImg")
                } else {
                    // 변환 실패 시 리소스 URI 사용
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/$defaultImageResId")
                    dietLogBinding.dietImg.setImageURI(uri)
                    dietImg = uri.toString()
                    Log.w("DietLogFragment", "Failed to convert SVG, using resource URI: $dietImg")
                }
            } else {
                Log.d("DietLogFragment", "User selected image preserved")
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
            // 칩 상태 토글 (선택 해제)
            dietLogBinding.findIngredient.isChecked = false // 항상 false로 설정하여, 검색 Fragment에서 돌아왔을 때 다시 누를 수 있도록 함

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
                // 이전에 눌렸던 상태와 상관없이 항상 초기화
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
                val uploadedDietLogData = result.getOrNull()
                if (isEditMode) { // isEditMode 확인 추가
                    mealDataJson = Gson().toJson(uploadedDietLogData) // 성공한 DietLogData를 받아와 업데이트
                    val intent = Intent().apply {
                        putExtra("mode", 0) // 0: 수정 모드임을 알림
                        putExtra("dietLogData", mealDataJson)
                    }
                    Log.d("DietLogFragment", "Activity finish() 호출됨 (수정 모드)")
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
                } else {
                    // 새로 저장하는 경우 (isEditMode == false)
                    val intent = Intent().apply {
                        putExtra("mode", 1) // 1: 새 저장 모드임을 알림 (선택 사항)
                        putExtra("dietLogData", Gson().toJson(uploadedDietLogData)) // 새로 생성된 데이터도 전달 가능
                    }
                    Log.d("DietLogFragment", "Activity finish() 호출됨 (새 저장 모드)")
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
                }
            } else {
                Toast.makeText(requireContext(), "오늘은 이미 해당 식사 유형을 기록하셨습니다.", Toast.LENGTH_SHORT).show()
                // 실패 시에도 finish()를 호출하여 이전 화면으로 돌아가도록 처리
                requireActivity().setResult(Activity.RESULT_CANCELED)
                requireActivity().finish()
            }
        }


        // 작성 버튼
        dietLogBinding.btnWriteDietLog.setOnClickListener {
            SaveDialog(requireActivity()) {
                // 현재 입력된 값들로 mealData 객체 생성
                val currentMealData = DietLogData(
                    id = if (isEditMode) dietId else 0, // 수정 모드일 때 mealData.id 사용, 아니면 0
                    dietTitle = dietTitle,
                    dietCategory = dietCategory,
                    score = score,
                    ingredientTags = ingredientTags,
                    time = if (isEditMode) {
                        // 수정 모드일 때는 기존 날짜 유지
                        LocalDateTime.of(mealData.time.toLocalDate(), selectedTime)
                    } else {
                        // 새로운 기록일 때는 현재 날짜 사용
                        selectedTime.atDate(LocalDate.now())
                    },
                    dietImg = dietImg
                )

                Log.d("DietLogFragment", "저장할 selectedTime: $selectedTime")
                Log.d("DietLogFragment", "저장할 dietImg URI: $dietImg")

                // 업로드용 이미지 File 객체 생성
                val imageFile: File? = when {
                    dietImg.startsWith("http://") || dietImg.startsWith("https://") -> {
                        // 서버 이미지인 경우
                        null
                    }
                    dietImg.startsWith("android.resource://") -> {
                        val resId = dietImg.substringAfterLast("/").toIntOrNull()
                        copyResourceToFile(requireContext(), resId ?: R.drawable.img_blank)
                    }
                    dietImg.startsWith("content://") -> {
                        // content:// URI는 반드시 파일로 복사해서 사용해야 함
                        val uri = Uri.parse(dietImg)
                        val copiedUri = copyUriToInternal(requireContext(), uri)
                        copiedUri?.let {
                            val file = File(it.path ?: "")
                            if (file.exists()) file else null
                        }
                    }
                    dietImg.startsWith("file://") -> {
                        val uri = Uri.parse(dietImg)
                        val file = File(uri.path ?: "")
                        if (file.exists()) file else null
                    }
                    else -> {
                        if (dietImg.isNotBlank()) {
                            val file = File(dietImg)
                            if (file.exists()) file else null
                        } else null
                    }
                }


                // 수정 모드
                if (isEditMode) {
                    try {
                        dietLogViewModel.updateDietLog(currentMealData, imageFile)
                        Toast.makeText(requireContext(), "식단 기록을 수정하였습니다.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("DietLogFragment", "식단 기록 수정 중 오류", e)
                        dietLogViewModel.updateToLocalDB(currentMealData)
                        Toast.makeText(requireContext(), "식단 기록 수정에 실패했습니다. 로컬에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    try {
                        if (imageFile != null) {
                            dietLogViewModel.saveAndUpload(currentMealData, imageFile)
                            Toast.makeText(requireContext(), "식단 기록을 저장하였습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "이미지 파일 처리에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("DietLogFragment", "Image file is null for new save")
                            return@SaveDialog
                        }
                    } catch (e: Exception) {
                        Log.e("DietLogFragment", "식단 기록 저장 중 오류", e)
                        dietLogViewModel.saveToLocalDB(currentMealData)
                        Toast.makeText(requireContext(), "식단 기록을 저장하지 못했습니다. 로컬에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }.show()
        }

        setupImageSelectionListeners()
        checkAllValid()
    }

    // 이미지 설정 관련 리스너만 모아둔 함수
    private fun setupImageSelectionListeners() {
        val imgSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                // 사용자가 갤러리에서 이미지를 선택했음을 표시
                isUserSelectedImage = true

                // 1. 내부 캐시에 복사. 반환된 URI는 file:// URI 형태일 것.
                val copiedFileUri = copyUriToInternal(requireContext(), uri)
                if (copiedFileUri != null) {
                    dietLogBinding.dietImg.setImageURI(copiedFileUri)
                    dietImg = copiedFileUri.toString() // dietImg에 file:// URI 저장
                    Log.d("DietLogFragment", "Image selected and copied to: $dietImg")
                } else {
                    Toast.makeText(requireContext(), "이미지 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("DietLogFragment", "Failed to copy selected image URI.")
                    dietImg = "" // 실패 시 dietImg 초기화
                    dietLogBinding.dietImg.setImageResource(R.drawable.img_blank)
                }

                // 이미지 생성 시간
                try {
                    val createdDateTime = getImageCreatedTime(requireContext(), uri)
                    selectedTime = createdDateTime.toLocalTime() // 선택된 이미지의 시간으로 selectedTime 업데이트
                    formattedTime = DateTimeFormatter
                        .ofPattern("a h:mm", Locale.ENGLISH)
                        .format(createdDateTime)
                    dietLogBinding.time12h.text = formattedTime

                    // 이미지 생성 시간과 현재 시간이 다르면 '현재 시간으로 설정' 체크박스 해제
                    if (createdDateTime.toLocalTime().withSecond(0).withNano(0) != LocalTime.now().withSecond(0).withNano(0))
                        dietLogBinding.setNowCb.isChecked = false
                    else
                        dietLogBinding.setNowCb.isChecked = true
                } catch (e: Exception) {
                    Log.e("DietLogFragment", "Failed to get image creation time: ${e.message}")
                    // 시간 가져오기 실패 시 현재 시간으로 유지
                    dietLogBinding.setNowCb.isChecked = true
                    selectedTime = LocalTime.now()
                    formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                    dietLogBinding.time12h.text = formattedTime
                }
                checkAllValid()
            }
        }

        dietLogBinding.dietImg.setOnClickListener {
            SelectImgDialog(requireContext(),
                {
                    // 기본 이미지 설정 - 이 경우는 사용자가 직접 선택한 것이 아님
                    isUserSelectedImage = false

                    val defaultImageResId = when (dietCategory) {
                        getString(R.string.breakfast) -> R.drawable.ic_meal_morning
                        getString(R.string.lunch) -> R.drawable.ic_meal_lunch
                        getString(R.string.dinner) -> R.drawable.ic_meal_dinner
                        getString(R.string.snack) -> R.drawable.ic_meal_snack
                        else -> R.drawable.img_blank
                    }

                    // SVG를 파일로 변환
                    val imageFile = copyResourceToFile(requireContext(), defaultImageResId)
                    if (imageFile != null) {
                        val fileUri = Uri.fromFile(imageFile)
                        dietLogBinding.dietImg.setImageURI(fileUri)
                        dietImg = fileUri.toString() // file:// URI로 저장
                        Log.d("DietLogFragment", "Default resource image converted and set: $dietImg")
                    } else {
                        // 변환 실패 시 기본 처리
                        val uri = Uri.parse("android.resource://${requireContext().packageName}/$defaultImageResId")
                        dietLogBinding.dietImg.setImageURI(uri)
                        dietImg = uri.toString()
                        Log.w("DietLogFragment", "Failed to convert SVG, using resource URI: $dietImg")
                    }
                    checkAllValid()
                },
                {
                    // 갤러리 선택 - 이후에 imgSelector에서 isUserSelectedImage = true로 설정됨
                    imgSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ).show()
        }

        // 초기 이미지 설정 시에도 플래그 설정
        if (!isEditMode && dietImg.isBlank()) {
            isUserSelectedImage = false // 초기 기본 이미지는 사용자 선택이 아님
            val defaultImageResId = when (dietCategory) {
                getString(R.string.breakfast) -> R.drawable.ic_meal_morning
                getString(R.string.lunch) -> R.drawable.ic_meal_lunch
                getString(R.string.dinner) -> R.drawable.ic_meal_dinner
                getString(R.string.snack) -> R.drawable.ic_meal_snack
                else -> R.drawable.img_blank
            }

            // SVG를 파일로 변환
            val imageFile = copyResourceToFile(requireContext(), defaultImageResId)
            if (imageFile != null) {
                val fileUri = Uri.fromFile(imageFile)
                dietLogBinding.dietImg.setImageURI(fileUri)
                dietImg = fileUri.toString() // file:// URI로 저장
                Log.d("DietLogFragment", "Initial SVG image converted and set: $dietImg")
            } else {
                // 변환 실패 시 리소스 URI 사용
                val uri = Uri.parse("android.resource://${requireContext().packageName}/$defaultImageResId")
                dietLogBinding.dietImg.setImageURI(uri)
                dietImg = uri.toString()
                Log.d("DietLogFragment", "Initial image set as resource URI: $dietImg")
            }
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

                val timeMillis = dateTaken ?: dateAdded?.times(1000L) ?: System.currentTimeMillis() // DATE_ADDED는 초 단위일 수 있어 1000L 곱함

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

    // Photo Picker URI를 내부 캐시에 복사 (압축 제거)
    private fun copyUriToInternal(context: Context, uri: Uri): Uri? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // 캐시 디렉토리에 임시 파일 생성 (갤러리 선택 이미지임을 구분하기 위해 파일명 변경)
                val file = File(context.cacheDir, "user_selected_image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                Uri.fromFile(file) // file:// URI 형태로 반환
            }
        } catch (e: Exception) {
            Log.e("DietLogFragment", "이미지 복사 실패", e)
            null
        }
    }


    private fun copyResourceToFile(context: Context, resId: Int): File? {
        return try {
            // SVG 리소스를 비트맵으로 변환
            val drawable = ContextCompat.getDrawable(context, resId)
            if (drawable == null) {
                Log.e("DietLogFragment", "Failed to load drawable resource: $resId")
                return null
            }

            // 비트맵 생성 (SVG를 래스터화)
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.takeIf { it > 0 } ?: 512,
                drawable.intrinsicHeight.takeIf { it > 0 } ?: 512,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            // 파일로 저장 (기본 이미지임을 구분하기 위해 파일명 변경)
            val file = File(context.cacheDir, "default_resource_image_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            // 비트맵 메모리 해제
            bitmap.recycle()

            if (file.exists() && file.length() > 0) {
                Log.d("DietLogFragment", "Resource file created successfully: ${file.absolutePath}, size: ${file.length()}")
                file
            } else {
                Log.e("DietLogFragment", "Resource file creation failed or empty")
                null
            }
        } catch (e: Exception) {
            Log.e("DietLogFragment", "리소스 이미지 변환 실패", e)
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
                    (this@DietLogFragment.ingredientTags as? MutableList)?.remove(ingredientId) // Fragment의 ingredientTags 업데이트
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