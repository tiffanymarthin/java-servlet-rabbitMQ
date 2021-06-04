package servlet;

import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet(name = "TextAnalysisServlet", urlPatterns = "/textbody/*")
public class TextAnalysisServlet extends HttpServlet {

  private static final Logger logger = LogManager.getLogger(TextAnalysisServlet.class.getName());
  private final static String QUEUE_NAME = "wordCountQueue";
  private Connection connection;

  @Override
  public void init() throws ServletException {
    super.init();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try {
      connection = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      logger.info(e.getMessage());
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    response.setStatus(HttpServletResponse.SC_OK);
    JsonObject jsonResp = new JsonObject();
    jsonResp.addProperty("message", "[GET] received");
    out.write(String.valueOf(jsonResp));
    out.flush();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();
//    logger.info("[POST] URL path: " + urlPath);
//    logger.info("[POST] URI path: " + request.getRequestURI());

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
//      JsonObject jsonResp = new JsonObject();
//      jsonResp.addProperty("message", 0);
//      out.write(String.valueOf(jsonResp));
      String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      JsonObject jsonMap = LineProcessing.processLine(requestBody);

      if (sendMessageToQueue(jsonMap)) {
        out.write("unique words: " + jsonMap.size());
      } else {
        out.write("message is not sent to queue");
      }
      out.flush();
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    int n = urlPath.length;
    return n == 2;
  }

  private boolean sendMessageToQueue(JsonObject message) {
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.basicPublish("", QUEUE_NAME, null, message.toString().getBytes(StandardCharsets.UTF_8));
      channel.close();
      return true;
    } catch (IOException | TimeoutException e) {
      logger.info(e.getMessage());
      return false;
    }
  }
}
