package top.nomelin.cometpan.listener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePublisher {

    private final static String EXCHANGE_NAME = "announcements"; // 定义交换机名称
    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    private ConnectionFactory factory;

    public MessagePublisher(String host, String username, String password, String virtualHost) {
        factory = new ConnectionFactory();
        factory.setHost(host); // 设置RabbitMQ服务器的IP地址
        factory.setUsername(username); // 设置RabbitMQ的用户名
        factory.setPassword(password); // 设置RabbitMQ的密码
        factory.setVirtualHost(virtualHost); // 设置RabbitMQ的虚拟主机
    }

    public void publishMessage(String message) {
        try (Connection connection = factory.newConnection(); // 建立连接
             Channel channel = connection.createChannel()) { // 创建信道
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout"); // 声明一个交换机，类型为fanout

            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8")); // 发布消息到交换机
            logger.info(" [x] Sent '" + message + "'"); // 打印消息发送日志
        } catch (Exception e) {
            logger.error("Failed to publish message", e);
        }
    }
}
