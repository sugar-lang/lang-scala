package org.sugarj.scala;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.ScalaProcessor;
import org.sugarj.common.ATermCommands;

public class ImportTermExtractor {
  private ScalaProcessor proc;

  public ImportTermExtractor(ScalaProcessor proc) {
    this.proc = proc;
  }

  public List<String> extract(IStrategoTerm importTerm) {
    List<String> imports = new ArrayList<String>();
    IStrategoTerm importExprList = ATermCommands.getApplicationSubterm(importTerm, "Import", 0);
    for (IStrategoTerm child: importExprList) {
      if (ATermCommands.isApplication(child, "ImportExpr")) {
        imports.add(fromImportExpr(child));
      } else if (ATermCommands.isApplication(child, "WildcardImportExpr")) {
        imports.add(fromWildcardImportExpr(child));
      }
    }
    return imports;
  }

  private String fromWildcardImportExpr(IStrategoTerm wildcardImportExpr) {
    IStrategoAppl stableId = (IStrategoAppl) ATermCommands.getApplicationSubterm(wildcardImportExpr, "WildcardImportExpr", 0);
    String pretty = proc.prettyPrint(stableId);
    return pretty;
  }

  private String fromImportExpr(IStrategoTerm importExpr) {
    IStrategoAppl stableId = (IStrategoAppl) ATermCommands.getApplicationSubterm(importExpr, "ImportExpr", 0);
    String pretty = proc.prettyPrint(stableId);
    return pretty;
  }

  public String extractFirst(IStrategoTerm importTerm) {
    return extract(importTerm).get(0);
  }
}
