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
import com.example.elixir.R
import com.example.elixir.databinding.FragmentSignupFindPasswordBinding
import com.example.elixir.member.viewmodel.MemberViewModel
import com.example.elixir.RetrofitClient
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody


class FindPasswordFragment :Fragment() {
    private var _binding: FragmentSignupFindPasswordBinding? = null
    private val binding get() = _binding!!
    private var email: String = ""
    private var pw: String = ""
    private var checkPw: String = ""

    // ViewModel 선언 추가
    private val memberViewModel: MemberViewModel by activityViewModels {
        val api = RetrofitClient.instanceMemberApi
        val db = MemberDB.getInstance(requireContext())
        val dao = db.memberDao()
        MemberViewModelFactory(MemberService(MemberRepository(api, dao)))
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
        binding.checkPw.visibility = View.GONE
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
                updateSelection();
            }
        })

        // 인증번호 입력 유효 여부 확인 (이 부분도 updateSelection() 필요 없음)
        binding.verify.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 인증번호 입력값 필요시 변수에 저장
            }
        })

        // 이메일 인증 버튼 클릭
        binding.checkEmail.setOnClickListener {
            if (isEmailValid(email)) {
                memberViewModel.requestEmailVerification(email)
                binding.checkEmail.isEnabled = false // 중복 클릭 방지
                binding.textVerify.visibility = View.VISIBLE
                binding.verify.visibility = View.VISIBLE
                binding.checkVerify.visibility = View.VISIBLE
            } else {
            }
        }

        // 이메일 인증 결과 관찰
        memberViewModel.emailVerificationResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                if (it.status == 200) { // 성공적으로 200 응답 받았다고 가정
                    // 인증번호 입력란, 확인 버튼 활성화
                    binding.verify.isEnabled = true
                    binding.checkVerify.isEnabled = true
                    // 이메일 인증 성공 시 인증번호 입력 UI가 보여야 하므로 아래 줄들은 제거하거나 VISIBLE로 변경
                    binding.textVerify.visibility = View.VISIBLE // GONE -> VISIBLE로 변경
                    binding.verify.visibility = View.VISIBLE     // GONE -> VISIBLE로 변경
                    binding.checkVerify.visibility = View.VISIBLE// GONE -> VISIBLE로 변경

                    // 이메일 입력 고정 (수정 불가)
                    binding.registEmail.isEnabled = false
                    binding.checkEmail.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_gray)
                } else {
                    binding.checkEmail.isEnabled = true // 실패 시 다시 활성화
                }
            }
        }

        // 인증번호 확인 버튼 클릭
        binding.checkVerify.setOnClickListener {
            val code = binding.verify.text.toString().trim()
            memberViewModel.verifyEmailCode(email, code)
            binding.checkVerify.isEnabled = false // 중복 클릭 방지
        }

        // 인증번호 확인 결과 관찰
        memberViewModel.emailCodeVerifyResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                if (it.status == 200) {
                    // 비밀번호 입력란 보이기 및 활성화
                    binding.textPw.visibility = View.VISIBLE
                    binding.registPw.visibility = View.VISIBLE
                    binding.registPw.isEnabled = true

                    // 인증번호 입력 고정 (수정 불가)
                    binding.verify.isEnabled = false
                    binding.checkVerify.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_gray)
                } else {
                    binding.checkVerify.isEnabled = true // 실패 시 다시 활성화
                }
            }
        }

        // 비밀번호 입력 유효 여부 확인
        binding.registPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 비밀번호 입력 받기
                pw = s.toString().trim()

                // 만약 유효하다면 체크 표시 띄워주기
                if(isPwValid(pw)) {
                    binding.errorPw.visibility = View.GONE
                    binding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                    binding.checkPw.visibility = View.VISIBLE
                }
                // 만약 유효하지 않다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    binding.errorPw.visibility = View.VISIBLE
                    binding.registPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                updateSelection();
            }
        })



        // 비밀번호 일치 확인
        binding.checkPw.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 비밀번호 입력 받기
                checkPw = s.toString().trim()

                // 만약 일치한다면 체크 표시 띄워주기
                if(incorrectPw(pw, checkPw)) {
                    binding.incorrectPw.visibility = View.GONE
                    binding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_check), null)
                }
                // 만약 일치하지 않는다면 X 표시와 함께 에러 메세지 띄워주기
                else {
                    binding.incorrectPw.visibility = View.VISIBLE
                    binding.checkPw.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_not), null)
                }
                updateSelection();
            }
        })

        binding.btnNext.setOnClickListener{
            // 1. JSON body 생성
            val json = """{"email":"$email","newPassword":"$pw"}"""
            val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)

            // 2. Retrofit 호출 (코루틴 사용)
            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.instanceMemberApi
                    val response = api.putUpdatePassword(requestBody)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        // 비밀번호 변경 성공 후 이동/처리
                    } else {
                        Toast.makeText(requireContext(), "비밀번호 변경 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            activity?.finish()
        }
    }

    // 이메일 유효 여부
    private fun isEmailValid(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // 비밀번호 유효 여부
    private fun isPwValid(pw: String): Boolean {
        // 특수 문자 정의
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.!@#$%])[A-Za-z\\d.!@#$%]{8,20}$")
        return regex.matches(pw)
        binding.checkPw.visibility = View.VISIBLE
        binding.checkPw.isEnabled = true
    }

    // 비밀번호 일치 여부
    private fun incorrectPw(pw: String, checkPw: String): Boolean = pw == checkPw

    private fun updateSelection() {
        // 다 유효한 상태일 떄만 버튼 활성화
        val allValid = isEmailValid(email) && isPwValid(pw) && incorrectPw(pw, checkPw)
        binding.btnNext.isEnabled = allValid

        // 버튼 색상 변경 (활성화: 주황, 비활성화: 회색)
        binding.btnNext.setBackgroundTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                if (allValid) R.color.elixir_orange
                else R.color.elixir_gray
            )
        )
    }
}