package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.interfaces.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlCartDao extends MySqlDaoBase implements ShoppingCartDao {

    @Autowired
    public MySqlCartDao(DataSource dataSource) {
        super(dataSource);
    }
    // GET: Retrieve the shopping cart for the current user
    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        String sql = "SELECT * FROM shopping_cart " +
                "JOIN products ON products.product_id = shopping_cart.product_id " +
                "WHERE user_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    shoppingCart.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return shoppingCart;
    }

    @Override
    public ShoppingCart addProductToCart(int userId, int productId, int quantity) {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.setInt(4, quantity);

            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Failed to add product to cart");
            }

            return getByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProductInCart(int userId, int productId, int quantity) {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No cart item found for user: " + userId + ", product: " + productId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating cart product for user: " + userId, e);
        }
    }

    @Override
    public void removeItemsCart(int userId) {
    }

    private ShoppingCartItem mapRow(ResultSet row) throws SQLException {
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        String imageUrl = row.getString("image_url");
        int stock = row.getInt("stock");
        boolean featured = row.getBoolean("featured");

        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(new Product(productId, name, price, categoryId, description, color, stock, featured, imageUrl));
        item.setQuantity(quantity);

        return item;
    }
}
