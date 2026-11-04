/* Home: shows and sorts. Every job opens the Job screen the same way. */
(function () {
  "use strict";

  const { api, el, dateKey, formatDate, formatDateTime, roundName, situationText, showError, closeOnCancel, formValues, STATUS_LABELS } = window.JHCC;

  const THEIR_MOVE = ["ROUND_LATER", "WAITING"];
  const PHASES = ["WISHLIST", "APPLIED", "INTERVIEWING", "OFFER"];

  function text(id, value) { document.getElementById(id).textContent = value; }

  function plural(n, word) { return n + " " + word + (n === 1 ? "" : "s"); }

  function phaseBar(job) {
    const reached = PHASES.indexOf(job.status);
    return el("span", { class: "phase-bar", "aria-hidden": "true" },
      PHASES.map((_, i) => el("span", { class: i <= reached ? "on" : "" })));
  }

  function card(summary) {
    const job = summary.job;
    const yours = !THEIR_MOVE.includes(summary.situation.situation);
    return el("a", { class: "job-card job-" + job.colorIndex, href: "/jobs/" + job.id },
      el("span", { class: "job-card-top" },
        el("strong", { text: job.company }),
        el("span", { class: "pill", text: STATUS_LABELS[job.status] })),
      el("span", { class: "job-card-role", text: job.roleTitle }),
      phaseBar(job),
      el("span", { class: "job-card-line" + (yours ? " yours" : ""), text: situationText(summary.situation) }));
  }

  function group(title, summaries) {
    if (summaries.length === 0) return [];
    return [el("h2", { class: "group-title" }, title + " ", el("span", { class: "count", text: String(summaries.length) })),
      ...summaries.map(card)];
  }

  function closedRow(job) {
    return el("a", { class: "closed-row", href: "/jobs/" + job.id },
      el("span", { text: job.company }),
      el("span", { class: "muted", text: STATUS_LABELS[job.status] + (job.closedDate ? ", " + formatDate(job.closedDate) : "") }));
  }

  function renderStats(home, mine) {
    text("stat-move", String(mine.length));
    document.querySelector(".stat-move").classList.toggle("active", mine.length > 0);
    text("stat-move-note", mine.length === 0 ? "Nothing needs you" : mine.length === 1 ? "job needs you" : "jobs need you");
    text("stat-active", String(home.active.length));
    text("stat-active-note", home.closed.length ? home.closed.length + " closed" : "None closed yet");

    const upcoming = home.active
      .filter((s) => s.situation.situation === "ROUND_SOON" || s.situation.situation === "ROUND_LATER")
      .sort((a, b) => a.situation.round.scheduledAt.localeCompare(b.situation.round.scheduledAt))[0];
    text("stat-next", upcoming ? upcoming.job.company : "None scheduled");
    text("stat-next-note", upcoming ? roundName(upcoming.situation.round) + ", " + formatDateTime(upcoming.situation.round.scheduledAt) : "Add a round when one is booked");

    text("stat-week", String(home.applicationsSentThisWeek));
    text("stat-week-note", plural(home.applicationsSentThisWeek, "application") + " sent, " + plural(home.roundsHeldThisWeek, "round") + " held");
  }

  async function load() {
    try {
      const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const home = await api.get("/api/overview?tz=" + encodeURIComponent(tz));
      const mine = home.active.filter((s) => !THEIR_MOVE.includes(s.situation.situation));
      const waiting = home.active.filter((s) => THEIR_MOVE.includes(s.situation.situation));

      renderStats(home, mine);
      const jobs = document.getElementById("jobs");
      if (home.active.length === 0) {
        jobs.replaceChildren(el("p", { class: "empty", text: "Add your first job to start tracking it." }));
      } else {
        jobs.replaceChildren(...group("Your move", mine), ...group("Waiting on them", waiting));
      }

      document.getElementById("closed-section").hidden = home.closed.length === 0;
      text("closed-title", "Closed (" + home.closed.length + ")");
      document.getElementById("closed-jobs").replaceChildren(...home.closed.map(closedRow));
    } catch (err) {
      showError(err);
    }
  }

  function wireAddJob() {
    const dialog = document.getElementById("job-dialog");
    const form = document.getElementById("job-form");
    closeOnCancel(dialog);

    document.getElementById("add-job").addEventListener("click", () => {
      form.reset();
      form.elements.appliedDate.value = dateKey(new Date());
      form.elements.appliedDate.disabled = false;
      dialog.showModal();
    });

    form.elements.status.addEventListener("change", () => {
      const wishlist = form.elements.status.value === "WISHLIST";
      form.elements.appliedDate.disabled = wishlist;
      form.elements.appliedDate.value = wishlist ? "" : dateKey(new Date());
    });

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      try {
        const job = await api.post("/api/jobs", formValues(form));
        window.location.href = "/jobs/" + job.id;
      } catch (err) {
        showError(err);
      }
    });
  }

  document.addEventListener("DOMContentLoaded", () => {
    text("today", new Date().toLocaleDateString(undefined, { weekday: "long", day: "numeric", month: "long" }));
    wireAddJob();
    load();
  });
})();
