package org.doremus.diaboloConverter.files;

import org.doremus.diaboloConverter.musResource.E21_Person;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Interp extends DiaboloRecord {
  @XmlElement(name = "DORIS_KEY")
  private String workId;
  @XmlElement(name = "NOM_DORIS_KEY")
  private String id;
  @XmlElement(name = "AUT")
  private String aut;
  @XmlElement(name = "FONCTION", defaultValue = "compositeur")
  private String funct;
  @XmlElement(name = "ROLE")
  private String role;

  private Function function = null;
  private E21_Person person = null;

  @Override
  public String getId() {
    return id;
  }


  public String getWorkId() {
    return workId;
  }

  public boolean isAuthor() {
    return "Auteur".equalsIgnoreCase(aut);
  }

  String getFunct() {
    return funct;
  }

  private void initFunction() {
    function = Function.fromString(this.getFunct());
  }

  public String getFunction() {
    if (funct == null) return null;
    if (function == null) initFunction();
    return function.getLabel();
  }

  public E21_Person getPerson() {
    if (person == null)
      person = new E21_Person(this.id);

    return person;
  }

  public String getDerivation() {
    if (funct == null) return null;
    if (function == null) initFunction();
    return function.getDerivation();
  }
}
