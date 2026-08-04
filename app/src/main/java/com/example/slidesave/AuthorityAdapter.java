package com.example.slidesave;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class AuthorityAdapter extends RecyclerView.Adapter<AuthorityAdapter.AuthorityViewHolder> {

    public interface OnAuthorityClickListener {
        void onAuthoritySelected(Authority authority);
        void onAuthorityFocused(Authority authority);
    }

    private final List<Authority> authorityList;
    private final OnAuthorityClickListener listener;

    public AuthorityAdapter(List<Authority> authorityList,OnAuthorityClickListener listener) {
        this.authorityList = authorityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AuthorityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.authority_card, parent, false);
        return new AuthorityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorityViewHolder holder, int position) {
        Authority authority = authorityList.get(position);
        holder.tvName.setText(authority.name);
        holder.tvDistance.setText(
                String.format(Locale.getDefault(),"%.2f km", authority.distance));

        // Display readable authority type
        switch (authority.type) {
            case "police":
                holder.tvType.setText("Police");
                holder.imgAuthority.setImageResource(R.drawable.ic_police);
                break;
            case "fire_station":
                holder.tvType.setText("Fire Station");
                holder.imgAuthority.setImageResource(R.drawable.ic_fire_station);
                break;
            case "hospital":
                holder.tvType.setText("Hospital");
                holder.imgAuthority.setImageResource(R.drawable.ic_hospital);
                break;
            default:
                holder.tvType.setText(authority.type);
                holder.imgAuthority.setImageResource(R.drawable.ic_location);
                break;
        }

        holder.btnCall.setOnClickListener(v ->listener.onAuthoritySelected(authority));
        holder.itemView.setOnClickListener(v ->listener.onAuthorityFocused(authority));
    }

    @Override
    public int getItemCount() {
        return authorityList.size();
    }

    static class AuthorityViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDistance;
        TextView tvType;
        ImageView imgAuthority;
        ImageButton btnCall;

        public AuthorityViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvAuthorityName);
            tvDistance = itemView.findViewById(R.id.tvAuthorityDistance);
            tvType = itemView.findViewById(R.id.tvAuthorityType);
            imgAuthority = itemView.findViewById(R.id.imgAuthority);
            btnCall = itemView.findViewById(R.id.btnCall);
        }
    }
}