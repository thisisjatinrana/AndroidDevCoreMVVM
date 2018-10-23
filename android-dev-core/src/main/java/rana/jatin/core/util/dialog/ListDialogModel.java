package rana.jatin.core.util.dialog;

import javax.inject.Inject;

import rana.jatin.core.base.BaseViewModel;
import rana.jatin.core.util.rx.SchedulerProvider;

/**
 * Created by Apetrail on 10/24/2017.
 */

public class ListDialogModel extends BaseViewModel<ListDialogBridge> {

    @Inject
    public ListDialogModel(SchedulerProvider provider) {
        super(provider);
    }

    public void showToast(){
           getBridge().getViewUtil().toast("Click");
    }

}
