package com.example.vlad.financemanager.ui.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.ImageButton;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OperationsAdapter extends RecyclerView.Adapter<OperationsAdapter.OperationViewHolder> {

    private Context context;
    private List<Operation> operationList;
    private onItemDeleteClickListener itemDeleteClickListener;
    private OnItemClickListener itemClickListener;

    public OperationsAdapter(Context context, List<Operation> operations) {
        this.context = context;
        operationList = operations;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface onItemDeleteClickListener {
        void onItemDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public void setOnItemDeleteClickListener(onItemDeleteClickListener listener) {
        itemDeleteClickListener = listener;
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
        if (operation.getComment().isEmpty()) {
            holder.commentText.setVisibility(View.GONE);
        }

        holder.categoryText.setText(operation.getCategory().getName());

        if (operation.isOperationIncome()) {
            holder.amountText.setText(String.format("+%s ₴", operation.getAmount().toString()));
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.lite_green));
        } else {
            holder.amountText.setText(String.format("-%s ₴", operation.getAmount().toString()));
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.lite_red));
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
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, holder.moreButton);
                popup.inflate(R.menu.more_operation_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.deleteMoreMenuItem:
                                if (itemDeleteClickListener != null) {
                                    itemDeleteClickListener.onItemDeleteClick(holder.getAdapterPosition());
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
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
        @BindView(R.id.moreButton) ImageButton moreButton;


        OperationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
    }
}
