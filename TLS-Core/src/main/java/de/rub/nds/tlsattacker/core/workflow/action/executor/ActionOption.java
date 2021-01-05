/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2020 Ruhr University Bochum, Paderborn University,
 * and Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package de.rub.nds.tlsattacker.core.workflow.action.executor;

public enum ActionOption {
    EARLY_CLEAN_SHUTDOWN,
    IGNORE_UNEXPECTED_NEW_SESSION_TICKETS,
    IGNORE_UNEXPECTED_WARNINGS,
    MAY_FAIL,
    CHECK_ONLY_EXPECTED;
}
