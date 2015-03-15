package org.sugarj.scala;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sugarj.common.Exec;
import org.sugarj.common.Exec.ExecutionError;
import org.sugarj.common.Exec.ExecutionResult;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;

public class ScalaCommands {
  private static final String ARG_SEP = " ";
  private static final String CLASSPATH_PARAM = "-classpath";
  private static final String DIR_PARAM = "-d";
  private static final String VERBOSE_FLAG = "-verbose";
  private static final String SCALAC = "scalac";

  public static List<Path> scalac(List<Path> outFiles, Path bin,
      List<Path> includePaths) {
    try {
      ExecutionResult output = Exec.run(buildArgs(outFiles, bin, includePaths));
      List<Path> generatedFiles = parseForWrittenFiles(output.errMsgs);
      return generatedFiles;
    } catch (ExecutionError e){
      try {
        Exec.run(false, buildArgs(outFiles, bin, includePaths, false));
      } catch (ExecutionError _) {}
      return Collections.emptyList();
    }
  }

  public static boolean isExternallyResolvable(String modulename) {
    String modname = modulename.replace(File.separatorChar, '.');
    try {
      File tmpFile = File.createTempFile("extResolvable", ".scala");
      FileWriter fw = new FileWriter(tmpFile);
      fw.write(String.format("import %s", modname));
      fw.close();
      Exec.run(buildArgs(tmpFile));
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } catch (ExecutionError e) {
      return false;
    }
  }

  private static String[] buildArgs(File outFile) throws IOException {
    java.nio.file.Path tmpDir = Files.createTempDirectory(null);
    List<String> args = new LinkedList<String>();
    args.add(SCALAC);
    args.add(VERBOSE_FLAG);
    args.add(DIR_PARAM);
    args.add(tmpDir.toString());
    args.add(outFile.toString());
    return args.toArray(new String[args.size()]);
  }

  private static String[] buildArgs(List<Path> outFiles, Path bin,
      List<Path> includePaths) {
    return buildArgs(outFiles, bin, includePaths, true);
  }

  private static String[] buildArgs(List<Path> outFiles, Path bin,
      List<Path> includePaths, boolean verbose) {
    List<String> args = new LinkedList<String>();
    args.add(SCALAC);
    if (verbose) args.add(VERBOSE_FLAG);
    args.add(DIR_PARAM);
    args.add(bin.getAbsolutePath());
    args.add(CLASSPATH_PARAM);
    args.add(toClassPath(includePaths));
    args.add(implode(outFiles, ARG_SEP));
    return args.toArray(new String[args.size()]);
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
    String pathSep = Character.toString(File.pathSeparatorChar);
    return implode(includePaths, pathSep);
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
