package rana.jatin.core.adapter.recyclerview;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import rana.jatin.core.base.BaseViewHolder;

/*
*  BaseRecyclarAdapter is a super-powered {@link android.support.v7.widget.RecyclerView.Adapter<RecyclerView.ViewHolder> RecyclerView.Adapter}
*  to be used with {@link android.support.v7.widget.RecyclerView}
*/
public abstract class BaseRecyclarAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    public RecyclerClickListener listener;

    public void OnClickListener(RecyclerClickListener listener) {
        this.listener = listener;
    }

    public <T extends ViewDataBinding> T getDataBinding(LayoutInflater inflater, @LayoutRes int id, ViewGroup parent, boolean attachParent) {
        return DataBindingUtil.inflate(inflater, id, parent, attachParent);
    }
}
