package com.rose.back.user.dto.oauth2;

public interface OAuth2Response {

    String getProvider(); // 제공자 (Ex. naver, google, kakao)
    String getProviderId(); // 제공자에서 발급해주는 아이디(번호)  
    String getUserEmail(); // 이메일
    String getUserNick(); // 사용자 실명 (설정한 이름)
}
