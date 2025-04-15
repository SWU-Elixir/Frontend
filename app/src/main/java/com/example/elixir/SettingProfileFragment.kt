package com.example.elixir

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import com.example.elixir.databinding.FragmentSettingProfileBinding

class SettingProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentSettingProfileBinding
    var listener: OnProfileCompletedListener? = null
    
    private lateinit var img: String
    private lateinit var nick: String
    private lateinit var sex: String
    private var birthYear: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        profileBinding = FragmentSettingProfileBinding.inflate(inflater, container, false)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        img = ""
        nick = ""
        sex = ""

        // 프로필 이미지 선택
        selectImg()

        // 별명 입력
        profileBinding.registNick.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                nick = s.toString().trim()
                checkAllValid()
            }

        })

        // 성별 선택
        profileBinding.selectSex.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.btn_female -> sex = R.string.female.toString()
                R.id.btn_male -> sex = R.string.male.toString()
                R.id.btn_other -> sex = R.string.other.toString()
                R.id.btn_selected_not -> sex = R.string.selected_not.toString()
            }
            checkAllValid()
        }

        // 생년월일 드롭다운 메뉴
        selectBirthYear()
    }

    // 출생년도 스피너 드롭다운 메뉴 보여주기
    private fun selectBirthYear() {
        // 생년 리스트 생성(1900 ~ 2025)
        val birthYears = (1900..2025).map { it.toString() }

        // 어댑터 설정
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_year, birthYears)
        profileBinding.birthYear.adapter = adapter

        // 초기값 (1990년)
        profileBinding.birthYear.setSelection(birthYears.indexOf("1990"))
        birthYear = 1990

        // 선택 리스너 : Int로 변환해서 저장
        profileBinding.birthYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                birthYear = parent.getItemAtPosition(position).toString().toInt()
                checkAllValid()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // 프로필 사진 갤러리에서 가져오기
    private fun selectImg() {
        // 이미지 피커 정의
        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileBinding.registProfile.setImageURI(it)
                img = it.toString()
                checkAllValid()
            }
        }

        // 이미지 갤러리에서 가져오기
        profileBinding.registProfile.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }
    fun checkAllValid() {
        Log.d("SettingProfileFragment", "nick=$nick, img=$img, sex=$sex, birthYear=$birthYear")
        if (img.isNotBlank() && nick.isNotBlank() && sex.isNotBlank() && birthYear != 0) {
            listener?.onProfileCompleted(img, nick, sex, birthYear)
        } else {
            listener?.onProfileInvalid()
        }
    }
}