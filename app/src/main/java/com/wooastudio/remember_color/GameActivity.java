package com.wooastudio.remember_color;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener, FailedDialog.ItemClickListener {

    private static final int COMPLETE_COUNT = 100;
    private static final int[][] LEVEL_COUNT = {
            {16, 25, 36, 49, 64},
            {10, 15, 20, 25, 30}
    };

    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private ArrayList<String> mItems;

    private FrameLayout mStartLayout;
    private TextView mCDTView;
    private CountDownTimer mCDT;

    private int mNumberOfColumns = 4;
    private int mLimitCount = 3;
    private int mCurrentLevel = 1;

    private Vibrator mVibrator;

    private ImageView mFirstStarOn;
    private ImageView mFirstStarOff;
    private ImageView mSecondStarOn;
    private ImageView mSecondStarOff;
    private ImageView mThirdStarOn;
    private ImageView mThirdStarOff;

    private TextView mCurrentLevelCount;

    private int mCurrentLevelRemainingCount = 0;
    private int mLevelCountPosition = 0;
    private int mItemCount = 1;

    private boolean mGoHome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.GRAY);
        }

        // set up the RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });
        mRecyclerView.addItemDecoration(new ItemDecoration(mNumberOfColumns, (int) getResources().getDimension(R.dimen.recycler_view_item_space)));

        mItems = new ArrayList<>();

        mAdapter = new MyRecyclerViewAdapter(this, mItems);
        mAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mAdapter);

        Point size = Util.getScreenSize(this);

        ViewGroup.LayoutParams layoutParams= mRecyclerView.getLayoutParams();
        layoutParams.height = size.x - (int) getResources().getDimension(R.dimen.recycler_view_width_padding);//40;
        layoutParams.width = size.x - (int) getResources().getDimension(R.dimen.recycler_view_width_padding);//50;
        mRecyclerView.setLayoutParams(layoutParams);

        for (int i = 0; i < mNumberOfColumns*mNumberOfColumns; i++) {
            mItems.add(Integer.toString(mItemCount++));
        }

        mAdapter.notifyDataSetChanged();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // star
        mFirstStarOn = findViewById(R.id.firstStarOn);
        mFirstStarOff = findViewById(R.id.firstStarOff);
        mSecondStarOn = findViewById(R.id.secondStartOn);
        mSecondStarOff = findViewById(R.id.secondStarOff);
        mThirdStarOn = findViewById(R.id.thirdStartOn);
        mThirdStarOff = findViewById(R.id.thirdStarOff);

        mCurrentLevelCount = findViewById(R.id.currentLevelCount);
        mCurrentLevelCount.setText(Integer.toString(mCurrentLevel));

        mCurrentLevelRemainingCount = LEVEL_COUNT[1][0] - 1;
        mLevelCountPosition = 0;

        mStartLayout = (FrameLayout) findViewById(R.id.startBtnLayout);
        mStartLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });

        mCDTView = findViewById(R.id.cdt);

        readyGame();
    }

    private void startCDT() {
        if (mCDT != null) mCDT.cancel();

        mCDTView.setText("");
        mCDT = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCDTView.setText(Long.toString(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                startGame();
            }
        };
        mCDT.start();
    }

    private void readyGame() {
        mCDTView.setVisibility(View.VISIBLE);
        mStartLayout.setVisibility(View.VISIBLE);
        startCDT();
        mAdapter.setGameState(MyRecyclerViewAdapter.GAME_STATE.READY);
        mAdapter.notifyDataSetChanged();

        mAdapter.setAnswerPos();
    }

    private void startGame() {
        mCDTView.setVisibility(View.GONE);
        mStartLayout.setVisibility(View.GONE);
        mCDT.cancel();
        mAdapter.setGameState(MyRecyclerViewAdapter.GAME_STATE.CLEAR);
        mAdapter.notifyDataSetChanged();
    }

    private void endGame() {
        mAdapter.setGameState(MyRecyclerViewAdapter.GAME_STATE.END);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position, boolean isOk, boolean isFinish) {

        if (mLimitCount == 0) {
            return;
        }

        if (mCurrentLevel == COMPLETE_COUNT) {
            FailedDialog dialog = new FailedDialog(GameActivity.this);
            dialog.setClickListener(GameActivity.this);
            dialog.setInfo(mCurrentLevel - 1);
            dialog.show();
        }

        if (isFinish == true) {
            long[] pattern = {70, 70, 70, 70};
            mVibrator.vibrate(pattern, -1);
            readyGame();

            mCurrentLevel++;
            mCurrentLevelCount.setText(Integer.toString(mCurrentLevel));

            if (mCurrentLevelRemainingCount == 0) {
                mNumberOfColumns++;

                int count = mItems.size();
                for (int i = 0; i < (mNumberOfColumns * mNumberOfColumns - count); i++) {
                    mItems.add(Integer.toString(mItemCount++));
                }

                mAdapter.notifyDataSetChanged();
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns));

                mLevelCountPosition++;
                mCurrentLevelRemainingCount = LEVEL_COUNT[1][mLevelCountPosition] - 1;

                ((ItemDecoration) mRecyclerView.getItemDecorationAt(0)).setSpanCount(mNumberOfColumns);
            } else {
                mCurrentLevelRemainingCount--;
                mAdapter.notifyDataSetChanged();
            }
            startCDT();
        }

        if (isOk == false) {
            mVibrator.vibrate(100);

            mLimitCount--;
            if (mLimitCount == 2) {
                mThirdStarOn.setVisibility(View.INVISIBLE);
                mThirdStarOff.setVisibility(View.VISIBLE);
            }
            else if (mLimitCount == 1) {
                mSecondStarOn.setVisibility(View.INVISIBLE);
                mSecondStarOff.setVisibility(View.VISIBLE);
            }

            if (mLimitCount == 0) {
                setEndGame();
            }
        }
    }

    private void setEndGame() {
        mLimitCount = 0;

        mFirstStarOn.setVisibility(View.INVISIBLE);
        mFirstStarOff.setVisibility(View.VISIBLE);
        mSecondStarOn.setVisibility(View.INVISIBLE);
        mSecondStarOff.setVisibility(View.VISIBLE);
        mThirdStarOn.setVisibility(View.INVISIBLE);
        mThirdStarOff.setVisibility(View.VISIBLE);

        endGame();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FailedDialog dialog = new FailedDialog(GameActivity.this);
                dialog.setClickListener(GameActivity.this);
                dialog.setInfo(mCurrentLevel - 1);
                dialog.show();
            }
        }, 2000);


        int highestLevel = 0;
        int latestLevel = mCurrentLevel - 1;

        Util.setConfigValue(this, "latestLevel", Integer.toString(latestLevel));

        if (Util.getConfigValue(this, "highestLevel") != null) {
            highestLevel = Integer.parseInt(Util.getConfigValue(this, "highestLevel"));
        }

        if (latestLevel > highestLevel) {
            Util.setConfigValue(this, "highestLevel", Integer.toString(latestLevel));
        }
    }

    @Override
    public void onDialogItemClick(View view, boolean isHome) {
        mGoHome = isHome;

        if (isHome) {
            finish();
        }
        else {
            reset();
        }
    }

    private void reset() {

        mItems.clear();
        mNumberOfColumns = 4;
        mLevelCountPosition = 0;
        mCurrentLevel = 1;
        mLimitCount = 3;
        mItemCount = 1;
        mCurrentLevelRemainingCount = 0;

        for (int i = 0; i < mNumberOfColumns*mNumberOfColumns; i++) {
            mItems.add(Integer.toString(mItemCount++));
        }
        readyGame();

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mNumberOfColumns));

        ((ItemDecoration) mRecyclerView.getItemDecorationAt(0)).setSpanCount(mNumberOfColumns);

        mCurrentLevelCount.setText(Integer.toString(mCurrentLevel));

        mCurrentLevelRemainingCount = LEVEL_COUNT[1][0] - 1;
        mLevelCountPosition = 0;

        mFirstStarOn.setVisibility(View.VISIBLE);
        mFirstStarOff.setVisibility(View.INVISIBLE);
        mSecondStarOn.setVisibility(View.VISIBLE);
        mSecondStarOff.setVisibility(View.INVISIBLE);
        mThirdStarOn.setVisibility(View.VISIBLE);
        mThirdStarOff.setVisibility(View.INVISIBLE);
        startCDT();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mCDT.cancel();
    }
}
