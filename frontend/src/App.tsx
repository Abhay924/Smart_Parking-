import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  Activity,
  ArrowDownRight,
  ArrowUpRight,
  Bell,
  CarFront,
  CheckCircle2,
  ChevronDown,
  CircleAlert,
  CircleDollarSign,
  Clock3,
  CreditCard,
  Download,
  LayoutDashboard,
  Menu,
  Search,
  Settings,
  ShieldCheck,
  SquareParking,
  X,
  Zap,
  Camera,
  CircleHelp,
  RefreshCw,
  LogIn,
  LogOut,
} from "lucide-react";
import { ApiData, BillItem, SessionItem, VehicleItem, loadDashboard } from "./api";

type PageName =
  | "Overview"
  | "Vehicles"
  | "Parking Sessions"
  | "Billing & Payments"
  | "ANPR Detection"
  | "Gate Control"
  | "Activity Logs"
  | "Settings"
  | "Help & Support";

const navGroups = [
  {
    label: "WORKSPACE",
    items: [
      { name: "Overview" as PageName, icon: LayoutDashboard },
      { name: "Vehicles" as PageName, icon: CarFront },
      { name: "Parking Sessions" as PageName, icon: SquareParking },
      { name: "Billing & Payments" as PageName, icon: CreditCard },
    ],
  },
  {
    label: "MONITORING",
    items: [
      { name: "ANPR Detection" as PageName, icon: Camera },
      { name: "Gate Control" as PageName, icon: ShieldCheck },
      { name: "Activity Logs" as PageName, icon: Activity },
    ],
  },
];

const pageDescriptions: Record<PageName, string> = {
  Overview: "Live figures from your parking database.",
  Vehicles: "Registered vehicles retrieved from the backend.",
  "Parking Sessions": "Parking entries, durations and session status.",
  "Billing & Payments": "Bills and payment statuses from the database.",
  "ANPR Detection": "ANPR integration status.",
  "Gate Control": "Gate controller integration status.",
  "Activity Logs": "Recent database activity.",
  Settings: "Workspace configuration.",
  "Help & Support": "Smart Parking help and support.",
};

function formatMoney(value: number, currency = "INR") {
  try {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency,
      maximumFractionDigits: 2,
    }).format(value);
  } catch {
    return `₹${value.toFixed(2)}`;
  }
}

function formatDateTime(value?: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDuration(minutes?: number | null) {
  if (minutes == null || !Number.isFinite(minutes)) return "—";
  const safeMinutes = Math.max(0, Math.floor(minutes));
  const hours = Math.floor(safeMinutes / 60);
  const mins = safeMinutes % 60;
  return `${String(hours).padStart(2, "0")}h ${String(mins).padStart(2, "0")}m`;
}

function titleCase(value?: string | null) {
  if (!value) return "Unknown";
  return value.toLowerCase().replace(/(^|\s|_)\w/g, (letter) => letter.toUpperCase()).replaceAll("_", " ");
}

function downloadCsv(filename: string, rows: string[][]) {
  const csv = rows
    .map((row) => row.map((cell) => `"${String(cell ?? "").replaceAll('"', '""')}"`).join(","))
    .join("\r\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

function App() {
  const [activePage, setActivePage] = useState<PageName>("Overview");
  const [query, setQuery] = useState("");
  const [mobileMenu, setMobileMenu] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [credentials, setCredentials] = useState({ username: "abhay", password: "" });
  const [apiData, setApiData] = useState<ApiData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [loggedIn, setLoggedIn] = useState(false);
  const [entryPlate, setEntryPlate] = useState("");
  const [exitPlate, setExitPlate] = useState("");
  const [billSessionId, setBillSessionId] = useState("");
  const [paymentBillId, setPaymentBillId] = useState("");
  const [paymentProvider, setPaymentProvider] = useState("MANUAL");
  const [paymentIdToCheck, setPaymentIdToCheck] = useState("");
  const [gateId, setGateId] = useState("ENTRY-GATE-01");
  const [gateDirection, setGateDirection] = useState("ENTRY");
  const [gateSessionId, setGateSessionId] = useState("");
  const [gateBillId, setGateBillId] = useState("");
  const [gateStatusId, setGateStatusId] = useState("ENTRY-GATE-01");
  const [operationBusy, setOperationBusy] = useState(false);
  const [operationMessage, setOperationMessage] = useState("");
  const [operationError, setOperationError] = useState("");
  const [operationResult, setOperationResult] = useState<unknown>(null);

  useEffect(() => {
    const timer = window.setInterval(() => setCurrentTime(new Date()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  async function connectDashboard(event?: FormEvent) {
    event?.preventDefault();
    if (!credentials.username.trim() || !credentials.password) {
      setError("Enter your backend username and password.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const result = await loadDashboard(credentials.username.trim(), credentials.password);
      setApiData(result);
      setLoggedIn(true);
    } catch (err) {
      setApiData(null);
      setLoggedIn(false);
      setError(err instanceof Error ? err.message : "Could not connect to the backend.");
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    setApiData(null);
    setLoggedIn(false);
    setCredentials((previous) => ({ ...previous, password: "" }));
    setQuery("");
    setOperationMessage("");
    setOperationError("");
    setOperationResult(null);
  }

  async function apiRequest<T = unknown>(path: string, method = "GET", body?: unknown): Promise<T> {
    const response = await fetch(path, {
      method,
      headers: {
        Authorization: `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`,
        Accept: "application/json",
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
      },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    });
    const text = await response.text();
    let data: unknown = null;
    if (text) {
      try { data = JSON.parse(text); } catch { data = text; }
    }
    if (!response.ok) {
      const detail = typeof data === "object" && data !== null && "message" in data
        ? String((data as { message: unknown }).message)
        : typeof data === "string" && data
          ? data
          : `Request failed (${response.status})`;
      throw new Error(detail);
    }
    return data as T;
  }

  async function refreshDashboard() {
    const refreshed = await loadDashboard(credentials.username.trim(), credentials.password);
    setApiData(refreshed);
  }

  async function runOperation(label: string, operation: () => Promise<unknown>, refresh = true) {
    setOperationBusy(true);
    setOperationError("");
    setOperationMessage("");
    setOperationResult(null);
    try {
      const result = await operation();
      setOperationResult(result);
      setOperationMessage(`${label} request completed.`);
      if (refresh) {
        try { await refreshDashboard(); } catch {
          setOperationMessage(`${label} request completed, but dashboard refresh failed. Refresh the dashboard manually.`);
        }
      }
    } catch (err) {
      setOperationError(err instanceof Error ? err.message : "Operation failed.");
    } finally {
      setOperationBusy(false);
    }
  }

  const vehicles = apiData?.vehicles ?? [];
  const sessions = apiData?.sessions ?? [];
  const bills = apiData?.bills ?? [];

  const filteredVehicles = useMemo(
    () =>
      vehicles.filter((vehicle) => {
        const matchingSession = sessions.find((session) => session.plateNumber === vehicle.plateNumber);
        const searchText = `${vehicle.plateNumber} ${vehicle.vehicleType} ${vehicle.active ? "active" : "inactive"} ${matchingSession?.status ?? ""}`;
        return searchText.toLowerCase().includes(query.toLowerCase());
      }),
    [vehicles, sessions, query],
  );

  const filteredSessions = useMemo(
    () =>
      sessions.filter((session) =>
        `${session.plateNumber} ${session.status} ${session.id}`.toLowerCase().includes(query.toLowerCase()),
      ),
    [sessions, query],
  );

  const filteredBills = useMemo(
    () =>
      bills.filter((bill) =>
        `${bill.billNumber} ${bill.plateNumber} ${bill.status}`.toLowerCase().includes(query.toLowerCase()),
      ),
    [bills, query],
  );

  function exportCurrentData() {
    if (activePage === "Parking Sessions") {
      downloadCsv("parking-sessions.csv", [
        ["ID", "Vehicle", "Entry time", "Exit time", "Duration minutes", "Status"],
        ...filteredSessions.map((session) => [
          String(session.id),
          session.plateNumber,
          session.entryTime,
          session.exitTime ?? "",
          String(session.durationMinutes),
          session.status,
        ]),
      ]);
      return;
    }
    if (activePage === "Billing & Payments") {
      downloadCsv("parking-bills.csv", [
        ["Bill number", "Vehicle", "Duration minutes", "Amount", "Currency", "Status", "Generated at", "Paid at"],
        ...filteredBills.map((bill) => [
          bill.billNumber,
          bill.plateNumber,
          String(bill.durationMinutes),
          String(bill.totalAmount),
          bill.currency,
          bill.status,
          bill.generatedAt,
          bill.paidAt ?? "",
        ]),
      ]);
      return;
    }
    downloadCsv("registered-vehicles.csv", [
      ["Plate number", "Vehicle type", "Active", "Created at"],
      ...filteredVehicles.map((vehicle) => [
        vehicle.plateNumber,
        vehicle.vehicleType,
        vehicle.active ? "Yes" : "No",
        vehicle.createdAt ?? "",
      ]),
    ]);
  }

  const stats = apiData?.stats;
  const capacity = stats?.totalCapacity;
  const parked = stats?.currentlyParked ?? 0;
  const available = capacity == null ? null : Math.max(0, capacity - parked);
  const occupancy = capacity && capacity > 0 ? Math.min(100, (parked / capacity) * 100) : null;

  if (!loggedIn || !apiData) {
    return (
      <div className="app-shell">
        <main className="main-area" style={{ minHeight: "100vh", display: "grid", placeItems: "center", padding: 24 }}>
          <section className="content-card simple-page" style={{ width: "min(460px, 100%)", padding: 32 }}>
            <div className="brand" style={{ justifyContent: "center", marginBottom: 28 }}>
              <div className="brand-icon"><SquareParking size={23} /></div>
              <div className="brand-copy"><strong>parkpilot<span>.</span></strong><small>PARKING INTELLIGENCE</small></div>
            </div>
            <div className="simple-icon"><LogIn size={25} /></div>
            <h2>Connect to your parking system</h2>
            <p>Sign in with the backend admin account to load live database records.</p>
            <form onSubmit={connectDashboard} style={{ display: "grid", gap: 14, textAlign: "left", marginTop: 22 }}>
              <label>
                <span style={{ display: "block", marginBottom: 6 }}>Username</span>
                <input
                  className="login-input"
                  autoComplete="username"
                  value={credentials.username}
                  onChange={(event) => setCredentials({ ...credentials, username: event.target.value })}
                  placeholder="Backend username"
                />
              </label>
              <label>
                <span style={{ display: "block", marginBottom: 6 }}>Password</span>
                <input
                  className="login-input"
                  type="password"
                  autoComplete="current-password"
                  value={credentials.password}
                  onChange={(event) => setCredentials({ ...credentials, password: event.target.value })}
                  placeholder="Backend password"
                />
              </label>
              {error && <p role="alert" style={{ color: "#ff8b8b", margin: 0 }}>{error}</p>}
              <button className="primary-button" type="submit" disabled={loading}>
                {loading ? <RefreshCw size={16} className="spin-icon" /> : <LogIn size={16} />}
                {loading ? "Connecting..." : "Connect dashboard"}
              </button>
            </form>
            <p style={{ marginTop: 18, fontSize: 12 }}>Credentials stay in this page's memory and are not saved to local storage.</p>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="app-shell">
      {mobileMenu && <button className="mobile-overlay" aria-label="Close menu" onClick={() => setMobileMenu(false)} />}
      <aside className={`sidebar ${mobileMenu ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-icon"><SquareParking size={23} /></div>
          <div className="brand-copy"><strong>parkpilot<span>.</span></strong><small>PARKING INTELLIGENCE</small></div>
          <button className="icon-button mobile-close" onClick={() => setMobileMenu(false)} aria-label="Close navigation"><X size={19} /></button>
        </div>
        <div className="facility-selector">
          <div className="facility-symbol"><Zap size={17} /></div>
          <div className="facility-copy"><small>ACTIVE FACILITY</small><strong>Central Parking</strong></div>
          <ChevronDown size={15} className="muted-icon" />
        </div>
        <nav className="navigation">
          {navGroups.map((group) => (
            <div className="nav-group" key={group.label}>
              <div className="nav-heading">{group.label}</div>
              {group.items.map((item) => {
                const Icon = item.icon;
                return (
                  <button key={item.name} className={`nav-item ${activePage === item.name ? "active" : ""}`} onClick={() => { setActivePage(item.name); setMobileMenu(false); setQuery(""); }}>
                    <Icon size={18} strokeWidth={1.8} /><span>{item.name}</span>
                    {item.name === "ANPR Detection" && <span className="nav-dot" />}
                  </button>
                );
              })}
            </div>
          ))}
        </nav>
        <div className="sidebar-spacer" />
        <div className="system-card">
          <div className="system-title"><span className="online-dot" />System overview<span className="system-live">CONNECTED</span></div>
          <div className="system-row"><span>Backend API</span><span className="system-unknown">Connected</span></div>
          <div className="system-row"><span>Database records</span><span className="system-unknown">Loaded</span></div>
          <div className="system-progress"><span style={{ width: "100%" }} /></div>
          <small>Live API response received.</small>
        </div>
        <div className="sidebar-bottom">
          <button className="nav-item" onClick={() => setActivePage("Settings")}><Settings size={18} /><span>Settings</span></button>
          <button className="nav-item" onClick={() => setActivePage("Help & Support")}><CircleHelp size={18} /><span>Help &amp; Support</span></button>
          <div className="profile">
            <div className="avatar">AK</div>
            <div className="profile-copy"><strong>{credentials.username}</strong><small>Administrator</small></div>
            <button className="icon-button" aria-label="Log out" onClick={logout}><LogOut size={16} /></button>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb">
            <button className="icon-button menu-toggle" onClick={() => setMobileMenu(true)} aria-label="Open navigation"><Menu size={21} /></button>
            <span>Workspace</span><span className="breadcrumb-slash">/</span><strong>{activePage}</strong>
          </div>
          <div className="topbar-actions">
            <div className="clock"><Clock3 size={15} /><span>{currentTime.toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}</span></div>
            <div className="topbar-divider" />
            <button className="icon-button" aria-label="Refresh dashboard" onClick={() => void connectDashboard()} disabled={loading}><RefreshCw size={17} /></button>
            <button className="icon-button notification-button" aria-label="Notifications" onClick={() => setShowNotifications(!showNotifications)}><Bell size={19} /></button>
            <div className="top-avatar">AK</div>
          </div>
          {showNotifications && <div className="notification-popover"><strong>System status</strong><p><CheckCircle2 size={16} /> Dashboard data loaded from the backend API.</p><button onClick={() => setShowNotifications(false)}>Dismiss</button></div>}
        </header>

        <div className="page-content">
          <div className="page-heading">
            <div>
              <div className="eyebrow"><span className="eyebrow-line" /> PARKING OPERATIONS <span className="demo-pill">LIVE API</span></div>
              <h1>{activePage === "Overview" ? "Good morning, Admin" : activePage}<span className="heading-period">.</span></h1>
              <p>{pageDescriptions[activePage]}</p>
            </div>
            <div className="heading-actions">
              <div className="date-chip"><Clock3 size={16} /><span>{currentTime.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })}</span></div>
              <button className="primary-button" onClick={exportCurrentData}><Download size={16} /> Export report</button>
            </div>
          </div>

          {error && <div className="content-card" role="alert" style={{ padding: 14, color: "#ff8b8b" }}>{error}</div>}

          {activePage === "Settings" || activePage === "Help & Support" ? (
            <section className="content-card simple-page">
              <div className="simple-icon"><Settings size={25} /></div>
              <h2>{activePage}</h2>
              <p>Dashboard API connection is active. Account and facility configuration will be added in a later stage.</p>
              <button className="secondary-button" onClick={() => setActivePage("Overview")}>Back to overview</button>
            </section>
          ) : activePage === "ANPR Detection" || activePage === "Activity Logs" ? (
            <section className="content-card simple-page">
              <div className="simple-icon">{activePage === "ANPR Detection" ? <Camera size={25} /> : <Activity size={25} />}</div>
              <h2>{activePage}</h2>
              <p>{activePage === "ANPR Detection" ? "The ANPR endpoint is available on the backend. Live camera detection events are not yet loaded into this dashboard." : "A dedicated activity-log endpoint is not currently part of the dashboard API response."}</p>
              <span className="status-badge status-unknown"><span />Integration pending</span>
            </section>
          ) : (
            <>
              {activePage === "Gate Control" && (
                <section className="content-card" style={{ padding: 20, marginBottom: 18 }}>
                  <div className="card-heading">
                    <div><h2>Gate status and command</h2><p>Backend simulator/API only. Physical barrier connectivity is not confirmed.</p></div>
                    <span className="small-label">GATE API</span>
                  </div>
                  <form onSubmit={(event) => { event.preventDefault(); void runOperation("Gate status", () => apiRequest(`/api/v1/gates/${encodeURIComponent(gateStatusId)}/status`, "GET"), false); }} style={{ display: "flex", flexWrap: "wrap", gap: 12, alignItems: "end", marginBottom: 20 }}>
                    <label style={{ flex: "1 1 220px" }}>Gate ID<input className="login-input" value={gateStatusId} onChange={(event) => setGateStatusId(event.target.value)} required /></label>
                    <button className="secondary-button" type="submit" disabled={operationBusy}><Search size={15} /> Check gate status</button>
                  </form>
                  <div className="simple-page" style={{ marginTop: 12 }}>
                    <h2>Gate opening is disabled</h2>
                    <p>Before enabling gate commands, we need to inspect GateService and verify that exit opening is blocked unless the backend confirms the bill is paid. Frontend-only checks are not sufficient.</p>
                  </div>
                  <p style={{ fontSize: 12, color: "var(--text-muted, #9ca3af)", marginTop: 12 }}>Physical barrier connectivity is unverified. No gate-open command is sent from this page.</p>
                  {operationBusy && <p role="status">Processing request…</p>}
                  {operationMessage && <p role="status" style={{ color: "#70d6a5" }}>{operationMessage}</p>}
                  {operationError && <p role="alert" style={{ color: "#ff8b8b" }}>{operationError}</p>}
                  {operationResult !== null && <pre style={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere", padding: 12, borderRadius: 8, background: "rgba(0,0,0,.2)", fontSize: 12 }}>{JSON.stringify(operationResult, null, 2)}</pre>}
                </section>
              )}
              {activePage === "Overview" && (
                <section className="content-card" style={{ padding: 20, marginBottom: 18 }}>
                  <div className="card-heading">
                    <div><h2>Parking operations</h2><p>Run entry, exit, billing and payment requests against the backend.</p></div>
                    <span className="small-label">API ACTIONS</span>
                  </div>
                  <div className="middle-grid" style={{ alignItems: "start" }}>
                    <form onSubmit={(event) => { event.preventDefault(); void runOperation("Vehicle entry", () => apiRequest("/api/v1/parking/entry", "POST", { plateNumber: entryPlate.trim().toUpperCase() })); }} style={{ display: "grid", gap: 10, padding: 12 }}>
                      <h3>Vehicle entry</h3>
                      <label>Number plate<input className="login-input" value={entryPlate} onChange={(event) => setEntryPlate(event.target.value)} placeholder="e.g. UP78AB1234" maxLength={20} required /></label>
                      <button className="primary-button" type="submit" disabled={operationBusy}><CarFront size={15} /> Register entry</button>
                    </form>
                    <form onSubmit={(event) => { event.preventDefault(); void runOperation("Vehicle exit", () => apiRequest("/api/v1/parking/exit", "POST", { plateNumber: exitPlate.trim().toUpperCase() })); }} style={{ display: "grid", gap: 10, padding: 12 }}>
                      <h3>Vehicle exit</h3>
                      <label>Number plate<input className="login-input" value={exitPlate} onChange={(event) => setExitPlate(event.target.value)} placeholder="e.g. UP78AB1234" maxLength={20} required /></label>
                      <button className="secondary-button" type="submit" disabled={operationBusy}><LogOut size={15} /> Register exit</button>
                    </form>
                    <form onSubmit={(event) => { event.preventDefault(); void runOperation("Bill generation", () => apiRequest(`/api/v1/billing/session/${encodeURIComponent(billSessionId)}`, "POST")); }} style={{ display: "grid", gap: 10, padding: 12 }}>
                      <h3>Generate bill</h3>
                      <label>Parking session ID<input className="login-input" type="number" min="1" value={billSessionId} onChange={(event) => setBillSessionId(event.target.value)} placeholder="Session ID" required /></label>
                      <button className="primary-button" type="submit" disabled={operationBusy}><CircleDollarSign size={15} /> Generate bill</button>
                    </form>
                    <form onSubmit={(event) => { event.preventDefault(); void runOperation("Create payment", () => apiRequest("/api/v1/payments", "POST", { billId: Number(paymentBillId), provider: paymentProvider })); }} style={{ display: "grid", gap: 10, padding: 12 }}>
                      <h3>Create payment request</h3>
                      <label>Bill ID<input className="login-input" type="number" min="1" value={paymentBillId} onChange={(event) => setPaymentBillId(event.target.value)} placeholder="Bill ID" required /></label>
                      <label>Provider<input className="login-input" value={paymentProvider} onChange={(event) => setPaymentProvider(event.target.value)} placeholder="Configured payment provider" required /></label>
                      <button className="secondary-button" type="submit" disabled={operationBusy}><CreditCard size={15} /> Create payment</button>
                    </form>
                    <form onSubmit={(event) => { event.preventDefault(); void runOperation("Payment status lookup", () => apiRequest(`/api/v1/payments/${encodeURIComponent(paymentIdToCheck)}`, "GET"), false); }} style={{ display: "grid", gap: 10, padding: 12 }}>
                      <h3>Check payment status</h3>
                      <label>Payment ID<input className="login-input" type="number" min="1" value={paymentIdToCheck} onChange={(event) => setPaymentIdToCheck(event.target.value)} placeholder="Payment ID" required /></label>
                      <button className="secondary-button" type="submit" disabled={operationBusy}><Search size={15} /> Check status</button>
                    </form>
                  </div>
                  {operationBusy && <p role="status">Processing request…</p>}
                  {operationMessage && <p role="status" style={{ color: "#70d6a5" }}>{operationMessage}</p>}
                  {operationError && <p role="alert" style={{ color: "#ff8b8b" }}>{operationError}</p>}
                  {operationResult !== null && <pre style={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere", padding: 12, borderRadius: 8, background: "rgba(0,0,0,.2)", fontSize: 12 }}>{JSON.stringify(operationResult, null, 2)}</pre>}
                  <p style={{ fontSize: 12, color: "var(--text-muted, #9ca3af)" }}>A created payment request is not proof of successful payment. This panel does not mark payments as paid or open a physical gate.</p>
                </section>
              )}
              <section className="stats-grid">
                <StatCard title="Registered vehicles" value={String(stats?.registeredVehicles ?? 0)} detail="Vehicles in database" icon={<CarFront size={19} />} tone="violet" trend="Live database" />
                <StatCard title="Currently parked" value={String(stats?.currentlyParked ?? 0)} detail="Active parking sessions" icon={<SquareParking size={19} />} tone="blue" trend="Live database" />
                <StatCard title="Total bills" value={String(stats?.totalBills ?? 0)} detail={`${stats?.paidBillsToday ?? 0} paid today`} icon={<CreditCard size={19} />} tone="green" trend="Billing records" />
                <StatCard title="Today's revenue" value={formatMoney(stats?.revenueToday ?? 0)} detail="Paid bills today" icon={<CircleDollarSign size={19} />} tone="orange" trend="Live database" />
              </section>

              {activePage === "Overview" && (
                <section className="middle-grid">
                  <div className="content-card occupancy-card">
                    <div className="card-heading"><div><h2>Parking occupancy</h2><p>Based on configured facility capacity</p></div><span className="small-label">LIVE API</span></div>
                    {capacity == null || capacity <= 0 ? (
                      <div className="simple-page"><h2>Capacity not configured</h2><p>Set a facility capacity before occupancy and available-space figures can be calculated.</p></div>
                    ) : (
                      <div className="occupancy-body">
                        <div className="donut-wrap">
                          <svg viewBox="0 0 180 180" className="donut-chart" role="img" aria-label={`Occupancy ${occupancy?.toFixed(1)} percent`}>
                            <circle cx="90" cy="90" r="69" className="donut-track" />
                            <circle cx="90" cy="90" r="69" className="donut-value" style={{ strokeDasharray: `${(occupancy ?? 0) * 4.335} 433.5` }} />
                          </svg>
                          <div className="donut-center"><strong>{occupancy?.toFixed(1)}<span>%</span></strong><small>OCCUPIED</small></div>
                        </div>
                        <div className="occupancy-legend">
                          <div className="legend-row"><span className="legend-dot violet" /><div><strong>{parked}</strong><small>Occupied spaces</small></div><span className="legend-percent">{occupancy?.toFixed(1)}%</span></div>
                          <div className="legend-row"><span className="legend-dot pale" /><div><strong>{available}</strong><small>Available spaces</small></div><span className="legend-percent">{(100 - (occupancy ?? 0)).toFixed(1)}%</span></div>
                          <div className="legend-row"><span className="legend-dot blue" /><div><strong>{capacity}</strong><small>Total capacity</small></div><span className="legend-percent">100%</span></div>
                        </div>
                      </div>
                    )}
                    <div className="occupancy-footer"><span className="status-indicator" /><span>Source: backend database</span></div>
                  </div>
                  <div className="content-card activity-card">
                    <div className="card-heading"><div><h2>Entry &amp; exit activity</h2><p>Session records returned by the API</p></div><span className="small-label">LIVE API</span></div>
                    <div className="simple-page"><h2>{sessions.length} sessions loaded</h2><p>Hourly activity chart will be added when the backend provides time-bucketed entry and exit counts.</p></div>
                  </div>
                </section>
              )}

              <section className="content-card vehicles-card">
                <div className="card-heading vehicle-heading">
                  <div>
                    <h2>{activePage === "Vehicles" ? "Registered vehicles" : activePage === "Parking Sessions" ? "Parking sessions" : activePage === "Billing & Payments" ? "Bills & payments" : "Recent database records"}</h2>
                    <p>Records retrieved from the backend API</p>
                  </div>
                  <div className="table-actions">
                    <div className="search-box"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder={activePage === "Billing & Payments" ? "Search bill or plate..." : "Search plate or status..."} aria-label="Search records" />{query && <button onClick={() => setQuery("")} aria-label="Clear search"><X size={14} /></button>}</div>
                    <button className="secondary-button" onClick={exportCurrentData}><Download size={15} /><span>Export</span></button>
                  </div>
                </div>
                <div className="table-wrap">
                  {activePage === "Parking Sessions" ? (
                    <table>
                      <thead><tr><th>ID</th><th>VEHICLE NUMBER</th><th>ENTRY TIME</th><th>EXIT TIME</th><th>DURATION</th><th>STATUS</th></tr></thead>
                      <tbody>
                        {filteredSessions.map((session: SessionItem) => <tr key={session.id}><td>#{session.id}</td><td><div className="plate-cell"><div className="car-mini"><CarFront size={17} /></div><strong>{session.plateNumber}</strong></div></td><td className="time-cell">{formatDateTime(session.entryTime)}</td><td className="time-cell">{formatDateTime(session.exitTime)}</td><td><span className="duration-cell"><Clock3 size={14} />{formatDuration(session.durationMinutes)}</span></td><td><span className={`status-badge ${session.status === "ACTIVE" ? "status-parked" : "status-exited"}`}><span />{titleCase(session.status)}</span></td></tr>)}
                        {filteredSessions.length === 0 && <tr><td colSpan={6} className="empty-state">No session records found.</td></tr>}
                      </tbody>
                    </table>
                  ) : activePage === "Billing & Payments" ? (
                    <table>
                      <thead><tr><th>BILL NUMBER</th><th>VEHICLE</th><th>DURATION</th><th>AMOUNT</th><th>GENERATED</th><th>STATUS</th></tr></thead>
                      <tbody>
                        {filteredBills.map((bill: BillItem) => <tr key={bill.id}><td><strong>{bill.billNumber}</strong></td><td><div className="plate-cell"><div className="car-mini"><CarFront size={17} /></div><strong>{bill.plateNumber}</strong></div></td><td><span className="duration-cell"><Clock3 size={14} />{formatDuration(bill.durationMinutes)}</span></td><td>{formatMoney(bill.totalAmount, bill.currency || "INR")}</td><td className="time-cell">{formatDateTime(bill.generatedAt)}</td><td><span className={`status-badge ${bill.status === "PAID" ? "status-parked" : "status-exited"}`}><span />{titleCase(bill.status)}</span></td></tr>)}
                        {filteredBills.length === 0 && <tr><td colSpan={6} className="empty-state">No bills found.</td></tr>}
                      </tbody>
                    </table>
                  ) : (
                    <table>
                      <thead><tr><th>VEHICLE NUMBER</th><th>TYPE</th><th>REGISTERED</th><th>ACTIVE</th><th>LAST SESSION</th><th>ACTION</th></tr></thead>
                      <tbody>
                        {filteredVehicles.map((vehicle: VehicleItem) => {
                          const lastSession = sessions.filter((session) => session.plateNumber === vehicle.plateNumber).sort((a, b) => new Date(b.entryTime).getTime() - new Date(a.entryTime).getTime())[0];
                          return <tr key={vehicle.id}><td><div className="plate-cell"><div className="car-mini"><CarFront size={17} /></div><strong>{vehicle.plateNumber}</strong></div></td><td><span className="type-cell">{titleCase(vehicle.vehicleType)}</span></td><td className="time-cell">{formatDateTime(vehicle.createdAt)}</td><td><span className={`status-badge ${vehicle.active ? "status-parked" : "status-exited"}`}><span />{vehicle.active ? "Active" : "Inactive"}</span></td><td>{lastSession ? <span className={`status-badge ${lastSession.status === "ACTIVE" ? "status-parked" : "status-exited"}`}><span />{titleCase(lastSession.status)}</span> : "No session"}</td><td><button className="row-action" onClick={() => { setQuery(vehicle.plateNumber); setActivePage("Vehicles"); }}>Filter <ArrowUpRight size={14} /></button></td></tr>;
                        })}
                        {filteredVehicles.length === 0 && <tr><td colSpan={6} className="empty-state">No vehicle records found.</td></tr>}
                      </tbody>
                    </table>
                  )}
                </div>
                <div className="table-footer"><span>Showing <strong>{activePage === "Parking Sessions" ? filteredSessions.length : activePage === "Billing & Payments" ? filteredBills.length : filteredVehicles.length}</strong> live records</span><button onClick={() => { setQuery(""); setActivePage(activePage === "Overview" ? "Vehicles" : activePage); }}>Clear search <ArrowUpRight size={15} /></button></div>
              </section>

              {activePage === "Overview" && (
                <section className="bottom-grid">
                  <div className="content-card gate-card">
                    <div className="card-heading"><div><h2>Gate monitoring</h2><p>Hardware status has not been verified</p></div><span className="monitor-icon"><ShieldCheck size={19} /></span></div>
                    <div className="gate-row"><div className="gate-symbol"><ArrowDownRight size={19} /></div><div className="gate-copy"><strong>Entry Gate 01</strong><small>Controller integration pending</small></div><span className="status-badge status-unknown"><span />Unverified</span></div>
                    <div className="gate-row"><div className="gate-symbol"><ArrowUpRight size={19} /></div><div className="gate-copy"><strong>Exit Gate 01</strong><small>Controller integration pending</small></div><span className="status-badge status-unknown"><span />Unverified</span></div>
                    <button className="text-link" onClick={() => setActivePage("Gate Control")}>Open gate monitoring <ArrowUpRight size={15} /></button>
                  </div>
                  <div className="content-card anpr-card">
                    <div className="card-heading"><div><h2>ANPR recognition</h2><p>Camera events are not connected yet</p></div><span className="monitor-icon"><Camera size={19} /></span></div>
                    <div className="anpr-event"><div className="anpr-symbol"><CarFront size={18} /></div><div className="anpr-copy"><strong>ANPR API</strong><small>Backend endpoint available</small></div><span className="status-badge status-unknown"><span />Pending</span></div>
                    <button className="text-link" onClick={() => setActivePage("ANPR Detection")}>View ANPR status <ArrowUpRight size={15} /></button>
                  </div>
                </section>
              )}
            </>
          )}
          <footer className="footer"><span>© 2026 ParkPilot · Smart Parking Management</span><span><span className="footer-dot" /> Live backend data <span className="footer-separator">·</span> ANPR/gate integration pending</span></footer>
        </div>
      </main>
    </div>
  );
}

function StatCard({ title, value, detail, icon, tone, trend }: { title: string; value: string; detail: string; icon: React.ReactNode; tone: string; trend: string }) {
  return (
    <div className="stat-card">
      <div className="stat-top"><span>{title}</span><span className={`stat-icon ${tone}`}>{icon}</span></div>
      <div className="stat-value">{value}</div>
      <div className="stat-detail">{detail}</div>
      <div className="stat-bottom"><span className="trend">{trend}</span><span className="stat-bottom-note">LIVE</span></div>
    </div>
  );
}

export default App;
