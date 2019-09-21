package com.wooastudio.remember_color;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FailedDialog extends Dialog {

    private int mLevel;

    private ItemClickListener mClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.failed_dialog);
        setCanceledOnTouchOutside(false);

        RelativeLayout replayButtonLayout = (RelativeLayout) findViewById(R.id.replayButtonLayout);
        replayButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    dismiss();
                    mClickListener.onDialogItemClick(v, false);
                }
            }
        });

        RelativeLayout homeButtonLayout = (RelativeLayout) findViewById(R.id.homeButtonLayout);
        homeButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    dismiss();
                    mClickListener.onDialogItemClick(v, true);
                }
            }
        });
    }

    public FailedDialog(Context context) {
        super(context);
    }

    public void setInfo(int level) {
        mLevel = level;
    }

    @Override
    public void show() {
        super.show();

        TextView currentRecordCount = (TextView) findViewById(R.id.currentRecordCount);
        currentRecordCount.setText(Integer.toString(mLevel));
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onDialogItemClick(View view, boolean isHome);
    }
}
