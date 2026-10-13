package com.PiliPili.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.io.Serializable;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDto implements Serializable {

    private String userId;
    private String userName;
    private String avatar;
    private Long expireTime;
    private String token;

    private Integer fansCount;
    private Integer currentCoinCount;
    private Integer focusCount;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    public void setCurrentCoinCount(Integer currentCoinCount) {
        this.currentCoinCount = currentCoinCount;
    }

    public void setFocusCount(Integer focusCount) {
        this.focusCount = focusCount;
    }
}
