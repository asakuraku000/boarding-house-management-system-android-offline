package com.mingmingdecoder.bhms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private Context context;
    private List<Room> roomList;
    private OnRoomClickListener onRoomClickListener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room, int position);
    }

    public RoomAdapter(Context context, List<Room> roomList, OnRoomClickListener onRoomClickListener) {
        this.context = context;
        this.roomList = roomList;
        this.onRoomClickListener = onRoomClickListener;
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.roomNumberTextView.setText(room.getRoomNumber());
        
        String descriptionText = room.getDescription();
        if (descriptionText != null && !descriptionText.isEmpty()) {
            holder.descriptionTextView.setText(descriptionText);
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setVisibility(View.GONE);
        }
        
        holder.capacityTextView.setText("Capacity: " + room.getCapacity() + " tenant(s)");
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRoomClickListener.onRoomClick(room, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomNumberTextView;
        TextView descriptionTextView;
        TextView capacityTextView;

        public RoomViewHolder(View itemView) {
            super(itemView);
            roomNumberTextView = itemView.findViewById(R.id.roomNumberTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            capacityTextView = itemView.findViewById(R.id.capacityTextView);
        }
    }
}