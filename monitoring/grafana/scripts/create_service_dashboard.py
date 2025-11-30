#!/usr/bin/env python3
"""
Script to generate Grafana dashboards for each microservice
"""
import json
import os

SERVICES = {
    "API-GATEWAY": {
        "title": "API Gateway Dashboard",
        "uid": "api-gateway",
        "description": "Dashboard for API Gateway service monitoring",
        "tags": ["api-gateway", "microservices"]
    },
    "USER-SERVICE": {
        "title": "User Service Dashboard",
        "uid": "user-service",
        "description": "Dashboard for User Service monitoring",
        "tags": ["user-service", "microservices"]
    },
    "PRODUCT-SERVICE": {
        "title": "Product Service Dashboard",
        "uid": "product-service",
        "description": "Dashboard for Product Service monitoring",
        "tags": ["product-service", "microservices"]
    },
    "ORDER-SERVICE": {
        "title": "Order Service Dashboard",
        "uid": "order-service",
        "description": "Dashboard for Order Service monitoring",
        "tags": ["order-service", "microservices"]
    },
    "PAYMENT-SERVICE": {
        "title": "Payment Service Dashboard",
        "uid": "payment-service",
        "description": "Dashboard for Payment Service monitoring",
        "tags": ["payment-service", "microservices"]
    },
    "SHIPPING-SERVICE": {
        "title": "Shipping Service Dashboard",
        "uid": "shipping-service",
        "description": "Dashboard for Shipping Service monitoring",
        "tags": ["shipping-service", "microservices"]
    },
    "FAVOURITE-SERVICE": {
        "title": "Favourite Service Dashboard",
        "uid": "favourite-service",
        "description": "Dashboard for Favourite Service monitoring",
        "tags": ["favourite-service", "microservices"]
    },
    "PROXY-CLIENT": {
        "title": "Proxy Client Dashboard",
        "uid": "proxy-client",
        "description": "Dashboard for Proxy Client service monitoring",
        "tags": ["proxy-client", "microservices"]
    }
}

def create_panel(panel_id, title, expr, legend_format, panel_type="timeseries", unit="", grid_pos=None, ref_id="A"):
    """Create a Grafana panel configuration"""
    panel = {
        "datasource": "Prometheus",
        "fieldConfig": {
            "defaults": {
                "color": {"mode": "palette-classic"},
                "custom": {
                    "axisLabel": "",
                    "axisPlacement": "auto",
                    "barAlignment": 0,
                    "drawStyle": "line",
                    "fillOpacity": 10,
                    "gradientMode": "none",
                    "hideFrom": {"tooltip": False, "viz": False, "legend": False},
                    "lineInterpolation": "linear",
                    "lineWidth": 1,
                    "pointSize": 5,
                    "scaleDistribution": {"type": "linear"},
                    "showPoints": "never",
                    "spanNulls": True
                },
                "mappings": [],
                "thresholds": {
                    "mode": "absolute",
                    "steps": [{"color": "green", "value": None}]
                },
                "unit": unit
            }
        },
        "gridPos": grid_pos or {"h": 8, "w": 12, "x": 0, "y": 0},
        "id": panel_id,
        "options": {
            "legend": {
                "calcs": ["mean", "lastNotNull", "max"],
                "displayMode": "table",
                "placement": "bottom"
            },
            "tooltip": {"mode": "multi"}
        },
        "pluginVersion": "8.0.0",
        "targets": [{
            "expr": expr,
            "legendFormat": legend_format,
            "refId": ref_id
        }],
        "title": title,
        "type": panel_type
    }
    return panel

def create_dashboard(service_name, service_config):
    """Create a complete dashboard for a service"""
    application = service_name
    
    panels = []
    y_pos = 0
    
    # Panel 1: Request Rate
    panels.append(create_panel(
        1,
        "Request Rate (req/s)",
        f'sum(rate(http_server_requests_seconds_count{{application="{application}"}}[5m]))',
        "Total Requests/sec",
        unit="reqps",
        grid_pos={"h": 8, "w": 12, "x": 0, "y": y_pos}
    ))
    
    # Panel 2: Request Latency
    panels.append({
        "datasource": "Prometheus",
        "fieldConfig": {
            "defaults": {
                "color": {"mode": "palette-classic"},
                "custom": {
                    "axisLabel": "",
                    "axisPlacement": "auto",
                    "barAlignment": 0,
                    "drawStyle": "line",
                    "fillOpacity": 10,
                    "gradientMode": "none",
                    "hideFrom": {"tooltip": False, "viz": False, "legend": False},
                    "lineInterpolation": "linear",
                    "lineWidth": 1,
                    "pointSize": 5,
                    "scaleDistribution": {"type": "linear"},
                    "showPoints": "never",
                    "spanNulls": True
                },
                "mappings": [],
                "thresholds": {
                    "mode": "absolute",
                    "steps": [
                        {"color": "green", "value": None},
                        {"color": "yellow", "value": 500},
                        {"color": "red", "value": 1000}
                    ]
                },
                "unit": "ms"
            }
        },
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": y_pos},
        "id": 2,
        "options": {
            "legend": {
                "calcs": ["mean", "lastNotNull", "max"],
                "displayMode": "table",
                "placement": "bottom"
            },
            "tooltip": {"mode": "multi"}
        },
        "pluginVersion": "8.0.0",
        "targets": [
            {
                "expr": f'histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket{{application="{application}"}}[5m])) by (le)) * 1000',
                "legendFormat": "p99",
                "refId": "A"
            },
            {
                "expr": f'histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{{application="{application}"}}[5m])) by (le)) * 1000',
                "legendFormat": "p95",
                "refId": "B"
            },
            {
                "expr": f'histogram_quantile(0.50, sum(rate(http_server_requests_seconds_bucket{{application="{application}"}}[5m])) by (le)) * 1000',
                "legendFormat": "p50",
                "refId": "C"
            }
        ],
        "title": "Request Latency (ms)",
        "type": "timeseries"
    })
    
    y_pos += 8
    
    # Panel 3: Success Rate
    panels.append(create_panel(
        3,
        "Success Rate (%)",
        f'sum(rate(http_server_requests_seconds_count{{application="{application}",status=~"2.."}}[5m])) / sum(rate(http_server_requests_seconds_count{{application="{application}"}}[5m])) * 100',
        "Success Rate",
        panel_type="gauge",
        unit="percent",
        grid_pos={"h": 8, "w": 12, "x": 0, "y": y_pos}
    ))
    
    # Panel 4: Error Rate
    panels.append(create_panel(
        4,
        "Error Rate (5xx)",
        f'sum(rate(http_server_requests_seconds_count{{application="{application}",status=~"5.."}}[5m])) by (status)',
        "{{status}}",
        unit="short",
        grid_pos={"h": 8, "w": 12, "x": 12, "y": y_pos}
    ))
    
    y_pos += 8
    
    # Panel 5: HTTP Status Codes
    panels.append(create_panel(
        5,
        "HTTP Status Codes",
        f'sum(rate(http_server_requests_seconds_count{{application="{application}"}}[5m])) by (status)',
        "{{status}}",
        panel_type="timeseries",
        unit="short",
        grid_pos={"h": 8, "w": 12, "x": 0, "y": y_pos}
    ))
    
    # Panel 6: Circuit Breaker Status
    panels.append(create_panel(
        6,
        "Circuit Breaker Status",
        f'resilience4j_circuitbreaker_state{{application="{application}"}}',
        "{{name}} - {{state}}",
        unit="short",
        grid_pos={"h": 8, "w": 12, "x": 12, "y": y_pos}
    ))
    
    y_pos += 8
    
    # Panel 7: JVM Memory
    panels.append({
        "datasource": "Prometheus",
        "fieldConfig": {
            "defaults": {
                "color": {"mode": "palette-classic"},
                "custom": {
                    "axisLabel": "",
                    "axisPlacement": "auto",
                    "barAlignment": 0,
                    "drawStyle": "line",
                    "fillOpacity": 10,
                    "gradientMode": "none",
                    "hideFrom": {"tooltip": False, "viz": False, "legend": False},
                    "lineInterpolation": "linear",
                    "lineWidth": 1,
                    "pointSize": 5,
                    "scaleDistribution": {"type": "linear"},
                    "showPoints": "never",
                    "spanNulls": True
                },
                "mappings": [],
                "thresholds": {
                    "mode": "absolute",
                    "steps": [
                        {"color": "green", "value": None},
                        {"color": "yellow", "value": 80},
                        {"color": "red", "value": 90}
                    ]
                },
                "unit": "MiB"
            }
        },
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": y_pos},
        "id": 7,
        "options": {
            "legend": {
                "calcs": ["mean", "lastNotNull", "max"],
                "displayMode": "table",
                "placement": "bottom"
            },
            "tooltip": {"mode": "multi"}
        },
        "pluginVersion": "8.0.0",
        "targets": [
            {
                "expr": f'jvm_memory_used_bytes{{application="{application}",area="heap"}} / 1024 / 1024',
                "legendFormat": "Heap Used",
                "refId": "A"
            },
            {
                "expr": f'jvm_memory_max_bytes{{application="{application}",area="heap"}} / 1024 / 1024',
                "legendFormat": "Heap Max",
                "refId": "B"
            }
        ],
        "title": "JVM Memory Usage",
        "type": "timeseries"
    })
    
    # Panel 8: CPU Usage
    panels.append(create_panel(
        8,
        "CPU Usage (%)",
        f'process_cpu_usage{{application="{application}"}} * 100',
        "CPU Usage",
        unit="percent",
        grid_pos={"h": 8, "w": 12, "x": 12, "y": y_pos}
    ))
    
    y_pos += 8
    
    # Panel 9: Requests by Endpoint
    panels.append(create_panel(
        9,
        "Requests by Endpoint",
        f'sum(rate(http_server_requests_seconds_count{{application="{application}"}}[5m])) by (uri)',
        "{{uri}}",
        unit="reqps",
        grid_pos={"h": 8, "w": 24, "x": 0, "y": y_pos}
    ))
    
    y_pos += 8
    
    # Panel 10: Health Status
    panels.append({
        "datasource": "Prometheus",
        "fieldConfig": {
            "defaults": {
                "color": {"mode": "thresholds"},
                "mappings": [
                    {
                        "options": {
                            "0": {"color": "red", "index": 1, "text": "DOWN"},
                            "1": {"color": "green", "index": 0, "text": "UP"}
                        },
                        "type": "value"
                    }
                ],
                "thresholds": {
                    "mode": "absolute",
                    "steps": [
                        {"color": "red", "value": None},
                        {"color": "green", "value": 1}
                    ]
                },
                "unit": "short"
            }
        },
        "gridPos": {"h": 8, "w": 24, "x": 0, "y": y_pos},
        "id": 10,
        "options": {
            "colorMode": "background",
            "graphMode": "none",
            "justifyMode": "auto",
            "orientation": "auto",
            "reduceOptions": {
                "values": False,
                "calcs": ["lastNotNull"],
                "fields": ""
            },
            "textMode": "auto"
        },
        "pluginVersion": "8.0.0",
        "targets": [{
            "expr": f'up{{application="{application}"}}',
            "legendFormat": "Service Health",
            "refId": "A"
        }],
        "title": "Service Health Status",
        "type": "stat"
    })
    
    dashboard = {
        "annotations": {
            "list": [{
                "builtIn": 1,
                "datasource": "-- Grafana --",
                "enable": True,
                "hide": True,
                "iconColor": "rgba(0, 211, 255, 1)",
                "name": "Annotations & Alerts",
                "type": "dashboard"
            }]
        },
        "editable": True,
        "gnetId": None,
        "graphTooltip": 0,
        "id": None,
        "links": [],
        "panels": panels,
        "refresh": "10s",
        "schemaVersion": 27,
        "style": "dark",
        "tags": service_config["tags"],
        "templating": {"list": []},
        "time": {"from": "now-15m", "to": "now"},
        "timepicker": {},
        "timezone": "",
        "title": service_config["title"],
        "uid": service_config["uid"],
        "version": 1
    }
    
    return dashboard

def main():
    """Generate dashboards for all services"""
    output_dir = os.path.join(os.path.dirname(__file__), "..", "dashboards")
    os.makedirs(output_dir, exist_ok=True)
    
    for service_name, service_config in SERVICES.items():
        dashboard = create_dashboard(service_name, service_config)
        filename = f"{service_config['uid']}.json"
        filepath = os.path.join(output_dir, filename)
        
        with open(filepath, 'w') as f:
            json.dump(dashboard, f, indent=2)
        
        print(f"Generated dashboard: {filename}")

if __name__ == "__main__":
    main()

