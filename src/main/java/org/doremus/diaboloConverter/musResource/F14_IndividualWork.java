package org.doremus.diaboloConverter.musResource;

import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F14_IndividualWork extends DoremusResource {

  public F14_IndividualWork(Oeuvre record) {
    super(record);
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

  public F14_IndividualWork(Oeuvre source, String identifier) {
    super(source, identifier);
    this.resource.addProperty(RDF.type, FRBROO.F14_Individual_Work);
  }

  public void setDerivation(String derivation) {
    this.resource.addProperty(MUS.U47_has_derivation_type, derivation);
  }

  public F14_IndividualWork add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R9_is_realised_in, f22.asResource());
    return this;
  }

  public F14_IndividualWork add(F14_IndividualWork child) {
    this.resource.addProperty(CIDOC.P148_has_component, child.asResource());
    return this;
  }

  public F14_IndividualWork addPremiere(M42_PerformedExpressionCreation premiere) {
    this.resource.addProperty(MUS.U5_had_premiere, premiere.getMainPerformance());
    return this;
  }

  public F14_IndividualWork add(F30_PublicationEvent publication) {
    this.resource.addProperty(MUS.U4_had_princeps_publication, publication.asResource());
    return this;
  }

}
