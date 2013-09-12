package org.sugarj.scala;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.ScalaProcessor;
import org.sugarj.common.ATermCommands;
import org.sugarj.util.TermFinder;

public class ImportTermExtractor {
  private ScalaProcessor proc;

  public ImportTermExtractor(ScalaProcessor proc) {
    this.proc = proc;
  }

  public List<String> extract(IStrategoTerm importTerm) {
    List<String> imports = new ArrayList<String>();
    List<IStrategoTerm> importExprs = TermFinder.select("ImportExpr", importTerm);
    for (IStrategoTerm importExpr: importExprs) {
      imports.add(fromImportExpr(importExpr));
    }
    return imports;
  }

  private String fromImportExpr(IStrategoTerm importExpr) {
    IStrategoAppl stableId = (IStrategoAppl) ATermCommands.getApplicationSubterm(importExpr, "ImportExpr", 0);
    String pretty = proc.prettyPrint(stableId);
    String compacted = compact(pretty);
    return compacted;
  }

  private String compact(String pretty) {
    return pretty.replaceAll(" ", "");
  }

  public String extractFirst(IStrategoTerm importTerm) {
    return extract(importTerm).get(0);
  }
}
