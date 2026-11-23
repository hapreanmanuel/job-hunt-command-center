# Job Hunt Command Center

A personal tracker for ongoing job applications. **Discontinued**, replaced by
[ASAP (Applications Saved As Private)](https://github.com/heaped-and-stacked/applications-saved-as-private).

The goal of the app is to help me organize my notes, details and preparation around interviews. A central place to collect
the activity in recruitment process, as usually when one looks for a job they have several leads ongoing at a time.

I have looked into existing solutions that solve this problem already. The most interesting ones I found were **Teal** and **Huntr**,
both of them offer more features than I need, with AI tailored resume writing, career platform or kanban-style application tracking.

Ultimately, I decided to implement my own solution for this and the primary reason was the desire to own my own data.
This topic is sensitive-data, the job posting is public, but my application, feedback and offer is not.

## Discontinued

The hosted copy is taken down and this repository is no longer maintained.

A server with a database works against the reason this app exists. The data sits in a database hosted by someone else,
and whoever runs it, me included, can read it. On free plans it also sleeps when idle, has no backups and can disappear at any time.

ASAP keeps the idea and drops the server. It is a static page with no database, and each user's data stays in a single file
in their own Google Drive, synced across their devices, phone included. Most of the logic had to move to the browser for this
anyway, so it was also a good chance to try Solid 2.0.

## What it does

- Shows a calendar with the Job Applications you have ongoing, month or week view
- Allows you to add a new Job Application with all the details
- Add notes to each application to quickly remember key details
- Add interview-ing rounds and debriefs 
- A small nudge when enough time has passed without hearing anything from a company, hinting you to follow up

## Run it locally

You need Docker.

```sh
docker compose up --build
```

Then open http://localhost:8080. There's no sign-in, so it only accepts connections from your own
machine.

## License

[MIT](LICENSE)
