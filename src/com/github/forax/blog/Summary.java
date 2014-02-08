package com.github.forax.blog;

import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;

/**
 *  Extract a summary of a text in markdown by extracting either the first 128 characters
 *  or the first sentence (that stop with a '.').
 */
public class Summary {
  private static final int MAX_LENGTH = 128;
  
  static String summary(RootNode root, boolean isHtml) {
    StringBuilder builder = new StringBuilder();
    appendSummary(root, builder, isHtml);
    return builder.toString();
  }
  
  private static boolean appendSummary(Node node, StringBuilder builder, boolean isHtml) {
    if (node instanceof TextNode) {
      String text = ((TextNode) node).getText();
      int index = text.indexOf('.');
      if (index != - 1) {
        builder.append(text, 0, index + 1);
        return true;
      }
      builder.append(text);
      if (builder.length() > MAX_LENGTH) {
        builder.setLength(MAX_LENGTH);
        builder.append("...");
        return true;
      }
      builder.append(isHtml? "</br>": "\n");
      return false;
    }
    if (node instanceof SuperNode) {
      for(Node n: ((SuperNode)node).getChildren()) {
        if (appendSummary(n, builder, isHtml)) {
          return true;
        }
      }
    }
    return false;
  }
}
