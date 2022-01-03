/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.protocol.message;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PskDheServerKeyExchangeMessageTest {

    PskDheServerKeyExchangeMessage message;

    @Before
    public void setUp() {
        message = new PskDheServerKeyExchangeMessage();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of toString method, of class PskDheServerKeyExchangeMessage.
     */
    @Test
    public void testToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PskDheServerKeyExchangeMessage:");
        sb.append("\n  Modulus p: ").append("null");
        sb.append("\n  Generator g: ").append("null");
        sb.append("\n  Public Key: ").append("null");

        assertEquals(message.toString(), sb.toString());
    }
}
