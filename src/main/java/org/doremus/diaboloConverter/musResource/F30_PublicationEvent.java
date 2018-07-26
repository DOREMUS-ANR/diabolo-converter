package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;

public class F30_PublicationEvent extends DoremusResource {
  public F30_PublicationEvent(Oeuvre record) {
    super(record);

    this.resource.addProperty(RDF.type, FRBROO.F30_Publication_Event);

    Literal date = record.getPublicationDate();
    E52_TimeSpan timeSpan = new E52_TimeSpan(URI.create(this.uri + "/interval"), date, date);
    this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
    this.model.add(timeSpan.getModel());
  }


  public F30_PublicationEvent add(F24_PublicationExpression expression) {
    this.resource.addProperty(FRBROO.R24_created, expression.asResource());
    return this;
  }

  public F30_PublicationEvent add(F19_PublicationWork work) {
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, work.asResource());
    return this;
  }

}
