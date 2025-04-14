package com.example.elixir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.activityViewModels
import com.example.elixir.databinding.FragmentCreateAccountBinding

class CreateAccountFragment : Fragment() {
    private lateinit var accountBinding: FragmentCreateAccountBinding
    private lateinit var email: String
    private lateinit var pw: String
    private lateinit var checkPw: String
    private val userModel: UserInfoViewModel by activityViewModels()

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
        accountBinding.registEmail.setCompoundDrawables(null, null, null, null)
        accountBinding.registPw.setCompoundDrawables(null, null, null, null)
        accountBinding.checkPw.setCompoundDrawables(null, null, null, null)

        return accountBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이메일, 비번 받아오기
        email = accountBinding.registEmail.text.toString().trim()
        pw = accountBinding.registPw.text.toString().trim()
        checkPw = accountBinding.checkPw.text.toString().trim()

        // 로그인 버튼 클릭 시 로그인 액티비티로
        accountBinding.btnLogin.setOnClickListener {
            activity?.finish()
        }

        // 이메일 입력 유효 여부 확인
        accountBinding.registEmail.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 이메일 입력 받기
                email = s.toString().trim()

                // 만약 유효하다면 체크 표시 띄워주기
                if (isEmailValid(email)) {
                    accountBinding.errorEmail.visibility = View.GONE
                    accountBinding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getDrawable(requireContext(), R.drawable.ic_check), null)
                }
                // 만약 유효하지 않다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    accountBinding.errorEmail.visibility = View.VISIBLE
                    accountBinding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                // 다음 버튼 활성화 여부
                updateNextButton()
            }
        })

        // 비밀번호 입력 유효 여부 확인
        accountBinding.registPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 비밀번호 입력 받기
                pw = s.toString().trim()
                // 만약 유효하다면 체크 표시 띄워주기
                if(isPwValid(pw)) {
                    accountBinding.errorPw.visibility = View.GONE
                    accountBinding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getDrawable(requireContext(), R.drawable.ic_check), null)
                    accountBinding.checkPw.visibility = View.VISIBLE
                }
                // 만약 유효하지 않다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    accountBinding.errorPw.visibility = View.VISIBLE
                    accountBinding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                // 다음 버튼 활성화 여부
                updateNextButton()
            }
        })

        // 비밀번호 일치 확인
        accountBinding.checkPw.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                // 비밀번호 입력 받기
                checkPw = s.toString().trim()

                // 만약 일치한다면 체크 표시 띄워주기
                if(incorrectPw(pw, checkPw)) {
                    accountBinding.incorrectPw.visibility = View.GONE
                    accountBinding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getDrawable(requireContext(), R.drawable.ic_check), null)
                }
                // 만약 일치하지 않는다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    accountBinding.incorrectPw.visibility = View.VISIBLE
                    accountBinding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                // 다음 버튼 활성화 여부
                updateNextButton()
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
        }
    }

    // 이메일 유효 여부
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 비밀번호 유효 여부
    private fun isPwValid(pw: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.!@#$%])[A-Za-z\\d.!@#$%]{8,20}$")
        return regex.matches(pw)
    }

    // 비밀번호 일치 여부
    private fun incorrectPw(pw: String, checkPw: String): Boolean {
        return pw == checkPw
    }

    // 버튼 상태 갱신 함수
    private fun updateNextButton() {
        // 다 유효한 상태일 떄만 버튼 활성화
        val allValid = isEmailValid(email) && isPwValid(pw) && incorrectPw(pw, checkPw)
        accountBinding.btnNext.isEnabled = allValid

        // 버튼 색상 변경 (활성화: 주황, 비활성화: 회색)
        accountBinding.btnNext.setBackgroundTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                if (allValid)
                    R.color.elixir_orange
                else
                    R.color.elixir_gray
            )
        )
    }
}