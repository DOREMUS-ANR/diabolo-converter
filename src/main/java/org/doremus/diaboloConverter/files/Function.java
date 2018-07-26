package org.doremus.diaboloConverter.files;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class Function {
  private static List<Function> list = null;

  @CsvBindByName(column = "DORIS_KEY")
  private String id;
  @CsvBindByName(column = "Diabolo")
  private String label_diabolo;
  @CsvBindByName(column = "DOREMUS")
  private String label_doremus;
  @CsvBindByName(column = "M9 Derivation Type")
  private String derivation;

  public Function() {
  }

  public Function(String label) {
    this.label_doremus = label;
    this.derivation = null;
  }

  public String getLabel() {
    return label_diabolo;
  }

  public String getDerivation() {
    return this.derivation;
  }

  public static List<Function> getList() {
    if (list == null) init();
    return list;
  }

  public static boolean isInList(String function, boolean _default) {
    if (function == null || function.isEmpty() || "compositeur".equalsIgnoreCase(function))
      return _default;
    return getList().stream()
      .anyMatch(x -> x.label_diabolo.equalsIgnoreCase(function));
  }

  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    @SuppressWarnings("ConstantConditions")
    File csv = new File(cl.getResource("functions.csv").getFile());

    try {
      //noinspection unchecked
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(Function.class).build().parse();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  static Function fromString(String label) {
    if (label == null) return null;
    return getList().stream()
      .filter(f -> label.equalsIgnoreCase(f.label_diabolo))
      .findFirst()
      .orElse(new Function(label));
  }

}
