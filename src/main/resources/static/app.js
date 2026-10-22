const api = "/api";
let activeStatus = "ALL";
let activeMatch = null;
let selectedRuns = 0;
let refreshTimer;

const $ = id => document.getElementById(id);
const safe = value => String(value ?? "").replace(/[&<>"']/g, char => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[char]));
const number = value => Number(value || 0).toLocaleString("en-IN");

async function request(path, options = {}) {
  const response = await fetch(api + path, {
    headers: {"Content-Type":"application/json", ...(options.headers || {})},
    ...options
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error || "Something went wrong.");
  return data;
}

async function loadDashboard() {
  try {
    const search = encodeURIComponent($("searchInput").value.trim());
    const status = activeStatus === "ALL" ? "" : "&status=" + activeStatus;
    const [matches, summary] = await Promise.all([
      request("/matches?search=" + search + status),
      request("/summary")
    ]);
    renderSummary(summary);
    renderMatches(matches);
    await loadPlayerStats();
    $("updatedAt").textContent = "Updated " + new Date().toLocaleTimeString([], {hour:"2-digit", minute:"2-digit", second:"2-digit"});
  } catch (error) {
    $("matchGrid").innerHTML = `<div class="empty-state">Could not connect to the server. Start Spring Boot and refresh.</div>`;
    $("updatedAt").textContent = "Offline";
  }
}

function renderSummary(data) {
  const cards = [
    ["Live matches", data.liveMatches, "Currently in play", "●"],
    ["Scheduled", data.scheduledMatches, "Coming up next", "◷"],
    ["Completed", data.completedMatches, "Full-time results", "✓"],
    ["Deliveries", data.totalDeliveries, "Balls recorded", "↗"]
  ];
  $("summaryCards").innerHTML = cards.map(([label, value, note, icon]) => `<article class="stat-card"><div class="stat-top"><span>${label}</span><span class="stat-icon">${icon}</span></div><div class="stat-number">${number(value)}</div><div class="stat-note">${note}</div></article>`).join("");
}

function renderMatches(matches) {
  if (!matches.length) {
    $("matchGrid").innerHTML = `<div class="empty-state">No matches found. Try another search or create a match.</div>`;
    return;
  }
  $("matchGrid").innerHTML = matches.map(match => `
    <article class="match-card">
      <div class="match-card-top"><span class="league">${safe(match.format)} · MATCH ${String(match.id).padStart(2,"0")}</span><span class="status-pill ${match.status}">${match.status}</span></div>
      <div class="teams-row">
        <div><div class="team-name">${safe(match.teamOne)}</div><div class="team-sub">Team 1</div><div class="team-score">${safe(match.teamOneScore)} <span>(${safe(match.teamOneOvers)} ov)</span></div></div>
        <div class="vs">VS</div>
        <div style="text-align:right"><div class="team-name">${safe(match.teamTwo)}</div><div class="team-sub">Team 2</div><div class="team-score">${safe(match.teamTwoScore)} <span>(${safe(match.teamTwoOvers)} ov)</span></div></div>
      </div>
      <div class="match-divider"></div>
      <div class="match-foot"><span>⌖ ${safe(match.venue)}</span><button class="view-btn" onclick="openScore(${match.id})">View scorecard ↗</button></div>
    </article>`).join("");
}

async function loadPlayerStats() {
  const selected = $("playerMatchFilter").value;
  const stats = await request("/players/stats" + (selected ? "?matchId=" + selected : ""));
  $("playerRows").innerHTML = stats.length ? stats.map(player => `<tr><td>${safe(player.player)}</td><td>${number(player.runs)}</td><td>${number(player.ballsFaced)}</td><td>${number(player.fours)}</td><td>${number(player.sixes)}</td><td>${number(player.wickets)}</td><td>${number(player.ballsBowled)}</td></tr>`).join("") : `<tr><td colspan="7">No player stats recorded yet.</td></tr>`;
  const matches = await request("/matches");
  const current = $("playerMatchFilter").value;
  $("playerMatchFilter").innerHTML = `<option value="">All matches</option>` + matches.map(m => `<option value="${m.id}">${safe(m.teamOne)} vs ${safe(m.teamTwo)}</option>`).join("");
  $("playerMatchFilter").value = current;
}

async function openScore(id) {
  try {
    activeMatch = await request("/matches/" + id);
    const score = await request("/matches/" + id + "/scorecard");
    renderScoreDialog(activeMatch, score);
    $("scoreDialog").showModal();
  } catch (error) { showToast(error.message); }
}

function renderScoreDialog(match, score) {
  const innings = score.innings;
  const recent = [...score.recentBalls].reverse();
  $("scoreDialogContent").innerHTML = `<div class="score-dialog">
    <div class="score-head"><div><h2>${safe(match.teamOne)} <span style="color:#a0aaa2">vs</span> ${safe(match.teamTwo)}</h2><div class="score-venue">${safe(match.venue)} · ${safe(match.format)} · ${safe(match.status)}</div></div><button class="close-btn" onclick="$('scoreDialog').close()">×</button></div>
    <div class="score-innings">${innings.map(i => `<div class="innings-box"><small>INNINGS ${i.innings} · ${safe(i.team)}</small><strong>${i.runs}/${i.wickets}</strong><span>${safe(i.overs)} overs · RR ${i.runRate}</span></div>`).join("")}</div>
    <div class="score-subhead">Recent deliveries</div>
    <div class="ball-list">${recent.length ? recent.map(ball => `<div class="ball-item"><span>${safe(ball.batter)} <span style="color:#9aa59e">vs ${safe(ball.bowler)}</span></span><span class="ball-result">${ball.wicket ? "WICKET" : ball.extraType !== "NONE" ? safe(ball.extraType) + " +" + ball.totalRuns : ball.runs + " run" + (ball.runs === 1 ? "" : "s")}</span></div>`).join("") : `<div class="ball-item">No deliveries recorded yet.</div>`}</div>
    ${match.status !== "COMPLETED" ? `<div class="score-entry"><h3>Record next ball</h3><div class="entry-row"><label>Batter<input id="batterName" placeholder="Batter name" value="A. Sharma"></label><label>Bowler<input id="bowlerName" placeholder="Bowler name" value="R. Patil"></label></div><label>Innings<select id="inningsSelect"><option value="1">First innings</option><option value="2">Second innings</option></select></label><label>Runs off bat</label><div class="run-buttons">${[0,1,2,3,4,5,6].map(n => `<button class="${selectedRuns === n ? "selected" : ""}" onclick="selectRuns(${n})">${n}</button>`).join("")}</div><div class="entry-row"><label>Extras<select id="extraType"><option value="NONE">None</option><option value="WIDE">Wide</option><option value="NO_BALL">No ball</option><option value="BYE">Bye</option><option value="LEG_BYE">Leg bye</option></select></label><label>Extra runs<input id="extraRuns" type="number" min="0" max="6" value="0"></label></div><label style="display:flex;align-items:center;gap:8px"><input id="wicketFlag" type="checkbox" style="width:auto;margin:0"> Wicket on this ball</label><label>Wicket type<input id="wicketType" placeholder="Caught, bowled, run out…"></label><div class="score-actions"><button class="secondary-btn" onclick="finishMatch(${match.id})">Mark completed</button><button class="primary-btn" onclick="saveBall(${match.id})">Save delivery</button></div></div>` : `<div class="score-actions"><button class="secondary-btn" onclick="$('scoreDialog').close()">Close scorecard</button></div>`}
  </div>`;
}

function selectRuns(value) {
  selectedRuns = value;
  const buttons = document.querySelectorAll(".run-buttons button");
  buttons.forEach((button, index) => button.classList.toggle("selected", index === value));
}

async function saveBall(id) {
  const batter = $("batterName").value.trim();
  const bowler = $("bowlerName").value.trim();
  if (!batter || !bowler) { showToast("Enter both batter and bowler names."); return; }
  try {
    await request(`/matches/${id}/balls`, {method:"POST", body:JSON.stringify({
      batter, bowler, runs:selectedRuns, extraType:$("extraType").value,
      extraRuns:Number($("extraRuns").value || 0), wicket:$("wicketFlag").checked,
      wicketType:$("wicketType").value, inningsNumber:Number($("inningsSelect").value)
    })});
    selectedRuns = 0;
    await loadDashboard();
    await openScore(id);
    showToast("Delivery saved.");
  } catch (error) { showToast(error.message); }
}

async function finishMatch(id) {
  try {
    await request(`/matches/${id}/status?status=COMPLETED`, {method:"PATCH"});
    $("scoreDialog").close();
    await loadDashboard();
    showToast("Match marked completed.");
  } catch (error) { showToast(error.message); }
}

function showToast(message) {
  const toast = $("toast");
  toast.textContent = message;
  toast.classList.add("show");
  clearTimeout(toast.hideTimer);
  toast.hideTimer = setTimeout(() => toast.classList.remove("show"), 2600);
}

$("filterTabs").addEventListener("click", event => {
  const button = event.target.closest("button[data-status]");
  if (!button) return;
  activeStatus = button.dataset.status;
  document.querySelectorAll("#filterTabs button").forEach(item => item.classList.toggle("active", item === button));
  loadDashboard();
});
$("searchInput").addEventListener("input", () => {
  clearTimeout(window.searchDelay);
  window.searchDelay = setTimeout(loadDashboard, 250);
});
$("playerMatchFilter").addEventListener("change", loadPlayerStats);
$("newMatchBtn").addEventListener("click", () => $("matchDialog").showModal());
$("closeMatchDialog").addEventListener("click", () => $("matchDialog").close());
$("matchForm").addEventListener("submit", async event => {
  event.preventDefault();
  const form = new FormData(event.target);
  const data = Object.fromEntries(form.entries());
  try {
    await request("/matches", {method:"POST", body:JSON.stringify(data)});
    $("matchDialog").close();
    event.target.reset();
    $("formError").textContent = "";
    await loadDashboard();
    showToast("Match created.");
  } catch (error) { $("formError").textContent = error.message; }
});
$("scoreDialog").addEventListener("click", event => {
  if (event.target === $("scoreDialog")) $("scoreDialog").close();
});
loadDashboard();
refreshTimer = setInterval(loadDashboard, 5000);
