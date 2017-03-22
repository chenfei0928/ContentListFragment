package com.chenfei.contentlistfragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chenfei.User;

import java.util.List;

/**
 * Created by MrFeng on 2017/3/22.
 */
class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final List<User> mList;

    UserAdapter(List<User> list) {
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = mList.get(position);
        holder.name.setText(user.getLogin());
        Glide.with(holder.itemView.getContext())
                .load(user.getAvatar_url())
                .into(holder.cover);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.iv_cover);
            name = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}
