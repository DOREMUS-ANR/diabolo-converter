package org.doremus.diaboloConverter.musResource;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.ConstructURI;
import org.doremus.diaboloConverter.Utils;
import org.doremus.diaboloConverter.files.Genre;
import org.doremus.diaboloConverter.files.Oeuvre;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;
import org.doremus.ontology.MUS;
import org.doremus.string2vocabulary.MODS;
import org.doremus.string2vocabulary.VocabularyManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class F22_SelfContainedExpression extends DoremusResource {
  private static final Property MODS_ID_PROP = ResourceFactory.createProperty(MODS.uri, "identifier");

  private static final String TITLE_SKIP_REGEX = "(?i)(int[eèé]grale|extraits?)";
  private static final String ACTE_REGEX = "(?:(?:epi|pro)logue|(acte|sc(?:[eè]ne)*) ([\\dIV]+))";
  private static final Pattern ACTE_PATTERN = Pattern.compile(ACTE_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String ARTICLES_REGEX = "(?i)[a-z']{1,3}";

  private static final String NUM_REGEX_STRING = "(?: n(?:[°º.]|o\\.?) ?(\\d+))";
  private static final Pattern OPUS_REGEX = Pattern.compile(" op(?:us|[. ]) ?(?:posth )?(\\d+[a-z]*)" +
    NUM_REGEX_STRING + "?", Pattern.CASE_INSENSITIVE);
  private static final Pattern WOO_PATTERN = Pattern.compile("woo ([0-9a-z]+)" + NUM_REGEX_STRING + "?",
    Pattern.CASE_INSENSITIVE);

  private static final Pattern ORDER_NUM_REGEX = Pattern.compile(NUM_REGEX_STRING, Pattern.CASE_INSENSITIVE);
  private static final Pattern LIVRE_REGEX = Pattern.compile("Livre ([0-9IV]+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern KEY_REGEX = Pattern.compile(" en ([^ ]+(?: (di[èe]se|b[ée]mol))? (maj|min)(eur)?)",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern ENG_KEY_REGEX = Pattern.compile(" in (.+ (maj|min)(or)?)", Pattern.CASE_INSENSITIVE);

  private static final String CASTING_REGEX = "pour (?!l[ea']).+";


  private final List<String> composers;


  public F22_SelfContainedExpression(Oeuvre oeuvre) {
    this(oeuvre, false);
  }

  public F22_SelfContainedExpression(Oeuvre oeuvre, boolean isMother) {
    this(oeuvre, new ArrayList<>(), isMother);
  }

  public F22_SelfContainedExpression(Oeuvre oeuvre, List<String> composers) {
    this(oeuvre, composers, false);
  }

  public F22_SelfContainedExpression(Oeuvre oeuvre, List<String> composers, boolean isMother) {
    super();
    this.record = oeuvre;
    this.identifier = isMother ? "m" + oeuvre.getId() : oeuvre.getId();

    this.composers = composers;

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

    title = parseTitle(Utils.fixCase(title, true));
    System.out.println(oeuvre.getId() + " | " + title);

    if (oeuvre.isInstrumental()) addNote("Instrumental");
    else addNote("Vocal");

    this.resource.addProperty(CIDOC.P102_has_title, title)
      .addProperty(RDFS.label, title);


    if (oeuvre.getSubtitle() != null)
      for (String t : oeuvre.getSubtitle().split("( / |\n)")) {

        t = t.trim();
        if (t.matches(TITLE_SKIP_REGEX)) continue;
        if (t.toLowerCase().matches("pour (?!l[ea']).+")) {
          boolean first = true;
          for (String x : t.split("/")) {
            if (first) {
              addCasting(x, oeuvre.isInstrumental());
              first = false;
              continue;
            }
            this.resource.addProperty(MUS.U67_has_subtitle, Utils.fixCase(x));
          }

        } else this.resource.addProperty(MUS.U67_has_subtitle, Utils.fixCase(t));
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
      if (label == null) {
        Resource concept = VocabularyManager.getVocabulary("diabolo-genre").getConcept(g.getId());
        this.resource.addProperty(MUS.U12_has_genre, concept);
        continue;
      }

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
          this.add(label, MUS.M5_Genre, MUS.U12_has_genre);
          break;
        case "religion":
          this.add(g.getLabel(), MUS.M40_Context, MUS.U63_has_religious_context);
      }
    }

    if (oeuvre.isOriginalVersion())
      for (String l : oeuvre.getLang()) {
        String langUri = "http://data.doremus.org/vocabulary/diabolo/lang/" + l;
        this.resource.addProperty(CIDOC.P72_has_language, model.createTypedLiteral(langUri));
      }

    if (oeuvre.getNote() != null)
      addNote(oeuvre.getNote());

  }

  private String parseTitle(String title) {
    Matcher m = Utils.BRACKETS_PATTERN.matcher(title);
    while (m.find())
      title = processBrackets(title, m);

    String text = title;

    // opus number
    Matcher opusMatch = OPUS_REGEX.matcher(text);
    if (opusMatch.find()) {
      String note = opusMatch.group(0);
      String opus = opusMatch.group(1);
      String opusSub = opusMatch.group(2);

      addOpus(note, opus, opusSub);
      text = text.replace(note, "");
    }

    String op = text.replaceAll("op\\.? ?posth\\.?", "");
    if (!op.equals(text)) {
      addNote("Op. posth.");
      text = op;
    }

    // WoO number
    Matcher wooMatch = WOO_PATTERN.matcher(text);
    if (wooMatch.find()) {
      String note = wooMatch.group(0);
      String woo = wooMatch.group(1);
      String subWoo = wooMatch.group(2);
      addOpus(note, woo, subWoo);
      text = text.replace(note, "");
    }

    // catalogs
    for (String u : composers) {
      for (Resource res : VocabularyManager.getMODS("catalogue").bySubject(u)) {
        StmtIterator it = res.listProperties(MODS_ID_PROP);
        while (it.hasNext()) {
          String code = it.nextStatement().getString();
          Pattern catPattern = Pattern.compile(" " + code + "[ .] ?((?:\\d[0-9a-z]*|[IXV]+)( ?: ?[^\\s]+)?)" +
              NUM_REGEX_STRING + "?",
            Pattern.CASE_INSENSITIVE);
          Matcher catMatch = catPattern.matcher(text);
          if (catMatch.find()) {
            String note = catMatch.group(0);
            String catNum = catMatch.group(1).trim();
            addCatalogue(note, code, catNum, u);
            text = text.replace(note, "");
            break;
          }
        }
      }
    }

    // order number
    String orderNum = "";
    Matcher livreMatcher = LIVRE_REGEX.matcher(text);
    if (livreMatcher.find()) {
      orderNum = "Livre " + livreMatcher.group(1).toUpperCase();
      text = text.replace(orderNum, "");
    }
    Matcher orderNumMatch = ORDER_NUM_REGEX.matcher(text);
    if (orderNumMatch.find()) {
      orderNum = orderNumMatch.group(1);
      text = text.replace(orderNumMatch.group(0), "");
    }
    if (!orderNum.isEmpty())
      this.resource.addProperty(MUS.U10_has_order_number, Utils.toSafeNumLiteral(orderNum));


    // key
    String key = "";
    Matcher keyMatch = KEY_REGEX.matcher(text);
    if (keyMatch.find()) {
      key = keyMatch.group(1).toLowerCase()
        .replaceAll("maj$", "majeur")
        .replaceAll("min$", "mineur");
      text = text.replace(keyMatch.group(0), "");
    } else {
      keyMatch = ENG_KEY_REGEX.matcher(text);
      if (keyMatch.find()) {
        key = keyMatch.group(1).replace('-', ' ');
        text = text.replace(keyMatch.group(0), "");
      }
    }
    if (!key.isEmpty())
      this.resource.addProperty(MUS.U11_has_key, key);

    return title;
  }

  private String processBrackets(String title, Matcher m) {
    String content = m.group(1).trim();
    String outer = m.group(0);
    String outerX = outer.replaceAll("([()])", "\\\\$1");

    Matcher acteMatcher = ACTE_PATTERN.matcher(content);
    if (content.equals("com mus")) {
      // comedie musicale, already in the M19 style
      title = title.replace(outer, "");

    } else if (acteMatcher.find()) {
      String[] parts = title.split(outerX, 2);
      title = parts[0];
      this.resource.addProperty(MUS.U68_has_variant_title, parts[1]);
      this.addNote(content);

    } else if ("DUO".equals(content)) {
      title = title.replace(outer, "");
      this.addNote("Duo");

    } else if (content.matches(ARTICLES_REGEX) || content.matches("\\d+")) {
      title = StringUtils.capitalize(content) + " " + title.replace(outer, "");
    }
    // if not, normally it is part of the title

    return title.replaceAll("\\s+", " ");
  }

  private void addCasting(String text, boolean instrumental) {
    String uri = this.uri.toString() + "/casting/1";
    M6_Casting casting = new M6_Casting(text, uri, instrumental);

    this.resource.addProperty(MUS.U13_has_casting, casting.asResource());
    model.add(casting.getModel());
  }

  private void addCatalogue(String note, String code, String num, String composer) {
    String label = (code != null) ? (code + " " + num) : note;

    Resource M1CatalogStatement =
      model.createResource(this.uri.toString() + "/catalog/" + label.replaceAll("[ /]", "_"))
        .addProperty(RDF.type, MUS.M1_Catalogue_Statement)
        .addProperty(RDFS.label, label)
        .addProperty(CIDOC.P3_has_note, note.trim());

    this.resource.addProperty(MUS.U16_has_catalogue_statement, M1CatalogStatement);

    if (null == num) {
      System.out.println("Not parsable catalog: " + note);
      // Should never happen normally
      return;
    }

    Resource match = VocabularyManager.getMODS("catalogue")
      .findModsResource(code, Collections.singletonList(composer));

    if (match == null)
      M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, code);
    else M1CatalogStatement.addProperty(MUS.U40_has_catalogue_name, match);

    M1CatalogStatement.addProperty(MUS.U41_has_catalogue_number, Utils.toSafeNumLiteral(num));
  }

  private void addOpus(String note, String number, String subnumber) {
    Property numProp = MUS.U42_has_opus_number,
      subProp = MUS.U17_has_opus_statement;

    if (note.substring(0, 3).equalsIgnoreCase("WoO")) {
      numProp = MUS.U69_has_WoO_number;
      subProp = MUS.U76_has_WoO_subnumber;
    }

    String id = number;
    if (subnumber != null) id += "-" + subnumber;

    Resource M2OpusStatement = model.createResource(this.uri + "/opus/" + id.replaceAll(" ", "_"))
      .addProperty(RDF.type, MUS.M2_Opus_Statement)
      .addProperty(CIDOC.P3_has_note, note)
      .addProperty(RDFS.label, note)
      .addProperty(numProp, Utils.toSafeNumLiteral(number));

    if (subnumber != null)
      M2OpusStatement.addProperty(subProp, Utils.toSafeNumLiteral(subnumber));

    this.resource.addProperty(MUS.U17_has_opus_statement, M2OpusStatement);
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
