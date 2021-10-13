package com.agora.data.model;

import androidx.annotation.NonNull;

import com.agora.data.R;

import java.io.Serializable;

public class User implements Serializable, Cloneable {
    private String objectId;
    private String name;
    private String avatar;

    public User() {
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return objectId.equals(user.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }

    @NonNull
    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new User();
    }

    public int getAvatarRes() {
        if (avatar == null) {
            return R.mipmap.portrait01;
        }

        switch (avatar) {
            case "1":
                return R.mipmap.portrait01;
            case "2":
                return R.mipmap.portrait02;
            case "3":
                return R.mipmap.portrait03;
            case "4":
                return R.mipmap.portrait04;
            case "5":
                return R.mipmap.portrait05;
            case "6":
                return R.mipmap.portrait06;
            case "7":
                return R.mipmap.portrait07;
            case "8":
                return R.mipmap.portrait08;
            case "9":
                return R.mipmap.portrait09;
            case "10":
                return R.mipmap.portrait10;
            case "11":
                return R.mipmap.portrait11;
            case "12":
                return R.mipmap.portrait12;
            case "13":
                return R.mipmap.portrait13;
            case "14":
                return R.mipmap.portrait14;
            case "15":
                return R.mipmap.portrait15;
            case "16":
                return R.mipmap.portrait16;
            case "17":
                return R.mipmap.portrait17;
            case "18":
                return R.mipmap.portrait18;
            case "19":
                return R.mipmap.portrait19;
            case "20":
                return R.mipmap.portrait20;
            case "21":
                return R.mipmap.portrait21;
            case "22":
                return R.mipmap.portrait22;
            case "23":
                return R.mipmap.portrait23;
            case "24":
                return R.mipmap.portrait24;
            case "25":
                return R.mipmap.portrait25;
            case "26":
                return R.mipmap.portrait26;
            case "27":
                return R.mipmap.portrait27;
            case "28":
                return R.mipmap.portrait28;
            case "29":
                return R.mipmap.portrait29;
            case "30":
                return R.mipmap.portrait30;
            case "31":
                return R.mipmap.portrait31;
            default:
                return R.mipmap.portrait01;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "objectId='" + objectId + '\'' +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
