package rana.jatin.core.base;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.reactivex.functions.Consumer;
import rana.jatin.core.RxBus.RxBus;
import rana.jatin.core.etc.ContextWrapper;
import rana.jatin.core.util.FragmentUtil;
import rana.jatin.core.util.PermissionUtil;
import rana.jatin.core.util.ViewUtil;
import rana.jatin.core.model.Event;

/*
*  BaseActivity is a super-powered {@link android.support.v7.app.AppCompatActivity AppCompatActivity}
*  to be used with {@link rana.jatin.core.base.BaseIntent}
*/
public abstract class BaseActivity<T extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity implements HasSupportFragmentInjector {

    private String TAG = BaseActivity.class.getName();
    private T mViewDataBinding;
    private V mViewModel;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        performDependencyInjection();
        super.onCreate(savedInstanceState);
        performDataBinding();
        mViewModel.onViewCreated();
        //Subscribe for RxBus REFRESH event.
        //see {@link rana.jatin.core.model.Event#REFRESH}
        RxBus.getInstance().subscribe(Event.REFRESH.name(), this, String.class,refreshConsumer);
    }

    private void performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        this.mViewModel = mViewModel == null ? getViewModel() : mViewModel;
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.executePendingBindings();
    }

    public void performDependencyInjection() {
        AndroidInjection.inject(this);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentInjector;
    }

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();


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

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    protected final void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getBooleanExtra(Extras.CLEAR_STACK.name(), false)) { //TODO fix animation
            getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        onNewIntentInternal(intent);
    }

    private void onNewIntentInternal(Intent intent) {
        onIntent(intent, null);
        if (intent.getData() != null)
            onDataIntent(intent);
        if (intent.hasExtra(Extras.FRAGMENT.name()))
            onFragmentIntent(intent);
    }

    /**
     * Called for any new incoming intent
     *
     * @param intent
     * @param savedInstanceState
     */
    public void onIntent(Intent intent, Bundle savedInstanceState) {

    }

    /**
     * Called when new intent contains data
     *
     * @param intent
     */
    public void onDataIntent(Intent intent) {

    }

    /*
    * return Extras passed to the activity using
    * {@link BaseIntent#setModel(Serializable object) setModel}
    * and {@link BaseIntent#setModel(Model object) setModel} methods.
    */
    public <T> T getModel() {
        Bundle args = getIntent().getExtras();
        if (args == null)
            return null;

        return (T) args.getSerializable(Extras.MODEL.name());
    }

    /**
     * Called when new intent contains fragment instructions. See {@link BaseIntent}
     *
     * @param intent
     */
    public void onFragmentIntent(Intent intent) {
        boolean needRefresh = intent.getBooleanExtra(Extras.REFRESH.name(), false);

        Class<? extends Fragment> fragmentType = (Class<? extends Fragment>) intent.getSerializableExtra(Extras.FRAGMENT.name());
        Fragment fragment = instantiate(fragmentType);

        if (fragment == null)
            return;

        int container = intent.getIntExtra(Extras.CONTAINER.name(), 0);
        fragment.setArguments(intent.getExtras()); //copy extras to fragment
        fragment.setTargetFragment(getCurrentFragment(container), 0); //set current fragment as target

        if (fragment instanceof DialogFragment && ((DialogFragment) fragment).getShowsDialog()) {
            DialogFragment dialog = ((DialogFragment) fragment);
            dialog.show(getSupportFragmentManager(), null);
        } else {
            boolean skipStack = intent.getBooleanExtra(Extras.SKIP_STACK.name(), false);
            boolean replace = intent.getBooleanExtra(Extras.REPLACE.name(), false);

            boolean added = FragmentUtil.with(this).fragment(fragment, container, replace).skipStack(skipStack).commit();
            needRefresh = !added;
        }

        if (needRefresh)
            RxBus.getInstance().publish(Event.REFRESH.toString(), fragment.getClass().getName()); //refresh
    }

    private Fragment instantiate(Class<? extends Fragment> cls) {
        if (cls == null)
            return null;

        try {
            return cls.newInstance();
        } catch (Exception ignore) {
        }

        return null;
    }

    private Fragment getCurrentFragment(int container) {
        return getSupportFragmentManager().findFragmentById(container);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyHelper();
        mViewModel.onDestroyView();
        RxBus.getInstance().unregister(this);
    }

    private void destroyHelper(){
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = ContextWrapper.wrap(newBase, new Locale(Locale.ENGLISH.getLanguage()));
        super.attachBaseContext(context);
    }

    /*
    * Invoke abstract method {@link onBackPress(int) onBackPress}
    */
    @Override
    public void onBackPressed() {
        int i = getSupportFragmentManager().getBackStackEntryCount();
        boolean onBackPress=true;

        onBackPress = onBackPress(i);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        if (fragments != null && !fragments.isEmpty()) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseFragment && fragment.isVisible())
                    onBackPress = ((BaseFragment) fragment).onBackPress(i);
            }
        }
        // if false super.onBackPressed() will not be called because sometimes we need to perform some action on back press
        if (!onBackPress)
            return;

        super.onBackPressed();
    }


    /*
    * invoke method {@link android.support.v4.app.Fragment#onActivityResult(int ,int ,Intent) onActivityResult} of visible fragments.
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.isVisible())
                fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    /*
   * invoke method {@link android.support.v4.app.Fragment#onRequestPermissionsResult(int ,String[] ,int[]) onRequestPermissionsResult} of visible fragments.
   */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null && fragment.isVisible())
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // return true if you want to call super.onBackPressed()
    // if false super.onBackPressed() will not be called because sometimes we need to perform some action on back press
    public abstract boolean onBackPress(int fragmentCount);

    private Consumer<String> refreshConsumer=new Consumer<String>() {
        @Override
        public void accept(String classToRefresh) throws Exception {
               onRefresh(classToRefresh);
        }
    };

    // @param classToRefresh will be #getClass().getName()
    public abstract void onRefresh(String classToRefresh);
}
