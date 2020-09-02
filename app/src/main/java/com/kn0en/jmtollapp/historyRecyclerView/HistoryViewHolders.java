package com.kn0en.jmtollapp.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kn0en.jmtollapp.HistorySingleActivity;
import com.kn0en.jmtollapp.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;
    public TextView time;

    public HistoryViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView) itemView.findViewById(R.id.rideId);
        time = (TextView) itemView.findViewById(R.id.time);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}
