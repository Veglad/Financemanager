package com.example.vlad.financemanager.data.models

import java.io.Serializable

data class Category(var id: Int,
               var name: String,
               var icon: Int,
               var isCustom: Boolean,
               var isInputCategory: Boolean) : Serializable {

    constructor() : this(-1, "", -1, false, false)
}
