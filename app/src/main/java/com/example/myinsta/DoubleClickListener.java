package com.example.myinsta;


import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {

    private static final long delay=300;
    private long lastclick=0;
    @Override
    public void onClick(View view) {
        long clicktime=System.currentTimeMillis();
        if(clicktime-lastclick<delay){
            onDoubeclick(view);
            lastclick=0;
        }
        lastclick=clicktime;
    }
    public abstract void onDoubeclick(View v);
}
