package com.example.elixir.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.databinding.FragmentChatbotBinding

class ChatBotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val chatList = mutableListOf<String>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리사이클러뷰 세팅
        chatAdapter = ChatAdapter(chatList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatAdapter

        // 초기 웰컴 메시지
        showWelcomeMessage()

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                chatList.add(message)
                chatAdapter.notifyItemInserted(chatList.size - 1)
                binding.chatRecyclerView.scrollToPosition(chatList.size - 1)
                binding.messageEditText.text.clear()

                // 여기에 AI 응답 추가 가능
            }
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.resetButton.setOnClickListener {
            resetChat()
        }
    }

    private fun showWelcomeMessage() {
        chatList.add("안녕하세요! 👋\n저는 저속노화 식단을 함께하는 AI 챗봇입니다.")
        chatList.add(
            "식단에 도전하며 생긴 궁금증이나 어려운 점이 있다면 언제든 질문해보세요!\n\n" +
                    "새로운 대화를 시작하고 싶다면, 오른쪽 상단의 초기화하기 버튼을 눌러 주세요.\n" +
                    "기록해주신 식단과 레시피에 대한 피드백은 물론, 목표에 맞는 식단 추천도 도와드릴게요.✨"
        )
        chatList.add("아래 중 원하는 목적을 선택해 주세요.")
    }

    private fun resetChat() {
        chatList.clear()
        showWelcomeMessage()
        chatAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}
