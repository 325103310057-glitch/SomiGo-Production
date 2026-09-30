-- ============================================================================
-- BITEDASH CLOUD POSTGRESQL AUTHORITATIVE SEED DATA
-- Enables instant verification in pgAdmin, DBeaver, or Cloud DB Console
-- ============================================================================

-- 1. Insert Core Users
INSERT INTO users (id, full_name, email, phone_number, password_hash, role, account_status, email_verified, phone_verified, created_at, updated_at)
VALUES 
(100, 'Admin Owner', 'owner@bitedash.in', '+919999900000', crypt('AdminSecretPass2026!', gen_salt('bf')), 'ADMIN', 'ACTIVE', TRUE, TRUE, NOW(), NOW()),
(101, 'Rahul Kumar', 'rahul.kumar@gmail.com', '+919848012312', crypt('CustomerPass1!', gen_salt('bf')), 'CUSTOMER', 'ACTIVE', TRUE, TRUE, NOW() - INTERVAL '2 days', NOW()),
(102, 'Priya Sharma', 'priya.sharma@yahoo.com', '+919700045645', crypt('CustomerPass2!', gen_salt('bf')), 'CUSTOMER', 'ACTIVE', TRUE, TRUE, NOW() - INTERVAL '1 day', NOW()),
(103, 'Arjun Reddy', 'arjun.reddy@outlook.com', '+919440198731', crypt('CustomerPass3!', gen_salt('bf')), 'CUSTOMER', 'ACTIVE', TRUE, TRUE, NOW() - INTERVAL '5 hours', NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- 2. Insert Customer Addresses
INSERT INTO customer_addresses (id, user_id, address_line, apartment, landmark, city, state, postal_code, latitude, longitude, address_type, is_default, created_at, updated_at)
VALUES
(1, 101, 'Flat 402, Sea Pearl Heights', 'Tower B', 'Near Beach Road', 'Visakhapatnam', 'Andhra Pradesh', '530002', 17.7231, 83.3012, 'Home', TRUE, NOW(), NOW()),
(2, 102, 'House 12-4, MVP Colony Sector 3', NULL, 'Opposite Park', 'Visakhapatnam', 'Andhra Pradesh', '530017', 17.7400, 83.3320, 'Home', TRUE, NOW(), NOW()),
(3, 103, 'Villa 88, Jubilee Hills', 'Road No. 36', 'Near Metro', 'Hyderabad', 'Telangana', '500033', 17.4319, 78.4073, 'Home', TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('customer_addresses_id_seq', (SELECT MAX(id) FROM customer_addresses));

-- 3. Insert Restaurants
INSERT INTO restaurants (id, owner_id, restaurant_name, description, phone, email, logo_url, cover_image_url, address, city, state, postal_code, latitude, longitude, cuisine_type, average_rating, total_reviews, minimum_order_value, delivery_fee, estimated_delivery_minutes, is_open, is_active, approval_status, created_at, updated_at)
VALUES
(1, 100, 'Paradise Dum Biryani', 'Authentic Hyderabadi Dum Biryani cooked with royal spices and saffron', '+918912543210', 'paradise@bitedash.in', 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=200', 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800', 'Siripuram Circle', 'Visakhapatnam', 'Andhra Pradesh', '530003', 17.7210, 83.3150, 'Biryani, North Indian, Kebabs', 4.6, 1420, 149.0, 25.0, 25, TRUE, TRUE, 'APPROVED', NOW(), NOW()),
(2, 100, 'Crust & Craft Artisan Pizzeria', 'Authentic woodfired Neapolitan pizzas and hand-crafted pastas', '+918912543211', 'crust@bitedash.in', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800', 'Waltair Uplands', 'Visakhapatnam', 'Andhra Pradesh', '530003', 17.7240, 83.3120, 'Italian, Woodfired Pizza, Pasta', 4.8, 890, 199.0, 35.0, 30, TRUE, TRUE, 'APPROVED', NOW(), NOW()),
(3, 100, 'Spice Symphony Kitchen', 'Traditional Andhra meals, crispy dosas, and coastal seafood', '+918912543212', 'spicesymphony@bitedash.in', 'https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=200', 'https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=800', 'Daba Gardens', 'Visakhapatnam', 'Andhra Pradesh', '530020', 17.7120, 83.2980, 'South Indian, Thali, Chettinad', 4.4, 620, 99.0, 0.0, 20, TRUE, TRUE, 'APPROVED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('restaurants_id_seq', (SELECT MAX(id) FROM restaurants));

-- 4. Categories
INSERT INTO categories (id, name, image_url, is_active, display_order)
VALUES
(1, 'Biryani', 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=200', TRUE, 1),
(2, 'Pizza', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200', TRUE, 2),
(3, 'South Indian', 'https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=200', TRUE, 3),
(4, 'Burgers', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200', TRUE, 4),
(5, 'Desserts', 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=200', TRUE, 5)
ON CONFLICT (id) DO NOTHING;

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- 5. Menu Items
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, image_url, price, discounted_price, tax_percentage, preparation_time, vegetarian, vegan, spicy_level, available, is_featured, created_at, updated_at)
VALUES
(101, 1, 1, 'Royal Hyderabadi Chicken Dum Biryani', 'Fragrant basmati rice layered with spiced marinated chicken slow-cooked in handi', 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=800', 320.0, 280.0, 5.0, 20, FALSE, FALSE, 2, TRUE, TRUE, NOW(), NOW()),
(102, 1, 1, 'Nizami Paneer Dum Biryani', 'Fresh cottage cheese cubes marinated in aromatic spices and saffron basmati', 'https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=800', 260.0, 230.0, 5.0, 15, TRUE, FALSE, 1, TRUE, FALSE, NOW(), NOW()),
(201, 2, 2, 'Woodfired Margherita Pizza', 'San Marzano tomato sauce, fresh buffalo mozzarella, fresh basil', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800', 340.0, 299.0, 5.0, 15, TRUE, FALSE, 1, TRUE, TRUE, NOW(), NOW()),
(301, 3, 3, 'Ghee Roast Masala Dosa', 'Golden crispy fermented crepe roasted with pure desi ghee and spiced potato filling', 'https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=800', 140.0, 120.0, 5.0, 10, TRUE, FALSE, 2, TRUE, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('menu_items_id_seq', (SELECT MAX(id) FROM menu_items));

-- 6. Delivery Partners
INSERT INTO delivery_partners (id, user_id, partner_name, phone_number, vehicle_type, vehicle_number, verification_status, availability_status, current_latitude, current_longitude, rating, total_deliveries, created_at, updated_at)
VALUES
(1, 100, 'Rajesh Sharma', '+919848012345', 'Bike', 'AP39 AZ 4210', 'VERIFIED', 'ONLINE', 17.7231, 83.3012, 4.9, 312, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('delivery_partners_id_seq', (SELECT MAX(id) FROM delivery_partners));

-- 7. Sample Orders (as shown in Section 19)
INSERT INTO orders (id, order_number, customer_id, restaurant_id, delivery_partner_id, delivery_address_id, subtotal, delivery_fee, taxes, discount, platform_fee, tip, total_amount, payment_status, order_status, placed_at, delivered_at, created_at, updated_at)
VALUES
(1001, 'ORD1001', 101, 1, 1, 1, 480.0, 25.0, 24.0, 50.0, 5.0, 20.0, 520.0, 'PAID', 'DELIVERED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '32 minutes', NOW() - INTERVAL '1 day', NOW()),
(1002, 'ORD1002', 102, 2, 1, 2, 299.0, 35.0, 15.0, 30.0, 5.0, 10.0, 340.0, 'PAID', 'DELIVERED', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours' + INTERVAL '28 minutes', NOW() - INTERVAL '12 hours', NOW()),
(1003, 'ORD1003', 101, 1, 1, 1, 740.0, 25.0, 37.0, 100.0, 5.0, 30.0, 780.0, 'PAID', 'ON_THE_WAY', NOW() - INTERVAL '15 minutes', NULL, NOW() - INTERVAL '15 minutes', NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));

-- 8. Order Items
INSERT INTO order_items (order_id, menu_item_id, item_name_snapshot, quantity, unit_price, tax, discount, customization_snapshot, total_price)
VALUES
(1001, 101, 'Royal Hyderabadi Chicken Dum Biryani', 2, 280.0, 28.0, 0.0, '{"variant": "Regular"}', 560.0),
(1002, 201, 'Woodfired Margherita Pizza', 1, 299.0, 15.0, 0.0, '{"variant": "10 inch"}', 299.0),
(1003, 101, 'Royal Hyderabadi Chicken Dum Biryani', 2, 280.0, 28.0, 0.0, '{"variant": "Regular"}', 560.0),
(1003, 102, 'Nizami Paneer Dum Biryani', 1, 230.0, 11.5, 0.0, '{"variant": "Regular"}', 230.0)
ON CONFLICT DO NOTHING;

-- 9. Payments
INSERT INTO payments (order_id, user_id, payment_provider, transaction_id, amount, currency, payment_status, payment_method, created_at, updated_at)
VALUES
(1001, 101, 'RAZORPAY', 'pay_1001_rzp_live', 520.0, 'INR', 'PAID', 'UPI', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
(1002, 102, 'RAZORPAY', 'pay_1002_rzp_live', 340.0, 'INR', 'PAID', 'UPI', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),
(1003, 101, 'RAZORPAY', 'pay_1003_rzp_live', 780.0, 'INR', 'PAID', 'CARD', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes')
ON CONFLICT DO NOTHING;
