package com.damn.polito.damneat.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.damn.polito.damneat.R;

import org.json.JSONException;
import org.json.JSONObject;

public class RateDialog extends DialogFragment {

    private Button send;
    private TextView scale_tv;
    private EditText note;
    private RatingBar ratingBar;

    private HandleDismissDialog from;
    private JSONObject result;
    private boolean ok = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(from == null)
            throw new IllegalStateException("You must call setListener() before create show the dialog");

        View v = inflater.inflate(R.layout.dialog_five_stars_rate, container, false);
        send = v.findViewById(R.id.rate_send);
        scale_tv = v.findViewById(R.id.rate_scale);
        note = v.findViewById(R.id.rate_note);
        ratingBar = v.findViewById(R.id.rate_stars);

        ratingBar.setOnRatingBarChangeListener((r, value, bool) -> changeValueAction());
        send.setOnClickListener(b -> sendAction());
        return v;
    }

    private void sendAction(){
        if(ok) {
            try {
                result = new JSONObject();
                result.put("value", (int) ratingBar.getRating());
                result.put("note", note.getText().toString());
            } catch (JSONException e) {
                dismiss();
            }
            ok = true;
        }
        dismiss();
    }

    private void changeValueAction() {
        switch ((int) ratingBar.getRating()) {
            case 1:
                scale_tv.setText(R.string.rate_one);
                break;
            case 2:
                scale_tv.setText(R.string.rate_two);
                break;
            case 3:
                scale_tv.setText(R.string.rate_three);
                break;
            case 4:
                scale_tv.setText(R.string.rate_four);
                break;
            case 5:
                scale_tv.setText(R.string.rate_five);
                break;
            default:
                scale_tv.setVisibility(View.GONE);
                return;
        }

        ok = true;
    }

    public void setListener(HandleDismissDialog listener){this.from = listener;}

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(!ok || result == null) {
            super.onDismiss(dialog);
            return;
        }

        from.handleOnDismiss(DialogType.RateDialog, result.toString());
    }
}
