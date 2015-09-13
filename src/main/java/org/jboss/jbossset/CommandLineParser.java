package org.jboss.jbossset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Ryan Emerson
 */
public class CommandLineParser {

    private final Map<String, CSVFormat> validCSVFormats;
    private final String[] args;
    private final Options options = new Options();
    private List<String> usernames;
    private LocalDate startDate = LocalDate.of(1990, 1, 1);
    private LocalDate endDate = LocalDate.now();
    private String issueOrder = "ASC";
    private CSVFormat csvFormat = CSVFormat.EXCEL;
    private int issueLimit = 50;
    private Map<String, String> domains = new HashMap<>();

    {
        validCSVFormats = new HashMap<>();
        validCSVFormats.put("excel", CSVFormat.EXCEL);
        validCSVFormats.put("mysql", CSVFormat.MYSQL);
        validCSVFormats.put("rfc4180", CSVFormat.RFC4180);
        validCSVFormats.put("tdf", CSVFormat.TDF);
    }

    public CommandLineParser(String[] args) {
        this.args = args;
        addOptions();
    }

    public void parse() throws ParseException {
        CommandLine cmd = new DefaultParser().parse(options, args);

        if (cmd.hasOption("help")) {
            new HelpFormatter().printHelp(JiraReporter.class.getSimpleName(), options);
            System.exit(0);
            return;
        }

        usernames = getValidUsernames(cmd);
        domains = getValidDomains(cmd);

        if (cmd.hasOption("startDate"))
            startDate = getValidLocalDate(cmd.getOptionValue("startDate"), "startDate");

        if (cmd.hasOption("endDate"))
            endDate = getValidLocalDate(cmd.getOptionValue("endDate"), "endDate");

        if (startDate.isAfter(endDate))
            throw new ParseException("startDate cannot be after the specified endDate.");

        if (cmd.hasOption("lifo"))
            issueOrder = "DESC";

        if (cmd.hasOption("csvFormat"))
            csvFormat = getValidCSVFormat(cmd.getOptionValue("csvFormat"));

        if (cmd.hasOption("issueLimit"))
            issueLimit = getValidIssueLimit(cmd.getOptionValue("issueLimit"));
    }

    public Options getOptions() {
        return options;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getIssueOrder() {
        return issueOrder;
    }

    public CSVFormat getCSVFormat() {
        return csvFormat;
    }

    public int getIssueLimit() {
        return issueLimit;
    }

    public Map<String, String> getDomains() {
        return domains;
    }

    private LocalDate getValidLocalDate(String date, String optionName) throws ParseException {
        try {
            LocalDate localDate = LocalDate.parse(date);
            if (optionName.equals("startDate") && localDate.isAfter(LocalDate.now()))
                throw new ParseException("startDate cannot be in the future.");
            return localDate;
        } catch (DateTimeParseException e) {
            throw new ParseException("Invalid " + optionName + ". Date format must be YYYY-MM-DD.");
        }
    }

    private List<String> getValidUsernames(CommandLine cmd) throws ParseException {
        boolean userFileSet = cmd.hasOption("userFile");
        boolean usernamesSet = cmd.hasOption("usernames");
        if (!userFileSet && !usernamesSet)
            throw new ParseException("You must specify either the usernames or userFile option");

        if (userFileSet && usernamesSet)
            throw new ParseException("The usernames and userFile cannot be set at the same time");

        if (usernamesSet) {
            String[] values = cmd.getOptionValues("usernames");
            return Arrays.asList(values);
        }

        String url = cmd.getOptionValue("userFile");
        if (!new File(url).exists())
            throw new ParseException("The specified file <" + url + "> does not exist");

        List<String> users;
        try {
            users = Files.readAllLines(Paths.get(url));
        } catch (IOException e) {
            throw new ParseException("The specified file <" + url + "> cannot be opened");
        }
        return users;
    }

    private Map<String, String> getValidDomains(CommandLine cmd) throws ParseException {
        Properties properties = new Properties();
        if (cmd.hasOption("domainFile")) {
            String url = cmd.getOptionValue("domainFile");
            File file = new File(url);
            if (!file.exists())
                throw new ParseException("The specified file does not exist");

            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                throw new ParseException(e.getMessage() + ": " + e.getCause());
            }

            if (properties.isEmpty())
                throw new ParseException("The specified file does not contain any domain pairs");
        } else {
            try (InputStream in = JiraReporter.class.getClassLoader().getResourceAsStream("domains")) {
                properties.load(in);
            } catch (IOException e) {
                throw new ParseException("Default domains file could not be read: " + e);
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map) properties;
        return map;
    }

    private CSVFormat getValidCSVFormat(String specifiedFormat) throws ParseException {
        CSVFormat format = validCSVFormats.get(specifiedFormat.toLowerCase());

        if (format == null)
            throw new ParseException("Invalid CSV format. Valid options are " + validCSVFormats.entrySet());
        return format;
    }

    private int getValidIssueLimit(String issueLimit) throws ParseException {
        try {
            int limit = new Integer(issueLimit);
            if (limit > 0)
                return limit;
        } catch (NumberFormatException e) {
        }
        throw new ParseException("Invalid issueLimit value: " + issueLimit + ". A positive integer is expected");
    }

    private void addOptions() {
        addUsernameOptions();
        addDateOptions();
        addBooleanFlags();
        addCSVTypeOption();
        addIssueLimitOption();
        addDomainFileOption();
    }

    private void addUsernameOptions() {
        options.addOption(Option.builder("u")
                .argName("usernames")
                .longOpt("usernames")
                .desc("Pass the JIRA usernames to be searched as command line options.")
                .required(false)
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .valueSeparator(' ')
                .build());

        options.addOption(Option.builder("uf")
                .argName("userFile")
                .longOpt("userFile")
                .desc("Read JIRA usernames from the specified file.")
                .required(false)
                .numberOfArgs(1)
                .build());
    }

    private void addDateOptions() {
        options.addOption(Option.builder("s")
                .argName("startDate")
                .longOpt("startDate")
                .desc("The date from which JIRAs are returned.")
                .required(false)
                .numberOfArgs(1)
                .build());

        options.addOption(Option.builder("e")
                .argName("endDate")
                .longOpt("endDate")
                .desc("The date of the most recent JIRA to be returned.")
                .required(false)
                .numberOfArgs(1)
                .build());
    }

    private void addBooleanFlags() {
        options.addOption(new Option("lifo", "JIRA issues are output from the most recently updated issue."));
        options.addOption(new Option("h", "help", false, "Display this help and exit."));
    }

    private void addCSVTypeOption() {
        options.addOption(Option.builder("c")
                .argName("csvFormat")
                .longOpt("csvFormat")
                .desc("The format of the created CSV files. Valid options are " + validCSVFormats.keySet())
                .required(false)
                .numberOfArgs(1)
                .build());
    }

    private void addIssueLimitOption() {
        options.addOption(Option.builder("l")
                .argName("issueLimit")
                .longOpt("issueLimit")
                .desc("The maximum number of JIRA issues that will be returned for each domain.")
                .required(false)
                .numberOfArgs(1)
                .build());
    }

    private void addDomainFileOption() {
        options.addOption(Option.builder("d")
                .argName("domainFile")
                .longOpt("domainFile")
                .desc("A properties file which contains a key name and url for each of the domains to be searched.")
                .required(false)
                .numberOfArgs(1)
                .build());
    }
}
