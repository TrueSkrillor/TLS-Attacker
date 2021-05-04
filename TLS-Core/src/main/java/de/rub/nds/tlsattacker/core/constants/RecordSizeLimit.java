/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.constants;

public class RecordSizeLimit {
    public static final Integer MIN_RECORD_SIZE_LIMIT = 64;
    /**
     * RecordSizeLimit is uint16. TODO: decide if it would be interesting to go out of bounds here. that would also need
     * some tweaking around the basic classes as they have to support byte lengths > 2
     */
    public static final Integer MAX_RECORD_SIZE_LIMIT = 65535;
}
