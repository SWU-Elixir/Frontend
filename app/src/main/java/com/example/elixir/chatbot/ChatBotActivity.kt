package com.example.elixir.chatbot

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.ActivityChatbotBinding

class ChatBotActivity : ToolbarActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val chatList = mutableListOf<ChatItem>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Toolbar 설정
        toolBinding.title.text = "챗봇"
        toolBinding.btnMore.visibility = View.VISIBLE
        toolBinding.btnMore.setImageResource(R.drawable.ic_chatbot_reload)
        toolBinding.btnMore.setColorFilter(ContextCompat.getColor(this, R.color.elixir_orange), PorterDuff.Mode.SRC_IN)
        toolBinding.btnMore.setOnClickListener {
            resetChat()
        }

        // 뒤로가기 버튼 설정
        toolBinding.btnBack.setOnClickListener {
            finish()
        }

        // 채팅 레이아웃을 fragment_registration에 추가
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        toolBinding.fragmentRegistration.addView(binding.root)

        setupRecyclerView()
        showWelcomeMessage()
        setupListeners()
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chatList) { example ->
            handleExampleClick(example)
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = chatAdapter
    }

    // 버튼 클릭 리스너 초기화
    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }

    // 예시 버튼 클릭 시 처리
    private fun handleExampleClick(example: String) {
        when (example) {
            "나의 식단 피드백 받기" -> {
                addBotMessage("식단 피드백을 원하시는군요! 식단을 입력해 주세요.")
            }
            "나의 레시피 피드백 받기" -> {
                addBotMessage("레시피 피드백을 원하시는군요! 레시피를 입력해 주세요.")
            }
            "식단 추천 받기" -> {
                addBotMessage("추천 받을 기간을 선택해 주세요.\n\n최대 일주일 선택이 가능하며, 하루 세끼를 기준으로 추천됩니다.")
                // 1~7일 버튼 추가
                chatList.add(ChatItem.ExampleList((1..7).map { "${it}일" }))
                chatAdapter.notifyItemInserted(chatList.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            }
            "자유롭게 대화하기" -> {
                addBotMessage("궁금한 점이나 하고 싶은 말을 자유롭게 입력해 주세요!")
            }
            in listOf("1일", "2일", "3일", "4일", "5일", "6일", "7일") -> {
                addBotMessage("${example} 동안의 식단을 추천해드릴게요!")
                // 실제 추천 로직을 추가
            }
            else -> {
                addBotMessage("선택하신 항목: $example")
            }
        }
    }

    private fun addBotMessage(message: String) {
        val insertPosition = chatList.size
        chatList.add(ChatItem.TextMessage(message, isFromUser = false))
        chatAdapter.notifyItemInserted(insertPosition)
        binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
    }

    // 사용자가 직접 메시지를 입력하고 보내는 처리
    private fun sendMessage() {
        val message = binding.messageEditText.text.toString().trim()
        if (message.isNotEmpty()) {
            chatList.add(ChatItem.TextMessage(message, isFromUser = true))
            chatAdapter.notifyItemInserted(chatList.size - 1)
            binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
            binding.messageEditText.text.clear()
        }
    }

    // 웰컴 메시지 출력
    private fun showWelcomeMessage() {
        chatList.clear()
        chatList.add(ChatItem.TextMessage("안녕하세요! 👋\n저는 저속노화 식단을 함께하는 AI 챗봇입니다.", isFromUser = false))
        chatList.add(ChatItem.TextMessage(
            "식단에 도전하며 생긴 궁금증이나 어려운 점이 있다면 언제든 질문해보세요!\n\n" +
                    "새로운 대화를 시작하고 싶다면, 오른쪽 상단의 초기화하기 버튼을 눌러 주세요.\n" +
                    "기록해주신 식단과 레시피에 대한 피드백은 물론, 목표에 맞는 식단 추천도 도와드릴게요.✨", isFromUser = false))
        chatList.add(ChatItem.ExampleList(
            listOf("나의 식단 피드백 받기", "나의 레시피 피드백 받기", "식단 추천 받기", "자유롭게 대화하기")
        ))

        // 전체 변경 알림
        chatAdapter.notifyDataSetChanged()
    }

    // 채팅 기록 초기화
    private fun resetChat() {
        showWelcomeMessage()
    }
} 