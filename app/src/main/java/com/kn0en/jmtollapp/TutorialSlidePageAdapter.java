package com.kn0en.jmtollapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TutorialSlidePageAdapter extends RecyclerView.Adapter<TutorialSlidePageAdapter.TutorialViewHolder> {

    private List<TutorialSlidePageItem> tutorialSlidePageItems;

    public TutorialSlidePageAdapter(List<TutorialSlidePageItem> tutorialSlidePageItems) {
        this.tutorialSlidePageItems = tutorialSlidePageItems;
    }

    @NonNull
    @Override
    public TutorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TutorialViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_tutorial_container_page, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialViewHolder holder, int position) {
        holder.setTutorialData(tutorialSlidePageItems.get(position));
    }

    @Override
    public int getItemCount() {
        return tutorialSlidePageItems.size();
    }

    class TutorialViewHolder extends RecyclerView.ViewHolder{
        private TextView textTitle, textContent;
        private ImageView imageSlidePage;

       TutorialViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textContent = itemView.findViewById(R.id.textContent);
            imageSlidePage = itemView.findViewById(R.id.imageSlide);
        }

        void setTutorialData(TutorialSlidePageItem tutorialItem){
            textTitle.setText(tutorialItem.getTextTitle());
            textContent.setText(tutorialItem.getTextContent());
            imageSlidePage.setImageResource(tutorialItem.getImage());
        }
    }
}
