/* Job: the full story of one job, and the only place you act on it. */
(function () {
  "use strict";

  const {
    api, el, toast, showError, dateKey, toLocalInput, formatDateTime, formatDate, roundName, situationText,
    closeOnCancel, fillSelect, formValues, STATUS_LABELS, CLOSED_STATUSES, ROUND_LABELS, RESULT_LABELS,
  } = window.JHCC;

  const WORK_MODES = { REMOTE: "Remote", HYBRID: "Hybrid", ONSITE: "Onsite" };
  const SOURCES = {
    FOUND_MYSELF: "I found it and applied", RECRUITER: "A recruiter contacted me",
    REFERRAL: "Someone referred me", OTHER: "Other",
  };

  const PHASES = ["WISHLIST", "APPLIED", "INTERVIEWING", "OFFER"];

  const jobId = window.location.pathname.split("/").pop();
  let job = null;
  let situation = null;
  let editingRound = null;
  let debriefingRound = null;

  async function load() {
    try {
      const [loadedJob, rounds, loadedSituation] = await Promise.all([
        api.get("/api/jobs/" + jobId),
        api.get("/api/jobs/" + jobId + "/rounds"),
        api.get("/api/jobs/" + jobId + "/situation"),
      ]);
      job = loadedJob;
      situation = loadedSituation;
      render(rounds);
      document.getElementById("job").hidden = false;
    } catch (err) {
      if (err.status === 404) document.getElementById("not-found").hidden = false;
      else showError(err);
    }
  }

  function render(rounds) {
    document.title = job.company + " - Job Hunt Command Center";
    document.getElementById("job-dot").className = "dot job-" + job.colorIndex;
    document.getElementById("job-company").textContent = job.company;
    document.getElementById("job-role").textContent = job.roleTitle;
    document.getElementById("job-status").textContent = STATUS_LABELS[job.status] + (job.archived ? ", archived" : "");
    document.getElementById("archive-job").textContent = job.archived ? "Unarchive" : "Archive";
    renderStepper();
    renderNextStep();
    renderTimeline(rounds);
    renderListing();
    renderDetails();
  }

  function renderStepper() {
    const closed = CLOSED_STATUSES.includes(job.status);
    const steps = ["Wishlist", "Applied", "Interviewing", "Offer", closed ? STATUS_LABELS[job.status] : "Outcome"];
    const current = closed ? 4 : PHASES.indexOf(job.status);
    const stepper = document.getElementById("stepper");
    stepper.className = "stepper job-" + job.colorIndex;
    stepper.replaceChildren(...steps.map((text, i) =>
      el("div", { class: "step" + (i < current ? " done" : i === current ? " current" : ""), text })));
  }

  /** What the highlighted line says and does, per situation. */
  function nextStep() {
    const updateStatus = { label: "Update status", run: openStatus };
    const viewListing = { label: "View listing", run: showListing };
    const closed = CLOSED_STATUSES.includes(job.status);
    if (closed) return { text: STATUS_LABELS[job.status] + ". Nothing left to do." };

    const line = situationText(situation);
    switch (situation.situation) {
      case "ROUND_SOON":
      case "ROUND_LATER":
        return { text: "Next: " + line + ". Prepare with the listing and past debriefs.", ...viewListing };
      case "OFFER_OPEN": return { text: "You have an offer to decide on.", ...updateStatus };
      case "NEEDS_DEBRIEF":
        return { text: line + ".", label: "Debrief", run: () => openDebrief(situation.round) };
      case "RESULT_OVERDUE":
      case "NO_REPLY":
        return { text: line + ". Chase them, or update the status.", ...updateStatus };
      case "PASSED_NO_NEXT": return { text: line + ".", label: "Add round", run: () => openRound(null) };
      case "FAILED_OPEN": return { text: line + ".", ...updateStatus };
      case "NOT_APPLIED": return { text: "You haven't applied yet.", ...updateStatus };
      default: return { text: line + ". Nothing needed from you." };
    }
  }

  function renderNextStep() {
    const step = nextStep();
    const old = document.getElementById("next-step-action");
    const button = old.cloneNode(false);
    document.getElementById("next-step-text").textContent = step.text;
    button.hidden = !step.label;
    button.textContent = step.label || "";
    if (step.run) button.addEventListener("click", step.run);
    old.replaceWith(button);
  }

  function score(label, value) {
    return value ? el("span", { class: "tag", text: label + " " + value + "/5" }) : null;
  }

  function roundCard(round) {
    const when = round.scheduledAt ? formatDateTime(round.scheduledAt) : "No date yet";
    const dueLabel = round.type === "TAKE_HOME" && round.scheduledAt ? "Due " + when : when;
    return el("article", { class: "round" + (round.outcome === "CANCELLED" ? " cancelled" : "") },
      el("div", { class: "round-head" },
        el("strong", { text: roundName(round) }),
        el("span", { class: "muted", text: dueLabel }),
        el("span", { class: "tag", text: RESULT_LABELS[round.outcome] }),
        score("Felt", round.feltScore),
        score("Difficulty", round.difficultyScore),
        el("span", { class: "spacer" }),
        el("button", { type: "button", text: round.feltScore ? "Edit debrief" : "Debrief", onclick: () => openDebrief(round) }),
        el("button", { type: "button", text: "Edit", onclick: () => openRound(round) })),
      round.questionsAsked ? el("div", { class: "round-text" },
        el("div", { class: "label", text: "Questions they asked" }),
        el("div", { class: "prewrap", text: round.questionsAsked })) : null,
      round.debrief ? el("div", { class: "round-text" },
        el("div", { class: "label", text: "How it went" }),
        el("div", { class: "prewrap", text: round.debrief })) : null);
  }

  function renderTimeline(rounds) {
    const items = [];
    if (job.appliedDate) {
      items.push(el("div", { class: "milestone" }, "Applied ", el("span", { class: "muted", text: formatDate(job.appliedDate) })));
    }
    rounds.forEach((r) => items.push(roundCard(r)));
    if (job.closedDate) {
      items.push(el("div", { class: "milestone" }, STATUS_LABELS[job.status] + " ", el("span", { class: "muted", text: formatDate(job.closedDate) })));
    }
    if (items.length === 0) items.push(el("p", { class: "empty", text: "No rounds yet. Add the first one when it's scheduled." }));
    const timeline = document.getElementById("timeline");
    timeline.className = "timeline job-" + job.colorIndex;
    timeline.replaceChildren(...items);
  }

  function renderListing() {
    const link = document.getElementById("job-link");
    link.hidden = !job.jobUrl;
    if (job.jobUrl) link.setAttribute("href", job.jobUrl);
    document.getElementById("job-listing").textContent = job.jobDescription || "No listing saved yet. Add it with Edit job.";
  }

  function showListing() {
    const section = document.getElementById("listing-section");
    section.scrollIntoView({ behavior: "smooth", block: "center" });
    section.classList.add("flash");
    setTimeout(() => section.classList.remove("flash"), 1500);
  }


  function renderDetails() {
    const rows = [
      ["Salary expectation", job.salaryExpectation], ["Salary offered", job.salaryOffer],
      ["Location", job.location], ["Work mode", WORK_MODES[job.workMode]],
      ["How it started", SOURCES[job.source]], ["Start details", job.sourceNote], ["Added", formatDate(job.createdAt)],
    ].filter(([, value]) => value);
    document.getElementById("job-details").replaceChildren(
      ...rows.flatMap(([term, value]) => [el("dt", { text: term }), el("dd", { text: value })]));
  }

  // ---------- rounds ----------

  function openRound(round) {
    editingRound = round;
    const form = document.getElementById("round-form");
    form.reset();
    document.getElementById("round-title").textContent = round ? "Edit round" : "Add round";
    document.getElementById("round-delete").hidden = !round;
    form.elements.type.value = round ? round.type : "SCREENING";
    form.elements.scheduledAt.value = round ? toLocalInput(round.scheduledAt) : "";
    form.elements.outcome.value = round ? round.outcome : "PENDING";
    updateRoundDateLabel();
    document.getElementById("round-dialog").showModal();
  }

  function updateRoundDateLabel() {
    const takeHome = document.getElementById("round-type").value === "TAKE_HOME";
    document.getElementById("round-date-label").textContent = takeHome ? "Due date" : "Date and time";
  }

  function roundBody(round) {
    return {
      type: round.type, scheduledAt: round.scheduledAt, outcome: round.outcome,
      feltScore: round.feltScore, difficultyScore: round.difficultyScore,
      questionsAsked: round.questionsAsked, debrief: round.debrief,
    };
  }

  async function saveRound(e) {
    e.preventDefault();
    const values = formValues(e.target);
    const scheduledAt = values.scheduledAt ? new Date(values.scheduledAt).toISOString() : null;
    try {
      if (editingRound) {
        await api.put("/api/rounds/" + editingRound.id, { ...roundBody(editingRound), ...values, scheduledAt });
      } else {
        await api.post("/api/jobs/" + jobId + "/rounds", { ...values, scheduledAt });
      }
      document.getElementById("round-dialog").close();
      toast("Round saved");
      load();
    } catch (err) {
      showError(err);
    }
  }

  async function deleteRound() {
    if (!window.confirm("Delete this round and its debrief? This can't be undone.")) return;
    try {
      await api.del("/api/rounds/" + editingRound.id);
      document.getElementById("round-dialog").close();
      toast("Round deleted");
      load();
    } catch (err) {
      showError(err);
    }
  }

  // ---------- debrief ----------

  function scale(containerId, name) {
    document.getElementById(containerId).replaceChildren(...[1, 2, 3, 4, 5].map((n) =>
      el("label", { class: "scale-option" }, el("input", { type: "radio", name, value: String(n) }), String(n))));
  }

  function openDebrief(round) {
    debriefingRound = round;
    const form = document.getElementById("debrief-form");
    form.reset();
    document.getElementById("debrief-error").hidden = true;
    document.getElementById("debrief-title").textContent = "Debrief: " + roundName(round);
    form.elements.feltScore.value = round.feltScore || "";
    form.elements.difficultyScore.value = round.difficultyScore || "";
    form.elements.questionsAsked.value = round.questionsAsked || "";
    form.elements.debrief.value = round.debrief || "";
    form.elements.outcome.value = round.outcome;
    document.getElementById("debrief-dialog").showModal();
  }

  async function saveDebrief(e) {
    e.preventDefault();
    const values = formValues(e.target);
    if (!values.feltScore || !values.difficultyScore) {
      document.getElementById("debrief-error").hidden = false;
      return;
    }
    try {
      await api.put("/api/rounds/" + debriefingRound.id, { ...roundBody(debriefingRound), ...values });
      document.getElementById("debrief-dialog").close();
      toast("Debrief saved");
      load();
    } catch (err) {
      showError(err);
    }
  }

  // ---------- status ----------

  function updateStatusDate() {
    const status = document.getElementById("status-select").value;
    const closed = CLOSED_STATUSES.includes(status);
    document.getElementById("status-date-field").hidden = !closed && status !== "APPLIED";
    document.getElementById("status-date-label").textContent = closed ? "Closed date" : "Applied date";
    document.getElementById("status-date").value = status === "APPLIED" && job.appliedDate ? job.appliedDate : dateKey(new Date());
  }

  function openStatus() {
    document.getElementById("status-select").value = job.status;
    updateStatusDate();
    document.getElementById("status-dialog").showModal();
  }

  async function saveStatus(e) {
    e.preventDefault();
    const values = formValues(e.target);
    const usesDate = !document.getElementById("status-date-field").hidden;
    try {
      await api.post("/api/jobs/" + jobId + "/status", { status: values.status, date: usesDate ? values.date : null });
      document.getElementById("status-dialog").close();
      toast("Status updated");
      load();
    } catch (err) {
      showError(err);
    }
  }

  // ---------- job ----------

  function openEdit() {
    const form = document.getElementById("edit-form");
    form.reset();
    ["company", "roleTitle", "location", "workMode", "source", "sourceNote", "appliedDate", "salaryExpectation", "salaryOffer", "jobUrl", "jobDescription"]
      .forEach((name) => { form.elements[name].value = job[name] == null ? "" : job[name]; });
    form.elements.appliedDate.disabled = job.status === "WISHLIST";
    document.getElementById("edit-dialog").showModal();
  }

  async function saveEdit(e) {
    e.preventDefault();
    const values = formValues(e.target);
    try {
      await api.put("/api/jobs/" + jobId, values);
      document.getElementById("edit-dialog").close();
      toast("Job saved");
      load();
    } catch (err) {
      showError(err);
    }
  }

  async function toggleArchive() {
    try {
      await api.post("/api/jobs/" + jobId + (job.archived ? "/unarchive" : "/archive"));
      toast(job.archived ? "Job unarchived" : "Job archived");
      load();
    } catch (err) {
      showError(err);
    }
  }

  async function deleteJob() {
    if (!window.confirm("Delete this job and all its rounds and debriefs? This can't be undone.")) return;
    try {
      await api.del("/api/jobs/" + jobId);
      window.location.href = "/";
    } catch (err) {
      showError(err);
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    fillSelect(document.getElementById("round-type"), ROUND_LABELS);
    fillSelect(document.getElementById("round-outcome"), RESULT_LABELS);
    fillSelect(document.getElementById("debrief-outcome"), RESULT_LABELS);
    fillSelect(document.getElementById("status-select"), STATUS_LABELS);
    scale("felt-scale", "feltScore");
    scale("difficulty-scale", "difficultyScore");
    document.querySelectorAll("dialog").forEach(closeOnCancel);

    document.getElementById("add-round").addEventListener("click", () => openRound(null));
    document.getElementById("round-type").addEventListener("change", updateRoundDateLabel);
    document.getElementById("round-form").addEventListener("submit", saveRound);
    document.getElementById("round-delete").addEventListener("click", deleteRound);
    document.getElementById("debrief-form").addEventListener("submit", saveDebrief);
    document.getElementById("status-select").addEventListener("change", updateStatusDate);
    document.getElementById("status-form").addEventListener("submit", saveStatus);
    document.getElementById("update-status").addEventListener("click", openStatus);
    document.getElementById("edit-job").addEventListener("click", openEdit);
    document.getElementById("edit-form").addEventListener("submit", saveEdit);
    document.getElementById("archive-job").addEventListener("click", toggleArchive);
    document.getElementById("delete-job").addEventListener("click", deleteJob);
    load();
  });
})();
