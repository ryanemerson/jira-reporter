package org.jboss.jbossset;

import net.rcarz.jiraclient.Comment;
import net.rcarz.jiraclient.Issue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Emerson
 */
public class IssueProcessor {

    public static final Object[] CSV_HEADERS = {"key", "project", "summary", "status", "reporter", "assignee"};

    private final String user;
    private final Issue issue;

    public IssueProcessor(String user, Issue issue) {
        this.user = user;
        this.issue = issue;
    }

    public List getPrintableRecord() {
        List<Object> record = new ArrayList<>();
        record.add(issue.getKey());
        record.add(issue.getProject().getName());
        record.add(issue.getSummary());
        record.add(issue.getStatus());
        record.add(issue.getReporter());
        record.add(issue.getAssignee());
//        record.add(getNumberOfUserComments(user, issue));
        return record;
    }

    // Jira client does not return any comments at the moment
    private int getNumberOfUserComments(Issue issue) {
        int numberOfComments = 0;
        for (Comment c : issue.getComments())
            if (c.getAuthor().getDisplayName().equals(user))
                numberOfComments++;
        return numberOfComments;
    }
}
