package org.doremus.diaboloConverter.files;

import org.apache.commons.lang3.text.WordUtils;
import org.doremus.diaboloConverter.Converter;
import org.doremus.diaboloConverter.musResource.E21_Person;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NomCom {
  private static Map<String, String> list = null;
  private static Map<String, List<String>> map = null;

  public static String get(String id) {
    if (list == null) init();
    return WordUtils.capitalizeFully(list.get(id));
  }

  public static List<String> getNomComOf(String workId) {
    if (map == null) init();
    return map.getOrDefault(workId, new ArrayList<>()).stream()
      .map(NomCom::get)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public static List<E21_Person> getNomPropOf(String workId) {
    if (map == null) init();
    return map.getOrDefault(workId, new ArrayList<>()).stream()
      .map(NomCom::toPerson)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }


  private static void init() {
    File input = new File(Paths.get(Converter.inputFolderPath, "RÇfÇrentiels", "NOMCOM.xml").toString());
    File inputM = new File(Paths.get(Converter.inputFolderPath, "Notices DIABOLO", "N33TCD_DESC_COMM.xml").toString());

    try {
      list = DiaboloRecord.toDictionary(input, "DORIS_KEY", "DE");
      map = DiaboloRecord.toStringMap(inputM, "DORIS_KEY", "NOM_DORIS_KEY");
    } catch (FileNotFoundException | XMLStreamException e) {
      e.printStackTrace();
    }
  }


  private static E21_Person toPerson(String id) {
    try {
      return new E21_Person(id);
    } catch (RuntimeException re) {
      return null;
    }
  }
}
