/*
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2023 Ruhr University Bochum, Paderborn University, Technology Innovation Institute, and Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.protocol.parser;

import de.rub.nds.tlsattacker.core.constants.ChangeCipherSpecByteLength;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessageParser;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChangeCipherSpecParser extends ProtocolMessageParser<ChangeCipherSpecMessage> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Constructor for the Parser class
     *
     * @param stream
     */
    public ChangeCipherSpecParser(InputStream stream) {
        super(stream);
    }

    @Override
    public void parse(ChangeCipherSpecMessage message) {
        LOGGER.debug("Parsing ChangeCipherSpecMessage");
        parseCcsProtocolType(message);
        message.setCompleteResultingMessage(getAlreadyParsed());
    }

    /**
     * Reads the next bytes as the CcsProtocolType and writes them in the message
     *
     * @param msg Message to write in
     */
    private void parseCcsProtocolType(ChangeCipherSpecMessage msg) {
        msg.setCcsProtocolType(parseByteArrayField(ChangeCipherSpecByteLength.TYPE_LENGTH));
        LOGGER.debug("CcsProtocolType: {}", msg.getCcsProtocolType().getValue());
    }
}
