package com.example.vlad.financemanager;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.vlad.financemanager.FinanceManagerContract.*;

import java.text.SimpleDateFormat;
import java.util.List;

public class OperationsAdapter extends RecyclerView.Adapter<OperationsAdapter.OperationViewHolder> {

    private Context mContext;
    private List<Operation> operationList;


    public OperationsAdapter(Context context, List<Operation> operations){
        mContext = context;
        operationList = operations;
    }

    public class OperationViewHolder extends RecyclerView.ViewHolder{
        TextView categoryText;
        TextView amountText;
        TextView commentText;
        TextView textDate;
        ImageView categoryImg;


        public OperationViewHolder(View itemView) {
            super(itemView);

            categoryText = itemView.findViewById(R.id.tv_itemCategoryName);
            amountText = itemView.findViewById(R.id.tv_itemAmount);
            commentText = itemView.findViewById(R.id.tv_itemComment);
            textDate = itemView.findViewById(R.id.tv_textDate);
            categoryImg = itemView.findViewById(R.id.circleIcon);

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
