//package com.webank.weevent.processor.jobs;
//
//import org.apache.curator.test.TestingServer;
//import java.io.File;
//import java.io.IOException;
//
//public class EmbedZookeeperServer {
//
//    private static TestingServer testingServer;
//
//    /**
//     * 内存版的内嵌Zookeeper.
//     *
//     * @param port Zookeeper的通信端口号
//     */
//    public static void start(final int port) {
//        try {
//            testingServer = new TestingServer(port, new File(String.format("target/test_zk_data/%s/", System.nanoTime())));
//            // CHECKSTYLE:OFF
//        } catch (final Exception ex) {
//            // CHECKSTYLE:ON
//            ex.printStackTrace();
//        } finally {
//            Runtime.getRuntime().addShutdownHook(new Thread() {
//
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(1000L);
//                        testingServer.close();
//                    } catch (final InterruptedException | IOException ex) {
//                    }
//                }
//            });
//        }
//    }
//}