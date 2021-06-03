package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class LineProcessing {

  public static JsonObject processLine(String line) {
    String[] arr = line.split(" ");
    Map<String, Integer> wordCount = new HashMap<>();
    for (String word : arr) {
      wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
    }

    Gson gson = new Gson();
    JsonObject jsonMap = gson.toJsonTree(wordCount).getAsJsonObject();
    System.out.println(jsonMap.size());
    return jsonMap;
  }

//  public static void main(String[] args) {
//    String line = "Testing % this testing testing word % ok loh deh";
//    JsonObject result = processLine(line);
//    System.out.println(result);
//    System.out.println(result.size());
//  }
}
