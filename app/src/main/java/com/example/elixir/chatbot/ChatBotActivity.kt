package com.example.elixir.chatbot

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.ActivityChatbotBinding
import com.example.elixir.ingredient.data.IngredientItem
import kotlinx.coroutines.launch

class ChatBotActivity : ToolbarActivity() {
    private lateinit var chatGptService: ChatGptService
    private lateinit var binding: ActivityChatbotBinding
    private val chatList = mutableListOf<ChatItem>()
    private lateinit var chatAdapter: ChatAdapter
    private var ingredientMap: Map<Int, IngredientItem> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ChatGptService 초기화
        chatGptService = ChatGptService()

        // 툴바 UI 구성
        toolBinding.title.text = "챗봇"
        toolBinding.btnMore.visibility = View.VISIBLE
        toolBinding.btnMore.setImageResource(R.drawable.ic_chatbot_reload)
        toolBinding.btnMore.setColorFilter(ContextCompat.getColor(this, R.color.elixir_orange), PorterDuff.Mode.SRC_IN)
        toolBinding.btnMore.setOnClickListener {
            resetChat()
        }

        // 툴바 뒤로가기
        toolBinding.btnBack.setOnClickListener {
            finish()
        }

        // ViewBinding 연결 및 레이아웃 삽입
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        toolBinding.fragmentRegistration.addView(binding.root)

        // 식재료 전체 리스트 받아오기
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instanceIngredientApi.getAllIngredients()
                ingredientMap = response.data.associateBy { it.id }
                setupRecyclerView()
                showWelcomeMessage()
            } catch (e: Exception) {
                ingredientMap = emptyMap()
                setupRecyclerView()
                showWelcomeMessage()
            }
        }
        setupListeners()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            chatList = chatList,
            ingredientMap = ingredientMap,
            onExampleClick = { item ->
                when (item) {
                    is ChatMeal -> {
                        // ChatMeal 선택 시 실제 데이터로 API 요청
                        val requestDto = ChatRequestDto(
                            type = ChatRequestDto.TYPE_DIET_FEEDBACK,
                            targetId = item.id.toLong(),
                            message = "식단 피드백 요청" // 서버에서 요구하는 필수 필드
                        )
                        // 로딩 메시지 추가
                        val loadingMessage = "답변을 생성중입니다..."
                        chatList.add(ChatItem.TextMessage(loadingMessage, isFromUser = false))
                        val loadingPosition = chatList.size - 1
                        chatAdapter.notifyItemInserted(loadingPosition)
                        binding.chatRecyclerView.scrollToPosition(loadingPosition)

                        // API 요청
                        lifecycleScope.launch {
                            try {
                                val responseDto = chatGptService.sendChatRequest(requestDto)
                                // 로딩 메시지 제거
                                chatList.removeAt(loadingPosition)
                                chatAdapter.notifyItemRemoved(loadingPosition)
                                // 실제 응답 추가
                                chatList.add(ChatItem.TextMessage(responseDto.message, isFromUser = false))
                                chatAdapter.notifyItemInserted(chatList.size - 1)
                                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                            } catch (e: Exception) {
                                // 로딩 메시지 제거
                                chatList.removeAt(loadingPosition)
                                chatAdapter.notifyItemRemoved(loadingPosition)
                                // 에러 메시지 추가
                                chatList.add(ChatItem.TextMessage(
                                    "죄송합니다. 응답을 생성하는데 실패했습니다. 다시 시도해주세요.",
                                    isFromUser = false
                                ))
                                chatAdapter.notifyItemInserted(chatList.size - 1)
                                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                            }
                        }
                    }
                    is ChatRecipe -> {
                        // ChatRecipe 선택 시 실제 데이터로 API 요청
                        val requestDtoToUpdate = ChatRequestDto(
                            type = ChatRequestDto.TYPE_RECIPE_FEEDBACK,
                            targetId = item.id,
                            message = "레시피 피드백 요청" // 서버에서 요구하는 필수 필드
                        )
                        // 로딩 메시지 추가
                        val loadingMessage = "답변을 생성중입니다..."
                        chatList.add(ChatItem.TextMessage(loadingMessage, isFromUser = false))
                        val loadingPosition = chatList.size - 1
                        chatAdapter.notifyItemInserted(loadingPosition)
                        binding.chatRecyclerView.scrollToPosition(loadingPosition)

                        // API 요청
                        lifecycleScope.launch {
                            try {
                                val responseDto = chatGptService.sendChatRequest(requestDtoToUpdate)
                                // 로딩 메시지 제거
                                chatList.removeAt(loadingPosition)
                                chatAdapter.notifyItemRemoved(loadingPosition)
                                // 실제 응답 추가
                                chatList.add(ChatItem.TextMessage(responseDto.message, isFromUser = false))
                                chatAdapter.notifyItemInserted(chatList.size - 1)
                                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                            } catch (e: Exception) {
                                // 로딩 메시지 제거
                                chatList.removeAt(loadingPosition)
                                chatAdapter.notifyItemRemoved(loadingPosition)
                                // 에러 메시지 추가
                                chatList.add(ChatItem.TextMessage(
                                    "죄송합니다. 응답을 생성하는데 실패했습니다. 다시 시도해주세요.",
                                    isFromUser = false
                                ))
                                chatAdapter.notifyItemInserted(chatList.size - 1)
                                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                            }
                        }
                    }
                    else -> handleExampleClick(item.toString())
                }
            }
        )
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = chatAdapter
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageEditText.text.clear()
            }
        }
    }

    private fun handleExampleClick(example: String) {
        when (example) {
            "나의 식단 피드백 받기" -> {
                val requestDto = ChatRequestDto(type = ChatRequestDto.TYPE_DIET_FEEDBACK)
                addBotMessage("피드백 받을 식단을 선택해 주세요. \n1개의 식단 선택이 가능합니다.", requestDto)
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instanceDietApi.getDietLogRecent(10)
                        val meals = response.body()?.data?.map { meal ->
                            ChatMeal(
                                id = meal.id,
                                imageUrl = meal.imageUrl,
                                date = meal.time.toString(),
                                title = meal.name,
                                subtitle = "",
                                badgeNumber = meal.score,
                                ingredientTags = meal.ingredientTagIds
                            )
                        } ?: emptyList()
                        chatList.add(ChatItem.ChatMealList(meals, requestDto))
                        chatAdapter.notifyItemInserted(chatList.size - 1)
                        binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                    } catch (e: Exception) {
                        addBotMessage("식단 목록을 가져오는데 실패했습니다. 다시 시도해주세요.")
                    }
                }
            }
            "나의 레시피 피드백 받기" -> {
                val requestDto = ChatRequestDto(type = ChatRequestDto.TYPE_RECIPE_FEEDBACK)
                addBotMessage("피드백 받을 레시피를 선택해 주세요. \n1개의 레시피 선택이 가능합니다.", requestDto)
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instanceRecipeApi.getRecipeMy(10)
                        val recipes = response.body()?.data?.map { recipe ->
                            ChatRecipe(
                                id = recipe.recipeId.toLong(),
                                iconResUrl = recipe.imageUrl,
                                title = recipe.title,
                                subtitle = "",
                                ingredientTags = recipe.ingredientTagIds
                            )
                        } ?: emptyList()
                        chatList.add(ChatItem.ChatRecipeList(recipes, requestDto))
                        chatAdapter.notifyItemInserted(chatList.size - 1)
                        binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                    } catch (e: Exception) {
                        addBotMessage("레시피 목록을 가져오는데 실패했습니다. 다시 시도해주세요.")
                    }
                }
            }
            "식단 추천 받기" -> {
                val requestDto = ChatRequestDto(type = ChatRequestDto.TYPE_RECOMMEND)
                addBotMessage("추천 받을 기간을 선택해 주세요.\n\n최대 일주일 선택이 가능하며, 하루 세끼를 기준으로 추천됩니다.", requestDto)
                chatList.add(ChatItem.ExampleList((1..7).map { "${it}일" }, requestDto))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }
            "자유롭게 대화하기" -> {
                val requestDto = ChatRequestDto(type = ChatRequestDto.TYPE_FREETALK)
                addBotMessage("궁금한 점이나 하고 싶은 말을 자유롭게 입력해 주세요!", requestDto)
            }
            in listOf("1일", "2일", "3일", "4일", "5일", "6일", "7일") -> {
                val durationDays = example.replace("일", "").toIntOrNull()
                val requestDto = ChatRequestDto(
                    type = ChatRequestDto.TYPE_RECOMMEND,
                    durationDays = durationDays
                )
                addBotMessage("이번 달 챌린지 식재료를 포함해서 식단을 짜드릴까요?", requestDto)
                chatList.add(ChatItem.ExampleList(listOf("사용함", "사용안함"), requestDto))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }
            in listOf("사용함","사용안함") -> {
                // "사용함" 또는 "사용안함"이 클릭되었을 때
                val includeChallenge = example == "사용함"
                Log.d("ChatBotActivity", "includeChallengeIngredients set to: $includeChallenge")
                // 임시적으로 가장 최근에 추가된 ChatItem.ExampleList (기간 선택)의 DTO를 찾아서 업데이트
                val lastExampleList = chatList.findLast { it is ChatItem.ExampleList } as? ChatItem.ExampleList
                val requestDtoToUpdate = lastExampleList?.requestDto?.copy(
                    includeChallengeIngredients = includeChallenge
                )
                Log.d("ChatBotActivity", "Updated requestDto: includeChallengeIngredients = ${requestDtoToUpdate?.includeChallengeIngredients}")
                // 업데이트된 DTO를 다음 챗봇 메시지 ("추가 조건 입력 안내")에 연결
                addBotMessage("추가로 원하는 조건이 있다면 입력해 주세요. \n(예: 닭고기 포함, 낮은 칼로리, 없음 등)", requestDtoToUpdate)
            }
            else -> {
                addBotMessage("선택하신 항목: $example")
            }
        }
    }

    private fun addBotMessage(message: String, requestDto: ChatRequestDto? = null) {
        val insertPosition = chatList.size
        chatList.add(ChatItem.TextMessage(message, isFromUser = false, requestDto = requestDto))
        chatAdapter.notifyItemInserted(insertPosition)
        binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
    }

    private fun sendMessage(message: String) {
        // 바로 이전 챗봇 메시지를 확인하여 연결된 DTO가 있는지 찾습니다.
        val lastBotMessage = chatList.findLast { it is ChatItem.TextMessage && !it.isFromUser } as? ChatItem.TextMessage
        val prevDto = lastBotMessage?.requestDto

        // FREETALK이면 message 필드에, 그 외에는 additionalConditions에 값 할당
        val finalRequest = if (prevDto?.type == ChatRequestDto.TYPE_FREETALK) {
            prevDto.copy(message = message)
        } else if (prevDto != null) {
            prevDto.copy(additionalConditions = message)
        } else {
            ChatRequestDto(type = ChatRequestDto.TYPE_FREETALK, message = message)
        }

        // 로딩 메시지 추가
        val loadingMessage = "답변을 생성중입니다..."
        chatList.add(ChatItem.TextMessage(loadingMessage, isFromUser = false))
        val loadingPosition = chatList.size - 1
        chatAdapter.notifyItemInserted(loadingPosition)
        binding.chatRecyclerView.scrollToPosition(loadingPosition)

        // 서버 API 호출
        lifecycleScope.launch {
            try {
                val responseDto = chatGptService.sendChatRequest(finalRequest)
                // 로딩 메시지 제거
                chatList.removeAt(loadingPosition)
                chatAdapter.notifyItemRemoved(loadingPosition)
                // 실제 응답 추가
                chatList.add(ChatItem.TextMessage(responseDto.message, isFromUser = false))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            } catch (e: Exception) {
                // 로딩 메시지 제거
                chatList.removeAt(loadingPosition)
                chatAdapter.notifyItemRemoved(loadingPosition)
                // 에러 메시지 추가
                chatList.add(ChatItem.TextMessage(
                    "죄송합니다. 응답을 생성하는데 실패했습니다. 다시 시도해주세요.",
                    isFromUser = false
                ))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }
        }
    }

    private fun showWelcomeMessage() {
        chatList.clear()
        chatList.add(ChatItem.TextMessage("안녕하세요! 👋\n저는 건강한 식단을 함께하는 AI 챗봇입니다.", isFromUser = false))
        chatList.add(
            ChatItem.TextMessage(
                "식단에 도전하며 생긴 궁금증이나 어려운 점이 있다면 언제든 질문해보세요!\n\n" +
                        "기록해주신 식단과 레시피에 대한 피드백은 물론, 목표에 맞는 식단 추천도 도와드릴게요. ✨\n" +
                        "새로운 대화를 시작하고 싶다면, 오른쪽 상단의 초기화하기 버튼을 눌러 주세요.\n" +
                        "그럼, 아래에서 원하는 목적을 선택해 대화를 시작해 볼까요?",
                isFromUser = false
            )
        )
        chatList.add(
            ChatItem.ExampleList(
                listOf("나의 식단 피드백 받기", "나의 레시피 피드백 받기", "식단 추천 받기", "자유롭게 대화하기")
            )
        )
        chatAdapter.notifyDataSetChanged()
    }

    // 챗봇 초기화 (초기화 버튼 클릭 시)
    private fun resetChat() {
        showWelcomeMessage()
        // 눌러졌던 버튼 초기화
    }
}
