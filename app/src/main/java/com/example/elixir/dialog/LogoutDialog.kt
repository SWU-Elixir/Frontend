package com.example.elixir.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.login.LogoutResponse
import com.example.elixir.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogoutDialog(private val act: Activity) {
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

            // 로그아웃 API 호출
            positiveButton.setOnClickListener {
                val token = act.getSharedPreferences("authPrefs", Context.MODE_PRIVATE)
                    .getString("accessToken", null)

                if (token != null) {
                    RetrofitClient.instance.logout().enqueue(object : Callback<LogoutResponse> {
                        override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                            if (response.isSuccessful) {
                                Log.d("LogoutDialog", "로그아웃 성공")
                            } else {
                                Log.w("LogoutDialog", "로그아웃 응답 실패: "+response.code())
                            }
                            completeLogout()
                        }

                        override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                            Log.e("LogoutDialog", "로그아웃 실패", t)
                            completeLogout()
                        }
                    })
                } else {
                    completeLogout()
                }

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun completeLogout() {
        // 토큰 삭제
        act.getSharedPreferences("authPrefs", Context.MODE_PRIVATE).edit().remove("accessToken").apply()

        // 홈으로 이동 또는 로그인 화면으로 이동
        val intent = Intent(act, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        act.startActivity(intent)
        act.finish()
    }
}
