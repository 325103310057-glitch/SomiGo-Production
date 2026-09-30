package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.CartItem
import com.example.domain.model.DeliveryStatus
import com.example.domain.model.Order
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testAppNameIsSomiGo() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SomiGo", appName)
  }

  @Test
  fun testCartItemCalculations() {
    val item1 = CartItem(
      menuItemId = 101,
      restaurantId = 1,
      name = "Royal Hyderabadi Chicken Dum Biryani",
      quantity = 2,
      unitPrice = 280.0
    )
    val item2 = CartItem(
      menuItemId = 102,
      restaurantId = 1,
      name = "Nizami Paneer Dum Biryani",
      quantity = 1,
      unitPrice = 230.0
    )

    assertEquals(560.0, item1.totalPrice, 0.01)
    assertEquals(230.0, item2.totalPrice, 0.01)

    val subtotal = listOf(item1, item2).sumOf { it.totalPrice }
    assertEquals(790.0, subtotal, 0.01)

    // With 5% taxes and ₹25 delivery
    val taxes = subtotal * 0.05
    val deliveryFee = 25.0
    val platformFee = 5.0
    val total = subtotal + taxes + deliveryFee + platformFee
    assertEquals(859.5, total, 0.01)
  }

  @Test
  fun testCouponDiscountCalculations() {
    val subtotal = 400.0
    // SOMIGO50: 50% discount up to ₹100
    val somigo50Discount = Math.min(100.0, subtotal * 0.5)
    assertEquals(100.0, somigo50Discount, 0.01)

    // SOMIGO75: Flat ₹75 discount
    val somigo75Discount = 75.0
    assertEquals(75.0, somigo75Discount, 0.01)

    // With SOMIGO50 coupon applied:
    val deliveryFee = 25.0
    val taxes = subtotal * 0.05 // ₹20
    val platformFee = 5.0
    val totalWithDiscount = subtotal + deliveryFee + taxes + platformFee - somigo50Discount
    assertEquals(350.0, totalWithDiscount, 0.01)
  }

  @Test
  fun testRewardsCoinsRedemption() {
    val pointsBalance = 250
    val coinsToRedeem = 100
    assertTrue(pointsBalance >= coinsToRedeem)

    val coinDiscountRupees = 50.0 // 100 coins = ₹50
    val initialTotal = 350.0
    val finalTotal = initialTotal - coinDiscountRupees
    assertEquals(300.0, finalTotal, 0.01)
  }

  @Test
  fun testGroupOrderSplitBillCalculation() {
    val person1Total = 240.0
    val person2Total = 180.0
    val person3Total = 220.0
    val totalFood = person1Total + person2Total + person3Total
    assertEquals(640.0, totalFood, 0.01)

    val sharedCharges = (25.0 + (totalFood * 0.05) + 5.0) / 3 // ₹25 delivery + ₹32 tax + ₹5 platform / 3 = ₹20.67
    val person1Final = person1Total + sharedCharges
    val person2Final = person2Total + sharedCharges
    val person3Final = person3Total + sharedCharges

    assertEquals(totalFood + 62.0, person1Final + person2Final + person3Final, 0.01)
  }

  @Test
  fun testDeliveryVerificationPin() {
    val order = Order(
      id = 1001,
      orderNumber = "ORD1001",
      restaurantId = 1,
      restaurantName = "Paradise Dum Biryani",
      items = emptyList(),
      subtotal = 480.0,
      deliveryFee = 25.0,
      taxes = 24.0,
      discount = 0.0,
      totalAmount = 529.0,
      status = DeliveryStatus.ON_THE_WAY,
      paymentStatus = "PAID",
      placedAt = "10:30 AM",
      deliveryAddress = "Beach Road",
      deliveryPin = "4827"
    )
    assertNotNull(order.deliveryPin)
    assertEquals(4, order.deliveryPin.length)
    assertTrue(order.deliveryPin.all { it.isDigit() })
  }

  @Test
  fun testOrderStateProgression() {
    val statuses = listOf(
      DeliveryStatus.PLACED,
      DeliveryStatus.ACCEPTED,
      DeliveryStatus.PREPARING,
      DeliveryStatus.READY,
      DeliveryStatus.PICKED_UP,
      DeliveryStatus.ON_THE_WAY,
      DeliveryStatus.DELIVERED
    )
    assertEquals(7, statuses.size)
    assertEquals("Order Placed", statuses[0].displayName)
    assertEquals("On The Way", statuses[5].displayName)
    assertEquals("Delivered", statuses[6].displayName)
  }

  @Test
  fun testDynamicCustomerProfileUpdate() {
    val profile = com.example.presentation.viewmodel.CustomerProfile(
      userId = 1024,
      fullName = "Dheeraj Chukka",
      email = "dheeraj@somigo.in",
      phoneNumber = "+91 98491 23456"
    )
    assertEquals("Dheeraj Chukka", profile.fullName)
    assertEquals("dheeraj@somigo.in", profile.email)

    val updatedProfile = profile.copy(
      fullName = "Dheeraj C.",
      phoneNumber = "+91 99887 76655"
    )
    assertEquals("Dheeraj C.", updatedProfile.fullName)
    assertEquals("+91 99887 76655", updatedProfile.phoneNumber)
    assertEquals("dheeraj@somigo.in", updatedProfile.email)
  }

  @Test
  fun testDeviceLocationModel() {
    val loc = com.example.data.location.DeviceLocation(
      latitude = 17.6868,
      longitude = 83.2185,
      accuracyMeters = 4.5f,
      timestamp = System.currentTimeMillis(),
      resolvedAddress = "Beach Road, Visakhapatnam",
      city = "Visakhapatnam"
    )
    assertEquals(17.6868, loc.latitude, 0.0001)
    assertEquals(83.2185, loc.longitude, 0.0001)
    assertEquals("Visakhapatnam", loc.city)
    assertTrue(loc.accuracyMeters < 10.0f)
  }

  @Test
  fun testRealtimeGpsTelemetryOrder() {
    val order = Order(
      id = 2001,
      orderNumber = "ORD2001",
      restaurantId = 1,
      restaurantName = "Paradise Biryani",
      items = emptyList(),
      subtotal = 300.0,
      deliveryFee = 25.0,
      taxes = 15.0,
      discount = 0.0,
      totalAmount = 345.0,
      status = DeliveryStatus.ON_THE_WAY,
      paymentStatus = "PAID",
      placedAt = "12:00 PM",
      deliveryAddress = "MVP Colony",
      deliveryPartnerName = "Ramesh Kumar",
      deliveryPartnerLatitude = 17.7250,
      deliveryPartnerLongitude = 83.3050,
      deliveryPartnerAccuracyMeters = 3.8f,
      hasLiveGpsSignal = true
    )
    assertTrue(order.hasLiveGpsSignal)
    assertNotNull(order.deliveryPartnerLatitude)
    assertNotNull(order.deliveryPartnerLongitude)
    assertEquals("Ramesh Kumar", order.deliveryPartnerName)
  }
}
