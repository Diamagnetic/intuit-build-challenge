"""
CSV generator

Deterministically generates synthetic sales data in CSV format
"""

import csv
import random
from datetime import datetime, timedelta
from pydantic import BaseModel, Field
from typing import List

from product import Product

class CSVGenerator(BaseModel):
  """
  Generates a synthetic sales CSV file given a list of Products
  Uses seeded randomness to allow reproducibility
  """

  file_path: str
  rows: int = Field(gt = 0, default = 50)
  products: List[Product]
  regions: List[str] = ["North", "South", "East", "West"]
  seed: int = 123

  def random_date(self):
    """
    Generate a random date for the order
    
    Format = YYYY-MM-DD
    """

    start = datetime(2025, 1, 1)
    end = datetime(2025, 12, 31)
    delta = end - start

    return (start + timedelta(days = random.randint(0, delta.days))
           ).strftime("%Y-%m-%d")

  def generate_csv(self):
    """
    Create a CSV file at self.file_path with specified number of rows
    Fixed seed to ensure deterministic output for unit tests

    Each row contains:
    - order_id
    - date
    - region
    - product (name)
    - category
    - price
    - quantity
    """

    # Ensures reproducible CSV data
    random.seed(self.seed)

    with open(self.file_path, "w", newline = "") as file:
      writer = csv.writer(file)
      writer.writerow(["order_id", "date", "region", "product", "category",
                       "price", "quantity"
                     ])

      for i in range(self.rows):
        # randomly choose a product
        prod = random.choice(self.products)

        # create a single row/order/record
        row = [
          i + 1,
          self.random_date(),
          random.choice(self.regions),
          prod.name,
          prod.category,
          prod.price,
          random.randint(1, 25),
        ]

        writer.writerow(row)

    print(f"Generated CSV with {self.rows} rows in {self.file_path}")
