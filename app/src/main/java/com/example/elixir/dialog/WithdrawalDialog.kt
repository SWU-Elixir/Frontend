package com.example.elixir.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.login.LogoutResponse
import com.example.elixir.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WithdrawalDialog(private val act: Activity) {
    @SuppressLint("InflateParams")
    private val dialogView = LayoutInflater.from(act).inflate(R.layout.diallog_logout_confirm, null, false)

    fun show() {
        val dialog = AlertDialog.Builder(act)
            .setView(dialogView)
            .setPositiveButton("확인", null) // 커스텀 핸들링
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            // 폰트
            val font = ResourcesCompat.getFont(act, R.font.pretendard_medium)
            positiveButton.typeface = font
            negativeButton.typeface = font

            // 색상
            positiveButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_orange))
            negativeButton.setTextColor(ContextCompat.getColor(act, R.color.elixir_gray))

            // 클릭 리스너 등록
            positiveButton.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.instanceMemberApi.withdrawal()
                        }

                        if (response.isSuccessful) {
                            Toast.makeText(act, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                            // 로그인 화면으로 이동
                            val intent = Intent(act, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            act.startActivity(intent)

                            dialog.dismiss()
                        } else {
                            Toast.makeText(act, "탈퇴 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(act, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("WithdrawalDialog", "회원 탈퇴 실패", e)
                    }
                }
            }
        }

        dialog.show()
    }
}
