package com.example.nhs_blood_staff_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;


public class RequestAdapter extends FirestoreRecyclerAdapter<Request, RequestAdapter.RequestHolder> {

    public RequestAdapter(@NonNull FirestoreRecyclerOptions<Request> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestHolder holder, int position, @NonNull Request model) {
        holder.BloodType.setText(model.getbloodType());
        holder.ReqDate.setText(model.getreqDate());
        holder.EndDate.setText(model.getendDate());
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new RequestHolder(v);
    }

    class RequestHolder extends RecyclerView.ViewHolder {
        TextView BloodType, ReqDate, EndDate;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            BloodType = itemView.findViewById(R.id.BloodType);
            ReqDate = itemView.findViewById(R.id.ReqDate);
            EndDate = itemView.findViewById(R.id.EndDate);
        }
    }
}
