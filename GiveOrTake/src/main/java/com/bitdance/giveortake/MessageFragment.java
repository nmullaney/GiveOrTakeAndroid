package com.bitdance.giveortake;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by nora on 8/16/13.
 */
public class MessageFragment extends Fragment {
    public static final String TAG = "MessageFragment";

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_OWNER_ID = "extra_owner_ID";

    private Item item;
    private User owner;

    private EditText editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Long itemID;
        Long ownerID;
        if (getArguments() != null) {
            itemID = (Long)getArguments().getSerializable(MessageFragment.EXTRA_ITEM_ID);
            ownerID = (Long) getArguments().getSerializable(MessageFragment.EXTRA_OWNER_ID);
        } else {
            itemID = (Long)getActivity().getIntent()
                    .getSerializableExtra(MessageFragment.EXTRA_ITEM_ID);
            ownerID = (Long) getActivity().getIntent()
                    .getSerializableExtra(MessageFragment.EXTRA_OWNER_ID);
        }
        GiveOrTakeApplication app = (GiveOrTakeApplication) getActivity().getApplication();
        item = app.getItem(itemID);
        owner = app.getUser(ownerID);

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 14) {
            getActivity().getActionBar().setIcon(item.getThumbnail(getActivity()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        TextView toView = (TextView)view.findViewById(R.id.message_to);
        toView.setText(owner.getUserName());
        TextView replyToView = (TextView)view.findViewById(R.id.message_reply_to);
        ActiveUser activeUser = ((GiveOrTakeApplication) getActivity().getApplication()).getActiveUser();
        replyToView.setText(activeUser.getEmail());
        TextView subjectView = (TextView)view.findViewById(R.id.message_subject);
        subjectView.setText(item.getName());

        editText = (EditText)view.findViewById(R.id.message_text);
        editText.setSelection(0);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.message_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_item_send:
                Intent sendMessageIntent = new Intent(getActivity(), ItemService.class);
                sendMessageIntent.setAction(ItemService.SEND_MESSAGE);
                sendMessageIntent.putExtra(ItemService.EXTRA_ITEM_ID, item.getId());
                sendMessageIntent.putExtra(ItemService.EXTRA_MESSAGE, editText.getText().toString());
                getActivity().startService(sendMessageIntent);
                Log.i(TAG, "Starting to send message");
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}
