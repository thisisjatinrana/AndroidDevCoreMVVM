
package rana.jatin.core.base;

import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;

import io.reactivex.disposables.CompositeDisposable;
import rana.jatin.core.RxBus.RxBus;
import rana.jatin.core.util.rx.SchedulerProvider;

public abstract class BaseViewModel<N> extends ViewModel {

    private N bridge;
    private final ObservableBoolean mIsLoading = new ObservableBoolean(false);
    private CompositeDisposable mCompositeDisposable;
    private final SchedulerProvider mSchedulerProvider;

    public BaseViewModel(SchedulerProvider schedulerProvider) {
        this.mSchedulerProvider = schedulerProvider;
    }

    public void setBridge(N bridge) {
        this.bridge = bridge;
    }

    public N getBridge() {
        return bridge;
    }

    public void onViewCreated() {
        this.mCompositeDisposable = new CompositeDisposable();
    }

    public void onDestroyView() {
        mCompositeDisposable.dispose();
        RxBus.getInstance().unregister(this);
    }

    public SchedulerProvider getSchedulerProvider() {
        return mSchedulerProvider;
    }

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public ObservableBoolean getIsLoading() {
        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading.set(isLoading);
    }
}
