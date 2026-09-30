"""Initial production food delivery schema

Revision ID: 001_initial_schema
Create Date: 2026-09-30 00:00:00.000000
"""
from alembic import op
import sqlalchemy as sa

revision = '001_initial_schema'
down_revision = None
branch_labels = None
depends_on = None

def upgrade() -> None:
    # 1. users
    op.create_table(
        'users',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('full_name', sa.String(length=255), nullable=False),
        sa.Column('email', sa.String(length=255), nullable=False),
        sa.Column('phone_number', sa.String(length=20), nullable=False),
        sa.Column('password_hash', sa.String(length=255), nullable=True),
        sa.Column('profile_image_url', sa.String(length=500), nullable=True),
        sa.Column('date_of_birth', sa.String(length=50), nullable=True),
        sa.Column('role', sa.String(length=50), nullable=False, server_default='CUSTOMER'),
        sa.Column('account_status', sa.String(length=50), nullable=False, server_default='ACTIVE'),
        sa.Column('email_verified', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('phone_verified', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.Column('updated_at', sa.DateTime(), nullable=False),
        sa.Column('last_login_at', sa.DateTime(), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_users_email', 'users', ['email'], unique=True)
    op.create_index('ix_users_phone_number', 'users', ['phone_number'], unique=True)

    # 2. customer_addresses
    op.create_table(
        'customer_addresses',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('user_id', sa.Integer(), nullable=False),
        sa.Column('address_line', sa.String(length=255), nullable=False),
        sa.Column('apartment', sa.String(length=100), nullable=True),
        sa.Column('landmark', sa.String(length=100), nullable=True),
        sa.Column('city', sa.String(length=100), nullable=False),
        sa.Column('state', sa.String(length=100), nullable=False),
        sa.Column('postal_code', sa.String(length=20), nullable=False),
        sa.Column('latitude', sa.Float(), nullable=False),
        sa.Column('longitude', sa.Float(), nullable=False),
        sa.Column('address_type', sa.String(length=50), nullable=False, server_default='Home'),
        sa.Column('is_default', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.Column('updated_at', sa.DateTime(), nullable=False),
        sa.ForeignKeyConstraint(['user_id'], ['users.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_customer_addresses_user_id', 'customer_addresses', ['user_id'])
    op.create_index('ix_customer_addresses_city', 'customer_addresses', ['city'])

    # 3. restaurants
    op.create_table(
        'restaurants',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('owner_id', sa.Integer(), nullable=False),
        sa.Column('restaurant_name', sa.String(length=255), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('phone', sa.String(length=20), nullable=False),
        sa.Column('email', sa.String(length=255), nullable=True),
        sa.Column('logo_url', sa.String(length=500), nullable=True),
        sa.Column('cover_image_url', sa.String(length=500), nullable=True),
        sa.Column('address', sa.String(length=255), nullable=False),
        sa.Column('city', sa.String(length=100), nullable=False),
        sa.Column('state', sa.String(length=100), nullable=False),
        sa.Column('postal_code', sa.String(length=20), nullable=False),
        sa.Column('latitude', sa.Float(), nullable=False),
        sa.Column('longitude', sa.Float(), nullable=False),
        sa.Column('cuisine_type', sa.String(length=255), nullable=False),
        sa.Column('average_rating', sa.Float(), nullable=False, server_default='4.0'),
        sa.Column('total_reviews', sa.Integer(), nullable=False, server_default='0'),
        sa.Column('minimum_order_value', sa.Float(), nullable=False, server_default='99.0'),
        sa.Column('delivery_fee', sa.Float(), nullable=False, server_default='30.0'),
        sa.Column('estimated_delivery_minutes', sa.Integer(), nullable=False, server_default='30'),
        sa.Column('opening_time', sa.String(length=10), nullable=False, server_default='09:00'),
        sa.Column('closing_time', sa.String(length=10), nullable=False, server_default='23:00'),
        sa.Column('is_open', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('is_active', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('approval_status', sa.String(length=50), nullable=False, server_default='APPROVED'),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.Column('updated_at', sa.DateTime(), nullable=False),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_restaurants_city', 'restaurants', ['city'])
    op.create_index('ix_restaurants_restaurant_name', 'restaurants', ['restaurant_name'])

    # 4. categories
    op.create_table(
        'categories',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('image_url', sa.String(length=500), nullable=True),
        sa.Column('is_active', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('display_order', sa.Integer(), nullable=False, server_default='0'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_categories_name', 'categories', ['name'], unique=True)

    # 5. menu_items
    op.create_table(
        'menu_items',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('restaurant_id', sa.Integer(), nullable=False),
        sa.Column('category_id', sa.Integer(), nullable=True),
        sa.Column('name', sa.String(length=255), nullable=False),
        sa.Column('description', sa.Text(), nullable=True),
        sa.Column('image_url', sa.String(length=500), nullable=True),
        sa.Column('price', sa.Float(), nullable=False),
        sa.Column('discounted_price', sa.Float(), nullable=True),
        sa.Column('tax_percentage', sa.Float(), nullable=False, server_default='5.0'),
        sa.Column('preparation_time', sa.Integer(), nullable=False, server_default='15'),
        sa.Column('vegetarian', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('vegan', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('spicy_level', sa.Integer(), nullable=False, server_default='1'),
        sa.Column('available', sa.Boolean(), nullable=False, server_default='true'),
        sa.Column('is_featured', sa.Boolean(), nullable=False, server_default='false'),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.Column('updated_at', sa.DateTime(), nullable=False),
        sa.ForeignKeyConstraint(['restaurant_id'], ['restaurants.id'], ondelete='CASCADE'),
        sa.ForeignKeyConstraint(['category_id'], ['categories.id']),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_menu_items_restaurant_id', 'menu_items', ['restaurant_id'])

    # 6. orders
    op.create_table(
        'orders',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('order_number', sa.String(length=50), nullable=False),
        sa.Column('customer_id', sa.Integer(), nullable=False),
        sa.Column('restaurant_id', sa.Integer(), nullable=False),
        sa.Column('delivery_partner_id', sa.Integer(), nullable=True),
        sa.Column('delivery_address_id', sa.Integer(), nullable=False),
        sa.Column('subtotal', sa.Float(), nullable=False),
        sa.Column('delivery_fee', sa.Float(), nullable=False, server_default='0.0'),
        sa.Column('taxes', sa.Float(), nullable=False, server_default='0.0'),
        sa.Column('discount', sa.Float(), nullable=False, server_default='0.0'),
        sa.Column('platform_fee', sa.Float(), nullable=False, server_default='5.0'),
        sa.Column('tip', sa.Float(), nullable=False, server_default='0.0'),
        sa.Column('total_amount', sa.Float(), nullable=False),
        sa.Column('payment_status', sa.String(length=50), nullable=False, server_default='PENDING'),
        sa.Column('order_status', sa.String(length=50), nullable=False, server_default='PLACED'),
        sa.Column('estimated_delivery_time', sa.DateTime(), nullable=True),
        sa.Column('placed_at', sa.DateTime(), nullable=False),
        sa.Column('accepted_at', sa.DateTime(), nullable=True),
        sa.Column('prepared_at', sa.DateTime(), nullable=True),
        sa.Column('picked_up_at', sa.DateTime(), nullable=True),
        sa.Column('delivered_at', sa.DateTime(), nullable=True),
        sa.Column('cancelled_at', sa.DateTime(), nullable=True),
        sa.Column('cancellation_reason', sa.String(length=255), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.Column('updated_at', sa.DateTime(), nullable=False),
        sa.ForeignKeyConstraint(['customer_id'], ['users.id']),
        sa.ForeignKeyConstraint(['restaurant_id'], ['restaurants.id']),
        sa.ForeignKeyConstraint(['delivery_address_id'], ['customer_addresses.id']),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_orders_order_number', 'orders', ['order_number'], unique=True)
    op.create_index('ix_orders_customer_id', 'orders', ['customer_id'])
    op.create_index('ix_orders_restaurant_id', 'orders', ['restaurant_id'])
    op.create_index('ix_orders_created_at', 'orders', ['created_at'])
    op.create_index('ix_orders_order_status', 'orders', ['order_status'])

    # 7. order_items
    op.create_table(
        'order_items',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('order_id', sa.Integer(), nullable=False),
        sa.Column('menu_item_id', sa.Integer(), nullable=True),
        sa.Column('item_name_snapshot', sa.String(length=255), nullable=False),
        sa.Column('quantity', sa.Integer(), nullable=False),
        sa.Column('unit_price', sa.Float(), nullable=False),
        sa.Column('tax', sa.Float(), nullable=False, server_default='0.0'),
        sa.Column('discount', sa.Float(), nullable=False, server_default='0.0'),
        sa.Column('customization_snapshot', sa.Text(), nullable=True),
        sa.Column('total_price', sa.Float(), nullable=False),
        sa.ForeignKeyConstraint(['order_id'], ['orders.id'], ondelete='CASCADE'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index('ix_order_items_order_id', 'order_items', ['order_id'])

def downgrade() -> None:
    op.drop_table('order_items')
    op.drop_table('orders')
    op.drop_table('menu_items')
    op.drop_table('categories')
    op.drop_table('restaurants')
    op.drop_table('customer_addresses')
    op.drop_table('users')
