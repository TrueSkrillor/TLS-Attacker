/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UnknownHandshakeHandlerTest {

    private UnknownHandshakeHandler handler;
    private TlsContext context;

    @Before
    public void setUp() {
        context = new TlsContext();
        handler = new UnknownHandshakeHandler(context);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of adjustContext method, of class UnknownHandshakeHandler.
     */
    @Test
    public void testadjustContext() {
    }
}
