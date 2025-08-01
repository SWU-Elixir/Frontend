package com.example.elixir.signup

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.databinding.FragmentSettingProfileBinding
import com.google.gson.Gson

class SettingProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentSettingProfileBinding
    private val userModel: UserInfoViewModel by activityViewModels()
    var listener: OnProfileCompletedListener? = null

    private var profileImage: String = ""
    private var nickname: String = ""
    private var gender: String? = null
    private var birthYear: Int = 1990

    companion object {
        private const val ARG_PROFILE_DATA_JSON = "profileData"
        private const val ARG_EMAIL = "email"

        fun newInstance(profileDataJson: String?, email: String?): SettingProfileFragment {
            val fragment = SettingProfileFragment()
            val args = Bundle()

            if (profileDataJson != null) {
                args.putString(ARG_PROFILE_DATA_JSON, profileDataJson)
            }
            if (email != null) {
                args.putString(ARG_EMAIL, email)
            }
            fragment.arguments = args
            return fragment
        }
    }

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

        val profileDataJson = arguments?.getString("profileData")
        val email = arguments?.getString("email")

        // 소셜 회원가입 중이라 이메일 값이 존재한다면, 이메일 값 저장
        if(!email.isNullOrBlank())
            userModel.setEmail(email)

        // 초기화 : 뷰 모델에 저장된 데이터가 있다면 불러오기
        val data = userModel.getProfile()
        Log.d("Signup", "저장된 데이터: $data")
        if(data != null) {
            // 클래스 속성에 데이터 값 집어넣기
            profileImage = data.profileImage
            nickname = data.nickname
            gender = data.gender
            birthYear = data.birthYear

            setInitialUI()
        } else if(!profileDataJson.isNullOrBlank()) {
            val profileData = Gson().fromJson(profileDataJson, ProfileData::class.java)
            // 클래스 속성에 데이터 값 집어넣기
            profileImage = profileData.profileImage
            nickname = profileData.nickname
            gender = profileData.gender
            birthYear = profileData.birthYear
            Log.d("Signup", "이미지: $profileImage")
            Log.d("Signup", "닉네임: $nickname")
            Log.d("Signup", "성별: $gender")
            Log.d("Signup", "출생년도: $birthYear")

            userModel.setProfile(profileImage, nickname, gender, birthYear)

            setInitialUI()
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
                R.id.btn_selected_not -> gender = null
            }
            checkAllValid()
        }

        // 생년월일 드롭다운 메뉴
        selectBirthYear()
    }

    // 초기 설정: 저장한 이미지, 닉네임, 성별을 ui에 반영
    private fun setInitialUI() {
        with(profileBinding) {
            Glide.with(this@SettingProfileFragment)
                .load(profileImage)
                .error(R.drawable.img_blank)
                .placeholder(R.drawable.img_blank)
                .into(registProfile)

            registNick.setText(nickname)

            when (gender) {
                "female" -> selectGender.check(R.id.btn_female)
                "male" -> selectGender.check(R.id.btn_male)
                "other" -> selectGender.check(R.id.btn_other)
                else -> selectGender.check(R.id.btn_selected_not)
            }
        }
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
            // 커스텀 다이얼로그 띄우기: 기본 이미지 / 갤러리
            SelectImgDialog(requireContext(),
                {
                    val uri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.img_blank}")
                    profileImage = uri.toString()
                    Glide.with(this@SettingProfileFragment)
                        .load(profileImage)
                        .error(R.drawable.img_blank)
                        .placeholder(R.drawable.img_blank)
                        .into(profileBinding.registProfile)
                    checkAllValid()
                },
                { imgSelector.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ).show()
        }
    }

    // 모든 변수에 유효한 값이 들어왔는지 확인
    fun checkAllValid() {
        if (profileImage.isNotBlank() && nickname.isNotBlank() && birthYear != 0) {
            Log.d("Signup", "이미지: $profileImage")
            Log.d("Signup", "닉네임: $nickname")
            Log.d("Signup", "성별: $gender")
            Log.d("Signup", "출생년도: $birthYear")
            userModel.setProfile(profileImage, nickname, gender, birthYear)
            listener?.onProfileCompleted(profileImage, nickname, gender, birthYear)
        } else {
            listener?.onProfileInvalid()
        }
    }
}