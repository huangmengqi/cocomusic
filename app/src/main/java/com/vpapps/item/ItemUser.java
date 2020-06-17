package com.vpapps.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemUser implements Serializable {

    private String id, name, email, mobile;

    public ItemUser(String id, String name, String email, String mobile)
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getEmail()
    {
        return email;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
}