package com.example.elixir

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.elixir.databinding.DialogSelectImgBinding

class SelectImgDialog(private val context: Context,
                      private val onDefaultSelected: () -> Unit,
                      private val onGallerySelected: () -> Unit) : Fragment() {
    @SuppressLint("InflateParams")
    // 다이얼로그 레이아웃을 바인딩 객체로 변환
    val dialogBinding = DialogSelectImgBinding.inflate(LayoutInflater.from(context))

    fun show() {
        // 다이얼로그 만들기
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        // 기본 이미지 클릭 시 기본 이미지 선택 함수 부르기
        dialogBinding.btnDefault.setOnClickListener {
            onDefaultSelected()
            dialog.dismiss()
        }

        // 갤러리 버튼 클릭 시 갤러리 선택 함수 부르기
        dialogBinding.btnGallery.setOnClickListener {
            onGallerySelected()
            dialog.dismiss()
        }
        // 다이얼로그 보여주기
        dialog.show()
    }
}