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

import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.ui.OnItemClickListener
import com.example.vlad.financemanager.ui.OnItemDeleteClickListener
import com.example.vlad.financemanager.utils.DateUtils

class OperationsAdapter(private val context: Context,
                        private var operationList: List<Operation>?) : RecyclerView.Adapter<OperationsAdapter.OperationViewHolder>() {
    private var itemDeleteClickListener: OnItemDeleteClickListener? = null
    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun setOnItemDeleteClickListener(listener: OnItemDeleteClickListener) {
        itemDeleteClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_operation_recycler, parent, false)
        return OperationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val (amount, operationDate, comment, isOperationIncome, category) = operationList!![position]

        holder.commentText.text = comment
        if (comment.isEmpty()) {
            holder.commentText.visibility = View.GONE
        }

        holder.categoryText.text = category.name

        if (isOperationIncome) {
            holder.amountText.text = String.format("+%s ₴", amount.toString())
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.lite_green))
        } else {
            holder.amountText.text = String.format("-%s ₴", amount.toString())
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.lite_red))
        }

        val resultDateText = DateUtils.getStringDate(operationDate, DateUtils.DATE_FULL_PATTERN)
        holder.textDate.text = resultDateText
        holder.categoryImg.setImageResource(category.icon)

        holder.itemView.setOnClickListener {
            if (itemClickListener != null) {
                itemClickListener!!.onItemClick(holder.adapterPosition)
            }
        }
        holder.moreButton.setOnClickListener {
            val popup = PopupMenu(context, holder.moreButton)
            popup.inflate(R.menu.more_operation_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteMoreMenuItem -> {
                        if (itemDeleteClickListener != null) {
                            itemDeleteClickListener!!.onItemDeleteClick(holder.adapterPosition)
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
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