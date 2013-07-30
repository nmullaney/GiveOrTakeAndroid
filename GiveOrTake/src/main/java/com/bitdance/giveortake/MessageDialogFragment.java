package com.bitdance.giveortake;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by nora on 6/28/13.
 */
public class MessageDialogFragment extends DialogFragment {
    public static final String TAG = "MessageDialogFragment";

    private Item item;
    private User owner;

    private EditText editText;

    public MessageDialogFragment(Item item, User owner) {
        this.item = item;
        this.owner = owner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogLayout = layoutInflater.inflate(R.layout.fragment_message_dialog, null);

        TextView toView = (TextView)dialogLayout.findViewById(R.id.message_dialog_to);
        toView.setText(owner.getUserName());
        TextView replyToView = (TextView)dialogLayout.findViewById(R.id.message_dialog_reply_to);
        replyToView.setText(ActiveUser.getInstance().getEmail());
        TextView subjectView = (TextView)dialogLayout.findViewById(R.id.message_dialog_subject);
        subjectView.setText(item.getName());

        editText = (EditText)dialogLayout.findViewById(R.id.message_dialog_message);
        editText.setSelection(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.send_a_message));
        builder.setView(dialogLayout);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // No need to do anything, just close
            }
        });
        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent sendMessageIntent = new Intent(getActivity(), ItemService.class);
                sendMessageIntent.setAction(ItemService.SEND_MESSAGE);
                sendMessageIntent.putExtra(ItemService.EXTRA_ITEM_ID, item.getId());
                sendMessageIntent.putExtra(ItemService.EXTRA_MESSAGE, editText.getText().toString());
                getActivity().startService(sendMessageIntent);
                Log.i(TAG, "Starting to send message");
            }
        });
        return builder.create();
    }


}
