# Jira Reporter

A simple script to retrieve a summary of JIRA issues associated with a specified username.

## Installation

Requires Java 8 and maven.

```bash
> git clone git@github.com:ryanemerson/jira-reporter.git
> cd jira-reporter
> mvn clean package
```

## Running
The minimum requirement is for a list of usernames to be passed to the jar.  This can be achieved via
the command line by specifying the --usernames flag followed by a list of Jira usernames, or by passing
the path of a text file via the --userFile flag. Note, the text file must specify each distinct username
on a new line and must contain no additional Strings.

```bash
> java -jar target/jira-reporter-<VERSION>.jar
```

### Optional Arguments
A list of all available arguments can be found by passing the ```--help``` or ```-h``` flag to the jar.
