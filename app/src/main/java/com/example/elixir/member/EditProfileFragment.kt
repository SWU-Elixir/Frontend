package com.example.elixir.member

import android.content.Intent
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
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentEditProfileBinding
import com.example.elixir.dialog.SelectImgDialog
import com.example.elixir.dialog.WithdrawalDialog
import com.example.elixir.signup.OnProfileCompletedListener
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class EditProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentEditProfileBinding
    var listener: OnProfileCompletedListener? = null

    private var title: String = ""
    private var profileImage: String = ""
    private var nickname: String = ""
    private var gender: String = ""
    private var birthYearInt: Int = 1990

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileBinding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 프로필 정보 로드
        loadMemberProfile()

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
        setTitleSpinner()

        // 저장 버튼 클릭 리스너
        profileBinding.btnEdit.setOnClickListener {
            saveProfile()
        }

        profileBinding.btnWithdrawal.setOnClickListener{
            WithdrawalDialog(requireActivity()).show()
        }

        profileBinding.btnSurvey.setOnClickListener {
            // ToolbarActivity로 이동
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 15)
                putExtra("title", "설문 조사 수정")
            }
            startActivity(intent)
        }
    }

    private fun loadMemberProfile() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getMember()
                if (response.status == 200) {
                    response.data?.let { member ->
                        // 프로필 정보 설정
                        profileImage = member.profileUrl ?: ""
                        nickname = member.nickname ?: ""
                        gender = member.gender ?: ""
                        birthYearInt = member.birthYear ?: 1990

                        // UI 업데이트
                        with(profileBinding) {
                            // 프로필 이미지 설정
                            Glide.with(requireContext())
                                .load(profileImage)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .circleCrop()
                                .into(registProfile)

                            // 닉네임 설정
                            registNick.setText(nickname)

                            // 성별 설정
                            when (gender) {
                                "female" -> selectGender.check(R.id.btn_female)
                                "male" -> selectGender.check(R.id.btn_male)
                                "other" -> selectGender.check(R.id.btn_other)
                                else -> selectGender.check(R.id.btn_selected_not)
                            }

                            // 생년월일 설정
                            birthYear.setSelection((1900..2025).toList().indexOf(birthYearInt))
                        }
                    }
                } else {
                    Log.e("EditProfileFragment", "회원 정보 로드 실패: ${response.message}")
                    Toast.makeText(requireContext(), "회원 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EditProfileFragment", "회원 정보 로드 실패", e)
                Toast.makeText(requireContext(), "회원 정보를 불러올 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi

                // dto 객체 생성
                val dtoMap = mapOf(
                    "title" to title, // 필요시 값 입력
                    "nickname" to nickname,
                    "gender" to gender,
                    "birthYear" to birthYearInt
                )
                val dtoJson = Gson().toJson(dtoMap)
                val dtoRequestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), dtoJson)

                val profileImagePart = if (profileImage.isNotBlank() && !profileImage.startsWith("http")) {
                    val uri = Uri.parse(profileImage)
                    val file = uriToFile(uri)
                    if (file != null) {
                        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
                    } else {
                        null
                    }
                } else {
                    null
                }

                val response = api.patchProfile(dtoRequestBody, profileImagePart)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "프로필이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                } else {
                    Toast.makeText(requireContext(), "프로필 수정에 실패했습니다: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("EditProfileFragment", "프로필 수정 실패", e)
                Toast.makeText(requireContext(), "프로필 수정에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val savedBirthYear =  if (birthYearInt != 0) birthYearInt.toString() else "1990"
        profileBinding.birthYear.setSelection(birthYears.indexOf(savedBirthYear))
        
        // 선택 리스너 : Int로 변환해서 저장
        profileBinding.birthYear.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                birthYearInt = parent.getItemAtPosition(position).toString().toInt()
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

    private fun setTitleSpinner() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getTitle()
                if (response.status == 200) {
                    val titleList = response.data?.titles ?: emptyList()
                    if (titleList.isEmpty()) {
                        val emptyList = listOf("칭호가 없습니다")
                        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_year, emptyList)
                        profileBinding.title.adapter = adapter
                        profileBinding.title.isEnabled = false
                        title = ""
                    } else {
                        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_year, titleList)
                        profileBinding.title.adapter = adapter
                        profileBinding.title.isEnabled = true
                        // 선택 리스너
                        profileBinding.title.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                title = parent.getItemAtPosition(position).toString()
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "타이틀 정보를 불러올 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 모든 변수에 유효한 값이 들어왔는지 확인
    fun checkAllValid() {
        if (profileImage.isNotBlank() && nickname.isNotBlank() && gender.isNotBlank() && birthYearInt != 0) {
            listener?.onProfileCompleted(profileImage, nickname, gender, birthYearInt)
        } else {
            listener?.onProfileInvalid()
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("profile", ".jpg", requireContext().cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}