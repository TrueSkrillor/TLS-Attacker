/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2022 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package de.rub.nds.tlsattacker.core.protocol.parser.extension;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.protocol.message.extension.GreaseExtensionMessage;
import java.io.InputStream;

public class GreaseExtensionParser extends ExtensionParser<GreaseExtensionMessage> {

    public GreaseExtensionParser(InputStream stream, Config config) {
        super(stream, config);
    }

    @Override
    public void parseExtensionMessageContent(GreaseExtensionMessage msg) {
        msg.setRandomData(parseByteArrayField(getBytesLeft()));
        msg.setData(msg.getRandomData().getValue());
    }
}
