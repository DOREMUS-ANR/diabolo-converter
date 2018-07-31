package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M23_Casting_Detail extends DoremusResource {
  //  private final StanfordLemmatizer slem;
  String uri;
  String name;

  private String namePlural, nameComplete;
  private String note;
  private String lang;
  int quantity;
  private boolean solo;


  public M23_Casting_Detail(String name, int quantity, boolean isSubSolo, String uri) {
    super();
    this.name = name;
    this.note = name;
    this.quantity = quantity;
    this.solo = isSubSolo;
    this.uri = uri;
    this.lang = "fr";

//    this.slem = StanfordLemmatizer.getDefaultLemmatizer();

    name = name
      .replaceAll("ou \\d", "") // 2 ou 4 bassoon
      .replaceAll("non spécifiée?", "")
      .replaceAll("\\(\\)", "")
      .trim()
      .replaceAll("[.\\-/]$", "");


    String gradeRegex = "(.+) (I+|\\d)";
    if (name.matches(gradeRegex)) {// Violon II, soprano 1, soprano 2
      Pattern grade = Pattern.compile(gradeRegex);
      Matcher matcher = grade.matcher(name);
      matcher.find();

      name = matcher.group(0);
    }

    this.nameComplete = name;

    this.namePlural = name
      .replaceAll("[(\\[].+[)\\]]", "").trim()
      .replaceFirst("(?i)en (ut|ré|mi|fa|sol|la|si)( b(émol)?)?", "");
    this.name = namePlural;
  }

  public Resource asResource(boolean forceMatch) {
    Resource M23CastingDetail = model.createResource(uri)
      .addProperty(RDF.type, MUS.M23_Casting_Detail)
      .addProperty(RDFS.label, (quantity > -1 ? quantity + " " : "") + nameComplete + (solo ? " solo" : ""))
      .addProperty(RDFS.comment, note, lang)
      .addProperty(CIDOC.P3_has_note, note, lang);

    if (solo)
      M23CastingDetail.addProperty(MUS.U36_foresees_responsibility, model.createLiteral("soloist", lang));

    if (quantity > -1)
      M23CastingDetail.addProperty(MUS.U30_foresees_quantity_of_mop, model.createTypedLiteral(quantity));

    Literal mopLiteral = model.createLiteral(name, lang);


    Resource match = getMatch();

    if (match != null) {
      M23CastingDetail.addProperty(MUS.U2_foresees_use_of_medium_of_performance, match);
      return M23CastingDetail;
    }

    if (forceMatch) return null;

    M23CastingDetail.addProperty(MUS.U2_foresees_use_of_medium_of_performance,
      model.createResource(this.uri + "/mop")
        .addProperty(RDF.type, MUS.M14_Medium_Of_Performance)
        .addProperty(RDFS.label, mopLiteral)
        .addProperty(CIDOC.P1_is_identified_by, mopLiteral));


    return M23CastingDetail;
  }

  private Resource getMatch() {
    Resource match = VocabularyManager.searchInCategory(name, lang, "mop", false);

    if (match != null) return match;

    // workaround for choirs
    String _name = name.toLowerCase();
    if (_name.contains("choeur") || _name.contains("choristes") || name.equals("satb"))
      return VocabularyManager.searchInCategory("choir", "en", "mop", false);

    // workaround for voices
    if (_name.contains("voix")) {
      String voixClean = _name.replace("voix", "").trim()
        .replaceAll("^[^\\w]+", "").trim();
      String _is = toItalianSinger(voixClean);
      if (_is != null) {
        lang = "it";
        voixClean = _is;
      }
      match = VocabularyManager.searchInCategory(voixClean, lang, "mop", false);

      if (match != null) return match;
      return VocabularyManager.searchInCategory("voix", lang, "mop", false);
    }

    // 2nd attempt, pluralize the whole name (i.e. saxophones sopranos -> saxophone soprano)
    match = VocabularyManager.searchInCategory(namePlural, lang, "mop", true);
    return match;
  }

  public String getLName() {
    if (name == null) return null;
    return name.toLowerCase();
  }

  public void setAsVoice() {
    String _name = toItalianSinger(this.name);
    if (_name == null) return;

    this.lang = "it";
    this.name = _name;
  }

  private static String toItalianSinger(String input) {
    return toItalianSinger(input, false);
  }

  private static String toItalianSinger(String input, boolean safeMode) {
    switch (input) {
      case "alto":
        return "contralto";
      case "baryton":
        return "baritono";
      default:
        return safeMode ? input : null;
    }
  }

}