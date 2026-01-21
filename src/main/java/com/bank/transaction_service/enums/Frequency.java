package com.bank.transaction_service.enums;

import java.time.LocalDate;

public enum Frequency {
    DAILY {
        @Override
        public LocalDate next(LocalDate date) {
            return date.plusDays(1);
        }
    },
    WEEKLY {
        @Override
        public LocalDate next(LocalDate date) {
            return date.plusWeeks(1);
        }
    },
    MONTHLY {
        @Override
        public LocalDate next(LocalDate date) {
            return date.plusMonths(1);
        }
    },
    YEARLY {
        @Override
        public LocalDate next(LocalDate date) {
            return date.plusYears(1);
        }
    };

    public abstract LocalDate next(LocalDate date);
}
