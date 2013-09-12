package org.sugarj.scala;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sugarj.common.CommandExecution;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;

public class ScalaCommands {
  private static final String ARG_SEP = " ";
  private static final String OS_NAME_WIN_IDENT = "win";
  private static final String OS_NAME_PROPERTY = "os.name";
  private static final String CLASSPATH_SEP = ":";
  private static final String CLASSPATH_SEP_WIN = ";";
  private static final String CLASSPATH_PARAM = "-classpath";
  private static final String DIR_PARAM = "-d";
  private static final String VERBOSE_FLAG = "-verbose";
  private static final String SCALAC = "scalac";

  private static boolean DEBUG = true;

  public static List<Path> scalac(List<Path> outFiles, Path bin, List<Path> includePaths) {
    List<String> args = buildArgs(outFiles, bin, includePaths);
    String[][] output = new CommandExecution(!DEBUG).execute(args.toArray(new String[args.size()]));
    String[] stderr = output[1];
    List<Path> generatedFiles = parseForWrittenFiles(stderr);
    return generatedFiles;
  }

  private static List<String> buildArgs(List<Path> outFiles, Path bin, List<Path> includePaths) {
    List<String> args = new LinkedList<String>();
    args.add(SCALAC);
    args.add(VERBOSE_FLAG);
    args.add(DIR_PARAM);
    args.add(bin.getAbsolutePath());
    args.add(CLASSPATH_PARAM);
    args.add(toClassPath(includePaths));
    args.add(implode(outFiles, ARG_SEP));
    return args;
  }

  private static List<Path> parseForWrittenFiles(String[] input) {
    List<Path> paths = new LinkedList<Path>();
    Pattern pattern = Pattern.compile("^\\[wrote '[^']*' to (.*)\\]$");
    for (String line: input) {
      Matcher m = pattern.matcher(line);
      if (m.matches()) {
        paths.add(new AbsolutePath(m.group(1)));
      }
    }
    return paths;
  }

  private static String toClassPath(List<Path> includePaths) {
    String pathSep = determineClassPathSeparator();
    return implode(includePaths, pathSep);
  }

  private static String determineClassPathSeparator() {
    String os = System.getProperty(OS_NAME_PROPERTY).toLowerCase();
    if (os.contains(OS_NAME_WIN_IDENT)) {
      return CLASSPATH_SEP_WIN;
    } else {
      return CLASSPATH_SEP;
    }
  }

  private static String implode(Iterable<? extends Object> iterable, String sep) {
    StringBuilder b = new StringBuilder();
    Iterator<? extends Object> it = iterable.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      b.append(o.toString());
      if (it.hasNext()) {
        b.append(sep);
      }
    }
    return b.toString();
  }
}
