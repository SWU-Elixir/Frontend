package com.example.elixir.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log // Log import 추가
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.R
import com.example.elixir.databinding.FragmentSignupFindPasswordBinding
import com.example.elixir.member.viewmodel.MemberViewModel
import com.example.elixir.RetrofitClient
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberViewModelFactory
import com.example.elixir.network.AppDatabase

class FindPasswordFragment :Fragment() {
    private var _binding: FragmentSignupFindPasswordBinding? = null
    private val binding get() = _binding!!
    private var email: String = ""
    private var pw: String = ""
    private var checkPw: String = ""

    // ViewModel 선언 추가
    private val memberViewModel: MemberViewModel by activityViewModels {
        val api = RetrofitClient.instanceMemberApi
        val db = AppDatabase.getInstance(requireContext()) // MemberDatabase 사용
        val dao = db.memberDao()
        MemberViewModelFactory(MemberRepository(api, dao)) // MemberService 제거
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupFindPasswordBinding.inflate(inflater, container, false)

        // 에러 메시지/아이콘은 기본적으로 숨김
        binding.errorEmail.visibility = View.GONE
        binding.errorPw.visibility = View.GONE
        binding.checkPw.visibility = View.GONE // 이전에 GONE으로 설정되었으나, 비밀번호 일치 확인을 위해 Visible로 변경될 예정
        binding.incorrectPw.visibility = View.GONE
        binding.textPw.visibility = View.GONE
        binding.registPw.visibility = View.GONE
        binding.textVerify.visibility = View.GONE
        binding.verify.visibility = View.GONE
        binding.checkVerify.visibility = View.GONE
        binding.registEmail.setCompoundDrawables(null, null, null, null)
        binding.registPw.setCompoundDrawables(null, null, null, null)
        binding.checkPw.setCompoundDrawables(null, null, null, null)

        // 비밀번호 관련 입력 초기 비활성화
        binding.verify.isEnabled = false
        binding.checkVerify.isEnabled = false
        binding.registPw.isEnabled = false // 초기 비밀번호 입력란 비활성화
        binding.checkPw.isEnabled = false // 초기 확인 비밀번호 입력란 비활성화

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이메일 입력 유효 여부 확인
        binding.registEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                email = s.toString().trim()
                if (isEmailValid(email)) {
                    binding.errorEmail.visibility = View.GONE
                    binding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                } else {
                    binding.errorEmail.visibility = View.VISIBLE
                    binding.registEmail.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                updateSelection(); // 이메일 유효성 검사 후 버튼 상태 업데이트
            }
        })

        // 인증번호 입력 유효 여부 확인 (이 부분은 유효성 검사 자체는 없으므로 버튼 활성화와만 관련)
        binding.verify.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 인증번호 입력값 필요시 변수에 저장
                // 인증번호 입력 시점에 따라 다음 버튼 상태를 업데이트해야 할 수도 있음 (현재는 비밀번호 단계에서만)
            }
        })

        // 이메일 인증 버튼 클릭
        binding.checkEmail.setOnClickListener {
            if (isEmailValid(email)) {
                memberViewModel.requestEmailVerification(email)
                binding.checkEmail.isEnabled = false // 중복 클릭 방지
                // 인증번호 관련 UI는 ViewModel 결과에 따라 보이도록 변경
            } else {
                Toast.makeText(requireContext(), "유효한 이메일 주소를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 이메일 인증 결과 관찰
        memberViewModel.emailVerificationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                if (it.status == 200) {
                    // 성공 시: 인증번호 입력란, 확인 버튼 활성화 및 UI 표시
                    binding.verify.isEnabled = true
                    binding.checkVerify.isEnabled = true
                    binding.textVerify.visibility = View.VISIBLE
                    binding.verify.visibility = View.VISIBLE
                    binding.checkVerify.visibility = View.VISIBLE

                    // 이메일 입력 고정 (수정 불가) 및 버튼 비활성화
                    binding.registEmail.isEnabled = false
                    binding.checkEmail.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_gray)
                } else {
                    // 실패 시: checkEmail 버튼 다시 활성화
                    binding.checkEmail.isEnabled = true
                    // 인증번호 입력 관련 UI 숨김 (성공하지 않았으므로)
                    binding.textVerify.visibility = View.GONE
                    binding.verify.visibility = View.GONE
                    binding.checkVerify.visibility = View.GONE
                }
            }
        }

        // 인증번호 확인 버튼 클릭
        binding.checkVerify.setOnClickListener {
            val code = binding.verify.text.toString().trim()
            if (code.isNotEmpty()) { // 인증번호가 비어있지 않은지 확인
                memberViewModel.verifyEmailCode(email, code)
                binding.checkVerify.isEnabled = false // 중복 클릭 방지
            } else {
                Toast.makeText(requireContext(), "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 인증번호 확인 결과 관찰
        memberViewModel.emailCodeVerifyResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                if (it.status == 200) {
                    // 성공 시: 비밀번호 입력란 보이기 및 활성화
                    binding.textPw.visibility = View.VISIBLE
                    binding.registPw.visibility = View.VISIBLE
                    binding.registPw.isEnabled = true
                    binding.checkPw.visibility = View.VISIBLE
                    binding.checkPw.isEnabled = true // 확인 비밀번호 입력란 활성화

                    // 인증번호 입력 고정 (수정 불가) 및 버튼 비활성화
                    binding.verify.isEnabled = false
                    binding.checkVerify.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_gray)
                } else {
                    // 실패 시: checkVerify 버튼 다시 활성화
                    binding.checkVerify.isEnabled = true
                    // 비밀번호 입력 관련 UI 숨김 (성공하지 않았으므로)
                    binding.textPw.visibility = View.GONE
                    binding.registPw.visibility = View.GONE
                    binding.checkPw.visibility = View.GONE
                    binding.registPw.isEnabled = false
                    binding.checkPw.isEnabled = false
                }
            }
        }

        // 비밀번호 입력 유효 여부 확인
        binding.registPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                pw = s.toString().trim()

                if(isPwValid(pw)) {
                    binding.errorPw.visibility = View.GONE
                    binding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                    binding.checkPw.visibility = View.VISIBLE // 비밀번호가 유효하면 확인 비밀번호 입력 필드 보이기
                    binding.checkPw.isEnabled = true // 확인 비밀번호 입력 필드 활성화
                } else {
                    binding.errorPw.visibility = View.VISIBLE
                    binding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                    binding.checkPw.visibility = View.GONE // 비밀번호가 유효하지 않으면 확인 비밀번호 입력 필드 숨기기
                    binding.checkPw.isEnabled = false // 확인 비밀번호 입력 필드 비활성화
                    binding.incorrectPw.visibility = View.GONE // 비밀번호가 유효하지 않으면 불일치 메시지 숨김
                }
                updateSelection();
            }
        })

        // 비밀번호 일치 확인
        binding.checkPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPw = s.toString().trim()

                if(incorrectPw(pw, checkPw)) {
                    binding.incorrectPw.visibility = View.GONE
                    binding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                } else {
                    binding.incorrectPw.visibility = View.VISIBLE
                    binding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                updateSelection();
            }
        })

        binding.btnNext.setOnClickListener{
            // 모든 유효성 검사를 통과했을 때만 비밀번호 업데이트 요청
            if (isEmailValid(email) && isPwValid(pw) && incorrectPw(pw, checkPw)) {
                memberViewModel.updatePassword(email, pw)
            } else {
                Toast.makeText(requireContext(), "모든 정보를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 비밀번호 업데이트 결과 관찰
        memberViewModel.passwordUpdateResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                activity?.finish() // 성공 시 화면 종료
            } else {
                Toast.makeText(requireContext(), "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                // 실패 시 다음으로 넘어가지 않음 (버튼 활성화 등 별도 처리 필요 없음, UI가 현재 상태 유지)
            }
        }

        // 에러 메시지 관찰 (ViewModel에서 발생한 에러를 토스트로 표시)
        memberViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e("FindPasswordFragment", "API 오류: $it") // 디버깅을 위한 로그 추가
            }
        }
    }

    // 이메일 유효 여부
    private fun isEmailValid(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // 비밀번호 유효 여부
    private fun isPwValid(pw: String): Boolean {
        // 특수 문자 정의: 대문자, 소문자, 숫자, 특수문자(.!@#$%)를 모두 포함하고 8자 이상 20자 이하
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.!@#$%])[A-Za-z\\d.!@#$%]{8,20}$")
        return regex.matches(pw)
    }

    // 비밀번호 일치 여부
    private fun incorrectPw(pw: String, checkPw: String): Boolean = pw == checkPw

    private fun updateSelection() {
        // 모든 조건이 유효할 때만 버튼 활성화
        // 이메일 유효성, 비밀번호 유효성, 비밀번호 일치 여부를 모두 확인
        val allValid = isEmailValid(email) && isPwValid(pw) && incorrectPw(pw, checkPw)
        binding.btnNext.isEnabled = allValid && binding.registPw.visibility == View.VISIBLE

        // 버튼 색상 변경 (활성화: 주황, 비활성화: 회색)
        binding.btnNext.setBackgroundTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                if (binding.btnNext.isEnabled) R.color.elixir_orange
                else R.color.elixir_gray
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}