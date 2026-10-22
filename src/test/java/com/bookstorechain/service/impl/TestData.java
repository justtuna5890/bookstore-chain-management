package com.bookstorechain.service.impl;

import com.bookstorechain.entity.Book;
import com.bookstorechain.entity.BookCopy;
import com.bookstorechain.entity.Cart;
import com.bookstorechain.entity.CartItem;
import com.bookstorechain.entity.Store;
import com.bookstorechain.entity.User;
import com.bookstorechain.enums.BookCondition;
import com.bookstorechain.enums.CopyStatus;
import com.bookstorechain.enums.Role;

import java.math.BigDecimal;

/** Dữ liệu dựng sẵn cho các test đơn vị của giỏ hàng và đặt hàng. */
final class TestData {

    private TestData() {
    }

    static User customer(long id) {
        User user = User.builder().username("customer" + id).fullName("Khach " + id).role(Role.CUSTOMER).build();
        user.setId(id);
        return user;
    }

    static Store store(long id, String name) {
        Store store = Store.builder().name(name).address("So " + id + " Pho Hue").phone("0900000000").build();
        store.setId(id);
        return store;
    }

    static Book book(long id, String title) {
        Book book = Book.builder().title(title).author("Tac gia").build();
        book.setId(id);
        return book;
    }

    static BookCopy copy(long id, String code, String price, Store store, Book book, CopyStatus status) {
        BookCopy copy = BookCopy.builder()
                .copyCode(code)
                .bookCondition(BookCondition.GOOD)
                .price(new BigDecimal(price))
                .status(status)
                .store(store)
                .book(book)
                .build();
        copy.setId(id);
        return copy;
    }

    static Cart cart(long id, User owner) {
        Cart cart = Cart.builder().user(owner).build();
        cart.setId(id);
        return cart;
    }

    static CartItem cartItem(Cart cart, BookCopy copy) {
        return CartItem.builder().cart(cart).copy(copy).build();
    }
}
