package servlet;

import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the Java Servlet implementation for analyzing texts
 */
@WebServlet(name = "TextAnalysisServlet", urlPatterns = "/textbody/*")
public class TextAnalysisServlet extends HttpServlet {

  private static final Logger logger = LogManager.getLogger(TextAnalysisServlet.class.getName());
  private final static String QUEUE_NAME = "wordCountQueue";
  private ObjectPool<Channel> pool;

  /**
   * Initialize the RabbitMQ Channel pool during Servlet initialization
   *
   * @throws ServletException when Servlet can't be initialized
   */
  @Override
  public void init() throws ServletException {
    super.init();

    try {
      pool = initializePool();
    } catch (Exception e) {
      logger.info("Pool initialization failed");
    }
  }

  /**
   * Method to handle GET requests from client side
   *
   * @param request  http request
   * @param response http response
   * @throws IOException when PrintWriter has IO error
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    response.setStatus(HttpServletResponse.SC_OK);
    JsonObject jsonResp = new JsonObject();
    jsonResp.addProperty("message", "[GET] received");
    out.write(String.valueOf(jsonResp));
    out.flush();
  }

  /**
   * Method to handle a POST request It will validate the request and send messages to RabbitMQ
   * server to be processed Set response code to 404 if URL is not valid or empty, otherwise 200
   *
   * @param request  http request
   * @param response http response
   * @throws IOException when PrintWriter has IO error
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();

    PrintWriter out = response.getWriter();
    // check if we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.write("Missing parameters.");
      return;
    }

    String[] urlArr = urlPath.split("/");
    if (!isUrlValid(urlArr)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.write("Parameters are not valid.");
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      String requestBody = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      JsonObject jsonMap = LineProcessing.processLine(requestBody);

      if (sendMessageToQueue(jsonMap)) {
        out.write(jsonMap.size());
      } else {
        logger.info("Failed to send message to RabbitMQ");
      }
      out.flush();
    }
  }

  /**
   * A helper method to validate URL
   *
   * @param urlPath specified URL path
   * @return true if URL path is valid, false otherwise
   */
  private boolean isUrlValid(String[] urlPath) {
    int n = urlPath.length;
    return n == 2;
  }

  /**
   * Method to send POST request message to RabbitMQ
   *
   * @param message in the format of JsonObject to be sent to RabbitMQ
   * @return true if message was successfully sent, false otherwise
   */
  private boolean sendMessageToQueue(JsonObject message) {
    try {
      Channel channel = pool.borrowObject();
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel
          .basicPublish("", QUEUE_NAME, null, message.toString().getBytes(StandardCharsets.UTF_8));
      pool.returnObject(channel);
      return true;
    } catch (Exception e) {
      logger.info("Failed to send message to RabbitMQ");
      return false;
    }
  }

  /**
   * Method to initialize RabbitMQ Channel pool using ChannelFactory class
   *
   * @return Channel Object Pool
   * @throws Exception when initialization fails
   */
  private ObjectPool<Channel> initializePool() throws Exception {
    return new GenericObjectPool<>(new ChannelFactory());
  }
}
