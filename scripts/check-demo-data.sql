USE test;

SELECT COUNT(*) AS order_count FROM orders;

SELECT SUM(amount) AS total_sales FROM orders;

SELECT u.name, SUM(o.amount) AS total_sales
FROM users u
JOIN orders o ON o.user_id = u.id
GROUP BY u.name;
