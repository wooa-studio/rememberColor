package com.wooastudio.remember_color;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    enum GAME_STATE {
        READY,
        CLEAR,
        START,
        END
    }

    private Context mContext;
    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Random mRand;

    private int[] mIsOn;
    private GAME_STATE mGameState = GAME_STATE.READY;

    private int mAnswerCount = 0;
    private int mCurrentAnswerCount = 0;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList<String> data) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mData = data;
        mRand = new Random();
        mIsOn = new int[64];
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // VIew 크기 변경
        Point size = Util.getScreenSize((Activity) mContext);
        int itemCount = getItemCount();

        GridLayoutManager.LayoutParams layoutParams= (GridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();

        int space = (int) mContext.getResources().getDimension(R.dimen.recycler_view_item_space);
        int itemWidth = ((size.x - (int) mContext.getResources().getDimension(R.dimen.recycler_view_width_padding)) / (int) (Math.sqrt((double) itemCount)))
                - (space  + (space * 1/2));
        layoutParams.height = layoutParams.width = itemWidth;
        //
        if (mGameState == GAME_STATE.READY) {


            if (mIsOn[position] == 2) {
                holder.mItemView.setBackgroundColor(mContext.getResources().getColor(R.color.cornflowerBlue));
            }
            else {
                holder.mItemView.setBackgroundColor(Color.LTGRAY);
            }
        }
        else if (mGameState == GAME_STATE.CLEAR) {
            if (mIsOn[position] == 2) {
                colorAnimation(holder.mItemView, mContext.getResources().getColor(R.color.cornflowerBlue), Color.LTGRAY, 200);
            }
            else {
                holder.mItemView.setBackgroundColor(Color.LTGRAY);
            }

        }
        else if (mGameState == GAME_STATE.END) {
            if (mIsOn[position] == 2 || mIsOn[position] == 1) {
                holder.mItemView.setBackgroundColor(mContext.getResources().getColor(R.color.indianRed));
            }
            else {
                holder.mItemView.setBackgroundColor(Color.LTGRAY);
            }
        }
    }

    private void colorAnimation(final View view, int beforeColor, int afterColor, int duration) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), beforeColor, afterColor);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                view.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setAnswerPos() {
        for (int i = 0; i < mIsOn.length; i++) {
            mIsOn[i] = 0;
        }
        mCurrentAnswerCount = 0;
        mAnswerCount = 0;

        int answerNum = ((int) mData.size() / 3) + mRand.nextInt(((int) mData.size() / 3));
        for (int i = 0; i < answerNum; i++) {
            int rand = mRand.nextInt(mData.size());
            if (mIsOn[rand] == 0) {
                mIsOn[rand] = 2;
                mAnswerCount++;
            }
        }
    }

    public void setGameState(GAME_STATE state) {
        mGameState = state;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mItemView;

        ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView.findViewById(R.id.item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mGameState == GAME_STATE.READY || mGameState == GAME_STATE.END) {
                return;
            }
            else if (mGameState == GAME_STATE.CLEAR) {
                setGameState(GAME_STATE.START);
            }

            boolean isOk = true;
            boolean isFinish = false;

            if (mIsOn[getAdapterPosition()] == 0) {
                isOk = false;
            }
            else if (mIsOn[getAdapterPosition()] == 2) {
                mIsOn[getAdapterPosition()] = 1;
                mCurrentAnswerCount++;
                //notifyItemChanged(getAdapterPosition());
                mItemView.setBackgroundColor(mContext.getResources().getColor(R.color.cadetBlue));
                if (mCurrentAnswerCount == mAnswerCount) {
                    mCurrentAnswerCount = 0;
                    mAnswerCount = 0;
                    isFinish = true;
                }
            }

            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition(), isOk, isFinish);
            }
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, boolean isOk, boolean isFinish);
    }
}