package com.example.finalversion.ui.slideshow;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalversion.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardViewHolder> {
    private List<LeaderboardComponent> leaderboardList;
    private final OnItemListener onItemListener;
    private Context cont;
    private String currentLeaderboard;

    public LeaderboardAdapter(List<LeaderboardComponent> leaderboardList, OnItemListener onItemListener, Context context, String currentlb) {
        this.leaderboardList = leaderboardList;
        this.onItemListener = onItemListener;
        cont = context;
        currentLeaderboard = currentlb;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_component, parent, false);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.leaderboard_component, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 35, cont.getResources().getDisplayMetrics());
        //layoutParams.height = (int) (parent.getHeight() * 0.05);
        return new LeaderboardViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        if (currentLeaderboard.equals("Weekly")){
            holder.rankTextView.setText(leaderboardList.get(position).getWeeklyRank());
            holder.nameTextView.setText(leaderboardList.get(position).getName());
            holder.scoreTextView.setText(leaderboardList.get(position).getWeeklyScore());
        } else if (currentLeaderboard.equals("Monthly")) {
            holder.rankTextView.setText(leaderboardList.get(position).getMonthlyRank());
            holder.nameTextView.setText(leaderboardList.get(position).getName());
            holder.scoreTextView.setText(leaderboardList.get(position).getMonthlyScore());
        } else if (currentLeaderboard.equals("AllTime")) {
            holder.rankTextView.setText(leaderboardList.get(position).getAllTimeRank());
            holder.nameTextView.setText(leaderboardList.get(position).getName());
            holder.scoreTextView.setText(leaderboardList.get(position).getAllTimeScore());
        }

        if (position % 2 == 0){
            holder.leaderboardComponentParent.setBackgroundColor(Color.parseColor("#44757575"));
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}