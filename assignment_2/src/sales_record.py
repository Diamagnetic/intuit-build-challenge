from pydantic import BaseModel, Field
from decimal import Decimal

class SalesRecord(BaseModel):
  """Pydantic model representing single row of a sale"""
  order_id: int
  date: str
  region: str
  product: str
  category: str
  price: Decimal = Field(gt = 0, decimal_places = 2)
  quantity: int
