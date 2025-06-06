package com.example.elixir.recipe.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.FragmentRecipeLogBinding
import com.example.elixir.dialog.SaveDialog
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.FlavoringData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.data.RecipeStepData
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.android.material.chip.Chip
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
    private val ingredientList = mutableListOf<FlavoringData>()
    private val seasoningList = mutableListOf<FlavoringData>()
    private val steps = mutableListOf<RecipeStepData>()
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

        repository = RecipeRepository(AppDatabase.getInstance(requireContext()).recipeDao())
        chipMap = mapOf(
            recipeBinding.ingredientSeasonedCabbage.id to 614,
            recipeBinding.ingredientStrawberry.id to 388,
            recipeBinding.ingredientSpinach.id to 768,
            recipeBinding.ingredientAlmond.id to 802
        )
        thumbnailUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")

        // 데이터 초기화
        initData()

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

        // 식재료
        setupIngredientChips()

        // 알러지 설정
        setupAllergyChips()

        // 난이도 설정
        setupDifficultyChips()

        // 리사이클러뷰 설정
        setupRecyclerViews()

        // 추가 버튼 설정
        setupAddButtons()

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
        recipeBinding.enterRecipeTitle.addTextChangedListener(simpleTextWatcher {
            if (!isBindingData) {
                recipeTitle = it
                updateWriteButtonState()
            }
        })
        recipeBinding.enterRecipeDescription.addTextChangedListener(simpleTextWatcher {
            if (!isBindingData) {
                recipeDescription = it
                updateWriteButtonState()
            }
        })
        recipeBinding.enterTipCaution.addTextChangedListener(simpleTextWatcher {
            if (!isBindingData) {
                tips = it
                updateWriteButtonState()
            }
        })
    }

    // 식재료 설정
    private fun setupIngredientChips() {
        val chipList = listOf(recipeBinding.ingredientSeasonedCabbage, recipeBinding.ingredientStrawberry, recipeBinding.ingredientSpinach, recipeBinding.ingredientAlmond)
        chipList.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                val chipTag = chipMap[chip.id] ?: return@setOnCheckedChangeListener
                if (isChecked) {
                    if (ingredientTags.size >= 5) {
                        chip.isChecked = false
                        Toast.makeText(requireContext(), "식재료 태그는 최대 5개까지 선택 가능합니다", Toast.LENGTH_SHORT).show()
                    } else ingredientTags.add(chipTag)
                } else ingredientTags.remove(chipTag)
            }
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
                        email = "email",
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
                        createdAt = org.threeten.bp.LocalDateTime.now(),
                        updatedAt = org.threeten.bp.LocalDateTime.now()
                    )
                    recipeViewModel.saveRecipeToDB(recipeData)
                    val intent = Intent().apply {
                        putExtra("mode", if (arguments?.getBoolean("isEdit") == true) 9 else 0)
                        putExtra("recipeData", Gson().toJson(recipeData))
                    }
                    Toast.makeText(requireContext(), if (arguments?.getBoolean("isEdit") == true) "레시피가 수정되었습니다." else "레시피가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
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
            recipeThumbnail.setImageURI(Uri.parse(recipeData.imageUrl))

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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        recipeLogBinding = null
    }
}