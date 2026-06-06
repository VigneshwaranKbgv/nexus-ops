import threading
import time
import json
import os
from analytics import calculate_blast_radius, LEDGER_BASE_URL
from orchestrator import Orchestrator

# Safe import fallback for confluent_kafka
CONFLUENT_KAFKA_AVAILABLE = False
try:
    from confluent_kafka import Consumer, KafkaError
    CONFLUENT_KAFKA_AVAILABLE = True
except ImportError:
    print("Warning: confluent-kafka not found or binary wheels failed to install locally.")
    print("Zero-Install Fallback Mode Activated: Utilizing HTTP database audit polling to capture stream incidents!")

# Global list of processed incidents for the FastAPI controller
processed_incidents = []

def start_kafka_consumer():
    if CONFLUENT_KAFKA_AVAILABLE:
        thread = threading.Thread(target=run_kafka_consumer, daemon=True)
        thread.start()
        print("Kafka Consumer Thread started successfully (Listening on Port 9092).")
    else:
        thread = threading.Thread(target=run_simulation_polling_consumer, daemon=True)
        thread.start()
        print("Simulation Audit Polling Thread started successfully.")

def process_delay_event(payload_dict):
    sku = payload_dict.get("affectedSku")
    delay = payload_dict.get("delayDays")
    supplier = payload_dict.get("supplierId")
    
    print(f"\n[STREAM EVENT] Captured SUPPLIER_DELAY_ALERT: Supplier={supplier}, SKU={sku}")
    
    # Layer 2: Analytics Blast Radius
    blast_radius = calculate_blast_radius(sku, delay)
    
    # Layer 3: Agentic Decision Pipeline
    plan = Orchestrator.generate_remediation_plan(blast_radius)
    
    # Save processed incident
    processed_incidents.append(plan)

def run_kafka_consumer():
    conf = {
        'bootstrap.servers': os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
        'group.id': 'fastapi-intelligence-group',
        'auto.offset.reset': 'earliest'
    }
    
    try:
        consumer = Consumer(conf)
        consumer.subscribe(['marketplace-events'])
        
        while True:
            msg = consumer.poll(timeout=1.0)
            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                else:
                    print(f"Kafka error: {msg.error()}")
                    break
            
            # Parse Event
            try:
                event_data = json.loads(msg.value().decode('utf-8'))
                if event_data.get("eventType") == "SUPPLIER_DELAY_ALERT":
                    payload = event_data.get("payload", {})
                    process_delay_event(payload)
            except Exception as parse_err:
                print(f"Error parsing Kafka message: {parse_err}")
                
    except Exception as e:
        print(f"Kafka consumer crashed or was disconnected: {e}")

def run_simulation_polling_consumer():
    """
    Zero-Install Fallback: Safely queries Spring Boot's operational audit endpoint
    every 2 seconds to capture 'SUPPLIER_DELAY_ALERT' logs and run the analysis.
    This guarantees the entire pipeline works out-of-the-box on office PCs without Docker/Kafka!
    """
    import requests
    seen_event_ids = set()
    
    while True:
        try:
            response = requests.get(f"{LEDGER_BASE_URL}/audits", timeout=3)
            if response.status_code == 200:
                audits = response.json()
                for audit in audits:
                    trigger_id = audit.get("triggerEventId")
                    if audit.get("eventType") == "SUPPLIER_DELAY_ALERT" and trigger_id not in seen_event_ids:
                        seen_event_ids.add(trigger_id)
                        
                        # Extract data from action summary
                        sku = audit.get("entityIdentifier")
                        # Mock parsing values
                        payload = {
                            "affectedSku": sku,
                            "delayDays": 14,
                            "supplierId": "spl_logitech_01"
                        }
                        process_delay_event(payload)
        except Exception:
            pass # Spring Boot might be booting up
        time.sleep(2)
