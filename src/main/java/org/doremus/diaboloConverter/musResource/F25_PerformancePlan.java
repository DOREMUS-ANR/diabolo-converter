package org.doremus.diaboloConverter.musResource;

import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URISyntaxException;

public class F25_PerformancePlan extends DoremusResource {

  public F25_PerformancePlan(Oeuvre record) {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F25_Performance_Plan);

    F20_PerformanceWork work = new F20_PerformanceWork(this.identifier);
    F28_ExpressionCreation planCreation = new F28_ExpressionCreation(record, true);
    work.add(this);
    planCreation.asResource()
      .addProperty(FRBROO.R17_created, this.resource)
      .addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    model.add(work.getModel());
    model.add(planCreation.getModel());
  }

  public F25_PerformancePlan add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(CIDOC.P165_incorporates, f22.asResource());
    return this;
  }
}
