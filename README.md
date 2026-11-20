# Job Hunt Command Center

A personal tracker for ongoing job applications.

The goal of the app is to help me organize my notes, details and preparation around interviews. A central place to collect
the activity in recruitment process, as usually when one looks for a job they have several leads ongoing at a time.

I have looked into existing solutions that solve this problem already. The most interesting ones I found were **Teal** and **Huntr**,
both of them offer more features than I need, with AI tailored resume writing, career platform or kanban-style application tracking.

Ultimately, I decided to implement my own solution for this and the primary reason was the desire to own my own data.
This topic is sensitive-data, the job posting is public, but my application, feedback and offer is not.
I don't expect much usage from other people for this app, so I tailored it for myself. It is free to try out if anybody would like to:

**Live at [job-hunt-center.onrender.com](https://job-hunt-center.onrender.com)** 

I decided to use free tier hosting, both for server and database, and code fully written by AI for this app.
The UI does not have a separate repository and uses plain js and html, out of simplicity. Exploring using something like Solid 2.0 for it.

## What it does

- Shows a calendar with the Job Applications you have ongoing, month or week view
- Allows you to add a new Job Application with all the details
- Add notes to each application to quickly remember key details
- Add interview-ing rounds and debriefs 
- A small nudge when enough time has passed without hearing anything from a company, hinting you to follow up

## Free tier issues

It runs on free plans, as an experiment.

- It sleeps when idle, so the first load can take about a minute.
- There are no backups, so your data could be lost.
- It could stop or disappear at any time.
- Your data is kept apart from other users, but I can read the database.

## Run it locally

You need Docker.

```sh
docker compose up --build
```

Then open http://localhost:8080. There's no sign-in, so it only accepts connections from your own
machine.

## License

[MIT](LICENSE)
