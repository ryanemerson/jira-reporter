/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
