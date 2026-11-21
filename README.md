# 207-course-project
A better timetable builder!

Don't push to main, work on new branch, push the branch, create pull request.

## First user story

As a student, I want to search by course code and see the matching offerings,
their sections, and their meeting times, so I can choose sections that fit my
schedule.

The first feature will support an exact course-code search, ignoring capitalization
and surrounding whitespace. Results will distinguish offerings by term and campus
and show their sections, meeting times, cancellation status, and unknown-time
information.

See [the architecture plan](docs/architecture.md) for the agreed responsibilities,
interfaces, implementation order, and coding conventions.

## Catalogue data

`uoft-ttb-raw-catalogue-2026-09-05.zip` contains `courses.json`, a catalogue snapshot
with 8,172 offering records (about 130 MB uncompressed). Course codes can appear in
multiple offerings. The data is a snapshot, not live enrolment information.

## Project status

The repository currently contains catalogue data and planning documentation.
Application code, build configuration, and tests have not been added yet.
