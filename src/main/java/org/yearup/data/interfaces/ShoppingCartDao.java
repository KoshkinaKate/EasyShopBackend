package org.yearup.data.interfaces;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    ShoppingCart addProductToCart (int userId, int productId, int quantity);
    void updateProductInCart (int userId, int productId, int quantity);
    void removeItemsCart (int userId);


}
