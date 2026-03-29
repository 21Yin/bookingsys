package com.testing.bookingsys.service;

import com.testing.bookingsys.entity.AppUser;
import com.testing.bookingsys.entity.ClassSchedule;
import com.testing.bookingsys.entity.CreditTransaction;
import com.testing.bookingsys.entity.PurchasedPackage;
import com.testing.bookingsys.enums.CreditTransactionType;
import com.testing.bookingsys.repository.CreditTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditLedgerService {

    private final CreditTransactionRepository creditTransactionRepository;

    public void record(
            AppUser user,
            PurchasedPackage purchasedPackage,
            ClassSchedule schedule,
            CreditTransactionType type,
            Integer delta,
            Integer balanceAfter,
            String remarks
    ) {
        creditTransactionRepository.save(CreditTransaction.builder()
                .user(user)
                .purchasedPackage(purchasedPackage)
                .schedule(schedule)
                .type(type)
                .deltaCredit(delta)
                .balanceAfter(balanceAfter)
                .remarks(remarks)
                .build());
    }
}
