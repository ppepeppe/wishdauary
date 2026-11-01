-- Preset gifts were sample data. Keep gift request snapshots and clover history intact.
delete from wish_goals
where product_id in (select id from shop_products where source = 'PRESET');

delete from shop_products where source = 'PRESET';
