package com.pos.service;

import com.pos.dto.CustomerDebtResponse;
import com.pos.dto.DebtPaymentRequest;
import com.pos.dto.DebtPaymentResponse;
import java.util.List;

public interface DebtService {

    /** Semua pelanggan yang masih punya hutang, dikelompokkan per pelanggan. */
    List<CustomerDebtResponse> listAllDebts();

    /** Catat pelunasan (parsial atau penuh) untuk satu transaksi. */
    DebtPaymentResponse pay(DebtPaymentRequest request);
}
