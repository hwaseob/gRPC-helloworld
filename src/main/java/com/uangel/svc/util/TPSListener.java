package com.uangel.svc.util;

import java.util.Date;

public interface TPSListener {
    void read(Date at, long[]... tps);
}
