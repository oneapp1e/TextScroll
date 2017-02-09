package com.mlr.textscroll;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mlr.utils.BaseActivity;

public class TextLinkInfoHolder {

    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Fields
    // ==========================================================================

    private TextView mContent;// 评论内容
    private View mRootView;

    BaseActivity mActivity;

    // ==========================================================================
    // Constructors
    // ==========================================================================
    public TextLinkInfoHolder(BaseActivity activity, TextLinkTipInfo data) {
        mActivity = activity;
        initView();
    }

    // ==========================================================================
    // Getters
    // ==========================================================================

    // ==========================================================================
    // Setters
    // ==========================================================================

    public void setContent(CharSequence content) {
        if (null != mContent) {
            mContent.setText(Html.fromHtml(content.toString()));
        }
    }

    // ==========================================================================
    // Methods
    // ==========================================================================

    public View initView() {
        mRootView = mActivity.inflate(R.layout.text_link_layout);
        if (null != mRootView) {
            mContent = (TextView) mRootView.findViewById(R.id.textLinkContent);
        }
        return mRootView;
    }

    public View getRootView() {
        return mRootView;
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
}
