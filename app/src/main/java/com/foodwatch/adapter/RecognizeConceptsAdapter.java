package com.foodwatch.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foodwatch.android.starter.api.v2.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import clarifai2.dto.prediction.Concept;

import static android.media.CamcorderProfile.get;

public class RecognizeConceptsAdapter extends RecyclerView.Adapter<RecognizeConceptsAdapter.Holder> {

    private String[] nutritionData;

    @NonNull
    private List<Concept> concepts = new ArrayList<>();

    public RecognizeConceptsAdapter setData(@NonNull String[] nutritionData) {
        this.nutritionData = nutritionData;
        notifyDataSetChanged();
        return this;
    }

    public RecognizeConceptsAdapter setData(@NonNull List<Concept> concepts) {
        this.concepts = concepts;
        notifyDataSetChanged();
        return this;
    }

    @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_concept, parent, false));
    }

    @Override public void onBindViewHolder(Holder holder, int position) {
        if (nutritionData != null) {
            final String data = nutritionData[position];
            String type = "";
            switch (position) {
                case 0:
                    type = "Name";
                    break;
                case 1:
                    type = "Calories";
                    break;
                case 2:
                    type = "Protein";
                    break;
                case 3:
                    type = "Total Fat";
                    break;
                case 4:
                    type = "Carbohydrates";
                    break;
                case 5:
                    type = "Fiber";
                    break;
                case 6:
                    type = "Sugar";
                    break;
                case 7:
                    type = "Calcium";
                    break;
                case 8:
                    type = "Iron";
                    break;
                case 9:
                    type = "Potassium";
                    break;
                case 10:
                    type = "Sodium";
                    break;
                case 11:
                    type = "Vitamin C";
                    break;
            }

            holder.label.setText(type);
            holder.probability.setText(String.valueOf(data));
        } else {
            final Concept concept = concepts.get(position);
            holder.label.setText(concept.name() != null ? concept.name() : concept.id());
            holder.probability.setText(String.valueOf(concept.value()));
        }

    }

    @Override public int getItemCount() {
        if (nutritionData != null) {
            return nutritionData.length;
        }
        return concepts.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.label)
        TextView label;
        @BindView(R.id.probability) TextView probability;

        public Holder(View root) {
            super(root);
            ButterKnife.bind(this, root);
        }
    }
}