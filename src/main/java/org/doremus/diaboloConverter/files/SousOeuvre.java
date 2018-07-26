package org.doremus.diaboloConverter.files;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class SousOeuvre extends Oeuvre {
  @XmlElement(name = "DORIS_KEY")
  private String workId;
  @XmlElement(name = "SOUSOEUVRE_KEY")
  private String id;
  @XmlElement(name = "TITRE_SO")
  private String title;
  @XmlElement(name = "AUT_TIT_SO")
  private String variantTitle;
  @XmlElement(name = "DATE_COMP_DEB")
  private String startDate;
  @XmlElement(name = "DATE_COMP_FIN")
  private String endDate;

  @Override
  public String getId() {
    return workId + id;
  }

  String getWorkId() {
    return workId;
  }
}
