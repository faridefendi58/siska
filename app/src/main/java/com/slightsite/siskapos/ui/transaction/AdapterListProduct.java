package com.slightsite.siskapos.ui.transaction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.inventory.Product;

import java.util.ArrayList;
import java.util.List;

public class AdapterListProduct extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private List<Product> items = new ArrayList<>();
    private List<Product> items2 = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Product obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListProduct(Context context, List<Product> items) {
        this.items = items;
        this.items2 = items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView brief;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            brief = (TextView) v.findViewById(R.id.brief);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_category, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Product p = items.get(position);
            view.title.setText(p.getName());
            int tot_stock = 0;
            try {
                tot_stock = p.getStock(p.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String brief = "out of stock";
            if (tot_stock > 0) {
                brief = tot_stock + " in stock";
            }

            view.brief.setText(brief);
            view.image.setImageResource(R.drawable.ic_image_black_24dp);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String charString = constraint.toString();

                if (charString.isEmpty()){
                    items = items2;
                }else{

                    List<Product> filterList = new ArrayList<>();

                    for (Product data : items2){

                        if (data.getName().toLowerCase().contains(charString)
                                || data.getBarcode().contains(charString)){
                            filterList.add(data);
                        }
                    }

                    items = filterList;

                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = items;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                items = (List<Product>) results.values;
                notifyDataSetChanged();
            }
        };

    }
}
