package org.doremus.diaboloConverter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.List;

@XmlRootElement(name = "main")
@XmlAccessorType(XmlAccessType.FIELD)
public class OeuvresNotMod {
  @XmlElement(name = "DATA_RECORD")
  private List<Oeuvre> works;

  public OeuvresNotMod() {
  }


  public List<Oeuvre> getWorks() {
    return works;
  }


  public static OeuvresNotMod fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(OeuvresNotMod.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (OeuvresNotMod) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

}
