package com.example.elixir.signup

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.R
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.databinding.FragmentSettingProfileBinding

class SettingProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentSettingProfileBinding
    private val userModel: UserInfoViewModel by activityViewModels()
    var listener: OnProfileCompletedListener? = null

    private var profileImage: String = ""
    private var nickname: String = ""
    private var gender: String = ""
    private var birthYear: Int = 1990

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

        // 초기화 : 뷰 모델에 저장된 데이터가 있다면 불러오기
        val data = userModel.getProfile()
        if(data != null) {
            // 클래스 속성에 데이터 값 집어넣기
            profileImage = data.profileImage
            nickname = data.nickname
            gender = data.gender
            birthYear = data.birthYear

            with(profileBinding) {
                // 설정: 이미지 링크 파싱, 닉네임에 작성한 닉네임 불러오기, 선택한 성별에 따라 버튼 누르게
                registProfile.setImageURI(Uri.parse(profileImage))
                registNick.setText(nickname)

                when (gender) {
                    R.string.female.toString() -> selectGender.check(R.id.btn_female)
                    R.string.male.toString() -> selectGender.check(R.id.btn_male)
                    R.string.other.toString() -> selectGender.check(R.id.btn_other)
                    R.string.selected_not.toString() -> selectGender.check(R.id.btn_selected_not)
                }
            }
        }

        // 프로필 이미지 선택
        setImg()

        // 별명 입력
        profileBinding.registNick.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                nickname = s.toString().trim()
                checkAllValid()
            }
        })

        // 라디오 버튼 : 성별 선택
        profileBinding.selectGender.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.btn_female -> gender = "female"
                R.id.btn_male -> gender = "male"
                R.id.btn_other -> gender = "other"
                R.id.btn_selected_not -> gender = "not_selected"
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

        // 저장된 생년 가져오기 (없으면 기본값 1990)
        val savedBirthYear =  if (birthYear != 0) birthYear.toString() else "1990"
        profileBinding.birthYear.setSelection(birthYears.indexOf(savedBirthYear))
        
        // 선택 리스너 : Int로 변환해서 저장
        profileBinding.birthYear.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                birthYear = parent.getItemAtPosition(position).toString().toInt()
                checkAllValid()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // 이미지 설정
    private fun setImg() {
        // 이미지 피커 선언 (PickVisualMedia)
        val imgSelector = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            // uri를 string으로 형변환 후 저장
            uri?.let {
                profileBinding.registProfile.setImageURI(uri)
                profileImage = uri.toString()
                checkAllValid()
            }
        }
        // 프로필 이미지를 눌렀을 때,
        profileBinding.registProfile.setOnClickListener {
            // 커스텀 다이얼로그 띄우기
            SelectImgDialog(requireContext(),
                {
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
                    profileBinding.registProfile.setImageURI(uri)
                    profileImage = uri.toString()
                    checkAllValid()
                },
                { imgSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ).show()
        }
    }

    // 모든 변수에 유효한 값이 들어왔는지 확인
    fun checkAllValid() {
        if (profileImage.isNotBlank() && nickname.isNotBlank() && gender.isNotBlank() && birthYear != 0) {
            listener?.onProfileCompleted(profileImage, nickname, gender, birthYear)
        } else {
            listener?.onProfileInvalid()
        }
    }
}