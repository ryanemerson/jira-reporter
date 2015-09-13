package org.jboss.jbossset;

import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A program which retrieves a summary of JIRA issues associated with specified usernames.
 */
public class JiraReporter {

    private static final String QUERY_TEMPLATE = "AND (updated >= '%1$s' OR created >= '%1$s') " +
            "AND (updated < '%2$s' OR created < '%2$s') " +
            "ORDER BY updated %3$s";

    private static final String USER_QUERY_TEMPLATE = "(assignee = %1$s OR reporter = %1$s) ";

    public static void main(String[] args) {
        CommandLineParser parser = new CommandLineParser(args);
        try {
            parser.parse();
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e);
            new HelpFormatter().printHelp(JiraReporter.class.getSimpleName(), parser.getOptions());
            System.exit(-1);
            return;
        }

        Map<String, String> domains = parser.getDomains();
        String queryBody = String.format(QUERY_TEMPLATE, parser.getStartDate(), parser.getEndDate(), parser.getIssueOrder());
        String lineBreak = "---------------------------------------------------------------------";
        System.out.println("JiraReporter");
        System.out.println(lineBreak);
        for (String user : parser.getUsernames()) {
            System.out.println("Starting to search for JIRA issues associated with user " + user);
            try (FileWriter fileWriter = new FileWriter(Paths.get("", user + ".csv").toFile());
                 CSVPrinter printer = new CSVPrinter(fileWriter, parser.getCSVFormat())) {
                for (Map.Entry<String, String> domain : domains.entrySet()) {
                    System.out.println("Searching domain " + domain.getKey() + " at url " + domain.getValue());

                    String jqlQuery = String.format(USER_QUERY_TEMPLATE, user) + queryBody;
                    JiraClient jira = new JiraClient(domain.getValue());
                    Issue.SearchResult sr;
                    try {
                        sr = jira.searchIssues(jqlQuery, parser.getIssueLimit());
                    } catch (JiraException e) {
                        System.err.println("Exception while searching domain " + domain.getKey() +
                                           ": " + e + ": " + e.getCause());
                        continue;
                    }

                    printer.printRecord(domain.getKey() + " Issues");
                    printer.printRecord(IssueProcessor.CSV_HEADERS);
                    for (Issue i : sr.issues) {
                        IssueProcessor processor = new IssueProcessor(user, i);
                        printer.printRecord(processor.getPrintableRecord());
                    }
                    printer.println();
                }
            } catch (IOException e) {
                System.err.println("Error writing to " + user + ".csv: " + e);
                continue;
            }
            System.out.println("All domains searched and results have been output to " + user + ".csv");
            System.out.println(lineBreak);
        }
        System.out.println("Searching Complete");
    }
}
