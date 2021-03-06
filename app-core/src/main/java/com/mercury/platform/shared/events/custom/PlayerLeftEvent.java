package com.mercury.platform.shared.events.custom;

import com.mercury.platform.shared.events.MercuryEvent;

public class PlayerLeftEvent implements MercuryEvent {
    private String nickName;

    public PlayerLeftEvent(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
