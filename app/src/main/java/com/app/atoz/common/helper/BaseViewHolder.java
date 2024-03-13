package com.app.atoz.common.helper;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

public class BaseViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding binding;

    public BaseViewHolder(View itemView) {
        super(itemView);
        setBinding(itemView);
    }

    @SuppressWarnings("unchecked")
    public <T extends ViewDataBinding> T getBinding() {
        return (T) binding;
    }

    private void setBinding(View itemView) {
        binding = DataBindingUtil.bind(itemView);
    }
}