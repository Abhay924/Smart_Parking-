
export type DashboardStats = {
  registeredVehicles: number;
  currentlyParked: number;
  totalBills: number;
  paidBillsToday: number;
  revenueToday: number;
  totalCapacity: number | null;
};

export type VehicleItem = {
  id: number;
  plateNumber: string;
  vehicleType: string;
  active: boolean;
  createdAt: string | null;
};

export type SessionItem = {
  id: number;
  vehicleId: number;
  plateNumber: string;
  entryTime: string;
  exitTime: string | null;
  status: string;
  durationMinutes: number;
};

export type BillItem = {
  id: number;
  billNumber: string;
  sessionId: number;
  vehicleId: number;
  plateNumber: string;
  durationMinutes: number;
  totalAmount: number;
  currency: string;
  status: string;
  generatedAt: string;
  paidAt: string | null;
};

export type ApiData = {
  stats: DashboardStats;
  vehicles: VehicleItem[];
  sessions: SessionItem[];
  bills: BillItem[];
};

export async function loadDashboard(
  username: string,
  password: string
): Promise<ApiData> {
  const headers = {
    Authorization: `Basic ${btoa(`${username}:${password}`)}`,
    Accept: "application/json",
  };

  async function get<T>(path: string): Promise<T> {
    const response = await fetch(path, { headers });

    if (response.status === 401 || response.status === 403) {
      throw new Error(
        response.status === 401
          ? "Login failed. Check your credentials."
          : "You do not have permission to access this data."
      );
    }

    if (!response.ok) {
      throw new Error(`API request failed (${response.status}): ${path}`);
    }

    return response.json() as Promise<T>;
  }

  const [stats, vehicles, sessions, bills] = await Promise.all([
    get<DashboardStats>("/api/v1/dashboard/stats"),
    get<VehicleItem[]>("/api/v1/dashboard/vehicles"),
    get<SessionItem[]>("/api/v1/dashboard/sessions"),
    get<BillItem[]>("/api/v1/dashboard/bills"),
  ]);

  return { stats, vehicles, sessions, bills };
}
