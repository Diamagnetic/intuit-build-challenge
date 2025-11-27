from decimal import Decimal
from pydantic import BaseModel, Field

class Product(BaseModel):
  """Pydantic model representing a Product"""
  name: str
  category: str
  price: Decimal = Field(gt = 0, decimal_places = 2)
