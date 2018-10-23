
package rana.jatin.core.util.rx;

import io.reactivex.Scheduler;


public interface SchedulerProvider {

    Scheduler ui();

    Scheduler computation();

    Scheduler io();

}
