package com.qweex.smashuphelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class SimpleNotice {
    Activity activity;
    Object finishArg;
    OnCloseNotice onClose;
    public SimpleNotice(Activity activity) {
        this.activity = activity;
    }
    public void show(String text, final Object finishArg, OnCloseNotice onClose) {
        this.onClose = onClose;
        this.finishArg = finishArg;
        new AlertDialog.Builder(activity)
                    .setMessage(text)
                    .setNegativeButton("OK", dismiss)
                    .show();
    }
    public void showError(Exception error, OnCloseNotice onClose) {
        this.onClose = onClose;
        new AlertDialog.Builder(activity)
                .setTitle("Error")
                .setIcon(android.R.drawable.stat_notify_error)
                .setMessage(error.getMessage())
                .setNegativeButton("OK I guess", dismiss)
                .show();
        // TODO
    }

    DialogInterface.OnClickListener dismiss = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            if(onClose != null) {
                onClose.OnCloseNotice(finishArg);
            }
        }
    };
}
