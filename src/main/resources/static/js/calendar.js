/*
 * The calendar on Home: an overview. Hover shows details, click opens the job.
 * Week is an hour grid with a lane for all-day events, month is a compact grid.
 */
(function () {
  "use strict";

  const { api, el, dateKey, formatDateTime, formatTime, roundName, showError, STATUS_LABELS } = window.JHCC;

  const FIRST_HOUR = 8;
  const LAST_HOUR = 20;
  const MONTH_CHIPS = 3;

  let view = "week";
  let anchor = new Date();

  function startOfWeek(d) {
    const monday = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    monday.setDate(monday.getDate() - ((monday.getDay() + 6) % 7));
    return monday;
  }

  function addDays(d, n) {
    const next = new Date(d);
    next.setDate(next.getDate() + n);
    return next;
  }

  function visibleDays() {
    const first = view === "week" ? startOfWeek(anchor) : startOfWeek(new Date(anchor.getFullYear(), anchor.getMonth(), 1));
    let count = 7;
    if (view === "month") {
      const lastOfMonth = new Date(anchor.getFullYear(), anchor.getMonth() + 1, 0);
      count = Math.round((startOfWeek(lastOfMonth).getTime() - first.getTime()) / 86400000) + 7;
    }
    return Array.from({ length: count }, (_, i) => addDays(first, i));
  }

  function title(days) {
    if (view === "month") return anchor.toLocaleDateString(undefined, { month: "long", year: "numeric" });
    const options = { day: "numeric", month: "short" };
    return days[0].toLocaleDateString(undefined, options) + " - " + days[6].toLocaleDateString(undefined, options);
  }

  /** Applied and closed dates, the milestones that have a day but no time. */
  function milestones(jobs) {
    const items = [];
    jobs.forEach((job) => {
      if (job.appliedDate) items.push({ job, key: job.appliedDate, label: "Applied" });
      if (job.closedDate) items.push({ job, key: job.closedDate, label: STATUS_LABELS[job.status] });
    });
    return items;
  }

  function bucket(items, keyOf) {
    const map = new Map();
    items.forEach((item) => {
      const key = keyOf(item);
      if (!map.has(key)) map.set(key, []);
      map.get(key).push(item);
    });
    return map;
  }

  function milestoneChip(m) {
    return el("a", {
      class: "milestone-chip job-" + m.job.colorIndex,
      href: "/jobs/" + m.job.id,
      title: m.label + ": " + m.job.company + ", " + m.job.roleTitle,
    }, el("span", { class: "chip-label", text: m.label }), " " + m.job.company);
  }

  function roundTitle(s) {
    return s.company + ", " + s.roleTitle + "\n" + roundName(s) + ", " + formatDateTime(s.scheduledAt);
  }

  function roundChip(s) {
    return el("a", {
      class: "round-chip job-" + s.colorIndex + (s.outcome === "CANCELLED" ? " cancelled" : ""),
      href: "/jobs/" + s.jobId,
      title: roundTitle(s),
    }, el("span", { class: "chip-label", text: formatTime(s.scheduledAt) }), " " + s.company);
  }

  function hourOf(date) {
    return date.getHours() + date.getMinutes() / 60;
  }

  function percentOfDay(hour) {
    return (hour - FIRST_HOUR) / (LAST_HOUR - FIRST_HOUR) * 100;
  }

  function weekEvent(s, index, siblings) {
    const start = Math.min(Math.max(hourOf(new Date(s.scheduledAt)), FIRST_HOUR), LAST_HOUR - 1);
    const event = el("a", {
      class: "week-event job-" + s.colorIndex + (s.outcome === "CANCELLED" ? " cancelled" : ""),
      href: "/jobs/" + s.jobId,
      title: roundTitle(s),
    },
    el("strong", { text: s.company }),
    el("span", { text: roundName(s) + ", " + formatTime(s.scheduledAt) }));
    event.style.top = percentOfDay(start) + "%";
    event.style.height = (100 / (LAST_HOUR - FIRST_HOUR)) + "%";
    event.style.left = (index / siblings * 100) + "%";
    event.style.width = (100 / siblings) + "%";
    return event;
  }

  function nowLine() {
    const hour = hourOf(new Date());
    if (hour < FIRST_HOUR || hour > LAST_HOUR) return null;
    const line = el("div", { class: "now-line" });
    line.style.top = percentOfDay(hour) + "%";
    return line;
  }

  function renderWeek(days, rounds, marks) {
    const today = dateKey(new Date());
    const heads = days.map((d) => el("div", { class: "week-head" + (dateKey(d) === today ? " today" : "") },
      el("span", { text: d.toLocaleDateString(undefined, { weekday: "short" }) }),
      el("strong", { text: String(d.getDate()) })));
    const lanes = days.map((d) => el("div", { class: "week-lane" }, (marks.get(dateKey(d)) || []).map(milestoneChip)));

    const hours = [];
    for (let h = FIRST_HOUR; h < LAST_HOUR; h++) {
      hours.push(el("span", { text: String(h).padStart(2, "0") + ":00" }));
    }
    const columns = days.map((d) => {
      const key = dateKey(d);
      const items = rounds.get(key) || [];
      return el("div", { class: "week-col" + (key === today ? " today" : "") },
        items.map((s, i) => weekEvent(s, i, items.length)),
        key === today ? nowLine() : null);
    });

    return [
      el("div", { class: "week-row" }, el("div"), heads),
      el("div", { class: "week-row lane-row" }, el("div", { class: "lane-label", text: "Events" }), lanes),
      el("div", { class: "week-row week-body" }, el("div", { class: "week-hours" }, hours), columns),
    ];
  }

  function renderMonth(days, rounds, marks) {
    const today = dateKey(new Date());
    const heads = days.slice(0, 7).map((d) => el("div", {
      class: "month-head", text: d.toLocaleDateString(undefined, { weekday: "short" }),
    }));
    const cells = days.map((d) => {
      const key = dateKey(d);
      const outside = d.getMonth() !== anchor.getMonth();
      const chips = [...(rounds.get(key) || []).map(roundChip), ...(marks.get(key) || []).map(milestoneChip)];
      const hidden = chips.length - MONTH_CHIPS;
      return el("div", { class: "month-day" + (key === today ? " today" : "") + (outside ? " outside" : "") },
        el("span", { class: "month-num", text: String(d.getDate()) }),
        chips.slice(0, MONTH_CHIPS),
        hidden > 0 ? el("span", { class: "more", text: "+" + hidden + " more" }) : null);
    });
    return [el("div", { class: "month-grid" }, heads, cells)];
  }

  async function refresh() {
    const days = visibleDays();
    const calendar = document.getElementById("calendar");
    document.getElementById("cal-title").textContent = title(days);
    try {
      const [found, jobs] = await Promise.all([
        api.get("/api/rounds?from=" + dateKey(days[0]) + "&to=" + dateKey(days[days.length - 1])),
        api.get("/api/jobs"),
      ]);
      const rounds = bucket(found.filter((s) => s.scheduledAt), (s) => dateKey(new Date(s.scheduledAt)));
      const marks = bucket(milestones(jobs), (m) => m.key);
      calendar.className = "calendar " + view;
      calendar.replaceChildren(...(view === "week" ? renderWeek(days, rounds, marks) : renderMonth(days, rounds, marks)));
    } catch (err) {
      showError(err);
    }
  }

  function shift(direction) {
    if (view === "week") anchor = addDays(anchor, 7 * direction);
    else anchor = new Date(anchor.getFullYear(), anchor.getMonth() + direction, 1);
    refresh();
  }

  function setView(next) {
    view = next;
    document.getElementById("cal-week").setAttribute("aria-pressed", String(next === "week"));
    document.getElementById("cal-month").setAttribute("aria-pressed", String(next === "month"));
    refresh();
  }

  document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("cal-prev").addEventListener("click", () => shift(-1));
    document.getElementById("cal-next").addEventListener("click", () => shift(1));
    document.getElementById("cal-today").addEventListener("click", () => { anchor = new Date(); refresh(); });
    document.getElementById("cal-week").addEventListener("click", () => setView("week"));
    document.getElementById("cal-month").addEventListener("click", () => setView("month"));
    refresh();
  });
})();
