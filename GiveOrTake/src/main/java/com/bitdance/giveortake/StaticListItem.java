package com.bitdance.giveortake;

import android.content.Context;
import android.view.View;

/**
 * Created by nora on 7/3/13.
 */
abstract public class StaticListItem {

    public boolean isEnabled() {
        return true;
    }

    abstract public View getView(Context context, View convertView);
}
