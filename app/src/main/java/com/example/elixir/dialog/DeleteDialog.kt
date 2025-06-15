package com.example.elixir.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.elixir.R

class DeleteDialog(
    private val act: Activity,
    private val onConfirm: () -> Unit // 확인 버튼 콜백 추가
    ) {
    @SuppressLint("InflateParams")
    val dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_delete_confirm, null, false)!!
    fun show(){
        val dialog = AlertDialog.Builder(act)
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                onConfirm() // 확인 버튼 클릭 시 콜백 호출
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // 버튼 커스텀
        dialog.setOnShowListener {
            // 버튼 불러오기
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // 폰트 설정
            val font = ResourcesCompat.getFont(act, R.font.pretendard_medium)
            positiveButton.typeface = font
            negativeButton.typeface = font

            // 색상 설정
            positiveButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_orange))
            negativeButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_gray))
        }
        // 다이얼로그 보이기
        dialog.show()
    }
}