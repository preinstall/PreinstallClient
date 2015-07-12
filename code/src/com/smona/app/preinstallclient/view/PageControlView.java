package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.view.ScrollLayout.OnScreenChangeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PageControlView extends LinearLayout {
    private Context context;

    private int count;

    public void bindScrollViewGroup(ScrollLayout scrollViewGroup) {
        this.count = scrollViewGroup.getChildCount();
        System.out.println("count=" + count);
        generatePageControl(scrollViewGroup.getCurScreen());

        scrollViewGroup.setOnScreenChangeListener(new OnScreenChangeListener() {

            public void onScreenChange(int currentIndex) {
                // TODO Auto-generated method stub
                generatePageControl(currentIndex);
            }
        });
    }

    public PageControlView(Context context) {
        super(context);
        this.init(context);
    }

    public PageControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    private void generatePageControl(int currentIndex) {
        this.removeAllViews();

        int pageNum = 6; // ��ʾ���ٸ�
        int pageNo = currentIndex + 1; // �ڼ�ҳ
        int pageSum = this.count; // �ܹ�����ҳ

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        param.topMargin = 20;
        if (pageSum > 1) {
            int currentNum = (pageNo % pageNum == 0 ? (pageNo / pageNum) - 1
                    : (int) (pageNo / pageNum)) * pageNum;

            if (currentNum < 0)
                currentNum = 0;

            if (pageNo > pageNum) {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.ico_y_f);
                this.addView(imageView, param);
            }

            for (int i = 0; i < pageNum; i++) {
                if ((currentNum + i + 1) > pageSum || pageSum < 2)
                    break;

                ImageView imageView = new ImageView(context);
                if (currentNum + i + 1 == pageNo) {
                    imageView.setImageResource(R.drawable.ico_y_on);
                } else {
                    imageView.setImageResource(R.drawable.ico_y_f);
                }
                this.addView(imageView, param);
            }

            if (pageSum > (currentNum + pageNum)) {
                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.ico_y_f);
                this.addView(imageView, param);
            }
        }
    }
}