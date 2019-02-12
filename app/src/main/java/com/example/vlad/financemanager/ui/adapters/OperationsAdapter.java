package com.example.vlad.financemanager.ui.adapters;

import android.content.Context;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.utils.DateUtils;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OperationsAdapter extends RecyclerView.Adapter<OperationsAdapter.OperationViewHolder> {

    private Context context;
    private List<Operation> operationList;
    private ItemLongClick itemLongClickListener;
    private ItemClick itemClickListener;

    public OperationsAdapter(Context context, List<Operation> operations) {
        this.context = context;
        operationList = operations;
    }

    public interface ItemClick {
        void onItemClick(int position);
    }

    public interface ItemLongClick {
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(ItemClick listener) {
        itemClickListener = listener;
    }

    public void setOnItemLongClickListener(ItemLongClick listener) {
        itemLongClickListener = listener;
    }

    @NonNull
    @Override
    public OperationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_operation_recycler, parent, false);
        return new OperationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OperationViewHolder holder, int position) {
        Operation operation = operationList.get(position);

        holder.commentText.setText(operation.getComment());
        holder.categoryText.setText(operation.getCategory().getName());

        if (operation.getIsOperationIncome()) {
            holder.amountText.setText(String.format("+%s ₴", operation.getAmount().toString()));
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.colorLiteGreen));
        } else {
            holder.amountText.setText(String.format("-%s ₴", operation.getAmount().toString()));
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.colorLiteRed));
        }

        String resultDateText = DateUtils.getStringDate(operation.getOperationDate(), DateUtils.DATE_FULL_PATTERN);
        holder.textDate.setText(resultDateText);
        holder.categoryImg.setImageResource(operation.getCategory().getIcon());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemLongClickListener != null) {
                    itemLongClickListener.onItemLongClick(holder.getAdapterPosition());
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return operationList.size();
    }

    class OperationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemCategoryNameTextView) TextView categoryText;
        @BindView(R.id.itemAmountTextView) TextView amountText;
        @BindView(R.id.itemCommentTextView) TextView commentText;
        @BindView(R.id.dateOperationRecyclerItemTextView) TextView textDate;
        @BindView(R.id.circleIconImageView) ImageView categoryImg;


        OperationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
    }
}
