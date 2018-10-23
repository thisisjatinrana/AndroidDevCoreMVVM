/*
 *  Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://mindorks.com/license/apache-v2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package rana.jatin.core.base;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.functions.Consumer;
import rana.jatin.core.R;
import rana.jatin.core.RxBus.RxBus;
import rana.jatin.core.model.Event;
import rana.jatin.core.util.FragmentUtil;
import rana.jatin.core.util.PermissionUtil;
import rana.jatin.core.util.ViewUtil;

public abstract class BaseDialog<T extends ViewDataBinding, V extends BaseViewModel> extends DialogFragment {

    private T mViewDataBinding;
    private V mViewModel;
    private View mRootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        performDependencyInjection();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        mRootView = mViewDataBinding.getRoot();
        mViewModel = getViewModel();
        //Subscribe for RxBus REFRESH event.
        //see {@link rana.jatin.core.model.Event#REFRESH}
        RxBus.getInstance().subscribe(Event.REFRESH.name(), this, String.class,refreshConsumer);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void performDependencyInjection() {
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void dismissDialog(String tag) {
           dismiss();
    }
    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    private Consumer<String> refreshConsumer=new Consumer<String>() {
        @Override
        public void accept(String classToRefresh) throws Exception {
            onRefresh(classToRefresh);
        }
    };

    // @param classToRefresh will be #getClass().getName()
    public abstract void onRefresh(String classToRefresh);

    public abstract boolean onBackPress(int fragmentCount);


    @Override
    public void onDestroyView() {
        mViewModel.onDestroyView();
        super.onDestroyView();
    }

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();
}
