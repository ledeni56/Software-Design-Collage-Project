package com.example.lovro.myapplication.adapters;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lovro.myapplication.R;
import com.example.lovro.myapplication.domain.Offer;
import com.example.lovro.myapplication.domain.Story;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private List<Story> stories;
    private StoryAdapter.OnShowClickListener onShowClickListener;


    public StoryAdapter(List<Story> stories){
        this.stories = stories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_story,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.ViewHolder viewHolder, int i) {
        final Story story = stories.get(i);
        ImageView imageView = viewHolder.itemView.findViewById(R.id.story_image);
        TextView storyDesc = viewHolder.itemView.findViewById(R.id.story_desc);
        //ImageView profileImage = viewHolder.itemView.findViewById(R.id.profile_image);
        TextView textView = viewHolder.itemView.findViewById(R.id.username_story);

        Picasso.get().load(story.getImageUrl()).into(imageView);
        storyDesc.setText(story.getText());
        textView.setText(story.getUser().getUsername());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowClickListener.onShowClick(story);
            }
        };

        viewHolder.itemView.setOnClickListener(listener);
    }


    public void setStories(List<Story> stories) {
        this.stories = stories;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private View itemView;

        public ViewHolder(View itemView){
            super(itemView);
            this.itemView = itemView;
        }
    }

    public void setListener(StoryAdapter.OnShowClickListener listener){
        this.onShowClickListener = listener;
    }


    public interface OnShowClickListener{
        void onShowClick(Story story);
    }
}