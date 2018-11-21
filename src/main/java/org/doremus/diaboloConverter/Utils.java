package org.doremus.diaboloConverter;

import io.github.pasqlisena.RomanConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.doremus.diaboloConverter.musResource.E52_TimeSpan;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
  private static final String BRACKETS_REGEX = "\\(([^(]+)\\)";
  public static final Pattern BRACKETS_PATTERN = Pattern.compile(BRACKETS_REGEX);

  public static QuerySolution queryDoremus(String sparql) {
    Query query = QueryFactory.create();
    try {
      QueryFactory.parse(query, sparql, "", Syntax.syntaxSPARQL_11);
      QueryExecution qexec = QueryExecutionFactory.sparqlService("http://data.doremus.org/sparql", query);
      ResultSet r = qexec.execSelect();
      if (!r.hasNext()) return null;
      return r.next();

    } catch (QueryParseException e) {
      System.out.println(query);
      e.printStackTrace();
      return null;
    }
  }

  public static RDFNode queryDoremus(String sparql, String var) {
    QuerySolution result = queryDoremus(sparql);
    if (result == null) return null;
    else return result.get(var);
  }

  public static Literal toSafeNumLiteral(String str) {
    if (str.matches("\\d+"))
      return ResourceFactory.createTypedLiteral(Integer.parseInt(str));
    else return ResourceFactory.createTypedLiteral(str);
  }

  public static boolean areQuotesBalanced(String[] parts) {
    return Arrays.stream(parts)
      .noneMatch(p -> (StringUtils.countMatches(p, "\"") % 2) != 0 ||
        (StringUtils.countMatches(p, "(") % 2) != (StringUtils.countMatches(p, ")") % 2));
  }


  public static boolean startsLowerCase(String text) {
    String first = text.substring(0, 1);
    return first.matches("[a-z]");
  }

  public static String notEmptyString(String text) {
    if (text == null) return null;
    text = text.trim();
    if (text.isEmpty() || text.equals(".")) return null;
    else return text;
  }

  public static Literal date2literal(String date) {
    if (date == null || date.isEmpty() || "0000".equals(date)) return null;

    date = date.replaceAll("/", "-").trim();
    XSDDatatype type = XSDDatatype.XSDgYear;
    switch (StringUtils.countMatches(date, "-")) {
      case 1:
        type = XSDDatatype.XSDgMonth;
        break;
      case 2:
        type = XSDDatatype.XSDdate;
    }

    Matcher bcmatch = E52_TimeSpan.BC_PATTERN.matcher(date);
    if (bcmatch.find())
      date = "-" + date.replaceAll(E52_TimeSpan.BC_REGEX, "");

    if (!date.matches("^-?\\d{4}(-\\d{2}(-\\d{2})?)?")) return null;
    return ResourceFactory.createTypedLiteral(date, type);
  }


  public static String mergeNameSurname(String name, String surname) {
    List<String> parts = Stream.of(name, surname)
      .filter(Objects::nonNull)
      .map(String::trim)
      .filter(x -> !x.isEmpty())
      .collect(Collectors.toList());
    if (parts.size() < 1) return null;
    return String.join(" ", parts);
  }

  public static String fixCase(String str) {
    return fixCase(str, false);
  }

  public static String fixCase(String str, boolean numberIncluded) {
    str = notEmptyString(str);
    if (str == null) return null;

    String test = str.replaceAll("[^\\w]", "")
      .replaceAll("(WoO|Kaul|Hob)", "");
    if (numberIncluded) test = test.replaceAll("\\d", "");

    if (!StringUtils.isAllUpperCase(test)) return str;

    return Arrays.stream(str.split(" "))
      .map(Utils::smartCapitalize)
      .collect(Collectors.joining(" "));
  }

  private static String smartCapitalize(String x) {
    if (!RomanConverter.isRoman(x)) return StringUtils.capitalize(x.toLowerCase());
    else return x;
  }
}
