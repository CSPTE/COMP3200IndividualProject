package com.example.finalversion.ui.slideshow;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalversion.R;

public class LeaderboardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView nameTextView;
    public TextView scoreTextView;
    public TextView rankTextView;
    public LinearLayout leaderboardComponentParent;

    private final LeaderboardAdapter.OnItemListener onItemListener;

    public LeaderboardViewHolder(@NonNull View itemView, LeaderboardAdapter.OnItemListener onItemListener) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.leaderboard_user_name);
        scoreTextView = itemView.findViewById(R.id.leaderboard_score);
        rankTextView = itemView.findViewById(R.id.leaderboard_rank);
        leaderboardComponentParent = itemView.findViewById(R.id.leaderboard_component);

        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onItemListener.onItemClick(getAdapterPosition(), (String) nameTextView.getText());
    }
}
