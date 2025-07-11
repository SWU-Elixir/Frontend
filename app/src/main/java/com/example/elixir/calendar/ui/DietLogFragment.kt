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
import androidx.fragment.app.setFragmentResultListener
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
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import android.graphics.Bitmap
import com.example.elixir.dialog.EditNoticedDialog
import com.example.elixir.dialog.PreviousNoticedDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DietLogFragment : Fragment() {
    // 바인딩, 뷰모델 정의
    private lateinit var dietLogBinding: FragmentDietLogBinding

    // 날짜 & 시간
    private lateinit var formattedTime: String
    private var selectedHour: Int = -1
    private var selectedMin: Int = -1
    private lateinit var selectedTime: LocalTime
    private var selectedDate: LocalDate? = null // 선택된 날짜 저장용

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
    private var initialDietImg: String = ""
    private var isDefaultImage: Boolean = false


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

        // 선택된 날짜 받아오기
        val selectedDateStr = arguments?.getString("selectedDate")
        selectedDate = if (selectedDateStr != null) {
            LocalDate.parse(selectedDateStr)
        } else {
            LocalDate.now() // 기본값은 오늘
        }
        // 날짜 초기화 후 로그 추가
        Log.d("DietLogFragment", "Selected date: $selectedDate")
        Log.d("DietLogFragment", "Selected date string: $selectedDateStr")

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

        // 기존 식단 가져오기
        parentFragmentManager.setFragmentResultListener("meal_selection_request", viewLifecycleOwner) { key, bundle ->
            val mealDataJsonFromRecent = bundle.getString("selected_meal_data")
            if (mealDataJsonFromRecent != null) {
                // 수신된 데이터를 DietLogData 객체로 변환
                val selectedMeal = Gson().fromJson(mealDataJsonFromRecent, DietLogData::class.java)
                Log.d("DietLogFragment", "Received meal from recent list: ${selectedMeal.dietTitle}")
                Log.d("DietLogFragment", "Received meal data: $mealDataJsonFromRecent")

                // UI 업데이트 및 수정 모드 전환
                updateUIWithSelectedMeal(selectedMeal)
            }
        }

        // 수정 모드일 때 초기화
        if (mealDataJson != null) {
            mealData = Gson().fromJson(mealDataJson, DietLogData::class.java)
            updateUIWithSelectedMeal(mealData)
            isEditMode = true
            dietId = mealData.id // 수정 모드일 때 기존 ID 저장
            dietLogBinding.etDietTitle.setText(mealData.dietTitle)

            // 수정 모드일 때 최근 버튼 숨기기
            dietLogBinding.btnRecent.visibility = View.GONE

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
                .into(dietLogBinding.imgDiet)

            // 시간 처리
            selectedTime = mealData.time.toLocalTime()
            selectedHour = selectedTime.hour
            selectedMin = selectedTime.minute
            dietLogBinding.cbSetNow.isChecked = (selectedTime.hour == LocalTime.now().hour && selectedTime.minute == LocalTime.now().minute) // 현재 시간과 정확히 일치하는지 확인
            dietLogBinding.tvTime12h.text = mealData.time.format(DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH))

            // 점수(1~5)에 따라 라디오버튼 체크
            when (score) {
                1 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn1.id)
                2 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn2.id)
                3 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn3.id)
                4 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn4.id)
                5 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn5.id)
            }

            // 카테고리(아침, 점심, 저녁, 간식)에 따라 라디오버튼 체크
            when (dietCategory) {
                getString(R.string.breakfast) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnBreakfast.id)
                getString(R.string.lunch) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnLunch.id)
                getString(R.string.dinner) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnDinner.id)
                getString(R.string.snack) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnSnack.id)
            }

            // 기본 이미지인지 판단 (더 정확한 판단을 위해)
            isDefaultImage = when {
                dietImg.contains("ic_meal_morning") || dietImg.contains("default_resource_image") -> true
                dietImg.contains("ic_meal_lunch") || dietImg.contains("default_resource_image") -> true
                dietImg.contains("ic_meal_dinner") || dietImg.contains("default_resource_image") -> true
                dietImg.contains("ic_meal_snack") || dietImg.contains("default_resource_image") -> true
                dietImg.contains("img_blank") || dietImg.contains("default_resource_image") -> true
                // 파일명으로도 판단 (copyResourceToFile에서 생성된 파일명 패턴)
                dietImg.contains("default_resource_image_") -> true
                else -> false
            }

            isUserSelectedImage = !isDefaultImage

            // 식재료 태그
            ingredientTags = mealData.ingredientTags.toMutableList()

            dietLogViewModel.loadIngredients()

            dietLogViewModel.ingredientList.observe(viewLifecycleOwner) { ingredientList ->
                // ingredientMap 생성
                val ingredientMap = ingredientList.associateBy { it.id }

                // dietLogData?.ingredientTags는 태그로 보여줄 id 리스트라고 가정
                val ingredientTags = mealData.ingredientTags

                showInitialIngredientChips(ingredientTags = ingredientTags, ingredientMap = ingredientMap,
                    chipGroup = dietLogBinding.cgTagsIngredient, findIngredientChip = dietLogBinding.chipFindIngredient)
            }

            checkAllValid()

        } else {
            // 새로운 식단 작성 모드일 때 최근 버튼 표시
            dietLogBinding.btnRecent.visibility = View.VISIBLE

            // 기본 이미지로 설정
            val defaultUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
            dietLogBinding.imgDiet.setImageURI(defaultUri)
            dietImg = defaultUri.toString() // dietImg 변수에도 저장
            initialDietImg = defaultUri.toString()

            // 현재 시간으로 초기화
            selectedTime = LocalTime.now()
            dietLogBinding.cbSetNow.isChecked = true

            selectedHour = selectedTime.hour
            selectedMin = selectedTime.minute

            formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
            dietLogBinding.tvTime12h.text = formattedTime

            isUserSelectedImage = false

            checkAllValid()
        }

        // -------------------------------------------- 리스너 -----------------------------------------------//
        // 현재 시간으로 설정: 체크하면 현재 시간으로 설정되도록
        dietLogBinding.cbSetNow.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 시간 현재로 재설정
                val now = LocalTime.now()
                selectedTime = now // 현재 시간으로 업데이트
                dietLogBinding.cbSetNow.isChecked = (selectedTime.hour == now.hour && selectedTime.minute == now.minute) // 정확한 비교

                formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                dietLogBinding.tvTime12h.text = formattedTime
                selectedHour = now.hour
                selectedMin = now.minute
            }
            checkAllValid()
        }

        dietLogBinding.btnRecent.setOnClickListener{
            val mealRecentFragment = MealRecentListFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .add(android.R.id.content, mealRecentFragment)
                .addToBackStack(null) // 백 스택에 추가하여 뒤로 가기 버튼으로 돌아올 수 있도록 합니다.
                .commit()
        }

        // 타임피커: 다이얼로그 띄워주기(material3 제공)
        dietLogBinding.btnTimePicker.setOnClickListener {
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
                dietLogBinding.cbSetNow.isChecked = false

                // 선택된 시간 텍스트뷰에 띄워주기
                formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                dietLogBinding.tvTime12h.text = formattedTime

                checkAllValid()
            }

        }

        // 식단명 입력, 변경 탐지 및 유효성 검사
        dietLogBinding.etDietTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                dietTitle = s.toString().trim()
                checkAllValid()
            }
        })

        // 라디오 버튼 : 식단 유형 선택
        dietLogBinding.rgSelectDiet.setOnCheckedChangeListener { _, checkedId ->
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

            // 기본 이미지이고 사용자가 갤러리에서 선택하지 않은 경우에만 이미지 변경
            if (!isUserSelectedImage && (isDefaultImage || !isEditMode)) {
                val defaultImageResId = when (dietCategory) {
                    getString(R.string.breakfast) -> R.drawable.ic_meal_morning
                    getString(R.string.lunch) -> R.drawable.ic_meal_lunch
                    getString(R.string.dinner) -> R.drawable.ic_meal_dinner
                    getString(R.string.snack) -> R.drawable.ic_meal_snack
                    else -> R.drawable.img_blank
                }

                val imageFile = copyResourceToFile(requireContext(), defaultImageResId)
                if (imageFile != null) {
                    val fileUri = Uri.fromFile(imageFile)
                    dietLogBinding.imgDiet.setImageURI(fileUri)
                    dietImg = fileUri.toString()
                    Log.d("DietLogFragment", "Diet category changed, image updated: $dietImg")
                } else {
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/$defaultImageResId")
                    dietLogBinding.imgDiet.setImageURI(uri)
                    dietImg = uri.toString()
                }
            }
        }


        // SearchFragment에서 전달된 결과 수신
        parentFragmentManager.setFragmentResultListener("ingredient_selection", viewLifecycleOwner) { _, bundle ->
            val ingredientId = bundle.getInt("ingredientId", -1)
            val ingredientName = bundle.getString("ingredientName") ?: return@setFragmentResultListener

            if (ingredientId == -1) return@setFragmentResultListener
            val findIngredientChip = dietLogBinding.chipFindIngredient

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
                    dietLogBinding.cgTagsIngredient.removeView(this)
                    checkAllValid()
                }
            }

            // findIngredient Chip 앞에 삽입
            val index = dietLogBinding.cgTagsIngredient.indexOfChild(findIngredientChip)
            dietLogBinding.cgTagsIngredient.addView(chip, index)

            // 리스트에 추가 (ID 저장)
            ingredientTags.add(ingredientId)
            checkAllValid()
        }

        // 식재료 검색 버튼 클릭 리스너
        dietLogBinding.chipFindIngredient.setOnClickListener {
            // 칩 상태 토글 (선택 해제)
            dietLogBinding.chipFindIngredient.isChecked = false // 항상 false로 설정하여, 검색 Fragment에서 돌아왔을 때 다시 누를 수 있도록 함

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
                dietLogBinding.chipFindIngredient.isChecked = false
            }
        })

        // 라디오 버튼: 점수
        dietLogBinding.rgSelectScore.setOnCheckedChangeListener { _, checkedId ->
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

            // 시간 계산
            val calculatedTime = if (isEditMode) {
                LocalDateTime.of(mealData.time.toLocalDate(), selectedTime)
            } else {
                LocalDateTime.of(selectedDate ?: LocalDate.now(), selectedTime)
            }

            // 실제 저장 로직을 실행하는 함수
            fun executeSave() {
                SaveDialog(requireActivity()) {
                    // 현재 입력된 값들로 mealData 객체 생성
                    val currentMealData = DietLogData(
                        id = if (isEditMode) dietId else 0,
                        dietTitle = dietTitle,
                        dietCategory = dietCategory,
                        score = score,
                        ingredientTags = ingredientTags,
                        time = calculatedTime,
                        dietImg = dietImg
                    )

                    // 코루틴으로 이미지 처리
                    lifecycleScope.launch {
                        // 업로드용 이미지 File 객체 생성
                        val imageFile: File? = when {
                            dietImg.startsWith("http://") || dietImg.startsWith("https://") -> {
                                // 🔥 서버 이미지 처리 로직 수정
                                Log.d("DietLogFragment", "Processing server image: $dietImg")

                                // 불러온 식단의 서버 이미지를 다운로드해서 파일로 변환
                                val downloadedFile = downloadImageToFile(dietImg)
                                if (downloadedFile != null) {
                                    Log.d("DietLogFragment", "Server image downloaded successfully")
                                    downloadedFile
                                } else {
                                    Log.e("DietLogFragment", "Failed to download server image")
                                    // 다운로드 실패 시에도 null 반환하지 말고 기본 이미지로 처리
                                    val defaultImageResId = when (dietCategory) {
                                        getString(R.string.breakfast) -> R.drawable.ic_meal_morning
                                        getString(R.string.lunch) -> R.drawable.ic_meal_lunch
                                        getString(R.string.dinner) -> R.drawable.ic_meal_dinner
                                        getString(R.string.snack) -> R.drawable.ic_meal_snack
                                        else -> R.drawable.img_blank
                                    }
                                    copyResourceToFile(requireContext(), defaultImageResId)
                                }
                            }
                            dietImg.startsWith("android.resource://") -> {
                                val resId = dietImg.substringAfterLast("/").toIntOrNull()
                                copyResourceToFile(requireContext(), resId ?: R.drawable.img_blank)
                            }
                            dietImg.startsWith("content://") -> {
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

                        // 메인 스레드에서 UI 업데이트
                        withContext(Dispatchers.Main) {
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
                                        return@withContext
                                    }
                                } catch (e: Exception) {
                                    Log.e("DietLogFragment", "식단 기록 저장 중 오류", e)
                                    dietLogViewModel.saveToLocalDB(currentMealData)
                                    Toast.makeText(requireContext(), "식단 기록을 저장하지 못했습니다. 로컬에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }.show()
            }

            // 다이얼로그 조건 검사
            if (isEditMode) {
                // 수정 모드: EditNoticedDialog 표시
                val prefs = requireContext().getSharedPreferences("dialog_prefs", Context.MODE_PRIVATE)
                val skipEditNotice = prefs.getBoolean("skip_edit_notice", false)

                if (!skipEditNotice) {
                    // EditNoticedDialog를 먼저 띄우고, 완료 후 저장 다이얼로그 실행
                    EditNoticedDialog(requireActivity()) { checked ->
                        if (checked) {
                            prefs.edit().putBoolean("skip_edit_notice", true).apply()
                        }
                        // EditNoticedDialog가 닫힌 후 저장 다이얼로그 실행
                        executeSave()
                    }.show()
                } else {
                    // "다시 보지 않기"가 설정되어 있으면 바로 저장 다이얼로그 실행
                    executeSave()
                }
            } else if ((selectedDate ?: LocalDate.now()).isBefore(LocalDate.now())) {
                // 새 저장 모드 + 과거 날짜: PreviousNoticedDialog 표시
                val prefs = requireContext().getSharedPreferences("dialog_prefs", Context.MODE_PRIVATE)
                val skipPreviousNotice = prefs.getBoolean("skip_previous_notice", false)

                if (!skipPreviousNotice) {
                    // PreviousNoticedDialog를 먼저 띄우고, 완료 후 저장 다이얼로그 실행
                    PreviousNoticedDialog(requireActivity()) { checked ->
                        if (checked) {
                            prefs.edit().putBoolean("skip_previous_notice", true).apply()
                        }
                        // PreviousNoticedDialog가 닫힌 후 저장 다이얼로그 실행
                        executeSave()
                    }.show()
                } else {
                    // "다시 보지 않기"가 설정되어 있으면 바로 저장 다이얼로그 실행
                    executeSave()
                }
            } else {
                // 새 저장 모드 + 현재/미래 날짜: 바로 저장 다이얼로그 실행
                executeSave()
            }
        }

        setupImageSelectionListeners()
        checkAllValid()
    }

    private suspend fun downloadImageToFile(imageUrl: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DietLogFragment", "Downloading image from: $imageUrl")

                val url = java.net.URL(imageUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000 // 10초 타임아웃
                connection.readTimeout = 10000
                connection.connect()

                val inputStream = connection.getInputStream()
                val file = File(requireContext().cacheDir, "server_image_${System.currentTimeMillis()}.jpg")

                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }

                inputStream.close()

                if (file.exists() && file.length() > 0) {
                    Log.d("DietLogFragment", "Image downloaded successfully: ${file.absolutePath}, size: ${file.length()}")
                    file
                } else {
                    Log.e("DietLogFragment", "Downloaded file is empty or doesn't exist")
                    null
                }
            } catch (e: Exception) {
                Log.e("DietLogFragment", "Failed to download image: ${e.message}", e)
                null
            }
        }
    }

    // 이미지 설정 관련 리스너만 모아둔 함수
    private fun setupImageSelectionListeners() {
        val imgSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                // 사용자가 갤러리에서 이미지를 선택했음을 표시
                isUserSelectedImage = true
                isDefaultImage = false

                // 1. 내부 캐시에 복사. 반환된 URI는 file:// URI 형태일 것.
                val copiedFileUri = copyUriToInternal(requireContext(), uri)
                if (copiedFileUri != null) {
                    dietLogBinding.imgDiet.setImageURI(copiedFileUri)
                    dietImg = copiedFileUri.toString() // dietImg에 file:// URI 저장
                    Log.d("DietLogFragment", "Image selected and copied to: $dietImg")
                } else {
                    Toast.makeText(requireContext(), "이미지 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("DietLogFragment", "Failed to copy selected image URI.")
                    dietImg = "" // 실패 시 dietImg 초기화
                    dietLogBinding.imgDiet.setImageResource(R.drawable.img_blank)
                }

                // 이미지 생성 시간
                try {
                    val createdDateTime = getImageCreatedTime(requireContext(), uri)
                    selectedTime = createdDateTime.toLocalTime() // 선택된 이미지의 시간으로 selectedTime 업데이트
                    formattedTime = DateTimeFormatter
                        .ofPattern("a h:mm", Locale.ENGLISH)
                        .format(createdDateTime)
                    dietLogBinding.tvTime12h.text = formattedTime

                    // 이미지 생성 시간과 현재 시간이 다르면 '현재 시간으로 설정' 체크박스 해제
                    if (createdDateTime.toLocalTime().withSecond(0).withNano(0) != LocalTime.now().withSecond(0).withNano(0))
                        dietLogBinding.cbSetNow.isChecked = false
                    else
                        dietLogBinding.cbSetNow.isChecked = true
                } catch (e: Exception) {
                    Log.e("DietLogFragment", "Failed to get image creation time: ${e.message}")
                    // 시간 가져오기 실패 시 현재 시간으로 유지
                    dietLogBinding.cbSetNow.isChecked = true
                    selectedTime = LocalTime.now()
                    formattedTime = DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH).format(selectedTime)
                    dietLogBinding.tvTime12h.text = formattedTime
                }
                checkAllValid()
            }
        }

        dietLogBinding.imgDiet.setOnClickListener {
            SelectImgDialog(requireContext(),
                {
                    // 기본 이미지 설정 - 이 경우는 사용자가 직접 선택한 것이 아님
                    isUserSelectedImage = false
                    isDefaultImage = true

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
                        dietLogBinding.imgDiet.setImageURI(fileUri)
                        dietImg = fileUri.toString() // file:// URI로 저장
                        Log.d("DietLogFragment", "Default resource image converted and set: $dietImg")
                    } else {
                        // 변환 실패 시 기본 처리
                        val uri = Uri.parse("android.resource://${requireContext().packageName}/$defaultImageResId")
                        dietLogBinding.imgDiet.setImageURI(uri)
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
                dietLogBinding.imgDiet.setImageURI(fileUri)
                dietImg = fileUri.toString() // file:// URI로 저장
                Log.d("DietLogFragment", "Initial SVG image converted and set: $dietImg")
            } else {
                // 변환 실패 시 리소스 URI 사용
                val uri = Uri.parse("android.resource://${requireContext().packageName}/$defaultImageResId")
                dietLogBinding.imgDiet.setImageURI(uri)
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

    // 선택된 식단 데이터를 바탕으로 UI를 업데이트하는 함수
    private fun updateUIWithSelectedMeal(mealData: DietLogData) {
        this.mealData = mealData
        isEditMode = false
        dietId = -1

        dietLogBinding.etDietTitle.setText(mealData.dietTitle)
        dietTitle = mealData.dietTitle
        dietImg = mealData.dietImg
        dietCategory = mealData.dietCategory
        score = mealData.score

        isUserSelectedImage = true

        Glide.with(requireContext())
            .load(dietImg)
            .placeholder(R.drawable.img_blank)
            .error(R.drawable.img_blank)
            .into(dietLogBinding.imgDiet)

        selectedTime = mealData.time.toLocalTime()
        selectedHour = selectedTime.hour
        selectedMin = selectedTime.minute
        dietLogBinding.cbSetNow.isChecked = false
        dietLogBinding.tvTime12h.text = mealData.time.format(DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH))

        when (score) {
            1 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn1.id)
            2 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn2.id)
            3 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn3.id)
            4 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn4.id)
            5 -> dietLogBinding.rgSelectScore.check(dietLogBinding.btn5.id)
        }

        when (dietCategory) {
            getString(R.string.breakfast) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnBreakfast.id)
            getString(R.string.lunch) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnLunch.id)
            getString(R.string.dinner) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnDinner.id)
            getString(R.string.snack) -> dietLogBinding.rgSelectDiet.check(dietLogBinding.btnSnack.id)
        }

        // 기존 태그 초기화 및 새 태그 추가
        ingredientTags.clear()
        ingredientTags.addAll(mealData.ingredientTags)
        dietLogBinding.cgTagsIngredient.removeAllViews()

        // 식재료 데이터 로드 후 칩 표시
        dietLogViewModel.loadIngredients()
        dietLogViewModel.ingredientList.observe(viewLifecycleOwner) { ingredientList ->
            val ingredientMap = ingredientList.associateBy { it.id }
            showInitialIngredientChips(ingredientTags = ingredientTags, ingredientMap = ingredientMap,
                chipGroup = dietLogBinding.cgTagsIngredient, findIngredientChip = dietLogBinding.chipFindIngredient)
        }

        checkAllValid()
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