package org.doremus.diaboloConverter.musResource;

import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.files.DiaboloRecord;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

public class F24_PublicationExpression extends DoremusResource {
  public F24_PublicationExpression(DiaboloRecord record) {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F24_Publication_Expression);
  }

  public F24_PublicationExpression add(F22_SelfContainedExpression expression) {
    this.resource.addProperty(CIDOC.P165_incorporates, expression.asResource());
    return this;
  }

}
