package com.example.vlad.financemanager.data.models

import java.io.Serializable
import java.math.BigDecimal
import java.util.Date

class Operation(@Transient
                var amount: BigDecimal,
                var operationDate: Date,
                var comment: String,
                var isOperationIncome: Boolean,
                var category: Category,
                var id: Int,
                var accountId: Int) : Serializable