package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.ConstructURI;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URI;
import java.net.URISyntaxException;

public class M42_PerformedExpressionCreation extends DoremusResource {
  private Resource M44_Performed_Work, M43_Performed_Expression, F31_Performance;

  public M42_PerformedExpressionCreation(Oeuvre record) {
    super(record);


    String performanceUri = null;
    String expressionUri = null;
    String workUri = null;
    try {
      performanceUri = ConstructURI.build("rfd", "F31_Performance", this.identifier).toString();
      expressionUri = ConstructURI.build("rfd", "M43_PerformedExpression", this.identifier).toString();
      workUri = ConstructURI.build("rfd", "M44_PerformedWork", this.identifier).toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.F31_Performance = model.createResource(performanceUri)
      .addProperty(RDF.type, FRBROO.F31_Performance)
      .addProperty(CIDOC.P9_consists_of, this.resource);

    this.M43_Performed_Expression = model.createResource(expressionUri)
      .addProperty(RDF.type, MUS.M43_Performed_Expression);

    this.M44_Performed_Work = model.createResource(workUri)
      .addProperty(RDF.type, MUS.M44_Performed_Work)
      .addProperty(FRBROO.R9_is_realised_in, this.M43_Performed_Expression);

    this.resource.addProperty(FRBROO.R17_created, this.M43_Performed_Expression)
      .addProperty(FRBROO.R19_created_a_realisation_of, this.M44_Performed_Work);

    String ts_uri = this.F31_Performance.getURI() + "/interval";

    E52_TimeSpan timeSpan = new E52_TimeSpan(URI.create(ts_uri), record.getPremiereDate(), record.getPremiereDate());
    this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
    this.F31_Performance.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());

    this.model.add(timeSpan.getModel());
  }

  public Resource getExpression() {
    return M43_Performed_Expression;
  }

  public Resource getWork() {
    return M44_Performed_Work;
  }

  public M42_PerformedExpressionCreation add(F25_PerformancePlan plan) {
    this.F31_Performance.addProperty(FRBROO.R25_performed, plan.asResource());
    return this;
  }

  public M42_PerformedExpressionCreation add(F22_SelfContainedExpression f22) {
    this.M43_Performed_Expression.addProperty(MUS.U54_is_performed_expression_of, f22.asResource());
    this.F31_Performance.addProperty(FRBROO.R66_included_performed_version_of, f22.asResource());
    return this;
  }

  public Resource getMainPerformance() {
    return this.F31_Performance;
  }
}
