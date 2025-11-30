from locust import HttpUser, task, between
import os


GATEWAY_HOST = os.getenv("GATEWAY_HOST", "http://localhost:8080")


FAVOURITE_HOST_DIRECT = os.getenv("FAVOURITE_HOST_DIRECT", "http://localhost:8800")

# URLs que utilizan el Gateway (8080)
PRODUCT_URL = f"{GATEWAY_HOST}/product-service/api/products"
USER_URL = f"{GATEWAY_HOST}/user-service/api/users"
SHIPPING_URL = f"{GATEWAY_HOST}/shipping-service/api/shippings"
PAYMENT_URL = f"{GATEWAY_HOST}/payment-service/api/payments"
ORDER_URL = f"{GATEWAY_HOST}/order-service/api/orders"




class EcommerceUser(HttpUser):
    host = GATEWAY_HOST
    wait_time = between(0.25, 0.75)

    @task(3)
    def products_list(self):
        self.client.get(PRODUCT_URL, name="GET /products")

    @task(3)
    def users_list(self):
        self.client.get(USER_URL, name="GET /users")

    @task(1)
    def shipping_list(self):
        try:
            self.client.get(SHIPPING_URL, name="GET /shippings", timeout=5)
        except Exception:
            pass

    @task(1)
    def payments_list(self):
        try:
            self.client.get(PAYMENT_URL, name="GET /payments", timeout=5)
        except Exception:
            pass

    @task(1)
    def orders_list(self):
        try:
            self.client.get(ORDER_URL, name="GET /orders", timeout=5)
        except Exception:
            pass


    @task(1)
    def favourites_list(self):
        direct_url = f"{FAVOURITE_HOST_DIRECT}/favourite-service/api/favourites"
        
        self.client.get(
            direct_url,
            name="GET /favourite"
        )