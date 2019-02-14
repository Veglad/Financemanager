package com.example.vlad.financemanager.data.models

import java.util.ArrayList

class Account(var id: Int, var name: String, val icon: Int) {
    var operations: ArrayList<Operation>
        internal set

    init {
        operations = ArrayList()

    }
}
