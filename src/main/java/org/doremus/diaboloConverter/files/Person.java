package org.doremus.diaboloConverter.files;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.rdf.model.Literal;
import org.doremus.diaboloConverter.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
public class Person extends DiaboloRecord {
  private static final String PSEUDO_REGEX = "((?:utilise|sous) les? )?ps?eudo(?:nyme)?(s)?" +
    "(?: ?\\/ ?(?:nom de resistant|diminutif)| de sc[eéè]ne| probables?)? ?(de |d')?:?(.+)";
  private static final String ALIAS_REGEX = "(etat[- ]civil|alias|nom complet|(?:veritable|vrai) nom|ne|dit aussi) " +
    "(de )?:?(.+)";
  private static final Pattern PSEUDO_PATTERN = Pattern.compile(PSEUDO_REGEX, Pattern.CASE_INSENSITIVE);
  private static final Pattern ALIAS_PATTERN = Pattern.compile(ALIAS_REGEX, Pattern.CASE_INSENSITIVE);

  @XmlElement(name = "DORIS_KEY")
  private String id;
  @XmlElement(name = "DE")
  private String label;
  @XmlElement(name = "PRENOM")
  private String name;
  @XmlElement(name = "NOM")
  private String surname;
  @XmlElement(name = "SEXE")
  private String gender;
  @XmlElement(name = "DATE_NAIS")
  private String birthDate;
  @XmlElement(name = "DATE_DECES")
  private String deathDate;
  @XmlElement(name = "DATE_FORM")
  private String formationDate;
  @XmlElement(name = "APP_INFO")
  private String info;
  @XmlElement(name = "CODE_GI")
  private String code;
  @XmlElement(name = "NA")
  private String nameInfo;

  private String fullName = null;
  private List<String> pseudonym;
  private List<String> altName;

  public String getLabel() {
    return WordUtils.capitalizeFully(label);
  }

  private void setLabel(String label) {
    this.label = label;
  }

  public String getName() {
    return WordUtils.capitalizeFully(name);
  }

  public String getSurname() {
    return WordUtils.capitalizeFully(surname);
  }

  private void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getFullName() {
    if (this.fullName == null) {
      if (this.surname != null) {
        this.fullName = Utils.mergeNameSurname(this.getName(), this.getSurname());
      } else
        this.fullName = this.getLabel();
    }
    assert this.fullName != null;
    return this.fullName.trim();
  }

  public String getGender() {
    return gender;
  }

  public Literal getBirthDate() {
    return Utils.date2literal(birthDate);
  }

  public Literal getDeathDate() {
    return Utils.date2literal(deathDate);
  }

  public Literal getFormationDate() {
    return Utils.date2literal(formationDate);
  }

  public String getInfo() {
    return info;
  }

  public String getComment() {
    return Utils.notEmptyString(nameInfo);
  }

  @Override
  public String getId() {
    return id;
  }

  public boolean isAPerson() {
    return "I".equalsIgnoreCase(code);
  }

  public boolean isAGroup() {
    return "G".equalsIgnoreCase(code);
  }

  public List<String> getPseudonym() {
    return pseudonym;
  }

  public List<String> getAltName() {
    return altName;
  }

  public void init() {
    this.pseudonym = new ArrayList<>();
    this.altName = new ArrayList<>();

    if (nameInfo == null || nameInfo.isEmpty())
      return;

    boolean isPseudo, plural;
    boolean continueInNext = false;
    // split on \n or on point (excluded J. Smith et similar)
    for (String ni : nameInfo.split("((?<=\\w{2}| )\\.|\n)")) {
      ni = ni.replaceAll("\\s+", " ").trim();
      if (continueInNext) {
        this.pseudonym.add(ni.replaceAll("^-", "").trim());
        continue;
      }

      if (ni.equals("pseudo")) {
        this.pseudonym.add(this.getLabel());
        continue;
      }

      Matcher m = PSEUDO_PATTERN.matcher(ni);
      if (m.find()) {
        String body = m.group(4)
          .replaceAll("etc.+", "").trim();

        if (body.contains(":") || body.toLowerCase().contains("en tant que") ||
          body.toLowerCase().startsWith("utilis") ||
          body.equalsIgnoreCase("un inconnu"))
          continue; // too complex

        isPseudo = m.group(1) != null || m.group(3) == null;
        plural = m.group(2) != null;


        if (body.isEmpty()) {
          continueInNext = true;
          continue;
        }
        body = body.replaceAll("\\(.+\\)", "");

        String[] parts = new String[]{body};
        if (plural) parts = body.split("(?i)([/,]| puis | et | ou )");

        for (String p : parts) {
          p = WordUtils.capitalizeFully(p).trim();
          if (isPseudo)
            this.pseudonym.add(p);
          else {
            this.pseudonym.add(this.getLabel());
            this.setFullName(p);
          }
        }
      }
      m = ALIAS_PATTERN.matcher(ni);
      if (m.find()) {
        String body = m.group(3);
        String qual = m.group(1);
        isPseudo = m.group(2) != null;
        for (String p : body.split(",")) {
          p = WordUtils.capitalizeFully(p).trim();
          if ("alias".equalsIgnoreCase(qual) || "dit aussi".equalsIgnoreCase(qual))
            this.pseudonym.add(p);
          else this.altName.add(p);
        }
      }
    }
  }


//    <APP_INFO>|TURNER ANDY|||196631   |HANDLEY ED|||246280   </APP_INFO>
//    <APP_INFO>|BOWIE DAVID|1988|1992|22147   |GABRELS REEVES|1988|1992|69789   |SALES TONY|1988|1992|170915   |SALES HUNT|1988||170910   </APP_INFO>

}
