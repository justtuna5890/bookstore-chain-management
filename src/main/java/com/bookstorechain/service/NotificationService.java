package com.bookstorechain.service;

import com.bookstorechain.entity.BuybackRequest;
import com.bookstorechain.entity.StockTransfer;

public interface NotificationService {
    void notifyNewBuyback(BuybackRequest request);
    void notifyNewStockTransfer(StockTransfer transfer);
}
