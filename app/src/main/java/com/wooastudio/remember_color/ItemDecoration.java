package com.wooastudio.remember_color;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpanCount;
    private int mSpacing;
    private Paint mPaintRed;

    public ItemDecoration(int spanCount, int spacing) {
        mSpanCount = spanCount;
        mSpacing = spacing;

        mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRed.setColor(Color.RED);
        mPaintRed.setStyle(Paint.Style.STROKE);
        mPaintRed.setStrokeWidth(5);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        int column = position % mSpanCount;

        outRect.left = mSpacing - column * mSpacing / mSpanCount;
        outRect.right =(column + 1) * mSpacing / mSpanCount;
        if(position < mSpanCount){
            outRect.top = mSpacing;
        }
        outRect.bottom = mSpacing;
    }

    public void setSpanCount(int spanCount) {
        mSpanCount = spanCount;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
    }
}
