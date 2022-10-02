# High Way Cinema

Our client has a cinema in Wrocław, Poland. Currently, all movie scheduling is done using Pen and Paper on a big board where there is a plan for given time of all the movies the cinema shows. Planner Jadwiga needs to schedule screenings for best use of the space.

## Board overview

![Cinema - page 1 copy](https://user-images.githubusercontent.com/34231627/150541482-0b1e4a66-4298-4d3e-846f-c62ba1c8e37b.png)


## Domain requirements

We would like to help Jadwiga to do a better job with her weekly task of planning the screenings. The idea is to create a virtual board that she will be able to add screenings to.

User Stories:
- Planner Jadwiga will be able to schedule a screening for a given movie at a particular time every day of week between 8:00 and 22:00.
- Any 2 scheduled movies can't be screened at the same time in the same room. Even overlapping is forbidden.
- Every screening has to have a maintenance slot to clean up the whole room. Every room has a different cleaning slot.
- Some movies may require 3d.
- Not all movies are equal, e.g. Premiere has to be after working hours around 17:00-21:00.
- There is a possibility that a given room may not be available for a particular time slot or on specific days.


Your task is to model the weekly planning of the screenings by Jadwiga.

## Assumption
- The catalog of movies already exists (saying whether they need 3d glasses, how long the movie takes)

### Challenge notes

* The Movie Catalog is not in scope of this challenge but some model will be required to fulfill the given task
* Consider concurrent modification. How to solve the problem of two Jadwigas adding different movies at the same time and in the same room.
* If you have a question about the requirements simply just ask us.
* If during the assignment you decide to work on a real database and UI you will lose precious time, so we encourage you to not do so.

#### What we care for:
- Solid domain model
- Quality of tests
- Clean code
- Proper module/context modeling

#### What we don’t care for:
- UI to be implemented
- Using a database
- All the cases to be covered.

#### What we expect from solution:
- Treat it like production code. Develop your software in the same way that you would for any other code that is intended to be deployed to production.
- Would be good to describe the decisions you make so that future developers won't be scratching their heads about the reasoning.
- Tests should be green.
- Code should be on github repo.

------

## Q&A regarding the requirements:

**Q. Does the "8:00-22:00" requirement mean that the movie has to *start* or *end* before 22?**

It should start before 22.

**Q. Should the schedule operate on actual dates, or just days of a week? "Actual dates" seem more future-proof, as they allow for extending the schedule beyond a single week, while still allowing a creation of weekly schedules grouped by day.**

Let's use actual dates.

**Q. Do we expect the schedule to always be divided into days?**

We may have days when there are movies being screened after midnight which should still be assigned to the previous day, but otherwise there will always be a break between days.

*Implementation note:* In the end the implementation assumes all movies start before midnight, but it should be fairly easy to add after-midnight screenings.

**Q. Does room unavailability during specific days mean actual dates or days of a week?**

Rooms may be unavailable during given days of a week, e.g. room 1 might be unavailable every Monday between 8:00 and 12:00.

## Implementation notes:

I took the liberty of renaming "seans" to "screening" :).

SchedulingService is the entry point of both scheduling new screenings and getting a schedule. It's responsible for performing all communications with the "outside". The remaining classes in the schedule package are immutable and perform most of the business logic.

Concurrent modification could be solved using optimistic locking, where we check whether there haven't been any updates before saving the modification. In SQL it would look something like this:

```sql
UPDATE items SET version = $version, other_field = some_value WHERE version = $version - 1 
```

A part of that solution can be seen in the SchedulingService.

Things potentially missing:
* logging
* REST API
* support for a daily schedule which extends beyond midnight
* tests for exception messages
* movie and room repositories' support for modification, i.e. they're unusable outside of tests
