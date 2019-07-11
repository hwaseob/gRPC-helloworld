package com.uangel.svc.util;

import com.uangel.svc.oam.Statistics;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TPS extends Statistics {
    private int duration;

    public TPS(int duration/*seconds*/) {
        super(TimeUnit.SECOND, 1);
        fixedKey("tps");
        fixedItem("count");
        this.duration = duration;
    }

    public Number count() {
        return count("tps", "count");
    }

    public long getCount() {
        Calendar now = Calendar.getInstance();

        now.add(Calendar.SECOND, -1);
        now.clear(Calendar.MILLISECOND);

        return getCount(now.getTime());
    }

    public long getCount(Date date) {
        return getTpsPeakSum(date)[0];
    }

    public long[] getTpsPeakSum(Date date) {


        Calendar now = Calendar.getInstance();

        now.setTimeInMillis(date.getTime());
        int mod = duration;
        long sum=0;
        long peak=0;
        for (int i = 0; i< duration; i++)
        {
            long key=now.getTime().getTime();
            Map<String/*key*/, Map<String/*item*/, Number>> m=stat.get(key);
            if (m == null)
            {
                mod -=1;
                continue;
            }
            Map<String/*item*/, Number> dat=m.get("tps");
            Number count=dat.get("count");
            long val=count.longValue();
            peak = Math.max(peak, val);
            sum += val;
            now.set(Calendar.SECOND, now.get(Calendar.SECOND) - 1);
        }

        if (mod == 0)
            return new long[] {0,0,0};
        else
            return new long[] {sum/mod, peak, sum};
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


}

