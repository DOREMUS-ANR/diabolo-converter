package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.MUS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M6_Casting {
  private static final List<String> voices = Arrays.asList("soprano", "contralto", "tenor", "ténor",
    "basse", "voix", "choeur", "coeurs");

  private static final String SOLISTE_REGEX = "^(\\d+ )?(soli(?:ste))(s?)";
  private static final Pattern SOLISTE_PATTERN = Pattern.compile(SOLISTE_REGEX);
  private static final String SOLOS_REGEX = "(seule?|sol(?:o|iste)s?)";


  private static final String SPLIT_REGEX = "((?=(?<! [aà]| de| e[nt]| ou) \\d+) | et | avec )";

  private final Resource resource;
  private final Model model;

  private List<M23_Casting_Detail> cDets;

  public M6_Casting(String note, String uri) {
    this(note, uri, false);
  }

  public M6_Casting(String note, String uri, boolean instrumental) {
    this.model = ModelFactory.createDefaultModel();

    Resource M6Casting = model.createResource(uri)
      .addProperty(RDF.type, MUS.M6_Casting)
      .addProperty(RDFS.comment, note)
      .addProperty(CIDOC.P3_has_note, note);

    this.resource = M6Casting;
    this.cDets = new ArrayList<>();

    int detailNum = 0;

    note = note.toLowerCase();
    if (note.contains(":") || note.contains("exécutants") || note.contains("executants")) {
      // TODO complex
      // POUR 13 INSTRUMENTS : FLUTE TRAVERSIERE HAUTBOIS CLARINETTE BASSON COR TROMPETTE TROMBONE PIANO ET QUINTETTE A CORDES
      // pour cinq exécutants avec 54 instruments
      return;
    }

    if (note.contains(" ou ")) {
      // TODO multiple castings
      // pour violon ou violoncelle et piano ou orchestre
      return;
    }

    // replace unuseful strings
    note = note
      .replaceAll("^pour", "")
      .replaceAll("dans (.+)", "")
      .replaceAll("\\(.+\\)", "").trim();

    // replace french numeral with number
    note = note.replaceAll("(^| )une? ", "1 ")
      .replaceAll("(^| )deux ", "2 ")
      .replaceAll("(^| )trois ", "3 ")
      .replaceAll("(^| )quatre ", "4 ")
      .replaceAll("(^| )cinq ", "5 ")
      .replaceAll("(^| )six ", "6 ")
      .replaceAll("(^| )sept ", "7 ")
      .replaceAll("(^| )huit ", "8 ")
      .replaceAll("(^| )onze ", "11 ");

    Matcher sm = SOLISTE_PATTERN.matcher(note);
    if (sm.find()) {
      note = note.replace(sm.group(0), "");
      String mop = instrumental ? "instrument" : "voice";
      int quantity = sm.group(1) != null ? Integer.parseInt(sm.group(1).trim()) : -1;
      if (sm.group(3) == null) quantity = 1;
      M23_Casting_Detail x = new M23_Casting_Detail(mop, quantity, true, uri + "/detail/" + ++detailNum);
      cDets.add(x);
    }

    String[] splits = note.split(SPLIT_REGEX);

    for (String match : splits) {
      match = match.trim();

      // i.e. "clarinette soliste", "piano solo"
      String part = match.replaceAll(SOLOS_REGEX, "").trim();

      if (part.isEmpty() || part.matches("\\d+"))
        part = match.replaceAll(SOLOS_REGEX, "instrument");

      boolean isSolo = !part.equals(match);

      int quantity = -1;
      Pattern numRegex = Pattern.compile("^(\\d+) (.+)"); // i.e. "3 hautbois"
      Matcher matcher = numRegex.matcher(part);
      if (matcher.find()) {
        quantity = Integer.parseInt(matcher.group(1).trim());
        part = matcher.group(2).trim();
      }

      // sub instruments i.e. "3 bassoons dont 1 contrabassoon"
      String subInstrumentRegex = " dont (.+)";
      Pattern subInstrumentPattern = Pattern.compile(subInstrumentRegex);
      matcher = subInstrumentPattern.matcher(part);
      if (matcher.find()) {

        part = part.replace(matcher.group(1), "").trim();
        if (part.endsWith(",")) part = part.substring(0, part.length() - 1).trim();

        String subPart = matcher.group(2).trim();

        int subQuantity;
        Matcher subMatcher = numRegex.matcher(subPart);
        if (subMatcher.find()) {
          subQuantity = Integer.parseInt(subMatcher.group(2).trim());
          subPart = subMatcher.group(1).trim();
        } else subQuantity = 1;

        // special cases
        if (subPart.equals("la petite")) subPart = "piccolo";
        subPart = subPart
          .replaceAll("jouant .+", "").trim();

        if (subPart.startsWith("en ") | subPart.startsWith("à ") || subPart
          .startsWith("de ")) {
          subPart = part + " " + subPart;
        }
        cDets.add(new M23_Casting_Detail(subPart.trim(), subQuantity, false, uri + "/detail/" + ++detailNum));
        quantity -= subQuantity;
      }
      cDets.add(new M23_Casting_Detail(part, quantity, isSolo, uri + "/detail/" + ++detailNum));
    }

    for (int i = 0; i < cDets.size(); i++) {
      M23_Casting_Detail current = cDets.get(i);
      String currentName = current.getLName();

      if (currentName == null && current.quantity > -1 && i < cDets.size() - 1) {
        currentName = current.name = cDets.get(i + 1).name;
      }

      if (currentName == null) continue;

      if (currentName.equalsIgnoreCase("II"))
        current.name = cDets.get(i - 1).name;

      if (currentName.equals("alto") || currentName.equals("baryton")) {
        // workaround for https://github.com/DOREMUS-ANR/marc2rdf/issues/53
        String prev = "***", foll = "***";
        if (i > 0) prev = cDets.get(i - 1).getLName();
        if (i < cDets.size() - 1) foll = cDets.get(i + 1).getLName();

        if (voices.contains(prev) || voices.contains(foll))
          current.setAsVoice();
      }

      Resource res = current.asResource(true);

      if(res !=null){
        M6Casting.addProperty(MUS.U23_has_casting_detail, res);
        model.add(current.getModel());
      }
    }
  }


  public Resource asResource() {
    return resource;
  }

  public Model getModel() {
    return this.model;
  }
}
