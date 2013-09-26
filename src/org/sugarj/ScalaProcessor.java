package org.sugarj;

import static org.sugarj.common.ATermCommands.getApplicationSubterm;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.Environment;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;
import org.sugarj.scala.ImportTermExtractor;
import org.sugarj.scala.ScalaCommands;
import org.sugarj.util.TermFinder;

/**
 * @author Florian Jakob &lt;f_jakob@rbg.informatik.tu-darmstadt.de&gt;
 */
public class ScalaProcessor extends AbstractBaseProcessor {

  private static final long serialVersionUID = 9026624278268457077L;

  private String moduleHeader;
  private List<String> imports = new LinkedList<String>();
  private List<String> body = new LinkedList<String>();

  private Path outFile;
  private String namespaceName;

  private IStrategoTerm ppTable;

  private ImportTermExtractor importTermExtractor = new ImportTermExtractor(this);

  @Override
  public String getGeneratedSource() {
    if (body.isEmpty()) {
      return "";
    }

    StringBuilder sourceBuilder = new StringBuilder();

    if (moduleHeader != null) {
      sourceBuilder.append(moduleHeader).append("\n\n");
    }

    sourceBuilder.append(StringCommands.printListSeparated(imports, "\n")).append("\n\n");
    sourceBuilder.append(StringCommands.printListSeparated(body, "\n"));
    String source = sourceBuilder.toString();

    return source;
  }

  @Override
  public Path getGeneratedSourceFile() {
    return outFile;
  }

  @Override
  public String getNamespace() {
    return namespaceName;
  }

  @Override
  public ScalaLanguage getLanguage() {
    return ScalaLanguage.getInstance();
  }

  /*
   * processing stuff follows here
   */
  @Override
  public void init(RelativePath sourceFile, Environment environment) {
    outFile = environment.createOutPath(FileCommands.dropExtension(sourceFile.getRelativePath()) + "." + ScalaLanguage.getInstance().getBaseFileExtension());
  }

  private void processNamespaceDecl(IStrategoTerm toplevelDecl) throws IOException {
    namespaceName = extractNameFromPackageDeclaration(toplevelDecl);
    moduleHeader = prettyPrint(toplevelDecl);
  }

  private String extractNameFromPackageDeclaration(IStrategoTerm toplevelDecl) {
    String packageDeclarationName = prettyPrint(getApplicationSubterm(toplevelDecl, "PackageDeclaration", 0));
    return packageDeclarationName;
  }

  @Override
  public List<String> processBaseDecl(IStrategoTerm toplevelDecl) throws IOException {
    if (getLanguage().isNamespaceDec(toplevelDecl)) {
      processNamespaceDecl(toplevelDecl);
      return Collections.emptyList();
    }

    String text = null;
    try {
      text = prettyPrint(toplevelDecl);
    } catch (NullPointerException e) {
      ATermCommands.setErrorMessage(toplevelDecl, "pretty printing Scala failed");
    }
    if (text != null)
      body.add(text);

    return Collections.emptyList();
  }

  private String fqnToPath(String string) {
    return string.replaceAll("\\.", File.separator);
  }

  @Override
  public String getModulePathOfImport(IStrategoTerm toplevelDecl) {
    IStrategoTerm importTerm = TermFinder.find("Import", toplevelDecl);
    String extracted = importTermExtractor.extractFirst(importTerm);
    String converted = fqnToPath(extracted);
    return converted;
  }

  @Override
  public void processModuleImport(IStrategoTerm toplevelDecl) throws IOException {
    imports.add(prettyPrint(toplevelDecl));
  }

  @Override
  public String getExtensionName(IStrategoTerm decl) throws IOException {
    IStrategoTerm scalaExtensionHead = getApplicationSubterm(decl, "ScalaExtension", 0);
    IStrategoTerm scalaId = getApplicationSubterm(scalaExtensionHead, "ScalaExtensionHead", 0);
    String extensionName = prettyPrint(scalaId);
    return extensionName;
  }

  public String prettyPrint(IStrategoTerm term) {
    if (ppTable == null)
      ppTable = ATermCommands.readPrettyPrintTable(getLanguage().ensureFile("org/sugarj/languages/Scala.pp").getAbsolutePath());

    String prettyPrint = ATermCommands.prettyPrint(ppTable, term, interp);
    return prettyPrint;
  }

  @Override
  public List<Path> compile(List<Path> outFiles, Path bin, List<Path> includePaths) throws IOException {
    return ScalaCommands.scalac(outFiles, bin, includePaths);
  }

  @Override
  public boolean isModuleExternallyResolvable(String relModulePath) {
    return ScalaCommands.isExternallyResolvable(relModulePath);
  }

  @Override
  public IStrategoTerm getExtensionBody(IStrategoTerm decl) {
    IStrategoTerm scalaExtensionBody = getApplicationSubterm(decl, "ScalaExtension", 1);
    return getApplicationSubterm(scalaExtensionBody, "ScalaExtensionBody", 0);
  }
}
