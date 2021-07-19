package me.zadli.windows11launcher.adapters;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.zadli.windows11launcher.R;

public class AppList extends RecyclerView.Adapter<AppList.ViewHolder> {

    List<ResolveInfo> apps;
    PackageManager pm;
    Context context;

    public AppList(List<ResolveInfo> apps, PackageManager pm, Context context) {
        this.pm = pm;
        this.apps = apps;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppList.ViewHolder holder, int position) {
        holder.appName.setText(pm.getApplicationLabel(
                apps.get(position).activityInfo.applicationInfo));
        holder.appIcon.setImageDrawable(pm.getApplicationIcon(
                apps.get(position).activityInfo.applicationInfo));

    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appName;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);

            itemView.setOnTouchListener((v, event) -> {
                ObjectAnimator animationX;
                ObjectAnimator animationY;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    animationX = ObjectAnimator
                            .ofFloat(v, "ScaleX", 0.8f);
                    animationY = ObjectAnimator
                            .ofFloat(v, "ScaleY", 0.8f);
                } else {
                    animationX = ObjectAnimator
                            .ofFloat(v, "ScaleX", 1f);
                    animationY = ObjectAnimator
                            .ofFloat(v, "ScaleY", 1f);
                }
                animationX.setDuration(150);
                animationY.setDuration(150);
                animationX.start();
                animationY.start();
                return false;
            });

            itemView.setOnClickListener(v ->
                    context.startActivity(pm.getLaunchIntentForPackage(apps.get(getAdapterPosition())
                    .activityInfo.packageName)));
        }
    }
}
