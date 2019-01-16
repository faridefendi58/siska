package com.slightsite.siskapos.ui.transaction;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.siskapos.R;
import com.slightsite.siskapos.domain.CurrencyController;
import com.slightsite.siskapos.domain.inventory.LineItem;
import com.slightsite.siskapos.domain.sale.Register;

import java.util.ArrayList;
import java.util.List;

public class AdapterListCart extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SwipeItemTouchHelper.SwipeHelperAdapter {
    private List<LineItem> items = new ArrayList<>();
    private List<LineItem> items_swiped = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private TextView cart_total;

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListCart(Context context, List<LineItem> items, Register register, TextView cart_total) {
        this.items = items;
        ctx = context;
        this.register = register;
        this.cart_total = cart_total;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder implements SwipeItemTouchHelper.TouchViewHolder {
        public ImageView image;
        public TextView title;
        public TextView price;
        public TextView price_subtotal;
        public TextView quantity;
        public View lyt_parent;
        public ImageButton add_qty;
        public ImageButton substract_qty;
        public Button bt_undo;
        public View lyt_undo;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            price = (TextView) v.findViewById(R.id.price);
            price_subtotal = (TextView) v.findViewById(R.id.price_subtotal);
            quantity = (TextView) v.findViewById(R.id.quantity);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            add_qty = (ImageButton) v.findViewById(R.id.add_qty);
            substract_qty = (ImageButton) v.findViewById(R.id.substract_qty);
            bt_undo = (Button) v.findViewById(R.id.bt_undo);
            lyt_undo = (View) v.findViewById(R.id.lyt_undo);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(ctx.getResources().getColor(R.color.grey_5));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_line, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    private List<OriginalViewHolder> whs = new ArrayList<>();

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            OriginalViewHolder vwh = (OriginalViewHolder) holder;
            whs.add(position, vwh);

            final LineItem p = items.get(position);
            view.title.setText(p.getProduct().getName());
            int qty = 1;
            double prc = 0.0;
            double sub_total = 0.0;
            try {
                qty = p.getQuantity();
                prc = p.getPriceAtSale();
            } catch (Exception e) {
                Log.e("Adapter List Cart", e.getMessage());
            }
            view.quantity.setText(""+qty);
            sub_total = prc * qty;
            view.price.setText("@ "+ CurrencyController.getInstance().moneyFormat(prc));
            view.price_subtotal.setText(CurrencyController.getInstance().moneyFormat(sub_total));
            view.image.setImageResource(R.drawable.ic_no_image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            view.add_qty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int current_qty = 0;
                    try {
                        current_qty = Integer.parseInt(view.quantity.getText().toString());
                    } catch (Exception e) {
                        Log.e(this.getClass().getSimpleName(), e.getMessage());
                    }
                    current_qty = current_qty + 1;
                    view.quantity.setText(""+ current_qty);
                    try {
                        updateQty(p, current_qty, p.getPriceAtSale(), view);
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                }
            });

            view.substract_qty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int current_qty = 1;
                    try {
                        current_qty = Integer.parseInt(view.quantity.getText().toString());
                    } catch (Exception e) {
                        Log.e(this.getClass().getSimpleName(), e.getMessage());
                    }
                    if (current_qty > 1) {
                        current_qty = current_qty - 1;
                        view.quantity.setText(""+ current_qty);
                        try {
                            updateQty(p, current_qty, p.getPriceAtSale(), view);
                        } catch (Exception e) {
                            Log.e(getClass().getSimpleName(), e.getMessage());
                        }
                    }
                }
            });

            view.bt_undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    items.get(position).swiped = false;
                    items_swiped.remove(items.get(position));
                    notifyItemChanged(position);
                }
            });

            if (p.swiped) {
                view.lyt_parent.setVisibility(View.GONE);
            } else {
                view.lyt_parent.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (LineItem s : items_swiped) {
                    int index_removed = items.indexOf(s);
                    if (index_removed != -1) {
                        items.remove(index_removed);
                        notifyItemRemoved(index_removed);
                    }
                }
                items_swiped.clear();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onItemDismiss(final int position) {

        // handle when double swipe
        if (items.get(position).swiped) {
            items_swiped.remove(items.get(position));
            items.remove(position);
            notifyItemRemoved(position);
            return;
        }

        items.get(position).swiped = true;
        items_swiped.add(items.get(position));
        try {
            final int pos = position;
            final int swiped_key = items_swiped.indexOf(items.get(position));
            Log.e(getClass().getSimpleName(), "pos : "+ pos);
            Log.e(getClass().getSimpleName(), "swiped_key : "+ swiped_key);
            Log.e(getClass().getSimpleName(), "items_swiped : "+ items_swiped.toString());
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            if (items_swiped.contains(items.get(pos))) {
                                items_swiped.clear();
                                register.removeItem(items.get(pos));
                                notifyItemRemoved(position);
                                cart_total.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()));
                            }
                        }
                    },
                    3000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyItemChanged(position);
    }

    private void updateQty(LineItem lineItem, int qty, Double price, OriginalViewHolder view) {
        lineItem.setQuantity(qty);
        Double grosir_price = lineItem.getProduct().getUnitPriceByQuantity(lineItem.getProduct().getId(), qty);

        int saleId = lineItem.getId();
        if (grosir_price > 0) {
            register.updateItem(
                    saleId,
                    lineItem,
                    qty,
                    grosir_price
            );

            double sub_total = grosir_price * qty;
            view.price.setText("@ "+ CurrencyController.getInstance().moneyFormat(grosir_price));
            view.price_subtotal.setText(CurrencyController.getInstance().moneyFormat(sub_total));
        } else {
            register.updateItem(
                    saleId,
                    lineItem,
                    qty,
                    price
            );

            double sub_total = price * qty;
            view.price.setText("@ "+ CurrencyController.getInstance().moneyFormat(price));
            view.price_subtotal.setText(CurrencyController.getInstance().moneyFormat(sub_total));
        }

        cart_total.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()));
    }
}
