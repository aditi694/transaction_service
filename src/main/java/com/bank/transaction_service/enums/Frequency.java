package com.bank.transaction_service.enums;

import java.time.LocalDate;

public enum Frequency {
    DAILY {
        @Override
        public LocalDate next(LocalDate current) {
            return current.plusDays(1);
        }
    },
    WEEKLY {
        @Override
        public LocalDate next(LocalDate current) {
            return current.plusWeeks(1);
        }
    },
    MONTHLY {
        @Override
        public LocalDate next(LocalDate current) {
            return current.plusMonths(1);
        }
    },
    QUARTERLY {
        @Override
        public LocalDate next(LocalDate current) {
            return current.plusMonths(3);
        }
    },
    YEARLY {
        @Override
        public LocalDate next(LocalDate current) {
            return current.plusYears(1);
        }
    };

    public abstract LocalDate next(LocalDate current);
}