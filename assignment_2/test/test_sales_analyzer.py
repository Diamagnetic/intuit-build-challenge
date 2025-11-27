"""
Unit tests for SalesAnalyzer analytical methods using CSV data
"""

import os
import unittest
from decimal import Decimal, ROUND_HALF_UP

from sales_analyzer import SalesAnalyzer

TEST_CSV = "test_sales.csv"

CSV_CONTENT = """order_id,date,region,product,category,price,quantity
1,2025-01-01,North,Laptop,Electronics,1500.00,10
2,2025-01-02,South,Phone,Electronics,600.00,20
3,2025-01-03,North,Tablet,Electronics,300.00,5
4,2025-01-04,West,Laptop,Electronics,1200.00,3
5,2025-01-05,South,Headphones,Accessories,100.00,15
"""

class TestSalesAnalyzer(unittest.TestCase):
  """Tests for SalesAnalyzer class' analytical methods"""

  @classmethod
  def setUpClass(cls):
     """Write CSV data to a file"""
     with open(TEST_CSV, "w") as f:
       f.write(CSV_CONTENT)

  @classmethod
  def tearDownClass(cls):
    """Delete CSV file"""
    os.remove(TEST_CSV)

  def setUp(self):
    """Set up members required for testing"""
    self.sa = SalesAnalyzer(TEST_CSV)

  def test_total_revenue(self):
    """Total revenue should equal sum(price * quantity) across all rows"""
    expected = (
      Decimal("15000") +
      Decimal("12000") +
      Decimal("1500") +
      Decimal("3600") +
      Decimal("1500")
    )
    self.assertEqual(self.sa.total_revenue(), expected,
      "Total revenue calculation is incorrect"
    )

  def test_total_quantity(self):
    """Total quantity should equal sum(quantity) across all rows"""
    expected = 10 + 20 + 5 + 3 + 15
    self.assertEqual(self.sa.total_quantity(), expected,
      "Total quantity does not match expected sum" 
    )

  def test_mean_order_value(self):
    """Mean order value should be total revenue / total quantity"""
    expected = (
      self.sa.total_revenue() / self.sa.total_quantity()
    ).quantize(Decimal("0.01"), ROUND_HALF_UP)

    self.assertEqual(self.sa.mean_order_value(), expected,
      "Mean order value is incorrectly calculated"
    )

  def test_highest_revenue_sale(self):
    """Verify that the highest revenue sale is correctly identified"""
    best = self.sa.highest_revenue_sale()
    self.assertEqual(best.order_id, 1, "Incorrect order ID for highest revenue sale")

  def test_orders_above_threshold(self):
    """Orders above a threshold should return all rows whose revenue exceeds an amount"""
    results = list(self.sa.orders_above_threshold(Decimal("5000")))
    self.assertEqual(len(results), 2, "Expected exactly 2 orders above 5000")
    self.assertSetEqual({r.order_id for r in results}, {1, 2},
      "Incorrect orders returned"
    )

  def test_orders_above_threshold_no_order(self):
    """Threshold higher than any revenue. Should return empty list"""
    results = list(self.sa.orders_above_threshold(Decimal("1000000")))
    self.assertEqual(len(results), 0, "List should be empty")

  def test_revenue_for_region(self):
    """Revenue for each region must equal the sum of revenue only for that region"""
    self.assertEqual(
      self.sa.revenue_for_region("North"),
      Decimal("15000") + Decimal("1500"),
      "Incorrect revenue for region 'North'"
    )
    self.assertEqual(
      self.sa.revenue_for_region("South"),
      Decimal("12000") + Decimal("1500"),
      "Incorrect revenue for region 'South'"
    )
    self.assertEqual(
      self.sa.revenue_for_region("West"),
      Decimal("3600"),
      "Incorrect revenue for region 'West'"
    )

  def test_revenue_for_region_not_found(self):
    """Region not present in CSV data should return 0 revenue"""
    self.assertEqual(self.sa.revenue_for_region("East"), 0,
      "Revenue should be 0 for non-existent region"
    )

  def test_total_revenue_by_region_keys(self):
    """Grouped revenue should contain only the expected regions"""
    result = self.sa.total_revenue_by_region()
    expected = {"North", "South", "West"}
    self.assertEqual(set(result.keys()), expected, "Region grouping incorrect")

  def test_total_revenue_by_region(self):
    """Revenue grouped by region must match expected sums"""
    result = self.sa.total_revenue_by_region()
    self.assertEqual(result["North"], Decimal("15000") + Decimal("1500"),
      "Incorrect grouped revenue for 'North'"
    )
    self.assertEqual(result["South"], Decimal("12000") + Decimal("1500"),
      "Incorrect grouped revenue for 'South'"
    )
    self.assertEqual(result["West"], Decimal("3600"),
      "Incorrect grouped revenue for 'West'"
    )


if __name__ == "__main__":
    unittest.main()
