package com.bitdance.giveortake;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * ItemStateView is for showing an Item's state, complete with the representative icon.
 */
public class ItemStateView extends LinearLayout {
    public static final String TAG = "ItemStateView";

    private ImageView stateIconView;
    private TextView stateTextView;

    public ItemStateView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.i(TAG, "Creating new item state view");

        Item.ItemState itemState = Item.ItemState.PROMISED;
        if (attributeSet != null) {
            TypedArray a = getContext().obtainStyledAttributes(attributeSet,
                    R.styleable.ItemStateView);
            String itemStateName = a.getString(R.styleable.ItemStateView_item_state);
            if (itemStateName != null) {
                Log.i(TAG, "item state name is set to " + itemStateName);
                itemState = Item.ItemState.valueForName(itemStateName);
            }
        }

        Log.i(TAG, "item is set to " + itemState);

        setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;

        stateIconView = new ImageView(getContext());
        stateIconView.setPadding(5, 5, 5, 5);
        if (itemState != null) {
            stateIconView.setImageDrawable(itemState.getDrawable(getContext()));
        }
        addView(stateIconView, layoutParams);

        stateTextView = new TextView(getContext());
        stateTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        if (itemState != null) {
            stateTextView.setText(itemState.getName());
        }

        addView(stateTextView, layoutParams);
    }

    public void setItemState(Item.ItemState itemState) {
        stateIconView.setImageDrawable(itemState.getDrawable(getContext()));
        stateTextView.setText(itemState.getName());
    }
}
