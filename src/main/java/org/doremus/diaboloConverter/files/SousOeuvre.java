package org.doremus.diaboloConverter.files;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SousOeuvre extends Oeuvre {
  @XmlElement(name = "SOUSOEUVRE_KEY")
  private String _id;
  @XmlElement(name = "TITRE_SO")
  private String _title;
  @XmlElement(name = "AUT_TIT_SO")
  private String _variantTitle;
  @XmlElement(name = "DATE_COMP_DEB")
  private String _startDate;
  @XmlElement(name = "DATE_COMP_FIN")
  private String _endDate;

  @Override
  public String getId() {
    return this.id + _id;
  }

  String getWorkId() {
    this.title = _title;
    this.variantTitle = _variantTitle;
    this.startDate = _startDate;
    this.endDate = _endDate;

    return id;
  }

  @Override
  public List<Interp> getAuthors() {
    return SousOeuvres.authorsOf(this._id);
  }
}
