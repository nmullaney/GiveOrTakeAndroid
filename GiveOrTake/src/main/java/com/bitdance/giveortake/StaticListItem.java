package com.bitdance.giveortake;

import android.content.Context;
import android.view.View;

/**
 * StaticListItem is used by the StaticListAdapter.  Anything that should appear in
 * a static list view should subclass this.
 */
abstract public class StaticListItem {

    public boolean isEnabled() {
        return true;
    }

    public void handleOnClick() {
        // subclasses should handle clicks, if enabled
    }

    abstract public View getView(Context context, View convertView);
}
