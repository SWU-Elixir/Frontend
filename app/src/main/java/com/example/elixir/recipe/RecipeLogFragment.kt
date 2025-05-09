package com.example.elixir.recipe

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.FragmentRecipeLogBinding
import com.example.elixir.dialog.SaveDialog
import com.example.elixir.dialog.SelectImgDialog

class RecipeLogFragment : Fragment() {
    // 프래그먼트 바인딩 정의
    private var recipeLogBinding: FragmentRecipeLogBinding? = null
    private val recipeBinding get() = recipeLogBinding!!

    // 정보
    // 레시피 제목, 썸네일, 설명
    private var recipeTitle: String = ""
    private var thumbnail: String = ""
    private lateinit var thumbnailUri: Uri
    private var recipeDescription: String = ""

    // 카테고리 (저속노화, 종류(한식, 양식 등)), 난이도
    private var categorySlowAging: String = ""
    private var categoryType: String = ""
    private var difficulty: String = ""

    // 식재료 태그, 알레르기 태그
    private var ingredientTags = mutableListOf<String>()
    private var allergies = mutableListOf<String>()

    // 재료, 양념, 조리법, 팁
    private val ingredients = mutableListOf<FlavoringData>()
    private val seasoning = mutableListOf<FlavoringData>()
    private val steps = mutableListOf<RecipeStepData>()
    private var tips: String = ""

    // 조리 시간 (시, 분)
    private var timeHours: Int = 0
    private var timeMinutes: Int = 0

    // 어댑터 정의
    private lateinit var ingredientsAdapter: FlavoringLogAdapter
    private lateinit var seasoningAdapter: FlavoringLogAdapter
    private lateinit var stepAdapter: RecipeStepLogAdapter

    // 이미지 피커
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            // 선택된 이미지를 해당 단계 데이터에 반영
            steps[selectedPosition].stepImg = uri.toString()
            stepAdapter.notifyItemChanged(selectedPosition)
        } else {
            // 이미지 선택 취소 시 메시지 표시
            Toast.makeText(requireContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 선택된 단계의 위치
    private var selectedPosition: Int = -1

    // 뷰모델
    private lateinit var recipeViewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        recipeLogBinding = FragmentRecipeLogBinding.inflate(inflater, container, false)
        return recipeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recipeData = arguments?.getParcelable<RecipeData>("recipeData")

        recipeData?.let {
            recipeBinding.enterRecipeTitle.setText(it.title)
            recipeBinding.enterRecipeDescription.setText(it.tips ?: "")
            val hourIndex = (recipeBinding.selectHour.adapter as? ArrayAdapter<String>)?.getPosition(it.timeHours.toString()) ?: -1
            if (hourIndex != -1) {
                recipeBinding.selectHour.setSelection(hourIndex)
            } else {
                // 기본값 설정 또는 로그 출력
                android.util.Log.e("RecipeLogFragment", "시간 값이 어댑터에 없습니다: ${it.timeHours}")
            }

            val minIndex = (recipeBinding.selectMin.adapter as? ArrayAdapter<String>)?.getPosition(it.timeMinutes.toString()) ?: -1
            if (minIndex != -1) {
                recipeBinding.selectMin.setSelection(minIndex)
            } else {
                // 기본값 설정 또는 로그 출력
                android.util.Log.e("RecipeLogFragment", "시간 값이 어댑터에 없습니다: ${it.timeMinutes}")
            }


            val difficultyList = listOf(recipeBinding.levelEasy, recipeBinding.levelNormal, recipeBinding.levelHard)
            difficultyList.forEach { chip ->
                chip.isChecked = chip.text.toString() == it.difficulty
            }

            val ingredientChips = listOf(
                recipeBinding.ingredientBrownRice, recipeBinding.ingredientBean, recipeBinding.ingredientGrain,
                recipeBinding.ingredientGreenLeafy, recipeBinding.ingredientBerry, recipeBinding.ingredientNuts,
                recipeBinding.ingredientOliveOil, recipeBinding.ingredientFish, recipeBinding.ingredientPoultry
            )

            ingredientChips.forEach { chip ->
                chip.isChecked = it.ingredients.contains(chip.text.toString())
            }
        }

        // ViewModel 초기화
        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        // 썸네일 URI 초기화
        context?.let {
            thumbnailUri = Uri.parse("android.resource://${it.packageName}/${R.drawable.img_blank}")
        }

        ingredients.clear()
        ingredients.add(FlavoringData("", ""))

        seasoning.clear()
        seasoning.add(FlavoringData("", ""))

        // 단계 리스트 초기화
        steps.clear()
        context?.let {
            steps.add(RecipeStepData("android.resource://${it.packageName}/${R.drawable.img_blank}", ""))
        }

        // 레시피명 입력
        recipeBinding.enterRecipeTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                recipeTitle = s.toString().trim()
                updateWriteButtonState()
            }
        })

        // 레시피 썸네일 초기화: 기본 이미지로 설정
        recipeBinding.recipeThumbnail.setImageURI(thumbnailUri)
        thumbnail = thumbnailUri.toString()

        // 이미지 피커 선언 (PickVisualMedia)
        val imgSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                recipeBinding.recipeThumbnail.setImageURI(uri)
                thumbnail = uri.toString()
            }
        }
        

        // 프로필 이미지를 눌렀을 때, 선택 다이얼로그 띄우기
        recipeBinding.recipeThumbnail.setOnClickListener {
            SelectImgDialog(requireContext(),
                {
                    // 기본 이미지 선택 시 기본 이미지로 설정
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
                    recipeBinding.recipeThumbnail.setImageURI(uri)
                    thumbnail = uri.toString()
                },
                {
                    // 갤러리에서 이미지 선택
                    imgSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ).show()
        }

        // 레시피 설명 입력
        recipeBinding.enterRecipeDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                recipeDescription = s.toString().trim()
                updateWriteButtonState()
            }
        })

        // 카테고리 선택
        // 저속노화 Spinner 설정
        val lowAgingOptions = resources.getStringArray(R.array.method_list)
        val lowAgingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lowAgingOptions)
        lowAgingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeBinding.selectLowAging.adapter = lowAgingAdapter

        // 저속노화 Spinner 아이템 선택 리스너 설정
        recipeBinding.selectLowAging.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                categorySlowAging = if (selectedOption != "저속노화") { selectedOption } else { "" }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 종류 Spinner 설정
        val typeOptions = resources.getStringArray(R.array.type_list)
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeOptions)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeBinding.selectType.adapter = typeAdapter

        // 종류 Spinner 아이템 선택 리스너 설정
        recipeBinding.selectType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                categoryType = if (selectedOption != "종류") { selectedOption } else { "" }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 식재료 태그 선택
        with(recipeBinding) {
            // 일반 칩 리스트
            val ingredientsList = listOf(
                ingredientBrownRice, ingredientBean, ingredientGrain,
                ingredientGreenLeafy, ingredientBerry, ingredientNuts, ingredientOliveOil,
                ingredientFish, ingredientPoultry)

            // 챌린지 칩
            val chipChallenge = ingredientChallenge

            ingredientsList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    val tagText = chip.text.toString()

                    if (isChecked) {
                        if (chip != chipChallenge) {
                            // 일반 칩이면 5개 제한 체크
                            val normalTagCount = ingredientTags.count { it != chipChallenge.text.toString() }
                            if (normalTagCount >= 5) {
                                chip.isChecked = false
                                Toast.makeText(requireContext(), "일반 재료는 최대 5개까지만 선택할 수 있습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                return@setOnCheckedChangeListener
                            }
                        }

                        // 추가 (챌린지든 일반이든)
                        if (!ingredientTags.contains(tagText)) {
                            ingredientTags.add(tagText)
                        }
                    } else {
                        // 체크 해제: 무조건 제거
                        ingredientTags.remove(tagText)
                    }
                }
            }
        }

        // 알레르기 태그 선택
        with(recipeBinding) {
            // 일반 칩 리스트
            val allergyList = listOf(
                allergyEgg, allergyMilk, allergyBuckwheat, allergyPeanut,
                allergySoybean, allergyWheat, allergyMackerel, allergyCrab, allergyShrimp,
                allergyPig, allergyPeach, allergyTomato, allergyDioxide, allergyWalnut,
                allergyChicken, allergyCow, allergySquid, allergySeashell, allergyOyster,
                allergyPinenut)

            // 해당 없음 칩
            val chipNone = nA

            // 일반 칩 선택 시
            allergyList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    // 일반 칩을 선택했다면 해당 없음 칩을 리스트에서 제거
                    if (isChecked) {
                        chipNone.isChecked = false
                        allergies.remove(chipNone.text.toString())

                        // 중복 저장 방지
                        if (!allergies.contains(chip.text.toString()))
                            allergies.add(chip.text.toString())
                    }
                    // 두번 클릭 시 리스트에서 제거
                    else allergies.remove(chip.text.toString())
                }
            }

            // 특별히 없음 선택 시
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                // 일반 칩 모두 선택 해제, 리스트에서 제거하고 특별히 없음을 저장
                if (isChecked) {
                    allergyList.forEach { it.isChecked = false }
                    allergies.clear()
                    allergies.add(chipNone.text.toString())
                }
                // 두 번 클릭 시 리스트에서 제거
                else allergies.remove(chipNone.text.toString())
            }
        }

        // 난이도 칩 선택
        with(recipeBinding) {
            val difficultyList = listOf(levelEasy, levelNormal, levelHard)

            difficultyList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // 선택된 난이도 저장
                        difficulty = chip.text.toString()
                    } else {
                        // 체크 해제 시 난이도 초기화
                        if (difficulty == chip.text.toString()) {
                            difficulty = ""
                        }
                    }
                }
            }
        }

        // 시간 Spinner 설정
        val hourOptions = resources.getStringArray(R.array.cookingHours)
        val hourAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hourOptions)
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        recipeBinding.selectHour.adapter = hourAdapter

        // 시간 Spinner 선택 리스너 설정
        recipeBinding.selectHour.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (val selectedOption = parent.getItemAtPosition(position).toString()) {
                    "시" -> timeHours = 0 // 기본값
                    "12시간 이상" -> timeHours = 12 // 12시간 이상을 특정 값으로 설정
                    else -> selectedOption.toIntOrNull() ?: 0 // 숫자로 변환 가능하면 변환, 아니면 기본값
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 분 Spinner 설정
        val minuteSpinner = recipeBinding.selectMin
        val minuteOptions = resources.getStringArray(R.array.cookingMinutes)
        val minuteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, minuteOptions)
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        minuteSpinner.adapter = minuteAdapter

        // 분 Spinner 선택 리스너 설정
        minuteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (val selectedOption = parent.getItemAtPosition(position).toString()) {
                    "분" -> timeMinutes = 0 // 기본값
                    else -> selectedOption.toIntOrNull() ?: 0 // 숫자로 변환 가능하면 변환, 아니면 기본값
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // 재료 RecyclerView 삭제 버튼 클릭 시 해당 아이템 삭제
        ingredientsAdapter = FlavoringLogAdapter(ingredients, { position ->
            removeFlavoringItem(ingredients, position, ingredientsAdapter, recipeBinding.frameEnterIngredients)
        }, {
            updateAddButtonState()
        })

        // 재료 RecyclerView 어댑터, 레이아웃 설정
        recipeBinding.frameEnterIngredients.apply {
            adapter = ingredientsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // 초기 상태에서 추가 버튼 비활성화
        recipeBinding.btnIngredientsAdd.setOnClickListener {
            ingredients.add(FlavoringData("", ""))
            ingredientsAdapter.notifyItemInserted(ingredients.size - 1)
            // 리사이클러뷰 높이 업데이트
            updateRecyclerViewHeight(recipeBinding.frameEnterIngredients, ingredientsAdapter)
        }

        // 양념 RecyclerView 어댑터 설정
        seasoningAdapter = FlavoringLogAdapter(seasoning, { position ->
            removeFlavoringItem(seasoning, position, seasoningAdapter, recipeBinding.frameEnterSeasoning)
        }, {
            updateAddButtonState() // 버튼 상태 업데이트 함수 전달
        })

        // 양념 RecyclerView 어댑터, 레이아웃 설정
        recipeBinding.frameEnterSeasoning.apply {
            adapter = seasoningAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // 초기 상태에서 추가 버튼 비활성화
        recipeBinding.btnSeasoningAdd.setOnClickListener {
            seasoning.add(FlavoringData("", ""))
            seasoningAdapter.notifyItemInserted(seasoning.size - 1)
            // 리사이클러뷰 높이 업데이트
            updateRecyclerViewHeight(recipeBinding.frameEnterSeasoning, seasoningAdapter)
        }

        // 단계 RecyclerView 설정
        stepAdapter = RecipeStepLogAdapter(steps,
            // 삭제 처리
            { position ->
                if (steps.size > 1) {
                    steps.removeAt(position)
                    stepAdapter.notifyItemRemoved(position)
                    stepAdapter.notifyItemRangeChanged(position, steps.size)// 리사이클러뷰 높이 업데이트
                    updateRecyclerViewHeight(recipeBinding.frameEnterRecipeStep, stepAdapter)
                } else {
                    Toast.makeText(requireContext(), "최소 하나의 항목은 필요합니다.", Toast.LENGTH_SHORT).show()
                }
                updateAddButtonState() // 버튼 상태 업데이트 호출
            },
            // 이미지 클릭 시 다이얼로그 열기
            { position ->
                showSelectImgDialog(position)
            },
            // 버튼 상태 업데이트
            {
                updateAddButtonState()
            }
        )

        // 단계 RecyclerView 어댑터 설정
        recipeBinding.frameEnterRecipeStep.apply {
            adapter = stepAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // 초기 상태에서 추가 버튼 클릭 시 기본 이미지로 초기화
        recipeBinding.btnRecipeStepAdd.setOnClickListener {
            steps.add(RecipeStepData("android.resource://${requireContext().packageName}/${R.drawable.img_blank}", ""))
            stepAdapter.notifyItemInserted(steps.size - 1)
            // 리사이클러뷰 높이 업데이트
            updateRecyclerViewHeight(recipeBinding.frameEnterRecipeStep, stepAdapter)
        }

        // 팁/주의사항 입력
        recipeBinding.enterTipCaution.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tips = s.toString().trim()
                updateWriteButtonState()
            }
        })

        recipeBinding.btnWriteRecipe.setOnClickListener {
            if (isAllFieldsValid()) {
                val saveDialog = SaveDialog(requireActivity()) {
                    val recipeEntity = RecipeEntity(
                        title = recipeTitle,
                        description = recipeDescription,
                        categorySlowAging = categorySlowAging,
                        categoryType = categoryType,
                        difficulty = difficulty,
                        timeHours = timeHours,
                        timeMinutes = timeMinutes,
                        ingredientTagNames = ingredientTags,
                        ingredients = ingredients.associate { it.name to it.unit },
                        seasoning = seasoning.associate { it.name to it.unit },
                        stepDescriptions = steps.map { it.stepDescription },
                        stepImageUrls = steps.map { it.stepImg },
                        tips = tips,
                        allergies = allergies,
                        imageUrl = thumbnail,
                        createdAt = System.currentTimeMillis().toString(),
                        updatedAt = System.currentTimeMillis().toString()
                    )

                    recipeViewModel.saveRecipeToDB(recipeEntity)
                    Toast.makeText(requireContext(), "레시피가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
                saveDialog.show()
            }
        }
    }

    // 이미지 선택 다이얼로그
    private fun showSelectImgDialog(position: Int) {
        if (!isAdded) {
            Toast.makeText(activity, "Fragment가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("기본 이미지 선택", "갤러리에서 선택")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("이미지 선택")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // 기본 이미지 선택
                    steps[position].stepImg =
                        "android.resource://com.example.elixir/drawable/img_blank"
                    stepAdapter.notifyItemChanged(position)
                }

                1 -> {
                    // 갤러리에서 이미지 선택
                    selectedPosition = position
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
        }
        builder.show()
    }

    // 재료와 양념 추가 버튼 상태 업데이트
    private fun updateAddButtonState() {
        val isIngredientsValid = flavoringValid(ingredients)
        val isSeasoningValid = flavoringValid(seasoning)
        val isStepsValid = stepsValid(steps)

        // 재료 추가 버튼 상태 업데이트
        recipeBinding.btnIngredientsAdd.apply {
            isEnabled = isIngredientsValid
            backgroundTintList = resources.getColorStateList(
                if (isIngredientsValid) R.color.elixir_orange else R.color.elixir_gray,
                null
            )
        }

        // 양념 추가 버튼 상태 업데이트
        recipeBinding.btnSeasoningAdd.apply {
            isEnabled = isSeasoningValid
            backgroundTintList = resources.getColorStateList(
                if (isSeasoningValid) R.color.elixir_orange else R.color.elixir_gray,
                null
            )
        }

        // 단계 추가 버튼 상태 업데이트
        recipeBinding.btnRecipeStepAdd.apply {
            isEnabled = isStepsValid
            backgroundTintList = resources.getColorStateList(
                if (isStepsValid) R.color.elixir_orange else R.color.elixir_gray,
                null
            )
        }
    }

    // 리사이클러뷰 높이 업데이트
    private fun updateRecyclerViewHeight(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.post {
            val itemCount = adapter.itemCount
            val itemHeight = recyclerView.getChildAt(0)?.measuredHeight ?: 0
            val totalHeight = itemHeight * itemCount * 1.2 // 1.2배 여유 공간 추가

            val layoutParams = recyclerView.layoutParams
            layoutParams.height = totalHeight.toInt()
            recyclerView.layoutParams = layoutParams
        }
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

    // FlavoringData 리스트에서 아이템 삭제 함수
    private fun removeFlavoringItem(list: MutableList<FlavoringData>, position: Int,
                                    adapter: FlavoringLogAdapter,  recyclerView: RecyclerView) {
        if (list.size > 1) {
            list.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, list.size)
            updateRecyclerViewHeight(recyclerView, adapter) // 높이 업데이트 호출
        } else {
            Toast.makeText(requireContext(), "최소 하나의 항목은 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // isAllFieldsValid 함수 분리
    private fun isAllFieldsValid(): Boolean {
        return recipeTitle.isNotBlank() &&
                thumbnail.isNotBlank() &&
                recipeDescription.isNotBlank() &&
                categorySlowAging.isNotBlank() &&
                categoryType.isNotBlank() &&
                difficulty.isNotBlank() &&
                ingredients.isNotEmpty() &&
                seasoning.isNotEmpty() &&
                steps.isNotEmpty() &&
                tips.isNotBlank()
    }

    // updateWriteButtonState에서 호출
    private fun updateWriteButtonState() {
        val isValid = isAllFieldsValid()

        recipeBinding.btnWriteRecipe.isEnabled = isValid
        recipeBinding.btnWriteRecipe.apply {
            backgroundTintList = resources.getColorStateList(
                if (isValid) R.color.elixir_orange else R.color.elixir_gray,
                null
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recipeLogBinding = null
    }
}