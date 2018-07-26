package org.doremus.diaboloConverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.diaboloConverter.musResource.*;
import org.doremus.ontology.PROV;

import java.net.URISyntaxException;
import java.time.Instant;


public class RecordConverter {
  private Model model;
  private Resource provEntity, provActivity;


  public RecordConverter(Oeuvre source) {
    this.model = ModelFactory.createDefaultModel();

    try {
      // PROV-O tracing
      provEntity = model.createResource("http://data.doremus.org/source/diabolo/" + source.getId())
        .addProperty(RDF.type, PROV.Entity).addProperty(PROV.wasAttributedTo, Converter.RADIO_FRANCE);

      provActivity = model.createResource(ConstructURI.build("rfd", "prov", source.getId()).toString())
        .addProperty(RDF.type, PROV.Activity).addProperty(RDF.type, PROV.Derivation)
        .addProperty(PROV.used, provEntity)
        .addProperty(RDFS.comment, "Reprise et conversion de la notice avec id " + source.getId() +
          " de la base Diabolo de Radio France", "fr")
        .addProperty(RDFS.comment, "Resumption and conversion of the record with id " + source.getId()
          + " of the dataset Diabolo of Radio France", "en")
        .addProperty(PROV.atTime, Instant.now().toString(), XSDDatatype.XSDdateTime);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }


    F28_ExpressionCreation f28 = new F28_ExpressionCreation(source, false);
    F22_SelfContainedExpression f22 = new F22_SelfContainedExpression(source);
    F14_IndividualWork f14 = new F14_IndividualWork(source);
    F15_ComplexWork f15 = new F15_ComplexWork(source);

    if (f28.getDerivation() != null) f14.setDerivation(f28.getDerivation());

    addProvenanceTo(f22);
    addProvenanceTo(f15);
    f28.add(f14).add(f22);
    f15.add(f14).add(f22);
    f14.add(f22);

    if (source.getPublicationDate() != null) {
      F30_PublicationEvent f30 = new F30_PublicationEvent(source);
      F24_PublicationExpression f24 = new F24_PublicationExpression(source);
      F19_PublicationWork f19 = new F19_PublicationWork(source);

      f30.add(f24).add(f19);
      f19.add(f24);
      f24.add(f22);

      f14.add(f30);
      f22.add(f30);

      model.add(f24.getModel());
      model.add(f30.getModel());
      model.add(f19.getModel());
    }

    if (source.getPremiereDate() != null) {
      M42_PerformedExpressionCreation m42 = new M42_PerformedExpressionCreation(source);
      F25_PerformancePlan f25 = new F25_PerformancePlan(source);

      m42.add(f25).add(f22);
      f25.add(f22);
      f15.add(m42);

      f14.addPremiere(m42);
      f22.addPremiere(m42);

      model.add(m42.getModel());
      model.add(f25.getModel());
    }

    if (source.getMotherTitle() != null) {
      F14_IndividualWork f14m = new F14_IndividualWork(source, "m" + source.getId());
      F22_SelfContainedExpression f22m = new F22_SelfContainedExpression(source, true);
      f14m.add(f22m);
      f14m.add(f14);
      f28.add(f14m).add(f22m);
      model.add(f22m.getModel()).add(f14m.getModel());
    }

    for (Oeuvre w : source.getSubWorks()) {
      F14_IndividualWork f14c = new F14_IndividualWork(w);
      F22_SelfContainedExpression f22c = new F22_SelfContainedExpression(w);
      f14c.add(f22c);
      f14.add(f14c);
      if (w.containsAnyDate()) {
        F28_ExpressionCreation f28c = new F28_ExpressionCreation(w, false);
        f28.add(f14c).add(f22c);
        f28.add(f28c);
        model.add(f28c.getModel());
      } else
        f28.add(f14c).add(f22c);

      model.add(f22c.getModel()).add(f14c.getModel());
    }

    model.add(f22.getModel())
      .add(f28.getModel())
      .add(f15.getModel())
      .add(f14.getModel());

  }

  public Model getModel() {
    return model;
  }

  private void addProvenanceTo(DoremusResource res) {
    res.asResource().addProperty(RDF.type, PROV.Entity)
      .addProperty(PROV.wasAttributedTo, Converter.DOREMUS)
      .addProperty(PROV.wasDerivedFrom, this.provEntity)
      .addProperty(PROV.wasGeneratedBy, this.provActivity);
  }


}
