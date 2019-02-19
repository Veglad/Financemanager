package com.example.vlad.financemanager.ui.adapters

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.widget.ImageButton
import android.widget.ImageView
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView

import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.ui.OnItemClickListener
import com.example.vlad.financemanager.ui.OnItemDeleteClickListener
import com.example.vlad.financemanager.utils.DateUtils

class OperationsAdapter(private val context: Context,
                        private var operationList: List<Operation>?) : RecyclerView.Adapter<OperationsAdapter.OperationViewHolder>() {
    private var itemDeleteClickListener: ((Int) -> Unit)? = null
    private var itemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        itemClickListener = listener
    }

    fun setOnItemDeleteClickListener(listener: (Int) -> Unit) {
        itemDeleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_operation_recycler, parent, false)
        return OperationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val (amount, operationDate, comment, isOperationIncome, category) = operationList!![position]

        with(holder) {
            commentText.text = comment
            if (comment.isEmpty()) {
                commentText.visibility = View.GONE
            }

            categoryText.text = category.name

            if (isOperationIncome) {
                amountText.text = String.format("+%s ₴", amount.toString())
                amountText.setTextColor(ContextCompat.getColor(context, R.color.lite_green))
            } else {
                amountText.text = String.format("-%s ₴", amount.toString())
                amountText.setTextColor(ContextCompat.getColor(context, R.color.lite_red))
            }

            val resultDateText = DateUtils.getStringDate(operationDate, DateUtils.DATE_FULL_PATTERN)
            textDate.text = resultDateText
            categoryImg.setImageResource(category.icon)

            itemView.setOnClickListener {
                itemClickListener?.invoke(holder.adapterPosition)
            }
            moreButton.setOnClickListener {
                initPopupMenu(holder)
            }
        }
    }

    private fun initPopupMenu(holder: OperationViewHolder) {
        PopupMenu(context, holder.moreButton).apply {
            inflate(R.menu.more_operation_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteMoreMenuItem -> {
                        itemDeleteClickListener?.invoke(holder.adapterPosition)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    override fun getItemCount(): Int {
        return operationList!!.size
    }

    class OperationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryText: TextView = itemView.findViewById(R.id.itemCategoryNameTextView)
        var amountText: TextView = itemView.findViewById(R.id.itemAmountTextView)
        var commentText: TextView = itemView.findViewById(R.id.itemCommentTextView)
        var textDate: TextView = itemView.findViewById(R.id.dateOperationRecyclerItemTextView)
        var categoryImg: ImageView = itemView.findViewById(R.id.circleIconImageView)
        var moreButton: ImageButton = itemView.findViewById(R.id.moreButton)
    }

    fun setOperationList(operationList: List<Operation>) {
        this.operationList = operationList
    }
}
