package org.sugarj;

import static org.sugarj.common.ATermCommands.isApplication;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.ATermCommands.MatchError;
import org.sugarj.common.path.Path;

/**
 * @author Florian Jakob &lt;f_jakob@rbg.informatik.tu-darmstadt.de&gt;
 */

public class ScalaLanguage extends AbstractBaseLanguage {

	private ScalaLanguage() { }

	private static ScalaLanguage instance = new ScalaLanguage();

	public static ScalaLanguage getInstance() {
		return instance;
	}

	/**
	 * @see org.sugarj.AbstractBaseLanguage#createNewProcessor()
	 */
	@Override
	public AbstractBaseProcessor createNewProcessor() {
		return new ScalaProcessor();
	}

  @Override
  public String getVersion() {
    return "scala-0.2.0";
  }

  @Override
  public String getLanguageName() {
    return "Scala";
  }

  @Override
  public String getBinaryFileExtension() {
    return "class";
  }

  @Override
  public String getSugarFileExtension() {
    return "sugs";
  }

  @Override
  public String getBaseFileExtension() {
    return "scala";
  }

  @Override
  public List<Path> getPackagedGrammars() {
    List<Path> grammars = new LinkedList<Path>(super.getPackagedGrammars());
    grammars.add(ensureFile("org/sugarj/languages/SugarScala.def"));
    grammars.add(ensureFile("org/sugarj/languages/Scala.def"));
    return Collections.unmodifiableList(grammars);
  }

  @Override
  public Path getInitEditor() {
    return ensureFile("org/sugarj/scala/initEditor.serv");
  }

  @Override
  public String getInitEditorModuleName() {
    return "org/sugarj/scala/initEditor";
  }

  @Override
  public Path getInitGrammar() {
    return ensureFile("org/sugarj/scala/initGrammar.sdf");
  }

  @Override
  public String getInitGrammarModuleName() {
    return "org/sugarj/scala/initGrammar";
  }

  @Override
  public Path getInitTrans() {
    return ensureFile("org/sugarj/scala/initTrans.str");
  }

  @Override
  public String getInitTransModuleName() {
    return "org/sugarj/scala/initTrans";
  }

  @Override
  public boolean isExtensionDecl(IStrategoTerm decl) {
    return isApplication(decl, "ScalaExtension");
  }

  @Override
  public boolean isImportDecl(IStrategoTerm term) {
    return isApplicationOrTopStat(term, "Import");
  }

  @Override
  public boolean isBaseDecl(IStrategoTerm term) {
    return isApplicationOrTopStat(term, "TopTmplDef") ||
        isNamespaceDec(term);
  }

  @Override
  public boolean isPlainDecl(IStrategoTerm term) {
    return isApplication(term, "PlainDec");
  }

  public boolean isNamespaceDec(IStrategoTerm term) {
    return isApplication(term, "PackageDeclaration");
  }

  private boolean isApplicationOrTopStat(IStrategoTerm decl, String cons) {
    return isApplication(decl, cons) || isTopStatSemiSubterm(decl, cons);
  }

  private boolean isTopStatSemiSubterm(IStrategoTerm decl, String cons) {
    try {
      IStrategoTerm subterm =
          ATermCommands.getApplicationSubterm(decl, "TopStatSemi", 0);
      return isApplication(subterm, cons);
    } catch (NullPointerException | MatchError e) {
      return false;
    }
  }
}
