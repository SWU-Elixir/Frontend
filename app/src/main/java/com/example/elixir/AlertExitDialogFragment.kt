package com.example.elixir

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class AlertExitDialogFragment(private val act: Activity) {
    @SuppressLint("InflateParams")
    val dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_exit_confirm, null, false)!!
    fun show(){
        val dialog = AlertDialog.Builder(act)
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                act.finish()
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
            val font = ResourcesCompat.getFont(act, R.font.pretendard_regular)
            positiveButton.typeface = font
            negativeButton.typeface = font

            // 색상 설정
            positiveButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_orange))
            negativeButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_gray))
        }

        dialog.show()
    }
}