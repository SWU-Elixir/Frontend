package com.example.elixir.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemChatExampleListBinding
import com.example.elixir.databinding.ItemChatTextMessageBinding
import com.example.elixir.databinding.ItemChatTextMessageUserBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent

class ChatAdapter(
    private val chatList: List<ChatItem>,
    private val onExampleClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        // View 타입 상수
        private const val VIEW_TYPE_TEXT_MESSAGE = 0      // 챗봇 답변 메시지
        private const val VIEW_TYPE_TEXT_MESSAGE_USER = 1 // 사용자 입력 메시지
        private const val VIEW_TYPE_EXAMPLE_LIST = 2       // 예시 버튼 리스트
    }

    override fun getItemViewType(position: Int): Int {
        // 각 아이템에 맞는 ViewType 반환
        return when (val item = chatList[position]) {
            is ChatItem.TextMessage -> if (item.isFromUser) VIEW_TYPE_TEXT_MESSAGE_USER else VIEW_TYPE_TEXT_MESSAGE
            is ChatItem.ExampleList -> VIEW_TYPE_EXAMPLE_LIST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // ViewType에 따라 ViewHolder 생성
        return when (viewType) {
            VIEW_TYPE_TEXT_MESSAGE -> {
                val binding = ItemChatTextMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextMessageViewHolder(binding)
            }
            VIEW_TYPE_TEXT_MESSAGE_USER -> {
                val binding = ItemChatTextMessageUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextMessageUserViewHolder(binding)
            }
            VIEW_TYPE_EXAMPLE_LIST -> {
                val binding = ItemChatExampleListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ExampleListViewHolder(binding, onExampleClick)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // ViewHolder에 데이터 바인딩
        when (val item = chatList[position]) {
            is ChatItem.TextMessage -> {
                if (item.isFromUser) {
                    (holder as TextMessageUserViewHolder).bind(item)
                } else {
                    (holder as TextMessageViewHolder).bind(item)
                }
            }
            is ChatItem.ExampleList -> (holder as ExampleListViewHolder).bind(item)
        }
    }

    override fun getItemCount() = chatList.size

    // 챗봇 답변 메시지 ViewHolder
    class TextMessageViewHolder(private val binding: ItemChatTextMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem.TextMessage) {
            binding.chatMessage.text = item.message
        }
    }

    // 사용자 입력 메시지 ViewHolder
    class TextMessageUserViewHolder(private val binding: ItemChatTextMessageUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem.TextMessage) {
            binding.chatMessage.text = item.message
        }
    }

    // 예시 버튼 리스트 ViewHolder
    class ExampleListViewHolder(
        private val binding: ItemChatExampleListBinding,
        private val onExampleClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private var selectedIndex: Int = -1

        fun bind(item: ChatItem.ExampleList) {
            binding.buttonContainer.removeAllViews()

            // FlexboxLayout 속성 설정
            (binding.buttonContainer as FlexboxLayout).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP // 자동 줄바꿈
                justifyContent = JustifyContent.FLEX_START
            }

            item.examples.forEachIndexed { index, example ->
                val button = androidx.appcompat.widget.AppCompatButton(binding.root.context, null, R.style.ChatButton).apply {
                    text = example
                    textSize = 12f
                    setBackgroundResource(R.drawable.bg_rect_button_selector)
                    isSelected = (index == selectedIndex)
                    isFocusable = true
                    isClickable = true
                    stateListAnimator = null

                    val params = FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 8, 8, 8)
                        flexGrow = 0f
                    }
                    layoutParams = params

                    // 최초 생성 시 색상 지정
                    setTextColor(
                        if (index == selectedIndex)
                            context.getColor(R.color.elixir_orange)
                        else
                            context.getColor(R.color.black)
                    )

                    setOnClickListener {
                        selectedIndex = index
                        for (i in 0 until binding.buttonContainer.childCount) {
                            val btn = binding.buttonContainer.getChildAt(i) as androidx.appcompat.widget.AppCompatButton
                            btn.isSelected = (i == index)
                            btn.setTextColor(
                                if (i == index)
                                    context.getColor(R.color.elixir_orange)
                                else
                                    context.getColor(R.color.elixir_gray)
                            )
                        }
                        onExampleClick(example)
                    }
                }
                binding.buttonContainer.addView(button)
            }
        }
    }

}
