package org.doremus.diaboloConverter.files;

import org.doremus.diaboloConverter.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.doremus.diaboloConverter.Utils.notEmptyString;

@XmlAccessorType(XmlAccessType.FIELD)

public class Place extends DiaboloRecord {
  private static final String BRACKETS_REGEX = "\\(([^(]+)\\)";
  private static final Pattern BRACKETS_PATTERN = Pattern.compile(BRACKETS_REGEX);
  private static final String PREFIX_REGEX = "(?i)(.+ d['ue]s?|chateau|la|les?)";
  private static final String CITY_COUNTRY_REGEX = "(?i)ville (.+)";
  private static final String ISLAND_COUNTRY_REGEX = "(?i)iles? d'(.+)";
  private static final String PROVINCE_COUNTRY_REGEX = "(?i)province (.+)";

  @XmlElement(name = "DORIS_KEY")
  private String id;
  @XmlElement(name = "DE")
  private String originalLabel;
  @XmlElement(name = "NA")
  private String comment;
  @XmlElement(name = "DOMAINE")
  private String domain;

  private String label = null;
  private String type = null;
  private String country = null;

  @Override
  public String getId() {
    return id;
  }

  public String getComment() {
    String c = notEmptyString(comment);
    if (c == null) return null;
    return c.replaceAll("\\s+", " ");
  }

  public String getOriginalLabel() {
    return originalLabel;
  }

  public String getLabel() {
    if (this.label != null) return this.label;

    this.label = originalLabel;

    // VALETTE (LA) (VILLE)
    Matcher m = BRACKETS_PATTERN.matcher(label);
    while (m.find()) {
      String content = m.group(1).trim();
      label = label.replace(m.group(0), "").trim();

      if (content.matches(PREFIX_REGEX)) {
        label = content + " " + label; // VALETTE (LA) -> LA VALLETTE
        if (content.startsWith("REPUBLIQUE")) type = "PCLI";
        else if (content.startsWith("ILES")) type = "ISLS";
        else if (content.startsWith("ILE")) type = "ISL";
        else if (content.startsWith("MONTS")) type = "MTS";
        else if (content.startsWith("MONT")) type = "MT";
        else if (content.startsWith("CHATEAU")) type = "S";
        continue;
      }

      if (content.matches(CITY_COUNTRY_REGEX)) {
        type = "city";
        country = content.split(" ", 2)[1]; // (VILLE ANGLETERRE)
        continue;
      }
      if (content.matches(PROVINCE_COUNTRY_REGEX)) {
        type = "province";
        country = content.split(" ", 2)[1]; // (PROVINCE ESPAGNE)
        continue;
      }
      if (content.matches(ISLAND_COUNTRY_REGEX)) {
        type = "island";
        country = content.split("'", 2)[1]; // (ILE D'IRLANDE)
        continue;
      }

      if (TYPES.get(content) != null) type = TYPES.get(content);
      else if (COUNTRIES.get(content) != null) country = COUNTRIES.get(content);

    }
    return label;
  }

  // return continent code if available
  public String getContinent() {
    if (domain == null || domain.isEmpty()) return null;
    return CONTINENTS.getOrDefault(this.domain, null);
  }

  public String getCountry() {
    if (country != null)
      return COUNTRIES.getOrDefault(country, null);
    if ("FEDERATION DE RUSSIE".equals(domain)) return "RU";
    if ("DPT".equals(type)) return "FR";
    return null;
  }

  public String getType() {
    return type;
  }

  public boolean isPeople() {
    return "PEUPLES".equals(domain) || "PEOPLE".equals(type);
  }

  public boolean noGeoNames() {
    return isPeople() || "TERRITOIRES ANCIENS".contains(domain)
      || "REGIONS POLAIRES".equals(domain);
  }

  private static final Map<String, String> CONTINENTS =
    Collections.unmodifiableMap(new HashMap<String, String>() {{
      put("AMERIQUE", "NA,SA");
      put("EUROPE", "EU");
      put("OCEANIE", "OC");
      put("AFRIQUE", "AF");
      put("ASIE", "AS");
    }});

  // see also http://www.geonames.org/export/codes.html
  private static final Map<String, String> TYPES =
    Collections.unmodifiableMap(new HashMap<String, String>() {{
      put("VILLE", "P");
      put("REGION", "RGN,A");
      put("PROVINCE", "A");
      put("COMTE", "A");
      put("DUCHE", "PCLI");
      put("REP", "PCLI");
      put("ETAT", "PCLI,ADM1");
      put("CANTON", "ADM1");
      put("DPT", "ADM2");
      put("SULTANAT", "PCLI");

      put("AEROPORT", "AIRP");
      put("CHATEAU", "US");
      put("CENTRE SPATIAL", "CTRS");

      put("ILE", "ISL");
      put("ILES", "ISLS");
      put("MONTAGNES", "MTS");
      put("MONTAGNE", "MT");
      put("MONT", "MT");
      put("PLATEAU", "UPLD");
      put("PLAINE", "RGN");
      put("CAP", "CAPE");

      put("LAC", "LK");
      put("FLEUVE", "STM");
      put("RIVIERE", "STM");
      put("MER", "SEA");

      put("COMMUNAUTE", "PEOPLE"); // custom code
      put("PEUPLE", "PEOPLE"); // custom code
    }});

  private static final Map<String, String> COUNTRIES =
    Collections.unmodifiableMap(new HashMap<String, String>() {{
      put("ETATS-UNIS", "US");
      put("ETATS UNIS", "US");
      put("ITALIE", "IT");
      put("SICILE", "IT");
      put("GRECE", "GR");
      put("FRANCE", "FR");
      put("PAYS DE LA LOIRE", "FR");
      put("ESPAGNE", "ES");
      put("ANDALOUSIE", "ES");
      put("CASTILLE", "ES");
      put("ROUMANIE", "RO");
      put("CHINE", "CN");
      put("INDE", "IN");
      put("IRLANDE", "IE");
      put("ANGLETERRE", "GB");
      put("ECOSSE", "GB");
      put("BELGIQUE", "BE");
      put("BULGARIE", "BG");
      put("TURQUIE", "TR");
      put("PAKISTAN", "PK");
      put("VENEZUELA", "VE");
      put("JAMAIQUE", "JM");
      put("AUTRICHE", "AT");
      put("CARINTHIE", "AT");
      put("OHIO", "US");
      put("ALABAMA", "US");
      put("MASSACHUSETTS", "US");
      put("NEW HAMPSHIRE", "US");
      put("OREGON", "US");
      put("VIRGINIE", "US");
      put("MAINE", "US");
      put("PENNSYLVANIE", "US");
      put("TENNESSEE VIRGINIE", "US");
      put("NEW JERSEY", "US");
      put("ETAT DE NEW YORK", "US");
      put("CANADA", "CA");
      put("QUEBEC", "CA");
      put("COLOMBIE", "CO");
      put("MEXIQUE", "MX");
      put("SUISSE", "CH");
      put("PAYS BAS", "NL");
      put("NORVEGE", "NO");
      put("PORTO RICO", "PR");
      put("MARTINIQUE", "MQ");
    }});
}
