package com.github.forax.blog;

import static com.github.forax.blog.UnsafeIO.unsafeProc;
import static com.github.forax.blog.UnsafeIO.unsafeFun;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Stream.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.pegdown.Extensions.FENCED_CODE_BLOCKS;
import static org.pegdown.Extensions.HARDWRAPS;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.RootNode;

public class Main {
  private static final Pattern PATTERN = Pattern.compile("[^\\w]"); 
  private static void populate(String line, Set<String> allTags, HashSet<String> tags) {
    PATTERN.splitAsStream(line).<String>map(String::toLowerCase).filter(allTags::contains).forEach(tags::add);
  }
  
  private static LocalDate extractDate(Path path) {
    String filename = path.getFileName().toString();
    String[] components =  filename.split("-");
    if (components.length != 5) {
      throw new IllegalArgumentException("wrong format: year-month-day-gist-title" + filename);
    }
    return LocalDate.parse(components[0] + '-'+ components[1] + '-' + components[2]);
  }
  
  public static void main(String[] args) throws IOException {
    Path posts = get("posts");
    Path site = get("site");
    Set<String> allTags = of("java", "jvm", "lambda").collect(toSet());
    
    PegDownProcessor processor = new PegDownProcessor(HARDWRAPS|FENCED_CODE_BLOCKS);
    
    Map<LocalDate, Path> markdowns =
        walk(posts).filter(path -> path.getFileName().toString().endsWith(".md"))
                   .collect(toMap(Main::extractDate,
                                  identity(),
                                  (a, b) -> { throw new AssertionError(); },
                                  () -> new TreeMap<>(reverseOrder())));
    
    String index = Template.page(unsafeProc(indexBuilder -> {
      String feed = Template.RSSfeed(unsafeProc(itemBuilder -> {
        markdowns.forEach(unsafeProc((date, path) -> {
          HashSet<String> tags = new HashSet<>();
        
          RootNode root = processor.parseMarkdown(lines(path).peek(line -> populate(line, allTags, tags)).collect(joining("\n")).toCharArray());
          String content = new ToHtmlSerializer(new LinkRenderer(), emptyMap()).toHtml(root);
          
          String pathname = path.getFileName().toString();
          String filename = pathname.substring(0, pathname.length() - ".md".length());
          String[] components = filename.split("-");
          
          String title = components[4].replace('_', ' ');
          
          String page = Template.page(builder -> 
            builder.append(Template.article(title, tags, date, components[3], filename, () -> content)));
          write(site.resolve(filename + ".html"), asList(page));
          System.out.println(filename + " written with tags " + tags);
          
          indexBuilder.append(Template.article(title, tags, date, null, filename, () -> Summary.summary(root, true)));
          itemBuilder.append(Template.RSSItem(title, Summary.summary(root, false), date, filename));
        }));
      }));
      write(site.resolve("rss.xml"), asList(feed));
    }));
    write(site.resolve("index.html"), asList(index));
  }
}
