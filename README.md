# Job Hunt Command Center

Job Hunt Command Center is a personal job application tracker: a dashboard with a calendar of
what's coming up, and one central place for everything about each application.

I built it to keep myself organized while going through several interview processes at once. It
does only that, and it only grows when real use shows a need.

## What it does

- **Says what to do next.** The dashboard puts the jobs that need you first: an interview within
  48 hours, an offer waiting for an answer, a debrief you haven't written, a company silent for two
  weeks.
- **Shows what's coming.** A week or month calendar lays out your interviews across all your
  applications.
- **Remembers each interview.** A debrief keeps how it felt, how hard it was and what they asked.
  The next round often builds on the last.
- **Keeps the listing.** Listings disappear, often just when you need them to prepare. The text
  stays with the job, next to your salary expectation and any offer.
- **Shows each company's process.** Every job has its own timeline of rounds, so you can see how
  differently companies hire.

## Try it

I plan to host it on free plans, as a simple experiment. Free hosting has tight limits, so access is
by invitation, and you can request it on the sign-in page. If it moves to proper hosting, it may
open to everyone.

Free plans come with real limits:

- It sleeps when idle, so the first load can take about a minute.
- There are no backups, so your data could be lost.
- It could stop or disappear at any time.
- Your data is kept apart from other users, but I can read the database.

## How it's built

**Each person's data stays theirs.** Every repository method takes the owner, so a query that forgets
it doesn't compile. A round's foreign key includes the owner too, so the database itself refuses to
attach it to someone else's job.

**Your data can leave.** You can download everything as JSON. On a hosted copy, one button deletes
your account: all your data hangs off a single row, so it goes in one step, and your sessions end
with it. The database stores no email addresses, only an id derived from your sign-in account.

**Closed unless you open it.** The default configuration is the hosted one. Without sign-in settings
it refuses to start, and with an empty invitation list nobody gets in. Running without sign-in
takes an explicit `local` profile.

**The urgency rules are one pure function.** [`SituationRules`](src/main/java/org/manu/jobhunt/application/overview/SituationRules.java)
is the most opinionated part of the app: a list of checks in priority order, where the first match
wins.

**One small deployable.** The API and pages ship as one Spring Boot app, with plain JavaScript, no
frontend build and no CDN. It's made for free hosting: sessions live in Postgres, so you stay signed
in when the host sleeps.

Java 21, Spring Boot 4.1, PostgreSQL 16.

## Run it locally

You need Docker.

```sh
docker compose up --build
```

Then open http://localhost:8080. There's no sign-in, so it only accepts connections from your own
machine.

## License

[MIT](LICENSE)
