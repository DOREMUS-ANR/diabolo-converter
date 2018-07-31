package org.doremus.diaboloConverter.files;

import org.apache.jena.rdf.model.Literal;
import org.doremus.diaboloConverter.Converter;
import org.doremus.diaboloConverter.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@XmlAccessorType(XmlAccessType.FIELD)
public class Oeuvre extends DiaboloRecord {
  private static Map<String, List<String>> langMap = null;

  @XmlElement(name = "DORIS_KEY")
  private String id;
  @XmlElement(name = "TITRE_OEUVRE")
  private String title;
  @XmlElement(name = "SOUS_TITRE_OEUVRE")
  private String subTitle;
  @XmlElement(name = "AUTRE_TITRE_OEUVRE")
  private String variantTitle;
  @XmlElement(name = "TITRE_DISQUE")
  private String discTitle;
  @XmlElement(name = "VERS")
  private String version;
  @XmlElement(name = "NOTES")
  private String note;
  @XmlElement(name = "INST")
  private String instrumental;
  @XmlElement(name = "DATE_COMP_DEB")
  private String startDate;
  @XmlElement(name = "DATE_COMP_FIN")
  private String endDate;
  @XmlElement(name = "DATE_PUBLIC")
  private String datePublication;
  @XmlElement(name = "DATE_CREA")
  private String datePremiere;
  @XmlElement(name = "PERIODE")
  private String period;
  @XmlElement(name = "EXTRAIT_OEUVRE")
  private String motherTitle;

  @Override
  public String getId() {
    return id;
  }

  public String getTitle() {
    return Utils.notEmptyString(title);
  }

  public String getSubtitle() {
    return Utils.notEmptyString(subTitle);
  }

  public String getVariantTitle() {
    return Utils.notEmptyString(variantTitle);
  }

  public boolean isOriginalVersion() {
    return "VERSION ORIGINALE".equalsIgnoreCase(version);
  }

  public List<String> getLang() {
    if (langMap == null) init();
    return langMap.getOrDefault(id, new ArrayList<>());
  }

  public String getNote() {
    return Utils.notEmptyString(note);
  }

  public boolean isInstrumental() {
    return "INSTRUMENTAL".equalsIgnoreCase(instrumental);
  }

  private void init() {
    File input = new File(Paths.get(Converter.inputFolderPath, "Notices DIABOLO", "N33TCD_LANG.xml").toString());
    try {
      langMap = DiaboloRecord.toStringMap(input, "DORIS_KEY", "LAN_DORIS_KEY");
    } catch (FileNotFoundException | XMLStreamException e) {
      e.printStackTrace();
    }
  }

  public Literal getStartDate() {
    return Utils.date2literal(startDate);
  }

  public Literal getEndDate() {
    return Utils.date2literal(endDate);
  }

  public boolean containsAnyDate() {
    return getStartDate() != null || getEndDate() != null;
  }

  public Literal getPublicationDate() {
    return Utils.date2literal(datePublication);
  }

  public Literal getPremiereDate() {
    return Utils.date2literal(datePremiere);
  }

  public List<String> getPeriod() {
    if (period == null) return new ArrayList<>();
    return Arrays.asList(period.split("/"));
  }

  public String getMotherTitle() {
    return Utils.notEmptyString(motherTitle);
  }

  public List<Oeuvre> getSubWorks() {
    return SousOeuvres.subWorksOf(this.getId());
  }

  public String getDiscTitle() {
    return discTitle;
  }

  public List<Interp> getAuthors() {
    return Interps.authorsOf(this.getId());
  }

}
