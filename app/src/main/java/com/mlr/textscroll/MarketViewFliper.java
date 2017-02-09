/*
 * File Name: MarketViewFliper.java 
 * History:
 * Created by wangyl on 2014-4-29
 */
package com.mlr.textscroll;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.mlr.utils.LogUtils;

import java.util.LinkedList;
import java.util.List;

public class MarketViewFliper<T extends View> extends FrameLayout implements View.OnClickListener {

    // ==========================================================================
    // Constants
    // ==========================================================================
    private static final int DEFAULT_INTERVAL = 3000;

    // ==========================================================================
    // Fields
    // ==========================================================================
    private int mWhichChild = 0;
    private boolean mFirstTime = true;

    private boolean mAnimateFirstTime = true;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private OnItemClickListener mOnItemClickListener;

    protected BaseAdapter mAdapter;

    private AdapterDataSetObserver mDataSetObserver;

    private Recycler<T> mRecycler;

    private int mFlipInterval = DEFAULT_INTERVAL;
    private boolean mAutoStart = false;

    private boolean mRunning = false;
    private boolean mStarted = false;
    private boolean mVisible = false;
    private boolean mUserPresent = true;
    private boolean mAnimateSingleData = false;
    private boolean mReceiverRegister = false;

    private T mCurrentView = null;
    private int mOffsetTop = 0;
    private int mDisplayWidth;

    // 为论坛增加的字段
    private boolean mLoopShow = true;
    private int mMaxChildCount = 3;// child数量
    private boolean mReuseEnabled = true;
    private boolean mSupportPrevAnimation = false;// 是否支持 showPrevious 动画
    private Animation mPrevInAnimation;
    private Animation mPrevOutAnimation;
    private int mPreloadCount = 0;
    private SparseArray<T> mCachedView = null;
    private boolean mShowPrev = false;

    private boolean isForum = false;
    
    // ==========================================================================
    // Constructors
    // ==========================================================================

    public MarketViewFliper(Context context) {
        super(context);
        mDisplayWidth = context.getResources().getDisplayMetrics().widthPixels;
        mRecycler = new Recycler<T>();
    }

    public MarketViewFliper(Context context, int initIndex) {
        super(context);
        isForum  = true;
        mDisplayWidth = context.getResources().getDisplayMetrics().widthPixels;
        mRecycler = new Recycler<T>();
        mCachedView = new SparseArray<T>();
        mWhichChild = initIndex;

    }
    
    // ==========================================================================
    // Getters
    // ==========================================================================

    /**
     * Returns the index of the currently displayed child view.
     */
    public int getDisplayedChild() {
        return mWhichChild;
    }

    public int getFliperViewCnt() {
        if (mAdapter == null) {
            return 0;
        }
        return mAdapter.getCount();
    }

    // ==========================================================================
    // Setters
    // ==========================================================================

    public void setAdapter(BaseAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        setOnClickListener(this);
    }
    
    // 为论坛增加的set方法
    public void setLoopShow(boolean loopShow) {
        mLoopShow = loopShow;
    }
    
    public void setReuseEnabled(boolean enable) {
        mReuseEnabled = enable;
    }
    
    public void setPreloadCount(int count) {
        mPreloadCount = count;
    }
    
    public void setPrevAnimation(Animation inAnimation, Animation outAnimation) {
        mPrevInAnimation = inAnimation;
        mPrevOutAnimation = outAnimation;
        mSupportPrevAnimation = true;
    }
    
    // ==========================================================================
    // Methods
    // ==========================================================================

    /**
     * Manually shows the next child.
     */
    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }

    /**
     * Manually shows the previous child.
     */
    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }

    /**
     * Sets which child view will be displayed.
     * 
     * @param whichChild
     *            the index of the child view to display
     */
    public void setDisplayedChild(int whichChild) {
        // mWhichChild = whichChild;
        if (whichChild >= getFliperViewCnt()) {
            whichChild = 0;
        } else if (whichChild < 0) {
            whichChild = getFliperViewCnt() - 1;
        }
        boolean hasFocus = getFocusedChild() != null;
        // This will clear old focus if we had it
        showOnly(whichChild);
        if (hasFocus) {
            // Try to retake focus if we had it
            requestFocus(FOCUS_FORWARD);
        }
    }

    /**
     * 为论坛的帖子详情写的翻页的方法
     * Manually shows the next child.
     */
    public void showPostNext() {
        mShowPrev = false;
        prepareDisplayedChild(mWhichChild + 1, false);
    }

    /**
     * Manually shows the next child.
     */
    public void showPost(int childIndex) {
        if (childIndex != mWhichChild) {
            prepareDisplayedChild(childIndex, childIndex < mWhichChild);
        }
    }

    /**
     * Manually shows the previous child.
     */
    public void showPostPrevious() {
        mShowPrev = true;
        prepareDisplayedChild(mWhichChild - 1, true);
    }
    
    /**
     * Sets which child view will be displayed.
     * 
     * @param whichChild
     *            the index of the child view to display
     */
    public void prepareDisplayedChild(int whichChild, boolean isShowPrev) {
        // mWhichChild = whichChild;
        if (whichChild >= getFliperViewCnt()) {
            if (!mLoopShow) {
                return;
            }
            whichChild = 0;
        } else if (whichChild < 0) {
            if (!mLoopShow) {
                return;
            }
            whichChild = getFliperViewCnt() - 1;
        }
        boolean hasFocus = getFocusedChild() != null;
        // This will clear old focus if we had it
        showPostOnly(whichChild, isShowPrev);
        if (hasFocus) {
            // Try to retake focus if we had it
            requestFocus(FOCUS_FORWARD);
        }
    }
    
    public void setAnimateSingleData(boolean animate) {
        mAnimateSingleData = animate;
    }

    private boolean showAnimate() {
        return mAnimateSingleData || mAdapter.getCount() > 1;
    }

    private boolean showPostAnimate() {
        return mInited && (mAnimateSingleData || mAdapter.getCount() > 1);
    }
    
    /**
     * Shows only the specified child. The other displays Views exit the screen, optionally with the with the
     * {@link #getOutAnimation() out animation} and the specified child enters the screen, optionally with the
     * {@link #getInAnimation() in animation}.
     * 
     * @param childIndex
     *            The index of the child to be shown.
     * @param animate
     *            Whether or not to use the in and out animations, defaults to true.
     */
    void showOnly(final int childIndex, boolean animate) {
        if (!isShown()) {
            return;
        }
        final int count = getChildCount();
        // clear visible childs state
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                if (animate && mOutAnimation != null && child.getVisibility() == View.VISIBLE && showAnimate()) {
                    child.startAnimation(mOutAnimation);
                } else if (child.getAnimation() == mInAnimation) {
                    child.clearAnimation();
                }
                child.setVisibility(View.INVISIBLE);
            }
        }
        final T nextChild = makeAndAddView(childIndex);
        if (nextChild != null) {
            if (animate && mInAnimation != null) {
                if (showAnimate()) {
                    nextChild.startAnimation(mInAnimation);
                } else {
                    stopFlipping();
                }
            }
            nextChild.setVisibility(View.VISIBLE);
            recycle(mCurrentView);
            mFirstTime = false;
            mWhichChild = childIndex;
            mCurrentView = nextChild;
        }

    }

    void showPostOnly(int childIndex, boolean showPrev) {
        final boolean animate = (!mFirstTime || mAnimateFirstTime);
        showOnly(childIndex, animate, showPrev);
    }
    
    void showOnly(final int childIndex, boolean animate, boolean isShowPrev) {
        if (!isShown()) {
            return;
        }
        Animation outAnim = mOutAnimation;
        Animation inAnim = mInAnimation;
        if (mSupportPrevAnimation && animate) {
            // show previous animation
            if (isShowPrev) {
                outAnim = mPrevOutAnimation;
                inAnim = mPrevInAnimation;
            }
        }

        final int count = getChildCount();
        // clear visible childs state
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                if (animate && outAnim != null && child.getVisibility() == View.VISIBLE && showPostAnimate()) {
                    child.startAnimation(outAnim);
                } else if (child.getAnimation() == inAnim) {
                    child.clearAnimation();
                }
                child.setVisibility(View.GONE);
            }
        }
        final T nextChild = makeAndAddView(childIndex, false);
        if (nextChild != null) {
            if (animate && inAnim != null) {
                if (showAnimate() && getChildCount() > 1) {
                    nextChild.startAnimation(inAnim);
                } else {
                    stopFlipping();
                }
            }
            nextChild.setVisibility(View.VISIBLE);
            if (mReuseEnabled) {
                recycle(mCurrentView);
            }
            mFirstTime = false;
            mWhichChild = childIndex;
            mCurrentView = nextChild;
            // preloadView(isShowPrev);
            if (!mInited) {
                mInited = true;
            }
        }
    }
    
    public boolean isShown() {
        int[] coord = new int[2];
        try {
            getLocationOnScreen(coord);
        } catch (Throwable tr) {
            // ignored
        }
        // 不在当前屏幕显示的不自动播放；被遮盖住了 不自动播放；TODO offset bottom maybe required.
        return coord[0] >= 0 && coord[1] >= mOffsetTop && coord[0] < mDisplayWidth && super.isShown();

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.FrameLayout#onSizeChanged(int, int, int, int)
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 记录屏幕的宽度
        mDisplayWidth = getContext().getResources().getDisplayMetrics().widthPixels;
    }

    private boolean mInited = false;

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.FrameLayout#onLayout(boolean, int, int, int, int)
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mInited && getFliperViewCnt() > 0 && !isForum) {
            makeAndAddView(0);
            mInited = true;
        } else {
            super.onLayout(changed, left, top, right, bottom);
        }
    }

    // 论坛的预加载
    public void preloadView() {
        preloadView(mShowPrev);
    }

    public void preloadView(final boolean showPrev) {
        if (mPreloadCount > 0 && mWhichChild != getFliperViewCnt() - 1) {
            final int start;
            final int end;
            if (showPrev) {
                start = Math.max(mWhichChild - 1, 0);
                end = Math.max(mWhichChild - mPreloadCount, 0);
            } else {
                start = Math.min(mWhichChild + 1, mPreloadCount + mWhichChild);
                end = Math.min(getFliperViewCnt() - 1, mPreloadCount + mWhichChild);
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtils.e("Preload from " + start + " to " + end);
                    for (int i = start;;) {
                        makeAndAddView(i, true);
                        // onPreloadChild(makeAndAddView(i, true), i < mWhichChild);
                        LogUtils.e("preload index " + i);
                        // 添加end==-1的判断 因为这里getFliperViewCnt()获取到的页面数量pageCount为0 防止造成死循环
                        if (i == end || end == -1) {
                            break;
                        }
                        if (showPrev) {
                            i--;
                        } else {
                            i++;
                        }
                    }
                }
            }, 16);

        }
    }
    
    /**
     * Shows only the specified child. The other displays Views exit the screen with the {@link #getOutAnimation() out
     * animation} and the specified child enters the screen with the {@link #getInAnimation() in animation}.
     * 
     * @param childIndex
     *            The index of the child to be shown.
     */
    void showOnly(int childIndex) {
        final boolean animate = (!mFirstTime || mAnimateFirstTime);
        showOnly(childIndex, animate);
    }

    /**
     * Returns the View corresponding to the currently displayed child.
     * 
     * @return The View currently displayed.
     * 
     * @see #getDisplayedChild()
     */
    public T getCurrentView() {
        if (isForum) {
            return mCurrentView;
        }
        return (T) getChildAt(mWhichChild);
    }

    /**
     * Returns the current animation used to animate a View that enters the screen.
     * 
     * @return An Animation or null if none is set.
     * 
     * @see #setInAnimation(android.view.animation.Animation)
     * @see #setInAnimation(android.content.Context, int)
     */
    public Animation getInAnimation() {
        return mInAnimation;
    }

    /**
     * Specifies the animation used to animate a View that enters the screen.
     *
     * @param inAnimation
     *            The animation started when a View enters the screen.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.content.Context, int)
     */
    public void setInAnimation(Animation inAnimation) {
        mInAnimation = inAnimation;
    }

    /**
     * Returns the current animation used to animate a View that exits the screen.
     *
     * @return An Animation or null if none is set.
     *
     * @see #setOutAnimation(android.view.animation.Animation)
     * @see #setOutAnimation(android.content.Context, int)
     */
    public Animation getOutAnimation() {
        return mOutAnimation;
    }

    /**
     * Specifies the animation used to animate a View that exit the screen.
     *
     * @param outAnimation
     *            The animation started when a View exit the screen.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.content.Context, int)
     */
    public void setOutAnimation(Animation outAnimation) {
        mOutAnimation = outAnimation;
    }

    /**
     * Specifies the animation used to animate a View that enters the screen.
     *
     * @param context
     *            The application's environment.
     * @param resourceID
     *            The resource id of the animation.
     *
     * @see #getInAnimation()
     * @see #setInAnimation(android.view.animation.Animation)
     */
    public void setInAnimation(Context context, int resourceID) {
        setInAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    /**
     * Specifies the animation used to animate a View that exit the screen.
     *
     * @param context
     *            The application's environment.
     * @param resourceID
     *            The resource id of the animation.
     *
     * @see #getOutAnimation()
     * @see #setOutAnimation(android.view.animation.Animation)
     */
    public void setOutAnimation(Context context, int resourceID) {
        setOutAnimation(AnimationUtils.loadAnimation(context, resourceID));
    }

    /**
     * Indicates whether the current View should be animated the first time the ViewAnimation is displayed.
     * 
     * @param animate
     *            True to animate the current View the first time it is displayed, false otherwise.
     */
    public void setAnimateFirstView(boolean animate) {
        mAnimateFirstTime = animate;
    }

    public void setMaxChildCount(int count) {
        mMaxChildCount = count;
    }
    
    @Override
    public int getBaseline() {
        return (getCurrentView() != null) ? getCurrentView().getBaseline() : super.getBaseline();
    }

    public T getChild(int index) {
        int offset = index;

        if (mMaxChildCount <= index && getChildCount() == mMaxChildCount) {
            offset = index % mMaxChildCount;
        }
        return (T) getChildAt(offset);
    }
    
    @SuppressWarnings("deprecation")
    private T makeAndAddView(int position) {
        if (null == mAdapter || mAdapter.getCount() == 0) {
            return null;
        }
        T reuseView = reuse();
        T child = (T) mAdapter.getView(position, reuseView, this);
        if (null == child) {
            return null;
        }
        child.setFocusable(false);
        if (child.getParent() == null) {// 重用时 不需要重新measure layout
            LayoutParams lp;
            ViewGroup.LayoutParams setParams = child.getLayoutParams();
            if (setParams instanceof LayoutParams) {
                lp = (LayoutParams) setParams;
            } else if (setParams != null) {
                lp = new LayoutParams(setParams.width, setParams.height);
            } else {
                lp = (LayoutParams) generateDefaultLayoutParams();
            }
            int w = 0;
            int h = 0;
            int childWidthMeasureSpec;
            int childHeightMeasureSpec;
            if (lp.width >= 0) {
                w = lp.width;
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
            } else if (lp.width == LayoutParams.FILL_PARENT) {
                w = getWidth() - getPaddingLeft() - getPaddingRight();
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
            } else if (lp.width == LayoutParams.WRAP_CONTENT) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), MeasureSpec.AT_MOST);
            } else {
                w = 0;
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
            }

            if (lp.height >= 0) {
                h = lp.height;
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            } else if (lp.height == LayoutParams.FILL_PARENT) {
                h = getHeight() - getPaddingBottom() - getPaddingTop();
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            } else if (lp.height == LayoutParams.WRAP_CONTENT) {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredHeight(), MeasureSpec.AT_MOST);
            } else {
                h = 0;
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            }
            // Measure child
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            removeViewInLayout(child);
            addViewInLayout(child, getChildCount(), lp);
        }
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        return child;
    }
    
    @SuppressWarnings({ "deprecation", "unchecked" })
    private T makeAndAddView(int position, boolean preload) {
        if (null == mAdapter || mAdapter.getCount() == 0) {
            return null;
        }
        View child = null;
        if (mReuseEnabled) {
            child = mAdapter.getView(position, reuse(), this);
        } else {
            // if (getChildCount() > position) {
            int offset = position;

            if (mMaxChildCount <= position && getChildCount() == mMaxChildCount) {
                offset = position % mMaxChildCount;
            }
            child = mAdapter.getView(position, getChildAt(offset), this);
            if (!preload) {
                onShowChildChanged(position);
            }
            return (T) child;
            // } else {
            // child = mAdapter.getView(position, null, this);
            // }
            // if (preload) {
            // return null;
            // }

        }
        if (null == child) {
            return null;
        }
        if (!preload) {
            onShowChildChanged(position);
        }
        child.setFocusable(false);

        LayoutParams lp;
        ViewGroup.LayoutParams setParams = child.getLayoutParams();
        if (setParams instanceof LayoutParams) {
            lp = (LayoutParams) setParams;
        } else if (setParams != null) {
            lp = new LayoutParams(setParams.width, setParams.height);
        } else {
            lp = (LayoutParams) generateDefaultLayoutParams();
        }
        int w = 0;
        int h = 0;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        if (lp.width >= 0) {
            w = lp.width;
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        } else if (lp.width == LayoutParams.FILL_PARENT) {
            w = getWidth() - getPaddingLeft() - getPaddingRight();
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        } else if (lp.width == LayoutParams.WRAP_CONTENT) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), MeasureSpec.AT_MOST);
        } else {
            w = 0;
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        }

        if (lp.height >= 0) {
            h = lp.height;
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
        } else if (lp.height == LayoutParams.FILL_PARENT) {
            h = getHeight() - getPaddingBottom() - getPaddingTop();
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
        } else if (lp.height == LayoutParams.WRAP_CONTENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(this.getMeasuredHeight(), MeasureSpec.AT_MOST);
        } else {
            h = 0;
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
        }
        // Measure child
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        // removeViewInLayout(child);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        if (indexOfChild(child) == -1)
            addView(child, getChildCount(), lp);
        if (!preload) {
            onShowChildChanged(position);
        }
        return (T) child;
    }
    
    protected void onShowChildChanged(int index) {

    }
    
    private void recycle(T scrapView) {
        mRecycler.push(scrapView);
    }

    private T reuse() {
        return mRecycler.pull();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        if (!mReceiverRegister) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            try {
                getContext().registerReceiver(mReceiver, filter);
                mReceiverRegister = true;
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        if (mAutoStart) {
            // Automatically start when requested
            startFlipping();
        }
        if (mAdapter != null && mDataSetObserver == null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        if (mReceiverRegister) {
            try {
                getContext().unregisterReceiver(mReceiver);
                mReceiverRegister = false;
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        updateRunning();
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning(false);
    }

    /**
     * How long to wait before flipping to the next view
     * 
     * @param milliseconds
     *            time in milliseconds
     */
    public void setFlipInterval(int milliseconds) {
        mFlipInterval = milliseconds;
    }

    /**
     * Start a timer to cycle through child views
     */
    public void startFlipping() {
        mStarted = true;
        updateRunning();
    }

    /**
     * No more flips
     */
    public void stopFlipping() {
        mStarted = false;
        updateRunning();
    }

    /**
     * Internal method to start or stop dispatching flip {@link Message} based on {@link #mRunning} and
     * {@link #mVisible} state.
     */
    private void updateRunning() {
        updateRunning(true);
    }

    /**
     * Internal method to start or stop dispatching flip {@link Message} based on {@link #mRunning} and
     * {@link #mVisible} state.
     * 
     * @param flipNow
     *            Determines whether or not to execute the animation now, in addition to queuing future flips. If
     *            omitted, defaults to true.
     */
    private void updateRunning(boolean flipNow) {
        boolean running = mVisible && mStarted && mUserPresent;
        if (running != mRunning) {
            if (running) {
                if (isForum) {
                    showPostOnly(mWhichChild, flipNow);
                } else {
                    showOnly(mWhichChild, flipNow);
                }
                Message msg = mHandler.obtainMessage(FLIP_MSG);
                mHandler.sendMessageDelayed(msg, mFlipInterval);
            } else {
                mHandler.removeMessages(FLIP_MSG);
            }
            mRunning = running;
        }
    }

    /**
     * Returns true if the child views are flipping.
     */
    public boolean isFlipping() {
        return mStarted;
    }

    /**
     * Set if this view automatically calls {@link #startFlipping()} when it becomes attached to a window.
     */
    public void setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
    }

    /**
     * Returns true if this view automatically calls {@link #startFlipping()} when it becomes attached to a window.
     */
    public boolean isAutoStart() {
        return mAutoStart;
    }

    private final int FLIP_MSG = 1;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FLIP_MSG) {
                if (mRunning) {
                    showPostNext();
                    msg = obtainMessage(FLIP_MSG);
                    sendMessageDelayed(msg, mFlipInterval);
                }
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                updateRunning();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning(false);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null && mCurrentView != null) {
            mOnItemClickListener.onItemClick(mWhichChild, mCurrentView);
        }
    }

    /**
     * release resource
     */
    public void release() {
        if (mCachedView != null) {
            mCachedView.clear();
        }
        removeAllViews();

    }
    
    /**
     * @return the OffsetTop
     */
    public int getOffsetTop() {
        return mOffsetTop;
    }

    /**
     * @param OffsetTop
     *            the OffsetTop to set
     */
    public void setOffsetTop(int OffsetTop) {
        this.mOffsetTop = OffsetTop;
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
    private static class Recycler<T> {

        private List<T> mRecycledViews;

        Recycler() {
            mRecycledViews = new LinkedList<T>();
        }

        public void push(T view) {
            if (null != view && !mRecycledViews.contains(view)) {
                mRecycledViews.add(0, view);
            }
        }

        public T pull() {
            if (mRecycledViews.size() > 0) {
                return mRecycledViews.remove(0);
            } else {
                return null;
            }
        }

    }

    public static interface OnItemClickListener {

        public void onItemClick(int position, View clickedView);

    }

    class AdapterDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            requestLayout();
        }

    }

}
