/*
 * File Name: TextLinkAdapter.java 
 * History:
 * Created by wangyl on 2014-5-5
 */
package com.mlr.textscroll;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mlr.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class TextLinkAdapter extends BaseAdapter {

    // ==========================================================================
    // Constants
    // ==========================================================================

    // ==========================================================================
    // Fields
    // ==========================================================================
    private BaseActivity mActivity;
    private MarketViewFliper mViewFliper;
    private List<TextLinkTipInfo> mData = new ArrayList<TextLinkTipInfo>(5);

    // ==========================================================================
    // Constructors
    // ==========================================================================
    public TextLinkAdapter(MarketViewFliper viewFliper, BaseActivity act,
                           List<TextLinkTipInfo> data) {
        mActivity = act;
        mViewFliper = viewFliper;
        mData.addAll(data);
    }

    // ==========================================================================
    // Getters
    // ==========================================================================
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TextLinkTipInfo getItem(int position) {
        if (position >= mData.size()) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ==========================================================================
    // Setters
    // ==========================================================================
    public void setData(List<TextLinkTipInfo> data) {
        // 防止没有数据造成的UI跳动
        if (data == null || data.size() == 0) {
            return;
        }
        mData.clear();
        mData.addAll(data);
        if (mData.size() == 0) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }
        // notifyDataSetChanged();
    }

    // ==========================================================================
    // Methods
    // ==========================================================================
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextLinkInfoHolder holder;
        TextLinkTipInfo info = getItem(position);
        // LogUtils.e("TextLinkAdapter getview position " + position + ", info " + info);
        if (convertView == null) {
            holder = new TextLinkInfoHolder(mActivity, info);
            holder.getRootView().setTag(holder);
        } else {
            holder = (TextLinkInfoHolder) convertView.getTag();
        }
        if (info != null) {
            holder.setContent(info.getTextLinkContent());
        }
        return holder.getRootView();
    }

    // 控制文字链控件显示与隐藏
    private void setVisibility(int visibility) {
        if (mViewFliper.getParent() instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) mViewFliper.getParent();
            if (vg.getId() == R.id.text_link_lay) {
                vg.setVisibility(visibility);
                mViewFliper.setVisibility(visibility);
            }

        }
    }

    // ==========================================================================
    // Inner/Nested Classes
    // ==========================================================================
}
