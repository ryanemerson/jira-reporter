package org.jboss.jbossset;

import org.apache.commons.cli.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author Ryan Emerson
 */
public class JiraReporterTest {

    private static final int NUMBER_OF_USERS = 10;
    private static final String USERS_FILE_URL = "users";
    private static final String DOMAIN_FILE_URL = "domains";

    @BeforeClass
    public static void init() throws Exception {
        List<String> users = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_USERS; i++)
            users.add("user" + i);
        Files.write(Paths.get("", USERS_FILE_URL), users, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        Properties p = new Properties();
        p.put("JIRA", "https://issues.jboss.org");
        p.store(new PrintWriter(new File(DOMAIN_FILE_URL)), null);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Files.delete(Paths.get("", USERS_FILE_URL));
        Files.delete(Paths.get("", DOMAIN_FILE_URL));
    }

    @Test(expected = ParseException.class)
    public void testNoUsers() throws ParseException {
        String cmd = "-u";
        new CommandLineParser(cmd.split(" ")).parse();
    }

    @Test
    public void testUsernamesOption() throws ParseException {
        StringBuilder sb = new StringBuilder("-u ");
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            sb.append("user");
            sb.append(i);
            sb.append(" ");
        }
        CommandLineParser parser = new CommandLineParser(sb.toString().split(" "));
        parser.parse();
        testUsernames(parser.getUsernames());
    }

    @Test
    public void testUserFileOption() throws Exception {
        String cmd = "-uf " + USERS_FILE_URL;
        CommandLineParser parser = new CommandLineParser(cmd.split(" "));
        parser.parse();
        testUsernames(parser.getUsernames());
    }

    @Test(expected = ParseException.class)
    public void testBothUserFileAndCmds() throws ParseException {
        String cmd = "-u user1 -uf " + USERS_FILE_URL;
        new CommandLineParser(cmd.split(" ")).parse();
    }

    @Test(expected = ParseException.class)
    public void testInvalidDate() throws ParseException {
        String cmd = "-u user1 -s 01-08-2015";
        new CommandLineParser(cmd.split(" ")).parse();
    }

    @Test(expected = ParseException.class)
    public void testInvalidadStartDate() throws ParseException {
        String cmd = "-u user1 -s " + LocalDate.now().plusDays(1);
        new CommandLineParser(cmd.split(" ")).parse();
    }

    @Test(expected = ParseException.class)
    public void testStartAfterEndDate() throws ParseException {
        String cmd = "-u user1 -s " + LocalDate.of(2015, 2, 1) + " -e " + LocalDate.of(2015, 1, 1);
        new CommandLineParser(cmd.split(" ")).parse();
    }

    @Test
    public void testDefaultDomainsLoad() throws ParseException{
        String cmd = "-u user1 ";
        CommandLineParser parser = new CommandLineParser(cmd.split(" "));
        parser.parse();
        assertNotNull(parser.getDomains());
    }

    private void testUsernames(List<String> users) {
        assertNotNull(users);
        assertEquals(users.size(), NUMBER_OF_USERS);
        for (int i = 0; i < NUMBER_OF_USERS; i++)
            assertEquals(users.get(i), "user" + i);
    }
}
