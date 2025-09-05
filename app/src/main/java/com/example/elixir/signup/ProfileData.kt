package com.example.elixir.signup

// 프로필 데이터(이미지 링크, 닉네임, 성별, 출생년도) 저장
data class ProfileData(var profileImage: String, var nickname: String,
                       var gender: String?, var birthYear: Int)