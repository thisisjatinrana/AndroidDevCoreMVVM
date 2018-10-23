
package rana.jatin.core.base;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import rana.jatin.core.model.Model;

public abstract class BaseViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {

    private T mViewDataBinding;
    private Model model;
    public BaseViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        this.mViewDataBinding = (T) binding;
    }


    public abstract void onBind(int position);

    public abstract void onDetached();

    public  abstract void onViewRecycled();

    public T getBinding() {
        return this.mViewDataBinding;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
