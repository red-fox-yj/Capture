package com.example.dataget;

import java.util.ArrayList;
import java.util.List;

public final class constants {

    private constants() {
        // restrict instantiation
    }

    public static final double PI = 3.14159;
    public static final double PLANCK_CONSTANT = 6.62606896e-34;
    public static final List<String> expect_event_list = new ArrayList<String>();
    static {
        expect_event_list.add("com.taobao.android.detail.wrapper.activity.DetailActivity");
        expect_event_list.add("android.widget.FrameLayout");
        expect_event_list.add("android.widget.ListView");
        expect_event_list.add("android.widget.FrameLayout");
        expect_event_list.add("android.widget.FrameLayout");
        expect_event_list.add("android.widget.FrameLayout");
        expect_event_list.add("android.support.v7.widget.RecyclerView");
    }

}
