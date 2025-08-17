### Team Name
## Team Members

| Last Name | First Name | GitHub User Name |
|-----------|------------|------------------|
| Huett     | Alexander  | Tettanger-afk    |
| Rao       | Ahmad      | arjrao91219      |
| Tafoya    | Gavin      | GavinTafoya      |

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
## Reflection (Team member name: Ahmad Rao )
    This project wasnt too bad and working with group was a pretty fun aspect and made life easier. Everyone in our groups co-operated very well. I started on the insert and created helped methods for it which wasnt to bad and the cache implementation. Alex did the search and I helped complete it. Gavin did the diskwrite and the dikread and the extra credit stuff aswell.

    This week (week 3) was a lot harder in my opinion. It wasnt really logic based but wiring things together which was a lot harder for me. Me and Alex were trying to get thr scripts to run. It took me a long time to figure out chek-btree-search.sh and check-db-search.sh to start working. Although we didnt get to the dump-check failing. I couldnt find whats wrong with it. I changed the btree and SSHcreatebtree and the SSHFile reader and wrote a bunch of tests like Alex but it still was broken. 


## Reflection (Team member name: Gavin Tafoya)
    Overall I really liked the concept of this project, and working with my team. I liked how we were able to each contribute to differnt parts of the project and see them all come together in the end. It was interesting to see the project evolve over time, as we all have different approaches when it comes to a project like this, and working in a team was good way to understand how differnt stragegies can mesh and clash in certain circumstances. I do think that was a challege for me, because if I had been on the project alone, I would have done everything with my own stragy, but we had to figure out how our solutions worked together, which was a great experience to learn from. The communication between teammates was also great, and we all tried to support each other's tasks when we could.

    I worked on the disk functions and creating the Btree the first 2 weeks, and then made the Data Wrangler for the extra credit piece this week while Ahmad and Alex made sure we passed the tests correctly. I didn't find my portions of the project to be difficult, as most of it was adapting the disk functions from the example to work witrh a btree, but I really enjoyed getting to look at how the data was being written to the disk and watching how memory was used in that regard. I really liked those parts of the project and will probably look further into them on my own time.
    
# Additional Notes
TBD

