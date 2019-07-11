package com.uangel.svc.util;

import com.uangel.svc.event.EventEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TPSMonitor extends EventEmitter<TPSListener> {
    static final Logger log = LoggerFactory.getLogger(TPSMonitor.class);
    static final Timer timer = new Timer("TPSMonitor", true);
    

    private TPS[] tps;
    private TimerTask task;
    private AtomicBoolean stop=new AtomicBoolean(true);
    private long period=1000;

    public TPSMonitor(TPS ...tps) {
        this.tps = tps;
    }

    public TPSMonitor(long period/*ms*/, TPS ...tps) {
        this.tps = tps;
        this.period=period;
    }

    public TPS[] getTps() {
        return tps;
    }

    public void start() {
        if (stop.compareAndSet(true, false)) {
            Calendar now = Calendar.getInstance();
            int sec=now.get(Calendar.SECOND);
            int per=(int)(period/1000);
            int sec2=((sec+2)/per)*per+(per-1);
            now.add(Calendar.SECOND, (sec2-sec));
            now.clear(Calendar.MILLISECOND);

            task = new TimerTask() {
                public void run() {
                    try
                    {
                    Calendar t=Calendar.getInstance();
                    t.setTimeInMillis(scheduledExecutionTime());
                    t.add(Calendar.SECOND, -1);
                    t.clear(Calendar.MILLISECOND);
                    Date at = t.getTime();
                    long[][] N = new long[tps.length][];
                    for (int i = 0; i < N.length; i++)
                        N[i] = tps[i].getTpsPeakSum(at);
                    fireEvent(l -> l.read(at,
                               N));
                    } catch(Exception e)
                    {
                    log.error("TPSMonitor.start", e);
                    }


                }
            };
            timer.scheduleAtFixedRate(task,
                                      now.getTime(),
                                      period);
        }
    }

    public void stop() {
        if (stop.compareAndSet(false, true)) {
            if (task != null) {
                this.task.cancel();
                this.task = null;
            }
        }
    }


    public static void main1(String[] args) throws Exception {

//        TPS rxTps=new TPS(3);
//        TPS txTps=new TPS(3);
//        TPSMonitor tpsMonitor=new TPSMonitor(rxTps, txTps);
//        tpsMonitor.register(new TPSListener() {
//            @Override
//            public void read(Date at, long[]... tps) {
//                System.out.println("-----------------");
//                System.out.println("rxTps = "+tps[0][0]+" "+tps[0][1]+" "+tps[0][2]);
//                System.out.println("txTps = "+tps[1][0]+" "+tps[1][1]+" "+tps[1][2]);
//            }
//        });
//        tpsMonitor.start();
//
//        for (;;)
//        {
//            //System.out.println("111");
//            rxTps.count();
//            txTps.count();
//            Thread.sleep(10);
//        }



        TPS rxTps=new TPS(5);
        TPSMonitor tpsMonitor=new TPSMonitor(5000,rxTps);
        tpsMonitor.register(new TPSListener() {
            @Override
            public void read(Date at, long[]... tps) {
                Date now=new Date();
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                System.out.println(sdf.format(now));
                System.out.println("-----------------");
                System.out.println("rxTps = "+tps[0][0]);
            }

        });
        tpsMonitor.start();

        TPSGen tg=new TPSGen(300);
        tg.setExecutor(Executors.newSingleThreadExecutor());
        tg.start(()->()->rxTps.count());
//        for (;;)
//        {
//            //System.out.println("111");
//            rxTps.count();
//            Thread.sleep(10);
//        }
        Thread.sleep(11111111111L);
    }

//    public static void main(String[] args) throws Exception {
////
////        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
////        for (;;)
////        {
////
////            Calendar now = Calendar.getInstance();
////            System.out.println(sdf.format(now.getTime()));
////            int sec=now.get(Calendar.SECOND);
////            int sec2=((sec+2)/5)*5+4;
////            //System.out.println(sec+"-->"+sec2);
////            now.clear(Calendar.MILLISECOND);
////            now.add(Calendar.SECOND, (sec2-sec));
////            System.out.println("-->"+sdf.format(now.getTime()));
////            Thread.sleep(1000);
////        }
////    }

    public static void main(String[] args) throws Exception {

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        for (;;)
        {

            Calendar now = Calendar.getInstance();
            System.out.println(sdf.format(now.getTime()));
            int sec=now.get(Calendar.SECOND);
            int sec2=((sec+1)/5)*5+6;
            //System.out.println(sec+"-->"+sec2);
            now.clear(Calendar.MILLISECOND);
            now.add(Calendar.SECOND, (sec2-sec));
            System.out.println("-->"+sdf.format(now.getTime()));
            Thread.sleep(1000);
        }
    }
}
