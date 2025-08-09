package com.example.elixir.recipe.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeLogBinding
import com.example.elixir.dialog.SaveDialog
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.ui.IngredientSearchFragment
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.data.FlavoringItem
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.repository.RecipeRepository
import com.example.elixir.recipe.data.RecipeStepData
import com.example.elixir.recipe.data.toEntity
import com.example.elixir.recipe.ui.adapter.FlavoringLogAdapter
import com.example.elixir.recipe.ui.adapter.RecipeStepLogAdapter
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.android.material.chip.Chip
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileOutputStream

class RecipeLogFragment : Fragment() {
    private var recipeLogBinding: FragmentRecipeLogBinding? = null
    private val recipeBinding get() = recipeLogBinding!!

    // 수정 모드
    private var isEdit = false

    // 레시피 데이터 변수
    private var recipeId = -1
    private var recipeTitle = ""
    private var thumbnail = ""
    private var recipeDescription = ""
    private var categorySlowAging = ""
    private var categoryType = ""
    private var difficulty = ""
    private var ingredientTags = mutableListOf<Int>()
    private var allergies = mutableListOf<String>()
    private var ingredientList = mutableListOf<FlavoringItem>()
    private var seasoningList = mutableListOf<FlavoringItem>()
    private var steps = mutableListOf<RecipeStepData>()
    private var tips = ""
    private var timeHours = 0
    private var timeMinutes = 0

    private var authorFollowByCurrentUser = false
    private var likedByCurrentUser = false
    private var scrappedByCurrentUser = false
    private var likes = 0
    private var createdAt = LocalDateTime.now().toString()

    // 어댑터
    private lateinit var ingredientsAdapter: FlavoringLogAdapter
    private lateinit var seasoningAdapter: FlavoringLogAdapter
    private lateinit var stepAdapter: RecipeStepLogAdapter

    private lateinit var chipMap: Map<Int, Int>
    private var selectedPosition: Int = -1
    private lateinit var recipeRepository: RecipeRepository
    private var isDataSet = false // 데이터 설정 완료 여부

    private lateinit var pickThumbnailLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            val file = File(requireContext().filesDir, "step_image_${System.currentTimeMillis()}_$selectedPosition.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            steps[selectedPosition].stepImg = file.absolutePath
            stepAdapter.notifyItemChanged(selectedPosition)
        } ?: Toast.makeText(requireContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
    }

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("RecipeFragment", "onCreateView 호출")
        recipeLogBinding = FragmentRecipeLogBinding.inflate(inflater, container, false)
        return recipeBinding.root
    }

    private var isBindingData = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("RecipeLogFragment", "onViewCreated called")
        Log.d("RecipeLogFragment", "recipeThumbnail: ${recipeBinding.recipeThumbnail}")
        chipMap = emptyMap()

        try {
            recipeRepository = RecipeRepository(RetrofitClient.instanceRecipeApi, AppDatabase.getInstance(requireContext()).recipeDao())

            // id 값 먼저 가져오기
            recipeId = arguments?.getInt("recipeId") ?: -1
            Log.d("RecipeLogFragment", "recipeID: $recipeId")

            // 수정 모드 판단
            isEdit = recipeId != -1

            if (!isEdit) {
                // 등록 모드: 데이터 초기화 후 UI 설정
                initData()
                setupUI()
            } else {
                // 수정 모드: UI 먼저 설정하고 데이터 관찰
                setupUI()

                // 레시피 데이터 관찰
                recipeViewModel.recipeDetail.observe(viewLifecycleOwner) { recipeData ->
                    Log.d("RecipeFragment", "recipeData: $recipeData")
                    if (recipeData != null && !isDataSet) {
                        isDataSet = true
                        setRecipeDataToUI(recipeData)
                    }
                }

                // 데이터 요청
                recipeViewModel.getRecipeById(recipeId)
            }

        } catch (e: Exception) {
            Log.e("RecipeLogFragment", "Error in onViewCreated", e)
        }
    }

    private fun setupUI() {
        Log.d("RecipeLogFragment", "Setting up UI elements")
        try {
            // 레시피 썸네일 설정
            setupThumbnail()

            // 스피너 설정
            setupSpinners()

            // 텍스트 입력 설정
            bindTextInputs()

            // 알러지 설정
            setupAllergyChips()

            // 난이도 설정
            setupDifficultyChips()

            // 리사이클러뷰 설정
            setupRecyclerViews()

            // 추가 버튼 설정
            setupAddButtons()

            // 작성 버튼 설정
            setupWriteButton()

            // 식재료 칩 설정
            setupIngredientChips()

            // 레시피 가이드 버튼 설정
            setupRecipeGuideButton()

            Log.d("RecipeLogFragment", "UI setup completed")
        } catch (e: Exception) {
            Log.e("RecipeLogFragment", "Error setting up UI", e)
        }
    }

    private fun setupRecipeInfo() {
        recipeViewModel.recipeDetail.observe(viewLifecycleOwner) { recipeData ->
            if (recipeData != null) {
                Log.d("RecipeLogFragment", "Received recipe data: ${recipeData.title}")
                recipeTitle = recipeData.title
                thumbnail = recipeData.imageUrl
                recipeDescription = recipeData.description
                categorySlowAging = recipeData.categorySlowAging
                categoryType = recipeData.categoryType
                difficulty = recipeData.difficulty
                ingredientTags = recipeData.ingredientTagIds!!.toMutableList()
                allergies = recipeData.allergies!!.toMutableList()
                ingredientList = recipeData.ingredients!!.map { (name, value, unit) -> FlavoringItem(name = name, value = value, unit = unit)}.toMutableList()
                seasoningList = recipeData.seasonings!!.map { (name, value, unit) -> FlavoringItem(name = name, value = value, unit = unit)}.toMutableList()
                steps = recipeData.stepImageUrls.zip(recipeData.stepDescriptions) { img, desc -> RecipeStepData(stepImg = img, stepDescription = desc)}.toMutableList()
                tips = recipeData.tips!!
                timeHours = recipeData.timeHours
                timeMinutes = recipeData.timeMinutes
            }
        }
    }

    // 썸네일 이미지 세팅
    private fun setupThumbnail() {
        Log.d("RecipeLogFragment", "setupThumbnail called")
        Log.d("RecipeLogFragment", "recipeThumbnail: ${recipeBinding.recipeThumbnail}")
        // 처음엔 기본 이미지로
        setImgview(thumbnail)

        // 레시피 썸네일 사진 업로드
        pickThumbnailLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                val file = File(requireContext().filesDir, "picked_image_${System.currentTimeMillis()}.jpg")
                requireContext().contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                thumbnail = file.absolutePath
                setImgview(thumbnail)
            }
        }

        // 레시피 썸네일 선택방식 다이얼로그
        recipeBinding.recipeThumbnail.setOnClickListener {
            showThumbnailDialog()
        }
    }

    // 이미지 세팅
    private fun setImgview(path: String){
        Glide.with(requireContext())
            .load(path)
            .placeholder(R.drawable.img_blank)
            .error(R.drawable.img_blank)
            .into(recipeBinding.recipeThumbnail)
    }

    private fun setupSpinners() {
        setupSpinner(recipeBinding.selectLowAging, R.array.method_list) { categorySlowAging = if (it != "저속노화") it else "" }
        setupSpinner(recipeBinding.selectType, R.array.type_list) { categoryType = if (it != "종류") it else "" }
        setupSpinner(recipeBinding.selectHour, R.array.cookingHours) {
            timeHours = when (it) { "시" -> 0; "12시간 이상" -> 12; else -> it.toIntOrNull() ?: 0 }
        }
        setupSpinner(recipeBinding.selectMin, R.array.cookingMinutes) {
            timeMinutes = if (it == "분") 0 else it.toIntOrNull() ?: 0
        }
    }

    private fun setupRecipeGuideButton() {
        recipeBinding.btnRecipeGuide.setOnClickListener {
            Log.d("RecipeLogFragment", "Recipe guide button clicked")
            val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                putExtra("mode", 16)  // toolbar mode 16
            }
            startActivity(intent)
        }
    }

    // 식재료 설정
    private fun setupIngredientChips() {
        Log.d("RecipeLogFragment", "Setting up ingredient chips")

        try {
            // 식재료 뷰모델 호출 및 데이터 가져오기
            val ingredientRepository = IngredientRepository(RetrofitClient.instanceIngredientApi,
                IngredientDB.getInstance(requireContext()).ingredientDao())
            val ingredientViewModel = IngredientViewModel(ingredientRepository)

            ingredientViewModel.loadIngredients()

            // 한 번만 관찰하도록 수정
            ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredientList ->
                if (ingredientList.isNotEmpty()) {
                    val ingredientMap = ingredientList.associateBy { it.id }
                    Log.d("RecipeLogFragment", "ingredientTags: $ingredientTags, ingredientMap: $ingredientMap")

                    // 수정 모드에서 기존 칩이 있다면 먼저 업데이트
                    if (isEdit && ingredientTags.isNotEmpty()) {
                        updateIngredientChips(ingredientTags, ingredientMap)
                    }
                }
            }

            // 식재료 검색 버튼 클릭 리스너 (기존과 동일)
            recipeBinding.chipFindIngredient.setOnClickListener {
                Log.d("RecipeLogFragment", "Find ingredient button clicked")
                recipeBinding.chipFindIngredient.isChecked = !recipeBinding.chipFindIngredient.isChecked

                val ingredientSearchFragment = IngredientSearchFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, ingredientSearchFragment)
                    .addToBackStack(null)
                    .commit()
            }

            // Fragment가 다시 보일 때 검색 칩 상태 초기화
            viewLifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
                override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                    recipeBinding.chipFindIngredient.isChecked = false
                }
            })

            // SearchFragment에서 전달된 결과 수신
            parentFragmentManager.setFragmentResultListener("ingredient_selection", viewLifecycleOwner) { _, bundle ->
                Log.d("RecipeLogFragment", "Received ingredient selection result")
                handleIngredientSelection(bundle)
            }

            updateWriteButtonState()
            Log.d("RecipeLogFragment", "Ingredient chips setup completed")
        } catch (e: Exception) {
            Log.e("RecipeLogFragment", "Error setting up ingredient chips", e)
        }
    }

    private fun updateIngredientChips(ingredientTags: List<Int>, ingredientMap: Map<Int, IngredientData>) {
        // 기존 칩들 제거 (findIngredient 칩 제외)
        val findIngredientChip = recipeBinding.chipFindIngredient
        val chipsToRemove = mutableListOf<View>()

        for (i in 0 until recipeBinding.cgTagsIngredient.childCount) {
            val child = recipeBinding.cgTagsIngredient.getChildAt(i)
            if (child != findIngredientChip && child is Chip) {
                chipsToRemove.add(child)
            }
        }

        chipsToRemove.forEach { recipeBinding.cgTagsIngredient.removeView(it) }

        // 새로운 칩들 추가
        ingredientTags.forEach { ingredientId ->
            val ingredientName = ingredientMap[ingredientId]?.name ?: "알 수 없음"
            val chip = Chip(ContextThemeWrapper(requireContext(), R.style.ChipStyle_Short)).apply {
                text = ingredientName
                isClickable = true
                isCheckable = false
                chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.elixir_orange)
                )
                setTextColor(ContextCompat.getColor(context, R.color.white))

                setOnClickListener {
                    Log.d("RecipeLogFragment", "Chip clicked for removal: $ingredientName")
                    this@RecipeLogFragment.ingredientTags.remove(ingredientId)
                    recipeBinding.cgTagsIngredient.removeView(this)
                    updateWriteButtonState()
                }
            }

            // findIngredient Chip 앞에 삽입
            val index = recipeBinding.cgTagsIngredient.indexOfChild(findIngredientChip)
            recipeBinding.cgTagsIngredient.addView(chip, index)
        }
    }


    private fun handleIngredientSelection(bundle: Bundle) {
        try {
            val ingredientId = bundle.getInt("ingredientId", -1)
            val ingredientName = bundle.getString("ingredientName") ?: return

            Log.d("RecipeLogFragment", "Handling ingredient selection - ID: $ingredientId, Name: $ingredientName")

            if (ingredientId == -1) return
            val findIngredientChip = recipeBinding.chipFindIngredient

            // 중복 방지
            if (ingredientTags.contains(ingredientId)) {
                Toast.makeText(requireContext(), "이미 추가된 재료입니다.", Toast.LENGTH_SHORT).show()
                return
            }

            // 일반 태그 개수 제한
            if (ingredientTags.size >= 5) {
                Toast.makeText(requireContext(), "일반 재료는 최대 5개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
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
                    Log.d("RecipeLogFragment", "Chip clicked for removal: $ingredientName")
                    ingredientTags.remove(ingredientId)
                    recipeBinding.cgTagsIngredient.removeView(this)
                    updateWriteButtonState()
                }
            }

            // findIngredient Chip 앞에 삽입
            val index = recipeBinding.cgTagsIngredient.indexOfChild(findIngredientChip)
            recipeBinding.cgTagsIngredient.addView(chip, index)

            // 리스트에 추가 (ID 저장)
            ingredientTags.add(ingredientId)
            updateWriteButtonState()

            Log.d("RecipeLogFragment", "Ingredient chip added successfully")
        } catch (e: Exception) {
            Log.e("RecipeLogFragment", "Error handling ingredient selection", e)
        }
    }

    // 데이터 초기화
    private fun initData() {
        ingredientList.clear()
        ingredientList.add(FlavoringItem("", "", ""))

        seasoningList.clear()
        seasoningList.add(FlavoringItem("", "", ""))

        steps.clear()
        steps.add(RecipeStepData("android.resource://${requireContext().packageName}/${R.drawable.img_blank}", ""))
    }

    // 텍스트 설정
    private fun bindTextInputs() {
        recipeBinding.enterRecipeTitle.post { recipeBinding.enterRecipeTitle.setText(recipeTitle) }
        recipeBinding.enterRecipeDescription.post { recipeBinding.enterRecipeDescription.setText(recipeDescription) }
        recipeBinding.enterTipCaution.post { recipeBinding.enterTipCaution.setText(tips) }

        recipeBinding.enterRecipeTitle.addTextChangedListener(simpleTextWatcher {
            recipeTitle = it
            updateWriteButtonState()
        })
        recipeBinding.enterRecipeDescription.addTextChangedListener(simpleTextWatcher {
            recipeDescription = it
            updateWriteButtonState()
        })
        recipeBinding.enterTipCaution.addTextChangedListener(simpleTextWatcher {
            tips = it
            updateWriteButtonState()
        })
    }

    // 알러지 설정
    private fun setupAllergyChips() {
        val allergyList = listOf(
            recipeBinding.allergyEgg, recipeBinding.allergyMilk, recipeBinding.allergyBuckwheat, recipeBinding.allergyPeanut,
            recipeBinding.allergySoybean, recipeBinding.allergyWheat, recipeBinding.allergyMackerel, recipeBinding.allergyCrab, recipeBinding.allergyShrimp,
            recipeBinding.allergyPig, recipeBinding.allergyPeach, recipeBinding.allergyTomato, recipeBinding.allergyDioxide, recipeBinding.allergyWalnut,
            recipeBinding.allergyChicken, recipeBinding.allergyCow, recipeBinding.allergySquid, recipeBinding.allergySeashell, recipeBinding.allergyOyster,
            recipeBinding.allergyPinenut
        )
        val chipNone = recipeBinding.nA
        allergyList.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (allergies.size >= 5) {
                        chip.isChecked = false
                        Toast.makeText(requireContext(), "알레르기는 최대 5개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                        return@setOnCheckedChangeListener
                    }
                    chipNone.isChecked = false
                    allergies.remove(chipNone.text.toString())
                    if (!allergies.contains(chip.text.toString())) allergies.add(chip.text.toString())
                } else allergies.remove(chip.text.toString())
            }
        }
        chipNone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                allergyList.forEach { it.isChecked = false }
                allergies.clear()
                allergies.add(chipNone.text.toString())
            } else allergies.remove(chipNone.text.toString())
        }
        updateWriteButtonState()
    }

    // 난이도 설정
    private fun setupDifficultyChips() {
        val difficultyList = listOf(recipeBinding.levelEasy, recipeBinding.levelNormal, recipeBinding.levelHard)
        difficultyList.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    difficultyList.filter { it != chip }.forEach { it.isChecked = false }
                    difficulty = chip.text.toString()
                } else if (difficulty == chip.text.toString()) difficulty = ""
            }
        }
        updateWriteButtonState()
    }

    // RecyclerView 설정
    private fun setupRecyclerViews() {
        // 어댑터 초기화 (한 번만)
        if (!::ingredientsAdapter.isInitialized) {
            ingredientsAdapter = FlavoringLogAdapter(ingredientList,
                { removeFlavoringItem(ingredientList, it, ingredientsAdapter, recipeBinding.frameEnterIngredients) },
                { updateAddButtonState() }
            )
        }

        if (!::seasoningAdapter.isInitialized) {
            seasoningAdapter = FlavoringLogAdapter(seasoningList,
                { removeFlavoringItem(seasoningList, it, seasoningAdapter, recipeBinding.frameEnterSeasoning) },
                { updateAddButtonState() }
            )
        }

        if (!::stepAdapter.isInitialized) {
            stepAdapter = RecipeStepLogAdapter(steps,
                { removeStepItem(it) },
                { showSelectImgDialog(it) },
                { updateAddButtonState() }
            )
        }

        // RecyclerView 설정
        view?.post {
            try {
                // 어댑터가 아직 설정되지 않았을 때만 설정
                if (recipeBinding.frameEnterIngredients.adapter == null) {
                    setupRecyclerView(recipeBinding.frameEnterIngredients, ingredientsAdapter)
                }
                if (recipeBinding.frameEnterSeasoning.adapter == null) {
                    setupRecyclerView(recipeBinding.frameEnterSeasoning, seasoningAdapter)
                }
                if (recipeBinding.frameEnterRecipeStep.adapter == null) {
                    setupRecyclerView(recipeBinding.frameEnterRecipeStep, stepAdapter)
                }

                updateAddButtonState()
                updateWriteButtonState()
            } catch (e: Exception) {
                Log.e("RecipeLogFragment", "Error setting up RecyclerViews", e)
            }
        }
    }

    // 추가 버튼 설정
    private fun setupAddButtons() {
        recipeBinding.btnIngredientsAdd.setOnClickListener {
            ingredientList.add(FlavoringItem("", "", ""))

            // 안전한 어댑터 갱신
            view?.post {
                try {
                    ingredientsAdapter.notifyItemInserted(ingredientList.size - 1)
                    recipeBinding.frameEnterIngredients.requestLayout()
                    updateAddButtonState()
                } catch (e: Exception) {
                    Log.e("RecipeLogFragment", "Error adding ingredient", e)
                    // 실패 시 전체 갱신
                    ingredientsAdapter.notifyDataSetChanged()
                }
            }
        }

        recipeBinding.btnSeasoningAdd.setOnClickListener {
            seasoningList.add(FlavoringItem("", "", ""))

            view?.post {
                try {
                    seasoningAdapter.notifyItemInserted(seasoningList.size - 1)
                    recipeBinding.frameEnterSeasoning.requestLayout()
                    updateAddButtonState()
                } catch (e: Exception) {
                    Log.e("RecipeLogFragment", "Error adding seasoning", e)
                    seasoningAdapter.notifyDataSetChanged()
                }
            }
        }

        recipeBinding.btnRecipeStepAdd.setOnClickListener {
            steps.add(RecipeStepData("android.resource://${requireContext().packageName}/${R.drawable.img_blank}", ""))

            view?.post {
                try {
                    stepAdapter.notifyItemInserted(steps.size - 1)
                    recipeBinding.frameEnterRecipeStep.requestLayout()
                    updateAddButtonState()
                } catch (e: Exception) {
                    Log.e("RecipeLogFragment", "Error adding step", e)
                    stepAdapter.notifyDataSetChanged()
                }
            }
        }

        updateWriteButtonState()
    }

    // 작성 버튼 설정
    private fun setupWriteButton() {
        recipeBinding.btnWriteRecipe.setOnClickListener {
            if (isAllFieldsValid()) {
                SaveDialog(requireActivity()) {
                    val recipeData = RecipeData(
                        id = if(recipeId == -1) 0 else recipeId,
                        title = recipeTitle,
                        description = recipeDescription,
                        categorySlowAging = categorySlowAging,
                        categoryType = categoryType,
                        difficulty = difficulty,
                        timeHours = timeHours,
                        timeMinutes = timeMinutes,
                        ingredientTagIds = ingredientTags,
                        ingredients = ingredientList,
                        seasonings = seasoningList,
                        stepDescriptions = steps.map { it.stepDescription },
                        stepImageUrls = steps.map { it.stepImg },
                        tips = tips,
                        allergies = allergies,
                        imageUrl = thumbnail,
                        authorFollowByCurrentUser = authorFollowByCurrentUser,
                        likedByCurrentUser = likedByCurrentUser,
                        scrappedByCurrentUser = scrappedByCurrentUser,
                        authorId = 0,
                        likes = likes,
                        createdAt = createdAt,
                        updatedAt = LocalDateTime.now().toString()
                    )

                    Log.d("RecipeLogFragment", recipeData.toString())

                    val recipeEntity = recipeData.toEntity()
                    val thumbnailFile = parseImagePathToFile(requireContext(), thumbnail)
                    val stepImageFiles = parseStepImagesToFiles(requireContext(), recipeData.stepImageUrls)

                    Log.d("RecipeLogFragment", "레시피 ID: $recipeId")
                    Log.d("RecipeLogFragment", "레시피 수정모드: $isEdit")

                    // 수정 모드 -> PATCH 실행
                    if(isEdit) {
                        recipeViewModel.updateRecipe(recipeId, recipeEntity, thumbnailFile, stepImageFiles)
                    }
                    // 등록 모드 -> POST 실행
                    else {
                        recipeViewModel.uploadRecipe(recipeEntity, thumbnailFile, stepImageFiles)
                    }

                    // 값 넣기(모드(등록/수정), 레시피 아이디)
                    val intent = Intent().apply {
                        putExtra("mode", if (arguments?.getBoolean("isEdit") == true) 9 else 0)
                        putExtra("recipeId", recipeId)
                    }

                    // 레시피 등록 관찰
                    recipeViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
                        result.onSuccess {
                            Toast.makeText(requireContext(), "레시피가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            // 성공 메세지와 함께 값을 넘기고 화면 종료
                            requireActivity().setResult(Activity.RESULT_OK, intent)
                            requireActivity().finish()
                        }
                        result.onFailure { e ->
                            Log.e("RecipeUpload", "업로드 실패", e)
                            Toast.makeText(requireContext(), "레시피를 등록하지 못했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // 레시피 수정 관찰
                    recipeViewModel.updateResult.observe(viewLifecycleOwner) { result ->
                        result.onSuccess {
                            Toast.makeText(requireContext(), "레시피가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                            // 성공 메세지와 함께 값을 넘기고 화면 종료
                            requireActivity().setResult(Activity.RESULT_OK, intent)
                            requireActivity().finish()
                        }
                        result.onFailure { e ->
                            Log.e("RecipeUpload", "수정 실패", e)
                            Toast.makeText(requireContext(), "레시피를 수정하지 못하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.show()
            }
        }
    }

    // 스피너 설정
    private fun setupSpinner(spinner: android.widget.Spinner, arrayRes: Int, onSelected: (String) -> Unit) {
        val options = resources.getStringArray(arrayRes)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                onSelected(parent.getItemAtPosition(position).toString())
                updateWriteButtonState()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // TextWatcher 생성
    private fun simpleTextWatcher(afterChanged: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            afterChanged(s.toString().trim())
            updateWriteButtonState()
        }
    }

    // RecyclerView 설정
    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
        }
    }


    // 식재료 없애기
    private fun removeFlavoringItem(
        list: MutableList<FlavoringItem>,
        position: Int,
        adapter: FlavoringLogAdapter,
        recyclerView: ViewGroup
    ) {
        // isBindingData가 true일 때는 데이터를 바인딩 중이므로 삭제 로직을 실행하지 않음
        if (isBindingData) {
            Log.d("RemoveItem", "Data binding in progress, skipping removeFlavoringItem.")
            return
        }

        // 유효한 인덱스인지 확인
        if (position < 0 || position >= list.size) {
            Log.e("RemoveItem", "Invalid position for removeFlavoringItem: $position, list size: ${list.size}")
            return
        }

        // 최소 1개 항목은 유지 (빈 항목이라도)
        if (list.size <= 1) {
            // 마지막 항목을 빈 항목으로 초기화
            list[0] = FlavoringItem("", "", "")
            adapter.notifyItemChanged(0)
            Log.d("RemoveItem", "Reset last item to empty instead of removing")
            updateAddButtonState()
            return
        }

        // 항목 삭제
        list.removeAt(position)

        // RecyclerView 상태를 안전하게 갱신
        recyclerView.post {
            try {
                // 전체 데이터셋 갱신으로 변경 (더 안전함)
                adapter.notifyDataSetChanged()
                recyclerView.requestLayout()
            } catch (e: Exception) {
                Log.e("RemoveItem", "Error updating RecyclerView", e)
            }
        }

        updateAddButtonState()
        updateWriteButtonState()
    }

    // 2. removeStepItem 함수 수정
    private fun removeStepItem(position: Int) {
        // isBindingData가 true일 때는 데이터를 바인딩 중이므로 삭제 로직을 실행하지 않음
        if (isBindingData) {
            Log.d("RemoveItem", "Data binding in progress, skipping removeStepItem.")
            return
        }

        // 유효한 인덱스인지 확인
        if (position < 0 || position >= steps.size) {
            Log.e("RemoveItem", "Invalid position for removeStepItem: $position, list size: ${steps.size}")
            return
        }

        // 최소 1개 항목은 유지 (빈 항목이라도)
        if (steps.size <= 1) {
            // 마지막 항목을 빈 항목으로 초기화
            steps[0] = RecipeStepData("android.resource://${requireContext().packageName}/${R.drawable.img_blank}", "")
            stepAdapter.notifyItemChanged(0)
            Log.d("RemoveItem", "Reset last step to empty instead of removing")
            updateAddButtonState()
            return
        }

        // 항목 삭제
        steps.removeAt(position)

        // RecyclerView 상태를 안전하게 갱신
        recipeBinding.frameEnterRecipeStep.post {
            try {
                // 전체 데이터셋 갱신으로 변경 (더 안전함)
                stepAdapter.notifyDataSetChanged()
                recipeBinding.frameEnterRecipeStep.requestLayout()
            } catch (e: Exception) {
                Log.e("RemoveItem", "Error updating RecyclerView", e)
            }
        }

        updateAddButtonState()
        updateWriteButtonState()
    }



    private fun showSelectImgDialog(position: Int) {
        if (!isAdded) {
            Toast.makeText(activity, "Fragment가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        // 이미지 선택 메뉴
        SelectImgDialog(requireContext(),
            // 기본 이미지 선택
            {
                steps[position].stepImg = "android.resource://${requireContext().packageName}/${R.drawable.img_blank}"
                stepAdapter.notifyItemChanged(position)
            },
            // 갤러리 선택
            {
                selectedPosition = position
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        ).show()
    }

    private fun showThumbnailDialog() {
        SelectImgDialog(requireContext(),
            // 기본 이미지
            {
                thumbnail = "android.resource://${requireContext().packageName}/${R.drawable.img_blank}"
                setImgview(thumbnail)
            },
            // 갤러리 선택
            {
                pickThumbnailLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        ).show()
    }

    private fun updateAddButtonState() {
        // 어댑터가 초기화되지 않았다면 기본값으로 처리
        val isIngredientsValid = if (ingredientList.isEmpty()) false
        else ingredientList.all { it.name.isNotBlank() && it.unit.isNotBlank() }
        val isSeasoningValid = if (seasoningList.isEmpty()) false
        else seasoningList.all { it.name.isNotBlank() && it.unit.isNotBlank() }
        val isStepsValid = if (steps.isEmpty()) false
        else steps.all { it.stepDescription.isNotBlank() }

        setAddButtonState(recipeBinding.btnIngredientsAdd, isIngredientsValid)
        setAddButtonState(recipeBinding.btnSeasoningAdd, isSeasoningValid)
        setAddButtonState(recipeBinding.btnRecipeStepAdd, isStepsValid)
    }

    private fun setAddButtonState(button: View, isEnabled: Boolean) {
        button.isEnabled = isEnabled
        button.backgroundTintList = resources.getColorStateList(
            if (isEnabled) R.color.elixir_orange else R.color.elixir_gray, null
        )
    }

    // 모든 필드가 유효한지 확인하는 함수
    // isAllFieldsValid 함수 분리
    private fun isAllFieldsValid(): Boolean {
        return recipeTitle.isNotBlank() &&
                thumbnail.isNotBlank() &&
                recipeDescription.isNotBlank() &&
                categorySlowAging.isNotBlank() &&
                categoryType.isNotBlank() &&
                difficulty.isNotBlank() &&
                ingredientList.isNotEmpty() &&
                seasoningList.isNotEmpty() &&
                steps.isNotEmpty() &&
                tips.isNotBlank()
    }

    // 작성 버튼 활성화 상태 갱신
    private fun updateWriteButtonState() {
        val isValid = isAllFieldsValid()

        Log.d("Recipe", "isAllFieldsValid: $isValid")
        Log.d("Recipe", "title=$recipeTitle, thumbnail=$thumbnail, " +
                "desc=$recipeDescription, slowAging=$categorySlowAging, type=$categoryType, " +
                "diff=$difficulty, ingredients=${ingredientList.size}, seasoning=${seasoningList.size}, " +
                "steps=${steps.size}, tips=$tips")
        setButtonState(recipeBinding.btnWriteRecipe, isValid)
    }

    // 버튼 활성화/비활성화 및 색상 처리
    private fun setButtonState(button: View, isEnabled: Boolean) {
        button.isEnabled = isEnabled
        button.backgroundTintList = resources.getColorStateList(
            if (isEnabled) R.color.elixir_orange else R.color.elixir_gray, null
        )
    }

    private fun setRecipeDataToUI(recipeData: RecipeData) {
        Log.d("RecipeLogFragment", "Setting recipe data to UI")

        // 기본 데이터 설정
        recipeTitle = recipeData.title
        thumbnail = recipeData.imageUrl
        recipeDescription = recipeData.description
        categorySlowAging = recipeData.categorySlowAging
        categoryType = recipeData.categoryType
        difficulty = recipeData.difficulty
        ingredientTags = recipeData.ingredientTagIds!!.toMutableList()
        allergies = recipeData.allergies!!.toMutableList()
        tips = recipeData.tips!!
        timeHours = recipeData.timeHours
        timeMinutes = recipeData.timeMinutes
        allergies = recipeData.allergies?.toMutableList() ?: mutableListOf()

        // 기타 데이터
        authorFollowByCurrentUser = recipeData.authorFollowByCurrentUser
        likedByCurrentUser = recipeData.likedByCurrentUser
        scrappedByCurrentUser = recipeData.scrappedByCurrentUser
        likes = recipeData.likes
        createdAt = recipeData.createdAt

        // 데이터 바인딩 시작
        isBindingData = true

        // UI 업데이트를 post로 지연 실행하여 안전하게 처리
        view?.post {
            try {
                // 어댑터가 초기화되었는지 확인
                if (!::ingredientsAdapter.isInitialized ||
                    !::seasoningAdapter.isInitialized ||
                    !::stepAdapter.isInitialized) {
                    Log.d("RecipeLogFragment", "Adapters not initialized, re-setting up RecyclerViews")
                    setupRecyclerViews()
                }

                // 텍스트 필드 업데이트
                recipeBinding.enterRecipeTitle.setText(recipeTitle)
                recipeBinding.enterRecipeDescription.setText(recipeDescription)
                recipeBinding.enterTipCaution.setText(tips)

                // 썸네일 이미지 업데이트
                setImgview(thumbnail)

                // 스피너 업데이트
                setSpinnerSelection(recipeBinding.selectLowAging, categorySlowAging)
                setSpinnerSelection(recipeBinding.selectType, categoryType)
                setSpinnerSelection(recipeBinding.selectHour, if (timeHours == 0) "시" else if (timeHours >= 12) "12시간 이상" else timeHours.toString())
                setSpinnerSelection(recipeBinding.selectMin, if (timeMinutes == 0) "분" else timeMinutes.toString())

        /*// 식재료 태그 칩
        Log.d("RecipeLogFragment", ingredientTags.toString())
        ingredientTags.clear()
        ingredientTags.addAll(recipeData.ingredientTagIds!!.toMutableList())
        setIngredientChips(ingredientTags)*/
                // 알러지 칩 설정
                setAllergyChipsFromData(allergies)

                // 난이도 칩 설정
                setDifficultyChipFromData(difficulty)

                // 리스트 데이터 업데이트
                updateListData(recipeData)

                // 어댑터 갱신
                updateAdapters()

                Log.d("RecipeLogFragment", "Recipe data UI update completed")
            } catch (e: Exception) {
                Log.e("RecipeLogFragment", "Error updating UI", e)
            } finally {
                // 데이터 바인딩 종료
                isBindingData = false
            }
        }
    }

    private fun updateListData(recipeData: RecipeData) {
        // 식재료 리스트 업데이트
        ingredientList.clear()
        ingredientList.addAll(recipeData.ingredients!!.map { (name, value, unit) ->
            FlavoringItem(name = name, value = value, unit = unit)
        })
        if (ingredientList.isEmpty()) {
            ingredientList.add(FlavoringItem("", "", ""))
        }

        // 조미료 리스트 업데이트
        seasoningList.clear()
        seasoningList.addAll(recipeData.seasonings!!.map { (name, value, unit) ->
            FlavoringItem(name = name, value = value, unit = unit)
        })
        if (seasoningList.isEmpty()) {
            seasoningList.add(FlavoringItem("", "", ""))
        }

        // 단계 리스트 업데이트
        steps.clear()
        steps.addAll(recipeData.stepImageUrls.zip(recipeData.stepDescriptions) { img, desc ->
            RecipeStepData(stepImg = img, stepDescription = desc)
        })
        if (steps.isEmpty()) {
            steps.add(RecipeStepData("android.resource://${requireContext().packageName}/${R.drawable.img_blank}", ""))
        }
    }

    private fun updateAdapters() {
        try {
            if (::ingredientsAdapter.isInitialized) {
                ingredientsAdapter.notifyDataSetChanged()
            }
            if (::seasoningAdapter.isInitialized) {
                seasoningAdapter.notifyDataSetChanged()
            }
            if (::stepAdapter.isInitialized) {
                stepAdapter.notifyDataSetChanged()
            }

            // 레이아웃 갱신 요청
            recipeBinding.frameEnterIngredients.requestLayout()
            recipeBinding.frameEnterSeasoning.requestLayout()
            recipeBinding.frameEnterRecipeStep.requestLayout()

            // 버튼 상태 업데이트
            updateAddButtonState()
            updateWriteButtonState()
        } catch (e: Exception) {
            Log.e("RecipeLogFragment", "Error updating adapters", e)
        }
    }

    private fun setAllergyChipsFromData(allergiesData: List<String>) {
        val allergyList = listOf(
            recipeBinding.allergyEgg, recipeBinding.allergyMilk, recipeBinding.allergyBuckwheat,
            recipeBinding.allergyPeanut, recipeBinding.allergySoybean, recipeBinding.allergyWheat,
            recipeBinding.allergyMackerel, recipeBinding.allergyCrab, recipeBinding.allergyShrimp,
            recipeBinding.allergyPig, recipeBinding.allergyPeach, recipeBinding.allergyTomato,
            recipeBinding.allergyDioxide, recipeBinding.allergyWalnut, recipeBinding.allergyChicken,
            recipeBinding.allergyCow, recipeBinding.allergySquid, recipeBinding.allergySeashell,
            recipeBinding.allergyOyster, recipeBinding.allergyPinenut
        )
        val chipNone = recipeBinding.nA

        // 모든 칩 초기화
        allergyList.forEach { it.isChecked = false }
        chipNone.isChecked = false

        // 데이터에 따라 칩 설정
        if (allergiesData.isEmpty() || allergiesData.contains("해당없음") || allergiesData.contains("N/A")) {
            chipNone.isChecked = true
        } else {
            allergyList.forEach { chip ->
                if (allergiesData.contains(chip.text.toString())) {
                    chip.isChecked = true
                }
            }
        }
    }



    // 스피너 카테고리 설정
    private fun setSpinnerSelection(spinner: android.widget.Spinner, value: String?) {
        value ?: return
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }

    // RecipeData의 difficulty 값을 받아서 난이도 칩 초기 선택 상태 설정
    private fun setDifficultyChipFromData(difficultyValue: String) {
        val difficultyList = listOf(recipeBinding.levelEasy, recipeBinding.levelNormal, recipeBinding.levelHard)
        difficultyList.forEach { chip ->
            chip.isChecked = (chip.text.toString() == difficultyValue)
        }
        updateWriteButtonState()
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

    // 1. 단일 이미지 경로를 File로 변환하는 함수
    private fun parseImagePathToFile(context: Context, imagePath: String): File? = when {
        imagePath.startsWith("http://") || imagePath.startsWith("https://") -> null
        imagePath.startsWith("android.resource://") -> {
            val resId = imagePath.substringAfterLast("/").toIntOrNull()
            if (resId != null) copyResourceToFile(context, resId) else null
        }
        imagePath.startsWith("content://") -> {
            val uri = Uri.parse(imagePath)
            val copiedUri = copyUriToInternal(context, uri)
            copiedUri?.let { File(it.path!!) }
        }
        imagePath.startsWith("file://") -> File(Uri.parse(imagePath).path!!)
        else -> File(imagePath)
    }

    // 2. 여러 이미지 경로를 File 리스트로 변환하는 함수
    private fun parseStepImagesToFiles(context: Context, stepImages: List<String>): List<File?> =
        stepImages.map { parseImagePathToFile(context, it) }


    override fun onDestroyView() {
        super.onDestroyView()
        recipeLogBinding = null
    }
}