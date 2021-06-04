//package servlet;
//
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.concurrent.TimeoutException;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public class Sender {
//
//  private final static String QUEUE_NAME = "wordCountQueue";
//
//  private static final Logger logger = LogManager.getLogger(Sender.class.getName());
//
//  public static void main(String[] args) throws IOException, TimeoutException {
//    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
//
//    final Connection connection = factory.newConnection(); // Put this in init()?
//    Runnable runnable = new Runnable() {
//      @Override
//      public void run() {
//        try {
//          Channel channel = connection.createChannel();
//          channel.queueDeclare(QUEUE_NAME, true, false, false, null);
//          String message = "Here's a msg";
//          channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
//
//          channel.close();
//          System.out.println("Messages sent");
//        } catch (IOException | TimeoutException e) {
//          logger.info(e.getMessage());
//        }
//      }
//    };
//    connection.close();
//  }
//
//}
