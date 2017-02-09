package com.mlr.textscroll;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mlr.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout rlRoot = (RelativeLayout) findViewById(R.id.activity_main);

        MarketViewFliper textLinkView = createTextLinkView();
        LinearLayout tipLay = new LinearLayout(this);
        tipLay.setOrientation(LinearLayout.HORIZONTAL);
        tipLay.setGravity(Gravity.CENTER_VERTICAL);
        tipLay.setId(R.id.text_link_lay);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip2px(50));
        tipLay.addView(textLinkView, lp);

        rlRoot.addView(tipLay);
    }


    /**
     * 创建可轮播的文字链控件
     *
     * @return
     */
    public MarketViewFliper createTextLinkView() {
        List<TextLinkTipInfo> textLinks = getTextLinkTipInfos();
        if (textLinks == null || textLinks.size() == 0) {
            return null;
        }

        if (textLinks.size() > 0) {
            MarketViewFliper<View> textLinkView = new MarketViewFliper<View>(this);
            textLinkView.setOffsetTop(0);
            TextLinkAdapter tlAdapter = new TextLinkAdapter(textLinkView, this, textLinks);
            textLinkView.setAdapter(tlAdapter);
            textLinkView.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_in));
            textLinkView.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_out));
            textLinkView.startFlipping();
            return textLinkView;
        }
        return null;
    }

    public List<TextLinkTipInfo> getTextLinkTipInfos() {
        List<TextLinkTipInfo> lists = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            TextLinkTipInfo textLinkTipInfo= new TextLinkTipInfo();
            textLinkTipInfo.setTextLinkContent("<div><font color=\"red\">测试滚动文字"+i+"</font></div>"
                    +"</br><div><font color=\"red\">测试滚动文字"+i+"</font></div>");
            lists.add(textLinkTipInfo);
        }
        return lists;
    }
}
