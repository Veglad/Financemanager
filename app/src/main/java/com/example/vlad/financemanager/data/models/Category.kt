package com.example.vlad.financemanager.data.models

import android.text.TextUtils

import java.io.Serializable

class Category(var id: Int, private var name: String?, var icon: Int, val isCustom: Boolean, val isInputCategory: Boolean) : Serializable {

    fun getName(): String? {
        return name
    }

    fun setName(name: String) {
        if (!TextUtils.isEmpty(name)) {
            this.name = name
        }
    }
}
