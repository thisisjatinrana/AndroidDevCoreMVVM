package rana.jatin.core.base;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.functions.Consumer;
import rana.jatin.core.RxBus.RxBus;
import rana.jatin.core.model.Event;
import rana.jatin.core.util.FragmentUtil;
import rana.jatin.core.util.PermissionUtil;
import rana.jatin.core.util.ViewUtil;

/*
*  BaseFragment is a super-powered {@link android.support.v4.app.Fragment Fragment}
*  to be used with {@link rana.jatin.core.util.FragmentUtil}
*/
public abstract class BaseFragment<T extends ViewDataBinding, V extends BaseViewModel> extends Fragment {

    private T mViewDataBinding;
    private V mViewModel;
    private View mRootView;
    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        performDependencyInjection();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Subscribe for RxBus REFRESH event.
        //see {@link rana.jatin.core.model.Event#REFRESH}
        RxBus.getInstance().subscribe(Event.REFRESH.name(), this, String.class,refreshConsumer);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        mRootView = mViewDataBinding.getRoot();
        return mRootView;
    }

    public void performDependencyInjection() {
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();
        mViewModel.onViewCreated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(this);
    }

   /*
   * return arguments passed to the fragment using
   * {@link rana.jatin.core.util.FragmentUtil#setModel(Serializable object) setModel}
   * and {@link rana.jatin.core.util.FragmentUtil#setModel(Model object) setModel} methods.
   */
    public <T> T getModel() {
        Bundle args = getArguments();
        if (args == null)
            return null;

        return (T) args.getSerializable(Extras.MODEL.name());
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

    public T getViewDataBinding() {
        return mViewDataBinding;
    }


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
