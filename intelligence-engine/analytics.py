import requests

LEDGER_BASE_URL = "http://localhost:8085/api/ledger"

def fetch_accounts():
    try:
        response = requests.get(f"{LEDGER_BASE_URL}/accounts", timeout=5)
        return response.json() if response.status_code == 200 else []
    except Exception as e:
        print(f"Error fetching accounts from Spring Boot: {e}")
        return []

def fetch_orders():
    try:
        response = requests.get(f"{LEDGER_BASE_URL}/orders", timeout=5)
        return response.json() if response.status_code == 200 else []
    except Exception as e:
        print(f"Error fetching orders from Spring Boot: {e}")
        return []

def fetch_inventory():
    try:
        response = requests.get(f"{LEDGER_BASE_URL}/inventory", timeout=5)
        return response.json() if response.status_code == 200 else []
    except Exception as e:
        print(f"Error fetching inventory from Spring Boot: {e}")
        return []

def calculate_blast_radius(affected_sku, delay_days):
    print(f"Calculating blast radius for affected SKU: {affected_sku}...")
    
    # 1. Fetch live data from Spring Boot REST gateway
    orders = fetch_orders()
    accounts = fetch_accounts()
    
    # Map accounts for quick lookup
    accounts_map = {acc["email"]: acc for acc in accounts}
    
    revenue_at_risk = 0.0
    affected_vip_customers = []
    affected_orders_count = 0

    # 2. Compute metrics
    for order in orders:
        if order["sku"] == affected_sku and order["orderStatus"] == "PLACED":
            affected_orders_count += 1
            revenue_at_risk += float(order["totalAmount"])
            
            # Lookup customer loyalty metrics
            email = order["account"]["email"]
            if email in accounts_map:
                customer = accounts_map[email]
                if customer["loyaltyTier"] == "VIP" and customer["churnRiskScore"] >= 0.70:
                    affected_vip_customers.append({
                        "name": customer["name"],
                        "email": customer["email"],
                        "churnRisk": customer["churnRiskScore"],
                        "ltv": float(customer["lifetimeValue"]),
                        "orderId": order["orderId"]
                    })
                    
    print(f"Blast Radius computed: Affected Orders={affected_orders_count}, Revenue At Risk=${revenue_at_risk:.2f}, High-Risk VIPs={len(affected_vip_customers)}")
    
    return {
        "affectedSku": affected_sku,
        "delayDays": delay_days,
        "affectedOrdersCount": affected_orders_count,
        "revenueAtRisk": revenue_at_risk,
        "highRiskVips": affected_vip_customers
    }
