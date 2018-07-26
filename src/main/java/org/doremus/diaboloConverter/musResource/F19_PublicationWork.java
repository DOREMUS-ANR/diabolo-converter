package org.doremus.diaboloConverter.musResource;

import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.files.DiaboloRecord;
import org.doremus.ontology.FRBROO;

public class F19_PublicationWork extends DoremusResource {
  public F19_PublicationWork(DiaboloRecord record)  {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F19_Publication_Work);
  }

  public F19_PublicationWork add(F24_PublicationExpression expression) {
    this.resource.addProperty(FRBROO.R3_is_realised_in, expression.asResource());
    return this;
  }
}
