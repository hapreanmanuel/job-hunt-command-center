/*
 * Shared helpers, loaded on every page before the page script.
 * User text only ever reaches the page through el() and textContent, never
 * through innerHTML.
 */
(function () {
  "use strict";

  const STATUS_LABELS = {
    WISHLIST: "Wishlist", APPLIED: "Applied", INTERVIEWING: "Interviewing", OFFER: "Offer",
    ACCEPTED: "Accepted", DECLINED: "Declined", REJECTED: "Rejected", WITHDRAWN: "Withdrawn", GHOSTED: "Ghosted",
  };
  const CLOSED_STATUSES = ["ACCEPTED", "DECLINED", "REJECTED", "WITHDRAWN", "GHOSTED"];
  const ROUND_LABELS = {
    SCREENING: "Screening", TECHNICAL: "Technical", TAKE_HOME: "Take-home", BEHAVIORAL: "Behavioral",
    HIRING_MANAGER: "Hiring manager", ONSITE: "Onsite", FINAL: "Final", OTHER: "Other",
  };
  const RESULT_LABELS = { PENDING: "Pending", PASSED: "Passed", FAILED: "Failed", CANCELLED: "Cancelled" };

  class ApiError extends Error {
    constructor(status, message) {
      super(message);
      this.status = status;
    }
  }

  let csrf = null;

  /** Writes need the session's CSRF token. It's fetched once and kept for the page's lifetime. */
  async function csrfHeader() {
    if (!csrf) {
      const res = await fetch("/api/csrf");
      if (res.status === 401) window.location.href = "/login";
      csrf = await res.json();
    }
    return { [csrf.headerName]: csrf.token };
  }

  async function send(method, path, body) {
    const options = { method, headers: {} };
    if (method !== "GET") Object.assign(options.headers, await csrfHeader());
    if (body !== undefined) {
      options.headers["Content-Type"] = "application/json";
      options.body = JSON.stringify(body);
    }
    try {
      return await fetch(path, options);
    } catch (e) {
      throw new ApiError(0, "Couldn't reach the server. Check that it's running.");
    }
  }

  async function request(method, path, body) {
    let res = await send(method, path, body);
    if (res.status === 403 && method !== "GET") {
      csrf = null;
      res = await send(method, path, body);
    }
    if (res.status === 401) {
      window.location.href = "/login";
      return new Promise(() => {});
    }
    if (res.status === 204) return null;
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch (e) { data = null; }
    if (!res.ok) {
      throw new ApiError(res.status, (data && data.message) || "Something went wrong. Try again.");
    }
    return data;
  }

  const api = {
    get: (path) => request("GET", path),
    post: (path, body) => request("POST", path, body === undefined ? {} : body),
    put: (path, body) => request("PUT", path, body),
    del: (path) => request("DELETE", path),
  };

  /** Builds an element. Strings become text nodes, so they're never parsed as HTML. */
  function el(tag, props, ...children) {
    const node = document.createElement(tag);
    Object.entries(props || {}).forEach(([key, value]) => {
      if (value === undefined || value === null) return;
      if (key === "class") node.className = value;
      else if (key === "text") node.textContent = value;
      else if (key.startsWith("on")) node.addEventListener(key.slice(2), value);
      else node.setAttribute(key, value);
    });
    children.flat().forEach((child) => {
      if (child === null || child === undefined) return;
      node.append(child);
    });
    return node;
  }

  function toast(message, isError, details) {
    const box = el("div", { class: "toast" + (isError ? " toast-error" : ""), text: message });
    (details || []).forEach((d) => box.append(el("div", { class: "toast-detail", text: d })));
    document.getElementById("toasts").append(box);
    setTimeout(() => box.remove(), isError ? 6000 : 3000);
  }

  function showError(err) {
    const details = [];
    toast(err.message || "Something went wrong. Try again.", true, details);
  }

  function pad(n) { return String(n).padStart(2, "0"); }

  /** The local calendar date, as YYYY-MM-DD. */
  function dateKey(d) {
    return d.getFullYear() + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate());
  }

  function toLocalInput(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    return dateKey(d) + "T" + pad(d.getHours()) + ":" + pad(d.getMinutes());
  }

  function formatDateTime(iso) {
    return new Date(iso).toLocaleString(undefined, { weekday: "short", day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" });
  }

  function formatTime(iso) {
    return new Date(iso).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
  }

  function formatDate(value) {
    const d = value.length === 10 ? new Date(value + "T00:00:00") : new Date(value);
    return d.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" });
  }

  function relativeDay(iso) {
    const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
    const days = Math.round((startOfDay(new Date(iso)) - startOfDay(new Date())) / 86400000);
    if (days === 0) return "today";
    if (days === 1) return "tomorrow";
    if (days === -1) return "yesterday";
    if (days < 0) return Math.abs(days) + " days ago";
    return new Date(iso).toLocaleDateString(undefined, { weekday: "short", day: "numeric", month: "short" });
  }

  function roundName(round) { return ROUND_LABELS[round.type] || round.type; }

  /** The one line that says where a job stands, used on Home and on the Job screen. */
  function situationText(s) {
    const round = s.round;
    const days = s.daysSinceActivity;
    switch (s.situation) {
      case "ROUND_SOON": return roundName(round) + " " + relativeDay(round.scheduledAt) + ", " + formatTime(round.scheduledAt);
      case "OFFER_OPEN": return "Offer to decide on";
      case "NEEDS_DEBRIEF": return "Debrief the " + roundName(round).toLowerCase() + " round from " + relativeDay(round.scheduledAt);
      case "RESULT_OVERDUE": return "No result for " + days + " days";
      case "PASSED_NO_NEXT": return "Passed " + roundName(round).toLowerCase() + ", next round not set";
      case "FAILED_OPEN": return "Failed " + roundName(round).toLowerCase() + ", job still open";
      case "NO_REPLY": return "No reply for " + days + " days";
      case "NOT_APPLIED": return "Not applied yet";
      case "ROUND_LATER": return roundName(round) + " " + relativeDay(round.scheduledAt);
      default: return days === 0 ? "Waiting on them" : "Waiting on them, " + days + (days === 1 ? " day" : " days");
    }
  }

  function closeOnCancel(dialog) {
    dialog.querySelectorAll("[data-close]").forEach((b) => b.addEventListener("click", () => dialog.close()));
  }

  function fillSelect(select, labels) {
    select.replaceChildren(...Object.entries(labels).map(([value, text]) => el("option", { value, text })));
  }

  /** Empty form fields become null, so optional values are really unset. */
  function formValues(form) {
    const values = {};
    new FormData(form).forEach((value, key) => { values[key] = value === "" ? null : value; });
    return values;
  }

  async function downloadData() {
    try {
      const data = await api.get("/api/user/export");
      const link = el("a", {
        href: URL.createObjectURL(new Blob([JSON.stringify(data, null, 2)], { type: "application/json" })),
        download: "job-hunt-" + dateKey(new Date()) + ".json",
      });
      link.click();
      URL.revokeObjectURL(link.href);
    } catch (err) {
      showError(err);
    }
  }

  async function deleteData() {
    const sure = window.confirm("This permanently deletes all your jobs, rounds and debriefs, and signs you out. "
      + "It can't be undone. Download your data first if you want a copy.");
    if (!sure) return;
    try {
      await api.del("/api/user");
      window.location.href = "/login";
    } catch (err) {
      showError(err);
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    const download = document.getElementById("download-data");
    if (download) download.addEventListener("click", downloadData);
    const deleteButton = document.getElementById("delete-data");
    if (deleteButton) deleteButton.addEventListener("click", deleteData);
    const signOut = document.getElementById("sign-out");
    if (signOut) {
      signOut.addEventListener("click", async () => {
        await send("POST", "/logout");
        window.location.href = "/login";
      });
    }
  });

  window.JHCC = {
    api, el, toast, showError, dateKey, toLocalInput, formatDateTime, formatTime, formatDate,
    roundName, situationText, closeOnCancel, fillSelect, formValues,
    STATUS_LABELS, CLOSED_STATUSES, ROUND_LABELS, RESULT_LABELS,
  };
})();
