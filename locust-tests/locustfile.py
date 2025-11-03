from locust import HttpUser, task, between
import random
import string
from datetime import datetime

def random_string(length=6):
    return ''.join(random.choices(string.ascii_lowercase, k=length))

def random_email():
    return f"{random_string()}@mail.com"

def random_order_desc():
    return f"Compra {random_string(4)}"

def current_timestamp():
    return datetime.now().strftime("%d-%m-%Y__%H:%M:%S:%f")

class EcommerceUser(HttpUser):
    wait_time = between(1, 3)

    # Variables base
    base = "http://"
    user_container = "user-service:"
    order_container = "order-service:"
    payment_container = "payment-service:"
    user_port = "8700"
    order_port = "8300"
    payment_port = "8400"

    # Construcción de URLs
    users_url = f"{base}{user_container}{user_port}/user-service/api/users/"
    orders_url = f"{base}{order_container}{order_port}/order-service/api/orders/"
    payments_url = f"{base}{payment_container}{payment_port}/payment-service/api/payments/"
    carts_url = f"{base}{order_container}{order_port}/order-service/api/carts/"

    user_id = None
    cart_id = None
    order_id = None
    payment_id = None

    @task
    def full_flow(self):
        # 1. Crear usuario con datos únicos
        user_payload = {
            "firstName": random_string(),
            "lastName": random_string(),
            "email": random_email(),
            "credential": {
                "username": random_string(),
                "password": "securePassword123",
                "roleBasedAuthority": "ROLE_USER",
                "isEnabled": True
            },
            "addressDtos": [
                {"fullAddress": random_string(10), "postalCode": f"{random.randint(10000,99999)}", "city": random_string(6)}
            ]
        }
        response = self.client.post(self.users_url, json=user_payload)
        if response.status_code in [200, 201]:
            self.user_id = response.json().get("userId")

        # 2. Crear carrito
        if self.user_id:
            cart_payload = {"userId": self.user_id}
            response = self.client.post(self.carts_url, json=cart_payload)
            if response.status_code in [200, 201]:
                self.cart_id = response.json().get("cartId")

        # 3. Crear orden con datos únicos
        if self.cart_id:
            order_payload = {
                "orderDesc": random_order_desc(),
                "orderDate": current_timestamp(),
                "orderFee": random.randint(1000, 10000),
                "cart": {"cartId": self.cart_id, "userId": self.user_id}
            }
            response = self.client.post(self.orders_url, json=order_payload)
            if response.status_code in [200, 201]:
                self.order_id = response.json().get("orderId")

        # 4. Crear pago
        if self.order_id:
            payment_payload = {
                "isPayed": False,
                "paymentStatus": "NOT_STARTED",
                "order": {"orderId": self.order_id}
            }
            response = self.client.post(self.payments_url, json=payment_payload)
            if response.status_code in [200, 201]:
                self.payment_id = response.json().get("paymentId")
