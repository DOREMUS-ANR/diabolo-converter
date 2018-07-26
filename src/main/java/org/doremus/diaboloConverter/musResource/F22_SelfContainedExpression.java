package org.doremus.diaboloConverter.musResource;

import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.ConstructURI;
import org.doremus.diaboloConverter.files.Genre;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;

import java.net.URISyntaxException;
import java.util.List;


public class F22_SelfContainedExpression extends DoremusResource {
  private static final String TITLE_SKIP_REGEX = "(?i)(int[eèé]grale|extraits?)";

  public F22_SelfContainedExpression(Oeuvre oeuvre) {
    this(oeuvre, false);
  }

  public F22_SelfContainedExpression(Oeuvre oeuvre, boolean isMother) {
    super();
    this.record = oeuvre;
    this.identifier = isMother ? "m" + oeuvre.getId() : oeuvre.getId();

    try {
      this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.resource = model.createResource(this.uri.toString())
      .addProperty(RDF.type, FRBROO.F22_Self_Contained_Expression)
      .addProperty(MUS.U94_has_work_type, "musical work")
      .addProperty(DCTerms.identifier, this.identifier);

    if (isMother) {
      this.resource.addProperty(CIDOC.P102_has_title, oeuvre.getMotherTitle())
        .addProperty(RDFS.label, oeuvre.getMotherTitle());
    } else {
      parseWork();
    }
  }

  private void parseWork() {
    Oeuvre oeuvre = (Oeuvre) this.record;
    String title = oeuvre.getTitle()
      .replaceAll("(?i)^bis ?:", "").trim();

    this.resource.addProperty(CIDOC.P102_has_title, title)
      .addProperty(RDFS.label, title);


    if (oeuvre.getSubtitle() != null)
      for (String t : oeuvre.getSubtitle().split(" / ")) {
        t = t.trim();
        if (t.matches(TITLE_SKIP_REGEX)) continue;
        if (t.toLowerCase().startsWith("pour"))
          addCasting(t);
        else this.resource.addProperty(MUS.U67_has_subtitle, t);
      }

    if (oeuvre.getVariantTitle() != null)
      this.resource.addProperty(MUS.U68_has_variant_title, oeuvre.getVariantTitle());


    List<Genre> genres = Genre.getGenresOf(identifier);
    // voice
    boolean hasVoice = genres.contains(Genre.get(Genre.VOICE_KEY));
    boolean hasRecit = genres.contains(Genre.get(Genre.RECIT_KEY));

    for (Genre g : genres) {
      if (g.isVoice() || g.isRecit()) continue;
      String label = g.getLabel();

      if (g.is("169")) {
        this.resource.addProperty(CIDOC.P103_was_intended_for,
          model.createResource("http://data.doremus.org/audience/" + label)
            .addProperty(RDF.type, MUS.M60_Intended_Audience)
            .addProperty(RDFS.label, label));
        continue;
      }

      switch (g.getType()) {
        case "categorization":
          if (label.endsWith("+")) {
            if (hasVoice) label += " VOIX";
            else if (hasRecit) label += " RECIT";
            else label = label.replaceAll("\\+$", "").trim();
          }
          this.add(label, MUS.M19_Categorization, MUS.U19_is_categorized_as);
          break;
        case "religion":
          this.add(g.getLabel(), MUS.M40_Context, MUS.U63_has_religious_context);
      }

      this.resource.addProperty(MUS.U12_has_genre, model.createResource(g.getGenreUri()));
    }

    if (oeuvre.isOriginalVersion())
      for (String l : oeuvre.getLang()) {
        String langUri = "http://data.doremus.org/vocabulary/diabolo/lang/" + l;
        this.resource.addProperty(CIDOC.P72_has_language, model.createTypedLiteral(langUri));
      }

    if (oeuvre.getNote() != null)
      addNote(oeuvre.getNote());

    if (oeuvre.isInstrumental()) addNote("Instrumental");
    else addNote("Vocal");
  }

  private void addCasting(String text) {
    Resource cat = model.createResource(this.uri.toString() + "/casting/1")
      .addProperty(RDF.type, MUS.M6_Casting)
      .addProperty(RDFS.comment, text)
      .addProperty(CIDOC.P3_has_note, text);
    this.resource.addProperty(MUS.U13_has_casting, cat);

  }

  private void add(String label, OntClass ontClass, ObjectProperty property) {
    Resource cat = model.createResource()
      .addProperty(RDF.type, ontClass)
      .addProperty(RDFS.label, label);
    this.resource.addProperty(property, cat);
  }

  public F22_SelfContainedExpression add(F30_PublicationEvent f30) {
    this.resource.addProperty(MUS.U4_had_princeps_publication, f30.asResource());
    return this;
  }

  public F22_SelfContainedExpression addPremiere(M42_PerformedExpressionCreation m42) {
    this.resource.addProperty(MUS.U5_had_premiere, m42.getMainPerformance());
    return this;
  }

  public F22_SelfContainedExpression add(F22_SelfContainedExpression movement) {
    this.resource.addProperty(FRBROO.R5_has_component, movement.asResource());
    return this;
  }

}
