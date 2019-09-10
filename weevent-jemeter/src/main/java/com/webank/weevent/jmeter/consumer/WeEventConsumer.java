package com.webank.weevent.jmeter.consumer;


/**
 * WeEvent consumer performance.
 *
 * @author matthewliu@webank.com
 * @since 2018/11/27
 */
 /*@Slf4j
public class WeEventConsumer extends AbstractJavaSamplerClient {
   private String topic;
    @Autowired
    private static IWeEventClient weEventClient;


    // 每 个压测线程启动时跑一次
    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        try {
            this.topic = context.getParameter("topic");
            boolean result = this.weEventClient.open(this.topic);
            getNewLogger().info("open topic result: {}", result);
        } catch (Exception e) {
            getNewLogger().error("open Exception", e);
        }
    }

    // 每次runTest运行完执行
    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    // Jmeter GUI参数
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("topic", "com.webank.weevent.jmeter");
        return arguments;
    }

    // Jmeter跑一次runTest算一个事物，会重复跑
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("weEventClient");
        try {
            result.sampleStart();
            this.weEventClient.subscribe(this.topic, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {

                }

                @Override
                public void onException(Throwable e) {

                }
            });
            result.sampleEnd();
            result.setSuccessful(true);
        } catch (Exception e) {
            getNewLogger().error("publish Exception", e);
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage(e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) {
        String topic = "com.webank.irps.002";
        try {
            weEventClient.subscribe(topic, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    log.info(String.format("onEvent %s", event));
                }

                @Override
                public void onException(Throwable e) {
                    log.info(String.format("onException %s", e.getMessage()));
                    e.printStackTrace();
                }
            });

            Thread.sleep(10000);
        } catch (Exception e) {
            log.error("subscribe fail,error:{}",e.getMessage());
        }
    }
}*/
