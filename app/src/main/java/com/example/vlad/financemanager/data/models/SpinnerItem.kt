package com.example.vlad.financemanager.data.models

import java.io.Serializable

class SpinnerItem(val id: Int, val name: String, val image: Int) : Serializable {
    companion object {
        val POSITION_DAY = 0
        val POSITION_WEEK = 1
        val POSITION_MONTH = 2
        val POSITION_YEAR = 3
    }
}
