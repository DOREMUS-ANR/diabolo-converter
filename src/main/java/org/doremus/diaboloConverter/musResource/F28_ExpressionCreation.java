package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.doremus.diaboloConverter.Converter;
import org.doremus.diaboloConverter.RomanConverter;
import org.doremus.diaboloConverter.files.Interp;
import org.doremus.diaboloConverter.files.Interps;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class F28_ExpressionCreation extends DoremusResource {
  private static final Resource BACH = ResourceFactory.createResource("http://data.doremus.org/artist/269cec9d-5025-3a8a-b2ef-4f7acb088f2b");
  private static Interps interps;

  private List<String> composer;
  private String derivation;

  public F28_ExpressionCreation(Oeuvre omu, boolean createAPerformancePlan) {
    super(omu, getId(omu.getId(), createAPerformancePlan));
    this.record = omu;
    this.resource.addProperty(RDF.type, FRBROO.F28_Expression_Creation);

    if (createAPerformancePlan) parsePerformancePlan();
    else parseWork();
  }

  private static String getId(String id, boolean createAPerformancePlan) {
    return (createAPerformancePlan ? "p" : "w") + id;
  }

  private void parseWork() {
    Oeuvre oeuvre = (Oeuvre) this.record;
    this.composer = new ArrayList<>();


    if (oeuvre.getStartDate() != null || oeuvre.getEndDate() != null) {
      // date of the work
      try {
        E52_TimeSpan timeSpan = new E52_TimeSpan(new URI(this.uri + "/interval"), oeuvre.getStartDate(), oeuvre.getEndDate());
        this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
        this.model.add(timeSpan.model);
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    // composer
    int ipCount = 0;
    List<Interp> authorList = getInterps().authorsOf(oeuvre.getId());
    for (Interp ip : authorList) {
      String function = ip.getFunction();
      E21_Person person = ip.getPerson();
      composer.add(person.getUri().toString());
      String activityUri = this.uri + "/activity/" + ++ipCount;
      Resource activity = model.createResource(activityUri)
        .addProperty(RDF.type, CIDOC.E7_Activity)
        .addProperty(CIDOC.P14_carried_out_by, person.asResource());

      if (function != null)
        activity.addProperty(MUS.U31_had_function, function);

      this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());

      if (ip.getDerivation() != null)
        this.derivation = ip.getDerivation();
    }

    if (authorList.isEmpty()) {
      Resource r = null;
      String text = oeuvre.getDiscTitle();

      if (oeuvre.getTitle().contains("BWV")) {
        r = BACH;
      } else if (text != null && text.contains(" : ")) {

        String candidate = text.split(" : ", 2)[0]
          .replace("Guiseppe", "Giuseppe");

        r = E21_Person.getPersonFromDoremus(candidate, null);
      }
      if (r != null) {
        composer.add(r.getURI());
        String activityUri = this.uri + "/activity/" + ++ipCount;

        Resource activity = model.createResource(activityUri)
          .addProperty(RDF.type, CIDOC.E7_Activity)
          .addProperty(CIDOC.P14_carried_out_by, r)
          .addProperty(MUS.U31_had_function, "composer", "en");

        this.resource.addProperty(CIDOC.P9_consists_of, activity.asResource());
      }

    }
    // period
    for (String period : oeuvre.getPeriod())
      this.resource.addProperty(CIDOC.P10_falls_within, model.createResource(toPeriodUri(period)));
  }

  private static String toPeriodUri(String period) {
    if (period == null) return null;
    String num = period.replace("EME SIECLE", "");
    int x = RomanConverter.toNumerical(num);

    return "http://data.doremus.org/period/%%_century".replace("%%", Integer.toString(x));
  }

  private static Interps getInterps() {
    if (interps == null) {
      File input = new File(Paths.get(Converter.inputFolderPath, "Notices DIABOLO", "N33TCD_INTERPS.xml").toString());
      interps = Interps.fromFile(input);
    }
    return interps;
  }

  public String getDerivation() {
    return derivation;
  }


  private void parsePerformancePlan() {
  }

  public List<String> getComposers() {
    return composer;
  }

  public F28_ExpressionCreation add(F14_IndividualWork f14) {
    this.resource.addProperty(FRBROO.R19_created_a_realisation_of, f14.asResource());
    return this;
  }

  public F28_ExpressionCreation add(F22_SelfContainedExpression f22) {
    this.resource.addProperty(FRBROO.R17_created, f22.asResource());
    return this;
  }

  public F28_ExpressionCreation add(F28_ExpressionCreation child) {
    this.resource.addProperty(CIDOC.P9_consists_of, child.asResource());
    return this;
  }
}
