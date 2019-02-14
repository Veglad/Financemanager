package com.example.vlad.financemanager.data.models

import java.io.Serializable
import java.math.BigDecimal
import java.util.Date

class Operation : Serializable {

    @Transient
    var amount: BigDecimal? = null
    var operationDate: Date? = null
    var comment: String? = null
    var isOperationIncome: Boolean = false
    var category: Category? = null
    var id: Int = 0
    var accountId: Int = 0
        private set

    fun initOperation(id: Int, accountId: Int, amount: BigDecimal, operationDate: Date, comment: String,
                      isOperationIncome: Boolean, category: Category) {
        this.id = id
        this.accountId = accountId
        this.amount = amount
        this.operationDate = operationDate
        this.comment = comment
        this.isOperationIncome = isOperationIncome
        this.category = category
    }
}
