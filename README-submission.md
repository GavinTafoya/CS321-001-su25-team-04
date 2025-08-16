### Team Name
## Team Members

| Last Name | First Name | GitHub User Name |
|-----------|------------|------------------|
| Huett     | Alexander  | Tettanger-afk    |
| TBD       | TBD        | TBD              |
| TBD       | TBD        | TBD              |

# Test Results
How many of the dumpfiles matched (using the check-dump-files.sh script)?
    The only test that consistently failed was the user.ip test. Which kept failing because of something do with reverse/address and it printing the line equal to its frequency

How many of the btree query files results matched (using the check-btree-search.sh script)?
    They all should match

How many of the database query files results matched (using the check-db-search.sh script)?
    They all should patch

# AWS Notes
Brief reflection on your experience with running your code on AWS.

# Reflection

Provide a reflection by each of the team member (in a separate subsection)

## Reflection (Team member name: Alexander Huett)
    This wasn't too bad of a project. Working in groups was actually pretty fun. Especially since everybody did something and contributed. Ahmad started on insert and I finished it, I started on search and he finished that. It was good work. Gavin contributed of course, finishing up all lot of the peripheral.

    However, when we hit week 3(of the project) it got significantly harder. Dump-to-File/Database was an absolute nightmare. There were issues with meta data. reading the file, creating the file, make sure the file had the correct number of logs or any logs, etc, etc. We still didn't get dump-check finishing. I thought I did, but something more fundemental was going on with those. I checked btree and SSHFileReader a bunch of times and wrote a bunch of tests for both, but it was still broken.
## Reflection (Team member name: )
## Reflection (Team member name: )

# Additional Notes
TBD

