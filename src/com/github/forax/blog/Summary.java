package com.github.forax.blog;

import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;

public class Summary {
  static String summary(RootNode root) {
    StringBuilder builder = new StringBuilder();
    appendSummary(root, builder);
    return builder.toString();
  }
  private static boolean appendSummary(Node node, StringBuilder builder) {
    if (node instanceof TextNode) {
      String text = ((TextNode) node).getText();
      int index = text.indexOf('.');
      if (index != - 1) {
        builder.append(text, 0, index + 1);
        return true;
      }
      builder.append(text);
      if (builder.length() > 128) {
        builder.setLength(128);
        builder.append("...");
        return true;
      }
      builder.append("</br>");
      return false;
    }
    if (node instanceof SuperNode) {
      for(Node n: ((SuperNode)node).getChildren()) {
        if (appendSummary(n, builder)) {
          return true;
        }
      }
    }
    return false;
  }
  
}
