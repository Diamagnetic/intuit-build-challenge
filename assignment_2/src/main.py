"""
Driver program

Generates CSV data, and runs analytical queries
"""

from pathlib import Path

from sales_analyzer import SalesAnalyzer
from product import Product
from csv_generator import CSVGenerator

if __name__ == "__main__":
  """
  Main execution function demonstrating all analytical queries
  Demonstrates all functionality of SalesAnalyzer
  """

  FILE_PATH = str(Path(__file__).parent.parent / "data" / "sales.csv")

  PRODUCTS = [
    Product(name = "Laptop", category = "Electronics", price = 500),
    Product(name = "Headphones", category = "Electronics", price = 30),
    Product(name = "Mouse", category = "Electronics", price = 10),
    Product(name = "Jeans", category = "Clothing", price = 40),
    Product(name = "Shirt", category = "Clothing", price = 15),
    Product(name = "Coffee Machine", category = "Home", price = 120),
    Product(name = "Blender", category = "Home", price = 50),
    Product(name = "Treadmill", category = "Fitness", price = 135),
  ]

  generator = CSVGenerator(
    file_path = FILE_PATH,
    rows = 50,
    products = PRODUCTS,
    seed = 1234
  )

  # Create a CSV file
  generator.generate_csv()

  print("*" * 50)
  print("CSV Data Analysis")
  print("*" * 50)

  sa = SalesAnalyzer(FILE_PATH)

  # 1. Total Revenue
  print("*" * 50)
  print("1. Total Revenue")
  print("*" * 50)
  print(f"Total Revenue = ${sa.total_revenue()}\n")

  # 2. Total Quantity sold
  print("*" * 50)
  print("2. Total Quantity")
  print("*" * 50)
  print(f"Total quantity sold = {sa.total_quantity()}\n")

  # 3. Mean order value
  print("*" * 50)
  print("3. Mean order value")
  print("*" * 50)
  print(f"Mean order value = ${sa.mean_order_value()}\n")

  # 4. Highest revenue sale
  print("*" * 50)
  print("4. Highest revenue sale")
  print("*" * 50)
  order = sa.highest_revenue_sale()
  print("Highest revenue sale")
  print(", ".join(order.model_dump().keys()))
  print(", ".join(map(str, order.model_dump().values())))
  print("")

  # 5. Get orders above $10000
  print("*" * 50)
  print("5. Orders above threshold")
  print("*" * 50)
  orders = list(sa.orders_above_threshold(10000))
  print("Orders above $10,000:")
  print(", ".join(orders[0].model_dump().keys()))
  for o in orders:
    print(", ".join(map(str, o.model_dump().values())))
  print("")

  # 6. Get revenue for the 'North' region
  print("*" * 50)
  print("6. Revenue for region")
  print("*" * 50)
  print(f"Revenue for 'North' region = ${sa.revenue_for_region('North')}\n")

  # 7. Get total revenue by region
  print("*" * 50)
  print("7. Total Revenue grouped by region")
  print("*" * 50)
  revenue_grouped = sa.total_revenue_by_region()
  print("Revenue grouped by region:")
  for region, amount in revenue_grouped.items():
    print(f"{region}: ${amount}")
