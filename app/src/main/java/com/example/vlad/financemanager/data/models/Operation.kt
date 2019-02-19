package com.example.vlad.financemanager.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.math.BigDecimal
import java.util.Date

@Parcelize
data class Operation(@Transient
                     var amount: BigDecimal,
                     var operationDate: Date,
                     var comment: String,
                     var isOperationIncome: Boolean,
                     var category: Category,
                     var id: Int,
                     var accountId: Int) : Parcelable