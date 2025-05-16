package com.rose.back.domain.auth.oauth2.response;

import java.util.Map;

public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getUserEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getUserNick() {
        return attribute.get("name").toString();
    }
}