package com.example.vlad.financemanager.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SpinnerItem(val id: Int, val name: String, val image: Int) : Parcelable {
    companion object {
        const val POSITION_DAY = 0
        const val POSITION_WEEK = 1
        const val POSITION_MONTH = 2
        const val POSITION_YEAR = 3
    }
}
