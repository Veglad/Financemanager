package com.example.vlad.financemanager.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(var id: Int,
                    var name: String,
                    var icon: Int,
                    var isCustom: Boolean,
                    var isInputCategory: Boolean) : Parcelable {

    constructor() : this(-1, "", -1, false, false)
}
