package com.picpay.desafio.transaction;

import com.picpay.desafio.authorization.AuthorizedService;
import com.picpay.desafio.notification.NotificationService;
import com.picpay.desafio.wallet.Wallet;
import com.picpay.desafio.wallet.WalletRepository;
import com.picpay.desafio.wallet.WalletType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizedService authorizedService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository, AuthorizedService authorizedService,
                              NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizedService = authorizedService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        // 1 - validar
        validate(transaction);

        // 2 - criar a transação
        var newTransaction = transactionRepository.save(transaction);

        // 3 - debitar da carteira
        var walletPayer = walletRepository.findById(transaction.payer()).get();
        var walletPayee = walletRepository.findById(transaction.payee()).get();
        walletRepository.save(walletPayer.debit(transaction.value()));
        walletRepository.save(walletPayee.credit(transaction.value()));


        // 4 - chamar serviços externos
        // autorizar transação
        authorizedService.authorize(transaction);

        // notificação
        notificationService.notify(transaction);

        return newTransaction;
    }

    //               REGRAS
    // Se pagador tem uma carteira tipo comum.
    // Se pagador tem saldo suficiente.
    // O pagador não pode ser o recebedor.
    private void validate(Transaction transaction) {
        walletRepository.findById(transaction.payee())
            .map(payee -> walletRepository.findById(transaction.payer())
                .map(payer -> isTransactionValid(transaction, payer) ? transaction : null)
                    .orElseThrow(() -> new InvalidTransactionException("Invalid transaction -%s".formatted(transaction))))
                .orElseThrow(() -> new InvalidTransactionException("Invalid transaction -%s".formatted(transaction)));
    }

    private boolean isTransactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());
    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}
