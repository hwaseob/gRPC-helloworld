package com.uangel.svc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class TPSGen {
    static final Logger log = LoggerFactory.getLogger(TPSGen.class);
    static final Timer timer = new Timer("TPSGen", true);

    private int tps;
    private TimerTask task;
    private Executor threadPoolExecutor;

    public TPSGen(int tps) {
        this.tps=tps;
    }

    public void start(Supplier<Runnable> r) {
        Calendar now = Calendar.getInstance();

        now.add(Calendar.SECOND, 1);
        now.clear(Calendar.MILLISECOND);


        int N= tps >= 10 ? tps/10 : tps;
        this.task = new TimerTask() {
            public void run() {
                try
                {
                    for (int i = 0; i < N; i++) {
                        if (threadPoolExecutor == null)
                            r.get().run();
                        else
                            threadPoolExecutor.execute(r.get());
                    }
                } catch (Exception t)
                {
                    log.error("TPSGen.start", t);
                }
            }
        };
        timer.scheduleAtFixedRate(task,
                                  now.getTime(),
                                  tps >= 10 ? 100L : 1000L);
    }

    public void stop() {
        if (task != null)
        {
            this.task.cancel();
            this.task=null;
        }
    }

    public Executor getExecutor() {
        return threadPoolExecutor;
    }

    public void setExecutor(Executor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

}
