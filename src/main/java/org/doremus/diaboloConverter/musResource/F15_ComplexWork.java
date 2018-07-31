package org.doremus.diaboloConverter.musResource;

import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.files.Geo;
import org.doremus.diaboloConverter.files.NomCom;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

public class F15_ComplexWork extends DoremusResource {
  private static final String TOPIC_REGEX = "(?i)(PORTRAIT|NECRO|DD|ANNIVERSAIRE).+";

  public F15_ComplexWork(Oeuvre source) {
    super(source);
    this.resource.addProperty(RDF.type, FRBROO.F15_Complex_Work)
      .addProperty(MUS.U94_has_work_type, "musical work");

    for (String n : NomCom.getNomComOf(source.getId()))
      if (n.matches(TOPIC_REGEX))
        this.resource.addProperty(CIDOC.P129_is_about, n);

    for (E21_Person n : NomCom.getNomPropOf(source.getId()))
        this.resource.addProperty(MUS.U21_is_about_actor, n.asResource());

    for (String p : Geo.getPlacesOf(source.getId())){
      E53_Place px = new E53_Place(p);
      this.resource.addProperty(MUS.U22_is_about_place, px.asResource());
    }
  }

  public F15_ComplexWork add(F14_IndividualWork f14) {
    this.resource.addProperty(FRBROO.R10_has_member, f14.asResource());
    return this;
  }

  public F15_ComplexWork add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(MUS.U38_has_descriptive_expression, f22.asResource());
    return this;
  }

  public F15_ComplexWork add(M42_PerformedExpressionCreation performance) {
    this.resource.addProperty(FRBROO.R10_has_member, performance.getWork())
      .addProperty(FRBROO.R13_is_realised_in, performance.getExpression());
    return this;
  }
}
