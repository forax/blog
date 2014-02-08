package com.github.forax.blog;

import static java.nio.file.Paths.get;

import java.nio.file.Path;

/**
 */
public interface Config {
  Path     POSTS = get(System.getProperty("posts", "posts"));
  Path     SITE  = get(System.getProperty("site", "site"));
  String[] TAGS  = System.getProperty("tags", "java,jvm,lambda").split(",");
}
