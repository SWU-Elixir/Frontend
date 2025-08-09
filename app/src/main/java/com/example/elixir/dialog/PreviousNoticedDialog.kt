package com.example.elixir.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.elixir.R

class PreviousNoticedDialog(
    private val act: Activity,
    private val onDoNotShowAgainChecked: (Boolean) -> Unit = {}
) {
    fun show() {
        val dialogView = LayoutInflater.from(act).inflate(R.layout.diallog_previous_noticed, null, false)
        val checkBox = dialogView.findViewById<CheckBox>(R.id.cb1Month)

        val dialog = AlertDialog.Builder(act)
            .setView(dialogView)
            .setPositiveButton("닫기") { dialogInterface, _ ->
                onDoNotShowAgainChecked(checkBox.isChecked)
                dialogInterface.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            val font = ResourcesCompat.getFont(act, R.font.pretendard_medium)
            positiveButton.typeface = font
            positiveButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_orange))
        }

        dialog.show()
    }
}
