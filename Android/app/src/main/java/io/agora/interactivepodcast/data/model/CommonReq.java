package io.agora.interactivepodcast.data.model;

import com.google.gson.Gson;

import java.io.Serializable;


public class CommonReq implements Serializable {
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
