package com.github.forax.blog;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.function.Supplier;

class Template {
  static String page(Supplier<String> content) {
    return of(
  "<!DOCTYPE html>",
  "<html>",
  "  <head>",
  "    <meta charset='UTF-8'>",
  "    <meta name='viewport' content='width=device-width'>",
  "    <title>Remi Forax's Blog</title>",
  "    <link rel='stylesheet' id='twentythirteen-style-css'  href='style.css' type='text/css' media='all' />",
  "    <style type='text/css' id='twentythirteen-header-css'>",
  "    .site-header {",
  "      background: url(circle.png) no-repeat scroll top;",
  "      background-size: 1600px auto;",
  "    }",
  "    </style>",
  "    <link type='text/css' rel='stylesheet' href='styles/shCoreDefault.css'/>",
  "  </head>",
  "  <body class='home blog single-author'>",
  "    <div id='page' class='hfeed site'>",
  "      <header id='masthead' class='site-header' role='banner'>",
  "        <a class='home-link' href='index.html' title='forax's blog' rel='home'>",
  "          <h1 class='site-title'>Remi Forax's Blog</h1>",
  "          <h2 class='site-description'>Java, the JVM and stuff around</h2>",
  "        </a>",
  "      </header>",
  "      <div id='main' class='site-main'>",
  "        <div id='primary' class='content-area'>",
  "          <div id='content' class='site-content' role='main'>",
               content.get(),
  "          </div><!-- #content -->",
  "        </div><!-- #primary -->",
  "      </div><!-- #main -->",
  "    </div><!-- #page -->",
  "    <script type='text/javascript' src='scripts/shCore.js'></script>",
  "    <script type='text/javascript' src='scripts/shBrushJScript.js'></script>",
  "    <script type='text/javascript'>SyntaxHighlighter.all();</script>",
  "  </body>",
  "</html>"
  ).collect(joining("\n"));
  }
  
  static String article(String title, HashSet<String> tags, LocalDate date, String gist, String permalink, Supplier<String> content) {
    return of(
  "<article id='post' class='post type-post status-publish format-standard hentry category-uncategorized'>",
  "<header class='entry-header'>",
  "  <h1 class='entry-title'>",
  "    <a href='" + permalink + ".html' title='Permalink' rel='bookmark'>",
         title,
  "     </a>",
  "  </h1>",
  "  <div class='entry-meta'>",
  "    <span class='date'>",
  "      <time class='entry-date'>",
            date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
  "      </time>",
  "    </span>",
  "    <span class='categories-links'>",
         tags.stream().<String>flatMap(tag -> of(
             tag
         )).collect(joining("\n")),
  "    </span>",
  "  </div><!-- .entry-meta -->",
  "</header><!-- .entry-header -->",
  "<div class='entry-content'>",
  " <p>",
      content.get(),
  " </p>",
  "</div><!-- .entry-content -->",
  "<footer class='entry-meta'>",
  "   <div class='comments-link'>",
        (gist != null)?
          "<a href='https://gist.github.com/forax/" + gist + "' title='Comments'>Write a comment</a>": "",
  "   </div><!-- .comments-link -->",  
  "   <span id='comments'/>",
  "</footer><!-- .entry-meta -->",
  "</article><!-- #post -->",
  (gist != null)?
      "<script type='text/javascript' src='assets/ajax.js'></script>": "",
  (gist != null)?
    "<script type='text/javascript'>fetchComments(" + gist + ");</script>": ""
    ).collect(joining("\n"));
  }
}
