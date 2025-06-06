package com.example.elixir.recipe.ui

import android.app.Activity
import android.app.AlertDialog
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
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.databinding.FragmentRecipeLogBinding
import com.example.elixir.dialog.SaveDialog
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.ui.IngredientSearchFragment
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.FlavoringData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.data.RecipeStepData
import com.example.elixir.recipe.data.toEntity
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileOutputStream

class RecipeLogFragment : Fragment() {
    private var recipeLogBinding: FragmentRecipeLogBinding? = null
    private val recipeBinding get() = recipeLogBinding!!

    // 데이터 변수
    private var recipeTitle = ""
    private var thumbnail = ""
    private lateinit var thumbnailUri: Uri
    private var recipeDescription = ""
    private var categorySlowAging = ""
    private var categoryType = ""
    private var difficulty = ""
    private var ingredientTags = mutableListOf<Int>()
    private var allergies = mutableListOf<String>()
    private var ingredientList = mutableListOf<FlavoringData>()
    private var seasoningList = mutableListOf<FlavoringData>()
    private var steps = mutableListOf<RecipeStepData>()
    private var tips = ""
    private var timeHours = 0
    private var timeMinutes = 0

    // 어댑터
    private lateinit var ingredientsAdapter: FlavoringLogAdapter
    private lateinit var seasoningAdapter: FlavoringLogAdapter
    private lateinit var stepAdapter: RecipeStepLogAdapter

    private lateinit var chipMap: Map<Int, Int>
    private var selectedPosition: Int = -1
    private lateinit var repository: RecipeRepository

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
        RecipeViewModelFactory(repository)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        recipeLogBinding = FragmentRecipeLogBinding.inflate(inflater, container, false)
        return recipeBinding.root
    }

    private var isBindingData = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = RecipeRepository(RetrofitClient.instanceRecipeApi, AppDatabase.getInstance(requireContext()).recipeDao())
        thumbnailUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")

        // 데이터 초기화
        initData()

        // 레시피 기본 정보 설정
        recipeViewModel.recipeDetail.observe(viewLifecycleOwner) { recipeData ->
            if (recipeData != null) {
                // 저장된 데이터 값을 넣기
                recipeTitle = recipeData.title
                thumbnail = recipeData.imageUrl
                recipeDescription = recipeData.description
                categorySlowAging = recipeData.categorySlowAging
                categoryType = recipeData.categoryType
                difficulty = recipeData.difficulty
                ingredientTags = recipeData.ingredientTagIds.toMutableList()
                allergies = recipeData.allergies.toMutableList()
                ingredientList = recipeData.ingredients.map { (key, value) -> FlavoringData(name = key, unit = value)}.toMutableList()
                seasoningList = recipeData.seasoning.map { (key, value) -> FlavoringData(name = key, unit = value)}.toMutableList()
                steps = recipeData.stepImageUrls.zip(recipeData.stepDescriptions) { img, desc -> RecipeStepData(stepImg = img, stepDescription = desc)}.toMutableList()
                tips = recipeData.tips
                timeHours = recipeData.timeHours
                timeMinutes = recipeData.timeMinutes
            }
        }

        // id 값 가져오기
        val recipeId = arguments?.getInt("recipeId") ?: return
        Log.d("RecipeLogFragment", "recipeID: ${recipeId}")
        recipeViewModel.getRecipeById(recipeId)

        Log.d("RECIPE", "Title: $recipeTitle, Description: $recipeDescription, Tips: $tips")

        // 레시피 썸네일 사진 업로드
        pickThumbnailLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                val file = File(requireContext().filesDir, "picked_image_${System.currentTimeMillis()}.jpg")
                requireContext().contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                recipeBinding.recipeThumbnail.setImageURI(Uri.fromFile(file))
                thumbnail = file.absolutePath
            }
        }

        // 레시피 썸네일 선택방식 다이얼로그
        recipeBinding.recipeThumbnail.setOnClickListener {
            showThumbnailDialog()
        }

        // 스피너 설정
        setupSpinner(recipeBinding.selectLowAging, R.array.method_list) { categorySlowAging = if (it != "저속노화") it else "" }
        setupSpinner(recipeBinding.selectType, R.array.type_list) { categoryType = if (it != "종류") it else "" }
        setupSpinner(recipeBinding.selectHour, R.array.cookingHours) {
            timeHours = when (it) { "시" -> 0; "12시간 이상" -> 12; else -> it.toIntOrNull() ?: 0 }
        }
        setupSpinner(recipeBinding.selectMin, R.array.cookingMinutes) {
            timeMinutes = if (it == "분") 0 else it.toIntOrNull() ?: 0
        }

        bindTextInputs()

        // 알러지 설정
        setupAllergyChips()

        // 난이도 설정
        setupDifficultyChips()

        // 리사이클러뷰 설정
        setupRecyclerViews()

        // 추가 버튼 설정
        setupAddButtons()

        // 작성 버튼
        setupWriteButton()

        // 수정 모드 진입 시
        arguments?.let {
            if (it.getBoolean("isEdit", false)) {
                Gson().fromJson(it.getString("recipeData"), RecipeData::class.java)?.let { data ->
                    setRecipeDataToUI(data)
                }
            }
            else {
                // UI 바인딩 및 리스너 설정
                bindTextInputs()
            }
        }


        // 식재료 검색 버튼 클릭 리스너
        recipeBinding.findIngredient.setOnClickListener {
            // 칩 상태 토글
            recipeBinding.findIngredient.isChecked = !recipeBinding.findIngredient.isChecked

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
                recipeBinding.findIngredient.isChecked = false
            }
        })

    }

    // 데이터 초기화
    private fun initData() {
        ingredientList.clear();
        ingredientList.add(FlavoringData("", ""))

        seasoningList.clear();
        seasoningList.add(FlavoringData("", ""))

        steps.clear();
        steps.add(RecipeStepData(thumbnailUri.toString(), ""))

        recipeBinding.recipeThumbnail.setImageURI(thumbnailUri)
        thumbnail = thumbnailUri.toString()
    }

    // 텍스트 설정
    private fun bindTextInputs() {
        recipeBinding.enterRecipeTitle.post { recipeBinding.enterRecipeTitle.setText(recipeTitle) }
        recipeBinding.enterRecipeDescription.post { recipeBinding.enterRecipeDescription.setText(recipeDescription) }
        recipeBinding.enterTipCaution.post { recipeBinding.enterTipCaution.setText(tips) }

        recipeBinding.enterRecipeTitle.addTextChangedListener(simpleTextWatcher {
            recipeTitle = it
        })
        recipeBinding.enterRecipeDescription.addTextChangedListener(simpleTextWatcher {
            recipeDescription = it
        })
        recipeBinding.enterTipCaution.addTextChangedListener(simpleTextWatcher {
            tips = it
        })
    }

    // 식재료 설정
    private fun setupIngredientChips() {
        // SearchFragment에서 전달된 결과 수신
        parentFragmentManager.setFragmentResultListener("ingredient_selection", viewLifecycleOwner) { _, bundle ->
            val ingredientId = bundle.getInt("ingredientId", -1)
            val ingredientName = bundle.getString("ingredientName") ?: return@setFragmentResultListener

            if (ingredientId == -1) return@setFragmentResultListener
            val findIngredientChip = recipeBinding.findIngredient

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
            val chip = com.google.android.material.chip.Chip(
                ContextThemeWrapper(requireContext(), R.style.ChipStyle_Short)
            ).apply {
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
                    recipeBinding.tagsIngredient.removeView(this)
                    updateWriteButtonState()
                }
            }

            // findIngredient Chip 앞에 삽입
            val index = recipeBinding.tagsIngredient.indexOfChild(findIngredientChip)
            recipeBinding.tagsIngredient.addView(chip, index)

            // 리스트에 추가 (ID 저장)
            ingredientTags.add(ingredientId)
            //checkAllValid()
        }

        updateWriteButtonState()
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
        ingredientsAdapter = FlavoringLogAdapter(ingredientList,
            { removeFlavoringItem(ingredientList, it, ingredientsAdapter, recipeBinding.frameEnterIngredients) },
            { updateAddButtonState() }
        )
        seasoningAdapter = FlavoringLogAdapter(seasoningList,
            { removeFlavoringItem(seasoningList, it, seasoningAdapter, recipeBinding.frameEnterSeasoning) },
            { updateAddButtonState() }
        )
        stepAdapter = RecipeStepLogAdapter(steps,
            { removeStepItem(it) }, { showSelectImgDialog(it) }, { updateAddButtonState() }
        )
        setupRecyclerView(recipeBinding.frameEnterIngredients, ingredientsAdapter)
        setupRecyclerView(recipeBinding.frameEnterSeasoning, seasoningAdapter)
        setupRecyclerView(recipeBinding.frameEnterRecipeStep, stepAdapter)
        updateWriteButtonState()
    }

    // 추가 버튼 설정
    private fun setupAddButtons() {
        recipeBinding.btnIngredientsAdd.setOnClickListener {
            ingredientList.add(FlavoringData("", ""))
            ingredientsAdapter.notifyItemInserted(ingredientList.size - 1)
            updateRecyclerViewHeight(recipeBinding.frameEnterIngredients, ingredientsAdapter)
        }
        recipeBinding.btnSeasoningAdd.setOnClickListener {
            seasoningList.add(FlavoringData("", ""))
            seasoningAdapter.notifyItemInserted(seasoningList.size - 1)
            updateRecyclerViewHeight(recipeBinding.frameEnterSeasoning, seasoningAdapter)
        }
        recipeBinding.btnRecipeStepAdd.setOnClickListener {
            steps.add(RecipeStepData("android.resource://${requireContext().packageName}/${R.drawable.img_blank}", ""))
            stepAdapter.notifyItemInserted(steps.size - 1)
            updateRecyclerViewHeight(recipeBinding.frameEnterRecipeStep, stepAdapter)
        }
        updateWriteButtonState()
    }

    // 작성 버튼 설정
    private fun setupWriteButton() {
        recipeBinding.btnWriteRecipe.setOnClickListener {
            if (isAllFieldsValid()) {
                SaveDialog(requireActivity()) {
                    val recipeData = RecipeData(
                        id = 0,
                        title = recipeTitle,
                        description = recipeDescription,
                        categorySlowAging = categorySlowAging,
                        categoryType = categoryType,
                        difficulty = difficulty,
                        timeHours = timeHours,
                        timeMinutes = timeMinutes,
                        ingredientTagIds = ingredientTags,
                        ingredients = ingredientList.associate { it.name to it.unit },
                        seasoning = seasoningList.associate { it.name to it.unit },
                        stepDescriptions = steps.map { it.stepDescription },
                        stepImageUrls = steps.map { it.stepImg },
                        tips = tips,
                        allergies = allergies,
                        imageUrl = thumbnail,
                        authorFollowByCurrentUser = false,
                        likedByCurrentUser = false,
                        scrappedByCurrentUser = false,
                        authorNickname = "작성자 닉네임",
                        authorTitle = "작성자 직책",
                        likes = 0,
                        scraps = 0,
                        createdAt = LocalDateTime.now().toString(),
                        updatedAt = LocalDateTime.now().toString()
                    )

                    val recipeEntity = recipeData.toEntity()

                    val thumbnailFile = parseImagePathToFile(requireContext(), thumbnail)
                    val stepImageFiles = parseStepImagesToFiles(requireContext(), recipeData.stepImageUrls)

                    recipeViewModel.uploadRecipe(recipeEntity, thumbnailFile, stepImageFiles)

                    val intent = Intent().apply {
                        putExtra("mode", if (arguments?.getBoolean("isEdit") == true) 9 else 0)
                        putExtra("recipeData", Gson().toJson(recipeData))
                    }
                    // 3. 업로드 결과 관찰 (observe)
                    recipeViewModel.uploadResult.observe(viewLifecycleOwner) { result ->
                        result.onSuccess { data ->
                            Toast.makeText(requireContext(), "레시피가 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                            // 업로드 성공 시 결과 처리 (예: 화면 종료)
                            requireActivity().setResult(Activity.RESULT_OK, intent)
                            requireActivity().finish()
                        }
                        result.onFailure { e ->
                            Log.e("RecipeUpload", "업로드 실패", e)
                            Toast.makeText(requireContext(), "업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
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
            setHasFixedSize(true)
        }
    }

    // RecyclerView 높이 업데이트
    private fun updateRecyclerViewHeight(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.post {
            val itemCount = adapter.itemCount
            val itemHeight = recyclerView.getChildAt(0)?.measuredHeight ?: 0
            val totalHeight = itemHeight * itemCount * 1.2
            recyclerView.layoutParams = recyclerView.layoutParams.apply { height = totalHeight.toInt() }
        }
    }

    // 식재료 없애기
    private fun removeFlavoringItem(list: MutableList<FlavoringData>, position: Int, adapter: FlavoringLogAdapter, recyclerView: RecyclerView) {
        if (list.size > 1) {
            list.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, list.size)
            updateRecyclerViewHeight(recyclerView, adapter)
            updateWriteButtonState()
        } else
            Toast.makeText(requireContext(), "최소 하나의 항목은 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    private fun removeStepItem(position: Int) {
        if (steps.size > 1) {
            steps.removeAt(position)
            stepAdapter.notifyItemRemoved(position)
            stepAdapter.notifyItemRangeChanged(position, steps.size)
            updateRecyclerViewHeight(recipeBinding.frameEnterRecipeStep, stepAdapter)
            updateWriteButtonState()
        } else
            Toast.makeText(requireContext(), "최소 하나의 항목은 필요합니다.", Toast.LENGTH_SHORT).show()
        updateAddButtonState()
    }

    private fun showSelectImgDialog(position: Int) {
        if (!isAdded) {
            Toast.makeText(activity, "Fragment가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("이미지 선택")
            .setItems(arrayOf("기본 이미지 선택", "갤러리에서 선택")) { _, which ->
                when (which) {
                    0 -> {
                        steps[position].stepImg = "android.resource://com.example.elixir/drawable/img_blank"
                        stepAdapter.notifyItemChanged(position)
                    }
                    1 -> {
                        selectedPosition = position
                        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }
            }.show()
        updateWriteButtonState()
    }

    private fun showThumbnailDialog() {
        SelectImgDialog(requireContext(),
            {
                val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
                recipeBinding.recipeThumbnail.setImageURI(uri)
                thumbnail = uri.toString()
            },
            {
                pickThumbnailLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        ).show()
    }

    private fun updateAddButtonState() {
        val isIngredientsValid = ingredientList.all { it.name.isNotBlank() && it.unit.isNotBlank() }
        val isSeasoningValid = seasoningList.all { it.name.isNotBlank() && it.unit.isNotBlank() }
        val isStepsValid = steps.all { it.stepDescription.isNotBlank() && it.stepImg.isNotBlank() }
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

    // 모든 단계가 유효한지 확인하는 함수
    private fun stepsValid(stepList: List<RecipeStepData>): Boolean {
        for (item in stepList) {
            if (item.stepDescription.isBlank() || item.stepImg.isBlank()) {
                return false
            }
        }
        return true
    }

    // 모든 재료가 유효한지 확인하는 함수
    private fun flavoringValid(list: List<FlavoringData>): Boolean {
        return list.all { it.name.isNotBlank() && it.unit.isNotBlank() }
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

    // Spinner 리스너 생성 함수
    private fun simpleSpinnerListener(onSelected: (String) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            onSelected(parent.getItemAtPosition(position).toString())
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    // 레시피 데이터를 UI에 설정하는 함수
    private fun setRecipeDataToUI(recipeData: RecipeData) {
        isBindingData = true
        with(recipeBinding) {
            // 텍스트 필드
            Log.d("Recipe", "title from recipeData = ${recipeData.title}")
            recipeBinding.enterRecipeTitle.setText(recipeData.title)
            Log.d("Recipe", "EditText after setText = ${recipeBinding.enterRecipeTitle.text}")
            enterRecipeDescription.setText(recipeData.description)
            enterTipCaution.setText(recipeData.tips)

            // 썸네일 이미지
            thumbnail = recipeData.imageUrl
            Glide.with(requireContext())
                .load(thumbnail)
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(recipeThumbnail)

            // 카테고리 스피너
            setSpinnerSelection(selectLowAging, recipeData.categorySlowAging)
            setSpinnerSelection(selectType, recipeData.categoryType)

            // 시간 스피너
            setSpinnerSelection(selectHour, if (recipeData.timeHours == 0) "시" else recipeData.timeHours.toString())
            setSpinnerSelection(selectMin, if (recipeData.timeMinutes == 0) "분" else recipeData.timeMinutes.toString())

            // 식재료 태그 칩
            ingredientTags.clear()
            ingredientTags.addAll(recipeData.ingredientTagIds)
            setIngredientChips(recipeData.ingredientTagIds)

            // 알러지 칩
            allergies.clear()
            allergies.addAll(recipeData.allergies)
            setupAllergyChips(allergies)

            // 난이도 칩
            difficulty = recipeData.difficulty
            setDifficultyChipFromData(recipeData.difficulty)

            // 재료 리스트
            ingredientList.clear()
            ingredientList.addAll(recipeData.ingredients.map { FlavoringData(it.key, it.value) })
            ingredientsAdapter.notifyDataSetChanged()
            updateRecyclerViewHeight(frameEnterIngredients, ingredientsAdapter)

            // 양념 리스트
            seasoningList.clear()
            seasoningList.addAll(recipeData.seasoning.map { FlavoringData(it.key, it.value) })
            seasoningAdapter.notifyDataSetChanged()
            updateRecyclerViewHeight(frameEnterSeasoning, seasoningAdapter)

            // 단계 리스트
            steps.clear()
            val stepCount = maxOf(recipeData.stepDescriptions.size, recipeData.stepImageUrls.size)
            for (i in 0 until stepCount) {
                val desc = recipeData.stepDescriptions.getOrNull(i) ?: ""
                val img = recipeData.stepImageUrls.getOrNull(i) ?: ""
                steps.add(RecipeStepData(img, desc))
            }
            stepAdapter.notifyDataSetChanged()
            updateRecyclerViewHeight(frameEnterRecipeStep, stepAdapter)
        }

        isBindingData = false
        bindTextInputs()
        updateWriteButtonState()
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

    // 카테고리(저속노화, 종류) 스피너
    private fun setCategorySpinner(category: String?) {
        category?.let {
            val adapter = recipeBinding.selectLowAging.adapter
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString() == category) {
                    recipeBinding.selectLowAging.setSelection(i)
                    break
                }
            }
        }
    }

    // 시간 설정 스피너
    private fun setTimeSpinners(hour: Int, minute: Int) {
        val hourAdapter = recipeBinding.selectHour.adapter as? ArrayAdapter<String>
        hourAdapter?.let {
            val hourPosition = it.getPosition(hour.toString())
            recipeBinding.selectHour.setSelection(hourPosition)
        }

        val minuteAdapter = recipeBinding.selectMin.adapter as? ArrayAdapter<String>
        minuteAdapter?.let {
            val minutePosition = it.getPosition(minute.toString())
            recipeBinding.selectMin.setSelection(minutePosition)
        }
    }

    // 식재료 태그
    private fun setIngredientChips(ingredientTags: List<Int>?) {
        if (ingredientTags.isNullOrEmpty()) return

        // DB 태그 ID로 직접 비교
        recipeBinding.tagsIngredient.children.forEach { view ->
            if (view is Chip) {
                val tagId = chipMap[view.id] // chipMap: chipId -> tagId
                if (tagId in ingredientTags) {
                    view.isChecked = true
                }
            }
        }
    }

    // 알러지 태그 설정
    private fun setupAllergyChips(allergies: MutableList<String>) {
        val allergyList = listOf(
            recipeBinding.allergyEgg, recipeBinding.allergyMilk, recipeBinding.allergyBuckwheat,
            recipeBinding.allergyPeanut, recipeBinding.allergySoybean, recipeBinding.allergyWheat,
            recipeBinding.allergyMackerel, recipeBinding.allergyCrab, recipeBinding.allergyShrimp,
            recipeBinding.allergyPig, recipeBinding.allergyPeach, recipeBinding.allergyTomato,
            recipeBinding.allergyDioxide, recipeBinding.allergyWalnut, recipeBinding.allergyChicken,
            recipeBinding.allergyCow, recipeBinding.allergySquid, recipeBinding.allergySeashell,
            recipeBinding.allergyOyster, recipeBinding.allergyPinenut
        )
        val chipNone = recipeBinding.nA // '해당 없음' 칩

        // 1. 기존 선택 상태 복원
        allergyList.forEach { chip ->
            chip.isChecked = allergies.contains(chip.text.toString())
        }
        chipNone.isChecked = allergies.contains(chipNone.text.toString())

        // 2. 체크 변경 리스너 설정
        allergyList.forEach { chip ->
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    // 최대 5개 제한
                    if (allergies.size >= 5) {
                        button.isChecked = false // 선택 취소
                        Toast.makeText(button.context, "알레르기는 최대 5개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                        return@setOnCheckedChangeListener
                    }

                    chipNone.isChecked = false
                    allergies.remove(chipNone.text.toString())

                    if (!allergies.contains(chip.text.toString())) {
                        allergies.add(chip.text.toString())
                    }
                } else {
                    allergies.remove(chip.text.toString())
                }
            }
        }

        chipNone.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                allergyList.forEach { it.isChecked = false }
                allergies.clear()
                allergies.add(chipNone.text.toString())
            } else {
                allergies.remove(chipNone.text.toString())
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
                // 칩 삭제 리스너
                setOnCloseIconClickListener {
                    (ingredientTags as? MutableList)?.remove(ingredientId)
                    chipGroup.removeView(this)
                    updateWriteButtonState()
                }
            }
            // findIngredientChip 앞에 삽입
            val index = chipGroup.indexOfChild(findIngredientChip)
            chipGroup.addView(chip, index)
        }
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