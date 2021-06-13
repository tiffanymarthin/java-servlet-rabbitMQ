package servlet;


import com.rabbitmq.client.Channel;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {
  private final Connection connection;

  ChannelFactory() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();

    Properties prop = new Properties();
    try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
      //load a properties file from class path, inside static method
      prop.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    factory.setUsername(prop.getProperty("rabbit.username"));
    factory.setPassword(prop.getProperty("rabbit.password"));
    factory.setHost(prop.getProperty("rabbit.ip"));
    connection = factory.newConnection();
  }

  @Override
  public Channel create() throws Exception {
    return connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }


}
