package com.uangel.svc.oam;

import com.uangel.svc.util.TimedHashMap;

import java.util.*;

public class Statistics {

    protected Map<Long/*time*/, Map<String/*key*/, Map<String/*item*/, Number>>> stat;
    protected TimeUnit timeUnit;
    protected int period;
    private int offset=0;
    public enum TimeUnit
    {
        SECOND,
        MIN,
//        SECOND5,
//        HOUR
    }

    List<String> fixedKeys=new LinkedList<>();
    List<String> fixedItems=new LinkedList<>();



    public Statistics(TimeUnit unit, int period) {
        this(unit, period, 0);
    }

    private Statistics(TimeUnit unit,
                      final int period,
                      int offset) {
        long timeout=period;
        if (TimeUnit.SECOND==unit)
            timeout*=1000;
        else if (TimeUnit.MIN==unit)
            timeout*=1000*60;
        timeout*=10;

        this.stat=Collections.synchronizedMap(new TimedHashMap<Long, Map<String, Map<String, Number>>>(timeout));
        this.timeUnit=unit;
        this.period=period;
        this.offset=offset;
    }

    public long getPeriod() {
        long p= this.period;
        if (TimeUnit.SECOND==timeUnit)
            p*=1000;
        else if (TimeUnit.MIN==timeUnit)
            p*=1000*60;
        return p;
    }

    public synchronized Number count(String key, String item) {
        return count(new Date(), key, item);
    }

    public synchronized Number count(Date time, String key, String item) {

        return count(time, key, item, 1);
    }

    public synchronized Number count(String key, String item, long count) {
        return count(new Date(), key, item, count);
    }

    public synchronized Number count(Date time, String key, String item, long count) {
        Map<String, Number> st=stat(time, key);
        Number n=st.get(item);
        if (n==null)
        {
            n = (long) 0;
        }

        n=n.longValue()+count;
        st.put(item, n);


        return st.get(item);
    }

    public synchronized Number max(String key, String item, Long v) {
        return max(new Date(), key, item, v);
    }

    public synchronized Number max(Date time, String key, String item, Long v) {

        Map<String, Number> st=stat(time, key);
        Number n=st.get(item);
        if (n==null)
            n= (long) 0;

        n=Math.max(n.longValue(), v);
        st.put(item, n);
        return n;
    }

    public synchronized Number sum(String key, String item, Long v) {
        return sum(new Date(), key, item, v);
    }

    public synchronized Number sum(Date time, String key, String item, Long v) {

        Map<String, Number> st=stat(time, key);
        Number n=st.get(item);
        if (n==null)
            n= (long) 0;

        if (v != null)
        {
            n=(n.longValue() + v);
        }
        st.put(item, n);

        return n;
    }

    public synchronized Number avg(String key, String item, Long v) {
        return avg(new Date(), key, item, v);
    }

    public synchronized Number avg(Date time, String key, String item, Long v) {


        Map<String, Number> st=stat(time, key);
        Number n=st.get(item);
        if (n==null)
            n= (float) 0;

        if (v != null)
        {
            Number count=count(time, key, item+".count");
            Number sum=sum(time, key, item+".sum", v);
            n=sum.floatValue()/count.floatValue();
        }
        st.put(item, n);
        return n;
    }


    public synchronized Map<String, Number> stat(Date time, String key) {
        Map<String,Map<String,Number>> r=stat(time);

        return/*Map<String,Number> m=*/r.computeIfAbsent(key, k1 -> {
            Map<String,Number> map=Collections.synchronizedMap(new HashMap<String, Number>());
            r.put(key, map);
            return map;
        });

    }

    public synchronized Map<String,Map<String, Number>> stat(Date time) {
        Long k= timeKey(time);


        return stat.computeIfAbsent(k, k1 -> {

            Map<String, Map<String, Number>> r= Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Number>>());

            for(String key : fixedKeys)
            {
                Map<String, Number> m;

                r.put(key, m=Collections.synchronizedMap(new HashMap<String, Number>()));

                for(String item : fixedItems)
                {
                    m.put(item, Long.valueOf(0L));
                }
            }

            return r;
        });

    }



    public Long timeKey(Date time) {


        if (timeUnit == TimeUnit.SECOND)
        {
            Calendar t=Calendar.getInstance();
            t.setTime(time);
            int sec=t.get(Calendar.SECOND);
            //0,1,2,3,4->0
            //5,6,7,8,9->5
            //10,11,12,13,14->10
            t.set(Calendar.SECOND, (sec/period)*period + offset);
            t.clear(Calendar.MILLISECOND);
            return t.getTime().getTime();
        } else
        {
            Calendar t=Calendar.getInstance();
            t.setTime(time);
            int min=t.get(Calendar.MINUTE);
            t.set(Calendar.MINUTE, (min/period)*period + offset);
            t.clear(Calendar.SECOND);
            t.clear(Calendar.MILLISECOND);
            return t.getTime().getTime();

        }
    }




    public synchronized void clear() {
        stat.clear();
    }

    public Statistics fixedKey(String key) {
        fixedKeys.add(key);
        return this;
    }

    public Statistics fixedItem(String item) {
        fixedItems.add(item);
        return this;
    }


}


