CREATE TABLE pizza_order (
  id UUID PRIMARY KEY,
  customer_name VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE pizza_order_pizzas (
  pizza_order_id UUID NOT NULL,
  pizzas VARCHAR(50),
  FOREIGN KEY (pizza_order_id) REFERENCES pizza_order(id)
);