/*
 * Copyright 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jpdna.grpchello;

import com.google.common.util.concurrent.ListenableFuture;
import com.uangel.svc.util.TPS;
import com.uangel.svc.util.TPSGen;
import com.uangel.svc.util.TPSListener;
import com.uangel.svc.util.TPSMonitor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.Date;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class client_async_tps {
    //private static final Logger logger = Logger.getLogger(client_async_tps.class.getName());


    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        String host = "localhost";
//        String host = "192.168.7.22";
        int port = 50051;

        ManagedChannel channel;
        GreeterGrpc.GreeterFutureStub futureStub;

        channel = ManagedChannelBuilder.forAddress(host, port)
                                       .usePlaintext(true)
                                       .build();
        futureStub = GreeterGrpc.newFutureStub(channel);


        TPS tps=new TPS(2);
        TPSMonitor monitor=new TPSMonitor(tps);
        monitor.register(new TPSListener() {
            @Override
            public void read(Date at, long[]... tps) {
                //log.info()
                System.out.println("tps = "+tps[0][0]);
            }
        });
        monitor.start();


        int poolSize=20;
        ThreadPoolExecutor tpe1 = new ThreadPoolExecutor(poolSize,
                                                         poolSize,
                                                         60*1000, TimeUnit.MILLISECONDS,
                                                         new LinkedBlockingQueue<Runnable>(),
                                                         new BasicThreadFactory.Builder()
                                                                 .namingPattern("tpsgen-%d")
                                                                 .daemon(true)
                                                                 .priority(Thread.MAX_PRIORITY)
                                                                 .build());

        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize,
                                                         poolSize,
                                                         60*1000, TimeUnit.MILLISECONDS,
                                                         new LinkedBlockingQueue<Runnable>(),
                                                         new BasicThreadFactory.Builder()
                                                                 .namingPattern("executor-%d")
                                                                 .daemon(true)
                                                                 .priority(Thread.MAX_PRIORITY)
                                                                 .build());
        TPSGen tpsGen=new TPSGen(10);
        tpsGen.setExecutor(tpe1);
        Runnable r=() -> {
            try
            {
                //System.out.println("async result = " + f.get());
                tps.count();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        };
        tpsGen.start(()->{
            return () -> {

                String name = "world";
//            if (args.length > 0) {
//                name = args[0]; /* Use the arg as the name to greet if provided */
//            }
                //client.greet(user);

                //logger.info("Will try to greet " + name + " ...");
                HelloRequest request = HelloRequest.newBuilder().setName(name).build();
                ListenableFuture<HelloResponse> f = futureStub.sayHello(request);
                //logger.info("Greeting: " + response.getMessage());
//                Executor executor = Executors.newSingleThreadExecutor();
                f.addListener(r, executor);
            };
        });

//        client_async client = new client_async("localhost", 50051);
//        try {
        /* Access a service running on the local machine on port 50051 */


        Thread.sleep(1111111111L);
//        } finally {
        //client.shutdown();
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
//        }


    }
}
