package com.example.vlad.financemanager.ui

import com.example.vlad.financemanager.data.models.Operation

interface OnChangeOperationClickListener {
    fun onChangeOperationClick(operation: Operation)
}