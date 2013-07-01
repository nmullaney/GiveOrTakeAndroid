package com.bitdance.giveortake;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: nora
 * Date: 6/21/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginActivity extends SingleFragmentActivity {

    protected Fragment createFragment() {
        return new LoginFragment();
    }
}
