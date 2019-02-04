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

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OperationsAdapter extends RecyclerView.Adapter<OperationsAdapter.OperationViewHolder> {

    private Context mContext;
    private List<Operation> operationList;


    public OperationsAdapter(Context context, List<Operation> operations){
        mContext = context;
        operationList = operations;
    }

    public class OperationViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.itemCategoryNameTextView) TextView categoryText;
        @BindView(R.id.itemAmountTextView) TextView amountText;
        @BindView(R.id.itemCommentTextView) TextView commentText;
        @BindView(R.id.dateOperationRecyclerItemTextView) TextView textDate;
        @BindView(R.id.circleIconImageView) ImageView categoryImg;


        public OperationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public OperationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_operation_recycler,parent, false);

        return new OperationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OperationViewHolder holder, int position) {
        Operation operation = operationList.get(position);

        holder.commentText.setText(operation.getComment());

        holder.categoryText.setText(operation.getCategory().getName());

        if(operation.getIsOperationIncome()){
            holder.amountText.setText("+"+operation.getAmount().toString() + " ₴");
            holder.amountText.setTextColor(ContextCompat.getColor( mContext, R.color.colorLiteGreen));
        }
        else {
            holder.amountText.setText("-"+operation.getAmount().toString() + " ₴");
            holder.amountText.setTextColor(ContextCompat.getColor( mContext, R.color.colorLiteRed));
        }

        String resultDateText = new SimpleDateFormat("MM.dd.yyyy").format(operation.getOperationDate());
        holder.textDate.setText(resultDateText);
        holder.categoryImg.setImageResource(operation.getCategory().getIcon());
    }

    @Override
    public int getItemCount() {
        return operationList.size();
    }
}
