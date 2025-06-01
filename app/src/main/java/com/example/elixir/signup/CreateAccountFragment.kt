package com.example.elixir.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.elixir.dialog.AlertExitDialog
import com.example.elixir.R
import com.example.elixir.databinding.FragmentCreateAccountBinding
import com.example.elixir.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class CreateAccountFragment : Fragment() {
    private lateinit var accountBinding: FragmentCreateAccountBinding
    private val userModel: UserInfoViewModel by activityViewModels()
    private var email: String = ""
    private var pw: String = ""
    private var checkPw: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        accountBinding = FragmentCreateAccountBinding.inflate(inflater, container, false)

        // 에러 메시지/아이콘은 기본적으로 숨김
        accountBinding.errorEmail.visibility = View.GONE
        accountBinding.errorPw.visibility = View.GONE
        accountBinding.checkPw.visibility = View.GONE
        accountBinding.incorrectPw.visibility = View.GONE
        accountBinding.textPw.visibility = View.GONE
        accountBinding.registPw.visibility = View.GONE
        accountBinding.registEmail.setCompoundDrawables(null, null, null, null)
        accountBinding.registPw.setCompoundDrawables(null, null, null, null)
        accountBinding.checkPw.setCompoundDrawables(null, null, null, null)

        return accountBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기화 : 뷰 모델에 저장된 데이터가 있다면 불러오기
        val data = userModel.getAccount()
        if(data != null) {
            // 클래스 속성에 데이터 값 집어넣기
            email = data.id
            pw = data.password

            with(accountBinding) {
                // 설정: 이미지 링크 파싱, 닉네임에 작성한 닉네임 불러오기, 선택한 성별에 따라 버튼 누르게
                registEmail.setText(email)
                registPw.setText(pw)
            }
        }

        // 로그인 버튼 클릭 시 다이얼로그 띄우기
        accountBinding.btnLogin.setOnClickListener {
            activity?.let { it1 -> AlertExitDialog(it1).show() }
        }

        // 이메일 입력 유효 여부 확인
        accountBinding.registEmail.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 이메일 입력 받기
                email = s.toString().trim()

                // 만약 유효하다면 체크 표시 띄워주기
                if (isEmailValid(email)) {
                    accountBinding.errorEmail.visibility = View.GONE
                    accountBinding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                }
                // 만약 유효하지 않다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    accountBinding.errorEmail.visibility = View.VISIBLE
                    accountBinding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                // 상태 갱신
                updateSelection()
            }
        })

        // 비밀번호 입력 유효 여부 확인
        accountBinding.registPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 비밀번호 입력 받기
                pw = s.toString().trim()

                // 만약 유효하다면 체크 표시 띄워주기
                if(isPwValid(pw)) {
                    accountBinding.errorPw.visibility = View.GONE
                    accountBinding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                    accountBinding.checkPw.visibility = View.VISIBLE
                }
                // 만약 유효하지 않다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    accountBinding.errorPw.visibility = View.VISIBLE
                    accountBinding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                // 상태 갱신
                updateSelection()
            }
        })

        // 비밀번호 일치 확인
        accountBinding.checkPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 비밀번호 입력 받기
                checkPw = s.toString().trim()

                // 만약 일치한다면 체크 표시 띄워주기
                if(incorrectPw(pw, checkPw)) {
                    accountBinding.incorrectPw.visibility = View.GONE
                    accountBinding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                }
                // 만약 일치하지 않는다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    accountBinding.incorrectPw.visibility = View.VISIBLE
                    accountBinding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                // 상태 갱신
                updateSelection()
            }
        })

        // 다음 버튼 클릭 시 입력한 이메일과 비밀번호는 뷰모델에 저장하고 프로필 설정 페이지로 넘어감
        accountBinding.btnNext.setOnClickListener {
            // 뷰모델에 저장
            userModel.setAccount(email, pw)

            // 프래그먼트 프레임 교체
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_registration, SignupFragment())
                .addToBackStack(null)
                .commit()
        }

        // 이메일 확인
        accountBinding.checkEmail.setOnClickListener {
            val email = accountBinding.registEmail.text.toString()

            if (!isEmailValid(email)) {
                Toast.makeText(requireContext(), "유효한 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.instancePublicApi
                    val response = api.getCheckEmail(email) // GET 방식: @Query 사용 가정

                    Log.d("Email", email)
                    if (response.status == 200 && !response.data) {
                        // 이메일 사용 가능
                        accountBinding.errorEmail.visibility = View.GONE
                        accountBinding.errorEmail.text = ""
                        accountBinding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                        Toast.makeText(requireContext(), "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show()

                        updateSelection()

                        // 추가 UI 설정
                        accountBinding.textPw.visibility = View.VISIBLE
                        accountBinding.registPw.visibility = View.VISIBLE
                        accountBinding.checkEmail.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_gray)
                        accountBinding.checkEmail.isEnabled = false
                        accountBinding.registEmail.isEnabled = false

                    } else {
                        // 이메일 중복
                        accountBinding.errorEmail.visibility = View.VISIBLE
                        accountBinding.errorEmail.text = "이미 사용중인 이메일입니다"
                        accountBinding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                        Toast.makeText(requireContext(), "이미 사용중인 이메일입니다.", Toast.LENGTH_SHORT).show()
                        updateSelection()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "이메일 확인 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 이메일 유효 여부
    private fun isEmailValid(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // 비밀번호 유효 여부
    private fun isPwValid(pw: String): Boolean {
        // 특수 문자 정의
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.!@#$%])[A-Za-z\\d.!@#$%]{8,20}$")
        return regex.matches(pw)
    }

    // 비밀번호 일치 여부
    private fun incorrectPw(pw: String, checkPw: String): Boolean = pw == checkPw

    // 작업 상태 갱신 & 값 저장 함수
    private fun updateSelection() {
        // 다 유효한 상태일 때만 버튼 활성화
        val allValid = isEmailValid(email) && isPwValid(pw) && incorrectPw(pw, checkPw) && 
                      accountBinding.errorEmail.visibility == View.GONE && 
                      accountBinding.errorEmail.text.toString().isEmpty()
        accountBinding.btnNext.isEnabled = allValid

        // 버튼 색상 변경 (활성화: 주황, 비활성화: 회색)
        accountBinding.btnNext.setBackgroundTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                if (allValid) R.color.elixir_orange
                else R.color.elixir_gray
            )
        )
    }
}