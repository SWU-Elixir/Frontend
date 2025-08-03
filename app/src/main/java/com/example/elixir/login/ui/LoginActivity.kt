package com.example.elixir.login.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.elixir.BuildConfig
import com.example.elixir.HomeActivity
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.ActivityLoginBinding
import com.example.elixir.login.data.LoginRequest
import com.example.elixir.login.data.LoginResponse
import com.example.elixir.login.network.GoogleSignInHelper
import com.example.elixir.member.network.GoogleSignupResponse
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberViewModel
import com.example.elixir.member.viewmodel.MemberViewModelFactory
import com.example.elixir.signup.ProfileData
import com.example.elixir.signup.UserInfoViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.gson.Gson
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import java.security.MessageDigest
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    // 선언부
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private val memberViewModel: MemberViewModel by viewModels {
        val api = RetrofitClient.instanceMemberApi
        val db = MemberDB.getInstance(this@LoginActivity)
        val dao = db.memberDao()
        MemberViewModelFactory(
            MemberRepository(api, dao)
        )
    }
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private val userModel: UserInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        installSplashScreen()

        // 앱 시작 시 저장된 토큰 불러와서 RetrofitClient에 세팅
        loadToken()?.let { token ->
            RetrofitClient.setAuthToken(token)
        }

        printKeyHash()
        printSha1Fingerprint()


        // 초기화
        // 바인딩 정의
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        googleSignInHelper = GoogleSignInHelper(this@LoginActivity)

        // 에러 메시지는 기본적으로 숨김
        loginBinding.errorLogin.visibility = View.GONE

        // 구글 로그인 성공 시
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val authCode = account.serverAuthCode
                    Log.d("OAuth", "인증 코드(authCode): $authCode")

                    getAccessTokenFromAuthCode(authCode)
                } catch (e: ApiException) {
                    Log.e("OAuth", "구글 로그인 실패: ", e)
                }
            } else {
                Log.e("OAuth", "구글 로그인 취소 또는 에러(resultCode=${result.resultCode})")
            }
        }

        memberViewModel.socialLoginResult.observe(this) { result ->
            result.onSuccess { data ->
                // 미등록 회원이라면 회원가입 페이지로 넘어감
                if(!data.registered) {
                    // 받아온 데이터
                    val profileData = data.socialUserInfo
                    val image = if(profileData.profileImage.isNullOrBlank())
                                    "android.resource://${this.packageName}/${R.drawable.ic_profile}"
                                else profileData.profileImage!!
                    val gender = profileData.gender
                    val birthYear = if(profileData.birthYear == null) 1990 else profileData.birthYear!!
                    val nickname = if(profileData.nickname.isNullOrBlank()) profileData.email else profileData.nickname!!

                    userModel.setLoginType(data.loginType)
                    userModel.setEmail(profileData.email)

                    val signupIntent = Intent(this, ToolbarActivity::class.java).apply {
                        putExtra("mode", 1)
                        putExtra("loginType", data.loginType)
                        putExtra("email", profileData.email)
                        putExtra("profileData", Gson().toJson(ProfileData(image, nickname,gender,birthYear)))
                    }

                    startActivity(signupIntent)
                }
                // 등록 회원이면 로그인
                else {
                    // 소셜 로그인 시 발급받은 토큰 저장
                    memberViewModel.socialLoginResult.observe(this) { result ->
                        if(result.isSuccess){
                            val socialLoginData = result.getOrNull() // SocialLoginData?
                            socialLoginData?.let {
                                val token = it.accessToken
                                RetrofitClient.setAuthToken(token)
                                saveToken(token)
                            }
                        } else {
                            // 에러 처리
                            Log.e("LoginActivity", result.exceptionOrNull().toString())
                        }
                    }
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }


        // 로그인 버튼 클릭
        loginBinding.btnLogin.setOnClickListener {
            val email = loginBinding.enterEmail.text.toString()
            val password = loginBinding.enterPw.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                loginBinding.errorLogin.text = "이메일과 비밀번호를 입력해주세요."
                loginBinding.errorLogin.visibility = View.VISIBLE
            }
        }

        // 회원가입 버튼을 누르면 등록 액티비티로
        loginBinding.btnSignup.setOnClickListener {
            val intent = Intent(this, ToolbarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("mode", 1)
            startActivity(intent)
        }

        loginBinding.findPw.setOnClickListener {
            val intent = Intent(this, ToolbarActivity::class.java).apply {
                putExtra("mode", 12)
            }
            startActivity(intent)
        }

        // 카카오 로그인
        loginBinding.btnLoginKakao.setOnClickListener {
            //kakaoLogout(this)
            kakaoLogin(this)
        }

        loginBinding.btnLoginGoogle.setOnClickListener {
            // 1. GoogleSignInOptions 빌드 (Auth Code 요청 + Scope 지정)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(BuildConfig.GOOGLE_CLIENT_KEY) // Web Client ID
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/drive.readonly"))    // Drive 예시, 필요 scope로 변경
                .build()

            // 2. GoogleSignInClient 생성
            val googleSignInClient = GoogleSignIn.getClient(this, gso)

            // 3. Sign-in Intent 실행
            signInLauncher.launch(googleSignInClient.signInIntent)
        }

        loginBinding.btnLoginNaver.setOnClickListener {
            NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
                override fun onSuccess() {
                    // 로그인 성공 시 처리
                    val accessToken = NaverIdLoginSDK.getAccessToken()
                    Log.d("LoginActivity", "네이버 토큰: $accessToken")
                    memberViewModel.socialLogin("NAVER", accessToken!!)
                }

                override fun onFailure(httpStatus: Int, message: String) {
                    Log.d("LoginActivity", "네이버 로그인 실패. 상태: $httpStatus, $message")
                }

                override fun onError(errorCode: Int, message: String) {
                    Log.d("LoginActivity", "네이버 로그인 에러. 상태: $errorCode, $message")
                }
            })
        }
    }

    // 자동 로그인 저장
    private fun saveAutoLogin(email: String, password: String) {
        val prefs = getSharedPreferences("autoLoginPrefs", MODE_PRIVATE)
        prefs.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    // 로그인 성공 시
    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        RetrofitClient.instance.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: retrofit2.Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("LoginActivity", "API Response: $result")

                    if (result?.status == 200) {
                        // 토큰 저장 - data.accessToken으로 접근
                        result.data?.accessToken?.let { token ->
                            Log.d("LoginActivity", "Access Token: $token")
                            RetrofitClient.setAuthToken(token)
                            saveToken(token)

                            // 저장 확인
                            val savedToken = loadToken()
                            Log.d("LoginActivity", "Saved Token: $savedToken")
                        } ?: run {
                            Log.e("LoginActivity", "Access token is null")
                        }

                        result.data?.refreshToken?.let { refreshToken ->
                            saveRefreshToken(refreshToken)
                            Log.d("LoginActivity", "Refresh Token saved")
                        }

                        // 자동 로그인 정보 저장
                        saveAutoLogin(email, password)
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // 로그인 실패 시 토큰 제거
                        clearTokens()
                        loginBinding.errorLogin.text = result?.message ?: "로그인에 실패했습니다."
                        loginBinding.errorLogin.visibility = View.VISIBLE
                    }
                } else {
                    // 로그인 실패 시 토큰 제거
                    clearTokens()
                    // 401 오류 처리
                    if (response.code() == 401) {
                        loginBinding.errorLogin.text = "이메일 또는 비밀번호가 일치하지 않습니다."
                    } else {
                        loginBinding.errorLogin.text = "서버 오류가 발생했습니다."
                    }
                    loginBinding.errorLogin.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // 네트워크 오류 시에도 토큰 제거
                clearTokens()
                Log.e("LoginActivity", "Network error: ${t.message}")
                loginBinding.errorLogin.text = "네트워크 오류가 발생했습니다."
                loginBinding.errorLogin.visibility = View.VISIBLE
            }
        })
    }

    // Refresh Token 저장 메서드 추가
    private fun saveRefreshToken(refreshToken: String) {
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        prefs.edit().putString("refreshToken", refreshToken).apply()
    }

    // 토큰을 SharedPreferences에 저장
    private fun saveToken(token: String) {
        Log.d("LoginActivity", "Saving token: $token")
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        val success = prefs.edit()
            .putString("accessToken", token)
            .commit() // apply() 대신 commit()으로 즉시 저장 확인
        Log.d("LoginActivity", "Token save success: $success")
    }

    private fun loadToken(): String? {
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)
        Log.d("LoginActivity", "Loaded token: $token")
        return token
    }

    // 토큰 제거 함수 추가
    private fun clearTokens() {
        // RetrofitClient의 토큰 제거
        RetrofitClient.setAuthToken(null)
        
        // SharedPreferences에서 토큰 제거
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        prefs.edit()
            .remove("accessToken")
            .remove("refreshToken")
            .apply()
    }

    // 로그인 버튼 클릭 시 호출
    private fun kakaoLogin(context: Context) {
        val loginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "로그인 실패: ${error.localizedMessage}")
                Toast.makeText(context, "로그인 실패: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                Log.d("KakaoLogin", "로그인 성공, 토큰 발급됨: ${token.accessToken}")
                fetchKakaoUserInfo(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = loginCallback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = loginCallback)
        }
    }

    private fun printKeyHash() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", "앱에서 사용하는 키 해시: $keyHash")
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "키 해시 생성 실패", e)
        }
    }

    private fun fetchKakaoUserInfo(token: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoLogin", "사용자 정보 요청 실패: ${error.localizedMessage}")
            } else if (user != null) {
                Log.d("KakaoLogin", "사용자 정보: ${user.kakaoAccount?.email}")
                // 동의하지 않은 항목 확인 후 재동의 처리
                if (user.kakaoAccount?.email == null) {
                    requestAdditionalConsent()
                } else {
                    // 소셜 로그인
                    Log.d("LoginActivity", "소셜 로그인 토큰: $token")
                    memberViewModel.socialLogin("KAKAO", token)
                }
            }
        }
    }

    private fun requestAdditionalConsent() {
        UserApiClient.instance.loginWithNewScopes(
            this, listOf("account_email")  // 추가 동의 받을 scope
        ) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "추가 동의 실패: ${error.localizedMessage}")
            } else {
                Log.d("KakaoLogin", "추가 동의 후 토큰 발급됨")
                fetchKakaoUserInfo(token!!.accessToken)
            }
        }
    }

    private fun printSha1Fingerprint() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val sha1 = md.digest().joinToString(":") { String.format("%02X", it) }
                Log.d("SHA1", "앱에서 사용하는 SHA-1: $sha1")
            }
        } catch (e: Exception) {
            Log.e("SHA1", "SHA-1 지문 생성 실패", e)
        }
    }

    private fun kakaoLogout(context: Context) {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("KakaoLogout", "로그아웃 실패. SDK에서 토큰 폐기됨", error)
                Toast.makeText(context, "로그아웃 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else {
                Log.i("KakaoLogout", "로그아웃 성공. SDK에서 토큰 폐기됨")
                Toast.makeText(context, "로그아웃 성공", Toast.LENGTH_SHORT).show()
                // 로그아웃 이후 원하는 화면으로 이동 처리 추가
            }
        }
    }

    // 구글
    private fun getAccessTokenFromAuthCode(authCode: String?) {
        if (authCode == null) return

        val clientId = BuildConfig.GOOGLE_CLIENT_KEY
        val clientSecret = BuildConfig.GOOGLE_CLIENT_SECRET_KEY
        val redirectUri = "https://port-0-elixir-backend-g0424l70py8py.gksl2.cloudtype.app" // 또는 등록한 redirectUri
        val url = "https://oauth2.googleapis.com/token"

        val requestBody = FormBody.Builder()
            .add("code", authCode)
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("redirect_uri", redirectUri)
            .add("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("GoogleLogin", "Token 요청 실패: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val gson = Gson()
                    val tokenResponse = gson.fromJson(responseBody, GoogleSignupResponse::class.java)
                    // 여기서 tokenResponse.accessToken만 사용
                    Log.d("GoogleLogin", "Access Token: ${tokenResponse.access_token}")
                    memberViewModel.socialLogin("GOOGLE", tokenResponse.access_token)
                } else {
                    Log.e("GoogleLogin", "에러 응답: $responseBody")
                }
            }
        })
    }
}